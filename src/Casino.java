import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

public class Casino {

    private static final double BLACKJACK_PAYOUT_MULT = 1.5;

    public static void playBlackjack(PlayerClass player, Scanner in) {
        System.out.println();
        System.out.println("She slides you a seat. Cards whisper over felt.");
        if (player.getCasinoDebt() > 0) {
            System.out.println("The casino's ledger already has your name in it.");
            System.out.println("Debt: " + player.getCasinoDebt());
            System.out.println("Any winnings will be taken to reduce it.");
        }

        int startingDebt = player.getCasinoDebt();
        int debtLossesThisVisit = 0;

        Deck deck = new Deck();

        while (true) {
            System.out.println();
            System.out.println("=== BLACKJACK ===");
            System.out.println("Gold: " + player.getGold() + " | Debt: " + player.getCasinoDebt());
            System.out.println("Type: bet <amount>, leave");

            String cmdLine = readLine(in);
            if (cmdLine.equals("leave") || cmdLine.equals("back") || cmdLine.equals("exit")) {
                System.out.println("You step away from the table.");
                return;
            }

            if (!cmdLine.startsWith("bet")) {
                System.out.println("Use: bet <amount> or leave");
                continue;
            }

            int bet = parseBet(cmdLine);
            if (bet <= 0) {
                System.out.println("Bet must be at least 1.");
                continue;
            }

            // If deck getting low, refresh
            if (deck.remaining() < 15) deck = new Deck();

            Hand playerHand = new Hand();
            Hand dealerHand = new Hand();

            playerHand.add(deck.draw());
            dealerHand.add(deck.draw());
            playerHand.add(deck.draw());
            dealerHand.add(deck.draw());

            boolean playerBJ = playerHand.isBlackjack();
            boolean dealerBJ = dealerHand.isBlackjack();

            System.out.println();
            System.out.println("Dealer shows: " + dealerHand.cards.get(0) + " [hidden]");
            System.out.println("You: " + playerHand + " (" + playerHand.bestValue() + ")");

            Outcome outcome;

            //player can only hit or stand
            if (playerBJ || dealerBJ) {
                if (playerBJ && dealerBJ) outcome = Outcome.PUSH;
                else if (playerBJ) outcome = Outcome.PLAYER_BLACKJACK;
                else outcome = Outcome.PLAYER_LOSS;
            } else {
                while (true) {
                    if (playerHand.bestValue() > 21) break;

                    System.out.print("Hit or stand? ");
                    String act = readLine(in);
                    if (act.equals("hit") || act.equals("h")) {
                        playerHand.add(deck.draw());
                        System.out.println("You: " + playerHand + " (" + playerHand.bestValue() + ")");
                    } else if (act.equals("stand") || act.equals("s")) {
                        break;
                    } else {
                        System.out.println("Type hit or stand.");
                    }
                }

                if (playerHand.bestValue() > 21) {
                    outcome = Outcome.PLAYER_BUST;
                } else {
                    // Dealer turn: reveal + play, stand on soft 17
                    System.out.println();
                    System.out.println("Dealer reveals: " + dealerHand + " (" + dealerHand.bestValue() + ")");
                    while (dealerShouldHit(dealerHand)) {
                        dealerHand.add(deck.draw());
                        System.out.println("Dealer hits: " + dealerHand + " (" + dealerHand.bestValue() + ")");
                    }

                    int pv = playerHand.bestValue();
                    int dv = dealerHand.bestValue();

                    if (dv > 21) outcome = Outcome.DEALER_BUST;
                    else if (pv > dv) outcome = Outcome.PLAYER_WIN;
                    else if (pv < dv) outcome = Outcome.PLAYER_LOSS;
                    else outcome = Outcome.PUSH;
                }
            }

            int deltaGold = 0;
            double payoff = 0.0;

            switch (outcome) {
                case PLAYER_BLACKJACK -> {
                    payoff = bet * BLACKJACK_PAYOUT_MULT;
                    deltaGold = bet + (int)Math.floor(payoff);
                    System.out.println("Blackjack.");
                }
                case PLAYER_WIN, DEALER_BUST -> {
                    deltaGold = bet;
                    System.out.println(outcome == Outcome.DEALER_BUST ? "Dealer busts." : "You win.");
                }
                case PUSH -> {
                    deltaGold = 0;
                    System.out.println("Push.");
                }
                case PLAYER_BUST -> {
                    deltaGold = -bet;
                    System.out.println("You bust.");
                }
                case PLAYER_LOSS -> {
                    deltaGold = -bet;
                    System.out.println("You lose.");
                }
            }

            // Apply result with debt rules:
            // - If entering with debt: winnings go to debt first.
            // - Losses increase debt if gold isn't enough (casino credit).
            // - If startingDebt == 0 and you end up with any debt: kicked out immediately.
            // - If you have debt and lose two hands this visit: kicked out.

            boolean hadDebtAtStartOfHand = (player.getCasinoDebt() > 0);
            boolean wentIntoDebtThisHandFromClean = (startingDebt == 0);

            if (deltaGold > 0) {
                // winnings
                if (player.getCasinoDebt() > 0) {
                    int used = Math.min(player.getCasinoDebt(), deltaGold);
                    player.addCasinoDebt(-used);
                    int remainingWin = deltaGold - used;
                    if (remainingWin > 0) player.setGold(player.getGold() + remainingWin);

                    System.out.println("Casino takes " + used + " to reduce your debt.");
                    if (remainingWin > 0) System.out.println("You keep " + remainingWin + ".");
                } else {
                    player.setGold(player.getGold() + deltaGold);
                }

                // winning breaks a loss streak for debt visits
                if (startingDebt > 0) debtLossesThisVisit = 0;
            } else if (deltaGold < 0) {
                int loss = -deltaGold;

                // pay from gold first; if not enough, remainder becomes debt
                int payFromGold = Math.min(player.getGold(), loss);
                player.setGold(player.getGold() - payFromGold);
                int remainder = loss - payFromGold;
                if (remainder > 0) {
                    player.addCasinoDebt(remainder);
                }

                // track "lose two hands" only if you currently have debt (either already had it, or just got it)
                if (player.getCasinoDebt() > 0 || hadDebtAtStartOfHand) {
                    debtLossesThisVisit++;
                }

                if (remainder > 0) {
                    System.out.println("You couldn't cover the full loss. The remainder is added to your debt: +" + remainder);
                }
            }

            System.out.println("Gold now: " + player.getGold() + " | Debt now: " + player.getCasinoDebt());

            if (wentIntoDebtThisHandFromClean && startingDebt == 0 && player.getCasinoDebt() > 0) {
                System.out.println();
                System.out.println("The hostess' smile doesn't change, but the tone does.");
                System.out.println("\"You're done. Come back when you can afford to bleed properly.\"");
                System.out.println("Kicked out. Debt recorded: " + player.getCasinoDebt());
                return;
            }

            if (startingDebt > 0 && debtLossesThisVisit >= 2) {
                System.out.println();
                System.out.println("Two losses while owing. The table goes cold.");
                System.out.println("\"Out.\"");
                return;
            }
        }
    }

