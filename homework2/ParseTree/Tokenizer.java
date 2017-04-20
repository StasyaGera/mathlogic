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
        index = 0;
        checkState();
        skipWS();
    }

    private void checkState() {
        hasTokens = (index < input.length());
    }

    private void skipWS() {
        while (hasTokens && isWhitespace(input.charAt(index))) {
            index++;
            checkState();
        }
    }

    private boolean isSymbol(char symbol) {
        for (Token token : Token.SYMBOLIC) {
            if (token.toString().contains(Character.toString(symbol))) {
                return true;
            }
        }

        return false;
    }

    private Token lexemByName(String name) {
        if (isDigit(name.charAt(0)) || isLetter(name.charAt(0))) {
            if (isUpperCase(name.charAt(0))) {
                return new Token(name, 4);
            }

            if (isLetter(name.charAt(0)) && peek() == Token.O_PAREN) {
                return new Token(name, 7);
            }

            return new Token(name, 7);
        }

        for (Token lexem : Token.VOCAB) {
            if (name.equals(lexem.toString())) {
                return lexem;
            }
        }

        return null;
    }

    private Token peek() {
        int temp = index;
        Token next = nextToken();
        index = temp;
        checkState();
        return next;
    }

    Token nextToken() {
        skipWS();

        if (!hasTokens)
            return Token.END;

        StringBuilder word = new StringBuilder();

        //try to read variable, predicate or functor names
        while (hasTokens &&
                isLetter(input.charAt(index)) && isLowerCase(input.charAt(index))) {
            word.append(input.charAt(index++));
            checkState();
        }
        if (word.toString().isEmpty()) {
            while (hasTokens &&
                    isLetter(input.charAt(index)) && isUpperCase(input.charAt(index))) {
                word.append(input.charAt(index++));
                checkState();
            }
        }
        if (!word.toString().isEmpty()) {
            while (hasTokens && isDigit(input.charAt(index))) {
                word.append(input.charAt(index++));
                checkState();
            }

            return lexemByName(word.toString());
        }

        //read symbols
        if (hasTokens && isSymbol(input.charAt(index))) {
            word.append(input.charAt(index++));
            checkState();
        }
        if (!word.toString().isEmpty())
            return lexemByName(word.toString());

        //read implication separately because it has two symbols in it
        if (hasTokens && (input.charAt(index) == '-')) {
            word.append(input.charAt(index++));
            checkState();
            if (hasTokens && (input.charAt(index) == '>')) {
                word.append(input.charAt(index++));
                checkState();
            } else {
                //because there is no token with key "-"
                return null;
            }
        }
        if (!word.toString().isEmpty()) {
            return lexemByName(word.toString());
        }

        return null;
    }
}
