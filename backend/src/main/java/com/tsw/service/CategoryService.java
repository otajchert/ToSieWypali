package com.tsw.service;

import com.tsw.dto.CategoryRequest;
import com.tsw.model.Category;
import com.tsw.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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

    public boolean delete(UUID id) {
        return categoryRepository.findById(id).map(category -> {
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
