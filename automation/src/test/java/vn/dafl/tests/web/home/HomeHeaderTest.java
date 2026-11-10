package vn.dafl.tests.web.home;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;
import vn.dafl.pageUIs.HomePageUI;

import java.util.Arrays;

/**
 * Pha B dot 15 buoc 3 — case loai (c) sheet SCR_09 Trang chu (phan header:
 * topbar, logo, o tim kiem, menu tien ich, nut Quan tri, badge chuong,
 * menu tai khoan, badge gio hang). Bo sung cho HomeHeroTest.
 *
 * Luu y ve du lieu (da kiem chung tu Header.tsx v15 + BE):
 * - Tai khoan moi dang ky DUOC TAO 1 thong bao chao mung (unread) ngay khi
 *   dang ky (AuthCommands.cs) nen nhanh badge-an (unreadCount = 0) chi kiem
 *   tra duoc o trang thai KHACH (chua dang nhap), khong dung tai khoan moi.
 * - Badge chuong cua demo phu thuoc trang thai doc cua thong bao seed (1
 *   thong bao chua doc luc seed); neu da bi danh dau da doc o lan chay
 *   truoc thi test nhanh hien-so tu skip.
 */
@Feature("Trang chủ")
public class HomeHeaderTest extends BaseTest {

    /** Bo dau "/" o cuoi URL de so sanh trang chu (baseUrl co the khong co "/"). */
    private String normalizeUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Story("Header")
    @Test(description = "TC_SCR9_UI_021: Topbar header hiển thị đủ 3 câu ưu đãi")
    public void topbarShowsAllThreeMessages() {
        HomePageObject homePage = openHomePage();
        String topbarText = homePage.getTopbarText();

        for (String message : Arrays.asList(
                "MIỄN PHÍ VẬN CHUYỂN CHO ĐƠN TỪ 299.000Đ",
                "ĐỔI TRẢ TRONG 30 NGÀY",
                "HÀNG CHÍNH HÃNG 100%")) {
            Assert.assertTrue(topbarText.contains(message),
                    "Topbar phai co cau '" + message + "', dang thay: " + topbarText);
        }
    }

    @Story("Header")
    @Test(description = "TC_SCR9_FUNC_022: Bấm logo DAFL từ trang khác thì về trang chủ (/) và hiện hero + lưới sản phẩm")
    public void brandLogoNavigatesBackToHome() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        Assert.assertNotNull(productId, "Phai co san pham de mo trang chi tiet");

        catalog.openProductDetail(productId);
        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai mo duoc trang chi tiet san pham");
        Assert.assertFalse(normalizeUrl(new HomePageObject().getCurrentPageUrl()).equals(normalizeUrl(baseUrl)),
                "Dang o trang chi tiet thi URL khong duoc la trang chu");

