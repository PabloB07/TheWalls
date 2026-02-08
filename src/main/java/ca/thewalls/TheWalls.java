package ca.thewalls;

import org.bukkit.plugin.java.JavaPlugin;

import ca.thewalls.Commands.*;
import ca.thewalls.Listeners.*;

public final class TheWalls extends JavaPlugin {

    public ArenaManager arenas;
    public com.samjakob.spigui.SpiGUI spigui;
    public TopHolograms topHolograms;

    @Override
    public void onEnable() {
        // Setup/Check for config file
        Config.initializeData();
        Messages.initialize(this);
        // No special init needed for copy-based reset
        spigui = new com.samjakob.spigui.SpiGUI(this);
        arenas = new ArenaManager(this);
        // No default arena: all arenas are explicit and created via config/commands.

        // Health in tablist is enabled only during active games.
        topHolograms = new TopHolograms(this);

        // Register commands
        this.getCommand("walls").setExecutor(new WallsInfo(this));
        this.getCommand("walls").setTabCompleter(new ca.thewalls.Commands.WallsCompleter(this));

        // Register Listeners
        this.getServer().getPluginManager().registerEvents(new PlayerLeave(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerAttack(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerRespawn(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerDeath(this), this);
        this.getServer().getPluginManager().registerEvents(new EntityDeath(this), this);
        this.getServer().getPluginManager().registerEvents(new EntityDamage(this), this);
        this.getServer().getPluginManager().registerEvents(new JoinSign(this), this);
        this.getServer().getPluginManager().registerEvents(new LobbyProtection(this), this);
        this.getServer().getPluginManager().registerEvents(new LobbyItemsListener(this), this);

        // Lobby tick: update lobby boards
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            if (arenas == null) return;
            for (Arena arena : arenas.getArenas().values()) {
                arena.getGame().updateLobbyBoards();
            }
        }, 20L, 20L);

        // Sign tick: update dynamic signs
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            if (arenas == null) return;
            SignUpdater.updateAll(this);
        }, 40L, 40L);

        // Hologram tick: keep top hologram synced
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            if (topHolograms == null || !topHolograms.isAvailable()) return;
            topHolograms.refresh();
        }, 20L, 20L);
    }

    @Override
    public void onDisable() {
        if (arenas != null) {
            for (Arena arena : arenas.getArenas().values()) {
                if (arena.getGame().started) {
                    arena.getGame().end(true, null);
                }
            }
        }
        if (topHolograms != null) {
            topHolograms.shutdown();
        }
    }

    public Arena getArenaByPlayer(org.bukkit.entity.Player player) {
        return arenas == null ? null : arenas.getArenaByPlayer(player);
    }
}
