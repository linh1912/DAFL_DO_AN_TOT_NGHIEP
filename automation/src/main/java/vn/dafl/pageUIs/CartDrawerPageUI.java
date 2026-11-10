package vn.dafl.pageUIs;

/** Locator cua gio hang (drawer o trang chu va cung giao dien tai trang /cart).
 *  Moi dong san pham co data-testid dong theo id san pham (trich tu CartItemRow.tsx):
 *  cart-item-{id}, cart-item-name-{id}, cart-item-price-{id}, cart-item-minus-{id},
 *  cart-item-qty-{id} (o NHAP so luong), cart-item-plus-{id}, cart-item-remove-{id}.
 *  Ban CN-06: giam so luong ve 0 hoac bam Xoa deu mo hop xac nhan cart-remove-confirm-dialog. */
public class CartDrawerPageUI {

    public static final String CART_DRAWER = "css=[data-testid='cart-drawer']";
    public static final String CART_CLOSE_BTN = "css=[data-testid='cart-close-btn']";
    public static final String CART_ITEMS_LIST = "css=[data-testid='cart-items-list']";
    public static final String CART_EMPTY_STATE = "css=[data-testid='cart-empty-state']";
    public static final String CONTINUE_SHOPPING_BTN = "css=[data-testid='continue-shopping-btn']";
    public static final String CART_ADDED_TOAST = "css=[data-testid='cart-added-toast']";

    public static final String DYNAMIC_CART_ITEM = "css=[data-testid='cart-item-%s']";
    public static final String DYNAMIC_CART_ITEM_NAME = "css=[data-testid='cart-item-name-%s']";
    public static final String DYNAMIC_CART_ITEM_PRICE = "css=[data-testid='cart-item-price-%s']";
    public static final String DYNAMIC_CART_ITEM_MINUS_BTN = "css=[data-testid='cart-item-minus-%s']";
    public static final String DYNAMIC_CART_ITEM_QTY = "css=[data-testid='cart-item-qty-%s']";
    public static final String DYNAMIC_CART_ITEM_PLUS_BTN = "css=[data-testid='cart-item-plus-%s']";
    public static final String DYNAMIC_CART_ITEM_REMOVE_BTN = "css=[data-testid='cart-item-remove-%s']";
    public static final String DYNAMIC_CART_ITEM_UNAVAILABLE = "css=[data-testid='cart-item-unavailable-%s']";
    public static final String DYNAMIC_CART_ITEM_PRICE_CHANGED = "css=[data-testid='cart-item-price-changed-%s']";

    // Hop xac nhan xoa san pham khoi gio (CN-06.3)
    public static final String CART_REMOVE_CONFIRM_DIALOG = "css=[data-testid='cart-remove-confirm-dialog']";
    public static final String CART_REMOVE_CONFIRM_BTN = "css=[data-testid='cart-remove-confirm-btn']";
    public static final String CART_REMOVE_CANCEL_BTN = "css=[data-testid='cart-remove-cancel-btn']";

    // Bang gia + voucher nam trong khu vuc gio hang
    public static final String CART_PRICING_SUMMARY = "css=[data-testid='cart-pricing-summary']";
    public static final String CART_RAW_SUBTOTAL = "css=[data-testid='cart-raw-subtotal']";
    public static final String CART_VOUCHER_DISCOUNT = "css=[data-testid='cart-voucher-discount']";
    public static final String CART_SHIPPING_FEE = "css=[data-testid='cart-shipping-fee']";
    public static final String CART_FINAL_TOTAL = "css=[data-testid='cart-final-total']";
    public static final String VOUCHER_SECTION = "css=[data-testid='voucher-section']";
    public static final String VOUCHER_CODE_INPUT = "css=[data-testid='voucher-code-input']";
    public static final String VOUCHER_APPLY_BTN = "css=[data-testid='voucher-apply-btn']";
    public static final String VOUCHER_ERROR_MSG = "css=[data-testid='voucher-error-msg']";
    public static final String APPLIED_VOUCHER_BANNER = "css=[data-testid='applied-voucher-banner']";
    public static final String REMOVE_VOUCHER_BTN = "css=[data-testid='remove-voucher-btn']";
    public static final String DYNAMIC_VOUCHER_CHIP = "css=[data-testid='voucher-chip-%s']";

    // Wave15.1-auto: TC_SCR6_UI_017 — thanh tien do freeship (CartDrawer.tsx:229).
    public static final String CART_FREESHIP_PROGRESS = "css=[data-testid='cart-freeship-progress']";
    // Wave15.1-auto: TC_SCR6_UI_002 — nut dat hang/thanh toan o form checkout trong /cart.
    public static final String CART_CHECKOUT_BTN = "css=[data-testid='cart-checkout-btn']";
    // Wave15.1-auto: TC_SCR6_UI_002 — dem so dong gio qua o nhap so luong (moi dong co dung 1 o).
    public static final String ANY_CART_ITEM_QTY = "css=[data-testid^='cart-item-qty-']";

    // Muc dung diem doi tien o gio hang (dot 15, chi hien khi da dang nhap):
    // bat toggle thi o nhap tu dien toi da; 1 diem = 1.000d; khong dung chung
    // voi ma FORDEV. Dong giam gia hien trong bang gia cua gio hang.
    public static final String USE_POINTS_SECTION = "css=[data-testid='use-points-section']";
    public static final String USE_POINTS_TOGGLE = "css=[data-testid='use-points-toggle']";
    public static final String POINTS_TO_USE_INPUT = "css=[data-testid='points-to-use-input']";
    public static final String POINTS_BALANCE_HINT = "css=[data-testid='points-balance-hint']";
    public static final String CART_POINTS_DISCOUNT = "css=[data-testid='cart-points-discount']";
    public static final String POINTS_ERROR_MSG = "css=[data-testid='points-error-msg']";
}
