package vn.dafl.pageUIs;

/** Locator cua trang don hang /orders (CN-08): danh sach, loc trang thai, phan trang,
 *  chi tiet don va hop thoai huy don. Ma don hien thi dang DH{yyyyMMdd}-{NNNN}
 *  duoc nhung thang vao data-testid: order-item-{maDH}, order-detail-btn-{maDH}. */
public class OrdersPageUI {

    public static final String ORDERS_LIST = "css=[data-testid='orders-list']";
    public static final String ORDERS_EMPTY_STATE = "css=[data-testid='orders-empty-state']";
    public static final String ANY_ORDER_ITEM = "css=[data-testid^='order-item-']";
    public static final String DYNAMIC_ORDER_ITEM = "css=[data-testid='order-item-%s']";
    public static final String DYNAMIC_ORDER_DETAIL_BTN = "css=[data-testid='order-detail-btn-%s']";
    public static final String DYNAMIC_ORDER_CODE_TEXT = "xpath=//*[contains(text(),'%s')]";
    public static final String ORDERS_HEADING = "xpath=//*[self::h1 or self::h2][contains(text(),'Đơn hàng')]";
    public static final String ORDER_STATUS_FILTER = "css=[data-testid='order-status-filter']";
    public static final String ORDERS_PAGINATION = "css=[data-testid='orders-pagination']";
    public static final String ORDERS_PREV_BTN = "css=[data-testid='orders-prev-btn']";
    public static final String ORDERS_NEXT_BTN = "css=[data-testid='orders-next-btn']";

    // Chi tiet don
    public static final String ORDER_DETAIL_VIEW = "css=[data-testid='order-detail-view']";
    public static final String ORDER_DETAIL_STATUS = "css=[data-testid='order-detail-status']";
    public static final String ORDER_TIMELINE = "css=[data-testid='order-timeline']";
    public static final String ORDER_SHIPPING_INFO = "css=[data-testid='order-shipping-info']";
    public static final String ORDER_SUPPORT_NOTE = "css=[data-testid='order-support-note']";

    // Huy don (CN-08.3)
    public static final String CANCEL_ORDER_BTN = "css=[data-testid='cancel-order-btn']";
    public static final String CANCEL_ORDER_DIALOG = "css=[data-testid='cancel-order-dialog']";
    public static final String CANCEL_REASON_SELECT = "css=[data-testid='cancel-reason-select']";
    public static final String CANCEL_REASON_INPUT = "css=[data-testid='cancel-reason-input']";
    public static final String CANCEL_CONFIRM_BTN = "css=[data-testid='cancel-confirm-btn']";
    public static final String CANCEL_CANCEL_BTN = "css=[data-testid='cancel-cancel-btn']";
    public static final String CANCEL_SUCCESS_MSG = "css=[data-testid='cancel-success-msg']";
}
