package dev.withershop.commands;

import dev.withershop.shop.ShopItem;
import dev.withershop.shop.ShopManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens the Wither Shop GUI. Players only ever click what's already stocked -
 * there's no way to request or search for a specific item.
 *
 * The 9 items are arranged as a 3x3 square in the middle of a 3-row inventory
 * (instead of a single line), framed with purple/black glass panes to match the
 * Wither theme.
 */
public class ShopCommand implements CommandExecutor {

    // Plain-text title used both to build and to identify the GUI in ShopGuiListener.
    public static final String SHOP_TITLE_PLAIN = "☠ Wither Shop ☠";

    private static final int INVENTORY_SIZE = 27; // 3 rows of 9

    // The 9 center slots of a 3-row, 9-column inventory, in row-major order -
    // this is what makes the shop read as a square instead of a single line.
    public static final int[] SHOP_SLOTS = {
            3, 4, 5,
            12, 13, 14,
            21, 22, 23
    };

    private final ShopManager shopManager;

    public ShopCommand(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open the shop.");
            return true;
        }
        player.openInventory(buildInventory());
        return true;
    }

    /**
     * Converts a raw inventory click slot into an index into the shop's current
     * stock array (0-8), or -1 if the click wasn't on one of the 9 item slots.
     */
    public static int slotToStockIndex(int rawSlot) {
        for (int i = 0; i < SHOP_SLOTS.length; i++) {
            if (SHOP_SLOTS[i] == rawSlot) {
                return i;
            }
        }
        return -1;
    }

    public Inventory buildInventory() {
        Inventory inv = Bukkit.createInventory(null, INVENTORY_SIZE,
                Component.text(SHOP_TITLE_PLAIN, NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));

        // Frame every non-item slot with a themed glass pane, alternating black and
        // purple, so the 3x3 block reads as an intentional square layout.
        ItemStack blackFiller = createFiller(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack purpleFiller = createFiller(Material.PURPLE_STAINED_GLASS_PANE);
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            inv.setItem(slot, (slot % 2 == 0) ? blackFiller : purpleFiller);
        }

        ShopItem[] stock = shopManager.getCurrentStock();
        for (int i = 0; i < stock.length && i < SHOP_SLOTS.length; i++) {
            ShopItem item = stock[i];
            if (item == null) continue;
            inv.setItem(SHOP_SLOTS[i], buildDisplayItem(item));
        }
        return inv;
    }

    private ItemStack createFiller(Material material) {
        ItemStack filler = new ItemStack(material);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            filler.setItemMeta(meta);
        }
        return filler;
    }

    private ItemStack buildDisplayItem(ShopItem item) {
        ItemStack display = new ItemStack(item.getMaterial(), item.getAmount());
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            meta.displayName(
                    Component.text("✦ ", NamedTextColor.DARK_PURPLE)
                            .append(Component.text(prettyName(item.getMaterial().name()) + " x" + item.getAmount(),
                                    NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                            .decoration(TextDecoration.ITALIC, false)
            );

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("▸ ", NamedTextColor.DARK_GRAY)
                    .append(Component.text(item.getPrice() + " points", NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("▸ Unlimited stock - click to buy", NamedTextColor.GRAY)
                    .decoration(TextDecoration.ITALIC, false));
            meta.lore(lore);

            display.setItemMeta(meta);
        }
        return display;
    }

    private String prettyName(String rawEnumName) {
        String[] parts = rawEnumName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1).toLowerCase())
                    .append(" ");
        }
        return sb.toString().trim();
    }
}
