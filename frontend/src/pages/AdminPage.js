import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCategories } from '../context/CategoryContext';
import { getOrderStatusOptions } from '../orderStatuses';
import { readApiError } from '../apiErrors';
import './AdminPage.css';

function AdminPage() {
    const { user, authHeader } = useAuth();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(null);
    const [error, setError] = useState('');

    const { categories, setCategories } = useCategories();
    const [newCatName, setNewCatName] = useState('');
    const [catSaving, setCatSaving] = useState(false);
    const [deleteDialog, setDeleteDialog] = useState(null);

    useEffect(() => {
        if (!user || user.role !== 'ADMIN') return;
        fetch('/api/orders', { headers: authHeader() })
            .then(async res => {
                if (!res.ok) {
                    const apiError = await readApiError(res, 'Nie udało się pobrać zamówień.');
                    setError(apiError.message);
                    return [];
                }
                return res.json();
            })
            .then(data => setOrders(data))
            .catch(() => {
                setOrders([]);
                setError('Błąd połączenia z serwerem.');
            })
            .finally(() => setLoading(false));
    }, [user]);

    async function changeStatus(orderId, statusId) {
        if (!statusId) return;
        setUpdating(orderId);
        setError('');
        try {
            const res = await fetch(`/api/orders/${orderId}/status`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', ...authHeader() },
                body: JSON.stringify({ statusId }),
            });
            if (res.ok) {
                const updated = await res.json();
                setOrders(prev => prev.map(o => o.id === orderId ? updated : o));
            } else {
                const apiError = await readApiError(res, 'Nie udało się zmienić statusu zamówienia.');
                setError(apiError.message);
            }
        } catch {
            setError('Błąd połączenia z serwerem.');
        } finally {
            setUpdating(null);
        }
    }

    async function addCategory(e) {
        e.preventDefault();
        const name = newCatName.trim();
        if (!name) return;
        setCatSaving(true);
        setError('');
        try {
            const res = await fetch('/api/categories', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', ...authHeader() },
                body: JSON.stringify({ categoryName: name }),
            });
            if (res.ok) {
                const created = await res.json();
                setCategories(prev => [...prev, created]);
                setNewCatName('');
            } else {
                const apiError = await readApiError(res, 'Nie udało się dodać kategorii.');
                setError(apiError.message);
            }
        } catch {
            setError('Błąd połączenia z serwerem.');
        } finally {
            setCatSaving(false);
        }
    }

    async function requestDelete(cat) {
        setError('');
        try {
            const res = await fetch(`/api/categories/${cat.id}`, {
                method: 'DELETE',
                headers: authHeader(),
            });
            if (res.status === 204) {
                setCategories(prev => prev.filter(c => c.id !== cat.id));
                return;
            }

            const apiError = await readApiError(res, 'Nie udało się usunąć kategorii.');
            const productCount = apiError.problem?.productCount;
            if (res.status === 409 && typeof productCount === 'number') {
                setDeleteDialog({ id: cat.id, name: cat.categoryName, productCount });
            } else {
                setError(apiError.message);
            }
        } catch {
            setError('Błąd połączenia z serwerem.');
        }
    }

    async function confirmDelete() {
        if (!deleteDialog) return;
        setError('');
        try {
            const res = await fetch(`/api/categories/${deleteDialog.id}?force=true`, {
                method: 'DELETE',
                headers: authHeader(),
            });
            if (res.status === 204) {
                setCategories(prev => prev.filter(c => c.id !== deleteDialog.id));
            } else {
                const apiError = await readApiError(res, 'Nie udało się usunąć kategorii.');
                setError(apiError.message);
            }
        } catch {
            setError('Błąd połączenia z serwerem.');
        } finally {
            setDeleteDialog(null);
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

                {error && <p className="admin-error">{error}</p>}

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
                                        <th></th>
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
                                                    ? `${Number(order.orderTotal).toFixed(2).replace('.', ',')} zł`
                                                    : '—'}
                                            </td>
                                            <td className="order-shipping">
                                                {order.shippingMethod?.name || '—'}
                                            </td>
                                            <td>
                                                <select
                                                    className="status-select"
                                                    value={order.orderStatus?.id || ''}
                                                    onChange={e => changeStatus(order.id, e.target.value)}
                                                    disabled={updating === order.id}
                                                >
                                                    {!order.orderStatus && (
                                                        <option value="" disabled>Wybierz status</option>
                                                    )}
                                                    {getOrderStatusOptions(order.orderStatus?.id).map(status => (
                                                        <option key={status.id} value={status.id}>{status.label}</option>
                                                    ))}
                                                </select>
                                            </td>
                                            <td>
                                                <Link to={`/zamowienie/${order.id}`} className="order-details-link">
                                                    Szczegóły
                                                </Link>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>

                <section className="admin-section">
                    <h2 className="section-title">Kategorie</h2>
                    <div className="cat-list">
                        {categories.length === 0 && (
                            <p className="admin-hint">Brak kategorii.</p>
                        )}
                        {categories.map(cat => (
                            <div key={cat.id} className="cat-row">
                                <span className="cat-name">{cat.categoryName}</span>
                                <button
                                    className="cat-delete-btn"
                                    onClick={() => requestDelete(cat)}
                                    title="Usuń kategorię"
                                >
                                    ×
                                </button>
                            </div>
                        ))}
                    </div>

                    <form className="cat-add-form" onSubmit={addCategory}>
                        <input
                            className="cat-add-input"
                            value={newCatName}
                            onChange={e => setNewCatName(e.target.value)}
                            placeholder="Nazwa nowej kategorii"
                            required
                        />
                        <button className="cat-add-btn" type="submit" disabled={catSaving}>
                            {catSaving ? 'Dodawanie...' : '+ Dodaj'}
                        </button>
                    </form>
                </section>
            </div>

            {deleteDialog && (
                <div className="cat-dialog-overlay" onClick={() => setDeleteDialog(null)}>
                    <div className="cat-dialog" onClick={e => e.stopPropagation()}>
                        <h3 className="cat-dialog-title">Usuń kategorię</h3>
                        <p className="cat-dialog-body">
                            Kategoria <strong>{deleteDialog.name}</strong> jest przypisana do{' '}
                            <strong>{deleteDialog.productCount}</strong>{' '}
                            {deleteDialog.productCount === 1 ? 'produktu' : 'produktów'}.
                            Produkty zachowają swoje pozostałe kategorie.
                        </p>
                        <div className="cat-dialog-actions">
                            <button className="cat-dialog-cancel" onClick={() => setDeleteDialog(null)}>
                                Anuluj
                            </button>
                            <button className="cat-dialog-confirm" onClick={confirmDelete}>
                                Usuń mimo to
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default AdminPage;
