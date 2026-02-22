import React, { useState, useEffect } from 'react';
import './App.css';

function App() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // fetch products from backend API
    useEffect(() => {
        fetch('/api/products')
            .then(res => {
                if (!res.ok) throw new Error('Blad pobierania produktow');
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

    if (loading) return <div className="loading">Ladowanie...</div>;
    if (error) return <div className="error">Blad: {error}</div>;

    return (
        <div className="app">
            <header>
                <h1>To Sie Wypali</h1>
                
            </header>

            <main className="products-grid">
                {products.length === 0 && <p>Brak produktow w bazie.</p>}
                {products.map(product => (
                    <div key={product.id} className="product-card">
                        <div className="product-photo">
                            {product.photo
                                ? <img src={product.photo} alt={product.name} />
                                : <div className="no-photo">Brak zdjecia</div>
                            }
                        </div>
                        <div className="product-info">
                            <h3>{product.name}</h3>
                            <p className="description">{product.description}</p>
                            <div className="product-meta">
                                <span className="price">{product.price} zl</span>
                                <span className="stock">
                                    {product.qtyInStock > 0
                                        ? `Dostepne: ${product.qtyInStock}`
                                        : 'Brak w magazynie'}
                                </span>
                            </div>
                            {product.category && (
                                <span className="category">{product.category.categoryName}</span>
                            )}
                        </div>
                    </div>
                ))}
            </main>
        </div>
    );
}

export default App;
