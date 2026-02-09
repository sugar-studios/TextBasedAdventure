public class Fighter extends PlayerClass {
    private boolean movedThisTurn = false;
    private boolean moveSwordBonusReady = false;

    private final FeintSpecial feintRef;

    public Fighter(String name) {
        super(name);

        feintRef = new FeintSpecial();

        addWeapon(new ItemDullSword());
        equipWeapon(0);

        addAttack(new FighterSwordAttack());
        addAttack(new ThrustAttack());
        addAttack(new DashSliceAttack());

        addSpecial(feintRef);
        addSpecial(new SurgeSpecial());
        addSpecial(new TauntSpecial());
        addSpecial(new QuickStepSpecial());
    }

    @Override
    public void startNewTurn() {
        resetActionEcon();
        clearStatusEffects();

        movedThisTurn = false;
        moveSwordBonusReady = false;

        setArmourClass(14);
        setMoveSpeed(6);

        for (Special s : getSpecialList()) s.onTurnStart(this);

        System.out.println("\n--- " + name + "'s Turn Started ---");
    }

    @Override
    public void resetActionEcon() {
        setCurrentMainActions(2);
        setCurrentQuickActions(2);
    }

    @Override
    public void spendMoveSpeed(int amount) {
        super.spendMoveSpeed(amount);
        if (!movedThisTurn && amount > 0) {
            movedThisTurn = true;
            moveSwordBonusReady = true;
        }
    }

    public boolean consumeGuaranteedHit() {
        if (feintRef == null) return false;
        return feintRef.consumeGuaranteedHit();
    }

    public int consumeMoveSwordBonus() {
        if (moveSwordBonusReady) {
            moveSwordBonusReady = false;
            return 2;
        }
        return 0;
    }
}
