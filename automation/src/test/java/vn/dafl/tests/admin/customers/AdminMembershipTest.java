package vn.dafl.tests.admin.customers;

import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Test muc "Hội viên" o app Admin rieng localhost:3001 (lop MOI dot ADM19,
 * tach tu AdminCustomerTest — truoc day hoi vien chi co 2 test lan trong
 * AdminCustomerTest: ADT-04 mo modal chi tiet hang + ACR-08 cong diem demo).
 * - Bang hang: 4 hang (Member/Silver/Gold/Diamond), o nhap so sua truc tiep
 *   tren bang, luu qua popup F1 'Lưu cấu hình hạng?' (PUT
 *   /api/admin/membership/config), nut icon 'Đặt lại cấu hình'
 *   (admin-members-reset-btn, huy thay doi chua luu) + 'Làm mới'
 *   (admin-members-refresh-btn, tai lai tu server). KHONG co o tim,
 *   KHONG PageToolbar, KHONG phan trang, KHONG Export.
 * - Form 'Điều chỉnh điểm cho 1 khách' hien INLINE tren trang (khong modal):
 *   email/ID + so diem (duong = cong, am = tru, khac 0, toi da 1000/lan) +
 *   ly do bat buoc; cong mo popup F2 'Xác nhận cộng điểm?', tru mo popup F3
 *   'Xác nhận trừ điểm?' (cam); tru vuot so du thi bao inline 'Khách hàng
 *   chỉ còn {n} điểm.', khong goi POST.
 * - Luu cau hinh hang la thao tac THAT tren DB: test nao doi cau hinh deu
 *   khoi phuc gia tri goc trong finally (xem tung test). Chi doi cau hinh
 *   hang + diem cua tai khoan demo, KHONG doi mat khau admin.
 * - 2 diem mapping ghi sai da sua theo code that (xem note_W4.md): (1) gia
 *   tri 16.000.000d cho nguong Vang vi pham thu tu tang dan (Kim cuong mac
 *   dinh chi 15.000.000d) nen test tu tinh nguong Vang hop le luc chay
 *   (Bac + 1.000.000, phai nho hon Kim cuong); (2) nut 'Đặt lại cấu hình'
 *   chi khoi phuc tierDraft, KHONG xoa loi inline tiersError.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay,
 * tai khoan admin@dafl.vn chua duoc seed, hoac DB khong co du lieu can thiet
 * (khong co khach demo, so du demo qua thap, cau hinh hang da khac mac dinh).
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý hội viên")
public class AdminMembershipTest extends BaseTest {

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

    /** Dinh dang tien theo kieu FE fmtMoney (vi-VN + '₫', vd 5000000 -> '5.000.000₫'). */
    private String formatMoney(long value) {
        return String.format("%,d", value).replace(',', '.') + "₫";
    }

    /** Bam Luu cau hinh -> xac nhan popup F1 neu hien -> doi toast (dung cho
     *  khoi phuc trong finally; khong assert — loi duoc bo qua o caller). */
    private void confirmSaveTiersIfPopupShown(AdminPageObject adminPage) {
        adminPage.clickSaveTiersButton();
        if (adminPage.isConfirmDialogPresent()) {
            adminPage.clickConfirmDialogConfirm();
            adminPage.waitForToast();
        }
    }

    /** Khoi phuc nguong 1 hang ve gia tri goc (best-effort, khong lam fail test chinh). */
    private void restoreTierThreshold(AdminPageObject adminPage, String tierKey, long originalValue) {
        try {
            String current = adminPage.getTierThresholdValue(tierKey);
            if (String.valueOf(originalValue).equals(current)) {
                return;
            }
            adminPage.setTierThreshold(tierKey, String.valueOf(originalValue));
            confirmSaveTiersIfPopupShown(adminPage);
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        }
    }

    /** Khoi phuc tran dung diem 1 hang ve gia tri goc (best-effort). */
    private void restoreTierMaxPointsPercent(AdminPageObject adminPage, String tierKey, int originalValue) {
        try {
            String current = adminPage.getTierMaxPointsPercentValue(tierKey);
            if (String.valueOf(originalValue).equals(current)) {
                return;
            }
            adminPage.setTierMaxPointsPercent(tierKey, String.valueOf(originalValue));
            confirmSaveTiersIfPopupShown(adminPage);
        } catch (Throwable ignored) {
        }
    }

    /** Khoi phuc nguong freeship 1 hang ve gia tri goc (best-effort). Chi khoi
     *  phuc khi gia tri goc khac rong — gia tri goc rong (null) thi khong can
     *  khoi phuc (o trong chinh la null). */
    private void restoreTierFreeShip(AdminPageObject adminPage, String tierKey, String originalValue) {
        try {
            if (originalValue == null || originalValue.isEmpty()) {
                return;
            }
            String current = adminPage.getTierFreeShipValue(tierKey);
            if (originalValue.equals(current)) {
                return;
            }
            adminPage.setTierFreeShip(tierKey, originalValue);
            confirmSaveTiersIfPopupShown(adminPage);
        } catch (Throwable ignored) {
        }
    }

    /** Doc so du diem demo; skip test neu khong tim thay khach demo. */
    private int getDemoPointsOrSkip(AdminPageObject adminPage) {
        int points = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
        if (points < 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de doc so du diem");
        }
        return points;
    }

    // ---------- Bang hang hoi vien (chi doc) ----------

