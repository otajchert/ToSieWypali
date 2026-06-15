import React, { useState, useEffect, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import './ProductDetailPage.css';

const imgSrc = url => url && (url.startsWith('http') ? url : `/api/images/${url}`);

function ProductDetailPage() {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [qty, setQty] = useState(1);
    const [activePhoto, setActivePhoto] = useState(null);
    const [notice, setNotice] = useState(null);
    const [adding, setAdding] = useState(false);
    const noticeTimer = useRef(null);
    const { addItem } = useCart();

    useEffect(() => {
        fetch(`/api/products/${id}`)
            .then(res => {
                if (!res.ok) throw new Error('Nie znaleziono produktu');
                return res.json();
            })
            .then(data => {
                setProduct(data);
                setActivePhoto(data.photo || (data.images && data.images[0]?.imageUrl) || null);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, [id]);

    function showNotice(type, text) {
        clearTimeout(noticeTimer.current);
        setNotice({ type, text });
        noticeTimer.current = setTimeout(() => setNotice(null), 6000);
    }

    async function handleAddToCart() {
        if (!product || adding) return;
        setAdding(true);
        try {
            const isFirst = await addItem(product, qty);
            if (isFirst) {
                showNotice('info', 'Dodanie produktu do koszyka nie rezerwuje go — rezerwacja następuje dopiero przy zakupie.');
            } else {
                showNotice('success', 'Dodano do koszyka!');
            }
        } catch (err) {
            showNotice('error', err.message || 'Błąd dodawania do koszyka');
        } finally {
            setAdding(false);
        }
    }

    if (loading) return <p className="status-text">Ładowanie...</p>;
    if (error) return <p className="status-text error">{error}</p>;

    const maxQty = product.qtyInStock || 0;

    const allPhotos = [
        ...(product.photo ? [product.photo] : []),
        ...(product.images || []).map(img => img.imageUrl).filter(url => url !== product.photo),
    ];
    const activeIndex = allPhotos.indexOf(activePhoto);
    const goPrev = () => setActivePhoto(allPhotos[(activeIndex - 1 + allPhotos.length) % allPhotos.length]);
    const goNext = () => setActivePhoto(allPhotos[(activeIndex + 1) % allPhotos.length]);

    return (
        <div className="detail-page">
            <div className="detail-inner">
                <Link to="/sklep" className="breadcrumb">← Sklep</Link>

                <div className="detail-layout">
                    <div className="detail-photo">
                        <div className="photo-viewer">
                            {activePhoto
                                ? <img src={imgSrc(activePhoto)} alt={product.name} />
                                : <div className="no-photo" />
                            }
                            {allPhotos.length > 1 && (
                                <>
                                    <button className="photo-arrow photo-arrow-left" onClick={goPrev}>&#8249;</button>
                                    <button className="photo-arrow photo-arrow-right" onClick={goNext}>&#8250;</button>
                                </>
                            )}
                        </div>

                        {allPhotos.length > 1 && (
                            <div className="gallery-thumbs">
                                {allPhotos.map(url => (
                                    <img
                                        key={url}
                                        src={imgSrc(url)}
                                        alt=""
                                        className={`gallery-thumb${activePhoto === url ? ' active' : ''}`}
                                        onClick={() => setActivePhoto(url)}
                                    />
                                ))}
                            </div>
                        )}
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

                        <button
                            className="add-to-cart"
                            disabled={maxQty === 0 || adding}
                            onClick={handleAddToCart}
                        >
                            {adding ? 'Dodawanie...' : 'Dodaj do koszyka'}
                        </button>

                        {notice && (
                            <div className={`add-notice add-notice--${notice.type}`}>
                                {notice.text}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProductDetailPage;
