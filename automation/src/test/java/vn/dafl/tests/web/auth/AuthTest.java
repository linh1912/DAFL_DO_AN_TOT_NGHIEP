package vn.dafl.tests.web.auth;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageUIs.AuthModalPageUI;

/**
 * CN-01 Dang ky + CN-02 Dang nhap/dang xuat (ban chot v5).
 * Dang ky xong dang nhap duoc ngay (khong con OTP/xac thuc email);
 * dang nhap dung JWT luu o localStorage (key dafl_token);
 * loi sai thong tin dang nhap: "Email hoac mat khau khong dung" hien duoi o mat khau.
 *
 * Gop them 19 test tu AuthExtendedTest (BUOC 3, dot 15.1: SCR_01 8 + SCR_02 11 case)
 * ngay 2026-11-10 — 1 file duy nhat cho chuc nang dang nhap/dang ky (38 test).
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
    // ---------- Test bo sung — gop tu AuthExtendedTest (BUOC 3, file manual v15.1) ----------

    /** Mo form quen mat khau buoc 2 (reset-password-form) bang email hop le.
     *  Khong can ma that: form nhap ma hien ngay sau khi buoc 1 gui thanh cong. */
    private AuthModalPageObject openResetPasswordStep(String email) {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.clickForgotPasswordLink();
        Assert.assertTrue(authModal.isForgotPasswordFormDisplayed(), "Phai mo duoc form quen mat khau buoc 1");
        authModal.submitForgotPasswordEmail(email);
        Assert.assertTrue(authModal.isResetPasswordFormDisplayed(),
                "Gui email hop le xong phai sang form nhap ma buoc 2 (reset-password-form)");
        return authModal;
    }

    /** Tao mot tai khoan test moi qua UI roi dang xuat ngay; tra ve email vua tao.
     *  Dung cho luong quen mat khau de moi test co han muc gui ma rieng. */
    private String registerTestAccountAndLogout(String fullName) {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin(fullName, email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        homePage.logout();
        homePage.waitForLoggedOut();
        return email;
    }

    // ---------- SCR_01 Dang ky ----------

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_UI_003: Icon hiện/ẩn mật khẩu ở form Đăng ký — ô Mật khẩu và ô Nhập lại hoạt động độc lập")
    public void registerPasswordTogglesWorkIndependently() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.switchToRegisterTab();

        authModal.enterRegisterPassword(GlobalConstants.DEMO_PASSWORD);
        Assert.assertEquals(authModal.getRegisterPasswordInputType(), "password",
                "Mac dinh o Mat khau dang ky phai bi che dang cham");
        authModal.toggleRegisterPasswordVisibility();
        Assert.assertEquals(authModal.getRegisterPasswordInputType(), "text",
                "Bam icon lan 1 o Mat khau phai hien ro mat khau");
        authModal.toggleRegisterPasswordVisibility();
        Assert.assertEquals(authModal.getRegisterPasswordInputType(), "password",
                "Bam icon lan 2 o Mat khau phai che lai mat khau");

        // O Nhap lai hoat dong doc lap: doi trang thai cua no khong doi o Mat khau.
        authModal.enterRegisterConfirmPassword(GlobalConstants.DEMO_PASSWORD);
        Assert.assertEquals(authModal.getRegisterConfirmPasswordInputType(), "password",
                "Mac dinh o Nhap lai mat khau phai bi che dang cham");
        authModal.toggleRegisterConfirmPasswordVisibility();
        Assert.assertEquals(authModal.getRegisterConfirmPasswordInputType(), "text",
                "Bam icon o Nhap lai phai hien ro mat khau");
        Assert.assertEquals(authModal.getRegisterPasswordInputType(), "password",
                "Bam toggle o Nhap lai khong duoc doi trang thai o Mat khau");
        authModal.toggleRegisterConfirmPasswordVisibility();
        Assert.assertEquals(authModal.getRegisterConfirmPasswordInputType(), "password",
                "Bam icon lan 2 o Nhap lai phai che lai mat khau");
    }

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_UI_004: Chuyển tab Đăng nhập ↔ Đăng ký làm sạch toàn bộ lỗi cũ (lỗi đăng nhập sai biến mất cả 2 chiều)")
    public void switchingTabsClearsPreviousErrors() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs(GlobalConstants.DEMO_EMAIL, "Wrong@123");
        Assert.assertTrue(authModal.isLoginPasswordErrorDisplayed(), "Dang nhap sai phai co loi truoc khi doi tab");

        authModal.switchToRegisterTab();
        Assert.assertTrue(authModal.isRegisterFormDisplayed(), "Phai thay form Dang ky sau khi doi tab");
        Assert.assertFalse(authModal.isLoginPasswordErrorVisible(),
                "Doi sang tab Dang ky thi loi dang nhap phai bien mat");
        Assert.assertFalse(authModal.isAnyRegisterFieldErrorVisible(),
                "Form Dang ky phai sach, khong co thong bao loi cu");

        authModal.switchToLoginTab();
        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Quay lai phai thay form Dang nhap");
        Assert.assertFalse(authModal.isLoginPasswordErrorVisible(),
                "Quay lai tab Dang nhap thi loi cu phai bien mat");
    }

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_VAL_005: Bỏ trống tất cả các trường khi Đăng ký -> đủ 5 lỗi đúng nguyên văn dưới từng ô, không tạo tài khoản")
    public void registerWithAllFieldsEmptyShowsAllErrors() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("", "", "", "", "");

        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_NAME_ERROR),
                "Bo trong Ho ten phai co loi duoi o ho ten");
        Assert.assertEquals(authModal.getRegisterNameError(), "Họ tên không hợp lệ");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_EMAIL_ERROR),
                "Bo trong Email phai co loi duoi o email");
        Assert.assertEquals(authModal.getRegisterEmailError(), "Vui lòng nhập email.");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_PASSWORD_ERROR),
                "Bo trong Mat khau phai co loi duoi o mat khau");
        Assert.assertEquals(authModal.getRegisterPasswordError(), "Vui lòng nhập mật khẩu.");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_CONFIRM_PASSWORD_ERROR),
                "Bo trong Nhap lai mat khau phai co loi duoi o nhap lai");
        Assert.assertEquals(authModal.getRegisterConfirmPasswordError(), "Vui lòng nhập lại mật khẩu.");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_PHONE_ERROR),
                "Bo trong SDT phai co loi duoi o SDT");
        Assert.assertEquals(authModal.getRegisterPhoneError(), "Số điện thoại không hợp lệ");

        Assert.assertTrue(authModal.isModalStillOpen(), "Form trong thi modal van mo, chua tao tai khoan");
        Assert.assertEquals(authModal.getListWebElements(AuthModalPageUI.REGISTER_SUCCESS_MSG).size(), 0,
                "Khong duoc hien thong bao dang ky thanh cong");
    }

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_VAL_006 (ca 2): Họ tên 51 ký tự khi Đăng ký -> lỗi 'Họ tên không hợp lệ' (ca 1 tên 'A' đã có trong AuthTest.registerFieldValidationErrors)")
    public void registerWithTooLongNameShowsError() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 51; i++) {
            longName.append('A');
        }

        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register(longName.toString(), randomEmail(), randomPhone(),
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_NAME_ERROR),
                "Ho ten 51 ky tu phai co loi duoi o ho ten");
        Assert.assertEquals(authModal.getRegisterNameError(), "Họ tên không hợp lệ");
    }

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_VAL_009 (ca 1 + ca 3): Số điện thoại trống / '1987654321' không bắt đầu bằng 0 khi Đăng ký -> lỗi 'Số điện thoại không hợp lệ' (ca 2 SĐT 9 số đã có trong AuthTest.registerFieldValidationErrors)")
    public void registerWithInvalidPhoneVariantsShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        String[] invalidPhones = {"", "1987654321"};
        for (String phone : invalidPhones) {
            authModal.register("Selenium Tester", randomEmail(), phone,
                    GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
            Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_PHONE_ERROR),
                    "SĐT '" + phone + "' phai co loi duoi o SDT");
            Assert.assertEquals(authModal.getRegisterPhoneError(), "Số điện thoại không hợp lệ",
                    "SĐT '" + phone + "' phai bao dung cau loi");
        }
    }

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_VAL_016: Bỏ trống Email khi Đăng ký -> lỗi 'Vui lòng nhập email.' dưới ô Email, không tạo tài khoản")
    public void registerWithEmptyEmailShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Selenium Tester", "", randomPhone(),
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_EMAIL_ERROR),
                "Bo trong Email phai co loi duoi o email");
        Assert.assertEquals(authModal.getRegisterEmailError(), "Vui lòng nhập email.");
    }

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_VAL_017: Bỏ trống ô Nhập lại mật khẩu khi Đăng ký -> lỗi 'Vui lòng nhập lại mật khẩu.' dưới ô Nhập lại")
    public void registerWithEmptyConfirmPasswordShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Selenium Tester", randomEmail(), randomPhone(),
                GlobalConstants.DEMO_PASSWORD, "");

        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_CONFIRM_PASSWORD_ERROR),
                "Bo trong Nhap lai mat khau phai co loi duoi o nhap lai");
        Assert.assertEquals(authModal.getRegisterConfirmPasswordError(), "Vui lòng nhập lại mật khẩu.");
    }

    @Story("Đăng ký")
    @Test(description = "TC_SCR1_FUNC_019: Đăng ký lỗi trùng email -> Họ tên/Email/SĐT giữ nguyên, 2 ô mật khẩu bị xóa trống, lỗi 'Email đã được sử dụng' vẫn hiện")
    public void duplicateEmailKeepsEnteredDataExceptPasswords() {
        String name = "Nguyễn Văn An";
        // SDT lay ngau nhien thay cho 0912345678 trong case goc de tranh trung
        // voi tai khoan cua lan chay truoc (gia tri SDT khong phai muc khang dinh).
        String phone = randomPhone();

        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register(name, GlobalConstants.DEMO_EMAIL, phone,
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(AuthModalPageUI.REGISTER_EMAIL_ERROR),
                "Dang ky trung email phai co loi duoi o email");
        Assert.assertTrue(authModal.getRegisterEmailError().contains(GlobalConstants.DUPLICATE_EMAIL_MESSAGE),
                "Loi phai dung cau 'Email đã được sử dụng'");
        Assert.assertEquals(authModal.getRegisterNameValue(), name,
                "O Ho ten phai giu nguyen du lieu da nhap");
        Assert.assertEquals(authModal.getRegisterEmailValue(), GlobalConstants.DEMO_EMAIL,
                "O Email phai giu nguyen du lieu da nhap");
        Assert.assertEquals(authModal.getRegisterPhoneValue(), phone,
                "O SDT phai giu nguyen du lieu da nhap");
        Assert.assertEquals(authModal.getRegisterPasswordValue(), "",
                "O Mat khau phai bi xoa trong vi ly do bao mat");
        Assert.assertEquals(authModal.getRegisterConfirmPasswordValue(), "",
                "O Nhap lai mat khau phai bi xoa trong vi ly do bao mat");
    }

    // ---------- SCR_02 Dang nhap / Quen mat khau ----------

    @Story("Đăng nhập")
    @Test(description = "TC_SCR2_UI_002: Form Đăng nhập KHÔNG còn nút 'Dùng tài khoản Demo' và link 'Đăng nhập bằng mã OTP', nhưng CÓ link 'Quên mật khẩu?'")
    public void loginFormHasNoDemoOrOtpEntry() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Phai mo duoc form dang nhap truoc");
        Assert.assertEquals(authModal.getListWebElements(
                "xpath=//*[contains(text(),'Dùng tài khoản Demo')]").size(), 0,
                "Form dang nhap khong duoc con nut 'Dùng tài khoản Demo'");
        Assert.assertEquals(authModal.getListWebElements(
                "xpath=//*[contains(text(),'Đăng nhập bằng mã OTP')]").size(), 0,
                "Form dang nhap khong duoc con link 'Đăng nhập bằng mã OTP'");
        Assert.assertTrue(authModal.isForgotPasswordLinkDisplayed(),
                "Form dang nhap phai CO link 'Quên mật khẩu?' (forgot-password-link)");
    }

    @Story("Bảo mật phiên")
    @Test(description = "TC_SCR2_SEC_015: Truy cập /orders và /profile khi chưa đăng nhập bị chuyển về /login, đăng nhập xong quay lại đúng trang vừa truy cập")
    public void protectedPageReturnsAfterLogin() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/orders");
        sleepInSecond(2);
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "Chua dang nhap mo /orders phai ve /login");

        AuthModalPageObject authModal = new AuthModalPageObject();
        authModal.loginAs(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        new HomePageObject().waitForLoggedIn();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/orders"),
                "Dang nhap xong phai quay lai dung /orders vua truy cap");

        HomePageObject homePage = new HomePageObject();
        homePage.logout();
        homePage.waitForLoggedOut();

        getDriver().get(GlobalConstants.getAppUrl() + "/profile");
        sleepInSecond(2);
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "Chua dang nhap mo /profile phai ve /login");

        new AuthModalPageObject().loginAs(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        new HomePageObject().waitForLoggedIn();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/profile"),
                "Dang nhap xong phai quay lai dung /profile vua truy cap");
    }

    @Story("Đăng nhập")
    @Test(description = "TC_SCR2_VAL_017: Nhập 'admin' ở form Đăng nhập của SHOP -> lỗi 'Email không đúng định dạng.' dưới ô Email, không gọi đăng nhập")
    public void loginWithUsernameAdminOnShopShowsFormatError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs("admin", GlobalConstants.ADMIN_PASSWORD);

        Assert.assertTrue(authModal.isLoginEmailErrorDisplayed(),
                "Nhap 'admin' o shop phai bao loi duoi o Email");
        Assert.assertEquals(authModal.getLoginEmailError(), "Email không đúng định dạng.");
        Assert.assertTrue(authModal.isModalStillOpen(), "Modal phai con mo, chua dang nhap");
        Assert.assertTrue(getAuthToken() == null || getAuthToken().isEmpty(),
                "Khong duoc luu token khi email sai dinh dang");
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_FUNC_018: Quên mật khẩu bước 1 — email trống -> 'Vui lòng nhập email.'; email 'udw' -> 'Email không đúng định dạng.'; cả 2 đều không gọi API, không sang bước nhập mã")
    public void forgotPasswordStep1EmptyOrInvalidEmailShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.clickForgotPasswordLink();
        Assert.assertTrue(authModal.isForgotPasswordFormDisplayed(), "Phai mo duoc form quen mat khau buoc 1");

        // Ca 1: email trong.
        authModal.submitForgotPasswordEmail("");
        Assert.assertTrue(authModal.isForgotPasswordEmailErrorDisplayed(),
                "Email trong phai co loi duoi o Email o buoc 1");
        Assert.assertEquals(authModal.getForgotPasswordEmailError(), "Vui lòng nhập email.");
        Assert.assertFalse(authModal.isElementDisplayed(AuthModalPageUI.RESET_PASSWORD_FORM),
                "Email trong thi khong duoc sang buoc nhap ma");

        // Ca 2: email sai dinh dang.
        authModal.submitForgotPasswordEmail("udw");
        Assert.assertTrue(authModal.isForgotPasswordEmailErrorDisplayed(),
                "Email sai dinh dang phai co loi duoi o Email o buoc 1");
        Assert.assertEquals(authModal.getForgotPasswordEmailError(), "Email không đúng định dạng.");
        Assert.assertFalse(authModal.isElementDisplayed(AuthModalPageUI.RESET_PASSWORD_FORM),
                "Email sai dinh dang thi khong duoc sang buoc nhap ma");
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_FUNC_019: Quên mật khẩu — email không tồn tại vẫn trả câu chung 'Nếu email tồn tại, mã đặt lại đã được gửi.' và chuyển sang bước nhập mã (chống dò tài khoản)")
    public void forgotPasswordWithUnregisteredEmailShowsGenericMessage() {
        String unregisteredEmail = randomEmail();

        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.clickForgotPasswordLink();
        authModal.submitForgotPasswordEmail(unregisteredEmail);

        Assert.assertTrue(authModal.isResetPasswordFormDisplayed(),
                "Email khong ton tai van phai sang buoc nhap ma (chong do tai khoan)");
        Assert.assertTrue(authModal.isForgotPasswordNoticeDisplayed(),
                "Phai hien thong bao chung chong do tai khoan");
        String notice = authModal.getForgotPasswordNoticeText();
        Assert.assertTrue(notice.contains("Nếu email tồn tại, mã đặt lại đã được gửi."),
                "Thong bao phai dung cau chung, khong tiet lo email co ton tai hay khong. Dang thay: " + notice);
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_VAL_020: Quên mật khẩu bước 2 — mã chỉ 4 số '1234' -> lỗi 'Mã đặt lại gồm 6 chữ số.' dưới ô mã, không gọi API")
    public void resetPasswordWithShortCodeShowsError() {
        String email = registerTestAccountAndLogout("Khach Quen MK 020");
        AuthModalPageObject authModal = openResetPasswordStep(email);

        authModal.submitResetPassword("1234", "Dafl@2026", "Dafl@2026");

        Assert.assertTrue(authModal.isResetCodeErrorDisplayed(),
                "Ma 4 so phai bao loi duoi o ma dat lai");
        Assert.assertEquals(authModal.getResetCodeError(), "Mã đặt lại gồm 6 chữ số.");
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_VAL_021: Quên mật khẩu bước 2 — mật khẩu mới 'newpass1' thiếu chữ hoa và ký tự đặc biệt -> checklist chưa xanh + lỗi 'Mật khẩu chưa đáp ứng đủ các điều kiện bảo mật bên dưới.'")
    public void resetPasswordWithWeakNewPasswordShowsError() {
        String email = registerTestAccountAndLogout("Khach Quen MK 021");
        AuthModalPageObject authModal = openResetPasswordStep(email);

        authModal.submitResetPassword("123456", "newpass1", "newpass1");

        Assert.assertFalse(authModal.isResetPasswordUppercaseRuleSatisfied(),
                "Mat khau 'newpass1' thieu chu hoa nen rule chu hoa chua dat");
        Assert.assertFalse(authModal.isResetPasswordSpecialRuleSatisfied(),
                "Mat khau 'newpass1' thieu ky tu dac biet nen rule ky tu dac biet chua dat");
        Assert.assertTrue(authModal.isResetNewPasswordErrorDisplayed(),
                "Mat khau moi khong du policy phai bao loi duoi o mat khau moi");
        Assert.assertEquals(authModal.getResetNewPasswordError(),
                "Mật khẩu chưa đáp ứng đủ các điều kiện bảo mật bên dưới.");
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_VAL_022: Quên mật khẩu bước 2 — nhập lại mật khẩu mới khác mật khẩu mới -> lỗi 'Mật khẩu xác nhận không khớp' dưới ô Nhập lại")
    public void resetPasswordWithMismatchedConfirmShowsError() {
        String email = registerTestAccountAndLogout("Khach Quen MK 022");
        AuthModalPageObject authModal = openResetPasswordStep(email);

        authModal.submitResetPassword("123456", "Dafl@2026", "Dafl@2027");

        Assert.assertTrue(authModal.isResetConfirmPasswordErrorDisplayed(),
                "Xac nhan khac mat khau moi phai bao loi duoi o nhap lai mat khau moi");
        Assert.assertEquals(authModal.getResetConfirmPasswordError(), "Mật khẩu xác nhận không khớp");
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_FUNC_023: Quên mật khẩu bước 2 — mã sai '000000' -> lỗi 'Mã đặt lại không đúng hoặc đã hết hạn.' và đăng nhập bằng mật khẩu cũ vẫn được")
    public void resetPasswordWithWrongCodeShowsErrorAndOldPasswordStillWorks() {
        String fullName = "Khach Quen MK 023";
        String email = registerTestAccountAndLogout(fullName);
        AuthModalPageObject authModal = openResetPasswordStep(email);

        authModal.submitResetPassword("000000", "Dafl@2026", "Dafl@2026");

        Assert.assertTrue(authModal.isResetPasswordErrorDisplayed(),
                "Ma sai phai bao loi chung o buoc 2");
        Assert.assertEquals(authModal.getResetPasswordError(), "Mã đặt lại không đúng hoặc đã hết hạn.");

        // Mat khau cua tai khoan khong doi: dang nhap lai bang mat khau cu phai duoc.
        HomePageObject homePage = openHomePage();
        AuthModalPageObject loginModal = homePage.openLoginModal();
        loginModal.loginAs(email, GlobalConstants.DEMO_PASSWORD);
        homePage.waitForLoggedIn();
        Assert.assertEquals(homePage.getAccountDisplayName(), fullName,
                "Dang nhap bang mat khau cu phai thanh cong (mat khau khong doi sau khi nhap sai ma)");
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_FUNC_024: Quên mật khẩu — nhập sai mã 5 lần liên tiếp thì mã bị hủy: 4 lần đầu báo 'Mã đặt lại không đúng hoặc đã hết hạn.', lần thứ 5 báo 'Bạn đã nhập sai quá 5 lần. Mã đã bị hủy, vui lòng xin mã mới.'")
    public void resetPasswordWrongCodeFiveTimesDestroysCode() {
        String email = registerTestAccountAndLogout("Khach Quen MK 024");
        AuthModalPageObject authModal = openResetPasswordStep(email);

        for (int attempt = 1; attempt <= 5; attempt++) {
            authModal.submitResetPassword("000000", "Dafl@2026", "Dafl@2026");
            Assert.assertTrue(authModal.isResetPasswordErrorDisplayed(),
                    "Lan nhap sai thu " + attempt + " phai bao loi");
            String error = authModal.getResetPasswordError();
            if (attempt < 5) {
                Assert.assertEquals(error, "Mã đặt lại không đúng hoặc đã hết hạn.",
                        "Lan nhap sai thu " + attempt + " phai bao loi ma sai chung");
            } else {
                Assert.assertEquals(error, "Bạn đã nhập sai quá 5 lần. Mã đã bị hủy, vui lòng xin mã mới.",
                        "Lan nhap sai thu 5 phai bao ma bi huy");
            }
        }
    }

    @Story("Quên mật khẩu")
    @Test(description = "TC_SCR2_FUNC_027: Quên mật khẩu — tối đa 3 lần gửi lại mã trong 1 giờ (SKIP: chưa xác minh được thông báo lỗi khi vượt giới hạn)")
    public void forgotPasswordResendLimitExceedsThreePerHour() {
        throw new SkipException("Chua viet duoc: inventory §10.3 chi ghi gioi han 'toi da 3 lan gui lai / "
                + "1 gio / email' (ForgotPasswordFeatures.cs:52-55) ma KHONG ghi thong bao loi / ma HTTP "
                + "tra ve khi vuot gioi han va khong ghi testid nut gui lai — khong the assert chuoi loi "
                + "chua xac minh. Can xac minh tu code BE/FE truoc khi viet test; dong thoi xin ma 4 lan "
                + "se tieu thu han muc gui lai cua tai khoan trong 1 gio nen khong thu mo.");
    }}
