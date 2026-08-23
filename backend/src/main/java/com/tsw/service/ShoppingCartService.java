package com.tsw.service;

import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.InsufficientStockException;
import com.tsw.exception.InvalidQuantityException;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.CartItem;
import com.tsw.model.Client;
import com.tsw.model.Product;
import com.tsw.model.ShoppingCart;
import com.tsw.repository.CartItemRepository;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.repository.ShoppingCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    public List<CartItem> getItems(UUID clientId) {
        return findCart(clientId)
                .map(cart -> cartItemRepository.findByCartId(cart.getId()))
                .orElse(List.of());
    }

    @Transactional
    public CartItem addItem(UUID clientId, UUID productId, int qty) {
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
            return cartItemRepository.save(item);
        }

        if (qty > product.getQtyInStock()) {
            throw new InsufficientStockException("Niewystarczająca ilość produktu w magazynie");
        }

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQty(qty);
        return cartItemRepository.save(item);
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
    public CartItem updateItemQty(UUID clientId, UUID itemId, int qty) {
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
        return cartItemRepository.save(item);
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

    private ResourceNotFoundException cartItemNotFound() {
        return new ResourceNotFoundException(ApiErrorCode.CART_ITEM_NOT_FOUND, "Nie znaleziono pozycji koszyka");
    }
}
