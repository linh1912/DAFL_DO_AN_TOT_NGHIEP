package vn.dafl.tests.admin.catalog;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Duyet + xem chi tiet danh muc san pham o Admin rieng localhost:3001 (chi doc):
 * tim kiem / toolbar chuan / phan trang o bang San pham + Danh muc (ADM-07,
 * ADM-11..ADM-14, tach tu AdminAndNewsletterTest cu) va modal xem chi tiet SP /
 * DM (ADT-01, ADT-02, ADT-08, ADT-09, tach tu AdminDetailTest cu) khi chia thu
 * muc admin dot 17 — moi lop chi lo 1 phan he.
 * KHONG doi trang thai don, KHONG them/sua/xoa san pham/khuyen mai, KHONG khoa
 * tai khoan. Test nao can du lieu thi tu skip co ly do ro rang neu DB do khong
 * co du lieu do.
 * Dot 12: o search tren topbar + breadcrumb da bo han; tim kiem gio nam trong
 * toolbar rieng cua tung muc; bang phan trang client-side 10 dong/trang.
 * Dot 21 (chi muc San pham): form them/sua SP la TRANG full page
 * (/products/new, /products/:code/edit, KHONG con Drawer/Modal, bang thay
 * the khoi DOM khi mo form); trang chi tiet SP cung la trang full page
 * (/products/:code, breadcrumb "Quản lý sản phẩm > Chi tiết sản phẩm");
 * bang SP BO cot Đánh giá (chi con 8 cot), rating chi con o trang chi tiet;
 * toolbar SP: o tim tu loc (khong nut Tim), nut 'Đặt lại bộ lọc' la nut chu.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Duyệt danh mục sản phẩm")
public class AdminCatalogBrowseTest extends BaseTest {

