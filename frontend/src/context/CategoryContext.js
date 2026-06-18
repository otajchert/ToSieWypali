import React, { createContext, useContext, useEffect, useState } from 'react';

const CategoryContext = createContext();

export function CategoryProvider({ children }) {
    const [categories, setCategories] = useState([]);

    useEffect(() => {
        let cancelled = false;
        function load(attempt) {
            fetch('/api/categories')
                .then(res => res.ok ? res.json() : Promise.reject())
                .then(data => { if (!cancelled) setCategories(data); })
                .catch(() => {
                    if (!cancelled && attempt < 4) {
                        setTimeout(() => load(attempt + 1), 2000);
                    }
                });
        }
        load(0);
        return () => { cancelled = true; };
    }, []);

    return (
        <CategoryContext.Provider value={{ categories, setCategories }}>
            {children}
        </CategoryContext.Provider>
    );
}

export function useCategories() {
    return useContext(CategoryContext);
}
