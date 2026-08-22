const PLACED_STATUS_ID = '00000000-0000-0000-0000-000000000021';
const PROCESSING_STATUS_ID = '00000000-0000-0000-0000-000000000022';
const SHIPPED_STATUS_ID = '00000000-0000-0000-0000-000000000023';
const DELIVERED_STATUS_ID = '00000000-0000-0000-0000-000000000024';
const CANCELLED_STATUS_ID = '00000000-0000-0000-0000-000000000025';

const ORDER_STATUSES = [
    { id: PLACED_STATUS_ID, label: 'Zamówienie złożone' },
    { id: PROCESSING_STATUS_ID, label: 'W realizacji' },
    { id: SHIPPED_STATUS_ID, label: 'Wysłane' },
    { id: DELIVERED_STATUS_ID, label: 'Dostarczone' },
    { id: CANCELLED_STATUS_ID, label: 'Anulowane' },
];

const ALLOWED_STATUS_IDS = {
    [PLACED_STATUS_ID]: [PROCESSING_STATUS_ID, CANCELLED_STATUS_ID],
    [PROCESSING_STATUS_ID]: [SHIPPED_STATUS_ID, CANCELLED_STATUS_ID],
    [SHIPPED_STATUS_ID]: [DELIVERED_STATUS_ID],
    [DELIVERED_STATUS_ID]: [],
    [CANCELLED_STATUS_ID]: [],
};

export function getOrderStatusOptions(currentStatusId) {
    if (!currentStatusId) {
        return ORDER_STATUSES.filter(status => status.id === PLACED_STATUS_ID);
    }

    const allowedStatusIds = ALLOWED_STATUS_IDS[currentStatusId] || [];
    return ORDER_STATUSES.filter(status =>
        status.id === currentStatusId || allowedStatusIds.includes(status.id)
    );
}
