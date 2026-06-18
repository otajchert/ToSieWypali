import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AccountPage.css';

const emptyAddressForm = {
    name: '',
    streetNumber: '',
    flat: '',
    postalCode: '',
    city: '',
    region: '',
    isDefault: false,
};

function AccountPage() {
    const { user, authHeader } = useAuth();
    const navigate = useNavigate();

    const [profile, setProfile] = useState(null);
    const [editingProfile, setEditingProfile] = useState(false);
    const [profileForm, setProfileForm] = useState({});
    const [profileMsg, setProfileMsg] = useState('');
    const [profileError, setProfileError] = useState('');
    const [savingProfile, setSavingProfile] = useState(false);

    const [addresses, setAddresses] = useState([]);
    const [editingAddrId, setEditingAddrId] = useState(null);
    const [editAddrForm, setEditAddrForm] = useState({});
    const [addForm, setAddForm] = useState(emptyAddressForm);
    const [showAddForm, setShowAddForm] = useState(false);
    const [addrError, setAddrError] = useState('');

    const [orders, setOrders] = useState([]);
    const [loadingOrders, setLoadingOrders] = useState(true);

    const fetchProfile = useCallback(() => {
        fetch(`/api/clients/${user.id}`, { headers: authHeader() })
            .then(res => res.ok ? res.json() : null)
            .then(data => {
                if (!data) return;
                setProfile(data);
                setProfileForm({
                    firstName: data.firstName || '',
                    lastName: data.lastName || '',
                    email: data.email || '',
                    phoneNumber: data.phoneNumber || '',
                });
            })
            .catch(() => {});
    }, [user]);

    const fetchAddresses = useCallback(() => {
        fetch(`/api/clients/${user.id}/addresses`, { headers: authHeader() })
            .then(res => res.ok ? res.json() : [])
            .then(setAddresses)
            .catch(() => setAddresses([]));
    }, [user]);

    useEffect(() => {
        if (!user) return;
        fetchProfile();
        fetchAddresses();
        fetch(`/api/orders/client/${user.id}`, { headers: authHeader() })
            .then(res => res.ok ? res.json() : [])
            .then(setOrders)
            .catch(() => setOrders([]))
            .finally(() => setLoadingOrders(false));
    }, [user]);

    async function saveProfile(e) {
        e.preventDefault();
        setProfileMsg('');
        setProfileError('');
        setSavingProfile(true);
        try {
            const res = await fetch(`/api/clients/${user.id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', ...authHeader() },
                body: JSON.stringify(profileForm),
            });
            if (!res.ok) {
                setProfileError(await res.text());
            } else {
                setProfileMsg('Zapisano.');
                setEditingProfile(false);
                fetchProfile();
            }
        } catch {
            setProfileError('Błąd połączenia z serwerem');
        } finally {
            setSavingProfile(false);
        }
    }

    function startEditProfile() {
        setProfileMsg('');
        setProfileError('');
        setEditingProfile(true);
    }

    function cancelEditProfile() {
        setEditingProfile(false);
        setProfileMsg('');
        setProfileError('');
        if (profile) {
            setProfileForm({
                firstName: profile.firstName || '',
                lastName: profile.lastName || '',
                email: profile.email || '',
                phoneNumber: profile.phoneNumber || '',
            });
        }
    }

    async function deleteAddress(addressId) {
        await fetch(`/api/clients/${user.id}/addresses/${addressId}`, {
            method: 'DELETE',
            headers: authHeader(),
        });
        fetchAddresses();
    }

    async function setDefaultAddress(addressId) {
        await fetch(`/api/clients/${user.id}/addresses/${addressId}/default`, {
            method: 'PUT',
            headers: authHeader(),
        });
        fetchAddresses();
    }

    function startEditAddress(ca) {
        setEditingAddrId(ca.address.id);
        setEditAddrForm({
            name: ca.name || '',
            streetNumber: ca.address.streetNumber || '',
            flat: ca.address.flat || '',
            postalCode: ca.address.postalCode || '',
            city: ca.address.city || '',
            region: ca.address.region || '',
            isDefault: ca.isDefault || false,
        });
        setAddrError('');
    }

    async function saveEditAddress(addressId) {
        setAddrError('');
        const res = await fetch(`/api/clients/${user.id}/addresses/${addressId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', ...authHeader() },
            body: JSON.stringify(editAddrForm),
        });
        if (res.ok) {
            setEditingAddrId(null);
            fetchAddresses();
        } else {
            setAddrError('Nie udało się zapisać adresu.');
        }
    }

    async function submitAddAddress(e) {
        e.preventDefault();
        setAddrError('');
        const res = await fetch(`/api/clients/${user.id}/addresses`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', ...authHeader() },
            body: JSON.stringify(addForm),
        });
        if (res.ok) {
            setShowAddForm(false);
            setAddForm(emptyAddressForm);
            fetchAddresses();
        } else {
            setAddrError('Nie udało się dodać adresu.');
        }
    }

    if (!user) {
        return (
            <div className="account-page">
                <div className="account-empty">
                    <p>Nie jesteś zalogowany.</p>
                    <button className="btn-primary" onClick={() => navigate('/logowanie')}>Zaloguj się</button>
                </div>
            </div>
        );
    }

    return (
        <div className="account-page">
            <div className="account-inner">
                <div className="account-header">
                    <h1 className="account-title">Moje konto</h1>
                </div>

                {/* ── Profile ─────────────────────────────────── */}
                <section className="account-section">
                    <div className="section-header">
                        <h2 className="section-title">Dane osobowe</h2>
                        {!editingProfile && (
                            <button className="btn-outline-sm" onClick={startEditProfile}>Zmień dane</button>
                        )}
                    </div>

                    {editingProfile ? (
                        <form className="profile-form" onSubmit={saveProfile}>
                            <div className="field-row">
                                <div className="field">
                                    <label>Imię</label>
                                    <input type="text" value={profileForm.firstName}
                                        onChange={e => setProfileForm(f => ({ ...f, firstName: e.target.value }))}
                                        placeholder="Jan" required autoComplete="given-name" />
                                </div>
                                <div className="field">
                                    <label>Nazwisko</label>
                                    <input type="text" value={profileForm.lastName}
                                        onChange={e => setProfileForm(f => ({ ...f, lastName: e.target.value }))}
                                        placeholder="Kowalski" required autoComplete="family-name" />
                                </div>
                            </div>
                            <div className="field-row">
                                <div className="field">
                                    <label>Email</label>
                                    <input type="email" value={profileForm.email} required
                                        onChange={e => setProfileForm(f => ({ ...f, email: e.target.value }))}
                                        autoComplete="email" />
                                </div>
                                <div className="field">
                                    <label>Telefon</label>
                                    <input type="tel" value={profileForm.phoneNumber}
                                        onChange={e => setProfileForm(f => ({ ...f, phoneNumber: e.target.value }))}
                                        placeholder="+48 000 000 000" autoComplete="tel" />
                                </div>
                            </div>
                            {profileError && <p className="form-error">{profileError}</p>}
                            <div className="form-actions">
                                <button type="submit" className="btn-primary" disabled={savingProfile}>
                                    {savingProfile ? 'Zapisywanie...' : 'Zapisz'}
                                </button>
                                <button type="button" className="btn-ghost-sm" onClick={cancelEditProfile}>
                                    Anuluj
                                </button>
                            </div>
                        </form>
                    ) : (
                        <div className="profile-view">
                            {profileMsg && <p className="form-success" style={{ marginBottom: 16 }}>{profileMsg}</p>}
                            <div className="profile-grid">
                                <ProfileRow label="Imię" value={profile?.firstName} />
                                <ProfileRow label="Nazwisko" value={profile?.lastName} />
                                <ProfileRow label="Email" value={profile?.email} />
                                <ProfileRow label="Telefon" value={profile?.phoneNumber} />
                            </div>
                        </div>
                    )}
                </section>

                {/* ── Addresses ────────────────────────────────── */}
                <section className="account-section">
                    <div className="section-header">
                        <h2 className="section-title">Adresy dostawy</h2>
                        {!showAddForm && (
                            <button className="btn-outline-sm"
                                onClick={() => { setShowAddForm(true); setAddrError(''); }}>
                                + Dodaj adres
                            </button>
                        )}
                    </div>

                    {addrError && <p className="form-error" style={{ marginBottom: 12 }}>{addrError}</p>}

                    {addresses.length === 0 && !showAddForm && (
                        <p className="account-hint">Brak zapisanych adresów.</p>
                    )}

                    <div className="addr-list">
                        {addresses.map(ca => (
                            <div key={ca.address.id}
                                className={`addr-card ${ca.isDefault ? 'addr-default' : ''}`}>
                                {editingAddrId === ca.address.id ? (
                                    <div className="addr-edit-form">
                                        <AddressFormFields form={editAddrForm} onChange={setEditAddrForm} />
                                        <div className="form-actions" style={{ marginTop: 12 }}>
                                            <button className="btn-primary-sm"
                                                onClick={() => saveEditAddress(ca.address.id)}>
                                                Zapisz
                                            </button>
                                            <button className="btn-ghost-sm"
                                                onClick={() => setEditingAddrId(null)}>
                                                Anuluj
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <>
                                        <div className="addr-info">
                                            <div className="addr-name">
                                                {ca.name || 'Adres'}
                                                {ca.isDefault && (
                                                    <span className="default-badge">Domyślny</span>
                                                )}
                                            </div>
                                            <div className="addr-line">
                                                {ca.address.streetNumber}
                                                {ca.address.flat ? `, lok. ${ca.address.flat}` : ''}
                                            </div>
                                            <div className="addr-line">
                                                {ca.address.postalCode} {ca.address.city}
                                            </div>
                                            {ca.address.region && (
                                                <div className="addr-line">{ca.address.region}</div>
                                            )}
                                        </div>
                                        <div className="addr-actions">
                                            {!ca.isDefault && (
                                                <button className="btn-ghost-sm"
                                                    onClick={() => setDefaultAddress(ca.address.id)}>
                                                    Ustaw domyślny
                                                </button>
                                            )}
                                            <button className="btn-ghost-sm"
                                                onClick={() => startEditAddress(ca)}>
                                                Edytuj
                                            </button>
                                            <button className="btn-ghost-sm btn-danger"
                                                onClick={() => deleteAddress(ca.address.id)}>
                                                Usuń
                                            </button>
                                        </div>
                                    </>
                                )}
                            </div>
                        ))}
                    </div>

                    {showAddForm && (
                        <form className="addr-card addr-add-form" onSubmit={submitAddAddress}>
                            <div className="addr-form-title">Nowy adres</div>
                            <AddressFormFields form={addForm} onChange={setAddForm} />
                            <div className="form-actions" style={{ marginTop: 12 }}>
                                <button type="submit" className="btn-primary-sm">Dodaj</button>
                                <button type="button" className="btn-ghost-sm"
                                    onClick={() => { setShowAddForm(false); setAddrError(''); }}>
                                    Anuluj
                                </button>
                            </div>
                        </form>
                    )}
                </section>

                {/* ── Orders ───────────────────────────────────── */}
                <section className="account-section">
                    <h2 className="section-title">Historia zamówień</h2>
                    {loadingOrders ? (
                        <p className="account-hint">Ładowanie...</p>
                    ) : orders.length === 0 ? (
                        <p className="account-hint">Brak zamówień.</p>
                    ) : (
                        <div className="orders-list">
                            {orders.map(order => (
                                <Link key={order.id} to={`/zamowienie/${order.id}`} className="order-card order-card--link">
                                    <div className="order-meta">
                                        <span className="order-date">
                                            {order.orderDate
                                                ? new Date(order.orderDate).toLocaleDateString('pl-PL')
                                                : '—'}
                                        </span>
                                        <span className="order-status">
                                            {order.orderStatus?.name || '—'}
                                        </span>
                                    </div>
                                    <div className="order-total">
                                        {order.orderTotal != null
                                            ? `${Number(order.orderTotal).toFixed(2).replace('.', ',')} zł`
                                            : '—'}
                                    </div>
                                </Link>
                            ))}
                        </div>
                    )}
                </section>
            </div>
        </div>
    );
}

