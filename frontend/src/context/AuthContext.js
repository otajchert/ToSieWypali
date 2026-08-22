import React, { createContext, useContext, useEffect, useState } from 'react';

const AuthContext = createContext(null);
const AUTH_KEY = 'tsw_auth_v2';
const LEGACY_AUTH_KEY = 'tsw_auth';

function readStoredAuth() {
    try {
        const stored = localStorage.getItem(AUTH_KEY);
        localStorage.removeItem(LEGACY_AUTH_KEY);
        if (!stored) return null;
        const auth = JSON.parse(stored);
        return auth?.token ? { token: auth.token } : null;
    } catch {
        return null;
    }
}

export function AuthProvider({ children }) {
    const [storedAuth] = useState(readStoredAuth);
    const [user, setUser] = useState(storedAuth);
    const [authReady, setAuthReady] = useState(false);

    useEffect(() => {
        const token = storedAuth?.token;
        if (!token) {
            setAuthReady(true);
            return;
        }

        const controller = new AbortController();
        fetch('/api/me', {
            headers: { Authorization: `Bearer ${token}` },
            signal: controller.signal,
        })
            .then(async response => {
                if (!response.ok) throw new Error();
                const profile = await response.json();
                setUser({ token, ...profile });
                localStorage.setItem(AUTH_KEY, JSON.stringify({ token }));
            })
            .catch(error => {
                if (error.name === 'AbortError') return;
                localStorage.removeItem(AUTH_KEY);
                setUser(null);
            })
            .finally(() => {
                if (!controller.signal.aborted) setAuthReady(true);
            });

        return () => controller.abort();
    }, [storedAuth]);

    function login(authData) {
        localStorage.setItem(AUTH_KEY, JSON.stringify({ token: authData.token }));
        setUser(authData);
        setAuthReady(true);
    }

    function logout() {
        localStorage.removeItem(AUTH_KEY);
        setUser(null);
    }

    function authHeader() {
        return user ? { Authorization: `Bearer ${user.token}` } : {};
    }

    return (
        <AuthContext.Provider value={{ user, login, logout, authHeader }}>
            {authReady ? children : null}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}
