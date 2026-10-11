package gt.calzado.erp.repository;

import gt.calzado.erp.domain.OrderStatus;
import gt.calzado.erp.domain.SalesOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @EntityGraph(attributePaths = "customer")
    List<SalesOrder> findAllByOrderByCreatedAtDesc();

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    long countByStatus(OrderStatus status);
}