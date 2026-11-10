package vn.dafl.tests.admin;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Modal xem chi tiet o trang quan tri /admin (dot 8, giao dien Spark).
 * Chi test DOC: mo modal bang nut mat (👁), doc noi dung, dong modal.
 * KHONG doi trang thai don, KHONG sua/luu san pham (chi mo form sua roi dong),
 * KHONG kich hoat/tat khuyen mai, KHONG duyet/tu choi tra hang, KHONG khoa tai khoan.
 * Test nao can du lieu (don hang, yeu cau tra hang) thi tu skip co ly do ro rang
 * neu DB do khong co du lieu do.
 * Dot 12: form them/sua mo trong modal (admin-*-form-modal); tieu de form nam o
 * tieu de modal, ten SP dien san trong o nhap (khong con trong text cua form).
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Chi tiết quản trị")
public class AdminDetailTest extends BaseTest {

    /** Dang nhap bang tai khoan Super Admin tren app Admin rieng :3001; bo qua test
     *  neu tai khoan chua duoc seed tren DB nay (giong AdminAndNewsletterTest). */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Mo /admin va dung o muc cho truoc; tra ve AdminPageObject da san sang. */
    private AdminPageObject openAdminTab(String navKey) {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav(navKey);
        return adminPage;
    }

    @Story("Sản phẩm")
    @Test(description = "ADT-01: Mở modal chi tiết sản phẩm bằng nút mắt → hiện đúng tên SP, đóng được")
    public void productDetailModalShowsCorrectProduct() {
        AdminPageObject adminPage = openAdminTab("products");
        String productName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PRODUCT_DETAIL_BTN, "Xem chi tiết sản phẩm ");
        if (productName == null || productName.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstProductDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_PRODUCT_DETAIL),
                "Phai mo duoc modal chi tiet san pham");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_PRODUCT_DETAIL);
        Assert.assertTrue(modalText.contains(productName),
                "Modal chi tiet phai hien dung ten san pham '" + productName + "', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Giá bán"),
                "Modal chi tiet san pham phai co dong 'Giá bán'");

        adminPage.closeDetailModal("admin-product-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_PRODUCT_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet san pham");
    }

    @Story("Danh mục")
    @Test(description = "ADT-02: Mở modal chi tiết danh mục bằng nút mắt → hiện đúng tên danh mục, đóng được")
    public void categoryDetailModalShowsCorrectCategory() {
        AdminPageObject adminPage = openAdminTab("categories");
        String categoryName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, "Xem chi tiết danh mục ");
        if (categoryName == null || categoryName.isEmpty()) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstCategoryDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Phai mo duoc modal chi tiet danh muc");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_CATEGORY_DETAIL);
        Assert.assertTrue(modalText.contains(categoryName),
                "Modal chi tiet phai hien dung ten danh muc '" + categoryName + "', dang thay: " + modalText);

        adminPage.closeDetailModal("admin-category-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet danh muc");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADT-03: Mở modal chi tiết khuyến mãi bằng nút mắt → hiện đúng mã voucher, đóng được")
    public void promoDetailModalShowsCorrectCode() {
        AdminPageObject adminPage = openAdminTab("promotions");
        String promoCode = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PROMO_DETAIL_BTN, "Xem chi tiết khuyến mãi ");
        if (promoCode == null || promoCode.isEmpty()) {
            throw new SkipException("Bo qua: muc Khuyen mai khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstPromoDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_PROMO_DETAIL),
                "Phai mo duoc modal chi tiet khuyen mai");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_PROMO_DETAIL);
        Assert.assertTrue(modalText.contains(promoCode),
                "Modal chi tiet phai hien dung ma khuyen mai '" + promoCode + "', dang thay: " + modalText);

        adminPage.closeDetailModal("admin-promo-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_PROMO_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet khuyen mai");
    }

    @Story("Hội viên")
    @Test(description = "ADT-04: Mở modal chi tiết hạng hội viên bằng nút mắt → hiện đúng tên hạng, đóng được")
    public void tierDetailModalShowsCorrectTier() {
        AdminPageObject adminPage = openAdminTab("membership");
        String tierLabel = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_TIER_DETAIL_BTN, "Xem chi tiết hạng ");
        if (tierLabel == null || tierLabel.isEmpty()) {
            throw new SkipException("Bo qua: muc Hoi vien khong co hang nao de mo chi tiet");
        }

        adminPage.clickFirstTierDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_TIER_DETAIL),
                "Phai mo duoc modal chi tiet hang hoi vien");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_TIER_DETAIL);
        Assert.assertTrue(modalText.contains(tierLabel),
                "Modal chi tiet phai hien dung ten hang '" + tierLabel + "', dang thay: " + modalText);

        adminPage.closeDetailModal("admin-tier-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_TIER_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet hang hoi vien");
    }

    @Story("Đơn hàng")
    @Test(description = "ADT-05: Mở modal chi tiết đơn hàng bằng nút mắt → hiện mã đơn + tiền, đóng được (skip nếu DB chưa có đơn)")
    public void orderDetailModalShowsOrderCode() {
        AdminPageObject adminPage = openAdminTab("orders");
        if (!adminPage.isElementDisplayedWithWait(AdminPageUI.FIRST_ORDER_DETAIL_BTN, 10)) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de mo chi tiet");
        }
        String orderLabel = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_ORDER_DETAIL_BTN, "Xem chi tiết đơn ");
        Assert.assertNotNull(orderLabel, "Nut mat don hang phai co aria-label chua ma don");

        adminPage.clickFirstOrderDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Phai mo duoc modal chi tiet don hang");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_ORDER_DETAIL);
        Assert.assertTrue(modalText.contains(orderLabel),
                "Modal chi tiet don phai hien dung ma don '" + orderLabel + "', dang thay: " + modalText);

        adminPage.closeDetailModal("admin-order-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet don hang");
    }

    @Story("Khách hàng")
    @Test(description = "ADT-06: Mở modal chi tiết khách hàng bằng nút mắt → hiện đúng tên khách, đóng được")
    public void customerDetailModalShowsCorrectCustomer() {
        AdminPageObject adminPage = openAdminTab("customers");
        String customerName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CUSTOMER_DETAIL_BTN, "Xem khách hàng ");
        if (customerName == null || customerName.isEmpty()) {
            throw new SkipException("Bo qua: muc Khach hang khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstCustomerDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CUSTOMER_DETAIL),
                "Phai mo duoc modal chi tiet khach hang");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_CUSTOMER_DETAIL);
        Assert.assertTrue(modalText.contains(customerName),
                "Modal chi tiet phai hien dung ten khach '" + customerName + "', dang thay: " + modalText);

        adminPage.closeDetailModal("admin-customer-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CUSTOMER_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet khach hang");
    }

    @Story("Trả hàng")
    @Test(description = "ADT-07: Mở modal chi tiết yêu cầu trả hàng bằng nút 'Duyệt / Từ chối' → hiện mã đơn, đóng được (skip nếu chưa có yêu cầu trả hàng)")
    public void returnDetailModalShowsOrderCode() {
        AdminPageObject adminPage = openAdminTab("returns");
        if (adminPage.isEmptyReturnsMessageDisplayed()) {
            throw new SkipException("Bo qua: DB nay chua co yeu cau tra hang nao de mo chi tiet");
        }
        if (!adminPage.isElementDisplayedWithWait(AdminPageUI.FIRST_RETURN_REVIEW_BTN, 10)) {
            throw new SkipException("Bo qua: muc Tra hang khong co nut 'Duyệt / Từ chối' de mo chi tiet");
        }

        adminPage.clickFirstReturnReview();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_RETURN_DETAIL),
                "Phai mo duoc modal chi tiet yeu cau tra hang");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_RETURN_DETAIL);
        Assert.assertTrue(modalText.contains("DH") || modalText.contains("Trả hàng"),
                "Modal chi tiet tra hang phai hien ma don hoac tieu de tra hang, dang thay: " + modalText);

        // Chi dong modal, KHONG bam Duyet/Tu choi (khong doi trang thai tra hang).
        adminPage.closeDetailModal("admin-return-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_RETURN_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet tra hang");
    }

    @Story("Sản phẩm")
    @Test(description = "ADT-08 (đợt 12, sửa): Từ modal chi tiết sản phẩm, bấm 'Sửa sản phẩm' → form sửa mở trong modal (admin-product-form-modal), tiêu đề 'Sửa sản phẩm' + ô tên điền sẵn tên SP; đóng form không lưu")
    public void productDetailEditButtonOpensEditFormWithoutSaving() {
        AdminPageObject adminPage = openAdminTab("products");
        String productName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PRODUCT_DETAIL_BTN, "Xem chi tiết sản phẩm ");
        if (productName == null || productName.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstProductDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_PRODUCT_DETAIL),
                "Phai mo duoc modal chi tiet san pham truoc");

        adminPage.openProductEditFormFromDetail();
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Bam 'Sửa sản phẩm' phai mo modal form sua san pham");
        Assert.assertTrue(adminPage.isProductFormDisplayed(),
                "Modal form sua san pham phai hien form");
        String modalText = adminPage.getProductFormModalText();
        Assert.assertTrue(modalText.contains("Sửa sản phẩm"),
                "Modal form mo ra phai co tieu de 'Sửa sản phẩm', dang thay: " + modalText);
        Assert.assertEquals(adminPage.getProductNameInputValue(), productName,
                "Form sua phai dien san ten san pham '" + productName + "' trong o ten");

        // Dong form bang nut 'Đóng', KHONG bam Luu (khong doi du lieu).
        adminPage.closeProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form xong khong duoc con thay modal form sua san pham");
    }

    @Story("Danh mục")
    @Test(description = "ADT-09: Modal chi tiết danh mục đóng bằng nút 'Đóng' ở footer (không dùng nút X)")
    public void categoryDetailModalClosesWithFooterButton() {
        AdminPageObject adminPage = openAdminTab("categories");
        if (!adminPage.isElementDisplayedWithWait(AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, 10)) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstCategoryDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Phai mo duoc modal chi tiet danh muc truoc");

        adminPage.clickToElement(AdminPageUI.DYNAMIC_MODAL_FOOTER_CLOSE_BTN, "admin-category-detail");
        adminPage.waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_DETAIL);
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam 'Đóng' o footer phai dong duoc modal chi tiet danh muc");
    }
}
