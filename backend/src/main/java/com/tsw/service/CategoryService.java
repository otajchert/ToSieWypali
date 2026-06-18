package com.tsw.service;

import com.tsw.dto.CategoryRequest;
import com.tsw.model.Category;
import com.tsw.model.Product;
import com.tsw.repository.CategoryRepository;
import com.tsw.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id);
    }

    public Category create(CategoryRequest req) {
        Category category = new Category();
        applyRequest(category, req);
        return categoryRepository.save(category);
    }

    public Optional<Category> update(UUID id, CategoryRequest req) {
        return categoryRepository.findById(id).map(category -> {
            applyRequest(category, req);
            return categoryRepository.save(category);
        });
    }

    public long countProducts(UUID id) {
        return productRepository.countByCategoryId(id);
    }

    @Transactional
    public boolean delete(UUID id, boolean force) {
        return categoryRepository.findById(id).map(category -> {
            if (force) {
                List<Product> affected = productRepository.findByCategoryId(id);
                for (Product p : affected) {
                    p.getCategories().remove(category);
                    productRepository.save(p);
                }
            }
            categoryRepository.delete(category);
            return true;
        }).orElse(false);
    }

    private void applyRequest(Category category, CategoryRequest req) {
        category.setCategoryName(req.getCategoryName());

        if (req.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(req.getParentCategoryId()).orElse(null);
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }
    }
}
