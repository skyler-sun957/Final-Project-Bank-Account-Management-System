package bank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Controls global bank state: accounts, clients, all transactions, totals, and account creation.
 */
public class Bank {
    public static final int BANK_CASH_ACCOUNT_NUMBER = 0;
    public static final int TEST_ACCOUNT_NUMBER = 999999;

    private static final MoneyValue STARTING_BANK_CASH = MoneyValue.ofDollars(1_000_000_000L);
    private static final MoneyValue STARTING_TEST_BALANCE = MoneyValue.ofDollars(100_000L);

    private final Map<Integer, Account> accounts;
    private final Map<String, Person> clients;
    private final ArrayList<Transaction> allTransactions;
    private final Random random;

    private MoneyValue totalLiquidCash;
    private MoneyValue totalDebitBalance;
    private MoneyValue totalCreditBalance;

    private final double defaultAPY;
    private final double defaultAPR;

    public Bank() {
        accounts = new HashMap<>();
        clients = new HashMap<>();
        allTransactions = new ArrayList<>();
        random = new Random();

        defaultAPY = 0.02;
        defaultAPR = 0.1899;

        initializeReservedAccounts();
        recomputeTotals();
    }

    /**
     * Creates a checking account for a client name.
     * Precondition: ownerName is valid; openingDeposit is non-negative.
     * Postcondition: account is stored and optional opening deposit is recorded.
     */
    public CheckingAccount createCheckingAccount(Name ownerName, MoneyValue openingDeposit) {
        Person owner = getOrCreatePerson(ownerName);
        CheckingAccount account = new CheckingAccount(generateAccountNumber(), owner);
        accounts.put(account.getAcctNumber(), account);
        if (openingDeposit != null && openingDeposit.isPositive()) {
            deposit(account.getAcctNumber(), openingDeposit);
        }
        recomputeTotals();
        return account;
    }

    /**
     * Creates a saving account using the provided APY.
     * Precondition: ownerName is valid; yearlyInterestRate >= 0.
     * Postcondition: account is stored and optional opening deposit is recorded.
     */
    public SavingAccount createSavingAccount(Name ownerName, MoneyValue openingDeposit, double yearlyInterestRate) {
        Person owner = getOrCreatePerson(ownerName);
        SavingAccount account = new SavingAccount(generateAccountNumber(), owner, yearlyInterestRate);
        accounts.put(account.getAcctNumber(), account);
        if (openingDeposit != null && openingDeposit.isPositive()) {
            deposit(account.getAcctNumber(), openingDeposit);
        }
        recomputeTotals();
        return account;
    }

    /**
     * Creates a credit card account with zero starting debt.
     * Precondition: ownerName is valid; apr >= 0.
     * Postcondition: credit card account is stored.
     */
    public CreditCardAccount createCreditCardAccount(Name ownerName, double apr) {
        Person owner = getOrCreatePerson(ownerName);
        CreditCardAccount account = new CreditCardAccount(generateAccountNumber(), owner, apr);
        accounts.put(account.getAcctNumber(), account);
        recomputeTotals();
        return account;
    }

    /**
     * Creates a loan account and records the opening principal as debt.
     * Precondition: principal is positive; apr >= 0.
     * Postcondition: account is stored and opening principal appears in ledgers.
     */
    public LoanAccount createLoanAccount(Name ownerName, MoneyValue principal, double apr) {
        if (principal == null || !principal.isPositive()) {
            throw new InvalidAmountException("Loan principal must be greater than $0.00.");
        }
        Person owner = getOrCreatePerson(ownerName);
        LoanAccount account = new LoanAccount(generateAccountNumber(), owner, apr);
        accounts.put(account.getAcctNumber(), account);
        Transaction transaction = Account.transfer(getBankCashAccount(), account, principal, "Loan opened");
        record(transaction);
        return account;
    }

