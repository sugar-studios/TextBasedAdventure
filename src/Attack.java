/*
Basically defines an interface of the attack's name, desc, range, if it can be used and its effect

the fisrt four are kinda just... tests?

the rest are kinda self explanatory, mostly is the interface below that tells you everythin gyou need to know
*/




interface Attack {
    String getName();
    String getDescription();
    int getRange();
    boolean canUse(PlayerClass player);
    void execute(PlayerClass player, Enemy enemy);
}

// Base attacks
class BaseAttack extends Action implements Attack {
    private final int mainCost;
    private final int quickCost;
    private final String description;

    public BaseAttack() {
        super(4, 1, "Basic Attack");
        this.hitChance = 12;
        this.mainCost = 1;
        this.quickCost = 0;
        this.description = "A basic melee attack.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentMainActions() >= mainCost && player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        int hitBonus = HitChance.checkHitChanceType(hitChance, player);
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - dmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + dmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " has been defeated!");
        }
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class SwordAttack extends Action implements Attack {
    private final int mainCost;
    private final int quickCost;
    private final String description;

    public SwordAttack() {
        super(5, 1, "Sword Attack");
        this.hitChance = 12;
        this.mainCost = 1;
        this.quickCost = 0;
        this.description = "Attack with your equipped sword. Uses sword durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemDullSword) || (w instanceof ItemSword) || (w instanceof ItemGildedSword);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon sword = player.getCurrentWeapon();
        if (!((sword instanceof ItemDullSword) || (sword instanceof ItemSword) || (sword instanceof ItemGildedSword))) {
            System.out.println("You need to have a sword equipped to use Sword Attack.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        int hitBonus = HitChance.checkHitChanceType(hitChance, player);
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int totalDmg = this.dmg + sword.getDmgBonus() + player.getTempDmgBonus();

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(sword, 1);

        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " has been defeated!");
        }
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class SpearAttack extends Action implements Attack {
    private final int mainCost;
    private final int quickCost;
    private final String description;

    public SpearAttack() {
        super(5, 2, "Spear Attack");
        this.hitChance = 12;
        this.mainCost = 1;
        this.quickCost = 0;
        this.description = "Attack with your equipped spear. Uses spear durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemShoddySpear) || (w instanceof ItemSpear) || (w instanceof ItemAttunedSpear);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon spear = player.getCurrentWeapon();
        if (!((spear instanceof ItemShoddySpear) || (spear instanceof ItemSpear) || (spear instanceof ItemAttunedSpear))) {
            System.out.println("You need to have a spear equipped to use Spear Attack.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        int hitBonus = HitChance.checkHitChanceType(hitChance, player);
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int totalDmg = this.dmg + spear.getDmgBonus() + player.getTempDmgBonus();

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(spear, 1);

        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " has been defeated!");
        }
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class BowAttack extends Action implements Attack {
    private final int mainCost;
    private final int quickCost;
    private final String description;

    public BowAttack() {
        super(4, 10, "Bow Attack");
        this.hitChance = 12;
        this.mainCost = 1;
        this.quickCost = 0;
        this.description = "Ranged attack. Consumes 1 arrow.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) return false;
        Ammo ammo = player.getBestAvailableArrow();
        return ammo != null && player.getAmmoCount(ammo) >= 1;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Ammo ammo = player.getBestAvailableArrow();
        if (ammo == null || player.getAmmoCount(ammo) < 1) {
            System.out.println("You need at least 1 arrow to use Bow Attack.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        int hitBonus = HitChance.checkHitChanceType(hitChance, player);
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int totalDmg = this.dmg + ammo.getDmgBonus() + player.getTempDmgBonus();

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The shot missed!");
        }

        player.removeAmmo(ammo, 1);

        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " has been defeated!");
        }
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

//Fighter Attacks
class FighterSwordAttack extends Action implements Attack {
    private final int mainCost;
    private final int quickCost;
    private final String description;

    public FighterSwordAttack() {
        super(5, 1, "Sword Attack");
        this.hitChance = 12;
        this.mainCost = 1;
        this.quickCost = 0;
        this.description = "Fighter sword attack. After you move, your next Sword Attack gets +2 damage. Feint can guarantee the hit. Uses sword durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemDullSword) || (w instanceof ItemSword) || (w instanceof ItemGildedSword);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon sword = player.getCurrentWeapon();
        if (!((sword instanceof ItemDullSword) || (sword instanceof ItemSword) || (sword instanceof ItemGildedSword))) {
            System.out.println("You need to have a sword equipped to use Sword Attack.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        int hitBonus = HitChance.checkHitChanceType(hitChance, player);
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        boolean guaranteed = (player instanceof Fighter f) && f.consumeGuaranteedHit();
        if (guaranteed) hit = true;

        int moveBonus = (player instanceof Fighter f) ? f.consumeMoveSwordBonus() : 0;
        int totalDmg = this.dmg + sword.getDmgBonus() + player.getTempDmgBonus() + moveBonus;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(sword, 1);

        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " has been defeated!");
        }
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class ThrustAttack extends Action implements Attack {
    private final int mainCost;
    private final int moveCost;
    private final String description;

    public ThrustAttack() {
        super(7, 2, "Thrust");
        this.mainCost = 1;
        this.moveCost = 1;
        this.description = "Damage 7, range 2. Hit bonus = (STR+DEX)/2. Cost: 1 Main + 1 Move. -1 AC. Uses sword durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        if (player.getMoveSpeed() < moveCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemDullSword) || (w instanceof ItemSword) || (w instanceof ItemGildedSword);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon sword = player.getCurrentWeapon();
        if (!((sword instanceof ItemDullSword) || (sword instanceof ItemSword) || (sword instanceof ItemGildedSword))) {
            System.out.println("You need to have a sword equipped to use Thrust.");
            return;
        }

        if (player.getCurrentMainActions() < mainCost || player.getMoveSpeed() < moveCost) {
            System.out.println("Not enough actions to use Thrust.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendMoveSpeed(moveCost);
        player.setArmourClass(player.getArmourClass() - 1);

        int statBonus = (player.getStr() + player.getDex()) / 2;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), statBonus);

        boolean guaranteed = (player instanceof Fighter f) && f.consumeGuaranteedHit();
        if (guaranteed) hit = true;

        int moveSwordBonus = (player instanceof Fighter f) ? f.consumeMoveSwordBonus() : 0;
        int totalDmg = this.dmg + sword.getDmgBonus() + player.getTempDmgBonus() + moveSwordBonus;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(sword, 1);

        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " has been defeated!");
        }
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class DashSliceAttack extends Action implements Attack {
    private final int mainCost;
    private final String description;

    public DashSliceAttack() {
        super(2, 1, "Dash Slice");
        this.mainCost = 1;
        this.description = "Damage = 2 + current Move Speed, range 1. Hit bonus = (STR+DEX)/2 + 1. Cost: 1 Main. -2 AC. Uses sword durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemDullSword) || (w instanceof ItemSword) || (w instanceof ItemGildedSword);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon sword = player.getCurrentWeapon();
        if (!((sword instanceof ItemDullSword) || (sword instanceof ItemSword) || (sword instanceof ItemGildedSword))) {
            System.out.println("You need to have a sword equipped to use Dash Slice.");
            return;
        }

        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Dash Slice.");
            return;
        }

        player.spendMainAction(mainCost);
        player.setArmourClass(player.getArmourClass() - 2);

        int statBonus = (player.getStr() + player.getDex()) / 2 + 1;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), statBonus);

        boolean guaranteed = (player instanceof Fighter f) && f.consumeGuaranteedHit();
        if (guaranteed) hit = true;

        int dynamic = Math.max(0, player.getMoveSpeed());
        int moveSwordBonus = (player instanceof Fighter f) ? f.consumeMoveSwordBonus() : 0;

        int totalDmg = 2 + dynamic + sword.getDmgBonus() + player.getTempDmgBonus() + moveSwordBonus;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(sword, 1);

        if (!enemy.isAlive()) {
            System.out.println(enemy.getName() + " has been defeated!");
        }
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}


//Soldier

class SpearJabAttack extends Action implements Attack {
    private final int mainCost = 1;
    private final String description;

    public SpearJabAttack() {
        super(6, 2, "Spear Jab");
        this.description = "Damage 6, range 2. Hit bonus = (STR+DEX)/2 + 2. Cost: 1 Main. Uses spear durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemShoddySpear) || (w instanceof ItemSpear) || (w instanceof ItemAttunedSpear);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon spear = player.getCurrentWeapon();
        if (!((spear instanceof ItemShoddySpear) || (spear instanceof ItemSpear) || (spear instanceof ItemAttunedSpear))) {
            System.out.println("You need to have a spear equipped to use Spear Jab.");
            return;
        }

        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Spear Jab.");
            return;
        }

        player.spendMainAction(mainCost);

        int brace = (player instanceof Soldier s) ? s.consumeBraceHitBonus() : 0;
        int statBonus = (player.getStr() + player.getDex()) / 2 + 2 + brace;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), statBonus);

        int dmgMod = (player instanceof Soldier s) ? s.consumeNextAttackDamageMod() : 0;
        int totalDmg = this.dmg + spear.getDmgBonus() + player.getTempDmgBonus() + dmgMod;
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(spear, 1);

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class DrivingThrustAttack extends Action implements Attack {
    private final int mainCost = 1;
    private final String description;

    public DrivingThrustAttack() {
        super(8, 2, "Driving Thrust");
        this.description = "Damage 8, range 2. Hit bonus = (STR+DEX)/2. Cost: 1 Main. Uses spear durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemShoddySpear) || (w instanceof ItemSpear) || (w instanceof ItemAttunedSpear);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon spear = player.getCurrentWeapon();
        if (!((spear instanceof ItemShoddySpear) || (spear instanceof ItemSpear) || (spear instanceof ItemAttunedSpear))) {
            System.out.println("You need to have a spear equipped to use Driving Thrust.");
            return;
        }

        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Driving Thrust.");
            return;
        }

        player.spendMainAction(mainCost);

        int brace = (player instanceof Soldier s) ? s.consumeBraceHitBonus() : 0;
        int statBonus = (player.getStr() + player.getDex()) / 2 + brace;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), statBonus);

        int dmgMod = (player instanceof Soldier s) ? s.consumeNextAttackDamageMod() : 0;
        int totalDmg = this.dmg + spear.getDmgBonus() + player.getTempDmgBonus() + dmgMod;
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(spear, 1);

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class SweepingHaftAttack extends Action implements Attack {
    private final int quickCost = 1;
    private final String description;

    public SweepingHaftAttack() {
        super(4, 1, "Sweeping Haft");
        this.description = "Damage 4, range 1. Hit bonus = (STR+DEX)/2 + 1. Cost: 1 Quick. Uses spear durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentQuickActions() < quickCost) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemShoddySpear) || (w instanceof ItemSpear) || (w instanceof ItemAttunedSpear);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Weapon spear = player.getCurrentWeapon();
        if (!((spear instanceof ItemShoddySpear) || (spear instanceof ItemSpear) || (spear instanceof ItemAttunedSpear))) {
            System.out.println("You need to have a spear equipped to use Sweeping Haft.");
            return;
        }

        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough actions to use Sweeping Haft.");
            return;
        }

        player.spendQuickAction(quickCost);

        int brace = (player instanceof Soldier s) ? s.consumeBraceHitBonus() : 0;
        int statBonus = (player.getStr() + player.getDex()) / 2 + 1 + brace;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), statBonus);

        int dmgMod = (player instanceof Soldier s) ? s.consumeNextAttackDamageMod() : 0;
        int totalDmg = this.dmg + spear.getDmgBonus() + player.getTempDmgBonus() + dmgMod;
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(spear, 1);

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class SetForChargeAttack extends Action implements Attack {
    private final int mainCost = 1;
    private final String description;

    public SetForChargeAttack() {
        super(10, 2, "Set for Charge");
        this.description = "Damage 10, range 2. Hit bonus = (STR+DEX)/2 - 1. Cost: 1 Main. Only if you did not move this turn. Drawback: -2 AC until start of your next turn. Uses spear durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        if (!(player instanceof Soldier s) || s.hasMovedThisTurn()) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemShoddySpear) || (w instanceof ItemSpear) || (w instanceof ItemAttunedSpear);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        if (!(player instanceof Soldier soldier)) {
            System.out.println("Only a Soldier can use Set for Charge.");
            return;
        }

        Weapon spear = player.getCurrentWeapon();
        if (!((spear instanceof ItemShoddySpear) || (spear instanceof ItemSpear) || (spear instanceof ItemAttunedSpear))) {
            System.out.println("You need to have a spear equipped to use Set for Charge.");
            return;
        }

        if (soldier.hasMovedThisTurn()) {
            System.out.println("You cannot use Set for Charge if you moved this turn.");
            return;
        }

        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Set for Charge.");
            return;
        }

        player.spendMainAction(mainCost);

        int brace = soldier.consumeBraceHitBonus();
        int statBonus = (player.getStr() + player.getDex()) / 2 - 1 + brace;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), statBonus);

        int dmgMod = soldier.consumeNextAttackDamageMod();
        int totalDmg = this.dmg + spear.getDmgBonus() + player.getTempDmgBonus() + dmgMod;
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(spear, 1);

        soldier.applySetForChargeDebuffUntilNextTurn();

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class ExecuteAttack extends Action implements Attack {
    private final int mainCost = 1;
    private final String description;

    public ExecuteAttack() {
        super(20, 2, "Execute");
        this.description = "Damage = 20 + 10*(AC-17), range 2. Hit bonus = STR - 2 - (AC-17). Cost: 1 Main. Only if you did not move this turn and have at least 17 AC. Drawback: reset Stand Firm, cannot gain Stand Firm until end of next round, next turn you only have 1 Main. Uses spear durability.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        if (!(player instanceof Soldier s) || s.hasMovedThisTurn()) return false;
        if (player.getArmourClass() < 17) return false;
        Weapon w = player.getCurrentWeapon();
        return (w instanceof ItemShoddySpear) || (w instanceof ItemSpear) || (w instanceof ItemAttunedSpear);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        if (!(player instanceof Soldier soldier)) {
            System.out.println("Only a Soldier can use Execute.");
            return;
        }

        Weapon spear = player.getCurrentWeapon();
        if (!((spear instanceof ItemShoddySpear) || (spear instanceof ItemSpear) || (spear instanceof ItemAttunedSpear))) {
            System.out.println("You need to have a spear equipped to use Execute.");
            return;
        }

        if (soldier.hasMovedThisTurn()) {
            System.out.println("You cannot use Execute if you moved this turn.");
            return;
        }

        int ac = player.getArmourClass();
        if (ac < 17) {
            System.out.println("You need at least 17 AC to use Execute.");
            return;
        }

        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Execute.");
            return;
        }

        player.spendMainAction(mainCost);

        int k = ac - 17;
        int brace = soldier.consumeBraceHitBonus();
        int hitBonus = player.getStr() - 2 - k + brace;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int dmgMod = soldier.consumeNextAttackDamageMod();
        int totalDmg = 20 + 10 * k + spear.getDmgBonus() + player.getTempDmgBonus() + dmgMod;
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The attack missed!");
        }

        player.removeDurability(spear, 1);

        soldier.applyExecuteDrawback();

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}


