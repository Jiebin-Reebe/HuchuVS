package managers.LogManager;

import util.Config;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class LogManager {

    private static Set<LogLevel> activeLevels = loadActiveLevels();

    // 로그 레벨 enum
    public enum LogLevel {
        INFO, WARN, ERROR
    }

    private static Set<LogLevel> loadActiveLevels() {
        String rawLevels = Config.getOrDefault("logLevels", "INFO");
        return Arrays.stream(rawLevels.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(l -> {
                    try {
                        LogLevel.valueOf(l);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .map(LogLevel::valueOf)
                .collect(Collectors.toSet());
    }

    // 외부에서 명령어로 레벨 재설정 후 호출 가능
    public static void reloadLevels() {
        activeLevels = loadActiveLevels();
    }

    private static TextChannel getLogChannel(Guild guild) {
        String id = Config.get("logChannelId");
        if (id == null) return null;
        return guild.getTextChannelById(id);
    }

    public static void sendLog(Guild guild, LogLevel info, String title, String description, Color color) {
        if (!activeLevels.contains(info)) return;

        TextChannel ch = getLogChannel(guild);
        if (ch == null) return;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .setTimestamp(Instant.now());

        ch.sendMessageEmbeds(embed.build()).queue();
    }

    // 기본 INFO 레벨로 로그 보내기
    public static void sendLog(Guild guild, String title, String description, Color color) {
        sendLog(guild, LogLevel.INFO, title, description, color);
    }
}
