/*
Name: Alexander Calkins
Course and Section: CSC 205
Date: 2/9/26

Program Description:
This program is a text-based adventure game where the player explores a village hub,
enters different locations (casino, shop, armory, hospital, forest, and mountain),
fights enemies in grid-based combat, manages gold, health, weapons, and inventory,
and progresses through encounters and events.

Program Inputs (main):
The program reads user input from the keyboard using a Scanner. The player:
- chooses whether audio is enabled (y/n)
- selects a character class
- enters a player name
- types commands to navigate the village and gameplay systems

Program Outputs (main):
The program prints all game text to the console, including:
- menus and location descriptions
- combat narration and results
- player status and inventory information
- story events and game-over messages
- It also optionally plays music and sound effects.

The core gameplay loop is fight in forest -> buy gear -> fight -> buy -> maybe gamble/bank ->forest
then once you good enough gear or health potions you kill the dragon in the mt. You purposly have very little to 
gain- just like the current economy! and just like the current economy if all your tools break and you have no gold- and no bank 
loan to bail you out- "restarting" yourself in the forest is th eonly solution

Its a game about managing your finances and griding to get better gear
*/




import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class App {
    public static PlayerClass player;
    public static Scanner in;
    private static final boolean FORCE_SADAKO = true;
    private static final Random RNG = new Random();

    // This code here was made by AI
    public static class RestartGameException extends RuntimeException { }
    public static class QuitGameException extends RuntimeException { }
    //End of AI Code

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");

        in = new Scanner(System.in);

        // This code here was made by AI
        while (true) {
            try {
                runGameOnce();
                // If runGameOnce returns normally, game ended without death-restart.
                break;
            } catch (RestartGameException ex) {
                // Player chose to play again after dying
                System.out.println("\n--- Restarting game ---\n");
            } catch (QuitGameException ex) {
                break;
            }
        }
        //End of AI Code
    }

    private static void runGameOnce() {
        player = null;

        System.out.println("Do you want audio in this project? y/n");
        switch (in.next().toLowerCase().charAt(0)) {
            case 'y':
                Audio.setMuted(false);
                break;
            default:
                Audio.setMuted(true);
                break;
        }

        Audio.stopMusic();
        Audio.playMusicLoop("title.wav", -12.0f);

        boolean classChosen = false;
        while (!classChosen) {
            System.out.println("Pick a class: ");
            System.out.println("[1] Fighter - a mobile skirmisher");
            System.out.println("[2] Soldier - a stalwart menace");
            System.out.println("[3] Ranger - never too many arrows");
            System.out.println("[4] Sorcerer - blow them up with your mind");
            System.out.println("[5] peasant");

            switch (in.next().toLowerCase().charAt(0)) {
                case '1' -> { player = new Fighter("Player"); classChosen = true; }
                case '2' -> { player = new Soldier("Player"); classChosen = true; }
                case '3' -> { player = new Ranger("Player"); classChosen = true; }
                case '4' -> { player = new Sorcerer("Player"); classChosen = true; }
                case '5' -> { player = new PlayerClass("Player"); classChosen = true; }
                default -> System.out.println("Invalid Input");
            }
        }

        System.out.println("Choose your name: ");
        player.setName(in.next());
        in.nextLine(); 

        TextAdventureHub.villageLoop(player, in);
    }

    public static void defeat(PlayerClass p) {
        Audio.stopMusic();

        System.out.println("\n========================================");
        System.out.println("              YOU DIED");
        System.out.println("========================================");

        String name = (p == null) ? "Unknown" : p.getName();
        System.out.println("Name: " + name);

        if (p != null) {
            System.out.println("Health: " + String.format("%.0f", p.getHealth()));
            System.out.println("AC: " + p.getArmourClass());
            System.out.println("Mana: " + p.getMana());

            System.out.println("\n--- Wealth ---");
            System.out.println("Gold: " + p.getGold());

            System.out.println("\n--- Debts ---");
            System.out.println("Casino debt: " + p.getCasinoDebt());

            try {
                if (p.getBankState() != null) {
                    System.out.println("Bank status: " + (p.getBankState().hasActiveLoan ? String.format("Loan pending, %d", p.getBankState().activeRepayAmount) : "No Loan"));
                }
            } catch (Exception ignored) { }

            System.out.println("\n--- Inventory ---");

            Map<Consumable, Integer> cons = p.getConsumableInventoryView();
            if (cons.isEmpty()) System.out.println("Consumables: (none)");
            else {
                System.out.println("Consumables:");
                for (Map.Entry<Consumable, Integer> e : cons.entrySet()) {
                    System.out.println(" - " + e.getKey().getName() + " x" + e.getValue());
                }
            }

            Map<Ammo, Integer> ammo = p.getAmmoInventoryView();
            if (ammo.isEmpty()) System.out.println("Ammo: (none)");
            else {
                System.out.println("Ammo:");
                for (Map.Entry<Ammo, Integer> e : ammo.entrySet()) {
                    System.out.println(" - " + e.getKey().getName() + " x" + e.getValue());
                }
            }

            if (p.getWeaponInventory() == null || p.getWeaponInventory().isEmpty()) {
                System.out.println("Weapons: (none)");
            } else {
                System.out.println("Weapons (Equipped: " + p.getEquippedWeaponName() + "):");
                for (Weapon w : p.getWeaponInventory()) {
                    System.out.println(" - " + w.getName() + " (DMG+" + w.getDmgBonus() + ", DUR " + String.format("%.0f", w.durability) + ")");
                }
            }
        }

        System.out.println("========================================\n");

        // This code here was made by AI
        while (true) {
            Audio.stopMusic();
            Audio.playMusicLoop("gameOver.wav", -12.0f);
            System.out.println("Play again? (y/n)");
            String ans = in.nextLine().trim().toLowerCase();
            if (ans.isEmpty()) continue;

            char c = ans.charAt(0);
            if (c == 'y') throw new RestartGameException();
            if (c == 'n') throw new QuitGameException();

            System.out.println("Invalid Input");
        }
        //End of AI Code
    }

    private static Enemy E(String name, double hp, int str, int dex, int chr, 
                          int ac, int gold, int move, int dmg, int range, 
                          int hit, char id) {
        return new Enemy(name, hp, str, dex, chr, ac, gold, move, dmg, range, hit, id);
    }

    private static int rollEncounter17WithMythics(Random rng) {
        int pick = 1 + rng.nextInt(17);
        
        if (pick == 16 || pick == 17) {
            if (!passesMythicCheck(rng)) {
                pick = 1 + rng.nextInt(15);
            }
        }
        
        return pick;
    }

    //there are two rare fights sadako and champion, I didnt want to actually add 50 fights- so I just cheated and made them a random chance 
    // if they get landed on and reroll the fight die if it fails
    private static boolean passesMythicCheck(Random rng) {
        return rng.nextInt(50) == 0;
    }

    public static void startForestBattle(PlayerClass player) {
        //Sadako caused a LOT of bugs, still super buggy so I had to add this
        int encounter = FORCE_SADAKO ? 17 : rollEncounter17WithMythics(RNG);


        BattleManager bM = new BattleManager();
        ArrayList<Enemy> eL = new ArrayList<>();
        BattleGrid grid;
        Renderer renderer;
        int wallDensity;
        int difficulty;

        switch (encounter) {
            case 1 -> {
                grid = new BattleGrid(8, 8);
                wallDensity = 2;
                difficulty = 3;
                Audio.stopMusic();
                Audio.playMusicLoop("combatB.wav", -12.0f);
                eL.add(E("Goblin1", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, '1'));
                eL.add(E("Goblin2", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, '2'));
                eL.add(E("Goblin3", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, '3'));
            }
            
            case 2 -> {
                grid = new BattleGrid(8, 8);
                wallDensity = 10;
                difficulty = 3;
                Audio.stopMusic();
                Audio.playMusicLoop("combatB.wav", -12.0f);
                eL.add(E("Scout", 6, 2, 4, 1, 8, 1, 3, 4, 1, 12, '1'));
                eL.add(E("Goblin", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, '2'));
            }
            
            case 3 -> {
                grid = new BattleGrid(9, 9);
                wallDensity = 15;
                difficulty = 5;
                Audio.stopMusic();
                Audio.playMusicLoop("combatB.wav", -12.0f);
                eL.add(E("Archer", 6, 1, 4, 1, 7, 1, 2, 4, 3, 12, '1'));
                eL.add(E("Archer2", 6, 1, 4, 1, 7, 1, 2, 4, 3, 12, '2'));
                eL.add(E("Guard", 9, 4, 2, 1, 9, 3, 2, 6, 1, 12, '3'));
            }
            
            case 4 -> {
                grid = new BattleGrid(7, 7);
                wallDensity = 5;
                difficulty = 2;
                Audio.stopMusic();
                Audio.playMusicLoop("combatB.wav", -12.0f);
                eL.add(E("Brute", 14, 6, 1, 1, 9, 3, 2, 7, 1, 12, '1'));
            }
            
            case 5 -> {
                grid = new BattleGrid(10, 8);
                wallDensity = 20;
                difficulty = 7;
                Audio.stopMusic();
                Audio.playMusicLoop(Math.random() < 0.5 ? "combatA.wav" : "combatD.wav", -12.0f);
                eL.add(E("Sneak", 7, 2, 5, 1, 8, 2, 4, 4, 1, 12, '1'));
                eL.add(E("Sneak2", 7, 2, 5, 1, 8, 2, 4, 4, 1, 12, '2'));
                eL.add(E("Shaman", 8, 1, 2, 6, 7, 3, 2, 5, 2, 13, '3'));
            }
            
            case 6 -> {
                grid = new BattleGrid(8, 10);
                wallDensity = 25;
                difficulty = 2;
                Audio.stopMusic();
                Audio.playMusicLoop(Math.random() < 0.5 ? "combatA.wav" : "combatD.wav", -12.0f);
                eL.add(E("Pikeman", 10, 4, 2, 1, 9, 2, 2, 6, 2, 12, '1'));
                eL.add(E("Pikeman2", 10, 4, 2, 1, 9, 2, 2, 6, 2, 12, '2'));
            }
            
            case 7 -> {
                grid = new BattleGrid(9, 7);
                wallDensity = 10;
                difficulty = 1;
                Audio.stopMusic();
                Audio.playMusicLoop(Math.random() < 0.5 ? "combatA.wav" : "combatD.wav", -12.0f);
                eL.add(E("Wolf", 8, 4, 4, 1, 8, 1, 4, 5, 1, 12, '1'));
                eL.add(E("Wolf2", 8, 4, 4, 1, 8, 1, 4, 5, 1, 12, '2'));
                eL.add(E("Wolf3", 8, 4, 4, 1, 8, 1, 4, 5, 1, 12, '3'));
            }
            
            case 8 -> {
                grid = new BattleGrid(10, 10);
                wallDensity = 30;
                difficulty = 3;
                Audio.stopMusic();
                Audio.playMusicLoop(Math.random() < 0.5 ? "combatA.wav" : "combatD.wav", -12.0f);
                eL.add(E("Captain", 16, 6, 3, 2, 10, 3, 2, 7, 1, 13, '1'));
                eL.add(E("Archer", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, '2'));
            }
            
            case 9 -> {
                grid = new BattleGrid(8, 8);
                wallDensity = 0;
                difficulty = 2;
                Audio.stopMusic();
                Audio.playMusicLoop(Math.random() < 0.5 ? "combatA.wav" : "combatD.wav", -12.0f);
                eL.add(E("Duelist", 12, 4, 5, 2, 10, 3, 3, 6, 1, 12, '3'));
            }
            
            case 10 -> {
                grid = new BattleGrid(9, 9);
                wallDensity = 35;
                difficulty = 5;
                Audio.stopMusic();
                Audio.playMusicLoop(Math.random() < 0.5 ? "combatA.wav" : "combatD.wav", -12.0f);
                eL.add(E("Wall-Crawler", 9, 3, 6, 1, 9, 2, 4, 5, 1, 12, '1'));
                eL.add(E("Wall-Crawler2", 9, 3, 6, 1, 9, 2, 4, 5, 1, 12, '2'));
                eL.add(E("Brute", 14, 6, 1, 1, 9, 3, 2, 7, 1, 12, '3'));
            }
            
            case 11 -> {
                grid = new BattleGrid(7, 10);
                wallDensity = 12;
                difficulty = 5;
                Audio.stopMusic();
                Audio.playMusicLoop(Math.random() < 0.5 ? "combatA.wav" : "combatD.wav", -12.0f);
                eL.add(E("Skirmisher", 10, 4, 4, 2, 9, 2, 3, 5, 2, 12, '1'));
                eL.add(E("Archer", 6, 1, 4, 1, 7, 2, 2, 4, 3, 12, '2'));
                eL.add(E("Archer2", 6, 1, 4, 1, 7, 2, 2, 4, 3, 12, '3'));
            }
            
            case 12 -> {
                grid = new BattleGrid(10, 7);
                wallDensity = 18;
                difficulty = 5;
                Audio.stopMusic();
                Audio.playMusicLoop("combatC1.wav", -12.0f);
                eL.add(E("Shaman", 10, 1, 2, 7, 8, 3, 2, 6, 2, 13, '1'));
                eL.add(E("Guard", 11, 5, 2, 1, 10, 2, 2, 6, 1, 12, '2'));
            }
            
            case 13 -> {
                grid = new BattleGrid(8, 9);
                wallDensity = 22;
                difficulty = 10;
                Audio.stopMusic();
                Audio.playMusicLoop("combatC1.wav", -12.0f);
                eL.add(E("Twin Blades", 20, 4, 6, 1, 10, 2, 3, 6, 1, 12, '1'));
                eL.add(E("Twin Blades2", 20, 4, 6, 1, 10, 2, 3, 6, 1, 12, '2'));
            }
            
            case 14 -> {
                grid = new BattleGrid(9, 8);
                wallDensity = 28;
                difficulty = 5;
                Audio.stopMusic();
                Audio.playMusicLoop("combatC1.wav", -12.0f);
                eL.add(E("Raider", 13, 6, 3, 1, 10, 1, 3, 7, 1, 12, '1'));
                eL.add(E("Raider2", 13, 6, 3, 1, 10, 1, 3, 7, 1, 12, '2'));
                eL.add(E("Archer", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, '3'));
            }
            
            case 15 -> {
                grid = new BattleGrid(10, 10);
                wallDensity = 40;
                difficulty = 15;
                Audio.stopMusic();
                Audio.playMusicLoop("combatC1.wav", -12.0f);
                eL.add(E("Warlord", 30, 8, 3, 2, 12, 3, 2, 8, 1, 13, '1'));
                eL.add(E("Archer", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, '2'));
                eL.add(E("Archer2", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, '3'));
            }
            
            // Mythic 1
            case 16 -> {
                grid = new BattleGrid(10, 10);
                wallDensity = 45;
                difficulty = 60;
                Audio.stopMusic();
                Audio.playMusicLoop("combatC2.wav", -12.0f);
                eL.add(E("Mythic Champion", 100, 10, 5, 3, 10, 0, 2, 9, 1, 14, '1'));
            }
            
            // Mythic 2
            case 17 -> {
                grid = new BattleGrid(1, 1);
                wallDensity = 0;
                difficulty = 0;
                Audio.stopMusic();
                Audio.playMusicLoop("cursed.wav", -12.0f);
                SadakoBattle.run(player, in);
                if (player.isAlive()) player.setGold(player.getGold() + 50);
                return;
            }
            
            default -> throw new IllegalStateException("Unexpected encounter: " + encounter);
        }

        renderer = new Renderer(grid);
        System.out.println("Forest encounter " + encounter + "/15");
        
        bM.createBasicBattle(player, eL, grid, renderer, wallDensity, difficulty, in);
    }

    public static void startMountainBattle(PlayerClass player) {
        BattleGrid grid = new BattleGrid(9, 7);
        Renderer renderer = new Renderer(grid);
        BattleManager bM = new BattleManager();
        ArrayList<Enemy> eL = new ArrayList<>();
        
        eL.add(new Dragon("Dragon", 'D'));
        
        ArrayList<int[]> walls = new ArrayList<>();
        walls.add(new int[]{1, 1});
        walls.add(new int[]{1, 2});
        walls.add(new int[]{6, 1});
        walls.add(new int[]{6, 2});
        walls.add(new int[]{1, 4});
        walls.add(new int[]{1, 5});
        walls.add(new int[]{6, 4});
        walls.add(new int[]{6, 5});
        
        int playerX = 0, playerY = 3;
        int dragonX = 3, dragonY = 3;
        
        Audio.playMusicLoop("combatF.wav", -8.0f);
        bM.createScriptedBattle(player, eL, grid, renderer, 80, 
                               playerX, playerY, dragonX, dragonY, walls, in);
        
        Enemy boss = eL.get(0);
        if (!boss.isAlive()) {
            System.out.println("The dragon collapses. The mountain goes quiet.");
            System.out.println("You win.");
            System.exit(0);
        }
    }
}




//This game is very hard to test once finished- I did most of my testing during the development progress. 
// This game is very open ended, PLease tell me how to better do this next time
/*
 * TESTS / VALIDATION
 * Example:
 * Test 1:
 * <full execution run of sadako boss fight>
 * <many bugs exist, still working on getting conole japnese to work>
 * <satisfactory>
 *
 * Test 2:
 * <full execution run of program>
 * <tested bank loans, black jack, and hospital>
 * <all seem to work along with dragon fight>
 *
 */