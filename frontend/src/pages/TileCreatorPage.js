import React, { useRef, useState, useEffect, useCallback } from 'react';
import './TileCreatorPage.css';

const iconUrl = (name, state) => `${process.env.PUBLIC_URL}/icons/${name}-${state}.svg`;

const CANVAS_SIZE = 600;
const CLAY_COLOR  = '#7a4a2a';
const GRAPHITE    = '#1e1e1e';
const TEXT_SIZE   = 48;
const MAX_UNDO    = 10;
const ZOOM_LEVELS = [480, 640, 800];

const PALETTE = [
    '#ffffff', '#f5e8c0', '#e8c84a', '#d4813a',
    '#c1613a', '#8b1a2a', '#1e50a0', '#5580c8',
    '#2a8a8a', '#3d7a4f', '#7a9a6a', '#1a2a6b',
];

const FONT_STYLES = [
    { id: 'serif',   label: 'Klasyczny', font: 'Georgia, serif' },
    { id: 'block',   label: 'Blok',      font: '"Arial Black", sans-serif' },
    { id: 'stencil', label: 'Stencil',   font: '"Courier New", monospace' },
];

const SHAPE_TYPES = ['rectangle', 'circle', 'triangle'];

function hexToRgb(hex) {
    return [parseInt(hex.slice(1,3),16), parseInt(hex.slice(3,5),16), parseInt(hex.slice(5,7),16)];
}

function floodFill(ctx, sx, sy, fillHex) {
    sx = Math.round(sx); sy = Math.round(sy);
    if (sx < 0 || sx >= CANVAS_SIZE || sy < 0 || sy >= CANVAS_SIZE) return;

    const img = ctx.getImageData(0, 0, CANVAS_SIZE, CANVAS_SIZE);
    const d   = img.data;

    const sp = (sy * CANVAS_SIZE + sx) * 4;
    const tr = d[sp], tg = d[sp+1], tb = d[sp+2], ta = d[sp+3];

    const [fr, fg, fb] = hexToRgb(fillHex);
    if (tr===fr && tg===fg && tb===fb && ta===255) return;

    const stack = [sy * CANVAS_SIZE + sx];
    const vis   = new Uint8Array(CANVAS_SIZE * CANVAS_SIZE);
    vis[sy * CANVAS_SIZE + sx] = 1;

    while (stack.length) {
        const pos = stack.pop();
        const i   = pos * 4;
        d[i]=fr; d[i+1]=fg; d[i+2]=fb; d[i+3]=255;

        const x = pos % CANVAS_SIZE;
        const y = (pos / CANVAS_SIZE) | 0;

        const check = (n) => {
            if (vis[n]) return;
            const ni = n * 4;
            if (d[ni]===tr && d[ni+1]===tg && d[ni+2]===tb && d[ni+3]===ta) {
                vis[n] = 1;
                stack.push(n);
            }
        };
        if (x > 0)             check(pos - 1);
        if (x < CANVAS_SIZE-1) check(pos + 1);
        if (y > 0)             check(pos - CANVAS_SIZE);
        if (y < CANVAS_SIZE-1) check(pos + CANVAS_SIZE);
    }

    ctx.putImageData(img, 0, 0);
}

function drawShapeOnCtx(ctx, type, x1, y1, x2, y2, lw) {
    const lx=Math.min(x1,x2), ly=Math.min(y1,y2), rx=Math.max(x1,x2), ry=Math.max(y1,y2);
    ctx.beginPath();
    if (type === 'rectangle') {
        ctx.rect(lx, ly, rx-lx, ry-ly);
    } else if (type === 'circle') {
        ctx.ellipse((lx+rx)/2,(ly+ry)/2,(rx-lx)/2,(ry-ly)/2,0,0,Math.PI*2);
    } else {
        ctx.moveTo((lx+rx)/2, ly); ctx.lineTo(rx, ry); ctx.lineTo(lx, ry); ctx.closePath();
    }
    ctx.strokeStyle = CLAY_COLOR;
    ctx.lineWidth   = lw;
    ctx.lineCap     = 'round';
    ctx.lineJoin    = 'round';
    ctx.stroke();
}

/* ── Icon buttons ── */

// Tool buttons: 3 states (default / hover / active)
function ToolBtn({ name, isActive, onClick, title }) {
    const [hov, setHov] = useState(false);
    const state = isActive ? 'active' : hov ? 'hover' : 'default';
    return (
        <button className="tb-btn" onClick={onClick} title={title}
            onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}>
            <img src={iconUrl(name, state)} alt={title} className="tb-icon-img" />
        </button>
    );
}

