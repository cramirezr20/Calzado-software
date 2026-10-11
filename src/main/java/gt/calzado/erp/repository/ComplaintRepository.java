package gt.calzado.erp.repository;

import gt.calzado.erp.domain.Complaint;
import gt.calzado.erp.domain.ComplaintStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findAllByOrderByCreatedAtDesc();

    boolean existsByOrderNumberAndCustomerEmail(String orderNumber, String customerEmail);

    long countByStatusIn(Collection<ComplaintStatus> statuses);
}