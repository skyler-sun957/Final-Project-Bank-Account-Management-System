package bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores all transactions for a single account and tracks the latest balance snapshot.
 */
public class Ledger {
    private final ArrayList<Transaction> transactions;
    private MoneyValue currentBalance;

    public Ledger() {
        transactions = new ArrayList<>();
        currentBalance = MoneyValue.ZERO;
    }

    /**
     * Precondition: transaction is not null.
     * Postcondition: appends transaction to this ledger.
     */
    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public MoneyValue getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(MoneyValue currentBalance) {
        if (currentBalance == null) {
            throw new IllegalArgumentException("Ledger balance cannot be null.");
        }
        this.currentBalance = currentBalance;
    }

    /**
     * Precondition: account owns this ledger.
     * Postcondition: returns formatted lines for paged CLI output.
     */
    public ArrayList<String> formatEntries(Account account) {
        ArrayList<String> lines = new ArrayList<>();
        if (transactions.isEmpty()) {
            lines.add("No transactions yet.");
            return lines;
        }
        for (Transaction transaction : transactions) {
            lines.add(transaction.formatFor(account));
        }
        return lines;
    }
}
