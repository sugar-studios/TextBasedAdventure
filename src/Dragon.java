public class Dragon extends Enemy {

    private int bossTurn = 0;

    private boolean charging = false;
    private int chargeTurnsLeft = 0;

    public Dragon(String name, char gridID) {

        super(name, 82.0, 10, 6, 4, 12, 150, 2, 10, 3, 12, gridID);
    }

    public void takeBossTurn(PlayerClass player, BattleGrid grid) {
        bossTurn++;

        boolean rage = (bossTurn % 5 == 0);
        if (rage) {
            System.out.println("The dragon is enraged and attacks fiercely!");
        }

        if (charging) {
            resolveCharge(player, grid, rage);
            return;
        }

        if (shouldStartCharge(player, grid)) {
            startCharge();
            return;
        }

        doNormalAttack(player, grid, rage);
    }

    private boolean shouldStartCharge(PlayerClass player, BattleGrid grid) {
        if (bossTurn % 7 != 0) return false;

        Point dp = findPointByOccupant(grid, this);
        Point pp = findPointByOccupant(grid, player);
        if (dp == null || pp == null) return false;

        return grid.hasLineOfSight(dp, pp);
    }

    private void startCharge() {
        charging = true;
        chargeTurnsLeft = 2;
        System.out.println("The dragon rears back and inhales deeply...");
        System.out.println("Heat ripples through the air. You have 2 turns to get behind a wall!");
    }

    private void resolveCharge(PlayerClass player, BattleGrid grid, boolean rage) {
        chargeTurnsLeft--;

        if (chargeTurnsLeft > 0) {
            System.out.println("The dragon keeps charging its inferno...");
            System.out.println("You feel the temperature climbing.");
            return;
        }

        // Fire the beam now
        charging = false;

        Point dp = findPointByOccupant(grid, this);
        Point pp = findPointByOccupant(grid, player);
        if (dp == null || pp == null) {
            System.out.println("The dragon's inferno fizzles oddly.");
            return;
        }

        boolean hasLoS = grid.hasLineOfSight(dp, pp);

        if (!hasLoS) {
            System.out.println("The dragon unleashes an INFERNO BEAM!");
            System.out.println("Flames carve across the battlefield, but the wall shields you!");
            return;
        }

        // If not behind a wall -> big damage
        int dmg = randInclusive(18, 30);
        if (rage) dmg *= 2;

        System.out.println("The dragon unleashes an INFERNO BEAM!");
        System.out.println("The fire hits you full-force!");
        applyDamage(player, dmg);
    }

    private void doNormalAttack(PlayerClass player, BattleGrid grid, boolean rage) {
        Point dp = findPointByOccupant(grid, this);
        Point pp = findPointByOccupant(grid, player);

        int dist = chebyshevDistance(dp, pp);

        // Prefer melee if close, otherwise ranged options
        if (dist <= 1) {
            int roll = randInclusive(1, 100);
            if (roll <= 55) {
                claw(player, rage);
            } else {
                bite(player, rage);
            }
            return;
        }
        if (dist <= 3 && randInclusive(1, 100) <= 30) {
            wingGust(player, rage);
            return;
        }

        if (dp != null && pp != null && grid.hasLineOfSight(dp, pp) && randInclusive(1, 100) <= 45) {
            fireball(player, rage);
            return;
        }

        claw(player, rage);
    }

    // --- Attacks ---

    // Requested baseline: 5–12 random damage
    private void claw(PlayerClass player, boolean rage) {
        int dmg = randInclusive(5, 12);
        if (rage) dmg *= 2;

        System.out.println(getName() + " rakes you with its claws!");
        applyDamage(player, dmg);
    }

    // Extra attack #1: big melee
    private void bite(PlayerClass player, boolean rage) {
        int dmg = randInclusive(9, 16);
        if (rage) dmg *= 2;

        System.out.println(getName() + " bites down with crushing force!");
        applyDamage(player, dmg);
    }

    // Extra attack #2: ranged
    private void fireball(PlayerClass player, boolean rage) {
        int dmg = randInclusive(6, 14);
        if (rage) dmg *= 2;

        System.out.println(getName() + " hurls a fireball!");
        applyDamage(player, dmg);
    }

    // Extra attack #3: medium range + minor utility
    private void wingGust(PlayerClass player, boolean rage) {
        int dmg = randInclusive(4, 10);
        if (rage) dmg *= 2;

        System.out.println(getName() + " beats its wings—an explosive gust slams into you!");
        applyDamage(player, dmg);

        if (player.getMoveSpeed() > 0) {
            player.spendMoveSpeed(1);
            System.out.println("The gust throws off your footing (-1 Move this turn).");
        }
    }


    private void applyDamage(PlayerClass player, int dmg) {
        player.setHealth(player.getHealth() - dmg);
        System.out.println("You take " + dmg + " damage!");

        if (!player.isAlive()) {
            System.out.println(player.name + " has been defeated!");
        }
    }

    private int randInclusive(int min, int max) {
        if (max < min) return min;
        return min + (int)(Math.random() * ((max - min) + 1));
    }

    private int chebyshevDistance(Point a, Point b) {
        if (a == null || b == null) return 999;
        int dx = Math.abs(a.getX() - b.getX());
        int dy = Math.abs(a.getY() - b.getY());
        return Math.max(dx, dy);
    }

    private Point findPointByOccupant(BattleGrid grid, Entity e) {
        for (Point p : grid.getGrid()) {
            if (p.getOccupant() == e) return p;
        }
        return null;
    }
}
