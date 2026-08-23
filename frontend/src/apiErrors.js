const ERROR_MESSAGES = {
    VALIDATION_FAILED: 'Sprawdź poprawność wprowadzonych danych.',
    INVALID_REQUEST: 'Wysłane dane są nieprawidłowe.',
    INVALID_QUANTITY: 'Ilość musi być większa od zera.',
    INVALID_CATEGORY_HIERARCHY: 'Nieprawidłowa struktura kategorii.',
    NOT_ACCEPTABLE: 'Nie można przygotować odpowiedzi w wymaganym formacie.',
    METHOD_NOT_ALLOWED: 'Ta operacja nie jest dostępna.',
    UNSUPPORTED_MEDIA_TYPE: 'Format wysłanych danych nie jest obsługiwany.',
    INVALID_CREDENTIALS: 'Nieprawidłowy email lub hasło.',
    UNAUTHORIZED: 'Zaloguj się, aby wykonać tę operację.',
    FORBIDDEN: 'Nie masz uprawnień do wykonania tej operacji.',
    RESOURCE_NOT_FOUND: 'Nie znaleziono zasobu.',
    CLIENT_NOT_FOUND: 'Nie znaleziono klienta.',
    PRODUCT_NOT_FOUND: 'Nie znaleziono produktu.',
    PRODUCT_IMAGE_NOT_FOUND: 'Nie znaleziono zdjęcia produktu.',
    CATEGORY_NOT_FOUND: 'Nie znaleziono kategorii.',
    ADDRESS_NOT_FOUND: 'Nie znaleziono adresu.',
    ORDER_NOT_FOUND: 'Nie znaleziono zamówienia.',
    ORDER_STATUS_NOT_FOUND: 'Nie znaleziono statusu zamówienia.',
    SHIPPING_METHOD_NOT_FOUND: 'Nie znaleziono metody dostawy.',
    CART_ITEM_NOT_FOUND: 'Nie znaleziono produktu w koszyku.',
    EMAIL_ALREADY_USED: 'Ten adres email jest już zajęty.',
    INSUFFICIENT_STOCK: 'Brak wystarczającej ilości produktu w magazynie.',
    INVALID_ORDER_STATUS_TRANSITION: 'Ta zmiana statusu zamówienia nie jest dozwolona.',
    PRODUCT_IN_USE: 'Nie można usunąć produktu przypisanego do zamówienia.',
    CATEGORY_NOT_EMPTY: 'Kategoria jest przypisana do produktów.',
    DATA_CONFLICT: 'Nie można wykonać operacji z powodu konfliktu danych.',
    FILE_TOO_LARGE: 'Przesłany plik jest zbyt duży.',
    STORAGE_OPERATION_FAILED: 'Nie udało się zapisać pliku.',
    ORDER_CONFIGURATION_ERROR: 'Nie udało się przygotować zamówienia.',
    INTERNAL_ERROR: 'Wystąpił nieoczekiwany błąd.',
    EMAIL_REQUIRED: 'Podaj adres email.',
    INVALID_EMAIL: 'Podaj prawidłowy adres email.',
    EMAIL_TOO_LONG: 'Adres email jest zbyt długi.',
    PASSWORD_REQUIRED: 'Podaj hasło.',
    PASSWORD_TOO_SHORT: 'Hasło musi mieć co najmniej 8 znaków.',
    PASSWORD_TOO_LONG: 'Hasło jest zbyt długie.',
    FIRST_NAME_REQUIRED: 'Podaj imię.',
    FIRST_NAME_TOO_LONG: 'Imię jest zbyt długie.',
    LAST_NAME_REQUIRED: 'Podaj nazwisko.',
    LAST_NAME_TOO_LONG: 'Nazwisko jest zbyt długie.',
    PHONE_NUMBER_TOO_LONG: 'Numer telefonu jest zbyt długi.',
    ADDRESS_NAME_TOO_LONG: 'Nazwa adresu jest zbyt długa.',
    CITY_REQUIRED: 'Podaj miasto.',
    CITY_TOO_LONG: 'Nazwa miasta jest zbyt długa.',
    REGION_REQUIRED: 'Podaj województwo.',
    REGION_TOO_LONG: 'Nazwa województwa jest zbyt długa.',
    POSTAL_CODE_REQUIRED: 'Podaj kod pocztowy.',
    POSTAL_CODE_TOO_LONG: 'Kod pocztowy jest zbyt długi.',
    STREET_NUMBER_REQUIRED: 'Podaj ulicę i numer budynku.',
    STREET_NUMBER_TOO_LONG: 'Ulica i numer budynku są zbyt długie.',
    FLAT_TOO_LONG: 'Numer mieszkania jest zbyt długi.',
    CATEGORY_NAME_REQUIRED: 'Podaj nazwę kategorii.',
    CATEGORY_NAME_TOO_LONG: 'Nazwa kategorii jest zbyt długa.',
    PRODUCT_NAME_REQUIRED: 'Podaj nazwę produktu.',
    PRODUCT_NAME_TOO_LONG: 'Nazwa produktu jest zbyt długa.',
    PRODUCT_DESCRIPTION_TOO_LONG: 'Opis produktu jest zbyt długi.',
    PRICE_REQUIRED: 'Podaj cenę produktu.',
    PRICE_MUST_BE_POSITIVE: 'Cena produktu musi być większa od zera.',
    INVALID_PRICE_FORMAT: 'Cena może mieć najwyżej dwie cyfry po przecinku.',
    STOCK_QUANTITY_REQUIRED: 'Podaj stan magazynowy.',
    STOCK_QUANTITY_MUST_NOT_BE_NEGATIVE: 'Stan magazynowy nie może być ujemny.',
    WEIGHT_TOO_LONG: 'Waga produktu jest zbyt długa.',
    HEIGHT_TOO_LONG: 'Wysokość produktu jest zbyt długa.',
    WIDTH_TOO_LONG: 'Szerokość produktu jest zbyt długa.',
    PRODUCT_LENGTH_TOO_LONG: 'Głębokość produktu jest zbyt długa.',
    CATEGORY_IDS_REQUIRED: 'Wybierz kategorie produktu.',
    CATEGORY_ID_REQUIRED: 'Wybrana kategoria jest nieprawidłowa.',
    TOO_MANY_CATEGORIES: 'Produkt ma przypisanych zbyt wiele kategorii.',
    IMAGE_URL_REQUIRED: 'Podaj adres zdjęcia.',
    IMAGE_URL_TOO_LONG: 'Adres zdjęcia jest zbyt długi.',
    INVALID_IMAGE_URL: 'Podaj prawidłowy adres HTTPS zdjęcia.',
    PRODUCT_ID_REQUIRED: 'Produkt jest wymagany.',
    QUANTITY_REQUIRED: 'Podaj ilość produktu.',
    QUANTITY_MUST_BE_POSITIVE: 'Ilość musi być większa od zera.',
    ORDER_ITEMS_REQUIRED: 'Zamówienie musi zawierać co najmniej jeden produkt.',
    ORDER_ITEM_REQUIRED: 'Pozycja zamówienia jest wymagana.',
    TOO_MANY_ORDER_ITEMS: 'Zamówienie zawiera zbyt wiele produktów.',
    SHIPPING_METHOD_REQUIRED: 'Wybierz metodę dostawy.',
    ORDER_STATUS_REQUIRED: 'Wybierz status zamówienia.',
};

