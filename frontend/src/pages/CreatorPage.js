import React from 'react';
import { Link } from 'react-router-dom';
import cuencaImg from './icons/cuenca.svg';
import './CreatorPage.css';

function CreatorPage() {
    return (
        <div className="creator-page">
            <div className="creator-inner">
                <h1 className="creator-title">Jaki obiekt chcesz zaprojektować?</h1>
                <p className="creator-subtitle">Wybierz produkt i stwórz coś wyjątkowego</p>

                <div className="creator-options">
                    <Link to="/kreator/kafelki" className="creator-option">
                        <div className="creator-preview">
                            <img src={cuencaImg} alt="Kafelek Cuenca" className="creator-preview-img" />
                        </div>
                        <span className="option-label">Kreator Kafelków</span>
                        <span className="option-desc">Zaprojektuj swój unikalny kafelek</span>
                    </Link>

                    <Link to="/kreator/kubki" className="creator-option">
                        <div className="creator-preview">
                            <img src={`${process.env.PUBLIC_URL}/mug-parts/miniature.svg`} alt="Kubek" className="creator-preview-img creator-preview-img--contain" />
                        </div>
                        <span className="option-label">Kreator Kubków</span>
                        <span className="option-desc">Dobierz kształt i kolor kubka</span>
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default CreatorPage;
