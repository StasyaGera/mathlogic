package ParseTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by penguinni on 21.10.16.
 * penguinni hopes it will work.
 */
public class Parser {
    private Tokenizer tokenizer;
    private Token previous, current;

    private void getNextToken() {
        previous = current;
        current = tokenizer.nextToken();
    }

    private Node[] readTerms() {
        List<Node> terms = new ArrayList<>();

        if (current.equals(Token.O_PAREN)) {
            while (!current.equals(Token.C_PAREN)) {
                terms.add(parseLeft(Token.TERM_PRIORITY));
            }
            getNextToken();
        }

        return terms.toArray(new Node[0]);
    }

    private int readApostrophes() {
        int res = 0;
        while (current.equals(Token.APOSTROPHE)) {
            getNextToken();
            res++;
        }
        return res;
    }

    private Node parseRight() {
        Node expression = parseLeft(1);
        if (current.equals(Token.IMPL)) {
            expression = current.create(current, expression, parseRight());
        }
        return expression;
    }

    private Node parseLeft(int priority) {
        Node expression;

        if (priority < Token.MAX_PRIORITY) {
            expression = parseLeft(priority + 1);
        } else {
            getNextToken();

            if (current == Token.O_PAREN) {
                Node result = parseRight();
                if (current == Token.C_PAREN) {
                    getNextToken();
                }
                result.addApostrophes(readApostrophes());
                return result;
            }

            if (Token.isUnary(current)) {
                return current.create(current, parseLeft(Token.UNARY_PRIORITY));
            }

            if (Token.isQuantifier(current)) {
                getNextToken();
                return previous.create(previous, current.create(current), parseLeft(Token.UNARY_PRIORITY));
            }

            if (Token.isPredicate(current) || Token.isObject(current)) {
                getNextToken();
                Node result = previous.create(previous, readTerms());
                result.addApostrophes(readApostrophes());
                return result;
            }

            getNextToken();
            Node result = previous.create(previous);
            result.addApostrophes(readApostrophes());
            return result;
        }

        while (current != Token.END) {
            if (current.priority() == priority) {
                expression = current.create(current, expression, parseLeft(priority + 1));
            } else {
                return expression;
            }
        }

        return expression;
    }

    public Node parse(String input) {
        current = Token.START;
        tokenizer = new Tokenizer(input);

        return parseRight();
    }
}