import java.util.*;

public class BattleManager {

    private boolean battleOver = false;
    private Scanner scnr;

    private int turnCount;
    private int roundCount;

    private int battleDifficulty = 0;

    public void createScriptedBattle(
        PlayerClass player,
        ArrayList<Enemy> enemies,
        BattleGrid grid,
        Renderer renderer,
        int difficulty,
        int playerX, int playerY,
        int bossX, int bossY,
        ArrayList<int[]> wallsXY
    ) {
        this.battleDifficulty = Math.max(0, difficulty);

        for (Point p : grid.getGrid()) {
            p.setOccupant(null);
            p.setLabel('.');
        }
        for (int y = 0; y < grid.getHeight(); y++) {
            for (int x = 0; x < grid.getWidth(); x++) {
                grid.setEmpty(x, y);
            }
        }

        if (wallsXY != null) {
            for (int[] w : wallsXY) {
                if (w == null || w.length < 2) continue;
                grid.setWall(w[0], w[1]);
            }
        }

        Point pp = grid.getPointAt(playerX, playerY);
        if (pp == null || pp.getIsWall() || pp.getOccupant() != null)
            throw new RuntimeException("Invalid player spawn in scripted battle.");

        pp.setOccupant(player);
        pp.setLabel(player.getGridID());

        if (enemies == null || enemies.isEmpty())
            throw new RuntimeException("Scripted battle requires at least 1 enemy.");

        Enemy first = enemies.get(0);
        Point bp = grid.getPointAt(bossX, bossY);
        if (bp == null || bp.getIsWall() || bp.getOccupant() != null)
            throw new RuntimeException("Invalid boss spawn in scripted battle.");

        bp.setOccupant(first);
        bp.setLabel(first.getGridID());

        for (int i = 1; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            boolean placed = false;

            for (int dy = -2; dy <= 2 && !placed; dy++) {
                for (int dx = -2; dx <= 2 && !placed; dx++) {
                    int sx = bossX + dx;
                    int sy = bossY + dy;
                    Point sp = grid.getPointAt(sx, sy);
                    if (sp == null) continue;
                    if (sp.getIsWall()) continue;
                    if (sp.getOccupant() != null) continue;
                    if (sx == playerX && sy == playerY) continue;

                    sp.setOccupant(e);
                    sp.setLabel(e.getGridID());
                    placed = true;
                }
            }

            if (!placed) {
                for (Point p : grid.getGrid()) {
                    if (!p.getIsWall() && p.getOccupant() == null) {
                        p.setOccupant(e);
                        p.setLabel(e.getGridID());
                        placed = true;
                        break;
                    }
                }
            }

            if (!placed) throw new RuntimeException("Could not place scripted enemy: " + e.getName());
        }

        startCombat(player, enemies, grid, renderer, this.battleDifficulty);
    }

    public void createBasicBattle(PlayerClass player, ArrayList<Enemy> enemyList, BattleGrid grid, Renderer renderer, int wallDensity, int difficulty) {
        wallDensity = Math.max(0, Math.min(100, wallDensity));
        this.battleDifficulty = Math.max(0, difficulty);

        Point playerPoint = spawnPlayer(player, grid);
        ArrayList<Point> enemyPoints = spawnEnemies(enemyList, player, grid);

        placeWallShapes(grid, playerPoint, enemyPoints, wallDensity);

        startCombat(player, enemyList, grid, renderer, this.battleDifficulty);
    }

    // scanner-sharing overloads
    public void createScriptedBattle(
        PlayerClass player,
        ArrayList<Enemy> enemies,
        BattleGrid grid,
        Renderer renderer,
        int difficulty,
        int playerX, int playerY,
        int bossX, int bossY,
        ArrayList<int[]> wallsXY,
        Scanner sharedScanner
    ) {
        this.scnr = sharedScanner;
        createScriptedBattle(player, enemies, grid, renderer, difficulty, playerX, playerY, bossX, bossY, wallsXY);
    }

    public void createBasicBattle(PlayerClass player, ArrayList<Enemy> enemyList, BattleGrid grid, Renderer renderer,
                                  int wallDensity, int difficulty, Scanner sharedScanner) {
        this.scnr = sharedScanner;
        createBasicBattle(player, enemyList, grid, renderer, wallDensity, difficulty);
    }

