package ParseTree;

/**
 * Created by penguinni on 22.10.16.
 * penguinni hopes it will work.
 */

public class Node {
    Node(Token token, Node... children) {
        this.token = token;
        this.children = children;
        this.fertility = children.length;
    }

    private Token token;
    private Node[] children;
    private int fertility;
    private int hcache = 0;

    public Token token() {
        return this.token;
    }

    public int fertility() {
        return this.fertility;
    }

    public Node child(int i) {
        return this.children[i];
    }

    private boolean equals(Node other) {
        if (!this.token.equals(other.token)) {
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
        return this.token.key();
    }

    @Override
    public int hashCode() {
        if (hcache == 0) {
            int hash = token.key().hashCode();
            for (int i = 0; i < fertility; i++) {
                hash += children[i].hashCode();
                hash -= (hash << 13) | (hash >> 19);
            }
            hcache = hash;
        }
        return  hcache;
    }
}
