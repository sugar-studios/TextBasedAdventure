// Soldier.java
public class Soldier extends PlayerClass {
    private static final int DEFAULT_AC = 14;

    private int standFirmStored = 0;
    private boolean movedThisTurn = false;

    private boolean vigilantiActiveThisTurn = false;

    private boolean setForChargeDebuffActive = false;
    private boolean executeMainLimitNextTurn = false;
    private int standFirmLockTurnsRemaining = 0;

    private int braceHitBonusStored = 0;
    private int nextAttackDamageModStored = 0;

    public Soldier(String name) {
        super(name);

        setHealth(35.0);

        setArmourClass(DEFAULT_AC);
        setMoveSpeed(2);
        setStr(7);
        setDex(4);
        setChr(4);

        resetActionEcon();

        addWeapon(new ItemShoddySpear());
        equipWeapon(0);

        addAttack(new SpearJabAttack());
        addAttack(new DrivingThrustAttack());
        addAttack(new SweepingHaftAttack());
        addAttack(new SetForChargeAttack());
        addAttack(new ExecuteAttack());

        addSpecial(new BraceSpecial());
        addSpecial(new HoldTheLineSpecial());
        addSpecial(new NoHoldsBarredSpecial());
        addSpecial(new VigilantiSpecial());

        for (Special s : getSpecialList()) System.out.println(s.getName());
    }

    @Override
    public void startNewTurn() {
        movedThisTurn = false;
        vigilantiActiveThisTurn = false;

        if (standFirmLockTurnsRemaining > 0) standFirmLockTurnsRemaining--;

        resetActionEcon();

        setMoveSpeed(2);
        int debuff = setForChargeDebuffActive ? 2 : 0;
        setForChargeDebuffActive = false;

        setArmourClass(DEFAULT_AC + standFirmStored - debuff);

        for (Special s : getSpecialList()) s.onTurnStart(this);

        System.out.println("\n--- " + name + "'s Turn Started ---");
    }

    @Override
    public void resetActionEcon() {
        int main = executeMainLimitNextTurn ? 1 : 2;
        executeMainLimitNextTurn = false;

        setCurrentMainActions(main);
        setCurrentQuickActions(1);
    }

    @Override
    public void spendMoveSpeed(int amount) {
        if (amount > 0) {
            movedThisTurn = true;

            if (!vigilantiActiveThisTurn) {
                if (standFirmStored != 0) {
                    standFirmStored = 0;
                }
                setArmourClass(DEFAULT_AC);
            }
        }
        super.spendMoveSpeed(amount);
    }

    @Override
    public void onTurnEnd() {
        if (!movedThisTurn) {
            if (standFirmLockTurnsRemaining == 0) {
                if (standFirmStored < 5) standFirmStored++;
                setArmourClass(DEFAULT_AC + standFirmStored);
                System.out.println("Stand Firm: +1 stored AC (now " + standFirmStored + ").");
            } else {
                System.out.println("Stand Firm: cannot gain charges this turn.");
            }
        }
    }

    public boolean hasMovedThisTurn() {
        return movedThisTurn;
    }

    public void applySetForChargeDebuffUntilNextTurn() {
        setArmourClass(getArmourClass() - 2);
        setForChargeDebuffActive = true;
    }

    public void applyExecuteDrawback() {
        standFirmStored = 0;
        setArmourClass(DEFAULT_AC);
        standFirmLockTurnsRemaining = 1;
        executeMainLimitNextTurn = true;
    }

    public void grantStandFirmChargeNow() {
        if (standFirmLockTurnsRemaining != 0) {
            System.out.println("Stand Firm: cannot gain charges right now.");
            return;
        }
        if (standFirmStored < 5) standFirmStored++;
        setArmourClass(DEFAULT_AC + standFirmStored);
        System.out.println("Stand Firm: +1 stored AC (now " + standFirmStored + ").");
    }

    public void setVigilantiActiveThisTurn(boolean val) {
        vigilantiActiveThisTurn = val;
    }

    public boolean isVigilantiActiveThisTurn() {
        return vigilantiActiveThisTurn;
    }

    public void addBraceHitBonus(int amount) {
        braceHitBonusStored += amount;
    }

    public int consumeBraceHitBonus() {
        int v = braceHitBonusStored;
        braceHitBonusStored = 0;
        return v;
    }

    public void addNextAttackDamageMod(int amount) {
        nextAttackDamageModStored += amount;
    }

    public int consumeNextAttackDamageMod() {
        int v = nextAttackDamageModStored;
        nextAttackDamageModStored = 0;
        return v;
    }
}
