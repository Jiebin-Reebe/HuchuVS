package tools;

import user.User;
import java.util.*;

public class BalanceMaker {

    public String balance(List<User> Users) {
        if (Users.size() != 10) {
            return "⚠️ 정확히 10명이 등록되어야 합니다. 현재: " + Users.size() + "명";
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
            return "❌ 라인 중복 없이 팀을 구성할 수 없습니다. 각 라인당 2명씩 등록되어야 합니다.";
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

        return result.toString();
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
}
