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
 *  mat khau admin - tru khi doi tuong do chinh test tao ra.
 *  Dot 12: form them/sua San pham / Danh muc / Khuyen mai mo trong MODAL (testid
 *  admin-*-form-modal); toolbar chuan tren moi bang (nut Them moi ben trai, o tim
 *  + nut Tim/Dat lai/Lam moi/Export ben phai); phan trang client-side 10 dong/trang
 *  (admin-*-pagination); o search tren topbar + breadcrumb da bo han. */
public class AdminPageObject extends BasePage {

    public static final String[] ADMIN_NAV_KEYS = {
            "dashboard", "orders", "products", "categories",
            "promotions", "customers", "membership", "returns"
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

    /** Tu modal chi tiet san pham, bam 'Sửa sản phẩm' -> form sua mo ra trong modal
     *  (dot 12); modal chi tiet tu dong khi mo form sua (chi mo, khong luu). */
    public void openProductEditFormFromDetail() {
        clickToElement(AdminPageUI.PRODUCT_DETAIL_EDIT_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM_MODAL);
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

    // ---------- Toolbar chuan + phan trang + form modal (dot 12) ----------
    /** Nut "+ Them san pham" o ben trai toolbar muc San pham. */
    public boolean isProductAddButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_ADD_BTN, 10);
    }

