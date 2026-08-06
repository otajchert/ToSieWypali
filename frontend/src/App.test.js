import { render, screen } from '@testing-library/react';
import App from './App';

beforeEach(() => {
    global.fetch = jest.fn(() =>
        Promise.resolve({ ok: true, json: () => Promise.resolve([]) })
    );
});

test('renders without crashing and shows the nav bar', async () => {
    render(<App />);
    expect(await screen.findByText('TSW!')).toBeInTheDocument();
});
