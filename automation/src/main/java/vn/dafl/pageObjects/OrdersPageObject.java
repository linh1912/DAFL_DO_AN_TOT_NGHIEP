package vn.dafl.pageObjects;

import org.openqa.selenium.WebElement;
import vn.dafl.commons.BasePage;
import vn.dafl.pageUIs.OrdersPageUI;

import java.util.List;

/** Page Object cho trang don hang /orders: danh sach, loc, chi tiet va huy don (CN-08). */
public class OrdersPageObject extends BasePage {

    public boolean isOrdersPageDisplayed() {
        return getCurrentPageUrl().contains("/orders");
    }

    public void waitForOrdersLoaded() {
        if (!isElementDisplayedWithWait(OrdersPageUI.ORDERS_LIST, 10)) {
            isElementDisplayedWithWait(OrdersPageUI.ORDERS_EMPTY_STATE, 5);
        }
    }

    /** Don hang duoc coi la xuat hien khi the don order-item-{maDH} co trong danh sach. */
    public boolean isOrderDisplayed(String orderDisplayCode) {
        return isElementDisplayedWithWait(
                getDynamicLocator(OrdersPageUI.DYNAMIC_ORDER_ITEM, orderDisplayCode), 10);
    }

    public boolean isOrderDisplayedByCodeText(String orderCode) {
        return isElementDisplayedWithWait(OrdersPageUI.DYNAMIC_ORDER_CODE_TEXT.replace("%s", orderCode), 10);
    }

    public int getOrderCountOnCurrentPage() {
        return getListWebElements(OrdersPageUI.ANY_ORDER_ITEM).size();
    }

    /** Ma don hien thi (DH...) cua don moi nhat - don dau tien trong danh sach. */
    public String getFirstOrderDisplayCode() {
        List<WebElement> items = getListWebElements(OrdersPageUI.ANY_ORDER_ITEM);
        if (items.isEmpty()) {
            return null;
        }
        String testId = items.get(0).getAttribute("data-testid");
        return testId == null ? null : testId.replace("order-item-", "");
    }

    public boolean isOrdersEmptyStateDisplayed() {
        return isElementDisplayedWithWait(OrdersPageUI.ORDERS_EMPTY_STATE, 5);
    }

    /** Loc danh sach theo tab trang thai tieng Viet (vd "Da huy", "Cho xac nhan").
     *  Web moi dung nhom TAB (order-status-tab-*), khong con <select>: bam truc tiep
     *  vao tab tuong ung. statusLabel rong/null = tab "Tat ca" (order-status-tab-all). */
    public void filterByStatus(String statusLabel) {
        String tabValue = (statusLabel == null || statusLabel.isEmpty()) ? "all" : statusLabel;
        waitForElementVisible(OrdersPageUI.ORDER_STATUS_FILTER);
        clickToElement(OrdersPageUI.DYNAMIC_ORDER_STATUS_TAB, tabValue);
        sleepInSecond(2); // cho danh sach nap lai theo bo loc
    }

    /** Text hien tai cua tab trang thai (bao gom so luong trong ngoac neu co, vd "Tất cả (3)"). */
    public String getOrderStatusTabText(String statusLabel) {
        String tabValue = (statusLabel == null || statusLabel.isEmpty()) ? "all" : statusLabel;
        waitForElementVisible(OrdersPageUI.DYNAMIC_ORDER_STATUS_TAB, tabValue);
        return getElementText(OrdersPageUI.DYNAMIC_ORDER_STATUS_TAB, tabValue).trim();
    }

    /** So luong hien trong ngoac tren tab trang thai; 0 neu tab khong ghi so luong. */
    public int getOrderStatusTabCount(String statusLabel) {
        String text = getOrderStatusTabText(statusLabel);
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\((\\d+)\\)").matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    public boolean isOrderStatusTabDisplayed(String statusLabel) {
        String tabValue = (statusLabel == null || statusLabel.isEmpty()) ? "all" : statusLabel;
        return isElementDisplayedWithWait(getDynamicLocator(OrdersPageUI.DYNAMIC_ORDER_STATUS_TAB, tabValue), 5);
    }

