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
 * Menu tai khoan + trang Ho so admin tren app Admin rieng localhost:3001
 * (dot 17, moi): chip tai khoan o topbar -> dropdown (Thong tin ca nhan /
 * Dang xuat) -> trang Ho so (the dau trang, Thong tin ca nhan, Doi mat khau).
 *
 * CANH BAO DOI MAT KHAU (ADM17-13): test doi mat khau admin THAT sang mat khau
 * tam roi DOI LAI ve Admin@123 NGAY TRONG TEST (buoc 4) + khoi phuc khan cap
 * trong finally neu test dung giua chung. Bien currentPassword theo doi mat
 * khau nao dang co hieu luc de finally biet phai dang nhap bang mat khau nao
 * de doi lai. Neu ca buoc doi lai lan 2 + finally deu that bai (vd mat dien /
 * suite dung giua chung), mat khau admin se ket o mat khau tam 'Admin@456' —
 * khi do dang nhap admin bang mat khau tam do roi doi lai ve 'Admin@123' thu
 * cong tren trang Ho so (xem TEST-MAPPING.md muc dot 17). Cac test khac trong
 * suite dang nhap bang Admin@123 nen CHI chay ADM17-13 khi chap nhan rui ro
 * nay; khong chay song song voi test admin khac.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Hồ sơ quản trị")
public class AdminProfileTest extends BaseTest {

    /** Mat khau tam dung trong test doi mat khau (hop policy BE: >= 8 ky tu +
     *  chu hoa + chu so + ky tu dac biet, khac mat khau cu). */
    private static final String TEMP_PASSWORD = "Admin@456";

    /** Dang nhap Super Admin tren app Admin rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Mo trang Ho so tu menu tai khoan (dang o trang /admin). */
    private void openProfilePage(AdminPageObject adminPage) {
        adminPage.openUserMenuIfClosed();
        adminPage.clickUserMenuProfile();
        Assert.assertTrue(adminPage.isProfilePageDisplayed(),
                "Bam 'Thông tin cá nhân' phai mo trang Ho so (admin-profile-page)");
    }

