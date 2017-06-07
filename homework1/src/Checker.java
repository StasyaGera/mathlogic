import ParseTree.Node;
import ParseTree.Token;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by penguinni on 22.10.16.
 * penguinni hopes it will work.
 */

class Checker {
    private class pair {
        pair(Node node, int number) {
            this.node = node;
            this.number = number;
        }
        Node node;
        int number;
    }

    private HashMap<String, Node> replaced = new HashMap<>();
    private HashMap<Node, Integer> proved = new HashMap<>();
    private HashMap<Node, LinkedList<pair>> implied = new HashMap<>();

    int counter = 0;

    private void update(Node statement) {
        if (statement.token().equals(Token.IMPLICATION)) {
            if (!implied.containsKey(statement.child(1))) {
                implied.put(statement.child(1), new LinkedList<>());
            }
            implied.get(statement.child(1)).add(new pair(statement.child(0), this.counter));
        }

        proved.put(statement, this.counter);
    }

    private boolean checkAxiom(Node expression, Node axiom) {
        if (axiom.fertility() == 0) {
            String k = axiom.rootTokenToString();
            if (replaced.containsKey(k)) {
                return expression.equals(replaced.get(k));
            }

            replaced.put(k, expression);
            return true;
        }

        if (!axiom.token().equals(expression.token())) {
            return false;
        }

        for (int i = 0; i < axiom.fertility(); i++) {
            if (!checkAxiom(expression.child(i), axiom.child(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean checkHypothesis(Node expression, Node hypothesis) {
        return expression.equals(hypothesis);
    }

    private String checkMP(Node statement) {
        if (implied.containsKey(statement)) {
            for (pair expression : implied.get(statement)) {
                if (proved.containsKey(expression.node)) {
                    update(statement);
                    return "M.P. " + proved.get(expression.node) + ", " + expression.number;
                }
            }
        }

        return "Не доказано";
    }

    String check(Node statement) {
        this.counter++;

        for (int i = 0; i < Main.hypothesis.size(); i++) {
            if (checkHypothesis(statement, Main.hypothesis.get(i))) {
                update(statement);
                return "Предп. " + (i + 1);
            }
        }

        for (int i = 0; i < Main.axioms.size(); i++) {
            replaced.clear();
            if (checkAxiom(statement, Main.axioms.get(i))) {
                update(statement);
                return "Сх. акс. " + (i + 1);
            }
        }

        return checkMP(statement);
    }

}
