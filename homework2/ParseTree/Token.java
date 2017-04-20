package ParseTree;

import java.util.Arrays;

/**
 * Created by penguinni on 21.10.16.
 * penguinni hopes it will work.
 */
public class Token {
    final static int UNARY_PRIORITY = 3;
    final static int TERM_PRIORITY = 4;
    final static int MAX_PRIORITY = 7;

    static final Token END   = new Token("", 0);
    static final Token START = new Token("", 0);

    public static final Token IMPL   = new Token("->", 0);
    public static final Token OR     = new Token("|", 1);
    public static final Token AND    = new Token("&", 2);
    public static final Token NOT    = new Token("!", 3);
    public static final Token FORALL = new Token("@", 3);
    public static final Token EXISTS = new Token("?", 3);
    public static final Token EQV    = new Token("=", 4);
    public static final Token ADD    = new Token("+", 5);
    public static final Token MUL    = new Token("*", 6);
    public static final Token CONST  = new Token("0", 7);

    public static final Token O_PAREN    = new Token("(", 7);
    public static final Token C_PAREN    = new Token(")", 7);
    public static final Token COMMA      = new Token(",", 7);
    public static final Token APOSTROPHE = new Token("'", 7);

    static final Token[] VOCAB = new Token[]{
            IMPL, OR, AND, NOT, FORALL, EXISTS, EQV, ADD, MUL, CONST, O_PAREN, C_PAREN, COMMA, APOSTROPHE
    };
    static final Token[] SYMBOLIC = new Token[]{
            OR, AND, NOT, FORALL, EXISTS, EQV, ADD, MUL, CONST, O_PAREN, C_PAREN, COMMA, APOSTROPHE
    };
    private final static Token[] QUANTIFIERS = new Token[]{FORALL, EXISTS};
    private final static Token[] BINARY = new Token[]{IMPL, OR, AND, EQV, ADD, MUL};
    private final static Token[] UNARY = new Token[]{NOT};

    private String key = "";
    private int priority = 0;

    public Token(String key, int priority) {
        this.key = key;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return key;
    }

    public int priority() {
        return priority;
    }

    public Node create(Token type, Node... children) {
        return new Node(type, children);
    }

    private boolean equals(Token other) {
        return this.key.equals(other.key);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Token) {
            return this.equals((Token) other);
        } else {
            return this == other;
        }
    }

    public static boolean isUnary(Token token) {
        return Arrays.asList(Token.UNARY).contains(token);
    }
    public static boolean isBinary(Token token) {
        return Arrays.asList(Token.BINARY).contains(token);
    }
    public static boolean isQuantifier(Token token) {
        return Arrays.asList(Token.QUANTIFIERS).contains(token);
    }
    public static boolean isPredicate(Token token) {
        return Character.isUpperCase(token.toString().charAt(0));
    }
    public static boolean isObject(Token token) {
        return Character.isLowerCase(token.toString().charAt(0));
    }
}