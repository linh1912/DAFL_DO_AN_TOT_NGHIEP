package vn.dafl.tests.admin.system;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageUIs.AdminPageUI;
import vn.dafl.pageUIs.AuthModalPageUI;

/**
 * Test dang nhap admin tren app Admin rieng localhost:3001 (dot 19, theo
 * mapping SCR_01): form co ban, map ten 'admin' sang admin@dafl.vn, validate
 * client, icon hien/an mat khau, trang 403 + doi tai khoan, route / va route
 * la, topbar moi khong nut Tao moi / o tim kiem / breadcrumb.
 *
 * Moi @Test chay tren mot trinh duyet MOI (BaseTest.setUp tao driver moi
 * truoc moi method) nen bat dau test nao cung o trang thai CHUA dang nhap —
 * khong can dang xuat truoc khi test luong dang nhap; helper
 * logoutToLoginPage chi dung DE dang xuat GIUA cac lan dang nhap trong cung
 * mot test (ADM19-02 lap 3 bien the ten dang nhap).
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay
 * hoac tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 * KHONG viet test cho G4 phien het han / 401 o gate / mat mang (manual-only
 * theo mapping SCR_01: TC_SCRA01_SEC_014/015, TC_SCRA01_VAL_017).
 */
@Feature("Đăng nhập quản trị")
public class AdminLoginTest extends BaseTest {

    /** Mo trang dang nhap Admin rieng va doi form hien; bo qua neu service chua chay. */
    private AuthModalPageObject openAdminLoginOrSkip() {
        AuthModalPageObject authModal = new AuthModalPageObject();
        authModal.openPageUrl(GlobalConstants.getAdminAppUrl() + "/login");
        try {
            authModal.waitForModalVisible();
        } catch (Exception e) {
            throw new SkipException("Bo qua: trang Admin rieng " + GlobalConstants.getAdminAppUrl()
                    + " chua chay (can 'docker compose up -d --build' de service admin len)");
        }
        return authModal;
    }

    /** Dang nhap bang ten/email cho truoc tren trang /login roi mo /admin. */
    private AdminPageObject loginAndOpenAdmin(String usernameOrEmail, String password) {
        AuthModalPageObject authModal = openAdminLoginOrSkip();
        authModal.loginAs(usernameOrEmail, password);
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openPageUrl(GlobalConstants.getAdminAppUrl() + "/admin");
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Dang nhap '" + usernameOrEmail + "' xong phai vao duoc menu quan tri");
        return adminPage;
    }

