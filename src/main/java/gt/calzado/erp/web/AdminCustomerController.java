package gt.calzado.erp.web;

import gt.calzado.erp.repository.CustomerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminCustomerController {

    private final CustomerRepository customers;

    public AdminCustomerController(CustomerRepository customers) {
        this.customers = customers;
    }

    @GetMapping("/admin/clientes")
    public String list(Model model) {
        model.addAttribute("customers", customers.findAllByOrderByFullNameAsc());
        return "admin/customers";
    }
}