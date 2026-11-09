package vn.dafl.commons;

/**
 * Hang so dung chung cho toan bo framework.
 * Du lieu demo (tai khoan, voucher) va thong diep loi lay tu du lieu seed +
 * Backend/Frontend that cua SUT DAFL (ban CN-01..CN-08).
 */
public class GlobalConstants {

    public static final long SHORT_TIMEOUT = 5;
    public static final long LONG_TIMEOUT = 30;

    public static final String DEFAULT_APP_URL = "http://localhost:3000";

    /** Trang quan tri rieng (build VITE_ADMIN_ONLY=true, service dafl-admin): chi render
     *  trang Admin + form dang nhap co ban; o day nhap "admin" cung dang nhap duoc. */
    public static final String ADMIN_APP_URL = "http://localhost:3001";

    // Tai khoan demo duoc seed san trong CSDL cua SUT
    public static final String DEMO_EMAIL = "demo@dafl.vn";
    public static final String DEMO_PASSWORD = "Password123!";
    public static final String DEMO_FULL_NAME = "Nguyễn Văn Demo";
    public static final String DEMO_PHONE = "0987654321";

    // Tai khoan Super Admin duy nhat cua SUT (seed trong DbInitializer.cs, chi tao khi
    // bang Users trong; DbInitializer con tu tao neu thieu — xem khoi 1c).
    // Mat khau mac dinh: Admin@123 (dang nhap duoc bang ten "admin" o trang
    // quan tri rieng localhost:3001). Chi dung cho test DOC (GET) o trang
    // /admin - KHONG doi mat khau, KHONG sua/xoa du lieu qua test.
    public static final String ADMIN_EMAIL = "admin@dafl.vn";
    public static final String ADMIN_PASSWORD = "Admin@123";

    // Dia chi giao hang mau (du lieu dia gioi tinh trong FE: vietnam-provinces.json)
    public static final String SHIPPING_PROVINCE = "Thành phố Hà Nội";
    public static final String SHIPPING_DISTRICT = "Quận Ba Đình";
    public static final String SHIPPING_WARD = "Phường Phúc Xá";
    public static final String SHIPPING_DETAIL = "Số 1 Đại Cồ Việt";

    // Ma giam gia duoc seed san trong CSDL cua SUT (DbInitializer.cs)
    public static final String VOUCHER_PERCENT_10 = "DAFL10"; // giam 10%, don toi thieu 100.000d
    public static final String VOUCHER_FIXED_20K = "DAFL20";  // giam thang 20.000d, don toi thieu 100.000d
    public static final String VOUCHER_FREESHIP = "FREESHIP"; // mien phi ship, don toi thieu 200.000d
    public static final String VOUCHER_VIP_50K = "VIP50K";    // giam 50.000d, don toi thieu 500.000d

    // Nguong tien cua SUT (ban chot CN + override O1)
    public static final long SHIPPING_FEE = 25_000;             // phi ship co dinh
    public static final long FREE_SHIPPING_THRESHOLD = 299_000; // mien phi ship tu muc nay

    // Thong diep thuc te tra ve tu Backend/Frontend cua SUT
    public static final String LOGIN_ERROR_MESSAGE = "Email hoặc mật khẩu không đúng";
    public static final String VOUCHER_ERROR_MESSAGE = "Mã giảm giá không tồn tại hoặc đã hết hiệu lực";
    public static final String VOUCHER_MIN_ORDER_MESSAGE = "Đơn hàng tối thiểu cần đạt";
    public static final String REGISTER_SUCCESS_MESSAGE = "Đăng ký thành công";
    public static final String SESSION_EXPIRED_MESSAGE = "Phiên đăng nhập đã hết hạn";
    public static final String PROFILE_SUCCESS_MESSAGE = "Cập nhật thành công";
    public static final String DUPLICATE_EMAIL_MESSAGE = "Email đã được sử dụng";
    public static final String DUPLICATE_PHONE_MESSAGE = "Số điện thoại đã được sử dụng";
    public static final String CANCEL_SUCCESS_MESSAGE = "Hủy đơn thành công";
    public static final String ORDER_STATUS_PENDING = "Chờ xác nhận";
    public static final String ORDER_STATUS_CANCELLED = "Đã hủy";

    /** Nhan trang thai don Pending rieng o TRANG QUAN TRI /admin (khac nhan o web
     *  ban hang: don Pending o admin hien 'Chờ xử lý', o shop hien 'Chờ xác nhận').
     *  Dung cho select loc 'admin-order-status-filter' (chon theo text hien thi). */
    public static final String ADMIN_ORDER_STATUS_PENDING_LABEL = "Chờ xử lý";

    // Trang quan tri /admin (chi Super Admin; tai khoan thuong thay trang 403 kem huong dan cap quyen)
    public static final String ADMIN_FORBIDDEN_MESSAGE = "Bạn không có quyền truy cập trang quản trị";
    public static final String ADMIN_FORBIDDEN_GUIDE_HEADING = "Cách cấp quyền Super Admin";
    // Dang ky ban tin o footer (POST /api/newsletter/subscribe, luu that vao DB)
    public static final String NEWSLETTER_SUCCESS_MESSAGE = "Cảm ơn bạn đã đăng ký nhận bản tin DAFL.";
    public static final String NEWSLETTER_INVALID_EMAIL_MESSAGE = "Vui lòng nhập email đúng định dạng.";
    // San pham re nhat trong du lieu seed my pham moi (DbInitializer.cs): dung cho test
    // phi ship / voucher can don gia < 100.000d (seed cu "Khan uot" khong con ton tai).
    public static final String CHEAP_SEED_PRODUCT_KEYWORD = "Mặt nạ giấy";

    /** Dinh dang ma don hien thi cua SUT: DH{yyyyMMdd}-{NNNN}. */
    public static final String ORDER_DISPLAY_CODE_REGEX = "DH\\d{8}-\\d{4}";

    private GlobalConstants() {
    }

    /** URL trang Admin rieng: uu tien -Dadmin.url, sau do bien moi truong ADMIN_APP_URL,
     *  cuoi cung la mac dinh (localhost:3001). Can cho Jenkins: trong mang Docker, admin
     *  chay o http://dafl-admin:80 chu khong phai localhost:3001. */
    public static String getAdminAppUrl() {
        String propUrl = System.getProperty("admin.url");
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return propUrl.trim();
        }
        String envUrl = System.getenv("ADMIN_APP_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        return ADMIN_APP_URL;
    }

    /** URL cua SUT: uu tien -Dapp.url, sau do bien moi truong APP_URL, cuoi cung la mac dinh. */
    public static String getAppUrl() {
        String propUrl = System.getProperty("app.url");
        if (propUrl != null && !propUrl.trim().isEmpty()) {
            return propUrl.trim();
        }
        String envUrl = System.getenv("APP_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        return DEFAULT_APP_URL;
    }
}
