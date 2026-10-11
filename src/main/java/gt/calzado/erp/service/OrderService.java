package gt.calzado.erp.service;

import gt.calzado.erp.domain.Customer;
import gt.calzado.erp.domain.OrderItem;
import gt.calzado.erp.domain.OrderStatus;
import gt.calzado.erp.domain.Product;
import gt.calzado.erp.domain.SalesOrder;
import gt.calzado.erp.repository.CustomerRepository;
import gt.calzado.erp.repository.ProductRepository;
import gt.calzado.erp.repository.SalesOrderRepository;
import gt.calzado.erp.web.ShoppingCart;
import gt.calzado.erp.web.form.CheckoutForm;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final CustomerRepository customers;
    private final ProductRepository products;
    private final SalesOrderRepository orders;

    public OrderService(CustomerRepository customers, ProductRepository products, SalesOrderRepository orders) {
        this.customers = customers;
        this.products = products;
        this.orders = orders;
    }

    @Transactional
    public SalesOrder placeOrder(ShoppingCart cart, CheckoutForm form) {
        if (cart.isEmpty()) {
            throw new StockUnavailableException("El carrito está vacío.");
        }

        Customer customer = customers.findByEmailIgnoreCase(form.getEmail().trim())
                .orElseGet(Customer::new);
        customer.setFullName(form.getFullName().trim());
        customer.setEmail(form.getEmail().trim().toLowerCase());
        customer.setPhone(form.getPhone() == null ? null : form.getPhone().trim());
        customer.setShippingAddress(form.getShippingAddress().trim());
        customer = customers.save(customer);

        SalesOrder order = new SalesOrder();
        order.setOrderNumber("PASO-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.RECEIVED);

        BigDecimal total = BigDecimal.ZERO;
        for (var cartItem : cart.getItems().entrySet()) {
            Product product = products.findByIdAndActiveTrue(cartItem.getKey())
                    .orElseThrow(() -> new StockUnavailableException("Un producto del carrito ya no está disponible."));
            int quantity = cartItem.getValue();
            if (quantity < 1 || product.getStock() < quantity) {
                throw new StockUnavailableException("Existencia insuficiente para " + product.getName() + ".");
            }
            product.setStock(product.getStock() - quantity);
            OrderItem line = new OrderItem(product, quantity, product.getPrice());
            order.addItem(line);
            total = total.add(line.getSubtotal());
        }
        order.setTotal(total);
        return orders.save(order);
    }

    @Transactional
    public SalesOrder changeStatus(Long id, OrderStatus nextStatus) {
        SalesOrder order = orders.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.CANCELLED && nextStatus != OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Un pedido cancelado no puede reabrirse.");
        }
        if (nextStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.CANCELLED) {
            if (currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
                throw new IllegalArgumentException("No se puede cancelar un pedido enviado o entregado.");
            }
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
            }
        }
        order.setStatus(nextStatus);
        return orders.save(order);
    }
}