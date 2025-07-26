package commands;

import user.User;
import user.UserQueue;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BalanceCommand extends ListenerAdapter {
    private final UserQueue queue;

    public BalanceCommand(UserQueue queue) {
        this.queue = queue;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();
        if (!content.startsWith("!")) return;

        String[] args = content.split(" ");
        String command = args[0];

        switch (command.toLowerCase()) {
            case "!내전추가":
            case "!내전ㅊㄱ":
                handleRegisterCommand(event, args);
                break;

            case "!밸런스":
                handleBalanceCommand(event);
                break;

            case "!내전삭제":
            case "!내전ㅅㅈ":
                handleRemoveCommand(event, args);
                break;

            case "!라인부족":
            case "!ㄹㅇㅂㅈ":
                handleRoleCheckCommand(event);
                break;

            case "!초기화":
            case "!ㅊㄱㅎ":
                handleClearCommand(event);
                break;

            case "!리스트":
                handleListCommand(event);
                break;
        }
    }

    private void handleRegisterCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 4) {
            event.getChannel().sendMessage("❌ 사용법: `!내전추가 / !내전ㅊㄱ[이름] [라인] [점수]`").queue();
            return;
        }

        String name = args[1];
        String role = translateRole(args[2]);
        double score;

        try {
            score = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ 점수는 숫자로 입력해 주세요. 예: `!내전추가 / !내전ㅊㄱ Alice Mid 14.5`").queue();
            return;
        }

        if (queue.isRoleFull(role)) {
            event.getChannel().sendMessage("❌ " + capitalize(role) + " 라인은 이미 2명이 등록되어 더 이상 추가할 수 없습니다.").queue();
            return;
        }

        if (queue.exists(name)) {
            event.getChannel().sendMessage("❌ 이미 등록된 이름입니다: " + name).queue();
            return;
        }

        queue.add(new User(name, role, score));
        event.getChannel().sendMessage("✅ " + name + " 님이 " + role + " / " + score + "점 으로 등록되었습니다.").queue();
    }

    private void handleBalanceCommand(MessageReceivedEvent event) {
        List<User> Users = queue.getAll();
        if (Users.size() != 10) {
            event.getChannel().sendMessage("⚠️ 정확히 10명이 등록되어야 합니다. 현재: " + Users.size() + "명").queue();
            return;
        }

        List<List<User>> combinations = generateCombinations(Users, 5);
        double minDiff = Double.MAX_VALUE;
        List<User> bestTeamA = null;
        List<User> bestTeamB = null;

        for (List<User> teamA : combinations) {
            if (!isValidTeam(teamA)) continue;

            List<User> teamB = new ArrayList<>(Users);
            teamB.removeAll(teamA);
            if (!isValidTeam(teamB)) continue;

            double scoreA = getTeamScore(teamA);
            double scoreB = getTeamScore(teamB);
            double diff = Math.abs(scoreA - scoreB);

            if (diff < minDiff) {
                minDiff = diff;
                bestTeamA = new ArrayList<>(teamA);
                bestTeamB = new ArrayList<>(teamB);
            }
        }

        if (bestTeamA == null || bestTeamB == null) {
            event.getChannel().sendMessage("❌ 라인 중복 없이 팀을 구성할 수 없습니다. 각 라인당 2명씩 등록되어야 합니다.").queue();
            return;
        }

        StringBuilder result = new StringBuilder("🎯 **밸런스 결과** (점수 차이: " + String.format("%.2f", minDiff) + ")\n\n");

        result.append("🟥 **Team A**\n");
        for (User p : bestTeamA) {
            result.append(p.name).append(" (").append(p.role).append(", ").append(p.score).append("점)\n");
        }

        result.append("\n🟦 **Team B**\n");
        for (User p : bestTeamB) {
            result.append(p.name).append(" (").append(p.role).append(", ").append(p.score).append("점)\n");
        }

        event.getChannel().sendMessage(result.toString()).queue();
        queue.clear();
    }

    private boolean isValidTeam(List<User> team) {
        Set<String> roles = new HashSet<>();
        for (User u : team) {
            String role = u.role.toLowerCase();
            if (roles.contains(role)) return false;
            roles.add(role);
        }
        return roles.size() == 5;
    }

    private double getTeamScore(List<User> team) {
        return team.stream().mapToDouble(p -> p.score).sum();
    }

    private List<List<User>> generateCombinations(List<User> Users, int k) {
        List<List<User>> result = new ArrayList<>();
        generateHelper(Users, new ArrayList<>(), 0, k, result);
        return result;
    }

    private void generateHelper(List<User> players, List<User> current, int index, int k, List<List<User>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int i = index; i < players.size(); i++) {
            current.add(players.get(i));
            generateHelper(players, current, i + 1, k, result);
            current.remove(current.size() - 1);
        }
    }

    private void handleRemoveCommand(MessageReceivedEvent event, String[] args) {
        if (args.length < 2) {
            event.getChannel().sendMessage("❌ 사용법: `!내전삭제 / !내전ㅅㅈ [이름]`").queue();
            return;
        }

        String nameToRemove = args[1];
        boolean removed = queue.removeByName(nameToRemove);
        if (removed) {
            event.getChannel().sendMessage("✅ `" + nameToRemove + "` 님이 삭제되었습니다.").queue();
        } else {
            event.getChannel().sendMessage("❌ `" + nameToRemove + "` 님은 등록되어 있지 않습니다.").queue();
        }
    }

    private void handleRoleCheckCommand(MessageReceivedEvent event) {
        List<User> users = queue.getAll();
        Map<String, Integer> roleCount = new HashMap<>();

        for (User u : users) {
            String role = u.role.toLowerCase();
            roleCount.put(role, roleCount.getOrDefault(role, 0) + 1);
        }

        StringBuilder result = new StringBuilder("📊 **라인별 등록 현황:**\n");
        String[] roles = {"top", "jungle", "mid", "bot", "support"};
        for (String role : roles) {
            int count = roleCount.getOrDefault(role, 0);
            String lineDisplay = capitalize(role);
            if (count < 2) {
                result.append("⚠️ ").append(lineDisplay).append(": ").append(count).append("명 (부족!)\n");
            } else {
                result.append("✅ ").append(lineDisplay).append(": ").append(count).append("명\n");
            }
        }

        event.getChannel().sendMessage(result.toString()).queue();
    }

    private void handleClearCommand(MessageReceivedEvent event) {
        queue.clear();
        event.getChannel().sendMessageEmbeds(createEmbed("🧹 대기열 초기화", "모든 등록된 플레이어가 삭제되었습니다.")).queue();
    }

    private void handleListCommand(MessageReceivedEvent event) {
        List<User> users = queue.getAll();
        if (users.isEmpty()) {
            event.getChannel().sendMessageEmbeds(createEmbed("📭 등록 리스트", "현재 등록된 플레이어가 없습니다.")).queue();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (User u : users) {
            sb.append("• ").append(u.name).append(" (").append(capitalize(u.role)).append(", ").append(u.score).append("점)\n");
        }

        event.getChannel().sendMessageEmbeds(createEmbed("📋 등록된 플레이어 (" + users.size() + "/10)", sb.toString())).queue();
    }

    private MessageEmbed createEmbed(String title, String description) {
        return new EmbedBuilder().setTitle(title).setDescription(description).setColor(new Color(0, 204, 255)).build();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String translateRole(String input) {
        switch (input.toLowerCase()) {
            case "탑": return "top";
            case "정글": return "jungle";
            case "미드": return "mid";
            case "원딜": case "바텀": return "bot";
            case "서폿": case "서포터": return "support";
            default: return input.toLowerCase();
        }
    }
}
