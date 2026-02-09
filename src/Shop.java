import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class Shop {

    public static void openShop(PlayerClass player, Scanner in) {
        runStore(player, in, StoreType.SHOP);
    }

    public static void openArmory(PlayerClass player, Scanner in) {
        runStore(player, in, StoreType.ARMORY);
    }

    private enum StoreType { SHOP, ARMORY }

    private static void runStore(PlayerClass player, Scanner in, StoreType type) {
        System.out.println();
        System.out.println(type == StoreType.SHOP ? "She slides a ledger toward you." : "\"Pick.\" The armorer gestures at the rack.");

        while (true) {
            System.out.println();
            System.out.println(type == StoreType.SHOP ? "=== SHOP COUNTER ===" : "=== ARMORY COUNTER ===");
            System.out.println("Options: list, buy, sell, gold, help, back");

            String cmd = read(in);
            switch (cmd) {
                case "list" -> printStock(player, type);
                case "buy" -> buyFlow(player, in, type);
                case "sell" -> sellFlow(player, in, type);
                case "gold", "status" -> System.out.println("Gold: " + player.getGold());
                case "help" -> printHelp(type);
                case "back", "leave", "exit" -> { return; }
                default -> System.out.println("Use: list, buy, sell, gold, help, back");
            }
        }
    }

    private static void printHelp(StoreType type) {
        System.out.println();
        if (type == StoreType.SHOP) {
            System.out.println("Shop sells consumables and ammo.");
            System.out.println("Commands:");
            System.out.println("  list");
            System.out.println("  buy  <#> [qty]");
            System.out.println("  sell <#> [qty]");
        } else {
            System.out.println("Armory sells weapons (each purchase is a fresh weapon with its own durability).");
            System.out.println("Commands:");
            System.out.println("  list");
            System.out.println("  buy  <#>");
            System.out.println("  sell <#>");
        }
    }

    private static void printStock(PlayerClass player, StoreType type) {
        List<Items> stock = buildStock(type);

        System.out.println();
        System.out.println("--- STOCK ---");
        System.out.println("Gold: " + player.getGold());

        for (int i = 0; i < stock.size(); i++) {
            Items it = stock.get(i);
            String owned = ownedString(player, it);
            String extra = "";
            if (it instanceof Weapon w) extra = " | Durability: " + trimDouble(w.getDurability()) + " | DMG+ " + w.getDmgBonus();
            if (it instanceof Ammo a) extra = " | Arrow DMG+ " + a.getDmgBonus();
            System.out.println((i + 1) + ") " + it.getName() + " | Buy: " + it.getBuyPrice() + " | Sell: " + it.getSellPrice() + owned + extra);
            System.out.println("    " + it.getDescription());
        }

        if (type == StoreType.ARMORY) {
            System.out.println();
            System.out.println("--- YOUR WEAPONS ---");
            LinkedList<Weapon> inv = player.getWeaponInventory();
            if (inv.isEmpty()) {
                System.out.println("None.");
            } else {
                for (int i = 0; i < inv.size(); i++) {
                    Weapon w = inv.get(i);
                    System.out.println((i + 1) + ") " + w.getName() + " | Durability: " + trimDouble(w.getDurability()) + " | DMG+ " + w.getDmgBonus()
                            + (w == player.getCurrentWeapon() ? " (equipped)" : ""));
                }
            }
        }
    }

    private static void buyFlow(PlayerClass player, Scanner in, StoreType type) {
        List<Items> stock = buildStock(type);

        System.out.print("Buy which #? ");
        String line = in.nextLine().trim();
        if (line.isEmpty()) return;

        String[] parts = line.split("\\s+");
        int idx = parseInt(parts[0], -1) - 1;
        int qty = (parts.length >= 2) ? parseInt(parts[1], 1) : 1;

        if (idx < 0 || idx >= stock.size()) {
            System.out.println("Invalid item number.");
            return;
        }

        Items pick = stock.get(idx);

        if (type == StoreType.ARMORY) qty = 1;
        if (qty <= 0) {
            System.out.println("Qty must be at least 1.");
            return;
        }

        int cost = pick.getBuyPrice() * qty;
        if (player.getGold() < cost) {
            System.out.println("Not enough gold. Need " + cost + ", you have " + player.getGold() + ".");
            return;
        }

        if (type == StoreType.SHOP) {
            if (pick instanceof Consumable c) {
                player.setGold(player.getGold() - cost);
                player.addConsumable(c, qty);
                System.out.println("Bought " + qty + "x " + c.getName() + ".");
            } else if (pick instanceof Ammo a) {
                player.setGold(player.getGold() - cost);
                player.addAmmo(a, qty);
                System.out.println("Bought " + qty + "x " + a.getName() + ".");
            } else {
                System.out.println("That doesn't belong in the Shop.");
            }
        } else {
            if (!(pick instanceof Weapon)) {
                System.out.println("That doesn't belong in the Armory.");
                return;
            }

            player.setGold(player.getGold() - cost);
            Weapon w = freshWeaponInstance(pick.getName());
            if (w == null) {
                System.out.println("Weapon not recognized: " + pick.getName());
                player.setGold(player.getGold() + cost);
                return;
            }
            player.addWeapon(w);
            System.out.println("Bought " + w.getName() + ".");
        }
    }

    private static void sellFlow(PlayerClass player, Scanner in, StoreType type) {
        if (type == StoreType.ARMORY) {
            LinkedList<Weapon> inv = player.getWeaponInventory();
            if (inv.isEmpty()) {
                System.out.println("You have no weapons to sell.");
                return;
            }

            System.out.print("Sell which weapon #? ");
            int idx = parseInt(in.nextLine().trim(), -1) - 1;
            if (idx < 0 || idx >= inv.size()) {
                System.out.println("Invalid weapon number.");
                return;
            }

            Weapon w = inv.get(idx);
            int value = w.getSellPrice();

            inv.remove(w);
            if (player.getCurrentWeapon() == w) {
                player.equipWeaponSafeNone();
            }

            player.setGold(player.getGold() + value);
            System.out.println("Sold " + w.getName() + " for " + value + " gold.");
            return;
        }

        List<Items> stock = buildStock(StoreType.SHOP);
        System.out.print("Sell which #? ");
        String line = in.nextLine().trim();
        if (line.isEmpty()) return;

        String[] parts = line.split("\\s+");
        int idx = parseInt(parts[0], -1) - 1;
        int qty = (parts.length >= 2) ? parseInt(parts[1], 1) : 1;

        if (idx < 0 || idx >= stock.size()) {
            System.out.println("Invalid item number.");
            return;
        }
        if (qty <= 0) {
            System.out.println("Qty must be at least 1.");
            return;
        }

        Items pick = stock.get(idx);

        if (pick instanceof Consumable c) {
            int have = player.getConsumableCount(c);
            if (have < qty) {
                System.out.println("You only have " + have + ".");
                return;
            }
            if (!player.removeConsumable(c, qty)) {
                System.out.println("Couldn't remove items.");
                return;
            }
            int value = c.getSellPrice() * qty;
            player.setGold(player.getGold() + value);
            System.out.println("Sold " + qty + "x " + c.getName() + " for " + value + " gold.");
            return;
        }

        if (pick instanceof Ammo a) {
            int have = player.getAmmoCount(a);
            if (have < qty) {
                System.out.println("You only have " + have + ".");
                return;
            }
            if (!player.removeAmmo(a, qty)) {
                System.out.println("Couldn't remove ammo.");
                return;
            }
            int value = a.getSellPrice() * qty;
            player.setGold(player.getGold() + value);
            System.out.println("Sold " + qty + "x " + a.getName() + " for " + value + " gold.");
            return;
        }

        System.out.println("That can't be sold here.");
    }

    private static List<Items> buildStock(StoreType type) {
        ArrayList<Items> stock = new ArrayList<>();

        if (type == StoreType.SHOP) {
            stock.add(new ItemLesserHealing());
            stock.add(new ItemGreaterHealing());
            stock.add(new ItemLesserMana());
            stock.add(new ItemGreaterMana());
            stock.add(new ItemLesserRage());
            stock.add(new ItemGreaterRage());
            stock.add(new ItemLesserSpeed());
            stock.add(new ItemGreaterSpeed());
            stock.add(new ItemEnergy());
            stock.add(new ItemDefense());

            stock.add(new ItemArrows());
            stock.add(new ItemBlackArrows());
        } else {
            stock.add(new ItemDullSword());
            stock.add(new ItemSword());
            stock.add(new ItemGildedSword());

            stock.add(new ItemShoddySpear());
            stock.add(new ItemSpear());
            stock.add(new ItemAttunedSpear());
        }

        return stock;
    }

    private static String ownedString(PlayerClass player, Items it) {
        if (it instanceof Consumable c) {
            int have = player.getConsumableCount(c);
            return " | Owned: " + have;
        }
        if (it instanceof Ammo a) {
            int have = player.getAmmoCount(a);
            return " | Owned: " + have;
        }
        return "";
    }

    private static Weapon freshWeaponInstance(String name) {
        return switch (name) {
            case "Dull Sword" -> new ItemDullSword();
            case "Sword" -> new ItemSword();
            case "Gilded Sword" -> new ItemGildedSword();
            case "Shoddy Spear" -> new ItemShoddySpear();
            case "Spear" -> new ItemSpear();
            case "Attuned Spear" -> new ItemAttunedSpear();
            default -> null;
        };
    }

    private static String read(Scanner in) {
        System.out.print("> ");
        return in.nextLine().trim().toLowerCase(Locale.ROOT);
    }

    private static int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return fallback; }
    }

    private static String trimDouble(double d) {
        if (d == (long) d) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
