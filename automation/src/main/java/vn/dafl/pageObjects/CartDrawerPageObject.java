package vn.dafl.pageObjects;

import vn.dafl.commons.BasePage;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageUIs.CartDrawerPageUI;

/** Page Object cho gio hang: dong san pham, so luong, tong tien va voucher.
 *  Ban CN-06: o so luong la <input> (doc bang thuoc tinh value), giam ve 0/Xoa phai xac nhan. */
public class CartDrawerPageObject extends BasePage {

    public void waitForDrawerVisible() {
        waitForElementVisible(CartDrawerPageUI.CART_DRAWER);
    }

    public boolean isCartEmpty() {
        return isElementDisplayedWithWait(CartDrawerPageUI.CART_EMPTY_STATE, 5);
    }

    public boolean isItemDisplayed(String productId) {
        return isElementDisplayed(CartDrawerPageUI.DYNAMIC_CART_ITEM, productId);
    }

    public String getItemName(String productId) {
        return getElementText(CartDrawerPageUI.DYNAMIC_CART_ITEM_NAME, productId);
    }

    /** So luong hien tai: o so luong la <input> nen doc thuoc tinh value. */
    public int getItemQuantity(String productId) {
        String value = getElementValue(CartDrawerPageUI.DYNAMIC_CART_ITEM_QTY, productId);
        return value.isEmpty() ? 0 : Integer.parseInt(value.replaceAll("[^0-9]", ""));
    }

    public void increaseQuantity(String productId) {
        int before = getItemQuantity(productId);
        clickToElement(CartDrawerPageUI.DYNAMIC_CART_ITEM_PLUS_BTN, productId);
        waitForItemQuantity(productId, before + 1);
    }

    public void decreaseQuantity(String productId) {
        clickToElement(CartDrawerPageUI.DYNAMIC_CART_ITEM_MINUS_BTN, productId);
    }

    /** Go truc tiep so luong vao o nhap roi nhan Enter de xac nhan (commit khi blur). */
    public void setItemQuantity(String productId, int quantity) {
        // Dung sendKeyToElement (xoa bang Ctrl+A/Delete) de React nhan dung gia tri moi;
        // clear() cua WebDriver tung khien so luong tren server khong duoc cap nhat.
        sendKeyToElement(CartDrawerPageUI.DYNAMIC_CART_ITEM_QTY, String.valueOf(quantity), productId);
        String locator = getDynamicLocator(CartDrawerPageUI.DYNAMIC_CART_ITEM_QTY, productId);
        getWebElement(locator).sendKeys(org.openqa.selenium.Keys.ENTER);
        pauseAfterAction();
    }

