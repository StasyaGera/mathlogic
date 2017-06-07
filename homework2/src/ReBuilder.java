import ParseTree.Node;
import ParseTree.Node.SubstPair;
import ParseTree.Parser;

import java.io.*;
import java.nio.file.Paths;

/**
 * Created by penguinni on 22.03.17.
 */
class ReBuilder {
    ReBuilder(PrintWriter out, Node alpha) {
        this.out = out;
        this.alpha = alpha;
    }

    private final PrintWriter out;
    private final Node alpha;

    private void print(String helpFile, SubstPair... substitutions) {
//        try (BufferedReader help = new BufferedReader(
//                new FileReader(Main.helpDir.resolve(helpFile).toAbsolutePath().toString()))) {
        try (BufferedReader help = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(
                        Main.helpDir.resolve(helpFile).toString())))) {

                String next;
            while ((next = help.readLine()) != null) {
                out.println(Main.parser.parse(next).subst(substitutions));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Some files missing, download or create them: ");
        } catch (IOException e) {
            System.err.println("I/O error occurred: " + e.getMessage());
        }
    }

    void doAxiom(Node axiom) {
        print("axiom&hypothesis.txt", new SubstPair(alpha, "a"),
                new SubstPair(axiom, "di"));
    }

    void doHypothesis(Node hypothesis) {
        doAxiom(hypothesis);
    }
    
    void doAlpha() {
        print("alpha.txt", new SubstPair(alpha, "a"));
    }

    void doMP(Node di, Node dj) {
        print("MP.txt", new SubstPair(alpha, "a"),
                new SubstPair(di, "di"), new SubstPair(dj, "dj"));
    }

    void doRuleForAll(Node dj, Node var) {
        Node phi = dj.child(0), psi = dj.child(1);

        print("ruleForall1.txt", new SubstPair(alpha, "a"),
                new SubstPair(phi, "b"), new SubstPair(psi, "c"));

        out.println(Main.parser.parse("a & phi -> @" + var + " psi")
                .subst(new SubstPair(alpha, "a"), new SubstPair(phi, "phi"),
                        new SubstPair(psi, "psi")));

        print("ruleForall2.txt", new SubstPair(alpha, "a"), new SubstPair(phi, "b"),
                new SubstPair(Main.parser.parse("@" + var + " psi")
                        .subst(new SubstPair(psi, "psi")), "c"));
    }

    void doRuleExists(Node dj, Node var) {
        Node phi = dj.child(1), psi = dj.child(0);

        print("ruleExists.txt", new SubstPair(alpha, "a"),
                new SubstPair(psi, "b"), new SubstPair(phi, "c"));

        out.println(Main.parser.parse("?" + var + " psi -> a -> phi")
                .subst(new SubstPair(alpha, "a"), new SubstPair(phi, "phi"),
                        new SubstPair(psi, "psi")));

        print("ruleExists.txt", new SubstPair(alpha, "b"), new SubstPair(phi, "c"),
                new SubstPair(Main.parser.parse("?" + var + " psi")
                        .subst(new SubstPair(psi, "psi")), "a"));
    }
}
