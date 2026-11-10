package vn.dafl.tests.web;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;
import vn.dafl.pageObjects.ProfilePageObject;
import vn.dafl.pageUIs.CheckoutPageUI;

import java.util.ArrayList;
import java.util.List;

/**
 * SCR_07 (buoc 3, Pha B) — form giao hang, validation, khoi tinh tien,
 * gioi han COD va freeship tinh truoc giam gia.
 *
 * Quy uoc du lieu:
 * - Test validation/UI dung tai khoan demo (gio co the con do thua tu lan chay
 *   truoc nhung khong anh huong cac khang dinh nay); test can tong tien CHINH
 *   XAC dung tai khoan MOI dang ky (gio bat dau rong).
 * - Gia seed lay tu DbInitializer.cs (kieu tim theo ten SP duy nhat, co kiem
 *   tra gia that truoc khi them gio — neu gia seed doi, test fail ro rang
 *   thay vi xay gio sai tong).
 * - formatPrice cua FE dung Intl vi-VN ("2.000.000 ₫") nen test so sanh tien
 *   bang parseMoney hoac chuoi so co dau cham, KHONG so sanh ca ky hieu ₫/đ.
 */
@Feature("Đặt hàng và thanh toán (mở rộng)")
public class CheckoutExtendedTest extends BaseTest {

    /** Ten SP seed (DbInitializer.cs) + gia (đ) dung de ghep gio dung tong can thiet. */
    private static final String[][] CART_EXACT_2M = {
            {"Velvet Lip Tint", "289000", "1"},
            {"Zero Matte Lipstick", "199000", "5"},
            {"Color Sensational", "179000", "4"}
    }; // 289.000 + 995.000 + 716.000 = 2.000.000d dung (ton kho seed 95/120/110)
    private static final String[][] CART_OVER_2M = {
            {"Velvet Lip Tint", "289000", "1"},
            {"Zero Matte Lipstick", "199000", "2"},
            {"Peripera Ink Glasting", "219000", "6"}
    }; // 289.000 + 398.000 + 1.314.000 = 2.001.000d (xem ghi chu o test 025)

