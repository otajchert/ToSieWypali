package com.tsw.repository;

import com.tsw.model.ShopOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, UUID> {
    List<ShopOrder> findAllByOrderByOrderDateDescIdDesc();

    List<ShopOrder> findByClientId(UUID clientId);

    Optional<ShopOrder> findByIdAndClientId(UUID orderId, UUID clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select shopOrder from ShopOrder shopOrder where shopOrder.id = :orderId")
    Optional<ShopOrder> findByIdForUpdate(@Param("orderId") UUID orderId);
}
