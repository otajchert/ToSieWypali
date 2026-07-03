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
import ProductFormPage from './pages/ProductFormPage';
import OrderDetailPage from './pages/OrderDetailPage';
import CheckoutPage from './pages/CheckoutPage';
import TileCreatorPage from './pages/TileCreatorPage';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { CategoryProvider } from './context/CategoryContext';
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
                    <Route path="/kreator/kafelki" element={<TileCreatorPage />} />
                    <Route path="/warsztaty" element={<WorkshopsPage />} />
                    <Route path="/koszyk" element={<CartPage />} />
                    <Route path="/logowanie" element={<LoginPage />} />
                    <Route path="/konto" element={
                        <RequireAuth><AccountPage /></RequireAuth>
                    } />
                    <Route path="/admin" element={
                        <RequireAdmin><AdminPage /></RequireAdmin>
                    } />
                    <Route path="/admin/nowy-produkt" element={
                        <RequireAdmin><ProductFormPage /></RequireAdmin>
                    } />
                    <Route path="/admin/produkt/:id" element={
                        <RequireAdmin><ProductFormPage /></RequireAdmin>
                    } />
                    <Route path="/checkout" element={
                        <RequireAuth><CheckoutPage /></RequireAuth>
                    } />
                    <Route path="/zamowienie/:id" element={
                        <RequireAuth><OrderDetailPage /></RequireAuth>
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
                <CartProvider>
                    <CategoryProvider>
                        <AppRoutes />
                    </CategoryProvider>
                </CartProvider>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
