import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Scanner;

public class TextAdventureHub {

    public static void villageLoop(PlayerClass player, Scanner in) {
        System.out.println("You wake up in the Village.");
        System.out.println("A small square. A few storefronts. And the unnerving sense that one woman is staffing all of them.");
        System.out.println("Type what you want to do. If you get lost, type 'help'.");

        while (true) {
            Audio.stopMusic();
            Audio.playMusicLoop("main.wav", -12.0f);
            System.out.println();
            System.out.println("=== VILLAGE HUB ===");
            System.out.println("Options: casino, bank, shop, armory, forest, mountain, status, help, quit");

            String cmd = readCommand(in);
            switch (cmd) {
                case "casino" -> casinoLoop(player, in);
                case "bank" -> bankLoop(player, in);
                case "shop" -> shopLoop(player, in);
                case "armory" -> armoryLoop(player, in);
                case "forest" -> forestVenture(player, in);
                case "mountain" -> mountainVenture(player, in);
                case "status" -> showStatus(player);
                case "help" -> villageHelp();
                case "quit", "exit" -> {
                    System.out.println("Goodbye.");
                    return;
                }
                default -> System.out.println("That isn't one of the options.");
            }
        }
    }

    private static String readCommand(Scanner in) {
        System.out.print("> ");
        String s = in.nextLine().trim().toLowerCase(Locale.ROOT);
        if (s.equals("armoury")) s = "armory";
        return s;
    }

