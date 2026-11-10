package vn.dafl.tests.web.auth;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.HomePageObject;

/**
 * CN-01 Dang ky + CN-02 Dang nhap/dang xuat (ban chot v5).
 * Dang ky xong dang nhap duoc ngay (khong con OTP/xac thuc email);
 * dang nhap dung JWT luu o localStorage (key dafl_token);
 * loi sai thong tin dang nhap: "Email hoac mat khau khong dung" hien duoi o mat khau.
 */
@Feature("Đăng nhập và đăng ký")
public class AuthTest extends BaseTest {

    @Story("Đăng nhập")
    @Test(description = "CN-02 TC-CN02-01: Đăng nhập thành công bằng tài khoản demo (dữ liệu seed của SUT)")
    public void loginWithDemoCredentialsSuccessfully() {
        Assert.assertEquals(loginWithDemoAccount().getAccountDisplayName(), GlobalConstants.DEMO_FULL_NAME);
        Assert.assertTrue(getAuthToken() != null && !getAuthToken().isEmpty(), "Token JWT phai duoc luu o localStorage sau dang nhap");
    }

    @Story("Đăng nhập")
    @Test(description = "CN-02 E1: Đăng nhập sai mật khẩu -> lỗi 'Email hoặc mật khẩu không đúng' dưới ô mật khẩu")
    public void loginWithWrongPasswordShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs(GlobalConstants.DEMO_EMAIL, "WrongPassword123!");

