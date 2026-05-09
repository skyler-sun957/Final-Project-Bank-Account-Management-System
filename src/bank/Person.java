package bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents one bank client. In this demo, a person is uniquely identified by their current name.
 */
public class Person {
    private Name name;
    private final ArrayList<Name> pastNames;

    public Person(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("Person name cannot be null.");
        }
        this.name = name;
        this.pastNames = new ArrayList<>();
    }

    /**
     * Precondition: newName is not null and is not already used by another client in Bank.
     * Postcondition: current name moves to pastNames, then the current name is changed.
     */
    public void changeName(Name newName) {
        if (newName == null) {
            throw new IllegalArgumentException("New name cannot be null.");
        }
        pastNames.add(name);
        name = newName;
    }

    public Name getName() {
        return name;
    }

    public List<Name> getPastNames() {
        return Collections.unmodifiableList(pastNames);
    }

    @Override
    public String toString() {
        return name.toString();
    }
}
