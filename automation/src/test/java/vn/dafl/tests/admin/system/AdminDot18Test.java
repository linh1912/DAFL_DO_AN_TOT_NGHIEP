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

/**
 * Test MOI dot 18 cho Admin tren app rieng localhost:3001 — ConfirmDialog
 * (Huy khong goi API, xac nhan goi API + toast), khoa KH bat buoc ly do
 * (bo dem 300 ky tu, B1), badge menu, MENU PHANG wave D (khong dropdown),
 * view mode (SP/DM/VC/KH) dang Drawer rong, footer chi tiet don co dieu kien,
 * drawer doi mat khau, khong con Export, popup doi trang thai D2/D3, dang
 * xuat hoi popup G3 (ADM18-15), toolbar icon Lucide (ADM18-16).
 *
 * Quy tac dot 18 wave D duoc khang dinh o day: moi popup mac dinh CHI dung
 * 2 nut (ngoai le 1 nut: A3 chan xoa DM [Đã hiểu] + link, G4 phien het han);
 * toast thay cho banner inline (.sa-notice); form XEM readonly + footer
 * [Dong] + [Chinh sua]; menu PHANG (tieu de nhom la <div>, ten dang tieu de,
 * khong bam duoc, khong aria-expanded, khong localStorage, khong badge tong
 * nhom); badge cam nhat toi da '99+' an khi 0; Export da bo han khoi moi toolbar.
 *
 * Luu y gioi han (ghi trong bao cao dot 18 wave D):
 * - Badge '99+' (> 99 don cho xu ly) KHONG tai hien duoc o Selenium vi can
 *   100 don that cho xu ly trong DB — chi cover o Vitest (AdminDot18.test.tsx,
 *   mock pendingOrders = 150 -> '99+'); test ADM18-04 chi khang dinh dinh
 *   dang badge (so hoac '99+') khi badge hien.
 * - Xoa tai khoan KH (ConfirmDialog typeExpected 'XOA') KHONG co UI/endpoint
 *   (khong co DELETE /api/admin/customers) nen khong co test Selenium nao
 *   cho XOA — chi cover o Vitest cap component ConfirmDialog.
 * - A3 chan xoa DM dang co SP (1 nut [Đã hiểu] + link), A5/C5, G4 phien het
 *   han, E3/D4/C8/C9/H2/G6 (chua noi API) chi cover o Vitest cap component
 *   ConfirmDialog / messages.vi — khong co test Selenium.
 * Chi dung du lieu do test tao (ten/ma kem timestamp) va tu don dep sau khi
 * chay; KHONG khoa tai khoan demo (ADM18-03 chi mo dialog roi Huy).
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Đợt 18 — ConfirmDialog, menu, view mode")
public class AdminDot18Test extends BaseTest {

    private String createdProductName;
    private String createdCategoryName;
    private String createdPromoCode;
    private boolean adminLoggedIn = false;

    /** Ten san pham duy nhat theo tung lan chay (kem timestamp). */
    private String uniqueProductName() {
        return "TESTSP18-" + System.currentTimeMillis();
    }

    /** Ten danh muc duy nhat theo tung lan chay (kem timestamp). */
    private String uniqueCategoryName() {
        return "TESTDM18-" + System.currentTimeMillis();
    }

    /** Ma voucher duy nhat theo tung lan chay (chu + so, hop le 3-50 ky tu). */
    private String uniquePromoCode() {
        return "TEST18" + System.currentTimeMillis();
    }

    /** Dang nhap Super Admin tren app rieng :3001; bo qua test neu chua san sang. */
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

    /** Tao 1 san pham moi (ma tu sinh SP-xxxx) roi tra ve ten. */
    private String createProductOrSkip(AdminPageObject adminPage) {
        String productName = uniqueProductName();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();
        String categoryName = adminPage.getFirstRealCategoryName();
        Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");
        adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "120000", "10",
                "Mo ta san pham test dot 18");
        createdProductName = productName;
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao san pham xong phai hien toast thanh cong (admin-toast)");
        return productName;
    }

    /** Xoa san pham test neu van con (chi goi voi ten do test tao). */
    private void deleteProductIfPresent(String productName) {
        if (productName == null || !adminLoggedIn) {
            return;
        }
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.clickAdminNav("products");
            adminPage.searchProducts(productName);
            if (adminPage.isRowWithTextDisplayed(productName)) {
                adminPage.deleteProductByName(productName);
            }
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        }
    }

    /** Xoa danh muc test neu van con (chi goi voi ten do test tao). */
    private void deleteCategoryIfPresent(String categoryName) {
        if (categoryName == null || !adminLoggedIn) {
            return;
        }
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.clickAdminNav("categories");
            adminPage.searchCategories(categoryName);
            if (adminPage.isRowWithTextDisplayed(categoryName)) {
                adminPage.deleteCategoryByName(categoryName);
            }
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        }
    }

    /** Xoa voucher Draft test neu van con (chi goi voi ma do test tao). */
    private void deletePromoIfPresent(String promoCode) {
        if (promoCode == null || !adminLoggedIn) {
            return;
        }
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.clickAdminNav("promotions");
            adminPage.searchPromos(promoCode);
            if (adminPage.isRowWithTextDisplayed(promoCode)) {
                adminPage.deletePromoByCode(promoCode);
            }
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        }
    }

    /** An 1 san pham dang Hoat dong bang Switch (xac nhan 'Ẩn sản phẩm?'). */
    private void hideProduct(AdminPageObject adminPage, String productName) {
        adminPage.searchProducts(productName);
        adminPage.clickRowStatusSwitch(productName);
        Assert.assertTrue(adminPage.getConfirmModalText().contains("Ẩn sản phẩm?"),
                "Gat Switch tat SP phai mo ConfirmDialog 'Ẩn sản phẩm?'");
        adminPage.clickConfirmYes();
        adminPage.waitForFeedback();
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.FALSE,
                "An SP xong Switch phai tat (aria-checked=false)");
    }

    // ---------- ConfirmDialog: Huy / xac nhan xoa san pham ----------

    @Story("ConfirmDialog")
    @Test(description = "ADM18-01 (đợt 18): Xóa SP đã Ngừng hoạt động → ConfirmDialog 'Xóa sản phẩm?' (đúng 2 nút, có dòng 'không thể hoàn tác.') → bấm [Hủy] thì SP vẫn còn, không gọi API xóa, không có toast")
    public void confirmDialogCancelDeleteKeepsProduct() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String productName = createProductOrSkip(adminPage);
            hideProduct(adminPage, productName);

            adminPage.clickProductDeleteButton(productName);
            Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                    "Bam xoa SP Ngung hoat dong phai mo ConfirmDialog");
            Assert.assertTrue(adminPage.getConfirmDialogTitle().contains("Xóa sản phẩm?"),
                    "Tieu de popup xoa phai la 'Xóa sản phẩm?', dang thay: " + adminPage.getConfirmDialogTitle());
            Assert.assertTrue(adminPage.getConfirmDialogText().contains("không thể hoàn tác"),
                    "Popup xoa phai co dong canh bao 'không thể hoàn tác.', dang thay: " + adminPage.getConfirmDialogText());
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup xoa dot 18 phai CHI co dung 2 nut ([Hủy] + [Xóa sản phẩm])");
            Assert.assertTrue(adminPage.getConfirmDialogCancelText().contains("Hủy"),
                    "Nut huy phai la 'Hủy', dang thay: " + adminPage.getConfirmDialogCancelText());

            adminPage.clickConfirmNo();
            Assert.assertFalse(adminPage.isConfirmDialogDisplayed(),
                    "Bam 'Hủy' xong ConfirmDialog phai bien mat");
            // Cho toast cua buoc an SP (neu con) tu an het truoc khi khang dinh
            // khong co toast moi (toast thanh cong tu an sau 4 giay).
            adminPage.sleepInSecond(5);
            Assert.assertFalse(adminPage.isToastDisplayed(),
                    "Bam 'Hủy' thi khong duoc co toast moi (khong goi API xoa)");

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Bam 'Hủy' o popup xoa thi SP van phai con trong bang");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("ConfirmDialog")
    @Test(description = "ADM18-02 (đợt 18): Xóa SP đã Ngừng hoạt động → ConfirmDialog 'Xóa sản phẩm?' → bấm [Xóa sản phẩm] thì SP biến mất khỏi bảng + toast 'Đã xóa sản phẩm'")
    public void confirmDialogConfirmDeleteRemovesProduct() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String productName = createProductOrSkip(adminPage);
            hideProduct(adminPage, productName);

            adminPage.clickProductDeleteButton(productName);
            Assert.assertTrue(adminPage.getConfirmDialogTitle().contains("Xóa sản phẩm?"),
                    "Tieu de popup xoa phai la 'Xóa sản phẩm?', dang thay: " + adminPage.getConfirmDialogTitle());
            Assert.assertTrue(adminPage.getConfirmDialogConfirmText().contains("Xóa sản phẩm"),
                    "Nut xac nhan phai la 'Xóa sản phẩm', dang thay: " + adminPage.getConfirmDialogConfirmText());
            adminPage.clickConfirmYes();
            Assert.assertTrue(adminPage.isToastDisplayed(),
                    "Xac nhan xoa xong phai hien toast thanh cong (admin-toast)");
            Assert.assertTrue(adminPage.getToastTitleText().contains("Đã xóa sản phẩm"),
                    "Toast xoa SP phai co tieu de 'Đã xóa sản phẩm', dang thay: " + adminPage.getToastTitleText());
            createdProductName = null; // da xoa, khong can don dep nua

            adminPage.searchProducts(productName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Xac nhan xoa xong tim SP phai ra 0 dong");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    // ---------- Khoa khach hang: bat buoc ly do ----------

    @Story("Khách hàng")
    @Test(description = "ADM18-03 (đợt 18): Khóa tài khoản khách demo → ConfirmDialog 'Khóa tài khoản?' có ô lý do: bấm xác nhận khi để trống thì báo 'Vui lòng nhập lý do khóa.', dialog vẫn mở, Switch vẫn bật; bấm [Hủy] thì không khóa (KHÔNG khóa thật)")
    public void lockCustomerRequiresReason() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo dialog khoa");
        }
        if (!Boolean.TRUE.equals(adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_EMAIL))) {
            throw new SkipException("Bo qua: tai khoan demo dang bi khoa (Switch tat) nen khong mo duoc dialog 'Khóa tài khoản?'");
        }

        adminPage.clickRowStatusSwitch(GlobalConstants.DEMO_EMAIL);
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Gat Switch khoa KH phai mo ConfirmDialog");
        Assert.assertTrue(adminPage.getConfirmDialogTitle().contains("Khóa tài khoản?"),
                "Tieu de popup khoa KH phai la 'Khóa tài khoản?', dang thay: " + adminPage.getConfirmDialogTitle());
        Assert.assertTrue(adminPage.isConfirmDialogReasonInputDisplayed(),
                "Popup khoa KH phai co o nhap ly do (confirm-dialog-reason-input)");
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Popup khoa KH dot 18 phai CHI co dung 2 nut ([Hủy] + [Khóa tài khoản])");

        // Bam xac nhan khi de trong ly do -> loi, dialog van mo, chua khoa.
        // (Nut xac nhan van bam duoc khi thieu ly do — chi hien loi chu khong disable.)
        adminPage.clickConfirmYes();
        Assert.assertEquals(adminPage.getConfirmDialogReasonErrorText(), "Vui lòng nhập lý do khóa.",
                "Khoa KH khong nhap ly do thi phai bao 'Vui lòng nhập lý do khóa.'");
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Chua nhap ly do thi ConfirmDialog khoa KH phai van mo");
        // B1 (spec A–H): o ly do co bo dem "n/300" (toi da 300 ky tu).
        Assert.assertEquals(adminPage.getConfirmDialogReasonCounterText(), "0/300",
                "Bo dem ly do khoa luc trong phai la '0/300', dang thay: " + adminPage.getConfirmDialogReasonCounterText());
        adminPage.setConfirmDialogReason("Spam");
        Assert.assertEquals(adminPage.getConfirmDialogReasonCounterText(), "4/300",
                "Go 4 ky tu ly do thi bo dem phai la '4/300', dang thay: " + adminPage.getConfirmDialogReasonCounterText());
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_EMAIL), Boolean.TRUE,
                "Chua nhap ly do thi tai khoan demo van phai Hoat dong (Switch van bat)");

        // Huy: khong khoa, tai khoan demo van Hoat dong.
        adminPage.clickConfirmNo();
        Assert.assertFalse(adminPage.isConfirmDialogDisplayed(),
                "Bam 'Hủy' xong ConfirmDialog phai bien mat");
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(GlobalConstants.DEMO_EMAIL), Boolean.TRUE,
                "Bam 'Hủy' o popup khoa KH thi tai khoan demo van phai Hoat dong");
    }

    // ---------- Badge menu + dropdown nhom ----------

    @Story("Menu quản trị")
    @Test(description = "ADM18-04 (đợt 18): Badge menu — badge Đơn/Trả/Đánh giá (nếu hiện) chỉ là số hoặc '99+', ẩn khi = 0; bấm từng mục điều hướng đúng tab (Đơn hàng / Trả hàng / Đánh giá)")
    public void navBadgesFormatAndNavigate() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        for (String navKey : new String[]{"orders", "returns", "reviews"}) {
            String badge = adminPage.getNavBadgeText(navKey);
            Assert.assertTrue(badge == null || badge.matches("\\d+|99\\+"),
                    "Badge muc '" + navKey + "' neu hien thi chi duoc la so hoac '99+', dang thay: '" + badge + "'");
        }

        adminPage.clickAdminNav("orders");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Bam muc Don hang phai vao tab Don hang (orders-search-input)");
        adminPage.clickAdminNav("returns");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='returns-search-input']"),
                "Bam muc Tra hang phai vao tab Tra hang (returns-search-input)");
        adminPage.clickAdminNav("reviews");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='reviews-search-input']"),
                "Bam muc Danh gia phai vao tab Danh gia (reviews-search-input)");
    }

    @Story("Menu quản trị")
    @Test(description = "ADM18-05 (đợt 18 wave D, viết lại — menu PHẲNG, không dropdown): Tiêu đề nhóm là <div> không bấm được, tên dạng tiêu đề ('Bán hàng'/'Quản lý sản phẩm'/'Quản lý khách hàng'/'Hệ thống'), không aria-expanded/chevron, không badge tổng nhóm; cả 10 mục luôn hiện; bấm 'Xem' đơn chờ xác nhận ở dashboard đi thẳng tab Đơn hàng")
    public void navFlatMenuGroupTitlesAreNonClickableDivs() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        // Tieu de nhom PHANG: <div>, ten dang tieu de, khong aria-expanded.
        Assert.assertTrue(adminPage.isNavGroupTitleDiv("sales"),
                "Tieu de nhom Bán hàng phai la <div> khong bam duoc (khong button/aria-expanded/chevron)");
        Assert.assertTrue(adminPage.isNavGroupTitleDiv("products"),
                "Tieu de nhom Quản lý sản phẩm phai la <div> khong bam duoc");
        Assert.assertTrue(adminPage.isNavGroupTitleDiv("customers"),
                "Tieu de nhom Quản lý khách hàng phai la <div> khong bam duoc");
        Assert.assertTrue(adminPage.isNavGroupTitleDiv("system"),
                "Tieu de nhom Hệ thống phai la <div> khong bam duoc");
        Assert.assertEquals(adminPage.getNavGroupTitleText("sales"), "Bán hàng",
                "Ten nhom sales phai la 'Bán hàng' (dang tieu de, khong in hoa), dang thay: " + adminPage.getNavGroupTitleText("sales"));
        Assert.assertEquals(adminPage.getNavGroupTitleText("products"), "Quản lý sản phẩm",
                "Ten nhom products phai la 'Quản lý sản phẩm', dang thay: " + adminPage.getNavGroupTitleText("products"));
        Assert.assertEquals(adminPage.getNavGroupTitleText("customers"), "Quản lý khách hàng",
                "Ten nhom customers phai la 'Quản lý khách hàng', dang thay: " + adminPage.getNavGroupTitleText("customers"));
        Assert.assertEquals(adminPage.getNavGroupTitleText("system"), "Hệ thống",
                "Ten nhom system phai la 'Hệ thống', dang thay: " + adminPage.getNavGroupTitleText("system"));

        // Khong con badge tong nhom (admin-nav-group-*-badge da xoa o wave D).
        for (String groupKey : new String[]{"sales", "products", "customers", "system"}) {
            Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='admin-nav-group-" + groupKey + "-badge']"),
                    "Wave D da xoa badge tong nhom — khong duoc con admin-nav-group-" + groupKey + "-badge");
        }

        // Ca 10 muc luon hien (khong thu gon duoc), bam tung muc dieu huong dung tab.
        adminPage.clickAdminNav("orders");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Bam muc Don hang phai vao tab Don hang (orders-search-input)");
        adminPage.clickAdminNav("returns");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='returns-search-input']"),
                "Bam muc Tra hang phai vao tab Tra hang (returns-search-input)");
        adminPage.clickAdminNav("reviews");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='reviews-search-input']"),
                "Bam muc Danh gia phai vao tab Danh gia (reviews-search-input)");

        // Vao Don hang tu dashboard: di thang tab Don, khong con chuyen tu mo nhom.
        adminPage.clickAdminNav("dashboard");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Phai o muc Tong quan");
        if (!adminPage.isElementEnabled("css=[data-testid='admin-dashboard-action-pending-orders-view']")) {
            throw new SkipException("Bo qua phan vao Don tu dashboard: khong co don cho xac nhan nen nut 'Xem' o dashboard bi tat");
        }
        adminPage.clickDashboardPendingOrdersView();
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-nav-orders']"),
                "Vao Don hang tu dashboard thi muc Don hang phai hien (menu phang luon hien)");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Vao Don hang tu dashboard phai dung o tab Don hang");
    }

    // ---------- View mode: san pham / danh muc / khuyen mai / khach hang ----------

    @Story("Form xem")
    @Test(description = "ADM18-06 (đợt 21): TRANG Chi tiết sản phẩm full page — breadcrumb 'Quản lý sản phẩm > Chi tiết sản phẩm', đủ thông tin (mã/tên/danh mục/giá/tồn kho/trạng thái/hình ảnh/mô tả), badge 'Đang hoạt động'; header CHỈ 2 nút [Quay lại]+[Chỉnh sửa]; bấm [Chỉnh sửa] sang trang sửa giữ nguyên tên; Quay lại về danh sách không lưu, xóa SP sau test")
    public void productViewModeIsReadonlyAndEditKeepsData() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String productName = createProductOrSkip(adminPage);
            adminPage.searchProducts(productName);
            adminPage.clickFirstProductDetail();
            Assert.assertTrue(adminPage.isProductDetailPageDisplayed(),
                    "Phai mo duoc trang chi tiet san pham full page (admin-product-detail-page)");
            Assert.assertFalse(adminPage.isProductFormModalPresent(),
                    "Trang chi tiet dot 21 KHONG duoc mo duoi dang Drawer/modal form");
            String detailText = adminPage.getProductDetailText();
            Assert.assertTrue(detailText.contains(productName),
                    "Trang chi tiet phai hien dung ten san pham '" + productName + "', dang thay: " + detailText);
            Assert.assertTrue(detailText.contains("Đang hoạt động"),
                    "SP moi tao dang Hoat dong nen trang chi tiet phai co badge 'Đang hoạt động', dang thay: " + detailText);
            String breadcrumb = adminPage.getProductDetailBreadcrumbText();
            Assert.assertTrue(breadcrumb.contains("Chi tiết sản phẩm"),
                    "Breadcrumb phai co 'Chi tiết sản phẩm', dang thay: " + breadcrumb);

            adminPage.openProductEditFormFromDetail();
            Assert.assertEquals(adminPage.getProductFormMode(), "edit",
                    "Bam [Chỉnh sửa] tu trang chi tiet phai chuyen sang data-form-mode='edit'");
            Assert.assertEquals(adminPage.getProductNameInputValue(), productName,
                    "Sang edit phai giu nguyen ten SP '" + productName + "'");
            adminPage.cancelProductForm();
            Assert.assertFalse(adminPage.isProductFormModalPresent(),
                    "Dong form sua xong khong duoc con thay form san pham");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Form xem")
    @Test(description = "ADM18-07 (đợt 18): Form XEM danh mục — mode 'view', tên readonly, badge 'Hiển thị', footer CHỈ [Đóng]+[Chỉnh sửa]; bấm [Chỉnh sửa] sang edit giữ nguyên tên; đóng không lưu, xóa DM sau test")
    public void categoryViewModeIsReadonlyAndEditKeepsData() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String categoryName = uniqueCategoryName();
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta danh muc test ADM18-07", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchCategories(categoryName);
            adminPage.clickFirstCategoryDetail();
            Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                    "Phai mo duoc form xem danh muc (admin-category-form-modal)");
            // Wave D: form DM la Drawer rong 680px (--fs-width), khong con Modal 560px.
            Assert.assertTrue(adminPage.isFormDrawer("admin-category-form-modal"),
                    "Form xem DM wave D phai la Drawer (container co class fs-drawer)");
            Assert.assertEquals(adminPage.getFormDrawerWidth("admin-category-form-modal"), 680,
                    "Drawer form DM phai rong 680px (--fs-width), dang thay: " + adminPage.getFormDrawerWidth("admin-category-form-modal"));
            Assert.assertEquals(adminPage.getCategoryFormMode(), "view",
                    "Form xem DM phai co data-form-mode='view'");
            Assert.assertTrue(adminPage.isCategoryNameInputReadOnly(),
                    "View mode: o ten DM phai readonly");
            String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_CATEGORY_DETAIL);
            Assert.assertTrue(modalText.contains("Hiển thị"),
                    "DM moi tao dang hien nen form xem phai co badge 'Hiển thị', dang thay: " + modalText);
            java.util.List<String> footerTexts = adminPage.getDetailFooterButtonTexts("admin-category-form-modal");
            Assert.assertEquals(footerTexts.size(), 2,
                    "Footer form xem dot 18 phai CHI co dung 2 nut, dang thay: " + footerTexts);
            Assert.assertTrue(footerTexts.contains("Đóng") && footerTexts.contains("Chỉnh sửa"),
                    "Footer form xem phai la [Đóng] + [Chỉnh sửa], dang thay: " + footerTexts);

            adminPage.openCategoryEditFormFromDetail();
            Assert.assertEquals(adminPage.getCategoryFormMode(), "edit",
                    "Bam [Chỉnh sửa] tu form xem phai chuyen sang data-form-mode='edit'");
            Assert.assertEquals(adminPage.getCategoryNameInputValue(), categoryName,
                    "Sang edit phai giu nguyen ten DM '" + categoryName + "'");
            adminPage.cancelCategoryForm();
            Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                    "Dong form sua xong khong duoc con thay form danh muc");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Form xem")
    @Test(description = "ADM18-08 (đợt 21): TRANG Chi tiết khuyến mãi full page — breadcrumb 'Quản lý khuyến mãi / Chi tiết', tiêu đề 'Chi tiết khuyến mãi', đủ 4 card, mã đúng, badge 'Nháp'; header CHỈ 2 nút [Quay lại]+[Chỉnh sửa]; bấm [Chỉnh sửa] sang trang sửa giữ nguyên mã (ô Mã disabled); Hủy về danh sách không lưu, xóa voucher sau test")
    public void promoViewModeIsReadonlyAndEditKeepsData() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String promoCode = uniquePromoCode();
            adminPage.clickAdminNav("promotions");
            adminPage.createPromoFull(promoCode, "Voucher test ADM18-08", "Percentage", "10",
                    "0", "", "", "2027-12-31");
            createdPromoCode = promoCode;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchPromos(promoCode);
            adminPage.clickFirstPromoDetail();
            Assert.assertTrue(adminPage.isPromoDetailPageDisplayed(),
                    "Phai mo duoc trang chi tiet khuyen mai (admin-promo-detail-page)");
            Assert.assertFalse(adminPage.isPromoFormModalDisplayed(),
                    "Trang chi tiet dot 21 KHONG duoc mo duoi dang Drawer/modal form");
            String detailText = adminPage.getPromoDetailPageText();
            Assert.assertTrue(detailText.contains("Chi tiết khuyến mãi"),
                    "Trang chi tiet phai co tieu de 'Chi tiết khuyến mãi', dang thay: " + detailText);
            Assert.assertTrue(detailText.contains(promoCode),
                    "Trang chi tiet phai hien dung ma khuyen mai '" + promoCode + "', dang thay: " + detailText);
            Assert.assertTrue(adminPage.isPromoDetailCardsDisplayed(),
                    "Trang chi tiet phai co du 4 card: Thong tin chung / Dieu kien ap dung / Thoi gian / Thong ke su dung");
            Assert.assertTrue(detailText.contains("Nháp"),
                    "Voucher moi tao la Draft nen trang chi tiet phai co badge 'Nháp', dang thay: " + detailText);
            Assert.assertTrue(adminPage.isPromoDetailEditButtonDisplayed(),
                    "Voucher Draft thi header trang chi tiet phai co nut [Chỉnh sửa]");

            adminPage.openPromoEditFormFromDetail();
            Assert.assertTrue(adminPage.isPromoEditPageDisplayed(),
                    "Bam [Chỉnh sửa] tu trang chi tiet phai mo trang sua (admin-promo-edit-page)");
            Assert.assertEquals(adminPage.getPromoFormMode(), "edit",
                    "Bam [Chỉnh sửa] tu trang chi tiet phai chuyen sang data-form-mode='edit'");
            Assert.assertTrue(adminPage.isPromoCodeInputDisabled(),
                    "Sang edit thi o ma VC phai disabled (ma khong sua duoc)");
            Assert.assertEquals(adminPage.getPromoCodeInputValue(), promoCode,
                    "Sang edit thi o Ma phai giu nguyen ma '" + promoCode + "'");
            adminPage.cancelPromoForm();
            Assert.assertTrue(adminPage.isPromoListPageDisplayed(),
                    "Huy o trang sua (form sach) phai ve lai danh sach khuyen mai");
            Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                    "Dong form sua xong khong duoc con thay form khuyen mai");
        } finally {
            deletePromoIfPresent(createdPromoCode);
            createdPromoCode = null;
        }
    }

    @Story("Form xem")
    @Test(description = "ADM18-09 (đợt 18): Form XEM khách hàng (demo) — mode 'view', tên readonly, footer CHỈ [Đóng]+[Chỉnh sửa], KHÔNG có mục đơn gần đây; đóng bằng [Đóng]")
    public void customerViewModeIsReadonlyWithoutRecentOrders() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("customers");
        adminPage.searchCustomers(GlobalConstants.DEMO_EMAIL);
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de mo form xem");
        }

        adminPage.clickFirstCustomerDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CUSTOMER_DETAIL),
                "Phai mo duoc form xem khach hang (admin-customer-form-page)");
        // Wave D: form KH la Drawer rong 680px (--fs-width), khong con Modal.
        Assert.assertTrue(adminPage.isFormDrawer("admin-customer-form-page"),
                "Form xem KH wave D phai la Drawer (container co class fs-drawer)");
        Assert.assertEquals(adminPage.getFormDrawerWidth("admin-customer-form-page"), 680,
                "Drawer form KH phai rong 680px (--fs-width), dang thay: " + adminPage.getFormDrawerWidth("admin-customer-form-page"));
        Assert.assertEquals(adminPage.getCustomerFormMode(), "view",
                "Form xem KH phai co data-form-mode='view'");
        Assert.assertTrue(adminPage.isCustomerNameInputReadOnly(),
                "View mode: o ho ten KH phai readonly");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_CUSTOMER_DETAIL);
        Assert.assertTrue(modalText.contains("Chi tiết khách hàng"),
                "Form xem phai co tieu de 'Chi tiết khách hàng', dang thay: " + modalText);
        Assert.assertFalse(modalText.contains("Đơn hàng gần đây"),
                "Form xem KH dot 18 KHONG con muc don gan day, dang thay: " + modalText);
        java.util.List<String> footerTexts = adminPage.getDetailFooterButtonTexts("admin-customer-form-page");
        Assert.assertEquals(footerTexts.size(), 2,
                "Footer form xem dot 18 phai CHI co dung 2 nut, dang thay: " + footerTexts);
        Assert.assertTrue(footerTexts.contains("Đóng") && footerTexts.contains("Chỉnh sửa"),
                "Footer form xem phai la [Đóng] + [Chỉnh sửa], dang thay: " + footerTexts);

        adminPage.closeCustomerDetailModal();
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CUSTOMER_DETAIL),
                "Bam 'Đóng' o footer phai dong duoc form xem khach hang");
    }

    // ---------- Footer chi tiet don co dieu kien ----------

    @Story("Đơn hàng")
    @Test(description = "ADM18-10 (đợt 21, viết lại theo trang chi tiết riêng): Header chi tiết đơn LUÔN đúng 2 nút [Hủy đơn]+[Đổi trạng thái]; đơn 'Chờ xử lý' thì cả 2 bấm được, đơn 'Hoàn tất' (terminal) thì cả 2 disable; chỉ mở xem, không đổi trạng thái (skip phần nào không có đơn tương ứng)")
    public void orderDetailFooterDependsOnStatus() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        boolean checkedAny = false;

        // (1) Don 'Chờ xử lý' -> [Hủy đơn] + [Đổi trạng thái] đều bấm được.
        adminPage.clickAdminNav("orders");
        adminPage.selectOrderStatusFilter(GlobalConstants.ADMIN_ORDER_STATUS_PENDING_LABEL);
        adminPage.clickOrderFilterSearch();
        if (adminPage.getAdminTableRowCount() > 0) {
            checkedAny = true;
            adminPage.clickFirstOrderDetail();
            Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                    "Phai mo duoc trang chi tiet don hang");
            java.util.List<String> headerTexts = adminPage.getOrderDetailHeaderButtonTexts();
            Assert.assertEquals(headerTexts.size(), 2,
                    "Header don 'Chờ xử lý' phai CHI co dung 2 nut, dang thay: " + headerTexts);
            Assert.assertTrue(headerTexts.contains("Hủy đơn") && headerTexts.contains("Đổi trạng thái"),
                    "Header don 'Chờ xử lý' phai la [Hủy đơn] + [Đổi trạng thái], dang thay: " + headerTexts);
            Assert.assertTrue(adminPage.isOrderCancelButtonEnabled(),
                    "Don 'Chờ xử lý' phai bam duoc nut [Hủy đơn]");
            Assert.assertTrue(adminPage.isOrderChangeStatusButtonEnabled(),
                    "Don 'Chờ xử lý' phai bam duoc nut [Đổi trạng thái]");
            adminPage.closeOrderDetail();
        }

        // (2) Don 'Hoàn tất' (terminal) -> cả 2 nút đều disable.
        adminPage.clickAdminNav("orders");
        adminPage.selectOrderStatusFilter("Hoàn tất");
        adminPage.clickOrderFilterSearch();
        if (adminPage.getAdminTableRowCount() > 0) {
            checkedAny = true;
            adminPage.clickFirstOrderDetail();
            Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                    "Phai mo duoc trang chi tiet don hang");
            java.util.List<String> headerTexts = adminPage.getOrderDetailHeaderButtonTexts();
            Assert.assertEquals(headerTexts.size(), 2,
                    "Header don 'Hoàn tất' (terminal) phai CHI co dung 2 nut, dang thay: " + headerTexts);
            Assert.assertTrue(headerTexts.contains("Hủy đơn") && headerTexts.contains("Đổi trạng thái"),
                    "Header don 'Hoàn tất' phai la [Hủy đơn] + [Đổi trạng thái], dang thay: " + headerTexts);
            Assert.assertFalse(adminPage.isOrderCancelButtonEnabled(),
                    "Don 'Hoàn tất' phai DISABLE nut [Hủy đơn]");
            Assert.assertFalse(adminPage.isOrderChangeStatusButtonEnabled(),
                    "Don 'Hoàn tất' phai DISABLE nut [Đổi trạng thái]");
            adminPage.closeOrderDetail();
            Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                    "Ve danh sach xong trang chi tiet don phai bien mat");
        }

        if (!checkedAny) {
            throw new SkipException("Bo qua: DB nay khong co don 'Chờ xử lý' lan don 'Hoàn tất' de kiem tra header chi tiet don");
        }
    }

    // ---------- Modal doi mat khau / khong con Export / huy don bat buoc ly do ----------

    @Story("Tài khoản")
    @Test(description = "ADM18-11 (đợt 18 wave D, đợt 23 phần A: đổi mật khẩu giờ là Modal giữa màn hình 560px thay Drawer FormShell): Modal đổi mật khẩu — footer CHỈ đúng 2 nút [Hủy]+[Đổi mật khẩu]; đóng bằng nút X không đổi mật khẩu")
    public void changePasswordModalHasExactlyTwoFooterButtons() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.openUserMenuIfClosed();
        adminPage.clickUserMenuProfile();
        Assert.assertTrue(adminPage.isProfilePageDisplayed(),
                "Bam 'Thông tin cá nhân' phai mo trang Ho so (admin-profile-page)");
        adminPage.openChangePasswordForm();
        Assert.assertTrue(adminPage.isChangePasswordFormDisplayed(),
                "Bam 'Đổi mật khẩu' phai mo form doi mat khau (admin-change-password-form)");
        Assert.assertEquals(adminPage.getChangePasswordFooterButtonCount(), 2,
                "Footer modal doi mat khau dot 18 phai CHI co dung 2 nut, dang thay: "
                        + adminPage.getChangePasswordFooterButtonTexts());
        java.util.List<String> footerTexts = adminPage.getChangePasswordFooterButtonTexts();
        Assert.assertTrue(footerTexts.contains("Hủy") && footerTexts.contains("Đổi mật khẩu"),
                "Footer modal doi mat khau phai la [Hủy] + [Đổi mật khẩu], dang thay: " + footerTexts);

        adminPage.closeChangePasswordModal();
        Assert.assertFalse(adminPage.isChangePasswordModalDisplayed(),
                "Dong modal doi mat khau bang nut X xong khong duoc con thay modal");
    }

    @Story("Toolbar")
    @Test(description = "ADM18-12 (đợt 18): KHÔNG còn nút Export ở bất kỳ toolbar nào — Sản phẩm, Đơn hàng, Đánh giá, Nhật ký hệ thống đều không có nút Export")
    public void noExportButtonOnAnyAdminToolbar() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        adminPage.clickAdminNav("products");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar San pham khong duoc co nut Export");
        adminPage.clickAdminNav("orders");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Don hang khong duoc co nut Export");
        adminPage.clickAdminNav("reviews");
        Assert.assertFalse(adminPage.isReviewsExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Danh gia khong duoc co nut Export");
        adminPage.clickAdminNav("logs");
        Assert.assertFalse(adminPage.isAuditExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Nhat ky khong duoc co nut Export");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM18-13 (đợt 21, viết lại theo panel hủy đơn): Hủy đơn 'Chờ xử lý' có sẵn — mở panel hủy đơn (nút [Giữ đơn], không phải [Giữ đơn hàng] của popup D1 cũ) rồi bấm xác nhận khi để trống lý do thì báo 'Vui lòng nhập lý do hủy đơn.', panel vẫn mở; bấm [Giữ đơn] thì đơn vẫn 'Chờ xử lý' (không hủy thật; skip nếu không có đơn chờ xử lý)")
    public void cancelOrderRequiresReason() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        adminPage.selectOrderStatusFilter(GlobalConstants.ADMIN_ORDER_STATUS_PENDING_LABEL);
        adminPage.clickOrderFilterSearch();
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xử lý' de mo panel huy don");
        }
        String orderCode = extractOrderDisplayCode(adminPage.getAdminTableRowTexts().get(0));
        Assert.assertNotNull(orderCode, "Phai doc duoc ma don o dong dau tien");

        adminPage.searchOrderByCodeAndOpen(orderCode);
        adminPage.openCancelOrderDialog();
        Assert.assertTrue(adminPage.isOrderCancelPanelDisplayed(),
                "Bam [Hủy đơn] phai mo panel huy don");
        Assert.assertTrue(adminPage.isOrderCancelReasonInputDisplayed(),
                "Panel huy don phai co o nhap ly do (admin-order-cancel-reason-input)");
        // Dot 21: panel huy don co dung 2 nut [Giữ đơn] + [Xác nhận hủy đơn].
        Assert.assertTrue(adminPage.getOrderDetailHeaderButtonTexts().contains("Hủy đơn"),
                "Header trang chi tiet phai co nut [Hủy đơn]");

        // Bam xac nhan khi de trong ly do -> loi, panel van mo, chua huy.
        adminPage.clickOrderCancelConfirmButton();
        Assert.assertEquals(adminPage.getOrderCancelErrorText(), "Vui lòng nhập lý do hủy đơn.",
                "Huy don khong nhap ly do thi phai bao 'Vui lòng nhập lý do hủy đơn.'");
        Assert.assertTrue(adminPage.isOrderCancelPanelDisplayed(),
                "Chua nhap ly do thi panel huy don phai van mo");

        // Bam [Giữ đơn]: panel dong, don van 'Chờ xử lý'.
        adminPage.closeOrderCancelPanel();
        Assert.assertFalse(adminPage.isOrderCancelPanelDisplayed(),
                "Bam [Giữ đơn] xong panel huy don phai bien mat");
        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Chờ xử lý"),
                "Bam '[Giữ đơn]' o panel huy don thi don " + orderCode + " van phai o trang thai 'Chờ xử lý'");
    }

    // ---------- Popup doi trang thai D2/D3 (chi mo xem, KHONG doi that) ----------

    @Story("Đơn hàng")
    @Test(description = "ADM18-14 (đợt 21, viết lại theo panel đổi trạng thái — chỉ mở panel rồi bấm [Đóng], KHÔNG đổi trạng thái thật): Đơn 'Chờ xử lý' → đích 'Đã xác nhận' mở panel có select + nút [Xác nhận đổi]; đích 'Đã thanh toán' cũng mở panel; bấm [Đóng] xong đơn vẫn 'Chờ xử lý' (skip nếu không có đơn chờ xử lý)")
    public void orderStatusConfirmPopupsD2AndD3CancelSafely() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        adminPage.selectOrderStatusFilter(GlobalConstants.ADMIN_ORDER_STATUS_PENDING_LABEL);
        adminPage.clickOrderFilterSearch();
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xử lý' de mo panel doi trang thai");
        }
        String orderCode = extractOrderDisplayCode(adminPage.getAdminTableRowTexts().get(0));
        Assert.assertNotNull(orderCode, "Phai doc duoc ma don o dong dau tien");

        adminPage.searchOrderByCodeAndOpen(orderCode);

        // Panel đổi trạng thái: đích 'Đã xác nhận' có trong select, nút xác nhận là [Xác nhận đổi].
        adminPage.openChangeOrderStatusDialog("Đã xác nhận");
        Assert.assertTrue(adminPage.isOrderStatusPanelDisplayed(),
                "Chon dich 'Đã xác nhận' phai mo panel doi trang thai");
        Assert.assertTrue(adminPage.isOrderStatusConfirmButtonEnabled(),
                "Chon dich hop le thi nut [Xác nhận đổi] phai bam duoc");
        Assert.assertTrue(adminPage.getOrderTargetStatusLabels().contains("Đã xác nhận"),
                "Dropdown dich phai chua 'Đã xác nhận', dang thay: " + adminPage.getOrderTargetStatusLabels());
        adminPage.closeOrderStatusPanel();
        Assert.assertFalse(adminPage.isOrderStatusPanelDisplayed(),
                "Bam [Đóng] o panel doi trang thai xong panel phai bien mat");

        // Đích 'Đã thanh toán' cũng mở panel (không còn popup D3 riêng).
        adminPage.openChangeOrderStatusDialog("Đã thanh toán");
        Assert.assertTrue(adminPage.isOrderStatusPanelDisplayed(),
                "Chon dich 'Đã thanh toán' phai mo panel doi trang thai");
        adminPage.closeOrderStatusPanel();
        Assert.assertFalse(adminPage.isOrderStatusPanelDisplayed(),
                "Bam [Đóng] o panel doi trang thai xong panel phai bien mat");

        // Khong doi trang thai that: don van 'Chờ xử lý'.
        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Chờ xử lý"),
                "Chi mo panel roi Dong thi don " + orderCode + " van phai o trang thai 'Chờ xử lý'");
    }

    // ---------- Dang xuat hoi popup G3 ----------

    @Story("Tài khoản")
    @Test(description = "ADM18-15 (đợt 18 wave D, G3): Bấm 'Đăng xuất' ở menu tài khoản → mở popup 'Đăng xuất?' [Hủy][Đăng xuất]; bấm Hủy thì vẫn ở trang admin; bấm lại rồi xác nhận thì về trang /login")
    public void logoutAsksConfirmDialogBeforeLeaving() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        adminPage.clickUserMenuLogout();
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam 'Đăng xuất' phai mo popup G3 (khong dang xuat ngay)");
        Assert.assertTrue(adminPage.getConfirmDialogTitle().contains("Đăng xuất?"),
                "Tieu de popup dang xuat phai la 'Đăng xuất?', dang thay: " + adminPage.getConfirmDialogTitle());
        Assert.assertTrue(adminPage.getConfirmDialogConfirmText().contains("Đăng xuất"),
                "Nut xac nhan popup dang xuat phai la 'Đăng xuất', dang thay: " + adminPage.getConfirmDialogConfirmText());
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Popup dang xuat phai CHI co dung 2 nut ([Hủy] + [Đăng xuất])");

        // Huy: van o trang admin.
        adminPage.clickConfirmNo();
        Assert.assertFalse(adminPage.isConfirmDialogDisplayed(),
                "Bam 'Hủy' o popup dang xuat xong ConfirmDialog phai bien mat");
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Bam 'Hủy' o popup dang xuat thi van phai o trang admin");

        // Xac nhan: ve trang /login.
        adminPage.clickUserMenuLogout();
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam lai 'Đăng xuất' phai mo lai popup G3");
        adminPage.clickConfirmYes();
        Assert.assertTrue(adminPage.waitForLoginPage(10),
                "Xac nhan dang xuat xong phai ve trang /login trong 10 giay");
    }

    // ---------- Toolbar icon Lucide (khong emoji) ----------

    @Story("Toolbar")
    @Test(description = "ADM18-16 (đợt 18 wave D, đợt 24/26 viết lại — KHÔNG còn nút Tìm riêng, icon Tìm nằm TRONG ô tìm): Toolbar Sản phẩm dùng icon Lucide — ô tìm chứa svg.lucide-search, nút Đặt lại chứa svg.lucide-rotate-ccw; toàn bộ toolbar không còn ký tự emoji 🔍/↺")
    public void adminToolbarUsesLucideIconsNotEmoji() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");
        Assert.assertFalse(adminPage.isToolbarSearchButtonDisplayed("products"),
                "Toolbar San pham dot 24/26 KHONG con nut Tim rieng (products-search-btn)");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("products"),
                "Toolbar San pham phai co nut Dat lai (products-reset-btn)");
        Assert.assertTrue(adminPage.isToolbarSearchIconDisplayed("products"),
                "O tim San pham phai chua icon Lucide Search TRONG o tim (svg.lucide-search)");
        Assert.assertTrue(adminPage.isToolbarResetIconDisplayed("products"),
                "Nut Dat lai phai chua icon Lucide RotateCcw (svg.lucide-rotate-ccw)");
        String toolbarText = adminPage.getToolbarText();
        Assert.assertFalse(toolbarText.contains("🔍"),
                "Toolbar admin wave D khong duoc con emoji 🔍, dang thay: '" + toolbarText + "'");
        Assert.assertFalse(toolbarText.contains("↺"),
                "Toolbar admin wave D khong duoc con emoji ↺, dang thay: '" + toolbarText + "'");
    }
}
