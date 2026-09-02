package dev.withershop.shop;

import org.bukkit.Material;

/**
 * A single purchasable entry in the Wither Shop.
 * Price and amount are fixed per material and never change.
 */
public class ShopItem {

    private final Material material;
    private final int price;
    private final int amount;

    public ShopItem(Material material, int price, int amount) {
        this.material = material;
        this.price = price;
        this.amount = amount;
    }

    public Material getMaterial() {
        return material;
    }

    public int getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }
}