const CODE_PATTERN = /^[A-Z][A-Z0-9_]*$/;

function translatedMessage(value) {
    if (typeof value !== 'string' || !value.trim()) return null;
    return ERROR_MESSAGES[value] || (CODE_PATTERN.test(value) ? null : value);
}

function fieldMessages(errors) {
    if (!errors || typeof errors !== 'object' || Array.isArray(errors)) return [];

    const messages = Object.values(errors)
        .flatMap(value => Array.isArray(value) ? value : [value])
        .map(translatedMessage)
        .filter(Boolean);

    return [...new Set(messages)];
}

function messageFromProblem(problem, fallback) {
    const messages = fieldMessages(problem.errors);
    if (messages.length > 0) return messages.join(' ');

    return translatedMessage(problem.code)
        || translatedMessage(problem.detail)
        || translatedMessage(problem.message)
        || fallback;
}

export async function readApiError(response, fallback = 'Wystąpił błąd.') {
    let text;

    try {
        text = await response.text();
    } catch {
        return { message: fallback, code: null, errors: {}, problem: null };
    }

    if (!text.trim()) {
        return { message: fallback, code: null, errors: {}, problem: null };
    }

    let body;

    try {
        body = JSON.parse(text);
    } catch {
        return { message: text, code: null, errors: {}, problem: null };
    }

    if (typeof body === 'string') {
        return { message: body || fallback, code: null, errors: {}, problem: null };
    }

    if (!body || typeof body !== 'object' || Array.isArray(body)) {
        return { message: fallback, code: null, errors: {}, problem: null };
    }

    return {
        message: messageFromProblem(body, fallback),
        code: typeof body.code === 'string' ? body.code : null,
        errors: body.errors && typeof body.errors === 'object' ? body.errors : {},
        problem: body,
    };
}
