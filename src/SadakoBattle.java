import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class SadakoBattle {

    private SadakoBattle() {}

    private static final class Puzzle {
        final String prompt;
        final String answer;
        Puzzle(String prompt, String answer) {
            this.prompt = prompt;
            this.answer = answer;
        }
    }

    private static final Random RNG = new Random();

    private static final List<Puzzle> PUZZLES = List.of(
        new Puzzle(
                "うN   うPPふる   FEふふ   'FR_ _'   THる   TRるる\n" +
                "\n" +
                "る = E\n" +
                "ぷ = L\n" +
                "\n" +
                "What word is FR_ _ ?\n",
                "from"
        ),
        new Puzzle(
                "TこE   FLののDくひTES   OF   '_ひくE'   SこひLL   のPEN   ひくひIN\n" +
                "\n" +
                "こ = H\n" +
                "\n" +
                "What word is _えくE?\n",
                "rage"
        ),
        new Puzzle(
                "つHE   'らつO_む'   IS   くOむING,   らEE   つHE   くLOUDら\n" +
                "\n" +
                "て = T\n" +
                "\n" +
                "What word is らつO_む?\n",
                "storm"
        ),
        new Puzzle(
                "THち   UNちVN   'Nあ_ _'    GちTS   HあMMちRちD   OUT\n" +
                "\n" +
                "み = N\n" +
                "\n" +
                "What word is Nあ_  _?\n",
                "nail"
        ),
        new Puzzle(
                "CとT   くとT   'Yくと_ '  くW N   TくNGとE,   つND   SWつLLくW\n" +
                "\n" +
                "A = つ\n" +
                "\n" +
                "What word is Yくと_ ?\n",
                "your"
        ),
        new Puzzle(
                "PLこカ   Bカ   THも   'O_もこN'   WこVもS,   MONSTもRS   Bも   カOUR   GRこVも\n" +
                "\n" +
                "カ = Y\n" +
                "\n" +
                "What word is O_もこN?\n",
                "ocean"
        ),
        new Puzzle(
                "ちNO   VIてつO,   もつVつN   てAYも   '_つF_'\n" +
                "\n" +
                "て = D\n" +
                "\n" +
                "What word is Lつ _ _?\n",
                "led"
        ),
        new Puzzle(
            "THめ DめLUDGめ OF 'Fめ_ _'" +
            "\n" +
            "What word is 'Fめ_ _'?\n",
            "fear"
        )
    );

    public static boolean run(PlayerClass player, Scanner in) {
        System.out.println();
        System.out.println("You push deeper into the woods.");
        sleep(2000);
        System.out.println("The air turns still. Even the birds stop.");
        sleep(2000);
        System.out.println("You find a well you don't remember existing.");
        sleep(2000);
        System.out.println("It smells like cold stone and old water.");
        sleep(2000);
        System.out.println();
        System.out.println("Something inside you whispers: solve it. Quickly.");
        System.out.println("You have 7 turns. A turn passes every 4 seconds.");
        System.out.println("Type the answer at any time.");

        Puzzle p = PUZZLES.get(RNG.nextInt(PUZZLES.size()));
        System.out.println();
        System.out.println("--- PUZZLE ---");
        System.out.println(p.prompt);

        BlockingQueue<String> lines = new LinkedBlockingQueue<>();

        Thread inputThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String s = in.nextLine();
                    if (s == null) break;
                    lines.offer(s);
                }
            } catch (Exception ignored) {
            }
        }, "SadakoInputThread");
        inputThread.setDaemon(true);
        inputThread.start();

        final int totalTurns = 7;
        final long turnMillis = 4000;

        long start = System.currentTimeMillis();

        for (int turn = 1; turn <= totalTurns; turn++) {
            long turnStart = System.currentTimeMillis();

            System.out.println();
            System.out.println("Turn " + turn + "/" + totalTurns + " (time remaining: " +
                    Math.max(0, 28 - ((System.currentTimeMillis() - start) / 1000)) + "s)");
            System.out.print("> ");

            long remaining = turnMillis;

            // During this 4-second window, accept as many attempts as the player types.
            while (remaining > 0) {
                String attempt;
                try {
                    attempt = lines.poll(remaining, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    attempt = null;
                }

                if (attempt != null) {
                    String ans = attempt.trim().toLowerCase(Locale.ROOT);
                    if (ans.equals(p.answer)) {
                        System.out.println();
                        System.out.println("The well goes silent.");
                        sleep(500);
                        System.out.println("The pressure in your skull releases.");
                        sleep(500);
                        System.out.println("You step back, and the woods remember how to breathe.");
                        return true; // survived
                    } else if (!ans.isEmpty()) {
                        System.out.println("Wrong.");
                        System.out.print("> ");
                    }
                }

                long now = System.currentTimeMillis();
                remaining = turnMillis - (now - turnStart);
            }

            // Turn auto-advances after 4 seconds
            System.out.println();
            System.out.println("(A turn passes.)");
        }

        // Time up -> cutscene
        try { inputThread.interrupt(); } catch (Exception ignored) {}

        System.out.println();
        System.out.println("You freeze.");
        sleep(3000);
        System.out.println("The well is the only thing in the world.");
        sleep(3000);
        System.out.println("A hand appears on the lip of the stone.");
        sleep(3000);
        System.out.println("Fingers. Nails. Slow certainty.");
        sleep(3000);
        System.out.println("A young woman, with long black hair covering her face crawls out.");
        sleep(3000);
        System.out.println("She stands.");
        sleep(3000);
        System.out.println("She takes a step.");
        sleep(3000);
        System.out.println("Another.");
        sleep(3000);

        System.out.println();
        System.out.println("As you stumble backward, you trip and fall on your head.");
        player.setHealth(player.getHealth() - 1);
        System.out.println("-1 HP.");
        if (!player.isAlive()) {
            System.out.println(player.getName() + " dies on the cold leaves.");
            System.out.println("Game over.");
            System.exit(0);
        }
        sleep(3000);
        System.out.println();

        System.out.println("Your ears ring as she takes another step.");
        sleep(3000);
        System.out.println("Your heart begins begins to make your ribs feel broken");
        sleep(3000);
        System.out.println("Her hair sways and you catch a glimmer of her eyes");
        sleep(1000);
        System.out.print("H");
        sleep(200);
        System.out.print("A");
        sleep(200);
        System.out.print("T");
        sleep(200);
        System.out.print("E");
        sleep(2000);
        System.out.print(". A");
        sleep(200);
        System.out.print("N");
        sleep(200);
        System.out.print("G");
        sleep(200);
        System.out.print("E");
        sleep(200);
        System.out.println("R");
        sleep(2000);
        System.out.println("Her energy is intolerable, your mind shatters");
        sleep(1000);

        System.out.println();
        player.setHealth(player.getHealth() - 1);
        System.out.println("-5 HP.");
        if (!player.isAlive()) {
            System.out.println(player.getName() + " dies on the cold leaves.");
            System.out.println("Game over.");
            System.exit(0);
        }
        System.out.println();
        sleep(3000);

        System.out.println("She is over you.");
        sleep(3500);
        System.out.println("Her head tilts up.");
        sleep(4000);
        System.out.println("You meet her eyes.");
        sleep(1200);

        System.out.println();
        System.out.println("Game over.");
        System.exit(0);
        return false;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
