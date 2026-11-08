package vn.dafl.pageUIs;

/** Locator cua khoi thanh toan nam trong trang /cart (route /checkout tu dong chuyen ve /cart)
 *  va man hinh thanh toan PayOS (mock). Ban CN-07: thong tin giao hang la khoi shipping-*
 *  (email, nguoi nhan, SDT, tinh/quan/phuong dang <select>, dia chi chi tiet, ghi chu <= 200 ky tu),
 *  nut dat hang la cart-checkout-btn. */
public class CheckoutPageUI {

    public static final String CHECKOUT_FORM = "css=[data-testid='checkout-form']";
    public static final String CHECKOUT_SUBMIT_BTN = "css=[data-testid='cart-checkout-btn']";
    // Cung nut dat hang do, them class checkout-submit-btn (dot 3: sticky duoi man hinh mobile).
    public static final String CHECKOUT_SUBMIT_BTN_STICKY_CLASS = "css=.checkout-submit-btn";
    public static final String BACK_TO_CART_BTN = "css=[data-testid='back-to-cart-btn']";
    public static final String CHECKOUT_ERROR_MSG = "css=[data-testid='checkout-error-msg']";
    public static final String CHECKOUT_PRICE_CHANGED_NOTICE = "css=[data-testid='checkout-price-changed-notice']";

    // Khoi thong tin giao hang (shipping-*)
    public static final String SHIPPING_EMAIL_INPUT = "css=[data-testid='shipping-email-input']";
    public static final String SHIPPING_RECIPIENT_INPUT = "css=[data-testid='shipping-recipient-input']";
    public static final String SHIPPING_PHONE_INPUT = "css=[data-testid='shipping-phone-input']";
    public static final String SHIPPING_PROVINCE_SELECT = "css=[data-testid='shipping-province-select']";
    public static final String SHIPPING_DISTRICT_SELECT = "css=[data-testid='shipping-district-select']";
    public static final String SHIPPING_WARD_SELECT = "css=[data-testid='shipping-ward-select']";
    public static final String SHIPPING_DETAIL_INPUT = "css=[data-testid='shipping-detail-input']";
    public static final String SHIPPING_NOTE_INPUT = "css=[data-testid='shipping-note-input']";
    public static final String SHIPPING_NOTE_COUNTER = "css=[data-testid='shipping-note-counter']";
    public static final String SHIPPING_SAVE_DEFAULT_CHECKBOX = "css=[data-testid='shipping-save-default-checkbox']";
    public static final String SHIPPING_RECIPIENT_ERROR = "css=[data-testid='shipping-recipient-error']";
    public static final String SHIPPING_PHONE_ERROR = "css=[data-testid='shipping-phone-error']";
    public static final String SHIPPING_EMAIL_ERROR = "css=[data-testid='shipping-email-error']";
    public static final String SHIPPING_PROVINCE_ERROR = "css=[data-testid='shipping-province-error']";
    public static final String SHIPPING_DISTRICT_ERROR = "css=[data-testid='shipping-district-error']";
    public static final String SHIPPING_WARD_ERROR = "css=[data-testid='shipping-ward-error']";
    public static final String SHIPPING_DETAIL_ERROR = "css=[data-testid='shipping-detail-error']";
    public static final String SHIPPING_NOTE_ERROR = "css=[data-testid='shipping-note-error']";

    // So dia chi da luu tai checkout
    public static final String SHIPPING_ADDRESS_LIST = "css=[data-testid='shipping-address-list']";
    public static final String DYNAMIC_SHIPPING_ADDRESS_OPTION = "css=[data-testid='shipping-address-option-%s']";
    public static final String ANY_SHIPPING_ADDRESS_OPTION = "css=[data-testid^='shipping-address-option-']";
    public static final String SHIPPING_NEW_ADDRESS_BTN = "css=[data-testid='shipping-new-address-btn']";
    public static final String SHIPPING_NEW_ADDRESS_FORM = "css=[data-testid='shipping-new-address-form']";

    // Phuong thuc thanh toan
    public static final String PAYMENT_METHOD_COD = "css=[data-testid='payment-method-cod']";
    public static final String PAYMENT_METHOD_PAYOS = "css=[data-testid='payment-method-payos']";

    // Sau khi dat hang COD thanh cong
    public static final String ORDER_SUCCESS_VIEW = "css=[data-testid='order-success-view']";
    public static final String ORDER_SUCCESS_CODE = "css=[data-testid='order-success-code']";
    public static final String VIEW_ORDERS_BTN = "css=[data-testid='view-orders-btn']";

    // Man hinh thanh toan PayOS nhung ngay trong trang gio
    public static final String CHECKOUT_ORDER_CODE = "css=[data-testid='checkout-order-code']";
    public static final String CHECKOUT_PAYMENT_VIEW = "css=[data-testid='checkout-payment-view']";

    // Trang thanh toan /payment/:orderCode (cong PayOS dang mock)
    public static final String PAYMENT_PAGE = "css=[data-testid='payment-page']";
    public static final String PAYMENT_TOTAL_AMOUNT = "css=[data-testid='payment-total-amount']";
    public static final String PAYMENT_TIMER = "css=[data-testid='payment-timer']";
    public static final String SIMULATE_PAYOS_SUCCESS_BTN = "css=[data-testid='simulate-payos-success-btn']";
    public static final String ORDER_PAID_VIEW = "css=[data-testid='order-paid-view']";
    public static final String ORDER_EXPIRED_VIEW = "css=[data-testid='order-expired-view']";

    // Trang thanh toan PayOS (dot 8): chi con quet VietQR tai cho + mo tab moi,
    // tab 'Nhúng Cổng PayOS' (iframe) da bi xoa han - khong duoc con iframe nao.
    public static final String PAYOS_QR_OPTION_TEXT =
            "xpath=//span[contains(text(),'Quét mã VietQR tại chỗ')]";
    public static final String PAYOS_ANY_IFRAME = "xpath=//iframe";
    public static final String PAYOS_EMBED_TAB_TEXT =
            "xpath=//*[contains(text(),'Nhúng Cổng PayOS')]";
    // Link mo tab moi chi hien khi dung key PayOS that (isRealPayOS); o che do demo khong co.
    public static final String PAYOS_OPEN_NEW_TAB_LINK =
            "xpath=//a[contains(text(),'Mở PayOS ở Tab Mới')]";
}
