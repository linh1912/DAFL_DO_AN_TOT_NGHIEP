package vn.dafl.tests.admin.customers;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Test khach hang + hoi vien tren app Admin rieng localhost:3001 (tach tu
 * AdminCrudTest / AdminAndNewsletterTest / AdminDetailTest cu khi chia thu muc
 * admin dot 17 — moi lop chi lo 1 phan he).
 * Chi doc so du diem / dieu chinh diem cho tai khoan demo (ACR-08, hoan tac
 * -5 sau test, tong ket bang 0); KHONG khoa/mokhoa tai khoan nguoi khac,
 * KHONG doi mat khau admin.
 * Dot 18: bang Khach hang them cot STT + cot Ma KH (KH-xxxx) + cot Trang thai
 * dang Switch (bat = dang hoat dong, tat = da khoa); form xem/sua KH la DRAWER
 * RONG 680px (admin-customer-form-page, class fs-drawer, --fs-width) co o Ma +
 * Email luon disable; form XEM khong con so du diem chi tiet / don gan day
 * nhu modal cu (diem van co o cot Diem trong bang); dieu chinh diem mo
 * ConfirmDialog rieng F2 'Xác nhận cộng điểm?' / F3 'Xác nhận trừ điểm?'
 * (wave D) roi moi toast 'Đã điều chỉnh điểm'. Luu form sua KH goi
 * PUT /api/admin/customers/{id} (endpoint da co tu dot 17) nhung test sua KH
 * van chi mo form doc trang thai + dong, KHONG bam Luu de khong doi du lieu demo.
 * Dot 21: xem/sua KH la TRANG FULL PAGE (/customers/:code,
 * /customers/:code/edit — KHONG con Drawer); tim/loc TU DONG (debounce 300ms,
 * KHONG con nut Tim kiem); khoa/mo khoa GIU NGUYEN popup B1/B2 + toast cu.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý khách hàng và hội viên")
public class AdminCustomerTest extends BaseTest {

    private boolean pointsAdjusted = false;
    private boolean adminLoggedIn = false;

    /** Dang nhap Super Admin tren app Admin rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        adminLoggedIn = true;
        return adminPage;
    }

    /** Mo /admin va dung o muc cho truoc; tra ve AdminPageObject da san sang. */
    private AdminPageObject openAdminTab(String navKey) {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav(navKey);
        return adminPage;
    }

