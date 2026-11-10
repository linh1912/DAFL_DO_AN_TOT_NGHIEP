package vn.dafl.tests.admin.system;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageObjects.AuthModalPageObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Test tinh nang moi dot 13 o app Admin rieng (chi chay tren app Admin, khong
 * mo web ban hang):
 * - ADM-W13-01: trang dang nhap admin khong co nut quay lai, khung can giua.
 * - ADM-W13-02: link 'Quên mật khẩu' hien o form dang nhap admin (chi kiem UI,
 *   khong gui mail that).
 * - ADM-W13-04: loc ngay + Export o nhat ky he thong (tab Tra hang).
 * (ADM-W13-03 nut 'Ghi nhận đã hoàn tiền' thuoc phan he Tra hang nen chuyen
 * sang orders/AdminOrderTest khi chia thu muc admin dot 17.)
 * Dot 17 khong doi giao dien cac muc nay nen cac test giu nguyen logic.
 * Test tu bo qua (skip, khong tinh fail) khi service admin chua chay hoac tai
 * khoan admin@dafl.vn chua duoc seed tren DB nay.
 */
@Feature("Tính năng đợt 13")
public class AdminWave13Test extends BaseTest {

    /** Dang nhap Super Admin tren app Admin rieng; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Mo trang dang nhap Admin rieng va doi form hien; bo qua neu service chua chay. */
    private AuthModalPageObject openAdminLoginOrSkip() {
        AuthModalPageObject authModal = new AuthModalPageObject();
        authModal.openPageUrl(GlobalConstants.getAdminAppUrl() + "/login");
        try {
            authModal.waitForModalVisible();
        } catch (Exception e) {
            throw new SkipException("Bo qua: trang Admin rieng " + GlobalConstants.getAdminAppUrl()
                    + " chua chay (can 'docker compose up -d --build' de service admin len)");
        }
        return authModal;
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM-W13-01 (đợt 13, mục 10): Trang đăng nhập admin KHÔNG có nút quay lại, khung đăng nhập căn giữa màn hình")
    public void adminLoginPageHasNoBackButtonAndCenteredWrapper() {
        AuthModalPageObject authModal = openAdminLoginOrSkip();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Phai thay form dang nhap admin truoc");
        Assert.assertFalse(authModal.isBackButtonDisplayed(),
                "Trang dang nhap admin khong duoc co nut quay lai (auth-close-btn phai vang mat)");
        Assert.assertTrue(authModal.isAuthPageWrapperDisplayed(),
                "Phai thay khung trang dang nhap (auth-page)");
        String wrapperClass = authModal.getAuthPageWrapperClass();
        Assert.assertTrue(wrapperClass != null && wrapperClass.contains("auth-page-wrapper"),
                "Khung dang nhap phai co class 'auth-page-wrapper' (can giua), dang thay: " + wrapperClass);
        Assert.assertEquals(authModal.getModalTitle(), "Đăng nhập quản trị",
                "Trang dang nhap admin phai co tieu de 'Đăng nhập quản trị'");
    }

    @Story("Đăng nhập admin")
    @Test(description = "ADM-W13-02 (đợt 13, mục 1): Link 'Quên mật khẩu' hiện ở form đăng nhập admin; bấm vào mở form xin mã, quay lại được (chỉ kiểm UI, không gửi mail thật)")
    public void adminLoginShowsForgotPasswordLinkAndOpensForm() {
        AuthModalPageObject authModal = openAdminLoginOrSkip();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Phai thay form dang nhap admin truoc");
        Assert.assertTrue(authModal.isForgotPasswordLinkDisplayed(),
                "Form dang nhap admin phai co link 'Quên mật khẩu?' (forgot-password-link)");
        Assert.assertEquals(authModal.getForgotPasswordLinkText(), "Quên mật khẩu?",
                "Link quen mat khau phai ghi dung 'Quên mật khẩu?'");

        authModal.clickForgotPasswordLink();
        Assert.assertTrue(authModal.isForgotPasswordFormDisplayed(),
                "Bam link phai mo form quen mat khau (forgot-password-form + o email + nut gui ma)");

        authModal.clickForgotPasswordBackButton();
        Assert.assertTrue(authModal.isLoginFormDisplayed(),
                "Bam 'Quay lai dang nhap' phai ve lai form dang nhap");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM-W13-04 (đợt 18, viết lại — bỏ phần Export CSV: Export đã bỏ hẳn khỏi mọi toolbar admin; nút 'Lọc' cũng đã bỏ, khoảng ngày áp dụng khi bấm nút Tìm của toolbar Nhật ký): Lọc nhật ký theo khoảng ngày (Từ/Đến) + khẳng định KHÔNG có nút Export ở toolbar Nhật ký")
    public void auditLogDateFilterWorks() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("logs");

        Assert.assertTrue(adminPage.isAuditLogSectionDisplayed(),
                "Tab Nhat ky he thong phai co muc 'Nhật ký hệ thống'");

        adminPage.setAuditDateFilter("2026-01-01", "2026-12-31");
        adminPage.clickAuditFilter();
        Assert.assertTrue(adminPage.isAuditLogSectionDisplayed(),
                "Loc ngay xong muc nhat ky he thong van phai hien (khong loi trang)");

        Assert.assertFalse(adminPage.isAuditExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Nhat ky khong duoc co nut Export");
    }

    // ---------- Dot 19 (mapping SCR_11): toolbar / bang / phan trang / tim / loc ngay / badge ----------

    /** Mo muc Nhat ky he thong; tra ve AdminPageObject da san sang. */
    private AdminPageObject openLogsOrSkip() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("logs");
        Assert.assertTrue(adminPage.isAuditLogSectionDisplayed(),
                "Tab Nhat ky he thong phai co muc 'Nhật ký hệ thống'");
        return adminPage;
    }

    /** Tat ca dong nhat ky qua cac trang phan trang (toi da 2 trang, 20 dong). */
    private List<String> getAllAuditRowTexts(AdminPageObject adminPage) {
        List<String> all = new ArrayList<>(adminPage.getAdminTableRowTexts());
        while (adminPage.isPaginationNextEnabled("returns-audit")) {
            adminPage.clickPaginationNext("returns-audit");
            all.addAll(adminPage.getAdminTableRowTexts());
        }
        return all;
    }

    /** Cac ngay (d/M/yyyy) rieng biet trong cac dong nhat ky, sap xep tang dan.
     *  Luu y: DateCell dung toLocaleDateString('vi-VN') nen ngay/thang 1 chu so
     *  khong co so 0 dung dau (vd '5/11/2026') — regex + formatter phai chap
     *  nhan ca 1-2 chu so. */
    private List<LocalDate> collectAuditDates(AdminPageObject adminPage) {
        TreeSet<LocalDate> dates = new TreeSet<>();
        Pattern datePattern = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})");
        DateTimeFormatter vnDate = DateTimeFormatter.ofPattern("d/M/yyyy");
        for (String row : getAllAuditRowTexts(adminPage)) {
            Matcher matcher = datePattern.matcher(row);
            if (matcher.find()) {
                dates.add(LocalDate.parse(matcher.group(1), vnDate));
            }
        }
        return new ArrayList<>(dates);
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-34 (TC_SCRA11_UI_001): Toolbar mục Nhật ký hệ thống có ô tìm logs-search-input + icon Tìm (Lucide Search) + icon Đặt lại (RotateCcw); KHÔNG có nút Làm mới/Thêm mới/Export")
    public void auditLogToolbarHasSearchResetAndNoExport() {
        AdminPageObject adminPage = openLogsOrSkip();

        Assert.assertTrue(adminPage.isAuditSearchInputDisplayed(),
                "Toolbar Nhat ky phai co o tim (logs-search-input)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='logs-search-btn']"),
                "Toolbar Nhat ky phai co nut Tim (logs-search-btn)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='logs-reset-btn']"),
                "Toolbar Nhat ky phai co nut Dat lai (logs-reset-btn)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='logs-search-btn'] svg.lucide-search"),
                "Nut Tim phai chua icon Lucide Search (svg.lucide-search)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='logs-reset-btn'] svg.lucide-rotate-ccw"),
                "Nut Dat lai phai chua icon Lucide RotateCcw (svg.lucide-rotate-ccw)");

        String toolbarText = adminPage.getToolbarText();
        Assert.assertFalse(toolbarText.contains("Làm mới"),
                "Toolbar Nhat ky khong duoc co nut 'Làm mới', dang thay: '" + toolbarText + "'");
        Assert.assertFalse(toolbarText.contains("Thêm mới"),
                "Toolbar Nhat ky khong duoc co nut 'Thêm mới', dang thay: '" + toolbarText + "'");
        Assert.assertFalse(adminPage.isAuditExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Nhat ky khong duoc co nut Export");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-35 (TC_SCRA11_UI_002): Bảng nhật ký có đủ 5 cột STT/Thời gian/Người thực hiện/Hành động/Đối tượng; toolbar có ô tìm placeholder 'Tìm người thực hiện, hành động...' + 2 ô ngày admin-log-date-from/to + icon Tìm/Đặt lại, không nút 'Tìm'/'Lọc' dạng text, không Export/Làm mới; mỗi dòng có Badge hành động + nút Copy sau ID cắt 8 ký tự (self-skip phần dòng nếu bảng trống)")
    public void auditLogTableLayoutShowsFiveColumnsAndCopyButton() {
        AdminPageObject adminPage = openLogsOrSkip();

        List<String> headers = adminPage.getAuditTableHeaders();
        Assert.assertEquals(headers, List.of("STT", "Thời gian", "Người thực hiện", "Hành động", "Đối tượng"),
                "Header bang nhat ky phai du 5 cot STT/Thời gian/Người thực hiện/Hành động/Đối tượng, dang thay: " + headers);
        Assert.assertEquals(adminPage.getAuditSearchPlaceholder(), "Tìm người thực hiện, hành động...",
                "O tim nhat ky phai co placeholder 'Tìm người thực hiện, hành động...'");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-log-date-from']"),
                "Toolbar Nhat ky phai co o Tu ngay (admin-log-date-from)");
        Assert.assertTrue(adminPage.isElementDisplayed("css=[data-testid='admin-log-date-to']"),
                "Toolbar Nhat ky phai co o Den ngay (admin-log-date-to)");
        Assert.assertEquals(adminPage.getListWebElements(
                        "xpath=//div[contains(@class,'sa-page-toolbar')]//button[normalize-space(text())='Tìm' or normalize-space(text())='Lọc']").size(), 0,
                "Toolbar Nhat ky khong duoc co nut 'Tìm'/'Lọc' dang text (chi co nut icon)");
        Assert.assertFalse(adminPage.isAuditExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Nhat ky khong duoc co nut Export");

        int rowCount = adminPage.getAdminTableRowCount();
        if (rowCount == 0) {
            throw new SkipException("Bo qua phan dong bang: bang nhat ky dang rong");
        }
        for (int i = 1; i <= rowCount; i++) {
            String badgeClass = adminPage.getAuditRowActionBadgeClass(i);
            Assert.assertTrue(badgeClass.contains("sa-badge"),
                    "Dong " + i + ": cot Hanh dong phai co Badge (sa-badge), dang thay: '" + badgeClass + "'");
        }
        // Chi dong nao co ID doi tuong moi co '(xxxxxxxx…)' + nut Copy (log co
        // entityId null thi khong co ca 2) — dem theo so dong co ID.
        int rowsWithId = 0;
        for (String rowText : adminPage.getAdminTableRowTexts()) {
            if (rowText.contains("(")) {
                rowsWithId++;
                Assert.assertTrue(rowText.matches("(?s).*\\([A-Za-z0-9\\-]{8}…\\).*"),
                        "Dong co ID doi tuong phai hien ID cat 8 ky tu dang '(xxxxxxxx…)', dang thay: '" + rowText + "'");
            }
        }
        int copyCount = adminPage.getListWebElements(
                "xpath=//div[contains(@class,'sa-table-wrap')]//tbody/tr//button[starts-with(@aria-label,'Sao chép ID')]").size();
        Assert.assertEquals(copyCount, rowsWithId,
                "Moi dong co ID doi tuong phai co 1 nut Copy sau ID");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-36 (TC_SCRA11_FUNC_003 + TC_SCRA11_UI_004): Phân trang bảng nhật ký 10 dòng/trang (testid thật admin-returns-audit-pagination, giữ chữ 'returns'): hiện 'Hiển thị 1–10 / N kết quả' + 'Trang 1 / 2'; bấm 'Sau' sang trang 2 hiện dòng còn lại; STT trang 2 bắt đầu từ 11 (self-skip nếu ≤ 10 dòng log)")
    public void auditLogPaginationShowsTenPerPageAndContinuousStt() {
        AdminPageObject adminPage = openLogsOrSkip();
        int total = adminPage.getPaginationTotalRows("returns-audit");
        if (total <= 10) {
            throw new SkipException("Bo qua: DB nay chi co " + total + " dong nhat ky (can > 10 de kiem tra phan trang)");
        }
        Assert.assertTrue(adminPage.isPaginationDisplayed("returns-audit"),
                "Bang nhat ky phai co phan trang admin-returns-audit-pagination");
        String page1Text = adminPage.getPaginationText("returns-audit");
        Assert.assertTrue(page1Text.contains("Hiển thị 1–10 / " + total + " kết quả"),
                "Trang 1 phai hien 'Hiển thị 1–10 / " + total + " kết quả', dang thay: '" + page1Text + "'");
        Assert.assertTrue(page1Text.contains("Trang 1 / 2"),
                "Trang 1 phai hien 'Trang 1 / 2', dang thay: '" + page1Text + "'");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");

        adminPage.clickPaginationNext("returns-audit");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("returns-audit"), 2,
                "Bam 'Sau' xong phai sang trang 2");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), total - 10,
                "Trang 2 phai hien " + (total - 10) + " dong con lai");
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai bat dau tu 11 (lien tuc theo trang)");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-37 (TC_SCRA11_FUNC_005): Tìm kiếm nhật ký theo người thực hiện/hành động/đối tượng — gõ từ khóa (hành động của dòng đầu) thì bảng lọc ngay client-side, không cần bấm Tìm; bấm icon Tìm thì về trang 1 và áp dụng khoảng ngày (nếu có); xóa từ khóa thì hiện lại toàn bộ (self-skip nếu bảng trống)")
    public void auditLogKeywordSearchFiltersClientSide() {
        AdminPageObject adminPage = openLogsOrSkip();
        int totalBefore = adminPage.getPaginationTotalRows("returns-audit");
        if (totalBefore <= 0) {
            throw new SkipException("Bo qua: bang nhat ky dang rong");
        }
        String firstAction = adminPage.getAuditRowActionText(1);
        Assert.assertFalse(firstAction == null || firstAction.trim().isEmpty(),
                "Phai doc duoc hanh dong o dong dau tien de lam tu khoa tim");

        // Go tu khoa — loc ngay client-side, khong can bam Tim.
        adminPage.setAuditSearchKeyword(firstAction);
        adminPage.sleepInSecond(1);
        int filteredTotal = adminPage.getPaginationTotalRows("returns-audit");
        Assert.assertTrue(filteredTotal >= 1,
                "Tim theo hanh dong '" + firstAction + "' phai ra it nhat 1 dong");
        for (String row : adminPage.getAdminTableRowTexts()) {
            Assert.assertTrue(row.toLowerCase().contains(firstAction.toLowerCase()),
                    "Moi dong sau khi loc phai chua tu khoa '" + firstAction + "', dang thay: '" + row + "'");
        }

        // Bam icon Tim sau khi sang trang 2 (neu co) thi ve trang 1.
        if (filteredTotal > 10) {
            adminPage.clickPaginationNext("returns-audit");
            Assert.assertEquals(adminPage.getPaginationCurrentPage("returns-audit"), 2,
                    "Phai sang duoc trang 2 truoc khi bam Tim");
        }
        adminPage.clickAuditFilter();
        Assert.assertEquals(adminPage.getPaginationCurrentPage("returns-audit"), 1,
                "Bam icon Tim xong phai ve trang 1");

        // Xoa tu khoa thi hien lai toan bo.
        adminPage.setAuditSearchKeyword("");
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getPaginationTotalRows("returns-audit"), totalBefore,
                "Xoa tu khoa xong bang phai hien lai du " + totalBefore + " dong");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-38 (TC_SCRA11_FUNC_006/007/008/009/010): Lọc nhật ký theo ngày — nhập ngày nhưng chưa bấm Tìm thì chưa lọc (ô ngày chỉ là bản nháp); bấm Tìm thì chỉ còn dòng trong khoảng; lọc chỉ Từ ngày / chỉ Đến ngày đúng; bấm Đặt lại thì xóa cả khoảng ngày + tìm kiếm, hiện lại đủ (self-skip nếu DB không có log ở cả 2 phía mốc ngày)")
    public void auditLogDateFilterFromOnlyToOnlyRangeAndReset() {
        AdminPageObject adminPage = openLogsOrSkip();
        List<LocalDate> distinctDates = collectAuditDates(adminPage);
        if (distinctDates.size() < 2) {
            throw new SkipException("Bo qua: can log o it nhat 2 ngay khac nhau de kiem tra loc ngay, dang co: " + distinctDates);
        }
        int totalBefore = adminPage.getPaginationTotalRows("returns-audit");
        LocalDate earliest = distinctDates.get(0);
        LocalDate second = distinctDates.get(1);
        LocalDate latest = distinctDates.get(distinctDates.size() - 1);
        DateTimeFormatter isoDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // (1) Nhap khoang nhung CHUA bam Tim — chua loc (o ngay chi la ban nhap).
        adminPage.setAuditDateFilter(second.format(isoDate), latest.format(isoDate));
        Assert.assertEquals(adminPage.getPaginationTotalRows("returns-audit"), totalBefore,
                "Chua bam Tim thi tat ca dong van phai hien du");

        // (2) Bam Tim — chi con dong trong khoang [second, latest].
        adminPage.clickAuditFilter();
        List<LocalDate> inRange = collectAuditDates(adminPage);
        Assert.assertFalse(inRange.isEmpty(), "Loc theo khoang phai con dong");
        for (LocalDate date : inRange) {
            Assert.assertTrue(!date.isBefore(second) && !date.isAfter(latest),
                    "Sau khi loc, moi dong phai co ngay trong [" + second + ", " + latest + "], gap ngay: " + date);
        }
        Assert.assertTrue(adminPage.getPaginationTotalRows("returns-audit") < totalBefore,
                "Loc khoang [" + second + ", " + latest + "] phai loai bo dong ngay " + earliest);

        // (3) Chi Tu ngay.
        adminPage.clickAuditReset();
        adminPage.setAuditDateFilter(second.format(isoDate), "");
        adminPage.clickAuditFilter();
        for (LocalDate date : collectAuditDates(adminPage)) {
            Assert.assertTrue(!date.isBefore(second),
                    "Loc chi Tu ngay " + second + " thi moi dong phai co ngay >= " + second + ", gap ngay: " + date);
        }

        // (4) Chi Den ngay.
        adminPage.clickAuditReset();
        adminPage.setAuditDateFilter("", earliest.format(isoDate));
        adminPage.clickAuditFilter();
        for (LocalDate date : collectAuditDates(adminPage)) {
            Assert.assertTrue(!date.isAfter(earliest),
                    "Loc chi Den ngay " + earliest + " thi moi dong phai co ngay <= " + earliest + ", gap ngay: " + date);
        }

        // (5) Dat lai — xoa ca khoang ngay + tim kiem, hien lai du.
        adminPage.clickAuditReset();
        Assert.assertEquals(adminPage.getPaginationTotalRows("returns-audit"), totalBefore,
                "Bam Dat lai xong bang phai hien lai du " + totalBefore + " dong");
        Assert.assertEquals(adminPage.getAuditSearchValue(), "",
                "Bam Dat lai xong o tim phai trong");
        Assert.assertEquals(adminPage.getAuditDateFromValue(), "",
                "Bam Dat lai xong o Tu ngay phai trong");
        Assert.assertEquals(adminPage.getAuditDateToValue(), "",
                "Bam Dat lai xong o Den ngay phai trong");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-39 (TC_SCRA11_FUNC_012, MUTATES_REAL_DATA): Thao tác admin vừa làm có dòng log mới — điều chỉnh điểm khách demo +5 rồi mở lại mục Nhật ký: có dòng log mới đúng email admin@dafl.vn, thời gian hôm nay; hoàn tác -5 trong finally để khôi phục điểm (log audit tạo ra thì ở lại DB, đó chính là đối tượng cần assert)")
    public void auditLogRecordsRecentAdminAction() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        boolean pointsAdded = false;
        try {
            try {
                adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "5",
                        "Kiem thu ADM19-39 ghi log dieu chinh diem");
                pointsAdded = true;
            } catch (Throwable e) {
                throw new SkipException("Bo qua: khong dieu chinh duoc diem cho khach "
                        + GlobalConstants.DEMO_EMAIL + " (" + e.getMessage() + ")");
            }

            adminPage.clickAdminNav("logs");
            Assert.assertTrue(adminPage.isAuditLogSectionDisplayed(),
                    "Tab Nhat ky he thong phai co muc 'Nhật ký hệ thống'");
            adminPage.setAuditSearchKeyword(GlobalConstants.ADMIN_EMAIL);
            adminPage.sleepInSecond(1);
            Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                    "Tim theo email admin phai ra dong log cua admin");

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("d/M/yyyy"));
            boolean foundTodayLog = false;
            for (String row : getAllAuditRowTexts(adminPage)) {
                if (row.contains(GlobalConstants.ADMIN_EMAIL) && row.contains(today)) {
                    foundTodayLog = true;
                    break;
                }
            }
            Assert.assertTrue(foundTodayLog,
                    "Phai co dong log moi cua " + GlobalConstants.ADMIN_EMAIL + " trong ngay hom nay (" + today + ")");
        } finally {
            if (pointsAdded) {
                try {
                    adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "-5",
                            "Hoan tac kiem thu ADM19-39");
                } catch (Throwable ignored) {
                    // Hoan tac best-effort: neu that bai, diem demo lech +5 — bao cao phai ghi ro.
                }
            }
        }
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-40 (TC_SCRA11_UI_013): Màu Badge cột Hành động — hành động chứa DELETE có Badge class sa-badge--darkred; chứa UNLOCK có Badge xanh lá (sa-badge--green); chứa LOCK (không UNLOCK) có Badge đỏ (sa-badge--red); hành động khác có Badge xám (sa-badge--gray) (self-skip từng nhóm nếu không có log thuộc nhóm đó; skip cả test nếu bảng trống)")
    public void auditLogActionBadgeTonesByActionType() {
        AdminPageObject adminPage = openLogsOrSkip();
        if (adminPage.getPaginationTotalRows("returns-audit") <= 0) {
            throw new SkipException("Bo qua: bang nhat ky dang rong");
        }
        int checked = 0;
        for (int page = 1; page <= 2; page++) {
            int rowCount = adminPage.getAdminTableRowCount();
            for (int i = 1; i <= rowCount; i++) {
                String action = adminPage.getAuditRowActionText(i);
                String badgeClass = adminPage.getAuditRowActionBadgeClass(i);
                if (action.toUpperCase().contains("DELETE")) {
                    Assert.assertTrue(badgeClass.contains("sa-badge--darkred"),
                            "Hanh dong '" + action + "' phai co Badge do dam (sa-badge--darkred), dang thay: '" + badgeClass + "'");
                } else if (action.toUpperCase().contains("UNLOCK")) {
                    Assert.assertTrue(badgeClass.contains("sa-badge--green"),
                            "Hanh dong '" + action + "' phai co Badge xanh la (sa-badge--green), dang thay: '" + badgeClass + "'");
                } else if (action.toUpperCase().contains("LOCK")) {
                    Assert.assertTrue(badgeClass.contains("sa-badge--red"),
                            "Hanh dong '" + action + "' phai co Badge do (sa-badge--red), dang thay: '" + badgeClass + "'");
                } else {
                    Assert.assertTrue(badgeClass.contains("sa-badge--gray"),
                            "Hanh dong '" + action + "' phai co Badge xam (sa-badge--gray), dang thay: '" + badgeClass + "'");
                }
                checked++;
            }
            if (!adminPage.isPaginationNextEnabled("returns-audit")) {
                break;
            }
            adminPage.clickPaginationNext("returns-audit");
        }
        Assert.assertTrue(checked > 0, "Phai kiem tra duoc it nhat 1 dong log");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM19-41 (TC_SCRA11_FUNC_015): Chỉ tải tối đa 20 dòng nhật ký (pageSize=20), không có nút 'Tải thêm' — tổng số dòng xem được qua cả 2 trang phân trang tối đa 20 (self-skip nếu bảng trống; không assert thứ tự sắp xếp log vì chưa xác minh)")
    public void auditLogLoadsAtMostTwentyRowsAndHasNoLoadMore() {
        AdminPageObject adminPage = openLogsOrSkip();
        int total = adminPage.getPaginationTotalRows("returns-audit");
        if (total <= 0) {
            throw new SkipException("Bo qua: bang nhat ky dang rong");
        }
        Assert.assertTrue(total <= 20,
                "Tong so dong nhat ky xem duoc toi da 20 (pageSize=20), dang thay: " + total);
        if (total > 10) {
            Assert.assertEquals(adminPage.getPaginationTotalPages("returns-audit"), 2,
                    "Toi da 20 dong thi phan trang toi da 2 trang");
        }
        Assert.assertFalse(adminPage.isElementDisplayed("xpath=//button[contains(.,'Tải thêm')]"),
                "Muc Nhat ky khong duoc co nut 'Tải thêm'");
    }
}
