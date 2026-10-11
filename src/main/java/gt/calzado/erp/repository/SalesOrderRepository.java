package gt.calzado.erp.repository;

import gt.calzado.erp.domain.OrderStatus;
import gt.calzado.erp.domain.SalesOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    @EntityGraph(attributePaths = "customer")
    List<SalesOrder> findAllByOrderByCreatedAtDesc();

    Optional<SalesOrder> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    long countByStatus(OrderStatus status);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThanAndStatusNot(
            Instant start, Instant end, OrderStatus excludedStatus);

    @Query("select coalesce(sum(o.total), 0) from SalesOrder o "
            + "where o.createdAt >= :start and o.createdAt < :end and o.status <> :excludedStatus")
    BigDecimal sumSalesBetween(@Param("start") Instant start, @Param("end") Instant end,
                               @Param("excludedStatus") OrderStatus excludedStatus);
}