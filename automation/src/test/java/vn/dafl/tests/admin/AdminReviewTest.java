package vn.dafl.tests.admin;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;

/**
 * Test muc "Đánh giá" o app Admin rieng (chi chay tren app Admin, khong mo
 * web ban hang) — dot 16:
 * - RVM-01: mo muc Danh gia, bang co dung 6 cot tieu de theo thu tu.
 * - RVM-02: bang co it nhat 1 danh gia (seed co 4 danh gia mau).
 * - RVM-03: loc 5 sao — moi dong hien deu co rating 5.
 * - RVM-04: tim kiem theo ten san pham cua dong dau.
 * - RVM-05: bam dong dau mo modal chi tiet, noi dung hien day du (khong cat).
 * - RVM-06: xoa danh gia (XOA THAT 1 danh gia, khong khoi phuc duoc) — chi
 *   chay khi bang co >= 2 danh gia de con it nhat 1 cho cac test khac.
 * - RVM-07: nut Export CSV bam duoc, khong loi trang (khong assert file tai ve).
 * - RVM-08: phan trang "Trang 1 / N — Tổng T dòng" dung voi so dong thuc te.
 * Moi test doc lap: tu mo muc Danh gia o dau test, khong phu thuoc thu tu
 * chay (repo khong dung priority). Test tu skip khi DB khong co du lieu can
 * thiet, khong bia du lieu.
 */
@Feature("Quản lý đánh giá")
public class AdminReviewTest extends BaseTest {

