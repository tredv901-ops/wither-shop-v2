# WitherShop

A Paper 26.2 plugin:

- Kill a **Wither** → get **+1 point** (styled kill message + running total).
- **`/shop`** opens a 9-slot GUI arranged as a 3x3 square, framed with black/purple
  glass panes, with bold purple/gold styled item names and prices. Players can't
  choose what's sold — they can only click to buy whatever is currently stocked,
  at a fixed price, as many times as they can afford (unlimited stock per item).
  **Elytra is permanently excluded from the pool — it will never appear for sale.**
- The shop **automatically restocks every real-life hour**, swapping in 9 different
  random items (drawn from a 30-item pool). **Restarting the server does not reroll
  the shop early** — the current stock and the exact restock time are saved to disk,
  so the timer only advances with real elapsed time.
- **`/points`** — check your own balance.
- **`/points give <player> <amount>`** — give any player (online or offline) points.
  **OP only.**
- **`/pay <player> <amount>`** — send some of your own points to another online
  player. No OP required.
- **`/shoprestock`** — immediately force a restock and reset the hourly timer.
  **OP only.**
- Points are saved to `plugins/WitherShop/points.yml` and survive restarts.

## Pricing

Withers are much rarer and harder to get than Wardens — you need to farm Wither
Skeleton skulls, build the summon structure, and survive a boss fight — so points
trickle in far more slowly. Prices are tuned to be affordable on a handful of
Wither kills rather than dozens:

| Item | Price | Amount per purchase |
|---|---|---|
| Iron Ingot | 1 | 4 |
| Gold Ingot | 1 | 4 |
| Experience Bottle | 1 | 4 |
| Emerald | 1 | 3 |
| Ender Pearl | 1 | 2 |
| Blaze Rod | 1 | 2 |
| Diamond | 2 | 1 |
| Iron Block | 3 | 1 |
| Gold Block | 3 | 1 |
| Ghast Tear | 3 | 1 |
| Spectral Arrow | 3 | 8 |
| Firework Rocket | 3 | 8 |
| Diamond Sword | 4 | 1 |
| Diamond Pickaxe | 4 | 1 |
| Diamond Chestplate | 5 | 1 |
| Netherite Scrap | 5 | 1 |
| Saddle | 5 | 1 |
| Name Tag | 5 | 1 |
| Emerald Block | 6 | 1 |
| Trident | 6 | 1 |
| Respawn Anchor | 6 | 1 |
| Lodestone | 6 | 1 |
| Diamond Block | 8 | 1 |
| Shulker Box | 8 | 1 |
| Netherite Ingot | 9 | 1 |
| Totem of Undying | 10 | 1 |
| Enchanted Golden Apple | 12 | 1 |
| Nether Star | 15 | 1 |
| Beacon | 18 | 1 |
| Dragon Egg | 20 | 1 |
| Netherite Block | 25 | 1 |

Each hourly restock picks 9 of these at random. Elytra is not in this list on
purpose — it's excluded from the plugin entirely.

## Requirements

- Paper (or a Paper fork) **26.2**
- **Java 25** to build (matches Paper 26.2's own toolchain — using Java 21 will
  fail with a "class file has wrong version" error)
- Maven

## Building

From this folder:

```
mvn clean package
```

The compiled plugin will be at `target/WitherShop.jar`.

## Installing

1. Copy `target/WitherShop.jar` into your server's `plugins/` folder.
2. Restart (or `/reload confirm`) the server.
3. Kill a Wither, then run `/shop` or `/points`.

## Customizing prices / items

Edit the `buildPool()` method in
`src/main/java/dev/withershop/shop/ShopManager.java` — each line is
`addItem(Material, price, amountPerPurchase)`. Add, remove, or reprice entries
and rebuild. The restock interval (`RESTOCK_INTERVAL_TICKS`, currently 1 hour =
`20 * 60 * 60` ticks) is defined at the top of the same file.
