import React, { Suspense, useEffect, useMemo, useRef, useState } from 'react';
import * as THREE from 'three';
import { Canvas, useFrame, useLoader, useThree } from '@react-three/fiber';
import { OrbitControls, SVGLoader } from 'three-stdlib';
import { MUG_BODIES, MUG_HANDLES } from './mugParts';
import './MugCreatorPage.css';

const iconUrl = (name, state) => `${process.env.PUBLIC_URL}/icons/${name}-${state}.svg`;

const SVG_REF_SIZE   = 512;                       
const WORLD_SIZE     = 1.6;                       
const PX_TO_WORLD    = WORLD_SIZE / SVG_REF_SIZE;
const WALL           = 0.055;                     // wall thickness
const HANDLE_DEPTH   = 45;                        // depth of extrusion
const HANDLE_OVERLAP = 0.07;                      // overlap space between handle and cup wall
const MAX_HANDLES    = 4;
const MAX_UNDO       = 10;
const PROFILE_STEPS  = 40;
const DRAG_THETA     = 0.012;                     
const DRAG_HEIGHT    = 0.004;                     

const PALETTE = ['#f5e8c0', '#8b1a2a', '#1a2a6b', '#e8c84a', '#5580c8'];

// where consecutive handles land: right, left, back, front
const HANDLE_ANGLES = [0, Math.PI, Math.PI / 2, -Math.PI / 2];

const initialDesign = () => ({
    bodyId:  MUG_BODIES[0].id,
    color:   PALETTE[0],
    handles: [{ uid: 1, partId: MUG_HANDLES[0].id, theta: 0, heightF: 0.5 }],
});

const clamp = (v, lo, hi) => Math.min(Math.max(v, lo), hi);

// only filled paths become clay — outline/decor strokes are ignored
const filledPaths = (paths) =>
    paths.filter(path => {
        const fill = path.userData && path.userData.style && path.userData.style.fill;
        return fill && fill !== 'none';
    });

function collectLoops(paths) {
    const loops = [];
    filledPaths(paths).forEach(path => {
        SVGLoader.createShapes(path).forEach(shape => {
            const pts = shape.extractPoints(48);
            loops.push(pts.shape, ...pts.holes);
        });
    });
    return loops;
}

// silhouette -> radius per height level -> hollow lathe geometry
function useBodyData(url) {
    const svgData = useLoader(SVGLoader, url);
    return useMemo(() => {
        const loops = collectLoops(svgData.paths);
        if (!loops.length) {
            console.warn(`Kreator kubków: brak wypełnionych kształtów w ${url}`);
            return { geometry: new THREE.BufferGeometry(), height: WORLD_SIZE * 0.5, radiusAt: () => 0.25 };
        }
        let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
        loops.forEach(loop => loop.forEach(p => {
            minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
            minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
        }));
        const cx    = (minX + maxX) / 2;
        const svgH  = maxY - minY;
        const inset = svgH * 0.005; // sample just inside the extremes so the rim keeps its radius

        const radii = [];
        for (let i = 0; i <= PROFILE_STEPS; i++) {
            const y = maxY - inset - ((svgH - 2 * inset) * i) / PROFILE_STEPS;
            let r = 0;
            loops.forEach(loop => {
                for (let j = 0; j < loop.length; j++) {
                    const a = loop[j], b = loop[(j + 1) % loop.length];
                    if (a.y === b.y) continue;
                    if ((y - a.y) * (y - b.y) > 0) continue;
                    const x = a.x + ((y - a.y) / (b.y - a.y)) * (b.x - a.x);
                    r = Math.max(r, Math.abs(x - cx));
                }
            });
            radii.push(Math.max(r * PX_TO_WORLD, 0.01));
        }

        const height = svgH * PX_TO_WORLD;
        const step   = height / PROFILE_STEPS;

        const profile = [new THREE.Vector2(0.001, 0)];
        radii.forEach((r, i) => profile.push(new THREE.Vector2(r, i * step)));
        profile.push(new THREE.Vector2(Math.max(radii[PROFILE_STEPS] - WALL, 0.02), height));
        for (let i = PROFILE_STEPS - 1; i >= 0; i--) {
            const h = i * step;
            if (h < WALL * 1.6) break;
            profile.push(new THREE.Vector2(Math.max(radii[i] - WALL, 0.02), h));
        }
        profile.push(new THREE.Vector2(0.001, WALL * 1.6));

        const geometry = new THREE.LatheGeometry(profile, 72);
        const radiusAt = (h) => {
            const t  = clamp(h / height, 0, 1) * PROFILE_STEPS;
            const lo = Math.floor(t), hi = Math.min(lo + 1, PROFILE_STEPS);
            return radii[lo] + (radii[hi] - radii[lo]) * (t - lo);
        };
        return { geometry, height, radiusAt };
    }, [svgData, url]);
}

