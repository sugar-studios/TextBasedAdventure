import java.util.*;

public final class Interactions {

private static final Random RNG = new Random();

    private Interactions() {}

        private static void playMenuClick() {
        // click1.wav ... click7.wav
        int n = 1 + RNG.nextInt(7);
        Audio.playSfx("click" + n + ".wav", -10.0f);
    }

    private static final class Option {
        final String key;     // "a","b","c"
        final String label;
        final String nextId;  // null = exit
        Option(String key, String label, String nextId) {
            this.key = key;
            this.label = label;
            this.nextId = nextId;
        }
    }

    private static final class Node {
        final String id;
        final List<String> npcLines;
        final List<Option> options;
        Node(String id, List<String> npcLines, List<Option> options) {
            this.id = id;
            this.npcLines = npcLines;
            this.options = options;
        }
    }

    private static void runDialogue(String title, Map<String, Node> nodes, String[] startIds, Scanner in) {
        Random rng = new Random();
        String current = startIds[rng.nextInt(startIds.length)];

        while (current != null) {
            Node n = nodes.get(current);
            if (n == null) {
                System.out.println("[Dialogue error: missing node " + current + "]");
                return;
            }

            System.out.println();
            System.out.println("[" + title + "]");
            for (String line : n.npcLines) System.out.println(line);

            for (Option o : n.options) System.out.println("  (" + o.key + ") " + o.label);

            String choice = readChoice(in, n.options);
            playMenuClick();
            Option picked = null;
            for (Option o : n.options) if (o.key.equals(choice)) { picked = o; break; }
            current = (picked == null) ? null : picked.nextId;
        }
    }

    private static String readChoice(Scanner in, List<Option> opts) {
        Set<String> allowed = new HashSet<>();
        for (Option o : opts) allowed.add(o.key);
        while (true) {
            System.out.print("> ");
            String s = in.nextLine().trim().toLowerCase(Locale.ROOT);
            if (allowed.contains(s)) return s;
            System.out.println("Choose one of: " + String.join(", ", allowed));
        }
    }

    private static List<String> L(String... lines) { return Arrays.asList(lines); }


    public static void talkCasino(PlayerClass p, Scanner in) {
        runDialogue("Casino - Bunny Hostess", casinoNodes(), new String[]{"c0","c1","c2"}, in);
    }

    public static void talkShop(PlayerClass p, Scanner in) {
        runDialogue("Shop - Witch Outfit", shopNodes(), new String[]{"s0","s1","s2"}, in);
    }

    public static void talkArmory(PlayerClass p, Scanner in) {
        runDialogue("Armory - 'General'", armoryNodes(), new String[]{"a0","a1","a2"}, in);
    }

    public static void talkBank(PlayerClass p, Scanner in) {
        runDialogue("Bank - Suit & Glasses", bankNodes(), new String[]{"b0","b1","b2"}, in);
    }

    public static void talkHospital(PlayerClass p, Scanner in) {
        runDialogue("Hospital - Nurse Outfit", hospitalNodes(), new String[]{"h0","h1","h2"}, in);
    }


