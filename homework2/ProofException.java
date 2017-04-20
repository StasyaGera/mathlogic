/**
 * Created by penguinni on 22.03.17.
 */
class ProofException extends Exception {
    private ProofException(String info, int n) {
        super("Вывод некорректен начиная с формулы номер " + n + info + ".");
    }

    ProofException(int n) {
        this("", n);
    }

    ProofException(int n, String cause) {
        this(": " + cause, n);
    }
}
