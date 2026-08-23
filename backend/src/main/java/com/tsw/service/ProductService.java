package com.tsw.service;

import com.tsw.dto.ProductRequest;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.ProductInUseException;
import com.tsw.exception.ResourceNotFoundException;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(this::productNotFound);
    }

    public Product create(ProductRequest req) {
        Product product = new Product();
        applyRequest(product, req);
        product.setSku("TSW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return productRepository.save(product);
    }

    public Product update(UUID id, ProductRequest req) {
        Product product = getById(id);
        applyRequest(product, req);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getById(id);
        if (orderProductRepository.existsByIdProductId(id)) {
            throw new ProductInUseException();
        }
        cartItemRepository.deleteByProductId(id);
        for (ProductImage image : product.getImages()) {
            storageService.delete(image.getImageUrl());
        }
        if (product.getPhoto() != null) {
            storageService.delete(product.getPhoto());
        }
        productRepository.delete(product);
    }

    public ProductImage addGalleryImage(UUID productId, MultipartFile file) {
        Product product = getById(productId);

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

    public void removeGalleryImage(UUID productId, UUID imageId) {
        getById(productId);
        ProductImage image = getProductImage(productId, imageId);
        storageService.delete(image.getImageUrl());
        productImageRepository.delete(image);
    }

    public Product linkPhotoUrl(UUID productId, String url) {
        Product product = getById(productId);
        product.setPhoto(url);
        return productRepository.save(product);
    }

    public ProductImage linkGalleryUrl(UUID productId, String url) {
        Product product = getById(productId);
        int nextOrder = product.getImages().stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;
        ProductImage image = new ProductImage();
        image.setImageUrl(url);
        image.setSortOrder(nextOrder);
        image.setProduct(product);
        return productImageRepository.save(image);
    }

    public Product setImageAsMain(UUID productId, UUID imageId) {
        Product product = getById(productId);
        ProductImage image = getProductImage(productId, imageId);
        product.setPhoto(image.getImageUrl());
        return productRepository.save(product);
    }

    public Product setMainPhoto(UUID productId, MultipartFile file) {
        Product product = getById(productId);

        if (product.getPhoto() != null) {
            storageService.delete(product.getPhoto());
        }

        String publicUrl = saveFile(file, "products");
        product.setPhoto(publicUrl);
        return productRepository.save(product);
    }

    private void applyRequest(Product product, ProductRequest req) {
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setQtyInStock(req.qtyInStock());
        product.setWeight(req.weight());
        product.setHeight(req.height());
        product.setWidth(req.width());
        product.setProductLength(req.productLength());

        Set<Category> categories = new LinkedHashSet<>();
        for (UUID categoryId : req.categoryIds()) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            ApiErrorCode.CATEGORY_NOT_FOUND,
                            "Nie znaleziono kategorii"
                    ));
            categories.add(category);
        }
        product.setCategories(categories);
    }

    private String saveFile(MultipartFile file, String subdir) {
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        String objectPath = subdir + "/" + UUID.randomUUID() + ext;
        return storageService.upload(file, objectPath);
    }

    private ProductImage getProductImage(UUID productId, UUID imageId) {
        return productImageRepository.findById(imageId)
                .filter(image -> image.getProduct().getId().equals(productId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.PRODUCT_IMAGE_NOT_FOUND,
                        "Nie znaleziono zdjęcia produktu"
                ));
    }

    private ResourceNotFoundException productNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.PRODUCT_NOT_FOUND, "Nie znaleziono produktu");
    }
}
