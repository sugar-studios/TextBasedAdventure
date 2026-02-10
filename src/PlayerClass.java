import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

public class PlayerClass extends Entity {
    private int maxMainActions;
    private int maxQuickActions;
    private int currentMainActions;
    private int currentQuickActions;
    private String[] options;

    private BankState bankState = new BankState();
    private Bank bank = new Bank(bankState);


    private int casinoDebt = 0;

    private int baseArmourClass;
    private int baseMoveSpeed;
    private int tempDmgBonus = 0;

    private int mana;

    private Map<Integer, Integer> consumableInventory = new HashMap<>();
    private Map<Integer, Consumable> consumableCatalog = new LinkedHashMap<>();

    private Map<Integer, Integer> ammoInventory = new HashMap<>();
    private Map<Integer, Ammo> ammoCatalog = new LinkedHashMap<>();

    private LinkedList<Weapon> weaponInventory = new LinkedList<>();
    private Weapon currentWeapon;
    private LinkedList<Attack> attackList;

    private LinkedList<Special> specialList;

    public PlayerClass(String name) {
        this.maxHealth = 30.0;
        this.name = name;
        this.health = this.maxHealth;
        this.gold = 10;
        this.mana = 0;
        this.gridID = 'P';

        this.baseArmourClass = 8;
        this.baseMoveSpeed = 3;
        this.armourClass = baseArmourClass;
        this.moveSpeed = baseMoveSpeed;

        this.maxMainActions = 1;
        this.maxQuickActions = 1;

        this.str = 5;
        this.dex = 5;
        this.chr = 5;

        this.attackList = new LinkedList<>();
        this.specialList = new LinkedList<>();

        this.weaponInventory = new LinkedList<>();
        this.options = new String[]{"Attack", "Special", "Move", "Items", "Look", "End Turn", "Flee"};

        initializeInventory();
        resetActionEcon();

        addAttack(new BaseAttack());

        addSpecial(new FlirtSpecial());
        addSpecial(new CrySpecial());
        addSpecial(new BlockSpecial());
    }

    // I think there is probably a better way I could've done this as items dont need to track their own stats, so an object is probably too much memeory
    private void initializeInventory() {
        addConsumable(new ItemLesserHealing(), 5);
        addConsumable(new ItemLesserMana(), 2);
        addConsumable(new ItemLesserRage(), 0);
        addConsumable(new ItemLesserSpeed(), 0);

        addConsumable(new ItemEnergy(), 0);
        addConsumable(new ItemDefense(), 0);
        addConsumable(new ItemGreaterHealing(), 0);
        addConsumable(new ItemGreaterMana(), 0);
        addConsumable(new ItemGreaterRage(), 0);

        addAmmo(new ItemArrows(), 0);
        addAmmo(new ItemBlackArrows(), 0);
    }

    public void startNewTurn() {
        resetActionEcon();
        clearStatusEffects();
        moveSpeed = baseMoveSpeed;

        for (Special s : specialList) s.onTurnStart(this);

        System.out.println("\n--- " + name + "'s Turn Started ---");
    }

    public void clearStatusEffects() {
        this.armourClass = baseArmourClass;
        this.moveSpeed = baseMoveSpeed;
        this.tempDmgBonus = 0;
        System.out.println("Temporary status effects have worn off.");
    }

    public void addTempDmgBonus(int amount) {
        this.tempDmgBonus += amount;
    }

    public int getTempDmgBonus() {
        int bonus = tempDmgBonus;
        tempDmgBonus = 0;
        return bonus;
    }

    public void addConsumable(Items item, int qty) {
        consumableInventory.put(item.getId(),
                consumableInventory.getOrDefault(item.getId(), 0) + qty);

        if (item instanceof Consumable c) {
            consumableCatalog.putIfAbsent(item.getId(), c);
        }
    }

    public boolean removeConsumable(Items item, int qty) {
        int current = consumableInventory.getOrDefault(item.getId(), 0);
        if (current < qty) return false;
        if (current == qty) consumableInventory.remove(item.getId());
        else consumableInventory.put(item.getId(), current - qty);
        return true;
    }

    public void useConsumable(Consumable item) {
        if (getConsumableCount(item) <= 0) {
            System.out.println("No " + item.getName() + " left.");
            return;
        }

        if (currentQuickActions >= 1) {
            spendQuickAction(1);
            removeConsumable(item, 1);
            item.use(this);
            System.out.println("Used " + item.getName() + ". Remaining Quick Actions: " + currentQuickActions);
        } else {
            System.out.println("Not enough Quick Actions to use " + item.getName() + "!");
        }
    }

    public Map<Consumable, Integer> getConsumableInventoryView() {
        Map<Consumable, Integer> view = new LinkedHashMap<>();
        for (Map.Entry<Integer, Consumable> entry : consumableCatalog.entrySet()) {
            int id = entry.getKey();
            int qty = consumableInventory.getOrDefault(id, 0);
            if (qty > 0) view.put(entry.getValue(), qty);
        }
        return view;
    }

    public void addAmmo(Ammo ammo, int qty) {
        ammoInventory.put(ammo.getId(),
                ammoInventory.getOrDefault(ammo.getId(), 0) + qty);
        ammoCatalog.putIfAbsent(ammo.getId(), ammo);
    }