function ProfileRow({ label, value }) {
    return (
        <div className="profile-row">
            <span className="profile-label">{label}</span>
            <span className="profile-value">{value || <span className="profile-empty">—</span>}</span>
        </div>
    );
}

function AddressFormFields({ form, onChange }) {
    const set = (key, val) => onChange(f => ({ ...f, [key]: val }));
    return (
        <div className="addr-fields">
            <div className="field-row">
                <div className="field">
                    <label>Etykieta</label>
                    <input type="text" value={form.name}
                        onChange={e => set('name', e.target.value)}
                        placeholder="np. Dom, Praca" />
                </div>
                <div className="field">
                    <label>Nr mieszkania / lokalu</label>
                    <input type="text" value={form.flat}
                        onChange={e => set('flat', e.target.value)}
                        placeholder="np. 12A" />
                </div>
            </div>
            <div className="field">
                <label>Ulica i numer budynku</label>
                <input type="text" value={form.streetNumber}
                    onChange={e => set('streetNumber', e.target.value)}
                    placeholder="ul. Przykładowa 1" required />
            </div>
            <div className="field-row">
                <div className="field">
                    <label>Kod pocztowy</label>
                    <input type="text" value={form.postalCode}
                        onChange={e => set('postalCode', e.target.value)}
                        placeholder="00-000" required />
                </div>
                <div className="field">
                    <label>Miasto</label>
                    <input type="text" value={form.city}
                        onChange={e => set('city', e.target.value)}
                        placeholder="Warszawa" required />
                </div>
            </div>
            <div className="field">
                <label>Województwo</label>
                <input type="text" value={form.region}
                    onChange={e => set('region', e.target.value)}
                    placeholder="Mazowieckie" required />
            </div>
            <label className="checkbox-label">
                <input type="checkbox" checked={form.isDefault}
                    onChange={e => set('isDefault', e.target.checked)} />
                Ustaw jako domyślny
            </label>
        </div>
    );
}

export default AccountPage;
