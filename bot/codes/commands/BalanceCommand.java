package commands;

import user.User;
import user.UserQueue;
import tools.BalanceMaker;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BalanceCommand extends ListenerAdapter {
    private final UserQueue queue;
    private final BalanceMaker balancer = new BalanceMaker();

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
        String result = balancer.balance(queue.getAll());
        event.getChannel().sendMessage(result).queue();
        queue.clear();
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
            case "탑": 
            case "ㅌ":
                return "top";

            case "정글":
            case "ㅈㄱ":
                return "jungle";

            case "미드":
            case "ㅁㄷ":
                return "mid";

            case "원딜": 
            case "바텀": 
            case "ㅇㄷ":
            case "ㅂㅌ":
                return "bot";

            case "서폿": 
            case "서포터": 
            case "ㅅㅍ":
                return "support";

            default: return input.toLowerCase();
        }
    }
}
