package gt.calzado.erp.repository;

import gt.calzado.erp.domain.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    java.util.Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCaseAndIdNot(String sku, Long id);

    List<Product> findAllByOrderByNameAsc();

    List<Product> findByActiveTrueOrderByNameAsc();

        List<Product> findByActiveTrueAndCategoryIgnoreCaseOrderByNameAsc(String category);

        List<Product> findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAsc(String name);

        List<Product> findByActiveTrueAndCategoryIgnoreCaseAndNameContainingIgnoreCaseOrderByNameAsc(
            String category, String name);

    Optional<Product> findByIdAndActiveTrue(Long id);

        @Query("select distinct p.category from Product p where p.active = true order by p.category")
        List<String> findActiveCategories();

        long countByActiveTrue();

        long countByActiveTrueAndStockLessThan(int stock);
}