    public boolean removeAmmo(Ammo ammo, int qty) {
        int current = ammoInventory.getOrDefault(ammo.getId(), 0);
        if (current < qty) return false;
        if (current == qty) ammoInventory.remove(ammo.getId());
        else ammoInventory.put(ammo.getId(), current - qty);
        return true;
    }

    public int getAmmoCount(Ammo ammo) {
        return ammoInventory.getOrDefault(ammo.getId(), 0);
    }

    public Map<Ammo, Integer> getAmmoInventoryView() {
        Map<Ammo, Integer> view = new LinkedHashMap<>();

        Ammo normal = ammoCatalog.get(new ItemArrows().getId());
        if (normal != null) {
            view.put(normal, getAmmoCount(normal));
        }

        for (Map.Entry<Integer, Ammo> entry : ammoCatalog.entrySet()) {
            int id = entry.getKey();
            Ammo ammo = entry.getValue();

            if (normal != null && id == normal.getId()) continue;

            int qty = ammoInventory.getOrDefault(id, 0);
            if (qty > 0) view.put(ammo, qty);
        }

        return view;
    }

    public Ammo getBestAvailableArrow() {
        Ammo black = ammoCatalog.get(new ItemBlackArrows().getId());
        if (black != null && getAmmoCount(black) > 0) return black;

        Ammo normal = ammoCatalog.get(new ItemArrows().getId());
        if (normal != null && getAmmoCount(normal) > 0) return normal;

        return null;
    }

    public void performAttack(Attack attack, Enemy target) {
        Attack knownAttack = null;
        for (Attack a : attackList) {
            if (a.getName().equals(attack.getName())) {
                knownAttack = a;
                break;
            }
        }

        if (knownAttack == null) {
            System.out.println(name + " doesn't have the " + attack.getName() + " attack yet!");
            return;
        }

        if (knownAttack.canUse(this)) {
            knownAttack.execute(this, target);
        } else {
            System.out.println("Not enough actions to use " + knownAttack.getName() + "!");
        }
    }

    public void performSpecial(Special special, Enemy enemyOrNull) {
        Special known = null;
        for (Special s : specialList) {
            if (s.getName().equals(special.getName())) {
                known = s;
                break;
            }
        }

        if (known == null) {
            System.out.println(name + " doesn't have the " + special.getName() + " special yet!");
            return;
        }

        if (known.canUse(this)) {
            known.execute(this, enemyOrNull);
        } else {
            System.out.println("Not enough actions to use " + known.getName() + "!");
        }
    }

    public void addWeapon(Weapon weapon) {
        weaponInventory.add(weapon);
    }

    public void equipWeapon(int index) {
        if (index < 0 || index >= weaponInventory.size()) {
            System.out.println("Invalid weapon index.");
            return;
        }
        currentWeapon = weaponInventory.get(index);
        System.out.println("Equipped: " + currentWeapon.getName());
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public String getEquippedWeaponName() {
        return (currentWeapon == null) ? "None" : currentWeapon.getName();
    }

    public LinkedList<Weapon> getWeaponInventory() {
        return weaponInventory;
    }

    public void removeDurability() {
        if (currentWeapon == null) return;
        removeDurability(currentWeapon, 1);
    }

    public void removeDurability(Weapon weapon, double amount) {
        if (weapon == null) return;
        weapon.durability -= amount;
        if (weapon.isBroken()) {
            System.out.println(weapon.getName() + " broke!");
            weaponInventory.remove(weapon);
            if (weapon == currentWeapon) currentWeapon = null;
        }
    }

    public void resetActionEcon() {
        this.currentMainActions = maxMainActions;
        this.currentQuickActions = maxQuickActions;
    }

    public void equipWeaponSafeNone() {
        this.currentWeapon = null;
    }

    public void onTurnEnd() { }

    public void onKill(Scanner scnr) { }

    public void spendMainAction(int amount) { this.currentMainActions -= amount; }

    public void spendQuickAction(int amount) { this.currentQuickActions -= amount; }

    public void spendMoveSpeed(int amount) { this.moveSpeed -= amount; }

    @Override
    public int getMoveSpeed() { return this.moveSpeed; }

    public void addAttack(Attack attack) { attackList.add(attack); }

    public void addSpecial(Special special) { specialList.add(special); }

    public LinkedList<Special> getSpecialList() { return specialList; }

    public int getMana() { return mana; }

    public void setMana(int mana) { this.mana = mana; }

    public int getCurrentMainActions() { return currentMainActions; }

    public int getCurrentQuickActions() { return currentQuickActions; }

    public void setCurrentMainActions(int val) { this.currentMainActions = val; }

    public void setCurrentQuickActions(int val) { this.currentQuickActions = val; }

    public int getConsumableCount(Items item) { return consumableInventory.getOrDefault(item.getId(), 0); }

    public String[] getOptions() { return options; }

    public LinkedList<Attack> getAttackList() { return attackList; }

    public int getStr() { return this.str; }

    public int getDex() { return this.dex; }

    public int getChr() { return this.chr; }

    public int getCasinoDebt() { return casinoDebt; }

    public void addCasinoDebt(int amount) {
        casinoDebt += amount;
        if (casinoDebt < 0) casinoDebt = 0;
    }

    public Bank getBank() { return bank; }

    public BankState getBankState() { return bankState; }
}
