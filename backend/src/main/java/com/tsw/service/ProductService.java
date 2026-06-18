package com.tsw.service;

import com.tsw.dto.ProductRequest;
import com.tsw.model.Category;
import com.tsw.model.Product;
import com.tsw.model.ProductImage;
import com.tsw.repository.CartItemRepository;
import com.tsw.repository.CategoryRepository;
import com.tsw.repository.OrderProductRepository;
import com.tsw.repository.ProductImageRepository;
import com.tsw.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderProductRepository orderProductRepository;
    private final SupabaseStorageService storageService;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          CategoryRepository categoryRepository,
                          CartItemRepository cartItemRepository,
                          OrderProductRepository orderProductRepository,
                          SupabaseStorageService storageService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderProductRepository = orderProductRepository;
        this.storageService = storageService;
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
        product.setSku("TSW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return productRepository.save(product);
    }

    public Optional<Product> update(UUID id, ProductRequest req) {
        return productRepository.findById(id).map(product -> {
            applyRequest(product, req);
            return productRepository.save(product);
        });
    }

    @Transactional
    public boolean delete(UUID id) {
        return productRepository.findById(id).map(product -> {
            if (orderProductRepository.existsByIdProductId(id)) {
                throw new IllegalStateException("Produkt jest częścią istniejących zamówień i nie może zostać usunięty.");
            }
            cartItemRepository.deleteByProductId(id);
            for (ProductImage img : product.getImages()) {
                storageService.delete(img.getImageUrl());
            }
            if (product.getPhoto() != null) {
                storageService.delete(product.getPhoto());
            }
            productRepository.delete(product);
            return true;
        }).orElse(false);
    }

    public ProductImage addGalleryImage(UUID productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        String publicUrl = saveFile(file, "products");

        int nextOrder = product.getImages().stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        ProductImage image = new ProductImage();
        image.setImageUrl(publicUrl);
        image.setSortOrder(nextOrder);
        image.setProduct(product);

        return productImageRepository.save(image);
    }

    public boolean removeGalleryImage(UUID productId, UUID imageId) {
        return productImageRepository.findById(imageId).map(image -> {
            if (!image.getProduct().getId().equals(productId)) {
                return false;
            }
            storageService.delete(image.getImageUrl());
            productImageRepository.delete(image);
            return true;
        }).orElse(false);
    }

    public Optional<Product> linkPhotoUrl(UUID productId, String url) {
        return productRepository.findById(productId).map(product -> {
            product.setPhoto(url);
            return productRepository.save(product);
        });
    }

    public Optional<ProductImage> linkGalleryUrl(UUID productId, String url) {
        return productRepository.findById(productId).map(product -> {
            int nextOrder = product.getImages().stream()
                    .mapToInt(ProductImage::getSortOrder)
                    .max()
                    .orElse(-1) + 1;
            ProductImage image = new ProductImage();
            image.setImageUrl(url);
            image.setSortOrder(nextOrder);
            image.setProduct(product);
            return productImageRepository.save(image);
        });
    }

    public Optional<Product> setImageAsMain(UUID productId, UUID imageId) {
        return productRepository.findById(productId).map(product -> {
            productImageRepository.findById(imageId).ifPresent(image -> {
                if (image.getProduct().getId().equals(productId)) {
                    product.setPhoto(image.getImageUrl());
                    productRepository.save(product);
                }
            });
            return product;
        });
    }

    public Product setMainPhoto(UUID productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getPhoto() != null) {
            storageService.delete(product.getPhoto());
        }

        String publicUrl = saveFile(file, "products");
        product.setPhoto(publicUrl);
        return productRepository.save(product);
    }

    private void applyRequest(Product product, ProductRequest req) {
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        product.setQtyInStock(req.getQtyInStock());
        product.setWeight(req.getWeight());
        product.setHeight(req.getHeight());
        product.setWidth(req.getWidth());
        product.setProductLength(req.getProductLength());

        Set<Category> categories = req.getCategoryIds().stream()
                .map(categoryId -> categoryRepository.findById(categoryId).orElse(null))
                .filter(c -> c != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        product.setCategories(categories);
    }

    // uploads file to Supabase and returns the public URL stored in the database
    private String saveFile(MultipartFile file, String subdir) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        String objectPath = subdir + "/" + UUID.randomUUID() + ext;
        return storageService.upload(file, objectPath);
    }
}
