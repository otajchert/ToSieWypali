import { readApiError } from './apiErrors';

function response(body) {
    return {
        text: () => Promise.resolve(body),
    };
}

test('uses translated field errors before the general problem code', async () => {
    const error = await readApiError(response(JSON.stringify({
        code: 'VALIDATION_FAILED',
        detail: 'Request validation failed',
        errors: {
            email: ['INVALID_EMAIL'],
            password: ['PASSWORD_TOO_SHORT'],
        },
    })), 'Nie udało się zapisać danych.');

    expect(error.message).toBe(
        'Podaj prawidłowy adres email. Hasło musi mieć co najmniej 8 znaków.'
    );
});

test('translates a stable problem code', async () => {
    const error = await readApiError(response(JSON.stringify({
        code: 'EMAIL_ALREADY_USED',
        detail: 'Email already used',
    })));

    expect(error.message).toBe('Ten adres email jest już zajęty.');
});

test('preserves legacy plain text errors', async () => {
    const error = await readApiError(response('Starszy komunikat błędu'));

    expect(error.message).toBe('Starszy komunikat błędu');
});

test('returns the caller fallback for an empty response', async () => {
    const error = await readApiError(response(''), 'Nie udało się usunąć produktu.');

    expect(error.message).toBe('Nie udało się usunąć produktu.');
});

test('keeps additional ProblemDetail properties', async () => {
    const error = await readApiError(response(JSON.stringify({
        code: 'CATEGORY_NOT_EMPTY',
        productCount: 3,
    })));

    expect(error.problem.productCount).toBe(3);
});
