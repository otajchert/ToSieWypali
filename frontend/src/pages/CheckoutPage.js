import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './CheckoutPage.css';

const PICKUP_SHIPPING_ID = '00000000-0000-0000-0000-000000000033';

const imgSrc = url => url && (url.startsWith('http') ? url : `/api/images/${url}`);

function CheckoutPage() {
    const { items, clearCart } = useCart();
    const { user } = useAuth();
    const navigate = useNavigate();
    const [showConfirm, setShowConfirm] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [placedOrderId, setPlacedOrderId] = useState(null);

    const total = items.reduce((sum, i) => sum + parseFloat(i.price) * i.qty, 0);
    const totalQty = items.reduce((sum, i) => sum + i.qty, 0);

    async function handlePlaceOrder() {
        setLoading(true);
        setError(null);

        const body = {
            items: items.map(i => ({ productId: i.productId, qty: i.qty })),
            shippingMethodId: PICKUP_SHIPPING_ID,
        };

        try {
            const res = await fetch('/api/me/orders', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${user.token}`,
                },
                body: JSON.stringify(body),
            });

            if (!res.ok) {
                const msg = await res.text();
                throw new Error(msg || 'Nie udało się złożyć zamówienia');
            }

            const order = await res.json();
            await clearCart();
            setShowConfirm(false);
            setPlacedOrderId(order.id);
        } catch (e) {
            setError(e.message);
            setLoading(false);
        }
    }

    if (items.length === 0 && !placedOrderId) {
        navigate('/koszyk');
        return null;
    }

    return (
        <div className="checkout-page">
            <div className="checkout-inner">
                <h1 className="checkout-title">Finalizacja zamówienia</h1>

                <div className="checkout-layout">
                    <div className="checkout-left">
                        <section className="checkout-section">
                            <h2 className="checkout-section-title">Metoda płatności</h2>
                            <div className="checkout-option checkout-option--selected">
                                <div className="checkout-option-radio" />
                                <div className="checkout-option-body">
                                    <span className="checkout-option-label">Zapłać przy odbiorze</span>
                                    <span className="checkout-option-desc">Płatność gotówką lub blikiem przy odbiorze</span>
                                </div>
                            </div>
                        </section>

                        <section className="checkout-section">
                            <h2 className="checkout-section-title">Sposób odbioru</h2>
                            <div className="checkout-option checkout-option--selected">
                                <div className="checkout-option-radio" />
                                <div className="checkout-option-body">
                                    <span className="checkout-option-label">Odbiór osobisty</span>
                                    <span className="checkout-option-desc">Odbierz zamówienie bezpośrednio w pracowni</span>
                                </div>
                                <span className="checkout-option-price">0,00 zł</span>
                            </div>
                        </section>
                    </div>

                    <div className="checkout-right">
                        <section className="checkout-section checkout-summary">
                            <h2 className="checkout-section-title">Twoje zamówienie</h2>
                            <ul className="checkout-items">
                                {items.map(item => (
                                    <li key={item.id || item.productId} className="checkout-item">
                                        <div className="checkout-item-photo">
                                            {item.photo
                                                ? <img src={imgSrc(item.photo)} alt={item.name} />
                                                : <div className="checkout-item-no-photo" />
                                            }
                                        </div>
                                        <div className="checkout-item-info">
                                            <span className="checkout-item-name">{item.name}</span>
                                            <span className="checkout-item-qty">× {item.qty}</span>
                                        </div>
                                        <span className="checkout-item-total">
                                            {(parseFloat(item.price) * item.qty).toFixed(2).replace('.', ',')} zł
                                        </span>
                                    </li>
                                ))}
                            </ul>

                            <div className="checkout-totals">
                                <div className="checkout-total-row">
                                    <span>Produkty ({totalQty} szt.)</span>
                                    <span>{total.toFixed(2).replace('.', ',')} zł</span>
                                </div>
                                <div className="checkout-total-row">
                                    <span>Dostawa</span>
                                    <span>0,00 zł</span>
                                </div>
                                <div className="checkout-total-divider" />
                                <div className="checkout-total-row checkout-grand-total">
                                    <span>Razem</span>
                                    <span>{total.toFixed(2).replace('.', ',')} zł</span>
                                </div>
                            </div>

                            <button
                                className="btn-place-order"
                                onClick={() => setShowConfirm(true)}
                            >
                                Złóż zamówienie
                            </button>
                        </section>
                    </div>
                </div>
            </div>

            {showConfirm && (
                <div className="checkout-overlay" onClick={() => !loading && setShowConfirm(false)}>
                    <div className="checkout-modal" onClick={e => e.stopPropagation()}>
                        <h2 className="modal-title">Potwierdzenie zamówienia</h2>

                        <div className="modal-row">
                            <span className="modal-label">Płatność</span>
                            <span className="modal-value">Zapłać przy odbiorze</span>
                        </div>
                        <div className="modal-row">
                            <span className="modal-label">Odbiór</span>
                            <span className="modal-value">Odbiór osobisty</span>
                        </div>
                        <div className="modal-row modal-row--total">
                            <span className="modal-label">Do zapłaty</span>
                            <span className="modal-value modal-total">{total.toFixed(2).replace('.', ',')} zł</span>
                        </div>

                        {error && <p className="checkout-error">{error}</p>}

                        <div className="modal-actions">
                            <button
                                className="btn-modal-confirm"
                                onClick={handlePlaceOrder}
                                disabled={loading}
                            >
                                {loading ? 'Składanie...' : 'Potwierdź zamówienie'}
                            </button>
                            <button
                                className="btn-modal-cancel"
                                onClick={() => setShowConfirm(false)}
                                disabled={loading}
                            >
                                Wróć
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {placedOrderId && (
                <div className="checkout-overlay">
                    <div className="checkout-modal checkout-modal--success">
                        <div className="success-icon">
                            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#6b1e1a" strokeWidth="2">
                                <circle cx="12" cy="12" r="10" />
                                <path d="M8 12l3 3 5-5" strokeLinecap="round" strokeLinejoin="round" />
                            </svg>
                        </div>
                        <h2 className="modal-title">Zamówienie złożone!</h2>
                        <p className="success-desc">Dziękujemy. Twoje zamówienie zostało przyjęte.</p>
                        <Link to={`/zamowienie/${placedOrderId}`} className="btn-modal-confirm">
                            Zobacz szczegóły zamówienia
                        </Link>
                    </div>
                </div>
            )}
        </div>
    );
}

export default CheckoutPage;
