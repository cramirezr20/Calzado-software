package gt.calzado.erp.web;

import gt.calzado.erp.domain.OrderStatus;
import gt.calzado.erp.repository.SalesOrderRepository;
import gt.calzado.erp.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminOrderController {

    private final SalesOrderRepository orders;
    private final OrderService orderService;

    public AdminOrderController(SalesOrderRepository orders, OrderService orderService) {
        this.orders = orders;
        this.orderService = orderService;
    }

    @GetMapping("/admin/pedidos")
    public String list(Model model) {
        model.addAttribute("orders", orders.findAllByOrderByCreatedAtDesc());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders";
    }

    @PostMapping("/admin/pedidos/{id}/estado")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status,
                               RedirectAttributes redirect) {
        try {
            orderService.changeStatus(id, status);
            redirect.addFlashAttribute("notice", "Estado del pedido actualizado.");
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/pedidos";
    }
}