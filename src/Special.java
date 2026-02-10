/*

Just like Attacks.java, the interface tells you everything- the rest is just nitty gritty nerd stuff. The code is pretty self explanatory. Its pretty basic,
just a lot of nested basics.  

*/



interface Special {
    String getName();
    String getDescription();
    boolean needsTarget();
    boolean canUse(PlayerClass player);
    void execute(PlayerClass player, Enemy enemyOrNull);
    default void onTurnStart(PlayerClass player) {}
}


//Base Specials
class FlirtSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Sometimes, the enemy can be a baddie.";

    @Override public String getName() { return "Flirt"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return true; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (enemyOrNull == null) {
            System.out.println("No target selected.");
            return;
        }
        player.spendQuickAction(quickCost);
        System.out.println("You try to seduce " + enemyOrNull.getName());
    }
}

class CrySpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Print: \"You cry your feelings out\".";

    @Override public String getName() { return "Cry"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        player.spendQuickAction(quickCost);
        System.out.println("You cry your feelings out");
    }
}

class BlockSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "+5 AC for this turn (costs 1 Quick).";
    private boolean activeThisTurn = false;

    @Override public String getName() { return "Block"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost && !activeThisTurn;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        player.spendQuickAction(quickCost);
        player.setArmourClass(player.getArmourClass() + 5);
        activeThisTurn = true;
        System.out.println("You raise your guard. +5 AC for this turn.");
    }

    @Override
    public void onTurnStart(PlayerClass player) {
        activeThisTurn = false;
    }
}


// FIGHTER 

class FeintSpecial implements Special {
    private final int quickCost = 1;
    private final int moveCost = 2;
    private final String description = "Cost: 1 Quick + 2 Move. Next turn your next attack is guaranteed to hit.";

    private boolean guaranteedHitNextTurn = false;
    private boolean guaranteedHitThisTurn = false;

    @Override public String getName() { return "Feint"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost && player.getMoveSpeed() >= moveCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Feint.");
            return;
        }
        if (player.getMoveSpeed() < moveCost) {
            System.out.println("Not enough Move Speed to use Feint.");
            return;
        }

        player.spendQuickAction(quickCost);
        player.spendMoveSpeed(moveCost);
        guaranteedHitNextTurn = true;

        System.out.println("Feint used. Next turn your next attack is guaranteed to hit.");
    }

    @Override
    public void onTurnStart(PlayerClass player) {
        guaranteedHitThisTurn = guaranteedHitNextTurn;
        guaranteedHitNextTurn = false;
    }

    public boolean consumeGuaranteedHit() {
        if (guaranteedHitThisTurn) {
            guaranteedHitThisTurn = false;
            return true;
        }
        return false;
    }
}

class SurgeSpecial implements Special {
    private final int quickCost = 2;
    private final String description = "Cost: 2 Quick. Gain +1 Main Action. Only 2 uses per game.";
    private int used = 0;

    @Override public String getName() { return "Surge"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost && used < 2;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (used >= 2) {
            System.out.println("Surge has already been used 2 times this game.");
            return;
        }
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Surge.");
            return;
        }

        player.spendQuickAction(quickCost);
        player.setCurrentMainActions(player.getCurrentMainActions() + 1);
        used++;

        System.out.println("Surge used. +1 Main Action.");
    }
}

class TauntSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Need 2+ Move. Cost: 1 Quick and all current Move. Gain +2 AC. Next turn lose 1 Move Speed.";
    private boolean movePenaltyNextTurn = false;

    @Override public String getName() { return "Taunt"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost && player.getMoveSpeed() >= 2;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (player.getMoveSpeed() < 2) {
            System.out.println("Need at least 2 Move Speed to use Taunt.");
            return;
        }
        if (player.getCurrentQuickActions() < 1) {
            System.out.println("Not enough Quick Actions to use Taunt.");
            return;
        }

        player.spendQuickAction(1);

        int ms = Math.max(0, player.getMoveSpeed());
        if (ms > 0) player.spendMoveSpeed(ms);

        player.setArmourClass(player.getArmourClass() + 2);
        movePenaltyNextTurn = true;

        System.out.println("Taunt used. +2 AC, but you lose 1 Move Speed next turn.");
    }

    @Override
    public void onTurnStart(PlayerClass player) {
        if (movePenaltyNextTurn) {
            player.spendMoveSpeed(1);
            movePenaltyNextTurn = false;
        }
    }
}

class QuickStepSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Cost: 1 Quick. Gain +2 Move Speed.";

    @Override public String getName() { return "Quick Step"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (player.getCurrentQuickActions() < 1) {
            System.out.println("Not enough Quick Actions to use Quick Step.");
            return;
        }

        player.spendQuickAction(1);
        player.setMoveSpeed(player.getMoveSpeed() + 2);

        System.out.println("Quick Step used. +2 Move Speed.");
    }
}





// SOLDIER

class BraceSpecial implements Special {
    private final int quickCost = 1;
    private final int moveCost = 1;
    private final String description = "Cost: 1 Quick + 1 Move. Gain +2 damage on your next attack.";

    @Override public String getName() { return "Brace"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost && player.getMoveSpeed() >= moveCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Brace.");
            return;
        }
        if (player.getMoveSpeed() < moveCost) {
            System.out.println("Not enough Move Speed to use Brace.");
            return;
        }

        player.spendQuickAction(quickCost);
        player.spendMoveSpeed(moveCost);
        player.addTempDmgBonus(2);

        System.out.println("Brace used. Next attack deals +2 damage.");
    }
}

class HoldTheLineSpecial implements Special {
    private final int mainCost = 2;
    private final String description = "Cost: 2 Main. No attacks this turn. Immediately gain +1 stored Stand Firm (max +5).";

    @Override public String getName() { return "Hold the Line"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentMainActions() >= mainCost && player instanceof Soldier;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (!(player instanceof Soldier s)) {
            System.out.println("Only a Soldier can use Hold the Line.");
            return;
        }
        if (player.getCurrentMainActions() < mainCost) {
            System.out.println("Not enough Main Actions to use Hold the Line.");
            return;
        }

        player.spendMainAction(mainCost);

        // "No attack this turn" -> burn remaining actions so menu can't meaningfully attack.
        player.setCurrentMainActions(0);
        player.setCurrentQuickActions(0);

        s.grantStandFirmChargeNow();

        System.out.println("You hold the line and do not attack this turn.");
    }
}

class NoHoldsBarredSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Cost: 1 Quick. Lose 5 HP. Gain +1 Main. Next attack deals -2 damage.";

    @Override public String getName() { return "No Holds Barred"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use No Holds Barred.");
            return;
        }

        player.spendQuickAction(quickCost);
        player.setHealth(Math.max(0.0, player.getHealth() - 5.0));
        player.setCurrentMainActions(player.getCurrentMainActions() + 1);
        player.addTempDmgBonus(-2);

        System.out.println("No Holds Barred used. -5 HP, +1 Main Action, next attack deals -2 damage.");
    }
}

class VigilantiSpecial implements Special {
    private final int mainCost = 1;
    private final int quickCost = 1;
    private final String description = "Cost: 1 Main + 1 Quick. This turn, you can spend Move Speed without losing Stand Firm.";

    @Override public String getName() { return "Vigilanti"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player instanceof Soldier
                && player.getCurrentMainActions() >= mainCost
                && player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (!(player instanceof Soldier s)) {
            System.out.println("Only a Soldier can use Vigilanti.");
            return;
        }
        if (player.getCurrentMainActions() < mainCost || player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough actions to use Vigilanti.");
            return;
        }

        player.spendMainAction(mainCost);
        player.spendQuickAction(quickCost);
        s.setVigilantiActiveThisTurn(true);

        System.out.println("Vigilanti used. You may move this turn without breaking Stand Firm.");
    }
}



//RANGER

class RepositionSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Cost: 1 Quick. Gain +2 Move Speed this turn.";

    @Override public String getName() { return "Reposition"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Reposition.");
            return;
        }

        player.spendQuickAction(quickCost);
        player.setMoveSpeed(player.getMoveSpeed() + 2);

        System.out.println("Reposition used. +2 Move Speed this turn.");
    }
}

