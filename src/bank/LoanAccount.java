package bank;

/**
 * Credit account for a loan. Users cannot take additional withdrawals after opening.
 */
public class LoanAccount extends CreditAccount {
    public LoanAccount(int acctNumber, Person owner, double apr) {
        super(acctNumber, owner, apr);
    }

    LoanAccount(int acctNumber, Person owner, double apr, MoneyValue startingDebt) {
        super(acctNumber, owner, apr, startingDebt);
    }

    @Override
    public String getAccountType() {
        return "Loan";
    }
}
