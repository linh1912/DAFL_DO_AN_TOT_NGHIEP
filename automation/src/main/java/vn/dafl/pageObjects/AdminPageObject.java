package vn.dafl.pageObjects;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import vn.dafl.commons.BasePage;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageUIs.AdminPageUI;

import java.util.ArrayList;
import java.util.List;

/** Page Object cho trang quan tri /admin (dot 3, chi Super Admin).
 *  Cac test dot 3-8 chi thao tac DOC (GET): mo trang, doc menu, chuyen muc, kiem tra
 *  o tim kiem hien thi. Tu dot 9, cac phuong thuc CRUD o cuoi file phuc vu lop
 *  AdminCrudTest: them/sua/xoa QUA GIAO DIEN nhung CHI tren du lieu do chinh test
 *  tao (ten/ma kem timestamp) va test tu don dep sau khi chay. Khong doi trang thai
 *  don, khong them/sua/xoa san pham/khuyen mai, khong khoa tai khoan, khong doi
 *  mat khau admin - tru khi doi tuong do chinh test tao ra.
 *  Dot 12: form them/sua San pham / Danh muc / Khuyen mai mo trong MODAL (testid
 *  admin-*-form-modal); toolbar chuan tren moi bang (nut Them moi ben trai, o tim
 *  + nut Tim/Dat lai/Lam moi/Export ben phai); phan trang client-side 10 dong/trang
 *  (admin-*-pagination); o search tren topbar + breadcrumb da bo han.
 *  Dot 17: form them/sua doi thanh TRANG RIENG 2 cot (class sa-form-page,
 *  role='dialog', khong overlay, bang danh sach thay the khoi DOM) nhung GIU
 *  testid container cu nen cac ham mo/dong form cu van dung; dong form dirty
 *  thi hoi 'Thoát mà không lưu?'; bang them cot STT + cot Ma + Switch trang thai
 *  (aria-checked); xoa dong dang Hoat dong bi chan boi popup 'Không thể xoá'
 *  (phai chuyen Ngung hoat dong truoc); tat voucher Active bang Switch mo modal
 *  nhap ly do (khong con nut 'Tắt khuyến mãi' rieng). */
public class AdminPageObject extends BasePage {

    public static final String[] ADMIN_NAV_KEYS = {
            "dashboard", "orders", "returns", "promotions", "products",
            "categories", "reviews", "customers", "membership", "logs"
    };

    /** Mo thang /admin tren app Admin rieng :3001 (khong qua menu header).
     *  KHONG dung /admin o web ban hang nua (test admin chi chay app Admin rieng). */
    public void openAdminPage() {
        openPageUrl(GlobalConstants.getAdminAppUrl() + "/admin");
    }

