public class Enemy extends Entity {
    
    private int damage;
    private int range;
    private int hitChance;

    public Enemy(String name, double health, int str, int dex, int chr, int armourClass, int gold,  int moveSpeed, int damage, int range, int hitChance, char gridID) {
        this.name = name;
        this.health = health;
        this.str = str;
        this.dex = dex;
        this.chr = chr;
        this.armourClass = armourClass;
        this.gold = gold;
        this.moveSpeed = moveSpeed;
        this.damage = damage;
        this.range = range;
        this.hitChance = hitChance;
        this.gridID = gridID;
        

    }

    public void attack(PlayerClass player) {
        boolean attackLands = HitChance.acCheck(player.getArmourClass(), HitChance.checkHitChanceType(hitChance, this));
        
        if (attackLands) {
            player.setHealth(player.getHealth() - damage);
            System.out.println(name + " attacks " + player.name + " for " + damage + " damage!");
            
            if (!player.isAlive()) {
                System.out.println(player.name + " has been defeated!");
            }
        }
        else {
            System.out.println(name + " missed!");
        }
    }

    public void attackWithCrit(PlayerClass player) {
        int attackResult = HitChance.acCheckWithCrit(player.getArmourClass(), HitChance.checkHitChanceType(hitChance, this));
        
        switch (attackResult) {
            case 2 -> {
                int critDamage = damage * 2; 
                player.setHealth(player.getHealth() - critDamage);
                System.out.println("CRITICAL HIT! " + name + " deals " + critDamage + " damage!");
            }
            case 1 -> {
                player.setHealth(player.getHealth() - damage);
                System.out.println(name + " hits for " + damage + " damage.");
            }
            default -> {
                System.out.println(name + " missed!");
            }
        }

        if (!player.isAlive()) {
            System.out.println(player.name + " has been defeated!");
        }
    }
    
    public void attackWithCrit(PlayerClass player, double critBonus) {
        int attackResult = HitChance.acCheckWithCrit(player.getArmourClass(), HitChance.checkHitChanceType(hitChance, this), critBonus);
        
        switch (attackResult) {
            case 2 -> {
                int critDamage = damage * 2; 
                player.setHealth(player.getHealth() - critDamage);
                System.out.println("CRITICAL HIT! " + name + " deals " + critDamage + " damage!");
            }
            case 1 -> {
                player.setHealth(player.getHealth() - damage);
                System.out.println(name + " hits for " + damage + " damage.");
            }
            default -> {
                System.out.println(name + " missed!");
            }
        }

        if (!player.isAlive()) {
            System.out.println(player.name + " has been defeated!");
        }
    }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }

    public int getRange() { return range; }
    public void setRange(int range) { this.range = range; }

    public int getHitChance() { return hitChance; }
    public void setHitChance(int hitChance) { this.hitChance = hitChance; }
}