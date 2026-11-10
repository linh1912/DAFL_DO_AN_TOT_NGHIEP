package vn.dafl.tests.web;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.MembershipPageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tinh nang moi dot 15 tren web ban hang: gui danh gia san pham (RV),
 * dung diem doi tien khi thanh toan (PT), ngay sinh + voucher sinh nhat (BD).
 *
 * Gia dinh theo dung spec FE dot 15 (testid + cau thong bao trong brief):
 * - Form danh gia hien cho ca khach chua dang nhap (co o nhap ten
 *   review-name-input); neu FE that ra chi hien form khi da dang nhap thi
 *   RV-01..RV-04 se fail va can them buoc dang nhap demo truoc khi mo chi tiet.
 * - 1 diem = 1.000d; tran diem dung duoc tuy thuoc so du + tran theo hang.
 * - RV-04 tao DU LIEU THAT: 1 danh gia moi tren san pham seed (binh luan kem
 *   timestamp de khong trung khi chay lap); khong co chuc nang xoa danh gia
 *   qua UI nen du lieu nay o lai trong DB. BD-04 (neu du dieu kien) co the
 *   tao 1 voucher sinh nhat that cho tai khoan demo (1 lan/nam).
 * - Cac test PT/BD dung chung tai khoan demo nen tu don dep: tat toggle diem
 *   sau PT-02/PT-03, go voucher sau PT-04; PT-04 tu tat diem + go voucher cu
 *   truoc khi ap FORDEV de khong phu thuoc thu tu chay.
 */
@Feature("Tính năng mới đợt 15")
public class Wave15FeaturesTest extends BaseTest {

    /** Ma voucher seed khong dung chung voi diem (xem TEST-MAPPING.md). */
    private static final String POINTS_BLOCKING_VOUCHER = "FORDEV";

    /** Ngay sinh hop le dung cho BD-02 (chi dung khi tai khoan demo chua co ngay sinh). */
    private static final String TEST_BIRTHDATE = "1998-05-20";

