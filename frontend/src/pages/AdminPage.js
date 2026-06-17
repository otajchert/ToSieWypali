import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AdminPage.css';

const STATUS_IDS = {
    'Nowe': '00000000-0000-0000-0000-000000000021',
    'W realizacji': '00000000-0000-0000-0000-000000000022',
    'Wyslane': '00000000-0000-0000-0000-000000000023',
    'Dostarczone': '00000000-0000-0000-0000-000000000024',
    'Anulowane': '00000000-0000-0000-0000-000000000025',
};

function AdminPage() {
    const { user, authHeader } = useAuth();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(null);

    useEffect(() => {
        if (!user || user.role !== 'ADMIN') return;
        fetch('/api/orders', { headers: authHeader() })
            .then(res => res.ok ? res.json() : [])
            .then(data => setOrders(data))
            .catch(() => setOrders([]))
            .finally(() => setLoading(false));
    }, [user]);

    async function changeStatus(orderId, statusName) {
        const statusId = STATUS_IDS[statusName];
        if (!statusId) return;
        setUpdating(orderId);
        try {
            const res = await fetch(`/api/orders/${orderId}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', ...authHeader() },
                body: JSON.stringify({ statusId }),
            });
            if (res.ok) {
                const updated = await res.json();
                setOrders(prev => prev.map(o => o.id === orderId ? updated : o));
            }
        } finally {
            setUpdating(null);
        }
    }

    if (!user || user.role !== 'ADMIN') {
        return (
            <div className="admin-page">
                <div className="admin-forbidden">
                    <p>Brak dostępu.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="admin-page">
            <div className="admin-inner">
                <div className="admin-header">
                    <h1 className="admin-title">Panel administracyjny</h1>
                    <Link to="/admin/nowy-produkt" className="admin-add-btn">+ Dodaj produkt</Link>
                </div>

                <section className="admin-section">
                    <h2 className="section-title">Zamówienia</h2>
                    {loading ? (
                        <p className="admin-hint">Ładowanie...</p>
                    ) : orders.length === 0 ? (
                        <p className="admin-hint">Brak zamówień.</p>
                    ) : (
                        <div className="admin-table-wrap">
                            <table className="admin-table">
                                <thead>
                                    <tr>
                                        <th>Data</th>
                                        <th>Klient</th>
                                        <th>Kwota</th>
                                        <th>Dostawa</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {orders.map(order => (
                                        <tr key={order.id}>
                                            <td className="order-date">
                                                {order.orderDate
                                                    ? new Date(order.orderDate).toLocaleDateString('pl-PL')
                                                    : '—'}
                                            </td>
                                            <td className="order-client">
                                                {order.client?.email || '—'}
                                            </td>
                                            <td className="order-total">
                                                {order.orderTotal != null
                                                    ? `${Number(order.orderTotal).toFixed(2)} zł`
                                                    : '—'}
                                            </td>
                                            <td className="order-shipping">
                                                {order.shippingMethod?.name || '—'}
                                            </td>
                                            <td>
                                                <select
                                                    className="status-select"
                                                    value={order.orderStatus?.name || 'Nowe'}
                                                    onChange={e => changeStatus(order.id, e.target.value)}
                                                    disabled={updating === order.id}
                                                >
                                                    {Object.keys(STATUS_IDS).map(s => (
                                                        <option key={s} value={s}>{s}</option>
                                                    ))}
                                                </select>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </div>
        </div>
    );
}

export default AdminPage;
