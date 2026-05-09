package bank;

/**
 * Debit account with no interest.
 */
public class CheckingAccount extends Account {
    public CheckingAccount(int acctNumber, Person owner) {
        super(acctNumber, owner, false, false, 0.0);
    }

    CheckingAccount(int acctNumber, Person owner, MoneyValue startingBalance) {
        super(acctNumber, owner, false, false, 0.0, startingBalance);
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }
}
