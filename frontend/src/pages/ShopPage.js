import React, { useState, useEffect } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './ShopPage.css';

function ShopPage() {
    const { user } = useAuth();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [searchParams] = useSearchParams();

    const searchQuery = searchParams.get('q') || '';
    const category = searchParams.get('kategoria') || '';

    useEffect(() => {
        fetch('/api/products')
            .then(res => {
                if (!res.ok) throw new Error('Błąd pobierania produktów');
                return res.json();
            })
            .then(data => {
                setProducts(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    const filtered = products.filter(p => {
        const matchesQuery = !searchQuery || p.name.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesCategory = !category || (p.categories || []).some(
            c => c.categoryName.toLowerCase() === category.toLowerCase()
        );
        return matchesQuery && matchesCategory;
    });

    const pageTitle = category
        ? category.charAt(0).toUpperCase() + category.slice(1)
        : 'Sklep';

    return (
        <div className="shop-page">
            <div className="shop-inner">
                <div className="shop-header">
                    <div className="shop-header-top">
                        <h1 className="shop-title">{pageTitle}</h1>
                        {user?.role === 'ADMIN' && (
                            <Link to="/admin/nowy-produkt" className="btn-add-product">+ Dodaj produkt</Link>
                        )}
                    </div>
                    {searchQuery && <p className="search-label">Wyniki dla: „{searchQuery}"</p>}
                </div>

                {loading && <p className="status-text">Ładowanie...</p>}
                {error && <p className="status-text error">{error}</p>}

                {!loading && !error && (
                    <>
                        <p className="product-count">{filtered.length} produktów</p>
                        <div className="products-grid">
                            {filtered.length === 0 && (
                                <p className="status-text">Brak produktów.</p>
                            )}
                            {filtered.map(product => (
                                <Link key={product.id} to={`/sklep/${product.id}`} className="product-card">
                                    <div className="product-photo">
                                        {(() => {
                                            const src = product.photo || product.images?.[0]?.imageUrl;
                                            return src
                                                ? <img src={src.startsWith('http') ? src : `/api/images/${src}`} alt={product.name} />
                                                : <div className="no-photo" />;
                                        })()}
                                    </div>
                                    <span className="product-name">{product.name}</span>
                                    <span className="product-price">{Number(product.price).toFixed(2).replace('.', ',')} zł</span>
                                </Link>
                            ))}
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}

export default ShopPage;
