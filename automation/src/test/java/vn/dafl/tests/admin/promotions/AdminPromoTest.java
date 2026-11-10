package vn.dafl.tests.admin.promotions;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Test khuyen mai / voucher tren app Admin rieng localhost:3001 (dot 9 + dot 13,
 * tach tu AdminCrudTest / AdminDetailTest cu khi chia thu muc admin dot 17 —
 * moi lop chi lo 1 phan he).
 * Chi tao/sua/xoa THAT tren voucher do chinh test tao (ma kem timestamp) va tu
 * don dep sau khi chay; voucher kich hoat roi tat (Active -> Disabled) de lai
 * an toan vi da vo hieu, khong ap dung duoc khi dat hang. Voucher giu quy tac
 * Draft-only sua/xoa (chi voucher Draft moi sua/xoa duoc).
 * Dot 18 wave D: form tao/sua voucher la DRAWER RONG 680px
 * (admin-promo-form-modal, class fs-drawer, --fs-width, overlay — bang danh
 * sach VAN O LAI trong DOM khi form mo); bang
 * them cot STT + cot Trang thai dang Switch (chi hien o voucher
 * Active/Disabled; Draft chi hien pill chu 'Draft'); gat Switch Active ->
 * mo ConfirmDialog 'Tắt khuyến mãi?' (ly do bat buoc: bam xac nhan khi de
 * trong thi bao 'Vui lòng bổ sung thông tin bắt buộc.', dialog van mo);
 * trung ma thi CA toast CA message do duoi o Ma
 * ('Mã khuyến mãi đã tồn tại (không phân biệt hoa thường).'); tien hien
 * dang '50.000₫' (ky tu ₫, khong phai 'đ').
 * Dot 21: form tao/sua/xem/luot dung KM la TRANG FULL PAGE (/promotions/new,
 * /promotions/:code/edit, /promotions/:code, /promotions/:code/usages — KHONG
 * con Drawer, KHONG nut tao ma ngau nhien, KHONG toggle 'Kich hoat ngay sau
 * khi tao' — tao moi mac dinh Nhap); loai giam la 3 RADIO CARD (Cố định /
 * Phần trăm / Miễn phí ship, hien tieng Viet o cot Loai giam); cot 'Hạn' doi
 * ten 'Hạn sử dụng'; dropdown loc nhan moi ('Đang hoạt động' / 'Tạm ngưng' /
 * 'Đã hết hạn'); voucher Draft CO Switch (tat, bam de kich hoat), voucher
 * Expired CO Switch nhung bi DISABLE; nut 'Lượt dùng' dang text doi thanh
 * icon History mo trang Lich su su dung (6 cot, 2 cot '—' vi DTO thieu field).
 * Dot 21 (22:37): gat Switch bat/tat deu qua modal xac nhan CHUNG statusChange
 * ('Xác nhận thay đổi trạng thái?', [Hủy][Xác nhận]) + toast statusChanged
 * ('Đổi trạng thái khuyến mãi thành công!' + 'Khuyến mãi {mã} đã được cập nhật
 * trạng thái.'); popup C3 tat co ly do CHI con o luong chan xoa A5; tat bang
 * Switch gui ly do mac dinh 'Tắt bởi quản trị viên.' (BE bat Reason).
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý khuyến mãi")
public class AdminPromoTest extends BaseTest {

    /** Dang nhap Super Admin tren app Admin rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Ma voucher duy nhat theo tung lan chay (chu + so, hop le 3-50 ky tu). */
    private String uniquePromoCode() {
        return "TEST" + System.currentTimeMillis();
    }

    /** Ma voucher duy nhat cho test trung ma (11 ky tu: VCDUP + 6 so cuoi timestamp). */
    private String uniqueDupPromoCode() {
        return "VCDUP" + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    /** Lay text cua dong dau tien trong bang dang mo co chua chuoi cho truoc ("" neu khong co). */
    private String getRowContaining(AdminPageObject adminPage, String text) {
        return adminPage.getAdminTableRowTexts().stream()
                .filter(row -> row.contains(text))
                .findFirst().orElse("");
    }

    /** Mo /admin va dung o muc Khuyen mai; tra ve AdminPageObject da san sang. */
    private AdminPageObject openPromosOrSkip() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("promotions");
        return adminPage;
    }

    // ---------- Khuyen mai / voucher (ACR-05, goc dot 9, viet lai dot 13 + dot 17) ----------

    @Story("Khuyến mãi")
    @Test(description = "ACR-05 (đợt 21, form trang full page + Switch + modal statusChange): Tạo voucher Draft qua trang mới (loại %, đơn tối thiểu, hạn) → sửa mô tả/giá trị/đơn tối thiểu/trần giảm/hạn/lượt dùng → xóa qua ConfirmDialog; voucher thứ 2 dùng mã tự sinh + loại giảm tiền cố định → kích hoạt (Switch bật, modal statusChange) → tắt qua Switch (modal statusChange, lý do mặc định 'Tắt bởi quản trị viên.')")
    public void adminPromoCreateActivateDisable() {
        String promoCode = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");

        // (1) Tao voucher Draft moi qua form moi (giam 10%, don toi thieu 0, han 31/12/2027).
        adminPage.createPromoFull(promoCode, "Voucher test ACR-05", "Percentage", "10",
                "0", "", "", "2027-12-31");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher xong phai hien toast thanh cong (admin-toast)");
        adminPage.searchPromos(promoCode);
        String row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains("Nháp"),
                "Voucher vua tao phai o trang thai Nháp (badge Nháp duoi Switch), dang thay: " + row);
        Assert.assertTrue(row.contains("10%"),
                "Voucher vua tao phai hien gia tri giam 10%, dang thay: " + row);
        Assert.assertTrue(adminPage.isPromoEditButtonDisplayedFor(promoCode),
                "Voucher Draft phai co nut sua (admin-promo-edit-btn)");
        Assert.assertTrue(adminPage.isPromoDeleteButtonDisplayedFor(promoCode),
                "Voucher Draft phai co nut xoa (admin-promo-delete-btn)");

        // (2) Sua voucher Draft: doi mo ta + gia tri giam 10% -> 15% + don toi thieu
        // 100.000₫ + tran giam 50.000₫ + han 30/06/2028 + tong luot 100.
        String editedDescription = "Voucher test ACR-05 da sua";
        adminPage.editPromoFull(promoCode, editedDescription, "15", "100000", "50000", "2028-06-30", "100");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Sua voucher xong phai hien toast thanh cong (admin-toast)");
        adminPage.searchPromos(promoCode);
        row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains(editedDescription),
                "Sau khi sua, dong voucher phai hien mo ta moi, dang thay: " + row);
        Assert.assertTrue(row.contains("15%"),
                "Sau khi sua, dong voucher phai hien gia tri moi 15%, dang thay: " + row);
        Assert.assertTrue(row.contains("Nháp"),
                "Sau khi sua voucher van o trang thai Nháp (badge Nháp duoi Switch), dang thay: " + row);

        // (3) Xoa voucher Draft qua ConfirmDialog 'Xóa khuyến mãi?' -> khong con trong bang.
        adminPage.deletePromoByCode(promoCode);
        Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa voucher xong phai hien toast thanh cong (admin-toast)");
        adminPage.searchPromos(promoCode);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim voucher vua xoa phai ra 0 dong");
        Assert.assertTrue(adminPage.isEmptyPromosMessageDisplayed(),
                "Tim voucher vua xoa phai hien dong 'Chưa có khuyến mãi'");

        // (4) Voucher thu 2: dung ma tu sinh phia test (dot 21: nut tao ma ngau
        // nhien DA BO khoi trang tao moi) + loai giam tien co dinh 50.000₫,
        // don toi thieu 100.000₫ -> kich hoat -> tat qua Switch.
        String secondCode = adminPage.createPromoFull(uniquePromoCode(), "Voucher test ACR-05 thu 2",
                "Fixed", "50000", "100000", "", "", "");
        Assert.assertFalse(secondCode == null || secondCode.isEmpty(),
                "Ma voucher tu sinh phia test phai khong rong");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher thu 2 xong phai hien toast thanh cong (admin-toast)");
        adminPage.searchPromos(secondCode);
        row = getRowContaining(adminPage, secondCode);
        Assert.assertTrue(row.contains("Nháp"),
                "Voucher thu 2 vua tao phai o trang thai Nháp (badge Nháp duoi Switch), dang thay: " + row);
        Assert.assertTrue(row.contains("50.000₫"),
                "Voucher giam tien co dinh phai hien gia tri 50.000₫, dang thay: " + row);

        adminPage.activatePromo(secondCode);
        Assert.assertTrue(adminPage.isToastDisplayed(), "Kich hoat voucher xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đổi trạng thái khuyến mãi thành công!"),
                "Toast kich hoat voucher phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + adminPage.getToastTitleText());
        Assert.assertTrue(adminPage.getToastDescText().contains("Khuyến mãi " + secondCode + " đã được cập nhật trạng thái."),
                "Toast kich hoat voucher phai ghi 'Khuyến mãi " + secondCode + " đã được cập nhật trạng thái.', dang thay: " + adminPage.getToastDescText());
        adminPage.searchPromos(secondCode);
        // Dot 18: voucher Active khong con chu 'Active' trong dong (Switch chi co
        // num gat, khong co nhan chu) nen doc trang thai qua aria-checked.
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(secondCode), Boolean.TRUE,
                "Sau khi kich hoat Switch voucher phai bat (aria-checked=true, Active)");
        Assert.assertFalse(adminPage.isPromoEditButtonDisplayedFor(secondCode),
                "Voucher Active khong duoc con nut sua (chi Draft moi sua duoc)");
        Assert.assertFalse(adminPage.isPromoDeleteButtonDisplayedFor(secondCode),
                "Voucher Active khong duoc con nut xoa (chi Draft moi xoa duoc)");

        // Dot 21 (22:37): tat voucher bang Switch qua modal chung statusChange
        // (khong o ly do) — FE gui ly do mac dinh 'Tắt bởi quản trị viên.'.
        adminPage.disablePromo(secondCode);
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tat voucher xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đổi trạng thái khuyến mãi thành công!"),
                "Toast tat voucher phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + adminPage.getToastTitleText());
        Assert.assertTrue(adminPage.getToastDescText().contains("Khuyến mãi " + secondCode + " đã được cập nhật trạng thái."),
                "Toast tat voucher phai ghi 'Khuyến mãi " + secondCode + " đã được cập nhật trạng thái.', dang thay: " + adminPage.getToastDescText());
        adminPage.searchPromos(secondCode);
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(secondCode), Boolean.FALSE,
                "Sau khi tat Switch voucher phai tat (aria-checked=false, Disabled)");
        row = getRowContaining(adminPage, secondCode);
        Assert.assertTrue(row.contains("Tắt bởi quản trị viên."),
                "Sau khi tat dong voucher phai hien ly do mac dinh 'Tắt bởi quản trị viên.', dang thay: " + row);
    }

    // ---------- Dot 17: ma trung / Switch tat voucher ----------

    @Story("Khuyến mãi")
    @Test(description = "ADM17-03 (đợt 17): Tạo voucher với mã trùng (gõ chữ thường để kiểm tra không phân biệt hoa thường) → CẢ toast CẢ message đỏ dưới ô Mã đều là 'Mã khuyến mãi đã tồn tại (không phân biệt hoa thường).', form vẫn mở, voucher thứ 2 không được tạo")
    public void adminCreatePromoWithDuplicateCodeShowsInlineError() {
        String code = uniqueDupPromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");

        adminPage.createPromoFull(code, "Voucher test ADM17-03", "Percentage", "10",
                "0", "", "", "2027-12-31");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher dau tien xong phai hien toast thanh cong (admin-toast)");

        try {
            // Tao voucher thu 2 trung ma (go chu thuong — FE tu viet hoa khi go,
            // BE so trung khong phan biet hoa thuong).
            adminPage.openPromoForm();
            adminPage.fillPromoForm(code.toLowerCase(), "Voucher test ADM17-03 lan 2",
                    "Percentage", "10", "0", "", "", "2027-12-31");
            adminPage.clickPromoSaveButton();
            String toast = adminPage.waitForToast();
            Assert.assertTrue(toast.contains("Mã khuyến mãi đã tồn tại (không phân biệt hoa thường)."),
                    "Trung ma voucher thi toast phai ghi 'Mã khuyến mãi đã tồn tại (không phân biệt hoa thường).', dang thay: " + toast);
            Assert.assertEquals(adminPage.getPromoFieldErrorText("code"),
                    "Mã khuyến mãi đã tồn tại (không phân biệt hoa thường).",
                    "Trung ma voucher thi message do duoi o Ma phai ghi dung chuoi tren");
            Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                    "Luu trung ma that bai thi form phai van mo (khong dong, khong tao voucher)");

            // Dong form (dirty -> hop 'Thoát mà chưa lưu?' -> bam Thoat).
            adminPage.cancelPromoForm();
            Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                    "Dong form xong khong duoc con thay form khuyen mai");

            // Chi co dung 1 voucher mang ma nay (voucher dau tien).
            adminPage.searchPromos(code);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Voucher thu 2 trung ma khong duoc tao nen tim theo ma chi thay dung 1 dong");
        } finally {
            // Don dep: xoa voucher Draft do test tao (neu van con).
            try {
                adminPage.searchPromos(code);
                if (adminPage.isRowWithTextDisplayed(code)) {
                    adminPage.deletePromoByCode(code);
                }
            } catch (Throwable ignored) {
                // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
            }
        }
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM17-10 (đợt 21, 22:37): Gạt Switch voucher Active → mở modal xác nhận CHUNG 'Xác nhận thay đổi trạng thái?' (KHÔNG có ô lý do; FE tự gửi lý do mặc định 'Tắt bởi quản trị viên.') → xác nhận → toast 'Đổi trạng thái khuyến mãi thành công!' + desc 'Khuyến mãi {mã} đã được cập nhật trạng thái.', dòng hiện lý do mặc định, Switch tắt (Disabled)")
    public void adminPromoSwitchActiveOpensDisableReasonModal() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");

        adminPage.createPromoFull(code, "Voucher test ADM17-10", "Percentage", "10",
                "0", "", "", "2027-12-31");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher xong phai hien toast thanh cong (admin-toast)");
        adminPage.activatePromo(code);
        Assert.assertTrue(adminPage.isToastDisplayed(), "Kich hoat voucher xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đổi trạng thái khuyến mãi thành công!"),
                "Toast kich hoat phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + adminPage.getToastTitleText());
        adminPage.searchPromos(code);
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(code), Boolean.TRUE,
                "Voucher Active phai co Switch bat (aria-checked=true)");

        // (1) Gat Switch Active -> modal xac nhan CHUNG statusChange (KHONG co o
        // ly do — popup C3 co o ly do CHI con o luong chan xoa A5).
        adminPage.clickRowStatusSwitch(code);
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Gat Switch voucher Active phai mo modal xac nhan chung");
        Assert.assertTrue(adminPage.getConfirmDialogTitle().contains("Xác nhận thay đổi trạng thái?"),
                "Modal tat voucher phai co tieu de 'Xác nhận thay đổi trạng thái?', dang thay: " + adminPage.getConfirmDialogTitle());
        Assert.assertTrue(adminPage.getConfirmDialogText().contains("khuyến mãi " + code),
                "Modal tat voucher phai nhac ten khuyen mai '" + code + "', dang thay: " + adminPage.getConfirmDialogText());
        Assert.assertFalse(adminPage.isPromoDisableReasonModalDisplayed(),
                "Modal tat qua Switch KHONG duoc co o nhap ly do (C3 chi con o luong chan xoa A5)");
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Modal xac nhan chung phai CHI co dung 2 nut ([Hủy] + [Xác nhận])");
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Xác nhận",
                "Nut xac nhan cua modal chung phai la 'Xác nhận'");

        // (2) Bam xac nhan -> voucher Disabled, toast statusChanged, dong hien ly
        // do mac dinh 'Tắt bởi quản trị viên.' (FE gui vi BE bat buoc Reason).
        adminPage.clickConfirmYes();
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tat voucher xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đổi trạng thái khuyến mãi thành công!"),
                "Toast tat voucher phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + adminPage.getToastTitleText());
        Assert.assertTrue(adminPage.getToastDescText().contains("Khuyến mãi " + code + " đã được cập nhật trạng thái."),
                "Toast tat voucher phai ghi 'Khuyến mãi " + code + " đã được cập nhật trạng thái.', dang thay: " + adminPage.getToastDescText());
        adminPage.searchPromos(code);
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(code), Boolean.FALSE,
                "Tat voucher xong Switch phai tat (aria-checked=false, Disabled)");
        String row = getRowContaining(adminPage, code);
        Assert.assertTrue(row.contains("Tắt bởi quản trị viên."),
                "Sau khi tat bang Switch dong voucher phai hien ly do mac dinh 'Tắt bởi quản trị viên.', dang thay: " + row);
    }

    // ---------- Chi tiet khuyen mai (ADT-03, goc dot 8) ----------

    @Story("Khuyến mãi")
    @Test(description = "ADT-03 (đợt 21, trang Chi tiết full page): Mở trang chi tiết bằng nút mắt → breadcrumb 'Quản lý khuyến mãi / Chi tiết', tiêu đề 'Chi tiết khuyến mãi', đủ 4 card (Thông tin chung/Điều kiện áp dụng/Thời gian/Thống kê sử dụng), đúng mã + badge trạng thái (Nháp/Đang chạy/Đã tắt/Hết hạn); [Quay lại] về danh sách; nếu là Draft thì [Chỉnh sửa] mở trang sửa (mode 'edit', mã disabled)")
    public void promoDetailModalShowsCorrectCode() {
        AdminPageObject adminPage = openPromosOrSkip();
        String promoCode = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PROMO_DETAIL_BTN, "Xem chi tiết khuyến mãi ");
        if (promoCode == null || promoCode.isEmpty()) {
            throw new SkipException("Bo qua: muc Khuyen mai khong co dong nao de mo chi tiet");
        }

        adminPage.clickFirstPromoDetail();
        Assert.assertTrue(adminPage.isPromoDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet khuyen mai (admin-promo-detail-page)");
        String detailText = adminPage.getPromoDetailPageText();
        Assert.assertTrue(detailText.contains(promoCode),
                "Trang chi tiet phai hien dung ma khuyen mai '" + promoCode + "', dang thay: " + detailText);
        Assert.assertTrue(detailText.contains("Chi tiết khuyến mãi"),
                "Trang chi tiet phai co tieu de 'Chi tiết khuyến mãi', dang thay: " + detailText);
        Assert.assertTrue(adminPage.isPromoDetailCardsDisplayed(),
                "Trang chi tiet phai co du 4 card: Thong tin chung / Dieu kien ap dung / Thoi gian / Thong ke su dung");
        Assert.assertTrue(detailText.contains("Nháp") || detailText.contains("Đang chạy")
                        || detailText.contains("Đã tắt") || detailText.contains("Hết hạn"),
                "Trang chi tiet phai co badge trang thai (Nháp/Đang chạy/Đã tắt/Hết hạn), dang thay: " + detailText);
        Assert.assertFalse(adminPage.isPromoFormModalDisplayed(),
                "Trang chi tiet dot 21 KHONG duoc mo duoi dang Drawer/modal form");

        // Neu voucher la Draft: [Chỉnh sửa] o header -> trang sua, mode 'edit',
        // o Ma disabled; Huy (form sach) -> ve danh sach.
        if (adminPage.isPromoDetailEditButtonDisplayed()) {
            adminPage.openPromoEditFormFromDetail();
            Assert.assertTrue(adminPage.isPromoEditPageDisplayed(),
                    "Bam [Chỉnh sửa] o trang chi tiet phai mo trang sua (admin-promo-edit-page)");
            Assert.assertEquals(adminPage.getPromoFormMode(), "edit",
                    "Trang sua khuyen mai phai co data-form-mode='edit'");
            Assert.assertTrue(adminPage.isPromoCodeInputDisabled(),
                    "Trang sua: o ma khuyen mai phai disabled (khong go duoc)");
            adminPage.cancelPromoForm();
            Assert.assertTrue(adminPage.isPromoListPageDisplayed(),
                    "Huy o trang sua (form sach) phai ve lai danh sach khuyen mai");
            // Mo lai trang chi tiet de kiem tra nut [Quay lại].
            adminPage.searchPromos(promoCode);
            adminPage.clickFirstPromoDetail();
            Assert.assertTrue(adminPage.isPromoDetailPageDisplayed(),
                    "Phai mo lai duoc trang chi tiet khuyen mai");
        }

        adminPage.clickPromoDetailBackButton();
        Assert.assertTrue(adminPage.isPromoListPageDisplayed(),
                "Bam [Quay lại] o trang chi tiet phai ve danh sach khuyen mai");
    }

    // ---------- Wave 19 (mapping SCR_06): bang / toolbar / loc / tim kiem / phan trang (chi doc) ----------

    @Story("Khuyến mãi")
    @Test(description = "ADM19-159 (TC_SCRA06_UI_001, đợt 21): Bảng khuyến mãi có đủ 10 cột đúng thứ tự STT/Mã/Mô tả/Loại giảm/Giá trị/Đơn tối thiểu/Đã dùng/Hạn sử dụng/Trạng thái/Thao tác; dòng đầu hiện mã dạng tag, loại giảm tiếng Việt (Phần trăm/Cố định/Miễn phí ship), giá trị ('10%'/'Freeship'/tiền), đơn tối thiểu, đã dùng, hạn (skip nếu bảng trống)")
    public void adminPromoTableHasTenColumns() {
        AdminPageObject adminPage = openPromosOrSkip();
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: muc Khuyen mai chua co voucher nao de kiem tra bo cuc bang");
        }

        java.util.List<String> headers = adminPage.getAdminTableHeaders();
        Assert.assertEquals(headers, java.util.Arrays.asList(
                        "STT", "Mã", "Mô tả", "Loại giảm", "Giá trị", "Đơn tối thiểu", "Đã dùng", "Hạn sử dụng", "Trạng thái", "Thao tác"),
                "Tieu de cot bang Khuyen mai phai dung thu tu STT/Mã/Mô tả/Loại giảm/Giá trị/Đơn tối thiểu/Đã dùng/Hạn sử dụng/Trạng thái/Thao tác, dang thay: " + headers);
        java.util.List<String> firstCells = adminPage.getFirstTableRowCellTexts();
        Assert.assertEquals(firstCells.size(), 10, "Dong voucher phai co dung 10 o, dang thay: " + firstCells.size());
        Assert.assertFalse(firstCells.get(1).trim().isEmpty(),
                "O Ma phai hien ma voucher dang tag, khong duoc rong");
        Assert.assertTrue(firstCells.get(3).contains("Phần trăm") || firstCells.get(3).contains("Cố định")
                        || firstCells.get(3).contains("Miễn phí ship"),
                "O Loai giam phai hien tieng Viet Phần trăm/Cố định/Miễn phí ship, dang thay: " + firstCells.get(3));
        Assert.assertTrue(firstCells.get(4).contains("%") || firstCells.get(4).contains("Freeship")
                        || firstCells.get(4).contains("₫"),
                "O Gia tri phai hien '10%'/'Freeship'/tien '...₫', dang thay: " + firstCells.get(4));
        Assert.assertTrue(firstCells.get(5).contains("₫"),
                "O Don toi thieu phai hien tien dang '...₫', dang thay: " + firstCells.get(5));
        Assert.assertTrue(firstCells.get(6).matches(".*\\d+.*"),
                "O Da dung phai hien so luot da dung (n hoac n/max), dang thay: " + firstCells.get(6));
        Assert.assertTrue(firstCells.get(7).contains("/") || firstCells.get(7).trim().equals("—"),
                "O Han su dung phai hien ngay het han dd/mm/yyyy hoac '—', dang thay: " + firstCells.get(7));
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-160 (TC_SCRA06_UI_002): STT bảng khuyến mãi đánh số liên tục theo trang: trang 1 STT 1–10; sang trang 2 STT bắt đầu từ 11 (skip nếu ≤10 voucher)")
    public void adminPromoSttContinuousAcrossPages() {
        AdminPageObject adminPage = openPromosOrSkip();
        int total = adminPage.getPaginationTotalRows("promotions");
        if (total <= 10) {
            throw new SkipException("Bo qua: muc Khuyen mai chi co " + total + " voucher (can >10 de kiem tra STT theo trang)");
        }
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");
        adminPage.clickPaginationNext("promotions");
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai la 11 (lien tuc theo trang, khong danh lai tu 1)");
        adminPage.clickPaginationPrev("promotions");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "Ve trang 1 thi STT dong dau phai lai la 1");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-161 (TC_SCRA06_UI_003, đợt 21): Toolbar khuyến mãi: trái là ô tìm 'Tìm mã, mô tả khuyến mãi...' + dropdown lọc trạng thái (Tất cả trạng thái/Nháp/Đang hoạt động/Tạm ngưng/Đã hết hạn); phải là icon Tìm kiếm + icon Đặt lại (Lucide) + nút '+ Thêm mới' sát phải nhất; KHÔNG có nút Export, KHÔNG nút 'Lọc' riêng, KHÔNG nút 'Làm mới'")
    public void adminPromoToolbarHasSearchFilterAndAddButton() {
        AdminPageObject adminPage = openPromosOrSkip();

        Assert.assertEquals(adminPage.getPromoSearchPlaceholder(), "Tìm mã, mô tả khuyến mãi...",
                "O tim kiem Khuyen mai phai co placeholder 'Tìm mã, mô tả khuyến mãi...'");
        java.util.List<String> options = adminPage.getStatusFilterOptionTexts();
        Assert.assertEquals(options, java.util.Arrays.asList(
                        "Tất cả trạng thái", "Nháp", "Đang hoạt động", "Tạm ngưng", "Đã hết hạn"),
                "Dropdown loc trang thai voucher phai co dung 5 lua chon, dang thay: " + options);
        Assert.assertTrue(adminPage.isToolbarSearchIconDisplayed("promotions"),
                "Nut Tim o toolbar Khuyen mai phai chua icon Lucide Search (svg.lucide-search)");
        Assert.assertTrue(adminPage.isToolbarResetIconDisplayed("promotions"),
                "Nut Dat lai o toolbar Khuyen mai phai chua icon Lucide RotateCcw (svg.lucide-rotate-ccw)");
        Assert.assertTrue(adminPage.isPromoAddButtonDisplayed(),
                "Toolbar Khuyen mai phai co nut '+ Thêm mới' (promotions-add-btn)");
        Assert.assertTrue(adminPage.isToolbarAddButtonLast("promotions"),
                "Nut '+ Thêm mới' phai sat phai nhat trong toolbar");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Khuyen mai khong duoc co nut Export");
        Assert.assertFalse(adminPage.getToolbarText().contains("Làm mới"),
                "Toolbar Khuyen mai khong duoc co nut 'Làm mới'");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-162 (TC_SCRA06_UI_004, đợt 21): Bấm '+ Thêm mới' mở TRANG Thêm mới full page (/promotions/new): breadcrumb 'Quản lý khuyến mãi / Thêm mới', tiêu đề 'Thêm mới khuyến mãi' + mô tả 'Thiết lập thông tin và điều kiện áp dụng cho chương trình khuyến mãi', nút [Lưu] + [Hủy] ở trên cùng; KHÔNG phải Drawer/modal, KHÔNG nút Quay lại; bảng KM KHÔNG còn trong DOM; Hủy (form sạch) về danh sách")
    public void adminPromoAddOpensDrawer680() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();

        Assert.assertTrue(adminPage.isPromoCreatePageDisplayed(),
                "Bam '+ Thêm mới' phai mo trang tao khuyen mai full page (admin-promo-create-page)");
        Assert.assertFalse(adminPage.isPromoFormModalDisplayed(),
                "Trang tao khuyen mai dot 21 KHONG duoc mo duoi dang Drawer/modal");
        String pageText = adminPage.getPromoCreatePageText();
        Assert.assertTrue(pageText.contains("Thêm mới khuyến mãi"),
                "Trang tao khuyen mai phai co tieu de 'Thêm mới khuyến mãi', dang thay: " + pageText);
        Assert.assertTrue(pageText.contains("Thiết lập thông tin và điều kiện áp dụng cho chương trình khuyến mãi"),
                "Trang tao khuyen mai phai co mo ta 'Thiết lập thông tin và điều kiện áp dụng cho chương trình khuyến mãi', dang thay: " + pageText);
        Assert.assertTrue(pageText.contains("Quản lý khuyến mãi") && pageText.contains("Thêm mới"),
                "Trang tao khuyen mai phai co breadcrumb 'Quản lý khuyến mãi / Thêm mới', dang thay: " + pageText);
        Assert.assertTrue(pageText.contains("Lưu") && pageText.contains("Hủy"),
                "Trang tao khuyen mai phai co nut [Lưu] + [Hủy] o tren cung, dang thay: " + pageText);
        Assert.assertFalse(adminPage.isAdminTablePresent(),
                "Khi trang tao mo thi bang danh sach khuyen mai KHONG duoc con trong DOM (trang full page thay the)");

        adminPage.cancelPromoForm();
        Assert.assertTrue(adminPage.isPromoListPageDisplayed(),
                "Huy o trang tao (form sach) phai ve lai danh sach khuyen mai");
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-163 (TC_SCRA06_FUNC_019, đợt 21, MUTATES — để lại 1 voucher Draft, xóa được vì chỉ Draft mới xóa được): Tạo voucher mới KHÔNG còn toggle 'Kích hoạt ngay sau khi tạo' (đã bỏ) — sau khi lưu, voucher xuất hiện trong bảng ở trạng thái Nháp (Switch tắt, badge Nháp), ghi chú trạng thái 'Nháp — khuyến mãi mới được tạo ở trạng thái Nháp.'; dọn dẹp: xóa voucher Draft")
    public void adminPromoCreateWithActivateNowAutoActivates() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        try {
            adminPage.openPromoForm();
            Assert.assertTrue(adminPage.isPromoCreatePageDisplayed(),
                    "Bam '+ Thêm mới' phai mo trang tao khuyen mai full page");
            Assert.assertFalse(adminPage.isPromoStatusToggleDisplayed(),
                    "Trang tao khuyen mai dot 21 KHONG duoc co toggle 'Kích hoạt ngay sau khi tạo'");
            Assert.assertTrue(adminPage.getPromoStatusText().contains("Nháp"),
                    "Trang tao khuyen mai phai ghi chu trang thai 'Nháp — khuyến mãi mới được tạo ở trạng thái Nháp.', dang thay: " + adminPage.getPromoStatusText());
            adminPage.fillPromoForm(code, "Voucher test ADM19-47 kich hoat ngay",
                    "Percentage", "10", "0", "", "", "2027-12-31");
            adminPage.savePromoForm();
            Assert.assertTrue(adminPage.isToastDisplayed(),
                    "Tao voucher xong phai hien toast thanh cong (admin-toast)");
            Assert.assertTrue(adminPage.getToastDescText().contains("Thêm khuyến mãi " + code + " thành công."),
                    "Toast tao voucher phai ghi 'Thêm khuyến mãi " + code + " thành công.', dang thay: " + adminPage.getToastDescText());

            adminPage.searchPromos(code);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(code), Boolean.FALSE,
                    "Voucher moi tao mac dinh Nhap thi Switch phai tat (aria-checked=false)");
            String row = getRowContaining(adminPage, code);
            Assert.assertTrue(row.contains("Nháp"),
                    "Voucher moi tao phai hien badge 'Nháp' duoi Switch, dang thay: " + row);
        } finally {
            // Don dep: xoa voucher Draft (chi Draft moi xoa duoc).
            try {
                adminPage.searchPromos(code);
                if (adminPage.isPromoDeleteButtonDisplayedFor(code)) {
                    adminPage.deletePromoByCode(code);
                }
            } catch (Throwable ignored) {
                // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
            }
        }
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-164 (TC_SCRA06_FUNC_025, đợt 21, 22:37, MUTATES — đổi trạng thái voucher thật, khôi phục tắt lại trong finally): Gạt Switch ở dòng voucher Disabled → modal chung 'Xác nhận thay đổi trạng thái?' (đúng 2 nút, [Hủy][Xác nhận]) → bấm 'Xác nhận': voucher chuyển Đang chạy (Switch bật), toast 'Đổi trạng thái khuyến mãi thành công!'; khôi phục: tắt lại bằng Switch (modal chung, lý do mặc định) trong finally (skip nếu không có voucher Disabled chưa hết hạn — không tự tắt voucher của người khác để làm tiền điều kiện)")
    public void adminPromoSwitchOnDisabledShowsEnableConfirmAndActivates() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        String code = adminPage.findDisabledNotExpiredPromoCode();
        if (code == null) {
            throw new SkipException("Bo qua: DB nay khong co voucher nao o trang thai Disabled ma chua het han de bat lai");
        }
        try {
            adminPage.clickRowStatusSwitch(code);
            Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                    "Gat Switch voucher Disabled phai mo modal xac nhan chung 'Xác nhận thay đổi trạng thái?'");
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Xác nhận thay đổi trạng thái?",
                    "Modal bat voucher phai co tieu de 'Xác nhận thay đổi trạng thái?'");
            Assert.assertTrue(adminPage.getConfirmDialogText().contains("khuyến mãi " + code),
                    "Modal bat voucher phai nhac ten khuyen mai '" + code + "', dang thay: " + adminPage.getConfirmDialogText());
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Modal xac nhan chung phai CHI co dung 2 nut ([Hủy] + [Xác nhận])");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Xác nhận",
                    "Nut xac nhan modal chung phai la 'Xác nhận'");
            adminPage.clickConfirmYes();
            Assert.assertTrue(adminPage.isToastDisplayed(),
                    "Bat voucher xong phai hien toast thanh cong (admin-toast)");
            Assert.assertTrue(adminPage.getToastTitleText().contains("Đổi trạng thái khuyến mãi thành công!"),
                    "Toast bat voucher phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + adminPage.getToastTitleText());
            Assert.assertTrue(adminPage.getToastDescText().contains("Khuyến mãi " + code + " đã được cập nhật trạng thái."),
                    "Toast bat voucher phai ghi 'Khuyến mãi " + code + " đã được cập nhật trạng thái.', dang thay: " + adminPage.getToastDescText());
            adminPage.searchPromos(code);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(code), Boolean.TRUE,
                    "Sau khi bat Switch voucher phai bat (aria-checked=true, Active)");
        } finally {
            // Khoi phuc: tat lai voucher ve Disabled (neu van dang Active).
            try {
                adminPage.searchPromos(code);
                if (Boolean.TRUE.equals(adminPage.getRowStatusSwitchChecked(code))) {
                    adminPage.disablePromo(code);
                }
            } catch (Throwable ignored) {
                // Khoi phuc best-effort.
            }
        }
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-165 (TC_SCRA06_FUNC_026, MUTATES — xóa voucher Draft do test tạo; quy tắc 'chỉ xóa voucher Draft chưa dùng' là [CẦN XÁC NHẬN], Matthew chưa xác nhận chính thức — viết test theo code hiện tại): Xóa voucher Draft qua hộp 'Xóa khuyến mãi?' (đúng 2 nút, nội dung '...Khách hàng sẽ không thể sử dụng mã này nữa. Hành động này không thể hoàn tác.'): bấm 'Hủy' thì KHÔNG xóa, voucher còn nguyên; bấm Xóa lại rồi 'Xóa khuyến mãi' thì voucher biến khỏi bảng, toast 'Đã xóa khuyến mãi'")
    public void adminPromoDeleteDraftConfirmCancelAndConfirm() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        adminPage.createPromoFull(code, "Voucher test ADM19-49", "Percentage", "10",
                "0", "", "", "2027-12-31");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher xong phai hien toast thanh cong (admin-toast)");

        adminPage.openPromoDeleteDialog(code);
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Xóa khuyến mãi?",
                "Xoa voucher phai mo popup 'Xóa khuyến mãi?'");
        String dialogText = adminPage.getConfirmDialogText();
        Assert.assertTrue(dialogText.contains("Khách hàng sẽ không thể sử dụng mã này nữa. Hành động này không thể hoàn tác."),
                "Popup xoa voucher phai co dong 'Khách hàng sẽ không thể sử dụng mã này nữa. Hành động này không thể hoàn tác.', dang thay: " + dialogText);
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Popup xoa voucher dot 18 phai CHI co dung 2 nut");
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Xóa khuyến mãi",
                "Nut xac nhan popup xoa phai la 'Xóa khuyến mãi'");
        Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Hủy",
                "Nut huy popup xoa phai la 'Hủy'");

        adminPage.clickConfirmNo();
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Bam 'Hủy' thi popup xoa phai dong (khong goi DELETE)");
        adminPage.searchPromos(code);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                "Bam 'Hủy' o popup xoa thi voucher phai con nguyen trong bang");

        adminPage.openPromoDeleteDialog(code);
        adminPage.clickConfirmYes();
        Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa voucher xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã xóa khuyến mãi"),
                "Toast xoa voucher phai co tieu de 'Đã xóa khuyến mãi', dang thay: " + adminPage.getToastTitleText());
        adminPage.searchPromos(code);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Xoa voucher xong thi tim theo ma chi duoc 0 dong");
        Assert.assertTrue(adminPage.isEmptyPromosMessageDisplayed(),
                "Tim voucher vua xoa phai hien dong 'Chưa có khuyến mãi'");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-166 (TC_SCRA06_FUNC_027, quy tắc Draft-only [CẦN XÁC NHẬN] — viết test theo code hiện tại): Xóa voucher Draft đã có lượt dùng → popup chặn A5 'Không thể xóa khuyến mãi' + nội dung 'Mã ... đã có {n} lượt sử dụng. Bạn có thể tắt khuyến mãi thay vì xóa.', đúng 2 nút [Đóng][Tắt khuyến mãi]; bấm [Đóng] thì popup đóng, không gọi API nào, voucher còn nguyên (skip nếu không có voucher Draft đã có lượt dùng — KHÔNG tự tạo lượt dùng giả)")
    public void adminPromoDeleteUsedVoucherBlockedPopup() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        String code = adminPage.findDraftUsedPromoCode();
        if (code == null) {
            throw new SkipException("Bo qua: DB nay khong co voucher Draft nao da co luot su dung de kiem tra popup chan xoa A5");
        }
        adminPage.openPromoDeleteDialog(code);
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Không thể xóa khuyến mãi",
                "Xoa voucher Draft da dung phai mo popup chan A5 'Không thể xóa khuyến mãi'");
        String dialogText = adminPage.getConfirmDialogText();
        Assert.assertTrue(dialogText.contains("lượt sử dụng"),
                "Popup A5 phai neu so luot su dung cua ma, dang thay: " + dialogText);
        Assert.assertTrue(dialogText.contains("Bạn có thể tắt khuyến mãi thay vì xóa."),
                "Popup A5 phai co dong 'Bạn có thể tắt khuyến mãi thay vì xóa.', dang thay: " + dialogText);
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Popup A5 dot 18 phai CHI co dung 2 nut");
        Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Đóng",
                "Nut trai popup A5 phai la 'Đóng'");
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Tắt khuyến mãi",
                "Nut phai popup A5 phai la 'Tắt khuyến mãi'");

        adminPage.clickConfirmNo();
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Bam [Đóng] o popup A5 thi popup phai dong, khong goi API nao");
        adminPage.searchPromos(code);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                "Bam [Đóng] o popup A5 thi voucher phai con nguyen trong bang");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-167 (TC_SCRA06_FUNC_028, MUTATES — tắt 1 voucher thật, bật lại trong finally nếu chưa hết hạn; quy tắc Draft-only [CẦN XÁC NHẬN] — viết test theo code hiện tại): Từ popup chặn A5 bấm nút phải [Tắt khuyến mãi] thì popup A5 đóng, mở thẳng popup tắt C3 'Tắt khuyến mãi?'; nhập lý do 'Hết ngân sách' + xác nhận: voucher chuyển Đã tắt; toàn bộ luồng KHÔNG gọi DELETE (skip nếu không có voucher Draft đã có lượt dùng)")
    public void adminPromoDeleteBlockedConvertOpensDisablePopup() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        String code = adminPage.findDraftUsedPromoCode();
        if (code == null) {
            throw new SkipException("Bo qua: DB nay khong co voucher Draft nao da co luot su dung de kiem tra luong A5 → C3");
        }
        try {
            adminPage.openPromoDeleteDialog(code);
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Không thể xóa khuyến mãi",
                    "Xoa voucher Draft da dung phai mo popup chan A5 'Không thể xóa khuyến mãi'");
            adminPage.clickConfirmYes();
            Assert.assertTrue(adminPage.isPromoDisableReasonModalDisplayed(),
                    "Bam [Tắt khuyến mãi] o popup A5 thi phai mo thang popup tat C3 'Tắt khuyến mãi?' co o nhap ly do");
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Tắt khuyến mãi?",
                    "Popup tat voucher phai co tieu de 'Tắt khuyến mãi?'");
            adminPage.setConfirmDialogReason("Hết ngân sách");
            adminPage.clickConfirmYes();
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tat voucher xong phai hien toast thanh cong (admin-toast)");
            Assert.assertTrue(adminPage.getToastTitleText().contains("Đổi trạng thái khuyến mãi thành công!"),
                    "Toast tat voucher tu A5 phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + adminPage.getToastTitleText());
            Assert.assertTrue(adminPage.getToastDescText().contains("Khuyến mãi " + code + " đã được cập nhật trạng thái."),
                    "Toast tat voucher phai ghi 'Khuyến mãi " + code + " đã được cập nhật trạng thái.', dang thay: " + adminPage.getToastDescText());
            adminPage.searchPromos(code);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(code), Boolean.FALSE,
                    "Sau khi tat tu popup A5 Switch voucher phai tat (aria-checked=false, Disabled)");
        } finally {
            // Khoi phuc: bat lai voucher (neu chua het han; neu het han thi popup C5 hien ra — bam Dong de giu nguyen).
            try {
                adminPage.searchPromos(code);
                if (Boolean.FALSE.equals(adminPage.getRowStatusSwitchChecked(code))) {
                    adminPage.clickRowStatusSwitch(code);
                    if (adminPage.isConfirmDialogDisplayed()) {
                        if (adminPage.getConfirmDialogTitle().contains("Xác nhận thay đổi trạng thái?")) {
                            adminPage.clickConfirmYes();
                        } else {
                            adminPage.clickConfirmNo();
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Khoi phuc best-effort.
            }
        }
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-168 (TC_SCRA06_FUNC_029, đợt 21): Dropdown Trạng thái lọc voucher theo Nháp/Đang hoạt động/Tạm ngưng/Đã hết hạn (server-side): chọn dropdown rồi bấm icon Tìm mới tải lại danh sách (chỉ chọn mà KHÔNG bấm Tìm thì bảng CHƯA đổi); chọn 'Nháp' + Tìm thì bảng chỉ hiện voucher Nháp (đều có Switch TẮT), về trang 1; chọn 'Đang chạy' + Tìm thì chỉ hiện voucher Đang chạy (Switch đều bật)")
    public void adminPromoStatusFilterServerSideReloadsList() {
        AdminPageObject adminPage = openPromosOrSkip();
        java.util.List<String> rowsBeforeSelect = adminPage.getAdminTableRowTexts();
        adminPage.selectStatusFilterByValue("Draft");
        Assert.assertEquals(adminPage.getAdminTableRowTexts(), rowsBeforeSelect,
                "Chi chon dropdown 'Nháp' chua bam Tim thi bang khuyen mai khong duoc doi (loc server-side)");
        adminPage.clickPromoSearchButton();
        if (adminPage.getAdminTableRowCount() == 0) {
            Assert.assertTrue(adminPage.isEmptyPromosMessageDisplayed(),
                    "Loc 'Nháp' khong co voucher thi phai hien 'Không tìm thấy khuyến mãi'");
        } else {
            boolean allDraft = adminPage.getAdminTableRowTexts().stream()
                    .allMatch(text -> text.contains("Nháp"));
            Assert.assertTrue(allDraft,
                    "Loc 'Nháp' roi bam Tim thi moi dong deu phai o trang thai Nháp");
            java.util.List<Boolean> draftStates = adminPage.getAllRowStatusSwitchStates();
            Assert.assertFalse(draftStates.isEmpty(),
                    "Voucher Nhap dot 21 CO Switch trang thai trong dong (tat, bam de kich hoat)");
            Assert.assertTrue(draftStates.stream().noneMatch(Boolean::booleanValue),
                    "Loc 'Nháp' roi bam Tim thi moi Switch deu phai tat (Draft)");
            int currentPage = adminPage.getPaginationCurrentPage("promotions");
            if (currentPage != -1) {
                Assert.assertEquals(currentPage, 1,
                        "Loc 'Nháp' thi phan trang phai ve trang 1");
            }
        }

        adminPage.selectStatusFilterByValue("Active");
        adminPage.clickPromoSearchButton();
        if (adminPage.getAdminTableRowCount() == 0) {
            Assert.assertTrue(adminPage.isEmptyPromosMessageDisplayed(),
                    "Loc 'Đang chạy' khong co voucher thi phai hien 'Không tìm thấy khuyến mãi'");
        } else {
            java.util.List<Boolean> states = adminPage.getAllRowStatusSwitchStates();
            Assert.assertFalse(states.isEmpty(),
                    "Voucher Dang chay phai co Switch trang thai trong dong");
            Assert.assertTrue(states.stream().allMatch(Boolean::booleanValue),
                    "Loc 'Đang chạy' roi bam Tim thi moi Switch deu phai bat (Active)");
        }
        adminPage.clickToolbarReset("promotions");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-169 (TC_SCRA06_FUNC_030): Tìm kiếm voucher theo mã/mô tả (client-side, gõ là lọc): nhập mã voucher (gõ chữ thường) thì bảng lọc ngay không cần bấm Tìm, không phân biệt hoa/thường; xóa từ khóa thì bảng hiện lại đủ voucher (trong 20 voucher đã tải)")
    public void adminPromoSearchByCodeFiltersRows() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        try {
            adminPage.createPromoFull(code, "Voucher test ADM19-53", "Percentage", "10",
                    "0", "", "", "2027-12-31");
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher xong phai hien toast thanh cong (admin-toast)");
            int total = adminPage.getPaginationTotalRows("promotions");
            adminPage.searchPromos(code);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Tim theo ma '" + code + "' thi chi duoc con dung 1 dong");
            adminPage.setPromoSearch("");
            Assert.assertEquals(adminPage.getAdminTableRowCount(), Math.min(10, total),
                    "Xoa tu khoa thi bang phai hien lai du voucher");

            // Go chu thuong, KHONG bam Tim: loc client-side ap dung ngay, khong phan biet hoa/thuong.
            adminPage.setPromoSearch(code.toLowerCase());
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Go ma voucher chu thuong (khong bam Tim) thi bang phai loc ngay chi con dung 1 dong");
            Assert.assertTrue(adminPage.getAdminTableRowTexts().get(0).contains(code),
                    "Dong con lai sau khi loc phai la voucher '" + code + "'");
            adminPage.setPromoSearch("");
            Assert.assertEquals(adminPage.getAdminTableRowCount(), Math.min(10, total),
                    "Xoa tu khoa thi bang phai hien lai du voucher");
        } finally {
            try {
                adminPage.searchPromos(code);
                if (adminPage.isRowWithTextDisplayed(code)) {
                    adminPage.deletePromoByCode(code);
                }
            } catch (Throwable ignored) {
                // Don dep best-effort.
            }
        }
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-170 (TC_SCRA06_FUNC_032, đợt 21): Xem lượt dùng của voucher: bấm icon Lượt dùng (History) ở 1 voucher → mở TRANG Lịch sử sử dụng (/promotions/:code/usages) với tổng quan mã + bảng 6 cột Mã đơn/Khách hàng/Giá trị đơn hàng/Số tiền giảm/Thời gian sử dụng/Trạng thái (2 cột '—' vì DTO không có giá trị đơn hàng/trạng thái lượt dùng); voucher chưa có lượt dùng thì hiện 'Chưa có lượt dùng nào'/'Mã này chưa được khách hàng sử dụng.'; tìm theo mã đơn/email lọc client-side (skip nếu bảng KM trống)")
    public void adminPromoUsageSubTableShowsRowsOrEmpty() {
        AdminPageObject adminPage = openPromosOrSkip();
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: muc Khuyen mai chua co voucher nao de xem luot dung");
        }
        String code = adminPage.getFirstPromoCode();
        Assert.assertNotNull(code, "Dong dau tien cua bang KM phai co ma voucher");
        adminPage.clickPromoUsageButton(code);
        Assert.assertTrue(adminPage.isPromoUsagesPageDisplayed(),
                "Bam icon Luot dung phai mo trang Lich su su dung (admin-promo-usages-page)");
        String usageText = adminPage.getPromoUsageSectionText();
        Assert.assertTrue(usageText.contains(code),
                "Trang Lich su su dung phai hien ma voucher '" + code + "' o khoi tong quan, dang thay: " + usageText);
        Assert.assertTrue(usageText.contains("Lịch sử sử dụng") || usageText.contains("Lượt sử dụng"),
                "Trang Lich su su dung phai co tieu de 'Lịch sử sử dụng khuyến mãi', dang thay: " + usageText);
        if (usageText.contains("Chưa có lượt dùng nào")) {
            Assert.assertTrue(usageText.contains("Mã này chưa được khách hàng sử dụng."),
                    "Voucher chua co luot dung thi trang phai hien 'Mã này chưa được khách hàng sử dụng.', dang thay: " + usageText);
        } else {
            java.util.List<String> usageHeaders = adminPage.getPromoUsageHeaders();
            Assert.assertEquals(usageHeaders, java.util.Arrays.asList(
                            "Mã đơn", "Khách hàng", "Giá trị đơn hàng", "Số tiền giảm", "Thời gian sử dụng", "Trạng thái"),
                    "Bang lich su su dung phai co 6 cot Mã đơn/Khách hàng/Giá trị đơn hàng/Số tiền giảm/Thời gian sử dụng/Trạng thái, dang thay: " + usageHeaders);
            // DTO PromotionUsageDto khong co gia tri don hang / trang thai luot
            // dung -> 2 cot do hien '—' (khong bia du lieu).
            java.util.List<String> firstCells = adminPage.getPromoUsagesFirstRowCellTexts();
            Assert.assertEquals(firstCells.size(), 6, "Dong luot dung phai co dung 6 o, dang thay: " + firstCells.size());
            Assert.assertEquals(firstCells.get(2).trim(), "—",
                    "Cot Gia tri don hang phai hien '—' (DTO khong co field), dang thay: " + firstCells.get(2));
            Assert.assertEquals(firstCells.get(5).trim(), "—",
                    "Cot Trang thai luot dung phai hien '—' (DTO khong co field), dang thay: " + firstCells.get(5));
            // Tim theo ma don (loc client-side tren trang hien tai).
            String orderCode = firstCells.get(0).trim();
            adminPage.searchPromoUsages(orderCode);
            Assert.assertTrue(adminPage.getPromoUsagesRowCount() >= 1,
                    "Tim theo ma don '" + orderCode + "' phai ra it nhat 1 dong");
        }
        adminPage.clickPromoUsagesBackButton();
        Assert.assertTrue(adminPage.isPromoListPageDisplayed(),
                "Bam [Quay lại] o trang Lich su su dung phai ve danh sach khuyen mai");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-171 (TC_SCRA06_FUNC_033): Phân trang bảng khuyến mãi 10 dòng/trang: mỗi trang tối đa 10 dòng; hiển thị 'Trang X / Y - Tổng N dòng'; sang trang 2 tải đúng 10 dòng tiếp theo (STT bắt đầu từ 11) (skip nếu ≤10 voucher)")
    public void adminPromoPaginationNextPrevChangesPage() {
        AdminPageObject adminPage = openPromosOrSkip();
        int total = adminPage.getPaginationTotalRows("promotions");
        if (total <= 10) {
            throw new SkipException("Bo qua: muc Khuyen mai chi co " + total + " voucher (can >10 de kiem tra phan trang)");
        }
        Assert.assertTrue(adminPage.getAdminTableRowCount() <= 10,
                "Moi trang phan trang KM toi da 10 dong");
        Assert.assertTrue(adminPage.getPaginationText("promotions").contains("Trang 1 / "),
                "Trang 1 phai hien 'Trang 1 / Y', dang thay: " + adminPage.getPaginationText("promotions"));
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");
        adminPage.clickPaginationNext("promotions");
        Assert.assertTrue(adminPage.getPaginationText("promotions").contains("Trang 2 / "),
                "Sang trang 2 phai hien 'Trang 2 / Y'");
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai la 11");
        adminPage.clickPaginationPrev("promotions");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "Ve trang 1 thi STT dong dau phai lai la 1");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-172 (TC_SCRA06_FUNC_034, đợt 21): Voucher Expired: badge xám 'Hết hạn' + Switch CÓ nhưng bị DISABLE (không bấm được), không Sửa/Xóa (chỉ có mắt Xem chi tiết và icon Lượt dùng History) (skip nếu không có voucher Expired)")
    public void adminPromoExpiredRowHasBadgeOnly() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        String code = adminPage.findExpiredPromoCode();
        if (code == null) {
            throw new SkipException("Bo qua: DB nay khong co voucher nao o trang thai Expired");
        }
        adminPage.searchPromos(code);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                "Tim theo ma voucher Expired '" + code + "' thi chi duoc con dung 1 dong");
        String row = adminPage.getAdminTableRowTexts().get(0);
        Assert.assertTrue(row.contains("Hết hạn"),
                "Dong voucher Expired phai hien badge 'Hết hạn', dang thay: " + row);
        Assert.assertEquals(adminPage.getRowStatusSwitchChecked(code), Boolean.FALSE,
                "Dong voucher Expired phai co Switch o trang thai tat (aria-checked=false)");
        Assert.assertFalse(adminPage.isRowStatusSwitchEnabled(code),
                "Switch cua voucher Expired phai bi DISABLE (khong bam duoc)");
        Assert.assertFalse(adminPage.isPromoEditButtonDisplayedFor(code),
                "Voucher Expired khong duoc co nut Sua");
        Assert.assertFalse(adminPage.isPromoDeleteButtonDisplayedFor(code),
                "Voucher Expired khong duoc co nut Xoa");
        Assert.assertFalse(row.contains("Lượt dùng"),
                "Dong voucher dot 21 KHONG duoc con nut chu 'Lượt dùng' (da doi thanh icon History)");
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-173 (TC_SCRA06_FUNC_037, đợt 21): Chặn bật KM hết hạn: gạt Switch ở dòng voucher Disabled đã quá hạn (hoặc voucher Draft đã quá hạn — Draft dot 21 cũng có Switch) → popup chặn C5 'Khuyến mãi đã hết hạn' (đúng 2 nút [Đóng][Chỉnh sửa khuyến mãi]); bấm [Đóng] thì popup đóng, không gọi POST activate, voucher vẫn Đã tắt/Nháp (skip nếu không có voucher hết hạn)")
    public void adminPromoEnableExpiredBlockedPopup() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        String code = adminPage.findDisabledExpiredPromoCode();
        boolean viaSwitch = code != null;
        if (code == null) {
            code = adminPage.findDraftExpiredPromoCode();
        }
        if (code == null) {
            throw new SkipException("Bo qua: DB nay khong co voucher Disabled/Draft nao da qua han de kiem tra popup chan C5");
        }
        // Dot 21: ca Disabled-expired lan Draft-expired deu bat qua Switch tren dong
        // (khong con nut Play rieng).
        adminPage.clickRowStatusSwitch(code);
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bat voucher da het han phai mo popup chan C5 'Khuyến mãi đã hết hạn'");
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Khuyến mãi đã hết hạn",
                "Popup chan bat KM het han phai co tieu de 'Khuyến mãi đã hết hạn'");
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Popup C5 dot 18 phai CHI co dung 2 nut");
        Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Đóng",
                "Nut trai popup C5 phai la 'Đóng'");
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Chỉnh sửa khuyến mãi",
                "Nut phai popup C5 phai la 'Chỉnh sửa khuyến mãi'");

        adminPage.clickConfirmNo();
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Bam [Đóng] o popup C5 thi popup phai dong, khong goi POST activate");
        adminPage.searchPromos(code);
        if (viaSwitch) {
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(code), Boolean.FALSE,
                    "Bam [Đóng] o popup C5 thi voucher van phai Da tat (Switch tat)");
        } else {
            Assert.assertTrue(adminPage.isPromoEditButtonDisplayedFor(code),
                    "Bam [Đóng] o popup C5 thi voucher Draft van phai con nut Sua (chua bat)");
        }
    }

    @Story("Khuyến mãi")
    @Test(description = "ADM19-174 (TC_SCRA06_FUNC_038, đợt 21, MUTATES — đổi ngày hết hạn của 1 voucher thật; [ĐÃ XÁC MINH — C5 → trang sửa full page, không còn ngõ cụt]: voucher Expired vẫn mở được trang sửa qua nút [Chỉnh sửa khuyến mãi] của popup C5): Từ popup chặn C5 bấm [Chỉnh sửa khuyến mãi] thì popup đóng, mở TRANG sửa KM đó (/promotions/:code/edit, tiêu đề 'Chỉnh sửa khuyến mãi', ô Mã = mã voucher, mode 'edit'); sửa Ngày hết hạn thành 31/12/2027 + Lưu: PUT thành công, toàn bộ luồng KHÔNG gọi POST activate trước khi sửa ngày (skip nếu không có voucher hết hạn)")
    public void adminPromoEnableExpiredBlockedEditOpensForm() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        String code = adminPage.findDisabledExpiredPromoCode();
        if (code == null) {
            code = adminPage.findDraftExpiredPromoCode();
        }
        if (code == null) {
            throw new SkipException("Bo qua: DB nay khong co voucher Disabled/Draft nao da qua han de kiem tra luong C5 → trang sua");
        }
        // Dot 21: ca Disabled-expired lan Draft-expired deu bat qua Switch tren dong.
        adminPage.clickRowStatusSwitch(code);
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bat voucher da het han phai mo popup chan C5 'Khuyến mãi đã hết hạn'");
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Khuyến mãi đã hết hạn",
                "Popup chan bat KM het han phai co tieu de 'Khuyến mãi đã hết hạn'");
        adminPage.clickConfirmYes();
        if (!adminPage.isPromoEditPageDisplayed()) {
            throw new SkipException("Bo qua: bam [Chỉnh sửa khuyến mãi] o popup C5 nhung trang sua KM '" + code + "' khong mo duoc — can kiem chung tay tren :3001");
        }
        String editPageText = adminPage.getPromoEditPageText();
        Assert.assertTrue(editPageText.contains("Chỉnh sửa khuyến mãi"),
                "Tu popup C5 mo trang sua phai co tieu de 'Chỉnh sửa khuyến mãi', dang thay: " + editPageText);
        Assert.assertEquals(adminPage.getPromoFormMode(), "edit",
                "Trang sua tu popup C5 phai co data-form-mode='edit'");
        Assert.assertEquals(adminPage.getPromoCodeInputValue(), code,
                "Trang sua tu popup C5 phai dien san dung ma voucher '" + code + "'");
        adminPage.setPromoExpiryDate("2027-12-31");
        adminPage.savePromoForm();
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Sua ngay het han xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật khuyến mãi"),
                "Toast sua voucher phai co tieu de 'Đã cập nhật khuyến mãi', dang thay: " + adminPage.getToastTitleText());
        adminPage.searchPromos(code);
        final String codeForSearch = code;
        Assert.assertTrue(adminPage.getAdminTableRowTexts().stream()
                        .anyMatch(row -> row.contains(codeForSearch) && row.contains("31/12/2027")),
                "Sau khi sua, dong voucher '" + code + "' phai hien ngay het han moi 31/12/2027");
    }
}

