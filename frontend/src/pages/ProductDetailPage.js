import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import './ProductDetailPage.css';

function ProductDetailPage() {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [qty, setQty] = useState(1);

    useEffect(() => {
        fetch(`/api/products/${id}`)
            .then(res => {
                if (!res.ok) throw new Error('Nie znaleziono produktu');
                return res.json();
            })
            .then(data => {
                setProduct(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, [id]);

    if (loading) return <p className="status-text">Ładowanie...</p>;
    if (error) return <p className="status-text error">{error}</p>;

    const maxQty = product.qtyInStock || 0;

    return (
        <div className="detail-page">
            <div className="detail-inner">
                <Link to="/sklep" className="breadcrumb">← Sklep</Link>

                <div className="detail-layout">
                    <div className="detail-photo">
                        {product.photo
                            ? <img src={product.photo} alt={product.name} />
                            : <div className="no-photo" />
                        }
                    </div>

                    <div className="detail-info">
                        {product.categories && product.categories.length > 0 && (
                            <div className="detail-categories">
                                {product.categories.map(c => (
                                    <Link
                                        key={c.id}
                                        to={`/sklep?kategoria=${encodeURIComponent(c.categoryName)}`}
                                        className="detail-category"
                                    >
                                        {c.categoryName}
                                    </Link>
                                ))}
                            </div>
                        )}
                        <h1 className="detail-name">{product.name}</h1>
                        <p className="detail-price">{product.price} zł</p>

                        {product.description && (
                            <p className="detail-description">{product.description}</p>
                        )}

                        {(product.material || product.height || product.width || product.productLength) && (
                            <div className="detail-meta">
                                {product.material && (
                                    <div className="meta-row">
                                        <span>materiał:</span>
                                        <span>{product.material}</span>
                                    </div>
                                )}
                                {product.height && (
                                    <div className="meta-row">
                                        <span>wysokość:</span>
                                        <span>{product.height}</span>
                                    </div>
                                )}
                                {product.width && (
                                    <div className="meta-row">
                                        <span>szerokość:</span>
                                        <span>{product.width}</span>
                                    </div>
                                )}
                                {product.productLength && (
                                    <div className="meta-row">
                                        <span>głębokość:</span>
                                        <span>{product.productLength}</span>
                                    </div>
                                )}
                            </div>
                        )}

                        <div className="qty-row">
                            <span className="qty-label">Ilość</span>
                            <div className="qty-controls">
                                <button className="qty-btn" onClick={() => setQty(q => Math.max(1, q - 1))}>−</button>
                                <span className="qty-value">{qty}</span>
                                <button className="qty-btn" onClick={() => setQty(q => Math.min(maxQty, q + 1))}>+</button>
                            </div>
                        </div>

                        <p className={`stock-info${maxQty === 0 ? ' out-of-stock' : ''}`}>
                            {maxQty > 0 ? `Dostępne: ${maxQty} szt.` : 'Brak w magazynie'}
                        </p>

                        <button className="add-to-cart" disabled={maxQty === 0}>
                            Dodaj do koszyka
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProductDetailPage;
