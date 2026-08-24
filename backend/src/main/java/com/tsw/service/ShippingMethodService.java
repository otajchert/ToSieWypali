package com.tsw.service;

import com.tsw.dto.ShippingMethodRequest;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.LastShippingMethodException;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.ShippingMethod;
import com.tsw.repository.ShippingMethodRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ShippingMethodService {

    private final ShippingMethodRepository shippingMethodRepository;

    public ShippingMethodService(ShippingMethodRepository shippingMethodRepository) {
        this.shippingMethodRepository = shippingMethodRepository;
    }

    @Transactional(readOnly = true)
    public List<ShippingMethod> findAll() {
        Sort order = Sort.by(Sort.Direction.ASC, "name")
                .and(Sort.by(Sort.Direction.ASC, "id"));
        return shippingMethodRepository.findAllByActiveTrue(order);
    }

    @Transactional
    public ShippingMethod create(ShippingMethodRequest request) {
        ShippingMethod shippingMethod = new ShippingMethod();
        shippingMethod.setName(request.name().trim());
        shippingMethod.setPrice(request.price());
        shippingMethod.setActive(true);
        return shippingMethodRepository.save(shippingMethod);
    }

    @Transactional
    public void delete(UUID id) {
        ShippingMethod shippingMethod = shippingMethodRepository.findById(id)
                .orElseThrow(this::shippingMethodNotFound);
        if (!shippingMethod.isActive()) {
            return;
        }
        if (shippingMethodRepository.countByActiveTrue() <= 1) {
            throw new LastShippingMethodException();
        }
        shippingMethod.setActive(false);
        shippingMethodRepository.save(shippingMethod);
    }

    private ResourceNotFoundException shippingMethodNotFound() {
        return new ResourceNotFoundException(
                ApiErrorCode.SHIPPING_METHOD_NOT_FOUND,
                "Nie znaleziono metody dostawy"
        );
    }
}
