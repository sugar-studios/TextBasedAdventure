import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class App {
    public static PlayerClass player;
    public static Scanner in; // shared scanner
    private static final boolean FORCE_SADAKO = true;
    private static final Random RNG = new Random();

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        player = new Sorcerer("Player");
        in = new Scanner(System.in);
        TextAdventureHub.villageLoop(player, in);
        in.close();
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

    private static boolean passesMythicCheck(Random rng) {
        return rng.nextInt(50) == 0;
    }

    public static void startForestBattle(PlayerClass player) {
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
                eL.add(E("Goblin1", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, '1'));
                eL.add(E("Goblin2", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, '2'));
                eL.add(E("Goblin3", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, '3'));
            }
            
            case 2 -> {
                grid = new BattleGrid(8, 8);
                wallDensity = 10;
                difficulty = 3;
                eL.add(E("Scout", 6, 2, 4, 1, 8, 1, 3, 4, 1, 12, 'S'));
                eL.add(E("Goblin", 5, 2, 2, 2, 7, 1, 2, 5, 1, 12, 'G'));
            }
            
            case 3 -> {
                grid = new BattleGrid(9, 9);
                wallDensity = 15;
                difficulty = 5;
                eL.add(E("Archer", 6, 1, 4, 1, 7, 1, 2, 4, 3, 12, 'A'));
                eL.add(E("Archer2", 6, 1, 4, 1, 7, 1, 2, 4, 3, 12, 'B'));
                eL.add(E("Guard", 9, 4, 2, 1, 9, 3, 2, 6, 1, 12, 'H'));
            }
            
            case 4 -> {
                grid = new BattleGrid(7, 7);
                wallDensity = 5;
                difficulty = 2;
                eL.add(E("Brute", 14, 6, 1, 1, 9, 3, 2, 7, 1, 12, 'R'));
            }
            
            case 5 -> {
                grid = new BattleGrid(10, 8);
                wallDensity = 20;
                difficulty = 7;
                eL.add(E("Sneak", 7, 2, 5, 1, 8, 2, 4, 4, 1, 12, 'N'));
                eL.add(E("Sneak2", 7, 2, 5, 1, 8, 2, 4, 4, 1, 12, 'M'));
                eL.add(E("Shaman", 8, 1, 2, 6, 7, 3, 2, 5, 2, 13, 'W'));
            }
            
            case 6 -> {
                grid = new BattleGrid(8, 10);
                wallDensity = 25;
                difficulty = 2;
                eL.add(E("Pikeman", 10, 4, 2, 1, 9, 2, 2, 6, 2, 12, 'P'));
                eL.add(E("Pikeman2", 10, 4, 2, 1, 9, 2, 2, 6, 2, 12, 'Q'));
            }
            
            case 7 -> {
                grid = new BattleGrid(9, 7);
                wallDensity = 10;
                difficulty = 1;
                eL.add(E("Wolf", 8, 4, 4, 1, 8, 1, 4, 5, 1, 12, 'W'));
                eL.add(E("Wolf2", 8, 4, 4, 1, 8, 1, 4, 5, 1, 12, 'V'));
                eL.add(E("Wolf3", 8, 4, 4, 1, 8, 1, 4, 5, 1, 12, 'U'));
            }
            
            case 8 -> {
                grid = new BattleGrid(10, 10);
                wallDensity = 30;
                difficulty = 3;
                eL.add(E("Captain", 16, 6, 3, 2, 10, 3, 2, 7, 1, 13, 'C'));
                eL.add(E("Archer", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, 'A'));
            }
            
            case 9 -> {
                grid = new BattleGrid(8, 8);
                wallDensity = 0;
                difficulty = 2;
                eL.add(E("Duelist", 12, 4, 5, 2, 10, 3, 3, 6, 1, 12, 'D'));
            }
            
            case 10 -> {
                grid = new BattleGrid(9, 9);
                wallDensity = 35;
                difficulty = 5;
                eL.add(E("Wall-Crawler", 9, 3, 6, 1, 9, 2, 4, 5, 1, 12, 'K'));
                eL.add(E("Wall-Crawler2", 9, 3, 6, 1, 9, 2, 4, 5, 1, 12, 'L'));
                eL.add(E("Brute", 14, 6, 1, 1, 9, 3, 2, 7, 1, 12, 'R'));
            }
            
            case 11 -> {
                grid = new BattleGrid(7, 10);
                wallDensity = 12;
                difficulty = 5;
                eL.add(E("Skirmisher", 10, 4, 4, 2, 9, 2, 3, 5, 2, 12, 'S'));
                eL.add(E("Archer", 6, 1, 4, 1, 7, 2, 2, 4, 3, 12, 'A'));
                eL.add(E("Archer2", 6, 1, 4, 1, 7, 2, 2, 4, 3, 12, 'B'));
            }
            
            case 12 -> {
                grid = new BattleGrid(10, 7);
                wallDensity = 18;
                difficulty = 5;
                eL.add(E("Shaman", 10, 1, 2, 7, 8, 3, 2, 6, 2, 13, 'Z'));
                eL.add(E("Guard", 11, 5, 2, 1, 10, 2, 2, 6, 1, 12, 'G'));
            }
            
            case 13 -> {
                grid = new BattleGrid(8, 9);
                wallDensity = 22;
                difficulty = 5;
                eL.add(E("Twin Blades", 11, 4, 6, 1, 10, 2, 3, 6, 1, 12, 'T'));
                eL.add(E("Twin Blades2", 11, 4, 6, 1, 10, 2, 3, 6, 1, 12, 'Y'));
            }
            
            case 14 -> {
                grid = new BattleGrid(9, 8);
                wallDensity = 28;
                difficulty = 5;
                eL.add(E("Raider", 13, 6, 3, 1, 10, 1, 3, 7, 1, 12, 'R'));
                eL.add(E("Raider2", 13, 6, 3, 1, 10, 1, 3, 7, 1, 12, 'S'));
                eL.add(E("Archer", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, 'A'));
            }
            
            case 15 -> {
                grid = new BattleGrid(10, 10);
                wallDensity = 40;
                difficulty = 10;
                eL.add(E("Warlord", 20, 8, 3, 2, 12, 3, 2, 8, 1, 13, 'W'));
                eL.add(E("Archer", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, 'A'));
                eL.add(E("Archer2", 7, 1, 4, 1, 7, 1, 2, 4, 3, 12, 'B'));
            }
            
            // Mythic 1 (ultra hard) - actual battle
            case 16 -> {
                grid = new BattleGrid(10, 10);
                wallDensity = 45;
                difficulty = 60;
                eL.add(E("Mythic Champion", 100, 10, 5, 3, 10, 0, 2, 9, 1, 14, 'M'));
            }
            
            // Mythic 2 (ultra hard) - Sadako event (NOT a battle)
            case 17 -> {
                grid = new BattleGrid(1, 1);
                wallDensity = 0;
                difficulty = 0;
                SadakoBattle.run(player, in);
                if (player.isAlive()) player.setGold(player.getGold() + 50);
                return;
            }
            
            default -> throw new IllegalStateException("Unexpected encounter: " + encounter);
        }

        renderer = new Renderer(grid);
        Audio.playMusicLoop("combatC1.wav", -12.0f);
        System.out.println("Forest encounter " + encounter + "/15");
        
        // IMPORTANT: use scanner-sharing overload
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
        
        Audio.playMusicLoop("combatC1.wav", -8.0f);
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