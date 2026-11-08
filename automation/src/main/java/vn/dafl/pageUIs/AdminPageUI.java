package vn.dafl.pageUIs;

/** Locator cua trang quan tri /admin (dot 3, chi Super Admin).
 *  Bao ve: chua dang nhap -> chuyen /login; tai khoan thuong -> trang 403
 *  (admin-forbidden, kem huong dan cap quyen bang SQL); Super Admin -> menu 8 muc.
 *  Cac test chi dung thao tac DOC (GET) - khong doi trang thai don, khong them/sua/xoa
 *  san pham, khong khoa tai khoan, de khong pha du lieu seed dung chung. */
public class AdminPageUI {

    public static final String ADMIN_PAGE = "css=[data-testid='admin-page']";
    public static final String ADMIN_FORBIDDEN = "css=[data-testid='admin-forbidden']";
    public static final String DYNAMIC_ADMIN_NAV = "css=[data-testid='admin-nav-%s']";
    public static final String ADMIN_NAV_DASHBOARD = "css=[data-testid='admin-nav-dashboard']";
    public static final String ADMIN_NAV_ORDERS = "css=[data-testid='admin-nav-orders']";
    public static final String ADMIN_NAV_PRODUCTS = "css=[data-testid='admin-nav-products']";
    public static final String ADMIN_NAV_CATEGORIES = "css=[data-testid='admin-nav-categories']";
    public static final String ADMIN_NAV_PROMOTIONS = "css=[data-testid='admin-nav-promotions']";
    public static final String ADMIN_NAV_CUSTOMERS = "css=[data-testid='admin-nav-customers']";
    public static final String ADMIN_NAV_MEMBERSHIP = "css=[data-testid='admin-nav-membership']";
    public static final String ADMIN_NAV_RETURNS = "css=[data-testid='admin-nav-returns']";

    // Tieu de muc Tong quan (h2) + o tim kiem cua cac muc chi doc
    public static final String ADMIN_DASHBOARD_HEADING = "xpath=//h2[contains(text(),'Tổng quan')]";
    public static final String ADMIN_ORDER_SEARCH = "css=[data-testid='admin-order-search']";
    public static final String ADMIN_PRODUCT_SEARCH = "css=[data-testid='admin-product-search']";
    public static final String ADMIN_CUSTOMER_SEARCH = "css=[data-testid='admin-customer-search']";
    public static final String ADMIN_PROMO_ADD_BTN = "css=[data-testid='admin-promo-add-btn']";
    public static final String ADMIN_CATEGORY_ADD_BTN = "css=[data-testid='admin-category-add-btn']";

    // Bieu do doanh thu (dot 4, muc Tong quan): SVG thuan tu GET /api/admin/reports/revenue.
    // Chi hien khi co don trong khoang (totalOrders > 0); chua co doanh thu thi hien dong
    // "Chưa có doanh thu trong khoảng thời gian này." thay cho bieu do.
    public static final String ADMIN_REVENUE_RANGE_7 = "css=[data-testid='admin-revenue-range-7']";
    public static final String ADMIN_REVENUE_RANGE_30 = "css=[data-testid='admin-revenue-range-30']";
    public static final String ADMIN_REVENUE_CHART = "css=[data-testid='admin-revenue-chart']";
    public static final String ADMIN_REVENUE_CHART_SVG = "css=[data-testid='admin-revenue-chart'] svg";
    public static final String ADMIN_REVENUE_EMPTY_MSG =
            "xpath=//*[contains(text(),'Chưa có doanh thu trong khoảng thời gian này.')]";
}
