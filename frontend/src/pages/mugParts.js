const partUrl = (file) => `${process.env.PUBLIC_URL}/mug-parts/${file}`;

export const MUG_BODIES = [
    { id: 'kubek-1', label: 'Kubek prosty',   url: partUrl('kubek-1.svg') },
    { id: 'kubek-2', label: 'Kubek stożkowy', url: partUrl('kubek-2.svg') },
    { id: 'kubek-3', label: 'Czarka',         url: partUrl('kubek-3.svg') },
];

export const MUG_HANDLES = [
    { id: 'ucho-1', label: 'Uszko okrągłe', url: partUrl('ucho-1.svg') },
    { id: 'ucho-2', label: 'Uszko duże',    url: partUrl('ucho-2.svg') },
];
