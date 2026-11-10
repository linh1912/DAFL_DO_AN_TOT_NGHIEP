package vn.dafl.tests.web.profile;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProfilePageObject;

/**
 * Mo rong CN-03 (Pha B buoc 3, dot 15.1): cac case manual loai (c) o
 * SCR_03_Ho_So_Dia_Chi (10) chua co trong ProfileAndAddressTest.
 * Khong sua lop test hien co; chi them lop moi nay.
 *
 * Moi test dung mot tai khoan test TU TAO qua UI (registerAndLogin) de khong
 * lam ban tai khoan demo (vd khong doi ten/SDT/mat khau cua demo). Rieng
 * TC_SCR3_VAL_006 (SĐT de trong van luu duoc) tu khoi phuc lai SDT sau test.
 */
@Feature("Hồ sơ và địa chỉ (mở rộng)")
public class ProfileExtendedTest extends BaseTest {

    /** Dang ky tai khoan moi roi mo /profile; tra ve ProfilePageObject. */
    private ProfilePageObject openFreshProfile(String fullName, String email, String phone) {
        HomePageObject homePage = registerAndLogin(fullName, email, phone, GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForProfileLoaded();
        return profile;
    }

    /** Chuoi toan chu 'A' co dung do dai cho truong hop bien (vd ho ten 51 ky tu). */
    private String nameOfLength(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('A');
        }
        return sb.toString();
    }

    // ---------- Ho so ca nhan ----------

    @Story("Cập nhật hồ sơ")
    @Test(description = "TC_SCR3_VAL_003 + TC_SCR3_VAL_004: Sửa họ tên trong hồ sơ thành 'A' (1 ký tự) / 51 ký tự -> lỗi 'Họ tên không hợp lệ', hồ sơ không đổi")
    public void profileNameTooShortOrTooLongShowsError() {
        String fullName = "Khach Ho So Ten";
        String email = randomEmail();
        String phone = randomPhone();
        ProfilePageObject profile = openFreshProfile(fullName, email, phone);

        String[] invalidNames = {"A", nameOfLength(51)};
        for (String invalidName : invalidNames) {
            profile.updateProfile(invalidName, phone);
            Assert.assertTrue(profile.isProfileNameErrorDisplayed(),
                    "Ho ten " + invalidName.length() + " ky tu phai bao loi duoi o Ho ten");
            Assert.assertEquals(profile.getProfileNameError(), "Họ tên không hợp lệ");

            getDriver().navigate().refresh();
            profile.waitForProfileLoaded();
            Assert.assertEquals(profile.getProfileName(), fullName,
                    "Tai lai trang: ho ten phai giu nguyen (khong luu gia tri sai)");
        }
    }

    @Story("Cập nhật hồ sơ")
    @Test(description = "TC_SCR3_VAL_005: Sửa số điện thoại trong hồ sơ thành '12345' -> lỗi 'Số điện thoại không hợp lệ', hồ sơ không đổi")
    public void profilePhoneInvalidFormatShowsError() {
        String fullName = "Khach Ho So SDT";
        String email = randomEmail();
        String phone = randomPhone();
        ProfilePageObject profile = openFreshProfile(fullName, email, phone);

        profile.updateProfile(fullName, "12345");

        Assert.assertTrue(profile.isProfilePhoneErrorDisplayed(),
                "SDT '12345' phai bao loi duoi o SDT");
        Assert.assertEquals(profile.getProfilePhoneError(), "Số điện thoại không hợp lệ");

        getDriver().navigate().refresh();
        profile.waitForProfileLoaded();
        Assert.assertEquals(profile.getProfilePhone(), phone,
                "Tai lai trang: SDT phai giu nguyen (khong luu gia tri sai)");
    }