    private static Map<String, Node> casinoNodes() {
        Map<String, Node> m = new LinkedHashMap<>();

        m.put("c0", new Node("c0", L(
                "She leans on the counter, bored on purpose.",
                "\"Welcome. Sit. Spend. Repeat.\""
        ), List.of(
                new Option("a", "Ask why she wants you to stay.", "cA"),
                new Option("b", "Ask about the 'same face’ around town.", "cB"),
                new Option("c", "Leave.", null)
        )));

        m.put("c1", new Node("c1", L(
                "She smiles like it costs her nothing.",
                "\"One more drink and the house starts calling you family.\""
        ), List.of(
                new Option("a", "Ask what the house actually wants.", "cC"),
                new Option("b", "Ask if she ever feels guilty.", "cD"),
                new Option("c", "Leave.", null)
        )));

        m.put("c2", new Node("c2", L(
                "She tilts her head, watching you like a wager.",
                "\"You look like you're here to test your luck.\""
        ), List.of(
                new Option("a", "Ask for one honest tip.", "cE"),
                new Option("b", "Call out the act: \"You're luring people.\" ", "cF"),
                new Option("c", "Leave.", null)
        )));

        m.put("cA", new Node("cA", L(
                "\"Because staying is easy.\"",
                "\"Leaving requires a spine.\""
        ), List.of(
                new Option("a", "Ask what she does all night.", "cG"),
                new Option("b", "Ask if anyone ever truly wins.", "cH"),
                new Option("c", "Leave.", null)
        )));

        m.put("cB", new Node("cB", L(
                "\"Same face?\" She shrugs.",
                "\"Either you're observant… or you're lonely.\""
        ), List.of(
                new Option("a", "Press harder.", "cI"),
                new Option("b", "Back off and change topic.", "cE"),
                new Option("c", "Leave.", null)
        )));

        m.put("cC", new Node("cC", L(
                "\"The house sells permission.\"",
                "\"Permission to be reckless.\""
        ), List.of(
                new Option("a", "Ask if she believes in luck.", "cE"),
                new Option("b", "Ask why she works here.", "cG"),
                new Option("c", "Leave.", null)
        )));

        m.put("cD", new Node("cD", L(
                "\"Guilt is for thieves.\"",
                "\"I just hold the door open.\""
        ), List.of(
                new Option("a", "Call that a cop-out.", "cF"),
                new Option("b", "Ask what she'd do elsewhere.", "cG"),
                new Option("c", "Leave.", null)
        )));

        m.put("cE", new Node("cE", L(
                "\"Set a limit before you sit.\"",
                "\"Leave while you still remember your starting number.\""
        ), List.of(
                new Option("a", "Thank her.", null),
                new Option("b", "Ask for a second tip.", "cF"),
                new Option("c", "Leave.", null)
        )));

        m.put("cF", new Node("cF", L(
                "\"Of course I'm luring people.\"",
                "\"That's the job.\""
        ), List.of(
                new Option("a", "Ask what she's paid to be.", "cG"),
                new Option("b", "Say you won't fall for it.", "cE"),
                new Option("c", "Leave.", null)
        )));

        m.put("cG", new Node("cG", L(
                "\"I watch choices happen.\"",
                "\"Then I pretend I'm not impressed when people pick the worst one.\""
        ), List.of(
                new Option("a", "Ask what impresses her.", "cH"),
                new Option("b", "Ask what scares her.", "cH"),
                new Option("c", "Leave.", null)
        )));

        m.put("cH", new Node("cH", L(
                "\"The ones who win big either leave…\"",
                "\"…or come back to prove it wasn't luck.\""
        ), List.of(
                new Option("a", "Say that sounds sad.", null),
                new Option("b", "Say that sounds familiar.", null),
                new Option("c", "Leave.", null)
        )));

        m.put("cI", new Node("cI", L(
                "Her smile stays. Her eyes don't.",
                "\"Careful. Obsession makes you sloppy.\""
        ), List.of(
                new Option("a", "Drop it.", null),
                new Option("b", "Ask anyway: \"Who are you?\"", null),
                new Option("c", "Leave.", null)
        )));

        return m;
    }

