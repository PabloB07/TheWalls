package ca.thewalls;

import fr.traqueur.currencies.Currencies;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public final class EconomyService {
    private static String provider = "vault";
    private static Currencies currency = null;
    private static boolean currencyAvailable = false;

    private EconomyService() {}

    public static void setup() {
        if (Config.data != null) {
            provider = Config.data.getString("economy.provider", "vault").toLowerCase();
        }
        if (provider.equals("currenciesapi")) {
            try {
                String id = Config.data.getString("economy.currency", "VAULT");
                currency = Currencies.valueOf(id.toUpperCase());
                // If currency uses VaultProvider, require Vault to be present
                if (id.equalsIgnoreCase("VAULT") && Bukkit.getPluginManager().getPlugin("Vault") == null) {
                    currencyAvailable = false;
                } else {
                    currencyAvailable = currency != null;
                }
            } catch (Exception ex) {
                currency = null;
                currencyAvailable = false;
            }
        } else {
            currency = null;
            currencyAvailable = false;
            EconomyHook.setup();
        }
    }

    public static boolean isAvailable() {
        if (provider.equals("currenciesapi")) {
            return currency != null && currencyAvailable;
        }
        return EconomyHook.isAvailable();
    }

    public static double getBalance(Player player) {
        if (player == null) return 0.0;
        if (provider.equals("currenciesapi")) {
            if (!currencyAvailable || currency == null) return 0.0;
            try {
                return currency.getBalance(player).doubleValue();
            } catch (Throwable ex) {
                currencyAvailable = false;
                return 0.0;
            }
        }
        Economy eco = EconomyHook.getEconomy();
        return eco == null ? 0.0 : eco.getBalance(player);
    }

    public static boolean withdraw(Player player, double amount) {
        if (player == null) return false;
        if (provider.equals("currenciesapi")) {
            if (!currencyAvailable || currency == null) return false;
            try {
                currency.withdraw(player, BigDecimal.valueOf(amount));
                return true;
            } catch (Throwable ex) {
                currencyAvailable = false;
                return false;
            }
        }
        Economy eco = EconomyHook.getEconomy();
        if (eco == null) return false;
        return eco.withdrawPlayer(player, amount).transactionSuccess();
    }

    public static String format(double amount) {
        if (provider.equals("currenciesapi")) {
            if (!currencyAvailable || currency == null) {
                return amount % 1 == 0 ? String.valueOf((int) amount) : String.valueOf(amount);
            }
            return amount % 1 == 0 ? String.valueOf((int) amount) : String.valueOf(amount);
        }
        Economy eco = EconomyHook.getEconomy();
        if (eco == null) return String.valueOf(amount);
        return eco.format(amount);
    }
}
