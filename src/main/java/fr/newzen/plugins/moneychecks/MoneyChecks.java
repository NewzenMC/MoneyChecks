package fr.newzen.plugins.moneychecks;

import fr.newzen.plugins.moneychecks.commands.AdminCheckCommand;
import fr.newzen.plugins.moneychecks.commands.CheckCommand;
import fr.newzen.plugins.moneychecks.listeners.CheckEventsListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MoneyChecks extends JavaPlugin {

    private Economy economy = null;
    private FileConfiguration config = null;
    private CheckUtils checkUtils = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        if (!Objects.equals(config.getString("plugin-version"), getDescription().getVersion())) {
            getLogger().warning("""
                    ╔═════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗
                    ║                                 An update is available for plugin configuration                                 ║
                    ║            The update has NOT been applied because it would have replaced your actual configuration             ║
                    ║        To update without losing your configuration, please rename the file config.yml to config.yml.old         ║
                    ║          Restart your server, it will automatically recreate the config.yml file with the new version           ║
                    ║ Finally all you have to do is open both files and put back your old configurations (and customize the new ones) ║
                    ╚═════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝
                    """);
        }

        setupEconomy();

        checkUtils = new CheckUtils(this, economy);

        registerCommands();
        registerListeners();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No economy plugin found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
    }

    public Economy getEconomy() {
        return economy;
    }

    public FileConfiguration getCachedConfig() {
        return config;
    }

    public CheckUtils getCheckUtils() {
        return checkUtils;
    }

    @SuppressWarnings({"ConstantConditions","SpellCheckingInspection"})
    private void registerCommands() {
        getCommand("check").setExecutor(new CheckCommand(checkUtils, config));
        getCommand("admincheck").setExecutor(new AdminCheckCommand(checkUtils, config));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CheckEventsListener(this), this);
    }
}
