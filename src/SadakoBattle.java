/*
I tried for SO LONG to get this to work- the japanese hirgana in the console that it

orgiannly I was going to use jswing to make a giu. If you want that I can send you the demo I made with it. The japanese text works over there 
(granted the puzzles are not implemented, just the diffretn fonts and texts)

but becuase I demo'd and designed each part of this game in it's own folder then merged it all togther, I had unittentally designed to 
depend on Scanner, which is sycnronys. But my jswing set up was async so it oculdn't be neatly ported over and I only had 2 days left to 
finish this project. My orginal plan was to make a custom console but alas time contraints really crunched down on me. In fact you can still see the old logo I had for the task bar and window 
of the jframe burried in the resources. If github and grdale hadn't sh*t the bed maybe I would've had enough hours to think of solution, but really my lack of planning is 
why I failed, I should've planned everything then coded before just coding. Will do for next time

So this projcet is added to a list of failures in all regards as I failed to meet the baseline idea I had for this project. Like everything else I make. 

*/



import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

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
                "THめ DめLUDGめ OF 'Fめ_ _'\n" +
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
        System.out.println("You have 28 seconds to solve the puzzle.");
        System.out.println("Type the answer at any time (press Enter).");


        Puzzle p = PUZZLES.get(RNG.nextInt(PUZZLES.size()));
        System.out.println();
        System.out.println("You have 28 seconds.");
        System.out.println("--- PUZZLE ---");
        System.out.println(p.prompt);

        final long totalMillis = 28_000L;
        final long start = System.currentTimeMillis();

        System.out.print("> ");
        while (System.currentTimeMillis() - start < totalMillis) {
            if (stdinHasLineReady()) {
                String attempt = in.nextLine();
                String ans = attempt.trim().toLowerCase(Locale.ROOT);

                if (ans.equals(p.answer)) {
                    System.out.println();
                    System.out.println("The well goes silent.");
                    sleep(500);
                    System.out.println("The pressure in your skull releases.");
                    sleep(500);
                    System.out.println("You step back, and the woods remember how to breathe.");
                    return true;
                }

                if (!ans.isEmpty()) {
                    int words = ans.split("\\s+").length;

                    if (words > 1) {
                        System.out.println("Wrong. It's only one word.");
                    } else {
                        System.out.println("Wrong. You hear the water in the well get restless.");
                    }
                }

                System.out.print("> ");
            } else {
                sleep(25);
            }
        }

        System.out.println();
        System.out.println("You freeze.");
        sleep(1000);
        System.out.println("The well is the only thing in the world.");
        sleep(1000);
        System.out.println("A hand appears on the lip of the stone.");
        sleep(1000);
        System.out.println("Fingers. Nails. Slow certainty.");
        sleep(1000);
        System.out.println("A young woman, with long black hair covering her face crawls out.");
        sleep(1000);
        System.out.println("She stands.");
        sleep(1000);
        System.out.println("She takes a step.");
        sleep(1000);
        System.out.println("Another.");
        sleep(1000);

        System.out.println();
        System.out.println("As you stumble backward, you trip and fall on your head.");
        player.setHealth(player.getHealth() - 1);
        System.out.println("-1 HP.");
        if (!player.isAlive()) {
            System.out.println(player.getName() + " dies on the cold leaves.");
            System.out.println("Game over.");
            System.exit(0);
        }
        sleep(1000);
        System.out.println();

        System.out.println("Your ears ring as she takes another step.");
        sleep(1000);
        System.out.println("Your heart begins begins to make your ribs feel broken");
        sleep(1000);
        System.out.println("Her hair sways and you catch a glimmer of her eyes");
        sleep(1000);
        System.out.print("H"); sleep(200);
        System.out.print("A"); sleep(200);
        System.out.print("T"); sleep(200);
        System.out.print("E"); sleep(2000);
        System.out.print(". A"); sleep(200);
        System.out.print("N"); sleep(200);
        System.out.print("G"); sleep(200);
        System.out.print("E"); sleep(200);
        System.out.println("R");
        sleep(2000);
        System.out.println("Her energy is intolerable, your mind shatters");
        sleep(1000);

        System.out.println();
        player.setHealth(player.getHealth() - 5);
        System.out.println("-5 HP.");
        if (!player.isAlive()) {
            System.out.println(player.getName() + " dies on the cold leaves.");
            System.out.println("Game over.");
            System.exit(0);
        }
        System.out.println();
        sleep(1000);

        System.out.println("She is over you.");
        sleep(1500);
        System.out.println("Her head tilts up.");
        sleep(2000);
        System.out.println("You meet her eyes.");
        sleep(3000);

        System.out.println();
        System.out.println("Game over.");
        System.exit(0);
        return false;
    }


    //AI GENERATED
    private static boolean stdinHasLineReady() {
        try {
            return System.in.available() > 0;
        } catch (IOException e) {
            return false;
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    //END OF AI GENERATED
}
