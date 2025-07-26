package bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer audioPlayer;
    private final BlockingQueue<AudioTrack> queue;
    private GuildMessageChannel textChannel;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> leaveTask = null;

    private boolean repeat = false;
    private List<AudioTrack> history = new ArrayList<>();

    public TrackScheduler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void toggleRepeat() {
        this.repeat = !this.repeat;
    }

    public boolean isRepeatEnabled() {
        return this.repeat;
    }

    public void setTextChannel(GuildMessageChannel channel) {
        this.textChannel = channel;
    }

    public void queue(AudioTrack track) {
        if (!this.audioPlayer.startTrack(track, true)) {
            this.queue.offer(track);
            history.add(track.makeClone());
            if (textChannel != null) {
                textChannel.sendMessage("🎵 '" + track.getInfo().title + "' 곡이 재생목록에 추가됐다냥").queue();
            }
        }
        cancelLeaveTask();
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        System.out.println("▶ 새로운 곡을 틀겠다냥: " + track.getInfo().title);
        cancelLeaveTask();
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        System.out.println("⏹️ 곡이 종료됐다냥: " + track.getInfo().title + " / 이유: " + endReason);

        if (endReason.mayStartNext) {
            nextTrack();
        }

        if (player.getPlayingTrack() == null && queue.isEmpty() && textChannel != null) {
            // 🔁 반복 재생 모드인 경우 history에서 다시 큐 채움
            if (repeat && !history.isEmpty()) {
                for (AudioTrack t : history) {
                    queue.offer(t.makeClone());
                }
                textChannel.sendMessage("🔁 반복 재생 중이라서 대기열을 다시 채웠다냥!").queue();
                nextTrack();
                return;
            }

            // 🕒 자동 퇴장 예약
            if (leaveTask != null && !leaveTask.isDone()) {
                leaveTask.cancel(false);
            }

            leaveTask = scheduler.schedule(() -> {
                Guild guild = textChannel.getGuild();
                AudioManager audioManager = guild.getAudioManager();

                if (audioManager.isConnected()) {
                    textChannel.sendMessage("3분 동안 아무 노래도 없어서 나간다냥 🕒👋").queue();
                    audioManager.closeAudioConnection();
                }
            }, 3, TimeUnit.MINUTES);
        }
    }


    public void nextTrack() {
        this.audioPlayer.startTrack(this.queue.poll(), false);
    }

    private void cancelLeaveTask() {
        if (leaveTask != null && !leaveTask.isDone()) {
            System.out.println("🛑 자동 퇴장 예약 취소됐다냥");
            leaveTask.cancel(false);
        }
    }

    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void clearQueue() {
        queue.clear();
    }
}