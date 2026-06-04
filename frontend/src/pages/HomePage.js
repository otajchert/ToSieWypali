import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './HomePage.css';

function HomePage() {
    const [featured, setFeatured] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/products')
            .then(res => res.json())
            .then(data => {
                setFeatured(data.slice(0, 4));
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    return (
        <div className="home-page">
            <section className="hero">
                <div className="hero-inner">
                    <h1 className="hero-title">To Się Wypali!</h1>
                </div>
            </section>

            <section className="featured">
                <div className="section-inner">
                    <h2 className="section-title">Wyróżnione produkty</h2>
                    {loading ? (
                        <p className="status-text">Ładowanie...</p>
                    ) : featured.length === 0 ? (
                        <p className="status-text">Brak produktów.</p>
                    ) : (
                        <div className="product-grid">
                            {featured.map(product => (
                                <Link key={product.id} to={`/sklep/${product.id}`} className="product-card">
                                    <div className="card-photo">
                                        {(() => {
                                            const src = product.photo || product.images?.[0]?.imageUrl;
                                            return src
                                                ? <img src={src.startsWith('http') ? src : `/api/images/${src}`} alt={product.name} />
                                                : <div className="no-photo" />;
                                        })()}
                                    </div>
                                    <span className="card-name">{product.name}</span>
                                    <span className="card-price">{product.price} zł</span>
                                </Link>
                            ))}
                        </div>
                    )}
                    <div className="see-all">
                        <Link to="/sklep" className="btn-outline">Zobacz wszystkie produkty</Link>
                    </div>
                </div>
            </section>
        </div>
    );
}

export default HomePage;
