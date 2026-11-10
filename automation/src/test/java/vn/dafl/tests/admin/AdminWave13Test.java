package vn.dafl.tests.admin;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageObjects.AuthModalPageObject;

/**
 * Test tinh nang moi dot 13 o app Admin rieng (chi chay tren app Admin, khong
 * mo web ban hang):
 * - ADM-W13-01: trang dang nhap admin khong co nut quay lai, khung can giua.
 * - ADM-W13-02: link 'Quên mật khẩu' hien o form dang nhap admin (chi kiem UI,
 *   khong gui mail that).
 * - ADM-W13-03: nut 'Ghi nhận đã hoàn tiền' chi hien khi co yeu cau Approved +
 *   PendingRefund (tu skip neu DB khong co; chi mo modal xem, khong ghi nhan that).
 * - ADM-W13-04: loc ngay + Export o nhat ky he thong (tab Tra hang).
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

    @Story("Trả hàng")
    @Test(description = "ADM-W13-03 (đợt 13, mục 7): Nút 'Ghi nhận đã hoàn tiền' chỉ hiện ở dòng trả hàng Approved + PendingRefund; bấm vào mở modal nhập mã giao dịch (skip nếu không có, không ghi nhận thật)")
    public void refundRecordButtonOnlyOnApprovedPendingRefundAndOpensModal() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");

        if (!adminPage.isRefundRecordButtonDisplayed()) {
            throw new SkipException("Bo qua: DB nay khong co yeu cau tra hang nao o trang thai Approved + PendingRefund (co refundId) de hien nut 'Ghi nhận đã hoàn tiền'");
        }
        String row = adminPage.getRefundRecordButtonRowText();
        Assert.assertTrue(row.contains("Approved"),
                "Dong co nut ghi nhan hoan tien phai o trang thai Approved, dang thay: " + row);
        Assert.assertTrue(row.contains("PendingRefund"),
                "Dong co nut ghi nhan hoan tien phai o trang thai hoan tien PendingRefund, dang thay: " + row);

        adminPage.clickFirstRefundRecordButton();
        Assert.assertTrue(adminPage.isRefundRecordModalDisplayed(),
                "Bam nut phai mo modal ghi nhan hoan tien (admin-refund-record-modal + o ma giao dich + nut xac nhan)");

        // Chi mo xem roi dong — KHONG nhap ma giao dich, KHONG bam xac nhan.
        adminPage.closeRefundRecordModal();
        Assert.assertFalse(adminPage.isRefundRecordModalDisplayed(),
                "Dong modal xong khong duoc con thay modal ghi nhan hoan tien");
    }

    @Story("Nhật ký hệ thống")
    @Test(description = "ADM-W13-04 (đợt 13, mục 10): Nhật ký hệ thống có ô lọc Từ/đến ngày + nút Lọc + nút Export; lọc khoảng rộng + bấm Export không lỗi trang")
    public void auditLogDateFilterAndExportControlsWork() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");

        Assert.assertTrue(adminPage.isAuditLogSectionDisplayed(),
                "Tab Tra hang phai co muc 'Nhật ký hệ thống (audit-log)'");
        Assert.assertTrue(adminPage.isAuditExportButtonDisplayed(),
                "Nhat ky he thong phai co nut Export (admin-log-export-btn)");

        adminPage.setAuditDateFilter("2026-01-01", "2026-12-31");
        adminPage.clickAuditFilter();
        Assert.assertTrue(adminPage.isAuditLogSectionDisplayed(),
                "Loc ngay xong muc nhat ky he thong van phai hien (khong loi trang)");

        adminPage.clickAuditExport();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(),
                "Bam Export nhat ky xong trang admin van dung yen (menu quan tri van hien)");
    }
}