    /** Dang nhap demo, them SP dau tien vao gio, mo /cart, tra ve form checkout. */
    private CheckoutPageObject openCheckoutFormWithOneProduct() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        return checkout;
    }

    /** Tim tung SP theo tu khoa duy nhat, kiem tra dung gia seed, them 1 cai vao gio. */
    private List<String> addProductsByKeyword(HomePageObject homePage, String[][] items) {
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        List<String> ids = new ArrayList<>();
        for (String[] item : items) {
            homePage.searchProduct(item[0]);
            catalog.waitForProductsLoaded();
            String productId = catalog.getFirstProductId();
            Assert.assertNotNull(productId, "Phai tim thay san pham voi tu khoa '" + item[0] + "'");
            Assert.assertEquals(catalog.getProductPrice(productId), Long.parseLong(item[1]),
                    "Gia SP tim duoc voi tu khoa '" + item[0] + "' phai dung gia seed " + item[1] + "d");
            int badgeBefore = homePage.getCartBadgeCount();
            catalog.addToCart(productId);
            homePage.waitForCartBadgeCount(badgeBefore + 1);
            ids.add(productId);
        }
        return ids;
    }

    /** Mo /cart va dat so luong tung dong theo bang items (chi goi khi so luong > 1). */
    private CartDrawerPageObject openCartAndSetQuantities(HomePageObject homePage,
                                                          List<String> productIds, String[][] items) {
        CartDrawerPageObject cart = homePage.openCart();
        for (int i = 0; i < productIds.size(); i++) {
            int quantity = Integer.parseInt(items[i][2]);
            if (quantity > 1) {
                cart.setItemQuantity(productIds.get(i), quantity);
                cart.waitForItemQuantity(productIds.get(i), quantity);
            }
        }
        return cart;
    }

    /** Xay gio dung tong tu bang items tren tai khoan moi (gio bat dau rong); tra ve gio hang. */
    private CartDrawerPageObject buildExactCart(String[][] items, long expectedSubtotal) {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Ghep Gio", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = addProductsByKeyword(homePage, items);
        CartDrawerPageObject cart = openCartAndSetQuantities(homePage, ids, items);
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), expectedSubtotal,
                "Tam tinh gio phai dung " + expectedSubtotal + "d, dang thay: " + cart.getSubtotalText());
        return cart;
    }

    // ---------- Form giao hang: hien thi ----------

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_001: Form giao hàng hiển thị đủ 8 trường + bộ đếm ghi chú \"{n}/200\"")
    public void shippingFormDisplaysAllEightFieldsAndNoteCounter() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.ensureNewAddressFormVisible();

        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_EMAIL_INPUT),
                "Phai thay o Email (shipping-email-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_INPUT),
                "Phai thay o Nguoi nhan (shipping-recipient-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_PHONE_INPUT),
                "Phai thay o So dien thoai (shipping-phone-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_PROVINCE_SELECT),
                "Phai thay o Tinh/TP (shipping-province-select)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_DISTRICT_SELECT),
                "Phai thay o Quan/Huyen (shipping-district-select)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Phai thay o Phuong/Xa (shipping-ward-select)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_DETAIL_INPUT),
                "Phai thay o So nha ten duong (shipping-detail-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_NOTE_INPUT),
                "Phai thay o Ghi chu (shipping-note-input)");
        Assert.assertEquals(checkout.getShippingNoteCounterText(), "0/200",
                "Bo dem ghi chu luc chua nhap phai la \"0/200\", dang thay: "
                        + checkout.getShippingNoteCounterText());
    }

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_002: Khối địa chỉ đã lưu hiển thị danh sách + địa chỉ mặc định có badge \"MẶC ĐỊNH\" + nút thêm địa chỉ mới")
    public void savedAddressListShowsDefaultBadgeAndAddButton() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Co Dia Chi", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();
        profile.addAddress("Khach Co Dia Chi", GlobalConstants.DEMO_PHONE, "Số 10 Đường Một");
        profile.addAddress("Khach Co Dia Chi", GlobalConstants.DEMO_PHONE, "Số 20 Đường Hai");
        String defaultAddressId = profile.getDefaultAddressId();
        Assert.assertNotNull(defaultAddressId, "Dia chi dau tien them phai tu thanh mac dinh");

        getDriver().get(GlobalConstants.getAppUrl() + "/");
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertTrue(checkout.isSavedAddressListDisplayed(),
                "Phai thay khoi dia chi da luu (shipping-address-list)");
        Assert.assertEquals(checkout.getSavedAddressOptionCount(), 2,
                "Tai khoan co 2 dia chi thi khoi dia chi da luu phai hien dung 2 lua chon");
        String defaultOptionText = checkout.getSavedAddressOptionText(defaultAddressId);
        Assert.assertTrue(defaultOptionText.contains("MẶC ĐỊNH"),
                "Dia chi mac dinh phai co badge \"MẶC ĐỊNH\", dang thay: " + defaultOptionText);
        Assert.assertTrue(defaultOptionText.contains("Khach Co Dia Chi"),
                "Dia chi mac dinh phai hien ten nguoi nhan, dang thay: " + defaultOptionText);
        Assert.assertTrue(defaultOptionText.contains(GlobalConstants.DEMO_PHONE),
                "Dia chi mac dinh phai hien SDT, dang thay: " + defaultOptionText);
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_NEW_ADDRESS_BTN),
                "Phai thay nut them dia chi moi (shipping-new-address-btn)");
    }

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_003: Chọn địa chỉ theo cascade Tỉnh → Quận → Phường; Quận/Phường bị khóa trước khi chọn cấp trên; đổi Tỉnh thì Quận/Phường reset về rỗng")
    public void addressCascadeLocksLowerLevelsAndResetsOnProvinceChange() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.ensureNewAddressFormVisible();

        Assert.assertFalse(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_DISTRICT_SELECT),
                "Chua chon Tinh thi o Quan/Huyen phai bi khoa");
        Assert.assertFalse(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Chua chon Quan thi o Phuong/Xa phai bi khoa");

        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_PROVINCE_SELECT, "Thành phố Hà Nội");
        sleepInSecond(1);
        Assert.assertTrue(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_DISTRICT_SELECT),
                "Chon Tinh xong o Quan/Huyen phai mo khoa de chon");
        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_DISTRICT_SELECT, "Quận Cầu Giấy");
        sleepInSecond(1);
        Assert.assertTrue(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Chon Quan xong o Phuong/Xa phai mo khoa de chon");
        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_WARD_SELECT, "Phường Dịch Vọng");
        Assert.assertTrue(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Phuong da chon xong van phai mo de doi neu can");

        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_PROVINCE_SELECT, "Thành phố Hồ Chí Minh");
        sleepInSecond(1);
        Assert.assertEquals(checkout.getSelectedValue(CheckoutPageUI.SHIPPING_DISTRICT_SELECT), "",
                "Doi Tinh thi Quan/Huyen da chon phai reset ve rong");
        Assert.assertEquals(checkout.getSelectedValue(CheckoutPageUI.SHIPPING_WARD_SELECT), "",
                "Doi Tinh thi Phuong/Xa da chon phai reset ve rong");
    }

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_004: Submit form trống -> lỗi đỏ từng ô + focus vào ô lỗi đầu tiên (Người nhận) + không tạo đơn")
    public void emptyFormSubmitShowsErrorsAndFocusesFirstError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.ensureNewAddressFormVisible();
        checkout.submitOrder();
        sleepInSecond(1); // cho focusFirstError cuon + focus o loi dau tien

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Form trong phai bao loi duoi o Nguoi nhan");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PHONE_ERROR),
                "Form trong phai bao loi duoi o So dien thoai");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Form trong phai bao loi duoi o Email");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PROVINCE_ERROR),
                "Form trong phai bao loi duoi o Tinh/TP");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DISTRICT_ERROR),
                "Form trong phai bao loi duoi o Quan/Huyen");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_WARD_ERROR),
                "Form trong phai bao loi duoi o Phuong/Xa");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DETAIL_ERROR),
                "Form trong phai bao loi duoi o Dia chi chi tiet");

        // FIELD_ORDER cua CheckoutForm.tsx: recipient dung dau -> o loi dau tien duoc focus
        // la o Nguoi nhan (shipping-recipient-input).
        Object activeTestId = checkout.executeScript(
                "return document.activeElement ? document.activeElement.getAttribute('data-testid') : '';");
        Assert.assertEquals(String.valueOf(activeTestId), "shipping-recipient-input",
                "Focus phai roi vao o loi dau tien (Nguoi nhan), dang o: " + activeTestId);
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(),
                "Submit form loi thi form van hien (khong tao don, khong chuyen trang)");
    }

    // ---------- Luu dia chi mac dinh ----------

    @Story("Địa chỉ đã lưu")
    @Test(description = "TC_SCR7_FUNC_006: Tick \"Lưu làm địa chỉ mặc định\" rồi đặt COD -> địa chỉ mới xuất hiện trong Sổ địa chỉ và là mặc định")
    public void saveAsDefaultCheckboxCreatesDefaultAddressAfterOrder() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertTrue(checkout.isSaveDefaultCheckboxDisplayed(),
                "Tai khoan da dang nhap phai thay checkbox \"Lưu làm địa chỉ mặc định\"");
        String newDetail = "Số 99 Đường Mới " + (System.currentTimeMillis() % 100000);
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE,
                GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD, newDetail, null);
        checkout.clickSaveDefaultCheckbox();
        checkout.selectCodPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");

        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();
        String defaultAddressId = profile.getDefaultAddressId();
        Assert.assertNotNull(defaultAddressId, "Sau khi dat hang phai co dia chi mac dinh trong so");
        boolean found = false;
        for (String addressId : profile.getAddressIds()) {
            if (profile.getAddressItemText(addressId).contains(newDetail)) {
                found = true;
                Assert.assertEquals(addressId, defaultAddressId,
                        "Dia chi vua dat (" + newDetail + ") phai la dia chi mac dinh");
            }
        }
        Assert.assertTrue(found, "Dia chi vua dat (" + newDetail + ") phai xuat hien trong So dia chi");
    }

    // ---------- Validation tung truong ----------

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_007: Để trống tên người nhận -> lỗi \"Vui lòng nhập họ tên người nhận.\" + không tạo đơn")
    public void emptyRecipientShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm("", GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "De trong Nguoi nhan phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Vui lòng nhập họ tên người nhận.", "Loi Nguoi nhan phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_008: Tên người nhận \"A\" (1 ký tự) -> lỗi \"Họ tên người nhận phải có ít nhất 2 ký tự.\" + không tạo đơn")
    public void oneCharRecipientShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm("A", GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Ten Nguoi nhan 1 ky tu phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Họ tên người nhận phải có ít nhất 2 ký tự.", "Loi Nguoi nhan phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_009: Để trống số điện thoại -> lỗi \"Vui lòng nhập số điện thoại.\" + không tạo đơn")
    public void emptyPhoneShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, "",
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PHONE_ERROR),
                "De trong SDT phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_PHONE_ERROR),
                "Vui lòng nhập số điện thoại.", "Loi SDT phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_011: Để trống email -> lỗi \"Vui lòng nhập email.\" + không tạo đơn")
    public void emptyEmailShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, "", GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "De trong Email phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Vui lòng nhập email.", "Loi Email phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_012: Email \"khachhang@\" sai định dạng -> lỗi \"Email không đúng định dạng.\" + không tạo đơn")
    public void invalidEmailFormatShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, "khachhang@", GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Email sai dinh dang phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Email không đúng định dạng.", "Loi Email phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_013: Không chọn Tỉnh/TP -> lỗi \"Vui lòng chọn Tỉnh / Thành phố.\" + không tạo đơn")
    public void missingProvinceShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingFormPartial(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, null, null, null, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PROVINCE_ERROR),
                "Khong chon Tinh/TP phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_PROVINCE_ERROR),
                "Vui lòng chọn Tỉnh / Thành phố.", "Loi Tinh/TP phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_014: Chọn Tỉnh nhưng không chọn Quận/Huyện -> lỗi \"Vui lòng chọn Quận / Huyện.\" + không tạo đơn")
    public void missingDistrictShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingFormPartial(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE, null, null,
                GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DISTRICT_ERROR),
                "Khong chon Quan/Huyen phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_DISTRICT_ERROR),
                "Vui lòng chọn Quận / Huyện.", "Loi Quan/Huyen phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_015: Chọn Tỉnh + Quận nhưng không chọn Phường/Xã -> lỗi \"Vui lòng chọn Phường / Xã.\" + không tạo đơn")
    public void missingWardShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingFormPartial(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE,
                GlobalConstants.SHIPPING_DISTRICT, null, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_WARD_ERROR),
                "Khong chon Phuong/Xa phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_WARD_ERROR),
                "Vui lòng chọn Phường / Xã.", "Loi Phuong/Xa phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_017: Địa chỉ chi tiết \"12 A\" (4 ký tự) -> lỗi \"Địa chỉ chi tiết phải có ít nhất 5 ký tự.\" + không tạo đơn")
    public void shortDetailAddressShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE,
                GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD, "12 A", null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DETAIL_ERROR),
                "Dia chi chi tiet 4 ky tu phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_DETAIL_ERROR),
                "Địa chỉ chi tiết phải có ít nhất 5 ký tự.", "Loi dia chi chi tiet phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    // ---------- Khoi tinh tien + phuong thuc ----------

    @Story("Khối tính tiền và phương thức")
    @Test(description = "TC_SCR7_UI_026: Khối tính tiền tách dòng Tạm tính/Phí vận chuyển/Tổng thanh toán + đủ 2 phương thức COD/PayOS (mặc định COD) + KHÔNG có tab \"Nhúng Cổng PayOS\" hay iframe")
    public void pricingSummaryAndPaymentMethodsDisplayedWithoutEmbeddedGateway() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();

        Assert.assertTrue(isTextVisibleOnPage("Tạm tính"), "Khoi tinh tien phai co dong Tam tinh");
        Assert.assertTrue(isTextVisibleOnPage("Phí vận chuyển:"), "Khoi tinh tien phai co dong Phi van chuyen");
        Assert.assertTrue(isTextVisibleOnPage("Tổng thanh toán:"), "Khoi tinh tien phai co dong Tong thanh toan");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.PAYMENT_METHOD_COD),
                "Phai thay lua chon COD (payment-method-cod)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.PAYMENT_METHOD_PAYOS),
                "Phai thay lua chon PayOS (payment-method-payos)");
        Assert.assertTrue(checkout.isCodPaymentSelected(),
                "Phuong thuc mac dinh phai la COD");
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán khi nhận hàng (COD)"),
                "Lua chon COD phai ghi dung ten");
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán trực tuyến PayOS"),
                "Lua chon PayOS phai ghi dung ten");
        Assert.assertFalse(isTextVisibleOnPage("Nhúng Cổng PayOS"),
                "Form checkout khong duoc con tab/chu \"Nhúng Cổng PayOS\"");
        Assert.assertEquals(checkout.getListWebElements(CheckoutPageUI.PAYOS_ANY_IFRAME).size(), 0,
                "Form checkout khong duoc con iframe nhung cong thanh toan nao");
    }

    @Story("Khối tính tiền và phương thức")
    @Test(description = "TC_SCR7_UI_033: Nhãn nút submit đổi theo phương thức: COD -> \"Đặt hàng ({tổng})\", PayOS -> \"Thanh toán trực tiếp PayOS ({tổng})\", chưa đăng nhập -> \"Đăng nhập để thanh toán ({tổng})\"")
    public void submitButtonLabelChangesByPaymentMethodAndLoginState() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");

        // Ca 1: COD mac dinh.
        String codLabel = checkout.getSubmitButtonText();
        Assert.assertTrue(codLabel.startsWith("Đặt hàng ("),
                "Chon COD nut phai ghi \"Đặt hàng ({tổng})\", dang thay: " + codLabel);

        // Ca 2: chuyen sang PayOS.
        checkout.selectPayosPayment();
        String payosLabel = checkout.getSubmitButtonText();
        Assert.assertTrue(payosLabel.startsWith("Thanh toán trực tiếp PayOS ("),
                "Chon PayOS nut phai ghi \"Thanh toán trực tiếp PayOS ({tổng})\", dang thay: " + payosLabel);

        // Ca 3: chua dang nhap (dang xuat roi them gio bang tai khoan khach).
        homePage.logout();
        getDriver().get(GlobalConstants.getAppUrl() + "/");
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        sleepInSecond(1);
        homePage.openCart();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Khach van thay form thanh toan trong trang gio");
        String guestLabel = checkout.getSubmitButtonText();
        Assert.assertTrue(guestLabel.startsWith("Đăng nhập để thanh toán ("),
                "Chua dang nhap nut phai ghi \"Đăng nhập để thanh toán ({tổng})\", dang thay: " + guestLabel);
    }

    // ---------- Gioi han COD ----------

    @Story("Giới hạn COD")
    @Test(description = "TC_SCR7_FUNC_024: Đơn có tổng cuối đúng 2.000.000đ vẫn chọn được COD + không hiện ghi chú giới hạn")
    public void codStillAvailableAtExactlyTwoMillion() {
        CartDrawerPageObject cart = buildExactCart(CART_EXACT_2M, 2_000_000L);
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 2_000_000L,
                "Tong cuoi phai dung 2.000.000d (tam tinh >= 299.000d nen mien phi ship), dang thay: "
                        + cart.getFinalTotalText());

        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertTrue(checkout.isCodPaymentEnabled(),
                "Tong dung 2.000.000d thi radio COD phai van chon duoc (codAvailable = finalTotal <= 2.000.000d)");
        Assert.assertFalse(checkout.isCodLimitNoteDisplayed(),
                "Tong dung 2.000.000d thi khong duoc hien ghi chu gioi han COD (payment-cod-limit-note)");
    }

    @Story("Giới hạn COD")
    @Test(description = "TC_SCR7_FUNC_025: Đơn tổng cuối 2.001.000đ (trên 2.000.000đ) -> radio COD bị disable + hiện ghi chú giới hạn + tự chuyển sang PayOS")
    public void codDisabledAboveTwoMillionWithNoteAndAutoSwitchToPayos() {
        // Luu y: 2.000.001d theo dung case manual KHONG ghep duoc tu gia seed
        // (moi gia seed deu la boi cua 1.000d) — dung 2.001.000d, muc tren 2.000.000d
        // gan nhat ghep duoc, van kich hoat dung luat codAvailable = finalTotal <= 2.000.000d.
        CartDrawerPageObject cart = buildExactCart(CART_OVER_2M, 2_001_000L);
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 2_001_000L,
                "Tong cuoi phai dung 2.001.000d (mien phi ship), dang thay: " + cart.getFinalTotalText());

        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertFalse(checkout.isCodPaymentEnabled(),
                "Tong tren 2.000.000d thi radio COD phai bi disable");
        Assert.assertTrue(checkout.isCodLimitNoteDisplayed(),
                "Tong tren 2.000.000d phai hien ghi chu gioi han COD (payment-cod-limit-note)");
        String note = checkout.getCodLimitNoteText();
        Assert.assertTrue(note.contains("Đơn hàng trên 2.000.000"),
                "Ghi chu phai neu dung moc 2.000.000d, dang thay: " + note);
        Assert.assertTrue(note.contains("chỉ hỗ trợ thanh toán trực tuyến PayOS."),
                "Ghi chu phai neu chi ho tro PayOS, dang thay: " + note);
        Assert.assertTrue(checkout.isPayosPaymentSelected(),
                "Tong tren 2.000.000d thi phuong thuc phai tu chuyen sang PayOS");
    }

    // ---------- Dieu huong ----------

    @Story("Điều hướng")
    @Test(description = "TC_SCR7_FUNC_052: Truy cập trực tiếp /checkout bị chuyển về /cart (không có trang checkout riêng)")
    public void directCheckoutUrlRedirectsToCart() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/checkout");
        sleepInSecond(2); // cho route <Navigate to="/cart" replace /> chay
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/cart"),
                "Truy cap /checkout phai chuyen ve /cart, dang o: " + getDriver().getCurrentUrl());
        Assert.assertFalse(getDriver().getCurrentUrl().contains("/checkout"),
                "URL sau chuyen huong khong duoc con /checkout");
    }
}
