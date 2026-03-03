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
}
