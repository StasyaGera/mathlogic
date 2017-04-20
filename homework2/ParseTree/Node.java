package ParseTree;

import java.util.function.Function;

/**
 * Created by penguinni on 22.10.16.
 * penguinni hopes it will work.
 */
public class Node {
    private Token token;
    private Node var = null;
    private Node[] children;
    private int fertility, hcache = 0, apostrophe = 0;

    public Node(Token token, Node... children) {
        this.token = token;
        if (Token.isQuantifier(token)) {
            this.var = children[0];
            this.children = new Node[children.length - 1];
            System.arraycopy(children, 1, this.children, 0, children.length - 1);
        } else {
            this.children = children;
        }
        this.fertility = this.children.length;
    }

    public void addApostrophes(int a) {
        apostrophe += a;
    }

    public Token token() {
        return this.token;
    }

    public int fertility() {
        return this.fertility;
    }

    public Node var() {
        return var;
    }

    public Node child(int i) {
        return this.children[i];
    }

    public void mapChildren(Function<Node, ?> fun) {
        for (Node child : children) {
            fun.apply(child);
        }
    }

    private void doSubst(int depth, SubstPair... args) {
        for (int i = 0; i < fertility; i++) {
            boolean done = false;
            for (SubstPair pair : args) {
                if (pair.depthStamp == -1 && child(i).equals(pair.where)) {
                    children[i] = pair.which;
                    done = true;
                    break;
                }
                if (Token.isQuantifier(child(i).token()) && child(i).var().equals(pair.where)) {
                    pair.depthStamp = depth;
                }
            }

            if (!done) {
                children[i].doSubst(depth + 1, args);
            }

            for (SubstPair np : args) {
                if (np.depthStamp == depth) {
                    np.depthStamp = -1;
                }
            }
        }
    }

    public Node subst(SubstPair... args) {
        for (SubstPair pair : args) {
            if (this.equals(pair.where)) {
                return pair.which;
            }
        }

        doSubst(0, args);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        if (Token.isUnary(token) || Token.isQuantifier(token)) {
            out.append(rootTokenToString()).append(child(0));
        } else if (Token.isBinary(token)) {
            out.append("(").append(child(0)).append(" ")
                    .append(rootTokenToString())
                    .append(" ").append(child(1).toString()).append(")");
        } else {
            out.append(rootTokenToString());
            if (fertility != 0) {
                out.append("(");
                for (int i = 0; i < fertility - 1; i++) {
                    out.append(child(i));
                    out.append(", ");
                }
                out.append(child(fertility - 1));
                out.append(")");
            }
        }

        for (int i = 0; i < apostrophe; i++) {
            out.append(Token.APOSTROPHE);
        }

        return out.toString();
    }

    private boolean equals(Node other) {
        if (!this.token.equals(other.token)) {
            return false;
        }

        if (Token.isQuantifier(this.token) && (!this.var.equals(other.var))) {
            return false;
        }

        for (int i = 0; i < this.fertility; i++) {
            if (!this.children[i].equals(other.children[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Node) {
            return this.equals((Node) other);
        } else {
            return this == other;
        }
    }

    public String rootTokenToString() {
        String res = this.token.toString();
        if (Token.isQuantifier(token)) {
            res += var.rootTokenToString() + " ";
        }

        return res;
    }

    @Override
    public int hashCode() {
        if (hcache == 0) {
            int hash = token.toString().hashCode();
            for (int i = 0; i < fertility; i++) {
                hash += children[i].hashCode();
                hash -= (hash << 13) | (hash >> 19);
            }
            hcache = hash;
        }
        return  hcache;
    }

    public static class SubstPair {
        public SubstPair(Node which, Node where) {
            this.which = which;
            this.where = where;
        }

        public SubstPair(Node which, String where) {
            this.which = which;
            this.where = new Parser().parse(where);
        }

        public SubstPair(String which, Node where) {
            this.which = new Parser().parse(which);
            this.where = where;
        }

        public SubstPair(String which, String where) {
            this.which = new Parser().parse(which);
            this.where = new Parser().parse(where);
        }

        private int depthStamp = -1;
        private Node which, where;
    }
}
