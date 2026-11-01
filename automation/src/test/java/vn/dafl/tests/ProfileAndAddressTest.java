package vn.dafl.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProfilePageObject;

/**
 * CN-03 Ho so nguoi dung: cap nhat thong tin (email chi doc, SDT khong trung),
 * doi mat khau theo policy nghiem ngat va so dia chi giao hang (toi da 5 dia chi,
 * dia chi dau tien tu dong la mac dinh, xoa mac dinh thi dia chi con lai ke thua).
 */
public class ProfileAndAddressTest extends BaseTest {

    @Test(description = "TC-CN03-01 (CN-03.1): Cap nhat ho ten va SDT hop le -> bao cap nhat thanh cong")
    public void updateProfileSuccessfully() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Ban Dau", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForProfileLoaded();

        String newName = "Khach Da Doi Ten";
        String newPhone = randomPhone();
        profile.updateProfile(newName, newPhone);

        Assert.assertTrue(profile.isProfileSuccessDisplayed(), "Phai hien thong bao cap nhat thanh cong");
        Assert.assertTrue(profile.getProfileSuccessMessage().contains(GlobalConstants.PROFILE_SUCCESS_MESSAGE),
                "Noi dung thong bao cap nhat khong dung");
        Assert.assertEquals(profile.getProfileName(), newName, "Ho ten moi phai duoc luu");
        Assert.assertEquals(profile.getProfilePhone(), newPhone, "SDT moi phai duoc luu");
        Assert.assertEquals(profile.getProfileEmail(), email, "Email chi doc, khong duoc thay doi");
    }

    @Test(description = "TC-CN03-02 (CN-03 E2): Cap nhat SDT trung voi tai khoan khac bi tu choi")
    public void updateProfileWithDuplicatePhoneShowsError() {
        HomePageObject homePage = registerAndLogin("Khach Trung SDT", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForProfileLoaded();

        profile.updateProfile("Khach Trung SDT", GlobalConstants.DEMO_PHONE); // SDT cua tai khoan demo

        Assert.assertTrue(profile.isProfilePhoneErrorDisplayed(), "SDT trung phai bao loi duoi o SDT");
        Assert.assertTrue(profile.getProfilePhoneError().contains(GlobalConstants.DUPLICATE_PHONE_MESSAGE),
                "Noi dung loi trung SDT khong dung");
    }

    @Test(description = "CN-03.2: Doi mat khau voi mat khau hien tai sai bi tu choi")
    public void changePasswordWithWrongCurrentShowsError() {
        HomePageObject homePage = registerAndLogin("Khach Doi Mat Khau", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForProfileLoaded();

        profile.changePassword("WrongCurrent1!", "NewPassword123!", "NewPassword123!");

        Assert.assertTrue(profile.isChangePasswordCurrentErrorDisplayed(),
                "Mat khau hien tai sai phai bao loi duoi o mat khau hien tai");
        Assert.assertTrue(profile.getChangePasswordCurrentError().contains("Mật khẩu hiện tại không đúng"),
                "Noi dung loi mat khau hien tai khong dung: " + profile.getChangePasswordCurrentError());
    }

    @Test(description = "TC-CN03-04 (CN-03.2): Doi mat khau thanh cong -> dang nhap lai bang mat khau moi duoc")
    public void changePasswordThenLoginWithNewPassword() {
        String email = randomEmail();
        String newPassword = "NewPassword456!";
        HomePageObject homePage = registerAndLogin("Khach Doi MK", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForProfileLoaded();

        profile.changePassword(GlobalConstants.DEMO_PASSWORD, newPassword, newPassword);
        Assert.assertTrue(profile.isChangePasswordSuccessDisplayed(), "Doi mat khau phai bao thanh cong");

        homePage.logout();
        homePage.waitForLoggedOut();
        loginAs(email, newPassword);
        Assert.assertEquals(new HomePageObject().getAccountDisplayName(), "Khach Doi MK",
                "Dang nhap bang mat khau moi phai thanh cong");
    }

    @Test(description = "TC-CN03-05 (CN-03.3): Dia chi dau tien them vao tu dong tro thanh mac dinh")
    public void firstAddressBecomesDefaultAutomatically() {
        HomePageObject homePage = registerAndLogin("Khach Dia Chi 1", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();

        profile.addAddress("Khach Dia Chi 1", GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_DETAIL);

        Assert.assertEquals(profile.getAddressCount(), 1, "Phai co dung 1 dia chi");
        String firstId = profile.getFirstAddressId();
        Assert.assertEquals(profile.getDefaultAddressId(), firstId,
                "Dia chi dau tien phai tu dong la mac dinh");
    }

    @Test(description = "CN-03.3: Dat mot dia chi khac lam mac dinh")
    public void setAnotherAddressAsDefault() {
        HomePageObject homePage = registerAndLogin("Khach Dia Chi 2", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();

        profile.addAddress("Khach Dia Chi 2", GlobalConstants.DEMO_PHONE, "Số 1 Đại Cồ Việt");
        profile.addAddress("Khach Dia Chi 2", GlobalConstants.DEMO_PHONE, "Số 2 Đường Láng");
        Assert.assertEquals(profile.getAddressCount(), 2, "Phai co 2 dia chi");

        String firstId = profile.getAddressIds().get(0);
        String secondId = profile.getAddressIds().get(1);
        Assert.assertEquals(profile.getDefaultAddressId(), firstId, "Mac dinh ban dau phai la dia chi dau tien");

        profile.setAddressAsDefault(secondId);
        Assert.assertEquals(profile.getDefaultAddressId(), secondId,
                "Sau khi dat mac dinh, huy hieu phai chuyen sang dia chi thu hai");
    }

    @Test(description = "CN-03.3: Sua thong tin mot dia chi da luu")
    public void editAddressUpdatesInformation() {
        HomePageObject homePage = registerAndLogin("Khach Sua Dia Chi", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();

        profile.addAddress("Khach Sua Dia Chi", GlobalConstants.DEMO_PHONE, "Số 1 Đại Cồ Việt");
        String addressId = profile.getFirstAddressId();

        profile.openEditAddressForm(addressId);
        profile.fillAndSaveAddressForm("Khach Sua Dia Chi", GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD,
                "Số 99 Đường Mới", false);
        profile.sleepInSecond(2);

        Assert.assertTrue(profile.getAddressItemText(addressId).contains("Số 99"),
                "Dia chi sau sua phai hien thong tin moi");
    }

    @Test(description = "CN-03.3: Xoa dia chi mac dinh -> dia chi con lai (moi nhat) tu dong ke thua mac dinh")
    public void deleteDefaultAddressPromotesRemaining() {
        HomePageObject homePage = registerAndLogin("Khach Xoa Dia Chi", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();

        profile.addAddress("Khach Xoa Dia Chi", GlobalConstants.DEMO_PHONE, "Số 1 Đại Cồ Việt");
        profile.addAddress("Khach Xoa Dia Chi", GlobalConstants.DEMO_PHONE, "Số 2 Đường Láng");
        String defaultId = profile.getDefaultAddressId();
        String remainingId = profile.getAddressIds().stream()
                .filter(id -> !id.equals(defaultId)).findFirst().orElse(null);
        Assert.assertNotNull(remainingId, "Phai co dia chi thu hai");

        profile.deleteAddress(defaultId);

        Assert.assertEquals(profile.getAddressCount(), 1, "Xoa xong chi con 1 dia chi");
        Assert.assertEquals(profile.getDefaultAddressId(), remainingId,
                "Dia chi con lai phai tu dong tro thanh mac dinh");
    }

    @Test(description = "TC-CN03-03 (CN-03 E4): So dia chi toi da 5 - them dia chi thu 6 bi chan kem thong bao gioi han")
    public void addressBookRejectsSixthAddress() {
        HomePageObject homePage = registerAndLogin("Khach Nhieu Dia Chi", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();

        for (int i = 1; i <= 5; i++) {
            profile.addAddress("Khach Nhieu Dia Chi", GlobalConstants.DEMO_PHONE, "So " + i + " Duong Test");
        }
        Assert.assertEquals(profile.getAddressCount(), 5, "Phai co dung 5 dia chi");

        boolean formOpened = profile.openAddAddressForm();
        Assert.assertFalse(formOpened, "Da du 5 dia chi thi khong duoc mo form them nua");
        Assert.assertTrue(profile.isAddressLimitErrorDisplayed(), "Phai hien thong bao gioi han 5 dia chi");
        Assert.assertEquals(profile.getAddressCount(), 5, "So dia chi van phai la 5");
    }
}