    /** Dang xuat (popup G3) ve trang /login — dung giua cac lan dang nhap trong cung 1 test. */
    private void logoutToLoginPage(AdminPageObject adminPage) {
        adminPage.clickUserMenuLogout();
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam 'Đăng xuất' phai mo popup G3 'Đăng xuất?'");
        adminPage.clickConfirmYes();
        Assert.assertTrue(adminPage.waitForLoginPage(10),
                "Xac nhan dang xuat xong phai ve trang /login trong 10 giay");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-01 (TC_SCRA01_UI_001): Trang đăng nhập admin chỉ có form cơ bản (ô Tên đăng nhập/email + ô Mật khẩu + nút Đăng nhập), KHÔNG có nút Google/Facebook, KHÔNG tab đăng ký, KHÔNG gợi ý tài khoản mặc định; ô mật khẩu autocomplete=new-password và không tự điền mật khẩu cũ")
    public void adminLoginPageShowsBasicFormOnly() {
        AuthModalPageObject authModal = openAdminLoginOrSkip();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Phai thay form dang nhap admin truoc");
        Assert.assertTrue(authModal.isElementDisplayed(AuthModalPageUI.LOGIN_EMAIL_INPUT),
                "Form phai co o Ten dang nhap/email (login-email-input)");
        Assert.assertTrue(authModal.isElementDisplayed(AuthModalPageUI.LOGIN_PASSWORD_INPUT),
                "Form phai co o Mat khau (login-password-input)");
        Assert.assertTrue(authModal.isElementDisplayed(AuthModalPageUI.LOGIN_SUBMIT_BTN),
                "Form phai co nut Dang nhap (login-submit-btn)");
        Assert.assertEquals(authModal.getElementText(AuthModalPageUI.LOGIN_SUBMIT_BTN), "Đăng nhập",
                "Nut submit phai ghi 'Đăng nhập'");

        Assert.assertFalse(authModal.isElementDisplayed("css=[data-testid='social-google-btn']"),
                "Trang dang nhap admin khong duoc co nut Google");
        Assert.assertFalse(authModal.isElementDisplayed("css=[data-testid='social-facebook-btn']"),
                "Trang dang nhap admin khong duoc co nut Facebook");
        Assert.assertFalse(authModal.isElementDisplayed(AuthModalPageUI.AUTH_TAB_REGISTER),
                "Trang dang nhap admin khong duoc co tab dang ky (auth-tab-register)");
        Assert.assertFalse(authModal.isElementDisplayed(AuthModalPageUI.REGISTER_FORM),
                "Trang dang nhap admin khong duoc co form dang ky (register-form)");

        String loginText = authModal.getElementText(AuthModalPageUI.AUTH_MODAL);
        Assert.assertFalse(loginText.contains("Tài khoản mặc định: admin / Admin@123"),
                "Trang dang nhap admin khong duoc goi y tai khoan mac dinh, dang thay: " + loginText);

        Assert.assertEquals(authModal.getElementAttribute(AuthModalPageUI.LOGIN_PASSWORD_INPUT, "autocomplete"),
                "new-password", "O mat khau admin phai co autocomplete='new-password'");
        Assert.assertEquals(authModal.getElementAttribute(AuthModalPageUI.LOGIN_EMAIL_INPUT, "autocomplete"),
                "off", "O ten dang nhap admin phai co autocomplete='off'");
        Assert.assertEquals(authModal.getElementValue(AuthModalPageUI.LOGIN_PASSWORD_INPUT), "",
                "O mat khau khong duoc tu dien mat khau cu khi vua mo trang dang nhap");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-02 (TC_SCRA01_FUNC_002/016): Đăng nhập bằng 'admin'/'ADMIN'/' admin ' (khoảng trắng đầu/cuối) đều map sang admin@dafl.vn và vào được /admin")
    public void adminLoginMapsAdminNameToAdminEmail() {
        for (String username : new String[]{"admin", "ADMIN", " admin "}) {
            AdminPageObject adminPage = loginAndOpenAdmin(username, GlobalConstants.ADMIN_PASSWORD);
            Assert.assertTrue(adminPage.getCurrentPageUrl().contains("/admin"),
                    "Dang nhap bang '" + username + "' xong URL phai o /admin, dang thay: "
                            + adminPage.getCurrentPageUrl());
            logoutToLoginPage(adminPage);
            AuthModalPageObject loginPage = new AuthModalPageObject();
            loginPage.waitForModalVisible();
            Assert.assertTrue(loginPage.isLoginFormDisplayed(),
                    "Dang xuat xong phai ve lai form dang nhap truoc khi thu bien the tiep theo");
        }
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-03 (TC_SCRA01_FUNC_003): Đăng nhập bằng email đầy đủ admin@dafl.vn thì vào /admin; bấm F5 (tải lại trang) vẫn ở /admin, menu quản trị vẫn hiện (JWT còn hiệu lực)")
    public void adminLoginWithFullEmailKeepsSessionAfterReload() {
        AdminPageObject adminPage = loginAndOpenAdmin(GlobalConstants.ADMIN_EMAIL, GlobalConstants.ADMIN_PASSWORD);
        Assert.assertTrue(adminPage.getCurrentPageUrl().contains("/admin"),
                "Dang nhap bang email day du xong URL phai o /admin");

        String currentUrl = adminPage.getCurrentPageUrl();
        adminPage.openPageUrl(currentUrl); // tai lai trang (F5)
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.getCurrentPageUrl().contains("/admin"),
                "Tai lai trang xong van phai o /admin (JWT con hieu luc), dang thay: "
                        + adminPage.getCurrentPageUrl());
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Tai lai trang xong menu quan tri van phai hien");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-04 (TC_SCRA01_VAL_004/005/006): Validate client ở form đăng nhập admin — để trống email thì 'Email không được để trống.', nhập 'admin@dafl' thì 'Email không đúng định dạng.', để trống mật khẩu thì 'Mật khẩu không được để trống.'; cả 3 trường hợp đều không chuyển trang")
    public void adminLoginClientValidationErrors() {
        AuthModalPageObject authModal = openAdminLoginOrSkip();

        // (1) De trong email.
        authModal.sendKeyToElement(AuthModalPageUI.LOGIN_EMAIL_INPUT, "");
        authModal.enterLoginPassword(GlobalConstants.ADMIN_PASSWORD);
        authModal.submitLogin();
        Assert.assertEquals(authModal.getLoginEmailError(), "Email không được để trống.",
                "De trong email thi phai bao 'Email không được để trống.', dang thay: "
                        + authModal.getLoginEmailError());
        Assert.assertTrue(authModal.getCurrentPageUrl().contains("/login"),
                "Loi validate thi khong duoc chuyen trang");
        Assert.assertTrue(authModal.isLoginFormDisplayed(),
                "Loi validate thi form dang nhap van phai hien");

        // (2) Email sai dinh dang.
        authModal.sendKeyToElement(AuthModalPageUI.LOGIN_EMAIL_INPUT, "admin@dafl");
        authModal.enterLoginPassword(GlobalConstants.ADMIN_PASSWORD);
        authModal.submitLogin();
        Assert.assertEquals(authModal.getLoginEmailError(), "Email không đúng định dạng.",
                "Nhap 'admin@dafl' thi phai bao 'Email không đúng định dạng.', dang thay: "
                        + authModal.getLoginEmailError());
        Assert.assertTrue(authModal.getCurrentPageUrl().contains("/login"),
                "Loi validate thi khong duoc chuyen trang");

        // (3) De trong mat khau.
        authModal.sendKeyToElement(AuthModalPageUI.LOGIN_EMAIL_INPUT, GlobalConstants.ADMIN_EMAIL);
        authModal.enterLoginPassword("");
        authModal.submitLogin();
        Assert.assertEquals(authModal.getLoginPasswordError(), "Mật khẩu không được để trống.",
                "De trong mat khau thi phai bao 'Mật khẩu không được để trống.', dang thay: "
                        + authModal.getLoginPasswordError());
        Assert.assertTrue(authModal.getCurrentPageUrl().contains("/login"),
                "Loi validate thi khong duoc chuyen trang");
        Assert.assertTrue(authModal.isLoginFormDisplayed(),
                "Loi validate thi form dang nhap van phai hien");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-05 (TC_SCRA01_VAL_007): Đăng nhập sai mật khẩu thì hiện đúng 1 thông điệp chung 'Email hoặc mật khẩu không đúng' dưới ô Mật khẩu, không có banner lỗi trùng, vẫn ở /login")
    public void adminLoginWrongPasswordShowsSingleGenericError() {
        AuthModalPageObject authModal = openAdminLoginOrSkip();
        authModal.loginAs("admin", "SaiMatKhau@1");

        Assert.assertTrue(authModal.isLoginPasswordErrorDisplayed(),
                "Sai mat khau thi phai hien loi duoi o Mat khau");
        Assert.assertEquals(authModal.getLoginPasswordError(), "Email hoặc mật khẩu không đúng",
                "Sai mat khau thi phai bao dung 1 thong diep chung 'Email hoặc mật khẩu không đúng', dang thay: "
                        + authModal.getLoginPasswordError());
        Assert.assertFalse(authModal.isLoginEmailErrorDisplayed(),
                "Sai mat khau thi KHONG duoc hien loi duoi o email (chi 1 cho duy nhat)");
        Assert.assertFalse(authModal.isElementDisplayed(AuthModalPageUI.AUTH_ERROR_MSG),
                "Sai mat khau thi KHONG duoc co banner loi trung (auth-error-msg)");
        Assert.assertTrue(authModal.getCurrentPageUrl().contains("/login"),
                "Sai mat khau thi van phai o /login");
        Assert.assertTrue(authModal.isLoginFormDisplayed(),
                "Sai mat khau thi form dang nhap van phai hien");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-06 (TC_SCRA01_UI_008): Icon hiện/ẩn mật khẩu ở form đăng nhập admin — mặc định ô mật khẩu type=password; bấm icon mắt (toggle-login-password-btn) thì thành type=text, bấm lại thì về type=password")
    public void adminLoginPasswordVisibilityToggle() {
        AuthModalPageObject authModal = openAdminLoginOrSkip();
        authModal.enterLoginPassword(GlobalConstants.ADMIN_PASSWORD);

        Assert.assertEquals(authModal.getLoginPasswordInputType(), "password",
                "Mac dinh o mat khau phai la type=password");
        authModal.toggleLoginPasswordVisibility();
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "text",
                "Bam icon mat 1 lan thi o mat khau phai thanh type=text");
        authModal.toggleLoginPasswordVisibility();
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "password",
                "Bam icon mat lan nua thi o mat khau phai ve type=password");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-07 (TC_SCRA01_FUNC_011): Trang 403 hiện email + vai trò tài khoản, hướng dẫn cấp quyền Super Admin, nút 'Về trang chủ' và 'Đăng nhập tài khoản khác'; bấm 'Đăng nhập tài khoản khác' thì xóa token và về /login")
    public void forbiddenPageSwitchAccountReturnsToLogin() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsUserOnAdminAppOrSkip(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        adminPage.openPageUrl(GlobalConstants.getAdminAppUrl() + "/admin");
        adminPage.waitForAdminGateResolved();

        Assert.assertTrue(adminPage.isForbiddenDisplayed(),
                "Tai khoan thuong mo /admin phai thay trang 403");
        String forbiddenText = adminPage.getForbiddenText();
        Assert.assertTrue(forbiddenText.contains(GlobalConstants.DEMO_EMAIL),
                "Trang 403 phai hien email tai khoan dang dang nhap, dang thay: " + forbiddenText);
        Assert.assertTrue(forbiddenText.contains("Khách hàng"),
                "Trang 403 phai hien vai tro 'Khách hàng' cua tai khoan, dang thay: " + forbiddenText);
        Assert.assertTrue(forbiddenText.contains("Super Admin"),
                "Trang 403 phai co huong dan cap quyen Super Admin, dang thay: " + forbiddenText);
        Assert.assertTrue(forbiddenText.contains("Về trang chủ"),
                "Trang 403 phai co nut 'Về trang chủ', dang thay: " + forbiddenText);
        Assert.assertTrue(forbiddenText.contains("Đăng nhập tài khoản khác"),
                "Trang 403 phai co nut 'Đăng nhập tài khoản khác', dang thay: " + forbiddenText);

        adminPage.clickForbiddenSwitchAccountButton();
        Assert.assertTrue(adminPage.waitForLoginPage(10),
                "Bam 'Đăng nhập tài khoản khác' xong phai ve trang /login trong 10 giay");
        AuthModalPageObject loginPage = new AuthModalPageObject();
        loginPage.waitForModalVisible();
        Assert.assertTrue(loginPage.isLoginFormDisplayed(),
                "Bam 'Đăng nhập tài khoản khác' xong phai thay lai form dang nhap");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-08 (TC_SCRA01_FUNC_012): Mở / thì chuyển sang /admin; mở /xyz thì chuyển về /admin; chỉ hiện trang Admin (không header/banner/footer shop), title 'DAFL Admin — Quản trị'")
    public void adminRootAndUnknownRoutesRedirectToAdmin() {
        AdminPageObject adminPage = loginAndOpenAdmin("admin", GlobalConstants.ADMIN_PASSWORD);

        adminPage.openPageUrl(GlobalConstants.getAdminAppUrl() + "/");
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.getCurrentPageUrl().contains("/admin"),
                "Mo / phai chuyen sang /admin, dang thay: " + adminPage.getCurrentPageUrl());
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Mo / xong phai thay menu quan tri");

        adminPage.openPageUrl(GlobalConstants.getAdminAppUrl() + "/xyz");
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.getCurrentPageUrl().contains("/admin"),
                "Mo /xyz phai chuyen ve /admin, dang thay: " + adminPage.getCurrentPageUrl());
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Mo /xyz xong phai thay menu quan tri");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PAGE),
                "Chi duoc hien trang Admin (admin-page)");
        Assert.assertEquals(adminPage.getPageTitle(), "DAFL Admin — Quản trị",
                "Title trang admin phai la 'DAFL Admin — Quản trị', dang thay: " + adminPage.getPageTitle());
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM19-09 (TC_SCRA01_UI_018 + TC_SCRA02_UI_065): Topbar admin (kiểm ở mục Tổng quan) chỉ có nút thu gọn menu + chuông + chip tài khoản ở góc phải; KHÔNG có nút 'Tạo mới', KHÔNG ô tìm kiếm, KHÔNG breadcrumb; tiêu đề mục là 'Tổng quan'")
    public void adminTopbarHasNoCreateSearchOrBreadcrumb() {
        AdminPageObject adminPage = loginAndOpenAdmin("admin", GlobalConstants.ADMIN_PASSWORD);
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(),
                "Sau khi dang nhap phai o muc Tong quan (h1 'Tổng quan')");

        Assert.assertTrue(adminPage.isElementDisplayed(
                        "xpath=//header[contains(@class,'sa-topbar')]//button[@aria-label='Thu gọn / mở menu']"),
                "Topbar phai co nut thu gon menu");
        Assert.assertTrue(adminPage.isElementDisplayed(
                        "xpath=//header[contains(@class,'sa-topbar')]//button[@aria-label='Thông báo']"),
                "Topbar phai co chuong thong bao");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_USER_MENU_BTN),
                "Topbar phai co chip tai khoan (admin-user-menu-btn)");

        Assert.assertFalse(adminPage.isElementDisplayed(
                        "xpath=//header[contains(@class,'sa-topbar')]//button[contains(.,'Tạo mới')]"),
                "Topbar admin khong duoc co nut 'Tạo mới'");
        Assert.assertFalse(adminPage.isTopbarSearchDisplayed(),
                "Topbar admin khong duoc co o tim kiem");
        Assert.assertFalse(adminPage.isElementDisplayed("css=.sa-breadcrumb"),
                "Trang admin khong duoc co breadcrumb (.sa-breadcrumb)");
        Assert.assertFalse(adminPage.isElementDisplayed("css=[class*='breadcrumb']"),
                "Trang admin khong duoc co phan tu breadcrumb nao");
    }
}