    @Story("Cập nhật hồ sơ")
    @Test(description = "TC_SCR3_VAL_006: Để trống Số điện thoại trong hồ sơ vẫn lưu được — không có lỗi dưới ô SĐT, hiện 'Cập nhật thành công', SĐT trở thành trống (sau đó khôi phục lại SĐT)")
    public void profilePhoneCanBeLeftEmptyAndSaved() {
        String fullName = "Khach Ho So Trong";
        String email = randomEmail();
        String phone = randomPhone();
        ProfilePageObject profile = openFreshProfile(fullName, email, phone);

        profile.updateProfile(fullName, "");

        Assert.assertFalse(profile.isProfilePhoneErrorDisplayed(),
                "SDT de trong o ho so khong duoc bao loi");
        Assert.assertTrue(profile.isProfileSuccessDisplayed(),
                "De trong SDT van phai luu thanh cong");
        Assert.assertTrue(profile.getProfileSuccessMessage().contains(GlobalConstants.PROFILE_SUCCESS_MESSAGE),
                "Thong bao thanh cong phai chua 'Cập nhật thành công'");
        Assert.assertEquals(profile.getProfilePhone(), "",
                "SDT trong ho so phai tro thanh trong sau khi luu");

        // Khoi phuc lai SDT de khong anh huong lan chay sau.
        profile.updateProfile(fullName, phone);
        Assert.assertTrue(profile.isProfileSuccessDisplayed(), "Khoi phuc SDT phai luu thanh cong");
        Assert.assertEquals(profile.getProfilePhone(), phone, "SDT phai duoc khoi phuc");
    }

    // ---------- Doi mat khau ----------

