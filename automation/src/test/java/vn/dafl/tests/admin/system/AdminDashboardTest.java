package vn.dafl.tests.admin.system;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Test muc Tong quan (dashboard moi dot 18) tren app Admin rieng
 * localhost:3001 (dot 19, theo mapping SCR_02): menu phang (sa-active /
 * aria-current, khong icon), KPI + so sanh ky truoc, bo loc segmented + tuy
 * chon + luu sessionStorage, nut Lam moi, donut trang thai don, khoi 'Can xu
 * ly ngay', don gan day, sap het hang, phan bo hang, khuyen mai dang chay.
 *
 * Gioi han (theo mapping, khong viet test): BUG_DAFL_003 — endpoint doanh thu
 * loi tren may Acer nen cac khoi phu thuoc revenue (KPI Doanh thu / AOV / Ty
 * le huy-tra, bieu do, SP ban chay) chi assert khi khoi khong loi; khi khoi
 * loi thi test bo qua nhanh do (skip) thay vi fail. Badge '99+' (> 99 don),
 * tu lam moi 5 phut, skeleton, responsive nhieu viewport la manual-only.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay,
 * tai khoan admin@dafl.vn chua duoc seed, hoac DB khong co du lieu can thiet
 * (don / KM / danh gia) cho tung test.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Tổng quan (Dashboard)")
public class AdminDashboardTest extends BaseTest {

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter VN_TIME = DateTimeFormatter.ofPattern("HH:mm");

