import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);
const AUTH_KEY = 'tsw_auth_v2';
const LEGACY_AUTH_KEY = 'tsw_auth';

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        try {
            const stored = localStorage.getItem(AUTH_KEY);
            localStorage.removeItem(LEGACY_AUTH_KEY);
            return stored ? JSON.parse(stored) : null;
        } catch {
            return null;
        }
    });

    function login(authData) {
        localStorage.setItem(AUTH_KEY, JSON.stringify(authData));
        setUser(authData);
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
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}
