package vn.dafl.tests.admin.orders;

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
 * Test don hang + tra hang tren app Admin rieng localhost:3001 (tach tu
 * AdminCrudTest / AdminAndNewsletterTest / AdminDetailTest / AdminWave13Test cu
 * khi chia thu muc admin dot 17 — moi lop chi lo 1 phan he).
 * Don hang (ACR-06/07): KHONG dat don moi qua web ban hang nua ma doi trang
 * thai / huy 1 don 'Chờ xác nhận' CO SAN trong DB (theo yeu cau bo web ban hang
 * khoi test admin). Don do la du lieu that, khong phai do test tao, va trang
 * thai cu khong khoi phuc lai duoc sau test; neu DB khong co don cho xu ly thi
 * test tu bo qua (skip, khong tinh fail). Chi 1 don ma chay ACR-06 va ACR-07
 * lien tiep thi test sau se skip (don dau da doi trang thai).
 * Dot 21 — danh sach don loc CLIENT-SIDE tren toi da 100 don da tai: dropdown
 * 6 nhom ("Trạng thái" + Chờ xác nhận/Đã xác nhận/Đang giao/Hoàn thành/Đã hủy/
 * Hết hạn thanh toán), go tu khoa loc ngay sau debounce 120ms (khong dau,
 * khong con nut 'Tìm' de bam), nut 'Đặt lại bộ lọc' dang nut chu (disable khi
 * chua loc), so ket qua "N / M đơn", EmptyState co nut "Xóa bộ lọc"; bam dong
 * mo trang chi tiet dang route /orders/:code (test o muc navigate URL, noi
 * dung trang chi tiet do worker chi tiet lo). Tim/loc chi phu toi da 100 don
 * da tai — don cu hon khong tim duoc neu khong co ho tro unaccent o BE.
 * Tra hang (dot 21): chi tiet la TRANG full page /returns/:code (khong con
 * Drawer/Modal); duyet/tu choi qua PANEL inline (van ban E1/E2 tai dung),
 * ADM19-153/154 MUTATES 1 yeu cau that (khong khoi phuc duoc).
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý đơn hàng và trả hàng")
public class AdminOrderTest extends BaseTest {

    /** Dang nhap Super Admin tren app Admin rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Lay ma don 'Chờ xác nhận' dau tien trong bang Don hang (dot 21: loc
     *  client-side theo NHOM trang thai; nhan hien thi cua Pending (COD) va
     *  Paid o admin deu la 'Chờ xác nhận', Pending (PayOS) la 'Chờ thanh
     *  toán'; khac nhan 'Chờ xử lý' cu cua FE). Huy duoc tu Pending / Paid /
     *  Confirmed / Preparing (AdminCanCancel) nen don Paid trong nhom nay van
     *  dung duoc cho ACR-06/07; null neu khong co don nao o trang thai nay. */
    private String getFirstPendingOrderCodeOrNull(AdminPageObject adminPage) {
        adminPage.clickAdminNav("orders");
        adminPage.selectOrderStatusFilter(GlobalConstants.ORDER_STATUS_PENDING);
        if (adminPage.getAdminTableRowCount() == 0) {
            return null;
        }
        return extractOrderDisplayCode(adminPage.getAdminTableRowTexts().get(0));
    }

    /** Mo /admin va dung o muc cho truoc; tra ve AdminPageObject da san sang. */
    private AdminPageObject openAdminTab(String navKey) {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav(navKey);
        return adminPage;
    }

    // ---------- Don hang: doi/huy 1 don 'Chờ xác nhận' CO SAN trong DB (ACR-06/07, dot 10) ----------

    @Story("Đơn hàng")
    @Test(description = "ACR-06: Đổi trạng thái 1 đơn 'Chờ xác nhận' có sẵn sang 'Đã xác nhận' trên Admin (skip nếu không có đơn chờ xác nhận)")
    public void adminChangePendingOrderStatusToConfirmed() {
        // LUU Y (dot 10): test nay KHONG dat don
        // moi qua web ban hang nua ma doi trang thai 1 don 'Chờ xác nhận' CO SAN trong DB.
        // Don do la du lieu that (khong phai do test tao) va trang thai cu khong khoi
        // phuc lai duoc sau test. Neu DB khong co don cho xu ly thi test tu bo qua.
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xác nhận' de doi trang thai");
        }