        Assert.assertTrue(authModal.isLoginPasswordErrorDisplayed(), "Thong bao loi phai xuat hien duoi o mat khau");
        Assert.assertEquals(authModal.getLoginPasswordError(), GlobalConstants.LOGIN_ERROR_MESSAGE);
        Assert.assertTrue(authModal.isModalStillOpen(), "Modal phai con mo sau khi dang nhap that bai");
        Assert.assertTrue(getAuthToken() == null || getAuthToken().isEmpty(), "Khong duoc luu token khi dang nhap that bai");
    }

    @Story("Đăng xuất")
    @Test(description = "CN-02: Đăng xuất thành công, menu tài khoản trở về trạng thái chưa đăng nhập")
    public void logoutSuccessfully() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.logout();
        homePage.waitForLoggedOut();
        Assert.assertEquals(homePage.getAccountDisplayName(), "Đăng nhập");
        Assert.assertTrue(getAuthToken() == null || getAuthToken().isEmpty(), "Token phai bi xoa khoi localStorage sau dang xuat");
    }

    @Story("Đăng xuất")
    @Test(description = "TC-CN02-04 (CN-02.3): Đăng xuất xong bấm Back của trình duyệt không thấy lại trang riêng tư")
    public void logoutThenBackDoesNotRevealPrivatePage() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.openOrdersPage();
        homePage.logout();
        homePage.waitForLoggedOut();

        getDriver().navigate().back();
        sleepInSecond(2);

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login")
                        || !isTextVisibleOnPage(GlobalConstants.DEMO_FULL_NAME), "Sau dang xuat + Back, trang rieng tu khong duoc hien (bi day ve dang nhap)");
    }

    @Story("Bảo mật phiên")
    @Test(description = "TC-CN02-03 (CN-02.3): Chưa đăng nhập truy cập thẳng /orders và /profile bị chuyển về đăng nhập")
    public void protectedPagesRedirectToLoginWhenLoggedOut() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/orders");
        sleepInSecond(2);
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"), "/orders khi chua dang nhap phai ve /login");

        getDriver().get(GlobalConstants.getAppUrl() + "/profile");
        sleepInSecond(2);
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"), "/profile khi chua dang nhap phai ve /login");
    }

    @Story("Bảo mật phiên")
    @Test(description = "TC-CN02-05 (CN-02 E3): Token hết hạn/bị sửa -> bị đẩy về đăng nhập kèm thông báo phiên hết hạn")
    public void expiredTokenRedirectsToLoginWithNotice() {
        loginWithDemoAccount();
        corruptAuthToken();

        getDriver().get(GlobalConstants.getAppUrl() + "/orders");
        sleepInSecond(2);

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"), "Phai bi chuyen ve trang dang nhap");
        AuthModalPageObject authModal = new AuthModalPageObject();
        Assert.assertTrue(authModal.isSessionExpiredNoticeDisplayed(), "Phai hien thong bao phien het han");
    }

    @Story("Đăng ký")
    @Test(description = "CN-01 TC-CN01-01: Đăng ký tài khoản mới thành công rồi đăng nhập ngay được (không còn OTP)")
    public void registerNewAccountThenLogin() {
        String name = "Selenium Tester";
        String email = randomEmail();
        String phone = randomPhone();
        String password = GlobalConstants.DEMO_PASSWORD;

        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.register(name, email, phone, password, password);

        Assert.assertTrue(authModal.isRegisterSuccessDisplayed(), "Phai hien thong bao dang ky thanh cong");
        Assert.assertTrue(authModal.getRegisterSuccessMessage().contains(GlobalConstants.REGISTER_SUCCESS_MESSAGE), "Noi dung thong bao dang ky thanh cong");
        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Sau dang ky phai tu dong chuyen ve tab dang nhap");

        authModal.loginAs(email, password);
        homePage.waitForLoggedIn();
        Assert.assertEquals(homePage.getAccountDisplayName(), name);
    }

    @Story("Đăng ký")
    @Test(description = "TC-CN01-02: Đăng ký thiếu/sai từng trường -> thông báo lỗi đúng ngay dưới ô nhập")
    public void registerFieldValidationErrors() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.switchToRegisterTab();

        // (a) Ten qua ngan (< 2 ky tu)
        authModal.register("A", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_NAME_ERROR), "Ten ngan phai co loi duoi o ho ten");

        // (b) Email sai dinh dang
        authModal.register("Selenium Tester", "khong-phai-email", randomPhone(), GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_EMAIL_ERROR), "Email sai dinh dang phai co loi duoi o email");

        // (c) SDT sai dinh dang (9 so)
        authModal.register("Selenium Tester", randomEmail(), "091234567", GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PHONE_ERROR), "SDT 9 so phai co loi duoi o SDT");

        Assert.assertTrue(authModal.isModalStillOpen(), "Dang ky khong hop le thi modal van mo, chua dang nhap");
    }

    @Story("Đăng ký")
    @Test(description = "TC-CN01-03: Checklist 4 quy tắc mật khẩu chuyển xanh dần theo từng điều kiện đạt được")
    public void registerPasswordPolicyChecklist() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.switchToRegisterTab();

        authModal.enterRegisterPassword("Abc1");
        Assert.assertTrue(!authModal.isLengthRuleSatisfied(), "Chua du 8 ky tu thi quy tac do dai chua dat");
        Assert.assertTrue(authModal.isUppercaseRuleSatisfied(), "Co chu hoa thi quy tac chu hoa dat");
        Assert.assertTrue(authModal.isDigitRuleSatisfied(), "Co chu so thi quy tac chu so dat");
        Assert.assertTrue(!authModal.isSpecialRuleSatisfied(), "Chua co ky tu dac biet thi quy tac ky tu dac biet chua dat");

        authModal.enterRegisterPassword(GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isLengthRuleSatisfied() && authModal.isUppercaseRuleSatisfied()
                        && authModal.isDigitRuleSatisfied() && authModal.isSpecialRuleSatisfied(), "Mat khau dat policy thi ca 4 quy tac deu xanh");
    }

    @Story("Đăng ký")
    @Test(description = "CN-01 E2: Đăng ký trùng email đã tồn tại -> 'Email đã được sử dụng'")
    public void registerDuplicateEmailShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Nguoi Dung Trung Email", GlobalConstants.DEMO_EMAIL, randomPhone(),
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_EMAIL_ERROR), "Loi trung email phai hien duoi o email");
        Assert.assertTrue(authModal.getRegisterEmailError().contains(GlobalConstants.DUPLICATE_EMAIL_MESSAGE), "Noi dung loi trung email");
    }

    @Story("Đăng ký")
    @Test(description = "CN-01 E3: Đăng ký trùng SĐT đã tồn tại -> 'Số điện thoại đã được sử dụng'")
    public void registerDuplicatePhoneShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Nguoi Dung Trung SDT", randomEmail(), GlobalConstants.DEMO_PHONE,
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PHONE_ERROR), "Loi trung SDT phai hien duoi o SDT");
        Assert.assertTrue(authModal.getRegisterPhoneError().contains(GlobalConstants.DUPLICATE_PHONE_MESSAGE), "Noi dung loi trung SDT");
    }

    @Story("Đăng ký")
    @Test(description = "Đăng ký bằng email sai định dạng -> lỗi ngay dưới ô email, không tạo tài khoản")
    public void registerWithInvalidEmailFormat() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Selenium Tester", "email@khong-co-domain", randomPhone(),
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_EMAIL_ERROR), "Loi dinh dang email phai hien");
        Assert.assertTrue(!authModal.isRegisterSuccessDisplayed(), "Thong bao dang ky thanh cong khong duoc xuat hien");
    }

    @Story("Đăng nhập")
    @Test(description = "Chuyển qua lại giữa tab Đăng nhập và Đăng ký trong Auth Modal")
    public void switchBetweenLoginAndRegisterTabs() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Mo modal thi mac dinh o tab Dang nhap");
        authModal.switchToRegisterTab();
        Assert.assertTrue(authModal.isRegisterFormDisplayed(), "Sau khi chuyen tab phai thay form Dang ky");
        authModal.switchToLoginTab();
        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Chuyen lai phai thay form Dang nhap");
    }

    @Story("Đăng nhập")
    @Test(description = "Đóng Auth Modal rồi mở lại vẫn hiển thị đúng form đăng nhập")
    public void closeModalAndReopen() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.closeModal();

        AuthModalPageObject reopenedModal = homePage.openLoginModal();
        Assert.assertTrue(reopenedModal.isLoginFormDisplayed(), "Mo lai modal phai thay form dang nhap");
    }

    @Story("Đăng nhập")
    @Test(description = "Nút con mắt ẩn/hiện mật khẩu trên form đăng nhập")
    public void toggleLoginPasswordVisibility() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.enterLoginPassword(GlobalConstants.DEMO_PASSWORD);
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "password");
        authModal.toggleLoginPasswordVisibility();
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "text");
        authModal.toggleLoginPasswordVisibility();
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "password");
    }

    @Story("Đăng nhập")
    @Test(description = "Đăng nhập sai một lần rồi nhập đúng ngay trong cùng phiên modal thì thành công")
    public void loginFailsThenSucceedsInSameModalSession() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs(GlobalConstants.DEMO_EMAIL, "WrongPassword123!");
        Assert.assertTrue(authModal.isLoginPasswordErrorDisplayed(), "Lan dang nhap sai phai bao loi");

        authModal.loginAs(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        homePage.waitForLoggedIn();
        Assert.assertEquals(homePage.getAccountDisplayName(), GlobalConstants.DEMO_FULL_NAME);
    }

    @Story("Đăng nhập")
    @Test(description = "CN-02 (đợt 8): Đăng nhập quản trị bằng tên 'admin' (không cần gõ email đầy đủ) ở trang Admin riêng localhost:3001")
    public void adminLoginWithUsernameAdminOnAdminOnlyApp() {
        // Trang Admin rieng chay service dafl-admin o cong 3001 (build VITE_ADMIN_ONLY=true);
        // chi o day o dang nhap moi map 'admin' -> 'admin@dafl.vn'. Neu service chua chay
        // thi bo qua test co ly do ro rang thay vi fail.
        HomePageObject homePage = new HomePageObject();
        homePage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/login");
        AuthModalPageObject authModal = new AuthModalPageObject();
        try {
            authModal.waitForModalVisible();
        } catch (Exception e) {
            throw new org.testng.SkipException("Bo qua: trang Admin rieng " + GlobalConstants.ADMIN_APP_URL
                    + " chua chay (can 'docker compose up -d --build' de service dafl-admin len cong 3001)");
        }
        Assert.assertEquals(authModal.getModalTitle(), "Đăng nhập quản trị",
                "Trang dang nhap Admin rieng phai co tieu de 'Đăng nhập quản trị'");

        authModal.loginAs("admin", GlobalConstants.ADMIN_PASSWORD);
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.openAdminPage();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Dang nhap bang ten 'admin' phai vao duoc trang Admin voi menu quan tri");
    }

    @Story("Đăng nhập")
    @Test(description = "CN-02 (đợt 8): Form đăng nhập ở shop KHÔNG còn nút Google/Facebook (đã bỏ hẳn đăng nhập mạng xã hội)")
    public void shopLoginFormHasNoSocialLoginButtons() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Phai mo duoc form dang nhap truoc");
        Assert.assertEquals(authModal.getListWebElements(
                "xpath=//button[contains(.,'Google')]").size(), 0,
                "Form dang nhap khong duoc con nut Google");
        Assert.assertEquals(authModal.getListWebElements(
                "xpath=//button[contains(.,'Facebook')]").size(), 0,
                "Form dang nhap khong duoc con nut Facebook");
        Assert.assertEquals(authModal.getListWebElements(
                "xpath=//*[contains(text(),'Đăng nhập bằng Google')]").size(), 0,
                "Form dang nhap khong duoc con dong chu 'Đăng nhập bằng Google'");
        Assert.assertEquals(authModal.getListWebElements(
                "xpath=//*[contains(text(),'Đăng nhập bằng Facebook')]").size(), 0,
                "Form dang nhap khong duoc con dong chu 'Đăng nhập bằng Facebook'");
    }

    @Story("Quên mật khẩu")
    @Test(description = "FP-01 (đợt 13, mục 1): Link 'Quên mật khẩu' hiện ở form đăng nhập shop; bấm vào mở form xin mã, quay lại được (chỉ kiểm UI, không gửi mail thật)")
    public void shopLoginShowsForgotPasswordLinkAndOpensForm() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Phai mo duoc form dang nhap truoc");
        Assert.assertTrue(authModal.isForgotPasswordLinkDisplayed(),
                "Form dang nhap shop phai co link 'Quên mật khẩu?' (forgot-password-link)");
        Assert.assertEquals(authModal.getForgotPasswordLinkText(), "Quên mật khẩu?",
                "Link quen mat khau phai ghi dung 'Quên mật khẩu?'");

        authModal.clickForgotPasswordLink();
        Assert.assertTrue(authModal.isForgotPasswordFormDisplayed(),
                "Bam link phai mo form quen mat khau (forgot-password-form + o email + nut gui ma)");

        authModal.clickForgotPasswordBackButton();
        Assert.assertTrue(authModal.isLoginFormDisplayed(),
                "Bam 'Quay lai dang nhap' phai ve lai form dang nhap");
    }
}
