import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './ProductFormPage.css';

function ProductFormPage() {
    const { id } = useParams();
    const isEdit = Boolean(id);
    const { authHeader } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: '',
        description: '',
        price: '',
        qtyInStock: '',
        material: '',
        height: '',
        width: '',
        productLength: '',
        categoryIds: [],
        mainPhotoUrl: '',
    });
    const [categories, setCategories] = useState([]);
    const [existingImages, setExistingImages] = useState([]);
    const [newGalleryUrls, setNewGalleryUrls] = useState(['']);
    const [loading, setLoading] = useState(isEdit);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetch('/api/categories')
            .then(res => res.ok ? res.json() : [])
            .then(setCategories)
            .catch(() => {});
    }, []);

    useEffect(() => {
        if (!isEdit) return;
        fetch(`/api/products/${id}`)
            .then(res => res.ok ? res.json() : null)
            .then(product => {
                if (!product) { navigate('/sklep'); return; }
                setForm({
                    name: product.name || '',
                    description: product.description || '',
                    price: product.price != null ? String(product.price) : '',
                    qtyInStock: product.qtyInStock != null ? String(product.qtyInStock) : '',
                    material: product.material || '',
                    height: product.height || '',
                    width: product.width || '',
                    productLength: product.productLength || '',
                    categoryIds: (product.categories || []).map(c => c.id),
                    mainPhotoUrl: product.photo || '',
                });
                setExistingImages(
                    (product.images || []).filter(img => img.imageUrl !== product.photo)
                );
                setLoading(false);
            })
            .catch(() => navigate('/sklep'));
    }, [id, isEdit, navigate]);

    function handleChange(e) {
        const { name, value } = e.target;
        setForm(f => ({ ...f, [name]: value }));
    }

    function toggleCategory(catId) {
        setForm(f => ({
            ...f,
            categoryIds: f.categoryIds.includes(catId)
                ? f.categoryIds.filter(c => c !== catId)
                : [...f.categoryIds, catId],
        }));
    }

    async function deleteExistingImage(imageId) {
        const res = await fetch(`/api/products/${id}/images/${imageId}`, {
            method: 'DELETE',
            headers: authHeader(),
        });
        if (res.ok) setExistingImages(imgs => imgs.filter(img => img.id !== imageId));
    }

    function setGalleryUrl(index, value) {
        setNewGalleryUrls(urls => urls.map((u, i) => i === index ? value : u));
    }

    function addGalleryUrl() {
        setNewGalleryUrls(urls => [...urls, '']);
    }

    function removeGalleryUrl(index) {
        setNewGalleryUrls(urls => urls.filter((_, i) => i !== index));
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setSaving(true);
        setError(null);

        const body = {
            name: form.name,
            description: form.description,
            price: parseFloat(form.price),
            qtyInStock: parseInt(form.qtyInStock, 10),
            material: form.material,
            height: form.height,
            width: form.width,
            productLength: form.productLength,
            categoryIds: form.categoryIds,
        };

        try {
            let productId = id;

            if (isEdit) {
                const res = await fetch(`/api/products/${id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json', ...authHeader() },
                    body: JSON.stringify(body),
                });
                if (!res.ok) throw new Error('Błąd zapisu produktu');
            } else {
                const res = await fetch('/api/products', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', ...authHeader() },
                    body: JSON.stringify(body),
                });
                if (!res.ok) throw new Error('Błąd tworzenia produktu');
                const product = await res.json();
                productId = product.id;
            }

            if (form.mainPhotoUrl.trim()) {
                await fetch(`/api/products/${productId}/photo-url`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', ...authHeader() },
                    body: JSON.stringify({ url: form.mainPhotoUrl.trim() }),
                });
            }

            for (const url of newGalleryUrls.filter(u => u.trim())) {
                await fetch(`/api/products/${productId}/image-url`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', ...authHeader() },
                    body: JSON.stringify({ url: url.trim() }),
                });
            }

            navigate(`/sklep/${productId}`);
        } catch (err) {
            setError(err.message);
            setSaving(false);
        }
    }

    if (loading) return <p className="status-text">Ładowanie...</p>;

    return (
        <div className="pf-page">
            <div className="pf-inner">
                <Link to={isEdit ? `/sklep/${id}` : '/sklep'} className="breadcrumb">
                    ← {isEdit ? 'Produkt' : 'Sklep'}
                </Link>
                <h1 className="pf-title">{isEdit ? 'Edytuj produkt' : 'Nowy produkt'}</h1>

                <form onSubmit={handleSubmit} className="pf-form">
                    <section className="pf-section">
                        <h2 className="pf-section-title">Podstawowe informacje</h2>
                        <div className="pf-field">
                            <label>Nazwa *</label>
                            <input name="name" value={form.name} onChange={handleChange} required />
                        </div>
                        <div className="pf-row pf-row-2">
                            <div className="pf-field">
                                <label>Cena (zł) *</label>
                                <input name="price" type="number" step="0.01" min="0" value={form.price} onChange={handleChange} required />
                            </div>
                            <div className="pf-field">
                                <label>Stan magazynowy *</label>
                                <input name="qtyInStock" type="number" min="0" value={form.qtyInStock} onChange={handleChange} required />
                            </div>
                        </div>
                        <div className="pf-field">
                            <label>Opis</label>
                            <textarea name="description" value={form.description} onChange={handleChange} rows={4} />
                        </div>
                    </section>

                    <section className="pf-section">
                        <h2 className="pf-section-title">Szczegóły</h2>
                        <div className="pf-field">
                            <label>Materiał</label>
                            <input name="material" value={form.material} onChange={handleChange} />
                        </div>
                        <div className="pf-row">
                            <div className="pf-field">
                                <label>Wysokość</label>
                                <input name="height" value={form.height} onChange={handleChange} placeholder="np. 12 cm" />
                            </div>
                            <div className="pf-field">
                                <label>Szerokość</label>
                                <input name="width" value={form.width} onChange={handleChange} placeholder="np. 8 cm" />
                            </div>
                            <div className="pf-field">
                                <label>Głębokość</label>
                                <input name="productLength" value={form.productLength} onChange={handleChange} placeholder="np. 8 cm" />
                            </div>
                        </div>
                    </section>

                    <section className="pf-section">
                        <h2 className="pf-section-title">Kategorie</h2>
                        <div className="pf-categories">
                            {categories.map(cat => (
                                <label key={cat.id} className="pf-cat-label">
                                    <input
                                        type="checkbox"
                                        checked={form.categoryIds.includes(cat.id)}
                                        onChange={() => toggleCategory(cat.id)}
                                    />
                                    {cat.categoryName}
                                </label>
                            ))}
                        </div>
                    </section>

                    <section className="pf-section">
                        <h2 className="pf-section-title">Zdjęcia</h2>
                        <div className="pf-field">
                            <label>Główne zdjęcie (URL z Supabase)</label>
                            <input
                                name="mainPhotoUrl"
                                value={form.mainPhotoUrl}
                                onChange={handleChange}
                                placeholder="https://..."
                            />
                            {form.mainPhotoUrl && (
                                <img src={form.mainPhotoUrl} alt="podgląd" className="pf-preview" />
                            )}
                        </div>

                        {isEdit && existingImages.length > 0 && (
                            <div className="pf-field">
                                <label>Istniejące zdjęcia galerii</label>
                                <div className="pf-existing-images">
                                    {existingImages.map(img => (
                                        <div key={img.id} className="pf-existing-img">
                                            <img src={img.imageUrl} alt="" />
                                            <button
                                                type="button"
                                                className="pf-remove-img"
                                                onClick={() => deleteExistingImage(img.id)}
                                            >×</button>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        <div className="pf-field">
                            <label>Dodaj zdjęcia galerii (URL z Supabase)</label>
                            {newGalleryUrls.map((url, i) => (
                                <div key={i} className="pf-gallery-row">
                                    <input
                                        value={url}
                                        onChange={e => setGalleryUrl(i, e.target.value)}
                                        placeholder="https://..."
                                    />
                                    {newGalleryUrls.length > 1 && (
                                        <button type="button" className="pf-remove-url" onClick={() => removeGalleryUrl(i)}>×</button>
                                    )}
                                </div>
                            ))}
                            <button type="button" className="pf-add-url" onClick={addGalleryUrl}>
                                + Dodaj kolejny URL
                            </button>
                        </div>
                    </section>

                    {error && <p className="pf-error">{error}</p>}

                    <div className="pf-actions">
                        <button type="submit" className="pf-btn-save" disabled={saving}>
                            {saving ? 'Zapisywanie...' : isEdit ? 'Zapisz zmiany' : 'Dodaj produkt'}
                        </button>
                        <Link to={isEdit ? `/sklep/${id}` : '/sklep'} className="pf-btn-cancel">
                            Anuluj
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default ProductFormPage;