    // ---------- Chi tiet don ----------
    public void openOrderDetail(String orderDisplayCode) {
        clickToElement(OrdersPageUI.DYNAMIC_ORDER_DETAIL_BTN, orderDisplayCode);
        waitForElementVisible(OrdersPageUI.ORDER_DETAIL_VIEW);
    }

    public boolean isOrderDetailDisplayed() {
        return isElementDisplayedWithWait(OrdersPageUI.ORDER_DETAIL_VIEW, 10);
    }

    public String getOrderDetailStatus() {
        waitForElementVisible(OrdersPageUI.ORDER_DETAIL_STATUS);
        return getElementText(OrdersPageUI.ORDER_DETAIL_STATUS);
    }

    public boolean isOrderShippingInfoDisplayed() {
        return isElementDisplayed(OrdersPageUI.ORDER_SHIPPING_INFO);
    }

    // ---------- Huy don (CN-08.3) ----------
    public boolean isCancelOrderButtonDisplayed() {
        return isElementDisplayedWithWait(OrdersPageUI.CANCEL_ORDER_BTN, 5);
    }

    public void clickCancelOrderButton() {
        clickToElement(OrdersPageUI.CANCEL_ORDER_BTN);
        waitForElementVisible(OrdersPageUI.CANCEL_ORDER_DIALOG);
    }

    /** Chon ly do san co trong danh sach ly do huy. */
    public void selectCancelReason(String reasonLabel) {
        selectByVisibleText(OrdersPageUI.CANCEL_REASON_SELECT, reasonLabel);
    }

    /** Chon "Khac" va nhap ly do tu do. */
    public void enterCustomCancelReason(String reasonText) {
        selectByVisibleText(OrdersPageUI.CANCEL_REASON_SELECT, "Khác");
        sendKeyToElement(OrdersPageUI.CANCEL_REASON_INPUT, reasonText);
    }

    public void confirmCancelOrder() {
        clickToElement(OrdersPageUI.CANCEL_CONFIRM_BTN);
    }

    public void dismissCancelDialog() {
        clickToElement(OrdersPageUI.CANCEL_CANCEL_BTN);
    }

    /** Huy don tron goi: mo dialog, chon ly do, xac nhan. */
    public void cancelOrderWithReason(String reasonLabel) {
        clickCancelOrderButton();
        selectCancelReason(reasonLabel);
        confirmCancelOrder();
    }

    public boolean isCancelSuccessDisplayed() {
        return isElementDisplayedWithWait(OrdersPageUI.CANCEL_SUCCESS_MSG, 10);
    }

    public String getCancelSuccessMessage() {
        waitForElementVisible(OrdersPageUI.CANCEL_SUCCESS_MSG);
        return getElementText(OrdersPageUI.CANCEL_SUCCESS_MSG);
    }

    // ---------- Wave15.1-auto: mo rong cho OrdersExtendedTest (Pha B, buoc 3) ----------

    // Wave15.1-auto: TC_SCR8_UI_013/014/015 — text gop cua the don order-item-{ma}.
    public String getOrderItemText(String orderDisplayCode) {
        return getElementText(OrdersPageUI.DYNAMIC_ORDER_ITEM, orderDisplayCode);
    }

    // Wave15.1-auto: TC_SCR8_UI_013 — so dong san pham hien trong the don (toi da 3 dong).
    public int getOrderItemRowCount(String orderDisplayCode) {
        return getListWebElements(OrdersPageUI.DYNAMIC_ORDER_ITEM_ROWS, orderDisplayCode).size();
    }

    // Wave15.1-auto: TC_SCR8_FUNC_014 — nut "Thanh toán ngay" trong the don (chi hien khi nhan "Chờ thanh toán").
    public boolean isPayNowButtonDisplayed(String orderDisplayCode) {
        return isElementDisplayed(
                getDynamicLocator(OrdersPageUI.DYNAMIC_ORDER_PAY_NOW_BTN, orderDisplayCode));
    }

