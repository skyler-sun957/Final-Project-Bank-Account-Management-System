package bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Reusable command-line helper methods for menus, money input, debug messages, and paged output.
 */
public final class CLIUtil {
    private static boolean debugEnabled = false;

    private CLIUtil() {
        // Utility class; do not instantiate.
    }

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static void debug(String message) {
        if (debugEnabled) {
            System.out.println("[DEBUG] " + message);
        }
    }

    /**
     * Precondition: scanner is open; prompt is non-null.
     * Postcondition: returns a non-empty trimmed line.
     */
    public static String promptRequired(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Please enter a value.");
        }
    }

    /**
     * Precondition: scanner is open.
     * Postcondition: returns a trimmed line, possibly empty.
     */
    public static String promptOptional(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    /**
     * Precondition: scanner is open.
     * Postcondition: returns a valid MoneyValue entered by the user.
     */
    public static MoneyValue promptMoney(Scanner scanner, String prompt) {
        while (true) {
            try {
                return MoneyValue.parse(promptRequired(scanner, prompt));
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid money amount: " + ex.getMessage());
            }
        }
    }

    /**
     * Precondition: scanner is open.
     * Postcondition: returns a valid account number as an int.
     */
    public static int promptAccountNumber(Scanner scanner, String prompt) {
        while (true) {
            String line = promptRequired(scanner, prompt).replace("-", "").trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a 6-digit account number.");
            }
        }
    }

    /**
     * Precondition: scanner is open; min <= max.
     * Postcondition: returns an integer inside [min, max].
     */
    public static int promptIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(promptRequired(scanner, prompt));
                if (choice >= min && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException ignored) {
                // Show friendly message below.
            }
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    /**
     * Precondition: scanner is open and defaultValue >= 0.
     * Postcondition: returns a decimal percent converted to a rate, such as 2.5 -> 0.025.
     */
    public static double promptPercentAsRate(Scanner scanner, String prompt, double defaultValue) {
        while (true) {
            String line = promptOptional(scanner, prompt + " [default " + String.format("%.2f", defaultValue * 100) + "%]: ");
            if (line.isEmpty()) {
                return defaultValue;
            }
            try {
                double percent = Double.parseDouble(line.replace("%", "").trim());
                if (percent >= 0) {
                    return percent / 100.0;
                }
            } catch (NumberFormatException ignored) {
                // Show friendly message below.
            }
            System.out.println("Please enter a non-negative percent, for example 2.5.");
        }
    }

    /**
     * Precondition: account number is non-negative.
     * Postcondition: returns a six-digit account number string.
     */
    public static String formatAcct(int acctNumber) {
        return String.format("%06d", acctNumber);
    }

    /**
     * Prints five entries at a time, then lets the user continue or stop.
     * Precondition: entries and scanner are not null.
     * Postcondition: all requested entries are printed without modifying the list.
     */
    public static void pagedOutput(List<String> entries, Scanner scanner) {
        if (entries == null || entries.isEmpty()) {
            System.out.println("No entries to show.");
            return;
        }

        int index = 0;
        while (index < entries.size()) {
            int end = Math.min(index + 5, entries.size());
            for (int i = index; i < end; i++) {
                System.out.println(entries.get(i));
            }
            index = end;
            if (index < entries.size()) {
                String response = promptOptional(scanner, "Show next page? (y/n): ");
                if (!response.equalsIgnoreCase("y")) {
                    break;
                }
            }
        }
    }

    public static ArrayList<String> formatAccounts(Iterable<Account> accounts) {
        ArrayList<String> lines = new ArrayList<>();
        for (Account account : accounts) {
            lines.add(String.format("%s | %-12s | %-20s | %s%s",
                    formatAcct(account.getAcctNumber()),
                    account.getAccountType(),
                    account.getOwner(),
                    account.isCredit() ? "Debt " : "Balance ",
                    account.getBalance()));
        }
        return lines;
    }
}
