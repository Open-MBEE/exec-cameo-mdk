package org.openmbee.mdk.fileexport;

public enum ContextExportLevel {
    None(0), Direct(1), Transitive(2), Containment(3);

    private final int value;
    private ContextExportLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isNoneIncluded() {
        return getValue() >= None.getValue();
    }

    public boolean isDirectIncluded() {
        return getValue() >= Direct.getValue();
    }

    public boolean isTransitiveIncluded() {
        return getValue() >= Transitive.getValue();
    }

    public boolean isContainmentIncluded() {
        return getValue() >= Containment.getValue();
    }
}
