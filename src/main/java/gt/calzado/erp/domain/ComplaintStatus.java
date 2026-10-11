package gt.calzado.erp.domain;

public enum ComplaintStatus {
    OPEN("Abierta"),
    IN_PROGRESS("En seguimiento"),
    RESOLVED("Resuelta");

    private final String label;

    ComplaintStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}