//ranger
class AimedShotAttack extends Action implements Attack {
    private final int mainCost = 1;
    private final int quickCost = 0;
    private final String description;

    public AimedShotAttack() {
        super(7, 12, "Aimed Shot");
        this.description = "Damage 7, range 12. Hit bonus = DEX. Cost: 1 Main + 1 Arrow.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) return false;
        Ammo ammo = player.getBestAvailableArrow();
        return ammo != null && player.getAmmoCount(ammo) >= 1;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Ammo ammo = player.getBestAvailableArrow();
        if (ammo == null || player.getAmmoCount(ammo) < 1) {
            System.out.println("You need at least 1 arrow to use Aimed Shot.");
            return;
        }
        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Aimed Shot.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        int hitBonus = player.getDex();
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int mark = (player instanceof Ranger r) ? r.consumeMarkBonus() : 0;
        int totalDmg = this.dmg + ammo.getDmgBonus() + player.getTempDmgBonus() + mark;
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");
        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The shot missed!");
        }

        player.removeAmmo(ammo, 1);

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class RapidShotAttack extends Action implements Attack {
    private final int mainCost = 0;
    private final int quickCost = 1;
    private final String description;

    public RapidShotAttack() {
        super(3, 5, "Rapid Shot");
        this.description = "Damage 3, range 5. Hit bonus = 0. Cost: 1 Quick + 1 Arrow.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) return false;
        Ammo ammo = player.getBestAvailableArrow();
        return ammo != null && player.getAmmoCount(ammo) >= 1;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Ammo ammo = player.getBestAvailableArrow();
        if (ammo == null || player.getAmmoCount(ammo) < 1) {
            System.out.println("You need at least 1 arrow to use Rapid Shot.");
            return;
        }
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough actions to use Rapid Shot.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        boolean hit = HitChance.acCheck(enemy.getArmourClass(), 0);

        int mark = (player instanceof Ranger r) ? r.consumeMarkBonus() : 0;
        int totalDmg = this.dmg + ammo.getDmgBonus() + player.getTempDmgBonus() + mark;
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");
        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The shot missed!");
        }

        player.removeAmmo(ammo, 1);

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}

class DoubleShotAttack extends Action implements Attack {
    private final int mainCost = 1;
    private final int quickCost = 1;
    private final String description;

