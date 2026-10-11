package gt.calzado.erp.domain;

public enum UserRole {
    ADMIN("Administrador"),
    SALES("Ventas"),
    WAREHOUSE("Bodega"),
    SUPPORT("Soporte"),
    ANALYST("Analista programador"),
    CUSTOMER("Cliente");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}