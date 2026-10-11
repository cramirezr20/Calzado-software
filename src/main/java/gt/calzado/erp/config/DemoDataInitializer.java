package gt.calzado.erp.config;

import gt.calzado.erp.domain.AppUser;
import gt.calzado.erp.domain.Complaint;
import gt.calzado.erp.domain.ComplaintStatus;
import gt.calzado.erp.domain.Customer;
import gt.calzado.erp.domain.OrderItem;
import gt.calzado.erp.domain.OrderStatus;
import gt.calzado.erp.domain.Product;
import gt.calzado.erp.domain.SalesOrder;
import gt.calzado.erp.domain.UserRole;
import gt.calzado.erp.repository.AppUserRepository;
import gt.calzado.erp.repository.ComplaintRepository;
import gt.calzado.erp.repository.CustomerRepository;
import gt.calzado.erp.repository.ProductRepository;
import gt.calzado.erp.repository.SalesOrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataInitializer implements ApplicationRunner {

    private final AppUserRepository users;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final SalesOrderRepository orders;
    private final ComplaintRepository complaints;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;

    public DemoDataInitializer(AppUserRepository users, ProductRepository products,
                               CustomerRepository customers, SalesOrderRepository orders,
                               ComplaintRepository complaints, PasswordEncoder passwordEncoder,
                               @Value("${app.demo-data.enabled:false}") boolean enabled) {
        this.users = users;
        this.products = products;
        this.customers = customers;
        this.orders = orders;
        this.complaints = complaints;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        seedUsers();
        seedProducts();
        seedCustomers();
        seedOrders();
        seedComplaints();
    }

    private void seedUsers() {
        createUserIfMissing("ventas.demo", "Ventas Demo", "ventas.demo@calzado.local", UserRole.SALES);
        createUserIfMissing("bodega.demo", "Bodega Demo", "bodega.demo@calzado.local", UserRole.WAREHOUSE);
        createUserIfMissing("soporte.demo", "Soporte Demo", "soporte.demo@calzado.local", UserRole.SUPPORT);
        createUserIfMissing("analista.demo", "Analista Demo", "analista.demo@calzado.local", UserRole.ANALYST);
    }

    private void createUserIfMissing(String username, String fullName, String email, UserRole role) {
        if (users.existsByUsernameIgnoreCase(username)) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Demo1234"));
        user.setRole(role);
        users.save(user);
    }

    private void seedProducts() {
        createProductIfMissing("PASO-DEMO-001", "Tenis Ruta Claro", "Tenis",
                "Ligero y versátil para el uso diario.", "349.00", 18,
                "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=1000&q=85");
        createProductIfMissing("PASO-DEMO-002", "Botín Sendero", "Botines",
                "Botín de perfil clásico para clima fresco.", "589.00", 12,
                "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?auto=format&fit=crop&w=1000&q=85");
        createProductIfMissing("PASO-DEMO-003", "Sandalia Brisa", "Sandalias",
                "Sandalia cómoda para días cálidos.", "219.00", 25,
                "https://images.unsplash.com/photo-1777979926381-46ffc1395cf8?auto=format&fit=crop&w=1000&q=85");
        createProductIfMissing("PASO-DEMO-004", "Zapato Nube", "Formal",
                "Zapato formal de acabado sobrio.", "429.00", 9,
                "https://images.unsplash.com/photo-1552422554-0d5af0c79fc6?auto=format&fit=crop&w=1000&q=85");
        createProductIfMissing("PASO-DEMO-005", "Tenis Campo Verde", "Deportivos",
                "Tenis de entrenamiento con unidades limitadas.", "379.00", 2,
                "https://images.unsplash.com/photo-1560769629-975ec94e6a86?auto=format&fit=crop&w=1000&q=85");
    }

    private void createProductIfMissing(String sku, String name, String category,
                                        String description, String price, int stock, String imageUrl) {
        Product product = products.findBySkuIgnoreCase(sku).orElse(null);
        if (product == null) {
            product = new Product();
            product.setSku(sku);
            product.setName(name);
            product.setCategory(category);
            product.setDescription(description);
            product.setPrice(new BigDecimal(price));
            product.setStock(stock);
            product.setActive(true);
        }
        if (product.getImageUrl() == null || product.getImageUrl().isBlank()) {
            product.setImageUrl(imageUrl);
            products.save(product);
        }
    }

    private void seedCustomers() {
        createCustomerIfMissing("Ana López", "ana.lopez@example.test", "5555-0101",
                "Zona 1, Ciudad de Guatemala");
        createCustomerIfMissing("Carlos Méndez", "carlos.mendez@example.test", "5555-0102",
                "Zona 10, Ciudad de Guatemala");
        createCustomerIfMissing("Lucía Morales", "lucia.morales@example.test", "5555-0103",
                "Mixco, Guatemala");
        createCustomerIfMissing("Roberto García", "roberto.garcia@example.test", "5555-0104",
                "Antigua Guatemala, Sacatepéquez");
    }

    private void createCustomerIfMissing(String name, String email, String phone, String address) {
        if (customers.findByEmailIgnoreCase(email).isPresent()) {
            return;
        }
        Customer customer = new Customer();
        customer.setFullName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setShippingAddress(address);
        customers.save(customer);
    }

    private void seedOrders() {
        createOrderIfMissing("PASO-DEMO-1001", "ana.lopez@example.test", OrderStatus.RECEIVED, 1,
                new DemoLine("PASO-DEMO-001", 2));
        createOrderIfMissing("PASO-DEMO-1002", "roberto.garcia@example.test", OrderStatus.PREPARING, 2,
                new DemoLine("PASO-DEMO-002", 1));
        createOrderIfMissing("PASO-DEMO-1003", "lucia.morales@example.test", OrderStatus.SHIPPED, 3,
                new DemoLine("PASO-DEMO-003", 1), new DemoLine("PASO-DEMO-001", 1));
        createOrderIfMissing("PASO-DEMO-1004", "carlos.mendez@example.test", OrderStatus.DELIVERED, 5,
                new DemoLine("PASO-DEMO-004", 1));
        createTodayOrderIfMissing();
    }

    private void createTodayOrderIfMissing() {
        String orderNumber = "PASO-DEMO-1005";
        if (orders.existsByOrderNumber(orderNumber)) {
            return;
        }
        Customer customer = customers.findByEmailIgnoreCase("ana.lopez@example.test").orElseThrow();
        Product product = products.findBySkuIgnoreCase("PASO-DEMO-001").orElseThrow();
        product.setStock(product.getStock() - 1);
        SalesOrder order = new SalesOrder();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.RECEIVED);
        order.setCreatedAt(Instant.now());
        order.addItem(new OrderItem(product, 1, product.getPrice()));
        order.setTotal(product.getPrice());
        orders.save(order);
    }

    private void createOrderIfMissing(String orderNumber, String customerEmail,
                                      OrderStatus status, int daysAgo, DemoLine... lines) {
        if (orders.existsByOrderNumber(orderNumber)) {
            return;
        }
        Customer customer = customers.findByEmailIgnoreCase(customerEmail).orElseThrow();
        SalesOrder order = new SalesOrder();
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setStatus(status);
        order.setCreatedAt(Instant.now().minus(daysAgo, ChronoUnit.DAYS));

        BigDecimal total = BigDecimal.ZERO;
        for (DemoLine line : lines) {
            Product product = products.findBySkuIgnoreCase(line.sku()).orElseThrow();
            product.setStock(product.getStock() - line.quantity());
            OrderItem item = new OrderItem(product, line.quantity(), product.getPrice());
            order.addItem(item);
            total = total.add(item.getSubtotal());
        }
        order.setTotal(total);
        orders.save(order);
    }

    private void seedComplaints() {
        createComplaintIfMissing("PASO-DEMO-1001", "Ana López", "ana.lopez@example.test",
                "Quisiera confirmar el horario estimado de entrega.", ComplaintStatus.OPEN);
        createComplaintIfMissing("PASO-DEMO-1003", "Lucía Morales", "lucia.morales@example.test",
                "El paquete llegó con la caja exterior dañada; el producto está bien.", ComplaintStatus.IN_PROGRESS);
        createComplaintIfMissing("PASO-DEMO-1004", "Carlos Méndez", "carlos.mendez@example.test",
                "Gracias por la atención, el cambio de talla quedó resuelto.", ComplaintStatus.RESOLVED);
    }

    private void createComplaintIfMissing(String orderNumber, String name, String email,
                                          String message, ComplaintStatus status) {
        if (complaints.existsByOrderNumberAndCustomerEmail(orderNumber, email)) {
            return;
        }
        Complaint complaint = new Complaint();
        complaint.setOrderNumber(orderNumber);
        complaint.setCustomerName(name);
        complaint.setCustomerEmail(email);
        complaint.setMessage(message);
        complaint.setStatus(status);
        complaints.save(complaint);
    }

    private record DemoLine(String sku, int quantity) {
    }
}