    public DoubleShotAttack() {
        super(3, 7, "Double Shot");
        this.description = "Two shots. Each deals 3 damage (range 7). Hit bonus = DEX. Cost: 1 Main + 1 Quick + 2 Arrows. Each shot: secret d20; on 20, double damage for that shot.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) return false;
        Ammo ammo = player.getBestAvailableArrow();
        return ammo != null && player.getAmmoCount(ammo) >= 2;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        Ammo ammo = player.getBestAvailableArrow();
        if (ammo == null || player.getAmmoCount(ammo) < 2) {
            System.out.println("You need at least 2 arrows to use Double Shot.");
            return;
        }
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough actions to use Double Shot.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);

        int mark = (player instanceof Ranger r) ? r.consumeMarkBonus() : 0;
        boolean markApplied = false;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");

        for (int i = 1; i <= 2; i++) {
            if (!enemy.isAlive()) break;

            int hitBonus = player.getDex();
            boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

            int secret = (int)(Math.random() * 20) + 1;

            int dmgThisShot = this.dmg + ammo.getDmgBonus() + player.getTempDmgBonus();
            if (!markApplied && mark > 0) {
                dmgThisShot += mark;
                markApplied = true;
            }
            if (dmgThisShot < 0) dmgThisShot = 0;

            if (secret == 20) dmgThisShot *= 2;

            if (hit) {
                enemy.setHealth(enemy.getHealth() - dmgThisShot);
                System.out.println("Shot " + i + ": Hit! " + enemy.getName() + " takes " + dmgThisShot + " damage.");
            } else {
                System.out.println("Shot " + i + ": Miss!");
            }

            player.removeAmmo(ammo, 1);
        }

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}


//magic
class ArcBoltAttack extends Action implements Attack {
    static final String KEY = "Arc Bolt";

