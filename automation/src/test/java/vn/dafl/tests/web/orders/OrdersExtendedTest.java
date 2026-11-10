package vn.dafl.tests.web.orders;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.OrdersPageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;
import vn.dafl.pageUIs.OrdersPageUI;

import java.util.ArrayList;
import java.util.List;

/**
 * SCR_08 (buoc 3, Pha B) — loc/phân trang/chi tiet don, nut Thanh toan ngay /
 * Mua lai, va huy don theo tung ly do.
 *
 * Quy uoc du lieu:
 * - Moi test dung tai khoan MOI dang ky (don bat dau rong) tru khi ghi chu khac,
 *   de khong phu thuoc lich su don cua tai khoan demo.
 * - Dat hang tao DON THAT (COD mac dinh); huy don cung la that va khong khoi phuc.
 * - 4 case TC_SCR8_FUNC_022/023/024/025 la bien the du lieu thuan tuy cua cung
 *   1 luong huy don theo ly do co san nen gop thanh 1 method dung vong lap
 *   (description liet ke het 4 ma case).
 * - formatPrice cua FE dung Intl vi-VN ("84.000 ₫") nen test so sanh tien bang
 *   parseMoney hoac chuoi so co dau cham, KHONG so sanh ca ky hieu ₫/đ.
 */
@Feature("Đơn hàng của tôi (mở rộng)")
public class OrdersExtendedTest extends BaseTest {

    /** Tim SP theo tu khoa duy nhat, kiem tra dung gia seed, them 1 cai vao gio. */
    private String addProductByKeyword(HomePageObject homePage, String keyword, long expectedPrice) {
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        homePage.searchProduct(keyword);
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        Assert.assertNotNull(productId, "Phai tim thay san pham voi tu khoa '" + keyword + "'");
        Assert.assertEquals(catalog.getProductPrice(productId), expectedPrice,
                "Gia SP tim duoc voi tu khoa '" + keyword + "' phai dung gia seed " + expectedPrice + "d");
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(badgeBefore + 1);
        return productId;
    }

