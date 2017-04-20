import ParseTree.Node;
import ParseTree.Parser;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by penguinni on 22.10.16.
 * penguinni hopes it will work.
 */

public class Main {
    final static String[] AXIOMS = { "A->B->A",
            "(A->B)->(A->B->C)->(A->C)",
            "A->B->A&B",
            "A&B->A",
            "A&B->B",
            "A->A|B",
            "B->A|B",
            "(A->C)->(B->C)->(A|B->C)",
            "(A->B)->(A->!B)->!A",
            "!!A->A"
    };

    static ArrayList<Node> axioms = new ArrayList<>();
    static ArrayList<Node> hypothesis = new ArrayList<>();
    private static Node statement = null;

    private static Parser parser = new Parser();
    private static Checker checker = new Checker();

    public static void main (String[] args) throws IOException {
        for (String axiom : AXIOMS) {
            axioms.add(parser.parse(axiom));
        }

        BufferedReader input = new BufferedReader(new FileReader(args[0]));

        String[] temp = input.readLine().split(",|(\\|-)");
        for (int i = 0; i < temp.length - 1; i++) {
            if (!temp[i].equals(""))
                hypothesis.add(parser.parse(temp[i]));
        }
        statement = parser.parse(temp[temp.length - 1]);

        PrintWriter output = new PrintWriter(new File(args[1]));

        String next;
        while ((next = input.readLine()) != null) {
            output.println("(" + (checker.counter + 1) + ") " + next + " (" + checker.check(parser.parse(next)) + ")");
        }

        output.close();
    }
}
