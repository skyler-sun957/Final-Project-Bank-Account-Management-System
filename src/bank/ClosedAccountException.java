package bank;

public class ClosedAccountException extends RuntimeException {
    public ClosedAccountException(String message) {
        super(message);
    }
}