    private void startCombat(PlayerClass player, ArrayList<Enemy> enemyList, BattleGrid grid, Renderer renderer, int difficulty) {

        // FIX: do NOT overwrite the shared scanner
        if (scnr == null) scnr = new Scanner(System.in);

        battleOver = false;
        turnCount = 1;
        roundCount = 1;

        while (!battleOver) {
            System.out.println(String.format("Round %d!", roundCount));

            renderer.drawGrid(true);

            player.startNewTurn();
            playerTurn(player, enemyList, grid, renderer, difficulty);
            if (battleOver) break;

            for (Enemy e : new ArrayList<>(enemyList)) {
                if (battleOver) break;
                if (!e.isAlive()) continue;

                renderer.drawGrid(true);
                enemyAI(e, player, grid);
                turnCount++;

                if (!player.isAlive()) {
                    System.out.println(player.getName() + " has been defeated!");
                    battleOver = true;
                    break;
                }
            }

            roundCount++;
        }

        renderer.drawGrid(true);
    }

    private void playerTurn(PlayerClass player, ArrayList<Enemy> enemyList, BattleGrid grid, Renderer renderer, int difficulty) {

        while (!battleOver) {
            if (!player.isAlive()) {
                System.out.println(player.getName() + " has been defeated!");
                battleOver = true;
                return;
            }
            if (allEnemiesDead(enemyList)) {
                onBattleWon(player, difficulty);
                battleOver = true;
                return;
            }

            System.out.println(String.format("Turn %d; %s's turn!", turnCount, player.getName()));
            System.out.println(String.format("Health %.0f; Mana %d; AC %d", player.getHealth(), player.getMana(), player.getArmourClass()));
            System.out.println(displayPlayerOptions(player));

            String cmd = processPlayerMenuInput();

            switch (cmd) {
                case "attack" -> playerAttackMenu(player, enemyList, grid);
                case "special" -> playerSpecialMenu(player, enemyList, grid);
                case "move" -> playerMoveMode(player, grid, renderer);
                case "item" -> playerItemMenu(player);
                case "info" -> playerInfoMenu(player);
                case "scout" -> playerLook(enemyList, grid);
                case "end" -> {
                    player.onTurnEnd();
                    turnCount++;
                    return;
                }
                case "flee" -> attemptFlee(player);
                default -> System.out.println("Invalid Input");
            }
        }
    }

    private String displayPlayerOptions(PlayerClass player) {
        return "" + "[1] Attack\n" +
                "[2] Specials\n" +
                "[3] Move\n" +
                "[4] Items\n" +
                "[5] Info\n" +
                "[6] Look\n" +
                "[7] End Turn\n" +
                "[8] Flee\n" +
                String.format("Main: %d  Quick: %d  Move: %d",
                        player.getCurrentMainActions(),
                        player.getCurrentQuickActions(),
                        player.getMoveSpeed()
                );
    }

