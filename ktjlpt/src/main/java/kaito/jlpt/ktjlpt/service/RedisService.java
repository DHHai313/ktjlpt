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
}
