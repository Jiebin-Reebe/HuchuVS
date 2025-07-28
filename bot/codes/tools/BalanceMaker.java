package tools;

import user.User;

import java.util.*;

public class BalanceMaker {
    public String balance(List<User> users) {
        if (users.size() != 10) {
            return "⚠️ 정확히 10명이 등록되어야 합니다. 현재: " + users.size() + "명";
        }

        List<List<User>> combinations = generateCombinations(users, 5);
        double minPenalty = Double.MAX_VALUE;
        List<User> bestTeamA = null;
        List<User> bestTeamB = null;

        for (List<User> teamA : combinations) {
            List<User> teamB = new ArrayList<>(users);
            teamB.removeAll(teamA);

            // 둘 다 유효한 팀인지 확인
            if (!isValidTeam(teamA) || !isValidTeam(teamB)) continue;

            double scoreA = getTeamScore(teamA);
            double scoreB = getTeamScore(teamB);
            double diff = Math.abs(scoreA - scoreB);
            double variance = getTeamVariance(teamA) + getTeamVariance(teamB);
            int uniqueRoles = countUniqueRoles(teamA) + countUniqueRoles(teamB);

            // 최종 평가 점수: diff + 분산 - 라인 다양성 보너스
            double totalPenalty = diff + 0.5 * variance - 0.2 * uniqueRoles;

            if (totalPenalty < minPenalty) {
                minPenalty = totalPenalty;
                bestTeamA = new ArrayList<>(teamA);
                bestTeamB = new ArrayList<>(teamB);
            }
        }

        if (bestTeamA == null || bestTeamB == null) {
            return "❌ 라인 중복 없이 팀을 구성할 수 없습니다. 각 라인당 2명씩 등록되어야 합니다.";
        }

        StringBuilder result = new StringBuilder("🎯 **개선된 밸런스 결과** (점수 차이 기준 + 내부 분산 고려)\n\n");
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

    // 기존 함수들 (수정 없이 그대로 사용)
    private double getTeamScore(List<User> team) {
        return team.stream().mapToDouble(p -> p.score).sum();
    }

    private double getTeamVariance(List<User> team) {
        double avg = getTeamScore(team) / team.size();
        return team.stream().mapToDouble(p -> Math.pow(p.score - avg, 2)).average().orElse(Double.MAX_VALUE);
    }

    private int countUniqueRoles(List<User> team) {
        return (int) team.stream().map(u -> u.role.toLowerCase()).distinct().count();
    }

    private boolean isValidTeam(List<User> team) {
        Set<String> roles = new HashSet<>();
        for (User u : team) {
            String role = u.role.toLowerCase();
            if (roles.contains(role)) return false;
            roles.add(role);
        }
        return true;
    }

    private List<List<User>> generateCombinations(List<User> users, int k) {
        List<List<User>> result = new ArrayList<>();
        generateHelper(users, new ArrayList<>(), 0, k, result);
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
