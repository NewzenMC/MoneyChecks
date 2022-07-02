package fr.newzen.plugins.moneychecks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CheckInfo {

    private final boolean valid;
    private final double value;
    private final String source;
    private final OfflinePlayer creator;
    private final boolean freeMoney;

    public CheckInfo(boolean valid, double value, @NotNull String source, @Nullable UUID creatorUUID, boolean freeMoney) {
        this.valid = valid;
        this.value = value;
        this.source = source;
        if (creatorUUID != null) {
            this.creator = Bukkit.getOfflinePlayer(creatorUUID);
        } else {
            this.creator = null;
        }
        this.freeMoney = freeMoney;
    }

    public boolean isValid() {
        return valid;
    }

    public double getValue() {
        return value;
    }

    @NotNull
    public String getSource() {
        return source;
    }

    @Nullable
    public OfflinePlayer getCreator() {
        return creator;
    }

    public boolean isFreeMoney() {
        return freeMoney;
    }
}
