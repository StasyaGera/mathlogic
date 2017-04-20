import ParseTree.Node;
import ParseTree.Parser;
import ParseTree.Token;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by penguinni on 22.10.16.
 * penguinni hopes it will work.
 */
public class Main {
    static Path helpDir = Paths.get("help");
    static List<Node>
            axioms = new ArrayList<>(),
            axiomSchemes = new ArrayList<>(),
            hypothesis = new ArrayList<>();
    static Node alpha = null, beta = null;
    static Set<Node> restrictedVars = Collections.emptySet();

    static Parser parser = new Parser();
    static Checker checker = new Checker();
    static ReBuilder reBuilder;

    public static void main (String[] args) {
        if (args.length != 2) {
            System.err.println("Expected 2 arguments: input and output file names, found " + args.length);
            return;
        }

        new Main().run(args[0], args[1]);
    }

    private void run(String inputFile, String outputFile) {
        try (BufferedReader axiomSchemesFile = new BufferedReader(new FileReader(helpDir.resolve("axiom_schemes.txt").toString()));
             BufferedReader axiomsFile = new BufferedReader(new FileReader(helpDir.resolve("axioms.txt").toString()))) {
            String next;
            while ((next = axiomSchemesFile.readLine()) != null) {
                axiomSchemes.add(parser.parse(next));
            }
            while ((next = axiomsFile.readLine()) != null) {
                axioms.add(parser.parse(next));
            }
        } catch (FileNotFoundException e) {
            System.err.println(helpDir.resolve("axiom_schemes.txt").normalize().toString());
            System.err.println("Some files missing, download or create them: ");
        } catch (IOException e) {
            System.err.println("I/O error occurred: " + e.getMessage());
        }

        try (BufferedReader input = new BufferedReader(new FileReader(inputFile));
             PrintWriter output = new PrintWriter(new File(outputFile))) {
            String[] splitInput = input.readLine().split(",|(\\|-)");
            List<Node> parsedInput = new ArrayList<>();
            for (int i = 0; i < splitInput.length; i++) {
                if (splitInput[i].equals("")) {
                    continue;
                }
                String temp = splitInput[i];
                while (i < splitInput.length) {
                    try {
                        parsedInput.add(parser.parse(temp));
                        break;
                    } catch (Exception e) {
                        temp = temp.concat(", " + splitInput[++i]);
                    }
                }
            }
            if (!parsedInput.isEmpty()) {
                beta = parsedInput.get(parsedInput.size() - 1);
            } else {
                System.err.println("Check your input file, nothing to prove");
                return;
            }
            if (parsedInput.size() > 1) {
                hypothesis = parsedInput.subList(0, parsedInput.size() - 1);
                alpha = hypothesis.get(hypothesis.size() - 1);
            }
            reBuilder = new ReBuilder(output, alpha);

            for (int i = 0; i < hypothesis.size() - 1; i++) {
                output.print(hypothesis.get(i));
                if (i < hypothesis.size() - 2) {
                    output.print(",");
                }
                output.print(" ");
            }
            output.print("|- ");
            if (alpha != null) {
                output.print(" " + alpha + " " + Token.IMPL + " ");
                restrictedVars = checker.getFreeVars(alpha);
            }
            output.println(beta);

            String nextLine;
            while ((nextLine = input.readLine()) != null) {
                Node nextNode = parser.parse(nextLine);

                try {
                    Checker.TypedExpr expr = checker.check(nextNode);
                    if (!hypothesis.isEmpty()) {
                        expr.type.rebuild(expr.nodes);
                        output.print(alpha + " " + Token.IMPL + " ");
                    }
                    output.println(nextNode);
                } catch (ProofException e) {
                    output.close();
                    try (PrintWriter err = new PrintWriter(new File(outputFile))) {
                        err.println(e.getMessage());
                    }
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File missing: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("An error occurred: " + e.getMessage());
        }
    }
}