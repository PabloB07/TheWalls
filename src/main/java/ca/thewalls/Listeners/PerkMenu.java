package ca.thewalls.Listeners;

import ca.thewalls.TheWalls;
import org.bukkit.entity.Player;

public class PerkMenu {
    private PerkMenu() {}

    public static void open(TheWalls plugin, Player player) {
        LoadoutMenu.openPerks(plugin, player);
    }
}
