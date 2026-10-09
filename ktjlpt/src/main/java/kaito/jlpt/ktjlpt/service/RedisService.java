package kaito.jlpt.ktjlpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 1. Lưu Refresh Token (Whitelist)
    public void saveRefreshToken(String username, String refreshToken, long durationInSeconds) {
        redisTemplate.opsForValue().set(
                "auth:rt:" + username,
                refreshToken,
                durationInSeconds,
                TimeUnit.SECONDS
        );
    }

    // Lấy Refresh Token
    public String getRefreshToken(String username) {
        return (String) redisTemplate.opsForValue().get("auth:rt:" + username);
    }

    // Xóa Refresh Token (khi Logout)
    public void deleteRefreshToken(String username) {
        redisTemplate.delete("auth:rt:" + username);
    }

    // 2. Lưu Access Token vào Blacklist
    public void blacklistAccessToken(String jti, long remainingTimeMs) {
        redisTemplate.opsForValue().set(
                "auth:bl:" + jti,
                "blacklisted",
                remainingTimeMs,
                TimeUnit.MILLISECONDS
        );
    }

    // Kiểm tra Access Token có bị Blacklist không
    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("auth:bl:" + jti));
    }

    // ======================== OTP Verification ========================

    // Prefix cho key OTP: otp:verify:{email} → lưu mã OTP 6 số
    private static final String OTP_PREFIX = "otp:verify:";
    // Prefix cho cooldown: otp:cooldown:{email} → chống gửi OTP liên tục (spam)
    private static final String OTP_COOLDOWN_PREFIX = "otp:cooldown:";

    /**
     * Lưu OTP vào Redis với TTL tính bằng phút.
     * Khi hết TTL, Redis tự động xoá → OTP hết hiệu lực.
     */
    public void saveOtp(String email, String otp, long expiryMinutes) {
        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otp,
                expiryMinutes,
                TimeUnit.MINUTES
        );
    }

    /**
     * Lấy OTP đã lưu theo email.
     * Trả về null nếu OTP hết hạn hoặc chưa được tạo.
     */
    public String getOtp(String email) {
        return (String) redisTemplate.opsForValue().get(OTP_PREFIX + email);
    }

    /**
     * Xoá OTP sau khi verify thành công → không cho dùng lại.
     */
    public void deleteOtp(String email) {
        redisTemplate.delete(OTP_PREFIX + email);
    }

    /**
     * Đặt cooldown chống spam resend OTP.
     * Trong khoảng cooldownSeconds, user không được yêu cầu OTP mới.
     */
    public void setCooldown(String email, long cooldownSeconds) {
        redisTemplate.opsForValue().set(
                OTP_COOLDOWN_PREFIX + email,
                "1",
                cooldownSeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * Kiểm tra user có đang trong thời gian cooldown không.
     * true = đang cooldown, chưa được gửi lại OTP.
     */
    public boolean isCooldownActive(String email) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(OTP_COOLDOWN_PREFIX + email)
        );
    }

    // ======================== Forgot Password OTP ========================

    // Prefix cho OTP reset password: otp:reset:{email}
    // Tách biệt với otp:verify:{email} để tránh nhầm lẫn
    private static final String RESET_OTP_PREFIX = "otp:reset:";

    // Prefix cho cooldown reset OTP: otp:reset:cooldown:{email}
    private static final String RESET_OTP_COOLDOWN_PREFIX = "otp:reset:cooldown:";

    // Prefix cho reset token sau khi OTP hợp lệ: pwd:reset:{email}
    private static final String RESET_TOKEN_PREFIX = "pwd:reset:";

    /**
     * Lưu OTP quên mật khẩu vào Redis với TTL tính bằng phút.
     */
    public void saveResetOtp(String email, String otp, long expiryMinutes) {
        redisTemplate.opsForValue().set(
                RESET_OTP_PREFIX + email,
                otp,
                expiryMinutes,
                TimeUnit.MINUTES
        );
    }

    /**
     * Lấy OTP quên mật khẩu theo email.
     * Trả về null nếu OTP hết hạn hoặc chưa được tạo.
     */
    public String getResetOtp(String email) {
        return (String) redisTemplate.opsForValue().get(RESET_OTP_PREFIX + email);
    }

    /**
     * Xoá OTP quên mật khẩu sau khi verify thành công.
     */
    public void deleteResetOtp(String email) {
        redisTemplate.delete(RESET_OTP_PREFIX + email);
    }

    /**
     * Đặt cooldown chống spam resend OTP quên mật khẩu.
     */
    public void setResetOtpCooldown(String email, long cooldownSeconds) {
        redisTemplate.opsForValue().set(
                RESET_OTP_COOLDOWN_PREFIX + email,
                "1",
                cooldownSeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * Kiểm tra cooldown gửi OTP quên mật khẩu.
     */
    public boolean isResetOtpCooldownActive(String email) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(RESET_OTP_COOLDOWN_PREFIX + email)
        );
    }

    /**
     * Lưu reset token (UUID) sau khi OTP quên mật khẩu được xác minh.
     * TTL tính bằng giây — mặc định 10 phút (600s).
     */
    public void saveResetToken(String email, String resetToken, long expirySeconds) {
        redisTemplate.opsForValue().set(
                RESET_TOKEN_PREFIX + email,
                resetToken,
                expirySeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * Lấy reset token theo email.
     * Trả về null nếu token hết hạn hoặc chưa tồn tại.
     */
    public String getResetToken(String email) {
        return (String) redisTemplate.opsForValue().get(RESET_TOKEN_PREFIX + email);
    }

    /**
     * Xoá reset token sau khi đổi mật khẩu thành công.
     */
    public void deleteResetToken(String email) {
        redisTemplate.delete(RESET_TOKEN_PREFIX + email);
    }
}
