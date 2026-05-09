package bank;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable transaction record.
 * Precondition: amount is positive and from/to account numbers are valid for the Bank object.
 * Postcondition: transaction data cannot be edited after construction.
 */
public class Transaction {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final int fromAcct;
    private final int toAcct;
    private final MoneyValue amount;
    private final String memo;

    public Transaction(int fromAcct, int toAcct, MoneyValue amount, String memo) {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidAmountException("Transaction amount must be positive.");
        }
        this.timestamp = LocalDateTime.now();
        this.fromAcct = fromAcct;
        this.toAcct = toAcct;
        this.amount = amount;
        this.memo = memo == null ? "" : memo.trim();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getFromAcct() {
        return fromAcct;
    }

    public int getToAcct() {
        return toAcct;
    }

    public MoneyValue getAmount() {
        return amount;
    }

    public String getMemo() {
        return memo;
    }

    public boolean isDeposit() {
        return fromAcct == Bank.BANK_CASH_ACCOUNT_NUMBER;
    }

    public boolean isWithdraw() {
        return toAcct == Bank.BANK_CASH_ACCOUNT_NUMBER;
    }

    /**
     * Precondition: perspective is an account involved in this transaction.
     * Postcondition: returns a user-friendly line for account ledger display.
     */
    public String formatFor(Account perspective) {
        String action = getActionFor(perspective);
        String counterparty;
        if (perspective.getAcctNumber() == fromAcct) {
            counterparty = "to " + CLIUtil.formatAcct(toAcct);
        } else if (perspective.getAcctNumber() == toAcct) {
            counterparty = "from " + CLIUtil.formatAcct(fromAcct);
        } else {
            counterparty = CLIUtil.formatAcct(fromAcct) + " -> " + CLIUtil.formatAcct(toAcct);
        }

        return String.format("%s | %-24s | %-22s | %12s | %s",
                timestamp.format(FORMATTER), action, counterparty, amount, memo);
    }

    /**
     * Precondition: none.
     * Postcondition: returns a user-friendly line for global transaction display.
     */
    public String formatGeneral() {
        return String.format("%s | %s -> %s | %12s | %s",
                timestamp.format(FORMATTER), CLIUtil.formatAcct(fromAcct), CLIUtil.formatAcct(toAcct), amount, memo);
    }

    private String getActionFor(Account perspective) {
        int acct = perspective.getAcctNumber();
        boolean credit = perspective.isCredit();

        if (memo != null && !memo.isEmpty()) {
            return memo;
        }
        if (acct == toAcct && fromAcct == Bank.BANK_CASH_ACCOUNT_NUMBER) {
            return credit ? "Credit draw" : "Deposit";
        }
        if (acct == fromAcct && toAcct == Bank.BANK_CASH_ACCOUNT_NUMBER) {
            return credit ? "Credit payment" : "Withdrawal";
        }
        return "Transfer";
    }
}
