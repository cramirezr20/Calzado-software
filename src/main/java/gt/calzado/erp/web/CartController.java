package gt.calzado.erp.web;

import gt.calzado.erp.domain.Product;
import gt.calzado.erp.repository.ProductRepository;
import gt.calzado.erp.repository.SalesOrderRepository;
import gt.calzado.erp.service.OrderService;
import gt.calzado.erp.service.StockUnavailableException;
import gt.calzado.erp.web.form.CheckoutForm;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@SessionAttributes("shoppingCart")
public class CartController {

    private final ProductRepository products;
    private final OrderService orderService;
    private final SalesOrderRepository orders;

    public CartController(ProductRepository products, OrderService orderService, SalesOrderRepository orders) {
        this.products = products;
        this.orderService = orderService;
        this.orders = orders;
    }

    @ModelAttribute("shoppingCart")
    public ShoppingCart shoppingCart() {
        return new ShoppingCart();
    }

    @PostMapping("/carrito/agregar/{id}")
    public String add(@PathVariable Long id, @RequestParam(defaultValue = "1") int quantity,
                      @ModelAttribute("shoppingCart") ShoppingCart cart, RedirectAttributes redirect) {
        Product product = products.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        int requested = cart.getItems().getOrDefault(id, 0) + quantity;
        if (quantity < 1 || requested > product.getStock()) {
            redirect.addFlashAttribute("cartError", "No hay suficientes pares disponibles.");
            return "redirect:/tienda/productos/" + id;
        }
        cart.add(id, quantity);
        redirect.addFlashAttribute("notice", "Producto agregado a tu bolsa.");
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/{id}/quitar")
    public String remove(@PathVariable Long id, @ModelAttribute("shoppingCart") ShoppingCart cart) {
        cart.remove(id);
        return "redirect:/carrito";
    }

    @GetMapping("/carrito")
    public String viewCart(@ModelAttribute("shoppingCart") ShoppingCart cart, Model model) {
        addCartModel(cart, model);
        return "storefront/cart";
    }

    @GetMapping("/checkout")
    public String checkout(@ModelAttribute("shoppingCart") ShoppingCart cart, Model model) {
        if (cart.isEmpty()) {
            return "redirect:/carrito";
        }
        model.addAttribute("checkoutForm", new CheckoutForm());
        addCartModel(cart, model);
        return "storefront/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                             BindingResult result,
                             @ModelAttribute("shoppingCart") ShoppingCart cart,
                             Model model, SessionStatus sessionStatus) {
        if (cart.isEmpty()) {
            return "redirect:/carrito";
        }
        if (result.hasErrors()) {
            addCartModel(cart, model);
            return "storefront/checkout";
        }
        try {
            var order = orderService.placeOrder(cart, form);
            sessionStatus.setComplete();
            return "redirect:/pedido/confirmacion/" + order.getOrderNumber();
        } catch (StockUnavailableException exception) {
            result.reject("stock", exception.getMessage());
            addCartModel(cart, model);
            return "storefront/checkout";
        }
    }

    @GetMapping("/pedido/confirmacion/{orderNumber}")
    public String confirmation(@PathVariable String orderNumber, Model model) {
        var order = orders.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("order", order);
        return "storefront/order-confirmation";
    }

    private void addCartModel(ShoppingCart cart, Model model) {
        List<CartLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var item : cart.getItems().entrySet()) {
            products.findByIdAndActiveTrue(item.getKey()).ifPresent(product -> {
                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getValue()));
                lines.add(new CartLine(product, item.getValue(), subtotal));
            });
        }
        for (CartLine line : lines) {
            total = total.add(line.subtotal());
        }
        model.addAttribute("cartLines", lines);
        model.addAttribute("cartTotal", total);
    }
}