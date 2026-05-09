package bank;

public class BalanceNotZeroException extends RuntimeException {
    public BalanceNotZeroException(String message) {
        super(message);
    }
}
