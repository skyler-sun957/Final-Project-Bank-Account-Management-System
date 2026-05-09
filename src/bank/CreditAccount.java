package bank;

/**
 * Abstract credit account. Balance means debt owed, not cash available.
 */
public abstract class CreditAccount extends Account {
    protected CreditAccount(int acctNumber, Person owner, double apr) {
        super(acctNumber, owner, true, true, apr);
    }

    protected CreditAccount(int acctNumber, Person owner, double apr, MoneyValue startingDebt) {
        super(acctNumber, owner, true, true, apr, startingDebt);
    }

    public double getApr() {
        return getYearlyInterestRate();
    }
}
