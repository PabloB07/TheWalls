package ca.thewalls.Listeners;

import ca.thewalls.TheWalls;
import org.bukkit.entity.Player;

public class CosmeticsMenu {
    private CosmeticsMenu() {}

    public static void openMain(TheWalls plugin, Player player) {
        LoadoutMenu.openCosmetics(plugin, player);
    }

    public static void openTrails(TheWalls plugin, Player player) {
        LoadoutMenu.openTrails(plugin, player);
    }

    public static void openKillEffects(TheWalls plugin, Player player) {
        LoadoutMenu.openKillEffects(plugin, player);
    }
}