    private static Map<String, Node> shopNodes() {
        Map<String, Node> m = new LinkedHashMap<>();

        m.put("s0", new Node("s0", L(
                "\"What do you say when you meet someone, and also when you pick up the magiScroll?\""
        ), List.of(
                new Option("a", "Ask why she talks in riddles.", "sA"),
                new Option("b", "Ask about the 'same face' in town.", "sB"),
                new Option("c", "Leave.", null)
        )));

        m.put("s1", new Node("s1", L(
                "She adjusts the witches hat she wears."
        ), List.of(
                new Option("a", "Ask what she's really selling.", "sC"),
                new Option("b", "Ask why she enjoys messing with people.", "sD"),
                new Option("c", "Leave.", null)
        )));

        m.put("s2", new Node("s2", L(
        "\"I know you need me.\""
        ), List.of(
                new Option("a", "Ask what this village is.", "sE"),
                new Option("b", "Ask what she knows about you.", "sF"),
                new Option("c", "Leave.", null)
        )));

        m.put("sA", new Node("sA", L(
       "\"What has eyes that don't cross,\"",
                "\"teeth that don't floss,\"",
                "\"and trees with no moss?\""

        ), List.of(
                new Option("a", "Ask if she can be direct once.", "sC"),
                new Option("b", "Tell her it's annoying.", "sD"),
                new Option("c", "Leave.", null)
        )));

        m.put("sB", new Node("sB", L(
        "\"Either you're noticing… or you're being invited to notice.\""
        ), List.of(
                new Option("a", "Demand a straight answer.", "sF"),
                new Option("b", "Play along: \"Invite me properly.\" ", "sE"),
                new Option("c", "Leave.", null)
        )));

        m.put("sC", new Node("sC", L(
                "\"What is twice as big as the moon, but only half as far away?\""
        ), List.of(
                new Option("a", "Ask about inventory instead.", null),
                new Option("b", "Ask the answer to the riddle", "yM"),
                new Option("c", "Ask what she's hiding.", "sF"),
                new Option("d", "Leave.", null)
        )));

        m.put("sD", new Node("sD", L(
        "\"If you can't handle riddles,\"",
            "\"you can't my handle prices, darlin\'\""
        ), List.of(
                new Option("a", "Tell her she's enjoying this too much.", "sDa"),
                new Option("b", "Leave.", null)
        )));

        m.put("sDa", new Node("sDa", L(
        "A real smirk cracks her fake smile",
            "\"every time I do, darlin'. Or I wouldn't be working multiple jobs\'\""
        ), List.of(
                new Option("a", "Ask what she means by that", "sDaA"),
                new Option("b", "Leave.", null)
        )));

        m.put("sDaA", new Node("sDaA", L(
                "\"Hmm?\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("sE", new Node("sE", L(
        "\"No.\"",
                "\"Hahahaha...\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("sF", new Node("sF", L(
        "\"I know you want certainty.\"",
            "\"You'll settle for a story if certainty refuses you.\""
        ), List.of(
                new Option("a", "Ask for the story, then.", "sFb"),
                new Option("b", "Say you hate that she's right.", "sFa"),
                new Option("c", "Leave.", null)
        )));

        m.put("sFa", new Node("sFa", L(
        "\"Certainty?\"",
            "\"One plus one is always two.\"",
            "\"That's certain. Everything else is negotiation.\""
        ), List.of(
            new Option("a", "Ask if she has any other certainty.", "sFaB"),
            new Option("b", "Say that's a dodge.", "sfaA"),
            new Option("c", "Leave.", null)
        )));

        m.put("sFaB", new Node("sFaB", L(
        "\"You want another certainty?\"",
            "\"Fine.\""
        ), List.of(
            new Option("a", "Yes, another one.", "sFaBa"),
            new Option("b", "Leave.", null)
        )));

        m.put("sFaBa", new Node("sFaBa", L(
        "\"Fire is hot.\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("sfaA", new Node("sFaA", L(
        "\"I do think 1 plus one is in fact two, which is certain\"", 
            "\"Which means I did give you a certianty, which means I didnt dodge\"" 
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("sFb", new Node("sFb", L(
                "\"Very well,\" she says, leaning forward.",
                "\"Story time.\"",
                "There was once a fisherman who refused to fish.",
                "Instead he collected buckets.",
                "He said the ocean might run out someday.",
                "So he saved empty containers for later.",
                "People laughed at the fisherman.",
                "The fisherman laughed at the people.",
                "One day a bird landed on his head.",
                "The bird refused to leave.",
                "They became business partners.",
                "Their business was predicting rain incorrectly.",
                "They were wrong often.",
                "Customers appreciated the confidence anyway.",
                "A traveling baker joined them.",
                "The baker baked square bread.",
                "Nobody knew why.",
                "The fisherman said it stacked better.",
                "The bird disagreed loudly.",
                "Arguments became tradition.",
                "Tradition became tourism.",
                "Tourism became taxes.",
                "Nobody liked the taxes.",
                "The mayor was a chair.",
                "The chair was elected fairly.",
                "It never spoke.",
                "Citizens appreciated the silence.",
                "One winter lasted three Thursdays.",
                "No one agreed which Thursdays.",
                "A dog learned to count.",
                "It stopped at seven every time.",
                "Seven became suspicious.",
                "Suspicion became fashion.",
                "People wore uncertainty like scarves.",
                "The baker invented circular soup.",
                "Bowls became confused.",
                "The fisherman retired from not fishing.",
                "The bird opened a bank account.",
                "Interest accumulated emotionally.",
                "A mountain moved two inches left.",
                "Nobody documented it.",
                "Documentation became a hobby.",
                "Hobbies became professions.",
                "Professions became hats.",
                "Everyone owned too many hats.",
                "The chair mayor approved a hat tax.",
                "The dog counted the votes.",
                "Seven again.",
                "Always seven.",
                "\"And that,\" she says,",
                "\"is why stories are unreliable.\""
        ), List.of(
                new Option("a", "Ask what that meant.", "sFc"),
                new Option("c", "Leave.", null)
        )));

        m.put("sFc", new Node("sFc", L(
        "She smiles slightly.",
            "\"Poor people have me.\"",
            "\"Rich people need me.\"",
            "\"If you eat me, you'll die.\"",
            "\"What am I?\""
        ), List.of(
            new Option("a", "nothing?", "sFcA"),
            new Option("c", "Leave.", null)
        )));

        m.put("sFc", new Node("sFcA", L(
        "She smiles slightly."
        ), List.of(
            new Option("c", "Leave.", null)
        )));


        m.put("yM", new Node("yM", L(
        "\"Your Mom\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        return m;
    }

    private static Map<String, Node> armoryNodes() {
        Map<String, Node> m = new LinkedHashMap<>();

        m.put("a0", new Node("a0", L(
                "She stands like the room owes her a salute.",
                "\"Report. State your purpose.\""
        ), List.of(
                new Option("a", "Play along: \"I seek arms and glory.\"", "aA"),
                new Option("b", "Challenge: \"You're not a general.\"", "aB"),
                new Option("c", "Leave.", null)
        )));

        m.put("a1", new Node("a1", L(
                "She barely looks up from the weapons.",
                "\"Touch carefully. Break nothing.\""
        ), List.of(
                new Option("a", "Ask what she recommends.", "aC"),
                new Option("b", "Ask why she acts like a commander.", "aB"),
                new Option("c", "Leave.", null)
        )));

        m.put("a2", new Node("a2", L(
                "\"You can shut up now\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("aA", new Node("aA", L(
                "\"Glory is optional. Arms are not.\"",
                "\"Pride kills faster than steel.\""
        ), List.of(
                new Option("a", "Ask how to avoid dying stupidly.", "aD"),
                new Option("b", "Ask what she respects.", "aH"),
                new Option("c", "Leave.", null)
        )));

        m.put("aB", new Node("aB", L(
                "\"The act is useful.\"",
                "\"When people are scared, they borrow courage from costumes.\""
        ), List.of(
                new Option("a", "Admit she's scared.", "aF"),
                new Option("b", "Say costumes are cringe.", "aG"),
                new Option("c", "Leave.", null)
        )));

        m.put("aC", new Node("aC", L(
                "\"Reliable weapon. Spare plan. Humility to retreat.\"",
                "\"If you can't retreat, you're not brave—just trapped.\""
        ), List.of(
                new Option("a", "Ask how to build a spare plan.", null),
                new Option("b", "Ask what she thinks of the casino.", "aE"),
                new Option("c", "Leave.", null)
        )));

        m.put("aD", new Node("aD", L(
                "\"Discipline is choosing the boring right thing.\"",
                "\"You look like you're good at exciting.\""
        ), List.of(
                new Option("a", "Ask for one boring rule.", null),
                new Option("b", "Ask what she's bad at.", null),
                new Option("c", "Leave.", null)
        )));

        m.put("aE", new Node("aE", L(
                "\"I do question the sanity of those who spend all their riches there- just to lose\""
        ), List.of(
                new Option("a", "Say fair.", null),
                new Option("b", "Say that's harsh.", null),
                new Option("c", "Leave.", null)
        )));

        m.put("aF", new Node("aF", L(
                "She suddenly drops the french accent",
                "\"Are you going to buy something, cause the door is right there otherwise.\""

        ), List.of(
                new Option("a", "Yes you will", null),
                new Option("b", "Laugh at her", null),
                new Option("c", "Leave.", null)
        )));

        m.put("aG", new Node("aG", L(
                "\"Fuck you\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("aH", new Node("aH", L(
                "\"The tenacity of goblins is to be admired. EPsically the psyhicic ones. Like Mind Goblins\""
        ), List.of(
                new Option("a", "Mind Goblins?", "aHa"),
                new Option("b", "Leave.", null)
        )));

        m.put("aHa", new Node("aHa", L(
                "\"Mind goblin deez nuts\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        return m;
    }

    private static Map<String, Node> bankNodes() {
        Map<String, Node> m = new LinkedHashMap<>();

        m.put("b0", new Node("b0", L(
                "She looks up without changing expression.",
                "\"State your business. Or state your exit.\""
        ), List.of(
                new Option("a", "Ask about loans.", "bA"),
                new Option("b", "Try small talk.", "bB"),
                new Option("c", "Leave.", null)
        )));

        m.put("b1", new Node("b1", L(
                "\"Yes, I'm bored,\" she says.",
                "\"No, it doesn't change policy.\""
        ), List.of(
                new Option("a", "Ask why she's like this.", "bC"),
                new Option("b", "Ask if she's the same as the others.", "bD"),
                new Option("c", "Leave.", null)
        )));

        m.put("b2", new Node("b2", L(
                "\"If you don't have numbers, you don't have a reason to be here.\"",
                "\"Proceed.\""
        ), List.of(
                new Option("a", "Ask her to explain things simply.", "bA"),
                new Option("b", "Ask what she does for fun.", "bC"),
                new Option("c", "Leave.", null)
        )));

        m.put("bA", new Node("bA", L(
                "\"You borrow. You owe. You repay in full.\""
        ), List.of(
                new Option("a", "Ask what happens if you don't repay.", "bE"),
                new Option("b", "Ask if she ever bends rules.", "bF"),
                new Option("c", "Leave.", null)
        )));

        m.put("bB", new Node("bB", L(
                "\"No,\" she says.",
                "\"If you need warmth, the casino sells it.\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("bC", new Node("bC", L(
                "\"It's called work.\""
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("bD", new Node("bD", L(
                "\"Similarity is not proof,\" she says.",
                "\"If you're accusing me, present evidence.\""
        ), List.of(
                new Option("a", "Say you saw the other shop keeps.", "bDa"),
                new Option("b", "Leave.", null)
        )));

        m.put("bE", new Node("bE", L(
                "\"Then you will have a problem,\" she says."
        ), List.of(
                new Option("a", "Leave.", null)
        )));

        m.put("bF", new Node("bF", L(
                "\"No,\" she says."
        ), List.of(
                new Option("a", "Leave.", null)
        )));

         m.put("bDa", new Node("bDa", L(
            "\"And?\""
        ), List.of(
                new Option("a", "\"And that means you are the same person...\"", "bDaA"),
                new Option("b", "\"Are you implying you're not the same person...\"", "bDaB"),
                new Option("c", "Keep pressing her to reveal the truth", "bDaC"),
                new Option("d", "Leave.", null)
        )));

        m.put("bDaA", new Node("bDaA", L(
            "\"wow... Have you ever heard of twins?\""
        ), List.of(
                new Option("a", "Sit there in embarrassment", "bDaC"),
                new Option("b", "Give her a side eye", "bDaC"),
                new Option("c", "Leave.", null)
        )));

        m.put("bDaB", new Node("bDaB", L(
            "\"wow... Have you ever heard of \'Cost of Living\', asshole\""
        ), List.of(
                new Option("a", "Sit there in embarrassment", "bDaC"),
                new Option("b", "Stare at her gold watch with a rasied eyebrow", "bDaC"),
                new Option("c", "Leave.", null)
        )));

        m.put("bDaC", new Node("bDaC", L(
            
            "She finally looks you in the eyes",
            "\"If you're not going to get or pay off a loan, leave. now. \""
        ), List.of(
                new Option("a", "Leave.", null)
        )));
        

        return m;
    }

    private static Map<String, Node> hospitalNodes() {
    Map<String, Node> m = new LinkedHashMap<>();

    m.put("h0", new Node("h0", L(
            "She glides over in a crisp nurse outfit, hands folded like prayer.",
            "\"Peace be with you,\" she purrs.",
            "\"Now tell me. Are you here to be healed… or to be ruined?\""
    ), List.of(
            new Option("a", "Ask for healing.", "hA"),
            new Option("b", "Ask why she sounds like that.", "hB"),
            new Option("c", "Ask about the 'same face' around town.", "hC"),
            new Option("d", "Leave.", null)
    )));

    m.put("h1", new Node("h1", L(
            "She checks a clipboard that is definitely blank.",
            "\"I can triage a sword wound, a bruised ego, and a terrible decision.\"",
            "\"Which one are you bleeding from today?\""
    ), List.of(
            new Option("a", "Sword wound.", "hD"),
            new Option("b", "Ego.", "hE"),
            new Option("c", "Terrible decision.", "hF"),
            new Option("d", "Leave.", null)
    )));

    m.put("h2", new Node("h2", L(
            "She taps your chest with two fingers like she's checking a fruit for ripeness.",
            "\"Still breathing. Disappointing.\"",
            "\"Kidding. Mostly.\""
    ), List.of(
            new Option("a", "Ask what the hospital actually is.", "hG"),
            new Option("b", "Flirt back: \"Try harder.\"", "hH"),
            new Option("c", "Ask about payment.", "hI"),
            new Option("d", "Leave.", null)
    )));

    // Healing / services
    m.put("hA", new Node("hA", L(
            "\"Blessed be the insured,\" she says.",
            "\"Twenty gold for absolution and stitches.\"",
            "\"No refunds for emotional damage.\""
    ), List.of(
            new Option("a", "Ask what you get for 20 gold.", "hI"),
            new Option("b", "Ask if she enjoys charging people.", "hJ"),
            new Option("c", "Back away slowly.", null)
    )));

    m.put("hI", new Node("hI", L(
            "\"Full restoration.\"",
            "\"I put you back the way you were before you met this village.\"",
            "\"Well—almost.\""
    ), List.of(
            new Option("a", "Ask what she means by 'almost'.", "hK"),
            new Option("b", "Ask if she can heal debt, too.", "hL"),
            new Option("c", "Leave.", null)
    )));

    m.put("hK", new Node("hK", L(
            "She smiles sweetly.",
            "\"I can't cure personality.\"",
            "\"But I can make you healthy enough to keep making mistakes.\""
    ), List.of(
            new Option("a", "That sounds like a scam.", "hJ"),
            new Option("b", "Respect the honesty.", null),
            new Option("c", "Leave.", null)
    )));

    m.put("hJ", new Node("hJ", L(
            "\"Charging is the holiest act I know.\"",
            "\"It teaches gratitude.\"",
            "\"And desperation.\""
    ), List.of(
            new Option("a", "Ask if she believes in anything.", "hM"),
            new Option("b", "Tell her she's awful.", "hN"),
            new Option("c", "Leave.", null)
    )));

    m.put("hL", new Node("hL", L(
            "\"Debt is a chronic condition.\"",
            "\"The bank will happily keep you sick.\"",
            "\"The casino will congratulate you for it.\""
    ), List.of(
            new Option("a", "Ask what she thinks of the casino.", "hO"),
            new Option("b", "Ask what she thinks of the bank.", "hP"),
            new Option("c", "Leave.", null)
    )));

    m.put("hO", new Node("hO", L(
            "\"The casino is a confessional with better lighting.\"",
            "\"People walk in guilty and walk out convinced it was destiny.\""
    ), List.of(
            new Option("a", "Ask if she ever gambles.", "hQ"),
            new Option("b", "Say that's bleak.", null),
            new Option("c", "Leave.", null)
    )));

    m.put("hP", new Node("hP", L(
            "\"The bank sells patience.\"",
            "\"You pay interest to borrow time.\"",
            "\"The funniest part is you spend it running in circles.\""
    ), List.of(
            new Option("a", "Ask if she's mocking you.", "hR"),
            new Option("b", "Ask if she wants you to escape.", "hS"),
            new Option("c", "Leave.", null)
    )));

    m.put("hB", new Node("hB", L(
            "She places a hand over her heart like a saint in a painting.",
            "\"I took an oath,\" she says softly.",
            "\"Not to God. To entertainment.\""
    ), List.of(
            new Option("a", "Ask what the oath requires.", "hT"),
            new Option("b", "Ask if she's flirting with you.", "hH"),
            new Option("c", "Leave.", null)
    )));

    m.put("hT", new Node("hT", L(
            "\"I heal what can be healed.\"",
            "\"I charge what can be charged.\"",
            "\"And I keep you curious.\""
    ), List.of(
            new Option("a", "Ask why she wants you curious.", "hS"),
            new Option("b", "Ask if she ever feels bad.", "hU"),
            new Option("c", "Leave.", null)
    )));

    m.put("hU", new Node("hU", L(
            "She pauses like she's considering it honestly.",
            "\"Sometimes.\"",
            "\"Then I remember you were going to walk into the forest anyway.\""
    ), List.of(
            new Option("a", "Fair point.", null),
            new Option("b", "Ask if she watches you.", "hV"),
            new Option("c", "Leave.", null)
    )));

    m.put("hV", new Node("hV", L(
            "\"Sweetheart,\" she says, adjusting her gloves,",
            "\"everyone in this village watches you.\"",
            "\"The difference is I'm the only one who patches you up after.\""
    ), List.of(
            new Option("a", "Ask why the village wants you alive.", "hS"),
            new Option("b", "Ask who 'everyone' is.", "hW"),
            new Option("c", "Leave.", null)
    )));

    m.put("hH", new Node("hH", L(
            "Her smile widens, sharp around the edges.",
            "\"Careful. I charge for pain relief.\"",
            "\"I charge extra for pain.\""
    ), List.of(
            new Option("a", "Ask what 'extra' costs.", "hX"),
            new Option("b", "Tell her she's insane.", "hN"),
            new Option("c", "Leave.", null)
    )));

    m.put("hX", new Node("hX", L(
            "\"Depends,\" she says.",
            "\"Do you want stitches or attention?\""
    ), List.of(
            new Option("a", "Stitches.", "hI"),
            new Option("b", "Attention.", "hY"),
            new Option("c", "Leave.", null)
    )));

    m.put("hY", new Node("hY", L(
            "She leans in, voice low.",
            "\"Then stop trying to die.\"",
            "\"You're making my job repetitive.\""
    ), List.of(
            new Option("a", "Ask if she gets bored.", "hQ"),
            new Option("b", "Promise nothing.", null),
            new Option("c", "Leave.", null)
    )));

    // “Same person” denial path
    m.put("hC", new Node("hC", L(
            "\"Same face?\" she repeats, sweet as sugar.",
            "\"That's a symptom. I can treat it for 20 gold.\""
    ), List.of(
            new Option("a", "Press: \"You work every building.\"", "hCa"),
            new Option("b", "Ask if she's related to them.", "hCb"),
            new Option("c", "Drop it.", null),
            new Option("d", "Leave.", null)
    )));

    m.put("hCa", new Node("hCa", L(
            "She makes a little sign of blessing over you.",
            "\"Delirium.\"",
            "\"Common in patients with low blood and high imagination.\""
    ), List.of(
            new Option("a", "Call her bluff.", "hCc"),
            new Option("b", "Ask for the truth.", "hW"),
            new Option("c", "Leave.", null)
    )));

    m.put("hCb", new Node("hCb", L(
            "\"Of course I'm related to them,\" she says.",
            "\"Women can have cheekbones in the same family.\"",
            "\"Try reading a book that isn't a wanted poster.\""
    ), List.of(
            new Option("a", "Ask how big her family is.", "hW"),
            new Option("b", "Apologize (barely).", null),
            new Option("c", "Leave.", null)
    )));

    m.put("hCc", new Node("hCc", L(
            "Her smile stays. Her eyes go flat.",
            "\"If you're here to accuse me,\" she says,",
            "\"do it quickly. I have patients who actually pay.\""
    ), List.of(
            new Option("a", "Back off.", null),
            new Option("b", "One last push: \"Who are you really?\"", "hW"),
            new Option("c", "Leave.", null)
    )));

    m.put("hW", new Node("hW", L(
            "She steps closer, voice almost gentle.",
            "\"I'm the one who keeps you alive.\"",
            "\"Everything else is a story you tell yourself.\""
    ), List.of(
            new Option("a", "Ask if you can ever leave.", "hS"),
            new Option("b", "Ask if she wants you to win.", "hS"),
            new Option("c", "Leave.", null)
    )));

    m.put("hD", new Node("hD", L(
            "\"Classic.\"",
            "\"Try keeping your insides on the inside.\""
    ), List.of(
            new Option("a", "Ask for advice.", "hT"),
            new Option("b", "Leave.", null)
    )));

    m.put("hE", new Node("hE", L(
            "\"Oh, honey.\"",
            "\"I can't stitch pride. I can only cauterize it.\""
    ), List.of(
            new Option("a", "Ask what that looks like.", "hN"),
            new Option("b", "Leave.", null)
    )));

    m.put("hF", new Node("hF", L(
            "\"Finally, honesty.\"",
            "\"Most people call it fate to feel better.\""
    ), List.of(
            new Option("a", "Ask if she believes in fate.", "hM"),
            new Option("b", "Leave.", null)
    )));

    m.put("hG", new Node("hG", L(
            "\"This is where consequences come to be cleaned up.\"",
            "\"Like a kitchen.\"",
            "\"Or a crime scene.\""
    ), List.of(
            new Option("a", "Ask which one she prefers.", "hQ"),
            new Option("b", "Leave.", null)
    )));

    m.put("hM", new Node("hM", L(
            "\"I believe in patterns.\"",
            "\"And you keep repeating yours.\""
    ), List.of(
            new Option("a", "Ask what pattern she sees.", "hS"),
            new Option("b", "Tell her to stop psychoanalyzing you.", "hN"),
            new Option("c", "Leave.", null)
    )));

    m.put("hN", new Node("hN", L(
            "\"Language,\" she scolds, not sounding scolding at all.",
            "\"This is a hospital.\"",
            "\"Sin elsewhere. Or pay me to pretend you didn't.\""
    ), List.of(
            new Option("a", "Ask if she's always like this.", "hQ"),
            new Option("b", "Leave.", null)
    )));

    m.put("hQ", new Node("hQ", L(
            "\"Boredom is a disease,\" she says.",
            "\"I treat it by getting under people's skin.\""
    ), List.of(
            new Option("a", "Ask if she's bored right now.", "hY"),
            new Option("b", "Ask what scares her.", "hR"),
            new Option("c", "Leave.", null)
    )));

    m.put("hR", new Node("hR", L(
            "\"Wasting effort,\" she admits.",
            "\"Patching someone up just so they stop trying.\"",
            "\"Don't do that.\""
    ), List.of(
            new Option("a", "Ask why she cares.", "hS"),
            new Option("b", "Leave.", null)
    )));

    m.put("hS", new Node("hS", L(
            "She watches you the way the casino watches a gambler.",
            "\"Because you're not finished yet.\"",
            "\"And neither am I.\""
    ), List.of(
            new Option("a", "Ask what 'finished' means.", null),
            new Option("b", "Leave.", null)
    )));

    return m;
}

}
