/*

The naming I used in this file should make it pretty self explanatory... the abstract class should tell you everything you need to know

*/



import java.util.Objects;

enum Rarity {
    LOW,
    MEDIUM,
    HIGH
}

public abstract class Items {
    protected String name;
    protected int id;
    protected int sellPrice;
    protected int buyPrice;
    protected Rarity rarity;
    protected String description;

    Items(String name, int sellPrice, int buyPrice, Rarity rarity, String description) {
        this.name = name;
        this.id = Objects.hash(this.name);
        this.sellPrice = sellPrice;
        this.buyPrice = buyPrice;
        this.rarity = rarity;
        this.description = description;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public int getSellPrice() { return sellPrice; }
    public void setSellPrice(int sellPrice) { this.sellPrice = sellPrice; }
    public int getBuyPrice() { return buyPrice; }
    public void setBuyPrice(int buyPrice) { this.buyPrice = buyPrice; }
    public Rarity getRarity() { return rarity; }
    public String getDescription() { return description; }
}

abstract class Weapon extends Items {
    int dmgBonus;
    double durability;

    Weapon(String name, int sellPrice, int buyPrice, Rarity rarity, String description) {
        super(name, sellPrice, buyPrice, rarity, description);
        dmgBonus = 0;
        durability = 1;
    }

    public int getDmgBonus() { return dmgBonus; }
    public double getDurability() { return durability; }
    public boolean isBroken() { return durability <= 0; }
}

abstract class Consumable extends Items {
    Consumable(String name, int sellPrice, int buyPrice, Rarity rarity, String description) {
        super(name, sellPrice, buyPrice, rarity, description);
    }

    public abstract void use(PlayerClass p);
}

abstract class Ammo extends Items {
    protected int dmgBonus;

    Ammo(String name, int sellPrice, int buyPrice, Rarity rarity, String description) {
        super(name, sellPrice, buyPrice, rarity, description);
        dmgBonus = 0;
    }

    public int getDmgBonus() { return dmgBonus; }
}


// ---------------- POTIONS ----------------

class ItemLesserHealing extends Consumable {
    ItemLesserHealing() { super("Healing Potion", 10, 25, Rarity.LOW, "Restores 5 HP."); }
    @Override
    public void use(PlayerClass p) {
        p.setHealth(p.getHealth() + 5);
        System.out.println("Used Healing Potion. +5 HP.");
    }
}

class ItemGreaterHealing extends Consumable {
    ItemGreaterHealing() { super("Greater Healing Potion", 30, 75, Rarity.MEDIUM, "Restores 10 HP."); }
    @Override public void use(PlayerClass p) {
        p.setHealth(p.getHealth() + 10);
        System.out.println("Used Greater Healing Potion. +10 HP.");
    }
}

class ItemLesserMana extends Consumable {
    ItemLesserMana() { super("Mana Potion", 10, 25, Rarity.LOW, "Restores 1 Mana."); }
    @Override public void use(PlayerClass p) {
        p.setMana(p.getMana() + 1);
        System.out.println("Used Mana Potion. +1 Mana.");
    }
}

class ItemGreaterMana extends Consumable {
    ItemGreaterMana() { super("Greater Mana Potion", 30, 75, Rarity.MEDIUM, "Restores 2 Mana."); }
    @Override public void use(PlayerClass p) {
        p.setMana(p.getMana() + 2);
        System.out.println("Used Greater Mana Potion. +2 Mana.");
    }
}

class ItemLesserRage extends Consumable {
    ItemLesserRage() { super("Rage Potion", 20, 50, Rarity.MEDIUM, "Next attack +2 DMG."); }
    @Override public void use(PlayerClass p) {
        p.addTempDmgBonus(2);
        System.out.println("Rage surges! Next attack deals +2 damage.");
    }
}

class ItemGreaterRage extends Consumable {
    ItemGreaterRage() { super("Greater Rage Potion", 40, 100, Rarity.HIGH, "Next attack +4 DMG."); }
    @Override public void use(PlayerClass p) {
        p.addTempDmgBonus(4);
        System.out.println("Tremendous rage! Next attack deals +4 damage.");
    }
}

class ItemLesserSpeed extends Consumable {
    ItemLesserSpeed() { super("Speed Potion", 15, 40, Rarity.LOW, "+1 Move Speed."); }
    @Override public void use(PlayerClass p) {
        p.setMoveSpeed(p.getMoveSpeed() + 1);
        System.out.println("Used Speed Potion. Move Speed is now " + p.getMoveSpeed());
    }
}

class ItemGreaterSpeed extends Consumable {
    ItemGreaterSpeed() { super("Greater Speed Potion", 25, 60, Rarity.MEDIUM, "+2 Move Speed."); }
    @Override public void use(PlayerClass p) {
        p.setMoveSpeed(p.getMoveSpeed() + 2);
        System.out.println("Used Greater Speed Potion. Move Speed is now " + p.getMoveSpeed());
    }
}

class ItemEnergy extends Consumable {
    ItemEnergy() { super("Energy Potion", 18, 45, Rarity.LOW, "Gain 1 Main Action."); }
    @Override public void use(PlayerClass p) {
        p.setCurrentMainActions(p.getCurrentMainActions() + 1);
        System.out.println("Energy restored! +1 Main Action.");
    }
}

class ItemDefense extends Consumable {
    ItemDefense() { super("Defense Potion", 22, 55, Rarity.MEDIUM, "+1 Armour Class."); }
    @Override public void use(PlayerClass p) {
        p.setArmourClass(p.getArmourClass() + 1);
        System.out.println("Defense buffed! AC is now " + p.getArmourClass());
    }
}


// ---------------- WEAPONS ----------------

class ItemShoddySpear extends Weapon {
    ItemShoddySpear() {
        super("Shoddy Spear", 5, 15, Rarity.LOW, "A poorly made spear.");
        durability = 9;
    }
}

class ItemSpear extends Weapon {
    ItemSpear() {
        super("Spear", 15, 40, Rarity.MEDIUM, "A standard spear.");
        dmgBonus = 1;
        durability = 11;
    }
}

class ItemAttunedSpear extends Weapon {
    ItemAttunedSpear() {
        super("Attuned Spear", 30, 80, Rarity.HIGH, "A spear sharp enough to cut the heavens.");
        dmgBonus = 2;
        durability = 12;
    }
}

class ItemDullSword extends Weapon {
    ItemDullSword() {
        super("Dull Sword", 8, 25, Rarity.LOW, "A sword that is not very sharp.");
        durability = 8;
    }
}

class ItemSword extends Weapon {
    ItemSword() {
        super("Sword", 20, 60, Rarity.MEDIUM, "A reliable sword.");
        dmgBonus = 1;
        durability = 10;
    }
}

class ItemGildedSword extends Weapon {
    ItemGildedSword() {
        super("Gilded Sword", 50, 150, Rarity.HIGH, "A finely crafted gilded sword.");
        dmgBonus = 2;
        durability = 14;
    }
}


// ---------------- AMMO ----------------

class ItemArrows extends Ammo {
    ItemArrows() {
        super("Arrows", 2, 5, Rarity.LOW, "Standard arrows for ranged attacks.");
        dmgBonus = 0;
    }
}

class ItemBlackArrows extends Ammo {
    ItemBlackArrows() {
        super("Black Tipped Arrows", 8, 20, Rarity.MEDIUM, "Dragon Slaying arrows.");
        dmgBonus = 2;
    }
}