    /** Dang nhap Super Admin tren app Admin rieng :3001; bo qua test neu chua san sang. */
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
    @Test(description = "ADM-07: Tìm kiếm ở mục Sản phẩm lọc đúng (từ khóa có → thấy dòng, từ khóa lạ → báo rỗng)")
    public void adminProductSearchFiltersRows() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        // Tu khoa co trong du lieu seed ('Mặt nạ' - san pham 'Mặt nạ giấy').
        adminPage.searchProducts("Mặt nạ");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Tim 'Mặt nạ' phai ra it nhat 1 dong san pham");
        boolean found = adminPage.getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains("Mặt nạ"));
        Assert.assertTrue(found, "Cac dong sau khi loc phai chua 'Mặt nạ'");

        // Tu khoa khong ton tai -> bang rong + dong bao rong.
        adminPage.searchProducts("zzzzzz-khong-co-san-pham");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim tu khoa la phai ra 0 dong san pham");
        Assert.assertTrue(adminPage.isEmptyProductsMessageDisplayed(),
                "Tim tu khoa la phai hien dong 'Không có sản phẩm nào.'");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM-11 (đợt 12, viết lại): Ô search trên topbar đã bỏ — tìm kiếm ở toolbar của mục Sản phẩm vẫn lọc đúng ('Mặt nạ' ra dòng); topbar không được còn ô tìm kiếm")
    public void adminToolbarSearchFiltersProductsAndTopbarSearchRemoved() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");

        // Topbar moi khong con o tim kiem (bo dot 12) - khang dinh no khong hien.
        Assert.assertFalse(adminPage.isTopbarSearchDisplayed(),
                "Topbar admin moi khong duoc con o tim kiem (da bo dot 12)");

        adminPage.clickAdminNav("products");
        Assert.assertTrue(adminPage.isProductSearchDisplayed(),
                "Muc San pham phai co o tim kiem rieng o toolbar");
        adminPage.searchProducts("Mặt nạ");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Tim 'Mặt nạ' bang o tim kiem cua muc San pham phai ra it nhat 1 dong san pham");
        boolean found = adminPage.getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains("Mặt nạ"));
        Assert.assertTrue(found, "Cac dong sau khi loc bang toolbar phai chua 'Mặt nạ'");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM-12 (đợt 12): Phân trang bảng Sản phẩm — trang 1 tối đa 10 dòng, bấm 'Sau' sang trang 2 rồi 'Trước' về trang 1, 'Trang X / Y' đổi theo (skip nếu <= 10 dòng)")
    public void adminProductsPaginationNextPrevChangesPage() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        int total = adminPage.getPaginationTotalRows("products");
        if (total <= 10) {
            throw new SkipException("Bo qua: muc San pham chi co " + total
                    + " dong (<= 10) nen khong co trang 2 de kiem tra phan trang");
        }
        Assert.assertTrue(adminPage.isPaginationDisplayed("products"),
                "Muc San pham co > 10 dong thi phai hien khoi phan trang");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                "Mac dinh phai o trang 1");
        Assert.assertTrue(adminPage.getAdminTableRowCount() <= 10,
                "Trang 1 chi hien toi da 10 dong");
        String firstRowPage1 = adminPage.getAdminTableRowTexts().get(0);
        Assert.assertFalse(adminPage.isPaginationPrevEnabled("products"),
                "O trang 1 nut 'Trước' phai bi tat");
        Assert.assertTrue(adminPage.isPaginationNextEnabled("products"),
                "Con trang sau thi nut 'Sau' phai bat duoc");

        adminPage.clickPaginationNext("products");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 2,
                "Bam 'Sau' phai sang trang 2");
        Assert.assertTrue(adminPage.getAdminTableRowCount() <= 10,
                "Trang 2 chi hien toi da 10 dong");
        String firstRowPage2 = adminPage.getAdminTableRowTexts().get(0);
        Assert.assertNotEquals(firstRowPage2, firstRowPage1,
                "Trang 2 phai hien cac dong khac trang 1");

        adminPage.clickPaginationPrev("products");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                "Bam 'Trước' phai ve lai trang 1");
        Assert.assertEquals(adminPage.getAdminTableRowTexts().get(0), firstRowPage1,
                "Ve trang 1 phai thay lai dong dau nhu ban dau");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM-13 (đợt 12, viết lại đợt 21 theo trang full page): Bấm '+ Thêm mới' ở toolbar mở TRANG Thêm SP (/products/new, tiêu đề 'Thêm mới sản phẩm', bảng danh sách KHÔNG còn trong DOM vì trang thay bảng, KHÔNG Drawer) — có dropzone upload ảnh + nút Hủy/Lưu ở header sticky; đóng bằng nút Hủy quay lại bảng, không tạo dữ liệu")
    public void adminAddProductOpensFormModalAndCloseReturnsToTable() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        adminPage.openProductFormModal();
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Bam '+ Thêm mới' phai mo trang them SP full page (admin-product-form)");
        Assert.assertTrue(adminPage.isProductFormDisplayed(),
                "Form san pham phai hien form (admin-product-form)");
        Assert.assertFalse(adminPage.isProductFormDrawer(),
                "Form them san pham dot 21 KHONG con la Drawer (khong class fs-drawer)");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Mo trang them SP thi bang danh sach KHONG con trong DOM (trang thay bang, khac Drawer dot 18)");
        String modalText = adminPage.getProductFormModalText();
        Assert.assertTrue(modalText.contains("Thêm mới sản phẩm"),
                "Trang form mo ra phai co tieu de 'Thêm mới sản phẩm', dang thay: " + modalText);
        Assert.assertTrue(adminPage.isProductImageDropzoneDisplayed(),
                "Trang form them san pham phai co dropzone upload anh (admin-product-image-dropzone)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-product-cancel-btn']"),
                "Trang form them san pham phai co nut 'Hủy' o header (admin-product-cancel-btn)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-product-save-btn']"),
                "Trang form them san pham phai co nut 'Lưu' o header (admin-product-save-btn)");

        // Dong trang form bang nut 'Hủy' (khong bam Luu, khong tao du lieu).
        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong trang form bang nut Hủy xong khong duoc con thay form san pham");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Dong trang form xong phai quay lai bang san pham co du lieu");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM-14 (đợt 12, viết lại đợt 21 theo toolbar mới — Thêm mới cuối phải, KHÔNG nút icon Tìm, nút Đặt lại là nút chữ, KHÔNG nút Làm mới, KHÔNG Export): Toolbar chuẩn mục Sản phẩm — nút '+ Thêm mới' ở CUỐI bên phải, ô tìm tự lọc (không nút Tìm), nút chữ 'Đặt lại bộ lọc'; bấm 'Đặt lại' xóa từ khóa và về trang 1")
    public void adminProductsToolbarHasAddResetRefreshButtons() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("products");

        Assert.assertTrue(adminPage.isProductAddButtonDisplayed(),
                "Toolbar muc San pham phai co nut '+ Thêm mới'");
        Assert.assertTrue(adminPage.isToolbarAddButtonLast("products"),
                "Nut '+ Thêm mới' dot 21 phai la nut CUOI CUNG o ben phai toolbar");
        Assert.assertFalse(adminPage.isToolbarSearchButtonDisplayed("products"),
                "Toolbar muc San pham dot 21 KHONG con nut icon 'Tìm' (loc tu dong khi go, debounce 120ms)");
        Assert.assertFalse(adminPage.isToolbarSearchIconDisplayed("products"),
                "Toolbar muc San pham dot 21 KHONG con icon Tim kiem o nut Tim");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("products"),
                "Toolbar muc San pham phai co nut chu 'Đặt lại bộ lọc' (products-reset-btn)");
        Assert.assertFalse(adminPage.isToolbarResetIconDisplayed("products"),
                "Nut 'Đặt lại' dot 21 la nut chu, KHONG con icon RotateCcw");
        Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='products-refresh-btn']"),
                "Dot 18 da bo nut 'Làm mới' khoi toolbar — khong duoc co products-refresh-btn");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar San pham khong duoc co nut Export");

        // Chuc nang nut "Dat lai": go tu khoa la (0 dong) roi bam Dat lai -> o tim
        // duoc xoa trang, bang ve day du, ve trang 1.
        adminPage.searchProducts("zzzzzz-khong-co-san-pham");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim tu khoa la phai ra 0 dong truoc khi bam 'Đặt lại'");
        adminPage.clickToolbarReset("products");
        Assert.assertTrue(adminPage.getProductSearchValue().isEmpty(),
                "Bam 'Đặt lại' phai xoa trang o tim kiem");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Bam 'Đặt lại' xong bang san pham phai hien lai du lieu");
        if (adminPage.isPaginationDisplayed("products")) {
            Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                    "Bam 'Đặt lại' phai ve trang 1");
        }

        // Toolbar chuan ap cho moi muc: Danh muc cung co nut Dat lai + Tim.
        adminPage.clickAdminNav("categories");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("categories"),
                "Toolbar muc Danh muc phai co nut icon 'Đặt lại'");
        Assert.assertTrue(adminPage.isToolbarSearchButtonDisplayed("categories"),
                "Toolbar muc Danh muc phai co nut icon 'Tìm'");
    }

    @Story("Sản phẩm")
    @Test(description = "ADT-01 (đợt 21, viết lại theo trang chi tiết full page): Mở trang chi tiết sản phẩm bằng nút mắt → route /products/:code, breadcrumb 'Quản lý sản phẩm > Chi tiết sản phẩm', đủ Mã/Tên/Danh mục/Giá/Trạng thái ('Đang hoạt động'/'Ngừng hoạt động') + rating; header CHỈ đúng 2 nút [Quay lại]+[Chỉnh sửa]; đóng bằng [Quay lại]")
    public void productDetailModalShowsCorrectProduct() {
        AdminPageObject adminPage = openAdminTab("products");
        String productName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PRODUCT_DETAIL_BTN, "Xem chi tiết sản phẩm ");
        if (productName == null || productName.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstProductDetail();
        Assert.assertTrue(adminPage.isProductDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet san pham full page (admin-product-detail-page)");
        String breadcrumb = adminPage.getProductDetailBreadcrumbText();
        Assert.assertTrue(breadcrumb.contains("Quản lý sản phẩm") && breadcrumb.contains("Chi tiết sản phẩm"),
                "Breadcrumb phai la 'Quản lý sản phẩm > Chi tiết sản phẩm', dang thay: " + breadcrumb);
        String detail = adminPage.getProductDetailText();
        Assert.assertTrue(detail.contains(productName),
                "Trang chi tiet phai hien dung ten san pham '" + productName + "', dang thay: " + detail);
        Assert.assertTrue(detail.contains("Đang hoạt động") || detail.contains("Ngừng hoạt động"),
                "Trang chi tiet san pham phai co trang thai 'Đang hoạt động' hoac 'Ngừng hoạt động', dang thay: " + detail);
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-product-detail-back-btn']"),
                "Trang chi tiet phai co nut [Quay lại] o header (admin-product-detail-back-btn)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-product-detail-edit-btn']"),
                "Trang chi tiet phai co nut [Chỉnh sửa] o header (admin-product-detail-edit-btn)");

        adminPage.closeProductDetailPage();
        Assert.assertFalse(adminPage.isProductDetailPageDisplayed(),
                "Bam [Quay lại] phai ve danh sach san pham (khong con trang chi tiet)");
        Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                "Ve danh sach xong SP vua xem phai con trong bang");
    }

    @Story("Danh mục")
    @Test(description = "ADT-02 (đợt 18, viết lại theo view mode): Mở form xem danh mục bằng nút mắt → mode 'view', tên danh mục readonly, có badge trạng thái ('Hiển thị'/'Đang ẩn'), footer CHỈ đúng 2 nút [Đóng]+[Chỉnh sửa]; đóng bằng [Đóng]")
    public void categoryDetailModalShowsCorrectCategory() {
        AdminPageObject adminPage = openAdminTab("categories");
        String categoryName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, "Xem chi tiết danh mục ");
        if (categoryName == null || categoryName.isEmpty()) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstCategoryDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Phai mo duoc form xem danh muc (admin-category-form-modal)");
        Assert.assertEquals(adminPage.getCategoryFormMode(), "view",
                "Form xem danh muc dot 18 phai co data-form-mode='view'");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_CATEGORY_DETAIL);
        Assert.assertTrue(modalText.contains(categoryName),
                "Form xem phai hien dung ten danh muc '" + categoryName + "', dang thay: " + modalText);
        Assert.assertTrue(adminPage.isCategoryNameInputReadOnly(),
                "View mode: o ten danh muc phai readonly (khong go duoc)");
        Assert.assertTrue(modalText.contains("Hiển thị") || modalText.contains("Đang ẩn"),
                "Form xem danh muc phai co badge trang thai 'Hiển thị' hoac 'Đang ẩn', dang thay: " + modalText);
        java.util.List<String> footerTexts = adminPage.getDetailFooterButtonTexts("admin-category-form-modal");
        Assert.assertEquals(footerTexts.size(), 2,
                "Footer form xem dot 18 phai CHI co dung 2 nut, dang thay: " + footerTexts);
        Assert.assertTrue(footerTexts.contains("Đóng") && footerTexts.contains("Chỉnh sửa"),
                "Footer form xem phai la [Đóng] + [Chỉnh sửa], dang thay: " + footerTexts);

        adminPage.clickToElement(AdminPageUI.DYNAMIC_MODAL_FOOTER_CLOSE_BTN, "admin-category-form-modal");
        adminPage.waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_DETAIL);
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam 'Đóng' o footer phai dong duoc form xem danh muc");
    }

    @Story("Sản phẩm")
    @Test(description = "ADT-08 (đợt 12, viết lại đợt 21 theo trang chi tiết + trang sửa full page): Từ trang chi tiết sản phẩm, bấm [Chỉnh sửa] → trang sửa /products/:code/edit (tiêu đề 'Chỉnh sửa sản phẩm' + ô tên điền sẵn tên SP, KHÔNG Drawer); đóng form bằng nút 'Hủy' không lưu, về danh sách")
    public void productDetailEditButtonOpensEditFormWithoutSaving() {
        AdminPageObject adminPage = openAdminTab("products");
        String productName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PRODUCT_DETAIL_BTN, "Xem chi tiết sản phẩm ");
        if (productName == null || productName.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstProductDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_PRODUCT_DETAIL),
                "Phai mo duoc form xem san pham truoc");

        adminPage.openProductEditFormFromDetail();
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Bam [Chỉnh sửa] phai mo trang sua san pham full page");
        Assert.assertTrue(adminPage.isProductFormDisplayed(),
                "Form sua san pham phai hien form");
        Assert.assertFalse(adminPage.isProductFormDrawer(),
                "Form sua dot 21 KHONG con la Drawer (khong class fs-drawer)");
        Assert.assertEquals(adminPage.getProductFormMode(), "edit",
                "Bam [Chỉnh sửa] tu trang chi tiet phai chuyen sang data-form-mode='edit'");
        String modalText = adminPage.getProductFormModalText();
        Assert.assertTrue(modalText.contains("Chỉnh sửa sản phẩm"),
                "Form mo ra phai co tieu de 'Chỉnh sửa sản phẩm', dang thay: " + modalText);
        Assert.assertEquals(adminPage.getProductNameInputValue(), productName,
                "Form sua phai dien san ten san pham '" + productName + "' trong o ten");

        // Dong form bang nut 'Hủy', KHONG bam Luu (khong doi du lieu, form sach
        // nen dong thang khong hoi dirty-close).
        adminPage.closeProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form xong khong duoc con thay form sua san pham");
    }

    @Story("Danh mục")
    @Test(description = "ADT-09 (đợt 18, viết lại — form xem là Modal admin-category-form-modal): Form xem danh mục đóng bằng nút 'Đóng' ở footer (không dùng nút X)")
    public void categoryDetailModalClosesWithFooterButton() {
        AdminPageObject adminPage = openAdminTab("categories");
        if (!adminPage.isElementDisplayedWithWait(AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, 10)) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstCategoryDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Phai mo duoc form xem danh muc truoc");

        adminPage.clickToElement(AdminPageUI.DYNAMIC_MODAL_FOOTER_CLOSE_BTN, "admin-category-form-modal");
        adminPage.waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_DETAIL);
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam 'Đóng' o footer phai dong duoc modal chi tiet danh muc");
    }

    // ---------- Wave 19: gap SCR_04/SCR_05 (UI bang + xem chi tiet + loc/phân trang) ----------

    @Story("Sản phẩm")
    @Test(description = "ADM19-104 (TC_SCRA04_UI_001, đợt 21): Bảng SP có 8 cột (STT/Mã SP/Sản phẩm/Danh mục/Giá/Tồn kho/Trạng thái/Thao tác — ĐÃ BỎ cột Đánh giá) + toolbar chuẩn: cột Mã bắt đầu 'SP-', cột Trạng thái là Switch, mỗi dòng có đúng 3 nút thao tác (Xem/Sửa/Xóa), ô tìm kiếm placeholder 'Tìm tên hoặc mã sản phẩm...', dropdown Lọc danh mục có 'Tất cả danh mục' + tên DM thật, dropdown Trạng thái có đủ 3 lựa chọn (Tất cả/Đang hoạt động/Ngừng hoạt động), nút + Thêm mới nằm cuối toolbar (phải nhất), KHÔNG nút icon Tìm, nút 'Đặt lại bộ lọc' là nút chữ, KHÔNG có nút Export")
    public void adminProductsTableShowsAllColumnsAndStandardToolbar() {
        AdminPageObject adminPage = openAdminTab("products");
        java.util.List<String> headers = adminPage.getAdminTableHeaderTexts();
        Assert.assertEquals(headers, java.util.Arrays.asList(
                        "STT", "Mã SP", "Sản phẩm", "Danh mục", "Giá", "Tồn kho", "Trạng thái", "Thao tác"),
                "Bang SP dot 21 phai co dung 8 cot theo thu tu STT/Mã SP/Sản phẩm/Danh mục/Giá/Tồn kho/Trạng thái/Thao tác (DA BO cot Danh gia), dang thay: " + headers);
        Assert.assertFalse(headers.contains("Đánh giá"),
                "Bang SP dot 21 KHONG duoc con cot 'Đánh giá', dang thay: " + headers);
        String firstProductName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PRODUCT_DETAIL_BTN, "Xem chi tiết sản phẩm ");
        if (firstProductName == null || firstProductName.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de kiem tra cot Ma/Switch/nut thao tac");
        }
        String firstCode = adminPage.getProductCodeInRow(firstProductName);
        Assert.assertTrue(firstCode.startsWith("SP-"),
                "Cot Ma SP o dong dau tien phai bat dau bang 'SP-', dang thay: '" + firstCode + "'");
        Assert.assertFalse(adminPage.getAllRowStatusSwitchStates().isEmpty(),
                "Cot Trang thai trong bang SP phai la Switch (moi dong co 1 Switch)");
        Assert.assertEquals(adminPage.getFirstRowActionButtonCount(), 3,
                "Moi dong SP phai co dung 3 nut thao tac (Xem chi tiet/Sua/Xoa)");
        Assert.assertEquals(adminPage.getElementAttribute(AdminPageUI.ADMIN_PRODUCT_SEARCH, "placeholder"),
                "Tìm tên hoặc mã sản phẩm...",
                "O tim kiem SP phai co placeholder 'Tìm tên hoặc mã sản phẩm...'");
        java.util.List<String> catOptions = adminPage.getProductCategoryFilterOptionTexts();
        Assert.assertFalse(catOptions.isEmpty(), "Dropdown Loc danh muc SP phai co it nhat 1 lua chon");
        Assert.assertEquals(catOptions.get(0), "Tất cả danh mục",
                "Lua chon dau tien cua dropdown Loc danh muc SP phai la 'Tất cả danh mục', dang thay: " + catOptions);
        Assert.assertTrue(catOptions.size() > 1,
                "Dropdown Loc danh muc SP phai co ten DM that ngoai 'Tất cả danh mục', dang thay: " + catOptions);
        Assert.assertEquals(adminPage.getStatusFilterOptionTexts(),
                java.util.Arrays.asList("Tất cả", "Đang hoạt động", "Ngừng hoạt động"),
                "Dropdown Trang thai SP phai co du 3 lua chon Tất cả/Đang hoạt động/Ngừng hoạt động");
        Assert.assertTrue(adminPage.isToolbarAddButtonLast("products"),
                "Nut '+ Thêm mới' phai nam cuoi toolbar (phai nhat) o muc San pham");
        Assert.assertFalse(adminPage.isToolbarSearchButtonDisplayed("products"),
                "Toolbar SP dot 21 KHONG con nut icon Tim kiem (loc tu dong khi go)");
        Assert.assertFalse(adminPage.isToolbarSearchIconDisplayed("products"),
                "Toolbar SP dot 21 KHONG con icon Tim kiem o nut Tim");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("products"),
                "Toolbar SP phai co nut chu 'Đặt lại bộ lọc' (products-reset-btn)");
        Assert.assertFalse(adminPage.isToolbarResetIconDisplayed("products"),
                "Nut 'Đặt lại' dot 21 la nut chu, KHONG con icon Dat lai (Lucide RotateCcw)");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Toolbar SP dot 21 KHONG duoc co nut Export");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-105 (TC_SCRA04_UI_003, đợt 21 — VIẾT LẠI): Cột Đánh giá ĐÃ BỎ khỏi bảng SP (bảng chỉ còn 8 cột, không còn ô sao ở dòng) — dữ liệu rating vẫn hiện ở TRANG CHI TIẾT SP: SP có đánh giá thì hàng 'Đánh giá' hiện dạng '<số> (<n> đánh giá)', không dùng ký tự sao '★'; SP chưa có đánh giá thì chỉ hiện '—'; tự bỏ qua loại nào không tìm được trong 5 SP đầu trang 1")
    public void adminProductsRatingColumnUsesLucideStar() {
        AdminPageObject adminPage = openAdminTab("products");
        java.util.List<String> headers = adminPage.getAdminTableHeaderTexts();
        Assert.assertFalse(headers.contains("Đánh giá"),
                "Bang SP dot 21 KHONG duoc con cot 'Đánh giá', dang thay: " + headers);
        java.util.List<String> names = adminPage.getAllProductDetailButtonLabels();
        if (names.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de kiem tra rating o trang chi tiet");
        }
        boolean sawRated = false;
        boolean sawUnrated = false;
        int checked = 0;
        for (String name : names) {
            if (checked >= 5 || (sawRated && sawUnrated)) {
                break;
            }
            checked++;
            adminPage.clickProductDetailButton(name);
            String rating = adminPage.getProductDetailRatingText();
            if ("—".equals(rating.trim())) {
                sawUnrated = true;
            } else {
                Assert.assertTrue(rating.matches(".*\\d+(\\.\\d+)? \\(\\d+ đánh giá\\).*"),
                        "Hang Danh gia cua SP co danh gia phai co dang '<số> (<n> đánh giá)', dang thay: '" + rating + "'");
                Assert.assertFalse(rating.contains("★"),
                        "Hang Danh gia KHONG duoc dung ky tu sao '★' (phai dung icon Star Lucide), dang thay: '" + rating + "'");
                sawRated = true;
            }
            adminPage.closeProductDetailPage();
        }
        Assert.assertTrue(sawRated || sawUnrated,
                "Trong 5 SP dau phai co it nhat 1 SP co danh gia hoac chua co danh gia de kiem tra hang Danh gia o trang chi tiet");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-106 (TC_SCRA04_FUNC_050, đợt 21 — VIẾT LẠI): Các cách đóng trang chi tiết SP full page — mở trang chi tiết: (1) nút [Quay lại] ở header về được danh sách; (2) mở lại, bấm link 'Quản lý sản phẩm' ở breadcrumb về được danh sách; (3) mở lại, bấm ESC thì trang chi tiết VẪN MỞ (không còn form Drawer để đóng bằng ESC), sau đó đóng bằng [Quay lại]; KHÔNG còn nút X / nút [Đóng] / overlay")
    public void adminProductViewFormCloseMethods() {
        AdminPageObject adminPage = openAdminTab("products");
        String productName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PRODUCT_DETAIL_BTN, "Xem chi tiết sản phẩm ");
        if (productName == null || productName.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de mo chi tiet");
        }

        // (1) Nut [Quay lại] o header.
        adminPage.clickFirstProductDetail();
        Assert.assertTrue(adminPage.isProductDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet san pham truoc");
        adminPage.closeProductDetailPage();
        Assert.assertFalse(adminPage.isProductDetailPageDisplayed(),
                "Bam [Quay lại] phai ve danh sach san pham (khong con trang chi tiet)");

        // (2) Link 'Quản lý sản phẩm' o breadcrumb.
        adminPage.clickFirstProductDetail();
        adminPage.clickProductDetailBreadcrumbLink();
        Assert.assertFalse(adminPage.isProductDetailPageDisplayed(),
                "Bam link breadcrumb 'Quản lý sản phẩm' phai ve danh sach san pham");

        // (3) Phim ESC: trang chi tiet full page KHONG dong bang ESC (khong con
        // Drawer/Modal); sau do dong bang [Quay lại].
        adminPage.clickFirstProductDetail();
        adminPage.pressEscapeKey();
        Assert.assertTrue(adminPage.isProductDetailPageDisplayed(),
                "Bam ESC thi trang chi tiet san pham VAN phai mo (trang full page khong dong bang ESC)");
        adminPage.closeProductDetailPage();
        Assert.assertFalse(adminPage.isProductDetailPageDisplayed(),
                "Bam [Quay lại] sau khi thu ESC phai ve danh sach san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-107 (TC_SCRA04_FUNC_053): Lọc SP theo Danh mục — chọn 1 DM thật ở dropdown 'Lọc danh mục' thì mọi dòng đều thuộc DM đó (cột Danh mục = tên DM); chọn lại 'Tất cả danh mục' thì bộ lọc về mặc định, bảng hiện lại đầy đủ (số dòng > số dòng đã lọc)")
    public void adminProductCategoryFilterFiltersRows() {
        AdminPageObject adminPage = openAdminTab("products");
        java.util.List<String> catOptions = adminPage.getProductCategoryFilterOptionTexts();
        String categoryName = null;
        for (String option : catOptions) {
            if (!"Tất cả danh mục".equals(option)) {
                categoryName = option;
                break;
            }
        }
        if (categoryName == null) {
            throw new SkipException("Bo qua: dropdown Loc danh muc SP khong co DM that nao de loc");
        }
        int totalBefore = adminPage.getPaginationTotalRows("products");

        adminPage.selectProductCategoryFilterByName(categoryName);
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductCategoryFilterSelectedText(), categoryName,
                "Chon DM o dropdown Loc danh muc thi lua chon dang chon phai la '" + categoryName + "'");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Loc theo DM '" + categoryName + "' phai ra it nhat 1 dong san pham");
        for (String row : adminPage.getAdminTableRowTexts()) {
            Assert.assertTrue(row.contains(categoryName),
                    "Loc theo DM '" + categoryName + "' thi moi dong phai thuoc DM do (cot Danh muc), dang thay: " + row);
        }

        adminPage.selectProductCategoryFilterAll();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductCategoryFilterSelectedText(), "Tất cả danh mục",
                "Chon lai 'Tất cả danh mục' thi bo loc Danh muc phai ve mac dinh");
        Assert.assertTrue(adminPage.getPaginationTotalRows("products") >= totalBefore,
                "Bo loc Danh muc xong tong so dong phai ve day du nhu ban dau (" + totalBefore + ")");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-108 (TC_SCRA04_FUNC_056): Nút 'Đặt lại' xóa hết bộ lọc SP — nhập từ khóa 'Mặt nạ' + chọn 1 DM + lọc Trạng thái 'Ngừng hoạt động' (và sang trang 2 nếu đủ dữ liệu), bấm 'Đặt lại' thì ô tìm kiếm trống, dropdown Danh mục về 'Tất cả danh mục', dropdown Trạng thái về '', về trang 1, bảng hiện lại đầy đủ")
    public void adminProductResetClearsAllFiltersAndReturnsToPageOne() {
        AdminPageObject adminPage = openAdminTab("products");
        int totalBefore = adminPage.getPaginationTotalRows("products");

        adminPage.searchProducts("Mặt nạ");
        if (adminPage.getPaginationTotalRows("products") > 10) {
            adminPage.clickPaginationNext("products");
            Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 2,
                    "Tim 'Mặt nạ' co > 10 ket qua thi phai sang duoc trang 2 truoc khi bam 'Đặt lại'");
        }
        java.util.List<String> catOptions = adminPage.getProductCategoryFilterOptionTexts();
        String categoryName = null;
        for (String option : catOptions) {
            if (!"Tất cả danh mục".equals(option)) {
                categoryName = option;
                break;
            }
        }
        if (categoryName != null) {
            adminPage.selectProductCategoryFilterByName(categoryName);
        }
        adminPage.selectStatusFilterByValue("false");
        adminPage.sleepInSecond(1);

        adminPage.clickToolbarReset("products");
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductSearchValue(), "",
                "Bam 'Đặt lại' xong o tim kiem SP phai trong");
        Assert.assertEquals(adminPage.getProductCategoryFilterSelectedText(), "Tất cả danh mục",
                "Bam 'Đặt lại' xong dropdown Loc danh muc SP phai ve 'Tất cả danh mục'");
        Assert.assertEquals(adminPage.getStatusFilterValue(), "",
                "Bam 'Đặt lại' xong dropdown Trang thai SP phai ve '' (Tất cả)");
        if (adminPage.isPaginationDisplayed("products")) {
            Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                    "Bam 'Đặt lại' xong phai ve trang 1");
        }
        Assert.assertEquals(adminPage.getPaginationTotalRows("products"), totalBefore,
                "Bam 'Đặt lại' xong tong so dong SP phai ve day du nhu ban dau (" + totalBefore + ")");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-109 (TC_SCRA04_FUNC_057): Phân trang SP hiển thị khoảng kết quả + khóa nút 'Sau' ở trang cuối — trang 1 hiện 'Hiển thị 1–10 / <tổng> kết quả'; sang trang cuối thì nút 'Sau' bị disable, nút 'Trước' bật, khoảng kết quả hiển thị đúng (từ = (trang cuối - 1) * 10 + 1, đến = tổng); tự bỏ qua khi tổng số SP ≤ 10")
    public void adminProductsPaginationShowsRangeTextAndDisablesNextOnLastPage() {
        AdminPageObject adminPage = openAdminTab("products");
        int total = adminPage.getPaginationTotalRows("products");
        if (total <= 10) {
            throw new SkipException("Bo qua: muc San pham chi co " + total + " dong (<= 10) nen khong co trang 2 de kiem tra phan trang");
        }
        Assert.assertEquals(adminPage.getPaginationText("products"),
                "Hiển thị 1–10 / " + total + " kết quả",
                "Trang 1 phai hien khoang 'Hiển thị 1–10 / <tổng> kết quả' (dau gach en-dash)");
        Assert.assertFalse(adminPage.isPaginationPrevEnabled("products"),
                "Trang 1 thi nut 'Trước' phai bi disable");
        Assert.assertTrue(adminPage.isPaginationNextEnabled("products"),
                "Trang 1 (con trang sau) thi nut 'Sau' phai bat");

        int totalPages = adminPage.getPaginationTotalPages("products");
        while (adminPage.getPaginationCurrentPage("products") < totalPages) {
            adminPage.clickPaginationNext("products");
        }
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), totalPages,
                "Bam 'Sau' lien tuc thi phai toi trang cuoi (" + totalPages + ")");
        Assert.assertFalse(adminPage.isPaginationNextEnabled("products"),
                "Trang cuoi thi nut 'Sau' phai bi disable");
        Assert.assertTrue(adminPage.isPaginationPrevEnabled("products"),
                "Trang cuoi thi nut 'Trước' phai bat");
        int expectedFrom = (totalPages - 1) * 10 + 1;
        Assert.assertEquals(adminPage.getPaginationText("products"),
                "Hiển thị " + expectedFrom + "–" + total + " / " + total + " kết quả",
                "Trang cuoi phai hien khoang 'Hiển thị " + expectedFrom + "–" + total + " / " + total + " kết quả'");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-110 (TC_SCRA04_UI_081): Mục Sản phẩm không lộ ghi chú endpoint dev — toàn bộ text trang Sản phẩm KHÔNG được chứa chuỗi 'GET /api/admin/products' hay đường dẫn API '/api/admin/products' (ghi chú dev, không thuộc UI người dùng)")
    public void adminProductsPageHasNoDevEndpointNote() {
        AdminPageObject adminPage = openAdminTab("products");
        String pageText = adminPage.getAdminPageText();
        Assert.assertFalse(pageText.contains("GET /api/admin/products"),
                "Trang San pham KHONG duoc lo ghi chu endpoint dev 'GET /api/admin/products'");
        Assert.assertFalse(pageText.contains("/api/admin/products"),
                "Trang San pham KHONG duoc lo duong dan API '/api/admin/products' trong UI");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-111 (TC_SCRA05_FUNC_032): Các cách đóng form xem DM — mở form xem DM: (1) nút [Đóng] ở footer đóng được; (2) mở lại, bấm nút X ở header đóng được; (3) mở lại, bấm ESC đóng được; (4) mở lại, bấm ra vùng overlay NGOÀI Drawer thì form VẪN MỞ (overlay không có onClick), sau đó đóng bằng [Đóng]")
    public void adminCategoryViewClosesViaXEscNotOverlay() {
        AdminPageObject adminPage = openAdminTab("categories");
        String categoryName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, "Xem chi tiết danh mục ");
        if (categoryName == null || categoryName.isEmpty()) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de mo chi tiet");
        }

        // (1) Nut 'Đóng' o footer.
        adminPage.clickFirstCategoryDetail();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Phai mo duoc form xem danh muc truoc");
        adminPage.clickToElement(AdminPageUI.DYNAMIC_MODAL_FOOTER_CLOSE_BTN, "admin-category-form-modal");
        adminPage.waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_DETAIL);
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam 'Đóng' o footer phai dong duoc form xem danh muc");

        // (2) Nut X o header Drawer.
        adminPage.clickFirstCategoryDetail();
        adminPage.closeDetailModal("admin-category-form-modal");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam nut X o header phai dong duoc form xem danh muc");

        // (3) Phim ESC.
        adminPage.clickFirstCategoryDetail();
        adminPage.pressEscapeKey();
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam ESC phai dong duoc form xem danh muc");

        // (4) Bam ra vung overlay NGOAI Drawer: form VAN MO (overlay khong co
        // onClick); sau do dong bang nut 'Đóng' o footer.
        adminPage.clickFirstCategoryDetail();
        adminPage.clickFormOverlayOutsideDrawer("admin-category-form-modal");
        adminPage.sleepInSecond(1);
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam ra vung overlay ngoai Drawer thi form xem danh muc VAN phai mo (overlay khong dong form)");
        adminPage.clickToElement(AdminPageUI.DYNAMIC_MODAL_FOOTER_CLOSE_BTN, "admin-category-form-modal");
        adminPage.waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_DETAIL);
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_CATEGORY_DETAIL),
                "Bam 'Đóng' o footer phai dong duoc form xem danh muc sau khi thu overlay");
    }
}