function useHandleGeometry(url) {
    const svgData = useLoader(SVGLoader, url);
    return useMemo(() => {
        const shapes = filledPaths(svgData.paths).flatMap(path => SVGLoader.createShapes(path));
        if (!shapes.length) {
            console.warn(`Kreator kubków: brak wypełnionych kształtów w ${url}`);
            return { geometry: new THREE.BufferGeometry(), halfH: 0.1 };
        }
        const geometry = new THREE.ExtrudeGeometry(shapes, {
            depth: HANDLE_DEPTH,
            bevelEnabled: true,
            bevelThickness: 5,
            bevelSize: 5,
            bevelSegments: 2,
            curveSegments: 20,
        });
        // flip Y (svg is y-down) and Z together so triangle winding stays correct
        geometry.scale(PX_TO_WORLD, -PX_TO_WORLD, -PX_TO_WORLD);
        geometry.computeBoundingBox();
        const bb = geometry.boundingBox;
        // anchor: left edge (cup side) at x=0, centered vertically and in depth
        geometry.translate(-bb.min.x, -(bb.min.y + bb.max.y) / 2, -(bb.min.z + bb.max.z) / 2);
        const halfH = (bb.max.y - bb.min.y) / 2;
        return { geometry, halfH };
    }, [svgData, url]);
}

function MugMaterial({ color, highlight }) {
    return (
        <meshStandardMaterial
            color={color}
            roughness={0.35}
            metalness={0.05}
            side={THREE.DoubleSide}
            emissive="#ffffff"
            emissiveIntensity={highlight ? 0.22 : 0}
        />
    );
}

function Handle({ spec, url, body, color, selected, controlsEnabledRef, onSelect, onDragStart, onDragMove, onDragEnd }) {
    const { geometry, halfH } = useHandleGeometry(url);

    const minF    = Math.min((halfH + 0.03) / body.height, 0.45);
    const centerH = clamp(spec.heightF, minF, 0.92) * body.height;
    const attachR = Math.max(body.radiusAt(centerH) - HANDLE_OVERLAP, 0.02);

    const handlePointerDown = (e) => {
        e.stopPropagation();
        onSelect(spec.uid);
        controlsEnabledRef.current = false;
        onDragStart();
        const start = { x: e.clientX, y: e.clientY, theta: spec.theta, heightF: spec.heightF };
        let moved = false;
        const move = (ev) => {
            if (!moved && Math.abs(ev.clientX - start.x) + Math.abs(ev.clientY - start.y) < 4) return;
            moved = true;
            onDragMove(spec.uid, {
                theta:   start.theta + (ev.clientX - start.x) * DRAG_THETA,
                heightF: clamp(start.heightF - (ev.clientY - start.y) * DRAG_HEIGHT, 0.05, 0.95),
            });
        };
        const up = () => {
            window.removeEventListener('pointermove', move);
            window.removeEventListener('pointerup', up);
            controlsEnabledRef.current = true;
            onDragEnd(moved);
        };
        window.addEventListener('pointermove', move);
        window.addEventListener('pointerup', up);
    };

    return (
        <group rotation-y={spec.theta}>
            <mesh
                geometry={geometry}
                position={[attachR, centerH, 0]}
                onPointerDown={handlePointerDown}
                onPointerOver={() => { document.body.style.cursor = 'grab'; }}
                onPointerOut={() => { document.body.style.cursor = ''; }}
            >
                <MugMaterial color={color} highlight={selected} />
            </mesh>
        </group>
    );
}

function MugScene({ design, selectedUid, controlsEnabledRef, onSelect, onDragStart, onDragMove, onDragEnd }) {
    const bodyPart = MUG_BODIES.find(b => b.id === design.bodyId) || MUG_BODIES[0];
    const body     = useBodyData(bodyPart.url);
    return (
        <>
            <mesh geometry={body.geometry} onPointerDown={() => onSelect(null)}>
                <MugMaterial color={design.color} />
            </mesh>
            {design.handles.map(h => (
                <Handle
                    key={h.uid}
                    spec={h}
                    url={(MUG_HANDLES.find(p => p.id === h.partId) || MUG_HANDLES[0]).url}
                    body={body}
                    color={design.color}
                    selected={h.uid === selectedUid}
                    controlsEnabledRef={controlsEnabledRef}
                    onSelect={onSelect}
                    onDragStart={onDragStart}
                    onDragMove={onDragMove}
                    onDragEnd={onDragEnd}
                />
            ))}
        </>
    );
}

