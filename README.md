# Bank Account Management System

Java command-line final project for CS A. The project models a bank app with inheritance, multiple account types, transactions, ledgers, interest, and a CLI.

## Repository layout

```text
BankAccountManagementSystem/
├── README.md
├── .gitignore
├── src/
│   └── bank/
│       ├── Main.java
│       ├── BankCLI.java
│       ├── CLIUtil.java
│       ├── Bank.java
│       ├── Account.java
│       ├── CheckingAccount.java
│       ├── SavingAccount.java
│       ├── CreditAccount.java
│       ├── LoanAccount.java
│       ├── CreditCardAccount.java
│       ├── Person.java
│       ├── Name.java
│       ├── MoneyValue.java
│       ├── Ledger.java
│       ├── Transaction.java
│       └── exception classes
└── tests/
    └── BankTestRunner.java
```

## Features

- Abstract `Account` superclass with inheritance for:
  - `CheckingAccount`
  - `SavingAccount`
  - abstract `CreditAccount`
  - `LoanAccount`
  - `CreditCardAccount`
- `Bank` object manages global state:
  - all accounts
  - all clients
  - all transactions
  - total liquid cash
  - total debit balances
  - total credit balances
- Account numbers:
  - `000000` = bank cash reserve
  - `999999` = test checking account with starting funds
  - normal customer accounts are randomly generated six-digit numbers
- Deposit, withdraw, transfer, close account, view account, view ledger, view all accounts, and bank audit summary.
- Daily interest/APR calculation rounded to the nearest cent.
- `MoneyValue` stores money safely in cents instead of using floating-point dollars.
- CLI paged output prints five ledger/account entries at a time.
- Major methods include precondition/postcondition comments.

## Compile and run

From the project root:

```bash
javac -d out src/bank/*.java
java -cp out bank.Main
```

## Run tests

```bash
javac -d out src/bank/*.java tests/BankTestRunner.java
java -cp out bank.BankTestRunner
```

Expected output:

```text
All tests passed.
```

## Suggested demo flow

1. Start the program.
2. Choose `6` to view all accounts and point out the reserved test account `999999`.
3. Create a checking account with an opening deposit.
4. Create a saving account for the same client and transfer money from checking to saving.
5. Create a credit card, withdraw from it to show debt increasing, then deposit to show debt decreasing.
6. Create a loan and try to withdraw from it again to demonstrate the rule check.
7. Run one day of interest and view account ledgers.
8. Show the bank audit summary.

## Notes on credit accounts

For this demo, credit account balance means debt owed. A credit card withdrawal is a cash advance that increases debt. A deposit to a credit account is treated as a payment that decreases debt. A loan receives its principal at opening, but additional withdrawals are blocked.
