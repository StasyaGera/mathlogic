import ParseTree.Node;
import ParseTree.Token;

import java.util.*;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by penguinni on 22.10.16.
 * penguinni hopes it will work.
 */
class Checker {
    private int counter = 0;
    private Set<Node> proved = new HashSet<>();
    private Map<Node, List<Node>> implied = new HashMap<>();
    private Map<String, Node> replaced = new HashMap<>();

    private void update(Node statement) {
        if (statement.token().equals(Token.IMPL)) {
            List<Node> newLL = new LinkedList<>(),
                    oldLL = implied.putIfAbsent(statement.child(1), newLL);
            if (oldLL == null) oldLL = newLL;
            oldLL.add(statement.child(0));
        }
        proved.add(statement);
    }

    private boolean checkReplaced(Node key, Node expression) {
        expression = expression.nude(key.getApostrophes());
        Node node = replaced.putIfAbsent(key.nude().toString(), expression);
        return (node == null) || expression.equals(node);
    }

    private boolean isFree(Node var, Node expression) {
        Deque<Node> tree = new ArrayDeque<>();
        tree.add(expression);
        while (!tree.isEmpty()) {
            Node curr = tree.poll();
            if (curr.equals(var)) {
                return true;
            }
            if (!Token.isQuantifier(curr.token()) || !curr.var().equals(var)) {
                curr.mapChildren(tree::add);
            }
        }
        return false;
    }

    Set<Node> getFreeVars(Node expression) {
        Set<Node> freeVars = new HashSet<>();
        Queue<Node> tree = new ArrayDeque<>();
        tree.add(expression);
        while (!tree.isEmpty()) {
            Node curr = tree.poll();
            if (curr.fertility() == 0 && Token.isObject(curr.token()) && isFree(curr, expression)) {
                freeVars.add(curr);
            } else {
                curr.mapChildren(tree::add);
            }
        }

        return freeVars;
    }

