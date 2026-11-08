package vn.dafl.pageObjects;

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
 *  mat khau admin - tru khi doi tuong do chinh test tao ra. */
public class AdminPageObject extends BasePage {

    public static final String[] ADMIN_NAV_KEYS = {
            "dashboard", "orders", "products", "categories",
            "promotions", "customers", "membership", "returns"
    };

    /** Mo thang /admin tren app Admin rieng :3001 (khong qua menu header).
     *  KHONG dung /admin o :3000 nua (dot 10: test admin chi chay :3001). */
    public void openAdminPage() {
        openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
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

    /** Danh sach key menu quan tri dang hien thi (du kien du 8 muc). */
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

    // ---------- Bieu do doanh thu (dot 4, chi doc GET /api/admin/reports/revenue) ----------
    public boolean isRevenueRange7Displayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_RANGE_7, 10);
    }

    public boolean isRevenueRange30Displayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_RANGE_30, 10);
    }

    public void clickRevenueRange7() {
        clickToElement(AdminPageUI.ADMIN_REVENUE_RANGE_7);
        sleepInSecond(2); // cho GET /api/admin/reports/revenue?from&to tai lai
    }

    public void clickRevenueRange30() {
        clickToElement(AdminPageUI.ADMIN_REVENUE_RANGE_30);
        sleepInSecond(2);
    }

    public boolean isRevenueChartDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_CHART, 10);
    }

    public boolean isRevenueChartSvgDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_CHART_SVG, 5);
    }

    public boolean isRevenueEmptyMessageDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_REVENUE_EMPTY_MSG, 5);
    }

    /** Trang thai tai du lieu doanh thu: "chart" neu co bieu do, "empty" neu chua co
     *  doanh thu trong khoang, "unknown" neu ca hai deu khong thay. */
    public String getRevenueState() {
        if (isRevenueChartDisplayed()) {
            return "chart";
        }
        if (isRevenueEmptyMessageDisplayed()) {
            return "empty";
        }
        return "unknown";
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

    public void clickFirstCategoryDetail() {
        clickToElement(AdminPageUI.FIRST_CATEGORY_DETAIL_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_DETAIL);
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
        waitForElementVisible(AdminPageUI.ADMIN_CUSTOMER_DETAIL);
    }

    public void clickFirstReturnReview() {
        clickToElement(AdminPageUI.FIRST_RETURN_REVIEW_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_RETURN_DETAIL);
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

    /** Tu modal chi tiet san pham, bam 'Sửa sản phẩm' -> form sua mo ra (chi mo, khong luu). */
    public void openProductEditFormFromDetail() {
        clickToElement(AdminPageUI.PRODUCT_DETAIL_EDIT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    public boolean isProductFormDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_FORM, 10);
    }

    public String getProductFormText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
        return getElementText(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Dong form them/sua san pham bang nut 'Đóng' (khong luu, khong doi du lieu). */
    public void closeProductForm() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_FORM_CLOSE_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    // ---------- Tim kiem / loc / Export o cac bang (chi doc) ----------
    public void searchProducts(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_SEARCH, keyword);
        sleepInSecond(2); // cho loc client-side ap dung
    }

    public void searchCustomers(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_CUSTOMER_SEARCH, keyword);
        sleepInSecond(2);
    }

    public void searchOrders(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_ORDER_SEARCH, keyword);
        sleepInSecond(2);
    }

    /** Tim bang o search tren topbar (chi ho tro muc Don hang / San pham / Khach hang). */
    public void topbarSearch(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_TOPBAR_SEARCH_INPUT, keyword);
        sleepInSecond(2);
    }

    public boolean isTopbarSearchEnabled() {
        return isElementEnabled(AdminPageUI.ADMIN_TOPBAR_SEARCH_INPUT);
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

    public void clickOrderFilterSearch() {
        clickToElement(AdminPageUI.ADMIN_ORDER_FILTER_BTN);
        sleepInSecond(2); // cho GET /api/admin/orders tai lai
    }

    public boolean isExportButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_EXPORT_BTN, 10);
    }

    /** Bam nut Export (xuat CSV client-side, chi tai file ve - khong doi du lieu).
     *  Khong khang dinh file tai ve (ten file phu thuoc trinh duyet/may chay);
     *  chi dam bao nut bam duoc va trang admin van dung yen sau khi bam. */
    public void clickExportButton() {
        clickToElement(AdminPageUI.ADMIN_EXPORT_BTN);
        sleepInSecond(1);
    }

    // ---------- Dang nhap Admin tren app rieng :3001 (dot 9, dung cho AdminCrudTest) ----------
    /** Dang nhap bang tai khoan bat ky tren trang dang nhap Admin rieng :3001
     *  (khong yeu cau phai la Super Admin - dung cho test 403 cua tai khoan thuong);
     *  bo qua test neu service :3001 chua chay. Sau khi dang nhap KHONG tu mo /admin
     *  (nguoi goi tu quyet dinh mo trang nao de kiem tra quyen). */
    public void loginAsUserOnAdminAppOrSkip(String usernameOrEmail, String password) {
        openPageUrl(GlobalConstants.ADMIN_APP_URL + "/login");
        AuthModalPageObject authModal = new AuthModalPageObject();
        try {
            authModal.waitForModalVisible();
        } catch (Exception e) {
            throw new SkipException("Bo qua: trang Admin rieng " + GlobalConstants.ADMIN_APP_URL
                    + " chua chay (can 'docker compose up -d --build' de service dafl-admin len cong 3001)");
        }
        authModal.loginAs(usernameOrEmail, password);
    }

    /** Dang nhap Super Admin tren trang Admin rieng (localhost:3001, build
     *  VITE_ADMIN_ONLY=true). Token JWT luu theo origin nen phai dang nhap o :3001
     *  va o lai :3001 (khong dung /admin o :3000 sau khi dang nhap :3001).
     *  Bo qua test (SkipException) khi service :3001 chua chay hoac tai khoan
     *  admin@dafl.vn chua duoc seed tren DB nay. */
    public void loginAsAdminOnAdminAppOrSkip() {
        loginAsUserOnAdminAppOrSkip("admin", GlobalConstants.ADMIN_PASSWORD);
        openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
        try {
            waitForAdminGateResolved();
        } catch (AssertionError e) {
            throw new SkipException("Bo qua: khong vao duoc trang Admin o " + GlobalConstants.ADMIN_APP_URL
                    + " (tai khoan admin chua duoc seed hoac dich vu admin chua san sang)");
        }
        if (!isAdminMenuDisplayed()) {
            throw new SkipException("Bo qua: tai khoan admin " + GlobalConstants.ADMIN_EMAIL
                    + " chua ton tai/khong dang nhap duoc tren DB nay (can seed hoac cap Role='SuperAdmin' bang SQL)");
        }
    }

    // ---------- CRUD (dot 9) - chi dung tren du lieu do test tao, co tu don dep ----------
    /** Thong bao thanh cong (.sa-notice) sau mot thao tac luu/xoa/doi trang thai;
     *  cho toi da 10 giay roi tra ve noi dung thong bao. */
    public String waitForNotice() {
        waitForElementVisible(AdminPageUI.ADMIN_NOTICE);
        return getElementText(AdminPageUI.ADMIN_NOTICE);
    }

    public boolean isNoticeDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_NOTICE, 10);
    }

    /** Co dong nao trong bang cua muc dang mo chua chuoi cho truoc khong. */
    public boolean isRowWithTextDisplayed(String text) {
        return getAdminTableRowTexts().stream().anyMatch(t -> t.contains(text));
    }

    /** Tim kiem don hang theo ma/tu khoa roi bam nut 'Tìm' (loc don chay server-side,
     *  khac voi loc client-side o muc San pham / Danh muc / Khuyen mai). */
    public void searchOrdersAndApply(String keyword) {
        searchOrders(keyword);
        clickOrderFilterSearch();
    }

    // ----- San pham -----
    /** Ten danh muc that dau tien trong select cua form them/sua san pham
     *  (bo qua option placeholder '— Chọn danh mục —'); null neu khong co danh muc nao. */
    public String getFirstRealCategoryName() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT);
        for (WebElement option : getListWebElements(
                "css=[data-testid='admin-product-category-select'] option")) {
            String value = option.getAttribute("value");
            String text = option.getText().trim();
            if (value != null && !value.isEmpty() && !text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    /** Tao san pham moi (ten 3-120 ky tu, gia > 0, ton kho >= 0, phai chon danh muc);
     *  thanh cong thi form tu dong va hien thong bao. */
    public void createProduct(String name, String categoryName, String price, String stock) {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT, name);
        selectByVisibleText(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SELECT, categoryName);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_PRICE_INPUT, price);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_STOCK_INPUT, stock);
        clickToElement(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN);
        waitForNotice();
        waitForElementInvisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Sua san pham co san (tim theo ten cu): doi ten + gia, luu lai. */
    public void editProduct(String oldName, String newName, String newPrice) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_EDIT_BTN, oldName);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT, newName);
        sendKeyToElement(AdminPageUI.ADMIN_PRODUCT_PRICE_INPUT, newPrice);
        clickToElement(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN);
        waitForNotice();
        waitForElementInvisible(AdminPageUI.ADMIN_PRODUCT_FORM);
    }

    /** Xoa san pham theo ten (khong co hop thoai xac nhan - xoa ngay khi bam nut thung rac). */
    public void deleteProductByName(String name) {
        clickToElement(AdminPageUI.DYNAMIC_PRODUCT_DELETE_BTN, name);
        waitForNotice();
        sleepInSecond(2); // cho loadProducts tai lai danh sach
    }

    // ----- Danh muc -----
    public void searchCategories(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_SEARCH, keyword);
        sleepInSecond(2); // cho loc client-side ap dung
    }

    /** Tao danh muc moi (ten 2-150 ky tu); thanh cong thi form tu dong va hien thong bao. */
    public void createCategory(String name) {
        clickToElement(AdminPageUI.ADMIN_CATEGORY_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, name);
        clickToElement(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN);
        waitForNotice();
        waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Sua danh muc co san (tim theo ten cu): doi ten, luu lai. */
    public void editCategory(String oldName, String newName) {
        clickToElement(AdminPageUI.DYNAMIC_CATEGORY_EDIT_BTN, oldName);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, newName);
        clickToElement(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN);
        waitForNotice();
        waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Xoa danh muc theo ten (khong co hop thoai xac nhan). */
    public void deleteCategoryByName(String name) {
        clickToElement(AdminPageUI.DYNAMIC_CATEGORY_DELETE_BTN, name);
        waitForNotice();
        sleepInSecond(2); // cho loadCategories tai lai danh sach
    }

    public boolean isEmptyCategoriesMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_CATEGORIES_MSG);
    }

    // ----- Khuyen mai / voucher -----
    // Giao dien KHONG co nut sua/xoa voucher: chi tao moi (trang thai Nhap), kich hoat
    // (Nhap -> Dang chay) va tat co ly do (Dang chay/Nhap -> Da tat).
    public void searchPromos(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_SEARCH, keyword);
        sleepInSecond(2); // cho loc client-side ap dung
    }

    /** Tao voucher moi (ma 3-50 ky tu, giam % 1-100, mac dinh toan san, khong gioi han
     *  ngay); tao xong o trang thai Nhap, form tu dong va hien thong bao. */
    public void createPromo(String code, String discountValue) {
        clickToElement(AdminPageUI.ADMIN_PROMO_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_CODE_INPUT, code);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_VALUE_INPUT, discountValue);
        clickToElement(AdminPageUI.ADMIN_PROMO_CREATE_BTN);
        waitForNotice();
        waitForElementInvisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Kich hoat voucher dang o trang thai Nhap (Nhap -> Dang chay). */
    public void activatePromo(String code) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_ACTIVATE_BTN, code);
        waitForNotice();
        sleepInSecond(2); // cho loadPromos tai lai danh sach
    }

    /** Tat voucher (Dang chay/Nhap -> Da tat), bat buoc nhap ly do. */
    public void disablePromo(String code, String reason) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_DISABLE_BTN, code);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_DISABLE_REASON);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_DISABLE_REASON, reason);
        clickToElement(AdminPageUI.ADMIN_PROMO_DISABLE_CONFIRM_BTN);
        waitForNotice();
        sleepInSecond(2); // cho loadPromos tai lai danh sach
    }

    public boolean isEmptyPromosMessageDisplayed() {
        return isElementDisplayed(AdminPageUI.ADMIN_EMPTY_PROMOS_MSG);
    }

    // ----- Don hang (dot 10: chi doi/huy 1 don 'Chờ xử lý' CO SAN trong DB;
    //  khong dat don moi qua web ban hang nua, khong khoi phuc duoc trang thai cu) -----
    /** Mo muc Don hang, tim theo ma don (loc server-side) roi mo modal chi tiet cua don do. */
    public void searchOrderByCodeAndOpen(String orderCode) {
        clickAdminNav("orders");
        searchOrdersAndApply(orderCode);
        clickToElement(AdminPageUI.DYNAMIC_ORDER_DETAIL_BTN, orderCode);
        waitForElementVisible(AdminPageUI.ADMIN_ORDER_DETAIL);
    }

    /** Doi trang thai don trong modal chi tiet (chon nhan tieng Viet, vd 'Đã xác nhận'). */
    public void changeOrderStatusTo(String visibleStatusLabel) {
        selectByVisibleText(AdminPageUI.ADMIN_ORDER_STATUS_SELECT, visibleStatusLabel);
        clickToElement(AdminPageUI.ADMIN_ORDER_CHANGE_STATUS_BTN);
        waitForNotice();
        sleepInSecond(2); // cho chi tiet don + danh sach tai lai
    }

    /** Huy don trong modal chi tiet (bat buoc nhap ly do). */
    public void cancelOrderWithReason(String reason) {
        sendKeyToElement(AdminPageUI.ADMIN_ORDER_CANCEL_REASON, reason);
        clickToElement(AdminPageUI.ADMIN_ORDER_CANCEL_BTN);
        waitForNotice();
        sleepInSecond(2); // cho chi tiet don + danh sach tai lai
    }

    /** Dong modal chi tiet don bang nut X. */
    public void closeOrderDetail() {
        closeDetailModal("admin-order-detail");
    }

    /** Tim don theo ma roi kiem tra dong cua no co hien dung nhan trang thai khong. */
    public boolean isOrderRowWithStatusDisplayed(String orderCode, String statusLabel) {
        searchOrdersAndApply(orderCode);
        return getAdminTableRowTexts().stream()
                .anyMatch(text -> text.contains(orderCode) && text.contains(statusLabel));
    }

    // ----- Hoi vien: dieu chinh diem -----
    /** Dieu chinh diem cho 1 khach (nhap email hoac ID; diem duong = cong, am = tru,
     *  khac 0, toi da 1000/lan; bat buoc co ly do). Thanh cong thi hien thong bao
     *  va o so diem + ly do duoc xoa trang. */
    public void adjustPointsForUser(String emailOrId, String points, String reason) {
        clickAdminNav("membership");
        waitForElementVisible(AdminPageUI.ADMIN_ADJUST_POINTS_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_USER_INPUT, emailOrId);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_POINTS_INPUT, points);
        sendKeyToElement(AdminPageUI.ADMIN_ADJUST_REASON_INPUT, reason);
        clickToElement(AdminPageUI.ADMIN_ADJUST_POINTS_BTN);
        waitForNotice();
    }

    /** Sau khi dieu chinh diem thanh cong, o so diem + ly do phai duoc xoa trang. */
    public boolean isAdjustPointsFormCleared() {
        return getElementValue(AdminPageUI.ADMIN_ADJUST_POINTS_INPUT).isEmpty()
                && getElementValue(AdminPageUI.ADMIN_ADJUST_REASON_INPUT).isEmpty();
    }
}
