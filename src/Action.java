
public abstract class Action {
    protected int dmg;
    protected int range;
    protected String name;
    protected int hitChance;

    protected Action(int dmg, int range, String name) {
        this.dmg = dmg;
        this.range = range;
        this.name = name;
    }

    public int getDmg() { return dmg; }
    public int getRange() { return range; }
    public String getName() { return name; }
}
