package ca.thewalls;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class EconomyHook {
    private static Economy economy;

    private EconomyHook() {}

    public static boolean setup() {
        try {
            if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
                return false;
            }
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            economy = rsp.getProvider();
            return economy != null;
        } catch (Exception ex) {
            return false;
        }
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static boolean isAvailable() {
        return economy != null;
    }
}
