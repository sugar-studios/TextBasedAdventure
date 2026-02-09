public abstract class Entity {

    protected double health;
    protected String name;

    protected int armourClass;
    protected int gold;

    protected int str;
    protected int dex;
    protected int chr;

    protected int moveSpeed;
    protected char gridID;

    public boolean isAlive() {
        return health > 0;
    }

    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getArmourClass() { return armourClass; }
    public void setArmourClass(int armourClass) { this.armourClass = armourClass; }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }

    public int getStr() { return str; }
    public int getDex() { return dex; }
    public int getChr() { return chr; }

    public void setChr(int chr) { this.chr = chr; }
    public void setDex(int dex) { this.dex = dex; }
    public void setStr(int str) { this.str = str; }

    public int getMoveSpeed() { return moveSpeed; }
    public void setMoveSpeed(int moveSpeed) { this.moveSpeed = moveSpeed; }

    public char getGridID() { return gridID; }
}
