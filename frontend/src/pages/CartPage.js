import React from 'react';
import { Link } from 'react-router-dom';
import './CartPage.css';

function CartPage() {
    return (
        <div className="cart-page">
            <div className="cart-inner">
                <h1 className="cart-title">Koszyk</h1>
                <div className="cart-empty">
                    <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="#c0a898" strokeWidth="1.5">
                        <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
                        <line x1="3" y1="6" x2="21" y2="6" />
                        <path d="M16 10a4 4 0 01-8 0" />
                    </svg>
                    <p className="empty-label">Twój koszyk jest pusty</p>
                    <Link to="/sklep" className="btn-shop">Przejdź do sklepu</Link>
                </div>
            </div>
        </div>
    );
}

export default CartPage;
