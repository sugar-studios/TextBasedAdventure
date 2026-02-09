// Ranger.java
public class Ranger extends PlayerClass {
    private static final int BASE_AC = 11;
    private static final int BASE_MOVE = 4;

    private int markedBonus = 0;
    private int markedTurnsRemaining = 0;

    private int meditateDebuffTurnsRemaining = 0;

    public Ranger(String name) {
        super(name);

        setHealth(25.0);

        setArmourClass(BASE_AC);
        setMoveSpeed(BASE_MOVE);
        setDex(10);
        setStr(4);
        setChr(4);

        resetActionEcon();

        addAmmo(new ItemArrows(), 8);

        addAttack(new BowAttack());
        addAttack(new AimedShotAttack());
        addAttack(new RapidShotAttack());
        addAttack(new DoubleShotAttack());

        addSpecial(new RepositionSpecial());
        addSpecial(new MarkSpecial());
        addSpecial(new MeditateSpecial());
    }

    @Override
    public void startNewTurn() {
        resetActionEcon();

        setMoveSpeed(BASE_MOVE);

        int debuff = (meditateDebuffTurnsRemaining > 0) ? 2 : 0;
        setArmourClass(BASE_AC - debuff);

        if (markedTurnsRemaining > 0) {
            markedTurnsRemaining--;
            if (markedTurnsRemaining == 0) markedBonus = 0;
        }

        if (meditateDebuffTurnsRemaining > 0) meditateDebuffTurnsRemaining--;

        for (Special s : getSpecialList()) s.onTurnStart(this);

        System.out.println("\n--- " + name + "'s Turn Started ---");
    }

    @Override
    public void resetActionEcon() {
        setCurrentMainActions(1);
        setCurrentQuickActions(1);
    }

    public int consumeMarkBonus() {
        if (markedTurnsRemaining <= 0 || markedBonus <= 0) return 0;
        int v = markedBonus;
        markedBonus = 0;
        markedTurnsRemaining = 0;
        return v;
    }

    public void applyMarkBonus() {
        markedBonus = 2;
        markedTurnsRemaining = 2;
    }

    public void applyMeditateDebuffTwoRoundsCurrentIncluded() {
        meditateDebuffTurnsRemaining = Math.max(meditateDebuffTurnsRemaining, 1);
        setArmourClass(getArmourClass() - 2);
    }
}
