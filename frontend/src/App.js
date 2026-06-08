import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import TopBar from './components/TopBar/TopBar';
import HomePage from './pages/HomePage';
import ShopPage from './pages/ShopPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CreatorPage from './pages/CreatorPage';
import WorkshopsPage from './pages/WorkshopsPage';
import CartPage from './pages/CartPage';
import LoginPage from './pages/LoginPage';
import AccountPage from './pages/AccountPage';
import AdminPage from './pages/AdminPage';
import { AuthProvider, useAuth } from './context/AuthContext';
import './App.css';

function RequireAuth({ children }) {
    const { user } = useAuth();
    const location = useLocation();
    if (!user) {
        return <Navigate to="/logowanie" state={{ from: location.pathname }} replace />;
    }
    return children;
}

function RequireAdmin({ children }) {
    const { user } = useAuth();
    const location = useLocation();
    if (!user) {
        return <Navigate to="/logowanie" state={{ from: location.pathname }} replace />;
    }
    if (user.role !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }
    return children;
}

function AppRoutes() {
    return (
        <>
            <TopBar />
            <main className="page-content">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/sklep" element={<ShopPage />} />
                    <Route path="/sklep/:id" element={<ProductDetailPage />} />
                    <Route path="/kreator" element={<CreatorPage />} />
                    <Route path="/warsztaty" element={<WorkshopsPage />} />
                    <Route path="/koszyk" element={<CartPage />} />
                    <Route path="/logowanie" element={<LoginPage />} />
                    <Route path="/konto" element={
                        <RequireAuth><AccountPage /></RequireAuth>
                    } />
                    <Route path="/admin" element={
                        <RequireAdmin><AdminPage /></RequireAdmin>
                    } />
                </Routes>
            </main>
        </>
    );
}

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <AppRoutes />
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
