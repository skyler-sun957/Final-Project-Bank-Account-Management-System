package bank;

/**
 * Immutable name value object.
 * Precondition: first and last names are non-empty. Middle name is optional.
 * Postcondition: once created, a Name cannot be changed.
 */
public final class Name {
    private final String firstName;
    private final String lastName;
    private final boolean hasMiddleName;
    private final String middleName;

    public Name(String firstName, String lastName) {
        this(firstName, "", lastName);
    }

    public Name(String firstName, String middleName, String lastName) {
        this.firstName = cleanRequired(firstName, "First name");
        this.lastName = cleanRequired(lastName, "Last name");
        this.middleName = cleanOptional(middleName);
        this.hasMiddleName = !this.middleName.isEmpty();
    }

    /**
     * Precondition: fullName contains at least two words.
     * Postcondition: returns a Name using first word as first name and last word as last name.
     */
    public static Name fromFullName(String fullName) {
        if (fullName == null) {
            throw new IllegalArgumentException("Full name cannot be null.");
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Please enter at least a first and last name.");
        }
        if (parts.length == 2) {
            return new Name(parts[0], parts[1]);
        }

        StringBuilder middle = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
            if (middle.length() > 0) {
                middle.append(' ');
            }
            middle.append(parts[i]);
        }
        return new Name(parts[0], middle.toString(), parts[parts.length - 1]);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean hasMiddleName() {
        return hasMiddleName;
    }

    public String getMiddleName() {
        return middleName;
    }

    /**
     * Precondition: none.
     * Postcondition: returns a lowercase key used by Bank to enforce unique client names.
     */
    public String registryKey() {
        return toString().toLowerCase();
    }

    @Override
    public String toString() {
        if (hasMiddleName) {
            return firstName + " " + middleName + " " + lastName;
        }
        return firstName + " " + lastName;
    }

    private static String cleanRequired(String text, String fieldName) {
        String cleaned = cleanOptional(text);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return cleaned;
    }

    private static String cleanOptional(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().replaceAll("\\s+", " ");
    }
}