    /** Dat 1 don COD 1 mon (SP re nhat seed) bang tai khoan dang dang nhap; tra ve ma don DH... */
    private String placeOneItemCodOrder(String name, String email) {
        HomePageObject homePage = new HomePageObject();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        addProductByKeyword(homePage, "Green Tea Balancing", 59_000L);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(name, email, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");
        return checkout.getOrderSuccessCode();
    }

    /** Dat 1 don COD voi ghi chu giao hang; tra ve ma don DH... */
    private String placeOneItemCodOrderWithNote(String name, String email, String note) {
        HomePageObject homePage = new HomePageObject();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        addProductByKeyword(homePage, "Green Tea Balancing", 59_000L);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillShippingForm(name, email, GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, note);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");
        return checkout.getOrderSuccessCode();
    }

    /** Dinh dang so tien kieu "84.000" de doi chieu chuoi hien thi tren trang. */
    private String formatVnd(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }

    // ---------- Loc + phan trang ----------

    @Story("Lọc và phân trang đơn hàng")
    @Test(description = "TC_SCR8_FUNC_004: Lọc danh sách đơn hàng theo tab \"Chờ thanh toán\" -> chỉ hiện đơn PayOS chưa thanh toán + số lượng (n) đúng")
    public void filterOrdersByPendingPaymentTab() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Loc Don", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);

        // Don COD (nhan "Chờ xác nhận") + don PayOS chua thanh toan (nhan "Chờ thanh toán").
        String codOrderCode = placeOneItemCodOrder("Khach Loc Don", email);

        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        getDriver().get(GlobalConstants.getAppUrl() + "/");
        catalog.waitForProductsLoaded();
        addProductByKeyword(homePage, "Green Tea Balancing", 59_000L);
        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Loc Don", email, GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Dat PayOS phai hien man hinh thanh toan");
        String payosOrderCodeText = checkout.getOrderCode(); // "Đơn hàng #<ma so>"
        Assert.assertTrue(payosOrderCodeText.startsWith("Đơn hàng #"),
                "Man hinh thanh toan phai hien ma don so");

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        Assert.assertEquals(ordersPage.getOrderStatusTabCount("Chờ thanh toán"), 1,
                "Tab \"Chờ thanh toán\" phai ghi dung (1), dang thay: "
                        + ordersPage.getOrderStatusTabText("Chờ thanh toán"));
        ordersPage.filterByStatus("Chờ thanh toán");
        Assert.assertEquals(ordersPage.getOrderCountOnCurrentPage(), 1,
                "Loc tab \"Chờ thanh toán\" phai chi hien dung 1 don PayOS chua thanh toan");
        Assert.assertFalse(ordersPage.isOrderDisplayed(codOrderCode),
                "Loc tab \"Chờ thanh toán\" khong duoc hien don COD \"Chờ xác nhận\"");
    }

    @Story("Lọc và phân trang đơn hàng")
    @Test(description = "TC_SCR8_UI_012: Phân trang danh sách đơn hàng 10 đơn/trang (12 đơn: trang 1 có 10, trang 2 có 2, nút Trang trước/sau chuyển đúng)")
    public void ordersPaginationShowsTenPerPage() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Nhieu Don", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        List<String> orderCodes = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            orderCodes.add(placeOneItemCodOrder("Khach Nhieu Don", email));
            getDriver().get(GlobalConstants.getAppUrl() + "/");
        }

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        Assert.assertEquals(ordersPage.getOrderCountOnCurrentPage(), 10,
                "Trang 1 danh sach don phai hien dung toi da 10 don");
        Assert.assertEquals(ordersPage.getOrdersPageIndicatorText(), "Trang 1",
                "Chi so phan trang ban dau phai la \"Trang 1\"");
        Assert.assertTrue(ordersPage.isOrdersNextButtonEnabled(),
                "Con trang 2 thi nut \"Trang sau\" phai bam duoc");
        ordersPage.clickOrdersNextPage();
        Assert.assertEquals(ordersPage.getOrderCountOnCurrentPage(), 2,
                "Trang 2 phai hien dung 2 don con lai");
        Assert.assertEquals(ordersPage.getOrdersPageIndicatorText(), "Trang 2",
                "Chi so phan trang sau khi sang trang phai la \"Trang 2\"");
        Assert.assertTrue(ordersPage.isOrdersPrevButtonEnabled(),
                "O trang 2 nut \"Trang trước\" phai bam duoc");
        ordersPage.clickOrdersPreviousPage();
        Assert.assertEquals(ordersPage.getOrderCountOnCurrentPage(), 10,
                "Quay lai trang 1 phai hien lai dung 10 don");
        Assert.assertEquals(ordersPage.getOrdersPageIndicatorText(), "Trang 1",
                "Chi so phan trang sau khi quay lai phai la \"Trang 1\"");
    }

    @Story("Lọc và phân trang đơn hàng")
    @Test(description = "TC_SCR8_UI_019: Tài khoản chưa có đơn hàng hiển thị trạng thái rỗng \"Bạn chưa có đơn hàng nào\"")
    public void freshAccountSeesEmptyOrdersState() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Chua Co Don", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        OrdersPageObject ordersPage = homePage.openOrdersPage();

        Assert.assertTrue(ordersPage.isOrdersEmptyStateDisplayed(),
                "Tai khoan moi chua co don phai thay trang thai rong (orders-empty-state)");
        Assert.assertTrue(ordersPage.getElementText(OrdersPageUI.ORDERS_EMPTY_STATE)
                        .contains("Bạn chưa có đơn hàng nào"),
                "Trang thai rong phai ghi \"Bạn chưa có đơn hàng nào\"");
        Assert.assertEquals(ordersPage.getOrderCountOnCurrentPage(), 0,
                "Tai khoan moi chua co don thi danh sach phai rong");
    }

    // ---------- Nut hanh dong tren the don ----------

    @Story("Thanh toán đơn chờ thanh toán")
    @Test(description = "TC_SCR8_FUNC_014: Nút \"Thanh toán ngay\" chỉ hiện với đơn Chờ thanh toán (không hiện với đơn Chờ xác nhận); bấm nút -> chuyển tới /payment/{orderCode}")
    public void payNowButtonOnlyOnPendingPaymentOrder() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Thanh Toan Ngay", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);

        String codOrderCode = placeOneItemCodOrder("Khach Thanh Toan Ngay", email);

        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        getDriver().get(GlobalConstants.getAppUrl() + "/");
        catalog.waitForProductsLoaded();
        addProductByKeyword(homePage, "Green Tea Balancing", 59_000L);
        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Thanh Toan Ngay", email, GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Dat PayOS phai hien man hinh thanh toan");
        String payosNumericCode = checkout.getOrderCode().replaceAll("[^0-9]", "");
        Assert.assertFalse(payosNumericCode.isEmpty(), "Man hinh thanh toan phai hien ma don so");

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        // Don PayOS vua dat la don moi nhat (len dau danh sach).
        String payosDisplayCode = ordersPage.getFirstOrderDisplayCode();
        Assert.assertNotNull(payosDisplayCode, "Lich su don phai co don PayOS vua dat");
        Assert.assertTrue(ordersPage.isPayNowButtonDisplayed(payosDisplayCode),
                "Don nhan \"Chờ thanh toán\" phai hien nut \"Thanh toán ngay\"");
        Assert.assertFalse(ordersPage.isPayNowButtonDisplayed(codOrderCode),
                "Don nhan \"Chờ xác nhận\" (COD) khong duoc hien nut \"Thanh toán ngay\"");

        ordersPage.clickPayNowButton(payosDisplayCode);
        sleepInSecond(2); // cho dieu huong sang /payment/{orderCode}
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/payment/" + payosNumericCode),
                "Bam \"Thanh toán ngay\" phai chuyen toi /payment/" + payosNumericCode
                        + ", dang o: " + getDriver().getCurrentUrl());
    }

    @Story("Mua lại")
    @Test(description = "TC_SCR8_FUNC_015: Nút \"Mua lại\" trên đơn đã hủy -> toast \"Đã thêm {n} món từ đơn {mã} vào giỏ hàng!\" + giỏ chứa lại đúng các món của đơn")
    public void reorderCancelledOrderAddsItemsBackToCart() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Mua Lai", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);

        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = addProductByKeyword(homePage, "Green Tea Balancing", 59_000L);
        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Mua Lai", email, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");
        String orderCode = checkout.getOrderSuccessCode();

        // Huy don de don sang trang thai Cancelled (1 trong cac trang thai co nut "Mua lại").
        OrdersPageObject ordersPage = checkout.clickViewOrders();
        ordersPage.openOrderDetail(orderCode);
        ordersPage.cancelOrderWithReason("Đổi ý, không muốn mua nữa");
        Assert.assertTrue(ordersPage.isCancelSuccessDisplayed(), "Huy don phai thanh cong");

        Assert.assertTrue(ordersPage.isReorderButtonDisplayed(orderCode),
                "Don da huy phai hien nut \"Mua lại\"");
        ordersPage.clickReorderButton(orderCode);
        sleepInSecond(2); // cho POST /api/cart/items tung mon + ve /cart + toast
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/cart"),
                "Bam \"Mua lại\" phai chuyen ve /cart, dang o: " + getDriver().getCurrentUrl());
        Assert.assertTrue(isTextVisibleOnPage("Đã thêm 1 món từ đơn " + orderCode + " vào giỏ hàng!"),
                "Bam \"Mua lại\" phai hien toast \"Đã thêm 1 món từ đơn " + orderCode
                        + " vào giỏ hàng!\"");
        CartDrawerPageObject cart = new CartDrawerPageObject();
        Assert.assertTrue(cart.isItemDisplayed(productId),
                "Gio hang sau \"Mua lại\" phai chua lai dung mon cua don da huy");
        Assert.assertEquals(cart.getItemQuantity(productId), 1,
                "Gio hang sau \"Mua lại\" phai chua dung 1 mon nhu don goc");
    }

    // ---------- Chi tiet don ----------

    @Story("Chi tiết đơn hàng")
    @Test(description = "TC_SCR8_UI_013: Thẻ đơn hàng hiển thị tối đa 3 món + dòng \"… và 2 sản phẩm khác\" (đơn 5 món)")
    public void orderCardShowsAtMostThreeItemsWithRemainderLine() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Nam Mon", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        // 5 mon khac nhau, moi mon 1 cai.
        addProductByKeyword(homePage, "Green Tea Balancing", 59_000L);
        addProductByKeyword(homePage, "Green Tea Hand Cream", 99_000L);
        addProductByKeyword(homePage, "No-Sebum", 149_000L);
        addProductByKeyword(homePage, "Speedy Eyebrow", 129_000L);
        addProductByKeyword(homePage, "Velvet Lip Tint", 289_000L);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Nam Mon", email, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD 5 mon phai thanh cong");
        String orderCode = checkout.getOrderSuccessCode();

        OrdersPageObject ordersPage = checkout.clickViewOrders();
        Assert.assertTrue(ordersPage.isOrderDisplayed(orderCode), "Don 5 mon phai xuat hien trong danh sach");
        Assert.assertEquals(ordersPage.getOrderItemRowCount(orderCode), 3,
                "The don 5 mon chi duoc hien toi da 3 dong san pham");
        Assert.assertTrue(ordersPage.getOrderItemText(orderCode).contains("… và 2 sản phẩm khác"),
                "The don 5 mon phai co dong \"… và 2 sản phẩm khác\"");
    }

    @Story("Chi tiết đơn hàng")
    @Test(description = "TC_SCR8_UI_017: Chi tiết đơn hàng có timeline 6 bước + lịch sử trạng thái + khối địa chỉ giao hàng + bảng tiền (Tạm tính/Phí vận chuyển/Giảm giá/Tổng cộng)")
    public void orderDetailShowsTimelineShippingInfoAndMoneyTable() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Xem Chi Tiet", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        String orderCode = placeOneItemCodOrderWithNote("Khach Xem Chi Tiet", email, "Giao giờ hành chính");

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        Assert.assertTrue(ordersPage.isOrderDetailDisplayed(), "Phai thay trang chi tiet don");
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Don COD moi phai o trang thai \"Chờ xác nhận\"");

        String timelineText = ordersPage.getOrderTimelineText();
        for (String step : new String[]{"Đặt hàng", "Thanh toán", "Xác nhận", "Đang giao", "Đã giao", "Hoàn tất"}) {
            Assert.assertTrue(timelineText.contains(step),
                    "Timeline don phai co buoc \"" + step + "\", dang thay: " + timelineText);
        }
        String shippingInfo = ordersPage.getOrderShippingInfoText();
        Assert.assertTrue(shippingInfo.contains("Khach Xem Chi Tiet"),
                "Khoi giao hang phai hien ten nguoi nhan");
        Assert.assertTrue(shippingInfo.contains(GlobalConstants.DEMO_PHONE),
                "Khoi giao hang phai hien SDT");
        Assert.assertTrue(shippingInfo.contains(GlobalConstants.SHIPPING_DETAIL),
                "Khoi giao hang phai hien dia chi chi tiet da dat");
        Assert.assertTrue(shippingInfo.contains("Ghi chú giao hàng: Giao giờ hành chính"),
                "Khoi giao hang phai hien ghi chu don da dat");
        Assert.assertTrue(shippingInfo.contains("Phương thức thanh toán: Thanh toán khi nhận hàng (COD)"),
                "Khoi giao hang phai hien phuong thuc COD");

        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains("Tạm tính:"), "Bang tien phai co dong \"Tạm tính:\"");
        Assert.assertTrue(detailText.contains("Phí vận chuyển:"),
                "Bang tien phai co dong \"Phí vận chuyển:\"");
        Assert.assertTrue(detailText.contains("Tổng cộng:"), "Bang tien phai co dong \"Tổng cộng:\"");
    }

    @Story("Chi tiết đơn hàng")
    @Test(description = "TC_SCR8_FUNC_018: Xem chi tiết đơn đúng dữ liệu đã đặt (sản phẩm, số lượng, giá, phí ship, tổng tiền, địa chỉ là bản sao lúc đặt)")
    public void orderDetailMatchesPlacedOrderData() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Doi Chieu", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = addProductByKeyword(homePage, "Green Tea Balancing", 59_000L);
        String productName = catalog.getProductName(productId);

        homePage.openCart();
        CartDrawerPageObject cart = new CartDrawerPageObject();
        long subtotal = parseMoney(cart.getSubtotalText());
        String shippingFeeText = cart.getShippingFeeText();
        long finalTotal = parseMoney(cart.getFinalTotalText());
        Assert.assertEquals(subtotal, 59_000L, "Tam tinh don 1 mon 59.000d phai dung 59.000d");

        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Doi Chieu", email, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");
        String orderCode = checkout.getOrderSuccessCode();

        OrdersPageObject ordersPage = checkout.clickViewOrders();
        ordersPage.openOrderDetail(orderCode);
        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains(productName),
                "Chi tiet don phai hien dung ten san pham da dat (" + productName + ")");
        Assert.assertTrue(detailText.contains("Số lượng: x1"),
                "Chi tiet don phai hien dung so luong x1");
        Assert.assertTrue(detailText.contains(formatVnd(59_000L)),
                "Chi tiet don phai hien dung don gia 59.000d");
        Assert.assertTrue(detailText.contains(formatVnd(subtotal)),
                "Chi tiet don phai hien dung tam tinh " + formatVnd(subtotal) + "d");
        if (shippingFeeText.contains("Miễn phí")) {
            Assert.assertTrue(detailText.contains("Miễn phí"),
                    "Phi ship mien phi thi chi tiet don phai hien \"Miễn phí\"");
        } else {
            Assert.assertTrue(detailText.contains(formatVnd(parseMoney(shippingFeeText))),
                    "Chi tiet don phai hien dung phi ship " + shippingFeeText);
        }
        Assert.assertTrue(detailText.contains(formatVnd(finalTotal)),
                "Chi tiet don phai hien dung tong cong " + formatVnd(finalTotal) + "d");
        Assert.assertTrue(detailText.contains("Khach Doi Chieu"),
                "Chi tiet don phai hien dung ten nguoi nhan luc dat");
        Assert.assertTrue(detailText.contains(GlobalConstants.DEMO_PHONE),
                "Chi tiet don phai hien dung SDT luc dat");
        Assert.assertTrue(detailText.contains(GlobalConstants.SHIPPING_DETAIL),
                "Chi tiet don phai hien dung dia chi (ban sao luc dat)");
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Don COD moi phai o trang thai \"Chờ xác nhận\"");
    }

    // ---------- Hop xac nhan huy don ----------

    @Story("Hủy đơn hàng")
    @Test(description = "TC_SCR8_UI_021: Hộp xác nhận hủy đơn hiển thị đúng nội dung: tiêu đề \"Hủy đơn {mã}?\", nội dung hoàn kho/không hoàn tác, lý do mặc định, 2 nút \"Không hủy nữa\"/\"Xác nhận hủy đơn\"")
    public void cancelDialogShowsCorrectContent() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Xem Hop Huy", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        String orderCode = placeOneItemCodOrder("Khach Xem Hop Huy", email);

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        ordersPage.clickCancelOrderButton();

        String dialogText = ordersPage.getCancelDialogText();
        Assert.assertTrue(dialogText.contains("Hủy đơn " + orderCode + "?"),
                "Hop xac nhan phai co tieu de \"Hủy đơn " + orderCode + "?\", dang thay: " + dialogText);
        Assert.assertTrue(dialogText.contains(
                        "Đơn hàng sẽ bị hủy và số lượng sản phẩm được hoàn về kho. Thao tác này không thể hoàn tác."),
                "Hop xac nhan phai ghi ro don bi huy + hoan kho + khong the hoan tac");
        Assert.assertEquals(ordersPage.getSelectedCancelReason(), "Đổi ý, không muốn mua nữa",
                "Ly do huy mac dinh phai la \"Đổi ý, không muốn mua nữa\"");
        Assert.assertTrue(ordersPage.isElementDisplayed(OrdersPageUI.CANCEL_CANCEL_BTN),
                "Hop xac nhan phai co nut \"Không hủy nữa\" (cancel-cancel-btn)");
        Assert.assertTrue(ordersPage.isElementDisplayed(OrdersPageUI.CANCEL_CONFIRM_BTN),
                "Hop xac nhan phai co nut \"Xác nhận hủy đơn\" (cancel-confirm-btn)");

        ordersPage.dismissCancelDialog();
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Dong hop xac nhan thi don van o \"Chờ xác nhận\"");
    }

    // ---------- Huy don theo ly do ----------

    @Story("Hủy đơn hàng")
    @Test(description = "TC_SCR8_FUNC_022 + TC_SCR8_FUNC_023 + TC_SCR8_FUNC_024 + TC_SCR8_FUNC_025: Hủy đơn với từng lý do có sẵn (\"Đổi ý, không muốn mua nữa\" / \"Đặt nhầm sản phẩm\" / \"Tìm được giá tốt hơn\" / \"Thời gian giao hàng quá lâu\") -> báo thành công + trạng thái \"Đã hủy\" + chi tiết hiện đúng \"Lý do hủy\"")
    public void cancelOrderWithEachPresetReasonShowsReasonInDetail() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Huy Nhieu Ly Do", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        String[] reasons = {
                "Đổi ý, không muốn mua nữa",
                "Đặt nhầm sản phẩm",
                "Tìm được giá tốt hơn",
                "Thời gian giao hàng quá lâu"
        };
        for (String reason : reasons) {
            // Moi ly do can 1 don "Chờ xác nhận" chua huy rieng (huy don la that).
            String orderCode = placeOneItemCodOrder("Khach Huy Nhieu Ly Do", email);

            OrdersPageObject ordersPage = homePage.openOrdersPage();
            ordersPage.openOrderDetail(orderCode);
            ordersPage.cancelOrderWithReason(reason);

            Assert.assertTrue(ordersPage.isCancelSuccessDisplayed(),
                    "Huy don voi ly do \"" + reason + "\" phai thanh cong");
            Assert.assertTrue(ordersPage.getCancelSuccessMessage()
                            .contains(GlobalConstants.CANCEL_SUCCESS_MESSAGE),
                    "Thong bao huy voi ly do \"" + reason + "\" phai chua \"Hủy đơn thành công\"");
            ordersPage.openOrderDetail(orderCode);
            Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_CANCELLED,
                    "Don huy voi ly do \"" + reason + "\" phai o trang thai \"Đã hủy\"");
            Assert.assertTrue(ordersPage.getOrderDetailText().contains("Lý do hủy: " + reason),
                    "Chi tiet don da huy phai hien \"Lý do hủy: " + reason + "\"");
            getDriver().get(GlobalConstants.getAppUrl() + "/");
        }
    }

    @Story("Hủy đơn hàng")
    @Test(description = "TC_SCR8_FUNC_026: Hủy đơn với lý do \"Khác\" -> hiện ô nhập lý do, xác nhận hủy xong chi tiết hiện đúng lý do tự nhập")
    public void cancelOrderWithCustomReasonShowsCustomReasonInDetail() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Huy Ly Do Khac", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        String orderCode = placeOneItemCodOrder("Khach Huy Ly Do Khac", email);

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        ordersPage.clickCancelOrderButton();
        ordersPage.enterCustomCancelReason("Tôi muốn đổi sang sản phẩm khác");
        Assert.assertTrue(ordersPage.isCancelReasonInputDisplayed(),
                "Chon ly do \"Khác\" phai hien o nhap ly do (cancel-reason-input)");
        ordersPage.confirmCancelOrder();

        Assert.assertTrue(ordersPage.isCancelSuccessDisplayed(), "Huy don voi ly do Khac phai thanh cong");
        ordersPage.openOrderDetail(orderCode);
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_CANCELLED,
                "Don da huy phai o trang thai \"Đã hủy\"");
        Assert.assertTrue(ordersPage.getOrderDetailText()
                        .contains("Lý do hủy: Tôi muốn đổi sang sản phẩm khác"),
                "Chi tiet don da huy phai hien dung ly do tu nhap");
    }

    @Story("Hủy đơn hàng")
    @Test(description = "TC_SCR8_FUNC_028: Bấm \"Không hủy nữa\" trong hộp xác nhận hủy -> hộp đóng, đơn vẫn \"Chờ xác nhận\" và vẫn hủy được sau đó")
    public void dismissCancelDialogKeepsOrderCancellable() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Huy Nhung Thoi", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        String orderCode = placeOneItemCodOrder("Khach Huy Nhung Thoi", email);

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        ordersPage.clickCancelOrderButton();
        ordersPage.dismissCancelDialog();

        Assert.assertFalse(ordersPage.isElementDisplayed(OrdersPageUI.CANCEL_ORDER_DIALOG),
                "Bam \"Không hủy nữa\" thi hop xac nhan phai dong");
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Dong hop xac nhan thi don van o \"Chờ xác nhận\"");
        Assert.assertTrue(ordersPage.isCancelOrderButtonDisplayed(),
                "Dong hop xac nhan thi nut Huy don van phai hien de huy lai sau");

        // Huy that sau khi da dong hop 1 lan (chung minh van huy duoc).
        ordersPage.cancelOrderWithReason("Đổi ý, không muốn mua nữa");
        Assert.assertTrue(ordersPage.isCancelSuccessDisplayed(),
                "Sau khi dong hop xac nhan, don van phai huy duoc binh thuong");
    }

    @Story("Hủy đơn hàng")
    @Test(description = "TC_SCR8_VAL_027: Chọn lý do \"Khác\" mà để trống ô nhập -> lỗi \"Vui lòng nhập lý do hủy đơn.\" + đơn vẫn \"Chờ xác nhận\"")
    public void customCancelReasonEmptyShowsError() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Huy Thieu Ly Do", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        String orderCode = placeOneItemCodOrder("Khach Huy Thieu Ly Do", email);

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        ordersPage.clickCancelOrderButton();
        ordersPage.enterCustomCancelReason("");
        ordersPage.confirmCancelOrder();

        Assert.assertTrue(isTextVisibleOnPage("Vui lòng nhập lý do hủy đơn."),
                "Chon \"Khác\" ma de trong o nhap thi phai bao loi \"Vui lòng nhập lý do hủy đơn.\"");
        Assert.assertTrue(ordersPage.isElementDisplayed(OrdersPageUI.CANCEL_ORDER_DIALOG),
                "Bao loi thi hop xac nhan phai van mo (don chua bi huy)");
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Bao loi thi don van o \"Chờ xác nhận\" (khong bi huy)");

        ordersPage.dismissCancelDialog();
    }
}
