package com.shopsphere.repository;
import com.shopsphere.model.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface OrderRepository extends JpaRepository<CustomerOrder,Long> {
 @EntityGraph(attributePaths="items")
 Optional<CustomerOrder> findByIdAndEmailIgnoreCaseAndAccessTokenHash(Long id,String email,String accessTokenHash);
}