// Utility buttons (undo, zoom): 2 states (default / hover), no active
function UtilBtn({ name, onClick, title }) {
    const [hov, setHov] = useState(false);
    return (
        <button className="tb-btn" onClick={onClick} title={title}
            onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}>
            <img src={iconUrl(name, hov ? 'hover' : 'default')} alt={title} className="tb-icon-img" />
        </button>
    );
}
const ShapePreview = ({ type }) => {
    if (type === 'rectangle') return <svg width="26" height="18" viewBox="0 0 26 18" fill="none" stroke="currentColor" strokeWidth="2"><rect x="1" y="1" width="24" height="16"/></svg>;
    if (type === 'circle')    return <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="10" cy="10" r="9"/></svg>;
    return <svg width="22" height="20" viewBox="0 0 22 20" fill="none" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"><polygon points="11 1 21 19 1 19"/></svg>;
};

function SaveModal({ onSave, onClose }) {
    return (
        <div className="modal-backdrop" onClick={onClose}>
            <div className="modal-card" onClick={e => e.stopPropagation()}>
                <h3 className="modal-title">Zapisz pracę</h3>
                <p className="modal-desc">Zapisz projekt jako plik PNG na swoim komputerze.</p>
                <button className="modal-btn-primary" onClick={() => { onSave(); onClose(); }}>Pobierz jako PNG</button>
                <button className="modal-btn-secondary" onClick={onClose}>Anuluj</button>
            </div>
        </div>
    );
}

