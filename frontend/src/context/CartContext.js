import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

const GUEST_KEY = 'tsw_guest_cart';
const NOTIFIED_KEY = 'tsw_cart_notified';

function readGuest() {
    try { return JSON.parse(localStorage.getItem(GUEST_KEY) || '[]'); }
    catch { return []; }
}

function saveGuest(items) {
    localStorage.setItem(GUEST_KEY, JSON.stringify(items));
}

function normalizeItem(si) {
    const p = si.product;
    return {
        id: si.id,
        productId: p.id,
        name: p.name,
        price: p.price,
        photo: p.photo || p.images?.[0]?.imageUrl || null,
        qty: si.qty,
        qtyInStock: p.qtyInStock,
    };
}

export function CartProvider({ children }) {
    const { user } = useAuth();
    const [items, setItems] = useState([]);
    const [cartReady, setCartReady] = useState(false);
    const prevUserIdRef = useRef(null);

    async function fetchFromServer(userId, token) {
        try {
            const res = await fetch(`/api/cart/${userId}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (!res.ok) return null;
            return (await res.json()).map(normalizeItem);
        } catch {
            return null;
        }
    }

    async function mergeGuestToServer(userId, token) {
        const guests = readGuest();
        if (guests.length === 0) return;
        for (const item of guests) {
            try {
                await fetch(`/api/cart/${userId}/items`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({ productId: item.productId, qty: item.qty }),
                });
            } catch {}
        }
        saveGuest([]);
    }

    async function refreshCart() {
        if (!user) return null;
        const fresh = await fetchFromServer(user.id, user.token);
        if (fresh) setItems(fresh);
        return fresh;
    }

    useEffect(() => {
        if (!user) {
            prevUserIdRef.current = null;
            setItems(readGuest());
            setCartReady(true);
            return;
        }

        const isNewLogin = prevUserIdRef.current !== user.id;
        prevUserIdRef.current = user.id;

        (async () => {
            if (isNewLogin) {
                await mergeGuestToServer(user.id, user.token);
            }
            const fresh = await fetchFromServer(user.id, user.token);
            if (fresh) setItems(fresh);
            setCartReady(true);
        })();
    }, [user?.id]); // intentionally omits functions — they'd change every render

    // Poll every 30s and refresh on window focus for cross-device sync
    useEffect(() => {
        if (!user) return;
        const { id: userId, token } = user;

        const poll = setInterval(async () => {
            const fresh = await fetchFromServer(userId, token);
            if (fresh) setItems(fresh);
        }, 30000);

        const onFocus = async () => {
            const fresh = await fetchFromServer(userId, token);
            if (fresh) setItems(fresh);
        };
        window.addEventListener('focus', onFocus);

        return () => {
            clearInterval(poll);
            window.removeEventListener('focus', onFocus);
        };
    }, [user?.id]); // intentionally omits functions — they'd change every render

    async function addItem(product, qty) {
        if (user) {
            const res = await fetch(`/api/cart/${user.id}/items`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${user.token}`,
                },
                body: JSON.stringify({ productId: product.id, qty }),
            });
            if (!res.ok) {
                const msg = await res.text();
                throw new Error(msg || 'Błąd dodawania do koszyka');
            }
            const fresh = await fetchFromServer(user.id, user.token);
            if (fresh) setItems(fresh);
        } else {
            const current = readGuest();
            const idx = current.findIndex(i => i.productId === product.id);
            let updated;
            if (idx >= 0) {
                updated = current.map((i, j) =>
                    j === idx ? { ...i, qty: Math.min(i.qty + qty, product.qtyInStock) } : i
                );
            } else {
                updated = [...current, {
                    id: null,
                    productId: product.id,
                    name: product.name,
                    price: product.price,
                    photo: product.photo || product.images?.[0]?.imageUrl || null,
                    qty: Math.min(qty, product.qtyInStock),
                    qtyInStock: product.qtyInStock,
                }];
            }
            saveGuest(updated);
            setItems(updated);
        }

        const isFirst = !localStorage.getItem(NOTIFIED_KEY);
        if (isFirst) localStorage.setItem(NOTIFIED_KEY, '1');
        return isFirst;
    }

    async function removeItem(item) {
        if (user) {
            await fetch(`/api/cart/${user.id}/items/${item.id}`, {
                method: 'DELETE',
                headers: { Authorization: `Bearer ${user.token}` },
            });
            setItems(prev => prev.filter(i => i.id !== item.id));
        } else {
            const updated = readGuest().filter(i => i.productId !== item.productId);
            saveGuest(updated);
            setItems(updated);
        }
    }

    async function updateQty(item, qty) {
        if (qty < 1) return;
        if (user) {
            const res = await fetch(`/api/cart/${user.id}/items/${item.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${user.token}`,
                },
                body: JSON.stringify({ qty }),
            });
            if (res.ok) {
                setItems(prev => prev.map(i => i.id === item.id ? { ...i, qty } : i));
            }
        } else {
            const updated = readGuest().map(i =>
                i.productId === item.productId ? { ...i, qty } : i
            );
            saveGuest(updated);
            setItems(updated);
        }
    }

    const totalCount = items.reduce((sum, i) => sum + i.qty, 0);

    return (
        <CartContext.Provider value={{ items, cartReady, addItem, removeItem, updateQty, totalCount, refreshCart }}>
            {children}
        </CartContext.Provider>
    );
}

export function useCart() {
    return useContext(CartContext);
}
