package vn.dafl.tests.web;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.MembershipPageObject;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mo rong trang Hoi vien /membership (buoc 3, dot 15.1): 4 hang + nguong,
 * quyen loi theo hang, so du diem, lich su/voucher rong, o ngay sinh dien san,
 * quy tac 90 ngay, ngay sinh khong hop le, cac nhanh voucher sinh nhat (BD-05..09).
 *
 * Can cu chuoi hien thi that trong MembershipPage.tsx (fork v15) — luu y:
 * - 4 badge hang CHI hien nhan (Thường/Bạc/Vàng/Kim cương), KHONG hien so nguong;
 *   nguong hang TIEP THEO chi suy ra duoc tu "Chi tieu 12 thang: {spent}" +
 *   "— còn {nextGoal - spent} nữa để lên hạng {Y}" (tong 2 so = nguong hang tiep theo).
 * - formatPrice tra "2.000.000 ₫" (ky tu ₫) nen cac assert tien chi khang dinh
 *   phan so, khong khang dinh hau to "đ"/"₫".
 * - Nhanh "— sắp hết hạn: {n} điểm" chi hien khi co diem het han trong 30 ngay toi —
 *   du lieu nay khong tao duoc qua UI nen chi khang dinh khi no xuat hien.
 * - Cac test BD dung chung tai khoan demo nen chi bam nut nhan voucher khi du dieu kien
 *   VA chua co voucher sinh nhat trong nam; neu BD-04 (Wave15FeaturesTest) da nhan
 *   thi BD-05/BD-08 tu skip, khong tao them voucher sinh nhat that (1 lan/nam).
 */
@Feature("Hội viên")
public class MembershipExtendedTest extends BaseTest {

    private static final String[] TIER_KEYS = {"Member", "Silver", "Gold", "Diamond"};
    private static final String[] TIER_LABELS = {"Thường", "Bạc", "Vàng", "Kim cương"};

    /** Mo /membership bang tai khoan demo. */
    private MembershipPageObject openDemoMembership() {
        HomePageObject homePage = loginWithDemoAccount();
        return homePage.openMembershipPage();
    }

    /** Dang ky tai khoan moi roi mo /membership (tai khoan sach: 0 diem, 0 voucher, chua co ngay sinh). */
    private MembershipPageObject openFreshMembership() {
        HomePageObject homePage = registerAndLogin("Khách Hội Viên", randomEmail(),
                randomPhone(), GlobalConstants.DEMO_PASSWORD);
        return homePage.openMembershipPage();
    }

    /** Nhan hang hien tai (vd "Thường") = nhan cua badge co them " • Hạng của bạn". */
    private String getCurrentTierLabel(MembershipPageObject membershipPage) {
        for (int i = 0; i < TIER_KEYS.length; i++) {
            String badgeText = membershipPage.getTierBadgeText(TIER_KEYS[i]);
            if (badgeText.contains("Hạng của bạn")) {
                return TIER_LABELS[i];
            }
        }
        throw new AssertionError("Khong tim thay badge hang nao danh dau 'Hạng của bạn'");
    }

    /** Sinh nhat nam nay / nam sau co nam trong 30 ngay quanh hom nay khong (mau BD-04). */
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

