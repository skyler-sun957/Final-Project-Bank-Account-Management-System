package bank;

/**
 * Lightweight tests that do not require JUnit.
 * Run with: javac -d out src/bank/*.java tests/BankTestRunner.java && java -cp out bank.BankTestRunner
 */
public class BankTestRunner {
    public static void main(String[] args) {
        testMoneyParsingAndArithmetic();
        testDebitDepositWithdrawTransfer();
        testCreditCardCashAdvanceAndPayment();
        testLoanCannotWithdrawAfterOpening();
        testInterestAccrual();
        System.out.println("All tests passed.");
    }

    private static void testMoneyParsingAndArithmetic() {
        MoneyValue value = MoneyValue.parse("$12.345");
        assertEquals(MoneyValue.ofCents(1235), value, "Money should round to nearest cent.");
        assertEquals(MoneyValue.ofCents(1335), value.plus(MoneyValue.ofCents(100)), "Addition failed.");
        assertEquals(MoneyValue.ofCents(1230), value.minus(MoneyValue.ofCents(5)), "Subtraction failed.");
    }

    private static void testDebitDepositWithdrawTransfer() {
        Bank bank = new Bank();
        CheckingAccount checking = bank.createCheckingAccount(new Name("Ada", "Lovelace"), MoneyValue.parse("100.00"));
        SavingAccount saving = bank.createSavingAccount(new Name("Ada", "Lovelace"), MoneyValue.ZERO, bank.getDefaultAPY());

        bank.withdraw(checking.getAcctNumber(), MoneyValue.parse("25.00"));
        assertEquals(MoneyValue.parse("75.00"), checking.getBalance(), "Checking withdrawal failed.");

        bank.transfer(checking.getAcctNumber(), saving.getAcctNumber(), MoneyValue.parse("50.00"));
        assertEquals(MoneyValue.parse("25.00"), checking.getBalance(), "Checking transfer debit failed.");
        assertEquals(MoneyValue.parse("50.00"), saving.getBalance(), "Saving transfer credit failed.");
    }

    private static void testCreditCardCashAdvanceAndPayment() {
        Bank bank = new Bank();
        CreditCardAccount card = bank.createCreditCardAccount(new Name("Grace", "Hopper"), bank.getDefaultAPR());
        bank.withdraw(card.getAcctNumber(), MoneyValue.parse("40.00"));
        assertEquals(MoneyValue.parse("40.00"), card.getBalance(), "Credit card cash advance should increase debt.");
        bank.deposit(card.getAcctNumber(), MoneyValue.parse("15.00"));
        assertEquals(MoneyValue.parse("25.00"), card.getBalance(), "Credit card payment should decrease debt.");
    }

    private static void testLoanCannotWithdrawAfterOpening() {
        Bank bank = new Bank();
        LoanAccount loan = bank.createLoanAccount(new Name("Katherine", "Johnson"), MoneyValue.parse("500.00"), bank.getDefaultAPR());
        assertEquals(MoneyValue.parse("500.00"), loan.getBalance(), "Loan opening principal should become debt.");
        boolean threw = false;
        try {
            bank.withdraw(loan.getAcctNumber(), MoneyValue.parse("1.00"));
        } catch (IncorrectAccountTypeException ex) {
            threw = true;
        }
        assertTrue(threw, "Loan withdrawal after opening should be blocked.");
    }

    private static void testInterestAccrual() {
        Bank bank = new Bank();
        SavingAccount saving = bank.createSavingAccount(new Name("Alan", "Turing"), MoneyValue.parse("365.00"), 0.365);
        MoneyValue interest = bank.iterateInterest();
        assertTrue(interest.isPositive(), "Interest should be generated.");
        assertEquals(MoneyValue.parse("365.37"), saving.getBalance(), "Daily interest should round to nearest cent.");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", actual: " + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }
}
