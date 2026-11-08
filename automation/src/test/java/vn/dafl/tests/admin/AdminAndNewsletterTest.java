package vn.dafl.tests.admin;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.SiteFooterPageObject;

import java.util.Arrays;
import java.util.List;

/**
 * Trang quan tri /admin + dang ky ban tin o footer (dot 3, web DAFL cuoi cung).
 * Admin: chi test DOC (GET) - mo trang, doc menu, chuyen muc. KHONG doi trang thai
 * don, KHONG them/sua/xoa san pham/khuyen mai, KHONG khoa tai khoan, KHONG doi
 * mat khau admin (test doi mat khau admin tu khoi phuc co rui ro ket mat khau tam
 * neu suite dung giua chung - da loai bo khoi bo test).
 * Newsletter: dang ky that qua POST /api/newsletter/subscribe, luu vao DB.
 */
public class AdminAndNewsletterTest extends BaseTest {

    private static final List<String> EXPECTED_ADMIN_NAV_KEYS = Arrays.asList(
            "dashboard", "orders", "products", "categories",
            "promotions", "customers", "membership", "returns"
    );

    /** Dang nhap bang tai khoan Super Admin; bo qua test neu tai khoan chua duoc seed
     *  tren DB nay (DbInitializer chi seed admin@dafl.vn khi bang Users trong). */
    private HomePageObject loginAsAdminOrSkip() {
        HomePageObject homePage = openHomePage();
        try {
            vn.dafl.pageObjects.AuthModalPageObject authModal = homePage.openLoginModal();
            authModal.loginAs(GlobalConstants.ADMIN_EMAIL, GlobalConstants.ADMIN_PASSWORD);
            homePage.waitForLoggedIn();
        } catch (AssertionError e) {
            throw new SkipException("Bo qua: tai khoan admin " + GlobalConstants.ADMIN_EMAIL
                    + " chua ton tai/khong dang nhap duoc tren DB nay (can seed hoac cap Role='SuperAdmin' bang SQL)");
        }
        return homePage;
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
        loginWithDemoAccount();
        AdminPageObject adminPage = new AdminPageObject();
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
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        List<String> visible = adminPage.getVisibleAdminNavKeys();
        Assert.assertEquals(visible, EXPECTED_ADMIN_NAV_KEYS,
                "Menu quan tri phai du 8 muc theo dung thu tu, dang thay: " + visible);
    }

    @Test(description = "ADM-04: Super Admin mo /admin -> muc Tong quan tai duoc")
    public void adminDashboardLoads() {
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(), "Muc Tong quan phai hien tieu de");
    }

    @Test(description = "ADM-05 (chi doc): Super Admin chuyen muc Don hang / San pham / Khach hang thay duoc noi dung")
    public void adminCanBrowseOrdersProductsCustomersReadOnly() {
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
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
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
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

    @Test(description = "NL-01: Dang ky ban tin bang email moi -> hien khoi cam on (luu that vao DB)")
    public void newsletterSubscribeSuccess() {
        openHomePage();
        SiteFooterPageObject footer = new SiteFooterPageObject();
        footer.subscribeNewsletter(randomEmail());

        Assert.assertTrue(footer.isNewsletterSuccessDisplayed(), "Dang ky email moi phai hien khoi cam on");
        Assert.assertTrue(footer.getNewsletterSuccessText()
                        .contains(GlobalConstants.NEWSLETTER_SUCCESS_MESSAGE),
                "Thong diep cam on khong dung: " + footer.getNewsletterSuccessText());
    }

    @Test(description = "NL-02: Dang ky ban tin bang email sai dinh dang -> bao loi ngay, khong goi API")
    public void newsletterInvalidEmailShowsError() {
        openHomePage();
        SiteFooterPageObject footer = new SiteFooterPageObject();
        footer.subscribeNewsletter("email-khong-hop-le");

        Assert.assertTrue(footer.isNewsletterErrorDisplayed(), "Email sai dinh dang phai hien loi");
        Assert.assertTrue(footer.getNewsletterErrorText()
                        .contains(GlobalConstants.NEWSLETTER_INVALID_EMAIL_MESSAGE),
                "Thong diep loi email khong dung: " + footer.getNewsletterErrorText());
        Assert.assertFalse(footer.isNewsletterSuccessDisplayed(),
                "Email sai dinh dang khong duoc hien khoi cam on");
    }

    @Test(description = "NL-03: Dang ky lai email da dang ky -> van hien khoi cam on, khong bao loi, khong tao trung lap")
    public void newsletterDuplicateEmailShowsAlreadyRegistered() {
        String email = randomEmail();
        openHomePage();
        SiteFooterPageObject footer = new SiteFooterPageObject();
        footer.subscribeNewsletter(email);
        Assert.assertTrue(footer.isNewsletterSuccessDisplayed(), "Lan dang ky dau phai thanh cong");

        // Tai lai trang chu roi dang ky lai cung email (form chi hien lai sau khi tai lai trang).
        getDriver().get(GlobalConstants.getAppUrl() + "/");
        new HomePageObject().waitForHomePageLoaded();
        footer.subscribeNewsletter(email);

        Assert.assertTrue(footer.isNewsletterSuccessDisplayed(),
                "Dang ky lai email da co phai van hien khoi cam on (BE tra 200, khong tao trung lap)");
        Assert.assertFalse(footer.isNewsletterErrorDisplayed(),
                "Dang ky lai email da co khong duoc bao loi");
    }

    // ---------- Tim kiem / loc / Export o bang Admin (dot 8, chi doc) ----------

    @Test(description = "ADM-07: Tim kiem o muc San pham loc dung (tu khoa co -> thay dong, tu khoa la -> bao rong)")
    public void adminProductSearchFiltersRows() {
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
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
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
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
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
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
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
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

    @Test(description = "ADM-11: O search tren topbar loc that theo muc dang mo (muc San pham)")
    public void adminTopbarSearchFiltersCurrentTab() {
        loginAsAdminOrSkip();
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        Assert.assertTrue(adminPage.isTopbarSearchEnabled(),
                "O search tren topbar phai bat duoc o muc San pham");
        adminPage.topbarSearch("Mặt nạ");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Tim 'Mặt nạ' bang topbar phai ra it nhat 1 dong san pham");
        boolean found = adminPage.getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains("Mặt nạ"));
        Assert.assertTrue(found, "Cac dong sau khi loc bang topbar phai chua 'Mặt nạ'");
    }
}
