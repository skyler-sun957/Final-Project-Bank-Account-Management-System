package bank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Immutable money value stored as a non-negative number of cents.
 * Precondition: all constructed values must be greater than or equal to zero.
 * Postcondition: arithmetic methods return new MoneyValue objects and never mutate this object.
 */
public final class MoneyValue implements Comparable<MoneyValue> {
    public static final MoneyValue ZERO = new MoneyValue(0);

    private final long cents;

    private MoneyValue(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("MoneyValue cannot be negative.");
        }
        this.cents = cents;
    }

    /**
     * Precondition: cents >= 0.
     * Postcondition: returns a MoneyValue representing exactly that many cents.
     */
    public static MoneyValue ofCents(long cents) {
        return new MoneyValue(cents);
    }

    /**
     * Precondition: dollars >= 0.
     * Postcondition: returns a MoneyValue representing whole dollars.
     */
    public static MoneyValue ofDollars(long dollars) {
        return new MoneyValue(Math.multiplyExact(dollars, 100));
    }

    /**
     * Parses input like "12", "12.30", "$12.30", or "1,234.56".
     * Precondition: input is not null and represents a non-negative amount.
     * Postcondition: returns the parsed amount rounded to the nearest cent.
     */
    public static MoneyValue parse(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Money input cannot be null.");
        }

        String cleaned = input.trim().replace("$", "").replace(",", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Money input cannot be empty.");
        }

        BigDecimal decimal = new BigDecimal(cleaned);
        if (decimal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Money input cannot be negative.");
        }

        long cents = decimal.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
        return new MoneyValue(cents);
    }

    /**
     * Precondition: other is not null.
     * Postcondition: returns this + other without changing either object.
     */
    public MoneyValue plus(MoneyValue other) {
        requireOther(other);
        return new MoneyValue(Math.addExact(this.cents, other.cents));
    }

    /**
     * Precondition: other is not null and this >= other.
     * Postcondition: returns this - other without changing either object.
     */
    public MoneyValue minus(MoneyValue other) {
        requireOther(other);
        if (this.cents < other.cents) {
            throw new NegativeBalanceException("This operation would create a negative money value.");
        }
        return new MoneyValue(this.cents - other.cents);
    }

    /**
     * Precondition: factor >= 0.
     * Postcondition: returns this * factor rounded to the nearest cent.
     */
    public MoneyValue multiply(double factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("MoneyValue cannot be multiplied by a negative factor.");
        }
        return new MoneyValue(Math.round(this.cents * factor));
    }

    public long getCents() {
        return cents;
    }

    public boolean isZero() {
        return cents == 0;
    }

    public boolean isPositive() {
        return cents > 0;
    }

    @Override
    public int compareTo(MoneyValue other) {
        requireOther(other);
        return Long.compare(this.cents, other.cents);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MoneyValue)) {
            return false;
        }
        MoneyValue other = (MoneyValue) obj;
        return cents == other.cents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cents);
    }

    @Override
    public String toString() {
        return formatCents(cents);
    }

    /**
     * Precondition: signedCents can be positive, zero, or negative.
     * Postcondition: returns a formatted money string such as -$12.34.
     */
    public static String formatSignedCents(long signedCents) {
        if (signedCents < 0) {
            return "-" + formatCents(Math.abs(signedCents));
        }
        return formatCents(signedCents);
    }

    private static String formatCents(long cents) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return "$" + formatter.format(cents / 100.0);
    }

    private static void requireOther(MoneyValue other) {
        if (other == null) {
            throw new IllegalArgumentException("MoneyValue argument cannot be null.");
        }
    }
}