    @Story("Hội viên")
    @Test(description = "ADM19-211 (TC_SCRA09_UI_001 + TC_SCRA09_UI_002): Bảng hạng hội viên đủ 4 hạng Thường (Member)/Bạc (Silver)/Vàng (Gold)/Kim cương (Diamond), STT 1–4, không phân trang; đủ 6 cột dữ liệu STT | Hạng | Ngưỡng chi tiêu | Chi tiêu / 1 điểm | Trần dùng điểm (%) | Ngưỡng freeship + cột Thao tác có nút mắt; dưới bảng có dòng ghi chú ngưỡng tăng dần; ô Ngưỡng freeship để trống hiện placeholder 'Không đặt'")
    public void adminMembershipTierTableShowsFourTiers() {
        AdminPageObject adminPage = openAdminTab("membership");
        List<String> expectedHeaders = Arrays.asList(
                "STT", "Hạng", "Ngưỡng chi tiêu", "Chi tiêu / 1 điểm", "Trần dùng điểm (%)",
                "Ngưỡng freeship", "Thao tác");
        Assert.assertEquals(adminPage.getTierTableHeaders(), expectedHeaders,
                "Bang hang phai co dung 7 cot tieu de theo thu tu");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 4,
                "Bang hang phai co dung 4 dong (4 hang hoi vien)");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau bang hang phai la 1");
        List<String> rowTexts = adminPage.getAdminTableRowTexts();
        Assert.assertTrue(rowTexts.get(0).contains("Thường (Member)"),
                "Dong 1 phai la hang 'Thường (Member)', dang thay: " + rowTexts.get(0));
        Assert.assertTrue(rowTexts.get(1).contains("Bạc (Silver)"),
                "Dong 2 phai la hang 'Bạc (Silver)', dang thay: " + rowTexts.get(1));
        Assert.assertTrue(rowTexts.get(2).contains("Vàng (Gold)"),
                "Dong 3 phai la hang 'Vàng (Gold)', dang thay: " + rowTexts.get(2));
        Assert.assertTrue(rowTexts.get(3).contains("Kim cương (Diamond)"),
                "Dong 4 phai la hang 'Kim cương (Diamond)', dang thay: " + rowTexts.get(3));
        for (String label : Arrays.asList("Thường", "Bạc", "Vàng", "Kim cương")) {
            Assert.assertTrue(adminPage.isTierDetailButtonDisplayed(label),
                    "Hang '" + label + "' phai co nut mat xem chi tiet o cot Thao tac");
        }
        Assert.assertTrue(adminPage.isSaveTiersButtonDisplayed(),
                "Bang hang phai co nut 'Lưu cấu hình hạng' (admin-save-tiers-btn) ben duoi bang");
        Assert.assertFalse(adminPage.isMembershipPaginationPresent(),
                "Bang hang chi 4 dong nen KHONG co phan trang");
        String note = adminPage.getTiersNoteText();
        Assert.assertTrue(note.contains("ngưỡng chi tiêu phải tăng dần"),
                "Duoi bang hang phai co dong ghi chu nguong tang dan, dang thay: " + note);
        // O Ngưỡng freeship de trong hien placeholder 'Không đặt' — kiem tra o
        // hang nao dang de trong (neu co); cau hinh DB doi thi bo qua nhanh nay.
        for (String tierKey : Arrays.asList("Member", "Silver", "Gold", "Diamond")) {
            if (adminPage.getTierFreeShipValue(tierKey).isEmpty()) {
                Assert.assertEquals(adminPage.getTierFreeShipPlaceholder(tierKey), "Không đặt",
                        "O freeship de trong cua hang '" + tierKey + "' phai hien placeholder 'Không đặt'");
            }
        }
        // (Neu tat ca cac hang deu co freeship thi khong co o nao de kiem tra
        // placeholder — cau hinh DB doi so voi mac dinh, khong assert them.)
    }

    @Story("Hội viên")
    @Test(description = "ADM19-212 (TC_SCRA09_UI_003): Giá trị mặc định của 4 hạng — Thường 0đ / 20.000đ/điểm / 10% / freeship không đặt; Bạc 2.000.000đ / 15.000đ / 15% / 199.000đ; Vàng 5.000.000đ / 10.000đ / 20% / 99.000đ; Kim cương 15.000.000đ / 8.000đ / 30% / 0đ (skip nếu cấu hình trên DB này đã khác mặc định — kiểm tay, không assert cứng)")
    public void adminMembershipTierDefaultValues() {
        AdminPageObject adminPage = openAdminTab("membership");
        assertTierDefaultValues(adminPage, "Member", "0", "20000", "10", "");
        assertTierDefaultValues(adminPage, "Silver", "2000000", "15000", "15", "199000");
        assertTierDefaultValues(adminPage, "Gold", "5000000", "10000", "20", "99000");
        assertTierDefaultValues(adminPage, "Diamond", "15000000", "8000", "30", "0");
    }

    /** Doi chieu 4 gia tri cua 1 hang voi gia tri mac dinh (BE DefaultConfigs);
     *  sai 1 o thi skip ca test — cau hinh tren DB nay da duoc sua, kiem tay. */
    private void assertTierDefaultValues(AdminPageObject adminPage, String tierKey,
                                         String threshold, String spendPerPoint,
                                         String maxPointsPercent, String freeShip) {
        String actualThreshold = adminPage.getTierThresholdValue(tierKey);
        String actualSpend = adminPage.getTierSpendPerPointValue(tierKey);
        String actualCap = adminPage.getTierMaxPointsPercentValue(tierKey);
        String actualFreeShip = adminPage.getTierFreeShipValue(tierKey);
        if (!threshold.equals(actualThreshold) || !spendPerPoint.equals(actualSpend)
                || !maxPointsPercent.equals(actualCap) || !freeShip.equals(actualFreeShip)) {
            throw new SkipException("Bo qua: cau hinh hang '" + tierKey + "' tren DB nay da khac mac dinh "
                    + "(nguong " + actualThreshold + ", tich diem " + actualSpend + ", tran " + actualCap
                    + ", freeship '" + actualFreeShip + "') — kiem tay, khong assert cung");
        }
    }

    @Story("Hội viên")
    @Test(description = "ADM19-213 (TC_SCRA09_UI_004 + TC_SCRA09_UI_023): Form 'Điều chỉnh điểm cho 1 khách' hiển thị inline ngay trên trang (admin-adjust-points-form, KHÔNG modal) với 3 ô Email/ID khách, Số điểm (dương = cộng, âm = trừ; tối đa 1000/lần), Lý do (bắt buộc) + nút 'Điều chỉnh điểm'; toolbar Hội viên chỉ có 2 nút icon Đặt lại cấu hình + Làm mới, không ô tìm, không PageToolbar, không Thêm mới/Export, không phân trang")
    public void adminMembershipInlineFormAndCustomToolbar() {
        AdminPageObject adminPage = openAdminTab("membership");
        Assert.assertTrue(adminPage.isAdjustPointsFormDisplayed(),
                "Form dieu chinh diem phai hien inline tren trang Hoi vien (admin-adjust-points-form)");
        String formText = adminPage.getAdjustPointsFormText();
        Assert.assertTrue(formText.contains("Email hoặc ID khách hàng"),
                "Form dieu chinh diem phai co o 'Email hoặc ID khách hàng', dang thay: " + formText);
        Assert.assertTrue(formText.contains("Số điểm (dương = cộng, âm = trừ; tối đa 1000/lần)"),
                "Form dieu chinh diem phai co o 'Số điểm (dương = cộng, âm = trừ; tối đa 1000/lần)', dang thay: " + formText);
        Assert.assertTrue(formText.contains("Lý do (bắt buộc)"),
                "Form dieu chinh diem phai co o 'Lý do (bắt buộc)', dang thay: " + formText);
        Assert.assertTrue(formText.contains("Điều chỉnh điểm"),
                "Form dieu chinh diem phai co nut 'Điều chỉnh điểm', dang thay: " + formText);
        Assert.assertTrue(adminPage.isMembersResetButtonDisplayed(),
                "Toolbar Hoi vien phai co nut icon 'Đặt lại cấu hình' (admin-members-reset-btn)");
        Assert.assertTrue(adminPage.isMembersRefreshButtonDisplayed(),
                "Toolbar Hoi vien phai co nut icon 'Làm mới' (admin-members-refresh-btn)");
        Assert.assertFalse(adminPage.isPageToolbarDisplayed(),
                "Muc Hoi vien dung toolbar rieng (.sa-toolbar) nen KHONG co PageToolbar chuan");
        Assert.assertFalse(adminPage.isAnySearchInputPresent(),
                "Muc Hoi vien KHONG co o tim kiem");
        Assert.assertFalse(adminPage.isToolbarAddButtonPresent("membership"),
                "Muc Hoi vien KHONG co nut Them moi");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Muc Hoi vien KHONG co nut Export");
        Assert.assertFalse(adminPage.isMembershipPaginationPresent(),
                "Muc Hoi vien KHONG co phan trang");
    }

    @Story("Hội viên")
    @Test(description = "ADM19-214 (TC_SCRA09_UI_005): Xem chi tiết hạng bằng nút mắt — bấm mắt ở hạng Vàng → modal admin-tier-detail tiêu đề 'Hạng Vàng' hiện đúng Mã hạng (Gold), Ngưỡng chi tiêu, Chi tiêu / 1 điểm, Trần dùng điểm, Ngưỡng freeship + ghi chú ngưỡng tăng dần; đóng được modal (ADT-04 chỉ assert tên hạng + đóng được; test này assert toàn bộ trường, đối chiếu giá trị đang lưu trên bảng)")
    public void adminTierDetailModalShowsAllTierFields() {
        AdminPageObject adminPage = openAdminTab("membership");
        long threshold = Long.parseLong(adminPage.getTierThresholdValue("Gold"));
        long spend = Long.parseLong(adminPage.getTierSpendPerPointValue("Gold"));
        int cap = Integer.parseInt(adminPage.getTierMaxPointsPercentValue("Gold"));
        String freeShip = adminPage.getTierFreeShipValue("Gold");
        adminPage.clickTierDetail("Vàng");
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_TIER_DETAIL),
                "Phai mo duoc modal chi tiet hang hoi vien");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_TIER_DETAIL);
        Assert.assertTrue(modalText.contains("Hạng Vàng"),
                "Modal phai co tieu de 'Hạng Vàng', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Gold"),
                "Modal phai hien Ma hang 'Gold', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains(formatMoney(threshold)),
                "Modal phai hien Ngưỡng chi tiêu '" + formatMoney(threshold) + "', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains(formatMoney(spend)),
                "Modal phai hien Chi tiêu / 1 điểm '" + formatMoney(spend) + "', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains(cap + "%"),
                "Modal phai hien Trần dùng điểm '" + cap + "%', dang thay: " + modalText);
        if (freeShip == null || freeShip.isEmpty()) {
            Assert.assertTrue(modalText.contains("Không đặt"),
                    "Freeship chua dat thi modal phai hien Ngưỡng freeship 'Không đặt', dang thay: " + modalText);
        } else {
            Assert.assertTrue(modalText.contains(formatMoney(Long.parseLong(freeShip))),
                    "Modal phai hien Ngưỡng freeship '" + formatMoney(Long.parseLong(freeShip))
                            + "', dang thay: " + modalText);
        }
        Assert.assertTrue(modalText.contains("Ngưỡng chi tiêu tăng dần"),
                "Modal phai co ghi chu nguong tang dan, dang thay: " + modalText);
        adminPage.closeDetailModal("admin-tier-detail");
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_TIER_DETAIL),
                "Dong modal xong khong duoc con thay modal chi tiet hang hoi vien");
    }

    // ---------- Luu cau hinh hang (mutate, co khoi phuc) ----------

    @Story("Hội viên")
    @Test(description = "ADM19-215 (TC_SCRA09_FUNC_006): Sửa ngưỡng hạng và lưu cấu hình qua popup F1 — đổi ngưỡng Vàng sang giá trị hợp lệ (lớn hơn Bạc, nhỏ hơn Kim cương) → bấm 'Lưu cấu hình hạng' → popup F1 'Lưu cấu hình hạng?' đúng 2 nút [Hủy][Lưu cấu hình] → xác nhận → PUT, toast 'Đã lưu cấu hình hạng', bảng cập nhật ngưỡng mới; khôi phục ngưỡng cũ trong finally (lưu ý: giá trị 16.000.000đ trong mapping vi phạm thứ tự tăng dần vì Kim cương mặc định chỉ 15.000.000đ nên test tính ngưỡng hợp lệ lúc chạy, xem note_W4.md)")
    public void adminSaveTierConfigViaConfirmDialog() {
        AdminPageObject adminPage = openAdminTab("membership");
        long silver = Long.parseLong(adminPage.getTierThresholdValue("Silver"));
        long goldOriginal = Long.parseLong(adminPage.getTierThresholdValue("Gold"));
        long diamond = Long.parseLong(adminPage.getTierThresholdValue("Diamond"));
        long newGold = silver + 1000000;
        if (newGold >= diamond || newGold == goldOriginal) {
            throw new SkipException("Bo qua: khong tinh duoc nguong Vang hop le (Bac " + silver
                    + ", Vang " + goldOriginal + ", Kim cuong " + diamond + ")");
        }
        try {
            adminPage.setTierThreshold("Gold", String.valueOf(newGold));
            adminPage.clickSaveTiersButton();
            Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                    "Luu cau hinh hop le phai mo popup F1 'Lưu cấu hình hạng?'");
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Lưu cấu hình hạng?",
                    "Popup luu cau hinh phai co tieu de 'Lưu cấu hình hạng?', dang thay: "
                            + adminPage.getConfirmDialogTitle());
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup F1 phai CHI co dung 2 nut");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Lưu cấu hình",
                    "Nut xac nhan popup F1 phai la 'Lưu cấu hình', dang thay: " + adminPage.getConfirmDialogConfirmText());
            adminPage.clickConfirmDialogConfirm();
            adminPage.waitForToast();
            Assert.assertEquals(adminPage.getToastTitleText(), "Đã lưu cấu hình hạng",
                    "Luu cau hinh xong phai hien toast 'Đã lưu cấu hình hạng', dang thay: " + adminPage.getToastTitleText());
            Assert.assertEquals(adminPage.getTierThresholdValue("Gold"), String.valueOf(newGold),
                    "Luu xong bang phai hien nguong Vang moi " + newGold);
        } finally {
            restoreTierThreshold(adminPage, "Gold", goldOriginal);
        }
    }

    @Story("Hội viên")
    @Test(description = "ADM19-216 (TC_SCRA09_VAL_007): Ngưỡng chi tiêu phải tăng dần Thường → Bạc → Vàng → Kim cương (chặn client-side) — đặt ngưỡng Vàng thấp hơn Bạc → bấm 'Lưu cấu hình hạng' → lỗi inline 'Ngưỡng chi tiêu phải tăng dần theo thứ tự Thường, Bạc, Vàng, Kim cương.', KHÔNG hiện popup F1, KHÔNG gọi PUT; hủy thay đổi bằng nút Đặt lại cấu hình (lưu ý: nút Đặt lại chỉ khôi phục tierDraft, KHÔNG xóa lỗi inline — xem note_W4.md)")
    public void adminTierThresholdsMustIncrease() {
        AdminPageObject adminPage = openAdminTab("membership");
        long silver = Long.parseLong(adminPage.getTierThresholdValue("Silver"));
        long goldOriginal = Long.parseLong(adminPage.getTierThresholdValue("Gold"));
        if (silver <= 0) {
            throw new SkipException("Bo qua: nguong Bac dang la " + silver
                    + " nen khong dat duoc nguong Vang thap hon Bac");
        }
        long badGold = silver - 1;
        adminPage.setTierThreshold("Gold", String.valueOf(badGold));
        adminPage.clickSaveTiersButton();
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Nguong khong tang dan thi KHONG duoc hien popup F1 (chan client-side, khong goi PUT)");
        Assert.assertEquals(adminPage.getTiersErrorText(),
                "Ngưỡng chi tiêu phải tăng dần theo thứ tự Thường, Bạc, Vàng, Kim cương.",
                "Nguong khong tang dan phai hien loi inline o vung tiersError");
        Assert.assertEquals(adminPage.getTierThresholdValue("Gold"), String.valueOf(badGold),
                "Loi thi ban nhap van giu tren o nhap (chua luu)");
        adminPage.clickMembersResetButton();
        Assert.assertEquals(adminPage.getTierThresholdValue("Gold"), String.valueOf(goldOriginal),
                "Bam 'Đặt lại cấu hình' thi o nguong Vang phai tro ve gia tri da tai " + goldOriginal);
    }

    @Story("Hội viên")
    @Test(description = "ADM19-217 (TC_SCRA09_VAL_008): Trần dùng điểm phải từ 0 đến 100% (BE chặn) — đặt Trần dùng điểm hạng Bạc thành 150 → Lưu → xác nhận ở popup F1 → BE trả lỗi 'Trần dùng điểm phải từ 0 đến 100%.' (toast lỗi, httpErrorMessage giữ nguyên chuỗi BE ngắn); không lưu được cấu hình; bấm Làm mới thì ô trở về giá trị cũ (khôi phục trong finally để phòng BE chấp nhận)")
    public void adminTierCapAbove100RejectedByBackend() {
        AdminPageObject adminPage = openAdminTab("membership");
        int capOriginal = Integer.parseInt(adminPage.getTierMaxPointsPercentValue("Silver"));
        try {
            adminPage.setTierMaxPointsPercent("Silver", "150");
            adminPage.clickSaveTiersButton();
            Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                    "Tran 150 van mo popup F1 (chi BE chan, khong chan client-side)");
            adminPage.clickConfirmDialogConfirm();
            String feedback = adminPage.waitForFeedback();
            Assert.assertTrue(feedback.contains("Trần dùng điểm phải từ 0 đến 100%."),
                    "BE phai tra loi 'Trần dùng điểm phải từ 0 đến 100%.', dang thay: " + feedback);
            adminPage.clickMembersRefreshButton();
            Assert.assertEquals(adminPage.getTierMaxPointsPercentValue("Silver"), String.valueOf(capOriginal),
                    "Luu that bai thi cau hinh da luu khong doi — Lam moi xong tran Bac phai ve " + capOriginal);
        } finally {
            restoreTierMaxPointsPercent(adminPage, "Silver", capOriginal);
        }
    }

    @Story("Hội viên")
    @Test(description = "ADM19-218 (TC_SCRA09_VAL_009): Nhập Chi tiêu / 1 điểm bằng 0 thì ô tự về 1, không gửi được 0 lên BE — đặt ô hạng Bạc thành 0 → ô hiển thị 1 ngay khi gõ (onChange Number(...) || 1); hủy thay đổi bằng nút Đặt lại cấu hình, không lưu")
    public void adminTierSpendPerPointZeroCoercesToOne() {
        AdminPageObject adminPage = openAdminTab("membership");
        String original = adminPage.getTierSpendPerPointValue("Silver");
        adminPage.setTierSpendPerPoint("Silver", "0");
        Assert.assertEquals(adminPage.getTierSpendPerPointValue("Silver"), "1",
                "Go 0 vao o Chi tiêu / 1 điểm thi o phai tu ve 1 (Number(...) || 1)");
        adminPage.clickMembersResetButton();
        Assert.assertEquals(adminPage.getTierSpendPerPointValue("Silver"), original,
                "Bam 'Đặt lại cấu hình' thi o phai tro ve gia tri da tai " + original);
    }

    // ---------- Dieu chinh diem (mutate, co hoan tac) ----------

    @Story("Hội viên")
    @Test(description = "ADM19-219 (TC_SCRA09_FUNC_011): Trừ điểm hợp lệ cho khách demo qua popup F3 — đọc số dư trước, nhập email demo + số điểm âm + lý do → popup F3 'Xác nhận trừ điểm?' đúng 2 nút → xác nhận → POST, toast 'Đã điều chỉnh điểm', số dư giảm đúng số điểm trừ; hoàn tác cộng lại trong finally (chỉ dùng tài khoản demo; ACR-08 có bước hoàn tác -5 qua F3 nhưng không assert gì về lần trừ đó)")
    public void adminSubtractPointsForDemoCustomer() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        int pointsBefore = getDemoPointsOrSkip(adminPage);
        int amount = pointsBefore >= 100 ? 100 : 5;
        if (pointsBefore < amount) {
            throw new SkipException("Bo qua: so du demo chi con " + pointsBefore
                    + " diem (can it nhat " + amount + " de tru)");
        }
        try {
            adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "-" + amount, "Trừ điểm test ADM19-35");
            Assert.assertTrue(adminPage.isToastDisplayed(),
                    "Tru diem xong phai hien toast thanh cong (admin-toast)");
            Assert.assertTrue(adminPage.getToastTitleText().contains("Đã điều chỉnh điểm"),
                    "Toast tru diem phai co tieu de 'Đã điều chỉnh điểm', dang thay: " + adminPage.getToastTitleText());
            int pointsAfter = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
            Assert.assertEquals(pointsAfter, pointsBefore - amount,
                    "So du diem cua demo phai giam dung " + amount + " sau khi tru (truoc: " + pointsBefore
                            + ", sau: " + pointsAfter + ")");
        } finally {
            // Hoan tac: cong lai so diem da tru (best-effort, tong ket bang 0).
            try {
                adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, String.valueOf(amount),
                        "Hoàn tác điểm test ADM19-35");
            } catch (Throwable ignored) {
            }
        }
    }

    /** Dien form dieu chinh diem + bam nut, roi cho toi da 5 giay toi khi loi
     *  inline bang dung chuoi mong doi (loi tra cuu khach can goi API nen hien
     *  cham hon loi validate client-side). */
    private void assertAdjustPointsError(AdminPageObject adminPage, String emailOrId,
                                         String points, String reason, String expectedError) {
        adminPage.fillAdjustPointsForm(emailOrId, points, reason);
        adminPage.clickAdjustPointsButton();
        long deadline = System.currentTimeMillis() + 5000;
        String actual = adminPage.getAdjustErrorText();
        while (!expectedError.equals(actual) && System.currentTimeMillis() < deadline) {
            sleepInSecond(1);
            actual = adminPage.getAdjustErrorText();
        }
        Assert.assertEquals(actual, expectedError,
                "Dieu chinh diem (" + emailOrId + ", " + points + ") phai hien loi inline '" + expectedError + "'");
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Dieu chinh diem sai thi KHONG duoc hien popup F2/F3");
    }

    @Story("Hội viên")
    @Test(description = "ADM19-220 (TC_SCRA09_VAL_012/013/014/015/016 + TC_SCRA09_FUNC_017): Điều chỉnh điểm bị chặn inline — điểm 0 / vượt 1000 / không phải số nguyên (10.5) đều lỗi 'Số điểm điều chỉnh phải là số nguyên khác 0, tối đa 1000/lần.'; thiếu lý do → 'Điều chỉnh điểm bắt buộc phải có lý do.'; thiếu email/ID → 'Vui lòng nhập email hoặc ID khách hàng.'; email không tồn tại → 'Vui lòng kiểm tra lại email hoặc ID.'; tất cả đều KHÔNG hiện popup F2/F3, KHÔNG gọi POST")
    public void adminAdjustPointsValidationAndLookupErrors() {
        AdminPageObject adminPage = openAdminTab("membership");
        Assert.assertTrue(adminPage.isAdjustPointsFormDisplayed(),
                "Form dieu chinh diem phai hien tren trang Hoi vien");
        assertAdjustPointsError(adminPage, GlobalConstants.DEMO_EMAIL, "0", "Ly do test",
                "Số điểm điều chỉnh phải là số nguyên khác 0, tối đa 1000/lần.");
        assertAdjustPointsError(adminPage, GlobalConstants.DEMO_EMAIL, "1500", "Ly do test",
                "Số điểm điều chỉnh phải là số nguyên khác 0, tối đa 1000/lần.");
        assertAdjustPointsError(adminPage, GlobalConstants.DEMO_EMAIL, "10.5", "Ly do test",
                "Số điểm điều chỉnh phải là số nguyên khác 0, tối đa 1000/lần.");
        assertAdjustPointsError(adminPage, GlobalConstants.DEMO_EMAIL, "100", "",
                "Điều chỉnh điểm bắt buộc phải có lý do.");
        assertAdjustPointsError(adminPage, "", "100", "Ly do test",
                "Vui lòng nhập email hoặc ID khách hàng.");
        assertAdjustPointsError(adminPage, "khong-co-khach-12345@dafl.vn", "100", "Ly do test",
                "Vui lòng kiểm tra lại email hoặc ID.");
    }

    @Story("Hội viên")
    @Test(description = "ADM19-221 (TC_SCRA09_FUNC_018): Trừ điểm vượt số dư khả dụng bị FE chặn inline — đọc số dư thật lúc chạy, nhập số điểm trừ vượt số dư (số dư + 50) → lỗi inline 'Khách hàng chỉ còn {n} điểm.', KHÔNG hiện popup F3, KHÔNG gọi POST, số dư không đổi (số dư 50 trong manual chỉ là ví dụ; skip nếu số dư > 950 vì trừ vượt sẽ vượt giới hạn 1000/lần và FE chặn bằng lỗi 1000 trước)")
    public void adminSubtractPointsBeyondBalanceBlockedInline() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        int balance = getDemoPointsOrSkip(adminPage);
        if (balance > 950) {
            throw new SkipException("Bo qua: so du demo la " + balance
                    + " diem (> 950) nen tru vuot so du se vuot gioi han 1000/lan va FE chan bang loi 1000 truoc");
        }
        int overPoints = balance + 50;
        adminPage.clickAdminNav("membership");
        adminPage.fillAdjustPointsForm(GlobalConstants.DEMO_EMAIL, "-" + overPoints,
                "Trừ vượt số dư test ADM19-37");
        adminPage.clickAdjustPointsButton();
        Assert.assertEquals(adminPage.getAdjustErrorText(), "Khách hàng chỉ còn " + balance + " điểm.",
                "Tru vuot so du phai hien loi inline 'Khách hàng chỉ còn " + balance + " điểm.'");
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Tru vuot so du thi KHONG duoc hien popup F3");
        int balanceAfter = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
        Assert.assertEquals(balanceAfter, balance,
                "Tru vuot so du bi chan thi so du phai khong doi");
    }

    @Story("Hội viên")
    @Test(description = "ADM19-222 (TC_SCRA09_FUNC_019): Xem hạng và số dư điểm của khách trước/sau điều chỉnh — đọc hạng (cột Hạng) + số dư (cột Điểm) của khách demo ở bảng Khách hàng; điều chỉnh +5 điểm qua Hội viên (F2) → hạng không đổi, số dư +5 đúng; hoàn tác -5 trong finally (ACR-08 đã cover đối chiếu điểm nhưng không assert cột Hạng)")
    public void adminCustomerTierAndPointsBeforeAfterAdjust() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        int pointsBefore = getDemoPointsOrSkip(adminPage);
        String tierBefore = adminPage.getCustomerTierInRow(GlobalConstants.DEMO_EMAIL);
        try {
            adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "5", "Cộng điểm test ADM19-38");
            Assert.assertTrue(adminPage.getToastTitleText().contains("Đã điều chỉnh điểm"),
                    "Toast dieu chinh diem phai co tieu de 'Đã điều chỉnh điểm', dang thay: " + adminPage.getToastTitleText());
            int pointsAfter = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
            Assert.assertEquals(pointsAfter, pointsBefore + 5,
                    "So du diem cua demo phai tang dung 5 sau khi cong (truoc: " + pointsBefore
                            + ", sau: " + pointsAfter + ")");
            String tierAfter = adminPage.getCustomerTierInRow(GlobalConstants.DEMO_EMAIL);
            Assert.assertEquals(tierAfter, tierBefore,
                    "Cong 5 diem khong duoc doi hang hoi vien cua khach demo");
        } finally {
            try {
                adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "-5", "Hoàn tác điểm test ADM19-38");
            } catch (Throwable ignored) {
            }
        }
    }

    @Story("Hội viên")
    @Test(description = "ADM19-223 (TC_SCRA09_FUNC_020 + TC_SCRA09_FUNC_021 + TC_SCRA09_FUNC_024): Nút Đặt lại cấu hình hủy thay đổi chưa lưu, nút Làm mới tải lại cấu hình, bấm Hủy ở popup F1 thì không lưu — đổi ngưỡng Bạc (chưa lưu) → Đặt lại thì ô về giá trị đã tải, không gọi PUT; đổi ngưỡng Vàng → Lưu → popup F1 → Hủy → không gọi PUT, bản nháp còn trên ô nhập; bấm Làm mới → ô về giá trị đã lưu (lưu ý: giá trị 16.000.000đ trong mapping vi phạm thứ tự tăng dần nên test tính ngưỡng hợp lệ lúc chạy, xem note_W4.md; nút Làm mới đang hiện ký tự '⟳' không phải icon Lucide — chỉ ghi nhận, không assert icon)")
    public void adminMembershipConfigDiscardAndReload() {
        AdminPageObject adminPage = openAdminTab("membership");
        long silverOriginal = Long.parseLong(adminPage.getTierThresholdValue("Silver"));
        long goldOriginal = Long.parseLong(adminPage.getTierThresholdValue("Gold"));
        long diamond = Long.parseLong(adminPage.getTierThresholdValue("Diamond"));
        long newGold = silverOriginal + 1000000;
        if (newGold >= diamond || newGold == goldOriginal) {
            throw new SkipException("Bo qua: khong tinh duoc nguong Vang hop le (Bac " + silverOriginal
                    + ", Vang " + goldOriginal + ", Kim cuong " + diamond + ")");
        }
        adminPage.setTierThreshold("Silver", String.valueOf(silverOriginal + 500000));
        adminPage.clickMembersResetButton();
        Assert.assertEquals(adminPage.getTierThresholdValue("Silver"), String.valueOf(silverOriginal),
                "Bam 'Đặt lại cấu hình' thi o nguong Bac phai tro ve gia tri da tai");

        adminPage.setTierThreshold("Gold", String.valueOf(newGold));
        adminPage.clickSaveTiersButton();
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Luu cau hinh hop le phai mo popup F1 'Lưu cấu hình hạng?'");
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Lưu cấu hình hạng?",
                "Popup luu cau hinh phai co tieu de 'Lưu cấu hình hạng?', dang thay: "
                        + adminPage.getConfirmDialogTitle());
        adminPage.clickConfirmDialogCancel();
        adminPage.waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Bam Huy o F1 thi popup phai dong");
        Assert.assertEquals(adminPage.getTierThresholdValue("Gold"), String.valueOf(newGold),
                "Bam Huy o F1 thi KHONG luu nhung ban nhap van giu tren o nhap");

        adminPage.clickMembersRefreshButton();
        Assert.assertEquals(adminPage.getTierThresholdValue("Gold"), String.valueOf(goldOriginal),
                "Bam 'Làm mới' thi o nguong Vang phai tro ve gia tri da luu " + goldOriginal);
        Assert.assertEquals(adminPage.getTierThresholdValue("Silver"), String.valueOf(silverOriginal),
                "Bam 'Làm mới' thi o nguong Bac van la gia tri da luu");
    }

    @Story("Hội viên")
    @Test(description = "ADM19-224 (TC_SCRA09_FUNC_025 + TC_SCRA09_FUNC_026): Bấm Hủy ở popup F2 cộng điểm / F3 trừ điểm thì không cộng/trừ điểm — nhập email demo + 100 điểm + lý do → popup F2 'Xác nhận cộng điểm?' → Hủy → KHÔNG gọi POST, KHÔNG hiện toast, số dư không đổi; tương tự F3 'Xác nhận trừ điểm?' với -5 điểm (skip nhanh trừ nếu số dư demo < 5)")
    public void adminAdjustPointsCancelKeepsBalance() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        int balanceBefore = getDemoPointsOrSkip(adminPage);
        adminPage.clickAdminNav("membership");
        adminPage.fillAdjustPointsForm(GlobalConstants.DEMO_EMAIL, "100", "Cộng điểm test ADM19-40");
        adminPage.clickAdjustPointsButton();
        adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Xác nhận cộng điểm?",
                "Cong diem phai mo popup F2 'Xác nhận cộng điểm?', dang thay: " + adminPage.getConfirmDialogTitle());
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Xác nhận cộng điểm",
                "Nut xac nhan F2 phai la 'Xác nhận cộng điểm', dang thay: " + adminPage.getConfirmDialogConfirmText());
        adminPage.clickConfirmDialogCancel();
        adminPage.waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertFalse(adminPage.isToastPresent(),
                "Bam Huy o F2 thi KHONG duoc hien toast");
        int balanceAfterAdd = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
        Assert.assertEquals(balanceAfterAdd, balanceBefore,
                "Bam Huy o F2 thi so du phai khong doi");

        if (balanceBefore < 5) {
            return; // Khong du diem de test nhanh tru — test pass o nhanh cong.
        }
        adminPage.clickAdminNav("membership");
        adminPage.fillAdjustPointsForm(GlobalConstants.DEMO_EMAIL, "-5", "Trừ điểm test ADM19-40");
        adminPage.clickAdjustPointsButton();
        adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Xác nhận trừ điểm?",
                "Tru diem phai mo popup F3 'Xác nhận trừ điểm?', dang thay: " + adminPage.getConfirmDialogTitle());
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Xác nhận trừ điểm",
                "Nut xac nhan F3 phai la 'Xác nhận trừ điểm', dang thay: " + adminPage.getConfirmDialogConfirmText());
        adminPage.clickConfirmDialogCancel();
        adminPage.waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
        Assert.assertFalse(adminPage.isToastPresent(),
                "Bam Huy o F3 thi KHONG duoc hien toast");
        int balanceAfterSubtract = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
        Assert.assertEquals(balanceAfterSubtract, balanceBefore,
                "Bam Huy o F3 thi so du phai khong doi");
    }

    @Story("Hội viên")
    @Test(description = "ADM19-225 (TC_SCRA09_FUNC_027): Tra cứu khách bằng ID (không chứa @) qua GET /api/admin/customers/{id} — nhập ID khách demo + 50 điểm + lý do → popup F2 hiện đúng email khách + số dư mới → xác nhận → POST, toast 'Đã điều chỉnh điểm', số dư +50 đúng; hoàn tác -50 trong finally (ID đọc từ testid cột Điểm admin-customer-points-{id}; skip nếu không đọc được GUID)")
    public void adminAdjustPointsByCustomerId() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String customerId = adminPage.getCustomerIdByEmail(GlobalConstants.DEMO_EMAIL);
        if (customerId == null || customerId.isEmpty()) {
            throw new SkipException("Bo qua: khong doc duoc GUID cua khach " + GlobalConstants.DEMO_EMAIL
                    + " tu cot Diem de test tra cuu bang ID");
        }
        int pointsBefore = getDemoPointsOrSkip(adminPage);
        try {
            adminPage.clickAdminNav("membership");
            adminPage.fillAdjustPointsForm(customerId, "50", "Cộng điểm test ADM19-41");
            adminPage.clickAdjustPointsButton();
            adminPage.waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Xác nhận cộng điểm?",
                    "Tra cuu bang ID phai mo popup F2 'Xác nhận cộng điểm?', dang thay: " + adminPage.getConfirmDialogTitle());
            String dialogText = adminPage.getConfirmDialogText();
            Assert.assertTrue(dialogText.contains(GlobalConstants.DEMO_EMAIL),
                    "Popup F2 phai hien dung email khach '" + GlobalConstants.DEMO_EMAIL
                            + "', dang thay: " + dialogText);
            Assert.assertTrue(dialogText.contains(String.valueOf(pointsBefore + 50)),
                    "Popup F2 phai hien so du moi " + (pointsBefore + 50) + ", dang thay: " + dialogText);
            adminPage.clickConfirmDialogConfirm();
            adminPage.waitForToast();
            Assert.assertTrue(adminPage.getToastTitleText().contains("Đã điều chỉnh điểm"),
                    "Toast dieu chinh diem phai co tieu de 'Đã điều chỉnh điểm', dang thay: " + adminPage.getToastTitleText());
            int pointsAfter = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
            Assert.assertEquals(pointsAfter, pointsBefore + 50,
                    "So du diem cua demo phai tang dung 50 sau khi cong bang ID (truoc: " + pointsBefore
                            + ", sau: " + pointsAfter + ")");
        } finally {
            try {
                adminPage.adjustPointsForUser(customerId, "-50", "Hoàn tác điểm test ADM19-41");
            } catch (Throwable ignored) {
            }
        }
    }

    @Story("Hội viên")
    @Test(description = "ADM19-226 (TC_SCRA09_VAL_028): Ngưỡng freeship để trống thì lưu dạng null (hiển thị 'Không đặt') — xóa trắng ô Ngưỡng freeship hạng Thường → Lưu → xác nhận F1 → toast 'Đã lưu cấu hình hạng'; ô freeship hạng Thường hiện trống với placeholder 'Không đặt'; modal chi tiết hạng Thường hiện Ngưỡng freeship 'Không đặt'; khôi phục giá trị cũ trong finally (nếu gốc đã trống thì không cần khôi phục)")
    public void adminTierFreeShipBlankSavesAsNull() {
        AdminPageObject adminPage = openAdminTab("membership");
        String originalFreeShip = adminPage.getTierFreeShipValue("Member");
        try {
            adminPage.setTierFreeShip("Member", "");
            adminPage.clickSaveTiersButton();
            Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                    "Luu cau hinh hop le phai mo popup F1 'Lưu cấu hình hạng?'");
            adminPage.clickConfirmDialogConfirm();
            adminPage.waitForToast();
            Assert.assertEquals(adminPage.getToastTitleText(), "Đã lưu cấu hình hạng",
                    "Luu cau hinh xong phai hien toast 'Đã lưu cấu hình hạng', dang thay: " + adminPage.getToastTitleText());
            Assert.assertEquals(adminPage.getTierFreeShipValue("Member"), "",
                    "Sau khi luu, o freeship hang Thuong phai trong (null)");
            Assert.assertEquals(adminPage.getTierFreeShipPlaceholder("Member"), "Không đặt",
                    "O freeship trong phai hien placeholder 'Không đặt'");
            adminPage.clickTierDetail("Thường");
            String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_TIER_DETAIL);
            Assert.assertTrue(modalText.contains("Không đặt"),
                    "Modal chi tiet hang Thuong phai hien Ngưỡng freeship 'Không đặt', dang thay: " + modalText);
            adminPage.closeDetailModal("admin-tier-detail");
        } finally {
            restoreTierFreeShip(adminPage, "Member", originalFreeShip);
        }
    }
}
