package ParseTree;

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

    private Node parseRight() {
        Node expression = parseLeft(1);
        if (current.equals(Token.IMPLICATION)) {
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
                return result;

            } else {
                if (Token.isUnary(current)) {
                    return current.create(current, parseLeft(Token.MAX_PRIORITY));
                } else {
                    getNextToken();
                    return previous.create(previous);
                }
            }
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
        this.current = Token.START;
        tokenizer = new Tokenizer(input);

        return parseRight();
    }
}
