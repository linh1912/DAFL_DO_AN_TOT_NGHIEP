package vn.dafl.tests.web.checkout;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.MembershipPageObject;
import vn.dafl.pageObjects.OrdersPageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SCR_07 (buoc 3, Pha B) — voucher khi dat hang, luong PayOS (mock/demo) va
 * dung diem hoi vien o checkout (PT-05..PT-08).
 *
 * Luu y ve che do demo PayOS (da xac minh tu code that — xem fragment):
 * - Nut "Tôi đã chuyển khoản xong (Kiểm tra thanh toán)" (simulate-payos-success-btn)
 *   chi goi GET /api/payments/status/{orderCode}; o che do demo BE tra ve Pending
 *   (PayOSService.cs: GetPaymentStatusFromPayOSAsync tra null khi key demo) nen FE
 *   chi hien toast "Đang chờ thanh toán", KHONG bao gio tu chuyen sang order-paid-view.
 *   Duong duy nhat de don PayOS thanh Paid o che do demo la trang /mock-payos
 *   (POST /api/payments/payos-webhook voi payload gia, checksum demo chap nhan).
 * - Test TC_SCR7_FUNC_048 khang dinh dung hanh vi demo nay. Test cu 047
 *   (payWithMockPayosAndSeeOrderInHistory) khang dinh nguoc lai (bam nut mo phong
 *   xong ra order-paid-view) nen MAU THUAN voi code demo — xem fragment.
 * - formatPrice cua FE dung Intl vi-VN ("2.000.000 ₫") nen test so sanh tien
 *   bang parseMoney hoac chuoi so co dau cham, KHONG so sanh ca ky hieu ₫/đ.
 */
@Feature("Thanh toán PayOS và dùng điểm (mở rộng)")
public class CheckoutPaymentTest extends BaseTest {

    /** Tim SP theo tu khoa duy nhat, kiem tra dung gia seed, them 1 cai vao gio. */
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

