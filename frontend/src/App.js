import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import TopBar from './components/TopBar/TopBar';
import HomePage from './pages/HomePage';
import ShopPage from './pages/ShopPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CreatorPage from './pages/CreatorPage';
import WorkshopsPage from './pages/WorkshopsPage';
import CartPage from './pages/CartPage';
import './App.css';

function App() {
    return (
        <BrowserRouter>
            <TopBar />
            <main className="page-content">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/sklep" element={<ShopPage />} />
                    <Route path="/sklep/:id" element={<ProductDetailPage />} />
                    <Route path="/kreator" element={<CreatorPage />} />
                    <Route path="/warsztaty" element={<WorkshopsPage />} />
                    <Route path="/koszyk" element={<CartPage />} />
                </Routes>
            </main>
        </BrowserRouter>
    );
}

export default App;
