package vn.dafl.tests.admin.system;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;

import java.util.Arrays;
import java.util.List;

/**
 * Phan quyen + tong quan Admin tren app Admin rieng localhost:3001 (dot 3-4,
 * tach tu AdminAndNewsletterTest cu khi chia thu muc admin dot 17 — moi lop
 * chi lo 1 phan he).
 * Admin: chi test DOC (GET) - mo trang, doc menu, chuyen muc, tong quan.
 * KHONG doi trang thai don, KHONG them/sua/xoa san pham/khuyen mai, KHONG khoa
 * tai khoan, KHONG doi mat khau admin.
 * Dot 21 (22:18): menu DROPDOWN (ghi de menu phang dot 18 wave D) — Tong quan
 * tren cung, khong tieu de nhom, roi 4 nhom BÁN HÀNG / SẢN PHẨM / QUẢN TRỊ /
 * HỆ THỐNG; tieu de nhom la <button> co chevron phai + aria-expanded; mac
 * dinh chi nhom chua tab hien tai MO, cac nhom khac DONG; trang thai nho qua
 * localStorage 'dafl_admin_nav_groups'; thu tu muc trong DOM: dashboard,
 * orders, returns, promotions, products, categories, reviews, customers,
 * membership, logs. Dashboard moi la component rieng (h1 'Tổng quan', KPI +
 * khoi 'Cần xử lý ngay', khong con bieu do cu / nut 7-30 ngay).
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Tổng quan và phân quyền")
public class AdminAccessTest extends BaseTest {

    private static final List<String> EXPECTED_ADMIN_NAV_KEYS = Arrays.asList(
            "dashboard", "orders", "returns", "promotions", "products",
            "categories", "reviews", "customers", "membership", "logs"
    );

    private static final List<String> NAV_GROUP_KEYS = Arrays.asList(
            "sales", "products", "customers", "system"
    );

    /** Nut tieu de nhom dropdown (data-testid admin-nav-group-*). */
    private WebElement navGroupElement(String groupKey) {
        return getDriver().findElement(By.cssSelector("[data-testid='admin-nav-group-" + groupKey + "']"));
    }

    private boolean isNavGroupExpanded(String groupKey) {
        return "true".equals(navGroupElement(groupKey).getAttribute("aria-expanded"));
    }

    /** Mo nhom neu dang dong (khong bam lai khi da mo — tranh dao trang thai). */
    private void expandNavGroup(String groupKey) {
        if (!isNavGroupExpanded(groupKey)) {
            navGroupElement(groupKey).click();
            sleepInSecond(1); // cho hieu ung mo ~200ms
        }
    }

    private void expandAllNavGroups() {
        for (String groupKey : NAV_GROUP_KEYS) {
            expandNavGroup(groupKey);
        }
    }

    /** Dang nhap bang tai khoan Super Admin tren app Admin rieng :3001; bo qua test
     *  neu tai khoan chua duoc seed tren DB nay (DbInitializer chi seed admin@dafl.vn
     *  khi bang Users trong). */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    @Story("Phân quyền truy cập")
    @Test(description = "ADM-01: Khách chưa đăng nhập mở /admin → bị chuyển về trang đăng nhập")
    public void guestOpeningAdminIsRedirectedToLogin() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "Khach mo /admin phai bi chuyen ve /login, dang o: " + getDriver().getCurrentUrl());
    }

    @Story("Phân quyền truy cập")
    @Test(description = "ADM-02: Tài khoản thường mở /admin → trang 403 kèm hướng dẫn cấp quyền Super Admin")
    public void customerOpeningAdminSeesForbiddenPageWithGuidance() {
        // Dang nhap bang tai khoan thuong ngay tren trang dang nhap Admin rieng :3001
        // (khong qua web ban hang).
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsUserOnAdminAppOrSkip(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        adminPage.openAdminPage();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isForbiddenDisplayed(), "Tai khoan thuong mo /admin phai thay trang 403");
        String forbiddenText = adminPage.getForbiddenText();
        Assert.assertTrue(forbiddenText.contains(GlobalConstants.ADMIN_FORBIDDEN_MESSAGE),
                "Trang 403 phai co thong diep khong co quyen, dang thay: " + forbiddenText);
        Assert.assertTrue(forbiddenText.contains(GlobalConstants.ADMIN_FORBIDDEN_GUIDE_HEADING),
                "Trang 403 phai co huong dan cap quyen Super Admin");
        Assert.assertTrue(forbiddenText.contains("SuperAdmin"),
                "Trang 403 phai nhac toi vai tro SuperAdmin");
        Assert.assertTrue(forbiddenText.contains(GlobalConstants.DEMO_EMAIL),
                "Trang 403 phai hien email tai khoan dang dang nhap (" + GlobalConstants.DEMO_EMAIL + ")");
    }

    @Story("Tổng quan và điều hướng")
    @Test(description = "ADM-03 (đợt 21, menu dropdown 22:18): Super Admin mở /admin → mặc định ở tab Tổng quan mọi nhóm ĐÓNG (chỉ thấy Tổng quan); tiêu đề nhóm là <button> có chevron + aria-expanded, nhóm khách hàng đổi tên 'Quản trị'; bấm mở 4 nhóm → đủ 10 mục menu theo đúng thứ tự (dashboard, orders, returns, promotions, products, categories, reviews, customers, membership, logs)")
    public void adminSeesTenMenuItems() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        // Mặc định ở tab Tổng quan (không thuộc nhóm nào): mọi nhóm ĐÓNG.
        for (String groupKey : NAV_GROUP_KEYS) {
            Assert.assertFalse(isNavGroupExpanded(groupKey),
                    "Nhom " + groupKey + " phai DONG khi moi mo /admin o tab Tong quan");
        }
        List<String> initiallyVisible = adminPage.getVisibleAdminNavKeys();
        Assert.assertEquals(initiallyVisible, Arrays.asList("dashboard"),
                "Mac dinh chi thay muc Tong quan, dang thay: " + initiallyVisible);
        // Tiêu đề nhóm là <button> dropdown có chevron (svg) + aria-expanded;
        // nhóm khách hàng đổi tên "Quản lý khách hàng" → "Quản trị".
        WebElement customersGroup = navGroupElement("customers");
        Assert.assertEquals(customersGroup.getTagName(), "button", "Tieu de nhom phai la <button> dropdown");
        Assert.assertFalse(customersGroup.findElements(By.tagName("svg")).isEmpty(),
                "Tieu de nhom phai co chevron (svg) o phia phai");
        String customersTitle = adminPage.getNavGroupTitleText("customers");
        Assert.assertTrue(customersTitle.contains("Quản trị"),
                "Nhom khach hang phai doi ten thanh 'Quản trị', dang thay: " + customersTitle);
        // Mở cả 4 nhóm → đủ 10 mục đúng thứ tự.
        expandAllNavGroups();
        List<String> visible = adminPage.getVisibleAdminNavKeys();
        Assert.assertEquals(visible, EXPECTED_ADMIN_NAV_KEYS,
                "Menu quan tri phai du 10 muc theo dung thu tu, dang thay: " + visible);
    }

    @Story("Tổng quan và điều hướng")
    @Test(description = "ADM-03D (đợt 21, menu dropdown): bấm tiêu đề nhóm đóng/mở từng nhóm (nhóm khác không đổi); reload nhớ trạng thái qua localStorage; điều hướng từ dashboard qua nút 'Xem' ở 'Đơn chờ xác nhận' TỰ MỞ nhóm Bán hàng đang đóng")
    public void adminNavGroupDropdownToggleAndPersistence() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        // Bấm tiêu đề nhóm Bán hàng → mở; bấm lại → đóng; nhóm khác không đổi.
        boolean productsBefore = isNavGroupExpanded("products");
        navGroupElement("sales").click();
        Assert.assertTrue(isNavGroupExpanded("sales"), "Bam tieu de nhom Ban hang phai MO nhom");
        Assert.assertTrue(adminPage.getVisibleAdminNavKeys().contains("orders"),
                "Mo nhom Ban hang phai thay muc Don hang");
        Assert.assertEquals(isNavGroupExpanded("products"), productsBefore,
                "Dong/mo nhom Ban hang khong duoc doi trang thai nhom San pham");
        navGroupElement("sales").click();
        Assert.assertFalse(isNavGroupExpanded("sales"), "Bam lai tieu de nhom Ban hang phai DONG nhom");
        Assert.assertFalse(adminPage.getVisibleAdminNavKeys().contains("orders"),
                "Dong nhom Ban hang phai an muc Don hang");

        // Reload: trạng thái đóng của nhóm Bán hàng được nhớ qua localStorage.
        getDriver().navigate().refresh();
        adminPage.waitForAdminGateResolved();
        Assert.assertFalse(isNavGroupExpanded("sales"),
                "Reload phai nho nhom Ban hang dang DONG (localStorage dafl_admin_nav_groups)");

        // Điều hướng từ dashboard qua nút "Xem" ở "Đơn chờ xác nhận" → tab Đơn
        // hàng, nhóm Bán hàng đang đóng TỰ MỞ. Cần có đơn chờ xác nhận (nút Xem
        // disable khi 0 đơn) — không có thì bỏ qua phần này (đã phủ ở Vitest).
        WebElement pendingOrdersView = getDriver().findElement(
                By.cssSelector("[data-testid='admin-dashboard-action-pending-orders-view']"));
        if (!pendingOrdersView.isEnabled()) {
            throw new SkipException("Khong co don cho xac nhan (nut Xem dang disable) nen khong kiem tra duoc tu mo nhom; phan tu mo da phu o Vitest AdminDot18");
        }
        adminPage.clickDashboardPendingOrdersView();
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(), "Bam Xem o Don cho xac nhan phai sang tab Don hang");
        Assert.assertTrue(isNavGroupExpanded("sales"), "Sang tab Don hang phai TU MO nhom Ban hang dang dong");
    }

    @Story("Tổng quan và điều hướng")
    @Test(description = "ADM-04 (đợt 18, dashboard mới): Super Admin mở /admin → mục Tổng quan tải được (h1 'Tổng quan' + khối 'Cần xử lý ngay')")
    public void adminDashboardLoads() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Muc Tong quan phai hien tieu de h1 'Tổng quan'");
        Assert.assertTrue(adminPage.isDashboardActionNeededDisplayed(),
                "Dashboard moi phai co khoi 'Cần xử lý ngay' (admin-dashboard-action-needed)");
    }

    @Story("Tổng quan và điều hướng")
    @Test(description = "ADM-05 (chỉ đọc, đợt 21 menu dropdown): Super Admin chuyển mục Đơn hàng / Sản phẩm / Khách hàng thấy được nội dung (mở nhóm chứa mục trước khi bấm vì mặc định các nhóm đóng)")
    public void adminCanBrowseOrdersProductsCustomersReadOnly() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        expandAllNavGroups();

        adminPage.clickAdminNav("orders");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(), "Muc Don hang phai co o tim kiem don");

        adminPage.clickAdminNav("products");
        Assert.assertTrue(adminPage.isProductSearchDisplayed(), "Muc San pham phai co o tim kiem san pham");

        adminPage.clickAdminNav("customers");
        Assert.assertTrue(adminPage.isCustomerSearchDisplayed(), "Muc Khach hang phai co o tim kiem khach hang");
    }

    @Story("Biểu đồ doanh thu")
    @Test(description = "ADM-06 (đợt 18, viết lại theo dashboard mới — bieu do cu + nut 7/30 ngay da bo): Dashboard mới hiện đủ KPI (Doanh thu, Đơn hàng, Khách hàng mới, Tỷ lệ hủy/trả hàng) + khối 'Cần xử lý ngay'")
    public void adminRevenueChartLoadsAndRangeButtonsWork() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Phai o muc Tong quan");

        // Dashboard moi (W5): KPI tinh tu du lieu that qua cac endpoint hien co;
        // bieu do cu (admin-revenue-chart) + 2 nut chon khoang 7/30 ngay DA BO
        // han dot 18 — chi khang dinh KPI + khoi hanh dong hien (chi tiet bieu
        // do / so sanh ky truoc cover sau o Vitest AdminDashboard.test.tsx).
        String dashboardText = adminPage.getDashboardText();
        Assert.assertTrue(dashboardText.contains("Doanh thu"),
                "Dashboard moi phai co KPI 'Doanh thu', dang thay: " + dashboardText);
        Assert.assertTrue(dashboardText.contains("Đơn hàng"),
                "Dashboard moi phai co KPI 'Đơn hàng', dang thay: " + dashboardText);
        Assert.assertTrue(dashboardText.contains("Khách hàng mới"),
                "Dashboard moi phai co KPI 'Khách hàng mới', dang thay: " + dashboardText);
        Assert.assertTrue(dashboardText.contains("Tỷ lệ hủy/trả hàng"),
                "Dashboard moi phai co KPI 'Tỷ lệ hủy/trả hàng', dang thay: " + dashboardText);
        Assert.assertTrue(adminPage.isDashboardActionNeededDisplayed(),
                "Dashboard moi phai co khoi 'Cần xử lý ngay'");
    }
}
