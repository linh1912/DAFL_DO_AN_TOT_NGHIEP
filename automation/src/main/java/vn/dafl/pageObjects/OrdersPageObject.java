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

    /** Loc danh sach theo nhan trang thai tieng Viet (vd "Da huy", "Cho xac nhan"). */
    public void filterByStatus(String statusLabel) {
        selectByValue(OrdersPageUI.ORDER_STATUS_FILTER, statusLabel);
        sleepInSecond(2); // cho danh sach nap lai theo bo loc
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
}
