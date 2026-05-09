package bank;

/**
 * Debit account that earns APY interest.
 */
public class SavingAccount extends Account {
    public SavingAccount(int acctNumber, Person owner, double yearlyInterestRate) {
        super(acctNumber, owner, false, true, yearlyInterestRate);
    }

    SavingAccount(int acctNumber, Person owner, double yearlyInterestRate, MoneyValue startingBalance) {
        super(acctNumber, owner, false, true, yearlyInterestRate, startingBalance);
    }

    @Override
    public String getAccountType() {
        return "Saving";
    }
}