function Controls({ enabledRef }) {
    const { camera, gl } = useThree();
    const ref = useRef(null);
    useEffect(() => {
        const c = new OrbitControls(camera, gl.domElement);
        c.target.set(0, WORLD_SIZE * 0.38, 0);
        c.enableDamping  = true;
        c.enablePan      = false;
        c.minDistance    = 2.0;
        c.maxDistance    = 6.5;
        c.maxPolarAngle  = Math.PI * 0.58;
        ref.current = c;
        return () => c.dispose();
    }, [camera, gl]);
    useFrame(() => {
        if (ref.current) {
            ref.current.enabled = enabledRef.current;
            ref.current.update();
        }
    });
    return null;
}

function ShadowBlob() {
    const texture = useMemo(() => {
        const c = document.createElement('canvas');
        c.width = c.height = 128;
        const ctx = c.getContext('2d');
        const g = ctx.createRadialGradient(64, 64, 6, 64, 64, 64);
        g.addColorStop(0, 'rgba(30,20,15,0.30)');
        g.addColorStop(1, 'rgba(30,20,15,0)');
        ctx.fillStyle = g;
        ctx.fillRect(0, 0, 128, 128);
        return new THREE.CanvasTexture(c);
    }, []);
    return (
        <mesh rotation-x={-Math.PI / 2} position={[0, 0.001, 0]}>
            <planeGeometry args={[3, 3]} />
            <meshBasicMaterial map={texture} transparent depthWrite={false} />
        </mesh>
    );
}

function PartBtn({ part, isActive, onClick, zoom = 1 }) {
    return (
        <button
            className={`mug-part-btn ${isActive ? 'mug-part-active' : ''}`}
            onClick={onClick}
            title={part.label}
        >
            <img src={part.url} alt={part.label} className="mug-part-img" style={{ transform: `scale(${zoom})` }} />
        </button>
    );
}

function UtilBtn({ name, onClick, title }) {
    const [hov, setHov] = useState(false);
    return (
        <button className="mug-util-btn" onClick={onClick} title={title}
            onMouseEnter={() => setHov(true)} onMouseLeave={() => setHov(false)}>
            <img src={iconUrl(name, hov ? 'hover' : 'default')} alt={title} className="mug-util-img" />
        </button>
    );
}

