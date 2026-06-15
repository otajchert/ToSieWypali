import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './LoginPage.css';

function LoginPage() {
    const [tab, setTab] = useState('login');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phone, setPhone] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const from = location.state?.from || '/konto';

    function switchTab(t) {
        setTab(t);
        setError('');
        setEmail('');
        setPassword('');
        setConfirmPassword('');
        setFirstName('');
        setLastName('');
        setPhone('');
    }

    async function handleLogin(e) {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });
            if (!res.ok) {
                setError(await res.text());
                return;
            }
            const data = await res.json();
            login(data);
            navigate(data.role === 'ADMIN' ? '/admin' : '/', { replace: true });
        } catch {
            setError('Błąd połączenia z serwerem');
        } finally {
            setLoading(false);
        }
    }

    async function handleRegister(e) {
        e.preventDefault();
        setError('');
        if (password !== confirmPassword) {
            setError('Hasła nie są identyczne');
            return;
        }
        if (password.length < 8) {
            setError('Hasło musi mieć co najmniej 8 znaków');
            return;
        }
        setLoading(true);
        try {
            const res = await fetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email,
                    password,
                    firstName: firstName || null,
                    lastName: lastName || null,
                    phoneNumber: phone || null,
                }),
            });
            if (!res.ok) {
                setError(await res.text());
                return;
            }
            const data = await res.json();
            login(data);
            navigate('/', { replace: true });
        } catch {
            setError('Błąd połączenia z serwerem');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-tabs">
                    <button
                        className={`login-tab ${tab === 'login' ? 'active' : ''}`}
                        onClick={() => switchTab('login')}
                    >
                        Zaloguj się
                    </button>
                    <button
                        className={`login-tab ${tab === 'register' ? 'active' : ''}`}
                        onClick={() => switchTab('register')}
                    >
                        Zarejestruj się
                    </button>
                </div>

                {tab === 'login' ? (
                    <form className="login-form" onSubmit={handleLogin}>
                        <div className="field">
                            <label>Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder="twoj@email.pl"
                                required
                                autoComplete="email"
                            />
                        </div>
                        <div className="field">
                            <label>Hasło</label>
                            <input
                                type="password"
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                placeholder="••••••••"
                                required
                                autoComplete="current-password"
                            />
                        </div>
                        {error && <p className="login-error">{error}</p>}
                        <button type="submit" className="login-submit" disabled={loading}>
                            {loading ? 'Logowanie...' : 'Zaloguj się'}
                        </button>
                    </form>
                ) : (
                    <form className="login-form" onSubmit={handleRegister}>
                        <div className="field-row">
                            <div className="field">
                                <label>Imię</label>
                                <input
                                    type="text"
                                    value={firstName}
                                    onChange={e => setFirstName(e.target.value)}
                                    placeholder="Jan"
                                    required
                                    autoComplete="given-name"
                                />
                            </div>
                            <div className="field">
                                <label>Nazwisko</label>
                                <input
                                    type="text"
                                    value={lastName}
                                    onChange={e => setLastName(e.target.value)}
                                    placeholder="Kowalski"
                                    required
                                    autoComplete="family-name"
                                />
                            </div>
                        </div>
                        <div className="field">
                            <label>Email</label>
                            <input
                                type="email"
                                value={email}
                                onChange={e => setEmail(e.target.value)}
                                placeholder="twoj@email.pl"
                                required
                                autoComplete="email"
                            />
                        </div>
                        <div className="field">
                            <label>Hasło</label>
                            <input
                                type="password"
                                value={password}
                                onChange={e => setPassword(e.target.value)}
                                placeholder="Minimum 8 znaków"
                                required
                                autoComplete="new-password"
                            />
                        </div>
                        <div className="field">
                            <label>Potwierdź hasło</label>
                            <input
                                type="password"
                                value={confirmPassword}
                                onChange={e => setConfirmPassword(e.target.value)}
                                placeholder="••••••••"
                                required
                                autoComplete="new-password"
                            />
                        </div>
                        <div className="field">
                            <label>Telefon <span className="optional">(opcjonalnie)</span></label>
                            <input
                                type="tel"
                                value={phone}
                                onChange={e => setPhone(e.target.value)}
                                placeholder="+48 000 000 000"
                                autoComplete="tel"
                            />
                        </div>
                        {error && <p className="login-error">{error}</p>}
                        <button type="submit" className="login-submit" disabled={loading}>
                            {loading ? 'Rejestracja...' : 'Zarejestruj się'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    );
}

export default LoginPage;
