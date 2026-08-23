package com.tsw.service;

import com.tsw.dto.CategoryRequest;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.CategoryNotEmptyException;
import com.tsw.exception.InvalidCategoryHierarchyException;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.Category;
import com.tsw.model.Product;
import com.tsw.repository.CategoryRepository;
import com.tsw.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category getById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(this::categoryNotFound);
    }

    @Transactional
    public Category create(CategoryRequest req) {
        Category category = new Category();
        applyRequest(category, req);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(UUID id, CategoryRequest req) {
        Category category = getById(id);
        applyRequest(category, req);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(UUID id, boolean force) {
        Category category = getById(id);
        long productCount = productRepository.countByCategoryId(id);
        if (!force && productCount > 0) {
            throw new CategoryNotEmptyException(productCount);
        }
        if (force) {
            List<Product> affected = productRepository.findByCategoryId(id);
            for (Product product : affected) {
                product.getCategories().remove(category);
                productRepository.save(product);
            }
        }
        categoryRepository.delete(category);
    }

    private void applyRequest(Category category, CategoryRequest req) {
        category.setCategoryName(req.categoryName());

        if (req.parentCategoryId() != null) {
            category.setParentCategory(getValidParent(category, req.parentCategoryId()));
        } else {
            category.setParentCategory(null);
        }
    }

    private ResourceNotFoundException categoryNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.CATEGORY_NOT_FOUND, "Nie znaleziono kategorii");
    }

    private Category getValidParent(Category category, UUID parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.CATEGORY_NOT_FOUND,
                        "Nie znaleziono kategorii nadrzędnej"
                ));
        if (category.getId() == null) {
            return parent;
        }

        Set<UUID> visited = new HashSet<>();
        Category current = parent;
        while (current != null) {
            if (category.getId().equals(current.getId()) || !visited.add(current.getId())) {
                throw new InvalidCategoryHierarchyException("Wybrana kategoria nadrzędna tworzy cykl");
            }
            current = current.getParentCategory();
        }
        return parent;
    }
}