class MarkSpecial implements Special {
    private final int quickCost = 1;
    private final int moveCost = 3;
    private final String description = "Cost: 1 Quick + 3 Move. Lose all Move Speed. Next bow attack this turn or next turn deals +2 damage.";

    @Override public String getName() { return "Mark"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return (player instanceof Ranger)
                && player.getCurrentQuickActions() >= quickCost
                && player.getMoveSpeed() >= moveCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (!(player instanceof Ranger r)) {
            System.out.println("Only a Ranger can use Mark.");
            return;
        }
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Mark.");
            return;
        }
        if (player.getMoveSpeed() < moveCost) {
            System.out.println("Not enough Move Speed to use Mark.");
            return;
        }

        player.spendQuickAction(quickCost);
        player.spendMoveSpeed(moveCost);
        player.setMoveSpeed(0);

        r.applyMarkBonus();

        System.out.println("Mark used. Next bow attack this turn or next turn deals +2 damage, and you lose all Move Speed.");
    }
}

class MeditateSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Cost: 1 Quick. -2 AC for two rounds. Gain 3 health.";

    @Override public String getName() { return "Meditate"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return (player instanceof Ranger) && player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (!(player instanceof Ranger r)) {
            System.out.println("Only a Ranger can use Meditate.");
            return;
        }
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Meditate.");
            return;
        }

        player.spendQuickAction(quickCost);

        r.applyMeditateDebuffTwoRoundsCurrentIncluded();

        player.setHealth(player.getHealth() + 3.0);

        System.out.println("Meditate used. +3 HP, -2 AC for two rounds.");
    }
}



//MAGIC

class ChannelSpecial implements Special {
    static final String KEY = "Channel";

    private final int quickCost = 1;
    private final String description = "Cost: 1 Quick. Gain +1 mana to your lowest non-full spell pool (max 10).";

    @Override public String getName() { return "Channel"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return (player instanceof Sorcerer) && player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (!(player instanceof Sorcerer s)) {
            System.out.println("Only a Sorcerer can use Channel.");
            return;
        }
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Channel.");
            return;
        }

        player.spendQuickAction(quickCost);
        s.addManaToLowestPool(1);
    }
}

class WardSpecial implements Special {
    static final String KEY = "Ward";

    private final int quickCost = 1;
    private final int manaCost = 1;
    private final String description = "Cost: 1 Quick + 1 mana (Ward pool). Gain +2 AC until start of your next turn.";

    @Override public String getName() { return "Ward"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        if (!(player instanceof Sorcerer s)) return false;
        return player.getCurrentQuickActions() >= quickCost && s.hasMana(KEY, manaCost);
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (!(player instanceof Sorcerer s)) {
            System.out.println("Only a Sorcerer can use Ward.");
            return;
        }
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Ward.");
            return;
        }
        if (!s.hasMana(KEY, manaCost)) {
            System.out.println("Not enough mana to use Ward.");
            return;
        }

        player.spendQuickAction(quickCost);
        s.spendMana(KEY, manaCost);

        s.applyWard();

        System.out.println("Ward used. +2 AC until start of your next turn.");
    }
}

class FocusSpecial implements Special {
    private final int quickCost = 1;
    private final String description = "Cost: 1 Quick. Your next spell this turn gains +2 to hit.";

    @Override public String getName() { return "Focus"; }
    @Override public String getDescription() { return description; }
    @Override public boolean needsTarget() { return false; }

    @Override
    public boolean canUse(PlayerClass player) {
        return (player instanceof Sorcerer) && player.getCurrentQuickActions() >= quickCost;
    }

    @Override
    public void execute(PlayerClass player, Enemy enemyOrNull) {
        if (!(player instanceof Sorcerer s)) {
            System.out.println("Only a Sorcerer can use Focus.");
            return;
        }
        if (player.getCurrentQuickActions() < quickCost) {
            System.out.println("Not enough Quick Actions to use Focus.");
            return;
        }

        player.spendQuickAction(quickCost);
        s.applyFocus();

        System.out.println("Focus used. Next spell this turn gains +2 to hit.");
    }
}



