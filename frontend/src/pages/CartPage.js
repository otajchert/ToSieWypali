import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './CartPage.css';

const imgSrc = url => url && (url.startsWith('http') ? url : `/api/images/${url}`);

function CartPage() {
    const { items, cartReady, removeItem, updateQty } = useCart();
    const { user } = useAuth();
    const [outOfStock, setOutOfStock] = useState([]);
    const [dismissedOos, setDismissedOos] = useState(false);
    const [removingOos, setRemovingOos] = useState(false);

    // Once cartReady, check for out-of-stock items.
    // For logged-in users the items already have fresh qtyInStock from the context fetch.
    // For guests we re-fetch each product to get current stock.
    useEffect(() => {
        if (!cartReady || items.length === 0) return;

        let cancelled = false;

        async function check() {
            let checked = items;

            if (!user && items.length > 0) {
                const products = await Promise.all(
                    items.map(item =>
                        fetch(`/api/products/${item.productId}`)
                            .then(r => r.ok ? r.json() : null)
                            .catch(() => null)
                    )
                );
                checked = items.map((item, i) => ({
                    ...item,
                    qtyInStock: products[i]?.qtyInStock ?? item.qtyInStock,
                }));
            }

            if (!cancelled) {
                setOutOfStock(checked.filter(i => i.qtyInStock === 0));
            }
        }

        check();
        return () => { cancelled = true; };
    }, [cartReady]); // run once after initial cart load

    async function handleRemoveOutOfStock() {
        setRemovingOos(true);
        for (const item of outOfStock) {
            await removeItem(item);
        }
        setOutOfStock([]);
        setRemovingOos(false);
    }

    const total = items.reduce((sum, i) => sum + parseFloat(i.price) * i.qty, 0);
    const totalQty = items.reduce((sum, i) => sum + i.qty, 0);

    if (!cartReady) {
        return (
            <div className="cart-page">
                <div className="cart-inner">
                    <h1 className="cart-title">Koszyk</h1>
                    <p className="cart-loading">Ładowanie...</p>
                </div>
            </div>
        );
    }

    return (
        <div className="cart-page">
            <div className="cart-inner">
                <h1 className="cart-title">Koszyk</h1>

                {outOfStock.length > 0 && !dismissedOos && (
                    <div className="oos-banner">
                        <div className="oos-banner-body">
                            <p className="oos-banner-title">Niestety następujące produkty są niedostępne:</p>
                            <ul className="oos-list">
                                {outOfStock.map(item => (
                                    <li key={item.productId}>{item.name}</li>
                                ))}
                            </ul>
                            <p className="oos-question">Czy chcesz je usunąć z koszyka?</p>
                        </div>
                        <div className="oos-banner-actions">
                            <button
                                className="oos-btn-yes"
                                onClick={handleRemoveOutOfStock}
                                disabled={removingOos}
                            >
                                {removingOos ? 'Usuwanie...' : 'Tak, usuń'}
                            </button>
                            <button
                                className="oos-btn-no"
                                onClick={() => setDismissedOos(true)}
                            >
                                Zostaw
                            </button>
                        </div>
                    </div>
                )}

                {items.length === 0 ? (
                    <div className="cart-empty">
                        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#c0a898" strokeWidth="1.5">
                            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
                            <line x1="3" y1="6" x2="21" y2="6" />
                            <path d="M16 10a4 4 0 01-8 0" />
                        </svg>
                        <p className="empty-label">Twój koszyk jest pusty</p>
                        <Link to="/sklep" className="btn-shop">Przejdź do sklepu</Link>
                    </div>
                ) : (
                    <div className="cart-layout">
                        <div className="cart-items">
                            {items.map(item => (
                                <div key={item.id || item.productId} className={`cart-item${item.qtyInStock === 0 ? ' cart-item--oos' : ''}`}>
                                    <div className="cart-item-photo">
                                        {item.photo
                                            ? <img src={imgSrc(item.photo)} alt={item.name} />
                                            : <div className="cart-item-no-photo" />
                                        }
                                    </div>

                                    <div className="cart-item-info">
                                        <Link to={`/sklep/${item.productId}`} className="cart-item-name">
                                            {item.name}
                                        </Link>
                                        <p className="cart-item-unit-price">{parseFloat(item.price).toFixed(2).replace('.', ',')} zł / szt.</p>
                                        {item.qtyInStock === 0 && (
                                            <p className="cart-item-oos-label">Brak w magazynie</p>
                                        )}
                                    </div>

                                    <div className="cart-item-qty">
                                        <button
                                            className="qty-btn"
                                            onClick={() => updateQty(item, item.qty - 1)}
                                            disabled={item.qty <= 1}
                                            aria-label="Zmniejsz ilość"
                                        >−</button>
                                        <span className="qty-value">{item.qty}</span>
                                        <button
                                            className="qty-btn"
                                            onClick={() => updateQty(item, item.qty + 1)}
                                            disabled={item.qty >= item.qtyInStock}
                                            aria-label="Zwiększ ilość"
                                        >+</button>
                                    </div>

                                    <p className="cart-item-total">
                                        {(parseFloat(item.price) * item.qty).toFixed(2).replace('.', ',')} zł
                                    </p>

                                    <button
                                        className="cart-item-remove"
                                        onClick={() => removeItem(item)}
                                        aria-label="Usuń produkt"
                                    >
                                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                            <line x1="18" y1="6" x2="6" y2="18" />
                                            <line x1="6" y1="6" x2="18" y2="18" />
                                        </svg>
                                    </button>
                                </div>
                            ))}
                        </div>

                        <div className="cart-summary">
                            <h2 className="summary-title">Podsumowanie</h2>
                            <div className="summary-row">
                                <span>Produkty ({totalQty} szt.)</span>
                                <span>{total.toFixed(2).replace('.', ',')} zł</span>
                            </div>
                            <div className="summary-row summary-shipping">
                                <span>Dostawa</span>
                                <span className="summary-shipping-note">obliczana przy zamówieniu</span>
                            </div>
                            <div className="summary-divider" />
                            <div className="summary-row summary-total">
                                <span>Razem</span>
                                <span>{total.toFixed(2).replace('.', ',')} zł</span>
                            </div>
                            {!user && (
                                <p className="summary-login-hint">
                                    <Link to="/logowanie">Zaloguj się</Link>, żeby zapisać koszyk i zamawiać.
                                </p>
                            )}
                            {user ? (
                                <Link to="/checkout" className="btn-checkout">Złóż zamówienie</Link>
                            ) : (
                                <button className="btn-checkout" disabled>Złóż zamówienie</button>
                            )}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

export default CartPage;