    /**
     * Deposits money. For debit accounts this adds cash; for credit accounts this pays down debt.
     * Precondition: account exists, is open, and amount is positive.
     * Postcondition: transaction is recorded and totals are updated.
     */
    public Transaction deposit(int acctNumber, MoneyValue amount) {
        validatePositiveAmount(amount);
        Account account = getAccount(acctNumber);
        Transaction transaction;
        if (account.isCredit()) {
            transaction = Account.transfer(account, getBankCashAccount(), amount, "Credit payment deposit");
        } else {
            transaction = Account.transfer(getBankCashAccount(), account, amount, "Deposit");
        }
        record(transaction);
        return transaction;
    }

    /**
     * Withdraws money. Loans block withdrawals after opening; credit cards create debt.
     * Precondition: account exists, is open, and amount is positive.
     * Postcondition: transaction is recorded and totals are updated.
     */
    public Transaction withdraw(int acctNumber, MoneyValue amount) {
        validatePositiveAmount(amount);
        Account account = getAccount(acctNumber);
        if (account instanceof LoanAccount) {
            throw new IncorrectAccountTypeException("Loan accounts cannot be withdrawn from after opening.");
        }

        Transaction transaction;
        if (account.isCredit()) {
            transaction = Account.transfer(getBankCashAccount(), account, amount, "Credit cash advance");
        } else {
            transaction = Account.transfer(account, getBankCashAccount(), amount, "Withdrawal");
        }
        record(transaction);
        return transaction;
    }

    /**
     * Transfers money between two debit accounts.
     * Precondition: both accounts exist, are open, non-credit accounts, and amount is positive.
     * Postcondition: transaction is recorded and totals are updated.
     */
    public Transaction transfer(int fromAcct, int toAcct, MoneyValue amount) {
        validatePositiveAmount(amount);
        Account from = getAccount(fromAcct);
        Account to = getAccount(toAcct);
        if (from.isCredit() || to.isCredit()) {
            throw new IncorrectAccountTypeException(
                    "Direct transfers are limited to checking/saving accounts. Use deposit to pay credit debt.");
        }
        if (fromAcct == toAcct) {
            throw new IllegalArgumentException("Cannot transfer to the same account.");
        }
        Transaction transaction = Account.transfer(from, to, amount, "Transfer");
        record(transaction);
        return transaction;
    }

    /**
     * Changes a client name while preserving old names in the Person object.
     * Precondition: current name exists and new name is not already used.
     * Postcondition: clients registry uses the new name as the key.
     */
    public void changeClientName(String currentFullName, Name newName) {
        if (newName == null) {
            throw new IllegalArgumentException("New name cannot be null.");
        }
        String oldKey = Name.fromFullName(currentFullName).registryKey();
        Person person = clients.get(oldKey);
        if (person == null) {
            throw new AccountNotFoundException("No client found with name " + currentFullName + ".");
        }
        String newKey = newName.registryKey();
        if (clients.containsKey(newKey)) {
            throw new DuplicatePersonException("Another client already uses the name " + newName + ".");
        }
        clients.remove(oldKey);
        person.changeName(newName);
        clients.put(newKey, person);
    }

    /**
     * Runs one day of interest accrual on every account that has interest.
     * Precondition: accounts have non-negative balances.
     * Postcondition: balances, ledgers, allTransactions, and totals are updated.
     */
    public MoneyValue iterateInterest() {
        MoneyValue totalInterest = MoneyValue.ZERO;
        for (Account account : accounts.values()) {
            int beforeSize = account.getLedger().getTransactions().size();
            MoneyValue interest = account.iterateInterest();
            if (interest.isPositive()) {
                totalInterest = totalInterest.plus(interest);
                Transaction latest = account.getLedger().getTransactions()
                        .get(account.getLedger().getTransactions().size() - 1);
                if (account.getLedger().getTransactions().size() > beforeSize) {
                    allTransactions.add(latest);
                }
            }
        }
        recomputeTotals();
        return totalInterest;
    }

    /**
     * Closes an account if it has a zero balance.
     * Precondition: account exists and balance is zero.
     * Postcondition: account is marked closed.
     */
    public void closeAccount(int acctNumber) {
        if (acctNumber == BANK_CASH_ACCOUNT_NUMBER || acctNumber == TEST_ACCOUNT_NUMBER) {
            throw new IncorrectAccountTypeException("Reserved system accounts cannot be closed.");
        }
        getAccount(acctNumber).closeAccount();
    }