    private static boolean askYesNo(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt + " ");
            String s = in.nextLine().trim().toLowerCase(Locale.ROOT);
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            System.out.println("Just Y or N.");
        }
    }

    private static void showStatus(PlayerClass player) {
        System.out.println();
        System.out.println("--- STATUS ---");
        System.out.println("Gold: " + player.getGold());
        System.out.println("Health: " + player.getHealth());
        System.out.println("Mana: " + player.getMana());
        System.out.println("AC: " + player.getArmourClass());
        System.out.println("Weapon: " + player.getEquippedWeaponName());
        System.out.println("Casino Debt: " + player.getCasinoDebt());
    }

    private static void villageHelp() {
        System.out.println();
        System.out.println("You're in the Village hub.");
        System.out.println("Go somewhere by typing its name. In any building, type 'help' to be walked through what it does.");
        System.out.println("If you want to chat with the staff, type 'talk' inside that location.");
    }

    // ---------------- Casino ----------------

    private static void casinoLoop(PlayerClass player, Scanner in) {
        Audio.stopMusic();
        Audio.playMusicLoop("shopCasino.wav", -12.0f);
        showArt(2);
        System.out.println();
        System.out.println();
        System.out.println("You enter the Casino.");
        System.out.println("A bunny-suited hostess leans in a drink in a tray outstretched.");

        while (true) {
            System.out.println();
            System.out.println("=== CASINO ===");
            System.out.println("Options: talk, help, play, leave");

            String cmd = readCommand(in);
            switch (cmd) {
                case "talk" -> Interactions.talkCasino(player, in);
                case "help" -> casinoHelp();
                case "play" -> Casino.playBlackjack(player, in);
                case "leave" -> {
                    System.out.println("You leave before the table learns your habits.");
                    return;
                }
                default -> System.out.println("\"What will it be, my precious?\"");
            }
        }
    }

    private static void casinoHelp() {
        System.out.println();
        System.out.println("Blackjack. Hit or stand.");
        System.out.println("Dealer stands on soft 17.");
        System.out.println("Blackjack pays 2:3.");
        System.out.println("If you go into debt, the casino keeps track.");
    }

    // ---------------- Shop ----------------

    private static void shopLoop(PlayerClass player, Scanner in) {
        Audio.stopMusic();
        Audio.playMusicLoop("shopGeneral.wav", -12.0f);
        showArt(4);
        System.out.println();
        System.out.println();
        System.out.println("You enter the Shop.");
        System.out.println("A witch-hatted shopkeeper nods her head at you");

        while (true) {
            System.out.println();
            System.out.println("=== SHOP ===");
            System.out.println("Options: talk, help, buy, leave");

            String cmd = readCommand(in);
            switch (cmd) {
                case "talk" -> Interactions.talkShop(player, in);
                case "help" -> shopHelp();
                case "buy" -> Shop.openShop(player, in);
                case "leave" -> {
                    System.out.println("You step back into the Village.");
                    return;
                }
                default -> System.out.println("She just stares, patiently judgmental.");
            }
        }
    }

    private static void shopHelp() {
        System.out.println();
        System.out.println("This is a shop: you trade gold for consumables and ammo.");
    }

    // ---------------- Armory ----------------

    private static void armoryLoop(PlayerClass player, Scanner in) {
        Audio.stopMusic();
        Audio.playMusicLoop("shopBlack.wav", -12.0f);
        showArt(1);
        System.out.println();
        System.out.println();
        System.out.println("You enter the Armory.");
        System.out.println("An eyepatched 'general' assesses you like you're badly supplied.");

        while (true) {
            System.out.println();
            System.out.println("=== ARMORY ===");
            System.out.println("Options: talk, help, buy, leave");

            String cmd = readCommand(in);
            switch (cmd) {
                case "talk" -> Interactions.talkArmory(player, in);
                case "help" -> armoryHelp();
                case "buy" -> Shop.openArmory(player, in);
                case "leave" -> {
                    System.out.println("You return to the Village.");
                    return;
                }
                default -> System.out.println("\"State your purpose. Now.\"");
            }
        }
    }

    private static void armoryHelp() {
        System.out.println();
        System.out.println("Armory: weapons, replacements, upgrades, arrow replenishment.");
    }

    // ---------------- Bank ----------------

    private static void bankLoop(PlayerClass player, Scanner in) {
        Audio.stopMusic();
        Audio.playMusicLoop("shopBank.wav", -12.0f);
        showArt(3);
        System.out.println();
        System.out.println();
        System.out.println("You enter the Bank.");
        System.out.println("A suited banker looks up like she's tired of being perceived.");

        while (true) {
            System.out.println();
            System.out.println("=== BANK ===");
            System.out.println("Options: talk, help, loan, payback, status, leave");

            String cmd = readCommand(in);
            switch (cmd) {
                case "talk" -> Interactions.talkBank(player, in);
                case "help" -> bankHelp();
                case "loan" -> System.out.println(player.getBank().takeLoan(player));
                case "payback", "repay" -> System.out.println(player.getBank().repayLoan(player));
                case "status" -> System.out.println(player.getBank().getStatus(player));
                case "leave" -> {
                    System.out.println("You exit the Bank.");
                    return;
                }
                default -> System.out.println("\"Use the options.\"");
            }
        }
    }

    private static void bankHelp() {
        System.out.println();
        System.out.println("Take loans and repay them to progress bank stages.");
        System.out.println("After stage 11, you must earn gold before repayment unlocks.");
    }

    // ---------------- Outside ----------------

    private static void forestVenture(PlayerClass player, Scanner in) {
        System.out.println();
        showArt(5);
        System.out.println();
        System.out.println("You stand at the edge of the Forest.");
        boolean go = askYesNo(in, "Venture into combat? (Y/N)");
        if (!go) {
            System.out.println("Back to the Village.");
            return;
        }

        System.out.println("You push into the trees...");
        App.startForestBattle(player);
        System.out.println("Back to the Village.");
    }

    private static void mountainVenture(PlayerClass player, Scanner in) {
        System.out.println();
        showArt(6);
        System.out.println();
        System.out.println("The Mountain looms.");
        boolean go = askYesNo(in, "Are you ready to take on the dragon? (Y/N)");
        if (!go) {
            System.out.println("Back to the Village.");
            return;
        }

        System.out.println("You climb until the air gets thin...");
        App.startMountainBattle(player);
        System.out.println("Back to the Village. For now.");
    }

    private static void showArt(int n) {
        Path p = Paths.get("src", "resources", "art", "art" + n + ".txt");
        System.out.println(readTextFile(p));
    }

    private static String readTextFile(Path p) {
        try {
            if (!Files.exists(p)) {
                return "Missing file: " + p.toAbsolutePath();
            }

            return Files.readString(p, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Failed to read file: " + p.toAbsolutePath() + "\n" + e;
        }
    }
}
