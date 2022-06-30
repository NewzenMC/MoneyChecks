package fr.newzen.plugins.moneychecks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MoneyChecks extends JavaPlugin {

    private static Economy economy = null;

    public void onEnable() {
        setupEconomy();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        assert rsp != null; // Assert because if Vault is not there, Spigot won't load this plugin (depend in plugin.yml)
        economy = rsp.getProvider();
    }

    public static Economy getEconomy() {
        return economy;
    }
}
