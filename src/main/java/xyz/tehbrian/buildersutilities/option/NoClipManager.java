package xyz.tehbrian.buildersutilities.option;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.tehbrian.buildersutilities.BuildersUtilities;
import xyz.tehbrian.buildersutilities.Constants;
import xyz.tehbrian.buildersutilities.user.User;
import xyz.tehbrian.buildersutilities.user.UserService;

@SuppressWarnings("deprecation") // no alternative to Player#isOnGround
public final class NoClipManager {

    private final BuildersUtilities main;
    private final UserService userService;

    @Inject
    public NoClipManager(
            final @NonNull BuildersUtilities main,
            final @NonNull UserService userService
    ) {
        this.main = main;
        this.userService = userService;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(this.main, this::checkForBlocks, 1L, 1L);
    }

    private void checkForBlocks() {
        for (final User user : this.userService.getUserMap().values()) {
            if (!user.noClipEnabled()) {
                continue;
            }

            final Player p = user.getPlayer();
            if (p == null || !p.isOnline() || !p.hasPermission(Constants.Permissions.NO_CLIP)) {
                continue;
            }

            final boolean noClip;
            boolean tp = false;
            if (p.getGameMode() == GameMode.CREATIVE) {
                if (p.isOnGround() && p.isSneaking()) {
                    noClip = true;
                } else {
                    noClip = this.shouldNoClip(p);
                    if (p.isOnGround()) {
                        tp = true;
                    }
                }

                if (noClip) {
                    p.setGameMode(GameMode.SPECTATOR);
                    if (tp) {
                        p.teleport(p.getLocation());
                    }
                }
            } else if (p.getGameMode() == GameMode.SPECTATOR) {
                if (p.isOnGround()) {
                    noClip = true;
                } else {
                    noClip = this.shouldNoClip(p);
                }

                if (!noClip) {
                    p.setGameMode(GameMode.CREATIVE);
                }
            }
        }
    }

    private boolean shouldNoClip(final Player p) {
        return p.getLocation().add(+0.4, 0, 0).getBlock().getType().isSolid()
                || p.getLocation().add(-0.4, 0, 0).getBlock().getType().isSolid()
                || p.getLocation().add(0, 0, +0.4).getBlock().getType().isSolid()
                || p.getLocation().add(0, 0, -0.4).getBlock().getType().isSolid()
                || p.getLocation().add(+0.4, 1, 0).getBlock().getType().isSolid()
                || p.getLocation().add(-0.4, 1, 0).getBlock().getType().isSolid()
                || p.getLocation().add(0, 1, +0.4).getBlock().getType().isSolid()
                || p.getLocation().add(0, 1, -0.4).getBlock().getType().isSolid()
                || p.getLocation().add(0, +1.9, 0).getBlock().getType().isSolid();
    }

}
