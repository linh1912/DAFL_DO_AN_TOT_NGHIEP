package vn.dafl.pageObjects;

import org.openqa.selenium.WebElement;
import vn.dafl.commons.BasePage;
import vn.dafl.pageUIs.ProductCatalogPageUI;

import java.util.ArrayList;
import java.util.List;

/** Page Object cho luoi san pham trang chu (CN-04) va trang chi tiet san pham (CN-05). */
public class ProductCatalogPageObject extends BasePage {

    public void waitForProductsLoaded() {
        waitForListElementsVisible(ProductCatalogPageUI.ANY_PRODUCT_CARD);
    }

    /** San pham dau tien dang hien thi; id lay tu data-testid cua the san pham. */
    public String getFirstProductId() {
        List<WebElement> cards = getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_CARD);
        if (cards.isEmpty()) {
            return null;
        }
        String testId = cards.get(0).getAttribute("data-testid");
        return testId == null ? null : testId.replace("product-card-", "");
    }

    public List<String> getDisplayedProductIds() {
        List<String> ids = new ArrayList<>();
        for (WebElement card : getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_CARD)) {
            String testId = card.getAttribute("data-testid");
            if (testId != null) {
                ids.add(testId.replace("product-card-", ""));
            }
        }
        return ids;
    }

    public int getProductCountOnPage() {
        return getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_CARD).size();
    }

    public String getProductName(String productId) {
        return getElementText(ProductCatalogPageUI.DYNAMIC_PRODUCT_NAME, productId);
    }

    public long getProductPrice(String productId) {
        String text = getElementText(ProductCatalogPageUI.DYNAMIC_PRODUCT_PRICE, productId);
        return parseMoney(text);
    }

    /** Chuoi ton kho tren the san pham: "Con X san pham" hoac "Het hang". */
    public String getProductStockText(String productId) {
        return getElementText(ProductCatalogPageUI.DYNAMIC_PRODUCT_STOCK, productId);
    }

    /** Gia cua tung the san pham theo thu tu hien thi (dung cho test sap xep). */
    public List<Long> getDisplayedProductPrices() {
        List<Long> prices = new ArrayList<>();
        for (String id : getDisplayedProductIds()) {
            prices.add(getProductPrice(id));
        }
        return prices;
    }

    public void addToCart(String productId) {
        scrollToElement(ProductCatalogPageUI.DYNAMIC_PRODUCT_CARD, productId);
        clickToElement(ProductCatalogPageUI.DYNAMIC_ADD_TO_CART_BTN, productId);
        pauseAfterAction();
    }

    public boolean isAddToCartButtonEnabled(String productId) {
        return isElementEnabled(ProductCatalogPageUI.DYNAMIC_ADD_TO_CART_BTN, productId);
    }

    public void openProductDetail(String productId) {
        scrollToElement(ProductCatalogPageUI.DYNAMIC_PRODUCT_CARD, productId);
        clickToElement(ProductCatalogPageUI.DYNAMIC_PRODUCT_NAME, productId);
        waitForElementVisible(ProductCatalogPageUI.PRODUCT_DETAIL_PAGE);
    }

    // ---------- Loc / sap xep / phan trang (CN-04) ----------
    public String getCatalogResultCountText() {
        return getElementText(ProductCatalogPageUI.CATALOG_RESULT_COUNT);
    }

    public boolean isCatalogEmptyStateDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.CATALOG_EMPTY_STATE, 5);
    }

    public void clearFilters() {
        clickToElement(ProductCatalogPageUI.CLEAR_FILTERS_BTN);
        sleepInSecond(2);
    }

    /** Sap xep theo value cua <select>: newest | price-asc | price-desc. */
    public void selectSort(String sortValue) {
        selectByValue(ProductCatalogPageUI.SORT_SELECT, sortValue);
        sleepInSecond(2);
    }

    public String getSortValue() {
        return getSelectedValue(ProductCatalogPageUI.SORT_SELECT);
    }

    /** Loc gia theo khoang (FE debounce 500ms roi tai lai danh sach). */
    public void setPriceRange(String from, String to) {
        if (from != null) {
            sendKeyToElement(ProductCatalogPageUI.PRICE_FROM_INPUT, from);
        }
        if (to != null) {
            sendKeyToElement(ProductCatalogPageUI.PRICE_TO_INPUT, to);
        }
        sleepInSecond(2);
    }

    public boolean isPriceFilterErrorDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRICE_FILTER_ERROR, 5);
    }

    /** Bam nut 'Áp dụng' de ap dung khoang gia da nhap o 2 o gia (dot 8). */
    public void clickPriceApply() {
        clickToElement(ProductCatalogPageUI.PRICE_APPLY_BTN);
        sleepInSecond(2); // cho loc ap dung + luoi tai lai
    }

    public boolean isPriceApplyButtonDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRICE_APPLY_BTN, 10);
    }

    /** Bam 1 khoang gia co san (dot 11): dien san Tu/Den + ap dung loc ngay. */
    public void clickPricePreset(int index) {
        clickToElement(String.format(ProductCatalogPageUI.DYNAMIC_PRICE_PRESET_BTN, index));
        sleepInSecond(2); // cho loc ap dung + luoi tai lai
    }

    public String getPriceFromValue() {
        return getElementAttribute(ProductCatalogPageUI.PRICE_FROM_INPUT, "value");
    }

    public String getPriceToValue() {
        return getElementAttribute(ProductCatalogPageUI.PRICE_TO_INPUT, "value");
    }

    public String getPriceFilterError() {
        waitForElementVisible(ProductCatalogPageUI.PRICE_FILTER_ERROR);
        return getElementText(ProductCatalogPageUI.PRICE_FILTER_ERROR);
    }

    public boolean isPaginationDisplayed() {
        return isElementDisplayed(ProductCatalogPageUI.CATALOG_PAGINATION);
    }

    public void goToCatalogPage(int pageNumber) {
        clickToElement(ProductCatalogPageUI.DYNAMIC_PAGINATION_PAGE_BTN, String.valueOf(pageNumber));
        sleepInSecond(2);
    }

    public void goToNextCatalogPage() {
        clickToElement(ProductCatalogPageUI.PAGINATION_NEXT_BTN);
        sleepInSecond(2);
    }

    public void goToPreviousCatalogPage() {
        clickToElement(ProductCatalogPageUI.PAGINATION_PREV_BTN);
        sleepInSecond(2);
    }

    public boolean isNextPageButtonEnabled() {
        return isElementEnabled(ProductCatalogPageUI.PAGINATION_NEXT_BTN);
    }

    // ---------- Trang chi tiet san pham (CN-05) ----------
    public boolean isProductDetailDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_DETAIL_PAGE, 10);
    }

    public boolean isProductNotFoundDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_NOT_FOUND_PAGE, 10);
    }

    /** So luong con lai ghi o dong "(Con X san pham co san)" tren trang chi tiet; -1 neu khong doc duoc. */
    public int getDetailAvailableStock() {
        if (!isElementDisplayed(ProductCatalogPageUI.DETAIL_STOCK_TEXT)) {
            return -1;
        }
        String text = getElementText(ProductCatalogPageUI.DETAIL_STOCK_TEXT);
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? -1 : Integer.parseInt(digits);
    }

    public int getDetailQuantity() {
        String text = getElementText(ProductCatalogPageUI.DETAIL_QTY_VALUE).trim();
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }

    public void increaseDetailQuantity() {
        clickToElement(ProductCatalogPageUI.DETAIL_QTY_PLUS);
    }

    public void decreaseDetailQuantity() {
        clickToElement(ProductCatalogPageUI.DETAIL_QTY_MINUS);
    }

    public boolean isDetailPlusEnabled() {
        return isElementEnabled(ProductCatalogPageUI.DETAIL_QTY_PLUS);
    }

    public boolean isDetailMinusEnabled() {
        return isElementEnabled(ProductCatalogPageUI.DETAIL_QTY_MINUS);
    }

    public void addToCartFromDetail() {
        clickToElement(ProductCatalogPageUI.DETAIL_ADD_TO_CART_BTN);
        pauseAfterAction();
    }

    public void buyNowFromDetail() {
        clickToElement(ProductCatalogPageUI.BUY_NOW_BTN);
    }

    public boolean isDetailAddToCartEnabled() {
        return isElementEnabled(ProductCatalogPageUI.DETAIL_ADD_TO_CART_BTN);
    }

    public boolean isBuyNowEnabled() {
        return isElementEnabled(ProductCatalogPageUI.BUY_NOW_BTN);
    }

    public boolean isOutOfStockNoticeDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.DETAIL_OUT_OF_STOCK_NOTICE, 5);
    }

    public void backFromDetail() {
        clickToElement(ProductCatalogPageUI.PRODUCT_DETAIL_BACK_BTN);
        waitForProductsLoaded();
    }

    // ---------- Danh gia sao o trang chi tiet (dot 4) ----------
    public boolean isReviewsSectionDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_REVIEWS_SECTION, 10);
    }

    public boolean isRatingSummaryDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_RATING_SUMMARY, 5);
    }

    /** Text tom tat diem trung binh (vd "5.0 ... 1 đánh giá"); rong neu khong co tom tat. */
    public String getRatingSummaryText() {
        if (!isRatingSummaryDisplayed()) {
            return "";
        }
        return getElementText(ProductCatalogPageUI.PRODUCT_RATING_SUMMARY);
    }

    /** So danh gia (product-review-item) dang hien tren trang chi tiet. */
    public int getReviewItemCount() {
        return getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_REVIEW_ITEM).size();
    }

    /** Text gop cua toan bo danh gia dang hien (ten nguoi danh gia + binh luan). */
    public String getAllReviewsText() {
        StringBuilder combined = new StringBuilder();
        for (WebElement item : getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_REVIEW_ITEM)) {
            combined.append(item.getText()).append("\n");
        }
        return combined.toString();
    }

    public boolean isReviewsEmptyStateDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_REVIEWS_EMPTY_MSG, 5);
    }

    // ---------- Form gui danh gia moi (dot 15) ----------
    public boolean isReviewFormDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.REVIEW_FORM, 10);
    }

    public boolean isReviewStarButtonDisplayed(int star) {
        return isElementDisplayedWithWait(
                getDynamicLocator(ProductCatalogPageUI.DYNAMIC_REVIEW_STAR_BTN, String.valueOf(star)), 5);
    }

    /** Bam chon so sao 1..5 o form danh gia. */
    public void clickReviewStar(int star) {
        clickToElement(ProductCatalogPageUI.DYNAMIC_REVIEW_STAR_BTN, String.valueOf(star));
    }

    public boolean isReviewNameInputDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.REVIEW_NAME_INPUT, 5);
    }

    public void setReviewerName(String name) {
        sendKeyToElement(ProductCatalogPageUI.REVIEW_NAME_INPUT, name);
    }

    public boolean isReviewCommentInputDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.REVIEW_COMMENT_INPUT, 5);
    }

    public void setReviewComment(String comment) {
        sendKeyToElement(ProductCatalogPageUI.REVIEW_COMMENT_INPUT, comment);
    }

    public boolean isReviewSubmitButtonDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.REVIEW_SUBMIT_BTN, 5);
    }

    public void clickSubmitReview() {
        clickToElement(ProductCatalogPageUI.REVIEW_SUBMIT_BTN);
        sleepInSecond(2); // cho FE validate + goi POST /api/products/{id}/evaluations
    }

    public boolean isReviewErrorDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.REVIEW_ERROR_MSG, 5);
    }

    public String getReviewErrorMessage() {
        waitForElementVisible(ProductCatalogPageUI.REVIEW_ERROR_MSG);
        return getElementText(ProductCatalogPageUI.REVIEW_ERROR_MSG);
    }

    public boolean isReviewSuccessDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.REVIEW_SUCCESS_MSG, 10);
    }

    public String getReviewSuccessMessage() {
        waitForElementVisible(ProductCatalogPageUI.REVIEW_SUCCESS_MSG);
        return getElementText(ProductCatalogPageUI.REVIEW_SUCCESS_MSG);
    }

    // ---------- Wave15.1-auto: bien the + form danh gia (Pha B, SCR_05) ----------
    /** So nut bien the dang hien tren trang chi tiet (ky vong: dung 1 nut "Mac dinh"). */
    public int getVariantOptionCount() {
        return getListWebElements(ProductCatalogPageUI.ANY_VARIANT_OPTION).size();
    }

    /** Nhan cua tat ca nut bien the dang hien. */
    public List<String> getVariantOptionLabels() {
        List<String> labels = new ArrayList<>();
        for (WebElement option : getListWebElements(ProductCatalogPageUI.ANY_VARIANT_OPTION)) {
            labels.add(option.getText().trim());
        }
        return labels;
    }

    /** Nut bien the dau tien co dang bam duoc khong (false khi SP het hang). */
    public boolean isFirstVariantOptionEnabled() {
        List<WebElement> options = getListWebElements(ProductCatalogPageUI.ANY_VARIANT_OPTION);
        if (options.isEmpty()) {
            return false;
        }
        return options.get(0).isEnabled();
    }

    /** Gia tri dang co trong o ten nguoi danh gia (dung kiem tra form duoc xoa sau khi gui). */
    public String getReviewerNameValue() {
        return getElementValue(ProductCatalogPageUI.REVIEW_NAME_INPUT);
    }

    /** Gia tri dang co trong o binh luan danh gia. */
    public String getReviewCommentValue() {
        return getElementValue(ProductCatalogPageUI.REVIEW_COMMENT_INPUT);
    }

    /** Diem trung binh X.Y trich tu tom tat danh gia; -1 neu khong doc duoc. */
    public double getRatingSummaryAverage() {
        String summary = getRatingSummaryText();
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("(\\d+\\.\\d)").matcher(summary);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : -1;
    }

    /** Tong so danh gia trich tu tom tat ("{n} danh gia"); -1 neu khong doc duoc. */
    public int getRatingSummaryReviewCount() {
        String summary = getRatingSummaryText();
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("(\\d+)\\s*đánh giá").matcher(summary);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    // ---------- Wave15.1-auto (SCR_04: luoi chinh, loc danh muc/gia) ----------

    /** Id SP chi trong LUOI CHINH (#products-section) — loai the -flash/-best/-suggest o cac muc ben duoi. */
    public List<String> getGridProductIds() {
        List<String> ids = new ArrayList<>();
        for (WebElement card : getListWebElements(ProductCatalogPageUI.ANY_GRID_PRODUCT_CARD)) {
            String testId = card.getAttribute("data-testid");
            if (testId != null) {
                ids.add(testId.replace("product-card-", ""));
            }
        }
        return ids;
    }

    /** So the SP trong luoi chinh (toi da 12/trang). */
    public int getGridProductCount() {
        return getListWebElements(ProductCatalogPageUI.ANY_GRID_PRODUCT_CARD).size();
    }

    /** Gia cua tung the trong luoi chinh theo thu tu hien thi. */
    public List<Long> getGridProductPrices() {
        List<Long> prices = new ArrayList<>();
        for (String id : getGridProductIds()) {
            prices.add(getProductPrice(id));
        }
        return prices;
    }

    /** Text badge giam gia ("-25%") cua 1 the luoi chinh; rong neu SP khong giam gia. */
    public String getProductDiscountBadgeText(String productId) {
        String locator = getDynamicLocator(ProductCatalogPageUI.DYNAMIC_PRODUCT_DISCOUNT_BADGE, productId);
        if (!isElementDisplayed(locator)) {
            return "";
        }
        return getElementText(locator);
    }

    /** Text sao cua 1 the luoi chinh (vd "4.9 (78)"); rong neu khong doc duoc. */
    public String getProductRatingText(String productId) {
        String locator = getDynamicLocator(ProductCatalogPageUI.DYNAMIC_PRODUCT_RATING, productId);
        if (!isElementDisplayed(locator)) {
            return "";
        }
        return getElementText(locator);
    }

    /** Text gia goc cua 1 the luoi chinh; rong neu khong co gia goc. */
    public String getProductOriginalPriceText(String productId) {
        String locator = getDynamicLocator(ProductCatalogPageUI.DYNAMIC_PRODUCT_ORIGINAL_PRICE, productId);
        if (!isElementDisplayed(locator)) {
            return "";
        }
        return getElementText(locator);
    }

    /** Gia goc cua the SP co bi gach ngang (text-decoration line-through) khong. */
    public boolean isOriginalPriceStruckThrough(String productId) {
        String locator = getDynamicLocator(ProductCatalogPageUI.DYNAMIC_PRODUCT_ORIGINAL_PRICE, productId);
        if (!isElementDisplayed(locator)) {
            return false;
        }
        Object result = executeScript(
                "var els = document.querySelectorAll(\"[data-testid='product-card-"
                        + productId + "'] .product-original-price\");"
                        + "if (els.length === 0) return '';"
                        + "return window.getComputedStyle(els[0]).textDecorationLine;");
        return result != null && String.valueOf(result).contains("line-through");
    }

    // ---------- Loc danh muc: nut "Xem tat ca", o tim danh muc ----------

    /** Nut "Xem tất cả" (clear-category-btn) co hien thi khong (chi hien khi da chon danh muc). */
    public boolean isClearCategoryButtonDisplayed() {
        return isElementDisplayed(ProductCatalogPageUI.CLEAR_CATEGORY_BTN);
    }

    /** Bam nut "Xem tất cả" de bo chon danh muc. */
    public void clickClearCategoryButton() {
        clickToElement(ProductCatalogPageUI.CLEAR_CATEGORY_BTN);
        sleepInSecond(2);
    }

    /** Go tu khoa vao o tim danh muc (loc theo ten hoac mo ta). */
    public void typeCategorySearch(String keyword) {
        sendKeyToElement(ProductCatalogPageUI.CATEGORY_SEARCH_INPUT, keyword);
        sleepInSecond(1);
    }

    /** Gia tri dang go trong o tim danh muc. */
    public String getCategorySearchValue() {
        return getElementValue(ProductCatalogPageUI.CATEGORY_SEARCH_INPUT);
    }

    /** Nut xoa trong o tim danh muc co hien thi khong (chi hien khi o co chu). */
    public boolean isCategorySearchClearButtonDisplayed() {
        return isElementDisplayed(ProductCatalogPageUI.CATEGORY_SEARCH_CLEAR_BTN);
    }

    /** Bam nut xoa trong o tim danh muc. */
    public void clickCategorySearchClearButton() {
        clickToElement(ProductCatalogPageUI.CATEGORY_SEARCH_CLEAR_BTN);
        sleepInSecond(1);
    }

    /** Ten day du (title) cua cac pill danh muc dang hien (tru pill "Tat ca danh muc"). */
    public List<String> getVisibleCategoryPillNames() {
        List<String> names = new ArrayList<>();
        for (WebElement pill : getListWebElements(vn.dafl.pageUIs.HomePageUI.ANY_CATEGORY_PILL)) {
            if (!pill.isDisplayed()) {
                continue;
            }
            String testId = pill.getAttribute("data-testid");
            if (testId == null || testId.equals("category-pill-all")) {
                continue;
            }
            String title = pill.getAttribute("title");
            names.add(title != null ? title : pill.getText().trim());
        }
        return names;
    }

    /** Ten day du cua pill danh muc dang duoc chon (class active); null neu dang xem tat ca. */
    public String getActiveCategoryPillName() {
        for (WebElement pill : getListWebElements(vn.dafl.pageUIs.HomePageUI.ANY_CATEGORY_PILL)) {
            String testId = pill.getAttribute("data-testid");
            if (testId == null || testId.equals("category-pill-all")) {
                continue;
            }
            String cssClass = pill.getAttribute("class");
            if (cssClass != null && cssClass.contains("active")) {
                String title = pill.getAttribute("title");
                return title != null ? title : pill.getText().trim();
            }
        }
        return null;
    }

    /** Trang thai rong tim danh muc (category-empty-state) co hien thi khong. */
    public boolean isCategoryEmptyStateDisplayed() {
        return isElementDisplayed(ProductCatalogPageUI.CATEGORY_EMPTY_STATE);
    }

    /** Text trang thai rong tim danh muc. */
    public String getCategoryEmptyStateText() {
        waitForElementVisible(ProductCatalogPageUI.CATEGORY_EMPTY_STATE);
        return getElementText(ProductCatalogPageUI.CATEGORY_EMPTY_STATE);
    }

    /** Bam nut "Hiển thị tất cả" trong trang thai rong tim danh muc. */
    public void clickCategoryEmptyShowAll() {
        clickToElement(ProductCatalogPageUI.CATEGORY_EMPTY_SHOW_ALL_BTN);
        sleepInSecond(1);
    }

    // ---------- Nhom "Loai san pham" o sidebar (class filter-link) ----------

    /** Ten cac muc trong nhom "Loai san pham" ("Tat ca san pham" + ten danh muc). */
    public List<String> getFilterLinkNames() {
        List<String> names = new ArrayList<>();
        for (WebElement link : getListWebElements(ProductCatalogPageUI.ANY_FILTER_LINK_BTN)) {
            names.add(link.getText().trim());
        }
        return names;
    }

    /** Bam 1 muc trong nhom "Loai san pham" theo ten hien thi day du. */
    public void clickFilterLinkByName(String name) {
        clickToElement(ProductCatalogPageUI.DYNAMIC_FILTER_LINK_BTN, name);
        sleepInSecond(2);
    }

    // ---------- Muc gia san: trang thai active ----------

    /** Muc gia san co dang duoc chon khong (class active + aria-pressed=true). */
    public boolean isPricePresetActive(int index) {
        String locator = getDynamicLocator(ProductCatalogPageUI.DYNAMIC_PRICE_PRESET_BTN, String.valueOf(index));
        if (!isElementDisplayed(locator)) {
            return false;
        }
        String cssClass = getElementAttribute(locator, "class");
        String pressed = getElementAttribute(locator, "aria-pressed");
        return cssClass != null && cssClass.contains("active") && "true".equals(pressed);
    }
}
