package gt.calzado.erp.web;

import gt.calzado.erp.domain.Product;
import java.math.BigDecimal;

public record CartLine(Product product, int quantity, BigDecimal subtotal) {
}