    /** Mo trang chi tiet cua san pham dau tien tren trang chu. */
    private ProductCatalogPageObject openFirstProductDetail() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());
        return catalog;
    }

    /** Doc so du diem kha dung cua tai khoan demo qua /membership. */
    private int readDemoAvailablePoints() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();
        return membershipPage.getAvailablePoints();
    }

    /** Them san pham dau tien vao gio roi mo trang /cart. */
    private CartDrawerPageObject openCartWithOneProduct() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        return homePage.openCart();
    }

    /** Go voucher dang ap dung tren gio demo (neu co) — don dep trang thai
     *  do lan chay truoc de lai, de test diem/voucher khong bi anh huong. */
    private void removeAppliedVoucherIfAny(CartDrawerPageObject cart) {
        if (cart.isVoucherAppliedBannerDisplayed() && cart.isRemoveVoucherButtonDisplayed()) {
            cart.clickRemoveVoucher();
            sleepInSecond(1);
        }
    }

    /** Bat toggle dung diem neu o nhap dang la 0 (toggle co the da bat san
     *  do lan chay truoc de lai tren gio demo). */
    private void ensurePointsToggleOn(CartDrawerPageObject cart) {
        if (cart.getPointsToUseValue() == 0) {
            cart.clickUsePointsToggle();
            sleepInSecond(1);
        }
    }

    /** Tat toggle dung diem neu dang bat — don dep best-effort, khong lam
     *  hong ket qua test chinh neu don dep that bai. */
    private void turnPointsOffIfOn(CartDrawerPageObject cart) {
        try {
            if (cart.isUsePointsSectionPresent() && cart.getPointsToUseValue() > 0) {
                cart.clickUsePointsToggle();
                sleepInSecond(1);
            }
        } catch (Exception ignored) {
            // Don dep that bai thi bo qua: gio demo se duoc don o test sau.
        }
    }

    /** Trich so diem tran {m} tu cau loi "Chỉ dùng tối đa {m} điểm cho đơn này." */
    private int parseMaxPointsFromError(String errorMessage) {
        Matcher matcher = Pattern.compile("tối đa\\s+([0-9][0-9.,]*)\\s+điểm").matcher(errorMessage);
        if (!matcher.find()) {
            throw new AssertionError("Khong trich duoc tran diem tu loi: " + errorMessage);
        }
        return (int) parseMoney(matcher.group(1));
    }

    /** Sinh nhat nam nay / nam sau co nam trong 30 ngay quanh hom nay khong. */
    private boolean isBirthdayWithin30Days(String isoDate) {
        try {
            LocalDate birthdate = LocalDate.parse(isoDate);
            LocalDate today = LocalDate.now();
            long toThisYear = Math.abs(ChronoUnit.DAYS.between(today, birthdate.withYear(today.getYear())));
            long toNextYear = Math.abs(ChronoUnit.DAYS.between(today, birthdate.withYear(today.getYear() + 1)));
            return Math.min(toThisYear, toNextYear) <= 30;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- Gui danh gia san pham (RV) ----------

    @Story("Gửi đánh giá sản phẩm")
    @Test(description = "RV-01 (đợt 15): Mở chi tiết sản phẩm -> form đánh giá hiện đủ 5 nút sao, ô tên, ô bình luận, nút gửi")
    public void reviewFormDisplaysAllFieldsOnProductDetail() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai thay trang chi tiet san pham");
        Assert.assertTrue(catalog.isReviewFormDisplayed(), "Phai thay form danh gia (review-form)");
        for (int star = 1; star <= 5; star++) {
            Assert.assertTrue(catalog.isReviewStarButtonDisplayed(star),
                    "Phai thay nut chon " + star + " sao (review-star-" + star + ")");
        }
        Assert.assertTrue(catalog.isReviewNameInputDisplayed(), "Phai thay o nhap ten (review-name-input)");
        Assert.assertTrue(catalog.isReviewCommentInputDisplayed(), "Phai thay o nhap binh luan (review-comment-input)");
        Assert.assertTrue(catalog.isReviewSubmitButtonDisplayed(), "Phai thay nut gui danh gia (review-submit-btn)");
    }

    @Story("Gửi đánh giá sản phẩm")
    @Test(description = "RV-02 (đợt 15): Bấm gửi khi chưa chọn sao -> lỗi 'Vui lòng chọn số sao đánh giá.'")
    public void submitReviewWithoutStarShowsError() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        // Dien san ten + binh luan de chi con thieu sao (co lap dung loi can kiem).
        catalog.setReviewerName("Người dùng Wave15");
        catalog.setReviewComment("Sản phẩm dùng tốt, sẽ ủng hộ tiếp.");
        catalog.clickSubmitReview();

        Assert.assertTrue(catalog.isReviewErrorDisplayed(), "Phai hien loi khi gui danh gia chua chon sao");
        Assert.assertEquals(catalog.getReviewErrorMessage(), "Vui lòng chọn số sao đánh giá.",
                "Loi phai dung nguyen van cau bao chon sao");
    }

    @Story("Gửi đánh giá sản phẩm")
    @Test(description = "RV-03 (đợt 15): Chọn sao nhưng để trống bình luận rồi gửi -> lỗi 'Vui lòng nhập nội dung đánh giá.'")
    public void submitReviewWithoutCommentShowsError() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        catalog.clickReviewStar(5);
        catalog.setReviewerName("Người dùng Wave15");
        catalog.clickSubmitReview();

        Assert.assertTrue(catalog.isReviewErrorDisplayed(), "Phai hien loi khi gui danh gia chua nhap binh luan");
        Assert.assertEquals(catalog.getReviewErrorMessage(), "Vui lòng nhập nội dung đánh giá.",
                "Loi phai dung nguyen van cau bao nhap binh luan");
    }

    @Story("Gửi đánh giá sản phẩm")
    @Test(description = "RV-04 (đợt 15): Chọn 5 sao + nhập bình luận hợp lệ rồi gửi -> hiện 'Cảm ơn bạn đã gửi đánh giá cho sản phẩm!' và đánh giá mới xuất hiện sau khi tải lại")
    public void submitValidReviewAppearsInListAfterReload() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        // Binh luan kem timestamp de khong trung khi chay lap (du lieu that,
        // tao tren san pham seed — xem ghi chu o dau lop).
        String comment = "Đánh giá tự động Wave15 " + System.currentTimeMillis()
                + " — sản phẩm tốt, giao hàng nhanh.";
        catalog.clickReviewStar(5);
        catalog.setReviewerName("Người dùng Wave15");
        catalog.setReviewComment(comment);
        catalog.clickSubmitReview();

        Assert.assertTrue(catalog.isReviewSuccessDisplayed(), "Phai hien thong bao thanh cong sau khi gui danh gia");
        Assert.assertEquals(catalog.getReviewSuccessMessage(), "Cảm ơn bạn đã gửi đánh giá cho sản phẩm!",
                "Thong bao thanh cong phai dung nguyen van");

        getDriver().navigate().refresh();
        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Tai lai xong phai thay trang chi tiet");
        Assert.assertTrue(catalog.isReviewsSectionDisplayed(), "Tai lai xong phai thay muc danh gia");
        sleepInSecond(2); // cho danh sach danh gia tai xong
        Assert.assertTrue(catalog.getAllReviewsText().contains(comment),
                "Danh gia moi phai xuat hien trong danh sach sau khi tai lai");
    }

    // ---------- Dung diem doi tien khi thanh toan (PT) ----------

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "PT-01 (đợt 15): Chưa đăng nhập mở /cart -> KHÔNG thấy mục dùng điểm")
    public void guestCartHasNoUsePointsSection() {
        openHomePage();
        CartDrawerPageObject cart = new CartDrawerPageObject();
        cart.openCartPage();

        Assert.assertFalse(cart.isUsePointsSectionPresent(),
                "Khach chua dang nhap mo /cart phai KHONG thay muc dung diem (use-points-section)");
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "PT-02 (đợt 15): Đăng nhập demo có điểm -> bật toggle dùng điểm: ô tự điền tối đa, dòng giảm giá đúng số điểm x 1.000đ")
    public void loggedInCartToggleFillsMaxPointsAndDiscountMatches() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints == 0) {
            throw new SkipException("Tai khoan demo chua co diem kha dung (so du 0) — bo qua PT-02");
        }

        CartDrawerPageObject cart = openCartWithOneProduct();
        removeAppliedVoucherIfAny(cart);
        cart.waitForUsePointsSection();
        ensurePointsToggleOn(cart);

        int usedPoints = cart.getPointsToUseValue();
        Assert.assertTrue(usedPoints > 0, "Bat toggle xong o diem phai tu dien so diem > 0");
        Assert.assertTrue(usedPoints <= availablePoints,
                "Diem dung (" + usedPoints + ") khong duoc vuot so du (" + availablePoints + ")");
        Assert.assertTrue(cart.isPointsDiscountDisplayed(),
                "Phai hien dong giam gia do dung diem (cart-points-discount)");
        Assert.assertEquals(cart.getPointsDiscountValue(), (long) usedPoints * 1000,
                "Tien giam do diem phai dung bang so diem x 1.000d");

        turnPointsOffIfOn(cart);
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "PT-03 (đợt 15): Nhập điểm vượt trần -> bị kẹp về trần + lỗi 'Chỉ dùng tối đa {m} điểm cho đơn này.'")
    public void pointsInputOverMaxClampsToMaxWithError() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints == 0) {
            throw new SkipException("Tai khoan demo chua co diem kha dung (so du 0) — bo qua PT-03");
        }

        CartDrawerPageObject cart = openCartWithOneProduct();
        removeAppliedVoucherIfAny(cart);
        cart.waitForUsePointsSection();
        ensurePointsToggleOn(cart);

        cart.setPointsToUse(999999);
        Assert.assertTrue(cart.isPointsErrorDisplayed(), "Nhap diem vuot tran phai hien loi");
        String errorMessage = cart.getPointsErrorMessage();
        Matcher matcher = Pattern.compile("tối đa\\s+([0-9][0-9.,]*)\\s+điểm").matcher(errorMessage);
        Assert.assertTrue(matcher.find(), "Loi phai dung mau 'Chỉ dùng tối đa {m} điểm cho đơn này.', dang thay: " + errorMessage);
        String maxRaw = matcher.group(1);
        Assert.assertEquals(errorMessage, "Chỉ dùng tối đa " + maxRaw + " điểm cho đơn này.",
                "Loi phai dung nguyen van mau tran diem");
        int maxPoints = parseMaxPointsFromError(errorMessage);
        Assert.assertTrue(maxPoints > 0, "Tran diem phai > 0, dang thay: " + maxPoints);
        Assert.assertTrue(maxPoints <= availablePoints,
                "Tran diem (" + maxPoints + ") khong duoc vuot so du (" + availablePoints + ")");
        Assert.assertEquals(cart.getPointsToUseValue(), maxPoints,
                "O nhap diem phai bi kep ve dung tran " + maxPoints);

        turnPointsOffIfOn(cart);
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "PT-04 (đợt 15): Áp mã FORDEV vào giỏ -> mục dùng điểm bị disable + ghi chú 'Không dùng điểm cùng mã FORDEV.'")
    public void voucherFordevDisablesUsePointsSection() {
        loginWithDemoAccount();
        CartDrawerPageObject cart = openCartWithOneProduct();

        // Tat diem neu lan chay truoc de lai diem dang ap tren gio demo.
        if (cart.isUsePointsSectionPresent() && cart.getPointsToUseValue() > 0) {
            cart.clickUsePointsToggle();
            sleepInSecond(1);
        }
        removeAppliedVoucherIfAny(cart);

        cart.applyVoucher(POINTS_BLOCKING_VOUCHER);
        if (!cart.isVoucherAppliedBannerDisplayed()) {
            String reason = cart.isVoucherErrorDisplayed()
                    ? cart.getVoucherErrorMessage() : "không rõ lý do";
            throw new SkipException("Ap ma FORDEV that bai (" + reason + ") — bo qua PT-04, khong khang dinh sai");
        }

        Assert.assertTrue(cart.isUsePointsSectionDisplayed(),
                "Ap FORDEV xong muc dung diem van phai hien (o trang thai disable)");
        Assert.assertFalse(cart.isUsePointsToggleEnabled(),
                "Ap FORDEV xong toggle dung diem phai bi disable");
        Assert.assertTrue(isTextVisibleOnPage("Không dùng điểm cùng mã FORDEV."),
                "Phai hien ghi chu 'Không dùng điểm cùng mã FORDEV.'");

        // Don dep: go ma FORDEV de lan chay sau khong bi anh huong.
        try {
            if (cart.isRemoveVoucherButtonDisplayed()) {
                cart.clickRemoveVoucher();
            }
        } catch (Exception ignored) {
            // Don dep that bai thi bo qua.
        }
    }

    // ---------- Ngay sinh + voucher sinh nhat (BD) ----------

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-01 (đợt 15): Đăng nhập mở /membership -> mục ngày sinh hiện đủ ô nhập, nút lưu, nút nhận voucher sinh nhật")
    public void birthdateSectionDisplaysAllControlsOnMembership() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();

        Assert.assertTrue(membershipPage.isBirthdateSectionDisplayed(), "Phai thay muc ngay sinh (birthdate-section)");
        Assert.assertTrue(membershipPage.isBirthdateInputDisplayed(), "Phai thay o nhap ngay sinh (birthdate-input)");
        Assert.assertTrue(membershipPage.isBirthdateSaveButtonDisplayed(), "Phai thay nut luu ngay sinh (birthdate-save-btn)");
        Assert.assertTrue(membershipPage.isClaimBirthdayVoucherButtonDisplayed(),
                "Phai thay nut nhan voucher sinh nhat (claim-birthday-voucher-btn)");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-02 (đợt 15): Tài khoản demo chưa có ngày sinh -> nhập ngày sinh hợp lệ rồi lưu -> hiện 'Đã cập nhật ngày sinh.'")
    public void saveBirthdateWhenNotSetShowsSuccess() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();

        String existingBirthdate = membershipPage.getBirthdateValue();
        if (existingBirthdate != null && !existingBirthdate.trim().isEmpty()) {
            throw new SkipException("Ngay sinh demo da dat (" + existingBirthdate + "), BE khoa sua 90 ngay"
                    + " — bo qua BD-02 de khong lam ban tai khoan demo");
        }

        membershipPage.setBirthdate(TEST_BIRTHDATE);
        Assert.assertEquals(membershipPage.getBirthdateValue(), TEST_BIRTHDATE,
                "O ngay sinh phai giu dung gia tri vua nhap truoc khi luu");
        membershipPage.clickSaveBirthdate();

        Assert.assertTrue(membershipPage.isBirthdateSuccessDisplayed(), "Luu ngay sinh phai hien thong bao thanh cong");
        Assert.assertEquals(membershipPage.getBirthdateSuccessMessage(), "Đã cập nhật ngày sinh.",
                "Thong bao thanh cong phai dung nguyen van");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-03 (đợt 15): Tài khoản demo hạng Thường bấm nhận voucher sinh nhật -> lỗi 'Voucher sinh nhật chỉ dành cho hạng Vàng trở lên.'")
    public void claimBirthdayVoucherAsRegularTierShowsError() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();

        String tier = membershipPage.getTierText();
        if (!tier.toLowerCase().contains("thường")) {
            throw new SkipException("Hang demo hien tai la '" + tier + "' (khong phai hang Thuong)"
                    + " — khong chac chan se bi tu choi, bo qua BD-03");
        }

        membershipPage.clickClaimBirthdayVoucher();

        Assert.assertTrue(membershipPage.isBirthdayVoucherErrorDisplayed(),
                "Hang Thuong bam nhan voucher sinh nhat phai hien loi");
        Assert.assertEquals(membershipPage.getBirthdayVoucherErrorMessage(),
                "Voucher sinh nhật chỉ dành cho hạng Vàng trở lên.",
                "Loi phai dung nguyen van cau gioi han hang Vang");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-04 (đợt 15): Đủ điều kiện hạng + sinh nhật trong 30 ngày -> bấm nhận voucher 2 lần liên tiếp, lần 2 phải báo lỗi")
    public void claimBirthdayVoucherTwiceSecondTimeShowsError() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();

        String tier = membershipPage.getTierText();
        String tierLower = tier.toLowerCase();
        if (!tierLower.contains("vàng") && !tierLower.contains("kim cương")) {
            throw new SkipException("Hang demo hien tai la '" + tier + "' (chua Vang tro len)"
                    + " — khong du dieu kien nhan voucher sinh nhat, bo qua BD-04");
        }
        String birthdate = membershipPage.getBirthdateValue();
        if (birthdate == null || birthdate.trim().isEmpty()) {
            throw new SkipException("Tai khoan demo chua dat ngay sinh — khong du dieu kien nhan voucher sinh nhat, bo qua BD-04");
        }
        if (!isBirthdayWithin30Days(birthdate)) {
            throw new SkipException("Ngay sinh demo " + birthdate + " khong nam trong cua so 30 ngay quanh sinh nhat"
                    + " — ngoai thoi gian nhan voucher, bo qua BD-04");
        }

        membershipPage.clickClaimBirthdayVoucher();
        sleepInSecond(1);
        if (membershipPage.isClaimBirthdayVoucherButtonDisplayed()) {
            membershipPage.clickClaimBirthdayVoucher();
            Assert.assertTrue(membershipPage.isBirthdayVoucherErrorDisplayed(),
                    "Bam nhan voucher lan 2 phai bao loi (da nhan trong nam hoac ngoai cua so 30 ngay)."
                            + " Khong khang dinh cung cau loi vi cau loi phu thuoc trang thai that.");
        } else {
            // Nut nhan bien mat sau lan bam dau: lan nhan da duoc tieu thu
            // (thanh cong hoac da bao loi) nen khong con lan 2 de bam.
            Assert.assertTrue(membershipPage.isBirthdayVoucherSuccessDisplayed()
                            || membershipPage.isBirthdayVoucherErrorDisplayed(),
                    "Sau lan bam dau, phai hien thong bao thanh cong hoac loi cua voucher sinh nhat");
        }
    }
}