    public Account getAccount(int acctNumber) {
        Account account = accounts.get(acctNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + CLIUtil.formatAcct(acctNumber) + " was not found.");
        }
        return account;
    }

    public Collection<Account> getAccounts() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    public ArrayList<Transaction> getAllTransactions() {
        return new ArrayList<>(allTransactions);
    }

    public double getDefaultAPY() {
        return defaultAPY;
    }

    public double getDefaultAPR() {
        return defaultAPR;
    }

    public MoneyValue getTotalLiquidCash() {
        return totalLiquidCash;
    }

    public MoneyValue getTotalDebitBalance() {
        return totalDebitBalance;
    }

    public MoneyValue getTotalCreditBalance() {
        return totalCreditBalance;
    }

    /**
     * Precondition: totals are current.
     * Postcondition: returns liquid cash + credit balances - debit balances.
     */
    public String getNetPositionString() {
        long netCents = totalLiquidCash.getCents() + totalCreditBalance.getCents() - totalDebitBalance.getCents();
        return MoneyValue.formatSignedCents(netCents);
    }

    /**
     * Precondition: none.
     * Postcondition: returns an audit summary useful for the CLI.
     */
    public String getAuditSummary() {
        return "Bank Audit Summary\n"
                + "------------------\n"
                + "Total liquid cash (reserve): " + totalLiquidCash + "\n"
                + "Total debit account balances: " + totalDebitBalance + "\n"
                + "Total credit debt balances: " + totalCreditBalance + "\n"
                + "Net position: " + getNetPositionString() + "\n"
                + "Accounts tracked: " + accounts.size() + "\n"
                + "Clients tracked: " + clients.size() + "\n"
                + "Transactions tracked: " + allTransactions.size();
    }

    private void initializeReservedAccounts() {
        Person bankPerson = new Person(new Name("Bank", "Reserve"));
        Person testPerson = new Person(new Name("Testing", "User"));
        clients.put(bankPerson.getName().registryKey(), bankPerson);
        clients.put(testPerson.getName().registryKey(), testPerson);

        accounts.put(BANK_CASH_ACCOUNT_NUMBER,
                new CheckingAccount(BANK_CASH_ACCOUNT_NUMBER, bankPerson, STARTING_BANK_CASH));
        accounts.put(TEST_ACCOUNT_NUMBER,
                new CheckingAccount(TEST_ACCOUNT_NUMBER, testPerson, STARTING_TEST_BALANCE));
    }

    private Person getOrCreatePerson(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        String key = name.registryKey();
        Person existing = clients.get(key);
        if (existing != null) {
            return existing;
        }
        Person person = new Person(name);
        clients.put(key, person);
        return person;
    }

    private int generateAccountNumber() {
        int candidate;
        do {
            candidate = 100000 + random.nextInt(899999);
        } while (candidate == TEST_ACCOUNT_NUMBER || candidate == BANK_CASH_ACCOUNT_NUMBER || accounts.containsKey(candidate));
        return candidate;
    }

    private Account getBankCashAccount() {
        return accounts.get(BANK_CASH_ACCOUNT_NUMBER);
    }

    private void record(Transaction transaction) {
        allTransactions.add(transaction);
        recomputeTotals();
    }

    private void recomputeTotals() {
        totalLiquidCash = getBankCashAccount().getBalance();
        MoneyValue debit = MoneyValue.ZERO;
        MoneyValue credit = MoneyValue.ZERO;
        for (Account account : accounts.values()) {
            if (account.getAcctNumber() == BANK_CASH_ACCOUNT_NUMBER) {
                continue;
            }
            if (account.isCredit()) {
                credit = credit.plus(account.getBalance());
            } else {
                debit = debit.plus(account.getBalance());
            }
        }
        totalDebitBalance = debit;
        totalCreditBalance = credit;
    }

    private void validatePositiveAmount(MoneyValue amount) {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidAmountException("Amount must be greater than $0.00.");
        }
    }
}