    public void clickPayNowButton(String orderDisplayCode) {
        clickToElement(OrdersPageUI.DYNAMIC_ORDER_PAY_NOW_BTN, orderDisplayCode);
    }

    // Wave15.1-auto: TC_SCR8_FUNC_015 — nut "Mua lại" trong the don (Paid/Delivered/Completed/Cancelled).
    public boolean isReorderButtonDisplayed(String orderDisplayCode) {
        return isElementDisplayed(
                getDynamicLocator(OrdersPageUI.DYNAMIC_ORDER_REORDER_BTN, orderDisplayCode));
    }

    public void clickReorderButton(String orderDisplayCode) {
        clickToElement(OrdersPageUI.DYNAMIC_ORDER_REORDER_BTN, orderDisplayCode);
    }

    // Wave15.1-auto: TC_SCR8_UI_017/018, TC_SCR8_FUNC_022..026 — text gop cua trang chi tiet don.
    public String getOrderDetailText() {
        waitForElementVisible(OrdersPageUI.ORDER_DETAIL_VIEW);
        return getElementText(OrdersPageUI.ORDER_DETAIL_VIEW);
    }

    // Wave15.1-auto: TC_SCR8_UI_017 — text cua khoi timeline (6 buoc vong doi + lich su trang thai).
    public String getOrderTimelineText() {
        waitForElementVisible(OrdersPageUI.ORDER_TIMELINE);
        return getElementText(OrdersPageUI.ORDER_TIMELINE);
    }

    // Wave15.1-auto: TC_SCR8_UI_017 — text cua khoi thong tin giao hang trong chi tiet don.
    public String getOrderShippingInfoText() {
        waitForElementVisible(OrdersPageUI.ORDER_SHIPPING_INFO);
        return getElementText(OrdersPageUI.ORDER_SHIPPING_INFO);
    }

    // Wave15.1-auto: TC_SCR8_UI_021 — text gop cua hop xac nhan huy don (tieu de + noi dung + ly do).
    public String getCancelDialogText() {
        waitForElementVisible(OrdersPageUI.CANCEL_ORDER_DIALOG);
        return getElementText(OrdersPageUI.CANCEL_ORDER_DIALOG);
    }

    // Wave15.1-auto: TC_SCR8_FUNC_026 — o nhap ly do tu do co hien khong sau khi chon "Khác".
    public boolean isCancelReasonInputDisplayed() {
        return isElementDisplayed(OrdersPageUI.CANCEL_REASON_INPUT);
    }

    // Wave15.1-auto: TC_SCR8_UI_021 — ly do dang duoc chon trong hop xac nhan huy don.
    public String getSelectedCancelReason() {
        return getSelectedValue(OrdersPageUI.CANCEL_REASON_SELECT);
    }

    // Wave15.1-auto: TC_SCR8_UI_012 — chuyen trang danh sach don (10 don/trang).
    public void clickOrdersNextPage() {
        clickToElement(OrdersPageUI.ORDERS_NEXT_BTN);
        sleepInSecond(2); // cho danh sach trang sau tai lai
    }

    public void clickOrdersPreviousPage() {
        clickToElement(OrdersPageUI.ORDERS_PREV_BTN);
        sleepInSecond(2); // cho danh sach trang truoc tai lai
    }

    public boolean isOrdersNextButtonEnabled() {
        return isElementEnabled(OrdersPageUI.ORDERS_NEXT_BTN);
    }

    public boolean isOrdersPrevButtonEnabled() {
        return isElementEnabled(OrdersPageUI.ORDERS_PREV_BTN);
    }

    public String getOrdersPageIndicatorText() {
        waitForElementVisible(OrdersPageUI.ORDERS_PAGE_INDICATOR);
        return getElementText(OrdersPageUI.ORDERS_PAGE_INDICATOR);
    }
}
