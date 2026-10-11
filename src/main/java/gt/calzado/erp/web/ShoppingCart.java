package gt.calzado.erp.web;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShoppingCart implements Serializable {

    private final Map<Long, Integer> items = new LinkedHashMap<>();

    public Map<Long, Integer> getItems() { return items; }

    public int getItemCount() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void add(Long productId, int quantity) {
        items.merge(productId, quantity, Integer::sum);
    }

    public void remove(Long productId) {
        items.remove(productId);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}