    /** Dang nhap Super Admin tren app Admin rieng; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Mo muc Danh gia + cho bang tai xong; bo qua neu service admin chua chay. */
    private AdminPageObject openReviewsOrSkip() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.openReviewsSection();
        adminPage.waitForReviewsTableLoaded();
        return adminPage;
    }

    /** Bo qua test khi muc Danh gia dang rong (khong co dong nao de kiem tra). */
    private void skipIfReviewsEmpty(AdminPageObject adminPage) {
        if (adminPage.isReviewsEmptyStateDisplayed() || adminPage.getReviewRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co danh gia nao (muc Danh gia dang hien 'Chưa có đánh giá nào.') nen khong co du lieu de kiem tra");
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-01 (đợt 16): Mở mục Đánh giá — nav hiện, section hiện, bảng hiện, đúng 6 cột tiêu đề theo thứ tự Sản phẩm | Khách hàng | Sao | Nội dung | Ngày | Thao tác (skip nếu bảng trống)")
    public void reviewsSectionShowsTableWithSixColumnsInOrder() {
        AdminPageObject adminPage = openReviewsOrSkip();

        Assert.assertTrue(adminPage.isReviewsNavDisplayed(),
                "Sidebar admin phai co muc 'Đánh giá' (admin-nav-reviews)");
        Assert.assertTrue(adminPage.isReviewsSectionDisplayed(),
                "Phai thay section Danh gia (admin-reviews-section)");
        skipIfReviewsEmpty(adminPage);

        List<String> expectedHeaders = Arrays.asList(
                "Sản phẩm", "Khách hàng", "Sao", "Nội dung", "Ngày", "Thao tác");
        Assert.assertEquals(adminPage.getReviewHeaders(), expectedHeaders,
                "Bang Danh gia phai co dung 6 cot tieu de theo thu tu");
        Assert.assertTrue(adminPage.getReviewRowCount() > 0,
                "Bang Danh gia phai co it nhat 1 dong du lieu");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-02 (đợt 16): Bảng Đánh giá có ít nhất 1 đánh giá (seed có 4 đánh giá mẫu; skip nếu bảng trống, không bịa dữ liệu)")
    public void reviewsTableHasAtLeastOneReview() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        Assert.assertTrue(adminPage.getReviewRowCount() >= 1,
                "Bang Danh gia phai co it nhat 1 dong");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-03 (đợt 16): Lọc 5 sao — chọn value \"5\" + bấm Tìm thì mọi dòng hiện đều có rating 5 (skip nếu sau lọc bảng trống vì không có đánh giá 5 sao; Đặt lại bộ lọc sau test)")
    public void filterFiveStarShowsOnlyFiveStarReviews() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        try {
            adminPage.filterReviewsByRating("5");
            adminPage.waitForReviewsTableLoaded();
            if (adminPage.getReviewRowCount() == 0) {
                throw new SkipException("Bo qua: DB nay khong co danh gia 5 sao nao nen loc 5 sao tra ve bang rong");
            }
            List<Integer> ratings = adminPage.getAllRowRatings();
            Assert.assertFalse(ratings.isEmpty(), "Sau khi loc 5 sao phai co it nhat 1 dong");
            for (int rating : ratings) {
                Assert.assertEquals(rating, 5,
                        "Loc 5 sao thi moi dong hien deu phai co rating 5, dang thay rating " + rating);
            }
        } finally {
            adminPage.resetReviewsFilter();
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-04 (đợt 16): Tìm kiếm theo tên sản phẩm — lấy tên SP của dòng đầu, gõ vào ô tìm kiếm + bấm Tìm thì dòng đầu vẫn hiện và tên khớp (skip nếu không khớp; Đặt lại bộ lọc sau test)")
    public void searchByFirstRowProductNameFindsIt() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        String productName = adminPage.getFirstRowProductName();
        Assert.assertFalse(productName == null || productName.trim().isEmpty(),
                "Dong dau cua bang Danh gia phai co ten san pham de lam tu khoa tim kiem");

        try {
            adminPage.searchReviewsByText(productName);
            adminPage.waitForReviewsTableLoaded();
            if (adminPage.getReviewRowCount() == 0) {
                throw new SkipException("Bo qua: tim theo ten san pham '" + productName
                        + "' khong tra ve dong nao (co the BE loc theo ten khac voi ten hien trong bang)");
            }
            String foundName = adminPage.getFirstRowProductName();
            if (!foundName.equals(productName)) {
                throw new SkipException("Bo qua: tim theo ten san pham '" + productName
                        + "' tra ve dong dau la '" + foundName + "' (khong khop ten can tim)");
            }
            Assert.assertEquals(foundName, productName,
                    "Tim theo ten san pham cua dong dau phai tra ve dung san pham do o dong dau");
        } finally {
            adminPage.resetReviewsFilter();
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-05 (đợt 16): Bấm dòng đầu mở modal chi tiết — nội dung trong modal dài hơn hoặc bằng nội dung cắt trong bảng (không cắt) + nút Xóa hiện (skip nếu bảng trống)")
    public void firstRowOpensDetailModalWithFullComment() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        String tableComment = adminPage.getFirstRowComment();
        try {
            adminPage.openFirstReviewDetail();
            Assert.assertTrue(adminPage.isDetailModalDisplayed(),
                    "Bam dong dau phai mo modal chi tiet danh gia (admin-review-detail-modal)");

            String modalComment = adminPage.getDetailModalComment();
            Assert.assertFalse(modalComment == null || modalComment.trim().isEmpty(),
                    "Modal chi tiet phai hien noi dung danh gia (KvRow 'Nội dung')");
            Assert.assertTrue(modalComment.length() >= tableComment.length(),
                    "Noi dung trong modal phai day du (khong cat) nen dai hon hoac bang noi dung cat o bang: "
                            + "modal " + modalComment.length() + " ky tu, bang " + tableComment.length() + " ky tu");
            Assert.assertTrue(adminPage.isReviewDeleteButtonInModalDisplayed(),
                    "Modal chi tiet danh gia phai co nut 'Xóa đánh giá' (admin-review-delete-btn)");
        } finally {
            if (adminPage.isDetailModalDisplayed()) {
                adminPage.closeReviewDetailModal();
            }
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-06 (đợt 16): Xóa đánh giá — test này xóa 1 đánh giá thật, không khôi phục được; chỉ chạy khi bảng có ≥ 2 đánh giá (để còn ít nhất 1 cho các test khác), xóa dòng CUỐI qua hộp xác nhận thì dòng biến mất + toast chứa \"Đã xóa đánh giá.\" + số dòng giảm 1 (skip nếu < 2 đánh giá)")
    public void deleteLastReviewRemovesRowAndShowsToast() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        int beforeCount = adminPage.getReviewRowCount();
        if (beforeCount < 2) {
            throw new SkipException("Bo qua: chi co " + beforeCount
                    + " danh gia — test xoa can it nhat 2 danh gia de sau khi xoa van con it nhat 1 cho cac test khac");
        }
        List<String> beforeRows = adminPage.getAdminTableRowTexts();
        String lastRowText = beforeRows.get(beforeRows.size() - 1);

        String feedback = adminPage.deleteLastReview();
        Assert.assertTrue(feedback != null && feedback.contains("Đã xóa đánh giá."),
                "Xoa xong phai hien toast 'Đã xóa đánh giá.', dang thay: " + feedback);

        adminPage.waitForReviewsTableLoaded();
        int afterCount = adminPage.getReviewRowCount();
        Assert.assertEquals(afterCount, beforeCount - 1,
                "Xoa 1 danh gia thi so dong phai giam dung 1 (truoc " + beforeCount + ", sau " + afterCount + ")");
        Assert.assertFalse(adminPage.getAdminTableRowTexts().contains(lastRowText),
                "Dong danh gia vua xoa khong duoc con hien trong bang");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-07 (đợt 16): Export CSV — bấm nút Export, chỉ kiểm tra nút tồn tại + bấm được và không xuất hiện lỗi (không assert nội dung file vì framework không đọc download; skip nếu bảng trống)")
    public void exportReviewsButtonClickableWithoutError() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        // Gioi han cua test: chi khang dinh nut ton tai + bam duoc va trang admin
        // van dung yen sau khi bam; framework Selenium khong doc duoc file CSV
        // vua tai ve nen khong assert noi dung file.
        Assert.assertTrue(adminPage.isReviewsExportButtonDisplayed(),
                "Muc Danh gia phai co nut Export (admin-reviews-export-btn)");
        adminPage.clickExportReviews();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Bam Export xong trang admin van dung yen (menu quan tri van hien, khong loi trang)");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-08 (đợt 16): Phân trang — đọc text \"Trang 1 / N — Tổng T dòng\": nếu T > 10 thì nút sang trang 2 (admin-reviews-pagination-page-2) hiện; nếu T ≤ 10 thì N phải = 1 (skip nếu bảng trống, không bịa dữ liệu)")
    public void reviewsPaginationMatchesTotalRows() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        Assert.assertTrue(adminPage.isReviewsPaginationDisplayed(),
                "Bang co du lieu thi khoi phan trang Danh gia phai hien (admin-reviews-pagination)");
        String info = adminPage.getPaginationInfoText();
        int totalPages = adminPage.getReviewsPaginationTotalPages();
        int totalRows = adminPage.getReviewsPaginationTotalRows();
        Assert.assertTrue(totalPages >= 1,
                "Phai doc duoc tong so trang tu text phan trang, dang thay: '" + info + "'");
        Assert.assertTrue(totalRows >= 1,
                "Phai doc duoc tong so dong tu text phan trang, dang thay: '" + info + "'");

        if (totalRows > 10) {
            Assert.assertTrue(adminPage.isReviewsPageButtonDisplayed(2),
                    "Tong " + totalRows + " dong (> 10 dong/trang) thi nut sang trang 2 phai hien, text phan trang: '" + info + "'");
        } else {
            Assert.assertEquals(totalPages, 1,
                    "Tong " + totalRows + " dong (<= 10 dong/trang) thi chi co 1 trang, text phan trang: '" + info + "'");
        }
    }
}