    @Story("Tài khoản")
    @Test(description = "ADM17-12 (đợt 17): Menu tài khoản — bấm chip → dropdown hiện 2 mục ('Thông tin cá nhân'/'Đăng xuất'); bấm 'Thông tin cá nhân' → trang Hồ sơ hiện đúng thông tin admin; sửa họ tên + lưu → toast + tên mới hiện ở topbar; khôi phục tên gốc trong finally")
    public void adminUserMenuOpensProfileAndEditNameUpdatesTopbar() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String originalName = null;
        boolean nameChanged = false;
        try {
            adminPage.clickUserMenuButton();
            Assert.assertTrue(adminPage.isUserMenuDisplayed(),
                    "Bam chip tai khoan phai mo dropdown menu (admin-user-menu)");
            String menuText = adminPage.getUserMenuText();
            Assert.assertTrue(menuText.contains("Thông tin cá nhân"),
                    "Dropdown menu tai khoan phai co muc 'Thông tin cá nhân', dang thay: " + menuText);
            Assert.assertTrue(menuText.contains("Đăng xuất"),
                    "Dropdown menu tai khoan phai co muc 'Đăng xuất', dang thay: " + menuText);

            adminPage.clickUserMenuProfile();
            Assert.assertTrue(adminPage.isProfilePageDisplayed(),
                    "Bam 'Thông tin cá nhân' phai mo trang Ho so (admin-profile-page)");
            String profileText = adminPage.getProfilePageText();
            Assert.assertTrue(profileText.contains(GlobalConstants.ADMIN_EMAIL),
                    "Trang Ho so phai hien email admin '" + GlobalConstants.ADMIN_EMAIL + "', dang thay: " + profileText);
            originalName = adminPage.getProfileHeaderName();
            Assert.assertFalse(originalName == null || originalName.trim().isEmpty(),
                    "Trang Ho so phai hien ho ten admin o the dau trang");

            String newName = originalName + " Test";
            adminPage.clickProfileEditButton();
            Assert.assertTrue(adminPage.isProfileFormDisplayed(),
                    "Bam 'Sửa thông tin' phai mo form sua ho so (admin-profile-form)");
            Assert.assertTrue(adminPage.isProfileEmailInputDisabled(),
                    "O Email trong form ho so phai bi disable (chi xem, khong sua)");
            adminPage.setProfileName(newName);
            adminPage.clickProfileSaveButton();
            nameChanged = true;
            Assert.assertTrue(adminPage.isToastDisplayed(),
                    "Luu ho so xong phai hien toast thanh cong (admin-toast)");
            Assert.assertEquals(adminPage.getProfileHeaderName(), newName,
                    "Luu xong the dau trang Ho so phai hien ten moi '" + newName + "'");
            Assert.assertTrue(adminPage.getTopbarUserName().contains(newName),
                    "Luu xong chip tai khoan o topbar phai hien ten moi '" + newName + "', dang thay: "
                            + adminPage.getTopbarUserName());
        } finally {
            // Khoi phuc ten goc (chi khi da luu ten moi trong test nay).
            if (nameChanged && originalName != null) {
                try {
                    if (!adminPage.isProfilePageDisplayed()) {
                        adminPage.openUserMenuIfClosed();
                        adminPage.clickUserMenuProfile();
                    }
                    adminPage.clickProfileEditButton();
                    adminPage.setProfileName(originalName);
                    adminPage.clickProfileSaveButton();
                } catch (Throwable ignored) {
                    // Khoi phuc best-effort: neu that bai, ten admin co the van la
                    // ten test — bao cao phai ghi ro (xem TEST-MAPPING.md muc dot 17).
                }
            }
        }
    }

    // ---------- Dot 21: menu topbar gon + Ho so 2 cot ----------

    @Story("Tài khoản")
    @Test(description = "ADM21-01 (đợt 21): Menu tài khoản topbar CHỈ còn đúng 2 mục ('Thông tin cá nhân'/'Đăng xuất'), không còn khối tiêu đề (tên/email/'Vai trò:'); có divider trước 'Đăng xuất', 'Đăng xuất' màu đỏ nhạt (#dc2626)")
    public void adminTopbarUserMenuHasExactlyTwoItemsNoHeader() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickUserMenuButton();
        Assert.assertTrue(adminPage.isUserMenuDisplayed(),
                "Bam chip tai khoan phai mo dropdown menu (admin-user-menu)");
        Assert.assertEquals(adminPage.getUserMenuItemCount(), 2,
                "Menu tai khoan topbar dot 21 phai CHI co dung 2 muc, dang thay: "
                        + adminPage.getUserMenuItemCount());
        String menuText = adminPage.getUserMenuText();
        Assert.assertTrue(menuText.contains("Thông tin cá nhân"),
                "Menu tai khoan phai co muc 'Thông tin cá nhân', dang thay: " + menuText);
        Assert.assertTrue(menuText.contains("Đăng xuất"),
                "Menu tai khoan phai co muc 'Đăng xuất', dang thay: " + menuText);
        Assert.assertFalse(menuText.contains("Vai trò:"),
                "Menu tai khoan dot 21 khong duoc con khoi tieu de 'Vai trò: ...', dang thay: " + menuText);
        Assert.assertTrue(adminPage.isUserMenuSeparatorDisplayed(),
                "Menu tai khoan phai co divider (ke ngan) truoc muc 'Đăng xuất'");
        String logoutColor = adminPage.getUserMenuLogoutColor();
        Assert.assertTrue(logoutColor != null && logoutColor.contains("220, 38, 38"),
                "Muc 'Đăng xuất' phai mau do nhat #dc2626 (rgb 220, 38, 38), dang thay: " + logoutColor);
    }

    @Story("Hồ sơ")
    @Test(description = "ADM21-02 (đợt 21): Thẻ Thông tin cá nhân 2 hàng × 2 cột (Họ tên | Email / Số điện thoại | Địa chỉ) ở CẢ trạng thái xem lẫn form sửa; cửa sổ đã maximize nên viewport > 640px, grid phải có 2 cột")
    public void adminProfileInfoGridHasTwoColumnsInViewAndEdit() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);
        Assert.assertEquals(adminPage.getProfileInfoGridColumnCount(), 2,
                "The Thong tin ca nhan o trang thai xem phai la grid 2 cot, dang thay: "
                        + adminPage.getProfileInfoGridColumnCount() + " cot");
        String profileText = adminPage.getProfilePageText();
        Assert.assertTrue(profileText.contains("Họ tên:") && profileText.contains("Email:")
                        && profileText.contains("Số điện thoại:") && profileText.contains("Địa chỉ:"),
                "The Thong tin ca nhan dang xem phai hien du 4 truong Ho ten/Email/SDT/Dia chi, dang thay: " + profileText);
        adminPage.clickProfileEditButton();
        Assert.assertTrue(adminPage.isProfileFormDisplayed(),
                "Bam 'Sửa thông tin' phai mo form sua ho so (admin-profile-form)");
        Assert.assertEquals(adminPage.getProfileFormGridColumnCount(), 2,
                "Form sua Thong tin ca nhan phai la grid 2 cot nhu trang thai xem, dang thay: "
                        + adminPage.getProfileFormGridColumnCount() + " cot");
        adminPage.clickProfileCancelButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_FORM),
                "Bam 'Hủy' xong form sua ho so phai dong");
    }

    @Story("Tài khoản")
    @Test(description = "ADM17-13 (đợt 17): Đổi mật khẩu admin — nhập sai mật khẩu hiện tại → lỗi inline 'Mật khẩu hiện tại không đúng'; nhập đúng → đổi sang mật khẩu tạm thành công, app về /login; đăng nhập bằng mật khẩu tạm rồi ĐỔI LẠI về Admin@123 ngay trong test + khôi phục trong finally nếu dở dang")
    public void adminChangePasswordWrongCurrentFailsThenRestoreAfterSuccess() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        // Mat khau nao dang co hieu luc (de finally biet phai dang nhap bang mat
        // khau nao de doi lai neu test dung giua chung).
        String currentPassword = GlobalConstants.ADMIN_PASSWORD;
        try {
            openProfilePage(adminPage);
            adminPage.openChangePasswordForm();
            Assert.assertTrue(adminPage.isChangePasswordFormDisplayed(),
                    "Bam 'Đổi mật khẩu' phai mo form doi mat khau (admin-change-password-form)");

            // (1) Nhap sai mat khau hien tai -> loi inline duoi o mat khau hien tai.
            adminPage.fillChangePasswordForm("SaiMatKhau@1", TEMP_PASSWORD, TEMP_PASSWORD);
            adminPage.clickChangePasswordSaveButton();
            Assert.assertTrue(adminPage.isChangePasswordFormDisplayed(),
                    "Nhap sai mat khau hien tai thi form doi mat khau phai van mo (khong doi mat khau)");
            String currentError = "";
            long deadline = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < deadline) {
                currentError = adminPage.getChangePasswordFieldErrorText("currentPassword");
                if (!currentError.isEmpty()) {
                    break;
                }
                adminPage.sleepInSecond(1);
            }
            Assert.assertTrue(currentError.contains("Mật khẩu hiện tại không đúng"),
                    "Nhap sai mat khau hien tai thi o mat khau hien tai phai bao 'Mật khẩu hiện tại không đúng', dang thay: '" + currentError + "'");

            // (2) Nhap dung mat khau hien tai -> doi sang mat khau tam; BE vo hieu
            // JWT nen app tu dang xuat ve /login.
            adminPage.fillChangePasswordForm(GlobalConstants.ADMIN_PASSWORD, TEMP_PASSWORD, TEMP_PASSWORD);
            adminPage.clickChangePasswordSaveButton();
            currentPassword = TEMP_PASSWORD; // ghi nhan NGAY sau khi gui
            Assert.assertTrue(adminPage.waitForLoginPage(10),
                    "Doi mat khau thanh cong thi app phai ve trang /login (JWT cu bi vo hieu)");

            // (3) Dang nhap lai bang mat khau tam de chung minh mat khau moi co hieu luc.
            adminPage.loginAsUserOnAdminAppOrSkip("admin", TEMP_PASSWORD);
            adminPage.openPageUrl(GlobalConstants.getAdminAppUrl() + "/admin");
            adminPage.waitForAdminGateResolved();
            Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                    "Dang nhap bang mat khau tam moi doi phai vao duoc menu admin");

            // (4) Doi LAI ve mat khau goc ngay trong test.
            openProfilePage(adminPage);
            adminPage.openChangePasswordForm();
            adminPage.fillChangePasswordForm(TEMP_PASSWORD, GlobalConstants.ADMIN_PASSWORD, GlobalConstants.ADMIN_PASSWORD);
            adminPage.clickChangePasswordSaveButton();
            currentPassword = GlobalConstants.ADMIN_PASSWORD;
            Assert.assertTrue(adminPage.waitForLoginPage(10),
                    "Doi lai mat khau goc xong app phai ve trang /login");

            // (5) Dang nhap lai bang mat khau goc de chung minh da khoi phuc.
            adminPage.loginAsAdminOnAdminAppOrSkip();
            Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                    "Khoi phuc xong phai dang nhap lai duoc bang mat khau goc Admin@123");
        } finally {
            // Khoi phuc khan cap: neu test dung giua chung khi mat khau dang la tam.
            if (!GlobalConstants.ADMIN_PASSWORD.equals(currentPassword)) {
                try {
                    adminPage.loginAsUserOnAdminAppOrSkip("admin", currentPassword);
                    adminPage.openPageUrl(GlobalConstants.getAdminAppUrl() + "/admin");
                    adminPage.waitForAdminGateResolved();
                    openProfilePage(adminPage);
                    adminPage.openChangePasswordForm();
                    adminPage.fillChangePasswordForm(currentPassword,
                            GlobalConstants.ADMIN_PASSWORD, GlobalConstants.ADMIN_PASSWORD);
                    adminPage.clickChangePasswordSaveButton();
                    adminPage.waitForLoginPage(10);
                } catch (Throwable ignored) {
                    // Neu khoi phuc that bai, mat khau admin co the van la mat khau
                    // tam 'Admin@456' — bao cao phai ghi ro de doi lai thu cong
                    // (xem TEST-MAPPING.md muc dot 17).
                }
            }
        }
    }

    // ---------- Dot 19 (mapping SCR_12) ----------

    @Story("Tài khoản")
    @Test(description = "ADM19-42 (TC_SCRA12_FUNC_002): Bấm ra ngoài thì popover menu tài khoản đóng lại — nút menu vẫn hiển thị bình thường, không chuyển trang, không đăng xuất")
    public void adminUserMenuClosesOnOutsideClick() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickUserMenuButton();
        Assert.assertTrue(adminPage.isUserMenuDisplayed(),
                "Bam chip tai khoan phai mo dropdown menu (admin-user-menu)");

        // Bam ra ngoai (vung spacer cua topbar, nam ngoai userMenuRef) — dong menu.
        adminPage.clickToElement("css=.sa-topbar-spacer");
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_USER_MENU),
                "Bam ra ngoai thi popover menu tai khoan phai dong lai");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_USER_MENU_BTN),
                "Dong menu xong nut menu tai khoan van phai hien binh thuong");
        Assert.assertTrue(adminPage.getCurrentPageUrl().contains("/admin"),
                "Bam ra ngoai thi khong duoc chuyen trang");
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Bam ra ngoai thi khong duoc dang xuat (menu quan tri van phai hien)");
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-43 (TC_SCRA12_FUNC_006 + TC_SCRA12_FUNC_014): Nút 'Quay lại' trên trang Hồ sơ thì về lại mục trước đó, không lưu/thay đổi dữ liệu; bấm Hủy trong form sửa hồ sơ thì đóng form, không lưu gì (đổi Họ tên + SĐT rồi Hủy thì thẻ Thông tin cá nhân vẫn hiển thị dữ liệu cũ)")
    public void adminProfileBackAndCancelDoNotSave() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);
        String profileTextBefore = adminPage.getProfilePageText();
        String originalName = adminPage.getProfileHeaderName();
        Assert.assertFalse(originalName == null || originalName.trim().isEmpty(),
                "Trang Ho so phai hien ho ten admin o the dau trang");

        // (1) Nut 'Quay lại' — ve muc truoc, khong doi du lieu.
        adminPage.clickProfileBackButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_PAGE),
                "Bam 'Quay lại' xong trang Ho so phai dong");
        Assert.assertTrue(adminPage.isDashboardHeadingDisplayed(),
                "Bam 'Quay lại' tu trang Ho so phai ve muc Tong quan (muc truoc do)");
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Bam 'Quay lại' xong menu quan tri van phai hien");

        // (2) Doi ten + SDT roi bam Huy — khong luu.
        openProfilePage(adminPage);
        adminPage.clickProfileEditButton();
        Assert.assertTrue(adminPage.isProfileFormDisplayed(),
                "Bam 'Sửa thông tin' phai mo form sua ho so");
        String originalPhone = adminPage.getProfilePhoneInputValue();
        adminPage.setProfileName(originalName + " Khong Luu");
        adminPage.setProfilePhone("0912345678");
        adminPage.clickProfileCancelButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_FORM),
                "Bam 'Hủy' xong form sua ho so phai dong");
        String profileTextAfter = adminPage.getProfilePageText();
        Assert.assertTrue(profileTextAfter.contains(originalName),
                "Bam 'Hủy' thi the Thong tin ca nhan phai hien lai ten cu '" + originalName + "'");
        Assert.assertFalse(profileTextAfter.contains(originalName + " Khong Luu"),
                "Bam 'Hủy' thi ten moi chua luu khong duoc xuat hien");
        Assert.assertEquals(profileTextAfter, profileTextBefore,
                "Bam 'Hủy' thi trang Ho so phai hien lai dung du lieu cu");
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-44 (TC_SCRA12_UI_007): Thẻ Thông tin cá nhân ở trạng thái xem hiển thị Họ tên/Email/Số điện thoại/Địa chỉ (chưa có thì '—'), không hiện form nhập liệu")
    public void adminProfileViewCardShowsContactDetails() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);

        String profileText = adminPage.getProfilePageText();
        Assert.assertTrue(profileText.contains("Họ tên:"),
                "The Thong tin ca nhan dang xem phai hien 'Họ tên:', dang thay: " + profileText);
        Assert.assertTrue(profileText.contains("Email:"),
                "The Thong tin ca nhan dang xem phai hien 'Email:', dang thay: " + profileText);
        Assert.assertTrue(profileText.contains("Số điện thoại:"),
                "The Thong tin ca nhan dang xem phai hien 'Số điện thoại:', dang thay: " + profileText);
        Assert.assertTrue(profileText.contains("Địa chỉ:"),
                "The Thong tin ca nhan dang xem phai hien 'Địa chỉ:', dang thay: " + profileText);
        Assert.assertTrue(profileText.contains(GlobalConstants.ADMIN_EMAIL),
                "The Thong tin ca nhan phai hien email admin '" + GlobalConstants.ADMIN_EMAIL + "'");
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_FORM),
                "Chua bam 'Sửa thông tin' thi khong duoc hien form nhap lieu");
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-45 (TC_SCRA12_VAL_009 + TC_SCRA12_VAL_010): Bỏ trống Họ tên khi sửa hồ sơ thì lỗi 'Vui lòng bổ sung thông tin bắt buộc.' dưới ô Họ tên; SĐT '123' / '098765432' (9 số) / '1987654321' (không bắt đầu bằng 0) thì lỗi 'Số điện thoại không đúng định dạng.' dưới ô SĐT; chỉ assert lỗi rồi đóng form KHÔNG lưu")
    public void adminProfileValidationErrors() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);
        String originalName = adminPage.getProfileHeaderName();
        adminPage.clickProfileEditButton();
        Assert.assertTrue(adminPage.isProfileFormDisplayed(),
                "Bam 'Sửa thông tin' phai mo form sua ho so");

        // (1) Bo trong Ho ten.
        adminPage.setProfileName("");
        adminPage.clickProfileSaveButtonNoWait();
        Assert.assertEquals(adminPage.getProfileFieldErrorText("name"),
                "Vui lòng bổ sung thông tin bắt buộc.",
                "Bo trong Ho ten thi phai bao 'Vui lòng bổ sung thông tin bắt buộc.'");
        Assert.assertTrue(adminPage.isProfileFormDisplayed(),
                "Loi validate thi form sua ho so phai van mo (khong goi PUT)");
        adminPage.setProfileName(originalName);

        // (2) SDT sai dinh dang — 3 truong hop.
        for (String badPhone : new String[]{"123", "098765432", "1987654321"}) {
            adminPage.setProfilePhone(badPhone);
            adminPage.clickProfileSaveButtonNoWait();
            Assert.assertEquals(adminPage.getProfileFieldErrorText("phone"),
                    "Số điện thoại không đúng định dạng.",
                    "SDT '" + badPhone + "' thi phai bao 'Số điện thoại không đúng định dạng.'");
            Assert.assertTrue(adminPage.isProfileFormDisplayed(),
                    "Loi validate thi form sua ho so phai van mo (khong goi PUT)");
        }

        // Dong form KHONG luu.
        adminPage.clickProfileCancelButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_FORM),
                "Bam 'Hủy' xong form sua ho so phai dong");
        Assert.assertTrue(adminPage.getProfilePageText().contains(originalName),
                "Dong form khong luu thi the Thong tin ca nhan van phai hien ten cu");
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-46 (TC_SCRA12_VAL_011, MUTATES_REAL_DATA): Để trống Số điện thoại khi sửa hồ sơ thì vẫn lưu được (SĐT không bắt buộc) — lưu thành công (toast), SĐT trên trang Hồ sơ hiển thị '—'; khôi phục SĐT gốc trong finally")
    public void adminProfileEmptyPhoneSavesAsNull() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);
        adminPage.clickProfileEditButton();
        String originalPhone = adminPage.getProfilePhoneInputValue();
        if (originalPhone == null || originalPhone.trim().isEmpty()) {
            adminPage.clickProfileCancelButton();
            throw new SkipException("Bo qua: SDT ho so admin dang trong san nen khong kiem tra duoc nhanh luu SDT ve trong");
        }
        boolean savedEmpty = false;
        try {
            adminPage.setProfilePhone("");
            adminPage.clickProfileSaveButton();
            savedEmpty = true;
            Assert.assertTrue(adminPage.isToastDisplayed(),
                    "Luu ho so voi SDT trong xong phai hien toast thanh cong (admin-toast)");
            Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_FORM),
                    "Luu thanh cong thi form sua ho so phai dong");
            Assert.assertTrue(adminPage.getProfilePageText().contains("Số điện thoại: —"),
                    "Luu SDT trong xong trang Ho so phai hien 'Số điện thoại: —'");
        } finally {
            if (savedEmpty) {
                try {
                    adminPage.clickProfileEditButton();
                    adminPage.setProfilePhone(originalPhone);
                    adminPage.clickProfileSaveButton();
                } catch (Throwable ignored) {
                    // Khoi phuc best-effort: neu that bai, SDT admin co the van
                    // dang trong — bao cao phai ghi ro.
                }
            }
        }
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-47 (TC_SCRA12_VAL_018/019/020/021/022/023/024/025): Validate đổi mật khẩu — mật khẩu mới 'ngan' thì 'Mật khẩu phải có tối thiểu 8 ký tự.'; thiếu chữ hoa thì 'Mật khẩu phải chứa ít nhất 1 ký tự chữ hoa (A-Z).'; thiếu chữ số thì '... 1 chữ số (0-9).'; thiếu ký tự đặc biệt thì '... 1 ký tự đặc biệt (ví dụ: !@#$%^&*...).'; xác nhận lệch thì 'Mật khẩu xác nhận không khớp.'; để trống ô nào thì 'Vui lòng bổ sung thông tin bắt buộc.' dưới đúng ô đó; chỉ assert lỗi, KHÔNG đổi mật khẩu thật")
    public void adminChangePasswordValidationErrors() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);
        adminPage.openChangePasswordForm();
        Assert.assertTrue(adminPage.isChangePasswordFormDisplayed(),
                "Bam 'Đổi mật khẩu' phai mo form doi mat khau");

        // {mat khau hien tai, mat khau moi, xac nhan, o loi, thong diep loi}
        String[][] cases = {
                {GlobalConstants.ADMIN_PASSWORD, "ngan", "ngan", "new",
                        "Mật khẩu phải có tối thiểu 8 ký tự."},
                {GlobalConstants.ADMIN_PASSWORD, "newpass1!", "newpass1!", "new",
                        "Mật khẩu phải chứa ít nhất 1 ký tự chữ hoa (A-Z)."},
                {GlobalConstants.ADMIN_PASSWORD, "Newpass!", "Newpass!", "new",
                        "Mật khẩu phải chứa ít nhất 1 chữ số (0-9)."},
                {GlobalConstants.ADMIN_PASSWORD, "Newpass1", "Newpass1", "new",
                        "Mật khẩu phải chứa ít nhất 1 ký tự đặc biệt (ví dụ: !@#$%^&*...)."},
                {GlobalConstants.ADMIN_PASSWORD, "Newpass1!", "Khacpass1!", "confirm",
                        "Mật khẩu xác nhận không khớp."},
                {"", "Newpass1!", "Newpass1!", "current",
                        "Vui lòng bổ sung thông tin bắt buộc."},
                {GlobalConstants.ADMIN_PASSWORD, "", "", "new",
                        "Vui lòng bổ sung thông tin bắt buộc."},
                {GlobalConstants.ADMIN_PASSWORD, "Newpass1!", "", "confirm",
                        "Vui lòng bổ sung thông tin bắt buộc."},
        };
        for (String[] entry : cases) {
            adminPage.fillChangePasswordForm(entry[0], entry[1], entry[2]);
            adminPage.clickChangePasswordSaveButton();
            Assert.assertEquals(adminPage.getChangePasswordFieldErrorText(entry[3]), entry[4],
                    "Nhap (" + entry[0] + " / " + entry[1] + " / " + entry[2] + ") thi o '" + entry[3]
                            + "' phai bao '" + entry[4] + "', dang thay: '"
                            + adminPage.getChangePasswordFieldErrorText(entry[3]) + "'");
            Assert.assertTrue(adminPage.isChangePasswordFormDisplayed(),
                    "Loi validate thi form doi mat khau phai van mo (khong goi POST)");
        }

        // Dong form KHONG luu (khong doi mat khau that).
        adminPage.clickChangePasswordCancelButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CHANGE_PASSWORD_FORM),
                "Bam 'Hủy' xong form doi mat khau phai dong");
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-48 (TC_SCRA12_FUNC_028): Bấm Hủy trong form đổi mật khẩu thì đóng form, mật khẩu không đổi — nhập mật khẩu hiện tại + mật khẩu mới (chưa bấm lưu) rồi bấm 'Hủy' thì drawer đóng, hiển thị lại nút 'Đổi mật khẩu'; đăng nhập lại bằng mật khẩu cũ vẫn được")
    public void adminChangePasswordCancelClosesFormWithoutChange() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);
        adminPage.openChangePasswordForm();
        Assert.assertTrue(adminPage.isChangePasswordFormDisplayed(),
                "Bam 'Đổi mật khẩu' phai mo form doi mat khau");

        adminPage.fillChangePasswordForm(GlobalConstants.ADMIN_PASSWORD, "Newpass1!", "Newpass1!");
        adminPage.clickChangePasswordCancelButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CHANGE_PASSWORD_FORM),
                "Bam 'Hủy' xong form doi mat khau phai dong");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CHANGE_PASSWORD_BTN),
                "Dong form xong phai hien lai nut 'Đổi mật khẩu'");

        // Chung minh mat khau khong doi: dang xuat roi dang nhap lai bang mat khau cu.
        adminPage.clickUserMenuLogout();
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam 'Đăng xuất' phai mo popup G3");
        adminPage.clickConfirmYes();
        Assert.assertTrue(adminPage.waitForLoginPage(10),
                "Xac nhan dang xuat xong phai ve trang /login");
        adminPage.loginAsUserOnAdminAppOrSkip("admin", GlobalConstants.ADMIN_PASSWORD);
        adminPage.openPageUrl(GlobalConstants.getAdminAppUrl() + "/admin");
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Huy doi mat khau thi van phai dang nhap duoc bang mat khau cu Admin@123");
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-49 (TC_SCRA12_UI_029, đợt 23 phần A): 2 dòng gợi ý ĐÃ BỎ — dưới ô Mật khẩu mới KHÔNG còn 'Tối thiểu 8 ký tự, gồm chữ hoa, chữ số và ký tự đặc biệt.', dưới ô Số điện thoại KHÔNG còn 'Bắt đầu bằng 0, gồm 10 chữ số.', dưới ô Email KHÔNG còn 'Email là định danh đăng nhập — chỉ xem, không sửa.'; đổi mật khẩu là MODAL giữa màn hình rộng 560px, 3 ô xếp dọc 1 cột; đang sửa hồ sơ thì nút 'Sửa thông tin' biến mất (không còn 'Đóng form sửa'); đóng cả 2 form không lưu")
    public void adminProfileAndPasswordFieldHints() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);

        // (1) Modal doi mat khau giua man hinh, 3 o xep doc 1 cot, khong hint policy.
        adminPage.openChangePasswordForm();
        Assert.assertTrue(adminPage.isChangePasswordFormDisplayed(),
                "Bam 'Đổi mật khẩu' phai mo form doi mat khau");
        Assert.assertTrue(adminPage.isChangePasswordModalCentered(),
                "Form doi mat khau dot 23 phai la MODAL giua man hinh (.sa-modal), khong con Drawer FormShell (fs-drawer)");
        Assert.assertEquals(adminPage.getChangePasswordModalWidth(), "560px",
                "Modal doi mat khau dot 23 phai rong 560px, dang thay: " + adminPage.getChangePasswordModalWidth());
        Assert.assertTrue(adminPage.areChangePasswordFieldsStackedVertically(),
                "3 o doi mat khau phai xep doc 1 cot (moi hang 1 o full width): cung toa do X, Y tang dan");
        Assert.assertFalse(adminPage.getProfilePageText().contains(
                        "Tối thiểu 8 ký tự, gồm chữ hoa, chữ số và ký tự đặc biệt."),
                "Duoi o Mat khau moi dot 23 KHONG duoc con goi y 'Tối thiểu 8 ký tự, gồm chữ hoa, chữ số và ký tự đặc biệt.'");
        adminPage.clickChangePasswordCancelButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CHANGE_PASSWORD_FORM),
                "Bam 'Hủy' xong form doi mat khau phai dong");

        // (2) Form sua ho so: khong hint SDT/Email; nut 'Sửa thông tin' bien mat khi dang sua.
        adminPage.clickProfileEditButton();
        Assert.assertTrue(adminPage.isProfileFormDisplayed(),
                "Bam 'Sửa thông tin' phai mo form sua ho so");
        Assert.assertFalse(adminPage.getProfilePageText().contains("Bắt đầu bằng 0, gồm 10 chữ số."),
                "Duoi o SDT dot 23 KHONG duoc con goi y 'Bắt đầu bằng 0, gồm 10 chữ số.'");
        Assert.assertFalse(adminPage.getProfilePageText().contains("Email là định danh đăng nhập — chỉ xem, không sửa."),
                "Duoi o Email dot 23 KHONG duoc con goi y 'Email là định danh đăng nhập — chỉ xem, không sửa.'");
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_EDIT_BTN),
                "Dang sua ho so thi nut 'Sửa thông tin' phai bien mat han (khong con 'Đóng form sửa')");
        adminPage.clickProfileCancelButton();
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_FORM),
                "Bam 'Hủy' xong form sua ho so phai dong");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PROFILE_EDIT_BTN),
                "Dong form sua xong nut 'Sửa thông tin' phai hien lai");
    }

    @Story("Hồ sơ")
    @Test(description = "ADM19-50 (TC_SCRA12_UI_031): Mở trang Hồ sơ chỉ còn 1 tiêu đề (pagehead chung ẩn, chỉ còn tiêu đề riêng 'Hồ sơ quản trị'); ghi nhận max-width thực tế của trang Hồ sơ, KHÔNG hard-code 1180px")
    public void adminProfilePageHidesPageheadAndMeasuresWidth() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        openProfilePage(adminPage);

        Assert.assertFalse(adminPage.isAdminPageheadDisplayed(),
                "Mo trang Ho so thi pagehead chung (tieu de + mo ta cua tab) phai AN, chi con tieu de rieng");
        String profileText = adminPage.getProfilePageText();
        Assert.assertTrue(profileText.contains("Hồ sơ quản trị"),
                "Trang Ho so phai co tieu de rieng 'Hồ sơ quản trị', dang thay: " + profileText);
        String maxWidth = adminPage.getProfilePageMaxWidth();
        Assert.assertFalse(maxWidth == null || maxWidth.trim().isEmpty(),
                "Phai doc duoc max-width thuc te cua trang Ho so (ghi nhan, khong hard-code 1180px)");
    }
}
