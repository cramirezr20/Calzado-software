package gt.calzado.erp.web;

import gt.calzado.erp.domain.Product;
import gt.calzado.erp.repository.ProductRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class StorefrontController {

    private final ProductRepository products;

    public StorefrontController(ProductRepository products) {
        this.products = products;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/tienda";
    }

    @GetMapping("/tienda")
    public String catalog(@RequestParam(required = false) String q,
                          @RequestParam(required = false) String category,
                          Model model) {
        String term = q == null || q.isBlank() ? null : q.trim();
        String selectedCategory = category == null || category.isBlank() ? null : category.trim();
        List<Product> catalog;
        if (term != null && selectedCategory != null) {
            catalog = products.findByActiveTrueAndCategoryIgnoreCaseAndNameContainingIgnoreCaseOrderByNameAsc(
                    selectedCategory, term);
        } else if (term != null) {
            catalog = products.findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(term);
        } else if (selectedCategory != null) {
            catalog = products.findByActiveTrueAndCategoryIgnoreCaseOrderByNameAsc(selectedCategory);
        } else {
            catalog = products.findByActiveTrueOrderByNameAsc();
        }
        model.addAttribute("products", catalog);
        model.addAttribute("categories", products.findActiveCategories());
        model.addAttribute("query", term == null ? "" : term);
        model.addAttribute("selectedCategory", selectedCategory == null ? "" : selectedCategory);
        return "storefront/catalog";
    }

    @GetMapping("/tienda/productos/{id}")
    public String product(@PathVariable Long id, Model model) {
        Product product = products.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("product", product);
        return "storefront/product";
    }
}