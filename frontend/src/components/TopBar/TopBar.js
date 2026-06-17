import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import './TopBar.css';

function TopBar() {
    const [searchQuery, setSearchQuery] = useState('');
    const [categories, setCategories] = useState([]);
    const { user, logout } = useAuth();
    const { totalCount } = useCart();
    const navigate = useNavigate();

    useEffect(() => {
        let cancelled = false;
        function load(attempt) {
            fetch('/api/categories')
                .then(res => res.ok ? res.json() : Promise.reject())
                .then(data => { if (!cancelled) setCategories(data); })
                .catch(() => {
                    if (!cancelled && attempt < 4) {
                        setTimeout(() => load(attempt + 1), 2000);
                    }
                });
        }
        load(0);
        return () => { cancelled = true; };
    }, []);

    function handleSearch(e) {
        e.preventDefault();
        if (searchQuery.trim()) {
            navigate(`/sklep?q=${encodeURIComponent(searchQuery.trim())}`);
            setSearchQuery('');
        }
    }

    function handleLogout() {
        logout();
        navigate('/logowanie');
    }

    return (
        <nav className="topbar">
            <div className="topbar-inner">
                <Link to="/" className="logo">TSW!</Link>

                <div className="nav-menu">
                    <div className="nav-item">
                        <Link to="/sklep" className="nav-link">
                            Sklep
                            <svg className="chevron" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                                <polyline points="6 9 12 15 18 9" />
                            </svg>
                        </Link>
                        <div className="dropdown">
                            {categories.map(cat => (
                                <Link
                                    key={cat.id}
                                    to={`/sklep?kategoria=${encodeURIComponent(cat.categoryName)}`}
                                    className="dropdown-item"
                                >
                                    {cat.categoryName}
                                </Link>
                            ))}
                        </div>
                    </div>

                    <Link to="/warsztaty" className="nav-link">Warsztaty</Link>
                    <Link to="/kreator" className="nav-link">Kreator</Link>
                </div>

                <form className="search-form" onSubmit={handleSearch}>
                    <input
                        className="search-input"
                        type="text"
                        placeholder="Szukaj produktów..."
                        value={searchQuery}
                        onChange={e => setSearchQuery(e.target.value)}
                    />
                    <button type="submit" className="search-btn" aria-label="Szukaj">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="11" cy="11" r="8" />
                            <line x1="21" y1="21" x2="16.65" y2="16.65" />
                        </svg>
                    </button>
                </form>

                <div className="nav-actions">
                    <button className="lang-btn">EN</button>

                    <Link to="/koszyk" className="icon-btn cart-icon-btn" aria-label="Koszyk">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z" />
                            <line x1="3" y1="6" x2="21" y2="6" />
                            <path d="M16 10a4 4 0 01-8 0" />
                        </svg>
                        {totalCount > 0 && (
                            <span className="cart-badge">{totalCount > 99 ? '99+' : totalCount}</span>
                        )}
                    </Link>

                    {user ? (
                        <div className="nav-item user-menu-wrap">
                            <button className="icon-btn user-menu-trigger" aria-label="Konto">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                                    <circle cx="12" cy="7" r="4" />
                                </svg>
                            </button>
                            <div className="dropdown user-dropdown">
                                <span className="user-dropdown-email">{user.email}</span>
                                {user.role === 'ADMIN' && (
                                    <Link to="/admin" className="dropdown-item">Panel administracyjny</Link>
                                )}
                                <Link to="/konto" className="dropdown-item">Moje konto</Link>
                                <button className="dropdown-item dropdown-logout" onClick={handleLogout}>
                                    Wyloguj
                                </button>
                            </div>
                        </div>
                    ) : (
                        <>
                            <Link to="/konto" className="icon-btn" aria-label="Konto">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" />
                                    <circle cx="12" cy="7" r="4" />
                                </svg>
                            </Link>
                            <Link to="/logowanie" className="login-btn">Zaloguj</Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
}

export default TopBar;
