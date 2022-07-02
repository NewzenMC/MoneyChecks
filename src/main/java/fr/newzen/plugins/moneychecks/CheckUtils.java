package fr.newzen.plugins.moneychecks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckUtils {
    private final Economy economy;
    private final NamespacedKey sourceKey;
    private final NamespacedKey valueKey;
    private final NamespacedKey creatorKey;
    private final NamespacedKey freeMoneyKey;
    private final FileConfiguration config;
    private final String signSource;
    private final List<String> validSources;

    public CheckUtils(MoneyChecks plugin, Economy economy) {
        this.economy = economy;

        sourceKey = new NamespacedKey(plugin, "checkSource");
        valueKey = new NamespacedKey(plugin, "checkValue");
        creatorKey = new NamespacedKey(plugin, "checkCreator");
        freeMoneyKey = new NamespacedKey(plugin, "freeMoney");

        config = plugin.getCachedConfig();

        signSource = config.getString("sign-check-source", "MoneyChecks");
        validSources = config.getStringList("valid-check-sources");
    }

    public boolean signCheck(Player player, double value, boolean freeMoney) {
        if (value <= 0) {
            player.sendMessage(getStringFromConfig("check-sign-error-value-below-zero", "&cCheck value must be above zero"));
            return false;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(getStringFromConfig("check-sign-error-inventory-full", "&cYour inventory is full"));
            return false;
        }

        if (!freeMoney) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
            if (economy.has(offlinePlayer, value)) {
                EconomyResponse resp = economy.withdrawPlayer(offlinePlayer, value);
                if (resp.type != EconomyResponse.ResponseType.SUCCESS) {
                    player.sendMessage(getStringFromConfig("check-sign-error-withdraw-failed", "&cWithdraw failed:") + " " + resp.errorMessage);
                    return false;
                }
                //noinspection ConstantConditions
                player.getInventory().addItem(createCheckItem(value, player, freeMoney));
                return true;
            } else {
                player.sendMessage(getStringFromConfig("check-sign-error-not-enough-money", "&cYou don't have enough money !"));
                return false;
            }
        } else {
            //noinspection ConstantConditions
            player.getInventory().addItem(createCheckItem(value, player, freeMoney));
            return true;
        }
    }

    public boolean isCheck(ItemStack item) {
        if (item.getType() != Material.PAPER || !item.hasItemMeta()) return false;

        // Suppress Warning because we checked before if it has item meta (item.hasItemMeta())
        @SuppressWarnings("ConstantConditions") PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();

        return container.has(sourceKey, PersistentDataType.STRING) && container.has(valueKey, PersistentDataType.DOUBLE);
    }

    @SuppressWarnings("ConstantConditions") // Lots of checks have been made in isCheck method
    @NotNull
    public CheckInfo getCheckInfo(ItemStack check) {
        if (!isCheck(check)) throw new IllegalArgumentException("Item is not a check");

        PersistentDataContainer container = check.getItemMeta().getPersistentDataContainer();

        String source = container.get(sourceKey, PersistentDataType.STRING);
        double value = container.get(valueKey, PersistentDataType.DOUBLE);
        UUID creatorUUID = null;
        boolean freeMoney = false;

        if (container.has(creatorKey, PersistentDataType.STRING)) {
            creatorUUID = UUID.fromString(container.get(creatorKey, PersistentDataType.STRING));
        }
        if (container.has(freeMoneyKey, PersistentDataType.INTEGER)) {
            freeMoney = container.get(freeMoneyKey, PersistentDataType.INTEGER) == 1;
        }


        boolean valid = validSources.contains(source);

        return new CheckInfo(valid, value, source, creatorUUID, freeMoney);
    }

    private @NotNull ItemStack createCheckItem(double value, @NotNull Player creator, boolean freeMoney) {
        if (value <= 0) {
            throw new IllegalArgumentException("value must be greater than 0");
        }

        ItemStack check = new ItemStack(Material.PAPER);
        ItemMeta checkMeta = check.getItemMeta();

        assert checkMeta != null;

        // Item Glowing
        addGlowing(checkMeta);

        // Item Name
        if (config.getBoolean("value-in-item-name", true)) {
            checkMeta.setDisplayName(getStringFromConfig("value-color", "&d&o") + value);
        } else {
            checkMeta.setDisplayName(getStringFromConfig("item-name", "&d&oCheck"));
        }

        // Item Lore
        List<String> lore = new ArrayList<>();
        if (!freeMoney || !config.getBoolean("value-in-item-name", true)) lore.add("");
        if (!freeMoney) lore.add(getStringFromConfig("item-lore-creator", "&fCreator:") + " " + getStringFromConfig("item-lore-creator-color", "&a") + creator.getName());
        if (!config.getBoolean("value-in-item-name", true)) lore.add(getStringFromConfig("item-lore-value", "&fValue:") + " " + getStringFromConfig("value-color", "&d&o") + value);
        String additionalLore = config.getString("additional-lore-line", "");
        if (!additionalLore.trim().equals("")) {
            lore.add(""); // Add a blank line
            lore.add(ChatColor.translateAlternateColorCodes('&', additionalLore));
        }
        checkMeta.setLore(lore);

        PersistentDataContainer container = checkMeta.getPersistentDataContainer();
        addData(container, value, signSource, creator, freeMoney);

        check.setItemMeta(checkMeta);
        return check;
    }

    private @NotNull String getStringFromConfig(@NotNull String path, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&', config.getString(path, defaultValue));
    }

    private void addGlowing(@NotNull ItemMeta meta) {
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    private void addData(@NotNull PersistentDataContainer container, double value, @NotNull String source, @NotNull Player creator, boolean freeMoney) {
        container.set(sourceKey, PersistentDataType.STRING, source);
        container.set(valueKey, PersistentDataType.DOUBLE, value);
        if (freeMoney) {
            container.set(freeMoneyKey, PersistentDataType.INTEGER , 1);
        } else {
            container.set(creatorKey, PersistentDataType.STRING, creator.getUniqueId().toString());
        }
    }
}