    /** Nut icon "Dat lai" (↺) o ben phai toolbar cua muc cho truoc
     *  (sectionKey = orders/products/categories/customers/promos/members/returns;
     *  bang nhat ky tra hang dung 'returns-audit'). */
    public boolean isToolbarResetButtonDisplayed(String sectionKey) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_RESET_BTN, sectionKey), 10);
    }

    /** Nut icon "Lam moi" (⟳) o ben phai toolbar cua muc cho truoc. */
    public boolean isToolbarRefreshButtonDisplayed(String sectionKey) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_REFRESH_BTN, sectionKey), 10);
    }

    /** Bam nut icon "Dat lai" cua muc: xoa trang o tim/loc + ve trang 1. */
    public void clickToolbarReset(String sectionKey) {
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_RESET_BTN, sectionKey);
        sleepInSecond(1); // cho loc client-side ap dung
    }

    /** Bam nut icon "Lam moi" cua muc: tai lai du lieu cua muc. */
    public void clickToolbarRefresh(String sectionKey) {
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_TOOLBAR_REFRESH_BTN, sectionKey);
        sleepInSecond(2); // cho GET tai lai
    }

    /** Khoi phan trang cua muc co hien khong (chi hien khi bang co du lieu). */
    public boolean isPaginationDisplayed(String sectionKey) {
        return isElementDisplayedWithWait(
                getDynamicLocator(AdminPageUI.DYNAMIC_ADMIN_PAGINATION, sectionKey), 10);
    }

    /** Text "Trang X / Y — Tong N dong" cua khoi phan trang; "" neu muc khong co phan trang. */
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

    /** Tong so dong cua bang (N trong "Tong N dong"); -1 neu muc khong co phan trang. */
    public int getPaginationTotalRows(String sectionKey) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("Tổng\\s+(\\d+)\\s+dòng")
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

    /** Bam nut so trang N cua phan trang. */
    public void clickPaginationPage(String sectionKey, int page) {
        clickToElement(AdminPageUI.DYNAMIC_ADMIN_PAGINATION_PAGE, sectionKey, String.valueOf(page));
        sleepInSecond(1);
    }

    /** Gia tri dang go trong o tim kiem cua muc San pham (dung de khang dinh nut "Dat lai" da xoa trang). */
    public String getProductSearchValue() {
        return getElementValue(AdminPageUI.ADMIN_PRODUCT_SEARCH);
    }

    // ----- Form modal (dot 12) -----
    /** Modal form them/sua san pham co dang hien khong (cho doi toi da 10 giay). */
    public boolean isProductFormModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PRODUCT_FORM_MODAL, 10);
    }

    /** Modal form them/sua san pham co dang hien khong (kiem tra ngay, khong cho doi -
     *  dung cho khang dinh modal da dong). */
    public boolean isProductFormModalPresent() {
        return isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_FORM_MODAL);
    }

    /** Modal form them/sua danh muc co dang hien khong (cho doi toi da 10 giay). */
    public boolean isCategoryFormModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CATEGORY_FORM_MODAL, 10);
    }

    /** Modal form tao khuyen mai co dang hien khong (cho doi toi da 10 giay). */
    public boolean isPromoFormModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_PROMO_FORM_MODAL, 10);
    }

    /** Text cua modal form san pham (gom ca tieu de modal 'Them/Sua san pham' + cac nhan truong). */
    public String getProductFormModalText() {
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM_MODAL);
        return getElementText(AdminPageUI.ADMIN_PRODUCT_FORM_MODAL);
    }

    /** Gia tri dang dien trong o ten san pham cua form (dung de khang dinh form sua da dien san ten). */
    public String getProductNameInputValue() {
        return getElementValue(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT);
    }

    /** Mo modal them san pham moi bang nut "+ Them san pham" o toolbar (chi mo, khong dien gi ca). */
    public void openProductFormModal() {
        clickToElement(AdminPageUI.ADMIN_PRODUCT_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM_MODAL);
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
     *  (bo qua option placeholder '— Chọn danh mục —'); null neu khong co danh muc nao.
     *  CHI GOI KHI FORM DA MO (form gio nam trong modal - dot 12): mo modal truoc
     *  bang openProductFormModal() roi moi doc duoc danh muc trong select. */
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
     *  thanh cong thi modal form tu dong va hien thong bao. */
    public void createProduct(String name, String categoryName, String price, String stock) {
        openProductFormModal();
        fillProductFormAndSave(name, categoryName, price, stock);
    }

    /** Dien cac truong cua form san pham (form da mo trong modal) roi bam Luu;
     *  thanh cong thi modal form tu dong va hien thong bao. */
    public void fillProductFormAndSave(String name, String categoryName, String price, String stock) {
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
        waitForElementVisible(AdminPageUI.ADMIN_PRODUCT_FORM_MODAL);
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

    /** Tao danh muc moi (ten 2-150 ky tu); thanh cong thi modal form tu dong va hien thong bao. */
    public void createCategory(String name) {
        clickToElement(AdminPageUI.ADMIN_CATEGORY_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM_MODAL);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, name);
        clickToElement(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN);
        waitForNotice();
        waitForElementInvisible(AdminPageUI.ADMIN_CATEGORY_FORM);
    }

    /** Sua danh muc co san (tim theo ten cu): doi ten, luu lai. */
    public void editCategory(String oldName, String newName) {
        clickToElement(AdminPageUI.DYNAMIC_CATEGORY_EDIT_BTN, oldName);
        waitForElementVisible(AdminPageUI.ADMIN_CATEGORY_FORM_MODAL);
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
    // Dot 13: giao dien DA CO sua/xoa voucher Draft (nut sua/xoa chi hien o dong
    // Draft) + hop xac nhan chung khi xoa; tao moi (trang thai Nhap), kich hoat
    // (Nhap -> Dang chay) va tat co ly do (Dang chay/Nhap -> Da tat) giu nguyen.
    public void searchPromos(String keyword) {
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_SEARCH, keyword);
        sleepInSecond(2); // cho loc client-side ap dung
    }

    /** Tao voucher moi (ma 3-50 ky tu, giam % 1-100, mac dinh toan san, khong gioi han
     *  ngay); tao xong o trang thai Nhap, modal form tu dong va hien thong bao. */
    public void createPromo(String code, String discountValue) {
        clickToElement(AdminPageUI.ADMIN_PROMO_ADD_BTN);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM_MODAL);
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
     *  doi); luu xong modal form tu dong va hien thong bao. */
    public void editPromo(String code, String newDescription, String newDiscountValue) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_EDIT_BTN, code);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM_MODAL);
        waitForElementVisible(AdminPageUI.ADMIN_PROMO_FORM);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_DESCRIPTION_INPUT, newDescription);
        sendKeyToElement(AdminPageUI.ADMIN_PROMO_DISCOUNT_VALUE_INPUT, newDiscountValue);
        clickToElement(AdminPageUI.ADMIN_PROMO_SAVE_BTN);
        waitForNotice();
        waitForElementInvisible(AdminPageUI.ADMIN_PROMO_FORM);
    }

    /** Xoa voucher Draft: bam nut xoa tren dong -> hop xac nhan chung hien ra ->
     *  bam 'Xóa khuyến mãi' -> hien thong bao. */
    public void deletePromoByCode(String code) {
        clickToElement(AdminPageUI.DYNAMIC_PROMO_DELETE_BTN, code);
        waitForElementVisible(AdminPageUI.ADMIN_CONFIRM_MODAL);
        clickToElement(AdminPageUI.ADMIN_CONFIRM_YES_BTN);
        waitForNotice();
        sleepInSecond(2); // cho loadPromos tai lai danh sach
    }

    /** Hop xac nhan dung chung (admin-confirm-modal) co dang hien khong. */
    public boolean isConfirmModalDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_CONFIRM_MODAL, 10);
    }

    /** Noi dung hop xac nhan (tieu de + thong diep). */
    public String getConfirmModalText() {
        waitForElementVisible(AdminPageUI.ADMIN_CONFIRM_MODAL);
        return getElementText(AdminPageUI.ADMIN_CONFIRM_MODAL);
    }

    /** Bam nut xac nhan trong hop xac nhan chung (nhan thay doi theo hanh dong). */
    public void clickConfirmYes() {
        clickToElement(AdminPageUI.ADMIN_CONFIRM_YES_BTN);
    }

    /** Bam 'Hủy' trong hop xac nhan chung (khong thuc hien hanh dong). */
    public void clickConfirmNo() {
        clickToElement(AdminPageUI.ADMIN_CONFIRM_NO_BTN);
    }

    // ----- Hoi vien: doc so du diem khach (dot 13, muc 3) -----
    /** Doc so du diem cua 1 khach qua modal chi tiet (tim theo email, mo nut mat,
     *  doc dong 'Số dư điểm: X điểm' o admin-customer-detail-points); tra -1 neu
     *  khong tim thay khach nao co email do trong bang Khach hang. */
    public int getCustomerPointsByEmail(String email) {
        clickAdminNav("customers");
        searchCustomers(email);
        if (getAdminTableRowCount() == 0) {
            return -1;
        }
        clickFirstCustomerDetail();
        String pointsText = getElementText(AdminPageUI.ADMIN_CUSTOMER_DETAIL_POINTS);
        closeCustomerDetailModal();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("Số dư điểm:\\s*([\\d.,]+)").matcher(pointsText);
        if (!matcher.find()) {
            throw new AssertionError("Khong doc duoc so du diem tu dong '" + pointsText + "'");
        }
        return Integer.parseInt(matcher.group(1).replace(".", "").replace(",", ""));
    }

    /** Dong modal chi tiet khach bang nut X trong dialog cha (SparkModal cua
     *  chi tiet khach khong co testid rieng nen khong dung closeDetailModal). */
    public void closeCustomerDetailModal() {
        clickToElement(AdminPageUI.CUSTOMER_DETAIL_CLOSE_BTN);
        waitForElementInvisible(AdminPageUI.ADMIN_CUSTOMER_DETAIL);
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

    // ----- Nhat ky he thong: loc ngay + Export (dot 13, muc 10) -----
    /** Tieu de 'Nhật ký hệ thống (audit-log)' o cuoi tab Tra hang co hien khong. */
    public boolean isAuditLogSectionDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_AUDIT_LOG_HEADING, 10);
    }

    /** Nhap khoang ngay loc nhat ky (dinh dang yyyy-MM-dd, vd 2026-01-01). */
    public void setAuditDateFilter(String dateFrom, String dateTo) {
        sendKeyToElement(AdminPageUI.ADMIN_LOG_DATE_FROM, dateFrom);
        sendKeyToElement(AdminPageUI.ADMIN_LOG_DATE_TO, dateTo);
    }

    /** Bam nut 'Lọc' ap dung khoang ngay cho nhat ky. */
    public void clickAuditFilter() {
        clickToElement(AdminPageUI.ADMIN_LOG_FILTER_BTN);
        sleepInSecond(2); // cho loc client-side ap dung
    }

    public boolean isAuditExportButtonDisplayed() {
        return isElementDisplayedWithWait(AdminPageUI.ADMIN_LOG_EXPORT_BTN, 10);
    }

    /** Bam nut 'Export' cua nhat ky (xuat CSV client-side — chi bam, khong
     *  khang dinh file tai ve, giong clickExportButton cua cac bang khac). */
    public void clickAuditExport() {
        clickToElement(AdminPageUI.ADMIN_LOG_EXPORT_BTN);
        sleepInSecond(1);
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