    private static boolean dealerShouldHit(Hand dealer) {
        int best = dealer.bestValue();
        if (best < 17) return true;
        if (best > 17) return false;
        return false;
    }

    private enum Outcome {
        PLAYER_WIN,
        PLAYER_LOSS,
        PUSH,
        PLAYER_BUST,
        DEALER_BUST,
        PLAYER_BLACKJACK
    }

    private static String readLine(Scanner in) {
        return in.nextLine().trim().toLowerCase(Locale.ROOT);
    }

    private static int parseBet(String cmdLine) {
        String[] parts = cmdLine.split("\\s+");
        if (parts.length < 2) return -1;
        try { return Integer.parseInt(parts[1]); }
        catch (Exception e) { return -1; }
    }

    private static class Deck {
        private final List<Card> cards = new ArrayList<>();
        private int idx = 0;

        Deck() {
            for (Suit s : Suit.values()) {
                for (Rank r : Rank.values()) {
                    cards.add(new Card(r, s));
                }
            }
            Collections.shuffle(cards, new Random());
        }

        Card draw() {
            if (idx >= cards.size()) {
                Collections.shuffle(cards, new Random());
                idx = 0;
            }
            return cards.get(idx++);
        }

        int remaining() {
            return cards.size() - idx;
        }
    }

    private enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    private enum Rank {
        TWO(2, "2"),
        THREE(3, "3"),
        FOUR(4, "4"),
        FIVE(5, "5"),
        SIX(6, "6"),
        SEVEN(7, "7"),
        EIGHT(8, "8"),
        NINE(9, "9"),
        TEN(10, "10"),
        JACK(10, "J"),
        QUEEN(10, "Q"),
        KING(10, "K"),
        ACE(11, "A");

        final int value;
        final String label;

        Rank(int value, String label) {
            this.value = value;
            this.label = label;
        }
    }

    private static class Card {
        final Rank rank;
        final Suit suit;

        Card(Rank rank, Suit suit) {
            this.rank = rank;
            this.suit = suit;
        }

        int value() {
            return rank.value;
        }

        @Override
        public String toString() {
            return rank.label;
        }
    }

    private static class Hand {
        final List<Card> cards = new ArrayList<>();

        void add(Card c) { cards.add(c); }

        boolean isBlackjack() {
            return cards.size() == 2 && bestValue() == 21;
        }

        int bestValue() {
            int total = 0;
            int aces = 0;
            for (Card c : cards) {
                total += c.value();
                if (c.rank == Rank.ACE) aces++;
            }
            while (total > 21 && aces > 0) {
                total -= 10;
                aces--;
            }
            return total;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cards.size(); i++) {
                if (i > 0) sb.append(" ");
                sb.append(cards.get(i));
            }
            return sb.toString();
        }
    }
}
