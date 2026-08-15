import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import cuencaImg from './icons/cuenca.svg';
import { MUG_BODIES } from './mugParts';
import './CreatorPage.css';

// dedicated miniature first, first cup body as fallback, placeholder when both missing
const MUG_PREVIEW_SOURCES = [
    `${process.env.PUBLIC_URL}/mug-parts/miniatura.svg`,
    MUG_BODIES.length ? MUG_BODIES[0].url : null,
].filter(Boolean);

function MugPreview() {
    const [srcIdx, setSrcIdx] = useState(0);

    if (srcIdx >= MUG_PREVIEW_SOURCES.length) {
        return (
            <div className="creator-preview creator-preview--empty">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#c0a898" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <line x1="3" y1="3" x2="21" y2="21"/>
                </svg>
            </div>
        );
    }

    return (
        <div className="creator-preview">
            <img
                src={MUG_PREVIEW_SOURCES[srcIdx]}
                alt="Kubek"
                className="creator-preview-img creator-preview-img--contain"
                onError={() => setSrcIdx(i => i + 1)}
            />
        </div>
    );
}

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
                        <MugPreview />
                        <span className="option-label">Kreator Kubków</span>
                        <span className="option-desc">Dobierz kształt i kolor kubka</span>
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default CreatorPage;