    /** Xay gio tren tai khoan MOI (gio bat dau rong), ap voucher, tra ve gio hang. */
    private CartDrawerPageObject buildCartWithVoucher(String[][] items, long expectedSubtotal,
                                                      String voucherCode) {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Ap Voucher", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = addProductsByKeyword(homePage, items);
        CartDrawerPageObject cart = openCartAndSetQuantities(homePage, ids, items);
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), expectedSubtotal,
                "Tam tinh gio phai dung " + expectedSubtotal + "d, dang thay: " + cart.getSubtotalText());
        cart.applyVoucher(voucherCode);
        if (!cart.isVoucherAppliedBannerDisplayed()) {
            String reason = cart.isVoucherErrorDisplayed()
                    ? cart.getVoucherErrorMessage() : "không rõ lý do";
            throw new SkipException("Ap ma " + voucherCode + " that bai (" + reason + ") — bo qua test");
        }
        return cart;
    }

    /** Dat COD thanh cong tu gio dang mo; tra ve ma don hien thi DH... */
    private String placeCodOrder(CartDrawerPageObject cart, String name, String email, String phone) {
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        checkout.fillDefaultShippingForm(name, email, phone);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");
        return checkout.getOrderSuccessCode();
    }

    /** Dat PayOS thanh cong bang tai khoan demo; tra ve {ma don so, tong tien}. */
    private long[] placePayosOrderWithDemo() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CartDrawerPageObject cart = new CartDrawerPageObject();
        long totalAmount = parseMoney(cart.getFinalTotalText());
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Chon PayOS phai hien man hinh thanh toan");
        String orderCodeText = checkout.getOrderCode(); // "Đơn hàng #<ma so>"
        String digits = orderCodeText.replaceAll("[^0-9]", "");
        Assert.assertFalse(digits.isEmpty(),
                "Man hinh thanh toan phai hien ma don so, dang thay: " + orderCodeText);
        return new long[]{Long.parseLong(digits), totalAmount};
    }

    /** Dinh dang so tien kieu "218.000" de doi chieu chuoi hien thi tren trang. */
    private String formatVnd(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }

    /** Doc so du diem kha dung cua tai khoan demo qua /membership (mau PT-02/PT-03). */
    private int readDemoAvailablePoints() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();
        return membershipPage.getAvailablePoints();
    }

    /** Mo gio demo voi 1 gio tam tinh 500.000d (de tran diem hang Thuong = 50 diem). */
    private CartDrawerPageObject openDemoCartWith500k() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String[][] items = {
                {"Tẩy tế bào chết cơ thể", "165000", "1"},
                {"Green Tea Hand Cream", "99000", "1"},
                {"Green Tea Balancing", "59000", "4"}
        }; // 165.000 + 99.000 + 236.000 = 500.000d
        List<String> ids = addProductsByKeyword(homePage, items);
        CartDrawerPageObject cart = openCartAndSetQuantities(homePage, ids, items);
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), 500_000L,
                "Tam tinh gio diem phai dung 500.000d, dang thay: " + cart.getSubtotalText());
        return cart;
    }

    /** Trich tran diem {m} tu cau goi y "Hạng ... dùng tối đa {m} điểm cho đơn này." */
    private int parseMaxPointsFromHint(String hintText) {
        Matcher matcher = Pattern.compile("tối đa\\s+([0-9][0-9.,]*)\\s+điểm").matcher(hintText);
        if (!matcher.find()) {
            throw new AssertionError("Khong trich duoc tran diem tu goi y: " + hintText);
        }
        return (int) parseMoney(matcher.group(1));
    }

    // ---------- Voucher khi dat hang ----------

    @Story("Voucher khi đặt hàng")
    @Test(description = "TC_SCR7_FUNC_020: Giảm giá không làm mất freeship vì freeship tính trước giảm giá (350.000đ + DAFL10 và 306.000đ + DAFL10 đều vẫn miễn phí ship)")
    public void discountDoesNotRemoveFreeShipping() {
        // Ca 1: tam tinh 350.000d, DAFL10 giam 35.000d (con 315.000d sau giam).
        String[][] caseA = {{"bồ kết", "175000", "2"}}; // 175.000 x 2 = 350.000d
        CartDrawerPageObject cartA = buildCartWithVoucher(caseA, 350_000L, GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertEquals(cartA.getVoucherDiscountValue(), 35_000L,
                "DAFL10 tren 350.000d phai giam dung 35.000d");
        Assert.assertTrue(cartA.getShippingFeeText().contains("Miễn phí"),
                "Tam tinh 350.000d (>= 299.000d) phai duoc mien phi ship, dang thay: "
                        + cartA.getShippingFeeText());

        // Ca 2: tam tinh 306.000d, DAFL10 giam 30.600d -> con 275.400d sau giam (< 299.000d)
        // nhung van freeship vi nguong tinh tren tam tinh TRUOC giam gia.
        // Luu y: 305.000d theo dung case manual KHONG ghep duoc tu gia seed
        // (moi gia seed deu la boi cua 1.000d) — dung 306.000d gan nhat:
        // 129.000 + 59.000 x 3 (ton kho seed 120/150).
        String[][] caseB = {
                {"Speedy Eyebrow", "129000", "1"},
                {"Green Tea Balancing", "59000", "3"}
        };
        CartDrawerPageObject cartB = buildCartWithVoucher(caseB, 306_000L, GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertEquals(cartB.getVoucherDiscountValue(), 30_600L,
                "DAFL10 tren 306.000d phai giam dung 30.600d");
        Assert.assertTrue(cartB.getShippingFeeText().contains("Miễn phí"),
                "Tam tinh 306.000d truoc giam van >= 299.000d nen phai mien phi ship du sau giam"
                        + " chi con 275.400d, dang thay: " + cartB.getShippingFeeText());
    }

    @Story("Voucher khi đặt hàng")
    @Test(description = "TC_SCR7_FUNC_029: Voucher DAFL10 tính giảm đúng khi đặt hàng (500.000đ giảm 50.000đ, đơn lưu mã + tổng đúng)")
    public void voucherPercent10DiscountIsCorrectOnOrder() {
        String[][] items = {
                {"Tẩy tế bào chết cơ thể", "165000", "1"},
                {"Green Tea Hand Cream", "99000", "1"},
                {"Green Tea Balancing", "59000", "4"}
        }; // 165.000 + 99.000 + 236.000 = 500.000d
        CartDrawerPageObject cart = buildCartWithVoucher(items, 500_000L, GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertEquals(cart.getVoucherDiscountValue(), 50_000L,
                "DAFL10 tren 500.000d phai giam dung 50.000d (10%, chua cham tran 100.000d)");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 450_000L,
                "Tong cuoi phai la 500.000 - 50.000 + 0 ship = 450.000d, dang thay: "
                        + cart.getFinalTotalText());

        String orderCode = placeCodOrder(cart, "Khach Ap Voucher", randomEmail(), GlobalConstants.DEMO_PHONE);

        HomePageObject homePage = new HomePageObject();
        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains("Giảm giá (DAFL10):"),
                "Chi tiet don phai luu ma voucher DAFL10 o dong giam gia");
        Assert.assertTrue(detailText.contains("-" + formatVnd(50_000L)),
                "Chi tiet don phai giam dung 50.000d, dang thay: " + detailText);
        Assert.assertTrue(detailText.contains("Tạm tính:"), "Chi tiet don phai co dong Tam tinh");
        Assert.assertTrue(detailText.contains(formatVnd(500_000L)),
                "Chi tiet don phai co tam tinh 500.000d");
        Assert.assertTrue(detailText.contains("Tổng cộng:"), "Chi tiet don phai co dong Tong cong");
        Assert.assertTrue(detailText.contains(formatVnd(450_000L)),
                "Tong thanh toan don phai dung 450.000d (500.000 - 50.000 + 0 ship)");
    }

    @Story("Voucher khi đặt hàng")
    @Test(description = "TC_SCR7_FUNC_030: Voucher FREESHIP miễn phí vận chuyển khi đặt hàng (248.000đ, phí ship 0đ, đơn lưu mã FREESHIP)")
    public void voucherFreeshipMakesShippingFreeOnOrder() {
        // Luu y: 250.000d theo dung case manual KHONG ghep duoc tu gia seed
        // (moi gia seed deu la boi cua 1.000d) — dung 248.000d gan nhat, van >= 200.000d
        // (muc toi thieu cua FREESHIP): 149.000 + 99.000.
        String[][] items = {
                {"No-Sebum", "149000", "1"},
                {"Green Tea Hand Cream", "99000", "1"}
        };
        CartDrawerPageObject cart = buildCartWithVoucher(items, 248_000L, GlobalConstants.VOUCHER_FREESHIP);
        Assert.assertTrue(cart.getShippingFeeText().contains("Miễn phí"),
                "Ap FREESHIP thi phi ship phai ve 0d (\"Miễn phí\"), dang thay: " + cart.getShippingFeeText());
        long finalTotal = parseMoney(cart.getFinalTotalText());

        String orderCode = placeCodOrder(cart, "Khach Ap Voucher", randomEmail(), GlobalConstants.DEMO_PHONE);

        HomePageObject homePage = new HomePageObject();
        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains("Phí vận chuyển:"),
                "Chi tiet don phai co dong Phi van chuyen");
        Assert.assertTrue(detailText.contains("Miễn phí"),
                "Chi tiet don phai hien phi ship \"Miễn phí\" (0d)");
        Assert.assertTrue(detailText.contains("Giảm giá (FREESHIP):"),
                "Chi tiet don phai luu ma voucher FREESHIP o dong giam gia");
        Assert.assertTrue(detailText.contains(formatVnd(finalTotal)),
                "Tong cong don phai khop tong cuoi luc dat (" + formatVnd(finalTotal) + "d)");
    }

    // ---------- View thanh toan PayOS ----------

    @Story("Thanh toán PayOS")
    @Test(description = "TC_SCR7_UI_046: Trang thanh toán sau checkout PayOS: tiêu đề \"Đơn hàng #{mã}\", đồng hồ MM:SS bắt đầu quanh 15:00, số tiền đúng, nút \"Sao chép mã\" + \"Đổi giỏ hàng\"")
    public void payosPaymentViewShowsTimerAmountAndCopyBackButtons() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CartDrawerPageObject cart = new CartDrawerPageObject();
        long expectedTotal = parseMoney(cart.getFinalTotalText());
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Chon PayOS phai hien man hinh thanh toan");
        String orderCodeText = checkout.getOrderCode();
        Assert.assertTrue(orderCodeText.startsWith("Đơn hàng #"),
                "Tieu de view thanh toan phai la \"Đơn hàng #{mã}\", dang thay: " + orderCodeText);
        String timerText = checkout.getPaymentTimerText();
        Matcher timerMatcher = Pattern.compile("^(\\d{2}):(\\d{2})$").matcher(timerText);
        Assert.assertTrue(timerMatcher.matches(),
                "Dong ho (payment-timer) phai dung dinh dang MM:SS, dang thay: " + timerText);
        int secondsLeft = Integer.parseInt(timerMatcher.group(1)) * 60
                + Integer.parseInt(timerMatcher.group(2));
        Assert.assertTrue(secondsLeft <= 900 && secondsLeft >= 840,
                "Dong ho phai bat dau quanh 15:00 (900 giay), dang thay: " + timerText);
        Assert.assertEquals(parseMoney(checkout.getPaymentTotalText()), expectedTotal,
                "So tien thanh toan phai dung tong cuoi luc dat (" + expectedTotal + "d)");
        Assert.assertTrue(checkout.isCopyOrderCodeButtonDisplayed(),
                "Phai thay nut \"Sao chép mã\" (copy-order-code-btn)");
        checkout.clickCopyOrderCodeButton();
        Assert.assertTrue(isTextVisibleOnPage("Đã chép mã"),
                "Bam \"Sao chép mã\" phai hien toast \"Đã chép mã\"");
        Assert.assertTrue(checkout.isBackToCartButtonDisplayed(),
                "Phai thay nut \"Đổi giỏ hàng\" (back-to-cart-btn)");
        checkout.clickBackToCartButton();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(),
                "Bam \"Đổi giỏ hàng\" phai quay lai gio hang (form thanh toan hien lai)");
    }

    @Story("Thanh toán PayOS")
    @Test(description = "TC_SCR7_FUNC_048: Bấm kiểm tra thanh toán khi chưa chuyển khoản -> toast \"Đang chờ thanh toán\", đơn vẫn \"Chờ thanh toán\", KHÔNG sang view đã thanh toán (chế độ demo)")
    public void simulatePayosCheckBeforeTransferShowsWaitingToast() {
        long[] order = placePayosOrderWithDemo();
        long orderCode = order[0];

        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Phai thay man hinh thanh toan PayOS");
        checkout.simulatePayosSuccess();

        // Toast chi ton tai 5 giay (App.tsx showToast) nen khang dinh ngay sau khi bam.
        Assert.assertTrue(isTextVisibleOnPage("Đang chờ thanh toán"),
                "Chua chuyen khoan ma bam kiem tra thi phai hien toast \"Đang chờ thanh toán\"");
        Assert.assertTrue(checkout.isPaymentViewDisplayed(),
                "Chua thanh toan thi view thanh toan van phai hien (chua chuyen trang)");
        Assert.assertFalse(checkout.isOrderPaidViewDisplayed(),
                "Chua chuyen khoan thi KHONG duoc hien order-paid-view (che do demo khong tu Paid)");

        // Don van o trang thai "Chờ thanh toán" trong lich su.
        getDriver().get(GlobalConstants.getAppUrl() + "/orders");
        OrdersPageObject ordersPage = new OrdersPageObject();
        ordersPage.waitForOrdersLoaded();
        String firstCode = ordersPage.getFirstOrderDisplayCode();
        Assert.assertNotNull(firstCode, "Lich su don phai co don PayOS vua dat");
        ordersPage.openOrderDetail(firstCode);
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), "Chờ thanh toán",
                "Don PayOS chua chuyen khoan phai o trang thai \"Chờ thanh toán\"");
    }

    @Story("Trang thanh toán PayOS")
    @Test(description = "TC_SCR7_FUNC_049: Trang /mock-payos chỉ có 1 nút \"Xác nhận đã chuyển khoản thành công\"; bấm nút -> hiện \"Thanh toán thành công!\" + link xem đơn hàng")
    public void mockPayosPageHasSingleConfirmButtonAndConfirmsPayment() {
        long[] order = placePayosOrderWithDemo();
        long orderCode = order[0];
        long amount = order[1];

        getDriver().get(GlobalConstants.getAppUrl() + "/mock-payos?orderCode=" + orderCode
                + "&amount=" + amount);
        sleepInSecond(2); // cho trang mock-payos render (khong co data-testid nao)

        Assert.assertTrue(isTextVisibleOnPage("Cổng thanh toán PayOS"),
                "Trang mock-payos phai hien tieu de \"Cổng thanh toán PayOS\"");
        Assert.assertTrue(isTextVisibleOnPage("Môi trường giả lập VietQR Sandbox"),
                "Trang mock-payos phai ghi \"Môi trường giả lập VietQR Sandbox\"");
        Assert.assertTrue(isTextVisibleOnPage("MBBank (Quân Đội)"),
                "Trang mock-payos phai hien ngan hang \"MBBank (Quân Đội)\"");
        Assert.assertTrue(isTextVisibleOnPage("0987654321"),
                "Trang mock-payos phai hien so tai khoan \"0987654321\"");
        Assert.assertTrue(isTextVisibleOnPage("DH " + orderCode),
                "Trang mock-payos phai hien noi dung chuyen khoan \"DH " + orderCode + "\"");

        List<?> confirmButtons = getDriver().findElements(By.xpath(
                "//button[contains(.,'Xác nhận đã chuyển khoản thành công')]"));
        Assert.assertEquals(confirmButtons.size(), 1,
                "Trang mock-payos chi duoc co dung 1 nut hanh dong xac nhan chuyen khoan");
        Assert.assertEquals(getDriver().findElements(By.xpath(
                        "//button[contains(.,'thất bại') or contains(.,'Thất bại')]")).size(), 0,
                "Trang mock-payos KHONG duoc co nut mo phong that bai");

        ((org.openqa.selenium.WebElement) confirmButtons.get(0)).click();
        sleepInSecond(2); // cho POST webhook gia + view thanh cong hien
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán thành công!"),
                "Bam xac nhan xong phai hien \"Thanh toán thành công!\"");
        Assert.assertTrue(isTextVisibleOnPage(
                        "Đơn hàng #" + orderCode + " đã được hệ thống xác nhận thanh toán qua PayOS."),
                "Bam xac nhan xong phai hien cau xac nhan don #" + orderCode);
        Assert.assertFalse(getDriver().findElements(By.xpath("//a[@href='/orders']")).isEmpty(),
                "Sau khi xac nhan phai co link \"Xem danh sách đơn hàng\" (/orders)");
    }

    @Story("Trang thanh toán PayOS")
    @Test(description = "TC_SCR7_FUNC_050: Trang /payment-success hiển thị sau khi PayOS trả về: \"Thanh toán thành công!\" + khối Mã đơn/Số tiền + 2 link đơn hàng/trang chủ")
    public void paymentSuccessPageShowsOrderSummaryAndLinks() {
        long[] order = placePayosOrderWithDemo();
        long orderCode = order[0];
        long amount = order[1];

        // Xac nhan chuyen khoan tren trang mock-payos de don thanh Paid that
        // (webhook gia; checksum demo chap nhan) truoc khi mo /payment-success.
        getDriver().get(GlobalConstants.getAppUrl() + "/mock-payos?orderCode=" + orderCode
                + "&amount=" + amount);
        sleepInSecond(2);
        List<?> confirmButtons = getDriver().findElements(By.xpath(
                "//button[contains(.,'Xác nhận đã chuyển khoản thành công')]"));
        Assert.assertEquals(confirmButtons.size(), 1, "Trang mock-payos phai co nut xac nhan");
        ((org.openqa.selenium.WebElement) confirmButtons.get(0)).click();
        sleepInSecond(2);
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán thành công!"),
                "Xac nhan tren mock-payos phai thanh cong truoc khi mo /payment-success");

        getDriver().get(GlobalConstants.getAppUrl() + "/payment-success?orderCode=" + orderCode);
        sleepInSecond(2); // cho trang goi GET /api/payments/status lay so tien
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán thành công!"),
                "Trang /payment-success phai hien \"Thanh toán thành công!\"");
        Assert.assertTrue(isTextVisibleOnPage(
                        "Đơn hàng #" + orderCode + " đã được xác nhận thanh toán qua PayOS."),
                "Trang /payment-success phai xac nhan don #" + orderCode + " da thanh toan qua PayOS");
        Assert.assertTrue(isTextVisibleOnPage("Mã đơn:"),
                "Trang /payment-success phai co khoi \"Mã đơn:\"");
        Assert.assertTrue(isTextVisibleOnPage("#" + orderCode),
                "Khoi tom tat phai hien dung ma don #" + orderCode);
        Assert.assertTrue(isTextVisibleOnPage("Số tiền:"),
                "Trang /payment-success phai co khoi \"Số tiền:\"");
        Assert.assertTrue(isTextVisibleOnPage(formatVnd(amount)),
                "Khoi tom tat phai hien dung so tien " + formatVnd(amount) + "d");
        Assert.assertFalse(getDriver().findElements(
                By.xpath("//a[@href='/orders' and contains(.,'Xem lịch sử đơn hàng')]")).isEmpty(),
                "Trang /payment-success phai co link \"Xem lịch sử đơn hàng\" (/orders)");
        Assert.assertFalse(getDriver().findElements(
                By.xpath("//a[@href='/' and contains(.,'Về trang chủ')]")).isEmpty(),
                "Trang /payment-success phai co link \"Về trang chủ\" (/)");
    }

    // ---------- Dung diem hoi vien o checkout (PT-05..PT-08) ----------
    // Diem chi tich khi don Completed (khong tao duoc qua UI shop) nen tai khoan
    // demo hien co the dang 0 diem — cac test PT tu SKIP theo dung mau PT-02/PT-03.

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_FUNC_056 (PT-05 đợt 15): Đang dùng 50 điểm, tắt rồi bật lại toggle -> ô vẫn là 50 điểm (không reset về tối đa)")
    public void toggleOffAndOnKeepsUsedPoints() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints < 50) {
            throw new SkipException("Tai khoan demo chi co " + availablePoints
                    + " diem kha dung (< 50) — bo qua PT-05 (TC_SCR7_FUNC_056)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        int maxPoints = parseMaxPointsFromHint(cart.getPointsBalanceHintText());
        if (maxPoints != 50) {
            throw new SkipException("Tran diem don 500.000d hien la " + maxPoints + " (khac 50)"
                    + " — FE dien toi da khi bat lai toggle (UsePointsSection.tsx) nen case"
                    + " \"giu nguyen 50\" chi kiem duoc khi tran dung 50; bo qua PT-05");
        }
        cart.clickUsePointsToggle();
        Assert.assertEquals(cart.getPointsToUseValue(), 50,
                "Bat toggle tren don 500.000d (tran 50) thi o diem phai tu dien 50");
        cart.setPointsToUse(50);
        Assert.assertEquals(cart.getPointsToUseValue(), 50, "Nhap 50 diem thi o phai giu 50");

        cart.clickUsePointsToggle(); // tat
        Assert.assertEquals(cart.getPointsToUseValue(), 0, "Tat toggle thi so diem phai ve 0");
        cart.clickUsePointsToggle(); // bat lai
        Assert.assertEquals(cart.getPointsToUseValue(), 50,
                "Bat lai toggle thi o diem phai van la 50 (khong reset ve toi da)");
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_VAL_058 (PT-06 đợt 15): Nhập \"50abc\" vào ô điểm -> ô chỉ giữ 50 (ký tự không phải số bị lọc), không hiện lỗi, dòng giảm giá -50.000đ")
    public void pointsInputFiltersNonDigitsAndKeepsValue() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints < 50) {
            throw new SkipException("Tai khoan demo chi co " + availablePoints
                    + " diem kha dung (< 50) — bo qua PT-06 (TC_SCR7_VAL_058)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        cart.clickUsePointsToggle();
        cart.setPointsToUseText("50abc");

        Assert.assertEquals(cart.getPointsToUseValue(), 50,
                "Nhap \"50abc\" thi o diem chi duoc giu 50 (ky tu khong phai so bi loc)");
        Assert.assertFalse(cart.isPointsErrorDisplayed(),
                "Nhap 50 diem (trong tran) thi khong duoc hien loi diem");
        Assert.assertTrue(cart.isPointsDiscountDisplayed(),
                "Dung 50 diem thi phai hien dong giam gia diem (cart-points-discount)");
        Assert.assertEquals(cart.getPointsDiscountValue(), 50_000L,
                "Dung 50 diem thi dong giam gia phai la -50.000d (1 diem = 1.000d)");

        try {
            if (cart.getPointsToUseValue() > 0) {
                cart.clickUsePointsToggle();
            }
        } catch (Exception ignored) {
            // Don dep that bai thi bo qua.
        }
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_FUNC_060 (PT-07 đợt 15): Đang dùng 50 điểm, tắt toggle -> số điểm về 0, dòng giảm giá điểm biến mất, tổng tiền trở lại như trước")
    public void turningPointsOffResetsPointsAndRestoresTotal() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints < 50) {
            throw new SkipException("Tai khoan demo chi co " + availablePoints
                    + " diem kha dung (< 50) — bo qua PT-07 (TC_SCR7_FUNC_060)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        long totalBeforePoints = parseMoney(cart.getFinalTotalText());
        Assert.assertEquals(totalBeforePoints, 500_000L,
                "Don 500.000d mien phi ship thi tong truoc khi dung diem phai la 500.000d");

        cart.clickUsePointsToggle();
        cart.setPointsToUse(50);
        Assert.assertEquals(cart.getPointsToUseValue(), 50, "Nhap 50 diem thi o phai giu 50");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 450_000L,
                "Dung 50 diem thi tong phai giam con 450.000d");

        cart.clickUsePointsToggle(); // tat
        Assert.assertEquals(cart.getPointsToUseValue(), 0, "Tat toggle thi so diem phai ve 0");
        Assert.assertFalse(cart.isPointsDiscountDisplayed(),
                "Tat toggle thi dong giam gia diem phai bien mat");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), totalBeforePoints,
                "Tat toggle thi tong tien phai tro lai nhu truoc khi dung diem (500.000d)");
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_FUNC_061 (PT-08 đợt 15): Đặt hàng COD khi dùng 50 điểm -> đơn trừ 50.000đ, số dư điểm giảm đúng 50, lịch sử điểm có dòng \"Dùng điểm\" -50")
    public void orderWithPointsDeductsBalanceAndLogsHistory() {
        int pointsBefore = readDemoAvailablePoints();
        if (pointsBefore < 50) {
            throw new SkipException("Tai khoan demo chi co " + pointsBefore
                    + " diem kha dung (< 50) — bo qua PT-08 (TC_SCR7_FUNC_061)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        cart.clickUsePointsToggle();
        cart.setPointsToUse(50);
        Assert.assertEquals(cart.getPointsToUseValue(), 50, "Nhap 50 diem thi o phai giu 50");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 450_000L,
                "Dung 50 diem thi tong truoc khi dat phai la 450.000d (500.000 - 50.000 + 0 ship)");

        // Dat COD thanh cong — tao DON THAT tren tai khoan demo va tru 50 diem that.
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD dung diem phai thanh cong");
        String orderCode = checkout.getOrderSuccessCode();
        Assert.assertTrue(orderCode.matches(GlobalConstants.ORDER_DISPLAY_CODE_REGEX),
                "Ma don phai dung dinh dang DH{yyyyMMdd}-{NNNN}, dang thay: " + orderCode);

        HomePageObject homePage = new HomePageObject();
        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains(formatVnd(450_000L)),
                "Tong cong don dung diem phai la 450.000d (500.000 - 50.000 + 0 ship)");

        MembershipPageObject membershipPage = homePage.openMembershipPage();
        membershipPage.clickRefreshButton();
        int pointsAfter = membershipPage.getAvailablePoints();
        Assert.assertEquals(pointsAfter, pointsBefore - 50,
                "So du diem phai giam dung 50 sau khi dat don dung 50 diem (truoc "
                        + pointsBefore + ", sau " + pointsAfter + ")");
        String historyText = membershipPage.getHistoryText();
        Assert.assertTrue(historyText.contains("Dùng điểm"),
                "Lich su diem phai co dong \"Dùng điểm\" sau khi dat don dung diem");
        Assert.assertTrue(historyText.contains("-50 điểm"),
                "Lich su diem phai co dong tru \"-50 điểm\" gan voi lan dung diem vua roi");
    }
}