    /** Cho toi khi trang admin quyet dinh xong quyen: hoac menu quan tri, hoac trang 403,
     *  hoac bi chuyen ve /login (khach). */
    public void waitForAdminGateResolved() {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            if (isElementDisplayed(AdminPageUI.ADMIN_NAV_DASHBOARD)
                    || isElementDisplayed(AdminPageUI.ADMIN_FORBIDDEN)
                    || getCurrentPageUrl().contains("/login")) {
                return;
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Mo /admin 15 giay van chua ro quyen (khong thay menu, trang 403 hay /login)");
    }

    public boolean isAdminMenuDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_NAV_DASHBOARD, 10);
    }

    public boolean isForbiddenDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_FORBIDDEN, 10);
    }

    public String getForbiddenText() {
        waitForElementVisible(AdminPageUI.ADMIN_FORBIDDEN);
        return getElementText(AdminPageUI.ADMIN_FORBIDDEN);
    }

    /** Nhom dropdown chua 1 muc nav (theo NAV_GROUPS o AdminPage.tsx, dot 21);
     *  null = muc dung rieng, khong thuoc nhom nao (dashboard). */
    private String navGroupOf(String navKey) {
        switch (navKey) {
            case "orders":
            case "returns":
            case "promotions":
                return "sales";
            case "products":
            case "categories":
            case "reviews":
                return "products";
            case "customers":
            case "membership":
                return "customers";
            case "logs":
                return "system";
            default:
                return null;
        }
    }

    /** Nhom dropdown co dang mo khong (doc aria-expanded cua nut tieu de nhom). */
    public boolean isNavGroupExpanded(String groupKey) {
        String expanded = getElementAttribute(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_NAV_GROUP, groupKey), "aria-expanded");
        return "true".equals(expanded);
    }

    /** Mo nhom dropdown neu dang dong (dot 21: muc con bi visibility:hidden khi
     *  nhom dong nen Selenium khong bam duoc neu khong mo nhom truoc). */
    public void expandNavGroup(String groupKey) {
        if (!isElementDisplayed(AdminPageUI.DYNAMIC_ADMIN_NAV_GROUP, groupKey)) {
            return; // khong co sidebar (vd trang 403) — khong mo duoc nhom
        }
        if (!isNavGroupExpanded(groupKey)) {
            clickToElement(AdminPageUI.DYNAMIC_ADMIN_NAV_GROUP, groupKey);
            sleepInSecond(1); // cho hieu ung mo 200ms xong
        }
    }

    /** Mo het 4 nhom dropdown (dung truoc khi liet ke menu hien thi). */
    public void expandAllNavGroups() {
        for (String groupKey : new String[]{"sales", "products", "customers", "system"}) {
            expandNavGroup(groupKey);
        }
    }

    /** Danh sach key menu quan tri dang hien thi (du kien du 10 muc, dot 17.1 tach 'logs' khoi 'returns').
     *  Ghi chu dot 21: KHONG tu mo nhom o day — ket qua phai phan anh dung trang
     *  thai dong/mo hien tai (ADM-03/ADM-03D assert mac dinh dong + dong nhom thi an muc). */
    public List<String> getVisibleAdminNavKeys() {
        List<String> visible = new ArrayList<>();
        for (String key : ADMIN_NAV_KEYS) {
            if (isElementDisplayed(AdminPageUI.DYNAMIC_ADMIN_NAV, key)) {
                visible.add(key);
            }
        }
        return visible;
    }

    public void clickAdminNav(String navKey) {
        String groupKey = navGroupOf(navKey);
        if (groupKey != null) {
            expandNavGroup(groupKey);
        }
        waitForElementVisible(AdminPageUI.DYNAMIC_ADMIN_NAV, navKey);
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_NAV, navKey);
        sleepInSecond(2); // cho muc tai du lieu (lazy load theo tab)
    }

    public boolean isDashboardHeadingDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_DASHBOARD_HEADING, 10);
    }

    public boolean isOrderSearchDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_ORDER_SEARCH, 10);
    }

    public boolean isProductSearchDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_SEARCH, 10);
    }

    public boolean isCustomerSearchDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CUSTOMER_SEARCH, 10);
    }

    public boolean isPromoAddButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_ADD_BTN, 10);
    }

    public boolean isCategoryAddButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_ADD_BTN, 10);
    }

    // ---------- Dashboard moi (dot 18, chi doc - Dashboard.tsx component rieng) ----------
    // Dashboard cu (bieu do admin-revenue-chart + 2 nut chon khoang 7/30 ngay)
    // DA BO; dashboard moi co h1 'Tổng quan', 4-5 KPI (Doanh thu / Đơn hàng /
    // Khách hàng mới / Tỷ lệ hủy/trả hàng...), bo loc thoi gian segmented, khoi
    // 'Cần xử lý ngay' + nut 'Xem' tung muc (admin-dashboard-action-*-view).
    // Kiem tra sau o Vitest (AdminDashboard.test.tsx, 21 test) — Selenium chi
    // khang dinh khung + khoi hanh dong hien.
    public boolean isDashboardActionNeededDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_DASHBOARD_ACTION_NEEDED, 10);
    }

    /** Toan bo text cua trang Tong quan (dung de khang dinh KPI hien). */
    public String getDashboardText() {
        waitForElementVisible(AdminPageUI.ADMIN_DASHBOARD_HEADING);
        return getElementText("css=[data-testid='admin-dashboard']");
    }

    /** Bam nut 'Xem' o muc Don cho xu ly trong khoi 'Cần xử lý ngay' -> chuyen
     *  sang muc Don hang (menu PHANG wave D nen khong con chuyen tu mo nhom). */
    public void clickDashboardPendingOrdersView() {
        clickToElement(AdminPageUI.ADMIN_DASHBOARD_PENDING_ORDERS_VIEW);
        sleepInSecond(2); // cho tab Don hang tai du lieu
    }

    // ---------- Menu PHANG + badge dem (dot 18 wave D) ----------
    /** Ten hien thi cua tieu de nhom (sales/products/customers/system) —
     *  dang tieu de ('Bán hàng' / 'Quản lý sản phẩm' / 'Quản lý khách hàng' /
     *  'Hệ thống'), KHONG in hoa nhu truoc 17:42. */
    public String getNavGroupTitleText(String groupKey) {
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_NAV_GROUP, groupKey));
    }

    /** Tieu de nhom la <div> (khong phai <button>), khong co aria-expanded,
     *  khong co chevron/nut con ben trong — menu phang khong bam duoc. */
    public boolean isNavGroupTitleDiv(String groupKey) {
        String locator = getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_NAV_GROUP, groupKey);
        if (!isElementDisplayed(locator)) {
            return false;
        }
        org.openqa.selenium.WebElement el = getWebElement(locator);
        if (!"div".equalsIgnoreCase(el.getTagName())) {
            return false;
        }
        if (el.getAttribute("aria-expanded") != null) {
            return false;
        }
        return el.findElements(org.openqa.selenium.By.tagName("button")).isEmpty()
                && el.findElements(org.openqa.selenium.By.tagName("svg")).isEmpty();
    }

    /** Text badge dem cua 1 muc menu (orders/returns/reviews); null neu badge
     *  dang an (0 hoac chua co du lieu). Text co the la so hoac '99+'. */
    public String getNavBadgeText(String navKey) {
        if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_NAV_BADGE, navKey))) {
            return null;
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_NAV_BADGE, navKey)).trim();
    }

    // ---------- ConfirmDialog dung chung (dot 18) ----------
    /** Popup xac nhan ConfirmDialog co dang hien khong. */
    public boolean isConfirmDialogDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.CONFIRM_DIALOG, 10);
    }

    /** Tieu de popup xac nhan dang hien; "" neu khong co popup. */
    public String getConfirmDialogTitle() {
        if (!isElementDisplayed(AdminPageUI.CONFIRM_DIALOG)) {
            return "";
        }
        return getElementText(AdminPageUI.CONFIRM_DIALOG_TITLE);
    }

    /** Toan bo text popup xac nhan (tieu de + thong diep + ly do/loi neu co). */
    public String getConfirmDialogText() {
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        return getElementText(AdminPageUI.CONFIRM_DIALOG);
    }

    /** So nut o footer popup xac nhan — dot 18 quy dinh CHI DUNG 2 nut. */
    public int getConfirmDialogFooterButtonCount() {
        return getListWebElements(AdminPageUI.CONFIRM_DIALOG_FOOTER_BUTTONS).size();
    }

    /** Nhan nut xac nhan trong popup (vd 'Xóa sản phẩm', 'Ẩn sản phẩm', 'Hủy đơn hàng'). */
    public String getConfirmDialogConfirmText() {
        return getElementText(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
    }

    /** Nhan nut huy trong popup (thuong la 'Hủy', dirty-close la 'Ở lại'). */
    public String getConfirmDialogCancelText() {
        return getElementText(AdminPageUI.CONFIRM_DIALOG_CANCEL_BTN);
    }

    /** Bam nut xac nhan trong popup (chi bam, khong doi ket qua). */
    public void clickConfirmDialogConfirm() {
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
    }

    /** Bam nut huy trong popup (khong thuc hien hanh dong, khong goi API). */
    public void clickConfirmDialogCancel() {
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CANCEL_BTN);
    }

    /** Nhap ly do vao o ly do trong popup (requireReason: khoa KH / tat voucher /
     *  huy don / tu choi tra hang); o nay la <textarea>. */
    public void setConfirmDialogReason(String reason) {
        sendKeyToElement(AdminPageUI.CONFIRM_DIALOG_REASON_INPUT, reason);
    }

    /** Loi duoi o ly do trong popup (vd de trong ly do); "" neu khong co loi. */
    public String getConfirmDialogReasonErrorText() {
        if (!isElementDisplayed(AdminPageUI.CONFIRM_DIALOG_REASON_ERROR)) {
            return "";
        }
        return getElementText(AdminPageUI.CONFIRM_DIALOG_REASON_ERROR);
    }

    /** O ly do trong popup co dang hien khong. */
    public boolean isConfirmDialogReasonInputDisplayed() {
        return isElementDisplayed(AdminPageUI.CONFIRM_DIALOG_REASON_INPUT);
    }

    /** Bo dem ky tu cua o ly do (B1, spec A–H): dang "{n}/300"; "" neu popup
     *  khong co bo dem. */
    public String getConfirmDialogReasonCounterText() {
        if (!isElementDisplayed(AdminPageUI.CONFIRM_DIALOG_REASON_COUNTER)) {
            return "";
        }
        return getElementText(AdminPageUI.CONFIRM_DIALOG_REASON_COUNTER);
    }

    /** Popup chan (A3) co link phu "Xem sản phẩm trong danh mục" khong. */
    public boolean isConfirmDialogLinkDisplayed() {
        return isElementDisplayed(AdminPageUI.CONFIRM_DIALOG_LINK);
    }

    /** Nhan cua link phu trong popup chan (A3); "" neu khong co link. */
    public String getConfirmDialogLinkText() {
        if (!isElementDisplayed(AdminPageUI.CONFIRM_DIALOG_LINK)) {
            return "";
        }
        return getElementText(AdminPageUI.CONFIRM_DIALOG_LINK);
    }

    /** Bam link phu trong popup chan (A3) — dong popup, khong xac nhan. */
    public void clickConfirmDialogLink() {
        clickToElement(AdminPageUI.CONFIRM_DIALOG_LINK);
        sleepInSecond(1); // cho popup dong + dieu huong (neu co)
    }

    /** Popup chi co dung 1 nut xac nhan (A3 chan xoa DM, G4 phien het han). */
    public boolean isConfirmDialogSingleButton() {
        return getConfirmDialogFooterButtonCount() == 1;
    }

    // ---------- Toast moi (dot 18): admin-toast, tieu de + mo ta rieng ----------
    /** Tieu de toast dang hien (vd 'Đã xóa sản phẩm'); "" neu khong co toast. */
    public String getToastTitleText() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_TOAST_TITLE)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_TOAST_TITLE);
    }

    /** Mo ta toast dang hien; "" neu khong co. */
    public String getToastDescText() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_TOAST_DESC)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_TOAST_DESC);
    }

    /** So nut o footer form XEM / modal chi tiet co testid cho truoc — dot 18
     *  quy dinh form xem CHI [Đóng] + [Chỉnh sửa] (2 nut), modal tra hang CHI
     *  [Từ chối] + [Duyệt] (2 nut). */
    public int getDetailFooterButtonCount(String detailTestId) {
        return getListWebElements(AdminPageUI.DYNAMIC_FORM_FOOTER_BUTTONS, detailTestId).size();
    }

    /** Nhan cac nut o footer form XEM / modal chi tiet co testid cho truoc
     *  (theo thu tu hien thi). */
    public java.util.List<String> getDetailFooterButtonTexts(String detailTestId) {
        java.util.List<String> texts = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement btn : getListWebElements(
                AdminPageUI.DYNAMIC_FORM_FOOTER_BUTTONS, detailTestId)) {
            texts.add(btn.getText().trim());
        }
        return texts;
    }

    // ---------- Modal xem chi tiet (dot 8, chi doc - mo, doc, dong) ----------
    /** Ten/ma doi tuong o dong dau tien, doc tu aria-label cua nut mat
     *  (vd 'Xem chi tiết sản phẩm Son 3CE...' -> 'Son 3CE...'); tra null neu khong co dong nao. */
    public String getFirstDetailButtonLabel(String eyeButtonLocator, String ariaPrefix) {
        if (!isElementDisplayedWithWait(eyeButtonLocator, 10)) {
            return null;
        }
        String ariaLabel = getElementAttribute(eyeButtonLocator, "aria-label");
        if (ariaLabel == null || !ariaLabel.startsWith(ariaPrefix)) {
            return null;
        }
        return ariaLabel.substring(ariaPrefix.length()).trim();
    }

    public void clickFirstProductDetail() {
        clickToElement(AdminPageUI.FIRST_PRODUCT_DETAIL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_DETAIL);
    }

    /** Bam link 'Quản lý sản phẩm' o breadcrumb trang chi tiet SP de ve danh sach. */
    public void clickProductDetailBreadcrumbLink() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_BREADCRUMB_LINK);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_ADD_BTN);
    }

    /** Ten tat ca SP dang hien tren trang hien tai cua bang (doc tu aria-label
     *  cua cac nut mat, dung cho test can duyet nhieu SP — vd tim SP co/khong
     *  co danh gia o trang chi tiet). */
    public java.util.List<String> getAllProductDetailButtonLabels() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement btn : getListWebElements(AdminPageUI.ADMIN_PRODUCT_DETAIL_BTNS)) {
            String label = btn.getAttribute("aria-label");
            if (label != null && label.startsWith("Xem chi tiết sản phẩm ")) {
                names.add(label.substring("Xem chi tiết sản phẩm ".length()).trim());
            }
        }
        return names;
    }

    /** Bam nut mat (Xem chi tiet) cua 1 SP cu the (theo ten) -> mo TRANG CHI
     *  TIET full page /products/:code (dot 21). */
    public void clickProductDetailButton(String productName) {
        clickToElement(getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_VIEW_BTN, productName));
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_DETAIL);
    }

    /** Trang chi tiet SP full page co dang hien khong. */
    public boolean isProductDetailPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_DETAIL, 10);
    }

    /** Text cua trang chi tiet SP (gom breadcrumb + cac card thong tin). */
    public String getProductDetailText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_DETAIL);
        return getElementText(AdminPageUI.ADMIN_PRODUCT_DETAIL);
    }

    /** Text breadcrumb tren trang chi tiet SP ("Quản lý sản phẩm > Chi tiết sản phẩm"). */
    public String getProductDetailBreadcrumbText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_DETAIL_BREADCRUMB);
        return getElementText(AdminPageUI.ADMIN_PRODUCT_DETAIL_BREADCRUMB);
    }

    /** Text rating o trang chi tiet SP ('{rating} ({n} đánh giá)' hoac '—'
     *  khi chua co danh gia) — doc tu hang 'Đánh giá' (luon render), bo nhan
     *  'Đánh giá' o dau. */
    public String getProductDetailRatingText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_DETAIL_RATING_ROW);
        String row = getElementText(AdminPageUI.ADMIN_PRODUCT_DETAIL_RATING_ROW);
        if (row == null) {
            return "";
        }
        return row.replaceFirst("^\\s*Đánh giá\\s*", "").trim();
    }

    public void clickFirstCategoryDetail() {
        clickToElement(AdminPageUI.FIRST_CATEGORY_DETAIL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_DETAIL_PAGE);
    }

    public void clickFirstPromoDetail() {
        clickToElement(AdminPageUI.FIRST_PROMO_DETAIL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_DETAIL);
    }

    public void clickFirstTierDetail() {
        clickToElement(AdminPageUI.FIRST_TIER_DETAIL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_TIER_DETAIL);
    }

    public void clickFirstOrderDetail() {
        clickToElement(AdminPageUI.FIRST_ORDER_DETAIL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_ORDER_DETAIL);
    }

    public void clickFirstCustomerDetail() {
        clickToElement(AdminPageUI.FIRST_CUSTOMER_DETAIL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CUSTOMER_DETAIL_PAGE);
    }

    public void clickFirstReturnReview() {
        clickToElement(AdminPageUI.FIRST_RETURN_REVIEW_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_RETURN_DETAIL_PAGE);
    }

    public boolean isDetailModalDisplayed(String detailModalLocator) {
        return isElementDisplayedWithWait(detailModalLocator, 10);
    }

    public String getDetailModalText(String detailModalLocator) {
        waitForElementVisible(detailModalLocator);
        return getElementText(detailModalLocator);
    }

    /** Dong modal chi tiet bang nut X (aria-label='Đóng'); doi modal bien mat. */
    public void closeDetailModal(String modalTestId) {
        clickToElement(AdminPageUI.DYNAMIC_MODAL_CLOSE_BTN, modalTestId);
        waitForElementInvisible("css=[data-testid='" + modalTestId + "']");
    }

    /** Dong trang chi tiet SP bang nut [Quay lại] o header (dot 21: trang
     *  full page, KHONG con nut X Đóng cua Drawer/Modal); doi ve danh sach. */
    public void closeProductDetailPage() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_DETAIL_BACK_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_ADD_BTN);
    }

    /** Tu trang chi tiet SP, bam 'Chỉnh sửa' -> mo TRANG SUA /products/:code/edit
     *  (dot 21: tieu de 'Chỉnh sửa sản phẩm', du lieu giu nguyen, khong luu). */
    public void openProductEditFormFromDetail() {
        clickToElement(AdminPageUI.PRODUCT_VIEW_EDIT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Tu trang XEM danh muc, bam 'Chỉnh sửa' -> mo TRANG SUA
     *  /categories/:code/edit (dot 21: tieu de 'Chỉnh sửa danh mục', du lieu
     *  giu nguyen, khong luu). */
    public void openCategoryEditFormFromDetail() {
        clickToElement(AdminPageUI.CATEGORY_VIEW_EDIT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Tu trang chi tiet KM, bam 'Chỉnh sửa' o header -> mo TRANG SUA
     *  /promotions/:code/edit (dot 21: tieu de 'Chỉnh sửa khuyến mãi', du lieu
     *  giu nguyen, khong luu). Nut chi hien khi voucher Draft. */
    public void openPromoEditFormFromDetail() {
        clickToElement(AdminPageUI.ADMIN_PROMO_VIEW_EDIT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Mo trang chi tiet KM cua voucher co ma cho truoc (nut mat o dong). */
    public void openPromoDetailByCode(String code) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_DETAIL_BTN, code);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_DETAIL);
    }

    /** Ma khuyen mai dang hien o card Thong tin chung cua trang Chi tiet. */
    public String getPromoDetailCode() {
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_DETAIL_CODE);
        return getElementText(AdminPageUI.ADMIN_PROMO_DETAIL_CODE).trim();
    }

    /** Tong luot da dung dang hien o card Thong ke cua trang Chi tiet. */
    public String getPromoDetailUsedCount() {
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_DETAIL_USED_COUNT);
        return getElementText(AdminPageUI.ADMIN_PROMO_DETAIL_USED_COUNT).trim();
    }

    /** Nut [Chỉnh sửa] o header trang Chi tiet co hien khong (chi voucher Draft). */
    public boolean isPromoDetailEditButtonDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PROMO_DETAIL_EDIT_BTN);
    }

    /** Trang Chi tiet khuyen mai co du 4 card: Thong tin chung / Dieu kien ap
     *  dung / Thoi gian / Thong ke su dung. */
    public boolean isPromoDetailCardsDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PROMO_DETAIL_GENERAL)
                && isElementDisplayed(AdminPageUI.ADMIN_PROMO_DETAIL_CONDITIONS)
                && isElementDisplayed(AdminPageUI.ADMIN_PROMO_DETAIL_TIME)
                && isElementDisplayed(AdminPageUI.ADMIN_PROMO_DETAIL_STATS);
    }

    /** Bam [Quay lại] o header trang Chi tiet -> ve danh sach khuyen mai. */
    public void clickPromoDetailBackButton() {
        clickToElement(AdminPageUI.ADMIN_PROMO_DETAIL_BACK_BTN);
        sleepInSecond(1); // cho route /admin + tab promotions render lai
    }

    /** Tu trang XEM khach hang, bam 'Chỉnh sửa' -> mo TRANG SUA
     *  /customers/:code/edit (dot 21). */
    public void openCustomerEditFormFromDetail() {
        clickToElement(AdminPageUI.CUSTOMER_VIEW_EDIT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CUSTOMER_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_CUSTOMER_FORM);
    }

    /** Che do cua form san pham dang mo ('create'/'edit'/'view', thuoc tinh
     *  data-form-mode); "" neu form chua mo. */
    public String getProductFormMode() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_FORM)) {
            return "";
        }
        return getElementAttribute(AdminPageUI.ADMIN_PRODUCT_FORM, "data-form-mode");
    }

    /** Che do cua form danh muc / khuyen mai / khach hang dang mo. */
    public String getCategoryFormMode() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_FORM)) {
            return "";
        }
        return getElementAttribute(AdminPageUI.ADMIN_CATEGORY_FORM, "data-form-mode");
    }

    public String getPromoFormMode() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_PROMO_FORM)) {
            return "";
        }
        return getElementAttribute(AdminPageUI.ADMIN_PROMO_FORM, "data-form-mode");
    }

    public String getCustomerFormMode() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_CUSTOMER_FORM)) {
            return "";
        }
        return getElementAttribute(AdminPageUI.ADMIN_CUSTOMER_FORM, "data-form-mode");
    }

    /** O Ten san pham trong form XEM co dang readonly khong. */
    public boolean isProductNameInputReadOnly() {
        String readonly = getElementAttribute(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT, "readonly");
        return readonly != null;
    }

    /** O Ten danh muc / Ma khuyen mai trong form XEM co dang readonly khong. */
    public boolean isCategoryNameInputReadOnly() {
        return getElementAttribute(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, "readonly") != null;
    }

    public boolean isPromoCodeInputReadOnly() {
        return getElementAttribute(AdminPageUI.ADMIN_PROMO_CODE_INPUT, "readonly") != null;
    }

    public boolean isCustomerNameInputReadOnly() {
        return getElementAttribute(AdminPageUI.ADMIN_CUSTOMER_NAME_INPUT, "readonly") != null;
    }

    public boolean isProductFormDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_FORM, 10);
    }

    public String getProductFormText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
        return getElementText(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Dong form them/sua san pham KHONG luu: bam nut 'Hủy' o footer trang form;
     *  form dirty thi FE hien hop xac nhan 'Thoát mà không lưu?' (dung chung
     *  admin-confirm-modal) — bam 'Thoát' (admin-confirm-yes-btn) de dong that. */
    public void closeProductForm() {
        cancelProductForm();
    }

    /** Dong form san pham bang nut 'Hủy' (khong luu, khong doi du lieu); neu
     *  form dirty thi xac nhan 'Thoát' o hop 'Thoát mà không lưu?' hien ra. */
    public void cancelProductForm() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_CANCEL_BTN);
        confirmDiscardFormIfShown();
        waitForElementInvisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Hop 'Thoát mà không lưu?' (ConfirmDialog dot 18) chi hien khi dong form
     *  DIRTY: bam 'Thoát' (nut xac nhan cua confirm-dialog); form sach thi
     *  khong co hop nay. */
    public void confirmDiscardFormIfShown() {
        sleepInSecond(1); // cho hop xac nhan 'Thoát mà không lưu?' hien ra (neu co)
        if (isElementDisplayed(AdminPageUI.CONFIRM_DIALOG)) {
            String title = getConfirmDialogTitle();
            if (title.contains("Thoát mà chưa lưu?")) {
                clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
                sleepInSecond(1); // cho form dong sau khi xac nhan Thoat
            }
        }
    }

    // ---------- Tim kiem / loc o cac bang (dot 18, PageToolbar) ----------
    // Tim kiem SP: loc client-side theo tu khoa go vao (debounce 120ms, dot 21
    // — KHONG con nut Tim de bam, giong don hang). Tim DM: loc client-side
    // theo tu khoa go vao (go xong bam nut Tim de ve trang 1). Tim KH/voucher/
    // danh gia/tra hang/nhat ky: loc server-side — BAT BUOC bam nut Tim
    // (search-btn) moi tai lai du lieu.
    public void searchProducts(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_SEARCH, keyword);
        sleepInSecond(2); // cho debounce 120ms + loc client-side ap dung
    }

    public void searchCustomers(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_CUSTOMER_SEARCH, keyword);
        sleepInSecond(2); // cho debounce 300ms + GET /api/admin/customers tai lai
    }

    /** Dot 21 — Tim kiem don hang theo tu khoa: go xong TU LOC sau debounce
     *  120ms (loc client-side tren 100 don da tai, khong dau), KHONG con nut
     *  'Tim' de bam. Cho 2s de debounce + React render xong. */
    public void searchOrders(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_ORDER_SEARCH, keyword);
        sleepInSecond(2); // cho debounce 120ms + loc client-side ap dung
    }

    /** O search tren topbar DA BI BO (dot 12) - chi dung de khang dinh am: topbar
     *  moi khong duoc con o tim kiem (tim kiem gio nam trong toolbar cua tung muc). */
    public boolean isTopbarSearchDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_TOPBAR_SEARCH_INPUT);
    }

    /** So dong du lieu dang hien trong bang cua muc dang mo (0 neu bang rong). */
    public int getAdminTableRowCount() {
        return getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS).size();
    }

    /** Noi dung text cua tung dong trong bang cua muc dang mo (dung de khang dinh loc). */
    public java.util.List<String> getAdminTableRowTexts() {
        java.util.List<String> texts = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
            texts.add(row.getText());
        }
        return texts;
    }

    public boolean isEmptyProductsMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_PRODUCTS_MSG);
    }

    public boolean isEmptyCustomersMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_CUSTOMERS_MSG);
    }

    public boolean isEmptyOrdersMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_ORDERS_MSG);
    }

    public boolean isEmptyReturnsMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_RETURNS_MSG);
    }

    /** Chon trang thai don trong select loc (nhan hien thi tieng Viet, vd 'Đã hủy'). */
    public void selectOrderStatusFilter(String visibleStatusLabel) {
        selectByVisibleText(AdminPageUI.ADMIN_ORDER_STATUS_FILTER, visibleStatusLabel);
    }

    /** Dot 21 — KHONG con nut icon 'Tìm' o toolbar Don hang: chon dropdown
     *  loc thi bang loc ngay (client-side), go tu khoa thi loc sau debounce
     *  120ms. Ham nay chi cho 1s de loc ap dung (giu ten + chu ky cu de cac
     *  test goi no khong vo). */
    public void clickOrderFilterSearch() {
        sleepInSecond(1); // cho loc client-side ap dung (khong con goi API)
    }

    /** Dot 18: Export DA BO HAN khoi MOI toolbar admin — khong con nut Export
     *  nao de bam. Ham nay khang dinh am: khong co nut nao chua chu 'Export'
     *  trong toolbar cua muc dang mo. */
    public boolean isExportButtonDisplayed() {
        return getListWebElements("xpath=//div[contains(@class,'sa-page-toolbar')]//button[contains(.,'Export')]").size() > 0;
    }

    // ---------- Toolbar chuan + phan trang + form Drawer rong (dot 18 wave D) ----------
    /** Nut "+ Thêm sản phẩm" o ben phai toolbar muc San pham (sat phai nhat). */
    public boolean isProductAddButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_ADD_BTN, 10);
    }

    /** Nut icon "Đặt lại" (Lucide RotateCcw) o ben phai toolbar cua muc cho
     *  truoc (sectionKey = orders/products/categories/promotions/customers/
     *  reviews/returns/logs). Dot 18 KHONG con nut "Làm mới" o toolbar chuan
     *  (chi muc Hoi vien giu nut rieng admin-members-refresh-btn). */
    public boolean isToolbarResetButtonDisplayed(String sectionKey) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_RESET_BTN, sectionKey), 10);
    }

    /** Nut icon "Tìm kiếm" (Lucide Search) o ben phai toolbar cua muc cho truoc. */
    public boolean isToolbarSearchButtonDisplayed(String sectionKey) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_SEARCH_BTN, sectionKey), 10);
    }

    /** Nut "Tìm kiếm" co chua icon Lucide Search (svg.lucide-search) khong. */
    public boolean isToolbarSearchIconDisplayed(String sectionKey) {
        return isElementDisplayed(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_SEARCH_ICON, sectionKey));
    }

    /** Nut "Đặt lại" co chua icon Lucide RotateCcw (svg.lucide-rotate-ccw) khong. */
    public boolean isToolbarResetIconDisplayed(String sectionKey) {
        return isElementDisplayed(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_RESET_ICON, sectionKey));
    }

    /** Toan bo text cua toolbar dau tien tren trang (dung de khang dinh am:
     *  khong con emoji 🔍/↺ trong toolbar admin, wave D). */
    public String getToolbarText() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_PAGE_TOOLBAR)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_PAGE_TOOLBAR);
    }

    /** Nut "Thêm mới" o ben phai toolbar cua muc cho truoc (chi muc nao co
     *  them moi: products/categories/promotions; orders/reviews/returns/logs
     *  KHONG co); "" neu muc khong co nut them. */
    public boolean isToolbarAddButtonDisplayed(String sectionKey) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_ADD_BTN, sectionKey), 10);
    }

    /** Nut "Thêm mới" co phai la nut CUOI CUNG trong nhom phai cua toolbar khong
     *  (dot 18: Them moi sat phai nhat, sau nut Tim + nut Dat lai). */
    public boolean isToolbarAddButtonLast(String sectionKey) {
        java.util.List<org.openqa.selenium.WebElement> rightButtons = getListWebElements(
                "xpath=//div[contains(@class,'sa-page-toolbar-right')]//button");
        if (rightButtons.isEmpty()) {
            return false;
        }
        String lastTestId = rightButtons.get(rightButtons.size() - 1).getAttribute("data-testid");
        return (sectionKey + "-add-btn").equals(lastTestId);
    }

    /** Bam nut icon "Đặt lại" cua muc: xoa trang o tim/loc + ve trang 1. */
    public void clickToolbarReset(String sectionKey) {
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_RESET_BTN, sectionKey);
        sleepInSecond(1); // cho loc ap dung
    }

    /** Khoi phan trang cua muc co hien khong (chi hien khi bang co du lieu). */
    public boolean isPaginationDisplayed(String sectionKey) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_PAGINATION, sectionKey), 10);
    }

    /** Text cua khoi phan trang ('Hiển thị {from}–{to} / {total} kết quả' +
     *  'Trang X / Y'); "" neu muc khong co phan trang. */
    public String getPaginationText(String sectionKey) {
        if (!isPaginationDisplayed(sectionKey)) {
            return "";
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_PAGINATION, sectionKey));
    }

    /** So trang hien tai (X trong "Trang X / Y"); -1 neu muc khong co phan trang. */
    public int getPaginationCurrentPage(String sectionKey) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("Trang\\s+(\\d+)\\s*/")
                .matcher(getPaginationText(sectionKey));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    /** Tong so trang (Y trong "Trang X / Y"); -1 neu muc khong co phan trang. */
    public int getPaginationTotalPages(String sectionKey) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("Trang\\s+\\d+\\s*/\\s*(\\d+)")
                .matcher(getPaginationText(sectionKey));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    /** Tong so dong cua bang (N trong "Hiển thị {from}–{to} / N kết quả");
     *  -1 neu muc khong co phan trang. */
    public int getPaginationTotalRows(String sectionKey) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("Hiển thị\\s+\\d+–\\d+\\s*/\\s*(\\d+)\\s+kết quả")
                .matcher(getPaginationText(sectionKey));
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    /** Nut "‹ Truoc" cua phan trang co bam duoc khong (bi tat o trang 1). */
    public boolean isPaginationPrevEnabled(String sectionKey) {
        return isElementEnabled(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_PAGINATION_PREV, sectionKey));
    }

    /** Nut "Sau ›" cua phan trang co bam duoc khong (bi tat o trang cuoi). */
    public boolean isPaginationNextEnabled(String sectionKey) {
        return isElementEnabled(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_PAGINATION_NEXT, sectionKey));
    }

    /** Bam nut "Sau ›" cua phan trang (sang trang ke). */
    public void clickPaginationNext(String sectionKey) {
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_PAGINATION_NEXT, sectionKey);
        sleepInSecond(1); // cho trang moi render
    }

    /** Bam nut "‹ Truoc" cua phan trang (ve trang truoc). */
    public void clickPaginationPrev(String sectionKey) {
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_PAGINATION_PREV, sectionKey);
        sleepInSecond(1);
    }

    /** Dot 18: phan trang KHONG con nut so trang — chi co Truoc / 'Trang X / Y' /
     *  Sau. (Ham clickPaginationPage cu da xoa.) */

    /** Gia tri dang go trong o tim kiem cua muc San pham (dung de khang dinh nut "Dat lai" da xoa trang). */
    public String getProductSearchValue() {
        return getElementValue(AdminPageUI.ADMIN_PRODUCT_SEARCH);
    }

    // ----- Form them/sua san pham — TRANG full page (dot 21) -----
    /** Form them/sua san pham (trang full page) co dang hien khong (cho doi
     *  toi da 10 giay). Ten phuong thuc giu 'Modal' de call site khong doi. */
    public boolean isProductFormModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_FORM, 10);
    }

    /** Form them/sua san pham (trang full page) co dang hien khong (kiem tra
     *  ngay, khong cho doi — dung cho khang dinh form da dong). */
    public boolean isProductFormModalPresent() {
        return isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Form them/sua danh muc (trang full page) co dang hien khong (cho doi
     *  toi da 10 giay). Ten phuong thuc giu 'Modal' de call site khong doi. */
    public boolean isCategoryFormModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_FORM, 10);
    }

    /** Form them/sua danh muc (trang full page) co dang hien khong (kiem tra
     *  ngay, khong cho doi — dung cho khang dinh form da dong). */
    public boolean isCategoryFormModalPresent() {
        return isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Trang tao danh muc full page (/categories/new) co dang hien khong. */
    public boolean isCategoryCreatePageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_CREATE_PAGE, 10);
    }

    /** Trang sua danh muc full page (/categories/:code/edit) co dang hien khong. */
    public boolean isCategoryEditPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_EDIT_PAGE, 10);
    }

    /** Trang xem danh muc full page (/categories/:code) co dang hien khong. */
    public boolean isCategoryDetailPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_DETAIL_PAGE, 10);
    }

    /** Danh sach danh muc co dang hien khong (nut '+ Thêm mới' + o tim kiem
     *  chi co o trang danh sach, khong co o cac trang con DM). */
    public boolean isCategoryListPageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_ADD_BTN)
                && isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_SEARCH);
    }

    /** Nhan cac nut o header sticky cua TRANG full page co testid cho truoc
     *  (theo thu tu hien thi) — doi ung DYNAMIC_FORM_FOOTER_BUTTONS cua Drawer. */
    public java.util.List<String> getPageHeadButtonTexts(String pageTestId) {
        java.util.List<String> texts = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement btn : getListWebElements(
                AdminPageUI.DYNAMIC_PAGE_HEAD_BUTTONS, pageTestId)) {
            texts.add(btn.getText().trim());
        }
        return texts;
    }

    /** Trang xem khach hang full page (/customers/:code) co dang hien khong. */
    public boolean isCustomerDetailPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CUSTOMER_DETAIL_PAGE, 10);
    }

    /** Trang sua khach hang full page (/customers/:code/edit) co dang hien khong. */
    public boolean isCustomerEditPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CUSTOMER_EDIT_PAGE, 10);
    }

    /** Trang chi tiet tra hang full page (/returns/:code) co dang hien khong. */
    public boolean isReturnDetailPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_RETURN_DETAIL_PAGE, 10);
    }

    /** Trang chi tiet danh gia full page (/reviews/:id) co dang hien khong. */
    public boolean isReviewDetailPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVIEW_DETAIL_PAGE, 10);
    }

    /** Form tao/sua khuyen mai (trang full page dot 21) co dang hien khong
     *  (cho doi toi da 10 giay). Ten phuong thuc giu 'Modal' de call site
     *  khong doi — dinh vi bang ADMIN_PROMO_FORM (form trong trang). */
    public boolean isPromoFormModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_FORM, 10);
    }

    /** Form tao/sua khuyen mai (trang full page dot 21) co dang hien khong
     *  (kiem tra ngay, khong cho doi — dung cho khang dinh form da dong). */
    public boolean isPromoFormModalPresent() {
        return isElementDisplayed(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Trang tao khuyen mai full page (/promotions/new) co dang hien khong. */
    public boolean isPromoCreatePageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_CREATE_PAGE, 10);
    }

    /** Trang sua khuyen mai full page (/promotions/:code/edit) co dang hien khong. */
    public boolean isPromoEditPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_EDIT_PAGE, 10);
    }

    /** Trang chi tiet khuyen mai full page (/promotions/:code) co dang hien khong. */
    public boolean isPromoDetailPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_DETAIL, 10);
    }

    /** Trang lich su su dung khuyen mai (/promotions/:code/usages) co dang hien khong. */
    public boolean isPromoUsagesPageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_USAGES_PAGE, 10);
    }

    /** Danh sach khuyen mai co dang hien khong (nut '+ Thêm mới' + o tim kiem
     *  chi co o trang danh sach, khong co o cac trang con KM). */
    public boolean isPromoListPageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PROMO_ADD_BTN)
                && isElementDisplayed(AdminPageUI.ADMIN_PROMO_SEARCH);
    }

    /** Text cua form san pham o trang full page (gom ca tieu de 'Thêm mới /
     *  Chỉnh sửa sản phẩm' + cac nhan truong). */
    public String getProductFormModalText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
        return getElementText(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Gia tri dang dien trong o ten san pham cua form (dung de khang dinh form sua da dien san ten). */
    public String getProductNameInputValue() {
        return getElementValue(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT);
    }

    /** Mo trang them san pham moi bang nut "+ Thêm mới" o toolbar (dot 21:
     *  trang full page /products/new, chi mo, khong dien gi ca). */
    public void openProductFormModal() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_CREATE_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Dong modal form bat ky bang nut X (aria-label='Đóng'); doi modal bien mat. */
    public void closeFormModal(String modalTestId) {
        closeDetailModal(modalTestId);
    }

    // ---------- Dang nhap Admin tren app rieng :3001 (dot 9, dung cho AdminCrudTest) ----------
    /** Dang nhap bang tai khoan bat ky tren trang dang nhap Admin rieng :3001
     *  (khong yeu cau phai la Super Admin - dung cho test 403 cua tai khoan thuong);
     *  bo qua test neu service :3001 chua chay. Sau khi dang nhap KHONG tu mo /admin
     *  (nguoi goi tu quyet dinh mo trang nao de kiem tra quyen). */
    public void loginAsUserOnAdminAppOrSkip(String usernameOrEmail, String password) {
        openPageUrl(GlobalConstants.getAdminAppUrl() + "/login");
        AuthModalPageObject authModal = new AuthModalPageObject();
        try {
            authModal.waitForModalVisible();
        } catch (Exception e) {
            throw new SkipException("Bo qua: trang Admin rieng " + GlobalConstants.getAdminAppUrl()
                    + " chua chay (can 'docker compose up -d --build' de service dafl-admin len cong 3001)");
        }
        authModal.loginAs(usernameOrEmail, password);
    }

    /** Dang nhap Super Admin tren trang Admin rieng (localhost:3001, build
     *  VITE_ADMIN_ONLY=true). Token JWT luu theo origin nen phai dang nhap o :3001
     *  va o lai :3001 (khong dung /admin o web ban hang sau khi dang nhap).
     *  Bo qua test (SkipException) khi service :3001 chua chay hoac tai khoan
     *  admin@dafl.vn chua duoc seed tren DB nay. */
    public void loginAsAdminOnAdminAppOrSkip() {
        loginAsUserOnAdminAppOrSkip("admin", GlobalConstants.ADMIN_PASSWORD);
        openPageUrl(GlobalConstants.getAdminAppUrl() + "/admin");
        try {
            waitForAdminGateResolved();
        } catch (AssertionError e) {
            throw new SkipException("Bo qua: khong vao duoc trang Admin o " + GlobalConstants.getAdminAppUrl()
                    + " (tai khoan admin chua duoc seed hoac dich vu admin chua san sang)");
        }
        if (!isAdminMenuDisplayed()) {
            throw new SkipException("Bo qua: tai khoan admin " + GlobalConstants.ADMIN_EMAIL
                    + " chua ton tai/khong dang nhap duoc tren DB nay (can seed hoac cap Role='SuperAdmin' bang SQL)");
        }
    }

    // ---------- CRUD (dot 9) - chi dung tren du lieu do test tao, co tu don dep ----------
    // Dot 18: .sa-notice DA BO — moi thao tac luu/xoa/doi trang thai deu bao
    // bang Toast (admin-toast); dung waitForToast()/isToastDisplayed() o duoi.

    // ---------- Toast moi (dot 18): admin-toast ----------
    // Hop dong: thong bao sau luu/xoa/doi trang thai dung testid admin-toast
    // (tieu de rieng admin-toast-title + mo ta admin-toast-desc). .sa-notice
    // DA BO HAN dot 18 nen cac ham doi thong bao chi cho toast.
    public boolean isToastDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_TOAST, 10);
    }

    public String getToastText() {
        waitForElementVisible(AdminPageUI.ADMIN_TOAST);
        return getElementText(AdminPageUI.ADMIN_TOAST);
    }

    public String waitForToast() {
        waitForElementVisible(AdminPageUI.ADMIN_TOAST);
        return getElementText(AdminPageUI.ADMIN_TOAST);
    }

    /** Cho toi da 10 giay toi khi co toast, tra ve noi dung. */
    public String waitForFeedback() {
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            if (isElementDisplayed(AdminPageUI.ADMIN_TOAST)) {
                return getElementText(AdminPageUI.ADMIN_TOAST);
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Khong thay toast (admin-toast) sau 10 giay");
    }

    /** Bo dau cham nghin + khoang trang khoi gia tri o nhap gia (vd "150.000" -> "150000"). */
    public static String normalizePriceInput(String rawValue) {
        return rawValue == null ? "" : rawValue.replace(".", "").replace(",", "").replace(" ", "").trim();
    }

    /** Co dong nao trong bang cua muc dang mo chua chuoi cho truoc khong. */
    public boolean isRowWithTextDisplayed(String text) {
        return getAdminTableRowTexts().stream().anyMatch(t -> t.contains(text));
    }

    /** Tim kiem don hang theo ma/tu khoa (dot 21: go xong tu loc sau debounce
     *  120ms, khong con nut 'Tìm' de bam). */
    public void searchOrdersAndApply(String keyword) {
        searchOrders(keyword); // searchOrders da go tu khoa + cho debounce
    }

    // ----- San pham (dot redesign: drawer + dropdown danh muc custom + upload anh) -----
    /** Ten danh muc that dau tien trong dropdown custom cua form them/sua san pham
     *  (bo qua option placeholder); null neu khong co danh muc nao. CHI GOI KHI
     *  FORM DA MO. Dropdown mo ra de doc option roi dong lai bang cach bam trigger
     *  them 1 lan (toggle) de form ve trang thai dong dropdown truoc khi dien tiep. */
    public String getFirstRealCategoryName() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT);
        clickToElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT);
        sleepInSecond(1); // cho dropdown custom render option
        List<WebElement> options = getListWebElements(AdminPageUI.ADMIN_PRODUCT_CATEGORY_OPTIONS);
        if (options.isEmpty()) {
            options = getListWebElements(
                    "xpath=//*[@data-testid='admin-product-form-modal']//li");
        }
        String first = null;
        for (WebElement option : options) {
            String text = option.getText().trim();
            if (!text.isEmpty() && !text.startsWith("—")) {
                first = text;
                break;
            }
        }
        // Dong dropdown lai (bam trigger them 1 lan) de form ve trang thai ban dau.
        clickToElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT);
        sleepInSecond(1);
        return first;
    }

    /** Chon danh muc trong dropdown custom cua form san pham: bam trigger -> go
     *  tim kiem neu co o search -> bam option theo text hien thi. Hop dong KHONG
     *  quy dinh testid cho option nen locator theo text (role='option', du phong
     *  <li> / <button>); neu FE doi kieu render option thi cap nhat locator o
     *  AdminPageUI.DYNAMIC_PRODUCT_CATEGORY_OPTION*. */
    public void selectProductCategory(String categoryName) {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT);
        sleepInSecond(1); // cho dropdown custom render option
        if (isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SEARCH)) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SEARCH, categoryName);
            sleepInSecond(1); // cho loc option theo tu khoa
        }
        if (getListWebElements(AdminPageUI.DYNAMIC_PRODUCT_CATEGORY_OPTION, categoryName).size() > 0) {
            clickToElement(AdminPageUI.DYNAMIC_PRODUCT_CATEGORY_OPTION, categoryName);
        } else if (getListWebElements(AdminPageUI.DYNAMIC_PRODUCT_CATEGORY_OPTION_LI, categoryName).size() > 0) {
            clickToElement(AdminPageUI.DYNAMIC_PRODUCT_CATEGORY_OPTION_LI, categoryName);
        } else {
            clickToElement(AdminPageUI.DYNAMIC_PRODUCT_CATEGORY_OPTION_BTN, categoryName);
        }
        sleepInSecond(1); // cho dropdown dong + gia tri duoc chon
    }

    /** Tao san pham moi (ten 3-120 ky tu, gia > 0, ton kho >= 0, phai chon danh muc);
     *  thanh cong thi drawer tu dong va hien toast. */
    public void createProduct(String name, String categoryName, String price, String stock) {
        openProductFormModal();
        fillProductFormAndSave(name, categoryName, price, stock);
    }

    /** Dien cac truong cua form san pham (form da mo trong drawer) roi bam Luu;
     *  thanh cong thi drawer tu dong va hien toast. Giu nguyen chu ky cu de test
     *  cu khong vo; truong moi (gia goc, mo ta) de trong. */
    public void fillProductFormAndSave(String name, String categoryName, String price, String stock) {
        fillProductFormFullAndSave(name, categoryName, price, "", stock, "");
    }

    /** Dien DU cac truong cua form san pham moi (gia goc + mo ta) roi bam Luu.
     *  Gia goc/mo ta de trong ("") thi bo qua o do. */
    public void fillProductFormFullAndSave(String name, String categoryName, String price,
                                           String originalPrice, String stock, String description) {
        fillProductForm(name, categoryName, price, originalPrice, stock, description);
        saveProductForm();
    }

    /** Dien DU cac truong cua form san pham (form da mo trong drawer) NHUNG KHONG
     *  bam Luu — dung khi test can thao tac them truoc khi luu (vd upload anh). */
    public void fillProductForm(String name, String categoryName, String price,
                                String originalPrice, String stock, String description) {
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT, name);
        selectProductCategory(categoryName);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_PRICE_INPUT, price);
        if (originalPrice != null && !originalPrice.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_ORIGINAL_PRICE_INPUT, originalPrice);
        }
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_STOCK_INPUT, stock);
        if (description != null && !description.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_DESCRIPTION_INPUT, description);
        }
    }

    /** Bam Luu trong form san pham dang mo; thanh cong thi drawer tu dong va hien toast. */
    public void saveProductForm() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN);
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Gia tri dang dien trong o gia ban, da chuan hoa bo dau cham nghin
     *  (vd hien "150.000" -> tra "150000"). */
    public String getProductPriceInputDigits() {
        return normalizePriceInput(getElementValue(AdminPageUI.ADMIN_PRODUCT_PRICE_INPUT));
    }

    /** So ky tu ten san pham dang hien o bo dem (admin-product-name-count). */
    public String getProductNameCountText() {
        return getElementText(AdminPageUI.ADMIN_PRODUCT_NAME_COUNT);
    }

    /** So ky tu mo ta dang hien o bo dem (admin-product-description-count). */
    public String getProductDescriptionCountText() {
        return getElementText(AdminPageUI.ADMIN_PRODUCT_DESCRIPTION_COUNT);
    }

    /** Badge % giam trong form (chi hien khi co gia goc > gia ban); "" neu khong co. */
    public boolean isDiscountBadgeDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_DISCOUNT_BADGE);
    }

    public String getDiscountBadgeText() {
        if (!isDiscountBadgeDisplayed()) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_PRODUCT_DISCOUNT_BADGE);
    }

    /** Toggle trang thai hien thi cua san pham trong form co dang hien khong. */
    public boolean isProductStatusToggleDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_STATUS_TOGGLE);
    }

    public void clickProductStatusToggle() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_STATUS_TOGGLE);
        sleepInSecond(1);
    }

    /** Khoi xem truoc (preview) cua san pham trong drawer co dang hien khong. */
    public boolean isProductPreviewDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_PREVIEW);
    }

    /** Tang/giam ton kho bang nut +/- trong form (moi lan bam doi 1 don vi). */
    public void clickProductStockPlus() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_STOCK_PLUS_BTN);
        sleepInSecond(1);
    }

    public void clickProductStockMinus() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_STOCK_MINUS_BTN);
        sleepInSecond(1);
    }

    /** Noi dung loi cua 1 truong trong form san pham (field = name/price/stock/
     *  category/...); "" neu truong do khong co loi. */
    public String getProductFieldErrorText(String field) {
        if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_FIELD_ERROR, field))) {
            return "";
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_FIELD_ERROR, field));
    }

    /** Nhap ma san pham vao o Ma (chi khi tao moi; khi sua o nay bi disable).
     *  FE tu viet hoa khi go. */
    public void setProductCode(String code) {
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_CODE_INPUT, code);
    }

    /** Gia tri dang hien trong o Ma san pham. */
    public String getProductCodeInputValue() {
        return getElementValue(AdminPageUI.ADMIN_PRODUCT_CODE_INPUT);
    }

    /** O Ma san pham co dang bi disable khong (khi sua thi disable). */
    public boolean isProductCodeInputDisabled() {
        return !isElementEnabled(AdminPageUI.ADMIN_PRODUCT_CODE_INPUT);
    }

    /** Nhap ten san pham vao o Ten (sendKey xoa het roi go lai). */
    public void setProductName(String name) {
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT, name);
    }

    /** Bam Luu trong form san pham (chi bam, khong doi ket qua — dung khi test
     *  can doc loi sau khi luu that bai, vd trung ma). */
    public void clickProductSaveButton() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN);
    }

    /** Dong trong bang vua tao co duoc highlight (class sa-row-highlight) khong. */
    public boolean isRowHighlighted(String rowText) {
        return getListWebElements(AdminPageUI.ADMIN_HIGHLIGHTED_ROWS).stream()
                .anyMatch(row -> row.getText().contains(rowText));
    }

    // ----- Upload anh san pham (dot redesign) -----
    /** Gui file anh vao input file THAT (admin-product-image-input) bang sendKeys
     *  duong dan tuyet doi — KHONG click truoc (click input file se mo hop thoai
     *  chon file cua he dieu hanh). Input co the bi an (display:none) nen ep hien
     *  bang JS truoc khi gui. */
    public void uploadProductImage(String absoluteFilePath) {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_IMAGE_DROPZONE);
        List<WebElement> inputs = getListWebElements(AdminPageUI.ADMIN_PRODUCT_IMAGE_INPUT);
        if (inputs.isEmpty()) {
            throw new AssertionError("Khong tim thay input file anh san pham (admin-product-image-input)");
        }
        WebElement input = inputs.get(0);
        executeScript("arguments[0].style.display='block'; arguments[0].style.visibility='visible';"
                + " arguments[0].style.opacity='1'; arguments[0].style.height='1px';", input);
        input.sendKeys(absoluteFilePath);
        sleepInSecond(2); // cho FE doc file + bat dau upload (POST /api/admin/upload)
    }

    /** Dropzone upload anh co dang hien khong (trong drawer san pham). */
    public boolean isProductImageDropzoneDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_IMAGE_DROPZONE, 10);
    }

    /** Thumbnail anh thu <index> (0-based) co dang hien khong. */
    public boolean isProductImageThumbDisplayed(int index) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_THUMB, String.valueOf(index)), 10);
    }

    /** Src cua anh thumbnail thu <index> (dung de so sanh URL anh chinh sau khi luu). */
    public String getProductImageThumbSrc(int index) {
        return getElementAttribute(
                getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_THUMB_IMG, String.valueOf(index)), "src");
    }

    /** Nhan "Anh chinh" co dang hien tren thumbnail thu <index> khong. */
    public boolean isProductImageMainBadgeDisplayed(int index) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_THUMB_MAIN_BADGE, String.valueOf(index)), 10);
    }

    /** So thumbnail anh dang co trong form (dem theo testid admin-product-image-thumb-<index>). */
    public int getProductImageThumbCount() {
        int count = 0;
        while (isElementDisplayed(getDynamicLocator(
                AdminPageUI.DYNAMIC_PRODUCT_IMAGE_THUMB, String.valueOf(count)))) {
            count++;
        }
        return count;
    }

    /** Dat anh thu <index> lam anh chinh. */
    public void setMainProductImage(int index) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_SET_MAIN_BTN, String.valueOf(index));
        sleepInSecond(1); // cho FE doi anh chinh
    }

    /** Xoa anh thu <index> khoi form. */
    public void removeProductImage(int index) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_REMOVE_BTN, String.valueOf(index));
        sleepInSecond(1);
    }

    /** Loi upload anh inline co dang hien khong + noi dung loi. */
    public boolean isProductImageErrorDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_IMAGE_ERROR);
    }

    public String getProductImageErrorText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_IMAGE_ERROR);
        return getElementText(AdminPageUI.ADMIN_PRODUCT_IMAGE_ERROR);
    }

    /** Them anh bang URL: bat che do nhap link roi dien URL (khong upload file). */
    public void setProductImageByUrl(String imageUrl) {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_IMAGE_LINK_TOGGLE);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_IMAGE_URL_INPUT, imageUrl);
        sleepInSecond(1);
    }

    /** Modal chi tiet san pham co hien anh (<img>) khong. */
    public boolean isProductDetailImageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_DETAIL_IMG, 10);
    }

    /** Dong san pham trong bang co hien thumbnail <img> khong. */
    public boolean isProductRowImageDisplayed(String productName) {
        return getListWebElements(AdminPageUI.DYNAMIC_PRODUCT_ROW_IMG, productName).size() > 0;
    }

    /** Mo form sua san pham theo ten (chi mo, khong dien gi ca) — dung khi test
     *  can doc trang thai form (vd badge % giam, gia tri o nhap) truoc khi luu. */
    public void openProductEditForm(String name) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_EDIT_BTN, name);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Sua san pham co san (tim theo ten cu): doi ten + gia, luu lai. */
    public void editProduct(String oldName, String newName, String newPrice) {
        editProductFull(oldName, newName, newPrice, "");
    }

    /** Sua san pham: doi ten + gia + gia goc (de trong "" thi giu nguyen gia goc cu). */
    public void editProductFull(String oldName, String newName, String newPrice, String newOriginalPrice) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_EDIT_BTN, oldName);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT, newName);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_PRICE_INPUT, newPrice);
        if (newOriginalPrice != null && !newOriginalPrice.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_ORIGINAL_PRICE_INPUT, newOriginalPrice);
        }
        clickToElement(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN);
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Bam nut thung rac xoa SP theo ten (chi bam, khong xu ly popup — dung khi
     *  test can khang dinh tung buoc cua popup chan xoa / hop xac nhan). */
    public void clickProductDeleteButton(String name) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_DELETE_BTN, name);
    }

    /** Bam nut thung rac xoa DM theo ten (chi bam, khong xu ly popup). */
    public void clickCategoryDeleteButton(String name) {
        clickToElement(AdminPageUI.DYNAMIC_CATEGORY_DELETE_BTN, name);
    }

    /** Xoa san pham theo ten (dot 21): SP moi tao mac dinh Hoat dong nen bam xoa
     *  se gap popup CHAN xoa (ConfirmDialog 'Không thể xóa sản phẩm') truoc —
     *  bam 'Chuyển sang Ngừng hoạt động' -> modal CHUNG 'Xác nhận thay đổi
     *  trạng thái?' -> xac nhan -> toast 'Đổi trạng thái sản phẩm thành công!'
     *  -> bam xoa lai -> ConfirmDialog 'Xóa sản phẩm?' -> bam 'Xóa sản phẩm'
     *  -> toast 'Đã xóa sản phẩm'. SP da Ngung hoat dong thi vao thang
     *  ConfirmDialog xoa. */
    public void deleteProductByName(String name) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_DELETE_BTN, name);
        handleDeleteBlockedIfShown();
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String title = getConfirmDialogTitle();
        if (!title.contains("Xóa sản phẩm?")) {
            throw new AssertionError("Xoa SP phai mo popup 'Xóa sản phẩm?', dang thay: " + title);
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        waitForFeedback();
        sleepInSecond(2); // cho loadProducts tai lai danh sach
    }

    /** Neu popup chan xoa (ConfirmDialog 'Không thể xóa ...') dang hien: bam
     *  'Chuyển sang Ngừng hoạt động' -> modal xac nhan doi trang thai hien ra
     *  (SP dot 21: modal CHUNG 'Xác nhận thay đổi trạng thái?'; DM van co the
     *  la 'Ẩn danh mục?' cu — chap nhan ca 2) -> bam nut xac nhan -> doi trang
     *  thai xong (toast 'Đổi trạng thái ... thành công!' / 'Đã ẩn ...'),
     *  doi popup chan bien mat. Khong co popup chan thi khong lam gi (dong
     *  da Ngung hoat dong / du lieu cu) — ConfirmDialog xoa van dang cho o
     *  buoc goi ham nay. */
    public void handleDeleteBlockedIfShown() {
        long deadline = System.currentTimeMillis() + 10000;
        String blockedTitle = null;
        while (System.currentTimeMillis() < deadline) {
            if (!isElementDisplayed(AdminPageUI.CONFIRM_DIALOG)) {
                sleepInSecond(1);
                continue;
            }
            String title = getConfirmDialogTitle();
            if (title.startsWith("Không thể xóa")) {
                blockedTitle = title;
                break;
            }
            return; // ConfirmDialog xoa (hoac popup khac) da hien, khong bi chan
        }
        if (blockedTitle == null) {
            waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
            return;
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN); // 'Chuyển sang Ngừng hoạt động'
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String hideTitle = getConfirmDialogTitle();
        if (!hideTitle.startsWith("Ẩn ") && !hideTitle.equals("Xác nhận thay đổi trạng thái?")) {
            throw new AssertionError("Bam 'Chuyển sang Ngừng hoạt động' phai mo popup 'Xác nhận thay đổi trạng thái?' (SP dot 21) hoac 'Ẩn ...?' (DM), dang thay: " + hideTitle);
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN); // 'Xác nhận' / 'Ẩn sản phẩm' / 'Ẩn danh mục'
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
        sleepInSecond(2); // cho loadProducts/loadCategories tai lai danh sach sau khi doi trang thai
    }

    // ----- Danh muc -----
    public void searchCategories(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SEARCH, keyword);
        sleepInSecond(2); // cho debounce 120ms + loc client-side ap dung + ve trang 1
    }

    /** Tao danh muc moi (ten 2-150 ky tu); thanh cong thi form trang dong va hien toast. */
    public void createCategory(String name) {
        createCategoryFull(name, "", "", "");
    }

    /** Mo form them danh muc moi (chi mo, khong dien gi ca) — dot 21: mo
     *  TRANG /categories/new, khong con Drawer. */
    public void openCategoryForm() {
        clickToElement(AdminPageUI.ADMIN_CATEGORY_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_CREATE_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Dien cac truong cua form danh muc (form da mo) NHUNG KHONG bam Luu. */
    public void fillCategoryForm(String name, String slug, String description, String sortOrder) {
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, name);
        sleepInSecond(1); // cho FE tu sinh slug tu ten (neu co)
        if (slug != null && !slug.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SLUG_INPUT, slug);
        }
        if (description != null && !description.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_DESCRIPTION_INPUT, description);
        }
        if (sortOrder != null && !sortOrder.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SORT_ORDER_INPUT, sortOrder);
        }
    }

    /** Nhap ma danh muc vao o Ma (chi khi tao moi; khi sua o nay bi disable). */
    public void setCategoryCode(String code) {
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_CODE_INPUT, code);
    }

    /** Nhap ten danh muc vao o Ten (sendKey xoa het roi go lai). */
    public void setCategoryName(String name) {
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, name);
    }

    /** Gia tri dang hien trong o Ten danh muc. */
    public String getCategoryNameInputValue() {
        return getElementValue(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT);
    }

    /** Gia tri dang hien trong o Ma danh muc. */
    public String getCategoryCodeInputValue() {
        return getElementValue(AdminPageUI.ADMIN_CATEGORY_CODE_INPUT);
    }

    /** O Ma danh muc co dang bi disable khong (khi sua thi disable). */
    public boolean isCategoryCodeInputDisabled() {
        return !isElementEnabled(AdminPageUI.ADMIN_CATEGORY_CODE_INPUT);
    }

    /** Bam Luu trong form danh muc (chi bam, khong doi ket qua — dung khi test
     *  can doc loi sau khi luu that bai, vd trung ma). */
    public void clickCategorySaveButton() {
        clickToElement(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN);
    }

    /** Bam Luu trong form danh muc roi doi thong bao + form dong (luu thanh cong). */
    public void saveCategoryForm() {
        clickCategorySaveButton();
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Noi dung loi cua 1 truong trong form danh muc (field = code/name/slug);
     *  "" neu truong do khong co loi. */
    public String getCategoryFieldErrorText(String field) {
        if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_CATEGORY_FIELD_ERROR, field))) {
            return "";
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_CATEGORY_FIELD_ERROR, field));
    }

    /** Tao danh muc moi dien DU truong: ten + slug + mo ta + thu tu (de trong "" thi bo qua o do). */
    public void createCategoryFull(String name, String slug, String description, String sortOrder) {
        openCategoryForm();
        fillCategoryForm(name, slug, description, sortOrder);
        saveCategoryForm();
    }

    /** Tao danh muc moi co ma chi dinh (ma de trong "" thi BE tu sinh DM-xxxx). */
    public void createCategoryFullWithCode(String code, String name, String slug,
                                           String description, String sortOrder) {
        openCategoryForm();
        if (code != null && !code.isEmpty()) {
            setCategoryCode(code);
        }
        fillCategoryForm(name, slug, description, sortOrder);
        saveCategoryForm();
    }

    /** Gia tri slug dang hien trong form danh muc (tu sinh tu ten neu FE co). */
    public String getCategorySlugValue() {
        return getElementValue(AdminPageUI.ADMIN_CATEGORY_SLUG_INPUT);
    }

    /** Toggle trang thai hien thi cua danh muc trong form co dang hien khong. */
    public boolean isCategoryStatusToggleDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_STATUS_TOGGLE);
    }

    public void clickCategoryStatusToggle() {
        clickToElement(AdminPageUI.ADMIN_CATEGORY_STATUS_TOGGLE);
        sleepInSecond(1);
    }

    /** Dong form danh muc bang nut 'Hủy' (khong luu); form dirty thi xac nhan
     *  'Thoát' o hop 'Thoát mà không lưu?' hien ra. */
    public void cancelCategoryForm() {
        clickToElement(AdminPageUI.ADMIN_CATEGORY_CANCEL_BTN);
        confirmDiscardFormIfShown();
        waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Dong form khuyen mai bang nut 'Hủy' (khong luu); form dirty thi xac nhan
     *  'Thoát' o hop 'Thoát mà không lưu?' hien ra. */
    public void cancelPromoForm() {
        clickToElement(AdminPageUI.ADMIN_PROMO_CANCEL_BTN);
        confirmDiscardFormIfShown();
        waitForElementInvisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Dong form sua khach hang bang nut 'Hủy' (khong luu); form dirty thi xac
     *  nhan 'Thoát' o hop 'Thoát mà không lưu?' hien ra. */
    public void cancelCustomerForm() {
        clickToElement(AdminPageUI.ADMIN_CUSTOMER_CANCEL_BTN);
        confirmDiscardFormIfShown();
        waitForElementInvisible(AdminPageUI.ADMIN_CUSTOMER_FORM);
    }

    // ----- Upload anh danh muc (chi 1 anh — IconUrl) -----
    /** Gui file anh vao input file THAT cua form danh muc (giong uploadProductImage). */
    public void uploadCategoryImage(String absoluteFilePath) {
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_IMAGE_DROPZONE);
        List<WebElement> inputs = getListWebElements(AdminPageUI.ADMIN_CATEGORY_IMAGE_INPUT);
        if (inputs.isEmpty()) {
            throw new AssertionError("Khong tim thay input file anh danh muc (admin-category-image-input)");
        }
        WebElement input = inputs.get(0);
        executeScript("arguments[0].style.display='block'; arguments[0].style.visibility='visible';"
                + " arguments[0].style.opacity='1'; arguments[0].style.height='1px';", input);
        input.sendKeys(absoluteFilePath);
        sleepInSecond(2); // cho FE doc file + bat dau upload
    }

    public boolean isCategoryImageThumbDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_IMAGE_THUMB, 10);
    }

    public void removeCategoryImage() {
        clickToElement(AdminPageUI.ADMIN_CATEGORY_IMAGE_REMOVE_BTN);
        sleepInSecond(1);
    }

    /** Mo form sua danh muc theo ten (chi mo, khong dien gi ca) — dung khi test
     *  can doc trang thai form (vd toggle trang thai) truoc khi luu. */
    public void openCategoryEditForm(String name) {
        clickToElement(AdminPageUI.DYNAMIC_CATEGORY_EDIT_BTN, name);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Sua danh muc co san (tim theo ten cu): doi ten, luu lai. */
    public void editCategory(String oldName, String newName) {
        editCategoryFull(oldName, newName, "", "", "");
    }

    /** Sua danh muc: doi ten + slug + mo ta + thu tu (de trong "" thi giu nguyen). */
    public void editCategoryFull(String oldName, String newName, String newSlug,
                                 String newDescription, String newSortOrder) {
        clickToElement(AdminPageUI.DYNAMIC_CATEGORY_EDIT_BTN, oldName);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, newName);
        if (newSlug != null && !newSlug.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SLUG_INPUT, newSlug);
        }
        if (newDescription != null && !newDescription.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_DESCRIPTION_INPUT, newDescription);
        }
        if (newSortOrder != null && !newSortOrder.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SORT_ORDER_INPUT, newSortOrder);
        }
        clickToElement(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN);
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Xoa danh muc theo ten (dot 18, giong xoa SP): DM dang Hoat dong gap popup
     *  chan xoa (ConfirmDialog 'Không thể xóa danh mục') truoc — 'Chuyển sang
     *  Ngừng hoạt động' -> ConfirmDialog 'Ẩn danh mục?' -> xac nhan -> xoa
     *  lai qua ConfirmDialog 'Xóa danh mục?'. DM da Ngung thi vao thang
     *  ConfirmDialog xoa. */
    public void deleteCategoryByName(String name) {
        clickToElement(AdminPageUI.DYNAMIC_CATEGORY_DELETE_BTN, name);
        handleDeleteBlockedIfShown();
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String title = getConfirmDialogTitle();
        if (!title.contains("Xóa danh mục?")) {
            throw new AssertionError("Xoa DM phai mo popup 'Xóa danh mục?', dang thay: " + title);
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        waitForFeedback();
        sleepInSecond(2); // cho loadCategories tai lai danh sach
    }

    public boolean isEmptyCategoriesMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_CATEGORIES_MSG);
    }

    // ----- Khuyen mai / voucher -----
    // Dot 13: giao dien DA CO sua/xoa voucher Draft (nut sua/xoa chi hien o dong
    // Draft) + hop xac nhan chung khi xoa; tao moi (trang thai Nhap), kich hoat
    // (Nhap -> Dang chay) va tat co ly do (Dang chay -> Da tat) giu nguyen.
    public void searchPromos(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_SEARCH, keyword);
        clickToElement(AdminPageUI.ADMIN_PROMO_SEARCH_BTN);
        sleepInSecond(2); // cho GET /api/admin/promotions tai lai
    }

    /** Tao voucher moi (ma 3-50 ky tu, giam % 1-100, mac dinh toan san, khong gioi han
     *  ngay); tao xong o trang thai Nhap, luu xong ve lai danh sach + hien toast
     *  'Đã tạo khuyến mãi'. Dot 21: form la TRANG FULL PAGE /promotions/new. */
    public void createPromo(String code, String discountValue) {
        clickToElement(AdminPageUI.ADMIN_PROMO_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_CREATE_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_CODE_INPUT, code);
        clickToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_PERCENT);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_VALUE_INPUT, discountValue);
        clickToElement(AdminPageUI.ADMIN_PROMO_SAVE_BTN);
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Mo trang tao khuyen mai moi (chi mo, khong dien gi ca). Dot 21: trang
     *  full page /promotions/new (breadcrumb 'Quản lý khuyến mãi / Thêm mới'). */
    public void openPromoForm() {
        clickToElement(AdminPageUI.ADMIN_PROMO_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_CREATE_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Dien cac truong cua form khuyen mai (form da mo) NHUNG KHONG bam Luu.
     *  Truyen code = null hoac "" de tu sinh ma duy nhat phia test (dot 21:
     *  nut 'Tạo mã ngẫu nhiên' DA BO khoi trang tao moi); tra ve ma voucher
     *  da dung (de test tiep tuc thao tac tren voucher do). Loai giam:
     *  'Fixed' / 'Percentage' / 'FreeShip' (FreeShip KHONG co o Gia tri giam
     *  — chi hien ghi chu 'Miễn phí phí vận chuyển — không cần nhập giá trị.'). */
    public String fillPromoForm(String code, String description, String discountType,
                                String discountValue, String minOrder, String maxDiscount,
                                String maxUses, String expiryDate) {
        String codeUsed = code;
        if (codeUsed == null || codeUsed.isEmpty()) {
            codeUsed = "AUTO" + (System.currentTimeMillis() % 100000000L);
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_CODE_INPUT, codeUsed);
        } else {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_CODE_INPUT, codeUsed);
        }
        if (description != null && !description.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_DESCRIPTION_INPUT, description);
        }
        if ("Fixed".equalsIgnoreCase(discountType)) {
            clickToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FIXED);
        } else if ("FreeShip".equalsIgnoreCase(discountType)) {
            clickToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FREESHIP);
        } else {
            clickToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_PERCENT);
        }
        if (!"FreeShip".equalsIgnoreCase(discountType)) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_VALUE_INPUT, discountValue);
        }
        if (minOrder != null && !minOrder.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_MIN_ORDER_INPUT, minOrder);
        }
        if (maxDiscount != null && !maxDiscount.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_MAX_DISCOUNT_INPUT, maxDiscount);
        }
        if (maxUses != null && !maxUses.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_MAX_USES_INPUT, maxUses);
        }
        if (expiryDate != null && !expiryDate.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_EXPIRY_INPUT, expiryDate);
        }
        return codeUsed;
    }

    /** Gia tri dang hien trong o Ma khuyen mai. */
    public String getPromoCodeInputValue() {
        return getElementValue(AdminPageUI.ADMIN_PROMO_CODE_INPUT);
    }

    /** O Ma khuyen mai co dang bi disable khong (khi sua thi disable). */
    public boolean isPromoCodeInputDisabled() {
        return !isElementEnabled(AdminPageUI.ADMIN_PROMO_CODE_INPUT);
    }

    /** Bam Luu trong form khuyen mai (chi bam, khong doi ket qua — dung khi test
     *  can doc loi sau khi luu that bai, vd trung ma). */
    public void clickPromoSaveButton() {
        clickToElement(AdminPageUI.ADMIN_PROMO_SAVE_BTN);
    }

    /** Bam Luu trong form khuyen mai roi doi thong bao + form dong (luu thanh cong). */
    public void savePromoForm() {
        clickPromoSaveButton();
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Noi dung loi cua 1 truong trong form khuyen mai (field = code/value/...);
     *  "" neu truong do khong co loi. */
    public String getPromoFieldErrorText(String field) {
        if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_PROMO_FIELD_ERROR, field))) {
            return "";
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_PROMO_FIELD_ERROR, field));
    }

    /** Tao voucher moi dien DU truong form moi: ma, mo ta, loai giam (Percentage/
     *  Fixed/FreeShip), gia tri, don toi thieu, tran giam, tong luot dung, ngay het han
     *  (yyyy-MM-dd). De trong ("") thi bo qua o do. Truyen code = null hoac "" de
     *  tu sinh ma duy nhat phia test (dot 21: nut tao ma ngau nhien DA BO);
     *  tra ve ma voucher da dung (de test tiep tuc thao tac tren voucher do). */
    public String createPromoFull(String code, String description, String discountType,
                                  String discountValue, String minOrder, String maxDiscount,
                                  String maxUses, String expiryDate) {
        openPromoForm();
        String codeUsed = fillPromoForm(code, description, discountType, discountValue,
                minOrder, maxDiscount, maxUses, expiryDate);
        savePromoForm();
        return codeUsed;
    }

    /** Gia tri ma dang hien trong o ma khuyen mai. */
    public String getPromoCodeValue() {
        return getElementValue(AdminPageUI.ADMIN_PROMO_CODE_INPUT);
    }

    /** Chon loai giam 'Giảm tiền cố định' (radio card, dot 21). */
    public void clickPromoDiscountTypeFixed() {
        clickToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FIXED);
        sleepInSecond(1);
    }

    /** Chon loai giam 'Giảm theo %' (radio card, dot 21). */
    public void clickPromoDiscountTypePercent() {
        clickToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_PERCENT);
        sleepInSecond(1);
    }

    /** Chon loai giam 'Miễn phí ship' (radio card, dot 21): chon xong KHONG co
     *  o Gia tri giam, chi hien ghi chu 'Miễn phí phí vận chuyển — không cần
     *  nhập giá trị.'; luu gui discountValue = 0. */
    public void clickPromoDiscountTypeFreeShip() {
        clickToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FREESHIP);
        sleepInSecond(1);
    }

    /** Ghi chu FreeShip co dang hien khong (thay o Gia tri giam). */
    public boolean isPromoFreeShipNoteDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PROMO_FREESHIP_NOTE);
    }

    /** Dong trang thai o muc Trang thai cua form tao/sua ('Nháp — ...' khi tao
     *  moi; 'Trạng thái hiện tại: ...' khi sua). */
    public String getPromoStatusText() {
        return getElementText(AdminPageUI.ADMIN_PROMO_STATUS_TEXT);
    }

    /** Nhap ngay het han khuyen mai (input type="date", dinh dang yyyy-MM-dd). */
    public void setPromoExpiryDate(String yyyyMmDd) {
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_EXPIRY_INPUT, yyyyMmDd);
        sleepInSecond(1);
    }

    /** Toggle 'Kích hoạt ngay sau khi tạo' trong form co dang hien khong
     *  (dot 21: DA BO khoi trang tao moi — tao moi mac dinh Nhap; ham nay
     *  dung de khang dinh am). */
    public boolean isPromoStatusToggleDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_PROMO_STATUS_TOGGLE);
    }

    /** Tieu de + mo ta cua modal xac nhan CHUNG doi trang thai (statusChange):
     *  'Xác nhận thay đổi trạng thái?' / 'Bạn có chắc chắn muốn thay đổi
     *  trạng thái của khuyến mãi {mã} không?'; xac nhan thi goi activate /
     *  disable + toast 'Đổi trạng thái khuyến mãi thành công!'. Dung chung
     *  cho ca bat (Nhap/Da tat -> Dang chay) va tat (Dang chay -> Da tat)
     *  qua Switch tren dong. */
    private void confirmPromoStatusChange(String code) {
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String title = getConfirmDialogTitle();
        if (!title.contains("Xác nhận thay đổi trạng thái?")) {
            throw new AssertionError("Gat Switch voucher phai mo modal chung 'Xác nhận thay đổi trạng thái?', dang thay: " + title);
        }
        String dialogText = getConfirmDialogText();
        if (!dialogText.contains("khuyến mãi " + code)) {
            throw new AssertionError("Modal doi trang thai phai nhac ma voucher '" + code + "', dang thay: " + dialogText);
        }
        if (!getConfirmDialogConfirmText().equals("Xác nhận")) {
            throw new AssertionError("Nut xac nhan modal doi trang thai phai la 'Xác nhận', dang thay: " + getConfirmDialogConfirmText());
        }
        if (getConfirmDialogFooterButtonCount() != 2) {
            throw new AssertionError("Modal doi trang thai phai CHI co dung 2 nut, dang thay: " + getConfirmDialogFooterButtonCount());
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        waitForToast();
        String toastTitle = getToastTitleText();
        if (!toastTitle.contains("Đổi trạng thái khuyến mãi thành công!")) {
            throw new AssertionError("Toast doi trang thai voucher phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + toastTitle);
        }
        String toastDesc = getToastDescText();
        if (!toastDesc.contains("Khuyến mãi " + code + " đã được cập nhật trạng thái.")) {
            throw new AssertionError("Toast doi trang thai voucher phai ghi 'Khuyến mãi " + code + " đã được cập nhật trạng thái.', dang thay: " + toastDesc);
        }
        sleepInSecond(2); // cho loadPromos tai lai danh sach
    }

    /** Kich hoat voucher dang o trang thai Nhap / Da tat (Nhap -> Dang chay,
     *  Da tat -> Dang chay): gat Switch tren dong -> modal xac nhan CHUNG
     *  statusChange [Hủy] + [Xác nhận] -> xac nhan -> toast statusChanged.
     *  Voucher het han thi gap popup chan C5 thay vi modal nay. */
    public void activatePromo(String code) {
        clickRowStatusSwitch(code);
        confirmPromoStatusChange(code);
    }

    /** Tat voucher (Dang chay -> Da tat): gat Switch tren dong -> modal xac
     *  nhan CHUNG statusChange [Hủy] + [Xác nhận] -> xac nhan -> toast
     *  statusChanged. Dot 21 (22:37): KHONG con popup C3 nhap ly do o toggle
     *  — BE bat Reason nen FE gui ly do mac dinh 'Tắt bởi quản trị viên.'
     *  (dong voucher hien ly do nay sau khi tat). */
    public void disablePromo(String code) {
        clickRowStatusSwitch(code);
        confirmPromoStatusChange(code);
    }

    /** Tat voucher tu luong chan xoa A5: o popup 'Không thể xóa khuyến mãi'
     *  bam nut phai [Tắt khuyến mãi] -> mo popup tat C3 'Tắt khuyến mãi?'
     *  (o ly do BAT BUOC); nhap ly do + xac nhan -> toast statusChanged.
     *  Day la NOI DUY NHAT con dung popup C3 o module KM. */
    public void disablePromoFromDeleteBlocked(String code, String reason) {
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String title = getConfirmDialogTitle();
        if (!title.contains("Tắt khuyến mãi?")) {
            throw new AssertionError("Tu popup A5 phai mo popup tat C3 'Tắt khuyến mãi?', dang thay: " + title);
        }
        sendKeyToElement(AdminPageUI.CONFIRM_DIALOG_REASON_INPUT, reason);
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        waitForToast();
        String toastTitle = getToastTitleText();
        if (!toastTitle.contains("Đổi trạng thái khuyến mãi thành công!")) {
            throw new AssertionError("Toast tat voucher tu A5 phai co tieu de 'Đổi trạng thái khuyến mãi thành công!', dang thay: " + toastTitle);
        }
        sleepInSecond(2); // cho loadPromos tai lai danh sach
    }

    /** Popup tat voucher (ConfirmDialog 'Tắt khuyến mãi?') co dang hien khong
     *  (co o ly do + nut xac nhan 'Tắt khuyến mãi'). */
    public boolean isPromoDisableReasonModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.CONFIRM_DIALOG, 10)
                && getConfirmDialogTitle().contains("Tắt khuyến mãi?")
                && isElementDisplayed(AdminPageUI.CONFIRM_DIALOG_REASON_INPUT);
    }

    /** Loi duoi o ly do tat voucher (vd de trong ly do); "" neu khong co loi. */
    public String getPromoDisableReasonErrorText() {
        return getConfirmDialogReasonErrorText();
    }

    public boolean isEmptyPromosMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_PROMOS_MSG);
    }

    // ----- Sua/xoa voucher Draft + hop xac nhan chung (dot 13, muc 4-5) -----
    /** Nut 'Sua khuyen mai' cua dong voucher co ma cho truoc co hien khong
     *  (chi voucher Draft moi co nut sua). */
    public boolean isPromoEditButtonDisplayedFor(String code) {
        return getListWebElements(AdminPageUI.DYNAMIC_PROMO_EDIT_BTN, code).size() > 0;
    }

    /** Nut 'Xoa khuyen mai' cua dong voucher co ma cho truoc co hien khong
     *  (chi voucher Draft moi co nut xoa). */
    public boolean isPromoDeleteButtonDisplayedFor(String code) {
        return getListWebElements(AdminPageUI.DYNAMIC_PROMO_DELETE_BTN, code).size() > 0;
    }

    /** Sua voucher Draft: doi mo ta + gia tri giam (ma, loai giam, pham vi khong
     *  doi); luu xong ve lai danh sach + hien toast 'Đã cập nhật khuyến mãi'.
     *  Dot 21: form sua la TRANG FULL PAGE /promotions/:code/edit. */
    public void editPromo(String code, String newDescription, String newDiscountValue) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_EDIT_BTN, code);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_DESCRIPTION_INPUT, newDescription);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_VALUE_INPUT, newDiscountValue);
        clickToElement(AdminPageUI.ADMIN_PROMO_SAVE_BTN);
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Sua voucher Draft dien DU truong cho phep sua: mo ta, gia tri, don toi
     *  thieu, tran giam, ngay het han, tong luot dung (de trong "" thi giu nguyen). */
    public void editPromoFull(String code, String newDescription, String newDiscountValue,
                              String newMinOrder, String newMaxDiscount,
                              String newExpiryDate, String newMaxUses) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_EDIT_BTN, code);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
        if (newDescription != null && !newDescription.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_DESCRIPTION_INPUT, newDescription);
        }
        if (newDiscountValue != null && !newDiscountValue.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_VALUE_INPUT, newDiscountValue);
        }
        if (newMinOrder != null && !newMinOrder.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_MIN_ORDER_INPUT, newMinOrder);
        }
        if (newMaxDiscount != null && !newMaxDiscount.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_MAX_DISCOUNT_INPUT, newMaxDiscount);
        }
        if (newExpiryDate != null && !newExpiryDate.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_EXPIRY_INPUT, newExpiryDate);
        }
        if (newMaxUses != null && !newMaxUses.isEmpty()) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_MAX_USES_INPUT, newMaxUses);
        }
        clickToElement(AdminPageUI.ADMIN_PROMO_SAVE_BTN);
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Xoa voucher Draft: bam nut xoa tren dong -> ConfirmDialog 'Xóa khuyến mãi?'
     *  [Hủy] + [Xóa khuyến mãi] (co dong 'không thể hoàn tác.') -> xac nhan
     *  -> toast 'Đã xóa khuyến mãi'. */
    public void deletePromoByCode(String code) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_DELETE_BTN, code);
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String title = getConfirmDialogTitle();
        if (!title.contains("Xóa khuyến mãi?")) {
            throw new AssertionError("Xoa voucher phai mo popup 'Xóa khuyến mãi?', dang thay: " + title);
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        waitForFeedback();
        sleepInSecond(2); // cho loadPromos tai lai danh sach
    }

    /** Hop xac nhan dung chung (ConfirmDialog dot 18) co dang hien khong. */
    public boolean isConfirmModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.CONFIRM_DIALOG, 10);
    }

    /** Noi dung hop xac nhan (tieu de + thong diep). */
    public String getConfirmModalText() {
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        return getElementText(AdminPageUI.CONFIRM_DIALOG);
    }

    /** Bam nut xac nhan trong hop xac nhan chung (nhan thay doi theo hanh dong). */
    public void clickConfirmYes() {
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
    }

    /** Bam 'Hủy' trong hop xac nhan chung (khong thuc hien hanh dong). */
    public void clickConfirmNo() {
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CANCEL_BTN);
    }

    // ----- Hoi vien: doc so du diem khach (dot 13, muc 3) -----
    /** Doc so du diem cua 1 khach TRUC TIEP tu cot 'Điểm' trong bang Khach hang
     *  (tim theo email, doc o admin-customer-points-<id>); tra -1 neu khong tim
     *  thay khach nao co email do trong bang. Dot 18: form XEM khach KHONG con
     *  dong so du diem nen khong mo modal chi tiet nua. */
    public int getCustomerPointsByEmail(String email) {
        clickAdminNav("customers");
        searchCustomers(email);
        if (getAdminTableRowCount() == 0) {
            return -1;
        }
        String pointsText = getElementText(
                getDynamicLocator(AdminPageUI.DYNAMIC_CUSTOMER_POINTS_CELL, email)).trim();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("([\\d.,]+)").matcher(pointsText);
        if (!matcher.find()) {
            throw new AssertionError("Khong doc duoc so du diem tu o '" + pointsText + "'");
        }
        return Integer.parseInt(matcher.group(1).replace(".", "").replace(",", ""));
    }

    /** Dong form XEM khach hang bang nut 'Đóng' o footer (dot 18: khach mo
     *  bang nut mat se mo form XEM, khong con modal chi tiet rieng). */
    /** Dong trang chi tiet khach hang bang nut [Đóng] o header (dot 21: trang
     *  full page, KHONG con nut X Đóng cua Drawer); doi ve danh sach. */
    public void closeCustomerDetailModal() {
        clickToElement(AdminPageUI.ADMIN_CUSTOMER_CANCEL_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_CUSTOMER_DETAIL_PAGE);
    }

    // ----- Tra hang: nut 'Ghi nhan da hoan tien' (dot 13, muc 7) -----
    /** Co yeu cau tra hang nao dang hien nut 'Ghi nhận đã hoàn tiền' khong
     *  (nut chi hien khi Approved + PendingRefund + co refundId). */
    public boolean isRefundRecordButtonDisplayed() {
        return getListWebElements(AdminPageUI.ADMIN_RETURN_RECORD_REFUND_BTN).size() > 0;
    }

    /** Text cua dong tra hang chua nut 'Ghi nhận đã hoàn tiền' (dung de khang
     *  dinh dong do that su o trang thai Approved + PendingRefund); "" neu khong co. */
    public String getRefundRecordButtonRowText() {
        return getAdminTableRowTexts().stream()
                .filter(row -> row.contains("Ghi nhận đã hoàn tiền"))
                .findFirst().orElse("");
    }

    /** Bam nut 'Ghi nhận đã hoàn tiền' dau tien -> mo modal nhap ma giao dich
     *  (chi mo, khong nhap ma, khong xac nhan — khong tao giao dich that). */
    public void clickFirstRefundRecordButton() {
        clickToElement(AdminPageUI.ADMIN_RETURN_RECORD_REFUND_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_REFUND_RECORD_MODAL);
    }

    /** Modal ghi nhan hoan tien co dang hien khong + co du o ma giao dich va
     *  nut xac nhan khong. */
    public boolean isRefundRecordModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REFUND_RECORD_MODAL, 10)
                && isElementDisplayed(AdminPageUI.ADMIN_REFUND_TRANSACTION_INPUT)
                && isElementDisplayed(AdminPageUI.ADMIN_REFUND_RECORD_CONFIRM_BTN);
    }

    /** Dong modal ghi nhan hoan tien bang nut X (khong ghi nhan gi ca). */
    public void closeRefundRecordModal() {
        closeDetailModal("admin-refund-record-modal");
    }

    // ----- Tra hang full page dot 21: panel xac nhan inline (KHONG modal) ----
    /** Panel xac nhan Duyet co dang hien khong (sau khi bam [Duyệt] o header). */
    public boolean isReturnApprovePanelDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_RETURN_APPROVE_PANEL, 10);
    }

    /** Panel xac nhan Tu choi co dang hien khong (sau khi bam [Từ chối] o header). */
    public boolean isReturnRejectPanelDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_RETURN_REJECT_PANEL, 10);
    }

    /** Bam [Duyệt] o header trang chi tiet tra hang -> mo panel xac nhan inline. */
    public void clickReturnApproveButton() {
        clickToElement(AdminPageUI.ADMIN_RETURN_APPROVE_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_RETURN_APPROVE_PANEL);
    }

    /** Bam [Từ chối] o header trang chi tiet tra hang -> mo panel xac nhan inline. */
    public void clickReturnRejectButton() {
        clickToElement(AdminPageUI.ADMIN_RETURN_REJECT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_RETURN_REJECT_PANEL);
    }

    /** Bam [Duyệt yêu cầu] trong panel Duyet (chi bam, khong doi ket qua). */
    public void clickReturnApproveConfirmButton() {
        clickToElement(AdminPageUI.ADMIN_RETURN_APPROVE_CONFIRM_BTN);
    }

    /** Bam [Từ chối yêu cầu] trong panel Tu choi (chi bam, khong doi ket qua). */
    public void clickReturnRejectConfirmButton() {
        clickToElement(AdminPageUI.ADMIN_RETURN_REJECT_CONFIRM_BTN);
    }

    /** Nhap ly do tu choi vao o ly do trong panel Tu choi. */
    public void setReturnRejectReason(String reason) {
        sendKeyToElement(AdminPageUI.ADMIN_RETURN_REJECT_REASON_INPUT, reason);
    }

    /** Noi dung loi khi xac nhan tu choi ma chua nhap ly do; "" neu khong co loi. */
    public String getReturnRejectErrorText() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_RETURN_REJECT_ERROR)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_RETURN_REJECT_ERROR);
    }

    /** Text cua trang chi tiet tra hang (breadcrumb + tieu de + 2 card). */
    public String getReturnDetailText() {
        waitForElementVisible(AdminPageUI.ADMIN_RETURN_DETAIL_PAGE);
        return getElementText(AdminPageUI.ADMIN_RETURN_DETAIL_PAGE);
    }

    /** Ve danh sach tra hang bang breadcrumb [Trả hàng] o dau trang chi tiet
     *  full page (dot 21: khong con nut X Đóng cua Drawer/Modal). */
    public void closeReturnDetailPage() {
        clickToElement(AdminPageUI.ADMIN_ENTITY_BREADCRUMB_BACK);
        waitForElementInvisible(AdminPageUI.ADMIN_RETURN_DETAIL_PAGE);
    }

    // ----- Nhat ky he thong: loc ngay (dot 13, muc 10; viet lai dot 18) -----
    /** Tieu de 'Nhật ký hệ thống (audit-log)' o cuoi tab Tra hang co hien khong. */
    public boolean isAuditLogSectionDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_AUDIT_LOG_HEADING, 10);
    }

    /** Nhap khoang ngay loc nhat ky (dinh dang yyyy-MM-dd, vd 2026-01-01). */
    public void setAuditDateFilter(String dateFrom, String dateTo) {
        sendKeyToElement(AdminPageUI.ADMIN_LOG_DATE_FROM, dateFrom);
        sendKeyToElement(AdminPageUI.ADMIN_LOG_DATE_TO, dateTo);
    }

    /** Bam nut icon 'Tìm' o toolbar Nhat ky de ap dung khoang ngay (dot 18:
     *  nut 'Lọc' rieng da bo, khoang ngay ap dung khi bam Tim). */
    public void clickAuditFilter() {
        clickToElement(AdminPageUI.ADMIN_LOG_SEARCH_BTN);
        sleepInSecond(2); // cho loc ap dung
    }

    /** Dot 18: Export nhat ky DA BO HAN — khang dinh am: khong co nut Export
     *  trong toolbar muc Nhat ky. */
    public boolean isAuditExportButtonDisplayed() {
        return getListWebElements("xpath=//div[contains(@class,'sa-page-toolbar')]//button[contains(.,'Export')]").size() > 0;
    }

    // ----- Don hang (dot 10: chi doi/huy 1 don 'Chờ xử lý' CO SAN trong DB;
    //  khong dat don moi qua web ban hang nua, khong khoi phuc duoc trang thai cu) -----
    /** Mo muc Don hang, tim theo ma don (loc server-side) roi mo TRANG chi tiet
     *  cua don do (/orders/:code, dot 21 — khong con Drawer/Modal). */
    public void searchOrderByCodeAndOpen(String orderCode) {
        clickAdminNav("orders");
        searchOrdersAndApply(orderCode);
        clickToElement(AdminPageUI.DYNAMIC_ORDER_DETAIL_BTN, orderCode);
        waitForElementVisible(AdminPageUI.ADMIN_ORDER_DETAIL);
    }

    /** Doi trang thai don tren TRANG chi tiet (chon nhan tieng Viet, vd 'Đã xác nhận'):
     *  bam [Đổi trạng thái] o header -> panel mo -> chon dich -> bam
     *  [Xác nhận đổi] trong panel -> toast 'Đã cập nhật đơn hàng'.
     *  Dot 21: KHONG con popup D2/D3 — panel chinh la buoc xac nhan. */
    public void changeOrderStatusTo(String visibleStatusLabel) {
        openChangeOrderStatusDialog(visibleStatusLabel);
        clickToElement(AdminPageUI.ADMIN_ORDER_STATUS_CONFIRM_BTN);
        waitForToast();
        sleepInSecond(2); // cho chi tiet don + danh sach tai lai
    }

    /** Mo panel doi trang thai don (chi mo + chon dich, khong xac nhan) — dung
     *  khi test can khang dinh noi dung panel roi bam [Đóng]. */
    public void openChangeOrderStatusDialog(String visibleStatusLabel) {
        openOrderStatusPanel();
        selectByVisibleText(AdminPageUI.ADMIN_ORDER_STATUS_SELECT, visibleStatusLabel);
    }

    /** Mo panel doi trang thai (neu chua mo): bam [Đổi trạng thái] o header. */
    public void openOrderStatusPanel() {
        if (getListWebElements(AdminPageUI.ADMIN_ORDER_STATUS_PANEL).isEmpty()) {
            clickToElement(AdminPageUI.ADMIN_ORDER_CHANGE_STATUS_BTN);
            waitForElementVisible(AdminPageUI.ADMIN_ORDER_STATUS_PANEL);
        }
    }

    /** Dong panel doi trang thai bang nut [Đóng] (khong doi trang thai). */
    public void closeOrderStatusPanel() {
        clickToElement(AdminPageUI.ADMIN_ORDER_STATUS_CLOSE_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_ORDER_STATUS_PANEL);
    }

    /** Huy don tren TRANG chi tiet: bam [Hủy đơn] o header -> panel huy don
     *  (nen do nhat) -> nhap ly do (BAT BUOC) -> bam [Xác nhận hủy đơn] ->
     *  toast 'Đã hủy đơn hàng'. Dot 21: KHONG con popup D1. */
    public void cancelOrderWithReason(String reason) {
        openCancelOrderDialog();
        sendKeyToElement(AdminPageUI.ADMIN_ORDER_CANCEL_REASON_INPUT, reason);
        clickToElement(AdminPageUI.ADMIN_ORDER_CANCEL_CONFIRM_BTN);
        waitForToast();
        sleepInSecond(2); // cho chi tiet don + danh sach tai lai
    }

    /** Mo panel huy don (chi mo, khong nhap ly do, khong xac nhan) — dung khi
     *  test can khang dinh tung buoc cua luong bat buoc ly do. */
    public void openCancelOrderDialog() {
        clickToElement(AdminPageUI.ADMIN_ORDER_CANCEL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_ORDER_CANCEL_PANEL);
    }

    /** Dong panel huy don bang nut [Giữ đơn] (khong huy don). */
    public void closeOrderCancelPanel() {
        clickToElement(AdminPageUI.ADMIN_ORDER_CANCEL_KEEP_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_ORDER_CANCEL_PANEL);
    }

    /** Ve danh sach don tu TRANG chi tiet: bam breadcrumb [← Đơn hàng]
     *  (dot 21 — khong con nut X / nut [Đóng] o footer nhu Drawer cu). */
    public void closeOrderDetail() {
        clickToElement(AdminPageUI.ADMIN_ORDER_DETAIL_BACK_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_ORDER_DETAIL);
        waitForElementVisible(AdminPageUI.ADMIN_ORDER_SEARCH);
    }

    /** Tieu de trang chi tiet: 'Đơn <mã đơn>' (khong kem trang thai nhu
     *  tieu de Drawer cu 'Đơn <mã> — <trạng thái>'). */
    public String getOrderDetailTitleText() {
        waitForElementVisible(AdminPageUI.ADMIN_ORDER_DETAIL_TITLE);
        return getElementText(AdminPageUI.ADMIN_ORDER_DETAIL_TITLE);
    }

    /** Nhan trang thai (pill) o tieu de trang chi tiet. */
    public String getOrderDetailStatusPillText() {
        waitForElementVisible(AdminPageUI.ADMIN_ORDER_DETAIL_STATUS);
        return getElementText(AdminPageUI.ADMIN_ORDER_DETAIL_STATUS);
    }

    /** Nhan 2 nut o header trang chi tiet ([Hủy đơn] + [Đổi trạng thái]),
     *  theo thu tu hien thi. */
    public java.util.List<String> getOrderDetailHeaderButtonTexts() {
        java.util.List<String> texts = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement btn : getListWebElements(AdminPageUI.ADMIN_ORDER_DETAIL_HEADER_BUTTONS)) {
            texts.add(btn.getText().trim());
        }
        return texts;
    }

    public boolean isOrderStatusPanelDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_ORDER_STATUS_PANEL);
    }

    public boolean isOrderCancelPanelDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_ORDER_CANCEL_PANEL);
    }

    public boolean isOrderStatusConfirmButtonEnabled() {
        return isElementEnabled(AdminPageUI.ADMIN_ORDER_STATUS_CONFIRM_BTN);
    }

    public boolean isOrderCancelButtonEnabled() {
        return isElementEnabled(AdminPageUI.ADMIN_ORDER_CANCEL_BTN);
    }

    public boolean isOrderCancelReasonInputDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_ORDER_CANCEL_REASON_INPUT);
    }

    /** Bam [Xác nhận hủy đơn] trong panel huy don (khong doi toast xuat hien). */
    public void clickOrderCancelConfirmButton() {
        clickToElement(AdminPageUI.ADMIN_ORDER_CANCEL_CONFIRM_BTN);
        sleepInSecond(1);
    }

    /** Loi inline trong panel huy don (ly do trong) — "" neu khong hien. */
    public String getOrderCancelErrorText() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_ORDER_CANCEL_ERROR)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_ORDER_CANCEL_ERROR);
    }

    public void setOrderCancelReason(String reason) {
        sendKeyToElement(AdminPageUI.ADMIN_ORDER_CANCEL_REASON_INPUT, reason);
    }

    /** Tim don theo ma roi kiem tra dong cua no co hien dung nhan trang thai khong. */
    public boolean isOrderRowWithStatusDisplayed(String orderCode, String statusLabel) {
        searchOrdersAndApply(orderCode);
        return getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains(orderCode) && text.contains(statusLabel));
    }

    // ----- Hoi vien: dieu chinh diem -----
    /** Dieu chinh diem cho 1 khach (nhap email hoac ID; diem duong = cong, am = tru,
     *  khac 0, toi da 1000/lan; bat buoc co ly do). Dot 18 wave D (spec A–H
     *  F2/F3): cong mo popup rieng 'Xác nhận cộng điểm?' [Hủy]+[Xác nhận cộng
     *  điểm] (info), tru mo 'Xác nhận trừ điểm?' [Hủy]+[Xác nhận trừ điểm]
     *  (cam); ca 2 deu hien ro so diem + so du moi -> xac nhan -> toast
     *  'Đã điều chỉnh điểm' va o so diem + ly do duoc xoa trang. */
    public void adjustPointsForUser(String emailOrId, String points, String reason) {
        clickAdminNav("membership");
        waitForElementVisible(AdminPageUI.ADMIN_ADJUST_POINTS_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_USER_INPUT, emailOrId);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_POINTS_INPUT, points);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_REASON_INPUT, reason);
        clickToElement(AdminPageUI.ADMIN_ADJUST_POINTS_BTN);
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String title = getConfirmDialogTitle();
        boolean isSubtract = points.trim().startsWith("-");
        String expectedTitle = isSubtract ? "Xác nhận trừ điểm?" : "Xác nhận cộng điểm?";
        String expectedConfirm = isSubtract ? "Xác nhận trừ điểm" : "Xác nhận cộng điểm";
        if (!title.contains(expectedTitle)) {
            throw new AssertionError("Dieu chinh diem " + points + " phai mo popup '"
                    + expectedTitle + "', dang thay: " + title);
        }
        if (!getConfirmDialogConfirmText().contains(expectedConfirm)) {
            throw new AssertionError("Nut xac nhan dieu chinh diem " + points + " phai la '"
                    + expectedConfirm + "', dang thay: " + getConfirmDialogConfirmText());
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        waitForToast();
    }

    /** Sau khi dieu chinh diem thanh cong, o so diem + ly do phai duoc xoa trang. */
    public boolean isAdjustPointsFormCleared() {
        return getElementValue(AdminPageUI.ADMIN_ADJUST_POINTS_INPUT).isEmpty()
                && getElementValue(AdminPageUI.ADMIN_ADJUST_REASON_INPUT).isEmpty();
    }

    // ---------- Muc Danh gia (dot 16) ----------
    // BE moi GET /api/admin/evaluations (loc productId/rating/search, phan trang
    // 10/trang) + DELETE /api/admin/evaluations/{id} (SuperAdmin only, xoa CUNG
    // vi entity Evaluation khong co co an/xoa; xoa xong tinh lai Rating, ghi
    // AuditLog). Admin khong tao/sua danh gia — chi xem, loc/tim, Export, xoa.

    // Dot16-reviews: mo muc Danh gia tu sidebar admin (nav 'reviews').
    public void openReviewsSection() {
        clickAdminNav("reviews");
        waitForElementVisible(AdminPageUI.ADMIN_REVIEWS_SECTION);
    }

    // Dot16-reviews: muc Danh gia co hien tren sidebar khong.
    public boolean isReviewsNavDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_NAV_REVIEWS, 10);
    }

    // Dot16-reviews: section Danh gia co dang hien khong.
    public boolean isReviewsSectionDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVIEWS_SECTION, 10);
    }

    // Dot16-reviews: cho bang tai xong — het "Đang tải..." thi hoac co dong,
    // hoac hien trang thai rong "Chưa có đánh giá nào." (toi da 10 giay).
    public void waitForReviewsTableLoaded() {
        waitForElementVisible(AdminPageUI.ADMIN_REVIEWS_SECTION);
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            if (getReviewRowCount() > 0 || isReviewsEmptyStateDisplayed()) {
                return;
            }
            sleepInSecond(1);
        }
    }

    // Dot16-reviews: so dong danh gia dang hien trong bang (0 neu bang rong).
    public int getReviewRowCount() {
        return getListWebElements(AdminPageUI.ADMIN_REVIEW_TABLE_ROWS).size();
    }

    // Dot16-reviews: text 6 tieu de cot cua bang, theo dung thu tu hien thi.
    public List<String> getReviewHeaders() {
        List<String> headers = new ArrayList<>();
        for (WebElement th : getListWebElements(AdminPageUI.ADMIN_REVIEW_TABLE_HEADERS)) {
            headers.add(th.getText().trim());
        }
        return headers;
    }

    // Dot16-reviews: tim theo ten san pham / khach hang / noi dung (loc
    // server-side, TU DONG sau debounce 300ms dot 21 — KHONG con nut Tim).
    public void searchReviewsByText(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_REVIEW_SEARCH, keyword);
        sleepInSecond(2); // cho debounce 300ms + GET /api/admin/evaluations tai lai
    }

    // Dot16-reviews: loc theo so sao (value = "" tat ca, "1".."5") — TU DONG
    // tai lai dot 21, KHONG con nut Tim.
    public void filterReviewsByRating(String value) {
        selectByValue(AdminPageUI.ADMIN_REVIEW_RATING_FILTER, value);
        sleepInSecond(2); // cho GET /api/admin/evaluations tai lai
    }

    // Dot16-reviews: bam nut icon "Đặt lại" — xoa trang o tim/loc + ve trang 1.
    public void resetReviewsFilter() {
        clickToElement(AdminPageUI.ADMIN_REVIEWS_RESET_BTN);
        sleepInSecond(1); // cho loc client-side ap dung
    }

    // Dot16-reviews: ten san pham o dong dau tien cua bang.
    public String getFirstRowProductName() {
        return getElementText(AdminPageUI.ADMIN_REVIEW_FIRST_ROW_PRODUCT);
    }

    // Dot16-reviews: so sao o dong dau tien, parse so truoc "/5" (o hien
    // dang "(n/5)" ke ben 5 icon Star Lucide — wave D khong con chuoi '★').
    public int getFirstRowRating() {
        String ratingText = getElementText(AdminPageUI.ADMIN_REVIEW_FIRST_ROW_RATING);
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\((\\d+)/5\\)")
                .matcher(ratingText);
        if (!matcher.find()) {
            throw new AssertionError("Khong doc duoc so sao tu o '" + ratingText + "' (du kien dang '(n/5)')");
        }
        return Integer.parseInt(matcher.group(1));
    }

    // Dot16-reviews: so sao cua TAT CA dong dang hien trong bang (theo thu tu dong).
    public List<Integer> getAllRowRatings() {
        List<Integer> ratings = new ArrayList<>();
        for (WebElement span : getListWebElements(AdminPageUI.ADMIN_REVIEW_ROW_RATING_SPANS)) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\((\\d+)/5\\)")
                    .matcher(span.getText());
            if (!matcher.find()) {
                throw new AssertionError("Khong doc duoc so sao tu o '" + span.getText() + "' (du kien dang '(n/5)')");
            }
            ratings.add(Integer.parseInt(matcher.group(1)));
        }
        return ratings;
    }

    // Dot18-wave-D-reviews: so sao o 1 dong (rowIndex 1-based), doc tu
    // aria-label "{n} trên 5 sao" cua o Sao (khong con chuoi '★').
    public int getReviewRowRatingFromAriaLabel(int rowIndex) {
        String label = getElementAttribute(
                getDynamicLocator(AdminPageUI.DYNAMIC_REVIEW_ROW_RATING_LABEL, String.valueOf(rowIndex)),
                "aria-label");
        if (label == null) {
            throw new AssertionError("Dong danh gia " + rowIndex + " khong co aria-label so sao");
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+) trên 5 sao")
                .matcher(label);
        if (!matcher.find()) {
            throw new AssertionError("Khong doc duoc so sao tu aria-label '" + label + "'");
        }
        return Integer.parseInt(matcher.group(1));
    }

    // Dot18-wave-D-reviews: so icon sao Lucide (svg.lucide-star) o 1 dong —
    // moi dong luon co dung 5 icon (day + rong).
    public int getReviewRowStarIconCount(int rowIndex) {
        return getListWebElements(AdminPageUI.DYNAMIC_REVIEW_ROW_STAR_ICONS, String.valueOf(rowIndex)).size();
    }

    // Dot18-wave-D-reviews: so icon sao DAY (fill #f59e0b) o 1 dong — phai
    // bang rating cua dong do.
    public int getReviewRowFilledStarCount(int rowIndex) {
        return getListWebElements(AdminPageUI.DYNAMIC_REVIEW_ROW_FILLED_STAR_ICONS, String.valueOf(rowIndex)).size();
    }

    // Dot18-wave-D-reviews: so icon sao DAY trong modal chi tiet danh gia.
    public int getReviewDetailFilledStarCount() {
        return getListWebElements(AdminPageUI.ADMIN_REVIEW_DETAIL_FILLED_STAR_ICONS).size();
    }

    // Dot16-reviews: noi dung (DA CAT 80 ky tu) o dong dau tien cua bang.
    public String getFirstRowComment() {
        return getElementText(AdminPageUI.ADMIN_REVIEW_FIRST_ROW_COMMENT);
    }

    // Dot16-reviews: bam ca dong dau tien -> mo modal chi tiet danh gia.
    public void openFirstReviewDetail() {
        clickToElement(AdminPageUI.ADMIN_REVIEW_FIRST_ROW);
        waitForElementVisible(AdminPageUI.ADMIN_REVIEW_DETAIL_PAGE);
    }

    // Dot16-reviews: noi dung DAY DU (khong cat) trong trang chi tiet — lay tu
    // KvRow co label 'Nội dung'.
    public String getDetailModalComment() {
        return getElementText(AdminPageUI.ADMIN_REVIEW_DETAIL_COMMENT);
    }

    // Dot16-reviews: trang chi tiet danh gia full page co dang hien khong.
    public boolean isDetailModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVIEW_DETAIL_PAGE, 10);
    }

    // Dot16-reviews: nut "Xóa đánh giá" trong trang chi tiet co hien khong.
    public boolean isReviewDeleteButtonInModalDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_REVIEW_DELETE_BTN);
    }

    // Dot16-reviews: dong trang chi tiet danh gia bang nut [Quay lại] o header.
    public void closeReviewDetailModal() {
        clickToElement(AdminPageUI.ADMIN_REVIEW_DETAIL_BACK_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_REVIEW_DETAIL_PAGE);
    }

    // Dot16-reviews: xoa dong CUOI cua trang hien tai — bam nut xoa tren dong ->
    // ConfirmDialog 'Xóa đánh giá?' [Hủy] + [Xóa đánh giá] (co dong 'không thể
    // hoàn tác.') -> bam xac nhan -> toast 'Đã xóa đánh giá'. XOA THAT, KHONG
    // khoi phuc duoc — test goi ham nay phai dam bao con du dong sau khi xoa.
    public String deleteLastReview() {
        clickToElement(AdminPageUI.ADMIN_REVIEW_LAST_ROW_DELETE_BTN);
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String confirmText = getConfirmDialogText();
        if (!confirmText.contains("Xóa đánh giá?")
                || !confirmText.contains("không thể hoàn tác")) {
            throw new AssertionError("Hop xac nhan xoa danh gia phai la ConfirmDialog "
                    + "'Xóa đánh giá?' + dong 'không thể hoàn tác.', dang thay: " + confirmText);
        }
        if (getConfirmDialogFooterButtonCount() != 2) {
            throw new AssertionError("Popup xoa danh gia phai CHI co dung 2 nut, dang thay: "
                    + getConfirmDialogFooterButtonCount());
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        String feedback = waitForFeedback();
        sleepInSecond(2); // cho loadReviews tai lai danh sach
        return feedback;
    }

    // Dot16-reviews: getToastText() DA CO SAN o tren (dung ADMIN_TOAST) — dung
    // lai, khong dinh nghia trung.

    // Dot16-reviews: khoi phan trang cua muc Danh gia co hien khong (chi hien khi co dong).
    public boolean isReviewsPaginationDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVIEWS_PAGINATION, 10);
    }

    // Dot16-reviews: text cua khoi phan trang ('Hiển thị {from}–{to} / {total}
    // kết quả' + 'Trang X / Y'); "" neu khong co phan trang.
    public String getPaginationInfoText() {
        if (!isReviewsPaginationDisplayed()) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_REVIEWS_PAGINATION);
    }

    // Dot16-reviews: tong so trang (Y trong "Trang X / Y"); -1 neu khong co phan trang.
    public int getReviewsPaginationTotalPages() {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("Trang\\s+\\d+\\s*/\\s*(\\d+)")
                .matcher(getPaginationInfoText());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    // Dot16-reviews: tong so dong (T trong "Hiển thị {from}–{to} / T kết quả"); -1 neu khong co phan trang.
    public int getReviewsPaginationTotalRows() {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("Hiển thị\\s+\\d+–\\d+\\s*/\\s*(\\d+)\\s+kết quả")
                .matcher(getPaginationInfoText());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    // Dot16-reviews: nut 'Sau' cua phan trang Danh gia co bam duoc khong (bi tat
    // o trang cuoi). Dot 18 KHONG con nut so trang.
    public boolean isReviewsNextEnabled() {
        return isElementEnabled(AdminPageUI.ADMIN_REVIEWS_PAGINATION_NEXT);
    }

    public void clickReviewsPaginationNext() {
        clickToElement(AdminPageUI.ADMIN_REVIEWS_PAGINATION_NEXT);
        sleepInSecond(1); // cho trang moi render
    }

    // Dot18-reviews: Export DA BO HAN khoi muc Danh gia — khang dinh am: khong
    // co nut Export trong toolbar muc Danh gia.
    public boolean isReviewsExportButtonDisplayed() {
        return getListWebElements("xpath=//div[contains(@class,'sa-page-toolbar')]//button[contains(.,'Export')]").size() > 0;
    }

    // Dot16-reviews: trang thai rong "Chưa có đánh giá nào" co hien khong (luc do khong co bang).
    public boolean isReviewsEmptyStateDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_REVIEWS_MSG);
    }

    // ---------- Dot 17: Switch trang thai / loc Trang thai / STT / cot Ma ----------
    /** Bam Switch trang thai tren dong co chua chuoi cho truoc (ten SP/DM hoac
     *  ma voucher); Switch chi mo popup xac nhan, chua doi trang thai ngay. */
    public void clickRowStatusSwitch(String rowText) {
        clickToElement(AdminPageUI.DYNAMIC_ROW_STATUS_SWITCH, rowText);
        sleepInSecond(1); // cho popup xac nhan / modal ly do hien ra
    }

    /** Trang thai Switch tren dong co chua chuoi cho truoc (true = Hoat dong,
     *  false = Ngung hoat dong / Da khoa / Nhap / Het han); null neu dong do
     *  khong co Switch. Dot 21: voucher Draft CO Switch (tat), voucher
     *  Expired CO Switch nhung bi disable (tat). */
    public Boolean getRowStatusSwitchChecked(String rowText) {
        List<WebElement> switches = getListWebElements(AdminPageUI.DYNAMIC_ROW_STATUS_SWITCH, rowText);
        if (switches.isEmpty()) {
            return null;
        }
        return "true".equals(switches.get(0).getAttribute("aria-checked"));
    }

    /** Dong co chua chuoi cho truoc co Switch trang thai khong. */
    public boolean isRowStatusSwitchDisplayed(String rowText) {
        return getListWebElements(AdminPageUI.DYNAMIC_ROW_STATUS_SWITCH, rowText).size() > 0;
    }

    /** Trang thai Switch cua TAT CA dong dang hien trong bang cua muc dang mo
     *  (theo thu tu dong); dung de khang dinh loc Trang thai loc dung. */
    public List<Boolean> getAllRowStatusSwitchStates() {
        List<Boolean> states = new ArrayList<>();
        for (WebElement sw : getListWebElements(AdminPageUI.ADMIN_TABLE_ROW_SWITCHES)) {
            states.add("true".equals(sw.getAttribute("aria-checked")));
        }
        return states;
    }

    /** Chon gia tri trong dropdown loc Trang thai o toolbar cua muc dang mo
     *  (SP/DM: "" tat ca / "true" Hoat dong / "false" Ngung; voucher: "" /
     *  "Draft" / "Active" / "Disabled" / "Expired"; KH: "" / "active" / "locked").
     *  Loc ap dung ngay khi doi select (client-side hoac server-side tuy muc);
     *  dot 18 KHONG con nut 'Lọc' de bam them. */
    public void selectStatusFilterByValue(String value) {
        selectByValue(AdminPageUI.ADMIN_STATUS_FILTER, value);
        sleepInSecond(2); // cho loc ap dung + bang tai lai
    }

    /** Gia tri dang chon trong dropdown loc Trang thai ("" = Tat ca). */
    public String getStatusFilterValue() {
        return getElementValue(AdminPageUI.ADMIN_STATUS_FILTER);
    }

    /** STT o dong dau tien cua bang dang mo (td[1]); -1 neu bang rong. */
    public int getFirstRowStt() {
        if (getAdminTableRowCount() == 0) {
            return -1;
        }
        String stt = getElementText(AdminPageUI.ADMIN_FIRST_ROW_STT).trim();
        try {
            return Integer.parseInt(stt);
        } catch (NumberFormatException e) {
            throw new AssertionError("Khong doc duoc STT o dong dau (du kien so, dang thay: '" + stt + "')");
        }
    }

    /** Ma hien trong o cot Ma cua dong co chua chuoi cho truoc (ten SP/DM hoac
     *  email KH); codeTestid = admin-product-code / admin-category-code /
     *  admin-customer-code. */
    public String getRowCodeCellText(String rowText, String codeTestid) {
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_ROW_CODE_CELL, rowText, codeTestid)).trim();
    }

    /** Ma SP hien trong dong co ten cho truoc. */
    public String getProductCodeInRow(String productName) {
        return getRowCodeCellText(productName, "admin-product-code");
    }

    /** Ma DM hien trong dong co ten cho truoc. */
    public String getCategoryCodeInRow(String categoryName) {
        return getRowCodeCellText(categoryName, "admin-category-code");
    }

    /** Ma KH hien trong dong co email cho truoc. */
    public String getCustomerCodeInRow(String email) {
        return getRowCodeCellText(email, "admin-customer-code");
    }

    // ---------- Dot 18: popup chan xoa (ConfirmDialog, khong con modal rieng) ----------
    /** Popup 'Không thể xóa sản phẩm' / 'Không thể xóa danh mục' co dang hien
     *  khong (ConfirmDialog, tieu de bat dau 'Không thể xóa'). */
    public boolean isDeleteBlockedModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.CONFIRM_DIALOG, 10)
                && getConfirmDialogTitle().startsWith("Không thể xóa");
    }

    /** Noi dung popup chan xoa (tieu de + thong diep). */
    public String getDeleteBlockedModalText() {
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        return getElementText(AdminPageUI.CONFIRM_DIALOG);
    }

    /** Bam 'Chuyển sang Ngừng hoạt động' trong popup chan xoa (mo tiep
     *  modal xac nhan doi trang thai: SP dot 21 la modal CHUNG 'Xác nhận thay
     *  đổi trạng thái?', DM van co the la 'Ẩn danh mục?' — chua xac nhan). */
    public void clickDeleteBlockedConvert() {
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
    }

    /** Bam 'Hủy' trong popup chan xoa (khong doi gi ca). */
    public void clickDeleteBlockedClose() {
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CANCEL_BTN);
        waitForElementInvisible(AdminPageUI.CONFIRM_DIALOG);
    }

    // ---------- Dot 18 wave D: form Drawer rong (khong con Modal giua man hinh) ----------
    /** Form SP dot 21 la TRANG full page — KHONG con Drawer nen LUON false
     *  (giu phuong thuc de call site khong doi; test ADM19-87 / ADM-13 viet
     *  lai assert trang thay vi Drawer). */
    public boolean isProductFormDrawer() {
        return false;
    }

    /** Container form co testid cho truoc co class fs-drawer khong (wave D:
     *  MOI form admin deu la Drawer, khong con fs-modal); false neu chua mo. */
    public boolean isFormDrawer(String formTestId) {
        if (!isElementDisplayed("css=[data-testid='" + formTestId + "']")) {
            return false;
        }
        String cls = getElementAttribute("css=[data-testid='" + formTestId + "']", "class");
        return cls != null && cls.contains("fs-drawer");
    }

    /** Be rong Drawer (px) cua form co testid cho truoc, doc tu CSS var
     *  --fs-width trong thuoc tinh style (SP 760, DM/KM/KH/Don 680);
     *  -1 neu khong doc duoc. */
    public int getFormDrawerWidth(String formTestId) {
        if (!isElementDisplayed("css=[data-testid='" + formTestId + "']")) {
            return -1;
        }
        String style = getElementAttribute("css=[data-testid='" + formTestId + "']", "style");
        if (style == null) {
            return -1;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("--fs-width:\\s*(\\d+)px").matcher(style);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    /** Bang danh sach co dang hien trong DOM khong. Dot 18: form mo dang
     *  Drawer (overlay) nen bang VAN O LAI trong DOM (khac dot 17). */
    public boolean isAdminTablePresent() {
        return getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS).size() > 0;
    }

    // ---------- Dot 23 phan A: modal doi mat khau giua man hinh (.sa-modal 560px) ----------
    /** Modal doi mat khau co dang hien khong. */
    public boolean isChangePasswordModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CHANGE_PASSWORD_MODAL, 10);
    }

    /** Modal doi mat khau co phai modal giua man hinh (.sa-modal) khong —
     *  dot 23 phan A: khong con Drawer FormShell (fs-drawer). */
    public boolean isChangePasswordModalCentered() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_CHANGE_PASSWORD_MODAL)) {
            return false;
        }
        String cls = getElementAttribute(AdminPageUI.ADMIN_CHANGE_PASSWORD_MODAL, "class");
        return cls != null && cls.contains("sa-modal") && !cls.contains("fs-drawer");
    }

    /** Be rong thuc te (px) cua modal doi mat khau — dot 23 phan A: 560px. */
    public String getChangePasswordModalWidth() {
        return getWebElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_MODAL).getCssValue("width");
    }

    /** 3 o mat khau co xep doc 1 cot khong (cung toa do X, Y tang dan) —
     *  dot 23 phan A: modal 3 hang, moi hang 1 o full width. */
    public boolean areChangePasswordFieldsStackedVertically() {
        org.openqa.selenium.Point current =
                getWebElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_CURRENT_INPUT).getLocation();
        org.openqa.selenium.Point next =
                getWebElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_NEW_INPUT).getLocation();
        org.openqa.selenium.Point confirm =
                getWebElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_CONFIRM_INPUT).getLocation();
        return current.getX() == next.getX() && next.getX() == confirm.getX()
                && current.getY() < next.getY() && next.getY() < confirm.getY();
    }

    /** So nut o footer modal doi mat khau — dot 18/23 quy dinh CHI [Hủy] +
     *  [Đổi mật khẩu] (2 nut). */
    public int getChangePasswordFooterButtonCount() {
        return getListWebElements(AdminPageUI.ADMIN_CHANGE_PASSWORD_FOOTER_BUTTONS).size();
    }

    /** Nhan cac nut o footer modal doi mat khau (theo thu tu hien thi). */
    public java.util.List<String> getChangePasswordFooterButtonTexts() {
        java.util.List<String> texts = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement btn : getListWebElements(
                AdminPageUI.ADMIN_CHANGE_PASSWORD_FOOTER_BUTTONS)) {
            texts.add(btn.getText().trim());
        }
        return texts;
    }

    /** Dong modal doi mat khau bang nut X (khong doi mat khau). */
    public void closeChangePasswordModal() {
        clickToElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_MODAL_CLOSE_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_CHANGE_PASSWORD_MODAL);
    }

    // ---------- Dot 17: menu tai khoan + trang Ho so ----------
    /** Bam chip tai khoan o topbar de mo/đong dropdown menu tai khoan. */
    public void clickUserMenuButton() {
        clickToElement(AdminPageUI.ADMIN_USER_MENU_BTN);
        sleepInSecond(1); // cho dropdown hien
    }

    /** Mo dropdown menu tai khoan neu dang dong (khong toggle khi da mo). */
    public void openUserMenuIfClosed() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_USER_MENU)) {
            clickUserMenuButton();
        }
    }

    /** Bam muc 'Đăng xuất' trong menu tai khoan — wave D (G3): KHONG dang xuat
     *  ngay ma mo popup 'Đăng xuất?' [Hủy]+[Đăng xuất]; test phai xu ly popup. */
    public void clickUserMenuLogout() {
        openUserMenuIfClosed();
        clickToElement(AdminPageUI.ADMIN_USER_MENU_LOGOUT);
        sleepInSecond(1); // cho popup 'Đăng xuất?' hien
    }

    /** Dropdown menu tai khoan co dang hien khong. */
    public boolean isUserMenuDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_USER_MENU, 10);
    }

    /** Noi dung dropdown menu tai khoan (ten + email + 2 muc). */
    public String getUserMenuText() {
        waitForElementVisible(AdminPageUI.ADMIN_USER_MENU);
        return getElementText(AdminPageUI.ADMIN_USER_MENU);
    }

    /** Bam 'Thông tin cá nhân' trong menu -> mo trang Ho so. */
    public void clickUserMenuProfile() {
        clickToElement(AdminPageUI.ADMIN_USER_MENU_PROFILE);
        waitForElementVisible(AdminPageUI.ADMIN_PROFILE_PAGE);
    }

    /** Ten hien tren chip tai khoan o topbar. */
    public String getTopbarUserName() {
        return getElementText(AdminPageUI.ADMIN_TOPBAR_USER_NAME);
    }

    /** Trang Ho so co dang hien khong. */
    public boolean isProfilePageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROFILE_PAGE, 10);
    }

    /** Toan bo text cua trang Ho so (the dau trang + thong tin ca nhan + bao mat). */
    public String getProfilePageText() {
        waitForElementVisible(AdminPageUI.ADMIN_PROFILE_PAGE);
        return getElementText(AdminPageUI.ADMIN_PROFILE_PAGE);
    }

    /** Ho ten o the dau trang Ho so (h2). */
    public String getProfileHeaderName() {
        return getElementText(AdminPageUI.ADMIN_PROFILE_HEADER_NAME);
    }

    /** Bam 'Sửa thông tin' de mo form sua ho so. */
    public void clickProfileEditButton() {
        clickToElement(AdminPageUI.ADMIN_PROFILE_EDIT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PROFILE_FORM);
    }

    /** Form sua ho so co dang hien khong. */
    public boolean isProfileFormDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROFILE_FORM, 10);
    }

    /** Nhap ho ten moi vao form sua ho so (sendKey xoa het roi go lai). */
    public void setProfileName(String fullName) {
        sendKeyToElement(AdminPageUI.ADMIN_PROFILE_NAME_INPUT, fullName);
    }

    /** Nhap SDT vao form sua ho so. */
    public void setProfilePhone(String phone) {
        sendKeyToElement(AdminPageUI.ADMIN_PROFILE_PHONE_INPUT, phone);
    }

    /** Nhap dia chi vao form sua ho so. */
    public void setProfileAddress(String address) {
        sendKeyToElement(AdminPageUI.ADMIN_PROFILE_ADDRESS_INPUT, address);
    }

    /** O Email trong form ho so co dang bi disable khong (chi xem, khong sua). */
    public boolean isProfileEmailInputDisabled() {
        return !isElementEnabled(AdminPageUI.ADMIN_PROFILE_EMAIL_INPUT);
    }

    /** Bam Luu trong form sua ho so (doi toast + form dong khi luu thanh cong). */
    public void clickProfileSaveButton() {
        clickToElement(AdminPageUI.ADMIN_PROFILE_SAVE_BTN);
        waitForToast();
        sleepInSecond(1); // cho refreshProfile tai lai ho so
    }

    /** Dong form sua ho so bang nut 'Hủy' (khong luu). */
    public void clickProfileCancelButton() {
        clickToElement(AdminPageUI.ADMIN_PROFILE_CANCEL_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_PROFILE_FORM);
    }

    /** Noi dung loi cua 1 truong trong form ho so (field = fullName/phoneNumber);
     *  "" neu truong do khong co loi. */
    public String getProfileFieldErrorText(String field) {
        if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_PROFILE_FIELD_ERROR, field))) {
            return "";
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_PROFILE_FIELD_ERROR, field));
    }

    /** Bam 'Đổi mật khẩu' de mo form doi mat khau. */
    public void openChangePasswordForm() {
        clickToElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CHANGE_PASSWORD_FORM);
    }

    /** Form doi mat khau co dang hien khong. */
    public boolean isChangePasswordFormDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CHANGE_PASSWORD_FORM, 10);
    }

    /** Dien 3 o cua form doi mat khau (mat khau hien tai / moi / xac nhan). */
    public void fillChangePasswordForm(String currentPassword, String newPassword, String confirmPassword) {
        sendKeyToElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_CURRENT_INPUT, currentPassword);
        sendKeyToElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_NEW_INPUT, newPassword);
        sendKeyToElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_CONFIRM_INPUT, confirmPassword);
    }

    /** Bam 'Đổi mật khẩu' de gui form (chi bam, khong doi ket qua — thanh cong
     *  thi BE vo hieu JWT va app ve /login; that bai thi form o lai + loi inline). */
    public void clickChangePasswordSaveButton() {
        clickToElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_SAVE_BTN);
        sleepInSecond(2); // cho POST /api/admin/change-password tra ve
    }

    /** Noi dung loi cua 1 truong trong form doi mat khau
     *  (field = currentPassword/newPassword/confirmPassword); "" neu khong co loi. */
    public String getChangePasswordFieldErrorText(String field) {
        if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_CHANGE_PASSWORD_FIELD_ERROR, field))) {
            return "";
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_CHANGE_PASSWORD_FIELD_ERROR, field));
    }

    /** Cho toi khi URL ve trang /login (doi mat khau thanh cong thi BE vo hieu
     *  JWT va app tu dang xuat); true neu ve /login trong thoi gian cho. */
    public boolean waitForLoginPage(int timeoutInSeconds) {
        long deadline = System.currentTimeMillis() + timeoutInSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (getCurrentPageUrl().contains("/login")) {
                return true;
            }
            sleepInSecond(1);
        }
        return false;
    }

    // ---------- Dot 17: sua thong tin khach hang ----------
    /** Mo form sua khach hang theo ten (chi mo, khong dien gi ca). Luu y: luu
     *  form goi PUT /api/admin/customers/{id} — endpoint nay BE dang bo sung
     *  song song (luc viet test chua co trong cay fork-check) nen test chi mo
     *  form doc trang thai + dong, KHONG bam Luu. */
    public void openCustomerEditForm(String fullName) {
        clickToElement(AdminPageUI.DYNAMIC_CUSTOMER_EDIT_BTN, fullName);
        waitForElementVisible(AdminPageUI.ADMIN_CUSTOMER_EDIT_PAGE);
        waitForElementVisible(AdminPageUI.ADMIN_CUSTOMER_FORM);
    }

    /** Form sua khach hang co dang hien khong. */
    public boolean isCustomerFormDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CUSTOMER_FORM, 10);
    }

    /** O Ma KH trong form sua co dang bi disable khong (luon disable). */
    public boolean isCustomerCodeInputDisabled() {
        return !isElementEnabled(AdminPageUI.ADMIN_CUSTOMER_CODE_INPUT);
    }

    /** O Email trong form sua KH co dang bi disable khong (luon disable). */
    public boolean isCustomerEmailInputDisabled() {
        return !isElementEnabled(AdminPageUI.ADMIN_CUSTOMER_EMAIL_INPUT);
    }

    /** Gia tri dang hien trong o Ma KH cua form sua. */
    public String getCustomerCodeInputValue() {
        return getElementValue(AdminPageUI.ADMIN_CUSTOMER_CODE_INPUT);
    }

    /** Gia tri dang hien trong o Ho ten cua form sua KH. */
    public String getCustomerNameInputValue() {
        return getElementValue(AdminPageUI.ADMIN_CUSTOMER_NAME_INPUT);
    }

    // ----- Dot 19: method moi tu file test case manual admin v18 -----

    public void addCategoryImageByUrl(String imageUrl) {
            clickToElement(AdminPageUI.ADMIN_CATEGORY_IMAGE_LINK_TOGGLE);
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_IMAGE_URL_INPUT, imageUrl);
            clickToElement(AdminPageUI.ADMIN_CATEGORY_IMAGE_URL_ADD_BTN);
            sleepInSecond(1);
        }

    public void addProductImageByUrl(String imageUrl) {
            clickToElement(AdminPageUI.ADMIN_PRODUCT_IMAGE_LINK_TOGGLE);
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_IMAGE_URL_INPUT, imageUrl);
            clickToElement(AdminPageUI.ADMIN_PRODUCT_IMAGE_URL_ADD_BTN);
            sleepInSecond(1); // cho thumbnail them vao form
        }

    public void clickAdjustPointsButton() {
        clickToElement(AdminPageUI.ADMIN_ADJUST_POINTS_BTN);
        sleepInSecond(1); // cho popup F2/F3 hoac loi inline hien ra
    }

    public void clickAuditReset() {
            clickToElement(AdminPageUI.ADMIN_LOG_RESET_BTN);
            sleepInSecond(1); // cho loc reset
        }

    public void clickCategoryImageLinkToggle() {
            clickToElement(AdminPageUI.ADMIN_CATEGORY_IMAGE_LINK_TOGGLE);
            sleepInSecond(1);
        }

    public void clickChangePasswordCancelButton() {
            clickToElement(AdminPageUI.ADMIN_CHANGE_PASSWORD_CANCEL_BTN);
            sleepInSecond(1);
        }

    public void clickCustomerCancelButtonOnly() {
        clickToElement(AdminPageUI.ADMIN_CUSTOMER_CANCEL_BTN);
    }

    public void clickCustomerSaveButton() {
        clickToElement(AdminPageUI.ADMIN_CUSTOMER_SAVE_BTN);
    }

    /** (BO dot 21) Nut 'Tim' o toolbar KH da bo — tim/loc TU DONG sau debounce
     *  300ms nen khong con nut de bam; dung searchCustomers() thay the. */

    public void clickDashboardActionView(String key) {
            clickToElement(AdminPageUI.DYNAMIC_DASHBOARD_ACTION_VIEW, key);
            sleepInSecond(2); // cho tab dich tai du lieu
        }

    public void clickDashboardCustomApply() {
            clickToElement(AdminPageUI.ADMIN_DASHBOARD_CUSTOM_APPLY);
            sleepInSecond(1);
        }

    public void clickDashboardDonutLegend(String key) {
            clickToElement(AdminPageUI.DYNAMIC_DASHBOARD_DONUT_LEGEND, key);
            sleepInSecond(2); // cho tab Don tai du lieu
        }

    public void clickDashboardKpi(String kpiKey) {
            clickToElement(AdminPageUI.DYNAMIC_DASHBOARD_KPI, kpiKey);
            sleepInSecond(2); // cho tab dich tai du lieu
        }

    public void clickDashboardRange(String rangeKey) {
            clickToElement(AdminPageUI.DYNAMIC_DASHBOARD_RANGE_BTN, rangeKey);
            sleepInSecond(1); // cho dashboard tai lai theo khoang moi
        }

    public void clickDashboardRecentOrdersAll() {
            clickToElement(AdminPageUI.ADMIN_DASHBOARD_RECENT_ORDERS_ALL);
            sleepInSecond(2); // cho tab Don tai du lieu
        }

    public void clickDashboardRefresh() {
            clickToElement(AdminPageUI.ADMIN_DASHBOARD_REFRESH_BTN);
            sleepInSecond(1);
        }

    public void clickDashboardTierRow(String tier) {
            clickToElement(AdminPageUI.DYNAMIC_DASHBOARD_TIER_ROW, tier);
            sleepInSecond(2); // cho tab Khach hang tai du lieu
        }

    public void clickFirstDashboardPromoRow() {
            clickToElement(AdminPageUI.ADMIN_DASHBOARD_PROMO_FIRST_ROW);
            sleepInSecond(2); // cho tab Khuyen mai tai du lieu
        }

    public void clickFirstDashboardRecentOrder() {
            clickToElement(AdminPageUI.ADMIN_DASHBOARD_RECENT_ORDER_FIRST_ROW);
            sleepInSecond(1);
        }

    public void clickForbiddenHomeButton() {
            clickToElement(AdminPageUI.ADMIN_FORBIDDEN_HOME_BTN);
            sleepInSecond(1);
        }

    public void clickForbiddenSwitchAccountButton() {
            clickToElement(AdminPageUI.ADMIN_FORBIDDEN_SWITCH_ACCOUNT_BTN);
            sleepInSecond(1);
        }

    public void clickFormOverlayOutsideDrawer(String formTestId) {
            WebElement overlay = getWebElement(getDynamicLocator(AdminPageUI.DYNAMIC_FORM_OVERLAY, formTestId));
            new org.openqa.selenium.interactions.Actions(getDriver()).moveToElement(overlay, 5, 5).click().perform();
            sleepInSecond(1);
        }

    public void clickMembersRefreshButton() {
        clickToElement(AdminPageUI.ADMIN_MEMBERS_REFRESH_BTN);
        sleepInSecond(2); // cho GET /api/admin/membership/config tai lai
    }

    public void clickMembersResetButton() {
        clickToElement(AdminPageUI.ADMIN_MEMBERS_RESET_BTN);
        sleepInSecond(1); // cho tierDraft tro ve gia tri da tai
    }

    /** Bam nut doi trang thai: panel dang dong thi MO panel; panel dang mo
     *  thi bam [Xác nhận đổi] trong panel (dung cho test validation — loi
     *  inline hien trong panel, khong mo popup D2/D3 nua). */
    public void clickOrderChangeStatusButton() {
            if (getListWebElements(AdminPageUI.ADMIN_ORDER_STATUS_PANEL).isEmpty()) {
                clickToElement(AdminPageUI.ADMIN_ORDER_CHANGE_STATUS_BTN);
                waitForElementVisible(AdminPageUI.ADMIN_ORDER_STATUS_PANEL);
            } else {
                clickToElement(AdminPageUI.ADMIN_ORDER_STATUS_CONFIRM_BTN);
            }
            sleepInSecond(1); // cho panel mo / loi inline hien ra
        }

    public void clickProfileBackButton() {
            clickToElement(AdminPageUI.ADMIN_PROFILE_BACK_BTN);
            sleepInSecond(1);
        }

    public void clickProfileSaveButtonNoWait() {
            clickToElement(AdminPageUI.ADMIN_PROFILE_SAVE_BTN);
            sleepInSecond(1); // cho loi inline hien (neu co)
        }

    public void clickPromoCancelButton() {
            clickToElement(AdminPageUI.ADMIN_PROMO_CANCEL_BTN);
            sleepInSecond(1); // cho hop 'Thoát mà chưa lưu?' hien ra (neu form dirty)
        }

    public void clickPromoSearchButton() {
            clickToElement(AdminPageUI.ADMIN_PROMO_SEARCH_BTN);
            sleepInSecond(2); // cho GET /api/admin/promotions tai lai
        }

    /** Bam icon Lượt sử dụng (History) o dong voucher -> mo TRANG Lich su su
     *  dung /promotions/:code/usages (dot 21: khong con bang con duoi bang). */
    public void clickPromoUsageButton(String code) {
            clickToElement(AdminPageUI.DYNAMIC_PROMO_USAGE_BTN, code);
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_USAGES_PAGE);
            sleepInSecond(2); // cho GET /api/admin/promotions/{id}/usage tai ve
        }

    public void clickRefundRecordCancel() {
            clickToElement(AdminPageUI.ADMIN_REFUND_RECORD_CANCEL_BTN);
            waitForElementInvisible(AdminPageUI.ADMIN_REFUND_RECORD_MODAL);
        }

    public void clickRefundRecordConfirm() {
            clickToElement(AdminPageUI.ADMIN_REFUND_RECORD_CONFIRM_BTN);
            sleepInSecond(1); // cho loi hien ra hoac POST record tra ve
        }

    public void clickReviewDeleteButtonInModal() {
        clickToElement(AdminPageUI.ADMIN_REVIEW_DELETE_BTN);
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
    }

    public void clickSaveTiersButton() {
        clickToElement(AdminPageUI.ADMIN_SAVE_TIERS_BTN);
        sleepInSecond(1); // cho popup F1 hoac loi inline hien ra
    }

    public void clickTierDetail(String tierLabel) {
        clickToElement(AdminPageUI.DYNAMIC_TIER_DETAIL_BTN, tierLabel);
        waitForElementVisible(AdminPageUI.ADMIN_TIER_DETAIL);
    }

    public String deleteReviewByRowIndex(int rowIndex) {
        clickToElement(AdminPageUI.DYNAMIC_REVIEW_ROW_DELETE_BTN, String.valueOf(rowIndex));
        waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        String confirmText = getConfirmDialogText();
        if (!confirmText.contains("Xóa đánh giá?")
                || !confirmText.contains("không thể hoàn tác")) {
            throw new AssertionError("Hop xac nhan xoa danh gia phai la ConfirmDialog "
                    + "'Xóa đánh giá?' + dong 'không thể hoàn tác.', dang thay: " + confirmText);
        }
        if (getConfirmDialogFooterButtonCount() != 2) {
            throw new AssertionError("Popup xoa danh gia phai CHI co dung 2 nut, dang thay: "
                    + getConfirmDialogFooterButtonCount());
        }
        clickToElement(AdminPageUI.CONFIRM_DIALOG_CONFIRM_BTN);
        String feedback = waitForFeedback();
        sleepInSecond(2); // cho loadReviews tai lai danh sach
        return feedback;
    }

    public void fillAdjustPointsForm(String emailOrId, String points, String reason) {
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_USER_INPUT, emailOrId);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_POINTS_INPUT, points);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_REASON_INPUT, reason);
    }

    public String findDisabledExpiredPromoCode() {
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
                java.util.List<String> cells = getTableRowCellTexts(row);
                if (cells.size() < 9) {
                    continue;
                }
                java.util.List<org.openqa.selenium.WebElement> switches =
                        row.findElements(org.openqa.selenium.By.cssSelector("button[role='switch']"));
                if (switches.isEmpty() || "true".equals(switches.get(0).getAttribute("aria-checked"))) {
                    continue;
                }
                if (!cells.get(7).contains("Đã hết hạn")) {
                    continue;
                }
                return cells.get(1); // o Ma
            }
            return null;
        }

    public String findDisabledNotExpiredPromoCode() {
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
                java.util.List<String> cells = getTableRowCellTexts(row);
                if (cells.size() < 9) {
                    continue;
                }
                java.util.List<org.openqa.selenium.WebElement> switches =
                        row.findElements(org.openqa.selenium.By.cssSelector("button[role='switch']"));
                if (switches.isEmpty() || "true".equals(switches.get(0).getAttribute("aria-checked"))) {
                    continue;
                }
                if (cells.get(7).contains("Đã hết hạn")) {
                    continue;
                }
                return cells.get(1); // o Ma
            }
            return null;
        }

    public String findDraftExpiredPromoCode() {
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
                java.util.List<String> cells = getTableRowCellTexts(row);
                if (cells.size() < 9) {
                    continue;
                }
                if (!cells.get(8).contains("Nháp")) {
                    continue;
                }
                if (!cells.get(7).contains("Đã hết hạn")) {
                    continue;
                }
                return cells.get(1); // o Ma
            }
            return null;
        }

    public String findDraftUsedPromoCode() {
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
                java.util.List<String> cells = getTableRowCellTexts(row);
                if (cells.size() < 9) {
                    continue;
                }
                if (!cells.get(8).contains("Nháp")) {
                    continue;
                }
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(\\d+)").matcher(cells.get(6));
                if (matcher.find() && Integer.parseInt(matcher.group(1)) > 0) {
                    return cells.get(1); // o Ma
                }
            }
            return null;
        }

    /** Tim ma voucher Expired tren trang hien tai (badge 'Hết hạn' o cot Trang
     *  thai). Dot 21: dong Expired CO Switch nhung bi DISABLE (aria-checked=false,
     *  khong bam duoc) — khac dot 18 (khong co Switch). */
    public String findExpiredPromoCode() {
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
                java.util.List<String> cells = getTableRowCellTexts(row);
                if (cells.size() < 9) {
                    continue;
                }
                if (!cells.get(8).contains("Hết hạn")) {
                    continue;
                }
                java.util.List<org.openqa.selenium.WebElement> switches =
                        row.findElements(org.openqa.selenium.By.cssSelector("button[role='switch']"));
                if (switches.isEmpty() || switches.get(0).isEnabled()) {
                    continue;
                }
                return cells.get(1); // o Ma
            }
            return null;
        }

    /** Switch trang thai tren dong co chua chuoi cho truoc co bam duoc khong
     *  (false neu dong khong co Switch, hoac Switch bi disable — vd voucher
     *  Expired). */
    public boolean isRowStatusSwitchEnabled(String rowText) {
        java.util.List<WebElement> switches = getListWebElements(AdminPageUI.DYNAMIC_ROW_STATUS_SWITCH, rowText);
        if (switches.isEmpty()) {
            return false;
        }
        return switches.get(0).isEnabled();
    }

    // (Dot 21: findFirstRatedProductName / findFirstUnratedProductName da bo
    // — cot 'Đánh giá' khong con o bang SP (chi doc rating o trang chi tiet
    // qua getProductRatingCellText); ADM19-105 viet lai theo huong moi.)

    public String findHiddenCategoryName() {
            int rows = getAdminTableRowCount();
            for (int i = 1; i <= rows; i++) {
                java.util.List<WebElement> switches = getListWebElements(getDynamicLocator(
                        AdminPageUI.DYNAMIC_TABLE_ROW_SWITCH, String.valueOf(i)));
                if (!switches.isEmpty() && "false".equals(switches.get(0).getAttribute("aria-checked"))) {
                    return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_TABLE_ROW_CELL_STRONG,
                            String.valueOf(i), "3"));
                }
            }
            return null;
        }

    public String findHiddenCategoryWithProductsName() {
            int rows = getAdminTableRowCount();
            for (int i = 1; i <= rows; i++) {
                java.util.List<WebElement> switches = getListWebElements(getDynamicLocator(
                        AdminPageUI.DYNAMIC_TABLE_ROW_SWITCH, String.valueOf(i)));
                if (switches.isEmpty() || "true".equals(switches.get(0).getAttribute("aria-checked"))) {
                    continue;
                }
                String countText = getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_TABLE_ROW_CELL,
                        String.valueOf(i), "6")).trim();
                try {
                    if (Integer.parseInt(countText) > 0) {
                        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_TABLE_ROW_CELL_STRONG,
                                String.valueOf(i), "3"));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            return null;
        }

    public String findReturnsRowWithoutRefundButton(String statusLabel, String refundStatus) {
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
                java.util.List<String> cells = getTableRowCellTexts(row);
                if (cells.size() < 7) {
                    continue;
                }
                boolean statusMatch = cells.get(5).contains(statusLabel);
                boolean refundMatch = cells.get(6).contains(refundStatus);
                boolean hasButton = row.findElements(org.openqa.selenium.By.cssSelector(
                        "[data-testid='admin-return-record-refund-btn']")).size() > 0;
                if (statusMatch && refundMatch && !hasButton) {
                    return cells.get(1); // o Ma don
                }
            }
            return null;
        }

    public String getAdjustErrorText() {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            if (isElementDisplayed(AdminPageUI.ADMIN_ADJUST_ERROR)) {
                return getElementText(AdminPageUI.ADMIN_ADJUST_ERROR);
            }
            sleepInSecond(1);
        }
        return "";
    }

    public String getAdjustPointsFormText() {
        waitForElementVisible(AdminPageUI.ADMIN_ADJUST_POINTS_FORM);
        return getElementText(AdminPageUI.ADMIN_ADJUST_POINTS_FORM);
    }

    public String getAdminPageText() {
            return getElementText("css=body");
        }

    public String getAuditDateFromValue() {
            return getElementValue(AdminPageUI.ADMIN_LOG_DATE_FROM);
        }

    public String getAuditDateToValue() {
            return getElementValue(AdminPageUI.ADMIN_LOG_DATE_TO);
        }

    public String getAuditRowActionBadgeClass(int rowIndex) {
            return getElementAttribute(String.format(
                    "(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[%d]/td[4]/span", rowIndex), "class");
        }

    public String getAuditRowActionText(int rowIndex) {
            return getElementText(String.format(
                    "(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[%d]/td[4]", rowIndex));
        }

    public String getAuditSearchPlaceholder() {
            return getElementAttribute(AdminPageUI.ADMIN_LOG_SEARCH_INPUT, "placeholder");
        }

    public String getAuditSearchValue() {
            return getElementValue(AdminPageUI.ADMIN_LOG_SEARCH_INPUT);
        }

    public String getCategoryDescriptionValue() {
            return getElementValue(AdminPageUI.ADMIN_CATEGORY_DESCRIPTION_INPUT);
        }

    public String getCategoryFormErrorText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_FORM_ERROR)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_CATEGORY_FORM_ERROR);
        }

    public String getCategoryFormModalText() {
            waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
            return getElementText(AdminPageUI.ADMIN_CATEGORY_FORM);
        }

    public String getCategoryImageDropzoneText() {
            return getElementText(AdminPageUI.ADMIN_CATEGORY_IMAGE_DROPZONE);
        }

    public String getCategoryImageErrorText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_ERROR)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_CATEGORY_IMAGE_ERROR);
        }

    public String getCategoryImageThumbErrorText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_THUMB_ERROR_TEXT)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_CATEGORY_IMAGE_THUMB_ERROR_TEXT);
        }

    public String getCategorySearchValue() {
            return getElementValue(AdminPageUI.ADMIN_CATEGORY_SEARCH);
        }

    public String getCategorySortOrderValue() {
            return getElementValue(AdminPageUI.ADMIN_CATEGORY_SORT_ORDER_INPUT);
        }

    public boolean getCategoryStatusToggleChecked() {
            return "true".equals(getElementAttribute(AdminPageUI.ADMIN_CATEGORY_STATUS_TOGGLE, "aria-checked"));
        }

    public String getConfirmDialogReasonValue() {
        return getElementValue(AdminPageUI.CONFIRM_DIALOG_REASON_INPUT);
    }

    public String getCustomerAddressInputValue() {
        return getElementValue(AdminPageUI.ADMIN_CUSTOMER_ADDRESS_INPUT);
    }

    public String getCustomerFieldErrorText(String field) {
        if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_CUSTOMER_FIELD_ERROR, field))) {
            return "";
        }
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_CUSTOMER_FIELD_ERROR, field));
    }

    public String getCustomerFormErrorText() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_CUSTOMER_FORM_ERROR)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_CUSTOMER_FORM_ERROR);
    }

    public String getCustomerIdByEmail(String email) {
        clickAdminNav("customers");
        searchCustomers(email);
        if (getAdminTableRowCount() == 0) {
            return null;
        }
        String testId = getElementAttribute(
                getDynamicLocator(AdminPageUI.DYNAMIC_CUSTOMER_POINTS_CELL, email), "data-testid");
        String prefix = "admin-customer-points-";
        if (testId == null || !testId.startsWith(prefix) || testId.length() <= prefix.length()) {
            return null;
        }
        return testId.substring(prefix.length());
    }

    public String getCustomerPhoneInputValue() {
        return getElementValue(AdminPageUI.ADMIN_CUSTOMER_PHONE_INPUT);
    }

    public String getCustomerSearchPlaceholder() {
        return getElementAttribute(AdminPageUI.ADMIN_CUSTOMER_SEARCH, "placeholder");
    }

    public String getCustomerSearchValue() {
        return getElementValue(AdminPageUI.ADMIN_CUSTOMER_SEARCH);
    }

    public String getCustomerTierFilterValue() {
            return getSelectedValue(AdminPageUI.ADMIN_CUSTOMER_TIER_FILTER);
        }

    public String getCustomerTierInRow(String email) {
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_CUSTOMER_TIER_CELL, email)).trim();
    }

    public String getDashboardActionCountText(String key) {
            if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_ACTION_COUNT, key))) {
                return null;
            }
            return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_ACTION_COUNT, key)).trim();
        }

    public String getDashboardActionNeededText() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_ACTION_NEEDED);
        }

    public String getDashboardCustomErrorText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_DASHBOARD_CUSTOM_ERROR)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_CUSTOM_ERROR);
        }

    public String getDashboardDonutAriaLabel() {
            return getElementAttribute(AdminPageUI.ADMIN_DASHBOARD_STATUS_DONUT, "aria-label");
        }

    public String getDashboardDonutLegendText(String key) {
            return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_DONUT_LEGEND, key));
        }

    public String getDashboardKpiCompareText(String kpiKey) {
            if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_KPI_COMPARE, kpiKey))) {
                return "";
            }
            return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_KPI_COMPARE, kpiKey));
        }

    public String getDashboardKpiSubText(String kpiKey) {
            if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_KPI_SUB, kpiKey))) {
                return "";
            }
            return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_KPI_SUB, kpiKey));
        }

    public String getDashboardKpiValueText(String kpiKey) {
            if (!isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_KPI_VALUE, kpiKey))) {
                return "";
            }
            return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_KPI_VALUE, kpiKey));
        }

    public String getDashboardLowStockCardText() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_LOW_STOCK_CARD);
        }

    public int getDashboardLowStockRowCount() {
            return getListWebElements(AdminPageUI.ADMIN_DASHBOARD_LOW_STOCK_ROWS).size();
        }

    public String getDashboardNewReviewsCountText() {
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            if (isElementDisplayed(AdminPageUI.ADMIN_DASHBOARD_NEW_REVIEWS_COUNT)) {
                return getElementText(AdminPageUI.ADMIN_DASHBOARD_NEW_REVIEWS_COUNT).trim();
            }
            sleepInSecond(1);
        }
        return null;
    }

    public String getDashboardPromoCardText() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_ACTIVE_PROMOS_CARD);
        }

    public int getDashboardPromoRowCount() {
            return getListWebElements(AdminPageUI.ADMIN_DASHBOARD_PROMO_ROWS).size();
        }

    public int getDashboardRecentOrderRowCount() {
            return getListWebElements(AdminPageUI.ADMIN_DASHBOARD_RECENT_ORDER_ROWS).size();
        }

    public String getDashboardRecentOrdersCardText() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_RECENT_ORDERS_CARD);
        }

    public String getDashboardTierMixCardText() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_TIER_MIX_CARD);
        }

    public String getDashboardTierRowText(String tier) {
            return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_TIER_ROW, tier));
        }

    public String getDashboardUpdatedAtText() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_UPDATED_AT);
        }

    public String getEmptyStateText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_EMPTY_STATE)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_EMPTY_STATE);
        }

    public String getFirstDashboardPromoCode() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_PROMO_FIRST_CODE);
        }

    public String getFirstDashboardRecentOrderCode() {
            return getElementText(AdminPageUI.ADMIN_DASHBOARD_RECENT_ORDER_FIRST_CODE);
        }

    public String getFirstPromoCode() {
            if (getAdminTableRowCount() == 0) {
                return null;
            }
            java.util.List<String> cells = getFirstTableRowCellTexts();
            return cells.size() > 1 ? cells.get(1) : null;
        }

    public int getFirstRowActionButtonCount() {
            return getListWebElements(AdminPageUI.ADMIN_FIRST_ROW_ACTION_BUTTONS).size();
        }

    public int getFirstTableRowActionButtonCount() {
            java.util.List<org.openqa.selenium.WebElement> rows = getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS);
            if (rows.isEmpty()) {
                return 0;
            }
            java.util.List<org.openqa.selenium.WebElement> cells =
                    rows.get(0).findElements(org.openqa.selenium.By.tagName("td"));
            if (cells.isEmpty()) {
                return 0;
            }
            return cells.get(cells.size() - 1).findElements(org.openqa.selenium.By.tagName("button")).size();
        }

    public String getOrderDetailErrorText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_ORDER_DETAIL_ERROR)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_ORDER_DETAIL_ERROR);
        }

    public String getOrderSearchPlaceholder() {
            return getElementAttribute(AdminPageUI.ADMIN_ORDER_SEARCH, "placeholder");
        }

    public String getOrderSearchValue() {
            return getElementValue(AdminPageUI.ADMIN_ORDER_SEARCH);
        }

    public String getOrderStatusFilterValue() {
            return getSelectedValue(AdminPageUI.ADMIN_ORDER_STATUS_FILTER);
        }

    public String getProductCategoryFilterSelectedText() {
            org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(
                    getWebElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_FILTER));
            return select.getFirstSelectedOption().getText().trim();
        }

    public String getProductCategoryTriggerText() {
            return getElementText(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT);
        }

    public String getProductDescriptionInputValue() {
            return getElementValue(AdminPageUI.ADMIN_PRODUCT_DESCRIPTION_INPUT);
        }

    public String getProductFormErrorText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_FORM_ERROR)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_PRODUCT_FORM_ERROR);
        }

    public String getProductImageDropzoneText() {
            return getElementText(AdminPageUI.ADMIN_PRODUCT_IMAGE_DROPZONE);
        }

    public int getProductImageReadyThumbCount() {
            int count = 0;
            for (int i = 0; i < 8; i++) {
                String locator = getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_THUMB, String.valueOf(i));
                if (!isElementDisplayed(locator)) {
                    break;
                }
                String cls = getElementAttribute(locator, "class");
                if (cls == null || !cls.contains("sa-thumb-error")) {
                    count++;
                }
            }
            return count;
        }

    public String getProductImageThumbErrorText(int index) {
            String locator = getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_THUMB_ERROR_TEXT, String.valueOf(index));
            if (!isElementDisplayed(locator)) {
                return "";
            }
            return getElementText(locator);
        }

    public int getProductMoneySuffixCount() {
            return getListWebElements(AdminPageUI.ADMIN_PRODUCT_MONEY_SUFFIX).size();
        }

    public String getProductOriginalPriceInputValue() {
            return getElementValue(AdminPageUI.ADMIN_PRODUCT_ORIGINAL_PRICE_INPUT);
        }

    public String getProductPreviewText() {
            waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_PREVIEW);
            return getElementText(AdminPageUI.ADMIN_PRODUCT_PREVIEW);
        }

    public String getProductPriceInputValue() {
            return getElementValue(AdminPageUI.ADMIN_PRODUCT_PRICE_INPUT);
        }

    /** Doc rating cua 1 SP — dot 21: cot 'Đánh giá' DA BO khoi bang SP nen
     *  doc o TRANG CHI TIET (bam mat -> doc hang 'Đánh giá' o trang chi tiet
     *  '{rating} ({n} đánh giá)' hoac '—' -> [Quay lại] ve danh sach).
     *  Giu nguyen signature de AdminReviewTest.readProductRating chay tiep
     *  (regex cua no khop dung dang text nay). */
    public String getProductRatingCellText(String productName) {
        clickToElement(getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_VIEW_BTN, productName));
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_DETAIL);
        String rating = getProductDetailRatingText();
        closeProductDetailPage();
        return rating == null ? "" : rating.trim();
    }

    public boolean getProductStatusToggleChecked() {
            return "true".equals(getElementAttribute(AdminPageUI.ADMIN_PRODUCT_STATUS_TOGGLE, "aria-checked"));
        }

    public String getProductStockInputValue() {
            return getElementValue(AdminPageUI.ADMIN_PRODUCT_STOCK_INPUT);
        }

    public String getProfilePageMaxWidth() {
            Object value = executeScript(
                    "return window.getComputedStyle(document.querySelector(\"[data-testid='admin-profile-page']\")).maxWidth;");
            return value == null ? "" : String.valueOf(value);
        }

    // ---------- Dot 21: menu tai khoan topbar gon + grid Ho so 2 cot ----------

    /** So muc trong menu tai khoan topbar — dot 21 quy dinh CHI DUNG 2 muc. */
    public int getUserMenuItemCount() {
        return getListWebElements(AdminPageUI.ADMIN_USER_MENU_ITEMS).size();
    }

    /** Divider truoc muc "Dang xuat" trong menu topbar co dang hien khong. */
    public boolean isUserMenuSeparatorDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_USER_MENU_SEPARATOR);
    }

    /** Mau chu thuc te cua muc "Dang xuat" trong menu topbar (vd "rgba(220, 38, 38, 1)"). */
    public String getUserMenuLogoutColor() {
        return getWebElement(AdminPageUI.ADMIN_USER_MENU_LOGOUT).getCssValue("color");
    }

    /** So cot thuc te cua grid Thong tin ca nhan o trang thai xem. */
    public int getProfileInfoGridColumnCount() {
        return getGridColumnCountByTestId("admin-profile-info-grid");
    }

    /** So cot thuc te cua grid Thong tin ca nhan o form sua. */
    public int getProfileFormGridColumnCount() {
        return getGridColumnCountByTestId("admin-profile-form-grid");
    }

    private int getGridColumnCountByTestId(String testId) {
        Object value = executeScript(
                "return window.getComputedStyle(document.querySelector(\"[data-testid='"
                        + testId + "']\")).gridTemplateColumns;");
        if (value == null) {
            return 0;
        }
        String cols = String.valueOf(value).trim();
        if (cols.isEmpty() || "none".equals(cols)) {
            return 0;
        }
        return cols.split("\\s+").length;
    }

    public String getProfilePhoneInputValue() {
            return getElementValue(AdminPageUI.ADMIN_PROFILE_PHONE_INPUT);
        }

    public String getPromoDescriptionInputValue() {
            return getElementValue(AdminPageUI.ADMIN_PROMO_DESCRIPTION_INPUT);
        }

    public String getPromoExpiryInputType() {
            return getElementAttribute(AdminPageUI.ADMIN_PROMO_EXPIRY_INPUT, "type");
        }

    /** Text trong form tao/sua khuyen mai (nhan truong + loi inline + ghi chu).
     *  Tieu de trang ('Thêm mới / Chỉnh sửa khuyến mãi') o header ngoai form
     *  nen dung getPromoCreatePageText() / getPromoEditPageText() rieng. */
    public String getPromoFormText() {
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
            return getElementText(AdminPageUI.ADMIN_PROMO_FORM);
        }

    /** Text cua trang tao khuyen mai (gom breadcrumb + tieu de + form). */
    public String getPromoCreatePageText() {
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_CREATE_PAGE);
            return getElementText(AdminPageUI.ADMIN_PROMO_CREATE_PAGE);
        }

    /** Text cua trang sua khuyen mai (gom breadcrumb + tieu de + form). */
    public String getPromoEditPageText() {
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_EDIT_PAGE);
            return getElementText(AdminPageUI.ADMIN_PROMO_EDIT_PAGE);
        }

    /** Text cua trang chi tiet khuyen mai (gom 4 card). */
    public String getPromoDetailPageText() {
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_DETAIL);
            return getElementText(AdminPageUI.ADMIN_PROMO_DETAIL);
        }

    /** Text cua trang lich su su dung khuyen mai (khoi tong quan + bang). */
    public String getPromoUsagesPageText() {
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_USAGES_PAGE);
            return getElementText(AdminPageUI.ADMIN_PROMO_USAGES_PAGE);
        }

    public String getPromoSearchPlaceholder() {
            return getElementAttribute(AdminPageUI.ADMIN_PROMO_SEARCH, "placeholder");
        }

    public String getPromoSearchValue() {
            return getElementValue(AdminPageUI.ADMIN_PROMO_SEARCH);
        }

    public String getPromoStartInputType() {
            return getElementAttribute(AdminPageUI.ADMIN_PROMO_START_INPUT, "type");
        }

    public String getPromoUsageSectionText() {
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_USAGES_PAGE);
            return getElementText(AdminPageUI.ADMIN_PROMO_USAGES_PAGE);
        }

    public String getRefundRecordErrorText() {
            if (!isElementDisplayed(AdminPageUI.ADMIN_REFUND_RECORD_ERROR)) {
                return "";
            }
            return getElementText(AdminPageUI.ADMIN_REFUND_RECORD_ERROR);
        }

    public String getReturnsSearchPlaceholder() {
            return getElementAttribute(AdminPageUI.ADMIN_RETURNS_SEARCH, "placeholder");
        }

    public String getReturnsSearchValue() {
            return getElementValue(AdminPageUI.ADMIN_RETURNS_SEARCH);
        }

    public String getReturnsStatusFilterValue() {
            return getElementValue(AdminPageUI.ADMIN_RETURNS_STATUS_FILTER);
        }

    public String getReviewRatingFilterValue() {
        return getElementValue(AdminPageUI.ADMIN_REVIEW_RATING_FILTER);
    }

    public String getReviewRowComment(int rowIndex) {
        return getElementText(AdminPageUI.DYNAMIC_REVIEW_ROW_COMMENT, String.valueOf(rowIndex));
    }

    public String getReviewRowCustomerName(int rowIndex) {
        return getElementText(AdminPageUI.DYNAMIC_REVIEW_ROW_CUSTOMER_NAME, String.valueOf(rowIndex));
    }

    public String getReviewRowProductName(int rowIndex) {
        return getElementText(AdminPageUI.DYNAMIC_REVIEW_ROW_PRODUCT_NAME, String.valueOf(rowIndex));
    }

    public String getReviewSearchPlaceholder() {
        return getElementAttribute(AdminPageUI.ADMIN_REVIEW_SEARCH, "placeholder");
    }

    public String getReviewSearchValue() {
        return getElementValue(AdminPageUI.ADMIN_REVIEW_SEARCH);
    }

    public String getReviewsEmptyStateText() {
        waitForElementVisible(AdminPageUI.ADMIN_EMPTY_STATE);
        return getElementText(AdminPageUI.ADMIN_EMPTY_STATE);
    }

    public String getTierFreeShipPlaceholder(String tierKey) {
        return getElementAttribute(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_FREESHIP_INPUT, tierKey), "placeholder");
    }

    public String getTierFreeShipValue(String tierKey) {
        return getElementValue(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_FREESHIP_INPUT, tierKey));
    }

    public String getTierMaxPointsPercentValue(String tierKey) {
        return getElementValue(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_MAX_POINTS_INPUT, tierKey));
    }

    public String getTierSpendPerPointValue(String tierKey) {
        return getElementValue(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_SPEND_PER_POINT_INPUT, tierKey));
    }

    public String getTierThresholdValue(String tierKey) {
        return getElementValue(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_THRESHOLD_INPUT, tierKey));
    }

    public String getTiersErrorText() {
        if (!isElementDisplayed(AdminPageUI.ADMIN_TIERS_ERROR)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_TIERS_ERROR);
    }

    public String getTiersNoteText() {
        return getElementText(AdminPageUI.ADMIN_TIERS_NOTE);
    }

    public boolean hasRefundRecordButtonInRow(String orderCode) {
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS)) {
                if (row.getText().contains(orderCode)) {
                    return row.findElements(org.openqa.selenium.By.cssSelector(
                            "[data-testid='admin-return-record-refund-btn']")).size() > 0;
                }
            }
            return false;
        }

    public boolean isAdjustPointsFormDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_ADJUST_POINTS_FORM, 10);
    }

    public boolean isAdminPageheadDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PAGEHEAD);
        }

    public boolean isAnySearchInputPresent() {
        return getListWebElements(AdminPageUI.ADMIN_ANY_SEARCH_INPUT).size() > 0;
    }

    public boolean isAuditSearchInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_LOG_SEARCH_INPUT);
        }

    public boolean isCategoryImageDropzoneDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_DROPZONE);
        }

    public boolean isCategoryImageErrorDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_ERROR);
        }

    public boolean isCategoryImageLinkInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_URL_INPUT);
        }

    public boolean isCategoryImageRetryButtonDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_RETRY_BTN);
        }

    public boolean isConfirmDialogPresent() {
            return isElementDisplayed(AdminPageUI.CONFIRM_DIALOG);
        }

    public boolean isCustomerRowAvatarDisplayed(String email) {
        return getListWebElements(AdminPageUI.DYNAMIC_CUSTOMER_ROW_AVATAR, email).size() > 0;
    }

    public boolean isCustomerRowDeleteButtonDisplayed(String fullName) {
        return getListWebElements(AdminPageUI.DYNAMIC_CUSTOMER_DELETE_BTN, fullName).size() > 0;
    }

    public boolean isCustomerRowEditButtonDisplayed(String fullName) {
        return getListWebElements(AdminPageUI.DYNAMIC_CUSTOMER_EDIT_BTN, fullName).size() > 0;
    }

    public boolean isCustomerSaveButtonEnabled() {
        return isElementEnabled(AdminPageUI.ADMIN_CUSTOMER_SAVE_BTN);
    }

    public boolean isCustomerTierFilterDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_CUSTOMER_TIER_FILTER);
    }

    public boolean isDashboardActionViewDisplayed(String key) {
            return isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_ACTION_VIEW, key));
        }

    public boolean isDashboardActionViewEnabled(String key) {
            return isElementEnabled(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_ACTION_VIEW, key));
        }

    public boolean isDashboardCustomPickerDisplayed() {
            return isElementDisplayedWithWait(AdminPageUI.ADMIN_DASHBOARD_CUSTOM_PICKER, 5);
        }

    public boolean isDashboardDonutDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_DASHBOARD_STATUS_DONUT);
        }

    public boolean isDashboardKpiError(String kpiKey) {
            return isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_KPI_ERROR, kpiKey));
        }

    public boolean isDashboardRangeActive(String rangeKey) {
            String locator = getDynamicLocator(AdminPageUI.DYNAMIC_DASHBOARD_RANGE_BTN, rangeKey);
            if (!isElementDisplayed(locator)) {
                return false;
            }
            String cssClass = getElementAttribute(locator, "class");
            return cssClass != null && cssClass.contains("db-segmented-btn--active");
        }

    public boolean isDashboardRefreshButtonDisplayed() {
            return isElementDisplayedWithWait(AdminPageUI.ADMIN_DASHBOARD_REFRESH_BTN, 10);
        }

    public boolean isDashboardTierMixEmpty() {
            return isElementDisplayed(AdminPageUI.ADMIN_DASHBOARD_TIER_MIX_EMPTY);
        }

    public boolean isMembersRefreshButtonDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_MEMBERS_REFRESH_BTN);
    }

    public boolean isMembersResetButtonDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_MEMBERS_RESET_BTN);
    }

    public boolean isMembershipPaginationPresent() {
        return getListWebElements(AdminPageUI.DYNAMIC_ADMIN_PAGINATION, "membership").size() > 0;
    }

    public boolean isOrderCancelButtonDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_ORDER_CANCEL_BTN);
        }

    public boolean isOrderCarrierInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_ORDER_CARRIER_INPUT);
        }

    public boolean isOrderChangeStatusButtonDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_ORDER_CHANGE_STATUS_BTN);
        }

    public boolean isOrderChangeStatusButtonEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_ORDER_CHANGE_STATUS_BTN);
        }

    public boolean isOrderStatusSelectDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_ORDER_STATUS_SELECT);
        }

    public boolean isOrderTrackingInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_ORDER_TRACKING_INPUT);
        }

    public boolean isPageToolbarDisplayed() {
        return getListWebElements(AdminPageUI.ADMIN_PAGE_TOOLBAR).size() > 0;
    }

    public boolean isProductCategorySearchDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SEARCH);
        }

    public boolean isProductCodeInputFocused() {
            WebElement active = getDriver().switchTo().activeElement();
            WebElement codeInput = getWebElement(AdminPageUI.ADMIN_PRODUCT_CODE_INPUT);
            return active != null && active.equals(codeInput);
        }

    public boolean isProductImageRetryButtonDisplayed(int index) {
            return isElementDisplayed(getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_IMAGE_RETRY_BTN, String.valueOf(index)));
        }

    // (Dot 21: isProductRatingStarDisplayed da bo — cot 'Đánh giá' khong con
    // o bang SP; icon sao chi con o trang chi tiet qua ADMIN_PRODUCT_DETAIL_RATING.)

    // (Dot 21: isPromoActivateButtonDisplayedFor / isPromoActivateNowChecked da
    // bo — khong con nut Play 'Kích hoạt' rieng, khong con toggle 'Kích hoạt
    // ngay sau khi tạo'; bat/tat qua Switch tren dong + modal statusChange.)

    public boolean isPromoCodeInputDisplayed() {
            return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_CODE_INPUT, 10);
        }

    public boolean isPromoDescriptionInputEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_PROMO_DESCRIPTION_INPUT);
        }

    public boolean isPromoDiscountTypeButtonDisabled(String type) {
            if ("Fixed".equalsIgnoreCase(type)) {
                return !isElementEnabled(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FIXED);
            } else if ("FreeShip".equalsIgnoreCase(type)) {
                return !isElementEnabled(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FREESHIP);
            }
            return !isElementEnabled(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_PERCENT);
        }

    public boolean isPromoDiscountTypeButtonDisplayed(String type) {
            if ("Fixed".equalsIgnoreCase(type)) {
                return isElementDisplayed(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FIXED);
            } else if ("FreeShip".equalsIgnoreCase(type)) {
                return isElementDisplayed(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_FREESHIP);
            }
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_DISCOUNT_TYPE_PERCENT);
        }

    public boolean isPromoDiscountValueInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_VALUE_INPUT);
        }

    public boolean isPromoDiscountValueInputEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_PROMO_VALUE_INPUT);
        }

    public boolean isPromoExpiryInputEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_PROMO_EXPIRY_INPUT);
        }

    public boolean isPromoFormImageUploadPresent() {
            return getListWebElements(AdminPageUI.ADMIN_PROMO_IMAGE_UPLOAD_CHECK).size() > 0;
        }

    public boolean isPromoGenerateCodeButtonDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_GENERATE_CODE_BTN);
        }

    public boolean isPromoMaxDiscountInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_MAX_DISCOUNT_INPUT);
        }

    public boolean isPromoMaxDiscountInputEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_PROMO_MAX_DISCOUNT_INPUT);
        }

    public boolean isPromoMaxUsesInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_MAX_USES_INPUT);
        }

    public boolean isPromoMaxUsesInputEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_PROMO_MAX_USES_INPUT);
        }

    public boolean isPromoMaxUsesPerUserInputDisabled() {
            return !isElementEnabled(AdminPageUI.ADMIN_PROMO_MAX_USES_PER_USER_INPUT);
        }

    public boolean isPromoMaxUsesPerUserInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_MAX_USES_PER_USER_INPUT);
        }

    public boolean isPromoMinOrderInputDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_MIN_ORDER_INPUT);
        }

    public boolean isPromoMinOrderInputEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_PROMO_MIN_ORDER_INPUT);
        }

    public boolean isPromoMinTierSelectDisabled() {
            return !isElementEnabled(AdminPageUI.ADMIN_PROMO_MIN_TIER_SELECT);
        }

    public boolean isPromoMinTierSelectDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_MIN_TIER_SELECT);
        }

    public boolean isPromoSaveButtonEnabled() {
            return isElementEnabled(AdminPageUI.ADMIN_PROMO_SAVE_BTN);
        }

    public boolean isPromoScopeSelectDisabled() {
            return !isElementEnabled(AdminPageUI.ADMIN_PROMO_SCOPE_SELECT);
        }

    public boolean isPromoScopeSelectDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_PROMO_SCOPE_SELECT);
        }

    public boolean isPromoStartInputDisabled() {
            return !isElementEnabled(AdminPageUI.ADMIN_PROMO_START_INPUT);
        }

    public boolean isReturnsNotFoundMessageDisplayed() {
            return isElementDisplayed(AdminPageUI.ADMIN_RETURNS_NOT_FOUND_MSG);
        }

    /** Trang chi tiet danh gia full page co dang hien khong (kiem tra ngay,
     *  khong cho doi). Ten phuong thuc giu 'Modal' de call site khong doi. */
    public boolean isReviewDetailModalPresent() {
        return getListWebElements(AdminPageUI.ADMIN_REVIEW_DETAIL_PAGE).size() > 0;
    }

    public boolean isReviewRatingFilterDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_REVIEW_RATING_FILTER);
    }

    public boolean isReviewRowProductNameBold(int rowIndex) {
        return getListWebElements(AdminPageUI.DYNAMIC_REVIEW_ROW_PRODUCT_BOLD, String.valueOf(rowIndex)).size() > 0;
    }

    public boolean isReviewsPaginationPresent() {
        return getListWebElements(AdminPageUI.ADMIN_REVIEWS_PAGINATION).size() > 0;
    }

    public boolean isReviewsSearchEmptyMessageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_EMPTY_REVIEWS_SEARCH_MSG, 10);
    }

    public boolean isSaveTiersButtonDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_SAVE_TIERS_BTN);
    }

    public boolean isTierDetailButtonDisplayed(String tierLabel) {
        return getListWebElements(AdminPageUI.DYNAMIC_TIER_DETAIL_BTN, tierLabel).size() > 0;
    }

    public boolean isToastPresent() {
        return getListWebElements(AdminPageUI.ADMIN_TOAST).size() > 0;
    }

    public boolean isToolbarAddButtonPresent(String sectionKey) {
        return getListWebElements(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_ADD_BTN, sectionKey).size() > 0;
    }

    public void openFirstOrderDetailByRowClick() {
            java.util.List<org.openqa.selenium.WebElement> rows = getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS);
            if (rows.isEmpty()) {
                throw new AssertionError("Khong co dong don hang nao de bam vao");
            }
            java.util.List<org.openqa.selenium.WebElement> cells =
                    rows.get(0).findElements(org.openqa.selenium.By.tagName("td"));
            cells.get(0).click();
            waitForElementVisible(AdminPageUI.ADMIN_ORDER_DETAIL);
        }

    /** Dot 21 — Bam vao dong don dau tien (khong bam nut mat): mo trang chi
     *  tiet don dang route /orders/:code (KHONG con Drawer modal). Chi bam +
     *  cho navigate; test tu khang dinh URL — noi dung trang chi tiet do
     *  worker chi tiet lo. */
    public void clickFirstOrderRow() {
            java.util.List<org.openqa.selenium.WebElement> rows = getListWebElements(AdminPageUI.ADMIN_TABLE_ROWS);
            if (rows.isEmpty()) {
                throw new AssertionError("Khong co dong don hang nao de bam vao");
            }
            rows.get(0).findElements(org.openqa.selenium.By.tagName("td")).get(0).click();
            sleepInSecond(2); // cho navigate sang /orders/:code
        }

    public void openProductCategoryPicker() {
            clickToElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT);
            sleepInSecond(1); // cho dropdown custom render
        }

    public void openPromoDeleteDialog(String code) {
            clickToElement(AdminPageUI.DYNAMIC_PROMO_DELETE_BTN, code);
            waitForElementVisible(AdminPageUI.CONFIRM_DIALOG);
        }

    /** Mo trang sua khuyen mai cua voucher Draft (nut Sua o dong) — dot 21:
     *  trang full page /promotions/:code/edit, form mode='edit'. */
    public void openPromoEditForm(String code) {
            clickToElement(AdminPageUI.DYNAMIC_PROMO_EDIT_BTN, code);
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_EDIT_PAGE);
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
        }

    /** Dot 21 — Enter trong o tim don KHONG con tac dung gi (khong co onSearch):
     *  loc da ap dung ngay khi go (debounce 120ms). Giu ham de test cu goi
     *  khong vo; chi cho debounce xong. */
    public void pressEnterInOrderSearch() {
            getWebElement(AdminPageUI.ADMIN_ORDER_SEARCH).sendKeys(org.openqa.selenium.Keys.ENTER);
            sleepInSecond(2); // cho debounce 120ms + loc client-side ap dung
        }

    public void pressEscapeInOrderDetail() {
            getWebElement(AdminPageUI.ADMIN_ORDER_DETAIL).sendKeys(org.openqa.selenium.Keys.ESCAPE);
            sleepInSecond(1); // cho panel dang mo dong (trang chi tiet khong dong)
        }

    public void pressEscapeKey() {
            getDriver().switchTo().activeElement().sendKeys(org.openqa.selenium.Keys.ESCAPE);
            sleepInSecond(1); // cho FormShell xu ly ESC / hop xac nhan hien ra (neu co)
        }

    public void saveCustomerForm() {
        clickCustomerSaveButton();
        waitForFeedback();
        waitForElementInvisible(AdminPageUI.ADMIN_CUSTOMER_FORM);
    }

    public void selectCustomerTierFilterByValue(String value) {
        selectByValue(AdminPageUI.ADMIN_CUSTOMER_TIER_FILTER, value);
        sleepInSecond(1); // cho state loc cap nhat
    }

    public void selectOrderTargetStatus(String visibleStatusLabel) {
            openOrderStatusPanel();
            selectByVisibleText(AdminPageUI.ADMIN_ORDER_STATUS_SELECT, visibleStatusLabel);
            sleepInSecond(1); // cho o van chuyen / nut doi trang thai cap nhat theo dich
        }

    public void selectProductCategoryFilterAll() {
            selectByValue(AdminPageUI.ADMIN_PRODUCT_CATEGORY_FILTER, "");
            sleepInSecond(2); // cho loc client-side ap dung
        }

    public void selectProductCategoryFilterByName(String categoryName) {
            selectByVisibleText(AdminPageUI.ADMIN_PRODUCT_CATEGORY_FILTER, categoryName);
            sleepInSecond(2); // cho loc client-side ap dung
        }

    public void selectProductCategoryOption(String categoryName) {
            clickToElement(AdminPageUI.DYNAMIC_PRODUCT_CATEGORY_OPTION, categoryName);
            sleepInSecond(1); // cho dropdown dong + gia tri duoc chon
        }

    public void selectPromoScope(String value) {
            selectByValue(AdminPageUI.ADMIN_PROMO_SCOPE_SELECT, value);
            sleepInSecond(1); // cho o Danh muc ap dung / ID san pham hien ra theo pham vi
        }

    public void selectReturnsStatusFilter(String value) {
            selectByValue(AdminPageUI.ADMIN_RETURNS_STATUS_FILTER, value);
            sleepInSecond(1);
        }

    public void setAuditSearchKeyword(String keyword) {
            sendKeyToElement(AdminPageUI.ADMIN_LOG_SEARCH_INPUT, keyword);
        }

    public void setCategoryDescription(String description) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_DESCRIPTION_INPUT, description);
        }

    public void setCategorySlug(String slug) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SLUG_INPUT, slug);
        }

    public void setCategorySortOrder(String sortOrder) {
            sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SORT_ORDER_INPUT, sortOrder);
        }

    public void setCustomerAddress(String address) {
        sendKeyToElement(AdminPageUI.ADMIN_CUSTOMER_ADDRESS_INPUT, address);
    }

    public void setCustomerName(String fullName) {
        sendKeyToElement(AdminPageUI.ADMIN_CUSTOMER_NAME_INPUT, fullName);
    }

    public void setCustomerPhone(String phone) {
        sendKeyToElement(AdminPageUI.ADMIN_CUSTOMER_PHONE_INPUT, phone);
    }

    public void setDashboardCustomRange(String dateFrom, String dateTo) {
            sendKeyToElement(AdminPageUI.ADMIN_DASHBOARD_CUSTOM_FROM, dateFrom);
            sendKeyToElement(AdminPageUI.ADMIN_DASHBOARD_CUSTOM_TO, dateTo);
        }

    public void setInputValueByJs(String locator, String value) {
            WebElement element = getWebElement(locator);
            executeScript(
                    "const el = arguments[0]; const val = arguments[1];"
                    + " const proto = el.tagName === 'TEXTAREA' ? window.HTMLTextAreaElement.prototype : window.HTMLInputElement.prototype;"
                    + " Object.getOwnPropertyDescriptor(proto, 'value').set.call(el, val);"
                    + " el.dispatchEvent(new Event('input', { bubbles: true }));",
                    element, value);
        }

    public void setOrderCarrier(String carrier) {
            sendKeyToElement(AdminPageUI.ADMIN_ORDER_CARRIER_INPUT, carrier);
        }

    public void setOrderSearch(String keyword) {
            sendKeyToElement(AdminPageUI.ADMIN_ORDER_SEARCH, keyword);
        }

    public void setOrderStatusNote(String note) {
            sendKeyToElement(AdminPageUI.ADMIN_ORDER_NOTE_INPUT, note);
        }

    public void setOrderTrackingCode(String trackingCode) {
            sendKeyToElement(AdminPageUI.ADMIN_ORDER_TRACKING_INPUT, trackingCode);
        }

    public void setProductCategorySearch(String keyword) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SEARCH, keyword);
            sleepInSecond(1); // cho loc option theo tu khoa
        }

    public void setProductDescription(String description) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_DESCRIPTION_INPUT, description);
        }

    public void setProductOriginalPrice(String originalPrice) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_ORIGINAL_PRICE_INPUT, originalPrice);
        }

    public void setProductPrice(String price) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_PRICE_INPUT, price);
        }

    public void setProductStock(String stock) {
            sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_STOCK_INPUT, stock);
        }

    public void setPromoCode(String code) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_CODE_INPUT, code);
        }

    public void setPromoDescription(String description) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_DESCRIPTION_INPUT, description);
        }

    public void setPromoDiscountValue(String discountValue) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_VALUE_INPUT, discountValue);
        }

    public void setPromoSearch(String keyword) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_SEARCH, keyword);
            sleepInSecond(1); // cho loc client-side ap dung
        }

    public void setRefundTransactionCode(String transactionCode) {
            sendKeyToElement(AdminPageUI.ADMIN_REFUND_TRANSACTION_INPUT, transactionCode);
        }

    public void setReturnsSearch(String keyword) {
            sendKeyToElement(AdminPageUI.ADMIN_RETURNS_SEARCH, keyword);
            sleepInSecond(1); // cho loc client-side ap dung
        }

    public void setTierFreeShip(String tierKey, String value) {
        sendKeyToElement(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_FREESHIP_INPUT, tierKey), value);
    }

    public void setTierMaxPointsPercent(String tierKey, String value) {
        sendKeyToElement(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_MAX_POINTS_INPUT, tierKey), value);
    }

    public void setTierSpendPerPoint(String tierKey, String value) {
        sendKeyToElement(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_SPEND_PER_POINT_INPUT, tierKey), value);
    }

    public void setTierThreshold(String tierKey, String value) {
        sendKeyToElement(getDynamicLocator(AdminPageUI.DYNAMIC_TIER_THRESHOLD_INPUT, tierKey), value);
    }

    // ----- Dot 19 (bo sung): method bi sot do regex trich lan 1 -----

    public java.util.List<String> getAdminTableHeaderTexts() {
            java.util.List<String> headers = new java.util.ArrayList<>();
            for (WebElement th : getListWebElements(AdminPageUI.ADMIN_TABLE_HEADERS)) {
                headers.add(th.getText().trim());
            }
            return headers;
        }

    public java.util.List<String> getAdminTableHeaders() {
            java.util.List<String> headers = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement th : getListWebElements(AdminPageUI.ADMIN_TABLE_HEADERS)) {
                headers.add(th.getText().trim());
            }
            return headers;
        }

    public java.util.List<String> getAllReviewProductNames() {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement cell : getListWebElements(AdminPageUI.ADMIN_REVIEW_PRODUCT_CELLS)) {
            names.add(cell.getText().trim());
        }
        return names;
    }

    public java.util.List<String> getAllToastTexts() {
            java.util.List<String> texts = new java.util.ArrayList<>();
            for (WebElement toast : getListWebElements(AdminPageUI.ADMIN_TOAST)) {
                texts.add(toast.getText());
            }
            return texts;
        }

    public java.util.List<String> getAuditTableHeaders() {
            java.util.List<String> headers = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement th : getListWebElements(
                    "xpath=//div[contains(@class,'sa-table-wrap')]//thead/tr/th")) {
                headers.add(th.getText().trim());
            }
            return headers;
        }

    public java.util.List<String> getCustomerHeaders() {
        java.util.List<String> headers = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement th : getListWebElements(AdminPageUI.ADMIN_GENERIC_TABLE_HEADERS)) {
            headers.add(th.getText().trim());
        }
        return headers;
    }

    public java.util.List<String> getCustomerStatusFilterOptions() {
        java.util.List<String> options = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement option : getListWebElements(AdminPageUI.ADMIN_STATUS_FILTER_OPTIONS)) {
            options.add(option.getText().trim());
        }
        return options;
    }

    public java.util.List<String> getCustomerTierFilterOptions() {
        java.util.List<String> options = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement option : getListWebElements(AdminPageUI.ADMIN_CUSTOMER_TIER_FILTER_OPTIONS)) {
            options.add(option.getText().trim());
        }
        return options;
    }

    public java.util.List<String> getDashboardLowStockRowTexts() {
            java.util.List<String> texts = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_DASHBOARD_LOW_STOCK_ROWS)) {
                texts.add(row.getText());
            }
            return texts;
        }

    public java.util.List<String> getDashboardPromoRowTexts() {
            java.util.List<String> texts = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement row : getListWebElements(AdminPageUI.ADMIN_DASHBOARD_PROMO_ROWS)) {
                texts.add(row.getText());
            }
            return texts;
        }

    public java.util.List<String> getFirstTableRowCellTexts() {
            java.util.List<String> cells = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement cell : getListWebElements(AdminPageUI.ADMIN_TABLE_FIRST_ROW_CELLS)) {
                cells.add(cell.getText().trim());
            }
            return cells;
        }

    public java.util.List<String> getOrderStatusFilterOptionTexts() {
            java.util.List<String> options = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement option : getListWebElements(AdminPageUI.ADMIN_ORDER_STATUS_FILTER_OPTIONS)) {
                options.add(option.getText().trim());
            }
            return options;
        }

    // ---------- Don hang dot 21: nut xoa nhanh, so ket qua, nut Dat lai chu ----------

    /** Nut "×" xoa nhanh trong o tim don (chi hien khi o tim co chu). */
    public boolean isOrderSearchClearButtonDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_ORDER_SEARCH_CLEAR_BTN);
    }

    /** Bam nut "×" xoa nhanh: o tim trong, bang ve du don (cho debounce 120ms). */
    public void clickOrderSearchClearButton() {
        clickToElement(AdminPageUI.ADMIN_ORDER_SEARCH_CLEAR_BTN);
        sleepInSecond(1); // cho debounce + loc client-side ap dung
    }

    /** Text so ket qua "N / M don" (loc duoc / da tai); "" neu khoi khong hien
     *  (dang tai hoac loi tai). Cho toi da 10s de don tai xong roi moi doc. */
    public String getOrderResultCountText() {
        if (!isElementDisplayedWithWait(AdminPageUI.ADMIN_ORDER_RESULT_COUNT, 10)) {
            return "";
        }
        return getElementText(AdminPageUI.ADMIN_ORDER_RESULT_COUNT);
    }

    /** Nut chu "Đặt lại bộ lọc" o toolbar Don hang co bam duoc khong
     *  (disable khi chua co tu khoa va chua chon trang thai). */
    public boolean isOrderResetButtonEnabled() {
        return isElementEnabled(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_RESET_BTN, "orders"));
    }

    /** Nhan chu cua nut Dat lai o toolbar Don hang (dot 21: "Đặt lại bộ lọc"). */
    public String getOrderResetButtonText() {
        return getElementText(getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_RESET_BTN, "orders"));
    }

    /** Nut "Xóa bộ lọc" trong EmptyState khi tim/loc khong co ket qua. */
    public boolean isOrderClearFiltersButtonDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_ORDER_CLEAR_FILTERS_BTN);
    }

    /** Bam nut "Xóa bộ lọc" trong EmptyState: xoa ca tu khoa + trang thai. */
    public void clickOrderClearFiltersButton() {
        clickToElement(AdminPageUI.ADMIN_ORDER_CLEAR_FILTERS_BTN);
        sleepInSecond(1); // cho loc client-side ap dung
    }

    public java.util.List<String> getOrderTargetStatusLabels() {
            java.util.List<String> labels = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement option : getListWebElements(AdminPageUI.ADMIN_ORDER_TARGET_STATUS_OPTIONS)) {
                labels.add(option.getText().trim());
            }
            return labels;
        }

    public java.util.List<String> getProductCategoryFilterOptionTexts() {
            java.util.List<String> options = new java.util.ArrayList<>();
            org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(
                    getWebElement(AdminPageUI.ADMIN_PRODUCT_CATEGORY_FILTER));
            for (WebElement option : select.getOptions()) {
                options.add(option.getText().trim());
            }
            return options;
        }

    public java.util.List<String> getProductCategoryOptionTexts() {
            java.util.List<String> options = new java.util.ArrayList<>();
            for (WebElement option : getListWebElements(AdminPageUI.ADMIN_PRODUCT_CATEGORY_OPTIONS)) {
                options.add(option.getText().trim());
            }
            return options;
        }

    public java.util.List<String> getPromoUsageHeaders() {
            java.util.List<String> headers = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement th : getListWebElements(AdminPageUI.ADMIN_PROMO_USAGES_HEADERS)) {
                headers.add(th.getText().trim());
            }
            return headers;
        }

    /** So dong trong bang lich su su dung (trang hien tai). */
    public int getPromoUsagesRowCount() {
            return getListWebElements(AdminPageUI.ADMIN_PROMO_USAGES_ROWS).size();
        }

    /** Text tung o cua dong dau tien trong bang lich su su dung. */
    public java.util.List<String> getPromoUsagesFirstRowCellTexts() {
            java.util.List<String> cells = new java.util.ArrayList<>();
            java.util.List<WebElement> rows = getListWebElements(AdminPageUI.ADMIN_PROMO_USAGES_ROWS);
            if (rows.isEmpty()) {
                return cells;
            }
            for (org.openqa.selenium.WebElement cell : rows.get(0).findElements(org.openqa.selenium.By.tagName("td"))) {
                cells.add(cell.getText().trim());
            }
            return cells;
        }

    /** Tong luot da dung dang hien o khoi tong quan trang Lich su su dung. */
    public String getPromoUsagesTotal() {
            waitForElementVisible(AdminPageUI.ADMIN_PROMO_USAGES_TOTAL);
            return getElementText(AdminPageUI.ADMIN_PROMO_USAGES_TOTAL).trim();
        }

    /** Tim trong trang Lich su su dung (loc client-side theo ma don / email,
     *  chi tren trang hien tai — API chua ho tro tham so tim). */
    public void searchPromoUsages(String keyword) {
            sendKeyToElement(AdminPageUI.ADMIN_PROMO_USAGES_SEARCH, keyword);
            sleepInSecond(1); // cho loc client-side ap dung
        }

    /** Bam [Quay lại] o trang Lich su su dung -> ve danh sach khuyen mai. */
    public void clickPromoUsagesBackButton() {
            clickToElement(AdminPageUI.ADMIN_PROMO_USAGES_BACK_BTN);
            sleepInSecond(1); // cho route /admin + tab promotions render lai
        }

    public java.util.List<String> getReturnsStatusFilterOptionTexts() {
            java.util.List<String> options = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement option : getListWebElements(AdminPageUI.ADMIN_RETURNS_STATUS_FILTER_OPTIONS)) {
                options.add(option.getText().trim());
            }
            return options;
        }

    public java.util.List<String> getStatusFilterOptionTexts() {
            java.util.List<String> options = new java.util.ArrayList<>();
            org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(
                    getWebElement(AdminPageUI.ADMIN_STATUS_FILTER));
            for (WebElement option : select.getOptions()) {
                options.add(option.getText().trim());
            }
            return options;
        }

    private java.util.List<String> getTableRowCellTexts(org.openqa.selenium.WebElement row) {
            java.util.List<String> cells = new java.util.ArrayList<>();
            for (org.openqa.selenium.WebElement cell : row.findElements(org.openqa.selenium.By.tagName("td"))) {
                cells.add(cell.getText().trim());
            }
            return cells;
        }

    public java.util.List<String> getTierTableHeaders() {
        java.util.List<String> headers = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement th : getListWebElements(AdminPageUI.ADMIN_GENERIC_TABLE_HEADERS)) {
            headers.add(th.getText().trim());
        }
        return headers;
    }
}
