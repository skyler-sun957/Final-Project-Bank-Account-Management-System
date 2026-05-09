package bank;

public class IncorrectAccountTypeException extends RuntimeException {
    public IncorrectAccountTypeException(String message) {
        super(message);
    }
}