    /** Hoan tac +5 diem da cong cho demo (tru lai 5 diem, tong ket bang 0). */
    private void reversePointsAdjustmentIfNeeded() {
        if (!pointsAdjusted || !adminLoggedIn) {
            return;
        }
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "-5", "Hoàn tác điểm test ACR-08");
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        } finally {
            pointsAdjusted = false;
        }
    }

    // ---------- Khach hang: tim kiem (ADM-08, dot 8, chi doc) ----------

    @Story("Khách hàng")
    @Test(description = "ADM-08: Tìm kiếm ở mục Khách hàng lọc đúng (email demo có → thấy dòng, từ khóa lạ → báo rỗng)")
    public void adminCustomerSearchFiltersRows() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("customers");

        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Tim email demo phai ra it nhat 1 dong khach hang");
        boolean found = adminPage.getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains(GlobalConstants.DEMO_EMAIL));
        Assert.assertTrue(found, "Cac dong sau khi loc phai chua email demo");

        adminPage.searchCustomers("zzzzzz-khong-co-khach@dafl.vn");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim email la phai ra 0 dong khach hang");
        Assert.assertTrue(adminPage.isEmptyCustomersMessageDisplayed(),
                "Tim email la phai hien dong 'Chưa có khách hàng nào'");
    }

    // ---------- Hoi vien: dieu chinh diem (ACR-08, dot 13) ----------

    @Story("Hội viên")
    @Test(description = "ACR-08 (đợt 18, ConfirmDialog + đọc điểm ở cột Điểm trong bảng): Đọc số dư điểm demo TRƯỚC/SAU qua cột Điểm trong bảng Khách hàng → cộng 5 điểm phải tăng đúng +5 (hoàn tác -5 sau test)")
    public void adminAdjustPointsForDemoCustomer() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        int pointsBefore = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
        if (pointsBefore < 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de doc so du diem");
        }
        try {
            adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "5", "Cộng điểm test ACR-08");
            pointsAdjusted = true;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Dieu chinh diem xong phai hien toast thanh cong (admin-toast)");
            Assert.assertTrue(adminPage.getToastTitleText().contains("Đã điều chỉnh điểm"),
                    "Toast dieu chinh diem phai co tieu de 'Đã điều chỉnh điểm', dang thay: " + adminPage.getToastTitleText());
            Assert.assertTrue(adminPage.isAdjustPointsFormCleared(),
                    "Dieu chinh diem xong o so diem + ly do phai duoc xoa trang");

            int pointsAfter = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
            Assert.assertEquals(pointsAfter, pointsBefore + 5,
                    "So du diem cua demo phai tang dung 5 sau khi cong (truoc: " + pointsBefore
                            + ", sau: " + pointsAfter + ")");
        } finally {
            reversePointsAdjustmentIfNeeded();
        }
    }

    // ---------- Chi tiet hang hoi vien / khach hang (ADT-04/06, dot 8, chi doc) ----------

    @Story("Hội viên")
    @Test(description = "ADT-04: Mở modal chi tiết hạng hội viên bằng nút mắt → hiện đúng tên hạng, đóng được")
    public void tierDetailModalShowsCorrectTier() {
        AdminPageObject adminPage = openAdminTab("membership");
        String tierLabel = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_TIER_DETAIL_BTN, "Xem chi tiết hạng ");
        if (tierLabel == null || tierLabel.isEmpty()) {
            throw new SkipException("Bo qua: muc Hoi vien khong co hang nao de mo chi tiet");
        }

        adminPage.clickFirstTierDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_TIER_DETAIL),
                "Phai mo duoc modal chi tiet hang hoi vien");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_TIER_DETAIL);
        Assert.assertTrue(modalText.contains(tierLabel),
                "Modal chi tiet phai hien dung ten hang '" + tierLabel + "', dang thay: " + modalText);

        adminPage.closeDetailModal("admin-tier-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_TIER_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet hang hoi vien");
    }

    @Story("Khách hàng")
    @Test(description = "ADT-06 (đợt 18 + đợt 21, viết lại theo view mode): Mở TRANG xem khách hàng bằng nút mắt → mode 'view', tiêu đề 'Chi tiết khách hàng', tên readonly, header CHỈ đúng 2 nút [Đóng]+[Chỉnh sửa]; trang xem KHÔNG có số dư điểm chi tiết / đơn gần đây (điểm chỉ có ở cột Điểm trong bảng); đóng bằng [Đóng]")
    public void customerDetailModalShowsCorrectCustomer() {
        AdminPageObject adminPage = openAdminTab("customers");
        String customerName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CUSTOMER_DETAIL_BTN, "Xem khách hàng ");
        if (customerName == null || customerName.isEmpty()) {
            throw new SkipException("Bo qua: muc Khach hang khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstCustomerDetail();
        Assert.assertTrue(adminPage.isCustomerDetailPageDisplayed(),
                "Phai mo duoc trang xem khach hang full page (admin-customer-detail-page)");
        Assert.assertEquals(adminPage.getCustomerFormMode(), "view",
                "Trang xem khach hang dot 21 phai co data-form-mode='view'");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_CUSTOMER_DETAIL_PAGE);
        Assert.assertTrue(modalText.contains(customerName),
                "Trang xem phai hien dung ten khach '" + customerName + "', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Chi tiết khách hàng"),
                "Trang xem phai co tieu de 'Chi tiết khách hàng', dang thay: " + modalText);
        Assert.assertTrue(adminPage.isCustomerNameInputReadOnly(),
                "View mode: o ho ten khach hang phai readonly (khong go duoc)");
        Assert.assertFalse(modalText.contains("Đơn hàng gần đây"),
                "Trang xem KH dot 21 KHONG con muc don gan day, dang thay: " + modalText);
        java.util.List<String> footerTexts = adminPage.getPageHeadButtonTexts("admin-customer-detail-page");
        Assert.assertEquals(footerTexts.size(), 2,
                "Header trang xem dot 21 phai CHI co dung 2 nut, dang thay: " + footerTexts);
        Assert.assertTrue(footerTexts.contains("Đóng") && footerTexts.contains("Chỉnh sửa"),
                "Header trang xem phai la [Đóng] + [Chỉnh sửa], dang thay: " + footerTexts);

        adminPage.closeCustomerDetailModal();
        Assert.assertFalse(adminPage.isCustomerDetailPageDisplayed(),
                "Bam 'Đóng' o header phai dong duoc trang xem khach hang");
    }

    // ---------- Dot 17: sua KH (chi mo form, khong luu) / cot Ma KH ----------

    @Story("Khách hàng")
    @Test(description = "ADM17-06 (đợt 18 wave D + đợt 21, TRANG full page): Mở form sửa khách demo → ô Mã KH + ô Email đều bị disable, ô Họ tên sửa được; đóng bằng Hủy không lưu (KHÔNG bấm Lưu để không đổi dữ liệu demo — PUT /api/admin/customers/{id} đã có từ đợt 17)")
    public void adminEditCustomerFormDisablesCodeAndEmailInputs() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form sua");
        }

        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        Assert.assertTrue(adminPage.isCustomerEditPageDisplayed(),
                "Bam sua khach hang dot 21 phai mo TRANG sua KH full page (admin-customer-edit-page)");
        Assert.assertTrue(adminPage.isCustomerFormDisplayed(),
                "Bam sua khach hang phai mo form sua KH (admin-customer-form)");
        Assert.assertTrue(adminPage.isCustomerCodeInputDisabled(),
                "Mo form sua KH thi o Ma KH phai bi disable (khong sua duoc ma)");
        Assert.assertTrue(adminPage.isCustomerEmailInputDisabled(),
                "Mo form sua KH thi o Email phai bi disable (chi xem, khong sua)");
        Assert.assertEquals(adminPage.getCustomerNameInputValue(), GlobalConstants.DEMO_FULL_NAME,
                "Form sua KH phai dien san ho ten '" + GlobalConstants.DEMO_FULL_NAME + "'");
        Assert.assertTrue(adminPage.isElementEnabled(AdminPageUI.ADMIN_CUSTOMER_NAME_INPUT),
                "O Ho ten trong form sua KH van phai sua duoc (khong disable)");
        Assert.assertFalse(adminPage.getCustomerCodeInputValue().isEmpty(),
                "O Ma KH trong form sua phai co gia tri (ma KH-... cua khach), khong duoc de trong");

        // Dong form bang nut 'Hủy' (khong sua gi nen form sach, dong luon).
        // KHONG bam Luu de khong doi du lieu demo (PUT da co tu dot 17 nhung
        // test nay chi kiem tra trang thai form).
        adminPage.cancelCustomerForm();
        Assert.assertFalse(adminPage.isCustomerFormDisplayed(),
                "Dong form xong khong duoc con thay form sua khach hang");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Dong form xong phai quay lai bang khach hang co du lieu");
    }

    @Story("Khách hàng")
    @Test(description = "ADM17-17 (đợt 17): Cột Mã KH trong bảng hiển thị đúng định dạng KH-... ở dòng khách demo (skip nếu tài khoản demo chưa có mã KH)")
    public void adminCustomerRowShowsKhCodeFormat() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de doc ma KH");
        }
        String code = adminPage.getCustomerCodeInRow(GlobalConstants.DEMO_EMAIL);
        if (code == null || code.equals("—") || code.isEmpty()) {
            throw new SkipException("Bo qua: tai khoan demo chua co ma KH (cot Ma hien '—', co the seed cu chua sinh CustomerCode)");
        }
        Assert.assertTrue(code.matches("KH-[A-Za-z0-9-]+"),
                "Ma KH cua khach demo phai dung dinh dang 'KH-...', dang thay: '" + code + "'");
    }

    // ---------- Dot ADM19 (W4): bang / toolbar / loc / phan trang (chi doc) ----------

    /** Mo khoa khach demo qua popup B2 (chi dung khi demo dang khoa): gat Switch
     *  -> popup 'Mở khóa tài khoản?' (khong o ly do) -> xac nhan -> toast. */
    private void unlockDemoCustomer(AdminPageObject adminPage) {
        adminPage.clickRowStatusSwitch(GlobalConstants.DEMO_FULL_NAME);
        adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Mở khóa tài khoản?",
                "Mo khoa khach demo phai mo popup 'Mở khóa tài khoản?', dang thay: " + adminPage.getConfirmDialogTitle());
        adminPage.clickConfirmDialogConfirm();
        adminPage.waitForToast();
        Assert.assertEquals(adminPage.getToastTitleText(), "Đã mở khóa tài khoản",
                "Mo khoa xong phai hien toast 'Đã mở khóa tài khoản', dang thay: " + adminPage.getToastTitleText());
    }

    /** Dam bao khach demo dang HOAT DONG truoc khi test khoa: neu dang khoa thi
     *  mo khoa lai (B2). Skip neu dong demo khong co Switch trang thai. */
    private void ensureDemoCustomerUnlocked(AdminPageObject adminPage) {
        Boolean active = adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_FULL_NAME);
        if (active == null) {
            throw new SkipException("Bo qua: dong khach demo khong co Switch trang thai de khoa/mo khoa");
        }
        if (!active) {
            unlockDemoCustomer(adminPage);
        }
    }

    /** Cho toi da 5 giay toi khi form sua KH hien loi form-level cua BE (PUT
     *  bat dong bo); tra "" neu khong co loi. */
    private String waitForCustomerFormError(AdminPageObject adminPage) {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            String error = adminPage.getCustomerFormErrorText();
            if (!error.isEmpty()) {
                return error;
            }
            sleepInSecond(1);
        }
        return "";
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-185 (TC_SCRA07_UI_001): Bảng Khách hàng đủ 10 cột theo đúng thứ tự STT | Mã KH | Khách hàng | Điện thoại | Hạng | Chi tiêu 12 tháng | Điểm | Số đơn | Trạng thái | Thao tác; dòng demo có avatar + tên + email, Switch trạng thái, nút mắt Xem + nút bút Sửa, cột Điểm đọc được số dư")
    public void adminCustomerTableShowsTenColumnsInOrder() {
        AdminPageObject adminPage = openAdminTab("customers");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: muc Khach hang dang rong nen khong co dong de kiem tra cot");
        }
        List<String> expectedHeaders = Arrays.asList(
                "STT", "Mã KH", "Khách hàng", "Điện thoại", "Hạng", "Chi tiêu 12 tháng",
                "Điểm", "Số đơn", "Trạng thái", "Thao tác");
        Assert.assertEquals(adminPage.getCustomerHeaders(), expectedHeaders,
                "Bang Khach hang phai co dung 10 cot tieu de theo thu tu");

        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de kiem tra dong");
        }
        String rowText = adminPage.getAdminTableRowTexts().get(0);
        Assert.assertTrue(rowText.contains(GlobalConstants.DEMO_FULL_NAME),
                "Dong demo phai hien ten '" + GlobalConstants.DEMO_FULL_NAME + "', dang thay: " + rowText);
        Assert.assertTrue(rowText.contains(GlobalConstants.DEMO_EMAIL),
                "Dong demo phai hien email '" + GlobalConstants.DEMO_EMAIL + "', dang thay: " + rowText);
        Assert.assertTrue(adminPage.isCustomerRowAvatarDisplayed(GlobalConstants.DEMO_EMAIL),
                "Dong demo phai co avatar (span.sa-avatar)");
        Assert.assertTrue(adminPage.isRowStatusSwitchDisplayed(GlobalConstants.DEMO_FULL_NAME),
                "Dong demo phai co Switch trang thai o cot Trang thai");
        Assert.assertTrue(adminPage.isCustomerRowEditButtonDisplayed(GlobalConstants.DEMO_FULL_NAME),
                "Dong demo phai co nut but Sua o cot Thao tac");
        String detailLabel = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CUSTOMER_DETAIL_BTN, "Xem khách hàng ");
        Assert.assertEquals(detailLabel, GlobalConstants.DEMO_FULL_NAME,
                "Nut mat o dong demo phai mo form xem cua '" + GlobalConstants.DEMO_FULL_NAME + "'");
        int points = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
        Assert.assertTrue(points >= 0,
                "Cot Diem cua khach demo phai doc duoc so du diem (>= 0), dang thay: " + points);
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-186 (TC_SCRA07_UI_004 + TC_SCRA07_FUNC_033): Phân trang Khách hàng 10 dòng/trang — trang 1 có 10 dòng, STT 1–10; sang trang 2 STT bắt đầu từ 11 và tiếp tục (skip nếu ≤ 10 khách; giới hạn tải tối đa 20 khách là [CẦN XÁC MINH] nên không assert)")
    public void adminCustomerPaginationSttContinuesOnPageTwo() {
        AdminPageObject adminPage = openAdminTab("customers");
        int total = adminPage.getPaginationTotalRows("customers");
        if (total <= 10) {
            throw new SkipException("Bo qua: chi co " + total
                    + " khach (can > 10 khach moi co trang 2 de kiem tra STT tiep tuc)");
        }
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 10,
                "Trang 1 phai co dung 10 dong khach hang");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("customers"), 1,
                "Luc dau phan trang phai o trang 1");

        adminPage.clickPaginationNext("customers");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("customers"), 2,
                "Bam 'Sau' phai sang trang 2");
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai tiep tuc tu 11");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), total - 10,
                "Trang 2 phai co dung " + (total - 10) + " dong con lai (tong " + total + " khach)");
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-187 (TC_SCRA07_UI_005): Toolbar Khách hàng đợt 18 có ô tìm placeholder 'Tìm email, tên, SĐT...', dropdown lọc hạng (Tất cả hạng/Thường/Bạc/Vàng/Kim cương, locate bằng aria-label 'Lọc hạng' vì chưa có testid), dropdown lọc trạng thái, nút icon Tìm (Lucide Search) + nút icon Đặt lại (Lucide RotateCcw); KHÔNG có nút Thêm mới/Export/Làm mới")
    public void adminCustomerToolbarHasSearchTierStatusFilters() {
        AdminPageObject adminPage = openAdminTab("customers");
        Assert.assertEquals(adminPage.getCustomerSearchPlaceholder(), "Tìm email, tên, SĐT...",
                "O tim Khach hang phai co placeholder 'Tìm email, tên, SĐT...'");
        Assert.assertTrue(adminPage.isCustomerTierFilterDisplayed(),
                "Toolbar Khach hang phai co dropdown loc hang (aria-label 'Lọc hạng')");
        Assert.assertEquals(adminPage.getCustomerTierFilterOptions(),
                Arrays.asList("Tất cả hạng", "Thường", "Bạc", "Vàng", "Kim cương"),
                "Dropdown loc hang phai co du 5 lua chon");
        Assert.assertEquals(adminPage.getCustomerStatusFilterOptions(),
                Arrays.asList("Tất cả", "Hoạt động", "Đã khoá"),
                "Dropdown loc trang thai phai co du 3 lua chon");
        Assert.assertTrue(adminPage.isToolbarSearchButtonDisplayed("customers"),
                "Toolbar Khach hang phai co nut icon Tim");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("customers"),
                "Toolbar Khach hang phai co nut icon Dat lai");
        Assert.assertTrue(adminPage.isToolbarSearchIconDisplayed("customers"),
                "Nut Tim phai dung icon Lucide Search (svg.lucide-search)");
        Assert.assertTrue(adminPage.isToolbarResetIconDisplayed("customers"),
                "Nut Dat lai phai dung icon Lucide RotateCcw (svg.lucide-rotate-ccw)");
        Assert.assertFalse(adminPage.isToolbarAddButtonPresent("customers"),
                "Muc Khach hang KHONG co nut Them moi");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Toolbar Khach hang KHONG co nut Export");
        String toolbarText = adminPage.getToolbarText();
        Assert.assertFalse(toolbarText.contains("Làm mới"),
                "Toolbar Khach hang KHONG co nut Lam moi, dang thay: " + toolbarText);
        Assert.assertFalse(toolbarText.contains("🔍") || toolbarText.contains("↺"),
                "Toolbar Khach hang khong duoc con emoji tim/lam moi, dang thay: " + toolbarText);
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-188 (TC_SCRA07_FUNC_007, dot 21): Lọc khách theo hạng Vàng (server-side) — chọn dropdown hạng 'Vàng' thì bảng TỰ ĐỘNG tải lại chỉ hiện khách hạng Vàng (không cần bấm Tìm); Đặt lại bộ lọc sau test (skip nếu DB không có khách hạng Vàng)")
    public void adminCustomerTierFilterShowsOnlyGoldTier() {
        AdminPageObject adminPage = openAdminTab("customers");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: muc Khach hang dang rong nen khong co du lieu de loc hang");
        }
        try {
            adminPage.selectCustomerTierFilterByValue("Gold");
            Assert.assertEquals(adminPage.getCustomerTierFilterValue(), "Gold",
                    "Chon loc hang 'Vàng' thi dropdown phai giu value 'Gold'");
            adminPage.sleepInSecond(2); // cho loc tu dong tai lai (khong con nut Tim)
            if (adminPage.getAdminTableRowCount() == 0) {
                throw new SkipException("Bo qua: DB nay khong co khach hang Vang nao nen loc hang Vang tra ve bang rong");
            }
            java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile(
                    "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
            for (String rowText : adminPage.getAdminTableRowTexts()) {
                Assert.assertTrue(rowText.contains("Vàng"),
                        "Loc hang Vang thi moi dong hien deu phai la hang Vang, dang thay: " + rowText);
                java.util.regex.Matcher matcher = emailPattern.matcher(rowText);
                if (matcher.find()) {
                    Assert.assertEquals(adminPage.getCustomerTierInRow(matcher.group()), "Vàng",
                            "Cot Hang cua dong '" + matcher.group() + "' phai la 'Vàng'");
                }
            }
        } finally {
            adminPage.clickToolbarReset("customers");
        }
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-189 (TC_SCRA07_FUNC_008 + TC_SCRA07_FUNC_009): Dropdown Trạng thái lọc client-side — chọn 'Đã khoá' chỉ hiện khách bị khoá (Switch tắt), 'Hoạt động' chỉ hiện khách chưa khoá (Switch bật), 'Tất cả' hiện lại toàn bộ khách đã tải")
    public void adminCustomerStatusFilterActiveLockedAll() {
        AdminPageObject adminPage = openAdminTab("customers");
        int total = adminPage.getPaginationTotalRows("customers");
        if (total <= 0) {
            throw new SkipException("Bo qua: muc Khach hang dang rong nen khong co du lieu de loc trang thai");
        }
        try {
            adminPage.selectStatusFilterByValue("locked");
            if (adminPage.getAdminTableRowCount() == 0) {
                Assert.assertTrue(adminPage.isEmptyCustomersMessageDisplayed(),
                        "Loc 'Đã khoá' khong co khach nao thi phai hien 'Không tìm thấy khách hàng'");
            } else {
                for (Boolean checked : adminPage.getAllRowStatusSwitchStates()) {
                    Assert.assertFalse(checked,
                            "Loc 'Đã khoá' thi moi dong hien deu phai co Switch TAT (da khoa)");
                }
            }
            adminPage.selectStatusFilterByValue("active");
            if (adminPage.getAdminTableRowCount() == 0) {
                Assert.assertTrue(adminPage.isEmptyCustomersMessageDisplayed(),
                        "Loc 'Hoạt động' khong co khach nao thi phai hien 'Không tìm thấy khách hàng'");
            } else {
                for (Boolean checked : adminPage.getAllRowStatusSwitchStates()) {
                    Assert.assertTrue(checked,
                            "Loc 'Hoạt động' thi moi dong hien deu phai co Switch BAT (chua khoa)");
                }
            }
            adminPage.selectStatusFilterByValue("");
            Assert.assertEquals(adminPage.getPaginationTotalRows("customers"), total,
                    "Chon 'Tất cả' phai hien lai toan bo " + total + " khach da tai");
        } finally {
            adminPage.selectStatusFilterByValue("");
        }
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-190 (TC_SCRA07_FUNC_010, dot 21): Nút Đặt lại bộ lọc ở mục Khách hàng — nhập từ khóa + chọn lọc hạng Vàng + trạng thái Đã khoá (lọc tự động tải lại, không cần bấm Tìm); bấm Đặt lại thì ô tìm, dropdown hạng và dropdown trạng thái đều xóa trắng, về trang 1, bảng tải lại toàn bộ khách")
    public void adminCustomerResetFilterClearsSearchAndFilters() {
        AdminPageObject adminPage = openAdminTab("customers");
        int total = adminPage.getPaginationTotalRows("customers");
        if (total <= 0) {
            throw new SkipException("Bo qua: muc Khach hang dang rong nen khong co du lieu de kiem tra Dat lai");
        }
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        adminPage.selectCustomerTierFilterByValue("Gold");
        adminPage.selectStatusFilterByValue("locked");
        adminPage.sleepInSecond(2); // cho loc tu dong tai lai (khong con nut Tim)

        adminPage.clickToolbarReset("customers");
        Assert.assertEquals(adminPage.getCustomerSearchValue(), "",
                "Bam Dat lai xong o tim phai trong");
        Assert.assertEquals(adminPage.getCustomerTierFilterValue(), "",
                "Bam Dat lai xong loc hang phai ve 'Tất cả hạng'");
        Assert.assertEquals(adminPage.getStatusFilterValue(), "",
                "Bam Dat lai xong loc trang thai phai ve 'Tất cả'");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("customers"), 1,
                "Bam Dat lai xong phan trang phai ve trang 1");
        Assert.assertEquals(adminPage.getPaginationTotalRows("customers"), total,
                "Bam Dat lai xong bang phai tai lai toan bo " + total + " khach");
    }

    // ---------- Dot ADM19 (W4): khoa / mo khoa khach (mutate, co khoi phuc) ----------

    @Story("Khách hàng")
    @Test(description = "ADM19-191 (TC_SCRA07_FUNC_011 + TC_SCRA07_FUNC_014): Gạt Switch khóa khách demo đang hoạt động → popup B1 'Khóa tài khoản?' đúng 2 nút + ô lý do bắt buộc → xác nhận → toast 'Đã khóa tài khoản', Switch tắt; rồi mở khóa lại trong cùng test qua popup B2 'Mở khóa tài khoản?' (không ô lý do) → toast 'Đã mở khóa tài khoản', Switch bật lại (CHỈ khóa tài khoản demo; finally mở lại nếu còn khóa — nếu chạy chết giữa chừng thì demo kẹt ở trạng thái khóa, xem note_W4.md)")
    public void adminLockAndUnlockCustomerRoundTrip() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de khoa/mo khoa");
        }
        ensureDemoCustomerUnlocked(adminPage);
        try {
            adminPage.clickRowStatusSwitch(GlobalConstants.DEMO_FULL_NAME);
            adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Khóa tài khoản?",
                    "Gat Switch khach dang hoat dong phai mo popup B1 'Khóa tài khoản?', dang thay: "
                            + adminPage.getConfirmDialogTitle());
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup B1 phai CHI co dung 2 nut");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Khóa tài khoản",
                    "Nut xac nhan popup B1 phai la 'Khóa tài khoản', dang thay: " + adminPage.getConfirmDialogConfirmText());
            Assert.assertTrue(adminPage.isConfirmDialogReasonInputDisplayed(),
                    "Popup B1 phai co o nhap ly do khoa (bat buoc)");
            Assert.assertEquals(adminPage.getConfirmDialogReasonCounterText(), "0/300",
                    "Bo dem ly do khi trong phai la '0/300'");
            adminPage.setConfirmDialogReason("Vi phạm điều khoản");
            adminPage.clickConfirmDialogConfirm();
            adminPage.waitForToast();
            Assert.assertEquals(adminPage.getToastTitleText(), "Đã khóa tài khoản",
                    "Khoa xong phai hien toast 'Đã khóa tài khoản', dang thay: " + adminPage.getToastTitleText());
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_FULL_NAME), Boolean.FALSE,
                    "Khoa xong Switch khach demo phai TAT (aria-checked=false)");

            adminPage.clickRowStatusSwitch(GlobalConstants.DEMO_FULL_NAME);
            adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Mở khóa tài khoản?",
                    "Gat Switch khach da khoa phai mo popup B2 'Mở khóa tài khoản?', dang thay: "
                            + adminPage.getConfirmDialogTitle());
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup B2 phai CHI co dung 2 nut");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Mở khóa",
                    "Nut xac nhan popup B2 phai la 'Mở khóa', dang thay: " + adminPage.getConfirmDialogConfirmText());
            Assert.assertFalse(adminPage.isConfirmDialogReasonInputDisplayed(),
                    "Popup B2 KHONG co o nhap ly do");
            adminPage.clickConfirmDialogConfirm();
            adminPage.waitForToast();
            Assert.assertEquals(adminPage.getToastTitleText(), "Đã mở khóa tài khoản",
                    "Mo khoa xong phai hien toast 'Đã mở khóa tài khoản', dang thay: " + adminPage.getToastTitleText());
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_FULL_NAME), Boolean.TRUE,
                    "Mo khoa xong Switch khach demo phai BAT lai (aria-checked=true)");
        } finally {
            // Dam bao demo ve trang thai hoat dong ke ca khi test fail giua chung.
            try {
                Boolean active = adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_FULL_NAME);
                if (Boolean.FALSE.equals(active)) {
                    unlockDemoCustomer(adminPage);
                }
            } catch (Throwable ignored) {
                // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
            }
        }
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-192 (TC_SCRA07_FUNC_012): Popup khóa B1 — bộ đếm lý do '0/300' khi trống, gõ 'Vi phạm' → '7/300', gõ 310 ký tự thì chỉ nhận 300 ('300/300', maxLength=300); bấm Hủy thì không khóa, Switch giữ nguyên bật")
    public void adminLockDialogReasonCounterCapsAt300() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo popup khoa");
        }
        ensureDemoCustomerUnlocked(adminPage);
        adminPage.clickRowStatusSwitch(GlobalConstants.DEMO_FULL_NAME);
        adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Khóa tài khoản?",
                "Gat Switch khach dang hoat dong phai mo popup B1 'Khóa tài khoản?', dang thay: "
                        + adminPage.getConfirmDialogTitle());
        Assert.assertEquals(adminPage.getConfirmDialogReasonCounterText(), "0/300",
                "Bo dem ly do khi trong phai la '0/300'");
        adminPage.setConfirmDialogReason("Vi phạm");
        Assert.assertEquals(adminPage.getConfirmDialogReasonCounterText(), "7/300",
                "Go 'Vi phạm' (7 ky tu) thi bo dem phai la '7/300'");
        adminPage.setConfirmDialogReason("a".repeat(310));
        Assert.assertEquals(adminPage.getConfirmDialogReasonCounterText(), "300/300",
                "Go 310 ky tu thi bo dem phai dung o '300/300'");
        Assert.assertEquals(adminPage.getConfirmDialogReasonValue().length(), 300,
                "O ly do co maxLength=300 nen chi giu 300 ky tu du go 310 ky tu");
        adminPage.clickConfirmDialogCancel();
        adminPage.waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_FULL_NAME), Boolean.TRUE,
                "Bam Huy o popup khoa thi Switch khach demo phai giu nguyen BAT (khong khoa)");
    }

    // ---------- Dot ADM19 (W4): form sua khach (validate / luu / dirty) ----------

    @Story("Khách hàng")
    @Test(description = "ADM19-193 (TC_SCRA07_VAL_019 + TC_SCRA07_VAL_020): Sửa khách demo — bỏ trống Họ tên → lỗi đỏ 'Vui lòng bổ sung thông tin bắt buộc.' dưới ô Họ tên; nhập SĐT '12345' → lỗi 'Số điện thoại không đúng định dạng.' dưới ô SĐT; cả 2 đều không gọi PUT, form vẫn mở; đóng bằng Hủy không lưu")
    public void adminCustomerEditValidationErrors() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form sua");
        }
        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        String originalName = adminPage.getCustomerNameInputValue();
        String originalPhone = adminPage.getCustomerPhoneInputValue();
        Assert.assertEquals(adminPage.getCustomerFormMode(), "edit",
                "Form sua KH phai co data-form-mode='edit'");

        adminPage.setCustomerName("");
        adminPage.clickCustomerSaveButton();
        Assert.assertEquals(adminPage.getCustomerFieldErrorText("name"),
                "Vui lòng bổ sung thông tin bắt buộc.",
                "Bo trong Ho ten phai hien loi duoi o Ho ten");
        Assert.assertTrue(adminPage.isCustomerFormDisplayed(),
                "Loi validate thi form sua van phai mo (khong goi PUT, khong dong)");

        adminPage.setCustomerName(originalName);
        adminPage.setCustomerPhone("12345");
        adminPage.clickCustomerSaveButton();
        Assert.assertEquals(adminPage.getCustomerFieldErrorText("phone"),
                "Số điện thoại không đúng định dạng.",
                "SDT '12345' phai hien loi duoi o SDT");
        Assert.assertTrue(adminPage.isCustomerFormDisplayed(),
                "Loi validate SDT thi form sua van phai mo (khong goi PUT, khong dong)");

        adminPage.setCustomerPhone(originalPhone);
        adminPage.cancelCustomerForm();
        Assert.assertFalse(adminPage.isCustomerFormDisplayed(),
                "Dong form xong khong duoc con thay form sua khach hang");
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-194 (TC_SCRA07_FUNC_021): Sửa khách demo hợp lệ — đổi Họ tên + SĐT hợp lệ → Lưu gọi PUT, toast 'Đã cập nhật khách hàng', form đóng, bảng hiện tên mới; khôi phục tên/SĐT gốc trong finally")
    public void adminEditCustomerSavesValidChanges() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form sua");
        }
        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        String originalName = adminPage.getCustomerNameInputValue();
        String originalPhone = adminPage.getCustomerPhoneInputValue();
        String newName = originalName + " Test";
        String newPhone = randomPhone();
        try {
            adminPage.setCustomerName(newName);
            adminPage.setCustomerPhone(newPhone);
            adminPage.saveCustomerForm();
            Assert.assertEquals(adminPage.getToastTitleText(), "Đã cập nhật khách hàng",
                    "Luu KH hop le phai hien toast 'Đã cập nhật khách hàng', dang thay: " + adminPage.getToastTitleText());
            Assert.assertFalse(adminPage.isCustomerFormDisplayed(),
                    "Luu xong form sua KH phai dong");
            adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
            Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                    "Sau khi luu phai tim lai duoc khach demo theo email");
            Assert.assertTrue(adminPage.getAdminTableRowTexts().get(0).contains(newName),
                    "Bang Khach hang phai hien ten moi '" + newName + "'");
        } finally {
            // Khoi phuc ten/SDT goc (best-effort, khong lam fail test chinh).
            try {
                adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
                if (adminPage.getAdminTableRowCount() > 0) {
                    adminPage.openCustomerEditForm(newName);
                    adminPage.setCustomerName(originalName);
                    adminPage.setCustomerPhone(originalPhone);
                    adminPage.saveCustomerForm();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-195 (TC_SCRA07_FUNC_022): Nút 'Lưu thay đổi' ở form sửa khách bị disable khi chưa sửa gì, enable khi sửa Họ tên, lại disable khi hoàn đúng giá trị cũ; đóng bằng Hủy không lưu")
    public void adminCustomerSaveButtonDisabledWhenNotDirty() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form sua");
        }
        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        String originalName = adminPage.getCustomerNameInputValue();
        Assert.assertFalse(adminPage.isCustomerSaveButtonEnabled(),
                "Mo form sua chua sua gi thi nut 'Lưu thay đổi' phai bi disable");
        adminPage.setCustomerName(originalName + " Tạm");
        Assert.assertTrue(adminPage.isCustomerSaveButtonEnabled(),
                "Sua Ho ten xong nut 'Lưu thay đổi' phai enable");
        adminPage.setCustomerName(originalName);
        Assert.assertFalse(adminPage.isCustomerSaveButtonEnabled(),
                "Hoan lai dung gia tri cu thi nut 'Lưu thay đổi' phai lai disable");
        adminPage.cancelCustomerForm();
        Assert.assertFalse(adminPage.isCustomerFormDisplayed(),
                "Dong form xong khong duoc con thay form sua khach hang");
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-196 (TC_SCRA07_FUNC_023 + TC_SCRA07_VAL_024): Sửa Địa chỉ mới + xóa trống SĐT → Lưu thành công (PUT phoneNumber=null), mở lại form thấy địa chỉ mới đã lưu và SĐT cũ vẫn giữ nguyên (SĐT không bắt buộc); khôi phục địa chỉ gốc trong finally")
    public void adminEditCustomerAddressAndEmptyPhoneKeepsOldPhone() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form sua");
        }
        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        String originalAddress = adminPage.getCustomerAddressInputValue();
        String originalPhone = adminPage.getCustomerPhoneInputValue();
        String newAddress = "123 Đường Test, Quận 1";
        try {
            adminPage.setCustomerAddress(newAddress);
            adminPage.setCustomerPhone("");
            adminPage.saveCustomerForm();
            Assert.assertEquals(adminPage.getToastTitleText(), "Đã cập nhật khách hàng",
                    "Luu KH hop le phai hien toast 'Đã cập nhật khách hàng', dang thay: " + adminPage.getToastTitleText());
            adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
            Assert.assertEquals(adminPage.getCustomerAddressInputValue(), newAddress,
                    "Mo lai form phai thay dia chi moi da luu");
            Assert.assertEquals(adminPage.getCustomerPhoneInputValue(), originalPhone,
                    "De trong SDT thi BE phai giu nguyen SDT cu '" + originalPhone + "'");
            adminPage.cancelCustomerForm();
        } finally {
            // Khoi phuc dia chi goc (best-effort). Luu y: neu dia chi goc rong
            // thi khong khoi phuc duoc qua UI (o trong -> FE gui null = giu
            // nguyen, theo dung quy uoc cua UpdateCustomerCommand).
            try {
                if (originalAddress != null && !originalAddress.isEmpty()) {
                    adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
                    if (adminPage.getAdminTableRowCount() > 0) {
                        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
                        adminPage.setCustomerAddress(originalAddress);
                        adminPage.saveCustomerForm();
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-197 (TC_SCRA07_VAL_025 + TC_SCRA07_VAL_026): Họ tên 1 ký tự / Địa chỉ 501 ký tự khi sửa khách bị BE từ chối — lỗi hiện ở form-level (formError), KHÔNG có lỗi dưới ô và KHÔNG có toast (toFormSaveError chỉ map lỗi trùng, xem note_W4.md); form vẫn mở, không lưu; đóng bằng Hủy")
    public void adminCustomerEditBackendLengthValidation() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form sua");
        }
        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        String originalName = adminPage.getCustomerNameInputValue();
        String originalAddress = adminPage.getCustomerAddressInputValue();

        adminPage.setCustomerName("A");
        adminPage.clickCustomerSaveButton();
        Assert.assertEquals(waitForCustomerFormError(adminPage), "Họ tên phải có ít nhất 2 ký tự.",
                "Ho ten 1 ky tu phai bi BE tu choi voi loi form-level 'Họ tên phải có ít nhất 2 ký tự.'");
        Assert.assertEquals(adminPage.getCustomerFieldErrorText("name"), "",
                "Loi do dai Ho ten cua BE KHONG map xuong o Ho ten (chi hien form-level)");
        Assert.assertTrue(adminPage.isCustomerFormDisplayed(),
                "Luu that bai thi form sua van phai mo (khong dong, khong luu)");

        adminPage.setCustomerName(originalName);
        adminPage.setCustomerAddress("a".repeat(501));
        adminPage.clickCustomerSaveButton();
        Assert.assertEquals(waitForCustomerFormError(adminPage), "Địa chỉ tối đa 500 ký tự.",
                "Dia chi 501 ky tu phai bi BE tu choi voi loi form-level 'Địa chỉ tối đa 500 ký tự.'");
        Assert.assertTrue(adminPage.isCustomerFormDisplayed(),
                "Luu that bai thi form sua van phai mo (khong dong, khong luu)");

        adminPage.setCustomerAddress(originalAddress);
        adminPage.cancelCustomerForm();
        Assert.assertFalse(adminPage.isCustomerFormDisplayed(),
                "Dong form xong khong duoc con thay form sua khach hang");
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-198 (TC_SCRA07_FUNC_028): Sửa Họ tên rồi bấm Hủy → hộp 'Thoát mà chưa lưu?' đúng 2 nút [Ở lại]+[Thoát]; bấm 'Ở lại' thì form vẫn mở, dữ liệu đã sửa còn nguyên; bấm 'Thoát' thì đóng form không lưu; chưa sửa gì mà bấm Hủy thì đóng luôn không hỏi")
    public void adminCustomerEditDirtyConfirmOnCancel() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form sua");
        }
        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        String originalName = adminPage.getCustomerNameInputValue();
        String editedName = originalName + " Tạm";
        adminPage.setCustomerName(editedName);
        adminPage.clickCustomerCancelButtonOnly();
        adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Thoát mà chưa lưu?",
                "Sua roi bam Huy phai hoi 'Thoát mà chưa lưu?', dang thay: " + adminPage.getConfirmDialogTitle());
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Hop 'Thoát mà chưa lưu?' phai CHI co dung 2 nut");
        Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Ở lại",
                "Nut trai cua hop dirty phai la 'Ở lại', dang thay: " + adminPage.getConfirmDialogCancelText());
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Thoát",
                "Nut phai cua hop dirty phai la 'Thoát', dang thay: " + adminPage.getConfirmDialogConfirmText());
        adminPage.clickConfirmDialogCancel();
        adminPage.waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertTrue(adminPage.isCustomerFormDisplayed(),
                "Bam 'Ở lại' thi form sua van phai mo");
        Assert.assertEquals(adminPage.getCustomerNameInputValue(), editedName,
                "Bam 'Ở lại' thi du lieu da sua phai con nguyen");
        adminPage.clickCustomerCancelButtonOnly();
        adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        adminPage.clickConfirmDialogConfirm();
        adminPage.waitForElementInvisible(AdminPageUI.ADMIN_CUSTOMER_FORM);
        Assert.assertFalse(adminPage.isCustomerFormDisplayed(),
                "Bam 'Thoát' thi form phai dong khong luu");

        // Mo lai form: chua sua gi ma bam Huy thi dong luon, khong hoi.
        adminPage.openCustomerEditForm(GlobalConstants.DEMO_FULL_NAME);
        adminPage.clickCustomerCancelButtonOnly();
        sleepInSecond(1);
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Chua sua gi ma bam Huy thi KHONG duoc hoi 'Thoát mà chưa lưu?'");
        Assert.assertFalse(adminPage.isCustomerFormDisplayed(),
                "Chua sua gi ma bam Huy thi form phai dong luon");
    }

    @Story("Khách hàng")
    @Test(description = "ADM19-199 (TC_SCRA07_UI_029 + TC_SCRA07_UI_030): KHÔNG có form tạo khách mới và KHÔNG xóa khách — toolbar không nút Thêm mới/Export, cột Thao tác chỉ có mắt Xem + bút Sửa (không nút Xóa), form xem footer chỉ [Đóng]+[Chỉnh sửa]")
    public void adminCustomerHasNoCreateOrDeleteCustomer() {
        AdminPageObject adminPage = openAdminTab("customers");
        Assert.assertFalse(adminPage.isToolbarAddButtonPresent("customers"),
                "Muc Khach hang KHONG co nut Them moi/Tao khach hang");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Muc Khach hang KHONG co nut Export");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de kiem tra cot Thao tac");
        }
        Assert.assertFalse(adminPage.isCustomerRowDeleteButtonDisplayed(GlobalConstants.DEMO_FULL_NAME),
                "Cot Thao tac KHONG co nut Xoa khach hang");
        Assert.assertTrue(adminPage.isCustomerRowEditButtonDisplayed(GlobalConstants.DEMO_FULL_NAME),
                "Cot Thao tac phai co nut but Sua khach hang");
        String detailLabel = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CUSTOMER_DETAIL_BTN, "Xem khách hàng ");
        Assert.assertEquals(detailLabel, GlobalConstants.DEMO_FULL_NAME,
                "Cot Thao tac phai co nut mat Xem khach hang");
        adminPage.clickFirstCustomerDetail();
        Assert.assertTrue(adminPage.isCustomerDetailPageDisplayed(),
                "Phai mo duoc trang xem khach hang full page (admin-customer-detail-page)");
        List<String> footerTexts = adminPage.getPageHeadButtonTexts("admin-customer-detail-page");
        Assert.assertEquals(footerTexts.size(), 2,
                "Header trang xem KH phai CHI co dung 2 nut, dang thay: " + footerTexts);
        Assert.assertTrue(footerTexts.contains("Đóng") && footerTexts.contains("Chỉnh sửa"),
                "Header trang xem KH phai la [Đóng] + [Chỉnh sửa], dang thay: " + footerTexts);
        adminPage.closeCustomerDetailModal();
        Assert.assertFalse(adminPage.isCustomerDetailPageDisplayed(),
                "Dong trang xem xong khong duoc con thay trang xem khach hang");
    }
}
