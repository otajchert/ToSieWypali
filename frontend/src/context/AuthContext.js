import React, { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(() => {
        try {
            const stored = localStorage.getItem('tsw_auth');
            return stored ? JSON.parse(stored) : null;
        } catch {
            return null;
        }
    });

    function login(authData) {
        localStorage.setItem('tsw_auth', JSON.stringify(authData));
        setUser(authData);
    }

    function logout() {
        localStorage.removeItem('tsw_auth');
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
