package ParseTree;

import static java.lang.Character.*;

/**
 * Created by penguinni on 21.10.16.
 * penguinni hopes it will work.
 */

class Tokenizer {
    private boolean hasTokens;
    private int index;

    private String input;

    Tokenizer(String input) {
        this.input = input;
        this.index = 0;
        this.hasTokens = (index < input.length());
        skipWS();
    }

    private void checkState() {
        hasTokens = (index < input.length());
    }

    private void skipWS() {
        while ((hasTokens) && isWhitespace(input.charAt(index))) {
            index++;
            checkState();
        }
    }

    private boolean isSymbol(char symbol) {
        for (Token token : Token.SYMBOLIC) {
            if (token.key().contains(Character.toString(symbol))) {
                return true;
            }
        }
        return false;
    }

    private Token lexemByName(String name) {
        if (isDigit(name.charAt(0)) || isLetter(name.charAt(0))) {

            return new Token(name, Token.MAX_PRIORITY, (Token type, Node... children) -> new Node(type));
        }

        for (Token lexem : Token.VOCAB) {
            if (name.equals(lexem.key())) {
                return lexem;
            }
        }

        return null;
    }

    Token nextToken() {
        skipWS();

        if (!hasTokens) {
            return Token.END;
        }

        String word = "";

        while ((hasTokens) && (isDigit(input.charAt(index)) || isLetter(input.charAt(index)))) {
            word += input.charAt(index++);
            checkState();
        }
        if (!word.equals("")) {
            return new Token(word, Token.MAX_PRIORITY, (Token type, Node... children) -> new Node(type));
        }

        if ((hasTokens) && isSymbol(input.charAt(index))) {
            word += input.charAt(index++);
            checkState();
            if ((hasTokens) && (input.charAt(index) == '>')) {
                word += input.charAt(index++);
                checkState();
            }
        }
        if (!word.equals("")) {
            return lexemByName(word);
        }

        return null;
    }
}