    private String processPlayerMenuInput() {
        while (true) {
            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }
            char c = raw.charAt(0);
            String move = switch (c) {
                case '1' -> "attack";
                case '2' -> "special";
                case '3' -> "move";
                case '4' -> "item";
                case '5' -> "info";
                case '6' -> "scout";
                case '7' -> "end";
                case '8' -> "flee";
                default -> null;
            };
            if (move == null) {
                System.out.println("Invalid Input");
                continue;
            }
            return move;
        }
    }

    private void playerAttackMenu(PlayerClass player, ArrayList<Enemy> enemyList, BattleGrid grid) {
        if (player.getAttackList().isEmpty()) {
            System.out.println("You have no attacks.");
            return;
        }

        final int pageSize = 3;
        int page = 0;

        while (true) {
            int total = player.getAttackList().size();
            int pageCount = (int) Math.ceil(total / (double) pageSize);

            if (page < 0) page = 0;
            if (page >= pageCount) page = pageCount - 1;

            int start = page * pageSize;
            int end = Math.min(total, start + pageSize);

            System.out.println("Attacks:");
            int displayNum = 1;
            for (int i = start; i < end; i++) {
                Attack a = player.getAttackList().get(i);
                System.out.println(String.format("[%d] %s (Range %d)", displayNum, a.getName(), a.getRange()));
                displayNum++;
            }

            if (pageCount > 1) System.out.println(String.format("Page %d/%d  (n=next, p=prev)", page + 1, pageCount));
            System.out.println("[0] Go Back");

            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }

            if (raw.equals("0") || raw.equals("back") || raw.equals("b")) return;

            if (pageCount > 1 && raw.equals("n")) {
                if (page < pageCount - 1) page++;
                else System.out.println("Already on last page.");
                continue;
            }
            if (pageCount > 1 && raw.equals("p")) {
                if (page > 0) page--;
                else System.out.println("Already on first page.");
                continue;
            }

            Integer choice;
            try {
                choice = Integer.parseInt(raw);
            } catch (Exception ex) {
                System.out.println("Invalid Input");
                continue;
            }

            if (choice < 1 || choice > (end - start)) {
                System.out.println("Invalid Input");
                continue;
            }

            int selectedIndex = start + (choice - 1);
            Attack selectedAttack = player.getAttackList().get(selectedIndex);

            Enemy target = pickEnemyTargetInRange(player, enemyList, grid, selectedAttack);
            if (target == null) continue;

            player.performAttack(selectedAttack, target);

            if (!target.isAlive()) {
                handleEnemyDefeat(player, target, enemyList, grid);
            }

            if (allEnemiesDead(enemyList)) {
                onBattleWon(player, battleDifficulty);
                battleOver = true;
            }

            return;
        }
    }

    private void playerSpecialMenu(PlayerClass player, ArrayList<Enemy> enemyList, BattleGrid grid) {
        if (player.getSpecialList().isEmpty()) {
            System.out.println("You have no specials.");
            return;
        }

        final int pageSize = 3;
        int page = 0;

        while (true) {
            int total = player.getSpecialList().size();
            int pageCount = (int) Math.ceil(total / (double) pageSize);

            if (page < 0) page = 0;
            if (page >= pageCount) page = pageCount - 1;

            int start = page * pageSize;
            int end = Math.min(total, start + pageSize);

            System.out.println("Specials:");
            int displayNum = 1;
            for (int i = start; i < end; i++) {
                Special s = player.getSpecialList().get(i);
                System.out.println(String.format("[%d] %s", displayNum, s.getName()));
                displayNum++;
            }

            if (pageCount > 1) System.out.println(String.format("Page %d/%d  (n=next, p=prev)", page + 1, pageCount));
            System.out.println("[0] Go Back");

            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }

            if (raw.equals("0") || raw.equals("back") || raw.equals("b")) return;

            if (pageCount > 1 && raw.equals("n")) {
                if (page < pageCount - 1) page++;
                else System.out.println("Already on last page.");
                continue;
            }
            if (pageCount > 1 && raw.equals("p")) {
                if (page > 0) page--;
                else System.out.println("Already on first page.");
                continue;
            }

            Integer choice;
            try {
                choice = Integer.parseInt(raw);
            } catch (Exception ex) {
                System.out.println("Invalid Input");
                continue;
            }

            if (choice < 1 || choice > (end - start)) {
                System.out.println("Invalid Input");
                continue;
            }

            int selectedIndex = start + (choice - 1);
            Special selected = player.getSpecialList().get(selectedIndex);

            Enemy target = null;
            if (selected.needsTarget()) {
                target = pickAnyAliveEnemy(enemyList);
                if (target == null) return;
            }

            player.performSpecial(selected, target);
            return;
        }
    }

    private void playerInfoMenu(PlayerClass player) {
        class Entry {
            String label;
            Entry(String label) { this.label = label; }
        }

        ArrayList<Entry> entries = new ArrayList<>();

        for (Attack a : player.getAttackList()) {
            entries.add(new Entry(a.getName() + ": " + a.getDescription()));
        }
        for (Special s : player.getSpecialList()) {
            entries.add(new Entry(s.getName() + ": " + s.getDescription()));
        }

        if (entries.isEmpty()) {
            System.out.println("No info available.");
            return;
        }

        final int pageSize = 3;
        int page = 0;

        while (true) {
            int total = entries.size();
            int pageCount = (int) Math.ceil(total / (double) pageSize);

            if (page < 0) page = 0;
            if (page >= pageCount) page = pageCount - 1;

            int start = page * pageSize;
            int end = Math.min(total, start + pageSize);

            System.out.println("Info:");
            for (int i = start; i < end; i++) {
                System.out.println(entries.get(i).label);
            }

            if (pageCount > 1) System.out.println(String.format("Page %d/%d  (n=next, p=prev)", page + 1, pageCount));
            System.out.println("[0] Go Back");

            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }

            if (raw.equals("0") || raw.equals("back") || raw.equals("b")) return;

            if (pageCount > 1 && raw.equals("n")) {
                if (page < pageCount - 1) page++;
                else System.out.println("Already on last page.");
                continue;
            }
            if (pageCount > 1 && raw.equals("p")) {
                if (page > 0) page--;
                else System.out.println("Already on first page.");
                continue;
            }

            System.out.println("Invalid Input");
        }
    }

    private Enemy pickAnyAliveEnemy(ArrayList<Enemy> enemyList) {
        ArrayList<Enemy> candidates = new ArrayList<>();
        for (Enemy e : enemyList) if (e != null && e.isAlive()) candidates.add(e);

        if (candidates.isEmpty()) {
            System.out.println("No enemies to target.");
            return null;
        }

        System.out.println("Pick a target:");
        for (int i = 0; i < candidates.size(); i++) {
            Enemy e = candidates.get(i);
            System.out.println(String.format("[%d] %c %s HP: %.0f", i + 1, e.getGridID(), e.getName(), e.getHealth()));
        }
        System.out.println("[0] Go Back");

        while (true) {
            Integer choice = readIntSafe();
            if (choice == null) {
                System.out.println("Invalid Input");
                continue;
            }
            if (choice == 0) return null;

            int idx = choice - 1;
            if (idx < 0 || idx >= candidates.size()) {
                System.out.println("Invalid Input");
                continue;
            }
            return candidates.get(idx);
        }
    }

    private Enemy pickEnemyTargetInRange(PlayerClass player, ArrayList<Enemy> enemyList, BattleGrid grid, Attack atk) {

        Point pp = findPointByOccupant(grid, player);
        if (pp == null) {
            System.out.println("Player position not found.");
            return null;
        }

        int r = atk.getRange();

        ArrayList<Enemy> candidates = new ArrayList<>();
        for (Enemy e : enemyList) {
            if (!e.isAlive()) continue;

            Point ep = findPointByOccupant(grid, e);
            if (ep == null) continue;

            int dx = Math.abs(ep.getX() - pp.getX());
            int dy = Math.abs(ep.getY() - pp.getY());
            boolean inRange = (dx <= r && dy <= r);

            if (inRange) candidates.add(e);
        }

        if (candidates.isEmpty()) {
            System.out.println("No enemies in range (Range: " + r + ").");
            return null;
        }

        System.out.println("Pick a target (Range: " + r + "):");
        for (int i = 0; i < candidates.size(); i++) {
            Enemy e = candidates.get(i);
            Point ep = findPointByOccupant(grid, e);
            System.out.println(String.format("[%d] %c %s HP: %.0f (%d,%d)", i + 1, e.getGridID(), e.getName(), e.getHealth(), ep.getX(), ep.getY()));
        }
        System.out.println("[0] Go Back");

        while (true) {
            Integer choice = readIntSafe();
            if (choice == null) {
                System.out.println("Invalid Input");
                continue;
            }
            if (choice == 0) return null;

            int idx = choice - 1;
            if (idx < 0 || idx >= candidates.size()) {
                System.out.println("Invalid Input");
                continue;
            }
            return candidates.get(idx);
        }
    }

    private void playerItemMenu(PlayerClass player) {
        while (true) {
            System.out.println("Items:");
            System.out.println("[1] Consumables");
            System.out.println("[2] Ammo");
            System.out.println("[3] Weapons (Equip)");
            System.out.println("[0] Go Back");

            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }

            char c = raw.charAt(0);
            if (c == '0' || raw.equals("back") || raw.equals("b")) return;

            if (c == '1') itemConsumablesMenu(player);
            else if (c == '2') itemAmmoMenu(player);
            else if (c == '3') itemWeaponsMenu(player);
            else System.out.println("Invalid Input");
        }
    }

    private void itemConsumablesMenu(PlayerClass player) {
        Map<Consumable, Integer> inv = player.getConsumableInventoryView();
        if (inv.isEmpty()) {
            System.out.println("You have no consumables.");
            return;
        }

        ArrayList<Consumable> items = new ArrayList<>(inv.keySet());

        final int pageSize = 3;
        int page = 0;

        while (true) {
            int total = items.size();
            int pageCount = (int) Math.ceil(total / (double) pageSize);

            if (page < 0) page = 0;
            if (page >= pageCount) page = pageCount - 1;

            int start = page * pageSize;
            int end = Math.min(total, start + pageSize);

            System.out.println("Consumables:");
            int displayNum = 1;
            for (int i = start; i < end; i++) {
                Consumable it = items.get(i);
                int qty = inv.getOrDefault(it, 0);
                System.out.println(String.format("[%d] %s x%d", displayNum, it.getName(), qty));
                displayNum++;
            }

            if (pageCount > 1) System.out.println(String.format("Page %d/%d  (n=next, p=prev)", page + 1, pageCount));
            System.out.println("[0] Go Back");

            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }

            if (raw.equals("0") || raw.equals("back") || raw.equals("b")) return;

            if (pageCount > 1 && raw.equals("n")) {
                if (page < pageCount - 1) page++;
                else System.out.println("Already on last page.");
                continue;
            }
            if (pageCount > 1 && raw.equals("p")) {
                if (page > 0) page--;
                else System.out.println("Already on first page.");
                continue;
            }

            Integer choice;
            try { choice = Integer.parseInt(raw); }
            catch (Exception ex) { System.out.println("Invalid Input"); continue; }

            if (choice < 1 || choice > (end - start)) {
                System.out.println("Invalid Input");
                continue;
            }

            int selectedIndex = start + (choice - 1);
            Consumable selected = items.get(selectedIndex);

            player.useConsumable(selected);
            return;
        }
    }

    private void itemAmmoMenu(PlayerClass player) {
        Map<Ammo, Integer> inv = player.getAmmoInventoryView();
        if (inv.isEmpty()) {
            System.out.println("You have no ammo.");
            return;
        }

        System.out.println("Ammo:");
        for (Map.Entry<Ammo, Integer> e : inv.entrySet()) {
            Ammo ammo = e.getKey();
            int qty = e.getValue();
            System.out.println(String.format("- %s x%d", ammo.getName(), qty));
        }

        System.out.println("[0] Go Back");
        while (true) {
            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) { System.out.println("Invalid Input"); continue; }
            if (raw.equals("0") || raw.equals("back") || raw.equals("b")) return;
            System.out.println("Invalid Input");
        }
    }

    private void itemWeaponsMenu(PlayerClass player) {
        LinkedList<Weapon> weapons = player.getWeaponInventory();
        if (weapons == null || weapons.isEmpty()) {
            System.out.println("You have no weapons.");
            return;
        }

        final int pageSize = 3;
        int page = 0;

        while (true) {
            int total = weapons.size();
            int pageCount = (int) Math.ceil(total / (double) pageSize);

            if (page < 0) page = 0;
            if (page >= pageCount) page = pageCount - 1;

            int start = page * pageSize;
            int end = Math.min(total, start + pageSize);

            System.out.println("Weapons (Equipped: " + player.getEquippedWeaponName() + "):");
            int displayNum = 1;
            for (int i = start; i < end; i++) {
                Weapon w = weapons.get(i);
                System.out.println(String.format("[%d] %s (DMG+%d, DUR %.0f)",
                        displayNum,
                        w.getName(),
                        w.getDmgBonus(),
                        w.durability
                ));
                displayNum++;
            }

            if (pageCount > 1) System.out.println(String.format("Page %d/%d  (n=next, p=prev)", page + 1, pageCount));
            System.out.println("[0] Go Back");

            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) { System.out.println("Invalid Input"); continue; }

            if (raw.equals("0") || raw.equals("back") || raw.equals("b")) return;

            if (pageCount > 1 && raw.equals("n")) {
                if (page < pageCount - 1) page++;
                else System.out.println("Already on last page.");
                continue;
            }
            if (pageCount > 1 && raw.equals("p")) {
                if (page > 0) page--;
                else System.out.println("Already on first page.");
                continue;
            }

            Integer choice;
            try { choice = Integer.parseInt(raw); }
            catch (Exception ex) { System.out.println("Invalid Input"); continue; }

            if (choice < 1 || choice > (end - start)) {
                System.out.println("Invalid Input");
                continue;
            }

            int selectedIndex = start + (choice - 1);
            player.equipWeapon(selectedIndex);

            return;
        }
    }

    private void playerMoveMode(PlayerClass player, BattleGrid grid, Renderer renderer) {
        int moveLeft = Math.max(0, player.getMoveSpeed());
        if (moveLeft == 0) {
            System.out.println("No move speed available.");
            return;
        }

        System.out.println("Move mode: use WASD to move 1 tile each. Type 'done' to exit move mode.");
        while (!battleOver) {
            moveLeft = Math.max(0, player.getMoveSpeed());
            if (moveLeft == 0) return;

            System.out.println("Move left: " + moveLeft);
            renderer.drawGrid(true);

            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) { System.out.println("Invalid Input"); continue; }

            if (raw.equals("done") || raw.equals("back") || raw.equals("b") || raw.equals("0")) return;

            int dx = 0, dy = 0;
            char c = raw.charAt(0);
            if (c == 'w') dy = -1;
            else if (c == 's') dy = 1;
            else if (c == 'a') dx = -1;
            else if (c == 'd') dx = 1;
            else { System.out.println("Invalid Input"); continue; }

            boolean moved = tryMoveEntity(grid, player, dx, dy);
            if (moved) player.spendMoveSpeed(1);
            else System.out.println("Blocked.");
        }
    }

    private boolean tryMoveEntity(BattleGrid grid, Entity e, int dx, int dy) {
        Point cur = findPointByOccupant(grid, e);
        if (cur == null) return false;

        int nx = cur.getX() + dx;
        int ny = cur.getY() + dy;

        Point next = grid.getPointAt(nx, ny);
        if (next == null) return false;
        if (next.getIsWall()) return false;
        if (next.getOccupant() != null) return false;

        cur.setOccupant(null);
        cur.setLabel('.');

        next.setOccupant(e);
        next.setLabel(e.getGridID());
        return true;
    }

    private void playerLook(ArrayList<Enemy> enemyList, BattleGrid grid) {
        System.out.println("Enemies:");
        for (Enemy e : enemyList) {
            if (!e.isAlive()) continue;
            Point ep = findPointByOccupant(grid, e);
            String pos = (ep == null) ? "(?,?)" : "(" + ep.getX() + "," + ep.getY() + ")";
            System.out.println(String.format("%c -> %s | HP: %.0f | %s", e.getGridID(), e.getName(), e.getHealth(), pos));
        }
    }

    private void attemptFlee(PlayerClass player) {
        System.out.println("Are you sure you want to flee? (yes/no)");
        String ans = scnr.nextLine().trim().toLowerCase();
        if (!ans.equals("yes") && !ans.equals("y")) {
            System.out.println("Flee cancelled.");
            return;
        }

        System.out.println("Type 'flee' to confirm.");
        String confirm = scnr.nextLine().trim().toLowerCase();
        if (!confirm.equals("flee")) {
            System.out.println("Flee cancelled.");
            return;
        }

        player.setHealth(player.getHealth() - 2);
        System.out.println("You fled. -2 HP.");

        if (!player.isAlive()) {
            System.out.println(player.getName() + " died while fleeing.");
        }

        battleOver = true;
    }

    private void handleEnemyDefeat(PlayerClass player, Enemy dead, ArrayList<Enemy> enemyList, BattleGrid grid) {

        int g = dead.getGold();
        if (g > 0) {
            player.setGold(player.getGold() + g);
            System.out.println("Gained " + g + " gold from " + dead.getName() + ".");
            // BANK TRACKER HOOK (eligible combat earnings)
            if (player.getBank() != null) {
                player.getBank().onGoldEarned(g, Bank.EarnSource.COMBAT_REWARD);
            }
        }

        Point ep = findPointByOccupant(grid, dead);
        if (ep != null) {
            ep.setOccupant(null);
            ep.setLabel('.');
        }

        enemyList.remove(dead);

        player.onKill(scnr);
    }

    private void onBattleWon(PlayerClass player, int difficulty) {
        System.out.println("All enemies defeated!");
        int reward = Math.max(0, difficulty);
        if (reward > 0) {
            player.setGold(player.getGold() + reward);
            System.out.println("Map clear reward: +" + reward + " gold.");
            // BANK TRACKER HOOK (eligible combat earnings)
            if (player.getBank() != null) {
                player.getBank().onGoldEarned(reward, Bank.EarnSource.COMBAT_REWARD);
            }
        }
    }

    private boolean allEnemiesDead(ArrayList<Enemy> enemyList) {
        for (Enemy e : enemyList) if (e.isAlive()) return false;
        return true;
    }

    private void enemyAI(Enemy enemy, PlayerClass player, BattleGrid grid) {
        System.out.println(String.format("Turn %d; %s's turn!", turnCount, enemy.getName()));

        if (!enemy.isAlive()) return;
        if (!player.isAlive()) { battleOver = true; return; }

        if (isInRange(grid, enemy, player, enemy.getRange())) {
            System.out.println(enemy.getName() + " attacks!");

            if (enemy instanceof Dragon d) d.takeBossTurn(player, grid);
            else enemy.attackWithCrit(player);

            if (!player.isAlive()) battleOver = true;
            return;
        }

        moveEnemyTowardPlayer(enemy, player, grid);

        if (!enemy.isAlive()) return;
        if (!player.isAlive()) { battleOver = true; return; }

        if (isInRange(grid, enemy, player, enemy.getRange())) {
            System.out.println(enemy.getName() + " attacks!");

            if (enemy instanceof Dragon d) d.takeBossTurn(player, grid);
            else enemy.attackWithCrit(player);

            if (!player.isAlive()) battleOver = true;
        }
    }

    private boolean isInRange(BattleGrid grid, Entity a, Entity b, int range) {
        if (range < 0) return false;

        Point pa = findPointByOccupant(grid, a);
        Point pb = findPointByOccupant(grid, b);
        if (pa == null || pb == null) return false;

        int dx = Math.abs(pa.getX() - pb.getX());
        int dy = Math.abs(pa.getY() - pb.getY());
        return dx <= range && dy <= range;
    }

    private void moveEnemyTowardPlayer(Enemy enemy, PlayerClass player, BattleGrid grid) {
        int steps = Math.max(0, enemy.getMoveSpeed());
        if (steps == 0) return;

        for (int i = 0; i < steps; i++) {
            if (!enemy.isAlive() || !player.isAlive()) return;

            Point ep = findPointByOccupant(grid, enemy);
            Point pp = findPointByOccupant(grid, player);
            if (ep == null || pp == null) return;

            int dx = Integer.compare(pp.getX(), ep.getX());
            int dy = Integer.compare(pp.getY(), ep.getY());

            if (dx != 0 && tryMoveEntity(grid, enemy, dx, 0)) continue;
            if (dy != 0 && tryMoveEntity(grid, enemy, 0, dy)) continue;

            if (dx != 0) {
                if (tryMoveEntity(grid, enemy, dx, 1)) continue;
                if (tryMoveEntity(grid, enemy, dx, -1)) continue;
            }
            if (dy != 0) {
                if (tryMoveEntity(grid, enemy, 1, dy)) continue;
                if (tryMoveEntity(grid, enemy, -1, dy)) continue;
            }

            return;
        }
    }

    private Point findPointByOccupant(BattleGrid grid, Entity e) {
        for (Point p : grid.getGrid()) {
            if (p.getOccupant() == e) return p;
        }
        return null;
    }

    private Integer readIntSafe() {
        String raw = scnr.nextLine().trim();
        if (raw.isEmpty()) return null;
        try { return Integer.parseInt(raw); }
        catch (Exception e) { return null; }
    }

    private Point spawnPlayer(PlayerClass player, BattleGrid grid) {
        int attempts = 0;
        Point p;

        do {
            int x = getRandomNumber(grid.getWidth());
            int y = getRandomNumber(grid.getHeight());
            p = grid.getPointAt(x, y);
            attempts++;
        } while ((p == null || p.getIsWall() || p.getOccupant() != null) && attempts < 200);

        if (p == null) throw new RuntimeException("Failed to find a spawn tile for player.");

        p.setOccupant(player);
        p.setLabel(player.getGridID());
        return p;
    }

    private ArrayList<Point> spawnEnemies(ArrayList<Enemy> enemyList, PlayerClass player, BattleGrid grid) {
        ArrayList<Point> enemyPoints = new ArrayList<>();
        final int maxAttemptsPerEnemy = 30;

        for (Enemy enemy : enemyList) {
            for (int attempt = 0; attempt < maxAttemptsPerEnemy; attempt++) {
                int x = getRandomNumber(grid.getWidth());
                int y = getRandomNumber(grid.getHeight());
                Point spawn = grid.getPointAt(x, y);

                if (spawn == null) continue;
                if (spawn.getIsWall()) continue;
                if (spawn.getOccupant() != null) continue;

                ArrayList<Point> radius = grid.getPointsInRadius(spawn, 1);
                boolean tooCloseToPlayer = false;
                for (Point rp : radius) {
                    if (rp.getOccupant() == player) { tooCloseToPlayer = true; break; }
                }
                if (tooCloseToPlayer) continue;

                spawn.setOccupant(enemy);
                spawn.setLabel(enemy.getGridID());
                enemyPoints.add(spawn);
                break;
            }
        }

        return enemyPoints;
    }

    private void placeWallShapes(BattleGrid grid, Point playerPoint, ArrayList<Point> enemyPoints, int wallDensity) {

        int totalCells = grid.getWidth() * grid.getHeight();
        int targetWallCells = (int) Math.round(totalCells * (wallDensity / 100.0));

        int maxPlacementAttempts = 10;

        int placedWalls = 0;
        int attempts = 0;

        while (placedWalls < targetWallCells && attempts < maxPlacementAttempts) {
            attempts++;

            boolean makeRect = Math.random() < 0.5;

            ArrayList<int[]> shape = makeRect ? generateRectangleShape(grid) : generateLShape(grid);

            if (shape.isEmpty()) continue;

            if (!canPlaceShape(grid, shape)) continue;

            applyWalls(grid, shape);

            boolean ok = pathsExist(grid, playerPoint, enemyPoints);

            if (!ok) {
                removeWalls(grid, shape);
                continue;
            }

            placedWalls += shape.size();
        }
    }

    private ArrayList<int[]> generateRectangleShape(BattleGrid grid) {
        int wMax = Math.min(6, grid.getWidth());
        int hMax = Math.min(6, grid.getHeight());

        int w = randRange(2, Math.max(2, wMax));
        int h = randRange(2, Math.max(2, hMax));

        int x0 = randRange(0, grid.getWidth() - 1);
        int y0 = randRange(0, grid.getHeight() - 1);

        ArrayList<int[]> cells = new ArrayList<>();

        for (int y = y0; y < y0 + h; y++) {
            for (int x = x0; x < x0 + w; x++) {
                if (grid.getPointAt(x, y) == null) return new ArrayList<>();
                cells.add(new int[]{x, y});
            }
        }

        return cells;
    }

    private ArrayList<int[]> generateLShape(BattleGrid grid) {
        int legMax = Math.min(7, Math.max(grid.getWidth(), grid.getHeight()));
        int a = randRange(2, Math.max(2, legMax));
        int b = randRange(2, Math.max(2, legMax));

        int x0 = randRange(0, grid.getWidth() - 1);
        int y0 = randRange(0, grid.getHeight() - 1);
        int orient = randRange(0, 3);

        ArrayList<int[]> cells = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();

        java.util.function.BiConsumer<Integer, Integer> add = (x, y) -> {
            if (grid.getPointAt(x, y) == null) return;
            String key = x + "," + y;
            if (seen.add(key)) cells.add(new int[]{x, y});
        };

        int dx1 = (orient == 0 || orient == 1) ? 1 : -1;
        int dy2 = (orient == 0 || orient == 2) ? 1 : -1;

        for (int i = 0; i < a; i++) add.accept(x0 + i * dx1, y0);
        for (int j = 0; j < b; j++) add.accept(x0, y0 + j * dy2);

        for (int[] c : cells) {
            if (grid.getPointAt(c[0], c[1]) == null) return new ArrayList<>();
        }

        return cells;
    }

    private boolean canPlaceShape(BattleGrid grid, ArrayList<int[]> shape) {
        for (int[] c : shape) {
            Point p = grid.getPointAt(c[0], c[1]);
            if (p == null) return false;
            if (p.getIsWall()) return false;
            if (p.getOccupant() != null) return false;
        }
        return true;
    }

    private void applyWalls(BattleGrid grid, ArrayList<int[]> shape) {
        for (int[] c : shape) grid.setWall(c[0], c[1]);
    }

    private void removeWalls(BattleGrid grid, ArrayList<int[]> shape) {
        for (int[] c : shape) grid.setEmpty(c[0], c[1]);
    }

    private boolean pathsExist(BattleGrid grid, Point start, ArrayList<Point> enemyPoints) {
        if (start == null) return false;
        if (enemyPoints == null || enemyPoints.isEmpty()) return true;

        boolean[][] visited = new boolean[grid.getHeight()][grid.getWidth()];
        ArrayDeque<Point> q = new ArrayDeque<>();
        q.add(start);
        visited[start.getY()][start.getX()] = true;

        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        while (!q.isEmpty()) {
            Point cur = q.removeFirst();

            for (int k = 0; k < 4; k++) {
                int nx = cur.getX() + dx[k];
                int ny = cur.getY() + dy[k];
                Point np = grid.getPointAt(nx, ny);

                if (np == null) continue;
                if (visited[ny][nx]) continue;
                if (np.getIsWall()) continue;

                visited[ny][nx] = true;
                q.add(np);
            }
        }

        for (Point ep : enemyPoints) {
            if (ep == null) continue;
            if (!visited[ep.getY()][ep.getX()]) return false;
        }

        return true;
    }

    private int getRandomNumber(int max) { return (int) (Math.random() * max); }

    private int randRange(int minInclusive, int maxInclusive) {
        if (maxInclusive < minInclusive) return minInclusive;
        int span = (maxInclusive - minInclusive) + 1;
        return minInclusive + (int) (Math.random() * span);
    }
}
