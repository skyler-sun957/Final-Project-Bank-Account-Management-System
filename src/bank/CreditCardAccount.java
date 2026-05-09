package bank;

/**
 * Credit account that allows cash advances and accrues APR interest.
 */
public class CreditCardAccount extends CreditAccount {
    public CreditCardAccount(int acctNumber, Person owner, double apr) {
        super(acctNumber, owner, apr);
    }

    @Override
    public String getAccountType() {
        return "Credit Card";
    }
}