    @Story("Đổi mật khẩu")
    @Test(description = "TC_SCR3_VAL_008: Đổi mật khẩu với mật khẩu mới trùng mật khẩu cũ -> lỗi 'Mật khẩu mới không được trùng với mật khẩu cũ' dưới ô Mật khẩu mới")
    public void changePasswordSameAsCurrentShowsError() {
        ProfilePageObject profile = openFreshProfile("Khach Doi MK Trung", randomEmail(), randomPhone());

        profile.changePassword(GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD,
                GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(profile.isChangePasswordNewErrorDisplayed(),
                "Mat khau moi trung mat khau cu phai bao loi duoi o mat khau moi");
        Assert.assertEquals(profile.getChangePasswordNewError(),
                "Mật khẩu mới không được trùng với mật khẩu cũ");
    }

    @Story("Đổi mật khẩu")
    @Test(description = "TC_SCR3_VAL_009: Đổi mật khẩu với mật khẩu mới 'newpass1' thiếu chữ hoa và ký tự đặc biệt -> checklist chưa xanh + lỗi 'Mật khẩu chưa đáp ứng đủ các điều kiện bảo mật bên dưới.'")
    public void changePasswordWeakNewPasswordShowsError() {
        ProfilePageObject profile = openFreshProfile("Khach Doi MK Yeu", randomEmail(), randomPhone());

        profile.changePassword(GlobalConstants.DEMO_PASSWORD, "newpass1", "newpass1");

        Assert.assertFalse(profile.isChangePasswordUppercaseRuleSatisfied(),
                "Mat khau 'newpass1' thieu chu hoa nen rule chu hoa chua dat");
        Assert.assertFalse(profile.isChangePasswordSpecialRuleSatisfied(),
                "Mat khau 'newpass1' thieu ky tu dac biet nen rule ky tu dac biet chua dat");
        Assert.assertTrue(profile.isChangePasswordNewErrorDisplayed(),
                "Mat khau moi khong du policy phai bao loi duoi o mat khau moi");
        Assert.assertEquals(profile.getChangePasswordNewError(),
                "Mật khẩu chưa đáp ứng đủ các điều kiện bảo mật bên dưới.");
    }

    @Story("Đổi mật khẩu")
    @Test(description = "TC_SCR3_VAL_010: Đổi mật khẩu với nhập lại khác mật khẩu mới -> lỗi 'Mật khẩu xác nhận không khớp' dưới ô nhập lại mật khẩu mới")
    public void changePasswordMismatchedConfirmShowsError() {
        ProfilePageObject profile = openFreshProfile("Khach Doi MK Lech", randomEmail(), randomPhone());

        profile.changePassword(GlobalConstants.DEMO_PASSWORD, "New@2026", "New@2027");

        Assert.assertTrue(profile.isChangePasswordConfirmErrorDisplayed(),
                "Xac nhan khac mat khau moi phai bao loi duoi o nhap lai mat khau moi");
        Assert.assertEquals(profile.getChangePasswordConfirmError(), "Mật khẩu xác nhận không khớp");
    }

    // ---------- So dia chi ----------

    @Story("Sổ địa chỉ")
    @Test(description = "TC_SCR3_VAL_011: Thêm địa chỉ thiếu trường bắt buộc hoặc SĐT sai — trống Tên người nhận -> 'Tên người nhận không hợp lệ'; SĐT '0123' -> 'Số điện thoại không hợp lệ'; trống Địa chỉ chi tiết -> 'Địa chỉ chi tiết phải có ít nhất 5 ký tự.'; địa chỉ không được lưu")
    public void addressFormMissingOrInvalidFieldsShowFieldErrors() {
        ProfilePageObject profile = openFreshProfile("Khach Dia Chi Loi", randomEmail(), randomPhone());
        profile.waitForAddressBookVisible();
        Assert.assertEquals(profile.getAddressCount(), 0, "Tai khoan moi phai chua co dia chi nao");

        // {ten nguoi nhan, SDT, dia chi chi tiet, cau loi mong doi, o loi}
        String[][] cases = {
                {"", GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_DETAIL,
                        "Tên người nhận không hợp lệ", "recipient"},
                {"Nguyễn Văn An", "0123", GlobalConstants.SHIPPING_DETAIL,
                        "Số điện thoại không hợp lệ", "phone"},
                {"Nguyễn Văn An", GlobalConstants.DEMO_PHONE, "",
                        "Địa chỉ chi tiết phải có ít nhất 5 ký tự.", "detail"},
        };
        for (String[] testCase : cases) {
            Assert.assertTrue(profile.openAddAddressForm(), "Phai mo duoc form them dia chi");
            profile.fillAndSaveAddressForm(testCase[0], testCase[1],
                    GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                    GlobalConstants.SHIPPING_WARD, testCase[2], false);
            switch (testCase[4]) {
                case "recipient":
                    Assert.assertTrue(profile.isAddressRecipientErrorDisplayed(),
                            "Bo trong ten nguoi nhan phai bao loi duoi o ten nguoi nhan");
                    Assert.assertEquals(profile.getAddressRecipientError(), testCase[3]);
                    break;
                case "phone":
                    Assert.assertTrue(profile.isAddressPhoneErrorDisplayed(),
                            "SDT '0123' phai bao loi duoi o SDT nguoi nhan");
                    Assert.assertEquals(profile.getAddressPhoneError(), testCase[3]);
                    break;
                default:
                    Assert.assertTrue(profile.isAddressDetailErrorDisplayed(),
                            "Bo trong dia chi chi tiet phai bao loi duoi o dia chi chi tiet");
                    Assert.assertEquals(profile.getAddressDetailError(), testCase[3]);
                    break;
            }
            Assert.assertEquals(profile.getAddressCount(), 0, "Dia chi loi khong duoc luu");
            profile.cancelAddressForm();
        }
    }

    @Story("Sổ địa chỉ")
    @Test(description = "TC_SCR3_VAL_012: Thêm địa chỉ khi chưa chọn đủ tỉnh/quận/phường — không chọn tỉnh -> 'Vui lòng chọn tỉnh/thành phố.'; chỉ chọn tỉnh -> 'Vui lòng chọn quận/huyện.'; chọn tỉnh + quận -> 'Vui lòng chọn phường/xã.'; địa chỉ không được lưu")
    public void addressFormMissingLocationShowsLocationErrors() {
        ProfilePageObject profile = openFreshProfile("Khach Dia Chi Tinh", randomEmail(), randomPhone());
        profile.waitForAddressBookVisible();
        Assert.assertEquals(profile.getAddressCount(), 0, "Tai khoan moi phai chua co dia chi nao");

        // {tinh, quan, phuong, cau loi mong doi, o loi} — null = khong chon cap do.
        String[][] cases = {
                {null, null, null, "Vui lòng chọn tỉnh/thành phố.", "province"},
                {GlobalConstants.SHIPPING_PROVINCE, null, null, "Vui lòng chọn quận/huyện.", "district"},
                {GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT, null,
                        "Vui lòng chọn phường/xã.", "ward"},
        };
        for (String[] testCase : cases) {
            Assert.assertTrue(profile.openAddAddressForm(), "Phai mo duoc form them dia chi");
            profile.fillAndSaveAddressFormPartial("Nguyễn Văn An", GlobalConstants.DEMO_PHONE,
                    testCase[0], testCase[1], testCase[2], GlobalConstants.SHIPPING_DETAIL);
            switch (testCase[4]) {
                case "province":
                    Assert.assertTrue(profile.isAddressProvinceErrorDisplayed(),
                            "Khong chon tinh phai bao loi duoi o Tinh/Thanh pho");
                    Assert.assertEquals(profile.getAddressProvinceError(), testCase[3]);
                    break;
                case "district":
                    Assert.assertTrue(profile.isAddressDistrictErrorDisplayed(),
                            "Khong chon quan phai bao loi duoi o Quan/Huyen");
                    Assert.assertEquals(profile.getAddressDistrictError(), testCase[3]);
                    break;
                default:
                    Assert.assertTrue(profile.isAddressWardErrorDisplayed(),
                            "Khong chon phuong phai bao loi duoi o Phuong/Xa");
                    Assert.assertEquals(profile.getAddressWardError(), testCase[3]);
                    break;
            }
            Assert.assertEquals(profile.getAddressCount(), 0, "Dia chi thieu dia gioi khong duoc luu");
            profile.cancelAddressForm();
        }
    }

    @Story("Sổ địa chỉ")
    @Test(description = "TC_SCR3_FUNC_020: Hủy xác nhận xóa địa chỉ thì địa chỉ còn nguyên — hộp xác nhận đóng, danh sách vẫn 1 địa chỉ, huy hiệu Mặc định không đổi")
    public void cancelDeleteAddressKeepsAddress() {
        ProfilePageObject profile = openFreshProfile("Khach Huy Xoa DC", randomEmail(), randomPhone());
        profile.waitForAddressBookVisible();

        profile.addAddress("Khach Huy Xoa DC", GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_DETAIL);
        Assert.assertEquals(profile.getAddressCount(), 1, "Phai co dung 1 dia chi truoc khi thu xoa");
        String addressId = profile.getFirstAddressId();
        String defaultId = profile.getDefaultAddressId();
        Assert.assertEquals(defaultId, addressId, "Dia chi dau tien phai la mac dinh");

        profile.openDeleteAddressConfirm(addressId);
        Assert.assertTrue(profile.isDeleteConfirmDisplayed(),
                "Bam Xoa phai hien buoc xac nhan xoa");
        profile.cancelDeleteAddress();

        Assert.assertFalse(profile.isDeleteConfirmDisplayed(),
                "Bam Huy thi hop xac nhan xoa phai dong lai");
        Assert.assertEquals(profile.getAddressCount(), 1, "Huy xoa thi dia chi phai con nguyen");
        Assert.assertEquals(profile.getDefaultAddressId(), defaultId,
                "Huy hieu Mac dinh khong duoc doi sau khi huy xoa");
        Assert.assertTrue(profile.getAddressItemText(addressId).contains(GlobalConstants.SHIPPING_DETAIL),
                "Dia chi sau khi huy xoa phai giu nguyen noi dung");
    }
}
