package com.tsw.repository;

import com.tsw.model.ShippingMethod;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, UUID> {
    List<ShippingMethod> findAllByActiveTrue(Sort sort);

    Optional<ShippingMethod> findByIdAndActiveTrue(UUID id);

    long countByActiveTrue();
}
