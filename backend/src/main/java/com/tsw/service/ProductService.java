package com.tsw.service;

import com.tsw.dto.CategoryResponse;
import com.tsw.dto.ProductImageResponse;
import com.tsw.dto.ProductRequest;
import com.tsw.dto.ProductResponse;
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
import com.tsw.storage.FileStorage;
import com.tsw.storage.FileUpload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FileStorage fileStorage;

    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          CategoryRepository categoryRepository,
                          CartItemRepository cartItemRepository,
                          OrderProductRepository orderProductRepository,
                          FileStorage fileStorage) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.categoryRepository = categoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderProductRepository = orderProductRepository;
        this.fileStorage = fileStorage;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        return toResponse(getProduct(id));
    }

    public ProductResponse create(ProductRequest req) {
        Product product = new Product();
        applyRequest(product, req);
        product.setSku("TSW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return toResponse(productRepository.save(product));
    }

    public ProductResponse update(UUID id, ProductRequest req) {
        Product product = getProduct(id);
        applyRequest(product, req);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getProduct(id);
        if (orderProductRepository.existsByIdProductId(id)) {
            throw new ProductInUseException();
        }
        cartItemRepository.deleteByProductId(id);
        for (ProductImage image : product.getImages()) {
            fileStorage.delete(image.getImageUrl());
        }
        if (product.getPhoto() != null) {
            fileStorage.delete(product.getPhoto());
        }
        productRepository.delete(product);
    }

    public ProductImageResponse addGalleryImage(UUID productId, FileUpload file) {
        Product product = getProduct(productId);

        String publicUrl = saveFile(file, "products");

        int nextOrder = product.getImages().stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        ProductImage image = new ProductImage();
        image.setImageUrl(publicUrl);
        image.setSortOrder(nextOrder);
        image.setProduct(product);

        return toResponse(productImageRepository.save(image));
    }

    public void removeGalleryImage(UUID productId, UUID imageId) {
        getProduct(productId);
        ProductImage image = getProductImage(productId, imageId);
        fileStorage.delete(image.getImageUrl());
        productImageRepository.delete(image);
    }

    public ProductResponse linkPhotoUrl(UUID productId, String url) {
        Product product = getProduct(productId);
        product.setPhoto(url);
        return toResponse(productRepository.save(product));
    }

    public ProductImageResponse linkGalleryUrl(UUID productId, String url) {
        Product product = getProduct(productId);
        int nextOrder = product.getImages().stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(-1) + 1;
        ProductImage image = new ProductImage();
        image.setImageUrl(url);
        image.setSortOrder(nextOrder);
        image.setProduct(product);
        return toResponse(productImageRepository.save(image));
    }

    public ProductResponse setImageAsMain(UUID productId, UUID imageId) {
        Product product = getProduct(productId);
        ProductImage image = getProductImage(productId, imageId);
        product.setPhoto(image.getImageUrl());
        return toResponse(productRepository.save(product));
    }

    public ProductResponse setMainPhoto(UUID productId, FileUpload file) {
        Product product = getProduct(productId);

        if (product.getPhoto() != null) {
            fileStorage.delete(product.getPhoto());
        }

        String publicUrl = saveFile(file, "products");
        product.setPhoto(publicUrl);
        return toResponse(productRepository.save(product));
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

    private String saveFile(FileUpload file, String subdir) {
        String originalName = file.originalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : "";
        String objectPath = subdir + "/" + UUID.randomUUID() + ext;
        return fileStorage.upload(file, objectPath);
    }

    private Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(this::productNotFound);
    }

    private ProductResponse toResponse(Product product) {
        Set<CategoryResponse> categories = new LinkedHashSet<>();
        for (Category category : product.getCategories()) {
            categories.add(toResponse(category));
        }

        List<ProductImageResponse> images = product.getImages().stream()
                .map(this::toResponse)
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPhoto(),
                product.getQtyInStock(),
                product.getSku(),
                product.getPrice(),
                product.getWeight(),
                product.getHeight(),
                product.getWidth(),
                product.getProductLength(),
                categories,
                images
        );
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getCategoryName());
    }

    private ProductImageResponse toResponse(ProductImage image) {
        return new ProductImageResponse(image.getId(), image.getImageUrl(), image.getSortOrder());
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
