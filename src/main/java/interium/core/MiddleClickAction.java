package interium.core;

/** The eventual adapter must supply target/item data and perform the chosen action. */
public enum MiddleClickAction {
    PEARL, FRIEND;
    public boolean isAvailable(boolean pearlInHotbar, boolean playerTargeted) {
        return this==PEARL ? pearlInHotbar : playerTargeted;
    }
}
