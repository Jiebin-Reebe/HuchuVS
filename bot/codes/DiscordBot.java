import commands.*;
import managers.*;
import music.*;
import tools.*;
import user.*;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.sql.SQLException;
import java.util.EnumSet;

public class DiscordBot {

    public static void main(String[] args) throws SQLException {
        MessageStatsManager db = new MessageStatsManager("message_stats.db");
        XpSystem xpSystem = new XpSystem(db);
        UserQueue queue = new UserQueue();

        BotTokenManager tokenManager = new BotTokenManager();
        String token = tokenManager.getDiscordBotToken();

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES
        );

        JDABuilder builder = JDABuilder.createDefault(token)
                .enableIntents(intents)
                // 상메
                .setActivity(Activity.customStatus("츄르 먹는중..."))
                // 이벤트 리스너 ()
                .addEventListeners(
                        xpSystem,

                        new MessageXpCommand(xpSystem, db),
                        new MusicCommand(),
                        new RoleGiverCommand(),
                        new ChattingReaction(),
                        new SlashCommandReaction(),
                        new BalanceCommand(queue),

                        new RoleButtonListener(),
                        new RoleSelectMenuListener(),
                        new MessageHistoryScanner(db),
                        new MessageTracker(db)
                );
        var jda = builder.build();

        //JVM 종료시 음성 채널에서 나감
        ShutdownManager.registerShutdownHook(jda);
    }
}