        new HomePageObject().clickBrandLogo();
        HomePageObject homePage = new HomePageObject();
        Assert.assertEquals(normalizeUrl(homePage.getCurrentPageUrl()), normalizeUrl(baseUrl),
                "Bam logo xong phai ve trang chu (/)");
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Ve trang chu xong phai thay banner hero");
        catalog.waitForProductsLoaded();
        Assert.assertTrue(catalog.getGridProductCount() > 0, "Ve trang chu xong phai thay luoi san pham");
    }

    @Story("Header")
    @Test(description = "TC_SCR9_UI_023: Ô tìm kiếm header có placeholder đúng, hiện nút xóa khi có chữ và không có nút submit riêng")
    public void searchBoxHasPlaceholderClearButtonAndNoSubmitButton() {
        HomePageObject homePage = openHomePage();

        Assert.assertEquals(homePage.getSearchPlaceholder(), "Tìm son, serum, kem chống nắng…",
                "Placeholder o tim kiem phai dung nguyen van");
        Assert.assertEquals(homePage.getSearchBarButtonCount(), 0,
                "Khi o tim kiem trong thi vung tim kiem khong duoc co nut nao (khong co nut submit)");

        homePage.searchProduct("son");
        Assert.assertTrue(homePage.isElementDisplayed(HomePageUI.SEARCH_CLEAR_BTN),
                "Go chu vao o tim kiem thi nut xoa (search-clear-btn) phai hien");
        Assert.assertEquals(homePage.getSearchBarButtonCount(), 1,
                "Khi co chu, vung tim kiem chi duoc co dung 1 nut (nut xoa) — khong co nut submit");

        homePage.clearSearch();
        Assert.assertEquals(homePage.getSearchInputValue(), "",
                "Bam nut xoa xong o tim kiem phai tro ve trong");
        Assert.assertEquals(homePage.getSearchBarButtonCount(), 0,
                "Xoa tim kiem xong vung tim kiem lai khong duoc co nut nao");
    }

    @Story("Header")
    @Test(description = "TC_SCR9_FUNC_024: Menu \"Gian hàng\" và \"Tạp chí\" đều về trang chủ, không có trang riêng")
    public void shopsAndMagazineMenuItemsNavigateHome() {
        HomePageObject homePage = openHomePage();

        homePage.clickHeaderUtilityGianHang();
        Assert.assertEquals(normalizeUrl(homePage.getCurrentPageUrl()), normalizeUrl(baseUrl),
                "Bam 'Gian hàng' phai ve trang chu (/)");
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(),
                "Bam 'Gian hàng' xong phai thay trang chu");

        homePage.clickHeaderUtilityTapChi();
        Assert.assertEquals(normalizeUrl(homePage.getCurrentPageUrl()), normalizeUrl(baseUrl),
                "Bam 'Tạp chí' phai ve trang chu (/)");
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(),
                "Bam 'Tạp chí' xong phai thay trang chu");
    }

    @Story("Header")
    @Test(description = "TC_SCR9_FUNC_025: Nút \"Quản trị\" chỉ hiện khi isAdmin; bấm vào thì rơi vào trang 404 của web (không có route /admin)")
    public void adminNavLinkOnlyShowsForAdminAndLeadsToWebNotFoundPage() {
        HomePageObject homePage = loginAs(GlobalConstants.ADMIN_EMAIL, GlobalConstants.ADMIN_PASSWORD);

        Assert.assertTrue(homePage.isAdminNavLinkDisplayed(),
                "Dang nhap bang tai khoan admin thi header phai hien nut 'Quản trị' (admin-nav-link)");
        homePage.clickAdminNavLink();

        Assert.assertTrue(homePage.getCurrentPageUrl().contains("/admin"),
                "Bam 'Quản trị' phai vao /admin, dang thay: " + homePage.getCurrentPageUrl());
        Assert.assertTrue(homePage.isNotFoundPageDisplayed(),
                "Web khong co route /admin nen phai hien trang 404 cua web (not-found-page)");
        Assert.assertTrue(homePage.getNotFoundPageText().contains("Không tìm thấy trang"),
                "Trang 404 phai co tieu de 'Không tìm thấy trang'");
    }

    @Story("Header")
    @Test(description = "TC_SCR9_UI_026 (nhánh unreadCount = 0): Badge chuông thông báo ẩn khi không có thông báo chưa đọc (khách chưa đăng nhập)")
    public void notificationBadgeHiddenWhenNoUnreadNotifications() {
        HomePageObject homePage = openHomePage();
        Assert.assertFalse(homePage.isNotificationBadgeDisplayed(),
                "Khach chua dang nhap (unreadCount = 0) thi badge chuong (notif-badge-count) phai an");
    }

    @Story("Header")
    @Test(description = "TC_SCR9_UI_026 (nhánh unreadCount > 0): Badge chuông thông báo hiện đúng số khi tài khoản demo có thông báo chưa đọc")
    public void notificationBadgeShowsUnreadCountForDemoAccount() {
        HomePageObject homePage = loginWithDemoAccount();

        // So chua doc tai bat dong bo sau dang nhap — cho toi da 5 giay truoc
        // khi ket luan demo khong con thong bao chua doc.
        if (!homePage.waitForNotificationBadge(5)) {
            throw new SkipException("Tai khoan demo hien khong con thong bao chua doc"
                    + " (thong bao seed da bi danh dau da doc o lan chay truoc)"
                    + " — khong kiem tra duoc nhanh badge hien so");
        }
        int unreadCount = homePage.getNotificationBadgeCount();
        Assert.assertTrue(unreadCount > 0,
                "Tai khoan demo co thong bao chua doc thi badge phai hien so > 0, dang thay: " + unreadCount);
    }

    @Story("Header")
    @Test(description = "TC_SCR9_UI_027: Menu tài khoản dropdown đủ các mục ở cả 2 trạng thái chưa/đã đăng nhập")
    public void accountDropdownShowsCorrectItemsForGuestAndLoggedInUser() {
        HomePageObject homePage = openHomePage();

        String guestMenuText = homePage.getAccountDropdownText();
        Assert.assertTrue(guestMenuText.contains("Chưa đăng nhập"),
                "Chua dang nhap dropdown phai ghi 'Chưa đăng nhập', dang thay: " + guestMenuText);
        Assert.assertTrue(guestMenuText.contains("Đăng nhập / Đăng ký"),
                "Chua dang nhap dropdown phai co muc 'Đăng nhập / Đăng ký', dang thay: " + guestMenuText);
        Assert.assertFalse(guestMenuText.contains("Đăng xuất tài khoản"),
                "Chua dang nhap dropdown khong duoc co muc dang xuat, dang thay: " + guestMenuText);

        HomePageObject loggedInPage = loginWithDemoAccount();
        String displayName = loggedInPage.getAccountDisplayName();
        String userMenuText = loggedInPage.getAccountDropdownText();
        Assert.assertTrue(userMenuText.contains(displayName),
                "Da dang nhap dropdown phai hien ten tai khoan '" + displayName + "', dang thay: " + userMenuText);
        Assert.assertTrue(userMenuText.contains("Thành viên DAFL"),
                "Da dang nhap dropdown phai hien vai tro 'Thành viên DAFL', dang thay: " + userMenuText);
        for (String item : Arrays.asList(
                "Đơn hàng của tôi", "Hội viên của tôi", "Thông tin & Sổ địa chỉ", "Đăng xuất tài khoản")) {
            Assert.assertTrue(userMenuText.contains(item),
                    "Da dang nhap dropdown phai co muc '" + item + "', dang thay: " + userMenuText);
        }
        Assert.assertFalse(userMenuText.contains("Đăng nhập / Đăng ký"),
                "Da dang nhap dropdown khong duoc co muc dang nhap, dang thay: " + userMenuText);
    }

    @Story("Header")
    @Test(description = "TC_SCR9_UI_028: Badge giỏ hàng ẩn khi giỏ trống, hiện số khi có hàng; bấm nút giỏ thì vào /cart")
    public void cartBadgeHiddenWhenEmptyShowsCountAfterAddingAndOpensCart() {
        HomePageObject homePage = openHomePage();
        Assert.assertFalse(homePage.isCartBadgeDisplayed(),
                "Gio trong thi badge gio hang (cart-badge-count) phai an");

        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        Assert.assertNotNull(productId, "Phai co san pham de them vao gio");
        catalog.addToCart(productId);

        homePage.waitForCartBadgeCount(1);
        Assert.assertTrue(homePage.isCartBadgeDisplayed(),
                "Them 1 SP vao gio xong badge gio hang phai hien");
        Assert.assertEquals(homePage.getCartBadgeCount(), 1,
                "Them 1 SP vao gio xong badge phai hien so 1");

        homePage.openCart();
        Assert.assertTrue(homePage.getCurrentPageUrl().contains("/cart"),
                "Bam nut gio hang phai vao /cart, dang thay: " + homePage.getCurrentPageUrl());
    }
}
