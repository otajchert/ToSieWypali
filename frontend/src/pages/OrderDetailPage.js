import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getOrderStatusOptions } from '../orderStatuses';
import { readApiError } from '../apiErrors';
import './OrderDetailPage.css';

const imgSrc = url => url && (url.startsWith('http') ? url : `/api/images/${url}`);

function OrderDetailPage() {
    const { id } = useParams();
    const { user, authHeader } = useAuth();

    const [order, setOrder] = useState(null);
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionError, setActionError] = useState('');
    const [updating, setUpdating] = useState(false);

    const isAdmin = user?.role === 'ADMIN';

    useEffect(() => {
        if (!user) return;
        const orderPath = isAdmin ? `/api/orders/${id}` : `/api/me/orders/${id}`;
        Promise.all([
            fetch(orderPath, { headers: authHeader() }),
            fetch(`${orderPath}/items`, { headers: authHeader() }),
        ])
            .then(async ([orderRes, itemsRes]) => {
                if (!orderRes.ok) {
                    const apiError = await readApiError(orderRes, 'Nie znaleziono zamówienia.');
                    throw new Error(apiError.message);
                }
                if (!itemsRes.ok) {
                    const apiError = await readApiError(itemsRes, 'Nie udało się pobrać produktów zamówienia.');
                    throw new Error(apiError.message);
                }
                return Promise.all([orderRes.json(), itemsRes.json()]);
            })
            .then(([orderData, itemsData]) => {
                setOrder(orderData);
                setItems(itemsData);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [id, user]);

    async function changeStatus(statusId) {
        if (!statusId) return;
        setUpdating(true);
        setActionError('');
        try {
            const res = await fetch(`/api/orders/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', ...authHeader() },
                body: JSON.stringify({ statusId }),
            });
            if (res.ok) {
                setOrder(await res.json());
            } else {
                const apiError = await readApiError(res, 'Nie udało się zmienić statusu zamówienia.');
                setActionError(apiError.message);
            }
        } catch {
            setActionError('Błąd połączenia z serwerem.');
        } finally {
            setUpdating(false);
        }
    }

    if (loading) return <p className="od-state">Ładowanie...</p>;
    if (error) return <p className="od-state od-state--error">{error}</p>;
    if (!order) return null;

    const addr = order.shippingAddress;

    return (
        <div className="od-page">
            <div className="od-inner">
                <Link to={isAdmin ? '/admin' : '/konto'} className="od-back">
                    ← {isAdmin ? 'Panel administracyjny' : 'Moje konto'}
                </Link>

                <div className="od-header">
                    <h1 className="od-title">Szczegóły zamówienia</h1>
                    <span className="od-date">
                        {order.orderDate
                            ? new Date(order.orderDate).toLocaleDateString('pl-PL')
                            : '—'}
                    </span>
                </div>

                {actionError && <p className="od-action-error">{actionError}</p>}

                <div className="od-meta">
                    {isAdmin && (
                        <div className="od-meta-row">
                            <span className="od-meta-label">Klient</span>
                            <span className="od-meta-val">{order.client?.email || '—'}</span>
                        </div>
                    )}
                    <div className="od-meta-row">
                        <span className="od-meta-label">Status</span>
                        {isAdmin ? (
                            <select
                                className="od-status-select"
                                value={order.orderStatus?.id || ''}
                                onChange={e => changeStatus(e.target.value)}
                                disabled={updating}
                            >
                                {!order.orderStatus && (
                                    <option value="" disabled>Wybierz status</option>
                                )}
                                {getOrderStatusOptions(order.orderStatus?.id).map(status => (
                                    <option key={status.id} value={status.id}>{status.label}</option>
                                ))}
                            </select>
                        ) : (
                            <span className="od-status-badge">{order.orderStatus?.name || '—'}</span>
                        )}
                    </div>
                    <div className="od-meta-row">
                        <span className="od-meta-label">Dostawa</span>
                        <span className="od-meta-val">{order.shippingMethod?.name || '—'}</span>
                    </div>
                    {addr && (
                        <div className="od-meta-row">
                            <span className="od-meta-label">Adres</span>
                            <span className="od-meta-val">
                                {addr.streetNumber}
                                {addr.flat ? `, lok. ${addr.flat}` : ''},{' '}
                                {addr.postalCode} {addr.city}
                                {addr.region ? `, ${addr.region}` : ''}
                            </span>
                        </div>
                    )}
                </div>

                <section className="od-items-section">
                    <h2 className="od-section-title">Produkty</h2>
                    {items.length === 0 ? (
                        <p className="od-hint">Brak pozycji.</p>
                    ) : (
                        <div className="od-item-list">
                            {items.map(item => (
                                <div key={item.productId} className="od-item">
                                    <div className="od-item-photo">
                                        {item.productPhoto
                                            ? <img src={imgSrc(item.productPhoto)} alt={item.productName} />
                                            : <div className="od-item-no-photo" />}
                                    </div>
                                    <Link to={`/sklep/${item.productId}`} className="od-item-name">
                                        {item.productName}
                                    </Link>
                                    <span className="od-item-unit">
                                        {Number(item.price).toFixed(2).replace('.', ',')} zł
                                    </span>
                                    <span className="od-item-qty">× {item.qty}</span>
                                    <span className="od-item-total">
                                        {(Number(item.price) * item.qty).toFixed(2).replace('.', ',')} zł
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                    <div className="od-summary">
                        <div className="od-summary-row">
                            <span>Produkty</span>
                            <span>
                                {items.reduce((sum, i) => sum + Number(i.price) * i.qty, 0).toFixed(2).replace('.', ',')} zł
                            </span>
                        </div>
                        {order.shippingMethod && (
                            <div className="od-summary-row">
                                <span>Dostawa ({order.shippingMethod.name})</span>
                                <span>{Number(order.shippingMethod.price).toFixed(2).replace('.', ',')} zł</span>
                            </div>
                        )}
                        <div className="od-summary-row od-summary-total">
                            <span>Łącznie</span>
                            <span>
                                {order.orderTotal != null
                                    ? `${Number(order.orderTotal).toFixed(2).replace('.', ',')} zł`
                                    : '—'}
                            </span>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    );
}

export default OrderDetailPage;
