package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.Permission;
import util.Config;

public class LogCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (!e.isFromGuild()) return;

        Member member = e.getMember();
        if (member == null || !member.hasPermission(Permission.ADMINISTRATOR)) {
            e.getChannel().sendMessage("❌ 이 명령어는 관리자만 사용할 수 있습니다.").queue();
            return;
        }

        String msg = e.getMessage().getContentRaw();
        if (!msg.startsWith("!로그채널설정") && !msg.startsWith("!로그레벨설정") && !msg.startsWith("!로그레벨삭제")) {
            return;
        }

        String[] args = msg.split(" ");
        String command = args[0];

        switch (command) {
            case "!로그채널설정":
                if (args.length < 2) {
                    e.getChannel().sendMessage("❌ 사용법: `!로그채널설정 [채널ID]`").queue();
                    return;
                }
                TextChannel target = e.getGuild().getTextChannelById(args[1]);
                if (target == null) {
                    e.getChannel().sendMessage("❌ 존재하지 않는 채널 ID입니다.").queue();
                    return;
                }
                Config.set("logChannelId", args[1]);
                e.getChannel().sendMessage("✅ 로그 채널이 `" + target.getName() + "` 으로 설정되었습니다.").queue();
                break;

            case "!로그레벨설정":
                if (args.length < 2) {
                    e.getChannel().sendMessage("❌ 사용법: `!로그레벨설정 [레벨]`").queue();
                    return;
                }
                // 여러 로그레벨을 콤마(,)로 구분해서 받는다고 가정
                Config.set("logLevels", args[1]);
                e.getChannel().sendMessage("✅ 로그 레벨이 `" + args[1] + "` 으로 설정되었습니다.").queue();
                break;

            case "!로그레벨삭제":
                Config.remove("logLevels");
                e.getChannel().sendMessage("✅ 로그 레벨 설정이 삭제되었습니다.").queue();
                break;
        }
    }
}
