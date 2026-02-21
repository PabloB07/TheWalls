package ca.thewalls.Listeners;

import ca.thewalls.TheWalls;
import org.bukkit.entity.Player;

public class KitSelectMenu {
    private KitSelectMenu() {}

    public static void open(TheWalls plugin, Player player) {
        LoadoutMenu.openKits(plugin, player);
    }
}