        adminPage.searchOrderByCodeAndOpen(orderCode);
        adminPage.changeOrderStatusTo("Đã xác nhận");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Doi trang thai don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật đơn hàng"),
                "Toast doi trang thai don phai co tieu de 'Đã cập nhật đơn hàng', dang thay: " + adminPage.getToastTitleText());

        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã xác nhận"),
                "Don " + orderCode + " sau khi doi trang thai phai hien 'Đã xác nhận' trong bang don");
    }

    @Story("Đơn hàng")
    @Test(description = "ACR-07: Hủy 1 đơn 'Chờ xác nhận' có sẵn trên Admin, bắt buộc có lý do (skip nếu không có đơn chờ xác nhận)")
    public void adminCancelPendingOrder() {
        // LUU Y (dot 10, giong ACR-06): huy 1 don 'Chờ xác nhận' CO SAN trong DB, khong
        // phai don do test tao; trang thai cu khong khoi phuc lai duoc sau test.
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xác nhận' de huy");
        }

        adminPage.searchOrderByCodeAndOpen(orderCode);
        adminPage.cancelOrderWithReason("Hủy bởi test ACR-07 (đơn Chờ xác nhận có sẵn)");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Huy don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã hủy đơn hàng"),
                "Toast huy don phai co tieu de 'Đã hủy đơn hàng', dang thay: " + adminPage.getToastTitleText());

        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã hủy"),
                "Don " + orderCode + " sau khi huy phai hien 'Đã hủy' trong bang don");
    }

    // ---------- Don hang: loc / Export (ADM-09/10, dot 8, chi doc) ----------

    @Story("Đơn hàng")
    @Test(description = "ADM-09 (đợt 21, viết lại — lọc client-side): Lọc đơn hàng theo trạng thái 'Đã hủy' → chỉ thấy đơn đã hủy hoặc báo rỗng; đổi dropdown là lọc ngay, không cần bấm nút")
    public void adminOrderStatusFilterShowsOnlyCancelled() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("orders");

        // Dot 21: chon dropdown la loc ngay (client-side), khong con nut Tim de bam.
        adminPage.selectOrderStatusFilter(GlobalConstants.ORDER_STATUS_CANCELLED);

        if (adminPage.getAdminTableRowCount() == 0) {
            Assert.assertTrue(adminPage.isEmptyOrdersMessageDisplayed(),
                    "Loc 'Đã hủy' khong co don thi phai hien dong 'Không tìm thấy đơn hàng'");
        } else {
            boolean allCancelled = adminPage.getAdminTableRowTexts().stream()
                    .allMatch(text -> text.contains(GlobalConstants.ORDER_STATUS_CANCELLED));
            Assert.assertTrue(allCancelled,
                    "Loc 'Đã hủy' thi moi dong deu phai o trang thai 'Đã hủy'");
        }
        adminPage.clickToolbarReset("orders");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM-10 (đợt 21, viết lại — Export đã bỏ hẳn khỏi mọi toolbar admin; nút Tìm đã bỏ khỏi toolbar Đơn hàng): Mục Đơn hàng KHÔNG còn nút Export, KHÔNG còn nút Tìm; toolbar có ô tìm kiếm + nút chữ 'Đặt lại bộ lọc' (disable khi chưa lọc) + số kết quả 'N / M đơn'")
    public void adminOrdersToolbarHasNoExportButton() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("orders");

        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Don hang khong duoc co nut Export");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Toolbar Don hang phai co o tim kiem (orders-search-input)");
        Assert.assertFalse(adminPage.isToolbarSearchButtonDisplayed("orders"),
                "Dot 21 da bo nut Tim khoi toolbar Don hang (loc ap dung ngay khi go/doi dropdown)");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("orders"),
                "Toolbar Don hang phai co nut Dat lai (orders-reset-btn)");
        Assert.assertEquals(adminPage.getOrderResetButtonText(), "Đặt lại bộ lọc",
                "Dot 21 nut Dat lai don hang la nut chu 'Đặt lại bộ lọc', dang thay: " + adminPage.getOrderResetButtonText());
        Assert.assertFalse(adminPage.isOrderResetButtonEnabled(),
                "Chua co tu khoa/trang thai thi nut 'Đặt lại bộ lọc' phai bi disable");
        Assert.assertFalse(adminPage.isOrderSearchClearButtonDisplayed(),
                "Chua go tu khoa thi khong duoc co nut '×' xoa nhanh trong o tim");
        String resultCount = adminPage.getOrderResultCountText();
        Assert.assertTrue(resultCount.matches("\\d+ / \\d+ đơn"),
                "Toolbar Don hang phai hien so ket qua dang 'N / M đơn', dang thay: '" + resultCount + "'");
    }

    // ---------- Tra hang: ghi nhan hoan tien (ADM-W13-03, dot 13, chi mo modal xem) ----------

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

    // ---------- Chi tiet don / tra hang (ADT-05/07, dot 8, chi doc) ----------

    @Story("Đơn hàng")
    @Test(description = "ADT-05 (đợt 21, viết lại theo trang chi tiết riêng /orders/:code): Mở trang chi tiết đơn hàng bằng nút mắt → hiện mã đơn + pill trạng thái, header LUÔN đúng 2 nút [Hủy đơn]+[Đổi trạng thái] (disable khi đơn không cho phép, không còn nút [Đóng] ở footer); về danh sách bằng breadcrumb (skip nếu DB chưa có đơn)")
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
                "Phai mo duoc trang chi tiet don hang");
        Assert.assertTrue(adminPage.getOrderDetailTitleText().startsWith("Đơn " + orderLabel),
                "Tieu de trang chi tiet phai bat dau bang 'Đơn " + orderLabel + "'");
        Assert.assertFalse(adminPage.getOrderDetailStatusPillText().trim().isEmpty(),
                "Trang chi tiet phai hien pill trang thai don");

        // Dot 21: header trang chi tiet LUON co dung 2 nut [Hủy đơn] +
        // [Đổi trạng thái] (disable + tooltip khi don khong cho phep, khong
        // con nut [Đóng] o footer nhu Drawer dot 18).
        java.util.List<String> headerTexts = adminPage.getOrderDetailHeaderButtonTexts();
        Assert.assertEquals(headerTexts.size(), 2,
                "Header chi tiet don dot 21 phai CHI co dung 2 nut, dang thay: " + headerTexts);
        Assert.assertTrue(headerTexts.contains("Hủy đơn") && headerTexts.contains("Đổi trạng thái"),
                "Header chi tiet don phai la [Hủy đơn] + [Đổi trạng thái], dang thay: " + headerTexts);

        // Ve danh sach bang breadcrumb [← Đơn hàng].
        adminPage.closeOrderDetail();
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Ve danh sach xong khong duoc con thay trang chi tiet don hang");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Ve danh sach xong phai thay lai bang don hang");
    }

    @Test(description = "ADT-07 (đợt 18 + đợt 21, viết lại theo TRANG full page): Mở trang chi tiết yêu cầu trả hàng bằng nút 'Duyệt / Từ chối' → hiện mã đơn, header CHỈ đúng 2 nút [Từ chối]+[Duyệt] (không nút Hủy — về danh sách bằng breadcrumb [Trả hàng]), đóng được (skip nếu chưa có yêu cầu trả hàng)")
    public void returnDetailModalShowsOrderCode() {
        AdminPageObject adminPage = openAdminTab("returns");
        if (adminPage.isEmptyReturnsMessageDisplayed()) {
            throw new SkipException("Bo qua: DB nay chua co yeu cau tra hang nao de mo chi tiet");
        }
        if (!adminPage.isElementDisplayedWithWait(AdminPageUI.FIRST_RETURN_REVIEW_BTN, 10)) {
            throw new SkipException("Bo qua: muc Tra hang khong co nut 'Duyệt / Từ chối' de mo chi tiet");
        }

        adminPage.clickFirstReturnReview();
        Assert.assertTrue(adminPage.isReturnDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet yeu cau tra hang full page (admin-return-detail-page)");
        String pageText = adminPage.getReturnDetailText();
        Assert.assertTrue(pageText.contains("DH") || pageText.contains("Trả hàng"),
                "Trang chi tiet tra hang phai hien ma don hoac tieu de tra hang, dang thay: " + pageText);

        // Dot 21: trang chi tiet tra hang KHONG co nut Huy — header chi dung 2
        // nut [Từ chối] + [Duyệt] (ve danh sach bang breadcrumb [Trả hàng]).
        java.util.List<String> headerTexts = adminPage.getPageHeadButtonTexts("admin-return-detail-page");
        Assert.assertEquals(headerTexts.size(), 2,
                "Header chi tiet tra hang dot 21 phai CHI co dung 2 nut, dang thay: " + headerTexts);
        Assert.assertTrue(headerTexts.contains("Từ chối") && headerTexts.contains("Duyệt"),
                "Header chi tiet tra hang phai la [Từ chối] + [Duyệt], dang thay: " + headerTexts);

        // Chi ve danh sach, KHONG bam Duyet/Tu choi (khong doi trang thai tra hang).
        adminPage.closeReturnDetailPage();
        Assert.assertFalse(adminPage.isReturnDetailPageDisplayed(),
                "Ve danh sach xong khong duoc con thay trang chi tiet tra hang");
    }

    // ---------- Wave 19 (mapping SCR_03 / SCR_10): helper dung chung ----------

    /** 10 nhan trang thai don HIEN THI trong bang (nhan BE OrderStatusLabels.For,
     *  dot 21): Pending(COD)/Paid = 'Chờ xác nhận', Pending(PayOS) = 'Chờ thanh
     *  toán', Completed = 'Hoàn tất', Expired = 'Hết hạn thanh toán'. */
    private static final String[] ORDER_STATUS_LABELS_BE = {
            "Chờ xác nhận", "Chờ thanh toán", "Đã xác nhận", "Đang chuẩn bị",
            "Đang giao", "Đã giao", "Hoàn tất", "Yêu cầu trả hàng", "Đã hủy", "Hết hạn thanh toán"};

    /** Dot 21 — nhan trang thai CU (FE, dung trong test cu) -> nhan NHOM loc moi
     *  o dropdown don; null = khong co nhom (tim bang cach quet bang khong loc).
     *  "Chờ xử lý" (Pending) va "Đã thanh toán" (Paid) cung ve nhom "Chờ xác nhận";
     *  "Hoàn tất" (Completed) ve nhom "Hoàn thành"; "Đã hết hạn" (Expired) ve
     *  nhom "Hết hạn thanh toán". "Đang chuẩn bị"/"Đã giao" khong co nhom. */
    private static String toOrderFilterGroupLabelOrNull(String statusLabel) {
        switch (statusLabel) {
            case "Chờ xử lý":
            case "Đã thanh toán":
                return GlobalConstants.ORDER_STATUS_PENDING; // "Chờ xác nhận"
            case "Đã xác nhận":
                return "Đã xác nhận";
            case "Đang giao":
                return "Đang giao";
            case "Hoàn tất":
                return "Hoàn thành";
            case "Đã hủy":
                return GlobalConstants.ORDER_STATUS_CANCELLED; // "Đã hủy"
            case "Đã hết hạn":
                return "Hết hạn thanh toán";
            default:
                return null;
        }
    }

    /** Ma don dau tien o trang thai cho truoc (dot 21: loc client-side theo nhom
     *  o dropdown, doi la loc ngay, khong con nut Tim); null neu khong co.
     *  Ham nay de lai bo loc o trang thai do — test goi ham nay phai
     *  clickToolbarReset("orders") truoc khi tim don theo ma o trang thai khac.
     *  LUU Y: nhom "Chờ xác nhận" gop Pending + Paid — neu can dung Pending
     *  that (vd doi Pending -> Paid) thi dong dau tien trong nhom co the la
     *  Paid; test doi trang thai phai tu SkipException khi dich khong hop le. */
    private String getFirstOrderCodeByStatusOrNull(AdminPageObject adminPage, String statusLabel) {
        adminPage.clickAdminNav("orders");
        String filterLabel = toOrderFilterGroupLabelOrNull(statusLabel);
        if (filterLabel == null) {
            // Trang thai khong co nhom loc (vd 'Đang chuẩn bị', 'Đã giao'):
            // quet bang khong loc tim nhan BE.
            return findOrderCodeByStatusInUnfilteredTableOrNull(adminPage, statusLabel);
        }
        adminPage.selectOrderStatusFilter(filterLabel);
        if (adminPage.getAdminTableRowCount() == 0) {
            return null;
        }
        return extractOrderDisplayCode(adminPage.getAdminTableRowTexts().get(0));
    }

    /** Mo trang chi tiet cua don dau tien o trang thai cho truoc; tra ve ma don, null neu khong co. */
    private String openFirstOrderWithStatusOrNull(AdminPageObject adminPage, String statusLabel) {
        String orderCode = getFirstOrderCodeByStatusOrNull(adminPage, statusLabel);
        if (orderCode == null) {
            return null;
        }
        adminPage.clickToolbarReset("orders");
        adminPage.searchOrderByCodeAndOpen(orderCode);
        return orderCode;
    }

    /** Tim ma don dau tien co nhan trang thai cho truoc trong bang KHONG loc (quet trang 1 roi trang 2);
     *  dung cho trang thai khong co trong dropdown loc (vd 'Yêu cầu trả hàng'). Null neu khong co. */
    private String findOrderCodeByStatusInUnfilteredTableOrNull(AdminPageObject adminPage, String statusLabel) {
        adminPage.clickAdminNav("orders");
        adminPage.clickToolbarReset("orders");
        for (int page = 1; page <= 2; page++) {
            for (String row : adminPage.getAdminTableRowTexts()) {
                if (row.contains(statusLabel)) {
                    String code = extractOrderDisplayCode(row);
                    if (code != null) {
                        return code;
                    }
                }
            }
            if (adminPage.isPaginationNextEnabled("orders")) {
                adminPage.clickPaginationNext("orders");
            } else {
                break;
            }
        }
        return null;
    }

    /** Doi trang thai 1 don that (MUTATES, khong khoi phuc duoc): mo don dau tien o trang thai nguon,
     *  doi sang trang thai dich qua panel doi trang thai (dot 21, khong con popup D2/D3),
     *  khang dinh toast + tieu de/pill trang chi tiet + dong trong bang.
     *  verifyInTable=false khi trang thai dich khong co trong dropdown loc bang (vd 'Yêu cầu trả hàng').
     *  Test goi ham nay phai tu SkipException khi khong co don nguon (ham tu nem SkipException). */
    private String changeFirstOrderStatus(AdminPageObject adminPage, String sourceLabel, String targetLabel,
                                          boolean verifyInTable) {
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, sourceLabel);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai '" + sourceLabel
                    + "' de doi sang '" + targetLabel + "'");
        }
        adminPage.changeOrderStatusTo(targetLabel);
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Doi trang thai don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật đơn hàng"),
                "Toast doi trang thai don phai co tieu de 'Đã cập nhật đơn hàng', dang thay: " + adminPage.getToastTitleText());
        Assert.assertTrue(adminPage.getToastDescText().contains("Đơn " + orderCode + " chuyển sang '" + targetLabel + "'."),
                "Toast doi trang thai phai ghi \"Đơn " + orderCode + " chuyển sang '" + targetLabel + "'.\", dang thay: " + adminPage.getToastDescText());
        // Trang chi tiet van mo sau khi doi (changeOrderStatus tai lai chi tiet) —
        // tieu de van la 'Đơn <mã>', pill trang thai phai la trang thai moi.
        Assert.assertTrue(adminPage.getOrderDetailTitleText().startsWith("Đơn " + orderCode),
                "Tieu de trang chi tiet sau khi doi phai bat dau bang 'Đơn " + orderCode + "'");
        Assert.assertEquals(adminPage.getOrderDetailStatusPillText(), targetLabel,
                "Pill trang thai sau khi doi phai la '" + targetLabel + "'");
        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        if (verifyInTable) {
            Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, targetLabel),
                    "Don " + orderCode + " sau khi doi phai hien '" + targetLabel + "' trong bang don");
        }
        return orderCode;
    }

    /** Ma don cua yeu cau tra hang 'Chờ duyệt' dau tien trong bang Tra hang (quet trang 1); null neu khong co. */
    private String getFirstPendingReturnOrderCodeOrNull(AdminPageObject adminPage) {
        adminPage.clickAdminNav("returns");
        if (adminPage.isEmptyReturnsMessageDisplayed()) {
            return null;
        }
        for (String row : adminPage.getAdminTableRowTexts()) {
            if (row.contains("Chờ duyệt")) {
                String code = extractOrderDisplayCode(row);
                if (code != null) {
                    return code;
                }
            }
        }
        return null;
    }

    // ---------- Don hang: toolbar / bang / loc / tim kiem (SCR_03 UI + FUNC, chi doc) ----------

    @Story("Đơn hàng")
    @Test(description = "ADM19-117 (TC_SCRA03_UI_001, đợt 21 viết lại): Toolbar mục Đơn hàng: KHÔNG có nút Thêm mới, KHÔNG còn nút Tìm (svg.lucide-search) và KHÔNG còn nút Đặt lại icon (svg.lucide-rotate-ccw); ô tìm kiếm có placeholder 'Tìm mã đơn, tên khách, email...'; nút Đặt lại là nút chữ 'Đặt lại bộ lọc'")
    public void adminOrdersToolbarHasNoAddButton() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("orders");

        Assert.assertFalse(adminPage.isToolbarAddButtonDisplayed("orders"),
                "Dot 18 muc Don hang khong duoc co nut Them moi (chi SP/DM/KM moi co)");
        Assert.assertEquals(adminPage.getOrderSearchPlaceholder(), "Tìm mã đơn, tên khách, email...",
                "O tim kiem Don hang phai co placeholder 'Tìm mã đơn, tên khách, email...'");
        Assert.assertFalse(adminPage.isToolbarSearchButtonDisplayed("orders"),
                "Dot 21 toolbar Don hang khong duoc co nut Tim (orders-search-btn)");
        Assert.assertFalse(adminPage.isToolbarSearchIconDisplayed("orders"),
                "Dot 21 toolbar Don hang khong duoc co icon Lucide Search cua nut Tim");
        Assert.assertFalse(adminPage.isToolbarResetIconDisplayed("orders"),
                "Dot 21 nut Dat lai don hang la nut chu, khong duoc co icon Lucide RotateCcw");
        Assert.assertEquals(adminPage.getOrderResetButtonText(), "Đặt lại bộ lọc",
                "Dot 21 nut Dat lai don hang phai la nut chu 'Đặt lại bộ lọc', dang thay: " + adminPage.getOrderResetButtonText());
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Don hang khong duoc co nut Export");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-118 (TC_SCRA03_UI_002): Bảng đơn hàng có đủ 8 cột đúng thứ tự STT/Mã đơn/Khách hàng/Trạng thái/Thanh toán/Tổng tiền/Ngày đặt/Thao tác; dòng đầu có mã đơn, avatar + tên + email khách, pill trạng thái, tiền dạng '...₫', ngày đặt 2 dòng, cột Thao tác chỉ 1 nút mắt (skip nếu DB chưa có đơn)")
    public void adminOrderTableShowsExpectedColumnsAndRowContent() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra bo cuc bang");
        }

        java.util.List<String> headers = adminPage.getAdminTableHeaders();
        Assert.assertEquals(headers, java.util.Arrays.asList(
                        "STT", "Mã đơn", "Khách hàng", "Trạng thái", "Thanh toán", "Tổng tiền", "Ngày đặt", "Thao tác"),
                "Tieu de cot bang Don hang phai dung thu tu STT/Mã đơn/Khách hàng/Trạng thái/Thanh toán/Tổng tiền/Ngày đặt/Thao tác, dang thay: " + headers);

        java.util.List<String> firstCells = adminPage.getFirstTableRowCellTexts();
        Assert.assertEquals(firstCells.size(), 8, "Dong don hang phai co dung 8 o, dang thay: " + firstCells.size());
        Assert.assertTrue(firstCells.get(1).matches("DH\\d{8}-\\d{4}"),
                "O Ma don phai la ma dang DH{yyyyMMdd}-{NNNN}, dang thay: " + firstCells.get(1));
        Assert.assertTrue(firstCells.get(2).contains("@"),
                "O Khach hang phai co ten + email (2 dong), dang thay: " + firstCells.get(2));
        java.util.List<String> allLabels = new java.util.ArrayList<>(java.util.Arrays.asList(ORDER_STATUS_LABELS_BE));
        Assert.assertTrue(allLabels.contains(firstCells.get(3).trim()),
                "O Trang thai phai la 1 trong 10 nhan BE (Chờ xác nhận/Chờ thanh toán/Đã xác nhận/...), dang thay: " + firstCells.get(3));
        Assert.assertFalse(firstCells.get(4).trim().isEmpty(),
                "O Thanh toan phai hien nguyen van paymentMethod, khong duoc rong");
        Assert.assertTrue(firstCells.get(5).trim().endsWith("₫"),
                "O Tong tien phai hien dang '...₫', dang thay: " + firstCells.get(5));
        Assert.assertTrue(java.util.regex.Pattern.compile("\\d{2}/\\d{2}/\\d{4}").matcher(firstCells.get(6)).find()
                        && firstCells.get(6).contains(":"),
                "O Ngay dat phai hien 2 dong dd/mm/yyyy + HH:mm, dang thay: " + firstCells.get(6));
        Assert.assertEquals(adminPage.getFirstTableRowActionButtonCount(), 1,
                "Cot Thao tac cua dong don hang chi duoc co 1 nut mat");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-119 (TC_SCRA03_UI_003, đợt 21 viết lại): Dropdown lọc trạng thái đơn hàng liệt kê đủ 7 lựa chọn theo thứ tự 'Trạng thái' + 6 nhóm (Chờ xác nhận/Đã xác nhận/Đang giao/Hoàn thành/Đã hủy/Hết hạn thanh toán); KHÔNG có 'Yêu cầu trả hàng', KHÔNG có 'Chờ xử lý'/'Đã thanh toán'/'Đang chuẩn bị'/'Đã giao'/'Đã hết hạn' riêng lẻ")
    public void adminOrderStatusFilterListsAllNineStatuses() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");

        java.util.List<String> options = adminPage.getOrderStatusFilterOptionTexts();
        Assert.assertEquals(options, java.util.Arrays.asList(
                        "Trạng thái", "Chờ xác nhận", "Đã xác nhận", "Đang giao",
                        "Hoàn thành", "Đã hủy", "Hết hạn thanh toán"),
                "Dropdown loc trang thai don hang dot 21 phai co dung 7 lua chon theo thu tu, dang thay: " + options);
        Assert.assertFalse(options.contains("Yêu cầu trả hàng"),
                "Dropdown loc trang thai don hang khong duoc co 'Yêu cầu trả hàng'");
        Assert.assertFalse(options.contains("Chờ xử lý"),
                "Dot 21 dropdown don hang khong duoc co 'Chờ xử lý' rieng (da gop vao 'Chờ xác nhận')");
        Assert.assertEquals(adminPage.getOrderStatusFilterValue(), "",
                "Dropdown mac dinh phai o 'Trạng thái' (value='')");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-120 (TC_SCRA03_FUNC_004 + TC_SCRA03_UI_005, đợt 21 viết lại — tổng số trang tính theo tổng thực tế, không hardcode 2 trang vì pageSize tải đã lên 100): Phân trang đơn hàng: >10 đơn thì trang 1 hiện 'Hiển thị 1–10 / N kết quả' + 'Trang 1 / ceil(N/10)', nút Trước disable, tối đa 10 dòng; sang trang 2 STT dòng đầu là 11; sang trang cuối nút Sau disable; về trang 1 lại hiện 10 đơn đầu (skip nếu ≤10 đơn)")
    public void adminOrderPaginationAndSerialNumbers() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        int total = adminPage.getPaginationTotalRows("orders");
        if (total <= 10) {
            throw new SkipException("Bo qua: DB nay chi co " + total + " don hang (can >10 don de kiem tra phan trang)");
        }
        int totalPages = (total + 9) / 10;

        String page1Text = adminPage.getPaginationText("orders");
        Assert.assertTrue(page1Text.contains("Hiển thị 1–10 / " + total + " kết quả"),
                "Trang 1 phai hien 'Hiển thị 1–10 / " + total + " kết quả', dang thay: " + page1Text);
        Assert.assertTrue(page1Text.contains("Trang 1 / " + totalPages),
                "Trang 1 phai hien 'Trang 1 / " + totalPages + "', dang thay: " + page1Text);
        Assert.assertFalse(adminPage.isPaginationPrevEnabled("orders"),
                "O trang 1 nut Truoc phai bi disable");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 10,
                "Trang 1 phai hien dung toi da 10 dong");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");

        adminPage.clickPaginationNext("orders");
        String page2Text = adminPage.getPaginationText("orders");
        Assert.assertTrue(page2Text.contains("Trang 2 / " + totalPages),
                "Trang 2 phai hien 'Trang 2 / " + totalPages + "', dang thay: " + page2Text);
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai la 11 (lien tuc theo trang, khong danh lai tu 1)");

        // Sang trang cuoi: nut Sau disable, STT dong dau = (trang cuoi - 1) * 10 + 1.
        for (int page = 3; page <= totalPages; page++) {
            adminPage.clickPaginationNext("orders");
        }
        Assert.assertEquals(adminPage.getPaginationCurrentPage("orders"), totalPages,
                "Sau khi bam Sau toi trang cuoi thi phai o trang " + totalPages);
        Assert.assertFalse(adminPage.isPaginationNextEnabled("orders"),
                "O trang cuoi nut Sau phai bi disable");
        Assert.assertEquals(adminPage.getFirstRowStt(), (totalPages - 1) * 10 + 1,
                "STT dong dau trang cuoi phai la " + ((totalPages - 1) * 10 + 1));

        adminPage.clickPaginationPrev("orders");
        if (totalPages > 2) {
            for (int page = totalPages - 1; page >= 2; page--) {
                adminPage.clickPaginationPrev("orders");
            }
        }
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "Ve trang 1 thi STT dong dau phai lai la 1");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 10,
                "Ve trang 1 phai hien lai 10 don dau");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-121 (TC_SCRA03_UI_007, đợt 21 viết lại — tìm lọc ngay khi gõ, không cần bấm Tìm): Tìm đơn hàng không khớp ('ZZZ999') thì hiện EmptyState 'Không tìm thấy đơn hàng' + 'Thử đổi từ khóa hoặc bộ lọc khác.' + nút 'Xóa bộ lọc', không hiện bảng/phân trang, số kết quả '0 / M đơn'; bấm 'Xóa bộ lọc' khôi phục đủ đơn")
    public void adminOrderSearchNoResultsShowsNotFoundEmptyState() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        int totalBefore = adminPage.getPaginationTotalRows("orders");
        adminPage.searchOrders("ZZZ999");

        Assert.assertTrue(adminPage.isEmptyOrdersMessageDisplayed(),
                "Tim 'ZZZ999' khong khop don nao thi phai hien 'Không tìm thấy đơn hàng'");
        String emptyText = adminPage.getEmptyStateText();
        Assert.assertTrue(emptyText.contains("Không tìm thấy đơn hàng"),
                "EmptyState phai co tieu de 'Không tìm thấy đơn hàng', dang thay: " + emptyText);
        Assert.assertTrue(emptyText.contains("Thử đổi từ khóa hoặc bộ lọc khác."),
                "EmptyState phai co mo ta 'Thử đổi từ khóa hoặc bộ lọc khác.', dang thay: " + emptyText);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Khong tim thay don thi khong duoc hien dong nao trong bang");
        Assert.assertTrue(adminPage.isOrderClearFiltersButtonDisplayed(),
                "EmptyState khong ket qua phai co nut 'Xóa bộ lọc' (admin-orders-clear-filters-btn)");
        Assert.assertTrue(adminPage.getOrderResultCountText().startsWith("0 / "),
                "Khong tim thay don thi so ket qua phai la '0 / M đơn', dang thay: '" + adminPage.getOrderResultCountText() + "'");

        // Bam "Xóa bộ lọc" trong EmptyState: xoa ca tu khoa + trang thai, bang ve du don.
        adminPage.clickOrderClearFiltersButton();
        Assert.assertEquals(adminPage.getOrderSearchValue(), "",
                "Bam 'Xóa bộ lọc' thi o tim kiem don hang phai trong");
        Assert.assertEquals(adminPage.getOrderStatusFilterValue(), "",
                "Bam 'Xóa bộ lọc' thi dropdown loc trang thai phai ve 'Trạng thái' (value='')");
        if (totalBefore > 0) {
            Assert.assertEquals(adminPage.getPaginationTotalRows("orders"), totalBefore,
                    "Bam 'Xóa bộ lọc' thi bang phai hien lai toan bo " + totalBefore + " don");
        }
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-122 (TC_SCRA03_FUNC_008, đợt 21 viết lại): Nút 'Đặt lại bộ lọc' xóa bộ lọc/tìm kiếm đơn hàng (ô tìm trống, dropdown về 'Trạng thái', về trang 1, tổng số đơn khôi phục) và lại bị disable sau khi đặt lại")
    public void adminOrderResetClearsFilterAndSearch() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        int totalBefore = adminPage.getPaginationTotalRows("orders");
        if (totalBefore <= 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra nut Dat lai");
        }

        adminPage.selectOrderStatusFilter("Đã hủy");
        adminPage.setOrderSearch("DH");
        Assert.assertTrue(adminPage.isOrderResetButtonEnabled(),
                "Co tu khoa/trang thai thi nut 'Đặt lại bộ lọc' phai bam duoc");

        adminPage.clickToolbarReset("orders");
        Assert.assertEquals(adminPage.getOrderSearchValue(), "",
                "Bam Dat lai thi o tim kiem don hang phai trong");
        Assert.assertEquals(adminPage.getOrderStatusFilterValue(), "",
                "Bam Dat lai thi dropdown loc trang thai phai ve 'Trạng thái' (value='')");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("orders"), 1,
                "Bam Dat lai thi phan trang phai ve trang 1");
        Assert.assertEquals(adminPage.getPaginationTotalRows("orders"), totalBefore,
                "Bam Dat lai thi bang phai tai lai toan bo " + totalBefore + " don");
        Assert.assertFalse(adminPage.isOrderResetButtonEnabled(),
                "Dat lai xong (khong con tu khoa/trang thai) thi nut 'Đặt lại bộ lọc' phai lai bi disable");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-123 (TC_SCRA03_FUNC_009, đợt 21 viết lại — ĐẢO NGƯỢC hành vi cũ: không còn nút Tìm/Enter, gõ là lọc sau debounce 120ms): Gõ từ khóa 'ZZZ999' nhưng chưa đủ 120ms thì bảng chưa đổi; đủ debounce thì bảng lọc chỉ còn đơn khớp (ở đây là rỗng + 'Không tìm thấy đơn hàng'); bấm Enter không làm gì thêm; bấm nút '×' xóa nhanh thì bảng khôi phục")
    public void adminOrderTypingWithoutSearchDoesNotReloadTable() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra go tu khoa chua tim");
        }
        java.util.List<String> rowsBefore = adminPage.getAdminTableRowTexts();

        // Go xong doc NGAY (chua du 120ms debounce): bang phai giu nguyen.
        adminPage.setOrderSearch("ZZZ999");
        Assert.assertEquals(adminPage.getAdminTableRowTexts(), rowsBefore,
                "Vua go xong (chua du 120ms debounce) thi noi dung bang don hang khong duoc doi");
        Assert.assertTrue(adminPage.isOrderSearchClearButtonDisplayed(),
                "Go tu khoa xong thi nut '×' xoa nhanh phai hien trong o tim");

        // Cho debounce xong: bang loc chi con don khop (o day la rong).
        sleepInSecond(2);
        Assert.assertTrue(adminPage.isEmptyOrdersMessageDisplayed(),
                "Cho debounce 120ms xong thi bang phai loc chi con don khop (o day la rong + 'Không tìm thấy đơn hàng')");

        // Bam Enter khong lam gi them (khong co onSearch): van la EmptyState.
        adminPage.pressEnterInOrderSearch();
        Assert.assertTrue(adminPage.isEmptyOrdersMessageDisplayed(),
                "Bam Enter o o tim don (dot 21) khong duoc doi ket qua loc");

        // Bam nut '×' xoa nhanh: o tim trong, bang khoi phuc.
        adminPage.clickOrderSearchClearButton();
        Assert.assertEquals(adminPage.getOrderSearchValue(), "",
                "Bam nut '×' thi o tim kiem don hang phai trong");
        Assert.assertFalse(adminPage.isOrderSearchClearButtonDisplayed(),
                "Xoa nhanh xong thi nut '×' phai an di");
        Assert.assertEquals(adminPage.getAdminTableRowTexts(), rowsBefore,
                "Bam nut '×' thi bang don hang phai ve nhu truoc khi tim");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-124 (TC_SCRA03_FUNC_010/011/012/013/014/015/016/018, đợt 21 viết lại — dropdown chỉ còn 6 nhóm): Lọc đơn hàng theo 6 nhóm Chờ xác nhận/Đã xác nhận/Đang giao/Hoàn thành/Đã hủy/Hết hạn thanh toán: mỗi nhóm chỉ còn đúng dòng mang nhãn BE tương ứng HOẶC hiện trạng thái rỗng; luôn về trang 1; Đặt lại sau test. LƯU Ý nhãn: nhóm 'Chờ xác nhận' gồm cả 'Chờ xác nhận' và 'Chờ thanh toán' (Pending PayOS); nhóm 'Hoàn thành' hiện nhãn 'Hoàn tất' trong bảng")
    public void adminOrderStatusFilterShowsOnlySelectedStatus() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        // {nhóm dropdown, các nhãn BE được phép xuất hiện trong bảng sau khi lọc}
        String[][] groups = {
                {"Chờ xác nhận", "Chờ xác nhận|Chờ thanh toán"},
                {"Đã xác nhận", "Đã xác nhận"},
                {"Đang giao", "Đang giao"},
                {"Hoàn thành", "Hoàn tất"},
                {"Đã hủy", "Đã hủy"},
                {"Hết hạn thanh toán", "Hết hạn thanh toán"},
        };
        for (String[] group : groups) {
            String filterLabel = group[0];
            String[] allowedLabels = group[1].split("\\|");
            adminPage.selectOrderStatusFilter(filterLabel);
            if (adminPage.getAdminTableRowCount() == 0) {
                Assert.assertTrue(adminPage.isEmptyOrdersMessageDisplayed(),
                        "Loc '" + filterLabel + "' khong co don thi phai hien 'Không tìm thấy đơn hàng'");
            } else {
                for (String row : adminPage.getAdminTableRowTexts()) {
                    boolean matches = false;
                    for (String label : allowedLabels) {
                        if (row.contains(label)) {
                            matches = true;
                            break;
                        }
                    }
                    Assert.assertTrue(matches,
                            "Loc '" + filterLabel + "' thi moi dong deu phai o trang thai "
                                    + java.util.Arrays.toString(allowedLabels) + ", dang thay: " + row);
                }
                int currentPage = adminPage.getPaginationCurrentPage("orders");
                if (currentPage != -1) {
                    Assert.assertEquals(currentPage, 1,
                            "Loc '" + filterLabel + "' thi phan trang phai ve trang 1");
                }
            }
        }
        adminPage.clickToolbarReset("orders");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-125 (TC_SCRA03_FUNC_019/020/021, đợt 21 viết lại — tìm lọc ngay khi gõ, debounce 120ms, không còn nút Tìm): Tìm đơn hàng theo mã đơn / tên khách / email khách (lấy động từ dòng đầu, không hardcode): tìm theo mã chỉ còn đúng đơn đó; tìm theo tên/email thì đơn đó vẫn hiện; Đặt lại sau mỗi lần tìm (skip nếu DB chưa có đơn)")
    public void adminOrderSearchFindsOrderByCodeCustomerNameAndEmail() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra tim kiem");
        }
        java.util.List<String> firstCells = adminPage.getFirstTableRowCellTexts();
        String orderCode = extractOrderDisplayCode(firstCells.get(1));
        Assert.assertNotNull(orderCode, "Dong dau tien phai co ma don dang DH{yyyyMMdd}-{NNNN}");
        String[] customerParts = firstCells.get(2).split("\n");
        String customerName = customerParts[0].trim();
        String customerEmail = customerParts.length > 1 ? customerParts[1].trim() : "";
        Assert.assertFalse(customerEmail.isEmpty(), "Dong dau tien phai co email khach de tim theo email");

        adminPage.searchOrders(orderCode);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                "Tim theo ma don '" + orderCode + "' thi chi duoc con dung 1 dong");
        Assert.assertTrue(adminPage.getAdminTableRowTexts().get(0).contains(orderCode),
                "Tim theo ma don thi dong con lai phai la don '" + orderCode + "'");
        Assert.assertTrue(adminPage.getOrderResultCountText().startsWith("1 / "),
                "Tim theo ma don thi so ket qua phai la '1 / M đơn', dang thay: '" + adminPage.getOrderResultCountText() + "'");
        adminPage.clickToolbarReset("orders");

        adminPage.searchOrders(customerName);
        Assert.assertTrue(adminPage.isRowWithTextDisplayed(orderCode),
                "Tim theo ten khach '" + customerName + "' thi don '" + orderCode + "' phai van hien");
        adminPage.clickToolbarReset("orders");

        adminPage.searchOrders(customerEmail);
        Assert.assertTrue(adminPage.isRowWithTextDisplayed(orderCode),
                "Tim theo email khach '" + customerEmail + "' thi don '" + orderCode + "' phai van hien");
        adminPage.clickToolbarReset("orders");
    }

    // ---------- Don hang dot 21: tim khong dau / bam dong navigate / so ket qua ----------

    /** Dot 21 — fold dau tieng Viet giong FE (NFD bo dau ket hop, d->d):
     *  dung de tao tu khoa khong dau tu ten khach co dau lay dong tu bang. */
    private static String foldVietnamese(String s) {
        String folded = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
        return folded.toLowerCase(java.util.Locale.ROOT);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM21-01 (đợt 21 — tìm không dấu): Gõ tên khách KHÔNG dấu (fold từ tên có dấu lấy động từ dòng đầu) thì đơn đó vẫn hiện; gõ đúng tên có dấu cũng hiện (skip nếu tên khách dòng đầu không có dấu để fold, hoặc DB chưa có đơn)")
    public void adminOrderSearchFindsCustomerByUnaccentedName() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra tim khong dau");
        }
        java.util.List<String> firstCells = adminPage.getFirstTableRowCellTexts();
        String orderCode = extractOrderDisplayCode(firstCells.get(1));
        Assert.assertNotNull(orderCode, "Dong dau tien phai co ma don dang DH{yyyyMMdd}-{NNNN}");
        String customerName = firstCells.get(2).split("\n")[0].trim();
        String folded = foldVietnamese(customerName);
        if (folded.equals(customerName.toLowerCase(java.util.Locale.ROOT))) {
            throw new SkipException("Bo qua: ten khach dong dau '" + customerName
                    + "' khong co dau tieng Viet de kiem tra tim khong dau");
        }

        // Go ten khong dau: don do phai van hien (fold o FE).
        adminPage.searchOrders(folded);
        Assert.assertTrue(adminPage.isRowWithTextDisplayed(orderCode),
                "Tim theo ten khong dau '" + folded + "' thi don '" + orderCode
                        + "' cua khach '" + customerName + "' phai van hien");
        adminPage.clickToolbarReset("orders");

        // Go dung ten co dau: don do van hien (baseline).
        adminPage.searchOrders(customerName);
        Assert.assertTrue(adminPage.isRowWithTextDisplayed(orderCode),
                "Tim theo ten co dau '" + customerName + "' thi don '" + orderCode + "' phai van hien");
        adminPage.clickToolbarReset("orders");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM21-02 (đợt 21 — bấm dòng mở chi tiết, chỉ kiểm ở mức navigate URL): Bấm vào dòng đơn đầu tiên (không bấm nút mắt) thì trình duyệt chuyển sang route trang chi tiết /orders/<mã đơn> của đúng đơn đó (skip nếu DB chưa có đơn; nội dung trang chi tiết do worker chi tiết lo)")
    public void adminOrderRowClickNavigatesToOrderDetailPage() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra bam vao dong don");
        }
        String orderCode = extractOrderDisplayCode(adminPage.getFirstTableRowCellTexts().get(1));
        Assert.assertNotNull(orderCode, "Dong dau tien phai co ma don dang DH{yyyyMMdd}-{NNNN}");

        adminPage.clickFirstOrderRow();
        String url = adminPage.getCurrentPageUrl();
        Assert.assertTrue(url.contains("/orders/" + orderCode),
                "Bam vao dong don phai navigate sang /orders/" + orderCode + ", dang o: " + url);
        // Quay lai muc Don hang de khong anh huong test sau.
        adminPage.clickAdminNav("orders");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM21-03 (đợt 21 — số kết quả): Số kết quả 'N / M đơn' khớp tổng phân trang sau khi lọc: chưa lọc thì 'M / M đơn'; lọc 'Đã hủy' thì N = tổng phân trang (hoặc '0 / M đơn' khi rỗng); Đặt lại thì về 'M / M đơn' (skip nếu DB chưa có đơn)")
    public void adminOrderResultCountMatchesFilteredTotal() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        int totalBefore = adminPage.getPaginationTotalRows("orders");
        if (totalBefore <= 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra so ket qua");
        }
        Assert.assertEquals(adminPage.getOrderResultCountText(), totalBefore + " / " + totalBefore + " đơn",
                "Chua loc thi so ket qua phai la '" + totalBefore + " / " + totalBefore + " đơn', dang thay: '"
                        + adminPage.getOrderResultCountText() + "'");

        adminPage.selectOrderStatusFilter("Đã hủy");
        String filtered = adminPage.getOrderResultCountText();
        int filteredTotal = adminPage.getPaginationTotalRows("orders"); // -1 neu rong (khong co phan trang)
        if (filteredTotal == -1) {
            Assert.assertTrue(filtered.startsWith("0 / "),
                    "Loc 'Đã hủy' khong co don thi so ket qua phai la '0 / M đơn', dang thay: '" + filtered + "'");
        } else {
            Assert.assertEquals(filtered, filteredTotal + " / " + totalBefore + " đơn",
                    "Loc 'Đã hủy' thi so ket qua phai la '" + filteredTotal + " / " + totalBefore
                            + " đơn', dang thay: '" + filtered + "'");
        }
        adminPage.clickToolbarReset("orders");
        Assert.assertEquals(adminPage.getOrderResultCountText(), totalBefore + " / " + totalBefore + " đơn",
                "Dat lai xong thi so ket qua phai ve '" + totalBefore + " / " + totalBefore + " đơn', dang thay: '"
                        + adminPage.getOrderResultCountText() + "'");
    }

    // ---------- Don hang: Trang chi tiet (SCR_03 FUNC_024-027, chi doc) ----------

    @Story("Đơn hàng")
    @Test(description = "ADM19-126 (TC_SCRA03_FUNC_024, đợt 21 viết lại): Bấm vào dòng đơn (không bấm nút mắt) thì mở đúng trang chi tiết /orders/:code của đơn đó (hiện đúng mã đơn); về danh sách được bằng breadcrumb (skip nếu DB chưa có đơn)")
    public void adminOrderDetailOpensByClickingRow() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra bam vao dong don");
        }
        String orderCode = extractOrderDisplayCode(adminPage.getFirstTableRowCellTexts().get(1));
        Assert.assertNotNull(orderCode, "Dong dau tien phai co ma don");

        adminPage.openFirstOrderDetailByRowClick();
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Bam vao dong don phai mo duoc trang chi tiet don hang");
        Assert.assertTrue(adminPage.getOrderDetailTitleText().startsWith("Đơn " + orderCode),
                "Trang chi tiet mo bang cach bam dong phai hien dung ma don '" + orderCode + "'");

        adminPage.closeOrderDetail();
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Ve danh sach xong khong duoc con thay trang chi tiet don hang");
        Assert.assertTrue(adminPage.isOrderSearchDisplayed(),
                "Ve danh sach xong phai thay lai bang don hang");
    }

    @Test(description = "ADM19-127 (TC_SCRA03_FUNC_025, đợt 21 viết lại): ESC chỉ đóng PANEL đang mở trên trang chi tiết (panel đổi trạng thái / panel hủy đơn), KHÔNG đóng trang; về danh sách bằng breadcrumb (skip nếu DB chưa có đơn Chờ xử lý/Đã xác nhận)")
    public void adminOrderDetailDrawerCloseMethods() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Chờ xử lý");
        if (orderCode == null) {
            orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đã xác nhận");
        }
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao o trang thai 'Chờ xử lý'/'Đã xác nhận' de kiem tra ESC tren trang chi tiet");
        }

        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Phai mo duoc trang chi tiet don hang");

        // ESC khi panel đổi trạng thái đang mở → panel đóng, trang vẫn mở.
        adminPage.openOrderStatusPanel();
        Assert.assertTrue(adminPage.isOrderStatusPanelDisplayed(),
                "Bam [Đổi trạng thái] phai mo panel doi trang thai");
        adminPage.pressEscapeInOrderDetail();
        Assert.assertFalse(adminPage.isOrderStatusPanelDisplayed(),
                "Bam ESC thi panel doi trang thai phai dong");
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Bam ESC dong panel xong trang chi tiet phai VAN MO");

        // ESC khi panel hủy đơn đang mở → panel đóng, trang vẫn mở.
        adminPage.openCancelOrderDialog();
        Assert.assertTrue(adminPage.isOrderCancelPanelDisplayed(),
                "Bam [Hủy đơn] phai mo panel huy don");
        adminPage.pressEscapeInOrderDetail();
        Assert.assertFalse(adminPage.isOrderCancelPanelDisplayed(),
                "Bam ESC thi panel huy don phai dong");
        Assert.assertTrue(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Bam ESC dong panel xong trang chi tiet phai VAN MO");

        adminPage.closeOrderDetail();
        Assert.assertFalse(adminPage.isDetailModalDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL),
                "Ve danh sach bang breadcrumb xong khong duoc con thay trang chi tiet");
    }

    @Test(description = "ADM19-128 (TC_SCRA03_FUNC_026, đợt 21 viết lại): Nội dung trang chi tiết đơn: card Khách hàng, card Địa chỉ giao hàng, card Thanh toán (phương thức + tình trạng suy ra + tổng tiền), card Sản phẩm '<tên> <SL> × <đơn giá>₫' + đúng 3 dòng Tạm tính/Phí vận chuyển/Tổng cộng; các dòng Voucher/Điểm đã dùng/Vận chuyển/Lý do hủy/Ghi chú nội bộ chỉ assert khi đơn đó có dữ liệu tương ứng (skip nếu DB chưa có đơn)")
    public void adminOrderDetailShowsFullContent() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra noi dung trang chi tiet");
        }
        String orderCode = extractOrderDisplayCode(adminPage.getFirstTableRowCellTexts().get(1));
        adminPage.clickFirstOrderDetail();
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_ORDER_DETAIL);
        Assert.assertTrue(adminPage.getOrderDetailTitleText().startsWith("Đơn " + orderCode),
                "Tieu de trang chi tiet phai bat dau bang 'Đơn " + orderCode + "'");
        Assert.assertTrue(modalText.contains("Khách hàng"),
                "Trang chi tiet don phai co card 'Khách hàng', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Địa chỉ giao hàng"),
                "Trang chi tiet don phai co card 'Địa chỉ giao hàng', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Thanh toán"),
                "Trang chi tiet don phai co card 'Thanh toán', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Tạm tính") && modalText.contains("Phí vận chuyển")
                        && modalText.contains("Tổng cộng"),
                "Card San pham phai co dung 3 dong 'Tạm tính' / 'Phí vận chuyển' / 'Tổng cộng', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Sản phẩm"),
                "Trang chi tiet don phai co card San pham, dang thay: " + modalText);
        Assert.assertTrue(modalText.contains(" × ") && modalText.contains("₫"),
                "Card San pham phai liet ke '<tên> <SL> × <đơn giá>₫', dang thay: " + modalText);
        if (modalText.contains("Voucher: ")) {
            Assert.assertTrue(modalText.contains("giảm") && modalText.contains("₫"),
                    "Dong Voucher (khi co) phai ghi 'Voucher: <mã> (giảm ...₫)', dang thay: " + modalText);
        }
        if (modalText.contains("Điểm đã dùng: ")) {
            Assert.assertTrue(modalText.contains("Điểm nhận:"),
                    "Dong diem (khi co) phai ghi ca 'Điểm nhận:', dang thay: " + modalText);
        }
        if (modalText.contains("Vận chuyển: ")) {
            Assert.assertTrue(modalText.contains("Mã vận đơn:"),
                    "Dong van chuyen (khi co) phai ghi 'Vận chuyển: <đơn vị> — Mã vận đơn: <mã>', dang thay: " + modalText);
        }
        if (modalText.contains("Lý do hủy: ")) {
            Assert.assertTrue(modalText.split("Lý do hủy: ", 2)[1].trim().length() > 0,
                    "Dong ly do huy (khi co) phai co noi dung sau dau ':', dang thay: " + modalText);
        }
        if (modalText.contains("Ghi chú nội bộ: ")) {
            Assert.assertTrue(modalText.split("Ghi chú nội bộ: ", 2)[1].trim().length() > 0,
                    "Dong ghi chu noi bo (khi co) phai co noi dung sau dau ':', dang thay: " + modalText);
        }
        adminPage.closeOrderDetail();
    }

    @Test(description = "ADM19-129 (TC_SCRA03_FUNC_027, đợt 21 viết lại): Mục 'Lịch sử trạng thái' trên trang chi tiết đơn: timeline mới nhất trước, mỗi dòng '<nhãn>' + thời gian 'HH:mm:ss · dd/mm/yyyy' (+ ghi chú bên dưới nếu có); đơn chưa có lịch sử thì hiện 'Chưa có lịch sử trạng thái.' (skip nếu DB chưa có đơn)")
    public void adminOrderDetailShowsStatusHistory() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("orders");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co don hang nao de kiem tra lich su trang thai");
        }
        adminPage.clickFirstOrderDetail();
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_ORDER_DETAIL);
        Assert.assertTrue(modalText.contains("Lịch sử trạng thái"),
                "Trang chi tiet don phai co muc 'Lịch sử trạng thái', dang thay: " + modalText);
        if (modalText.contains("Chưa có lịch sử trạng thái.")) {
            // Don chua co lich su — dung hanh vi mong doi cho truong hop rong.
            Assert.assertTrue(true, "Don chua co lich su thi hien 'Chưa có lịch sử trạng thái.'");
        } else {
            boolean hasHistoryLine = java.util.regex.Pattern.compile(
                            "(Chờ xử lý|Đã thanh toán|Đã xác nhận|Đang chuẩn bị|Đang giao|Đã giao|Hoàn tất|Yêu cầu trả hàng|Đã hủy|Đã hết hạn)[\\s\\S]{0,200}?\\d{2}:\\d{2}:\\d{2} · \\d{2}/\\d{2}/\\d{4}")
                    .matcher(modalText).find();
            Assert.assertTrue(hasHistoryLine,
                    "Lich su trang thai phai co dong '<nhãn>' + thoi gian 'HH:mm:ss · dd/mm/yyyy', dang thay: " + modalText);
        }
        adminPage.closeOrderDetail();
    }

    // ---------- Don hang: doi trang thai that (SCR_03 FUNC_028-039, MUTATES, moi test toi da 1 don) ----------

    @Story("Đơn hàng")
    @Test(description = "ADM19-130 (TC_SCRA03_FUNC_028, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Chờ xử lý → Đã thanh toán qua panel; toast 'Đã cập nhật đơn hàng' + \"Đơn <mã> chuyển sang 'Đã thanh toán'.\"; bảng hiện đơn ở 'Đã thanh toán' (skip nếu không có đơn Chờ xử lý)")
    public void adminChangePendingOrderStatusToPaid() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        changeFirstOrderStatus(adminPage, "Chờ xử lý", "Đã thanh toán", true);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-131 (TC_SCRA03_FUNC_030, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Chờ xử lý → Đã hết hạn qua panel; toast chuyển sang 'Đã hết hạn', bảng hiện đơn ở 'Đã hết hạn' (skip nếu không có đơn Chờ xử lý — mỗi test đổi trạng thái tiêu thụ 1 đơn Pending)")
    public void adminChangePendingOrderStatusToExpired() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        changeFirstOrderStatus(adminPage, "Chờ xử lý", "Đã hết hạn", true);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-132 (TC_SCRA03_FUNC_031, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đã thanh toán → Đã xác nhận qua panel (chọn theo đích = Confirmed, không theo nguồn); toast + đơn hiện 'Đã xác nhận' (skip nếu không có đơn Đã thanh toán)")
    public void adminChangePaidOrderStatusToConfirmed() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        changeFirstOrderStatus(adminPage, "Đã thanh toán", "Đã xác nhận", true);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-133 (TC_SCRA03_FUNC_032, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đã thanh toán → Đang chuẩn bị qua panel; toast + đơn hiện 'Đang chuẩn bị' (skip nếu không có đơn Đã thanh toán — mỗi test tiêu thụ 1 đơn Paid)")
    public void adminChangePaidOrderStatusToPreparing() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        changeFirstOrderStatus(adminPage, "Đã thanh toán", "Đang chuẩn bị", true);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-134 (TC_SCRA03_FUNC_033, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đã xác nhận → Đang chuẩn bị qua panel; toast + đơn hiện 'Đang chuẩn bị' (skip nếu không có đơn Đã xác nhận)")
    public void adminChangeConfirmedOrderStatusToPreparing() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        changeFirstOrderStatus(adminPage, "Đã xác nhận", "Đang chuẩn bị", true);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-135 (TC_SCRA03_FUNC_034, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đã xác nhận → Đang giao (kèm vận chuyển): chọn đích 'Đang giao' thì hiện ô Đơn vị vận chuyển + Mã vận đơn; nhập GHN / GHN123456 → panel → toast + đơn hiện 'Đang giao', trang chi tiết hiện 'Vận chuyển: GHN — Mã vận đơn: GHN123456' (skip nếu không có đơn Đã xác nhận)")
    public void adminChangeConfirmedOrderStatusToShippingWithCarrier() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đã xác nhận");
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Đã xác nhận' de doi sang 'Đang giao'");
        }
        adminPage.selectOrderTargetStatus("Đang giao");
        Assert.assertTrue(adminPage.isOrderCarrierInputDisplayed(),
                "Chon dich 'Đang giao' thi phai hien o Don vi van chuyen (admin-order-carrier-input)");
        Assert.assertTrue(adminPage.isOrderTrackingInputDisplayed(),
                "Chon dich 'Đang giao' thi phai hien o Ma van don (admin-order-tracking-input)");
        adminPage.setOrderCarrier("GHN");
        adminPage.setOrderTrackingCode("GHN123456");
        adminPage.changeOrderStatusTo("Đang giao");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Doi trang thai don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật đơn hàng"),
                "Toast doi trang thai don phai co tieu de 'Đã cập nhật đơn hàng', dang thay: " + adminPage.getToastTitleText());
        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đang giao"),
                "Don " + orderCode + " sau khi doi phai hien 'Đang giao' trong bang don");
        adminPage.searchOrderByCodeAndOpen(orderCode);
        String detailText = adminPage.getDetailModalText(AdminPageUI.ADMIN_ORDER_DETAIL);
        Assert.assertTrue(detailText.contains("Vận chuyển: GHN — Mã vận đơn: GHN123456"),
                "Trang chi tiet don sau khi doi phai hien 'Vận chuyển: GHN — Mã vận đơn: GHN123456', dang thay: " + detailText);
        adminPage.closeOrderDetail();
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-136 (TC_SCRA03_FUNC_035, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đang chuẩn bị → Đang giao (kèm vận chuyển GHN / GHN123456) qua panel; toast + đơn hiện 'Đang giao' + dòng vận chuyển trong trang chi tiết (skip nếu không có đơn Đang chuẩn bị)")
    public void adminChangePreparingOrderStatusToShippingWithCarrier() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đang chuẩn bị");
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Đang chuẩn bị' de doi sang 'Đang giao'");
        }
        adminPage.selectOrderTargetStatus("Đang giao");
        Assert.assertTrue(adminPage.isOrderCarrierInputDisplayed(),
                "Chon dich 'Đang giao' thi phai hien o Don vi van chuyen (admin-order-carrier-input)");
        Assert.assertTrue(adminPage.isOrderTrackingInputDisplayed(),
                "Chon dich 'Đang giao' thi phai hien o Ma van don (admin-order-tracking-input)");
        adminPage.setOrderCarrier("GHN");
        adminPage.setOrderTrackingCode("GHN123456");
        adminPage.changeOrderStatusTo("Đang giao");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Doi trang thai don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật đơn hàng"),
                "Toast doi trang thai don phai co tieu de 'Đã cập nhật đơn hàng', dang thay: " + adminPage.getToastTitleText());
        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đang giao"),
                "Don " + orderCode + " sau khi doi phai hien 'Đang giao' trong bang don");
        adminPage.searchOrderByCodeAndOpen(orderCode);
        String detailText = adminPage.getDetailModalText(AdminPageUI.ADMIN_ORDER_DETAIL);
        Assert.assertTrue(detailText.contains("Vận chuyển: GHN — Mã vận đơn: GHN123456"),
                "Trang chi tiet don sau khi doi phai hien 'Vận chuyển: GHN — Mã vận đơn: GHN123456', dang thay: " + detailText);
        adminPage.closeOrderDetail();
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-137 (TC_SCRA03_FUNC_036, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đang giao → Đã giao qua panel; toast + đơn hiện 'Đã giao' (skip nếu không có đơn Đang giao; tác dụng phụ BE với đơn COD không assert được qua UI)")
    public void adminChangeShippingOrderStatusToDelivered() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        changeFirstOrderStatus(adminPage, "Đang giao", "Đã giao", true);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-138 (TC_SCRA03_FUNC_037, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đã giao → Hoàn tất qua panel; toast + đơn hiện 'Hoàn tất' (skip nếu không có đơn Đã giao; cộng điểm/nâng hạng của BE không assert được qua UI)")
    public void adminChangeDeliveredOrderStatusToCompleted() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        changeFirstOrderStatus(adminPage, "Đã giao", "Hoàn tất", true);
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-139 (TC_SCRA03_FUNC_038, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Đã giao → Yêu cầu trả hàng qua panel; toast + trang chi tiết hiện 'Yêu cầu trả hàng' (kiểm qua trang chi tiết vì 'Yêu cầu trả hàng' không có trong dropdown lọc bảng) (skip nếu không có đơn Đã giao)")
    public void adminChangeDeliveredOrderStatusToReturnRequested() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đã giao");
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Đã giao' de doi sang 'Yêu cầu trả hàng'");
        }
        adminPage.changeOrderStatusTo("Yêu cầu trả hàng");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Doi trang thai don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật đơn hàng"),
                "Toast doi trang thai don phai co tieu de 'Đã cập nhật đơn hàng', dang thay: " + adminPage.getToastTitleText());
        Assert.assertTrue(adminPage.getOrderDetailTitleText().startsWith("Đơn " + orderCode),
                "Tieu de trang chi tiet sau khi doi phai bat dau bang 'Đơn " + orderCode + "'");
        Assert.assertEquals(adminPage.getOrderDetailStatusPillText(), "Yêu cầu trả hàng",
                "Pill trang thai sau khi doi phai la 'Yêu cầu trả hàng'");
        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        // 'Yêu cầu trả hàng' khong co trong dropdown loc bang nen kiem chung lai bang cach mo trang chi tiet.
        adminPage.searchOrderByCodeAndOpen(orderCode);
        Assert.assertEquals(adminPage.getOrderDetailStatusPillText(), "Yêu cầu trả hàng",
                "Mo lai trang chi tiet don " + orderCode + " phai thay trang thai 'Yêu cầu trả hàng'");
        adminPage.closeOrderDetail();
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-140 (TC_SCRA03_FUNC_039, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái đơn Yêu cầu trả hàng → Đã giao qua panel (ALLOWED_NEXT: ReturnRequested → ['Delivered'] theo code thật); toast + đơn hiện 'Đã giao' (skip nếu không có đơn Yêu cầu trả hàng — trạng thái hiếm nên test này thường sẽ skip)")
    public void adminChangeReturnRequestedOrderStatusToDelivered() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = findOrderCodeByStatusInUnfilteredTableOrNull(adminPage, "Yêu cầu trả hàng");
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Yêu cầu trả hàng' de doi sang 'Đã giao'");
        }
        adminPage.clickToolbarReset("orders");
        adminPage.searchOrderByCodeAndOpen(orderCode);
        java.util.List<String> targetOptions = adminPage.getOrderTargetStatusLabels();
        Assert.assertTrue(targetOptions.contains("Đã giao"),
                "Dropdown dich cua don 'Yêu cầu trả hàng' phai chua 'Đã giao', dang thay: " + targetOptions);
        adminPage.changeOrderStatusTo("Đã giao");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Doi trang thai don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật đơn hàng"),
                "Toast doi trang thai don phai co tieu de 'Đã cập nhật đơn hàng', dang thay: " + adminPage.getToastTitleText());
        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã giao"),
                "Don " + orderCode + " sau khi doi phai hien 'Đã giao' trong bang don");
    }

    // ---------- Don hang: validation doi trang thai / ghi chu / don terminal / huy don ----------

    @Story("Đơn hàng")
    @Test(description = "ADM19-141 (TC_SCRA03_VAL_040/041/042): Thiếu đơn vị vận chuyển / mã vận đơn 5 ký tự / mã vận đơn 31 ký tự khi chuyển sang Đang giao đều bị chặn bằng lỗi inline 'Chuyển sang Đang giao bắt buộc có đơn vị vận chuyển và mã vận đơn 6–30 ký tự.', KHÔNG gọi POST (đợt 21 không còn popup D3), đơn giữ nguyên trạng thái (không đổi dữ liệu thật; skip nếu không có đơn Đã xác nhận/Đang chuẩn bị)")
    public void adminShippingValidationBlocksInvalidCarrierAndTracking() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đã xác nhận");
        String sourceLabel = "Đã xác nhận";
        if (orderCode == null) {
            orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đang chuẩn bị");
            sourceLabel = "Đang chuẩn bị";
        }
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Đã xác nhận'/'Đang chuẩn bị' de kiem tra validation van chuyen");
        }
        String expectedError = "Chuyển sang Đang giao bắt buộc có đơn vị vận chuyển và mã vận đơn 6–30 ký tự.";
        adminPage.selectOrderTargetStatus("Đang giao");

        // (1) Co ma van don nhung de trong Don vi van chuyen.
        adminPage.setOrderTrackingCode("GHN123456");
        adminPage.clickOrderChangeStatusButton();
        Assert.assertEquals(adminPage.getOrderDetailErrorText(), expectedError,
                "Thieu don vi van chuyen thi phai hien dung loi inline tren");
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Bi chan validation thi khong duoc mo popup xac nhan nao");

        // (2) Carrier GHN + ma van don 5 ky tu 'GHN12'.
        adminPage.setOrderCarrier("GHN");
        adminPage.setOrderTrackingCode("GHN12");
        adminPage.clickOrderChangeStatusButton();
        Assert.assertEquals(adminPage.getOrderDetailErrorText(), expectedError,
                "Ma van don 5 ky tu thi phai hien dung loi inline tren");
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Bi chan validation thi khong duoc mo popup xac nhan nao");

        // (3) Carrier GHN + ma van don 31 ky tu.
        adminPage.setOrderTrackingCode("GHN" + "1".repeat(28));
        adminPage.clickOrderChangeStatusButton();
        Assert.assertEquals(adminPage.getOrderDetailErrorText(), expectedError,
                "Ma van don 31 ky tu thi phai hien dung loi inline tren");
        Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                "Bi chan validation thi khong duoc mo popup xac nhan nao");

        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, sourceLabel),
                "Don " + orderCode + " bi chan validation thi phai giu nguyen '" + sourceLabel + "'");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-142 (TC_SCRA03_FUNC_043 + TC_SCRA03_VAL_044, đợt 21 viết lại): Panel đổi trạng thái: chưa mở panel thì không có dropdown đích, không có ô vận chuyển/mã vận đơn; bấm [Đổi trạng thái] ở header thì mở panel, nút [Xác nhận đổi] disable đến khi chọn đích; chọn đích hợp lệ thì hết disable; chỉ đích 'Đang giao' mới hiện 2 ô vận chuyển, chọn lại đích khác thì 2 ô biến mất (chỉ mở xem, không đổi dữ liệu thật; skip nếu không có đơn còn đổi được trạng thái)")
    public void adminOrderChangeStatusSectionShowsShippingFieldsAndDisabledButton() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đã xác nhận");
        String sourceLabel = "Đã xác nhận";
        if (orderCode == null) {
            orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đang chuẩn bị");
            sourceLabel = "Đang chuẩn bị";
        }
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Đã xác nhận'/'Đang chuẩn bị' de kiem tra panel doi trang thai");
        }
        Assert.assertFalse(adminPage.isOrderCarrierInputDisplayed(),
                "Chua mo panel thi khong duoc hien o Don vi van chuyen");
        Assert.assertFalse(adminPage.isOrderTrackingInputDisplayed(),
                "Chua mo panel thi khong duoc hien o Ma van don");
        Assert.assertFalse(adminPage.isOrderStatusSelectDisplayed(),
                "Chua mo panel thi khong duoc hien dropdown chon trang thai dich");
        // Nut header [Đổi trạng thái] bam duoc (mo panel); nut [Xác nhận đổi]
        // trong panel disable den khi chon dich.
        Assert.assertTrue(adminPage.isOrderChangeStatusButtonEnabled(),
                "Nut [Đổi trạng thái] o header phai bam duoc de mo panel");
        adminPage.openOrderStatusPanel();
        Assert.assertFalse(adminPage.isOrderStatusConfirmButtonEnabled(),
                "Chua chon dich ('— Chọn trạng thái đích —') thi nut [Xác nhận đổi] phai bi disable");

        if ("Đã xác nhận".equals(sourceLabel)) {
            adminPage.selectOrderTargetStatus("Đang chuẩn bị");
            Assert.assertFalse(adminPage.isOrderCarrierInputDisplayed(),
                    "Chon dich 'Đang chuẩn bị' thi van khong duoc hien 2 o van chuyen");
            Assert.assertFalse(adminPage.isOrderTrackingInputDisplayed(),
                    "Chon dich 'Đang chuẩn bị' thi van khong duoc hien 2 o van chuyen");
            Assert.assertTrue(adminPage.isOrderStatusConfirmButtonEnabled(),
                    "Chon 1 dich hop le thi nut [Xác nhận đổi] phai het disable");
        }
        adminPage.selectOrderTargetStatus("Đang giao");
        Assert.assertTrue(adminPage.isOrderCarrierInputDisplayed(),
                "Chon dich 'Đang giao' thi phai hien o Don vi van chuyen");
        Assert.assertTrue(adminPage.isOrderTrackingInputDisplayed(),
                "Chon dich 'Đang giao' thi phai hien o Ma van don");
        if ("Đã xác nhận".equals(sourceLabel)) {
            adminPage.selectOrderTargetStatus("Đang chuẩn bị");
            Assert.assertFalse(adminPage.isOrderCarrierInputDisplayed(),
                    "Chon lai dich khac thi 2 o van chuyen phai bien mat");
            Assert.assertFalse(adminPage.isOrderTrackingInputDisplayed(),
                    "Chon lai dich khac thi 2 o van chuyen phai bien mat");
        }
        adminPage.closeOrderStatusPanel();
        Assert.assertFalse(adminPage.isOrderStatusPanelDisplayed(),
                "Bam [Đóng] thi panel doi trang thai phai dong");
        adminPage.closeOrderDetail();
    }

    @Test(description = "ADM19-143 (TC_SCRA03_FUNC_045, MUTATES — đổi 1 đơn thật, KHÔNG khôi phục được): Đổi trạng thái kèm ghi chú: đơn Chờ xử lý → Đã xác nhận, nhập Ghi chú 'Khách yêu cầu giao nhanh' → panel → toast chuyển sang 'Đã xác nhận'; mở lại trang chi tiết thì dòng Lịch sử mới ghi 'Đã xác nhận — <ngày giờ> — Khách yêu cầu giao nhanh' (skip nếu không có đơn Chờ xử lý)")
    public void adminChangeOrderStatusWithNoteShowsNoteInHistory() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Chờ xử lý");
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xử lý' de doi trang thai kem ghi chu");
        }
        adminPage.selectOrderTargetStatus("Đã xác nhận");
        adminPage.setOrderStatusNote("Khách yêu cầu giao nhanh");
        adminPage.changeOrderStatusTo("Đã xác nhận");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Doi trang thai don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã cập nhật đơn hàng"),
                "Toast doi trang thai don phai co tieu de 'Đã cập nhật đơn hàng', dang thay: " + adminPage.getToastTitleText());
        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        adminPage.searchOrderByCodeAndOpen(orderCode);
        String detailText = adminPage.getDetailModalText(AdminPageUI.ADMIN_ORDER_DETAIL);
        Assert.assertTrue(detailText.contains("Khách yêu cầu giao nhanh"),
                "Mo lai trang chi tiet don " + orderCode + " thi lich su phai ghi ghi chu 'Khách yêu cầu giao nhanh', dang thay: " + detailText);
        Assert.assertTrue(detailText.contains("Đã xác nhận"),
                "Mo lai trang chi tiet don " + orderCode + " thi lich su phai ghi trang thai moi 'Đã xác nhận', dang thay: " + detailText);
        adminPage.closeOrderDetail();
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-144 (TC_SCRA03_FUNC_046/047/048, đợt 21 viết lại): Đơn Hoàn tất/Đã hủy/Đã hết hạn không đổi/hủy được nữa: cả 2 nút header [Hủy đơn] + [Đổi trạng thái] đều HIỆN nhưng DISABLE (có tooltip), không mở được panel nào (chỉ mở xem; skip từng trạng thái nếu DB không có đơn đó; skip cả test nếu không có đơn terminal nào)")
    public void adminTerminalOrdersCannotChangeStatus() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String[] terminalLabels = {"Hoàn tất", "Đã hủy", "Đã hết hạn"};
        int checked = 0;
        for (String label : terminalLabels) {
            String orderCode = openFirstOrderWithStatusOrNull(adminPage, label);
            if (orderCode == null) {
                continue;
            }
            checked++;
            Assert.assertTrue(adminPage.isOrderCancelButtonDisplayed(),
                    "Trang don '" + label + "' van phai hien nut [Hủy đơn] o header");
            Assert.assertFalse(adminPage.isOrderCancelButtonEnabled(),
                    "Trang don '" + label + "' (" + orderCode + ") phai DISABLE nut [Hủy đơn]");
            Assert.assertTrue(adminPage.isOrderChangeStatusButtonDisplayed(),
                    "Trang don '" + label + "' van phai hien nut [Đổi trạng thái] o header");
            Assert.assertFalse(adminPage.isOrderChangeStatusButtonEnabled(),
                    "Trang don '" + label + "' (" + orderCode + ") phai DISABLE nut [Đổi trạng thái]");
            Assert.assertFalse(adminPage.isOrderStatusSelectDisplayed(),
                    "Trang don '" + label + "' khong duoc co dropdown chon trang thai dich (panel khong mo duoc)");
            adminPage.closeOrderDetail();
            adminPage.clickToolbarReset("orders");
        }
        if (checked == 0) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai Hoan tat/Da huy/Da het han");
        }
    }

    @Test(description = "ADM19-145 (TC_SCRA03_FUNC_054, MUTATES — hủy 1 đơn thật, KHÔNG khôi phục được): Hủy đơn Chờ xử lý qua panel hủy đơn với lý do 'Khách đổi ý' → toast 'Đã hủy đơn hàng'; mở lại trang chi tiết thì hiện 'Lý do hủy: Khách đổi ý' + dòng Lịch sử 'Admin hủy đơn. Lý do: Khách đổi ý' (skip nếu không có đơn Chờ xử lý; chạy cùng ACR-07 thì tiêu thụ 2 đơn Pending)")
    public void adminCancelOrderShowsReasonAndHistoryAfterCancel() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Chờ xử lý");
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xử lý' de huy");
        }
        adminPage.cancelOrderWithReason("Khách đổi ý");
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Huy don xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã hủy đơn hàng"),
                "Toast huy don phai co tieu de 'Đã hủy đơn hàng', dang thay: " + adminPage.getToastTitleText());
        String detailText = adminPage.getDetailModalText(AdminPageUI.ADMIN_ORDER_DETAIL);
        Assert.assertTrue(detailText.contains("Lý do hủy: Khách đổi ý"),
                "Trang chi tiet don sau khi huy phai hien 'Lý do hủy: Khách đổi ý', dang thay: " + detailText);
        Assert.assertTrue(detailText.contains("Admin hủy đơn. Lý do: Khách đổi ý"),
                "Lich su trang thai sau khi huy phai ghi 'Admin hủy đơn. Lý do: Khách đổi ý', dang thay: " + detailText);
        adminPage.closeOrderDetail();
        adminPage.clickToolbarReset("orders");
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã hủy"),
                "Don " + orderCode + " sau khi huy phai hien 'Đã hủy' trong bang don");
    }

    @Story("Đơn hàng")
    @Test(description = "ADM19-146 (TC_SCRA03_FUNC_055, đợt 21 viết lại): Nút Hủy đơn DISABLE (không ẩn) với đơn từ Đang giao trở đi: mở trang chi tiết đơn 'Đang giao' thì header vẫn có [Hủy đơn] + [Đổi trạng thái], [Hủy đơn] DISABLE, [Đổi trạng thái] bấm được (chỉ mở xem; skip nếu không có đơn Đang giao)")
    public void adminOrderCancelButtonDisabledFromShippingOnwards() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = openFirstOrderWithStatusOrNull(adminPage, "Đang giao");
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Đang giao' de kiem tra nut Huy don");
        }
        java.util.List<String> headerTexts = adminPage.getOrderDetailHeaderButtonTexts();
        Assert.assertEquals(headerTexts.size(), 2,
                "Header chi tiet don 'Đang giao' phai CHI co dung 2 nut, dang thay: " + headerTexts);
        Assert.assertTrue(headerTexts.contains("Hủy đơn") && headerTexts.contains("Đổi trạng thái"),
                "Header chi tiet don 'Đang giao' phai la [Hủy đơn] + [Đổi trạng thái], dang thay: " + headerTexts);
        Assert.assertTrue(adminPage.isOrderCancelButtonDisplayed(),
                "Trang don 'Đang giao' van phai hien nut Huy don (admin-order-cancel-btn)");
        Assert.assertFalse(adminPage.isOrderCancelButtonEnabled(),
                "Trang don 'Đang giao' (" + orderCode + ") phai DISABLE nut 'Hủy đơn' (dot 21: huy chi o Pending/Confirmed)");
        Assert.assertTrue(adminPage.isOrderChangeStatusButtonEnabled(),
                "Trang don 'Đang giao' phai bam duoc nut [Đổi trạng thái]");
        adminPage.closeOrderDetail();
    }

    // ---------- Tra hang (SCR_10, muc 'returns') ----------

    @Story("Trả hàng")
    @Test(description = "ADM19-147 (TC_SCRA10_UI_001 + TC_SCRA10_UI_002): Toolbar mục Trả hàng: ô tìm 'Tìm mã đơn, sản phẩm, lý do...' + icon Tìm (Lucide Search) + icon Đặt lại (RotateCcw), dropdown trạng thái có 4 lựa chọn (Tất cả/Chờ duyệt/Đã duyệt/Đã từ chối), KHÔNG có nút Làm mới/Thêm mới/Export, không nút 'Lọc' dạng text")
    public void returnsToolbarHasSearchStatusFilterAndNoExport() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("returns");

        Assert.assertEquals(adminPage.getReturnsSearchPlaceholder(), "Tìm mã đơn, sản phẩm, lý do...",
                "O tim kiem Tra hang phai co placeholder 'Tìm mã đơn, sản phẩm, lý do...'");
        Assert.assertTrue(adminPage.isToolbarSearchIconDisplayed("returns"),
                "Nut Tim o toolbar Tra hang phai chua icon Lucide Search (svg.lucide-search)");
        Assert.assertTrue(adminPage.isToolbarResetIconDisplayed("returns"),
                "Nut Dat lai o toolbar Tra hang phai chua icon Lucide RotateCcw (svg.lucide-rotate-ccw)");
        java.util.List<String> options = adminPage.getReturnsStatusFilterOptionTexts();
        Assert.assertEquals(options, java.util.Arrays.asList(
                        "Tất cả trạng thái", "Chờ duyệt", "Đã duyệt", "Đã từ chối"),
                "Dropdown loc trang thai tra hang phai co dung 4 lua chon, dang thay: " + options);
        Assert.assertFalse(adminPage.isToolbarAddButtonDisplayed("returns"),
                "Toolbar Tra hang khong duoc co nut Them moi");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Dot 18 da bo han Export — toolbar Tra hang khong duoc co nut Export");
        Assert.assertFalse(adminPage.getToolbarText().contains("Làm mới"),
                "Toolbar Tra hang khong duoc co nut 'Làm mới'");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-148 (TC_SCRA10_UI_003): Bảng yêu cầu trả hàng có đủ 8 cột STT/Mã đơn/Sản phẩm/SL/Lý do/Trạng thái/Hoàn tiền/Thao tác; dòng có Badge trạng thái; cột Hoàn tiền hiện trạng thái hoàn + số tiền hoặc '—'; nút 'Duyệt / Từ chối' chỉ có ở dòng Chờ duyệt (skip nếu bảng trống)")
    public void returnsTableLayoutShowsColumnsAndRefundStatus() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");
        if (adminPage.isEmptyReturnsMessageDisplayed() || adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co yeu cau tra hang nao de kiem tra bo cuc bang");
        }

        java.util.List<String> headers = adminPage.getAdminTableHeaders();
        Assert.assertEquals(headers, java.util.Arrays.asList(
                        "STT", "Mã đơn", "Sản phẩm", "SL", "Lý do", "Trạng thái", "Hoàn tiền", "Thao tác"),
                "Tieu de cot bang Tra hang phai dung thu tu STT/Mã đơn/Sản phẩm/SL/Lý do/Trạng thái/Hoàn tiền/Thao tác, dang thay: " + headers);
        java.util.List<String> firstCells = adminPage.getFirstTableRowCellTexts();
        Assert.assertEquals(firstCells.size(), 8, "Dong tra hang phai co dung 8 o, dang thay: " + firstCells.size());
        Assert.assertTrue(firstCells.get(5).contains("Chờ duyệt") || firstCells.get(5).contains("Đã duyệt")
                        || firstCells.get(5).contains("Đã từ chối"),
                "O Trang thai phai hien Badge 'Chờ duyệt'/'Đã duyệt'/'Đã từ chối', dang thay: " + firstCells.get(5));
        Assert.assertTrue(firstCells.get(6).contains("PendingRefund") || firstCells.get(6).contains("Refunded")
                        || firstCells.get(6).trim().equals("—") || firstCells.get(6).contains("₫"),
                "O Hoan tien phai hien trang thai hoan + so tien hoac '—', dang thay: " + firstCells.get(6));

        boolean sawPending = false;
        boolean sawNonPending = false;
        for (String row : adminPage.getAdminTableRowTexts()) {
            if (row.contains("Chờ duyệt")) {
                sawPending = true;
                Assert.assertTrue(row.contains("Duyệt / Từ chối"),
                        "Dong 'Chờ duyệt' phai co nut 'Duyệt / Từ chối', dang thay: " + row);
            } else {
                sawNonPending = true;
                Assert.assertFalse(row.contains("Duyệt / Từ chối"),
                        "Dong khong o trang thai 'Chờ duyệt' khong duoc co nut 'Duyệt / Từ chối', dang thay: " + row);
            }
        }
        Assert.assertTrue(sawPending || sawNonPending, "Bang tra hang phai co it nhat 1 dong de doi chieu");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-149 (TC_SCRA10_FUNC_004 + TC_SCRA10_UI_005): Phân trang bảng Trả hàng: >10 yêu cầu thì trang 1 hiện 'Hiển thị 1–10 / N kết quả' + 'Trang 1 / 2'; bấm 'Sau' sang trang 2 hiện các dòng còn lại, STT trang 2 bắt đầu từ 11 (skip nếu ≤10 yêu cầu)")
    public void returnsPaginationShowsTenPerPageAndContinuousStt() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");
        int total = adminPage.getPaginationTotalRows("returns");
        if (total <= 10) {
            throw new SkipException("Bo qua: DB nay chi co " + total + " yeu cau tra hang (can >10 de kiem tra phan trang)");
        }

        String page1Text = adminPage.getPaginationText("returns");
        Assert.assertTrue(page1Text.contains("Hiển thị 1–10 / " + total + " kết quả"),
                "Trang 1 phai hien 'Hiển thị 1–10 / " + total + " kết quả', dang thay: " + page1Text);
        Assert.assertTrue(page1Text.contains("Trang 1 / 2"),
                "Trang 1 phai hien 'Trang 1 / 2', dang thay: " + page1Text);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 10,
                "Trang 1 phai hien dung toi da 10 dong");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");

        adminPage.clickPaginationNext("returns");
        Assert.assertTrue(adminPage.getPaginationText("returns").contains("Trang 2 / 2"),
                "Sang trang 2 phai hien 'Trang 2 / 2'");
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai la 11 (lien tuc theo trang, khong danh lai tu 1)");

        adminPage.clickPaginationPrev("returns");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "Ve trang 1 thi STT dong dau phai lai la 1");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-150 (TC_SCRA10_FUNC_007/008/009 + TC_SCRA10_FUNC_013, dot 21): Lọc yêu cầu trả hàng theo trạng thái Chờ duyệt/Đã duyệt/Đã từ chối: chỉ chọn dropdown thì bảng LỌC NGAY (client-side, KHÔNG còn nút Tìm để bấm); bấm Đặt lại thì ô tìm trống + dropdown về 'Tất cả trạng thái' + về trang 1 + bảng hiện lại toàn bộ yêu cầu đã tải (skip từng trạng thái nếu không có dòng ở trạng thái đó)")
    public void returnsStatusFilterAppliesOnlyAfterSearchAndResetDoesNotReload() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");
        if (adminPage.isEmptyReturnsMessageDisplayed() || adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co yeu cau tra hang nao de kiem tra loc trang thai");
        }
        int totalRows = adminPage.getPaginationTotalRows("returns");
        String[][] filters = {{"Pending", "Chờ duyệt"}, {"Approved", "Đã duyệt"}, {"Rejected", "Đã từ chối"}};
        for (String[] filter : filters) {
            String value = filter[0];
            String label = filter[1];
            // Dot 21: chi chon dropdown la bang loc ngay (khong can bam Tim).
            adminPage.selectReturnsStatusFilter(value);
            if (adminPage.getAdminTableRowCount() == 0) {
                Assert.assertTrue(adminPage.isReturnsNotFoundMessageDisplayed(),
                        "Loc '" + label + "' khong co dong thi phai hien 'Không tìm thấy yêu cầu trả hàng'");
            } else {
                boolean allMatch = adminPage.getAdminTableRowTexts().stream()
                        .allMatch(text -> text.contains(label));
                Assert.assertTrue(allMatch,
                        "Loc '" + label + "' thi moi dong deu phai o trang thai '" + label + "'");
            }
            // Ve lai tat ca truoc khi loc trang thai tiep theo.
            adminPage.clickToolbarReset("returns");
            adminPage.sleepInSecond(1);
        }

        // Nut Dat lai: o tim/dropdown ve mac dinh, ve trang 1, bang hien lai toan bo.
        adminPage.selectReturnsStatusFilter("Rejected");
        adminPage.clickToolbarReset("returns");
        Assert.assertEquals(adminPage.getReturnsSearchValue(), "",
                "Bam Dat lai thi o tim kiem tra hang phai trong");
        Assert.assertEquals(adminPage.getReturnsStatusFilterValue(), "",
                "Bam Dat lai thi dropdown loc trang thai tra hang phai ve 'Tất cả trạng thái' (value='')");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("returns"), 1,
                "Bam Dat lai thi phan trang tra hang phai ve trang 1");
        Assert.assertEquals(adminPage.getPaginationTotalRows("returns"), totalRows,
                "Bam Dat lai thi bang phai hien lai toan bo " + totalRows + " yeu cau tra hang");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-151 (TC_SCRA10_FUNC_010/011/012): Tìm yêu cầu trả hàng theo mã đơn / tên sản phẩm / lý do trả (lấy động từ dòng đầu, không phân biệt hoa thường): nhập từ khóa thì bảng chỉ còn dòng khớp (lọc client-side, không cần bấm Tìm); xóa từ khóa thì hiện lại toàn bộ (skip nếu bảng trống)")
    public void returnsKeywordSearchFiltersByCodeProductAndReason() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");
        if (adminPage.isEmptyReturnsMessageDisplayed() || adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co yeu cau tra hang nao de kiem tra tim kiem");
        }
        int total = adminPage.getPaginationTotalRows("returns");
        java.util.List<String> firstCells = adminPage.getFirstTableRowCellTexts();
        String orderCode = firstCells.get(1).trim();
        String productName = firstCells.get(2).trim();
        String reason = firstCells.get(4).split("\n")[0].trim();

        adminPage.setReturnsSearch(orderCode.toLowerCase());
        Assert.assertTrue(adminPage.getAdminTableRowCount() >= 1,
                "Tim theo ma don '" + orderCode + "' (go chu thuong) thi phai con it nhat 1 dong khop");
        Assert.assertTrue(adminPage.getAdminTableRowTexts().stream().allMatch(row -> row.contains(orderCode)),
                "Tim theo ma don thi moi dong con lai deu phai chua ma '" + orderCode + "'");
        adminPage.setReturnsSearch("");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), Math.min(10, total),
                "Xoa tu khoa thi bang tra hang phai hien lai toan bo");

        adminPage.setReturnsSearch(productName);
        Assert.assertTrue(adminPage.getAdminTableRowCount() >= 1,
                "Tim theo ten san pham '" + productName + "' thi phai con it nhat 1 dong khop");
        Assert.assertTrue(adminPage.getAdminTableRowTexts().stream().allMatch(row -> row.contains(productName)),
                "Tim theo ten san pham thi moi dong con lai deu phai chua '" + productName + "'");
        adminPage.setReturnsSearch("");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), Math.min(10, total),
                "Xoa tu khoa thi bang tra hang phai hien lai toan bo");

        adminPage.setReturnsSearch(reason);
        Assert.assertTrue(adminPage.getAdminTableRowCount() >= 1,
                "Tim theo ly do tra '" + reason + "' thi phai con it nhat 1 dong khop");
        Assert.assertTrue(adminPage.getAdminTableRowTexts().stream().allMatch(row -> row.contains(reason)),
                "Tim theo ly do tra thi moi dong con lai deu phai chua '" + reason + "'");
        adminPage.setReturnsSearch("");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-152 (TC_SCRA10_FUNC_015, dot 21): Nội dung TRANG full page chi tiết yêu cầu trả hàng: hiện đủ Sản phẩm × số lượng, Mã đơn, Lý do khách, Số tiền hoàn, Trạng thái hoàn tiền, Trạng thái (+ Ghi chú khách/Ghi chú admin khi có); KHÔNG còn dòng hướng dẫn duyệt/từ chối của Drawer cũ; header CHỈ 2 nút [Từ chối][Duyệt], KHÔNG có ô nhập ghi chú admin, KHÔNG có nút Hủy/Đóng. Về danh sách bằng breadcrumb, không xử lý gì (skip nếu không có yêu cầu Chờ duyệt)")
    public void returnDetailDrawerShowsFullContentAndGuide() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingReturnOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co yeu cau tra hang nao o trang thai 'Chờ duyệt' de mo chi tiet");
        }
        // (Khong doc ten SP/SL tu dong dau tien — dong Chờ duyệt chua chac la dong dau;
        // chi khang dinh dang 'SP × SL' chung trong trang chi tiet.)
        adminPage.clickFirstReturnReview();
        Assert.assertTrue(adminPage.isReturnDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet yeu cau tra hang full page");
        String pageText = adminPage.getReturnDetailText();
        Assert.assertTrue(pageText.contains(orderCode),
                "Trang chi tiet tra hang phai hien ma don '" + orderCode + "', dang thay: " + pageText);
        Assert.assertTrue(pageText.contains(" × "),
                "Trang chi tiet tra hang phai hien 'San pham × so luong', dang thay: " + pageText);
        Assert.assertTrue(pageText.contains("Lý do khách") && pageText.contains("Số tiền hoàn")
                        && pageText.contains("Trạng thái hoàn tiền") && pageText.contains("Trạng thái"),
                "Trang chi tiet tra hang phai hien du cac nhan 'Lý do khách', 'Số tiền hoàn', 'Trạng thái hoàn tiền', 'Trạng thái', dang thay: " + pageText);
        // Dot 21: trang full page KHONG con dong huong dan duyet/tu choi cua Drawer cu.
        Assert.assertFalse(pageText.contains("Duyệt: hoàn kho + tạo bản ghi hoàn tiền"),
                "Trang chi tiet tra hang dot 21 khong duoc con dong huong dan duyet/tu choi cua Drawer cu");
        java.util.List<String> headerTexts = adminPage.getPageHeadButtonTexts("admin-return-detail-page");
        Assert.assertEquals(headerTexts.size(), 2,
                "Header chi tiet tra hang phai CHI co dung 2 nut, dang thay: " + headerTexts);
        Assert.assertTrue(headerTexts.contains("Từ chối") && headerTexts.contains("Duyệt"),
                "Header chi tiet tra hang phai la [Từ chối] + [Duyệt], dang thay: " + headerTexts);
        Assert.assertFalse(pageText.contains("Ghi chú admin") && pageText.contains("admin-return-admin-note"),
                "Trang chi tiet tra hang khong duoc co o nhap ghi chu admin");
        adminPage.closeReturnDetailPage();
        Assert.assertFalse(adminPage.isReturnDetailPageDisplayed(),
                "Ve danh sach xong khong duoc con thay chi tiet tra hang");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-153 (TC_SCRA10_FUNC_016 + TC_SCRA10_FUNC_017, MUTATES — duyệt 1 yêu cầu thật, KHÔNG khôi phục được, dot 21): Duyệt yêu cầu trả hàng qua PANEL inline 'Duyệt yêu cầu trả hàng?' (nêu tên SP × số lượng + mã đơn, đúng 2 nút [Hủy][Duyệt yêu cầu] — KHÔNG còn popup E1); xác nhận thì toast 'Đã duyệt yêu cầu trả hàng', trang chi tiết VẪN MỞ (hiển thị 'Đã duyệt'), về danh sách rồi dòng chuyển 'Đã duyệt' + cột Hoàn tiền hiện PendingRefund + xuất hiện nút 'Ghi nhận đã hoàn tiền' (skip nếu không có yêu cầu Chờ duyệt; hoàn kho/điểm/voucher của BE không kiểm chứng được qua UI)")
    public void approveReturnRequestCreatesPendingRefund() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingReturnOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co yeu cau tra hang nao o trang thai 'Chờ duyệt' de duyet");
        }
        // (Khong doc ten SP/SL tu dong dau tien — dong Chờ duyệt chua chac la dong dau;
        // panel duyet chi can khang dinh co ma don + dang 'SP × SL'.)
        adminPage.clickFirstReturnReview();
        Assert.assertTrue(adminPage.isReturnDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet yeu cau tra hang full page");
        adminPage.clickReturnApproveButton();
        Assert.assertTrue(adminPage.isReturnApprovePanelDisplayed(),
                "Bam 'Duyệt' phai mo panel xac nhan duyet (admin-return-approve-panel)");
        String panelText = adminPage.getReturnDetailText();
        Assert.assertTrue(panelText.contains("Duyệt yêu cầu trả hàng?"),
                "Panel duyet phai co tieu de 'Duyệt yêu cầu trả hàng?', dang thay: " + panelText);
        Assert.assertTrue(panelText.contains(orderCode),
                "Panel duyet phai neu ma don '" + orderCode + "', dang thay: " + panelText);
        Assert.assertTrue(panelText.contains(" × "),
                "Panel duyet phai neu ten SP × so luong, dang thay: " + panelText);

        adminPage.clickReturnApproveConfirmButton();
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Duyet tra hang xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã duyệt yêu cầu trả hàng"),
                "Toast duyet tra hang phai co tieu de 'Đã duyệt yêu cầu trả hàng', dang thay: " + adminPage.getToastTitleText());
        // Dot 21: duyet xong trang chi tiet VAN MO (resolveReturn chi reload list).
        Assert.assertTrue(adminPage.isReturnDetailPageDisplayed(),
                "Duyet xong trang chi tiet tra hang dot 21 phai VAN MO (khong dong nhu Drawer cu)");
        Assert.assertTrue(adminPage.getReturnDetailText().contains("Đã duyệt"),
                "Duyet xong trang chi tiet phai hien trang thai 'Đã duyệt'");

        adminPage.closeReturnDetailPage();
        adminPage.setReturnsSearch(orderCode);
        java.util.List<String> rows = adminPage.getAdminTableRowTexts();
        Assert.assertEquals(rows.size(), 1,
                "Tim theo ma don '" + orderCode + "' sau khi duyet thi chi duoc con dung 1 dong");
        Assert.assertTrue(rows.get(0).contains("Đã duyệt"),
                "Dong tra hang sau khi duyet phai chuyen 'Đã duyệt', dang thay: " + rows.get(0));
        Assert.assertTrue(rows.get(0).contains("PendingRefund"),
                "Dong tra hang sau khi duyet thi cot Hoan tien phai hien PendingRefund, dang thay: " + rows.get(0));
        Assert.assertTrue(rows.get(0).contains("Ghi nhận đã hoàn tiền"),
                "Dong tra hang sau khi duyet phai xuat hien nut 'Ghi nhận đã hoàn tiền', dang thay: " + rows.get(0));
        adminPage.setReturnsSearch("");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-154 (TC_SCRA10_VAL_018 + TC_SCRA10_FUNC_019 + TC_SCRA10_FUNC_020, MUTATES — từ chối 1 yêu cầu thật, KHÔNG khôi phục được, dot 21): Từ chối yêu cầu trả hàng qua PANEL inline (cam, văn bản E2 tái dùng — KHÔNG còn popup E2): để trống lý do rồi bấm 'Từ chối yêu cầu' thì lỗi 'Vui lòng nhập lý do từ chối.', panel vẫn mở, yêu cầu giữ Chờ duyệt; nhập lý do rồi xác nhận thì toast 'Đã từ chối yêu cầu trả hàng', dòng chuyển 'Đã từ chối' kèm ghi chú admin, KHÔNG có nút 'Ghi nhận đã hoàn tiền'; đơn gốc quay về Đã giao (kiểm qua tab Đơn hàng) (skip nếu không có yêu cầu Chờ duyệt)")
    public void rejectReturnRequestRequiresReasonThenRejects() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingReturnOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co yeu cau tra hang nao o trang thai 'Chờ duyệt' de tu choi");
        }
        String rejectReason = "Hàng lỗi, khách không nhận";
        adminPage.clickFirstReturnReview();
        Assert.assertTrue(adminPage.isReturnDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet yeu cau tra hang full page");
        adminPage.clickReturnRejectButton();
        Assert.assertTrue(adminPage.isReturnRejectPanelDisplayed(),
                "Bam 'Từ chối' phai mo panel xac nhan tu choi (admin-return-reject-panel)");
        Assert.assertTrue(adminPage.getReturnDetailText().contains("Từ chối yêu cầu trả hàng?"),
                "Panel tu choi phai co tieu de 'Từ chối yêu cầu trả hàng?'");
        Assert.assertTrue(adminPage.isElementDisplayedWithWait(AdminPageUI.ADMIN_RETURN_REJECT_REASON_INPUT, 10),
                "Panel tu choi phai co o 'Lý do từ chối' bat buoc (admin-return-reject-reason-input)");

        // (1) De trong ly do + bam xac nhan -> loi, panel van mo, yeu cau giu Chờ duyệt.
        adminPage.clickReturnRejectConfirmButton();
        Assert.assertEquals(adminPage.getReturnRejectErrorText(), "Vui lòng nhập lý do từ chối.",
                "Tu choi khong nhap ly do thi phai bao 'Vui lòng nhập lý do từ chối.'");
        Assert.assertTrue(adminPage.isReturnRejectPanelDisplayed(),
                "Chua nhap ly do thi panel tu choi phai van mo");

        // (2) Nhap ly do + xac nhan -> yeu cau chuyen 'Đã từ chối' kem ghi chu admin.
        adminPage.setReturnRejectReason(rejectReason);
        adminPage.clickReturnRejectConfirmButton();
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Tu choi tra hang xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã từ chối yêu cầu trả hàng"),
                "Toast tu choi tra hang phai co tieu de 'Đã từ chối yêu cầu trả hàng', dang thay: " + adminPage.getToastTitleText());
        // Dot 21: tu choi xong trang chi tiet VAN MO.
        Assert.assertTrue(adminPage.isReturnDetailPageDisplayed(),
                "Tu choi xong trang chi tiet tra hang dot 21 phai VAN MO (khong dong nhu Drawer cu)");

        adminPage.closeReturnDetailPage();
        adminPage.setReturnsSearch(orderCode);
        java.util.List<String> rows = adminPage.getAdminTableRowTexts();
        Assert.assertEquals(rows.size(), 1,
                "Tim theo ma don '" + orderCode + "' sau khi tu choi thi chi duoc con dung 1 dong");
        Assert.assertTrue(rows.get(0).contains("Đã từ chối"),
                "Dong tra hang sau khi tu choi phai chuyen 'Đã từ chối', dang thay: " + rows.get(0));
        Assert.assertTrue(rows.get(0).contains(rejectReason),
                "Dong tra hang sau khi tu choi phai hien ghi chu admin '" + rejectReason + "', dang thay: " + rows.get(0));
        Assert.assertFalse(rows.get(0).contains("Ghi nhận đã hoàn tiền"),
                "Dong tra hang sau khi tu choi khong duoc co nut 'Ghi nhận đã hoàn tiền', dang thay: " + rows.get(0));
        adminPage.setReturnsSearch("");

        // Don goc quay ve 'Đã giao' (kiem qua tab Don hang).
        adminPage.clickAdminNav("orders");
        adminPage.clickToolbarReset("orders");
        adminPage.searchOrderByCodeAndOpen(orderCode);
        Assert.assertTrue(adminPage.getOrderDetailTitleText().startsWith("Đơn " + orderCode),
                "Sau khi tu choi tra hang, don goc " + orderCode + " phai mo dung trang chi tiet");
        Assert.assertEquals(adminPage.getOrderDetailStatusPillText(), "Đã giao",
                "Sau khi tu choi tra hang, don goc " + orderCode + " phai quay ve 'Đã giao'");
        adminPage.closeOrderDetail();
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-155 (TC_SCRA10_FUNC_021, dot 21): Mở trang chi tiết yêu cầu trả hàng Chờ duyệt rồi về danh sách bằng breadcrumb [Trả hàng] thì trang đóng và dòng yêu cầu VẪN ở 'Chờ duyệt' (không gọi POST resolve) (skip nếu không có yêu cầu Chờ duyệt)")
    public void returnDetailDrawerCloseByXLeavesRequestPending() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingReturnOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co yeu cau tra hang nao o trang thai 'Chờ duyệt' de mo roi dong");
        }
        adminPage.clickFirstReturnReview();
        Assert.assertTrue(adminPage.isReturnDetailPageDisplayed(),
                "Phai mo duoc trang chi tiet yeu cau tra hang full page");
        adminPage.closeReturnDetailPage();
        Assert.assertFalse(adminPage.isReturnDetailPageDisplayed(),
                "Ve danh sach bang breadcrumb xong khong duoc con thay chi tiet tra hang");

        adminPage.setReturnsSearch(orderCode);
        java.util.List<String> rows = adminPage.getAdminTableRowTexts();
        Assert.assertEquals(rows.size(), 1,
                "Tim theo ma don '" + orderCode + "' thi chi duoc con dung 1 dong");
        Assert.assertTrue(rows.get(0).contains("Chờ duyệt"),
                "Ve danh sach bang breadcrumb thi yeu cau '" + orderCode + "' phai VAN o 'Chờ duyệt' (khong goi POST resolve), dang thay: " + rows.get(0));
        adminPage.setReturnsSearch("");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-156 (TC_SCRA10_FUNC_022): Nút 'Ghi nhận đã hoàn tiền' chỉ hiện khi đã duyệt + chờ hoàn tiền + có refundId: dòng Đã duyệt + PendingRefund nhưng refundId null KHÔNG có nút này; dòng Chờ duyệt KHÔNG có nút này (skip từng dòng nếu không có dòng tương ứng trên DB)")
    public void refundRecordButtonAbsentWithoutRefundIdOrWhenPending() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");
        if (adminPage.isEmptyReturnsMessageDisplayed() || adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: DB nay chua co yeu cau tra hang nao de kiem tra su vang mat cua nut ghi nhan hoan tien");
        }
        String approvedWithoutButton = adminPage.findReturnsRowWithoutRefundButton("Đã duyệt", "PendingRefund");
        if (approvedWithoutButton != null) {
            Assert.assertFalse(adminPage.hasRefundRecordButtonInRow(approvedWithoutButton),
                    "Dong 'Đã duyệt' + PendingRefund nhung refundId null (" + approvedWithoutButton + ") khong duoc co nut 'Ghi nhận đã hoàn tiền'");
        }
        String pendingCode = null;
        for (String row : adminPage.getAdminTableRowTexts()) {
            if (row.contains("Chờ duyệt")) {
                pendingCode = extractOrderDisplayCode(row);
                break;
            }
        }
        if (pendingCode != null) {
            Assert.assertFalse(adminPage.hasRefundRecordButtonInRow(pendingCode),
                    "Dong 'Chờ duyệt' (" + pendingCode + ") khong duoc co nut 'Ghi nhận đã hoàn tiền'");
        }
        if (approvedWithoutButton == null && pendingCode == null) {
            throw new SkipException("Bo qua: DB nay khong co dong 'Đã duyệt' + PendingRefund thieu refundId nao va cung khong co dong 'Chờ duyệt' de doi chieu");
        }
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-157 (TC_SCRA10_FUNC_023, MUTATES — ghi nhận hoàn tiền thật, KHÔNG khôi phục được): Bấm 'Ghi nhận đã hoàn tiền' mở Drawer admin-refund-record-modal hiện đúng SP × số lượng + số tiền hoàn; nhập 'TX123456', bấm 'Xác nhận đã hoàn tiền' thì toast 'Đã ghi nhận hoàn tiền.', drawer đóng, dòng sang Refunded và không còn nút ghi nhận (skip nếu không có dòng Approved + PendingRefund + refundId)")
    public void recordRefundWithValidTransactionCode() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");
        if (!adminPage.isRefundRecordButtonDisplayed()) {
            throw new SkipException("Bo qua: DB nay khong co yeu cau tra hang nao o trang thai Approved + PendingRefund (co refundId) de ghi nhan hoan tien");
        }
        String orderCode = extractOrderDisplayCode(adminPage.getRefundRecordButtonRowText());
        Assert.assertNotNull(orderCode, "Dong co nut ghi nhan hoan tien phai co ma don");
        adminPage.clickFirstRefundRecordButton();
        Assert.assertTrue(adminPage.isRefundRecordModalDisplayed(),
                "Bam nut phai mo Drawer ghi nhan hoan tien (admin-refund-record-modal + o ma giao dich + nut xac nhan)");
        String modalText = adminPage.getDetailModalText(AdminPageUI.ADMIN_REFUND_RECORD_MODAL);
        Assert.assertTrue(modalText.contains(orderCode),
                "Drawer ghi nhan hoan tien phai hien ma don '" + orderCode + "', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains(" × ") && modalText.contains("₫"),
                "Drawer ghi nhan hoan tien phai hien SP × so luong + so tien hoan, dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("3–100 ký tự"),
                "Drawer ghi nhan hoan tien phai co chu thich ma giao dich 3–100 ky tu, dang thay: " + modalText);

        adminPage.setRefundTransactionCode("TX123456");
        adminPage.clickRefundRecordConfirm();
        Assert.assertTrue(adminPage.isToastDisplayed(),
                "Ghi nhan hoan tien xong phai hien toast thanh cong (admin-toast)");
        Assert.assertTrue(adminPage.getToastTitleText().contains("Đã ghi nhận hoàn tiền"),
                "Toast ghi nhan hoan tien phai co tieu de 'Đã ghi nhận hoàn tiền', dang thay: " + adminPage.getToastTitleText());
        Assert.assertFalse(adminPage.isRefundRecordModalDisplayed(),
                "Ghi nhan xong Drawer ghi nhan hoan tien phai dong");

        adminPage.setReturnsSearch(orderCode);
        java.util.List<String> rows = adminPage.getAdminTableRowTexts();
        Assert.assertEquals(rows.size(), 1,
                "Tim theo ma don '" + orderCode + "' sau khi ghi nhan thi chi duoc con dung 1 dong");
        Assert.assertTrue(rows.get(0).contains("Refunded"),
                "Dong tra hang sau khi ghi nhan phai sang Refunded, dang thay: " + rows.get(0));
        Assert.assertFalse(rows.get(0).contains("Ghi nhận đã hoàn tiền"),
                "Dong tra hang sau khi ghi nhan khong duoc con nut 'Ghi nhận đã hoàn tiền', dang thay: " + rows.get(0));
        adminPage.setReturnsSearch("");
    }

    @Story("Trả hàng")
    @Test(description = "ADM19-158 (TC_SCRA10_VAL_024 + TC_SCRA10_VAL_025 + TC_SCRA10_FUNC_026): Ghi nhận hoàn tiền: mã giao dịch 2 ký tự 'TX' thì lỗi 'Mã giao dịch hoàn tiền phải có từ 3 đến 100 ký tự.', drawer vẫn mở; mã 101 ký tự thì cùng lỗi; nhập mã hợp lệ rồi bấm Hủy thì drawer đóng, bản ghi giữ PendingRefund (chỉ assert lỗi rồi đóng, KHÔNG ghi nhận thật; skip nếu không có dòng Approved + PendingRefund + refundId)")
    public void refundRecordValidationAndCancel() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("returns");
        if (!adminPage.isRefundRecordButtonDisplayed()) {
            throw new SkipException("Bo qua: DB nay khong co yeu cau tra hang nao o trang thai Approved + PendingRefund (co refundId) de kiem tra validation ghi nhan hoan tien");
        }
        String orderCode = extractOrderDisplayCode(adminPage.getRefundRecordButtonRowText());
        Assert.assertNotNull(orderCode, "Dong co nut ghi nhan hoan tien phai co ma don");
        adminPage.clickFirstRefundRecordButton();
        Assert.assertTrue(adminPage.isRefundRecordModalDisplayed(),
                "Bam nut phai mo Drawer ghi nhan hoan tien (admin-refund-record-modal)");
        String expectedError = "Mã giao dịch hoàn tiền phải có từ 3 đến 100 ký tự.";

        adminPage.setRefundTransactionCode("TX");
        adminPage.clickRefundRecordConfirm();
        Assert.assertEquals(adminPage.getRefundRecordErrorText(), expectedError,
                "Ma giao dich 2 ky tu thi phai bao dung loi tren");
        Assert.assertTrue(adminPage.isRefundRecordModalDisplayed(),
                "Nhap ma giao dich qua ngan thi Drawer ghi nhan hoan tien phai van mo (khong goi POST record)");

        adminPage.setRefundTransactionCode("TX" + "1".repeat(99));
        adminPage.clickRefundRecordConfirm();
        Assert.assertEquals(adminPage.getRefundRecordErrorText(), expectedError,
                "Ma giao dich 101 ky tu thi phai bao dung loi tren");
        Assert.assertTrue(adminPage.isRefundRecordModalDisplayed(),
                "Nhap ma giao dich qua dai thi Drawer ghi nhan hoan tien phai van mo (khong goi POST record)");

        adminPage.setRefundTransactionCode("TX123456");
        adminPage.clickRefundRecordCancel();
        Assert.assertFalse(adminPage.isRefundRecordModalDisplayed(),
                "Bam 'Hủy' trong Drawer ghi nhan hoan tien thi Drawer phai dong");
        adminPage.setReturnsSearch(orderCode);
        java.util.List<String> rows = adminPage.getAdminTableRowTexts();
        Assert.assertEquals(rows.size(), 1,
                "Tim theo ma don '" + orderCode + "' thi chi duoc con dung 1 dong");
        Assert.assertTrue(rows.get(0).contains("PendingRefund"),
                "Bam 'Hủy' thi ban ghi hoan tien phai giu PendingRefund, dang thay: " + rows.get(0));
        Assert.assertTrue(rows.get(0).contains("Ghi nhận đã hoàn tiền"),
                "Bam 'Hủy' thi nut 'Ghi nhận đã hoàn tiền' phai van con, dang thay: " + rows.get(0));
        adminPage.setReturnsSearch("");
    }
}



