## Design

stores and audits ledger across accounts, by tracking total liquid cash and net earning (by subtracting all debit account from all credit account + liquid cash)

## Class

### BankCLI

CLI interface with Bank class

### CLIUtil

CLI tools

- input/output handling
- debug print
- paged output: prints 5 entries at a time from an array; after each page the user is prompted to view the next page or exit
- menu choice

### Bank

Class controlling global state

- keep track of allTransactions, accounts, totalLiquidCash, totalDebitBalance, totalCreditBalance
- contains defaultAPY, defaultAPR
- provides deposit, withdraw, transfer
- deposit and withdraw are special transactions handled by Bank, routing cash flow through the bank's cash reserve account (account number `000000`). Account itself does not distinguish transaction types.
- totalLiquidCash, totalDebitBalance, and totalCreditBalance are updated on every transaction and every iterateInterest call
- test account `999999` is added to provide initial liquidity in testing
- maintains a clients registry (name → Person) so that no two persons share the same name; names serve as the lookup key in this demo in lieu of SSN
- account creation factory methods to be specified in the CLI design section

### Account

- Abstract base class; only leaf subclasses (CheckingAccount, SavingAccount, LoanAccount, CreditCardAccount) are instantiable
- each account is assigned and indexed by a 6 digit account number between 100000 to 999999; generated randomly with collision check; `000000` is the bank cash reserve, `999999` is the test account (both reserved at startup)
- all members are private; public getters provide read-only access; fields are immutable after construction except balance, closed, and ledger
- balance is always non-negative for all account types; any transaction that would result in a negative balance throws NegativeBalanceException
- provides transfer and view

```pseudo code
// all fields private
int acctNumber          // set by constructor, immutable
Person owner            // set by constructor, immutable
Ledger ledger
MoneyValue balance      // always >= 0
bool isCredit           // set by leaf constructor, immutable
bool hasInterest        // set by leaf constructor, immutable
float yearlyInterestRate  // set by constructor, immutable
bool closed

// abstract; leaf classes call this with their fixed isCredit/hasInterest values
protected Account(int acctNumber, Person owner, bool isCredit, bool hasInterest)
protected Account(int acctNumber, Person owner, bool isCredit, bool hasInterest, float yearlyInterestRate)

/**
Transfer from one account to another. Always succeeds or throws.
@throws NegativeBalanceException if source balance would go negative
 */
bool transfer(Account from, Account to, MoneyValue amount)
int getAcctNumber()
Person getOwner()
Ledger getLedger()
bool isCredit()
bool hasInterest()
bool isClosed()
/**
Called daily for interest calculation, if applicable.
Does nothing if !hasInterest.
Daily interest accrued = balance * (yearlyInterestRate / 365), rounded to nearest cent.
Updates bank totals after accrual.
Returns amount of interest generated.
 */
MoneyValue iterateInterest()
/**
Mark an account as closed. Fails if balance != 0 (works for both debit and credit accounts).
Closed accounts reject all further modifications.
@throws BalanceNotZeroException
 */
void closeAccount()
```

Overload print (for each account type) to something like
```
Credit Account# 123456
Jane Doe
Current Balance: $123.45
APR: 12.34%
```

### CheckingAccount:Account

boring old checking account, 0 interest enforced by constructor

### SavingAccount:Account

simple self explainatory

### CreditAccount:Account

Abstract class. Special case for credit account, where balance represents debt owed (always non-negative).

- "withdraw" always means the user taking cash out; for credit accounts this increases the balance (more debt), implemented as a transfer from account `000000` to the credit account
- "deposit" (paying down debt) decreases the balance; implemented as a transfer from the credit account to account `000000`
- this is intentional for a demo project; how cash physically flows out is not modelled

### LoanAccount and CreditCardAccount : CreditAccount

self explanatory. LoanAccount cannot be from-account so any form of withdraw is blocked except at account open time — subsequent withdraw attempts throw an IncorrectAccountTypeException.

### Person

Name, and past names. All fields private with getters.

```
// all fields private
Name name
ArrayList<Name> pastName
Person(Name name)
void changeName(Name newName)  // appends current name to pastName before updating
Name getName()
ArrayList<Name> getPastNames()
// print overload. Prints name
```

Persons are uniquely identified by name within the Bank clients registry. No two persons may share the same full name.

#### Name

simple struct

```
String firstName
String lastName
bool hasMiddleName
String middleName
```

### MoneyValue

Money is represented in cents, long unsigned int. Value is always non-negative; operations that would produce a negative result are illegal and must be caught by the caller before invoking them.
provides arithmetic operations (+ - between MoneyValue, * between MoneyValue and float, == < > for comparison)
all interest calculation rounds to nearest cent

### Ledger

List of transactions, also packs balance for convenience. Print formatting delegates to CLIUtil paged output.

#### Transaction

Simple struct. Fields:

```
timestamp   // date/time of transaction
int fromAcct  // account number of source
int toAcct    // account number of destination
MoneyValue amount
bool isDeposit()
bool isWithdraw()
```

Output formatting checks whether fromAcct or toAcct equals `000000` to render deposit/withdraw labels instead of raw account numbers.
