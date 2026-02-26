import React from 'react';
import { Link } from 'react-router-dom';
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
                            <div className="tile-grid">
                                {Array.from({ length: 9 }).map((_, i) => (
                                    <div key={i} className="tile-cell" />
                                ))}
                            </div>
                        </div>
                        <span className="option-label">Kreator Kafelków</span>
                        <span className="option-desc">Zaprojektuj swój unikalny kafelek</span>
                    </Link>

                    <Link to="/kreator/kubki" className="creator-option">
                        <div className="creator-preview">
                            <svg className="cup-svg" viewBox="0 0 100 120" fill="none">
                                <path
                                    d="M20 25 Q20 15 30 15 L70 15 Q80 15 80 25 L74 95 Q74 105 64 105 L36 105 Q26 105 26 95 Z"
                                    fill="#d4b8a8"
                                    stroke="#8b3a3a"
                                    strokeWidth="2"
                                />
                                <path
                                    d="M74 38 Q90 38 90 55 Q90 72 74 72"
                                    stroke="#8b3a3a"
                                    strokeWidth="2"
                                    fill="none"
                                    strokeLinecap="round"
                                />
                            </svg>
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
