package bank;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Command-line interface for the bank account management system.
 */
public class BankCLI {
    private final Bank bank;
    private final Scanner scanner;

    public BankCLI() {
        bank = new Bank();
        scanner = new Scanner(System.in);
    }

    /**
     * Precondition: standard input is available.
     * Postcondition: program loops until user chooses Exit.
     */
    public void run() {
        printWelcome();
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = CLIUtil.promptIntInRange(scanner, "Choose an option: ", 0, 10);
            try {
                switch (choice) {
                    case 1:
                        handleCreateAccount();
                        break;
                    case 2:
                        handleDeposit();
                        break;
                    case 3:
                        handleWithdraw();
                        break;
                    case 4:
                        handleTransfer();
                        break;
                    case 5:
                        handleViewAccount();
                        break;
                    case 6:
                        handleViewAllAccounts();
                        break;
                    case 7:
                        handleViewLedger();
                        break;
                    case 8:
                        handleInterest();
                        break;
                    case 9:
                        handleCloseAccount();
                        break;
                    case 10:
                        System.out.println(bank.getAuditSummary());
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Unknown option.");
                }
            } catch (RuntimeException ex) {
                System.out.println("Operation failed: " + ex.getMessage());
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
    }

    private void printWelcome() {
        System.out.println("=======================================");
        System.out.println(" Bank Account Management System");
        System.out.println("=======================================");
        System.out.println("Test checking account: " + CLIUtil.formatAcct(Bank.TEST_ACCOUNT_NUMBER));
        System.out.println("Use it for transfers, deposits, and withdrawals while testing.");
        System.out.println();
    }

    private void printMainMenu() {
        System.out.println("Main Menu");
        System.out.println("1. Create account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer between checking/saving accounts");
        System.out.println("5. View account");
        System.out.println("6. View all accounts");
        System.out.println("7. View account transactions");
        System.out.println("8. Run one day of interest");
        System.out.println("9. Close account");
        System.out.println("10. Bank audit summary");
        System.out.println("0. Exit");
    }

    /**
     * Precondition: user enters valid account data.
     * Postcondition: a new account is created and displayed.
     */
    private void handleCreateAccount() {
        System.out.println("Account Types");
        System.out.println("1. Checking");
        System.out.println("2. Saving");
        System.out.println("3. Credit Card");
        System.out.println("4. Loan");
        int type = CLIUtil.promptIntInRange(scanner, "Choose account type: ", 1, 4);
        Name ownerName = promptName();

        Account account;
        if (type == 1) {
            MoneyValue openingDeposit = CLIUtil.promptMoney(scanner, "Opening deposit (0 allowed): $");
            account = bank.createCheckingAccount(ownerName, openingDeposit);
        } else if (type == 2) {
            MoneyValue openingDeposit = CLIUtil.promptMoney(scanner, "Opening deposit (0 allowed): $");
            double apy = CLIUtil.promptPercentAsRate(scanner, "APY percent", bank.getDefaultAPY());
            account = bank.createSavingAccount(ownerName, openingDeposit, apy);
        } else if (type == 3) {
            double apr = CLIUtil.promptPercentAsRate(scanner, "APR percent", bank.getDefaultAPR());
            account = bank.createCreditCardAccount(ownerName, apr);
        } else {
            MoneyValue principal = CLIUtil.promptMoney(scanner, "Loan principal: $");
            double apr = CLIUtil.promptPercentAsRate(scanner, "APR percent", bank.getDefaultAPR());
            account = bank.createLoanAccount(ownerName, principal, apr);
        }

        System.out.println("Created account:");
        System.out.println(account);
    }

    /**
     * Precondition: user enters an account and positive amount.
     * Postcondition: deposit transaction is recorded.
     */
    private void handleDeposit() {
        int accountNumber = CLIUtil.promptAccountNumber(scanner, "Account number: ");
        MoneyValue amount = CLIUtil.promptMoney(scanner, "Deposit amount: $");
        Transaction transaction = bank.deposit(accountNumber, amount);
        System.out.println("Deposit complete: " + transaction.formatGeneral());
    }

    /**
     * Precondition: user enters an account and positive amount.
     * Postcondition: withdrawal transaction is recorded unless rules block it.
     */
    private void handleWithdraw() {
        int accountNumber = CLIUtil.promptAccountNumber(scanner, "Account number: ");
        MoneyValue amount = CLIUtil.promptMoney(scanner, "Withdrawal amount: $");
        Transaction transaction = bank.withdraw(accountNumber, amount);
        System.out.println("Withdrawal complete: " + transaction.formatGeneral());
    }

    /**
     * Precondition: both accounts are debit accounts and amount is positive.
     * Postcondition: transfer transaction is recorded.
     */
    private void handleTransfer() {
        int from = CLIUtil.promptAccountNumber(scanner, "From account: ");
        int to = CLIUtil.promptAccountNumber(scanner, "To account: ");
        MoneyValue amount = CLIUtil.promptMoney(scanner, "Transfer amount: $");
        Transaction transaction = bank.transfer(from, to, amount);
        System.out.println("Transfer complete: " + transaction.formatGeneral());
    }

    private void handleViewAccount() {
        int accountNumber = CLIUtil.promptAccountNumber(scanner, "Account number: ");
        System.out.println(bank.getAccount(accountNumber));
    }

    private void handleViewAllAccounts() {
        ArrayList<String> lines = CLIUtil.formatAccounts(bank.getAccounts());
        CLIUtil.pagedOutput(lines, scanner);
    }

    private void handleViewLedger() {
        int accountNumber = CLIUtil.promptAccountNumber(scanner, "Account number: ");
        Account account = bank.getAccount(accountNumber);
        System.out.println(account.getAccountType() + " Account " + CLIUtil.formatAcct(account.getAcctNumber()));
        System.out.println("Current " + (account.isCredit() ? "Debt: " : "Balance: ") + account.getBalance());
        CLIUtil.pagedOutput(account.getLedger().formatEntries(account), scanner);
    }

    private void handleInterest() {
        MoneyValue total = bank.iterateInterest();
        System.out.println("One day of interest applied. Total interest generated: " + total);
    }

    private void handleCloseAccount() {
        int accountNumber = CLIUtil.promptAccountNumber(scanner, "Account number to close: ");
        bank.closeAccount(accountNumber);
        System.out.println("Account closed.");
    }

    private Name promptName() {
        String first = CLIUtil.promptRequired(scanner, "First name: ");
        String middle = CLIUtil.promptOptional(scanner, "Middle name (optional): ");
        String last = CLIUtil.promptRequired(scanner, "Last name: ");
        return new Name(first, middle, last);
    }
}
