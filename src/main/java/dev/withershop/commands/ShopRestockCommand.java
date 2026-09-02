package dev.withershop.commands;

import dev.withershop.shop.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /shoprestock - immediately rerolls the shop's 9 items and resets the hourly
 * timer. OPs only.
 */
public class ShopRestockCommand implements CommandExecutor {

    private final ShopManager shopManager;

    public ShopRestockCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("Only OPs can force a shop restock.").color(NamedTextColor.RED));
            return true;
        }

        shopManager.forceRestock();
        sender.sendMessage(
                Component.text("✔ ", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                        .append(Component.text("Shop restocked.", NamedTextColor.GREEN))
        );
        return true;
    }
}
