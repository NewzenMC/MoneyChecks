package fr.newzen.plugins.moneychecks.listeners;

import fr.newzen.plugins.moneychecks.CheckInfo;
import fr.newzen.plugins.moneychecks.CheckUtils;
import fr.newzen.plugins.moneychecks.MoneyChecks;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class CheckEventsListener implements Listener {

    private static final List<Action> INTERACT_ACTIONS = Arrays.asList(Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK);

    private final CheckUtils checkUtils;
    private final FileConfiguration config;
    private final Economy economy;

    public CheckEventsListener(MoneyChecks plugin) {
        checkUtils = plugin.getCheckUtils();
        config = plugin.getCachedConfig();
        economy = plugin.getEconomy();
    }

    @EventHandler
    public void rightClickCheck(PlayerInteractEvent event) {
        if (!INTERACT_ACTIONS.contains(event.getAction()) || !event.hasItem()) {
            return;
        }

        ItemStack item = event.getItem();
        assert item != null; // We checked event.hasItem() above, so this should never be null

        if (checkUtils.isCheck(item)) {
            event.setUseInteractedBlock(Event.Result.DENY);

            CheckInfo checkInfos = checkUtils.getCheckInfo(item);
            Player player = event.getPlayer();

            if (checkInfos.isValid()) {
                player.sendMessage(getStringFromConfig("cashing-check", "&7&oCashing check..."));
                EconomyResponse resp = economy.depositPlayer(player, checkInfos.getValue());
                if (resp.type != EconomyResponse.ResponseType.SUCCESS) {
                    player.sendMessage(getStringFromConfig("cashing-check-failed", "&cCashing check failed:") + " " + resp.errorMessage);
                } else {
                    item.setAmount(item.getAmount() - 1); // Remove one check from inventory
                    if (checkInfos.isFreeMoney()) {
                        player.sendMessage(getStringFromConfig("cashed-admin-check", "&aYous successfully cashed a check of %value% !").replace("%value%", economy.format(checkInfos.getValue())));
                    } else {
                        //noinspection ConstantConditions Creator always exists if it's not free money
                        player.sendMessage(getStringFromConfig("cashed-check", "&aYous successfully cashed a check of %value% from %creator% !").replace("%value%", economy.format(checkInfos.getValue())).replace("%creator%", checkInfos.getCreator().getName()));
                    }
                }
            } else {
                player.sendMessage(getStringFromConfig("invalid-check", "&cInvalid check"));
            }
        }
    }

    private @NotNull String getStringFromConfig(@NotNull String path, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, defaultValue));
    }
}