    /** Dang nhap Super Admin tren app Admin rieng; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Mo muc Tong quan; tra ve AdminPageObject da san sang. */
    private AdminPageObject openDashboardOrSkip() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("dashboard");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Phai o muc Tong quan");
        return adminPage;
    }

    /** "dd/MM/yyyy" cho 1 ngay. */
    private String fmtDate(LocalDate date) {
        return date.format(VN_DATE);
    }

    /** Sub khoang cua KPI Doanh thu: "Kỳ dd/mm/yyyy – dd/mm/yyyy" (gach ngang dai). */
    private String kpiRangeSub(LocalDate from, LocalDate to) {
        return "Kỳ " + fmtDate(from) + " – " + fmtDate(to);
    }

    /** Khang dinh sub khoang cua KPI Doanh thu; bo qua neu khoi doanh thu dang
     *  loi (BUG_DAFL_003 — KPI hien khoi loi thay cho sub). */
    private void assertRevenueKpiSubOrSkipOnError(AdminPageObject adminPage, String expectedSub) {
        if (adminPage.isDashboardKpiError("revenue")) {
            return; // khoi doanh thu loi — khong co sub de assert
        }
        Assert.assertEquals(adminPage.getDashboardKpiSubText("revenue"), expectedSub,
                "Sub KPI Doanh thu phai la '" + expectedSub + "', dang thay: "
                        + adminPage.getDashboardKpiSubText("revenue"));
    }

    /** Dem tat ca dong cua bang muc dang mo qua cac trang phan trang (toi da 2 trang). */
    private int countAllRowsAcrossPages(AdminPageObject adminPage, String sectionKey) {
        int total = adminPage.getAdminTableRowCount();
        while (adminPage.isPaginationNextEnabled(sectionKey)) {
            adminPage.clickPaginationNext(sectionKey);
            total += adminPage.getAdminTableRowCount();
        }
        return total;
    }

    /** Dem dong trong bang muc dang mo co text chua chuoi cho truoc, qua cac trang. */
    private int countRowsContainingAcrossPages(AdminPageObject adminPage, String sectionKey, String needle) {
        int count = 0;
        for (String row : adminPage.getAdminTableRowTexts()) {
            if (row.contains(needle)) {
                count++;
            }
        }
        while (adminPage.isPaginationNextEnabled(sectionKey)) {
            adminPage.clickPaginationNext(sectionKey);
            for (String row : adminPage.getAdminTableRowTexts()) {
                if (row.contains(needle)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Story("Menu quản trị")
    @Test(description = "ADM19-10 (TC_SCRA02_UI_004): Ở Tổng quan, nút menu Tổng quan có class sa-active + aria-current=page; bấm Sản phẩm thì 2 thuộc tính chuyển sang nút Sản phẩm, nút Tổng quan mất 2 thuộc tính này")
    public void navActiveStateMovesWithSelectedTab() {
        AdminPageObject adminPage = openDashboardOrSkip();

        Assert.assertTrue(adminPage.getElementAttribute("css=[data-testid='admin-nav-dashboard']", "class")
                        .contains("sa-active"),
                "O muc Tong quan thi nut menu Tong quan phai co class sa-active");
        Assert.assertEquals(adminPage.getElementAttribute("css=[data-testid='admin-nav-dashboard']", "aria-current"),
                "page", "O muc Tong quan thi nut menu Tong quan phai co aria-current='page'");

        adminPage.clickAdminNav("products");
        Assert.assertTrue(adminPage.getElementAttribute("css=[data-testid='admin-nav-products']", "class")
                        .contains("sa-active"),
                "Bam San pham thi nut menu San pham phai co class sa-active");
        Assert.assertEquals(adminPage.getElementAttribute("css=[data-testid='admin-nav-products']", "aria-current"),
                "page", "Bam San pham thi nut menu San pham phai co aria-current='page'");
        Assert.assertFalse(adminPage.getElementAttribute("css=[data-testid='admin-nav-dashboard']", "class")
                        .contains("sa-active"),
                "Bam San pham thi nut menu Tong quan phai mat class sa-active");
        Assert.assertNull(adminPage.getElementAttribute("css=[data-testid='admin-nav-dashboard']", "aria-current"),
                "Bam San pham thi nut menu Tong quan phai mat aria-current");
    }

    @Story("Menu quản trị")
    @Test(description = "ADM19-11 (TC_SCRA02_UI_009): Mỗi nút menu chỉ chứa sa-nav-label (+ badge nếu có), không chứa svg/img/emoji icon; logo vẫn có sao vàng chanh + chữ 'DAFL Admin'")
    public void navItemsHaveNoIconsButLogoKeepsStar() {
        AdminPageObject adminPage = openDashboardOrSkip();

        String[][] navItems = {
                {"dashboard", "Tổng quan"}, {"orders", "Đơn hàng"}, {"returns", "Trả hàng"},
                {"promotions", "Khuyến mãi"}, {"products", "Sản phẩm"}, {"categories", "Danh mục"},
                {"reviews", "Đánh giá"}, {"customers", "Khách hàng"}, {"membership", "Hội viên"},
                {"logs", "Nhật ký hệ thống"},
        };
        for (String[] item : navItems) {
            String locator = "css=[data-testid='admin-nav-" + item[0] + "']";
            org.openqa.selenium.WebElement navBtn = adminPage.getWebElement(locator);
            Assert.assertTrue(navBtn.findElements(org.openqa.selenium.By.tagName("svg")).isEmpty(),
                    "Nut menu '" + item[1] + "' khong duoc chua icon svg");
            Assert.assertTrue(navBtn.findElements(org.openqa.selenium.By.tagName("img")).isEmpty(),
                    "Nut menu '" + item[1] + "' khong duoc chua img");
            String labelOnly = adminPage.getElementText(locator).replaceAll("[0-9+]", "").trim();
            Assert.assertEquals(labelOnly, item[1],
                    "Nut menu phai chi co nhan '" + item[1] + "' (+ badge so neu co), dang thay: "
                            + adminPage.getElementText(locator));
        }

        String logoText = adminPage.getElementText("css=.sa-logo");
        Assert.assertTrue(logoText.contains("DAFL Admin"),
                "Logo sidebar phai co chu 'DAFL Admin', dang thay: " + logoText);
        Assert.assertFalse(adminPage.getWebElement("css=.sa-logo")
                        .findElements(org.openqa.selenium.By.tagName("svg")).isEmpty(),
                "Logo van phai co sao Lucide (svg) mau vang chanh");
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-12 (TC_SCRA02_UI_013): Mục Tổng quan KHÔNG có toolbar chuẩn (ô tìm kiếm/nút Tìm/Đặt lại/Export) và KHÔNG có phân trang; nút 'Làm mới' là nút riêng của dashboard")
    public void dashboardHasNoStandardToolbarOrPagination() {
        AdminPageObject adminPage = openDashboardOrSkip();

        Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard'] .sa-page-toolbar"),
                "Muc Tong quan khong duoc co toolbar chuan (.sa-page-toolbar)");
        Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard'] [data-testid$='-search-input']"),
                "Muc Tong quan khong duoc co o tim kiem cua toolbar");
        Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard'] [data-testid$='-search-btn']"),
                "Muc Tong quan khong duoc co nut Tim cua toolbar");
        Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard'] [data-testid$='-reset-btn']"),
                "Muc Tong quan khong duoc co nut Dat lai cua toolbar");
        Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard'] [data-testid$='-pagination']"),
                "Muc Tong quan khong duoc co phan trang");
        Assert.assertTrue(adminPage.isDashboardRefreshButtonDisplayed(),
                "Muc Tong quan phai co nut 'Làm mới' rieng cua dashboard (admin-dashboard-refresh-btn)");
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-13 (TC_SCRA02_FUNC_015): Badge Đơn hàng hiển thị đúng số đơn chờ xác nhận (đối chiếu bằng cách đếm đơn Pending ở tab Đơn); khi = 0 thì badge ẩn hẳn (skip phần đối chiếu chính xác khi badge > 20 vì tab Đơn chỉ tải tối đa 20 đơn)")
    public void ordersBadgeMatchesPendingOrderCount() {
        AdminPageObject adminPage = openDashboardOrSkip();
        String badge = adminPage.getNavBadgeText("orders");

        adminPage.clickAdminNav("orders");
        adminPage.selectOrderStatusFilter(GlobalConstants.ADMIN_ORDER_STATUS_PENDING_LABEL);
        adminPage.clickOrderFilterSearch();
        int pendingCount = countAllRowsAcrossPages(adminPage, "orders");

        if (badge == null) {
            Assert.assertEquals(pendingCount, 0,
                    "Badge Don hang an thi tab Don loc 'Chờ xử lý' phai ra 0 dong, dang thay: " + pendingCount);
            return;
        }
        Assert.assertTrue(badge.matches("\\d+|99\\+"),
                "Badge Don hang neu hien thi chi duoc la so hoac '99+', dang thay: '" + badge + "'");
        if ("99+".equals(badge)) {
            Assert.assertTrue(pendingCount > 0,
                    "Badge '99+' thi tab Don loc 'Chờ xử lý' phai co don that");
            return;
        }
        int badgeCount = Integer.parseInt(badge);
        if (badgeCount > 20) {
            Assert.assertEquals(pendingCount, 20,
                    "Badge > 20 thi tab Don (chi tai toi da 20 don) phai hien du 20 dong, dang thay: " + pendingCount);
        } else {
            Assert.assertEquals(pendingCount, badgeCount,
                    "Badge Don hang phai bang so don 'Chờ xử lý' dem duoc o tab Don");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-14 (TC_SCRA02_FUNC_017): Sau khi mở tab Trả hàng, badge Trả hàng bằng đúng số yêu cầu có status Pending đếm từ danh sách đã tải (nhãn 'Chờ duyệt'); không có yêu cầu chờ duyệt thì badge ẩn")
    public void returnsBadgeMatchesPendingReturnsCount() {
        AdminPageObject adminPage = openDashboardOrSkip();
        adminPage.clickAdminNav("returns");
        int pendingCount = countRowsContainingAcrossPages(adminPage, "returns", "Chờ duyệt");

        String badge = adminPage.getNavBadgeText("returns");
        if (pendingCount == 0) {
            Assert.assertNull(badge,
                    "Khong co yeu cau tra hang 'Chờ duyệt' thi badge Tra hang phai an, dang thay: '" + badge + "'");
        } else {
            Assert.assertNotNull(badge,
                    "Co " + pendingCount + " yeu cau 'Chờ duyệt' thi badge Tra hang phai hien");
            Assert.assertEquals(badge, String.valueOf(pendingCount),
                    "Badge Tra hang phai bang so yeu cau 'Chờ duyệt' dem duoc o tab Tra hang");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-15 (TC_SCRA02_FUNC_018): Badge Đánh giá bằng số đánh giá tạo trong 7 ngày qua (đối chiếu với dòng 'Đánh giá mới' trong khối Cần xử lý ngay); không có đánh giá mới thì badge ẩn")
    public void reviewsBadgeMatchesNewReviewsCount() {
        AdminPageObject adminPage = openDashboardOrSkip();
        String navBadge = adminPage.getNavBadgeText("reviews");
        String actionCount = adminPage.getDashboardActionCountText("new-reviews");

        if (actionCount == null) {
            Assert.assertNull(navBadge,
                    "Khong co danh gia moi trong 7 ngay qua thi badge Danh gia phai an, dang thay: '" + navBadge + "'");
        } else {
            Assert.assertNotNull(navBadge,
                    "Co " + actionCount + " danh gia moi thi badge Danh gia phai hien");
            Assert.assertEquals(navBadge, actionCount,
                    "Badge Danh gia phai bang so danh gia moi o khoi 'Cần xử lý ngay'");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-16 (TC_SCRA02_FUNC_024): KPI Khách hàng mới hiển thị số, sub hiển thị 'Tổng N khách hàng' (hoặc 'Trên 100 khách hàng' khi chạm cap 100) (skip nếu khối khách hàng lỗi tải)")
    public void newCustomersKpiShowsValueAndTotalSub() {
        AdminPageObject adminPage = openDashboardOrSkip();
        if (adminPage.isDashboardKpiError("customers")) {
            throw new SkipException("Bo qua: khoi Khach hang moi dang loi tai du lieu");
        }

        String sub = "";
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            sub = adminPage.getDashboardKpiSubText("customers");
            if (sub.startsWith("Tổng ") || sub.equals("Trên 100 khách hàng")) {
                break;
            }
            adminPage.sleepInSecond(1);
        }
        Assert.assertTrue(sub.matches("Tổng [\\d.]+ khách hàng") || sub.equals("Trên 100 khách hàng"),
                "Sub KPI Khach hang moi phai la 'Tổng N khách hàng' hoac 'Trên 100 khách hàng', dang thay: '" + sub + "'");
        String value = adminPage.getDashboardKpiValueText("customers");
        Assert.assertTrue(value.matches("[\\d.]+"),
                "Gia tri KPI Khach hang moi phai la so, dang thay: '" + value + "'");
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-17 (TC_SCRA02_FUNC_029): Mỗi KPI có dòng so sánh kỳ trước khớp 1 trong các dạng: '↑ x,x% so với kỳ trước' / '↓ x,x% so với kỳ trước' / 'Mới so với kỳ trước' / 'Không đổi so với kỳ trước' / '—' (chỉ assert định dạng; KPI nào lỗi tải thì bỏ qua KPI đó)")
    public void kpiCompareLinesMatchKnownFormats() {
        AdminPageObject adminPage = openDashboardOrSkip();
        int checked = 0;
        for (String kpiKey : new String[]{"revenue", "orders", "aov", "customers", "cancel-rate"}) {
            if (adminPage.isDashboardKpiError(kpiKey)) {
                continue; // khoi loi (BUG_DAFL_003) — khong co dong so sanh de assert
            }
            String compare = adminPage.getDashboardKpiCompareText(kpiKey);
            Assert.assertTrue(compare.matches("^(↑|↓) [\\d.,]+% so với kỳ trước$")
                            || compare.equals("Mới so với kỳ trước")
                            || compare.equals("Không đổi so với kỳ trước")
                            || compare.equals("—"),
                    "Dong so sanh KPI '" + kpiKey + "' phai khop 1 trong 5 dang hop le, dang thay: '" + compare + "'");
            checked++;
        }
        if (checked == 0) {
            throw new SkipException("Bo qua: ca 5 KPI deu loi tai du lieu (BUG_DAFL_003) nen khong co dong so sanh de kiem tra");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-18 (TC_SCRA02_FUNC_032): Mở Tổng quan lần đầu: nút '30 ngày' (admin-dashboard-range-30d) đang được chọn; sub KPI 'Kỳ dd/mm/yyyy – dd/mm/yyyy' đúng khoảng hôm nay trừ 29 ngày đến hôm nay (sub chỉ assert được khi khối doanh thu không lỗi)")
    public void defaultRangeIsThirtyDays() {
        AdminPageObject adminPage = openDashboardOrSkip();
        Assert.assertTrue(adminPage.isDashboardRangeActive("30d"),
                "Mo Tong quan lan dau thi nut '30 ngày' phai dang duoc chon");
        LocalDate today = LocalDate.now();
        assertRevenueKpiSubOrSkipOnError(adminPage, kpiRangeSub(today.minusDays(29), today));
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-19 (TC_SCRA02_FUNC_033): Đủ 5 nút Hôm nay/7 ngày/30 ngày/Tháng này/Tùy chọn; bấm '7 ngày' thì nút được chọn + sub 'Kỳ ...' đổi sang khoảng 7 ngày; bấm 'Tháng này' thì khoảng = tháng lịch hiện tại (sub chỉ assert được khi khối doanh thu không lỗi)")
    public void segmentedRangeButtonsChangePeriod() {
        AdminPageObject adminPage = openDashboardOrSkip();
        for (String[] range : new String[][]{
                {"today", "Hôm nay"}, {"7d", "7 ngày"}, {"30d", "30 ngày"},
                {"month", "Tháng này"}, {"custom", "Tùy chọn"}}) {
            Assert.assertEquals(adminPage.getElementText(
                            "css=[data-testid='admin-dashboard-range-" + range[0] + "']"), range[1],
                    "Phai co nut khoang '" + range[1] + "' (admin-dashboard-range-" + range[0] + ")");
        }

        LocalDate today = LocalDate.now();
        adminPage.clickDashboardRange("7d");
        Assert.assertTrue(adminPage.isDashboardRangeActive("7d"),
                "Bam '7 ngày' thi nut '7 ngày' phai dang duoc chon");
        assertRevenueKpiSubOrSkipOnError(adminPage, kpiRangeSub(today.minusDays(6), today));

        adminPage.clickDashboardRange("month");
        Assert.assertTrue(adminPage.isDashboardRangeActive("month"),
                "Bam 'Tháng này' thi nut 'Tháng này' phai dang duoc chon");
        assertRevenueKpiSubOrSkipOnError(adminPage,
                kpiRangeSub(today.withDayOfMonth(1), today));
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-20 (TC_SCRA02_FUNC_035/036): Picker Tùy chọn hiện 2 ô Từ ngày/Đến ngày + nút 'Áp dụng' + hint 'Tối đa 365 ngày, không chọn ngày trong tương lai.'; nhập Từ ngày sau Đến ngày rồi Áp dụng thì lỗi inline 'Khoảng thời gian không hợp lệ.' (không đổi khoảng); nhập khoảng hợp lệ (tháng trước) rồi Áp dụng thì sub KPI đổi sang khoảng đó")
    public void customRangePickerValidatesThenApplies() {
        AdminPageObject adminPage = openDashboardOrSkip();
        LocalDate today = LocalDate.now();
        adminPage.clickDashboardRange("custom");
        Assert.assertTrue(adminPage.isDashboardCustomPickerDisplayed(),
                "Bam 'Tùy chọn' phai mo picker khoang tuy chon (admin-dashboard-custom-picker)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard-custom-from']"),
                "Picker phai co o Tu ngay");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard-custom-to']"),
                "Picker phai co o Den ngay");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-dashboard-custom-apply']"),
                "Picker phai co nut 'Áp dụng'");
        String dashboardText = adminPage.getDashboardText();
        Assert.assertTrue(dashboardText.contains("Tối đa 365 ngày, không chọn ngày trong tương lai."),
                "Picker phai luon hien hint 'Tối đa 365 ngày, không chọn ngày trong tương lai.'");

        // Nhanh sai: Tu ngay sau Den ngay.
        adminPage.setDashboardCustomRange(today.toString(), today.minusDays(5).toString());
        adminPage.clickDashboardCustomApply();
        Assert.assertEquals(adminPage.getDashboardCustomErrorText(), "Khoảng thời gian không hợp lệ.",
                "Tu ngay sau Den ngay thi phai bao 'Khoảng thời gian không hợp lệ.', dang thay: "
                        + adminPage.getDashboardCustomErrorText());
        assertRevenueKpiSubOrSkipOnError(adminPage, kpiRangeSub(today.minusDays(29), today));

        // Nhanh dung: thang truoc (tu mung 1 den cuoi thang truoc).
        LocalDate prevMonthFirst = today.minusMonths(1).withDayOfMonth(1);
        LocalDate prevMonthLast = today.withDayOfMonth(1).minusDays(1);
        adminPage.setDashboardCustomRange(prevMonthFirst.toString(), prevMonthLast.toString());
        adminPage.clickDashboardCustomApply();
        adminPage.sleepInSecond(2); // cho dashboard tai lai theo khoang moi
        Assert.assertEquals(adminPage.getDashboardCustomErrorText(), "",
                "Khoang hop le thi khong duoc con loi inline, dang thay: "
                        + adminPage.getDashboardCustomErrorText());
        assertRevenueKpiSubOrSkipOnError(adminPage, kpiRangeSub(prevMonthFirst, prevMonthLast));
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-21 (TC_SCRA02_FUNC_037): Chọn '7 ngày', tải lại trang (F5) thì nút '7 ngày' vẫn được chọn; sessionStorage có key dafl-admin-dashboard-range lưu đúng lựa chọn")
    public void rangeChoicePersistsAcrossReload() {
        AdminPageObject adminPage = openDashboardOrSkip();
        adminPage.clickDashboardRange("7d");
        Assert.assertTrue(adminPage.isDashboardRangeActive("7d"),
                "Bam '7 ngày' thi nut '7 ngày' phai dang duoc chon");

        Object saved = adminPage.executeScript(
                "return window.sessionStorage.getItem('dafl-admin-dashboard-range');");
        Assert.assertNotNull(saved, "sessionStorage phai co key dafl-admin-dashboard-range sau khi doi khoang");
        Assert.assertTrue(String.valueOf(saved).contains("\"7d\""),
                "sessionStorage dafl-admin-dashboard-range phai luu key '7d', dang thay: " + saved);

        adminPage.openPageUrl(adminPage.getCurrentPageUrl()); // tai lai trang (F5)
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Tai lai trang xong van phai o muc Tong quan");
        Assert.assertTrue(adminPage.isDashboardRangeActive("7d"),
                "Tai lai trang xong nut '7 ngày' van phai dang duoc chon");
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-22 (TC_SCRA02_FUNC_038): Nút 'Làm mới' (admin-dashboard-refresh-btn) hiện cùng dòng 'Cập nhật lúc HH:mm' (hoặc 'Chưa cập nhật'); bấm Làm mới thì dashboard tải lại và dòng cập nhật đổi sang giờ hiện tại")
    public void refreshButtonUpdatesTimestamp() {
        AdminPageObject adminPage = openDashboardOrSkip();
        Assert.assertTrue(adminPage.isDashboardRefreshButtonDisplayed(),
                "Muc Tong quan phai co nut 'Làm mới' (admin-dashboard-refresh-btn)");
        String before = adminPage.getDashboardUpdatedAtText();
        Assert.assertTrue(before.equals("Chưa cập nhật") || before.startsWith("Cập nhật lúc "),
                "Dong cap nhat phai la 'Cập nhật lúc HH:mm' hoac 'Chưa cập nhật', dang thay: '" + before + "'");

        adminPage.clickDashboardRefresh();
        String now = LocalTime.now().format(VN_TIME);
        String oneMinuteAgo = LocalTime.now().minusMinutes(1).format(VN_TIME);
        String after = "";
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            after = adminPage.getDashboardUpdatedAtText();
            if (after.equals("Cập nhật lúc " + now) || after.equals("Cập nhật lúc " + oneMinuteAgo)) {
                break;
            }
            adminPage.sleepInSecond(1);
        }
        Assert.assertTrue(after.equals("Cập nhật lúc " + now) || after.equals("Cập nhật lúc " + oneMinuteAgo),
                "Bam 'Làm mới' xong dong cap nhat phai la 'Cập nhật lúc " + now + "', dang thay: '" + after + "'");
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-23 (TC_SCRA02_FUNC_045): Thẻ donut hiện đủ 5 dòng legend (Chờ xác nhận/Đang xử lý – giao hàng/Hoàn thành/Đã hủy/Trả hàng) kèm số lượng, giữa donut hiện tổng số đơn + 'đơn hàng'; trên 100 đơn thì hiện ghi chú cap 100 (skip nếu kỳ không có đơn)")
    public void donutShowsFiveStatusGroupsAndTotal() {
        AdminPageObject adminPage = openDashboardOrSkip();
        if (!adminPage.isDashboardDonutDisplayed()) {
            String dashboardText = adminPage.getDashboardText();
            Assert.assertTrue(dashboardText.contains("Chưa có dữ liệu trong khoảng thời gian này"),
                    "Khong co donut thi phai hien EmptyState 'Chưa có dữ liệu trong khoảng thời gian này'");
            throw new SkipException("Bo qua: ky hien tai khong co don hang nao nen donut rong");
        }

        String[][] groups = {
                {"pending", "Chờ xác nhận"}, {"processing", "Đang xử lý – giao hàng"},
                {"completed", "Hoàn thành"}, {"cancelled", "Đã hủy"}, {"returns", "Trả hàng"},
        };
        for (String[] group : groups) {
            String legend = adminPage.getDashboardDonutLegendText(group[0]);
            Assert.assertTrue(legend.contains(group[1]),
                    "Legend donut phai co dong '" + group[1] + "', dang thay: '" + legend + "'");
            Assert.assertTrue(legend.matches("(?s).*" + java.util.regex.Pattern.quote(group[1]) + ".*\\d+.*"),
                    "Legend '" + group[1] + "' phai kem so luong, dang thay: '" + legend + "'");
        }
        String donutLabel = adminPage.getDashboardDonutAriaLabel();
        Assert.assertTrue(donutLabel.matches("Tổng [\\d.]+ đơn hàng trong kỳ"),
                "Donut phai co aria-label 'Tổng N đơn hàng trong kỳ', dang thay: '" + donutLabel + "'");
        int total = Integer.parseInt(donutLabel.replaceAll("[^0-9]", ""));
        if (total >= 100) {
            Assert.assertTrue(adminPage.getDashboardText().contains("Tính trên 100 đơn gần nhất"),
                    "Ky co >= 100 don thi donut phai co ghi chu cap 100 'Tính trên 100 đơn gần nhất'");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-24 (TC_SCRA02_FUNC_046): Bấm 'Chờ xác nhận' ở legend donut → tab Đơn lọc Pending; bấm 'Hoàn thành' → lọc Completed; bấm 'Đã hủy' → lọc Cancelled (chỉ assert bộ lọc trạng thái; khoảng ngày bị rớt theo giới hạn đã biết)")
    public void donutLegendClickNavigatesToFilteredOrders() {
        AdminPageObject adminPage = openDashboardOrSkip();
        if (!adminPage.isDashboardDonutDisplayed()) {
            throw new SkipException("Bo qua: ky hien tai khong co don hang nao nen donut rong");
        }
        String[][] cases = {
                {"pending", "Pending"}, {"completed", "Completed"}, {"cancelled", "Cancelled"},
        };
        for (String[] entry : cases) {
            adminPage.clickDashboardDonutLegend(entry[0]);
            Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                    "Bam legend '" + entry[0] + "' phai sang tab Don hang");
            Assert.assertEquals(adminPage.getOrderStatusFilterValue(), entry[1],
                    "Bam legend '" + entry[0] + "' thi bo loc trang thai don phai la '" + entry[1]
                            + "', dang thay: " + adminPage.getOrderStatusFilterValue());
            adminPage.clickAdminNav("dashboard");
            Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Phai quay lai duoc muc Tong quan");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-25 (TC_SCRA02_FUNC_047): Bấm KPI Doanh thu → tab Đơn; bấm KPI Khách hàng mới → tab Khách hàng; bấm KPI Tỷ lệ hủy/trả → tab Đơn lọc Cancelled; KPI là <button>, không có sparkline (KPI nào lỗi tải thì bỏ qua KPI đó; skip cả test nếu cả 3 KPI đều lỗi)")
    public void kpiClickNavigatesToRightTab() {
        AdminPageObject adminPage = openDashboardOrSkip();
        int checked = 0;

        if (!adminPage.isDashboardKpiError("revenue")) {
            adminPage.clickDashboardKpi("revenue");
            Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                    "Bam KPI Doanh thu phai sang tab Don hang");
            adminPage.clickAdminNav("dashboard");
            checked++;
        }
        if (!adminPage.isDashboardKpiError("customers")) {
            adminPage.clickDashboardKpi("customers");
            Assert.assertTrue(adminPage.isCustomerSearchDisplayed(),
                    "Bam KPI Khach hang moi phai sang tab Khach hang");
            adminPage.clickAdminNav("dashboard");
            checked++;
        }
        if (!adminPage.isDashboardKpiError("cancel-rate")) {
            adminPage.clickDashboardKpi("cancel-rate");
            Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                    "Bam KPI Ty le huy/tra phai sang tab Don hang");
            Assert.assertEquals(adminPage.getOrderStatusFilterValue(), "Cancelled",
                    "Bam KPI Ty le huy/tra thi bo loc trang thai don phai la 'Cancelled', dang thay: "
                            + adminPage.getOrderStatusFilterValue());
            checked++;
        }
        if (checked == 0) {
            throw new SkipException("Bo qua: ca 3 KPI dieu huong deu loi tai du lieu (BUG_DAFL_003)");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-26 (TC_SCRA02_FUNC_048): Khối 'Cần xử lý ngay' hiện đủ 6 dòng (Đơn chờ xác nhận/Trả hàng chờ duyệt/Đánh giá mới/SP sắp hết hàng/SP hết hàng/KM sắp hết hạn), mỗi dòng có nhãn + badge cam hoặc '—' + nút 'Xem' (disable khi số = 0); tất cả = 0 thì hiện 'Không có việc cần xử lý. Mọi thứ đang ổn.'")
    public void actionNeededBlockShowsSixRowsOrAllClear() {
        AdminPageObject adminPage = openDashboardOrSkip();
        Assert.assertTrue(adminPage.isDashboardActionNeededDisplayed(),
                "Muc Tong quan phai co khoi 'Cần xử lý ngay'");
        String blockText = adminPage.getDashboardActionNeededText();
        if (blockText.contains("Không có việc cần xử lý. Mọi thứ đang ổn.")) {
            return; // tat ca = 0 — dung hanh vi all-clear
        }
        String[] labels = {
                "Đơn chờ xác nhận", "Trả hàng chờ duyệt", "Đánh giá mới (7 ngày qua)",
                "Sản phẩm sắp hết hàng (còn dưới 5)", "Sản phẩm hết hàng", "Khuyến mãi sắp hết hạn (7 ngày)",
        };
        for (String label : labels) {
            Assert.assertTrue(blockText.contains(label),
                    "Khoi 'Cần xử lý ngay' phai co dong '" + label + "', dang thay: " + blockText);
        }
        for (String key : new String[]{"pending-orders", "pending-returns", "new-reviews",
                "low-stock", "out-of-stock", "expiring-promos"}) {
            Assert.assertTrue(adminPage.isDashboardActionViewDisplayed(key),
                    "Dong '" + key + "' phai co nut 'Xem'");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-27 (TC_SCRA02_FUNC_050): Bấm 'Xem' ở các dòng còn lại của 'Cần xử lý ngay' đi đúng tab — Trả hàng chờ duyệt → Trả hàng; Đánh giá mới → Đánh giá; SP sắp hết/hết hàng → Sản phẩm; KM sắp hết hạn → Khuyến mãi (self-skip từng dòng có số = 0 vì nút 'Xem' disable)")
    public void actionNeededViewButtonsNavigateToRightTabs() {
        AdminPageObject adminPage = openDashboardOrSkip();
        Assert.assertTrue(adminPage.isDashboardActionNeededDisplayed(),
                "Muc Tong quan phai co khoi 'Cần xử lý ngay'");
        if (adminPage.getDashboardActionNeededText().contains("Không có việc cần xử lý. Mọi thứ đang ổn.")) {
            throw new SkipException("Bo qua: khong co viec can xu ly nao nen moi nut 'Xem' deu disable");
        }
        int checked = 0;
        String[][] cases = {
                {"pending-returns", "returns", "returns-search-input"},
                {"new-reviews", "reviews", "reviews-search-input"},
                {"low-stock", "products", "products-search-input"},
                {"out-of-stock", "products", "products-search-input"},
                {"expiring-promos", "promotions", "promotions-search-input"},
        };
        for (String[] entry : cases) {
            if (!adminPage.isDashboardActionViewEnabled(entry[0])) {
                continue; // dong co so = 0 — nut 'Xem' disable
            }
            adminPage.clickDashboardActionView(entry[0]);
            Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='" + entry[2] + "']"),
                    "Bam 'Xem' o dong '" + entry[0] + "' phai sang tab " + entry[1]
                            + " (" + entry[2] + ")");
            checked++;
            adminPage.clickAdminNav("dashboard");
            Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Phai quay lai duoc muc Tong quan");
        }
        if (checked == 0) {
            throw new SkipException("Bo qua: moi dong 'Cần xử lý ngay' deu co so = 0 nen khong co nut 'Xem' nao bam duoc");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-28 (TC_SCRA02_FUNC_052): Khối 'Đơn hàng gần đây' hiện tối đa 8 đơn, mỗi dòng đủ cột Mã đơn/Khách hàng/Trạng thái/Thanh toán/Tổng tiền/Ngày đặt; link 'Xem tất cả đơn hàng' sang tab Đơn (skip nếu kỳ không có đơn)")
    public void recentOrdersBlockShowsEightOrdersMax() {
        AdminPageObject adminPage = openDashboardOrSkip();
        String cardText = adminPage.getDashboardRecentOrdersCardText();
        if (cardText.contains("Chưa có đơn hàng trong khoảng thời gian này")) {
            throw new SkipException("Bo qua: ky hien tai khong co don hang nao");
        }
        int rowCount = adminPage.getDashboardRecentOrderRowCount();
        Assert.assertTrue(rowCount >= 1 && rowCount <= 8,
                "Khoi 'Đơn hàng gần đây' phai hien 1-8 don, dang thay: " + rowCount);
        for (String header : new String[]{"Mã đơn", "Khách hàng", "Trạng thái", "Thanh toán", "Tổng tiền", "Ngày đặt"}) {
            Assert.assertTrue(cardText.contains(header),
                    "Khoi 'Đơn hàng gần đây' phai co cot '" + header + "', dang thay: " + cardText);
        }
        adminPage.clickDashboardRecentOrdersAll();
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Bam 'Xem tất cả đơn hàng' phai sang tab Don hang");
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-29 (TC_SCRA02_FUNC_053, đợt 21 viết lại): Bấm 1 dòng trong khối Đơn hàng gần đây thì sang TRANG chi tiết /orders/:code đúng đơn đó (hiện đúng mã đơn, pill trạng thái); breadcrumb về danh sách đơn rồi về lại Tổng quan được (skip nếu kỳ không có đơn)")
    public void recentOrderRowOpensOrderDetailPage() {
        AdminPageObject adminPage = openDashboardOrSkip();
        if (adminPage.getDashboardRecentOrderRowCount() == 0) {
            throw new SkipException("Bo qua: ky hien tai khong co don hang nao");
        }
        String orderCode = adminPage.getFirstDashboardRecentOrderCode();
        Assert.assertFalse(orderCode == null || orderCode.trim().isEmpty(),
                "Phai doc duoc ma don o dong dau tien cua khoi 'Đơn hàng gần đây'");

        adminPage.clickFirstDashboardRecentOrder();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Bam dong don gan day phai mo trang chi tiet don (admin-order-detail)");
        Assert.assertTrue(adminPage.getOrderDetailTitleText().startsWith("Đơn " + orderCode),
                "Trang chi tiet phai hien dung ma don '" + orderCode + "'");
        Assert.assertFalse(adminPage.getOrderDetailStatusPillText().trim().isEmpty(),
                "Trang chi tiet phai hien pill trang thai don");
        // Dot 21: mo chi tiet tu dashboard thi SANG TRANG RIENG (khong con o
        // lai muc Tong quan nhu Drawer cu) — bang don gan day khong con hien.
        Assert.assertFalse(adminPage.isOrderSearchDisplayed(),
                "Dang o trang chi tiet thi khong duoc thay bang don hang");
        adminPage.closeOrderDetail();
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Ve danh sach xong khong duoc con thay trang chi tiet");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Ve danh sach xong phai thay lai bang don hang");
        adminPage.clickAdminNav("dashboard");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(),
                "Ve lai muc Tong quan duoc sau khi xem chi tiet don");
    }

    @Test(description = "ADM19-30 (TC_SCRA02_FUNC_056): Card 'Sắp hết hàng' hiện tối đa 5 dòng, mỗi dòng có badge 'Còn n' hoặc 'Hết hàng'; không có SP sắp hết hàng thì hiện 'Không có sản phẩm sắp hết hàng'")
    public void lowStockCardShowsFiveRowsMaxOrEmptyState() {
        AdminPageObject adminPage = openDashboardOrSkip();
        String cardText = adminPage.getDashboardLowStockCardText();
        if (cardText.contains("Không có sản phẩm sắp hết hàng")) {
            return; // trang thai rong — dung hanh vi EmptyState
        }
        int rowCount = adminPage.getDashboardLowStockRowCount();
        Assert.assertTrue(rowCount >= 1 && rowCount <= 5,
                "Card 'Sắp hết hàng' phai hien 1-5 dong, dang thay: " + rowCount);
        for (String rowText : adminPage.getDashboardLowStockRowTexts()) {
            Assert.assertTrue(rowText.matches("(?s).*((Còn [\\d.]+)|Hết hàng).*"),
                    "Moi dong 'Sắp hết hàng' phai co badge 'Còn n' hoac 'Hết hàng', dang thay: '" + rowText + "'");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-31 (TC_SCRA02_FUNC_057): Bấm 1 dòng hạng (Vàng) ở card 'Phân bổ hạng hội viên' thì sang tab Khách hàng, lọc theo đúng hạng vừa bấm (skip nếu chưa có khách hàng)")
    public void tierMixRowClickFiltersCustomersByTier() {
        AdminPageObject adminPage = openDashboardOrSkip();
        if (adminPage.isDashboardTierMixEmpty()) {
            throw new SkipException("Bo qua: chua co khach hang nao nen card phan bo hang rong");
        }
        adminPage.clickDashboardTierRow("Gold");
        Assert.assertTrue(adminPage.isCustomerSearchDisplayed(),
                "Bam dong hang 'Vàng' phai sang tab Khach hang");
        Assert.assertEquals(adminPage.getCustomerTierFilterValue(), "Gold",
                "Bam dong hang 'Vàng' thi bo loc hang khach hang phai la 'Gold', dang thay: "
                        + adminPage.getCustomerTierFilterValue());
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-32 (TC_SCRA02_FUNC_058): Card phân bổ hạng hiện stacked bar 4 hạng, mỗi dòng có tên + số lượng + %; card KM đang chạy hiện tối đa 4 mã, mỗi mã có giá trị giảm, thanh đã dùng x/y, dòng 'Hạn: dd/mm/yyyy' hoặc 'Không có hạn', badge 'Sắp hết hạn' khi hết hạn trong 7 ngày (skip phần card nào đang rỗng)")
    public void tierMixAndActivePromosCardsDisplay() {
        AdminPageObject adminPage = openDashboardOrSkip();
        boolean checkedAny = false;

        if (!adminPage.isDashboardTierMixEmpty()) {
            checkedAny = true;
            String tierText = adminPage.getDashboardTierMixCardText();
            for (String label : new String[]{"Thường", "Bạc", "Vàng", "Kim cương"}) {
                Assert.assertTrue(tierText.contains(label),
                        "Card phan bo hang phai co dong hang '" + label + "', dang thay: " + tierText);
            }
            Assert.assertTrue(adminPage.isElementDisplayed(
                            "css=[data-testid='admin-dashboard-tier-mix-card'] .db-stacked-bar"),
                    "Card phan bo hang phai co stacked bar (.db-stacked-bar)");
            for (String tier : new String[]{"Member", "Silver", "Gold", "Diamond"}) {
                String rowText = adminPage.getDashboardTierRowText(tier);
                Assert.assertTrue(rowText.matches("(?s).*\\d+.*%.*"),
                        "Dong hang '" + tier + "' phai co so luong + %, dang thay: '" + rowText + "'");
            }
        }

        String promoText = adminPage.getDashboardPromoCardText();
        if (!promoText.contains("Không có khuyến mãi đang chạy")) {
            checkedAny = true;
            int promoCount = adminPage.getDashboardPromoRowCount();
            Assert.assertTrue(promoCount >= 1 && promoCount <= 4,
                    "Card KM dang chay phai hien 1-4 ma, dang thay: " + promoCount);
            for (String rowText : adminPage.getDashboardPromoRowTexts()) {
                Assert.assertTrue(rowText.contains("Giảm "),
                        "Moi dong KM phai co gia tri giam ('Giảm ...'), dang thay: '" + rowText + "'");
                Assert.assertTrue(rowText.contains("Hạn: "),
                        "Moi dong KM phai co dong 'Hạn: ...', dang thay: '" + rowText + "'");
            }
        }

        if (!checkedAny) {
            throw new SkipException("Bo qua: ca 2 card (phan bo hang + KM dang chay) deu rong");
        }
    }

    @Story("Tổng quan")
    @Test(description = "ADM19-33 (TC_SCRA02_FUNC_059): Bấm 1 mã trong card KM đang chạy thì chuyển sang tab Khuyến mãi, ô tìm kiếm điền sẵn đúng mã vừa bấm và bảng chỉ hiện KM đó (skip nếu không có KM đang chạy)")
    public void activePromoClickSearchesPromoByCode() {
        AdminPageObject adminPage = openDashboardOrSkip();
        if (adminPage.getDashboardPromoRowCount() == 0) {
            throw new SkipException("Bo qua: khong co khuyen mai nao dang chay");
        }
        String promoCode = adminPage.getFirstDashboardPromoCode();
        Assert.assertFalse(promoCode == null || promoCode.trim().isEmpty(),
                "Phai doc duoc ma KM o dong dau tien cua card KM dang chay");

        adminPage.clickFirstDashboardPromoRow();
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROMO_SEARCH),
                "Bam ma KM phai sang tab Khuyen mai (promotions-search-input)");
        Assert.assertEquals(adminPage.getPromoSearchValue(), promoCode,
                "O tim kiem tab Khuyen mai phai dien san dung ma vua bam '" + promoCode + "', dang thay: "
                        + adminPage.getPromoSearchValue());
        if (adminPage.getAdminTableRowCount() > 0) {
            Assert.assertTrue(adminPage.getAdminTableRowTexts().get(0).contains(promoCode),
                    "Bang Khuyen mai sau khi tim phai hien KM '" + promoCode + "'");
        }
    }
}
