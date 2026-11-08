package vn.dafl.pageObjects;

import vn.dafl.commons.BasePage;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageUIs.AdminPageUI;

import java.util.ArrayList;
import java.util.List;

/** Page Object cho trang quan tri /admin (dot 3, chi Super Admin).
 *  Chi thao tac DOC (GET): mo trang, doc menu, chuyen muc, kiem tra o tim kiem hien thi.
 *  Khong doi trang thai don, khong them/sua/xoa san pham/khuyen mai, khong khoa tai
 *  khoan, khong doi mat khau admin - de khong pha du lieu seed dung chung. */
public class AdminPageObject extends BasePage {

    public static final String[] ADMIN_NAV_KEYS = {
            "dashboard", "orders", "products", "categories",
            "promotions", "customers", "membership", "returns"
    };

    /** Mo thang /admin (khong qua menu header). */
    public void openAdminPage() {
        openPageUrl(GlobalConstants.getAppUrl() + "/admin");
    }

    /** Cho toi khi trang admin quyet dinh xong quyen: hoac menu quan tri, hoac trang 403,
     *  hoac bi chuyen ve /login (khach). */
    public void waitForAdminGateResolved() {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            if (isElementDisplayed(AdminPageUI.ADMIN_NAV_DASHBOARD)
                    || isElementDisplayed(AdminPageUI.ADMIN_FORBIDDEN)
                    || getCurrentPageUrl().contains("/login")) {
                return;
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Mo /admin 15 giay van chua ro quyen (khong thay menu, trang 403 hay /login)");
    }

    public boolean isAdminMenuDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_NAV_DASHBOARD, 10);
    }

    public boolean isForbiddenDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_FORBIDDEN, 10);
    }

    public String getForbiddenText() {
        waitForElementVisible(AdminPageUI.ADMIN_FORBIDDEN);
        return getElementText(AdminPageUI.ADMIN_FORBIDDEN);
    }

    /** Danh sach key menu quan tri dang hien thi (du kien du 8 muc). */
    public List<String> getVisibleAdminNavKeys() {
        List<String> visible = new ArrayList<>();
        for (String key : ADMIN_NAV_KEYS) {
            if (isElementDisplayed(AdminPageUI.DYNAMIC_ADMIN_NAV, key)) {
                visible.add(key);
            }
        }
        return visible;
    }

    public void clickAdminNav(String navKey) {
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_NAV, navKey);
        sleepInSecond(2); // cho muc tai du lieu (lazy load theo tab)
    }

    public boolean isDashboardHeadingDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_DASHBOARD_HEADING, 10);
    }

    public boolean isOrderSearchDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_ORDER_SEARCH, 10);
    }

    public boolean isProductSearchDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_SEARCH, 10);
    }

    public boolean isCustomerSearchDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CUSTOMER_SEARCH, 10);
    }

    public boolean isPromoAddButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_ADD_BTN, 10);
    }

    public boolean isCategoryAddButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_ADD_BTN, 10);
    }

    // ---------- Bieu do doanh thu (dot 4, chi doc GET /api/admin/reports/revenue) ----------
    public boolean isRevenueRange7Displayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_RANGE_7, 10);
    }

    public boolean isRevenueRange30Displayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_RANGE_30, 10);
    }

    public void clickRevenueRange7() {
        clickToElement(AdminPageUI.ADMIN_REVENUE_RANGE_7);
        sleepInSecond(2); // cho GET /api/admin/reports/revenue?from&to tai lai
    }

    public void clickRevenueRange30() {
        clickToElement(AdminPageUI.ADMIN_REVENUE_RANGE_30);
        sleepInSecond(2);
    }

    public boolean isRevenueChartDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_CHART, 10);
    }

    public boolean isRevenueChartSvgDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_CHART_SVG, 5);
    }

    public boolean isRevenueEmptyMessageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_EMPTY_MSG, 5);
    }

    /** Trang thai tai du lieu doanh thu: "chart" neu co bieu do, "empty" neu chua co
     *  doanh thu trong khoang, "unknown" neu ca hai deu khong thay. */
    public String getRevenueState() {
        if (isRevenueChartDisplayed()) {
            return "chart";
        }
        if (isRevenueEmptyMessageDisplayed()) {
            return "empty";
        }
        return "unknown";
    }
}
