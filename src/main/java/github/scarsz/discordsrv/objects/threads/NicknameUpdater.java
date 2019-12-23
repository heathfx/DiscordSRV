package github.scarsz.discordsrv.objects.threads;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;
import github.scarsz.discordsrv.util.PluginUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class NicknameUpdater extends Thread {

    public NicknameUpdater() {
        setName("DiscordSRV - Nickname updater");
    }

    @Override
    public void run() {
        while (true) {
            int rate = DiscordSRV.config().getInt("ChannelTopicUpdaterRateInMinutes");
            if (rate < 3) rate = 3;

            if (DiscordSRV.config().getBoolean("NicknameSynchronizationEnabled")) {
                DiscordSRV.debug("Synchronizing nicknames...");
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    String userId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(onlinePlayer.getUniqueId());
                    if (userId == null) continue;

                    User linkedUser = DiscordUtil.getJda().getUserById(userId);
                    if (linkedUser == null) continue;

                    Member member = DiscordSRV.getPlugin().getMainGuild().getMember(linkedUser);
                    if (member == null) {
                        DiscordSRV.debug(linkedUser + " is not in the Main guild, not setting nickname");
                        continue;
                    }

                    setNickname(member, onlinePlayer);
                }
            }

            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(rate));
            } catch (InterruptedException ignored) {
                DiscordSRV.warning("Broke from Nickname Updater thread: sleep interrupted");
                return;
            }
        }
    }

    public void setNickname(Member member, OfflinePlayer offlinePlayer) {
        String nickname;
        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();

            nickname = DiscordSRV.config().getString("NicknameSynchronizationFormat")
                    .replace("%displayname%", player.getDisplayName() != null ? player.getDisplayName() : player.getName())
                    .replace("%username%", player.getName());

            if (PluginUtil.pluginHookIsEnabled("placeholderapi"))
                nickname = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, nickname);
        } else {
            nickname = offlinePlayer.getName();
        }

        nickname = DiscordUtil.strip(nickname);
        DiscordUtil.setNickname(member, nickname);
    }
}