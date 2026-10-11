package gt.calzado.erp.domain;

public enum OrderStatus {
    RECEIVED("Recibido"),
    PREPARING("En preparacion"),
    SHIPPED("Enviado"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}