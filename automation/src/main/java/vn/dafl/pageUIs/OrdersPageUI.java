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
    // Bo loc trang thai: tu ban web dot 2 tro di la NHOM TAB (div role=tablist), KHONG
    // con la <select> nua - khong duoc dung selectByValue o day (se nem UnexpectedTagNameException).
    // Tab testid: order-status-tab-all (tat ca) hoac order-status-tab-{nhan tieng Viet}.
    // Nhan tab hien dang "Nhan (so luong)" khi so luong > 0, vd "Chờ xác nhận (2)".
    public static final String ORDER_STATUS_FILTER = "css=[data-testid='order-status-filter']";
    public static final String DYNAMIC_ORDER_STATUS_TAB = "css=[data-testid='order-status-tab-%s']";
    public static final String ORDER_STATUS_TAB_ALL = "css=[data-testid='order-status-tab-all']";
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

    // Wave15.1-auto: TC_SCR8_FUNC_014 — nut "Thanh toán ngay" (khong co testid rieng,
    // chi hien khi nhan don la "Chờ thanh toán"), pham vi trong the don order-item-{ma}.
    public static final String DYNAMIC_ORDER_PAY_NOW_BTN =
            "xpath=//*[@data-testid='order-item-%s']//button[contains(.,'Thanh toán ngay')]";
    // Wave15.1-auto: TC_SCR8_FUNC_015 — nut "Mua lại" (khong co testid rieng,
    // chi hien khi don o Paid/Delivered/Completed/Cancelled), pham vi trong the don.
    public static final String DYNAMIC_ORDER_REORDER_BTN =
            "xpath=//*[@data-testid='order-item-%s']//button[contains(.,'Mua lại')]";
    // Wave15.1-auto: TC_SCR8_UI_013 — cac dong san pham trong the don (the chi hien toi da 3 dong).
    public static final String DYNAMIC_ORDER_ITEM_ROWS =
            "xpath=//*[@data-testid='order-item-%s']//*[contains(@class,'order-item-row')]";
    // Wave15.1-auto: TC_SCR8_UI_012 — chi so "Trang {n}" trong khoi phan trang don hang.
    public static final String ORDERS_PAGE_INDICATOR =
            "xpath=//*[@data-testid='orders-pagination']//span[contains(@class,'pagination-current')]";
}