    public void waitForItemQuantity(String productId, int expected) {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            if (isItemDisplayed(productId) && getItemQuantity(productId) == expected) {
                return;
            }
            sleepInSecond(1);
        }
        throw new AssertionError("So luong san pham " + productId + " khong dat " + expected + " sau 15 giay");
    }

    /** Cho so luong on dinh o mot gia tri bat ky (dung khi he thong tu kep ve ton kho). */
    public int waitForQuantityToSettle(String productId) {
        int previous = -1;
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            int current = getItemQuantity(productId);
            if (current == previous && current > 0) {
                return current;
            }
            previous = current;
            sleepInSecond(1);
        }
        return getItemQuantity(productId);
    }

    // ---------- Xoa san pham (CN-06.3: phai xac nhan) ----------
    public void clickRemoveItem(String productId) {
        clickToElement(CartDrawerPageUI.DYNAMIC_CART_ITEM_REMOVE_BTN, productId);
    }

    public boolean isRemoveConfirmDialogDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.CART_REMOVE_CONFIRM_DIALOG, 5);
    }

    public void confirmRemoveItem() {
        clickToElement(CartDrawerPageUI.CART_REMOVE_CONFIRM_BTN);
    }

    public void cancelRemoveItem() {
        clickToElement(CartDrawerPageUI.CART_REMOVE_CANCEL_BTN);
    }

    public boolean isItemUnavailable(String productId) {
        return isElementDisplayed(CartDrawerPageUI.DYNAMIC_CART_ITEM_UNAVAILABLE, productId);
    }

    public boolean isAddedToCartToastDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.CART_ADDED_TOAST, 5);
    }

    public String getSubtotalText() {
        return getElementText(CartDrawerPageUI.CART_RAW_SUBTOTAL);
    }

    public String getFinalTotalText() {
        return getElementText(CartDrawerPageUI.CART_FINAL_TOTAL);
    }

    public String getShippingFeeText() {
        return getElementText(CartDrawerPageUI.CART_SHIPPING_FEE);
    }

    public boolean isVoucherDiscountDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.CART_VOUCHER_DISCOUNT, 5);
    }

    public void applyVoucher(String voucherCode) {
        waitForElementVisible(CartDrawerPageUI.VOUCHER_CODE_INPUT);
        sendKeyToElement(CartDrawerPageUI.VOUCHER_CODE_INPUT, voucherCode);
        clickToElement(CartDrawerPageUI.VOUCHER_APPLY_BTN);
    }

    public void applyVoucherByChip(String voucherCode) {
        clickToElement(CartDrawerPageUI.DYNAMIC_VOUCHER_CHIP, voucherCode);
    }

    public boolean isVoucherAppliedBannerDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.APPLIED_VOUCHER_BANNER, 5);
    }

    public String getVoucherErrorMessage() {
        waitForElementVisible(CartDrawerPageUI.VOUCHER_ERROR_MSG);
        return getElementText(CartDrawerPageUI.VOUCHER_ERROR_MSG);
    }

    public boolean isVoucherErrorDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.VOUCHER_ERROR_MSG, 5);
    }

    public void closeDrawer() {
        if (isElementDisplayed(CartDrawerPageUI.CART_CLOSE_BTN)) {
            clickToElement(CartDrawerPageUI.CART_CLOSE_BTN);
        }
    }

    public boolean isRemoveVoucherButtonDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.REMOVE_VOUCHER_BTN, 5);
    }

    /** Go ma voucher khoi gio (dung don dep sau test ap ma). */
    public void clickRemoveVoucher() {
        clickToElement(CartDrawerPageUI.REMOVE_VOUCHER_BTN);
        sleepInSecond(1);
    }

    // ---------- Dung diem doi tien o gio hang (dot 15) ----------
    /** Mo thang trang /cart (khong qua nut gio o header). */
    public void openCartPage() {
        openPageUrl(GlobalConstants.getAppUrl() + "/cart");
        waitForDrawerVisible();
    }

    /** Muc dung diem co xuat hien NGAY BAY GIO khong (khong cho doi) — dung
     *  cho khang dinh am (PT-01: khach chua dang nhap phai KHONG thay muc nay). */
    public boolean isUsePointsSectionPresent() {
        return isElementDisplayed(CartDrawerPageUI.USE_POINTS_SECTION);
    }

    public boolean isUsePointsSectionDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.USE_POINTS_SECTION, 10);
    }

    public void waitForUsePointsSection() {
        waitForElementVisible(CartDrawerPageUI.USE_POINTS_SECTION);
    }

    public void clickUsePointsToggle() {
        clickToElement(CartDrawerPageUI.USE_POINTS_TOGGLE);
        sleepInSecond(2); // cho FE tu dien toi da / cap nhat lai bang gia
    }

    public boolean isUsePointsToggleEnabled() {
        return isElementEnabled(CartDrawerPageUI.USE_POINTS_TOGGLE);
    }

    /** So diem dang nhap o o dung diem; 0 neu o chua hien hoac dang trong. */
    public int getPointsToUseValue() {
        if (!isElementDisplayed(CartDrawerPageUI.POINTS_TO_USE_INPUT)) {
            return 0;
        }
        String value = getElementValue(CartDrawerPageUI.POINTS_TO_USE_INPUT);
        return value.isEmpty() ? 0 : Integer.parseInt(value.replaceAll("[^0-9]", ""));
    }

    /** Go so diem muon dung (FE tu kep ve tran neu vuot). */
    public void setPointsToUse(int points) {
        sendKeyToElement(CartDrawerPageUI.POINTS_TO_USE_INPUT, String.valueOf(points));
        sleepInSecond(2); // cho FE kep ve tran + hien loi (neu co)
    }

    public boolean isPointsDiscountDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.CART_POINTS_DISCOUNT, 5);
    }

    /** So tien giam do dung diem (dong cart-points-discount); 0 neu dong chua hien. */
    public long getPointsDiscountValue() {
        if (!isElementDisplayed(CartDrawerPageUI.CART_POINTS_DISCOUNT)) {
            return 0;
        }
        return parseMoney(getElementText(CartDrawerPageUI.CART_POINTS_DISCOUNT));
    }

    public String getPointsBalanceHintText() {
        waitForElementVisible(CartDrawerPageUI.POINTS_BALANCE_HINT);
        return getElementText(CartDrawerPageUI.POINTS_BALANCE_HINT);
    }

    public boolean isPointsErrorDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.POINTS_ERROR_MSG, 5);
    }

    public String getPointsErrorMessage() {
        waitForElementVisible(CartDrawerPageUI.POINTS_ERROR_MSG);
        return getElementText(CartDrawerPageUI.POINTS_ERROR_MSG);
    }

    // ---------- Wave15.1-auto: gio hang + voucher (Pha B, SCR_06) ----------
    /** Text thanh tien do freeship (vd "Them 149.000d de freeship" / "Da duoc freeship"). */
    public String getFreeshipProgressText() {
        return getElementText(CartDrawerPageUI.CART_FREESHIP_PROGRESS);
    }

    public boolean isFreeshipProgressDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.CART_FREESHIP_PROGRESS, 5);
    }

    /** Gia tri dang co trong o nhap ma voucher. */
    public String getVoucherInputValue() {
        return getElementValue(CartDrawerPageUI.VOUCHER_CODE_INPUT);
    }

    /** Chi go ma voucher vao o nhap (khong bam Ap dung) — dung kiem tra tu uppercase. */
    public void setVoucherCode(String voucherCode) {
        sendKeyToElement(CartDrawerPageUI.VOUCHER_CODE_INPUT, voucherCode);
    }

    /** Bam nut "Ap dung" cua o voucher (sau khi ma da duoc dien san, vd qua chip goi y). */
    public void clickVoucherApplyButton() {
        clickToElement(CartDrawerPageUI.VOUCHER_APPLY_BTN);
        sleepInSecond(2); // cho BE tra ve + banner/loi hien
    }

    /** Text cua chip goi y ma voucher (vd "DAFL20 (-20.000d)"). */
    public String getVoucherChipText(String voucherCode) {
        return getElementText(CartDrawerPageUI.DYNAMIC_VOUCHER_CHIP, voucherCode);
    }

    public boolean isVoucherChipDisplayed(String voucherCode) {
        return isElementDisplayedWithWait(
                getDynamicLocator(CartDrawerPageUI.DYNAMIC_VOUCHER_CHIP, voucherCode), 5);
    }

    /** Text banner ma da ap (vd "Da ap dung ma DAFL20: -20.000d"). */
    public String getAppliedVoucherBannerText() {
        return getElementText(CartDrawerPageUI.APPLIED_VOUCHER_BANNER);
    }

    /** So tien giam do voucher (dong cart-voucher-discount); 0 neu dong chua hien. */
    public long getVoucherDiscountValue() {
        if (!isElementDisplayed(CartDrawerPageUI.CART_VOUCHER_DISCOUNT)) {
            return 0;
        }
        return parseMoney(getElementText(CartDrawerPageUI.CART_VOUCHER_DISCOUNT));
    }

    /** Go truc tiep chuoi tho vao o so luong (vd "2a3", "0", "-1") roi Enter —
     *  dung kiem tra FE loc ky tu / mo hop xac nhan xoa khi <= 0. */
    public void setItemQuantityText(String productId, String rawText) {
        sendKeyToElement(CartDrawerPageUI.DYNAMIC_CART_ITEM_QTY, rawText, productId);
        String locator = getDynamicLocator(CartDrawerPageUI.DYNAMIC_CART_ITEM_QTY, productId);
        getWebElement(locator).sendKeys(org.openqa.selenium.Keys.ENTER);
        pauseAfterAction();
    }

    /** Text gop cua 1 dong gio (ten, bien the, gia, so luong, thanh tien, nut Xoa). */
    public String getItemRowText(String productId) {
        return getElementText(CartDrawerPageUI.DYNAMIC_CART_ITEM, productId);
    }

    /** So dong gio dang hien (dem qua o nhap so luong — moi dong co dung 1 o). */
    public int getCartItemCount() {
        return getListWebElements(CartDrawerPageUI.ANY_CART_ITEM_QTY).size();
    }

    public String getItemPriceText(String productId) {
        return getElementText(CartDrawerPageUI.DYNAMIC_CART_ITEM_PRICE, productId);
    }

    public boolean isItemPriceDisplayed(String productId) {
        return isElementDisplayed(CartDrawerPageUI.DYNAMIC_CART_ITEM_PRICE, productId);
    }

    public boolean isItemMinusButtonDisplayed(String productId) {
        return isElementDisplayed(CartDrawerPageUI.DYNAMIC_CART_ITEM_MINUS_BTN, productId);
    }

    public boolean isItemPlusButtonDisplayed(String productId) {
        return isElementDisplayed(CartDrawerPageUI.DYNAMIC_CART_ITEM_PLUS_BTN, productId);
    }

    public boolean isItemRemoveButtonDisplayed(String productId) {
        return isElementDisplayed(CartDrawerPageUI.DYNAMIC_CART_ITEM_REMOVE_BTN, productId);
    }

    public boolean isCheckoutButtonDisplayed() {
        return isElementDisplayedWithWait(CartDrawerPageUI.CART_CHECKOUT_BTN, 5);
    }

    public String getCheckoutButtonText() {
        return getElementText(CartDrawerPageUI.CART_CHECKOUT_BTN);
    }

    // Wave15.1-auto: TC_SCR7_VAL_058 (PT-06) — go chuoi tho vao o diem (vd "50abc");
    // FE loc ky tu khong phai so ngay khi go (UsePointsSection.tsx).
    public void setPointsToUseText(String rawText) {
        sendKeyToElement(CartDrawerPageUI.POINTS_TO_USE_INPUT, rawText);
        sleepInSecond(2); // cho FE loc ky tu + cap nhat dong giam gia
    }
}