    private final int mainCost = 1;
    private final int manaCost = 2;
    private final String description;

    public ArcBoltAttack() {
        super(7, 100, KEY);
        this.description = "Damage 7, range 10. Hit bonus = CHR + 2. Cost: 1 Main + 2 mana (Arc Bolt pool).";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        if (!(player instanceof Sorcerer s)) return false;
        return s.hasMana(KEY, manaCost);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        if (!(player instanceof Sorcerer s)) {
            System.out.println("Only a Sorcerer can use Arc Bolt.");
            return;
        }
        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Arc Bolt.");
            return;
        }
        if (!s.hasMana(KEY, manaCost)) {
            System.out.println("Not enough mana to use Arc Bolt.");
            return;
        }

        player.spendMainAction(mainCost);
        s.spendMana(KEY, manaCost);

        int focus = s.consumeFocusToHit();
        int hitBonus = player.getChr() + 2 + focus;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int totalDmg = this.dmg + player.getTempDmgBonus();
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");
        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The spell missed!");
        }

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}


class RiftLanceAttack extends Action implements Attack {
    static final String KEY = "Rift Lance";

    private final int mainCost = 1;
    private final int manaCost = 4;
    private final String description;

    public RiftLanceAttack() {
        super(10, 100, KEY);
        this.description = "Damage 10, range 10. Hit bonus = CHR + 0. Cost: 1 Main + 4 mana (Rift Lance pool).";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        if (!(player instanceof Sorcerer s)) return false;
        return s.hasMana(KEY, manaCost);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        if (!(player instanceof Sorcerer s)) {
            System.out.println("Only a Sorcerer can use Rift Lance.");
            return;
        }
        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Rift Lance.");
            return;
        }
        if (!s.hasMana(KEY, manaCost)) {
            System.out.println("Not enough mana to use Rift Lance.");
            return;
        }

