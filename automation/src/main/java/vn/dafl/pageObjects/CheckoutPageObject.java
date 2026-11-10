package vn.dafl.pageObjects;

import org.openqa.selenium.WebElement;
import vn.dafl.commons.BasePage;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageUIs.CheckoutPageUI;

import java.util.List;

/** Page Object cho khoi thanh toan trong trang /cart va trang thanh toan /payment/:orderCode.
 *  Ban CN-07: bat buoc dien khoi thong tin giao hang shipping-* (chon tinh -> quan -> phuong
 *  theo dung text hien thi trong du lieu dia gioi tinh kem FE). */
public class CheckoutPageObject extends BasePage {

    /** Route /checkout cua SUT tu dong chuyen huong ve /cart - noi chua form thanh toan. */
    public void openCheckoutPage() {
        openPageUrl(GlobalConstants.getAppUrl() + "/cart");
        waitForElementVisible(CheckoutPageUI.CHECKOUT_FORM);
    }

    public boolean isCheckoutFormDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.CHECKOUT_FORM, 10);
    }

    public boolean isCheckoutSubmitDisplayed() {
        return isElementDisplayed(CheckoutPageUI.CHECKOUT_SUBMIT_BTN);
    }

    /** Nut dat hang co class checkout-submit-btn (dot 3, sticky tren mobile) va hien thi. */
    public boolean isStickySubmitButtonDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.CHECKOUT_SUBMIT_BTN_STICKY_CLASS, 10);
    }

    /** Text tren nut dat hang (vd "Đặt hàng (279.000đ)", khach thi "Đăng nhập để thanh toán (...)"). */
    public String getSubmitButtonText() {
        waitForElementVisible(CheckoutPageUI.CHECKOUT_SUBMIT_BTN_STICKY_CLASS);
        return getElementText(CheckoutPageUI.CHECKOUT_SUBMIT_BTN_STICKY_CLASS);
    }

    // ---------- Thong tin giao hang ----------
    /** Neu nguoi dung da co so dia chi, form nhap moi bi an - bam "Them dia chi moi" de mo. */
    public void ensureNewAddressFormVisible() {
        if (!isElementDisplayed(CheckoutPageUI.SHIPPING_NEW_ADDRESS_FORM)
                && isElementDisplayed(CheckoutPageUI.SHIPPING_NEW_ADDRESS_BTN)) {
            clickToElement(CheckoutPageUI.SHIPPING_NEW_ADDRESS_BTN);
        }
        waitForElementVisible(CheckoutPageUI.SHIPPING_NEW_ADDRESS_FORM);
    }

    /** Dien day du khoi giao hang bang dia chi moi (chon dia gioi theo text hien thi). */
    public void fillShippingForm(String recipient, String email, String phone,
                                 String province, String district, String ward,
                                 String detailAddress, String note) {
        ensureNewAddressFormVisible();
        sendKeyToElement(CheckoutPageUI.SHIPPING_RECIPIENT_INPUT, recipient);
        sendKeyToElement(CheckoutPageUI.SHIPPING_EMAIL_INPUT, email);
        sendKeyToElement(CheckoutPageUI.SHIPPING_PHONE_INPUT, phone);
        selectByVisibleText(CheckoutPageUI.SHIPPING_PROVINCE_SELECT, province);
        sleepInSecond(1); // cho danh sach quan/huyen nap theo tinh
        selectByVisibleText(CheckoutPageUI.SHIPPING_DISTRICT_SELECT, district);
        sleepInSecond(1); // cho danh sach phuong/xa nap theo quan
        selectByVisibleText(CheckoutPageUI.SHIPPING_WARD_SELECT, ward);
        sendKeyToElement(CheckoutPageUI.SHIPPING_DETAIL_INPUT, detailAddress);
        if (note != null) {
            sendKeyToElement(CheckoutPageUI.SHIPPING_NOTE_INPUT, note);
        }
    }

    /** Dien khoi giao hang mac dinh lay tu hang so cua SUT (Ha Noi / Ba Dinh / Phuc Xa). */
    public void fillDefaultShippingForm(String recipient, String email, String phone) {
        fillShippingForm(recipient, email, phone,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
    }

    /** Chon dia chi dau tien trong so dia chi da luu tai checkout. */
    public void selectFirstSavedAddress() {
        waitForElementVisible(CheckoutPageUI.SHIPPING_ADDRESS_LIST);
        List<WebElement> options = getListWebElements(CheckoutPageUI.ANY_SHIPPING_ADDRESS_OPTION);
        if (options.isEmpty()) {
            throw new IllegalStateException("So dia chi tai checkout dang rong");
        }
        options.get(0).click();
        pauseAfterAction();
    }

    public boolean isSavedAddressListDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.SHIPPING_ADDRESS_LIST, 5);
    }

    public boolean isFieldErrorDisplayed(String errorLocator) {
        return isElementDisplayedWithWait(errorLocator, 5);
    }

    public String getFieldError(String errorLocator) {
        waitForElementVisible(errorLocator);
        return getElementText(errorLocator);
    }

    // ---------- Phuong thuc thanh toan + dat hang ----------
    public void selectCodPayment() {
        clickToElement(CheckoutPageUI.PAYMENT_METHOD_COD);
    }

    public void selectPayosPayment() {
        clickToElement(CheckoutPageUI.PAYMENT_METHOD_PAYOS);
    }

    public void submitOrder() {
        clickToElement(CheckoutPageUI.CHECKOUT_SUBMIT_BTN);
    }

    /** Double click that nhanh vao nut dat hang (CN-07 E5) bang JavaScript. */
    public void doubleClickSubmitOrder() {
        waitForElementVisible(CheckoutPageUI.CHECKOUT_SUBMIT_BTN);
        executeScript("var el = document.querySelector(\"[data-testid='cart-checkout-btn']\");"
                + " if (el) { el.click(); el.click(); }");
    }

    public boolean isCheckoutErrorDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.CHECKOUT_ERROR_MSG, 10);
    }

    public String getCheckoutErrorMessage() {
        waitForElementVisible(CheckoutPageUI.CHECKOUT_ERROR_MSG);
        return getElementText(CheckoutPageUI.CHECKOUT_ERROR_MSG);
    }

    // ---------- Sau khi dat hang ----------
    public boolean isOrderSuccessViewDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.ORDER_SUCCESS_VIEW, 15);
    }

    /** Ma don hien thi dang DH{yyyyMMdd}-{NNNN} tren man hinh dat hang COD thanh cong. */
    public String getOrderSuccessCode() {
        waitForElementVisible(CheckoutPageUI.ORDER_SUCCESS_CODE);
        return getElementText(CheckoutPageUI.ORDER_SUCCESS_CODE).trim();
    }

    public boolean isOrderCodeDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.CHECKOUT_ORDER_CODE, 15);
    }

    public String getOrderCode() {
        waitForElementVisible(CheckoutPageUI.CHECKOUT_ORDER_CODE);
        return getElementText(CheckoutPageUI.CHECKOUT_ORDER_CODE).trim();
    }

    public boolean isPaymentViewDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.CHECKOUT_PAYMENT_VIEW, 15);
    }

    public OrdersPageObject clickViewOrders() {
        clickToElement(CheckoutPageUI.VIEW_ORDERS_BTN);
        return new OrdersPageObject();
    }

    // ---------- Trang thanh toan PayOS (mock) ----------
    public boolean isPaymentPageDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.PAYMENT_PAGE, 15);
    }

    public String getPaymentTotalText() {
        waitForElementVisible(CheckoutPageUI.PAYMENT_TOTAL_AMOUNT);
        return getElementText(CheckoutPageUI.PAYMENT_TOTAL_AMOUNT);
    }

    /** Gia lap thanh toan PayOS thanh cong bang nut co san tren cong mock. */
    public void simulatePayosSuccess() {
        waitForElementVisible(CheckoutPageUI.SIMULATE_PAYOS_SUCCESS_BTN);
        scrollToElement(CheckoutPageUI.SIMULATE_PAYOS_SUCCESS_BTN);
        clickToElement(CheckoutPageUI.SIMULATE_PAYOS_SUCCESS_BTN);
    }

    public boolean isOrderPaidViewDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.ORDER_PAID_VIEW, 15);
    }

    // ---------- Wave15.1-auto: mo rong cho CheckoutExtendedTest / CheckoutPaymentTest (Pha B, buoc 3) ----------

    // Wave15.1-auto: TC_SCR7_FUNC_024/025 — radio COD co dang bam duoc khong (bi disable khi tong > 2.000.000d).
    public boolean isCodPaymentEnabled() {
        return isElementEnabled(CheckoutPageUI.PAYMENT_METHOD_COD);
    }

    // Wave15.1-auto: TC_SCR7_UI_026/025 — radio nao dang duoc chon (phuong thuc hieu luc).
    public boolean isCodPaymentSelected() {
        try {
            return getWebElement(CheckoutPageUI.PAYMENT_METHOD_COD).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isPayosPaymentSelected() {
        try {
            return getWebElement(CheckoutPageUI.PAYMENT_METHOD_PAYOS).isSelected();
        } catch (Exception e) {
            return false;
        }
    }

    // Wave15.1-auto: TC_SCR7_FUNC_025 — ghi chu gioi han COD (khong cho doi khi chua hien).
    public boolean isCodLimitNoteDisplayed() {
        return isElementDisplayed(CheckoutPageUI.PAYMENT_COD_LIMIT_NOTE);
    }

    public String getCodLimitNoteText() {
        waitForElementVisible(CheckoutPageUI.PAYMENT_COD_LIMIT_NOTE);
        return getElementText(CheckoutPageUI.PAYMENT_COD_LIMIT_NOTE);
    }

    // Wave15.1-auto: TC_SCR7_UI_001 — bo dem ghi chu "{n}/200" (ban dau "0/200").
    public String getShippingNoteCounterText() {
        waitForElementVisible(CheckoutPageUI.SHIPPING_NOTE_COUNTER);
        return getElementText(CheckoutPageUI.SHIPPING_NOTE_COUNTER);
    }

    // Wave15.1-auto: TC_SCR7_FUNC_006 — checkbox "Luu lam dia chi mac dinh" (chi hien khi da dang nhap).
    public boolean isSaveDefaultCheckboxDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.SHIPPING_SAVE_DEFAULT_CHECKBOX, 5);
    }

    public void clickSaveDefaultCheckbox() {
        clickToElement(CheckoutPageUI.SHIPPING_SAVE_DEFAULT_CHECKBOX);
    }

    // Wave15.1-auto: TC_SCR7_UI_002 — text cua 1 dia chi da luu (nguoi nhan/SDT/dia chi + badge "MẶC ĐỊNH").
    public String getSavedAddressOptionText(String addressId) {
        return getElementText(CheckoutPageUI.DYNAMIC_SHIPPING_ADDRESS_OPTION, addressId);
    }

    public int getSavedAddressOptionCount() {
        return getListWebElements(CheckoutPageUI.ANY_SHIPPING_ADDRESS_OPTION).size();
    }

    // Wave15.1-auto: TC_SCR7_UI_046 — dong ho dem nguoc giu cho PayOS (dinh dang MM:SS).
    public String getPaymentTimerText() {
        waitForElementVisible(CheckoutPageUI.PAYMENT_TIMER);
        return getElementText(CheckoutPageUI.PAYMENT_TIMER);
    }

    // Wave15.1-auto: TC_SCR7_UI_046 — nut "Sao chép mã" o view thanh toan PayOS trong gio.
    public boolean isCopyOrderCodeButtonDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.COPY_ORDER_CODE_BTN, 5);
    }

    public void clickCopyOrderCodeButton() {
        clickToElement(CheckoutPageUI.COPY_ORDER_CODE_BTN);
    }

    // Wave15.1-auto: TC_SCR7_UI_046 — nut "Đổi giỏ hàng" (quay lai gio hang).
    public boolean isBackToCartButtonDisplayed() {
        return isElementDisplayedWithWait(CheckoutPageUI.BACK_TO_CART_BTN, 5);
    }

    public void clickBackToCartButton() {
        clickToElement(CheckoutPageUI.BACK_TO_CART_BTN);
    }

    // Wave15.1-auto: TC_SCR7_VAL_013/014/015 — dien form giao hang nhung BO QUA cac
    // truong truyen null (de kiem tra loi "Vui long chon tinh/quan/phuong"); cac
    // truong khac dien nhu fillShippingForm.
    public void fillShippingFormPartial(String recipient, String email, String phone,
                                        String province, String district, String ward,
                                        String detailAddress, String note) {
        ensureNewAddressFormVisible();
        if (recipient != null) {
            sendKeyToElement(CheckoutPageUI.SHIPPING_RECIPIENT_INPUT, recipient);
        }
        if (email != null) {
            sendKeyToElement(CheckoutPageUI.SHIPPING_EMAIL_INPUT, email);
        }
        if (phone != null) {
            sendKeyToElement(CheckoutPageUI.SHIPPING_PHONE_INPUT, phone);
        }
        if (province != null) {
            selectByVisibleText(CheckoutPageUI.SHIPPING_PROVINCE_SELECT, province);
            sleepInSecond(1); // cho danh sach quan/huyen nap theo tinh
        }
        if (district != null) {
            selectByVisibleText(CheckoutPageUI.SHIPPING_DISTRICT_SELECT, district);
            sleepInSecond(1); // cho danh sach phuong/xa nap theo quan
        }
        if (ward != null) {
            selectByVisibleText(CheckoutPageUI.SHIPPING_WARD_SELECT, ward);
        }
        if (detailAddress != null) {
            sendKeyToElement(CheckoutPageUI.SHIPPING_DETAIL_INPUT, detailAddress);
        }
        if (note != null) {
            sendKeyToElement(CheckoutPageUI.SHIPPING_NOTE_INPUT, note);
        }
    }
}
