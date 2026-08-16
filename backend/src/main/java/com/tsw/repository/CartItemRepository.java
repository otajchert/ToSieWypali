package com.tsw.repository;

import com.tsw.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByCartId(UUID cartId);

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    Optional<CartItem> findByIdAndCartId(UUID id, UUID cartId);

    @Modifying
    @Query("delete from CartItem item where item.cart.id = :cartId")
    void deleteAllByCartId(@Param("cartId") UUID cartId);

    void deleteByProductId(UUID productId);
}
