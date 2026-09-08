package com.tsw.service;

import com.tsw.dto.CartItemResponse;
import com.tsw.dto.CategoryResponse;
import com.tsw.dto.ProductImageResponse;
import com.tsw.dto.ProductResponse;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.InsufficientStockException;
import com.tsw.exception.InvalidQuantityException;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.CartItem;
import com.tsw.model.Category;
import com.tsw.model.Client;
import com.tsw.model.Product;
import com.tsw.model.ProductImage;
import com.tsw.model.ShoppingCart;
import com.tsw.repository.CartItemRepository;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.repository.ShoppingCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public ShoppingCartService(ShoppingCartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               ClientRepository clientRepository,
                               ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> getItems(UUID clientId) {
        return findCart(clientId)
                .map(cart -> cartItemRepository.findByCartId(cart.getId()))
                .orElse(List.of())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CartItemResponse addItem(UUID clientId, UUID productId, int qty) {
        validateQuantity(qty);
        Client client = findClientForUpdate(clientId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.PRODUCT_NOT_FOUND,
                        "Nie znaleziono produktu"
                ));
        ShoppingCart cart = getOrCreateCart(client);

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            long newQty = (long) item.getQty() + qty;
            if (newQty > product.getQtyInStock()) {
                throw new InsufficientStockException("Niewystarczająca ilość produktu w magazynie");
            }
            item.setQty((int) newQty);
            return toResponse(cartItemRepository.save(item));
        }

        if (qty > product.getQtyInStock()) {
            throw new InsufficientStockException("Niewystarczająca ilość produktu w magazynie");
        }

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQty(qty);
        return toResponse(cartItemRepository.save(item));
    }

    @Transactional
    public void removeItem(UUID clientId, UUID itemId) {
        Client client = findClientForUpdate(clientId);
        ShoppingCart cart = findCart(client.getId())
                .orElseThrow(this::cartItemNotFound);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(this::cartItemNotFound);
        cartItemRepository.delete(item);
    }

    @Transactional
    public CartItemResponse updateItemQty(UUID clientId, UUID itemId, int qty) {
        validateQuantity(qty);
        Client client = findClientForUpdate(clientId);
        ShoppingCart cart = findCart(client.getId())
                .orElseThrow(this::cartItemNotFound);
        CartItem item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(this::cartItemNotFound);

        if (qty > item.getProduct().getQtyInStock()) {
            throw new InsufficientStockException("Niewystarczająca ilość produktu w magazynie");
        }

        item.setQty(qty);
        return toResponse(cartItemRepository.save(item));
    }

    @Transactional
    public void clearCart(UUID clientId) {
        Client client = findClientForUpdate(clientId);
        findCart(client.getId())
                .ifPresent(cart -> cartItemRepository.deleteAllByCartId(cart.getId()));
    }

    private Optional<ShoppingCart> findCart(UUID clientId) {
        return cartRepository.findByClientId(clientId);
    }

    private ShoppingCart getOrCreateCart(Client client) {
        return findCart(client.getId()).orElseGet(() -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setClient(client);
            return cartRepository.save(cart);
        });
    }

    private Client findClientForUpdate(UUID clientId) {
        return clientRepository.findByIdForUpdate(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.CLIENT_NOT_FOUND,
                        "Nie znaleziono klienta"
                ));
    }

    private void validateQuantity(int qty) {
        if (qty <= 0) {
            throw new InvalidQuantityException("Ilość musi być większa od zera");
        }
    }

    private CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(item.getId(), toResponse(item.getProduct()), item.getQty());
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

    private ResourceNotFoundException cartItemNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.CART_ITEM_NOT_FOUND, "Nie znaleziono pozycji koszyka");
    }
}
