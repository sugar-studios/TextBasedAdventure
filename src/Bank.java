public class Bank {

    public enum EarnSource {
        COMBAT_REWARD,       // per-kill + end-of-battle reward (net positive only)
        CASINO_NET_PROFIT    // session net profit only (not gross)
    }

    private final BankState s;

    public Bank(BankState state) {
        this.s = state;
    }

    public String getStatus(PlayerClass p) {
        StringBuilder sb = new StringBuilder();
        sb.append("Stage x: ").append(s.bankStageX).append("/").append(s.maxStage).append("\n");
        sb.append("Active loan: ").append(s.hasActiveLoan ? "YES" : "NO").append("\n");
        if (s.hasActiveLoan) {
            sb.append("Loan amount: ").append(s.activeLoanAmount).append("\n");
            sb.append("Repay amount: ").append(s.activeRepayAmount).append("\n");
            if (s.post11TrackerEnabled) {
                sb.append("Settlement tracker remaining: ").append(s.post11TrackerRemaining).append("\n");
            }
        }
        sb.append("Player gold: ").append(p.getGold());
        return sb.toString();
    }

    public boolean canTakeLoan() {
        if (s.hasActiveLoan) return false;
        return s.bankStageX <= s.maxStage;
    }

    public String takeLoan(PlayerClass p) {
        if (s.hasActiveLoan) return "Denied: You still owe the bank.";
        if (s.bankStageX > s.maxStage) return "Denied: No further credit will be extended.";

        int x = s.bankStageX;

        int L = loan(x);
        int R = repay(x);

        p.setGold(p.getGold() + L);

        s.hasActiveLoan = true;
        s.activeLoanAmount = L;
        s.activeRepayAmount = R;

        s.post11TrackerEnabled = (x >= 11);
        if (s.post11TrackerEnabled) {
            s.post11TrackerRemaining = (L + 1) / 2; // ceil(0.5*L)
        } else {
            s.post11TrackerRemaining = 0;
        }

        return "Approved. Loaned " + L + " gold. Repay amount: " + R +
                (s.post11TrackerEnabled ? " (Settlement locked until tracker clears: " + s.post11TrackerRemaining + ")" : "");
    }

    public boolean canRepay(PlayerClass p) {
        if (!s.hasActiveLoan) return false;
        if (s.post11TrackerEnabled && s.post11TrackerRemaining > 0) return false;
        return p.getGold() >= s.activeRepayAmount;
    }

    public String repayLoan(PlayerClass p) {
        if (!s.hasActiveLoan) return "Denied: You have no active loan.";

        if (s.post11TrackerEnabled && s.post11TrackerRemaining > 0) {
            return "Denied: Your account is not yet eligible for settlement. Earn more. Remaining: " + s.post11TrackerRemaining;
        }

        if (p.getGold() < s.activeRepayAmount) {
            return "Denied: Insufficient funds. Need " + s.activeRepayAmount + ", you have " + p.getGold() + ".";
        }

        p.setGold(p.getGold() - s.activeRepayAmount);

        s.hasActiveLoan = false;
        s.activeLoanAmount = 0;
        s.activeRepayAmount = 0;
        s.post11TrackerRemaining = 0;
        s.post11TrackerEnabled = false;

        s.bankStageX += 1; // progression
        if (s.bankStageX == s.maxStage + 1) {
            // stage 16 means permanently closed
            return "Loan repaid. Your stage is now 16. No further credit will be extended.";
        }

        return "Loan repaid. Stage advanced to x=" + s.bankStageX + ".";
    }

    public void onGoldEarned(int amountEarned, EarnSource source) {
        if (amountEarned <= 0) return;

        if (!s.hasActiveLoan) return;
        if (!s.post11TrackerEnabled) return;
        if (s.post11TrackerRemaining <= 0) return;

        // Eligible sources only (as you specified).
        if (source != EarnSource.COMBAT_REWARD && source != EarnSource.CASINO_NET_PROFIT) return;

        s.post11TrackerRemaining = Math.max(0, s.post11TrackerRemaining - amountEarned);
    }

    // ---------------- MATH ----------------

    // Bonus:
    // y = cbrt(2) * x^(5/3) - 2x - 1
    public static double bonusY(int x) {
        double cbrt2 = Math.cbrt(2.0);
        double xPow = Math.pow(x, 5.0 / 3.0);
        return cbrt2 * xPow - 2.0 * x - 1.0;
    }

    // Loan:
    // base = 5 + 5x
    // appliedBonus = min(base, 0.5 * max(0, y))
    // loan = floor(base + appliedBonus)
    public static int loan(int x) {
        double base = 5.0 + 5.0 * x;
        double y = bonusY(x);
        double appliedBonus = Math.min(base, 0.5 * Math.max(0.0, y));
        return (int) Math.floor(base + appliedBonus);
    }

    // Repay:
    // repay = bankersRound((5 + 5x) * 1.25 + 6)
    public static int repay(int x) {
        double base = 5.0 + 5.0 * x;
        double raw = base * 1.25 + 6.0;
        return bankersRound(raw);
    }

    // Bankers rounding: .5 rounds to nearest even integer.
    public static int bankersRound(double v) {
        double floor = Math.floor(v);
        double frac = v - floor;

        if (frac < 0.5) return (int) floor;
        if (frac > 0.5) return (int) (floor + 1);

        // exactly .5
        int f = (int) floor;
        return (f % 2 == 0) ? f : (f + 1);
    }
}
