package ParseTree;

import java.util.Arrays;

/**
 * Created by penguinni on 21.10.16.
 * penguinni hopes it will work.
 */

public class Token {
    interface Creator {
        Node create(Token type, Node... children);
    }

    Token(String key, int priority, Creator creator) {
        this.key = key;
        this.priority = priority;
        this.creator = creator;
    }

    final static int MAX_PRIORITY = 3;

    private String key = "";
    private int priority = 0;
    private Creator creator = null;

    String key() {
        return this.key;
    }
    int priority() {
        return this.priority;
    }
    Node create(Token type, Node... children) {
        return this.creator.create(type, children);
    }

    public boolean equals(Token other) { return this.key.equals(other.key); }

    public static final Token IMPLICATION = new Token("->", 0, (Token type, Node... children) -> new Node(type, children[0], children[1]));
    public static final Token OR = new Token("|", 1, (Token type, Node... children) -> new Node(type, children[0], children[1]));
    public static final Token AND = new Token("&", 2, (Token type, Node... children) -> new Node(type, children[0], children[1]));
    public static final Token NOT = new Token("!", 3, (Token type, Node... children) -> new Node(type, children[0]));

    public static final Token O_PAREN = new Token("(", 3, null);
    public static final Token C_PAREN = new Token(")", 3, null);

    static final Token END = new Token("", 0, null);
    static final Token START = new Token("", 0, null);

    static final Token[] VOCAB = new Token[]{IMPLICATION, OR, AND, NOT, O_PAREN, C_PAREN};
    static final Token[] SYMBOLIC = new Token[]{IMPLICATION, OR, AND, NOT, O_PAREN, C_PAREN};

    private final static Token[] BINARY = new Token[]{IMPLICATION, OR, AND};
    private final static Token[] UNARY = new Token[]{NOT};

    public static boolean isUnary(Token token) {
        return Arrays.asList(Token.UNARY).contains(token);
    }
    public static boolean isBinary(Token token) {
        return Arrays.asList(Token.BINARY).contains(token);
    }
}
