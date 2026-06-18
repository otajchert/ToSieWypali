import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './OrderDetailPage.css';

const STATUS_IDS = {
    'Nowe': '00000000-0000-0000-0000-000000000021',
    'W realizacji': '00000000-0000-0000-0000-000000000022',
    'Wyslane': '00000000-0000-0000-0000-000000000023',
    'Dostarczone': '00000000-0000-0000-0000-000000000024',
    'Anulowane': '00000000-0000-0000-0000-000000000025',
};

const imgSrc = url => url && (url.startsWith('http') ? url : `/api/images/${url}`);

function OrderDetailPage() {
    const { id } = useParams();
    const { user, authHeader } = useAuth();

    const [order, setOrder] = useState(null);
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [updating, setUpdating] = useState(false);

    const isAdmin = user?.role === 'ADMIN';

    useEffect(() => {
        if (!user) return;
        Promise.all([
            fetch(`/api/orders/${id}`, { headers: authHeader() }),
            fetch(`/api/orders/${id}/items`, { headers: authHeader() }),
        ])
            .then(([orderRes, itemsRes]) => {
                if (!orderRes.ok) throw new Error('Nie znaleziono zamówienia');
                return Promise.all([orderRes.json(), itemsRes.ok ? itemsRes.json() : []]);
            })
            .then(([orderData, itemsData]) => {
                setOrder(orderData);
                setItems(itemsData);
            })
            .catch(err => setError(err.message))
            .finally(() => setLoading(false));
    }, [id, user]);

    async function changeStatus(statusName) {
        const statusId = STATUS_IDS[statusName];
        if (!statusId) return;
        setUpdating(true);
        try {
            const res = await fetch(`/api/orders/${id}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', ...authHeader() },
                body: JSON.stringify({ statusId }),
            });
            if (res.ok) setOrder(await res.json());
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
                                value={order.orderStatus?.name || 'Nowe'}
                                onChange={e => changeStatus(e.target.value)}
                                disabled={updating}
                            >
                                {Object.keys(STATUS_IDS).map(s => (
                                    <option key={s} value={s}>{s}</option>
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
