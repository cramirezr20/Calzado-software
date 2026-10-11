package gt.calzado.erp.web;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class AdminNavigationAdvice {

    @ModelAttribute
    public void addNavigationPermissions(Model model, Authentication authentication) {
        Set<String> roles = authentication == null ? Set.of()
                : authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .collect(Collectors.toSet());
        boolean admin = roles.contains("ROLE_ADMIN");
        model.addAttribute("canViewDashboard", admin || roles.contains("ROLE_ANALYST"));
        model.addAttribute("canViewProducts", admin || roles.contains("ROLE_WAREHOUSE"));
        model.addAttribute("canViewOrders", admin || roles.contains("ROLE_SALES")
                || roles.contains("ROLE_WAREHOUSE") || roles.contains("ROLE_ANALYST"));
        model.addAttribute("canViewCustomers", admin || roles.contains("ROLE_SALES")
                || roles.contains("ROLE_SUPPORT") || roles.contains("ROLE_ANALYST"));
        model.addAttribute("canViewComplaints", admin || roles.contains("ROLE_SUPPORT"));
        model.addAttribute("canManageUsers", admin);
    }
}