function MugCreatorPage() {
    const [design, setDesign]           = useState(initialDesign);
    const [selectedUid, setSelectedUid] = useState(null);
    const [resetConfirm, setResetConfirm] = useState(false);
    const [note, setNote] = useState(null);
    const [tooSmall]      = useState(() => window.innerWidth < 900);

    const designRef          = useRef(design);
    const undoRef            = useRef([]);
    const dragSnapRef        = useRef(null);
    const uidRef             = useRef(2);
    const controlsEnabledRef = useRef(true);
    const noteTimerRef       = useRef(null);

    designRef.current = design;

    useEffect(() => () => clearTimeout(noteTimerRef.current), []);

    const showNote = (msg) => {
        setNote(msg);
        clearTimeout(noteTimerRef.current);
        noteTimerRef.current = setTimeout(() => setNote(null), 3200);
    };

    const pushUndo = (snap) => {
        undoRef.current.push(snap);
        if (undoRef.current.length > MAX_UNDO) undoRef.current.shift();
    };

    const selectBody = (id) => {
        if (id === design.bodyId) return;
        pushUndo(design);
        setDesign({ ...design, bodyId: id });
    };

    const addHandle = (partId) => {
        if (design.handles.length >= MAX_HANDLES) {
            showNote('Twój projekt ma za dużo uszek — maksymalnie 4!');
            return;
        }
        pushUndo(design);
        const handle = {
            uid:     uidRef.current++,
            partId,
            theta:   HANDLE_ANGLES[design.handles.length % HANDLE_ANGLES.length],
            heightF: 0.5,
        };
        setDesign({ ...design, handles: [...design.handles, handle] });
    };

    const selectColor = (color) => {
        if (color === design.color) return;
        pushUndo(design);
        setDesign({ ...design, color });
    };

    const handleUndo = () => {
        const prev = undoRef.current.pop();
        if (prev) {
            setSelectedUid(null);
            setDesign(prev);
        }
    };

    const deleteSelected = () => {
        const exists = design.handles.some(h => h.uid === selectedUid);
        if (!exists) {
            showNote('Najpierw kliknij uszko, które chcesz usunąć');
            return;
        }
        pushUndo(design);
        setDesign({ ...design, handles: design.handles.filter(h => h.uid !== selectedUid) });
        setSelectedUid(null);
    };

    const handleReset = () => {
        if (!resetConfirm) {
            setResetConfirm(true);
            setTimeout(() => setResetConfirm(false), 3000);
            return;
        }
        undoRef.current = [];
        uidRef.current  = 2;
        setSelectedUid(null);
        setDesign(initialDesign());
        setResetConfirm(false);
    };

    const onDragStart = () => { dragSnapRef.current = designRef.current; };

    const onDragMove = (uid, patch) => {
        setDesign(d => ({
            ...d,
            handles: d.handles.map(h => (h.uid === uid ? { ...h, ...patch } : h)),
        }));
    };

    const onDragEnd = (moved) => {
        if (moved && dragSnapRef.current) pushUndo(dragSnapRef.current);
        dragSnapRef.current = null;
    };

    const handleAddToCart = () => {
        alert('Zamawianie własnych kubków będzie dostępne wkrótce!');
    };

    if (tooSmall) {
        return (
            <div className="mug-too-small-msg">
                <h2>Kreator Kubków</h2>
                <p>Twój ekran jest za mały</p>
            </div>
        );
    }

    return (
        <div className="mug-creator-page">
            <div className="mug-workspace">

                <div className="mug-canvas-area">
                    <div className="mug-canvas-frame">
                        <div className="mug-canvas-inner">
                            <Canvas camera={{ position: [0, 1.15, 3.6], fov: 38 }} dpr={[1, 2]} flat
                                onPointerMissed={() => setSelectedUid(null)}>
                                <ambientLight intensity={1.0} />
                                <directionalLight position={[4, 6, 4]} intensity={1.4} />
                                <directionalLight position={[-5, 3, -2]} intensity={0.5} />
                                <Suspense fallback={null}>
                                    <MugScene
                                        design={design}
                                        selectedUid={selectedUid}
                                        controlsEnabledRef={controlsEnabledRef}
                                        onSelect={setSelectedUid}
                                        onDragStart={onDragStart}
                                        onDragMove={onDragMove}
                                        onDragEnd={onDragEnd}
                                    />
                                </Suspense>
                                <ShadowBlob />
                                <Controls enabledRef={controlsEnabledRef} />
                            </Canvas>
                        </div>
                        {note && <div className="mug-note">{note}</div>}
                    </div>
                    <p className="mug-canvas-hint">Obracaj kubek przeciągając tło · złap uszko, aby je przestawić lub zaznaczyć</p>
                </div>

                <div className="mug-parts-strip">
                    {MUG_BODIES.map(b => (
                        <PartBtn key={b.id} part={b} isActive={design.bodyId === b.id} onClick={() => selectBody(b.id)} zoom={1.7} />
                    ))}
                    <div className="mug-strip-divider" />
                    {MUG_HANDLES.map(h => (
                        <PartBtn key={h.id} part={h} onClick={() => addHandle(h.id)} zoom={2.8} />
                    ))}
                    <div className="mug-strip-divider" />
                    <UtilBtn name="undo" onClick={handleUndo} title="Cofnij (maks. 10)" />
                    <UtilBtn name="delete" onClick={deleteSelected} title="Usuń zaznaczone uszko" />
                </div>

                <div className="mug-info-panel">
                    <h2 className="mug-info-title">Zaprojektuj swój kubek!</h2>
                    <p className="mug-info-desc">dobierz kształt i kolor kubka</p>
                    <div className="mug-palette">
                        {PALETTE.map(hex => (
                            <button
                                key={hex}
                                className={`mug-swatch ${design.color === hex ? 'mug-swatch-active' : ''}`}
                                style={{ background: hex }}
                                onClick={() => selectColor(hex)}
                            />
                        ))}
                    </div>
                    <div className="mug-price">200 zł</div>
                    <button className="mug-add-to-cart-btn" onClick={handleAddToCart}>Dodaj Do Koszyka</button>
                    <button className="mug-bottom-btn" onClick={handleReset}>
                        {resetConfirm ? 'Na pewno?' : 'Zacznij od nowa'}
                    </button>
                </div>
            </div>
        </div>
    );
}

export default MugCreatorPage;
