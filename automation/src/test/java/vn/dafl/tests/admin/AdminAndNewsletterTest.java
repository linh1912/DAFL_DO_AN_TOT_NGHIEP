package vn.dafl.tests.admin;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;

import java.util.Arrays;
import java.util.List;

/**
 * Trang quan tri /admin tren app Admin rieng localhost:3001 (dot 3, web DAFL cuoi cung).
 * Admin: chi test DOC (GET) - mo trang, doc menu, chuyen muc. KHONG doi trang thai
 * don, KHONG them/sua/xoa san pham/khuyen mai, KHONG khoa tai khoan, KHONG doi
 * mat khau admin (test doi mat khau admin tu khoi phuc co rui ro ket mat khau tam
 * neu suite dung giua chung - da loai bo khoi bo test).
 * 3 test dang ky ban tin NL-01..03 truoc day o day da chuyen sang lop web
 * vn.dafl.tests.web.NewsletterTest (dot 10) vi chung test footer cua web ban hang.
 * Dot 12: o search tren topbar + breadcrumb da bo han; tim kiem gio nam trong
 * toolbar rieng cua tung muc; form them/sua mo trong modal; bang phan trang
 * client-side 10 dong/trang (ADM-11 viet lai, ADM-12..ADM-14 moi).
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
public class AdminAndNewsletterTest extends BaseTest {

    private static final List<String> EXPECTED_ADMIN_NAV_KEYS = Arrays.asList(
            "dashboard", "orders", "products", "categories",
            "promotions", "customers", "membership", "returns"
    );

    /** Dang nhap bang tai khoan Super Admin tren app Admin rieng :3001; bo qua test
     *  neu tai khoan chua duoc seed tren DB nay (DbInitializer chi seed admin@dafl.vn
     *  khi bang Users trong). */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    @Test(description = "ADM-01: Khach chua dang nhap mo /admin -> bi chuyen ve trang dang nhap")
    public void guestOpeningAdminIsRedirectedToLogin() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "Khach mo /admin phai bi chuyen ve /login, dang o: " + getDriver().getCurrentUrl());
    }

    @Test(description = "ADM-02: Tai khoan thuong mo /admin -> trang 403 kem huong dan cap quyen Super Admin")
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

    @Test(description = "ADM-03: Super Admin mo /admin -> thay du 8 muc menu quan tri")
    public void adminSeesEightMenuItems() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        List<String> visible = adminPage.getVisibleAdminNavKeys();
        Assert.assertEquals(visible, EXPECTED_ADMIN_NAV_KEYS,
                "Menu quan tri phai du 8 muc theo dung thu tu, dang thay: " + visible);
    }

    @Test(description = "ADM-04: Super Admin mo /admin -> muc Tong quan tai duoc")
    public void adminDashboardLoads() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Muc Tong quan phai hien tieu de");
    }

    @Test(description = "ADM-05 (chi doc): Super Admin chuyen muc Don hang / San pham / Khach hang thay duoc noi dung")
    public void adminCanBrowseOrdersProductsCustomersReadOnly() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        adminPage.clickAdminNav("orders");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(), "Muc Don hang phai co o tim kiem don");

        adminPage.clickAdminNav("products");
        Assert.assertTrue(adminPage.isProductSearchDisplayed(), "Muc San pham phai co o tim kiem san pham");

        adminPage.clickAdminNav("customers");
        Assert.assertTrue(adminPage.isCustomerSearchDisplayed(), "Muc Khach hang phai co o tim kiem khach hang");
    }

    @Test(description = "ADM-06 (chi doc, dot 4): Bieu do doanh thu Admin hien (hoac bao chua co doanh thu) + 2 nut chon khoang 7/30 ngay hoat dong")
    public void adminRevenueChartLoadsAndRangeButtonsWork() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Phai o muc Tong quan");

        // 2 nut chon khoang luon hien trong muc Tong quan (mac dinh 7 ngay).
        Assert.assertTrue(adminPage.isRevenueRange7Displayed(), "Phai thay nut chon khoang 7 ngay");
        Assert.assertTrue(adminPage.isRevenueRange30Displayed(), "Phai thay nut chon khoang 30 ngay");

        // Bieu do chi ve khi co don trong khoang; DB chua co don thi hien dong bao rong.
        // Ca hai deu hop le - chi khang dinh du lieu da tai xong theo 1 trong 2 dang.
        String state7 = adminPage.getRevenueState();
        Assert.assertTrue(state7.equals("chart") || state7.equals("empty"),
                "Sau khi tai bao cao 7 ngay phai thay bieu do hoac dong 'Chưa có doanh thu', dang thay: " + state7);
        if (state7.equals("chart")) {
            Assert.assertTrue(adminPage.isRevenueChartSvgDisplayed(),
                    "Bieu do doanh thu phai ve bang SVG");
        }

        // Doi khoang sang 30 ngay: du lieu tai lai (GET), khong doi du lieu goc.
        adminPage.clickRevenueRange30();
        String state30 = adminPage.getRevenueState();
        Assert.assertTrue(state30.equals("chart") || state30.equals("empty"),
                "Sau khi doi sang 30 ngay phai thay bieu do hoac dong bao rong, dang thay: " + state30);

        // Doi lai 7 ngay van phai tai duoc.
        adminPage.clickRevenueRange7();
        String stateBack = adminPage.getRevenueState();
        Assert.assertTrue(stateBack.equals("chart") || stateBack.equals("empty"),
                "Doi lai 7 ngay phai tai duoc bao cao, dang thay: " + stateBack);
    }

    // ---------- Tim kiem / loc / Export o bang Admin (dot 8, chi doc) ----------

    @Test(description = "ADM-07: Tim kiem o muc San pham loc dung (tu khoa co -> thay dong, tu khoa la -> bao rong)")
    public void adminProductSearchFiltersRows() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        // Tu khoa co trong du lieu seed ('Mặt nạ' - san pham 'Mặt nạ giấy').
        adminPage.searchProducts("Mặt nạ");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Tim 'Mặt nạ' phai ra it nhat 1 dong san pham");
        boolean found = adminPage.getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains("Mặt nạ"));
        Assert.assertTrue(found, "Cac dong sau khi loc phai chua 'Mặt nạ'");

        // Tu khoa khong ton tai -> bang rong + dong bao rong.
        adminPage.searchProducts("zzzzzz-khong-co-san-pham");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim tu khoa la phai ra 0 dong san pham");
        Assert.assertTrue(adminPage.isEmptyProductsMessageDisplayed(),
                "Tim tu khoa la phai hien dong 'Không có sản phẩm nào.'");
    }

    @Test(description = "ADM-08: Tim kiem o muc Khach hang loc dung (email demo co -> thay dong, tu khoa la -> bao rong)")
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
                "Tim email la phai hien dong 'Không có khách hàng nào.'");
    }

    @Test(description = "ADM-09: Loc don hang theo trang thai 'Đã hủy' -> chi thay don da huy hoac bao rong")
    public void adminOrderStatusFilterShowsOnlyCancelled() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("orders");

        adminPage.selectOrderStatusFilter(GlobalConstants.ORDER_STATUS_CANCELLED);
        adminPage.clickOrderFilterSearch();

        if (adminPage.getAdminTableRowCount() == 0) {
            Assert.assertTrue(adminPage.isEmptyOrdersMessageDisplayed(),
                    "Loc 'Đã hủy' khong co don thi phai hien dong 'Không có đơn hàng nào.'");
        } else {
            boolean allCancelled = adminPage.getAdminTableRowTexts().stream()
                    .allMatch(text -> text.contains(GlobalConstants.ORDER_STATUS_CANCELLED));
            Assert.assertTrue(allCancelled,
                    "Loc 'Đã hủy' thi moi dong deu phai o trang thai 'Đã hủy'");
        }
    }

    @Test(description = "ADM-10: Nut Export o muc Don hang bam duoc, trang admin van dung yen (chi tai CSV ve, khong doi du lieu)")
    public void adminExportButtonIsClickableWithoutError() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("orders");

        Assert.assertTrue(adminPage.isExportButtonDisplayed(), "Muc Don hang phai co nut Export");
        adminPage.clickExportButton();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Bam Export xong menu quan tri phai van hien (trang khong loi)");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Bam Export xong muc Don hang phai van hien o tim kiem don");
    }

    @Test(description = "ADM-11 (dot 12, viet lai): O search tren topbar da bo — tim kiem o toolbar cua muc San pham van loc dung ('Mặt nạ' ra dong); topbar khong duoc con o tim kiem")
    public void adminToolbarSearchFiltersProductsAndTopbarSearchRemoved() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        // Topbar moi khong con o tim kiem (bo dot 12) - khang dinh no khong hien.
        Assert.assertFalse(adminPage.isTopbarSearchDisplayed(),
                "Topbar admin moi khong duoc con o tim kiem (da bo dot 12)");

        adminPage.clickAdminNav("products");
        Assert.assertTrue(adminPage.isProductSearchDisplayed(),
                "Muc San pham phai co o tim kiem rieng o toolbar");
        adminPage.searchProducts("Mặt nạ");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Tim 'Mặt nạ' bang o tim kiem cua muc San pham phai ra it nhat 1 dong san pham");
        boolean found = adminPage.getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains("Mặt nạ"));
        Assert.assertTrue(found, "Cac dong sau khi loc bang toolbar phai chua 'Mặt nạ'");
    }

    // ---------- Toolbar chuan / phan trang / form modal (dot 12, chi doc) ----------

    @Test(description = "ADM-12 (dot 12): Phan trang bang San pham — trang 1 toi da 10 dong, bam 'Sau' sang trang 2 roi 'Truoc' ve trang 1, 'Trang X / Y' doi theo (skip neu <= 10 dong)")
    public void adminProductsPaginationNextPrevChangesPage() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        int total = adminPage.getPaginationTotalRows("products");
        if (total <= 10) {
            throw new SkipException("Bo qua: muc San pham chi co " + total
                    + " dong (<= 10) nen khong co trang 2 de kiem tra phan trang");
        }
        Assert.assertTrue(adminPage.isPaginationDisplayed("products"),
                "Muc San pham co > 10 dong thi phai hien khoi phan trang");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                "Mac dinh phai o trang 1");
        Assert.assertTrue(adminPage.getAdminTableRowCount() <= 10,
                "Trang 1 chi hien toi da 10 dong");
        String firstRowPage1 = adminPage.getAdminTableRowTexts().get(0);
        Assert.assertFalse(adminPage.isPaginationPrevEnabled("products"),
                "O trang 1 nut 'Trước' phai bi tat");
        Assert.assertTrue(adminPage.isPaginationNextEnabled("products"),
                "Con trang sau thi nut 'Sau' phai bat duoc");

        adminPage.clickPaginationNext("products");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 2,
                "Bam 'Sau' phai sang trang 2");
        Assert.assertTrue(adminPage.getAdminTableRowCount() <= 10,
                "Trang 2 chi hien toi da 10 dong");
        String firstRowPage2 = adminPage.getAdminTableRowTexts().get(0);
        Assert.assertNotEquals(firstRowPage2, firstRowPage1,
                "Trang 2 phai hien cac dong khac trang 1");

        adminPage.clickPaginationPrev("products");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                "Bam 'Trước' phai ve lai trang 1");
        Assert.assertEquals(adminPage.getAdminTableRowTexts().get(0), firstRowPage1,
                "Ve trang 1 phai thay lai dong dau nhu ban dau");
    }

    @Test(description = "ADM-13 (dot 12): Bam '+ Thêm sản phẩm' o toolbar mo form trong modal (admin-product-form-modal); dong modal quay lai bang, khong tao du lieu")
    public void adminAddProductOpensFormModalAndCloseReturnsToTable() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        adminPage.openProductFormModal();
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Bam '+ Thêm sản phẩm' phai mo modal form san pham");
        Assert.assertTrue(adminPage.isProductFormDisplayed(),
                "Modal form san pham phai hien form");
        String modalText = adminPage.getProductFormModalText();
        Assert.assertTrue(modalText.contains("Thêm sản phẩm"),
                "Modal mo ra phai co tieu de 'Thêm sản phẩm', dang thay: " + modalText);

        // Dong modal bang nut X, KHONG bam Luu (khong tao du lieu).
        adminPage.closeFormModal("admin-product-form-modal");
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong modal xong khong duoc con thay modal form san pham");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Dong modal xong phai quay lai bang san pham co du lieu");
    }

    @Test(description = "ADM-14 (dot 12): Toolbar chuan muc San pham — nut '+ Thêm sản phẩm' o trai, nut icon 'Đặt lại'/'Làm mới' o phai; bam 'Đặt lại' xoa tu khoa va ve trang 1")
    public void adminProductsToolbarHasAddResetRefreshButtons() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        Assert.assertTrue(adminPage.isProductAddButtonDisplayed(),
                "Toolbar muc San pham phai co nut '+ Thêm sản phẩm' o ben trai");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("products"),
                "Toolbar muc San pham phai co nut icon 'Đặt lại' o ben phai");
        Assert.assertTrue(adminPage.isToolbarRefreshButtonDisplayed("products"),
                "Toolbar muc San pham phai co nut icon 'Làm mới' o ben phai");

        // Chuc nang nut "Dat lai": go tu khoa la (0 dong) roi bam Dat lai -> o tim
        // duoc xoa trang, bang ve day du, ve trang 1.
        adminPage.searchProducts("zzzzzz-khong-co-san-pham");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim tu khoa la phai ra 0 dong truoc khi bam 'Đặt lại'");
        adminPage.clickToolbarReset("products");
        Assert.assertTrue(adminPage.getProductSearchValue().isEmpty(),
                "Bam 'Đặt lại' phai xoa trang o tim kiem");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Bam 'Đặt lại' xong bang san pham phai hien lai du lieu");
        if (adminPage.isPaginationDisplayed("products")) {
            Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                    "Bam 'Đặt lại' phai ve trang 1");
        }

        // Nut "Dat lai" / "Lam moi" o muc Danh muc cung phai ton tai (toolbar chuan ap cho moi muc).
        adminPage.clickAdminNav("categories");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("categories"),
                "Toolbar muc Danh muc phai co nut icon 'Đặt lại'");
        Assert.assertTrue(adminPage.isToolbarRefreshButtonDisplayed("categories"),
                "Toolbar muc Danh muc phai co nut icon 'Làm mới'");
    }
}
