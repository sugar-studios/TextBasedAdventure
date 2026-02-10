// Sorcerer.java
import java.util.*;

public class Sorcerer extends PlayerClass {
    private static final int BASE_AC = 10;
    private static final int BASE_MOVE = 1;

    private static final int MANA_START = 6;
    private static final int MANA_MAX = 10;

    private final Map<String, Integer> manaPools = new HashMap<>();

    private int focusToHitStored = 0;

    private boolean wardActive = false;

    private final Map<Enemy, Integer> hexed = new HashMap<>();

    public Sorcerer(String name) {
        super(name);

        this.maxHealth = 20;

        setHealth(maxHealth);

        setArmourClass(BASE_AC);
        setMoveSpeed(BASE_MOVE);
        setDex(5);
        setStr(3);
        setChr(6);

        resetActionEcon();

        initManaPools();

        addAttack(new ArcBoltAttack());
        addAttack(new RiftLanceAttack());
        addAttack(new HexAttack());

        addSpecial(new ChannelSpecial());
        addSpecial(new WardSpecial());
        addSpecial(new FocusSpecial());
    }

    private void initManaPools() {
        manaPools.put(ArcBoltAttack.KEY, MANA_START);
        manaPools.put(RiftLanceAttack.KEY, MANA_START);
        manaPools.put(HexAttack.KEY, MANA_START);
        manaPools.put(WardSpecial.KEY, MANA_START);
    }

    @Override
    public void startNewTurn() {
        resetActionEcon();

        setMoveSpeed(BASE_MOVE);
        setArmourClass(BASE_AC);

        if (wardActive) wardActive = false;

        focusToHitStored = 0;

        if (!hexed.isEmpty()) {
            ArrayList<Enemy> toRemove = new ArrayList<>();
            for (Map.Entry<Enemy, Integer> e : hexed.entrySet()) {
                Enemy target = e.getKey();
                int t = e.getValue();
                if (target == null) {
                    toRemove.add(null);
                    continue;
                }
                if (t <= 0) {
                    toRemove.add(target);
                    continue;
                }
                hexed.put(target, t - 1);
                if (t - 1 <= 0) {
                    target.setArmourClass(target.getArmourClass() + 2);
                    toRemove.add(target);
                }
            }
            for (Enemy r : toRemove) hexed.remove(r);
        }

        for (Special s : getSpecialList()) s.onTurnStart(this);

        System.out.println("\n--- " + name + "'s Turn Started ---");
    }

    @Override
    public void resetActionEcon() {
        setCurrentMainActions(1);
        setCurrentQuickActions(3);
    }

    public int getMana(String key) {
        return manaPools.getOrDefault(key, 0);
    }

    public boolean hasMana(String key, int cost) {
        return getMana(key) >= cost;
    }

    public void spendMana(String key, int cost) {
        manaPools.put(key, Math.max(0, getMana(key) - Math.max(0, cost)));
    }

    public void addMana(String key, int amount) {
        manaPools.put(key, Math.min(MANA_MAX, getMana(key) + Math.max(0, amount)));
    }

    public void addManaToLowestPool(int amount) {
        String bestKey = null;
        int bestVal = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> e : manaPools.entrySet()) {
            if (e.getValue() >= MANA_MAX) continue;
            if (e.getValue() < bestVal) {
                bestVal = e.getValue();
                bestKey = e.getKey();
            }
        }

        if (bestKey == null) {
            System.out.println("Channel: all spell pools are already at max mana.");
            return;
        }

        addMana(bestKey, amount);
        System.out.println("Channel: +" + amount + " mana to " + bestKey + " (now " + getMana(bestKey) + ").");
    }

    public int consumeFocusToHit() {
        int v = focusToHitStored;
        focusToHitStored = 0;
        return v;
    }

    public void applyFocus() {
        focusToHitStored = 2;
    }

    public void applyWard() {
        if (!wardActive) {
            wardActive = true;
            setArmourClass(getArmourClass() + 2);
        }
    }

    public void applyHex(Enemy enemy) {
        if (enemy == null) return;
        enemy.setArmourClass(enemy.getArmourClass() - 2);
        hexed.put(enemy, 1);
    }

    @Override
    public void onKill(Scanner scnr) {
        System.out.println("Kill Replenish: choose one:");
        System.out.println("[1] +3 mana to a spell of your choice");
        System.out.println("[2] Heal 4 HP");

        while (true) {
            String raw = scnr.nextLine().trim().toLowerCase();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }
            char c = raw.charAt(0);

            if (c == '2') {
                setHealth(getHealth() + 4.0);
                System.out.println("You healed 4 HP.");
                return;
            }

            if (c == '1') {
                chooseManaPoolToAdd(scnr, 3);
                return;
            }

            System.out.println("Invalid Input");
        }
    }

    private void chooseManaPoolToAdd(Scanner scnr, int amount) {
        ArrayList<String> keys = new ArrayList<>(manaPools.keySet());
        Collections.sort(keys);

        System.out.println("Pick a spell pool to gain +" + amount + " mana:");
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i);
            System.out.println(String.format("[%d] %s (mana %d/%d)", i + 1, k, getMana(k), MANA_MAX));
        }
        System.out.println("[0] Cancel");

        while (true) {
            String raw = scnr.nextLine().trim();
            if (raw.isEmpty()) {
                System.out.println("Invalid Input");
                continue;
            }
            Integer choice;
            try {
                choice = Integer.parseInt(raw);
            } catch (Exception e) {
                System.out.println("Invalid Input");
                continue;
            }

            if (choice == 0) return;

            int idx = choice - 1;
            if (idx < 0 || idx >= keys.size()) {
                System.out.println("Invalid Input");
                continue;
            }

            String k = keys.get(idx);
            addMana(k, amount);
            System.out.println("+" + amount + " mana to " + k + " (now " + getMana(k) + ").");
            return;
        }
    }
}