    private boolean freeToSubst(Set<Node> freeInTerm, Node x, Node expression) {
        if (!isFree(x, expression)) {
            return true;
        }

        if (Token.isQuantifier(expression.token()) &&
                freeInTerm.contains(expression.var())) {
            return false;
        }

        for (int i = 0; i < expression.fertility(); i++) {
            if (!freeToSubst(freeInTerm, x, expression.child(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkAxiom(Node expression, Node axiom) {
        if (axiom.fertility() == 0 && Token.isObject(axiom.token()) &&
                expression.fertility() == 0 && Token.isObject(expression.token())) {
            return checkReplaced(axiom, expression);
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

    private boolean checkAxiomScheme(Node expression, Node axiomScheme) throws ProofException {
        if (axiomScheme.fertility() == 0 && Token.isObject(axiomScheme.token())) {
            return checkReplaced(axiomScheme, expression);
        }

        if (!axiomScheme.token().equals(expression.token())) {
            return false;
        }

        if (Token.isQuantifier(axiomScheme.token())) {
            if (!checkReplaced(axiomScheme.var(), expression.var())) {
                return false;
            }
        }

        for (int i = 0; i < axiomScheme.fertility(); i++) {
            if (!checkAxiomScheme(expression.child(i), axiomScheme.child(i))) {
                return false;
            }
        }

        if (axiomScheme.equals(Main.axiomSchemes.get(10))) {
            return checkQuantifiedAxioms(10, expression);
        } else if (axiomScheme.equals(Main.axiomSchemes.get(11))) {
            return checkQuantifiedAxioms(11, expression);
        } else if (axiomScheme.equals(Main.axiomSchemes.get(12))) {
            return checkArihmeticsAxiom(expression);
        }

        return true;
    }

    private boolean checkQuantifiedAxioms(int axiomNo, Node expression) throws ProofException {
        int i, j;
        if (axiomNo == 10) {
            i = 0;
            j = 1;
        } else if (axiomNo == 11) {
            i = 1;
            j = 0;
        } else {
            return true;
        }
        Node axiomScheme = Main.axiomSchemes.get(axiomNo);

        Node x = replaced.get(axiomScheme.child(i).var().rootTokenToString()),
                psi = expression.child(j),
                phi = expression.child(i).child(0);

        replaced.clear();
        if (!checkAxiomScheme(psi, phi)) {
            return false;
        }

        Node theta = replaced.get(x.rootTokenToString());
        if (theta == null || freeToSubst(getFreeVars(theta), x, phi)) {
            return true;
        }

        throw new ProofException(counter, "терм " + theta.toString() +
                " не свободен для подстановки в формулу " + phi.toString() +
                " вместо переменной " + x.toString());
    }

    private boolean checkArihmeticsAxiom(Node expression) {
        Node x = expression.child(0).child(1).var(),
                phi = expression.child(0).child(1).child(0).child(0),
                phi0 = expression.child(0).child(0),
                phi1 = expression.child(0).child(1).child(0).child(1);
        String x_str = x.toString();

        try {
            replaced.clear();
            if (checkAxiomScheme(phi0, phi)) {
                if (!replaced.get(x_str).equals(new Node(Token.CONST))) {
                    return false;
                }
            } else {
                return false;
            }

            replaced.clear();
            if (checkAxiomScheme(phi1, phi)) {
                x.addApostrophes(1);
                if (!replaced.get(x_str).equals(x)) {
                    return false;
                }
                x.removeApostrophes(1);
            } else {
                return false;
            }
        } catch (ProofException e) {
            return false;
        }

        return true;
    }

    private boolean checkHypothesis(Node expression, Node hypothesis) {
        return expression.equals(hypothesis);
    }

    private TypedExpr checkMP(Node statement) throws ProofException {
        if (implied.containsKey(statement)) {
            for (Node expression : implied.get(statement)) {
                if (proved.contains(expression)) {
                    update(statement);
                    return new TypedExpr(Type.MP, statement, expression);
                }
            }
        }

        return null;
    }

    private TypedExpr checkRule(Type rule, Node statement) throws ProofException {
        if (!statement.token().equals(Token.IMPL)) {
            return null;
        }
        boolean gate1;
        switch (rule) {
            case RuleForAll:
                gate1 = statement.child(1).token().equals(Token.FORALL);
                break;
            case RuleExists:
                gate1 = statement.child(0).token().equals(Token.EXISTS);
                break;
            default:
                gate1 = false;
        }

        if (gate1) {
            Node key = null, phi, psi, x = null;
            Predicate<Node> gate3 = null;
            switch (rule) {
                case RuleForAll:
                    phi = statement.child(0);
                    psi = statement.child(1).child(0);
                    key = psi;
                    x = statement.child(1).var();
                    gate3 = (expr) -> expr.equals(phi);
                    break;
                case RuleExists:
                    phi = statement.child(1);
                    psi = statement.child(0).child(0);
                    key = phi;
                    x = statement.child(0).var();
                    gate3 = (expr) -> expr.equals(psi);
                    break;
                default:
                    phi = null;
            }

            if (implied.containsKey(key)) {
                for (Node expression : implied.get(key)) {
                    if (gate3.test(expression)) {
                        if (isFree(x, phi)) {
                            throw new ProofException(counter, "переменная " + x + " входит свободно в формулу " + phi);
                        }
                        if (Main.restrictedVars.contains(x)) {
                            throw new ProofException(counter, "используется правило с квантором по переменной " + x +
                                    ", входящей свободно в допущение " + Main.alpha);
                        }

                        update(statement);
                        return new TypedExpr(rule, new Node(Token.IMPL, expression, key), x);
                    }
                }
            }
        }

        return null;
    }

    TypedExpr check(Node statement) throws ProofException {
        this.counter++;

        for (int i = 0; i < Main.hypothesis.size(); i++) {
            if (checkHypothesis(statement, Main.hypothesis.get(i))) {
                update(statement);
                if (i < Main.hypothesis.size() - 1) {
                    return new TypedExpr(Type.Hypothesis, statement);
                } else {
                    return new TypedExpr(Type.Alpha, statement);
                }
            }
        }

        for (int i = 0; i < Main.axiomSchemes.size(); i++) {
            replaced.clear();
            if (checkAxiomScheme(statement, Main.axiomSchemes.get(i))) {
                update(statement);
                return new TypedExpr(Type.Axiom, statement);
            }
        }

        for (int i = 0; i < Main.axioms.size(); i++) {
            replaced.clear();
            if (checkAxiom(statement, Main.axioms.get(i))) {
                update(statement);
                return new TypedExpr(Type.Axiom, statement);
            }
        }

        TypedExpr ruleRes;
        if ((ruleRes = checkMP(statement)) != null ||
                (ruleRes = checkRule(Type.RuleExists, statement)) != null ||
                (ruleRes = checkRule(Type.RuleForAll, statement)) != null) {
            return ruleRes;
        }

        throw new ProofException(counter);
    }

    class TypedExpr {
        TypedExpr(Type type, Node... nodes) {
            this.nodes = nodes;
            this.type = type;
        }

        Node[] nodes;
        Type type;
    }
}
