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
                        {product.category && (
                            <span className="detail-category">{product.category.categoryName}</span>
                        )}
                        <h1 className="detail-name">{product.name}</h1>
                        <p className="detail-price">{product.price} zł</p>

                        {product.description && (
                            <p className="detail-description">{product.description}</p>
                        )}

                        {(product.height || product.diameter || product.capacity) && (
                            <div className="detail-meta">
                                {product.height && (
                                    <div className="meta-row">
                                        <span>Wysokość</span>
                                        <span>{product.height} cm</span>
                                    </div>
                                )}
                                {product.diameter && (
                                    <div className="meta-row">
                                        <span>Średnica</span>
                                        <span>{product.diameter} cm</span>
                                    </div>
                                )}
                                {product.capacity && (
                                    <div className="meta-row">
                                        <span>Pojemność</span>
                                        <span>{product.capacity} l</span>
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

                        <p className="stock-info">
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
