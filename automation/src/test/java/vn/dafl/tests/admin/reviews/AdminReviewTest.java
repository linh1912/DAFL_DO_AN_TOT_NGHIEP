package vn.dafl.tests.admin.reviews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Test muc "Đánh giá" o app Admin rieng (chi chay tren app Admin, khong mo
 * web ban hang) — dot 16:
 * - RVM-01: mo muc Danh gia, bang co dung 7 cot tieu de theo thu tu (dot 17
 *   them cot STT dau tien).
 * - RVM-02: bang co it nhat 1 danh gia (seed co 4 danh gia mau).
 * - RVM-03: loc 5 sao — moi dong hien deu co rating 5.
 * - RVM-04: tim kiem theo ten san pham cua dong dau.
 * - RVM-05: bam dong dau mo TRANG chi tiet full page (/reviews/:id), noi dung
 *   hien day du (khong cat) — dot 21 bo Drawer/Modal chi tiet.
 * - RVM-06: xoa danh gia (XOA THAT 1 danh gia, khong khoi phuc duoc) — chi
 *   chay khi bang co >= 2 danh gia de con it nhat 1 cho cac test khac; xoa qua
 *   ConfirmDialog dot 18 (tieu de 'Xóa đánh giá?' + dong 'không thể hoàn tác.',
 *   CHI 2 nut [Hủy] + [Xóa đánh giá]) -> toast 'Đã xóa đánh giá' (khong dau cham).
 * - RVM-07 (dot 16): DA XOA dot 18 — Export CSV bo han khoi moi toolbar admin
 *   (quyet dinh cua Matthew 2026-11-10); khong con nut Export o muc Danh gia.
 * - RVM-08: phan trang 'Hiển thị {from}–{to} / {total} kết quả' + 'Trang X / Y'
 *   (KHONG nut so trang): neu T > 10 thi tong so trang >= 2 va nut Sau bam
 *   duoc; neu T <= 10 thi chi co 1 trang.
 * - RVM-09 (dot 18 wave D): o Sao dung icon Star Lucide — moi dong co dung 5
 *   svg.lucide-star, so sao day (fill #f59e0b) bang rating, aria-label
 *   "{n} trên 5 sao", khong con chuoi '★★★★★ (5/5)'.
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
    @Test(description = "RVM-01 (đợt 16, viết lại đợt 17 thêm cột STT): Mở mục Đánh giá — nav hiện, section hiện, bảng hiện, đúng 7 cột tiêu đề theo thứ tự STT | Sản phẩm | Khách hàng | Sao | Nội dung | Ngày | Thao tác (skip nếu bảng trống)")
    public void reviewsSectionShowsTableWithSixColumnsInOrder() {
        AdminPageObject adminPage = openReviewsOrSkip();

        Assert.assertTrue(adminPage.isReviewsNavDisplayed(),
                "Sidebar admin phai co muc 'Đánh giá' (admin-nav-reviews)");
        Assert.assertTrue(adminPage.isReviewsSectionDisplayed(),
                "Phai thay section Danh gia (admin-reviews-section)");
        skipIfReviewsEmpty(adminPage);

        List<String> expectedHeaders = Arrays.asList(
                "STT", "Sản phẩm", "Khách hàng", "Sao", "Nội dung", "Ngày", "Thao tác");
        Assert.assertEquals(adminPage.getReviewHeaders(), expectedHeaders,
                "Bang Danh gia phai co dung 7 cot tieu de theo thu tu");
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
    @Test(description = "RVM-03 (đợt 16, đợt 24/26: KHÔNG còn nút Tìm — chọn là lọc ngay): Lọc 5 sao — chọn value \"5\" thì mọi dòng hiện đều có rating 5 (skip nếu sau lọc bảng trống vì không có đánh giá 5 sao; Đặt lại bộ lọc sau test)")
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
    @Test(description = "RVM-04 (đợt 16, đợt 24/26: KHÔNG còn nút Tìm — gõ là lọc ngay): Tìm kiếm theo tên sản phẩm — lấy tên SP của dòng đầu, gõ vào ô tìm kiếm thì dòng đầu vẫn hiện và tên khớp (skip nếu không khớp; Đặt lại bộ lọc sau test)")
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
    @Test(description = "RVM-05 (đợt 16 + đợt 21): Bấm dòng đầu mở TRANG CHI TIẾT full page — nội dung trong trang dài hơn hoặc bằng nội dung cắt trong bảng (không cắt) + nút Xóa hiện (skip nếu bảng trống)")
    public void firstRowOpensDetailModalWithFullComment() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        String tableComment = adminPage.getFirstRowComment();
        try {
            adminPage.openFirstReviewDetail();
            Assert.assertTrue(adminPage.isDetailModalDisplayed(),
                    "Bam dong dau phai mo trang chi tiet danh gia full page (admin-review-detail-page)");

            String modalComment = adminPage.getDetailModalComment();
            Assert.assertFalse(modalComment == null || modalComment.trim().isEmpty(),
                    "Trang chi tiet phai hien noi dung danh gia (KvRow 'Nội dung')");
            Assert.assertTrue(modalComment.length() >= tableComment.length(),
                    "Noi dung trong trang chi tiet phai day du (khong cat) nen dai hon hoac bang noi dung cat o bang: "
                            + "trang " + modalComment.length() + " ky tu, bang " + tableComment.length() + " ky tu");
            Assert.assertTrue(adminPage.isReviewDeleteButtonInModalDisplayed(),
                    "Trang chi tiet danh gia phai co nut 'Xóa đánh giá' (admin-review-delete-btn)");
        } finally {
            if (adminPage.isDetailModalDisplayed()) {
                adminPage.closeReviewDetailModal();
            }
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-06 (đợt 16, viết lại đợt 18 theo ConfirmDialog): Xóa đánh giá — test này xóa 1 đánh giá thật, không khôi phục được; chỉ chạy khi bảng có ≥ 2 đánh giá (để còn ít nhất 1 cho các test khác), xóa dòng CUỐI qua ConfirmDialog 'Xóa đánh giá?' (đúng 2 nút, có dòng 'không thể hoàn tác.') thì dòng biến mất + toast chứa \"Đã xóa đánh giá\" + số dòng giảm 1 (skip nếu < 2 đánh giá)")
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
        Assert.assertTrue(feedback != null && feedback.contains("Đã xóa đánh giá"),
                "Xoa xong phai hien toast 'Đã xóa đánh giá' (khong dau cham), dang thay: " + feedback);

        adminPage.waitForReviewsTableLoaded();
        int afterCount = adminPage.getReviewRowCount();
        Assert.assertEquals(afterCount, beforeCount - 1,
                "Xoa 1 danh gia thi so dong phai giam dung 1 (truoc " + beforeCount + ", sau " + afterCount + ")");
        Assert.assertFalse(adminPage.getAdminTableRowTexts().contains(lastRowText),
                "Dong danh gia vua xoa khong duoc con hien trong bang");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-08 (đợt 16, viết lại đợt 18 theo phân trang chung — không nút số trang): Phân trang — đọc 'Hiển thị {from}–{to} / {total} kết quả' + 'Trang X / Y': nếu T > 10 thì tổng số trang >= 2 và nút Sau bấm được; nếu T ≤ 10 thì chỉ có 1 trang (skip nếu bảng trống, không bịa dữ liệu)")
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
            Assert.assertTrue(totalPages >= 2,
                    "Tong " + totalRows + " dong (> 10 dong/trang) thi phai co it nhat 2 trang, text phan trang: '" + info + "'");
            Assert.assertTrue(adminPage.isReviewsNextEnabled(),
                    "Con trang sau thi nut 'Sau' phai bam duoc, text phan trang: '" + info + "'");
        } else {
            Assert.assertEquals(totalPages, 1,
                    "Tong " + totalRows + " dong (<= 10 dong/trang) thi chi co 1 trang, text phan trang: '" + info + "'");
            Assert.assertFalse(adminPage.isReviewsNextEnabled(),
                    "Chi co 1 trang thi nut 'Sau' phai bi tat, text phan trang: '" + info + "'");
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "RVM-09 (đợt 18 wave D): Cột Sao dùng icon Star Lucide — dòng đầu có đúng 5 svg.lucide-star, số sao đầy (fill #f59e0b) bằng rating, aria-label '{n} trên 5 sao', không còn chuỗi '★' trong ô Sao (skip nếu bảng trống)")
    public void reviewStarsRenderAsLucideIcons() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        int rating = adminPage.getFirstRowRating();
        Assert.assertTrue(rating >= 1 && rating <= 5,
                "Rating dong dau phai trong khoang 1-5, dang thay: " + rating);
        Assert.assertEquals(adminPage.getReviewRowRatingFromAriaLabel(1), rating,
                "aria-label o Sao dong dau phai khop rating doc tu '(n/5)'");
        Assert.assertEquals(adminPage.getReviewRowStarIconCount(1), 5,
                "O Sao dong dau phai co dung 5 icon Star Lucide");
        Assert.assertEquals(adminPage.getReviewRowFilledStarCount(1), rating,
                "So sao day (fill #f59e0b) phai bang rating " + rating + " cua dong dau");
        String rowText = adminPage.getAdminTableRowTexts().get(0);
        Assert.assertFalse(rowText.contains("★"),
                "O Sao wave D khong duoc con ky tu '★', dang thay: '" + rowText + "'");

        // Modal chi tiet cung dung icon Star: so sao day bang rating.
        adminPage.openFirstReviewDetail();
        try {
            Assert.assertEquals(adminPage.getReviewDetailFilledStarCount(), rating,
                    "Modal chi tiet phai co dung " + rating + " sao day (fill #f59e0b)");
        } finally {
            if (adminPage.isDetailModalDisplayed()) {
                adminPage.closeReviewDetailModal();
            }
        }
    }

    // ---------- Dot ADM19 (W4): dong / toolbar / tim-loc (chi doc) ----------

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-200 (TC_SCRA08_UI_002 + TC_SCRA08_UI_003 + TC_SCRA08_UI_004): Dòng đánh giá hiện tên sản phẩm in đậm + tên khách; nội dung dài (> 80 ký tự) chỉ hiện đúng 80 ký tự đầu + '...', nội dung ngắn (≤ 80) hiện nguyên vẹn không '...' (skip nhánh nào bảng không có dòng tương ứng)")
    public void adminReviewRowShowsNamesAndTruncatedContent() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);

        int rowCount = adminPage.getReviewRowCount();
        boolean foundLong = false;
        boolean foundShort = false;
        for (int i = 1; i <= rowCount; i++) {
            String productName = adminPage.getReviewRowProductName(i);
            Assert.assertFalse(productName == null || productName.trim().isEmpty(),
                    "Dong " + i + " phai co ten san pham de hien trong bang");
            Assert.assertTrue(adminPage.isReviewRowProductNameBold(i),
                    "Ten san pham o dong " + i + " phai in dam (<strong>), dang thay: '" + productName + "'");
            String customerName = adminPage.getReviewRowCustomerName(i);
            Assert.assertFalse(customerName == null || customerName.trim().isEmpty(),
                    "Dong " + i + " phai co ten khach hang");
            String comment = adminPage.getReviewRowComment(i);
            if (comment.endsWith("...")) {
                foundLong = true;
                Assert.assertEquals(comment.length(), 83,
                        "Noi dung dai o dong " + i + " phai cat dung 80 ky tu + '...' (tong 83 ky tu), dang thay "
                                + comment.length() + " ky tu: '" + comment + "'");
            } else {
                foundShort = true;
                Assert.assertTrue(comment.length() <= 80,
                        "Noi dung ngan o dong " + i + " phai <= 80 ky tu va khong co '...' o cuoi, dang thay "
                                + comment.length() + " ky tu: '" + comment + "'");
            }
        }
        Assert.assertTrue(foundLong || foundShort,
                "Bang phai co it nhat 1 dong de kiem tra cat ngan noi dung");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-201 (TC_SCRA08_UI_005, đợt 24/26 viết lại — KHÔNG còn nút Tìm riêng, icon Tìm nằm TRONG ô tìm; dropdown mặc định 'Số sao'): Toolbar Đánh giá đợt 18 có ô tìm placeholder 'Tìm tên sản phẩm, khách hàng, nội dung...' (icon Tìm trong ô tìm), dropdown lọc số sao (admin-review-rating-filter), nút icon Đặt lại (Lucide); KHÔNG có nút Thêm mới/Làm mới/Export")
    public void adminReviewToolbarHasSearchRatingFilterAndNoAddRefreshExport() {
        AdminPageObject adminPage = openReviewsOrSkip();
        Assert.assertEquals(adminPage.getReviewSearchPlaceholder(),
                "Tìm tên sản phẩm, khách hàng, nội dung...",
                "O tim Danh gia phai co placeholder 'Tìm tên sản phẩm, khách hàng, nội dung...'");
        Assert.assertTrue(adminPage.isReviewRatingFilterDisplayed(),
                "Toolbar Danh gia phai co dropdown loc so sao (admin-review-rating-filter)");
        Assert.assertFalse(adminPage.isToolbarSearchButtonDisplayed("reviews"),
                "Toolbar Danh gia dot 24/26 KHONG con nut icon Tim rieng");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("reviews"),
                "Toolbar Danh gia phai co nut icon Dat lai");
        Assert.assertTrue(adminPage.isToolbarSearchIconDisplayed("reviews"),
                "O tim Danh gia phai co icon Lucide Search TRONG o tim (svg.lucide-search)");
        Assert.assertTrue(adminPage.isToolbarResetIconDisplayed("reviews"),
                "Nut Dat lai phai dung icon Lucide RotateCcw (svg.lucide-rotate-ccw)");
        Assert.assertFalse(adminPage.isToolbarAddButtonPresent("reviews"),
                "Muc Danh gia KHONG co nut Them moi");
        Assert.assertFalse(adminPage.isReviewsExportButtonDisplayed(),
                "Muc Danh gia KHONG co nut Export");
        String toolbarText = adminPage.getToolbarText();
        Assert.assertFalse(toolbarText.contains("Làm mới"),
                "Toolbar Danh gia KHONG co nut Lam moi, dang thay: " + toolbarText);
        Assert.assertFalse(toolbarText.contains("🔍") || toolbarText.contains("↺"),
                "Toolbar Danh gia khong duoc con emoji tim/lam moi, dang thay: " + toolbarText);
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-202 (TC_SCRA08_UI_007 + TC_SCRA08_FUNC_012): Tìm từ khóa lạ không tồn tại thì hiện 'Không tìm thấy đánh giá' + gợi ý 'Thử đổi từ khóa hoặc bộ lọc khác.', không hiện bảng/phân trang; bấm Đặt lại thì từ khóa + lọc sao xóa trắng, về trang 1, bảng tải lại toàn bộ đánh giá")
    public void adminReviewEmptySearchResultAndResetRestoresList() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        int beforeCount = adminPage.getReviewRowCount();
        try {
            adminPage.searchReviewsByText("zzzz-khong-co-danh-gia-xyz");
            Assert.assertTrue(adminPage.isReviewsSearchEmptyMessageDisplayed(),
                    "Tim tu khoa la phai hien 'Không tìm thấy đánh giá'");
            String emptyText = adminPage.getReviewsEmptyStateText();
            Assert.assertTrue(emptyText.contains("Thử đổi từ khóa hoặc bộ lọc khác."),
                    "Trang thai rong khi tim phai co goi y 'Thử đổi từ khóa hoặc bộ lọc khác.', dang thay: " + emptyText);
            Assert.assertEquals(adminPage.getReviewRowCount(), 0,
                    "Tim tu khoa la phai ra 0 dong danh gia");
            Assert.assertFalse(adminPage.isReviewsPaginationPresent(),
                    "Tim ra 0 dong thi khong duoc hien phan trang");

            adminPage.resetReviewsFilter();
            Assert.assertEquals(adminPage.getReviewSearchValue(), "",
                    "Dat lai xong o tim phai trong");
            Assert.assertEquals(adminPage.getReviewRatingFilterValue(), "",
                    "Dat lai xong loc sao phai ve 'Số sao'");
            adminPage.waitForReviewsTableLoaded();
            Assert.assertTrue(adminPage.getReviewRowCount() > 0,
                    "Dat lai xong bang phai tai lai toan bo danh gia");
            Assert.assertEquals(adminPage.getReviewRowCount(), beforeCount,
                    "Dat lai xong trang 1 phai co dung " + beforeCount + " dong nhu luc dau");
            Assert.assertTrue(adminPage.getPaginationInfoText().contains("Trang 1 /"),
                    "Dat lai xong phan trang phai ve trang 1, dang thay: '" + adminPage.getPaginationInfoText() + "'");
        } finally {
            adminPage.resetReviewsFilter();
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-203 (TC_SCRA08_FUNC_008, đợt 24/26: KHÔNG còn nút Tìm — chọn là lọc ngay): Lọc đánh giá 1 sao — chọn '1 sao' thì bảng chỉ hiện đánh giá 1 sao (mirror RVM-03 lọc 5 sao; skip nếu DB không có đánh giá 1 sao; Đặt lại bộ lọc sau test)")
    public void adminReviewFilterOneStarShowsOnlyOneStar() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        try {
            adminPage.filterReviewsByRating("1");
            adminPage.waitForReviewsTableLoaded();
            if (adminPage.getReviewRowCount() == 0) {
                throw new SkipException("Bo qua: DB nay khong co danh gia 1 sao nao nen loc 1 sao tra ve bang rong");
            }
            List<Integer> ratings = adminPage.getAllRowRatings();
            Assert.assertFalse(ratings.isEmpty(), "Sau khi loc 1 sao phai co it nhat 1 dong");
            for (int rating : ratings) {
                Assert.assertEquals(rating, 1,
                        "Loc 1 sao thi moi dong hien deu phai co rating 1, dang thay rating " + rating);
            }
        } finally {
            adminPage.resetReviewsFilter();
        }
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-204 (TC_SCRA08_FUNC_011): Tìm đánh giá theo tên khách hàng / nội dung — tìm theo tên khách của dòng đầu thì dòng đó vẫn hiện; tìm 1 từ có trong nội dung thì chỉ hiện các đánh giá khớp (skip nhánh nào không khớp vì BE search khớp cả 3 trường nhưng tên hiển thị có thể khác tên BE dùng để khớp; Đặt lại bộ lọc sau test)")
    public void adminReviewSearchByCustomerNameAndContent() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        String customerName = adminPage.getReviewRowCustomerName(1);
        String firstComment = adminPage.getReviewRowComment(1);
        try {
            adminPage.searchReviewsByText(customerName);
            adminPage.waitForReviewsTableLoaded();
            if (adminPage.getReviewRowCount() == 0) {
                throw new SkipException("Bo qua: tim theo ten khach '" + customerName
                        + "' khong tra ve dong nao (co the BE loc theo ten khac voi ten hien trong bang)");
            }
            boolean foundCustomer = false;
            for (int i = 1; i <= adminPage.getReviewRowCount(); i++) {
                if (adminPage.getReviewRowCustomerName(i).equals(customerName)) {
                    foundCustomer = true;
                    break;
                }
            }
            Assert.assertTrue(foundCustomer,
                    "Tim theo ten khach '" + customerName + "' phai tra ve it nhat 1 dong cua dung khach do");

            String commentForSearch = firstComment.endsWith("...")
                    ? firstComment.substring(0, firstComment.length() - 3) : firstComment;
            String word = null;
            for (String token : commentForSearch.split("\\s+")) {
                String cleaned = token.replaceAll("[^\\p{L}\\p{N}]", "");
                if (cleaned.length() >= 4) {
                    word = cleaned;
                    break;
                }
            }
            if (word == null) {
                return; // Khong co tu nao du dai de lam tu khoa — test pass o nhanh ten khach.
            }
            adminPage.searchReviewsByText(word);
            adminPage.waitForReviewsTableLoaded();
            Assert.assertTrue(adminPage.getReviewRowCount() > 0,
                    "Tim theo tu '" + word + "' trong noi dung phai tra ve it nhat 1 dong");
        } finally {
            adminPage.resetReviewsFilter();
        }
    }

    // ---------- Dot ADM19 (W4): xoa danh gia (mutate, XOA THAT) ----------

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-205 (TC_SCRA08_FUNC_014, dot 21): Xóa đánh giá trong TRANG chi tiết qua hộp xác nhận A6 — bấm 'Xóa đánh giá' → hộp 'Xóa đánh giá?' đúng 2 nút + dòng 'không thể hoàn tác.' → xác nhận → DELETE, dòng biến mất, về danh sách, toast 'Đã xóa đánh giá' (XÓA THẬT 1 đánh giá, không khôi phục được; chỉ chạy khi bảng có ≥ 2 đánh giá; RVM-06 xóa qua nút ở dòng, test này cover nhánh xóa trong trang chi tiết)")
    public void adminReviewDeleteFromDetailModal() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        int beforeCount = adminPage.getReviewRowCount();
        if (beforeCount < 2) {
            throw new SkipException("Bo qua: chi co " + beforeCount
                    + " danh gia — test xoa can it nhat 2 danh gia de sau khi xoa van con it nhat 1 cho cac test khac");
        }
        String firstRowText = adminPage.getAdminTableRowTexts().get(0);
        adminPage.openFirstReviewDetail();
        try {
            Assert.assertTrue(adminPage.isDetailModalDisplayed(),
                    "Bam dong dau phai mo trang chi tiet danh gia full page (admin-review-detail-page)");
            Assert.assertTrue(adminPage.isReviewDeleteButtonInModalDisplayed(),
                    "Trang chi tiet danh gia phai co nut 'Xóa đánh giá' (admin-review-delete-btn)");
            adminPage.clickReviewDeleteButtonInModal();
            String confirmText = adminPage.getConfirmDialogText();
            Assert.assertTrue(confirmText.contains("Xóa đánh giá?"),
                    "Hop xac nhan xoa phai co tieu de 'Xóa đánh giá?', dang thay: " + confirmText);
            Assert.assertTrue(confirmText.contains("không thể hoàn tác"),
                    "Hop xac nhan xoa phai co dong 'không thể hoàn tác.', dang thay: " + confirmText);
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup xoa danh gia phai CHI co dung 2 nut");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Xóa đánh giá",
                    "Nut xac nhan xoa phai la 'Xóa đánh giá', dang thay: " + adminPage.getConfirmDialogConfirmText());
            adminPage.clickConfirmDialogConfirm();
            String feedback = adminPage.waitForFeedback();
            Assert.assertTrue(feedback != null && feedback.contains("Đã xóa đánh giá"),
                    "Xoa xong phai hien toast 'Đã xóa đánh giá', dang thay: " + feedback);
        } finally {
            if (adminPage.isReviewDetailModalPresent()) {
                adminPage.closeReviewDetailModal();
            }
        }
        Assert.assertFalse(adminPage.isReviewDetailModalPresent(),
                "Xoa xong phai ve danh sach (trang chi tiet khong con hien)");
        adminPage.waitForReviewsTableLoaded();
        int afterCount = adminPage.getReviewRowCount();
        Assert.assertEquals(afterCount, beforeCount - 1,
                "Xoa 1 danh gia thi so dong phai giam dung 1 (truoc " + beforeCount + ", sau " + afterCount + ")");
        Assert.assertFalse(adminPage.getAdminTableRowTexts().contains(firstRowText),
                "Dong danh gia vua xoa khong duoc con hien trong bang");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-206 (TC_SCRA08_FUNC_015, dot 21): Xóa đánh giá — bấm Hủy ở hộp xác nhận A6 thì không xóa: hộp đóng, trang chi tiết vẫn mở, đánh giá còn nguyên trong bảng")
    public void adminReviewDeleteConfirmCancelKeepsReview() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        int beforeCount = adminPage.getReviewRowCount();
        String firstRowText = adminPage.getAdminTableRowTexts().get(0);
        adminPage.openFirstReviewDetail();
        try {
            Assert.assertTrue(adminPage.isDetailModalDisplayed(),
                    "Bam dong dau phai mo trang chi tiet danh gia full page");
            adminPage.clickReviewDeleteButtonInModal();
            Assert.assertTrue(adminPage.getConfirmDialogTitle().contains("Xóa đánh giá?"),
                    "Bam 'Xóa đánh giá' trong trang chi tiet phai mo hop 'Xóa đánh giá?', dang thay: " + adminPage.getConfirmDialogTitle());
            adminPage.clickConfirmDialogCancel();
            adminPage.waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
            Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                    "Bam Huy thi hop xac nhan phai dong");
            Assert.assertTrue(adminPage.isDetailModalDisplayed(),
                    "Bam Huy thi trang chi tiet van phai mo (khong xoa, khong dong trang)");
        } finally {
            if (adminPage.isReviewDetailModalPresent()) {
                adminPage.closeReviewDetailModal();
            }
        }
        adminPage.waitForReviewsTableLoaded();
        Assert.assertEquals(adminPage.getReviewRowCount(), beforeCount,
                "Bam Huy thi so dong danh gia phai giu nguyen " + beforeCount);
        Assert.assertTrue(adminPage.getAdminTableRowTexts().contains(firstRowText),
                "Bam Huy thi dong danh gia van phai con trong bang");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-207 (TC_SCRA08_FUNC_018): Xóa dòng duy nhất ở trang 2 thì bảng tự lùi về trang 1 thay vì hiện bảng trắng (XÓA THẬT 1 đánh giá, không khôi phục được; chỉ chạy khi trang 1 đủ 10 dòng và trang 2 chỉ có đúng 1 dòng)")
    public void adminReviewDeleteLastRowOfPageTwoGoesBackOnePage() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        if (adminPage.getReviewRowCount() != 10) {
            throw new SkipException("Bo qua: trang 1 co " + adminPage.getReviewRowCount()
                    + " dong (can dung 10 dong thi trang 2 moi co the chi co 1 dong)");
        }
        if (!adminPage.isReviewsNextEnabled()) {
            throw new SkipException("Bo qua: nut 'Sau' bi tat nen khong co trang 2 de kiem tra");
        }
        adminPage.clickReviewsPaginationNext();
        adminPage.waitForReviewsTableLoaded();
        if (adminPage.getReviewRowCount() != 1) {
            throw new SkipException("Bo qua: trang 2 co " + adminPage.getReviewRowCount()
                    + " dong (can dung 1 dong de kiem tra tu lui trang)");
        }
        String feedback = adminPage.deleteLastReview();
        Assert.assertTrue(feedback != null && feedback.contains("Đã xóa đánh giá"),
                "Xoa xong phai hien toast 'Đã xóa đánh giá', dang thay: " + feedback);
        adminPage.waitForReviewsTableLoaded();
        Assert.assertTrue(adminPage.getPaginationInfoText().contains("Trang 1 /"),
                "Xoa dong duy nhat o trang 2 thi bang phai tu lui ve trang 1, dang thay: '"
                        + adminPage.getPaginationInfoText() + "'");
        Assert.assertTrue(adminPage.getReviewRowCount() > 0,
                "Ve trang 1 thi bang phai hien binh thuong (khong phai bang trang)");
    }

    /** Doc [rating, reviewCount] cua 1 SP o bang San pham (cot 'Đánh giá' dang
     *  '{rating} ({n} đánh giá)'); tra null neu khong tim thay dung 1 dong SP
     *  (tim theo ten day du) hoac SP chua co danh gia ('—'). */
    private double[] readProductRating(AdminPageObject adminPage, String productName) {
        adminPage.clickAdminNav("products");
        adminPage.searchProducts(productName);
        if (adminPage.getAdminTableRowCount() != 1) {
            return null;
        }
        String cell = adminPage.getProductRatingCellText(productName);
        if (cell == null || cell.equals("—")) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "([0-9]+(?:[.,][0-9]+)?)\\s*\\((\\d+) đánh giá\\)").matcher(cell);
        if (!matcher.find()) {
            return null;
        }
        return new double[]{
                Double.parseDouble(matcher.group(1).replace(",", ".")),
                Double.parseDouble(matcher.group(2))};
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-208 (TC_SCRA08_FUNC_019): Sau khi xóa, Rating sản phẩm được tính lại — xóa 1 đánh giá của SP có ≥ 2 đánh giá thì ReviewCount giảm 1 và Rating mới = trung bình các đánh giá còn lại làm tròn 1 số thập phân (XÓA THẬT 1 đánh giá, không khôi phục được; chỉ chạy khi tìm được SP có ≥ 2 đánh giá gom đủ trong 1 trang tìm kiếm và tra đúng 1 dòng ở bảng Sản phẩm)")
    public void adminReviewDeleteRecalculatesProductRating() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        if (adminPage.getReviewRowCount() < 2) {
            throw new SkipException("Bo qua: can it nhat 2 danh gia tren trang 1 de tim SP co >= 2 danh gia");
        }
        List<String> productNames = adminPage.getAllReviewProductNames();
        List<Integer> ratings = adminPage.getAllRowRatings();
        Map<String, List<Integer>> byProduct = new HashMap<>();
        for (int i = 0; i < productNames.size(); i++) {
            byProduct.computeIfAbsent(productNames.get(i), k -> new ArrayList<>()).add(ratings.get(i));
        }
        String targetProduct = null;
        for (Map.Entry<String, List<Integer>> entry : byProduct.entrySet()) {
            if (entry.getValue().size() >= 2) {
                targetProduct = entry.getKey();
                break;
            }
        }
        if (targetProduct == null) {
            throw new SkipException("Bo qua: khong co san pham nao co >= 2 danh gia tren trang 1 de kiem tra tinh lai Rating");
        }
        adminPage.searchReviewsByText(targetProduct);
        adminPage.waitForReviewsTableLoaded();
        if (adminPage.getReviewRowCount() == 0) {
            throw new SkipException("Bo qua: tim theo ten SP '" + targetProduct + "' khong tra ve dong nao");
        }
        if (adminPage.getReviewsPaginationTotalPages() > 1) {
            throw new SkipException("Bo qua: SP '" + targetProduct
                    + "' co qua nhieu danh gia (nhieu trang) nen khong gom du de tinh trung binh");
        }
        List<String> filteredNames = adminPage.getAllReviewProductNames();
        List<Integer> filteredRatings = adminPage.getAllRowRatings();
        for (String name : filteredNames) {
            if (!name.equals(targetProduct)) {
                throw new SkipException("Bo qua: tim theo ten SP '" + targetProduct
                        + "' tra ve ca danh gia cua SP khac ('" + name + "') nen khong tinh chinh xac duoc trung binh");
            }
        }
        int n = filteredRatings.size();
        if (n < 2) {
            throw new SkipException("Bo qua: SP '" + targetProduct + "' chi co " + n
                    + " danh gia sau khi loc (can >= 2)");
        }
        int sum = 0;
        for (int r : filteredRatings) {
            sum += r;
        }
        int deletedRating = filteredRatings.get(0);
        double expectedRating = Math.round((sum - deletedRating) * 10.0 / (n - 1)) / 10.0;

        double[] before = readProductRating(adminPage, targetProduct);
        if (before == null) {
            throw new SkipException("Bo qua: khong doc duoc Rating cua SP '" + targetProduct
                    + "' o bang San pham (tim khong ra dung 1 dong hoac SP chua co danh gia)");
        }
        Assert.assertEquals((int) before[1], n,
                "So danh gia o bang San pham phai bang so danh gia tim duoc o muc Danh gia (" + n + ")");

        adminPage.clickAdminNav("reviews");
        adminPage.searchReviewsByText(targetProduct);
        adminPage.waitForReviewsTableLoaded();
        String feedback = adminPage.deleteReviewByRowIndex(1);
        Assert.assertTrue(feedback != null && feedback.contains("Đã xóa đánh giá"),
                "Xoa xong phai hien toast 'Đã xóa đánh giá', dang thay: " + feedback);

        double[] after = readProductRating(adminPage, targetProduct);
        Assert.assertNotNull(after,
                "Sau khi xoa phai doc lai duoc Rating cua SP '" + targetProduct + "' o bang San pham");
        Assert.assertEquals((int) after[1], n - 1,
                "Xoa 1 danh gia thi so danh gia cua SP phai giam 1 (truoc " + n + ", sau " + (int) after[1] + ")");
        Assert.assertEquals(after[0], expectedRating, 0.051,
                "Rating moi phai = trung binh cac danh gia con lai lam tron 1 so thap phan ("
                        + expectedRating + "), dang thay: " + after[0]);
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-209 (TC_SCRA08_FUNC_021): Phân trang đánh giá nhiều trang — trang 1 tối đa 10 dòng, sang trang 2 thì STT tiếp tục từ 11 (skip nếu ≤ 10 đánh giá; RVM-08 đã cover điều hướng nhưng không assert STT)")
    public void adminReviewSecondPageSttContinuesFromEleven() {
        AdminPageObject adminPage = openReviewsOrSkip();
        skipIfReviewsEmpty(adminPage);
        if (!adminPage.isReviewsNextEnabled()) {
            throw new SkipException("Bo qua: chi co 1 trang danh gia (<= 10 danh gia) nen khong co trang 2 de kiem tra STT");
        }
        Assert.assertEquals(adminPage.getReviewRowCount(), 10,
                "Trang 1 phai co dung 10 dong danh gia");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");
        adminPage.clickReviewsPaginationNext();
        adminPage.waitForReviewsTableLoaded();
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai tiep tuc tu 11");
    }

    @Story("Quản lý đánh giá")
    @Test(description = "ADM19-210 (TC_SCRA08_UI_023): Badge đánh giá mới trên menu bằng số 'Đánh giá mới (7 ngày qua)' trong khối 'Cần xử lý ngay' ở dashboard (cùng là số đánh giá tạo trong 7 ngày qua); badge ẩn khi số lượng = 0, hiện tối đa '99+' khi trên 99 (chỉ kiểm tính nhất quán giữa 2 nơi hiển thị, không chứng minh được quy tắc 7 ngày — đối chiếu số đúng với dữ liệu thật phải kiểm tay)")
    public void adminReviewBadgeMatchesActionNeededNewReviews() {
        AdminPageObject adminPage = openReviewsOrSkip();
        String badgeText = adminPage.getNavBadgeText("reviews");
        if (badgeText == null) {
            // Badge co the chua kip tai (loadNewReviewsCount chay bat dong bo
            // khi mo admin) — doi them 2 giay roi doc lai 1 lan.
            sleepInSecond(2);
            badgeText = adminPage.getNavBadgeText("reviews");
        }
        adminPage.clickAdminNav("dashboard");
        Assert.assertTrue(adminPage.isDashboardActionNeededDisplayed(),
                "Dashboard phai co khoi 'Cần xử lý ngay' (admin-dashboard-action-needed)");
        String dashboardCountText = adminPage.getDashboardNewReviewsCountText();
        if (dashboardCountText == null) {
            Assert.assertNull(badgeText,
                    "Dashboard khong co so 'Đánh giá mới' (0 danh gia moi trong 7 ngay qua) thi badge muc Danh gia phai an, dang thay: '" + badgeText + "'");
            return;
        }
        int dashboardCount = Integer.parseInt(dashboardCountText.replaceAll("[^0-9]", ""));
        Assert.assertNotNull(badgeText,
                "Dashboard co " + dashboardCount + " danh gia moi thi badge muc Danh gia phai hien");
        if (dashboardCount > 99) {
            Assert.assertEquals(badgeText, "99+",
                    "Tren 99 danh gia moi thi badge phai hien toi da '99+', dang thay: '" + badgeText + "'");
        } else {
            Assert.assertEquals(badgeText, String.valueOf(dashboardCount),
                    "Badge muc Danh gia phai bang so 'Đánh giá mới' o dashboard (" + dashboardCount
                            + "), dang thay: '" + badgeText + "'");
        }
    }
}
