package gt.calzado.erp.web;

import gt.calzado.erp.domain.Product;
import gt.calzado.erp.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminProductController {

    private final ProductRepository products;

    public AdminProductController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping("/admin/productos")
    public String list(Model model) {
        model.addAttribute("products", products.findAllByOrderByNameAsc());
        return "admin/products";
    }

    @GetMapping("/admin/productos/nuevo")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formTitle", "Nuevo producto");
        model.addAttribute("formAction", "/admin/productos/nuevo");
        return "admin/product-form";
    }

    @PostMapping("/admin/productos/nuevo")
    public String create(@Valid @ModelAttribute("product") Product product,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (product.getSku() != null && products.existsBySkuIgnoreCase(product.getSku().trim())) {
            result.rejectValue("sku", "duplicate", "Ese SKU ya está registrado.");
        }
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Nuevo producto");
            model.addAttribute("formAction", "/admin/productos/nuevo");
            return "admin/product-form";
        }
        product.setSku(product.getSku().trim());
        products.save(product);
        redirect.addFlashAttribute("notice", "Producto creado.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/admin/productos/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", getProduct(id));
        model.addAttribute("formTitle", "Editar producto");
        model.addAttribute("formAction", "/admin/productos/" + id + "/editar");
        return "admin/product-form";
    }

    @PostMapping("/admin/productos/{id}/editar")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("product") Product form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        Product current = getProduct(id);
        if (form.getSku() != null && products.existsBySkuIgnoreCaseAndIdNot(form.getSku().trim(), id)) {
            result.rejectValue("sku", "duplicate", "Ese SKU ya está registrado.");
        }
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Editar producto");
            model.addAttribute("formAction", "/admin/productos/" + id + "/editar");
            return "admin/product-form";
        }
        current.setSku(form.getSku().trim());
        current.setName(form.getName().trim());
        current.setCategory(form.getCategory().trim());
        current.setDescription(form.getDescription());
        current.setImageUrl(form.getImageUrl());
        current.setPrice(form.getPrice());
        current.setStock(form.getStock());
        current.setActive(form.isActive());
        products.save(current);
        redirect.addFlashAttribute("notice", "Cambios guardados.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/admin/productos/{id}/desactivar")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirect) {
        Product product = getProduct(id);
        product.setActive(false);
        products.save(product);
        redirect.addFlashAttribute("notice", "Producto desactivado; el historial se conserva.");
        return "redirect:/admin/productos";
    }

    private Product getProduct(Long id) {
        return products.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}