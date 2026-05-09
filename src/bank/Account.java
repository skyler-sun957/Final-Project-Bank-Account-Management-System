package bank;

/**
 * Abstract account base class. All account subclasses inherit number, owner, ledger, balance,
 * credit/debit type, interest settings, and closing rules.
 */
public abstract class Account {
    private final int acctNumber;
    private final Person owner;
    private final Ledger ledger;
    private MoneyValue balance;
    private final boolean credit;
    private final boolean hasInterest;
    private final double yearlyInterestRate;
    private boolean closed;

    protected Account(int acctNumber, Person owner, boolean credit, boolean hasInterest) {
        this(acctNumber, owner, credit, hasInterest, 0.0, MoneyValue.ZERO);
    }

    protected Account(int acctNumber, Person owner, boolean credit, boolean hasInterest, double yearlyInterestRate) {
        this(acctNumber, owner, credit, hasInterest, yearlyInterestRate, MoneyValue.ZERO);
    }

    protected Account(int acctNumber, Person owner, boolean credit, boolean hasInterest,
                      double yearlyInterestRate, MoneyValue startingBalance) {
        if (owner == null) {
            throw new IllegalArgumentException("Account owner cannot be null.");
        }
        if (startingBalance == null) {
            throw new IllegalArgumentException("Starting balance cannot be null.");
        }
        if (yearlyInterestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }
        this.acctNumber = acctNumber;
        this.owner = owner;
        this.credit = credit;
        this.hasInterest = hasInterest;
        this.yearlyInterestRate = yearlyInterestRate;
        this.balance = startingBalance;
        this.ledger = new Ledger();
        this.ledger.setCurrentBalance(startingBalance);
        this.closed = false;
    }

    /**
     * Transfers money from one account to another.
     * Precondition: from and to are open accounts; amount is positive; from has enough balance.
     * Postcondition: balances and ledgers are updated, or an exception is thrown before changes occur.
     */
    public static Transaction transfer(Account from, Account to, MoneyValue amount) {
        return transfer(from, to, amount, "Transfer");
    }

    /**
     * Transfers money with a memo shown in each account ledger.
     * Precondition: from and to are open accounts; amount is positive; from has enough balance.
     * Postcondition: balances and ledgers are updated, or an exception is thrown before changes occur.
     */
    public static Transaction transfer(Account from, Account to, MoneyValue amount, String memo) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Transfer accounts cannot be null.");
        }
        validatePositiveAmount(amount);
        from.ensureOpen();
        to.ensureOpen();
        if (from.balance.compareTo(amount) < 0) {
            throw new NegativeBalanceException("Account " + CLIUtil.formatAcct(from.acctNumber)
                    + " does not have enough available balance for this operation.");
        }

        from.applyOutgoing(amount);
        to.applyIncoming(amount);

        Transaction transaction = new Transaction(from.acctNumber, to.acctNumber, amount, memo);
        from.ledger.addTransaction(transaction);
        if (from != to) {
            to.ledger.addTransaction(transaction);
        }
        return transaction;
    }

    /**
     * Accrues one day of interest if this account has interest.
     * Precondition: account is open or closed with a zero balance; balance is non-negative.
     * Postcondition: balance increases by rounded daily interest and returns the interest amount.
     */
    public MoneyValue iterateInterest() {
        if (!hasInterest || balance.isZero()) {
            return MoneyValue.ZERO;
        }
        MoneyValue interest = balance.multiply(yearlyInterestRate / 365.0);
        if (interest.isZero()) {
            return MoneyValue.ZERO;
        }
        balance = balance.plus(interest);
        ledger.setCurrentBalance(balance);
        Transaction transaction = new Transaction(Bank.BANK_CASH_ACCOUNT_NUMBER, acctNumber, interest,
                credit ? "Daily APR interest" : "Daily APY interest");
        ledger.addTransaction(transaction);
        return interest;
    }

    /**
     * Closes the account only when the balance is zero.
     * Precondition: balance must be zero.
     * Postcondition: account rejects all future modifications.
     */
    public void closeAccount() {
        if (!balance.isZero()) {
            throw new BalanceNotZeroException("Account must have a $0.00 balance before it can be closed.");
        }
        closed = true;
    }

    public int getAcctNumber() {
        return acctNumber;
    }

    public Person getOwner() {
        return owner;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public MoneyValue getBalance() {
        return balance;
    }

    public boolean isCredit() {
        return credit;
    }

    public boolean hasInterest() {
        return hasInterest;
    }

    public boolean isClosed() {
        return closed;
    }

    public double getYearlyInterestRate() {
        return yearlyInterestRate;
    }

    public abstract String getAccountType();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getAccountType()).append(" Account# ").append(CLIUtil.formatAcct(acctNumber)).append("\n");
        builder.append(owner).append("\n");
        builder.append(credit ? "Current Debt: " : "Current Balance: ").append(balance).append("\n");
        if (hasInterest) {
            builder.append(credit ? "APR: " : "APY: ")
                    .append(String.format("%.2f%%", yearlyInterestRate * 100)).append("\n");
        }
        builder.append("Status: ").append(closed ? "Closed" : "Open");
        return builder.toString();
    }

    /**
     * Precondition: amount is positive and the account is open.
     * Postcondition: balance is decreased, never below zero.
     */
    private void applyOutgoing(MoneyValue amount) {
        ensureOpen();
        balance = balance.minus(amount);
        ledger.setCurrentBalance(balance);
    }

    /**
     * Precondition: amount is positive and the account is open.
     * Postcondition: balance is increased.
     */
    private void applyIncoming(MoneyValue amount) {
        ensureOpen();
        balance = balance.plus(amount);
        ledger.setCurrentBalance(balance);
    }

    private void ensureOpen() {
        if (closed) {
            throw new ClosedAccountException("Account " + CLIUtil.formatAcct(acctNumber) + " is closed.");
        }
    }

    private static void validatePositiveAmount(MoneyValue amount) {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidAmountException("Amount must be greater than $0.00.");
        }
    }
}