    /** Trich so tien dau tien dung sau 1 cum tu co dinh trong text (vd sau "Chi tiêu 12 tháng:"). */
    private long parseAmountAfter(String text, String anchorRegex) {
        Matcher matcher = Pattern.compile(anchorRegex + "\\s*([0-9][0-9.]*)").matcher(text);
        if (!matcher.find()) {
            throw new AssertionError("Khong tim thay so tien sau '" + anchorRegex + "' trong: " + text);
        }
        return parseMoney(matcher.group(1));
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_FUNC_002 + TC_SCR10_UI_012: Trang hội viên hiện đủ 4 badge hạng (Thường/Bạc/Vàng/Kim cương), badge hạng hiện tại có ' • Hạng của bạn', ngưỡng hạng tiếp theo suy ra từ chi tiêu 12 tháng + số còn thiếu")
    public void membershipTierBadgesAndNextTierThreshold() {
        MembershipPageObject membershipPage = openDemoMembership();

        Assert.assertEquals(membershipPage.getTierBadgeCount(), 4,
                "Phai co du 4 badge hang (Member/Silver/Gold/Diamond)");
        int markedCount = 0;
        for (int i = 0; i < TIER_KEYS.length; i++) {
            String badgeText = membershipPage.getTierBadgeText(TIER_KEYS[i]);
            Assert.assertTrue(badgeText.contains(TIER_LABELS[i]),
                    "Badge '" + TIER_KEYS[i] + "' phai hien nhan '" + TIER_LABELS[i] + "', dang thay: " + badgeText);
            if (badgeText.contains("Hạng của bạn")) {
                markedCount++;
            }
        }
        Assert.assertEquals(markedCount, 1,
                "Chi dung 1 badge hang hien tai duoc danh dau ' • Hạng của bạn'");

        // Nguong hang tiep theo = chi tieu 12 thang + so con thieu (FE khong hien truc tiep so nguong).
        String tierText = membershipPage.getTierText();
        String currentLabel = getCurrentTierLabel(membershipPage);
        if ("Kim cương".equals(currentLabel)) {
            Assert.assertTrue(tierText.contains("bạn đang ở hạng cao nhất"),
                    "Da o hang Kim cuong phai hien '— bạn đang ở hạng cao nhất', dang thay: " + tierText);
            return;
        }
        long spent = parseAmountAfter(tierText, "Chi tiêu 12 tháng:");
        long remaining = parseAmountAfter(tierText, "còn");
        Matcher nextTierMatcher = Pattern.compile("nữa để lên hạng\\s*([^\\n]+)").matcher(tierText);
        Assert.assertTrue(nextTierMatcher.find(),
                "Phai hien cau '— còn ... nữa để lên hạng ...', dang thay: " + tierText);
        String nextLabel = nextTierMatcher.group(1).trim();
        long expectedThreshold;
        if ("Bạc".equals(nextLabel)) {
            expectedThreshold = 2000000L;
        } else if ("Vàng".equals(nextLabel)) {
            expectedThreshold = 5000000L;
        } else if ("Kim cương".equals(nextLabel)) {
            expectedThreshold = 15000000L;
        } else {
            throw new AssertionError("Hang tiep theo khong nhan dien duoc: '" + nextLabel + "'");
        }
        Assert.assertEquals(spent + remaining, expectedThreshold,
                "Chi tieu 12 thang (" + spent + ") + so con thieu (" + remaining
                        + ") phai bang nguong hang '" + nextLabel + "' (" + expectedThreshold + ")");
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_FUNC_003: Quyền lợi hiển thị đúng tỷ lệ tích điểm của hạng hiện tại ('Tích 1 điểm cho mỗi {n}đ chi tiêu.')")
    public void membershipBenefitsShowCurrentTierEarnRate() {
        MembershipPageObject membershipPage = openDemoMembership();
        String tierLabel = getCurrentTierLabel(membershipPage);

        String expectedRate;
        if ("Thường".equals(tierLabel)) {
            expectedRate = "20.000";
        } else if ("Bạc".equals(tierLabel)) {
            expectedRate = "15.000";
        } else if ("Vàng".equals(tierLabel)) {
            expectedRate = "10.000";
        } else {
            expectedRate = "8.000";
        }
        String benefits = membershipPage.getBenefitsText();
        Assert.assertTrue(benefits.contains("Tích 1 điểm cho mỗi " + expectedRate + "đ chi tiêu."),
                "Hang '" + tierLabel + "' phai hien 'Tích 1 điểm cho mỗi " + expectedRate
                        + "đ chi tiêu.', dang thay: " + benefits);
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_FUNC_004: Subtitle và quyền lợi hiển thị đúng giá trị quy đổi '1 điểm = 1.000đ khi thanh toán' và hạn điểm 12 tháng")
    public void membershipBenefitsShowPointsValueAndExpiry() {
        MembershipPageObject membershipPage = openDemoMembership();

        String subtitle = membershipPage.getSubtitleText();
        Assert.assertTrue(subtitle.contains("Hạng hội viên, số dư điểm và ưu đãi của bạn — 1 điểm = 1.000đ khi thanh toán."),
                "Subtitle phai dung nguyen van, dang thay: " + subtitle);

        String benefits = membershipPage.getBenefitsText();
        Assert.assertTrue(benefits.contains("1 điểm = 1.000đ khi thanh toán; điểm hết hạn sau 12 tháng."),
                "Quyen loi phai hien dung cau gia tri diem + han 12 thang, dang thay: " + benefits);
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_FUNC_005: Quyền lợi hiển thị đúng mức dùng điểm tối đa của hạng hiện tại ('Dùng điểm giảm tối đa {n}% giá trị đơn hàng.')")
    public void membershipBenefitsShowMaxPointsPercentForCurrentTier() {
        MembershipPageObject membershipPage = openDemoMembership();
        String tierLabel = getCurrentTierLabel(membershipPage);

        String expectedPercent;
        if ("Thường".equals(tierLabel)) {
            expectedPercent = "10";
        } else if ("Bạc".equals(tierLabel)) {
            expectedPercent = "15";
        } else if ("Vàng".equals(tierLabel)) {
            expectedPercent = "20";
        } else {
            expectedPercent = "30";
        }
        String benefits = membershipPage.getBenefitsText();
        Assert.assertTrue(benefits.contains("Dùng điểm giảm tối đa " + expectedPercent + "% giá trị đơn hàng."),
                "Hang '" + tierLabel + "' phai hien 'Dùng điểm giảm tối đa " + expectedPercent
                        + "% giá trị đơn hàng.', dang thay: " + benefits);
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_FUNC_006: Quyền lợi hiển thị đúng ưu đãi freeship của hạng hiện tại")
    public void membershipBenefitsShowFreeShipForCurrentTier() {
        MembershipPageObject membershipPage = openDemoMembership();
        String tierLabel = getCurrentTierLabel(membershipPage);

        String expected;
        if ("Thường".equals(tierLabel)) {
            expected = "Chưa có ưu đãi freeship riêng theo hạng.";
        } else if ("Bạc".equals(tierLabel)) {
            expected = "Miễn phí vận chuyển cho đơn từ 199.000";
        } else if ("Vàng".equals(tierLabel)) {
            expected = "Miễn phí vận chuyển cho đơn từ 99.000";
        } else {
            expected = "Miễn phí vận chuyển mọi đơn hàng.";
        }
        String benefits = membershipPage.getBenefitsText();
        Assert.assertTrue(benefits.contains(expected),
                "Hang '" + tierLabel + "' phai hien quyen loi freeship '" + expected
                        + "', dang thay: " + benefits);
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_FUNC_007: Số dư điểm hiển thị dạng '{điểm} điểm' kèm 'Tương đương {giá trị} khi thanh toán' đúng 1 điểm = 1.000đ")
    public void membershipPointsBalanceShowsPointsAndConvertedValue() {
        MembershipPageObject membershipPage = openDemoMembership();

        String balance = membershipPage.getPointsBalanceText().trim();
        Assert.assertTrue(balance.matches("^[\\d.]+ điểm$"),
                "So du diem phai dang '{diem} diem', dang thay: '" + balance + "'");

        int availablePoints = membershipPage.getAvailablePoints();
        String tierText = membershipPage.getTierText();
        long converted = parseAmountAfter(tierText, "Tương đương");
        Assert.assertEquals(converted, (long) availablePoints * 1000,
                "Gia tri quy doi phai dung bang so du diem x 1.000d (so du: " + availablePoints + ")");
        Assert.assertTrue(tierText.contains("khi thanh toán"),
                "Khoi so du phai hien 'Tương đương ... khi thanh toán', dang thay: " + tierText);

        // Nhanh diem sap het han chi hien khi co diem het han trong 30 ngay toi (khong tao duoc qua UI):
        // neu xuat hien thi khang dinh dung mau, neu khong thi bo qua nhanh nay.
        if (tierText.contains("sắp hết hạn")) {
            Assert.assertTrue(tierText.matches("(?s).*— sắp hết hạn: [\\d.]+ điểm.*"),
                    "Dong diem sap het han phai dung mau '— sắp hết hạn: {n} điểm', dang thay: " + tierText);
        }
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_UI_008: Thanh tiến độ lên hạng hiển thị '— còn {X} nữa để lên hạng {Y}' (hoặc '— bạn đang ở hạng cao nhất' khi đã Kim cương)")
    public void membershipProgressToNextTierShowsRemainingOrTopTier() {
        MembershipPageObject membershipPage = openDemoMembership();
        String tierText = membershipPage.getTierText();
        String currentLabel = getCurrentTierLabel(membershipPage);

        boolean hasRemaining = tierText.contains("nữa để lên hạng");
        boolean isTopTier = tierText.contains("bạn đang ở hạng cao nhất");
        Assert.assertTrue(hasRemaining ^ isTopTier,
                "Phai hien dung 1 trong 2 cau tien do (con thieu / hang cao nhat), dang thay: " + tierText);
        if ("Kim cương".equals(currentLabel)) {
            Assert.assertTrue(isTopTier, "Da o hang Kim cuong phai hien cau hang cao nhat");
            Assert.assertFalse(membershipPage.isProgressBarDisplayed(),
                    "Da o hang cao nhat thi khong hien thanh tien do");
        } else {
            Assert.assertTrue(hasRemaining, "Chua o hang cao nhat phai hien cau con thieu de len hang");
            Assert.assertTrue(membershipPage.isProgressBarDisplayed(),
                    "Chua o hang cao nhat phai hien thanh tien do (role=progressbar)");
        }
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_UI_010: Tài khoản mới chưa có giao dịch điểm -> lịch sử điểm hiển thị 'Chưa có giao dịch điểm nào.'")
    public void membershipEmptyHistoryShowsEmptyMessageForNewAccount() {
        MembershipPageObject membershipPage = openFreshMembership();

        String history = membershipPage.getHistoryText();
        Assert.assertTrue(history.contains("Chưa có giao dịch điểm nào."),
                "Tai khoan moi phai hien 'Chưa có giao dịch điểm nào.', dang thay: " + history);
    }

    @Story("Trang hội viên")
    @Test(description = "TC_SCR10_FUNC_011: Danh sách voucher của tôi hiển thị khi có voucher; tài khoản mới chưa có voucher -> hiển thị 'Bạn chưa có voucher nào.'")
    public void membershipVouchersListOrEmptyState() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject demoPage = homePage.openMembershipPage();
        int demoVoucherCount = demoPage.getVoucherCount();
        if (demoVoucherCount > 0) {
            String pageText = demoPage.getMembershipPageText();
            Assert.assertTrue(pageText.contains("Ưu đãi của tôi"),
                    "Khoi voucher phai co tieu de 'Ưu đãi của tôi'");
        } else {
            Assert.assertTrue(demoPage.getMembershipPageText().contains("Bạn chưa có voucher nào."),
                    "Tai khoan demo khong co voucher thi phai hien 'Bạn chưa có voucher nào.'");
        }

        // Dang xuat demo truoc khi dang ky tai khoan moi (AuthModal chi mo duoc khi chua dang nhap).
        homePage.logout();
        MembershipPageObject freshPage = openFreshMembership();
        Assert.assertEquals(freshPage.getVoucherCount(), 0,
                "Tai khoan moi phai co 0 voucher");
        Assert.assertTrue(freshPage.getMembershipPageText().contains("Bạn chưa có voucher nào."),
                "Tai khoan moi phai hien 'Bạn chưa có voucher nào.'");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "TC_SCR10_UI_017: Tài khoản đã lưu ngày sinh -> mở lại trang Hội viên, ô ngày sinh điền sẵn đúng ngày đã lưu (YYYY-MM-DD)")
    public void membershipBirthdateInputPrefilledWithSavedDate() {
        MembershipPageObject membershipPage = openFreshMembership();

        membershipPage.setBirthdate("2000-05-15");
        membershipPage.clickSaveBirthdate();
        Assert.assertTrue(membershipPage.isBirthdateSuccessDisplayed(),
                "Luu ngay sinh lan dau phai hien thong bao thanh cong");
        Assert.assertEquals(membershipPage.getBirthdateSuccessMessage(), "Đã cập nhật ngày sinh.",
                "Thong bao thanh cong phai dung nguyen van");

        getDriver().navigate().refresh();
        new MembershipPageObject().waitForMembershipLoaded();
        Assert.assertEquals(new MembershipPageObject().getBirthdateValue(), "2000-05-15",
                "Tai lai trang xong o ngay sinh phai dien san dung ngay da luu (2000-05-15)");
        Assert.assertTrue(new MembershipPageObject().isBirthdateSaveButtonDisplayed(),
                "Phai thay nut luu ngay sinh (birthdate-save-btn) ben canh o ngay sinh");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-09 (TC_SCR10_VAL_019): Đổi ngày sinh lần 2 trong 90 ngày -> lỗi 'Ngày sinh chỉ được sửa 1 lần trong 90 ngày để chống trục lợi voucher sinh nhật.' và ngày lưu vẫn là ngày cũ")
    public void membershipSaveBirthdateTwiceWithin90DaysShowsError() {
        MembershipPageObject membershipPage = openFreshMembership();

        membershipPage.setBirthdate("2000-05-15");
        membershipPage.clickSaveBirthdate();
        Assert.assertTrue(membershipPage.isBirthdateSuccessDisplayed(),
                "Luu ngay sinh lan 1 phai thanh cong truoc khi thu doi lan 2");

        membershipPage.setBirthdate("2001-06-20");
        membershipPage.clickSaveBirthdate();

        Assert.assertTrue(membershipPage.isBirthdateErrorDisplayed(),
                "Doi ngay sinh lan 2 trong 90 ngay phai hien loi");
        Assert.assertEquals(membershipPage.getBirthdateErrorMessage(),
                "Ngày sinh chỉ được sửa 1 lần trong 90 ngày để chống trục lợi voucher sinh nhật.",
                "Loi 90 ngay phai dung nguyen van cau BE");
        // FE giu gia tri vua nhap bi tu choi trong o nhap cho toi khi tai lai trang,
        // nen kiem tra ngay DA LUU bang cach tai lai trang.
        getDriver().navigate().refresh();
        new MembershipPageObject().waitForMembershipLoaded();
        Assert.assertEquals(new MembershipPageObject().getBirthdateValue(), "2000-05-15",
                "Ngay sinh da luu phai van la ngay cu (2000-05-15) sau khi doi lan 2 bi tu choi");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "TC_SCR10_VAL_025: Lưu ngày sinh không hợp lệ (tuổi dưới 10 hoặc trên 120) -> lỗi 'Ngày sinh không hợp lệ.' và ngày lưu vẫn trống")
    public void membershipSaveInvalidBirthdateShowsError() {
        MembershipPageObject membershipPage = openFreshMembership();

        membershipPage.setBirthdate("2022-01-01");
        membershipPage.clickSaveBirthdate();
        Assert.assertTrue(membershipPage.isBirthdateErrorDisplayed(),
                "Luu ngay sinh tuoi duoi 10 phai hien loi");
        Assert.assertEquals(membershipPage.getBirthdateErrorMessage(), "Ngày sinh không hợp lệ.",
                "Loi ngay sinh khong hop le phai dung nguyen van");

        membershipPage.setBirthdate("1900-01-01");
        membershipPage.clickSaveBirthdate();
        Assert.assertTrue(membershipPage.isBirthdateErrorDisplayed(),
                "Luu ngay sinh tuoi tren 120 phai hien loi");
        Assert.assertEquals(membershipPage.getBirthdateErrorMessage(), "Ngày sinh không hợp lệ.",
                "Loi ngay sinh khong hop le phai dung nguyen van");

        getDriver().navigate().refresh();
        new MembershipPageObject().waitForMembershipLoaded();
        Assert.assertEquals(new MembershipPageObject().getBirthdateValue(), "",
                "Ngay sinh da luu phai van trong sau khi 2 lan luu khong hop le bi tu choi");
    }

    // ---------- Voucher sinh nhat (BD-05..BD-09) ----------

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-05 (TC_SCR10_FUNC_020): Đủ điều kiện hạng Vàng trở lên + sinh nhật trong 30 ngày + chưa nhận năm nay -> bấm nhận voucher sinh nhật thành công, hiện 'Đã phát voucher sinh nhật (giảm 15%, tối đa 150.000đ, hạn 30 ngày).' và voucher mới xuất hiện trong danh sách")
    public void claimBirthdayVoucherSuccessShowsMessageAndNewVoucher() {
        MembershipPageObject membershipPage = openDemoMembership();

        String tier = membershipPage.getTierText();
        String tierLower = tier.toLowerCase();
        if (!tierLower.contains("vàng") && !tierLower.contains("kim cương")) {
            throw new SkipException("Hang demo hien tai khong phai Vang tro len"
                    + " — khong du dieu kien nhan voucher sinh nhat, bo qua BD-05");
        }
        String birthdate = membershipPage.getBirthdateValue();
        if (birthdate == null || birthdate.trim().isEmpty()) {
            throw new SkipException("Tai khoan demo chua dat ngay sinh"
                    + " — khong du dieu kien nhan voucher sinh nhat, bo qua BD-05");
        }
        if (!isBirthdayWithin30Days(birthdate)) {
            throw new SkipException("Ngay sinh demo " + birthdate + " khong nam trong cua so 30 ngay quanh sinh nhat"
                    + " — ngoai thoi gian nhan voucher, bo qua BD-05");
        }
        if (membershipPage.getMembershipPageText().contains("Voucher sinh nhật")) {
            throw new SkipException("Tai khoan demo da co voucher sinh nhat trong nam nay (BD-04 da nhan)"
                    + " — bo qua BD-05 de khong tao them voucher sinh nhat that (1 lan/nam)");
        }

        int vouchersBefore = membershipPage.getVoucherCount();
        membershipPage.clickClaimBirthdayVoucher();

        Assert.assertTrue(membershipPage.isBirthdayVoucherSuccessDisplayed(),
                "Nhan voucher sinh nhat thanh cong phai hien thong bao thanh cong");
        Assert.assertEquals(membershipPage.getBirthdayVoucherSuccessMessage(),
                "Đã phát voucher sinh nhật (giảm 15%, tối đa 150.000đ, hạn 30 ngày).",
                "Thong bao thanh cong phai dung nguyen van cau BE");
        Assert.assertEquals(membershipPage.getVoucherCount(), vouchersBefore + 1,
                "Danh sach voucher phai them 1 voucher moi sau khi nhan thanh cong");
        Assert.assertTrue(membershipPage.getMembershipPageText().contains("Voucher sinh nhật"),
                "Danh sach voucher phai co voucher sinh nhat moi (mo ta 'Voucher sinh nhật ...')");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-06 (TC_SCR10_FUNC_022): Hạng Vàng trở lên nhưng chưa nhập ngày sinh -> bấm nhận voucher sinh nhật báo lỗi 'Vui lòng cập nhật ngày sinh trong hồ sơ hội viên trước.'")
    public void claimBirthdayVoucherWithoutBirthdateShowsError() {
        MembershipPageObject membershipPage = openDemoMembership();

        String tier = membershipPage.getTierText();
        String tierLower = tier.toLowerCase();
        if (!tierLower.contains("vàng") && !tierLower.contains("kim cương")) {
            throw new SkipException("Hang demo hien tai khong phai Vang tro len"
                    + " — khong chac chan se gap nhanh thieu ngay sinh, bo qua BD-06");
        }
        String birthdate = membershipPage.getBirthdateValue();
        if (birthdate != null && !birthdate.trim().isEmpty()) {
            throw new SkipException("Tai khoan demo da co ngay sinh (" + birthdate + ")"
                    + " — khong gap nhanh chua co ngay sinh, bo qua BD-06");
        }

        membershipPage.clickClaimBirthdayVoucher();

        Assert.assertTrue(membershipPage.isBirthdayVoucherErrorDisplayed(),
                "Hang Vang tro len chua co ngay sinh bam nhan voucher phai hien loi");
        Assert.assertEquals(membershipPage.getBirthdayVoucherErrorMessage(),
                "Vui lòng cập nhật ngày sinh trong hồ sơ hội viên trước.",
                "Loi phai dung nguyen van cau BE");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-07 (TC_SCR10_FUNC_023): Hạng Vàng trở lên, đã có ngày sinh nhưng ngoài 30 ngày quanh ngày sinh -> bấm nhận voucher sinh nhật báo lỗi 'Voucher sinh nhật chỉ nhận được trong 30 ngày quanh ngày sinh của bạn.'")
    public void claimBirthdayVoucherOutsideBirthdayWindowShowsError() {
        MembershipPageObject membershipPage = openDemoMembership();

        String tier = membershipPage.getTierText();
        String tierLower = tier.toLowerCase();
        if (!tierLower.contains("vàng") && !tierLower.contains("kim cương")) {
            throw new SkipException("Hang demo hien tai khong phai Vang tro len"
                    + " — khong chac chan se gap nhanh ngoai cua so 30 ngay, bo qua BD-07");
        }
        String birthdate = membershipPage.getBirthdateValue();
        if (birthdate == null || birthdate.trim().isEmpty()) {
            throw new SkipException("Tai khoan demo chua dat ngay sinh"
                    + " — khong gap nhanh ngoai cua so 30 ngay, bo qua BD-07");
        }
        if (isBirthdayWithin30Days(birthdate)) {
            throw new SkipException("Ngay sinh demo " + birthdate + " dang nam trong cua so 30 ngay quanh sinh nhat"
                    + " — khong gap nhanh ngoai cua so, bo qua BD-07");
        }

        membershipPage.clickClaimBirthdayVoucher();

        Assert.assertTrue(membershipPage.isBirthdayVoucherErrorDisplayed(),
                "Nhan voucher ngoai cua so 30 ngay phai hien loi");
        Assert.assertEquals(membershipPage.getBirthdayVoucherErrorMessage(),
                "Voucher sinh nhật chỉ nhận được trong 30 ngày quanh ngày sinh của bạn.",
                "Loi phai dung nguyen van cau BE");
    }

    @Story("Ngày sinh và voucher sinh nhật")
    @Test(description = "BD-08 (TC_SCR10_FUNC_024): Hạng Vàng trở lên đã nhận voucher sinh nhật trong năm nay -> bấm nhận lần nữa báo lỗi 'Bạn đã nhận voucher sinh nhật năm nay rồi.'")
    public void claimBirthdayVoucherSecondTimeThisYearShowsError() {
        MembershipPageObject membershipPage = openDemoMembership();

        String tier = membershipPage.getTierText();
        String tierLower = tier.toLowerCase();
        if (!tierLower.contains("vàng") && !tierLower.contains("kim cương")) {
            throw new SkipException("Hang demo hien tai khong phai Vang tro len"
                    + " — khong chac chan se gap nhanh da nhan roi, bo qua BD-08");
        }
        String birthdate = membershipPage.getBirthdateValue();
        if (birthdate == null || birthdate.trim().isEmpty()) {
            throw new SkipException("Tai khoan demo chua dat ngay sinh"
                    + " — khong du dieu kien nhan voucher sinh nhat, bo qua BD-08");
        }
        if (!isBirthdayWithin30Days(birthdate)) {
            throw new SkipException("Ngay sinh demo " + birthdate + " khong nam trong cua so 30 ngay quanh sinh nhat"
                    + " — ngoai thoi gian nhan voucher, bo qua BD-08");
        }
        if (!membershipPage.getMembershipPageText().contains("Voucher sinh nhật")) {
            throw new SkipException("Tai khoan demo chua co voucher sinh nhat trong nam nay"
                    + " — nhanh 'da nhan roi' chua kiem duoc; khong tu bam nhan o test nay"
                    + " (viec nhan lan dau thuoc ve BD-04/TC_SCR10_FUNC_020)");
        }

        membershipPage.clickClaimBirthdayVoucher();

        Assert.assertTrue(membershipPage.isBirthdayVoucherErrorDisplayed(),
                "Nhan voucher sinh nhat lan 2 trong nam phai hien loi");
        Assert.assertEquals(membershipPage.getBirthdayVoucherErrorMessage(),
                "Bạn đã nhận voucher sinh nhật năm nay rồi.",
                "Loi phai dung nguyen van cau BE");
    }

}
