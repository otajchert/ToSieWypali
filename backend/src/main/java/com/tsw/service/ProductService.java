package com.tsw.service;

import com.tsw.dto.ProductRequest;
import com.tsw.model.Category;
import com.tsw.model.Product;
import com.tsw.model.ProductImage;
import com.tsw.repository.CategoryRepository;
import com.tsw.repository.ProductImageRepository;
import com.tsw.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    public Product create(ProductRequest req) {
        Product product = new Product();
        applyRequest(product, req);
        return productRepository.save(product);
    }

    public Optional<Product> update(UUID id, ProductRequest req) {
        return productRepository.findById(id).map(product -> {
            applyRequest(product, req);
            return productRepository.save(product);
        });
    }

    public boolean delete(UUID id) {
        return productRepository.findById(id).map(product -> {
            // delete all image files from disk before removing the entity
            for (ProductImage img : product.getImages()) {
                deleteFile(img.getImageUrl());
            }
            if (product.getPhoto() != null) {
                deleteFile(product.getPhoto());
            }
            productRepository.delete(product);
            return true;
        }).orElse(false);
    }

    public ProductImage addGalleryImage(UUID productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String relativePath = saveFile(file, "products");

        int nextOrder = product.getImages().stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        ProductImage image = new ProductImage();
        image.setImageUrl(relativePath);
        image.setSortOrder(nextOrder);
        image.setProduct(product);

        return productImageRepository.save(image);
    }

    public boolean removeGalleryImage(UUID productId, UUID imageId) throws IOException {
        return productImageRepository.findById(imageId).map(image -> {
            if (!image.getProduct().getId().equals(productId)) {
                return false;
            }
            deleteFile(image.getImageUrl());
            productImageRepository.delete(image);
            return true;
        }).orElse(false);
    }

    public Product setMainPhoto(UUID productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // remove old main photo file if it exists
        if (product.getPhoto() != null) {
            deleteFile(product.getPhoto());
        }

        String relativePath = saveFile(file, "products");
        product.setPhoto(relativePath);
        return productRepository.save(product);
    }

    // copies fields from the request onto the product entity
    private void applyRequest(Product product, ProductRequest req) {
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setQtyInStock(req.getQtyInStock());
        product.setSku(req.getSku());
        product.setMaterial(req.getMaterial());
        product.setHeight(req.getHeight());
        product.setWidth(req.getWidth());
        product.setProductLength(req.getProductLength());

        List<Category> categories = req.getCategoryIds().stream()
                .map(categoryId -> categoryRepository.findById(categoryId).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toList());
        product.setCategories(categories);
    }

    // saves uploaded file to {uploadDir}/{subdir}/{uuid}.{ext} and returns the relative path
    private String saveFile(MultipartFile file, String subdir) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";

        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, subdir);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename));

        return subdir + "/" + filename;
    }

    private void deleteFile(String relativePath) {
        try {
            Path path = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // log but don't fail the request if the file is already gone
            System.err.println("Could not delete file: " + relativePath + " - " + e.getMessage());
        }
    }
}
