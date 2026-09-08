import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { readApiError } from '../apiErrors';
import './CheckoutPage.css';

const imgSrc = url => url && (url.startsWith('http') ? url : `/api/images/${url}`);
const formatPrice = value => Number(value).toFixed(2).replace('.', ',');

function CheckoutPage() {
    const { items, refreshCart } = useCart();
    const { user } = useAuth();
    const navigate = useNavigate();
    const [showConfirm, setShowConfirm] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [placedOrderId, setPlacedOrderId] = useState(null);
    const [idempotencyKey, setIdempotencyKey] = useState(null);
    const [shippingMethods, setShippingMethods] = useState([]);
    const [selectedShippingId, setSelectedShippingId] = useState('');
    const [shippingLoading, setShippingLoading] = useState(true);
    const [shippingError, setShippingError] = useState('');

    const total = items.reduce((sum, i) => sum + parseFloat(i.price) * i.qty, 0);
    const totalQty = items.reduce((sum, i) => sum + i.qty, 0);
    const selectedShipping = shippingMethods.find(method => method.id === selectedShippingId);
    const shippingPrice = selectedShipping ? Number(selectedShipping.price) : 0;
    const grandTotal = total + shippingPrice;

    useEffect(() => {
        const controller = new AbortController();

        async function loadShippingMethods() {
            setShippingLoading(true);
            setShippingError('');
            try {
                const res = await fetch('/api/shipping-methods', { signal: controller.signal });
                if (!res.ok) {
                    const apiError = await readApiError(res, 'Nie udało się pobrać metod dostawy.');
                    throw new Error(apiError.message);
                }

                const methods = await res.json();
                if (!Array.isArray(methods)) throw new Error('Nie udało się pobrać metod dostawy.');

                setShippingMethods(methods);
                setSelectedShippingId(current =>
                    methods.some(method => method.id === current) ? current : (methods[0]?.id || '')
                );
            } catch (e) {
                if (e.name === 'AbortError') return;
                setShippingMethods([]);
                setSelectedShippingId('');
                setShippingError(e.message || 'Błąd połączenia z serwerem.');
            } finally {
                if (!controller.signal.aborted) setShippingLoading(false);
            }
        }

        loadShippingMethods();
        return () => controller.abort();
    }, []);

    async function handlePlaceOrder() {
        if (!selectedShipping) {
            setError('Wybierz metodę dostawy.');
            return;
        }

        setLoading(true);
        setError(null);

        const body = {
            items: items.map(i => ({ productId: i.productId, qty: i.qty })),
            shippingMethodId: selectedShipping.id,
        };

        try {
            const res = await fetch('/api/me/orders', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${user.token}`,
                    'Idempotency-Key': idempotencyKey,
                },
                body: JSON.stringify(body),
            });

            if (!res.ok) {
                const apiError = await readApiError(res, 'Nie udało się złożyć zamówienia.');
                throw new Error(apiError.message);
            }

            const order = await res.json();
            setShowConfirm(false);
            setPlacedOrderId(order.id);
            await refreshCart();
        } catch (e) {
            setError(e.message || 'Nie udało się złożyć zamówienia.');
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
                            {shippingLoading ? (
                                <p className="checkout-hint">Ładowanie metod dostawy...</p>
                            ) : shippingError ? (
                                <p className="checkout-error checkout-load-error">{shippingError}</p>
                            ) : shippingMethods.length === 0 ? (
                                <p className="checkout-hint">Brak dostępnych metod dostawy.</p>
                            ) : (
                                <div className="checkout-options">
                                    {shippingMethods.map(method => {
                                        const selected = method.id === selectedShippingId;
                                        return (
                                            <label
                                                key={method.id}
                                                className={`checkout-option checkout-option--interactive${selected ? ' checkout-option--selected' : ''}`}
                                            >
                                                <input
                                                    className="checkout-option-radio-input"
                                                    type="radio"
                                                    name="shippingMethod"
                                                    value={method.id}
                                                    checked={selected}
                                                    onChange={() => {
                                                        setSelectedShippingId(method.id);
                                                        setError(null);
                                                    }}
                                                />
                                                <span className="checkout-option-body">
                                                    <span className="checkout-option-label">{method.name}</span>
                                                </span>
                                                <span className="checkout-option-price">{formatPrice(method.price)} zł</span>
                                            </label>
                                        );
                                    })}
                                </div>
                            )}
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
                                    <span>{formatPrice(total)} zł</span>
                                </div>
                                <div className="checkout-total-row">
                                    <span>Dostawa</span>
                                    <span>{selectedShipping ? `${formatPrice(shippingPrice)} zł` : '—'}</span>
                                </div>
                                <div className="checkout-total-divider" />
                                <div className="checkout-total-row checkout-grand-total">
                                    <span>Razem</span>
                                    <span>{formatPrice(grandTotal)} zł</span>
                                </div>
                            </div>

                            <button
                                className="btn-place-order"
                                onClick={() => { setIdempotencyKey(crypto.randomUUID()); setShowConfirm(true); }}
                                disabled={shippingLoading || !selectedShipping}
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
                            <span className="modal-value">{selectedShipping?.name}</span>
                        </div>
                        <div className="modal-row modal-row--total">
                            <span className="modal-label">Do zapłaty</span>
                            <span className="modal-value modal-total">{formatPrice(grandTotal)} zł</span>
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
