package fr.newzen.plugins.moneychecks.commands;

import fr.newzen.plugins.moneychecks.CheckUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCheckCommand implements CommandExecutor {

    private final CheckUtils checkUtils;
    private final FileConfiguration config;

    public AdminCheckCommand(CheckUtils checkUtils, FileConfiguration config) {
        this.checkUtils = checkUtils;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof Player player) {
            //noinspection SpellCheckingInspection
            if (!player.hasPermission("moneychecks.admincheck")) {
                player.sendMessage(getStringFromConfig("admin-check-error-no-permission", "&cYou don\\'t have permission to sign admin checks"));
                return true;
            }

            if (args.length == 1) {
                try {
                    double value = Double.parseDouble(args[0]);
                    player.sendMessage(getStringFromConfig("check-sign-signing", "&7&oPrinting and Signing check..."));
                    boolean result = checkUtils.signCheck(player, value, true);
                    if (result) {
                        player.sendMessage(getStringFromConfig("check-sign-signed", "&aCheck Printed and Signed !"));
                    }
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            sender.sendMessage(getStringFromConfig("admin-check-error-not-a-player", "&cYou must be a player to sign admin checks"));
            return true;
        }
    }

    private @NotNull String getStringFromConfig(@NotNull String path, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, defaultValue));
    }
}
