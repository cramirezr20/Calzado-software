package gt.calzado.erp.web;

import gt.calzado.erp.domain.ComplaintStatus;
import gt.calzado.erp.domain.OrderStatus;
import gt.calzado.erp.repository.ComplaintRepository;
import gt.calzado.erp.repository.ProductRepository;
import gt.calzado.erp.repository.SalesOrderRepository;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ProductRepository products;
    private final SalesOrderRepository orders;
    private final ComplaintRepository complaints;

    public DashboardController(ProductRepository products, SalesOrderRepository orders,
                               ComplaintRepository complaints) {
        this.products = products;
        this.orders = orders;
        this.complaints = complaints;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("activeProducts", products.countByActiveTrue());
        model.addAttribute("lowStock", products.countByActiveTrueAndStockLessThan(5));
        model.addAttribute("pendingOrders", orders.countByStatus(OrderStatus.RECEIVED));
        model.addAttribute("openComplaints", complaints.countByStatusIn(
                List.of(ComplaintStatus.OPEN, ComplaintStatus.IN_PROGRESS)));
        return "admin/dashboard";
    }
}