/* ── Main component ── */
function TileCreatorPage() {
    const canvasRef  = useRef(null); // single drawing surface
    const overlayRef = useRef(null); // shape drag preview only (temp, always cleared)

    const [tool,         setTool]         = useState('pen');
    const [activeColor,  setActiveColor]  = useState('#1e50a0');
    const [penSize,      setPenSize]      = useState(5);
    const [fontStyleIdx, setFontStyleIdx] = useState(0);
    const [shapeType,    setShapeType]    = useState('rectangle');
    const [zoomIdx,      setZoomIdx]      = useState(0);
    const [textInput,    setTextInput]    = useState(null);
    const [textValue,    setTextValue]    = useState('');
    const [clearConfirm, setClearConfirm] = useState(false);
    const [saveModalOpen,setSaveModalOpen]= useState(false);
    const [tooSmall]                      = useState(() => window.innerWidth < 900);

    const isDrawingRef  = useRef(false);
    const drawStartRef  = useRef(null);
    const lastPosRef    = useRef(null);
    const historyRef    = useRef([]);
    const textInputRef  = useRef(null);

    useEffect(() => {
        if (!canvasRef.current) return;
        const ctx = canvasRef.current.getContext('2d', { willReadFrequently: true });
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
    }, []);

    useEffect(() => {
        const stop = () => {
            if (isDrawingRef.current) {
                isDrawingRef.current = false;
                overlayRef.current?.getContext('2d').clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
            }
        };
        window.addEventListener('mouseup', stop);
        return () => window.removeEventListener('mouseup', stop);
    }, []);

    const saveSnapshot = useCallback(() => {
        if (!canvasRef.current) return;
        historyRef.current.push(canvasRef.current.toDataURL());
        if (historyRef.current.length > MAX_UNDO) historyRef.current.shift();
    }, []);

    const getPos = (e) => {
        const el   = e.currentTarget || canvasRef.current;
        const rect = el.getBoundingClientRect();
        const src  = e.touches?.[0] || e.changedTouches?.[0] || e;
        return {
            x: (src.clientX - rect.left) * (CANVAS_SIZE / rect.width),
            y: (src.clientY - rect.top)  * (CANVAS_SIZE / rect.height),
        };
    };

    const getCssPos = (e) => {
        const rect = e.currentTarget.getBoundingClientRect();
        const src  = e.touches?.[0] || e;
        return { x: src.clientX - rect.left, y: src.clientY - rect.top };
    };

    const commitText = useCallback(() => {
        if (!textInput) return;
        if (textValue.trim()) {
            const ctx = canvasRef.current.getContext('2d');
            ctx.font      = `${TEXT_SIZE}px ${FONT_STYLES[fontStyleIdx].font}`;
            ctx.fillStyle = GRAPHITE;
            ctx.fillText(textValue, textInput.canvasX, textInput.canvasY);
        }
        setTextInput(null);
        setTextValue('');
    }, [textInput, textValue, fontStyleIdx]);

    const selectTool = useCallback((t) => {
        commitText();
        setTool(t);
    }, [commitText]);

    const handleMouseDown = useCallback((e) => {
        e.preventDefault();
        const pos = getPos(e);

        if (tool === 'fill') {
            saveSnapshot();
            floodFill(canvasRef.current.getContext('2d'), pos.x, pos.y, activeColor);
            return;
        }

        if (tool === 'text') {
            const css = getCssPos(e);
            setTextInput({ canvasX: pos.x, canvasY: pos.y, cssX: css.x, cssY: css.y });
            setTextValue('');
            setTimeout(() => textInputRef.current?.focus(), 0);
            return;
        }

        saveSnapshot();
        isDrawingRef.current = true;
        lastPosRef.current   = pos;
        drawStartRef.current = pos;

        if (tool === 'pen') {
            const ctx = canvasRef.current.getContext('2d');
            ctx.beginPath();
            ctx.arc(pos.x, pos.y, penSize / 2, 0, Math.PI * 2);
            ctx.fillStyle = CLAY_COLOR;
            ctx.fill();
        }
    }, [tool, activeColor, penSize, saveSnapshot]);

    const handleMouseMove = useCallback((e) => {
        e.preventDefault();
        if (!isDrawingRef.current) return;
        const pos = getPos(e);

        if (tool === 'pen') {
            const ctx = canvasRef.current.getContext('2d');
            ctx.beginPath();
            ctx.moveTo(lastPosRef.current.x, lastPosRef.current.y);
            ctx.lineTo(pos.x, pos.y);
            ctx.strokeStyle = CLAY_COLOR;
            ctx.lineWidth   = penSize;
            ctx.lineCap     = 'round';
            ctx.lineJoin    = 'round';
            ctx.stroke();
        } else if (tool === 'shapes' && drawStartRef.current) {
            const octx = overlayRef.current.getContext('2d');
            octx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
            drawShapeOnCtx(octx, shapeType, drawStartRef.current.x, drawStartRef.current.y, pos.x, pos.y, penSize);
        }

        lastPosRef.current = pos;
    }, [tool, penSize, shapeType]);

    const handleMouseUp = useCallback((e) => {
        if (!isDrawingRef.current) return;
        isDrawingRef.current = false;

        if (tool === 'shapes' && drawStartRef.current) {
            const pos = getPos(e);
            drawShapeOnCtx(canvasRef.current.getContext('2d'), shapeType, drawStartRef.current.x, drawStartRef.current.y, pos.x, pos.y, penSize);
            overlayRef.current.getContext('2d').clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        }

        lastPosRef.current   = null;
        drawStartRef.current = null;
    }, [tool, shapeType, penSize]);

    const handleUndo = useCallback(() => {
        if (!historyRef.current.length) return;
        const ctx = canvasRef.current.getContext('2d');
        const img = new Image();
        img.src    = historyRef.current.pop();
        img.onload = () => ctx.drawImage(img, 0, 0);
    }, []);

    const handleClear = useCallback(() => {
        if (!clearConfirm) {
            setClearConfirm(true);
            setTimeout(() => setClearConfirm(false), 3000);
            return;
        }
        historyRef.current = [];
        const ctx = canvasRef.current.getContext('2d');
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);
        setClearConfirm(false);
    }, [clearConfirm]);

    const handleDownload = useCallback(() => {
        const link    = document.createElement('a');
        link.download = 'kafelek.png';
        link.href     = canvasRef.current.toDataURL('image/png');
        link.click();
    }, []);

    const handleAddToCart = useCallback(() => {
        alert('Zamawianie kafelków Cuenca będzie dostępne wkrótce!');
    }, []);

    const cursorMap = { pen: 'crosshair', fill: 'crosshair', shapes: 'crosshair', text: 'text' };

    if (tooSmall) {
        return (
            <div className="tile-too-small-msg">
                <h2>Kreator Kafelków</h2>
                <p>
                    Mamy interaktywne narzędzie do projektowania własnych kafelków Cuenca —
                    ale Twój ekran jest za mały, żeby wygodnie rysować.
                    Zapraszamy na komputer lub tablet!
                </p>
            </div>
        );
    }

    return (
        <div className="tile-creator-page">
            <div className="tile-creator-workspace">

                {/* Canvas + toolbar strip side by side, toolbar constrained to tile height */}
                <div className="canvas-area">
                    <div className="canvas-and-toolbar">
                        <div className="canvas-frame" style={{ maxWidth: ZOOM_LEVELS[zoomIdx] }}>
                            <div className="canvas-wrapper">
                                <canvas ref={canvasRef}  width={CANVAS_SIZE} height={CANVAS_SIZE} className="canvas-layer" style={{ zIndex: 1 }} />
                                <canvas ref={overlayRef} width={CANVAS_SIZE} height={CANVAS_SIZE} className="canvas-layer" style={{ zIndex: 2, pointerEvents: 'none' }} />
                                {textInput && (
                                    <input
                                        ref={textInputRef}
                                        className="text-cursor-input"
                                        style={{ left: textInput.cssX, top: textInput.cssY, fontFamily: FONT_STYLES[fontStyleIdx].font, zIndex: 10 }}
                                        value={textValue}
                                        onChange={e => setTextValue(e.target.value)}
                                        onKeyDown={e => { if (e.key === 'Enter') commitText(); if (e.key === 'Escape') { setTextInput(null); setTextValue(''); } }}
                                        onBlur={commitText}
                                        placeholder="Wpisz tekst…"
                                    />
                                )}
                                <div
                                    className="canvas-event-layer"
                                    style={{ cursor: cursorMap[tool] || 'default' }}
                                    onMouseDown={handleMouseDown}
                                    onMouseMove={handleMouseMove}
                                    onMouseUp={handleMouseUp}
                                    onMouseLeave={() => { isDrawingRef.current = false; }}
                                    onTouchStart={handleMouseDown}
                                    onTouchMove={handleMouseMove}
                                    onTouchEnd={handleMouseUp}
                                />
                            </div>
                        </div>

                        <div className="toolbar-strip">
                            <ToolBtn name="pen"    isActive={tool === 'pen'}    onClick={() => selectTool('pen')}    title="Pióro" />
                            <ToolBtn name="text"   isActive={tool === 'text'}   onClick={() => selectTool('text')}   title="Tekst" />
                            <ToolBtn name="fill"   isActive={tool === 'fill'}   onClick={() => selectTool('fill')}   title="Wypełnienie" />
                            <ToolBtn name="shapes" isActive={tool === 'shapes'} onClick={() => selectTool('shapes')} title="Kształty" />
                            <div className="tb-lower">
                                <UtilBtn name="undo" onClick={handleUndo} title="Cofnij (maks. 10)" />
                                <UtilBtn name="zoom" onClick={() => setZoomIdx(i => (i + 1) % ZOOM_LEVELS.length)} title={`Zoom — ${ZOOM_LEVELS[zoomIdx]}px`} />
                            </div>
                        </div>
                    </div>
                    <p className="canvas-label">10 × 10 cm</p>
                </div>

                {/* Tool menu — appears between toolbar and info panel */}
                <div className="tool-menu-zone">
                    {tool === 'pen' && (
                        <div className="tool-menu">
                            <span className="menu-label">Grubość: {penSize}px</span>
                            <input type="range" min="2" max="20" value={penSize} onChange={e => setPenSize(+e.target.value)} className="menu-slider" />
                            <div className="clay-preview">
                                <div className="clay-swatch" />
                                <span>Kolor gliny (stały)</span>
                            </div>
                        </div>
                    )}
                    {tool === 'text' && (
                        <div className="tool-menu">
                            <span className="menu-label">Styl tekstu</span>
                            <div className="menu-font-btns">
                                {FONT_STYLES.map((fs, i) => (
                                    <button key={fs.id} className={`menu-font-btn ${fontStyleIdx === i ? 'menu-active' : ''}`} style={{ fontFamily: fs.font }} onClick={() => setFontStyleIdx(i)}>
                                        {fs.label}
                                    </button>
                                ))}
                            </div>
                            <p className="menu-hint">Kliknij kafelek, wpisz tekst,<br/>zatwierdź Enterem</p>
                        </div>
                    )}
                    {tool === 'shapes' && (
                        <div className="tool-menu">
                            <span className="menu-label">Kształt</span>
                            <div className="menu-shape-btns">
                                {SHAPE_TYPES.map(s => (
                                    <button key={s} className={`menu-shape-btn ${shapeType === s ? 'menu-active' : ''}`} onClick={() => setShapeType(s)}>
                                        <ShapePreview type={s} />
                                    </button>
                                ))}
                            </div>
                            <p className="menu-hint">Przeciągnij na kafelku</p>
                        </div>
                    )}
                    {tool === 'fill' && (
                        <div className="tool-menu">
                            <span className="menu-label">Kolor wypełnienia</span>
                            <div className="menu-palette">
                                {PALETTE.map(hex => (
                                    <button key={hex} className={`menu-swatch ${activeColor === hex ? 'menu-swatch-active' : ''}`} style={{ background: hex }} onClick={() => setActiveColor(hex)} />
                                ))}
                            </div>
                        </div>
                    )}
                </div>

                {/* Info panel — sticky */}
                <div className="tile-info-panel">
                    <h2 className="tile-info-title">Zaprojektuj swoją płytkę!</h2>
                    <p className="tile-info-desc">
                        opis opis opis
                    </p>
                    <div className="tile-price">200 zł</div>
                    <button className="add-to-cart-btn" onClick={handleAddToCart}>Dodaj Do Koszyka</button>
                    <div className="info-bottom-btns">
                        <button className="bottom-btn" onClick={() => setSaveModalOpen(true)}>Zapisz Pracę</button>
                        <button className="bottom-btn bottom-btn-danger" onClick={handleClear}>
                            {clearConfirm ? 'Na pewno?' : 'Zacznij od nowa'}
                        </button>
                    </div>
                </div>
            </div>

            {saveModalOpen && <SaveModal onSave={handleDownload} onClose={() => setSaveModalOpen(false)} />}
        </div>
    );
}

export default TileCreatorPage;
