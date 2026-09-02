package dev.withershop.listeners;

import dev.withershop.commands.ShopCommand;
import dev.withershop.points.PointsManager;
import dev.withershop.shop.ShopItem;
import dev.withershop.shop.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Every click in the shop GUI either buys the clicked item (if the player can
 * afford it) or is silently rejected - nothing can be dragged out for free,
 * and the inventory can never be used to sell/store items.
 */
public class ShopGuiListener implements Listener {

    private final ShopManager shopManager;
    private final PointsManager pointsManager;

    public ShopGuiListener(ShopManager shopManager, PointsManager pointsManager) {
        this.shopManager = shopManager;
        this.pointsManager = pointsManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component titleComponent = event.getView().title();
        String plainTitle = PlainTextComponentSerializer.plainText().serialize(titleComponent);
        if (!plainTitle.equals(ShopCommand.SHOP_TITLE_PLAIN)) {
            return; // Not our GUI - ignore.
        }

        // Always cancel: this inventory is a catalog, not a real container.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int stockIndex = ShopCommand.slotToStockIndex(event.getRawSlot());
        if (stockIndex < 0) {
            return; // Click was on a filler pane, the player's own inventory, etc.
        }

        ShopItem item = shopManager.getCurrentStock()[stockIndex];
        if (item == null) {
            return;
        }

        if (!pointsManager.removePoints(player.getUniqueId(), item.getPrice())) {
            player.sendMessage(
                    Component.text("✖ ", NamedTextColor.RED).decorate(TextDecoration.BOLD)
                            .append(Component.text("Not enough points. Need " + item.getPrice()
                                            + ", you have " + pointsManager.getPoints(player.getUniqueId()) + ".",
                                    NamedTextColor.RED))
            );
            return;
        }

        player.getInventory().addItem(new ItemStack(item.getMaterial(), item.getAmount()));
        player.sendMessage(
                Component.text("✔ ", NamedTextColor.GREEN).decorate(TextDecoration.BOLD)
                        .append(Component.text("Bought " + item.getAmount() + "x " + item.getMaterial().name()
                                        + " for " + item.getPrice() + " points.",
                                NamedTextColor.GREEN))
        );
    }
}
