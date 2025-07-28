package tools;

import managers.LogManager.LogManager;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;

import java.awt.*;

public class ServerLogger extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.INFO, "✅ 새 멤버 입장",
                e.getUser().getAsTag() + " 님이 서버에 들어왔습니다.", Color.GREEN);
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.WARN, "⛔ 멤버 퇴장",
                e.getUser().getAsTag() + " 님이 서버에서 나갔습니다.", Color.ORANGE);
    }

    @Override
    public void onGuildBan(GuildBanEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.ERROR, "🛑 멤버 철퇴",
                e.getUser().getAsTag() + " 님이 서버에서 밴되었습니다.", Color.RED);
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.INFO, "🔖 별명 변경",
                e.getUser().getAsTag() + " 님의 별명 `" + e.getOldNickname() + "` → `" + e.getNewNickname() + "` 으로 변경됨", Color.BLUE);
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.INFO, "➕ 역할 추가",
                e.getUser().getAsTag() + " 님에게 역할 추가됨: " + e.getRoles(), Color.GREEN);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.WARN, "❌ 역할 제거",
                e.getUser().getAsTag() + " 님의 역할 제거됨: " + e.getRoles(), Color.ORANGE);
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.INFO, "📁 채널 생성됨",
                e.getChannel().getName(), Color.GREEN);
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.ERROR, "🗑️ 채널 삭제됨",
                e.getChannel().getName(), Color.RED);
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent e) {
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.INFO, "✏️ 채널 이름 변경됨",
                "`" + e.getOldValue() + "` → `" + e.getNewValue() + "`", Color.BLUE);
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent e) {
        if (!e.isFromGuild()) return;
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.WARN, "🧹 메시지 삭제됨 • 채널 `" + e.getChannel().getName() + "`",
                "메시지 ID `" + e.getMessageId() + "`", Color.ORANGE);
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent e) {
        if (!e.isFromGuild()) return;
        LogManager.sendLog(e.getGuild(), LogManager.LogLevel.INFO, "✏️ 메시지 수정됨 • " + e.getAuthor().getAsTag(),
                "내용이 변경되었습니다 → `" + e.getMessage().getContentRaw() + "`", Color.BLUE);
    }
}