        player.spendMainAction(mainCost);
        s.spendMana(KEY, manaCost);

        int focus = s.consumeFocusToHit();
        int hitBonus = player.getChr() + focus;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int totalDmg = this.dmg + player.getTempDmgBonus();
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");
        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
        } else {
            System.out.println("The spell missed!");
        }

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}


class HexAttack extends Action implements Attack {
    static final String KEY = "Hex";

    private final int mainCost = 1;
    private final int manaCost = 1;
    private final String description;

    public HexAttack() {
        super(2, 100, KEY);
        this.description = "Damage 2, range 10. Hit bonus = CHR + 1. Cost: 1 Main + 1 mana (Hex pool). Effect: -2 AC until start of your next turn.";
    }

    @Override
    public boolean canUse(PlayerClass player) {
        if (player.getCurrentMainActions() < mainCost) return false;
        if (!(player instanceof Sorcerer s)) return false;
        return s.hasMana(KEY, manaCost);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemy) {
        if (!(player instanceof Sorcerer s)) {
            System.out.println("Only a Sorcerer can use Hex.");
            return;
        }
        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough actions to use Hex.");
            return;
        }
        if (!s.hasMana(KEY, manaCost)) {
            System.out.println("Not enough mana to use Hex.");
            return;
        }

        player.spendMainAction(mainCost);
        s.spendMana(KEY, manaCost);

        int focus = s.consumeFocusToHit();
        int hitBonus = player.getChr() + 1 + focus;
        boolean hit = HitChance.acCheck(enemy.getArmourClass(), hitBonus);

        int totalDmg = this.dmg + player.getTempDmgBonus();
        if (totalDmg < 0) totalDmg = 0;

        System.out.println(player.getName() + " used " + getName() + " on " + enemy.getName() + "!");
        if (hit) {
            enemy.setHealth(enemy.getHealth() - totalDmg);
            System.out.println("Hit! " + enemy.getName() + " takes " + totalDmg + " damage.");
            s.applyHex(enemy);
            System.out.println(enemy.getName() + " is Hexed: -2 AC until start of your next turn.");
        } else {
            System.out.println("The spell missed!");
        }

        if (!enemy.isAlive()) System.out.println(enemy.getName() + " has been defeated!");
    }

    @Override public String getName() { return name; }
    @Override public int getRange() { return range; }
    @Override public String getDescription() { return description; }
}
