public class HitChance {

    public static int strDexCheck(int str, int dex) {
        return (int) (str + dex) / 2;
    }

    public static int strChrCheck(int str, int chr) {
        return (int) (str + chr) / 2;
    }

    public static int dexChrCheck(int dex, int chr) {
        return (int) (dex + chr) / 2;
    }



    public static int acCheckWithCrit(int ac, int hitChance) {
        int dieRoll = (int)(Math.random() * 20) + 1;
        int total = dieRoll + hitChance;

        if (dieRoll == 20) {
            return 2; 
        } else if (total >= ac) {
            return 1; 
        } else {
            return 0;
        }
    }

    //overload for crit bonus
    public static int acCheckWithCrit(int ac, int hitChance, double critBonus) {
    int dieRoll = (int)(Math.random() * 20) + 1;
    int total = dieRoll + hitChance;
    
    int critThreshold = 20 - (int)(20 * critBonus); 

    if (dieRoll >= critThreshold) {
        return 2; 
    } else if (total >= ac) {
        return 1; 
    } else {
        return 0;
    }
}

    public static boolean acCheck(int ac, int hitChance) {
        boolean passAcCheck;
        int randomNum = (int)(Math.random() * 20) + 1 + hitChance;

        if (randomNum >= ac) {
            passAcCheck = true;
        } else {
            passAcCheck = false;
        }

        return passAcCheck;

    }

    public static int checkHitChanceType(int hitChanceCode, Entity e) {
        return switch (hitChanceCode) {
            case 1 -> e.getStr();
            case 2 -> e.getDex();
            case 3 -> e.getChr();
            case 12 -> (e.getStr() + e.getDex()) / 2;
            case 13 -> (e.getStr() + e.getChr()) / 2;
            case 21 -> (e.getDex() + e.getChr()) / 2;
            default -> 0;
        };
    }
}