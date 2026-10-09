package kaito.jlpt.ktjlpt.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Đọc cấu hình email người gửi từ application.yaml
    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    /**
     * Sinh mã OTP 6 chữ số ngẫu nhiên.
     * Dùng SecureRandom thay vì Random để đảm bảo tính bảo mật,
     * tránh việc OTP có thể bị dự đoán.
     *
     * @return Chuỗi 6 ký tự số, VD: "384729"
     */
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        // Tạo số ngẫu nhiên trong khoảng [100000, 999999]
        int otp = 100_000 + random.nextInt(900_000);
        return String.valueOf(otp);
    }

    /**
     * Gửi email chứa mã OTP đến người dùng.
     * Chạy bất đồng bộ (@Async) để không block luồng chính,
     * giúp API đăng ký trả response nhanh hơn.
     *
     * Nếu gửi thất bại, chỉ log lỗi (không throw exception)
     * vì user có thể dùng resend-otp để yêu cầu gửi lại.
     *
     * @param toEmail Địa chỉ email nhận OTP
     * @param otp     Mã OTP 6 số cần gửi
     */
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("KT-JLPT - Xác thực email của bạn");

            // Tạo nội dung HTML cho email
            String htmlContent = buildOtpEmailTemplate(otp);
            helper.setText(htmlContent, true); // true = gửi dưới dạng HTML

            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            // Không throw exception vì chạy async —
            // nếu email không gửi được, user có thể bấm "Gửi lại OTP"
        }
    }

    /**
     * Tạo template HTML đẹp cho email OTP.
     * Hiển thị mã OTP nổi bật ở giữa, kèm thông tin thời hạn 5 phút.
     *
     * @param otp Mã OTP cần hiển thị trong email
     * @return Chuỗi HTML hoàn chỉnh
     */
    private String buildOtpEmailTemplate(String otp) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;
                        padding: 32px; border: 1px solid #e0e0e0; border-radius: 12px;">
                <h2 style="color: #1a73e8; text-align: center;">KT-JLPT</h2>
                <p style="font-size: 16px; color: #333;">Xin chào,</p>
                <p style="font-size: 16px; color: #333;">
                    Mã xác thực email của bạn là:
                </p>
                <div style="text-align: center; margin: 24px 0;">
                    <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                 color: #1a73e8; background: #f0f4ff; padding: 12px 24px;
                                 border-radius: 8px; display: inline-block;">
                        %s
                    </span>
                </div>
                <p style="font-size: 14px; color: #666; text-align: center;">
                    Mã có hiệu lực trong <strong>5 phút</strong>.<br/>
                    Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email.
                </p>
                <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;"/>
                <p style="font-size: 12px; color: #999; text-align: center;">
                    © KT-JLPT — Luyện thi JLPT hiệu quả
                </p>
            </div>
            """.formatted(otp);
    }

    /**
     * Gửi email chứa mã OTP đặt lại mật khẩu đến người dùng.
     * Chạy bất đồng bộ (@Async) để không block luồng chính.
     *
     * @param toEmail Địa chỉ email nhận OTP
     * @param otp     Mã OTP 6 số cần gửi
     */
    @Async
    public void sendForgotPasswordOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject("KT-JLPT - Đặt lại mật khẩu của bạn");

            String htmlContent = buildForgotPasswordEmailTemplate(otp);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Forgot-password OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send forgot-password OTP email to: {}", toEmail, e);
            // Không throw exception vì chạy async — user có thể bấm gửi lại
        }
    }

    /**
     * Tạo template HTML cho email quên mật khẩu.
     * Khác với template verify email: tiêu đề và nội dung nhấn mạnh đây là yêu cầu đặt lại mật khẩu,
     * kèm cảnh báo nếu user không yêu cầu.
     *
     * @param otp Mã OTP cần hiển thị
     * @return Chuỗi HTML hoàn chỉnh
     */
    private String buildForgotPasswordEmailTemplate(String otp) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto;
                        padding: 32px; border: 1px solid #e0e0e0; border-radius: 12px;">
                <h2 style="color: #e53935; text-align: center;">KT-JLPT</h2>
                <p style="font-size: 16px; color: #333;">Xin chào,</p>
                <p style="font-size: 16px; color: #333;">
                    Chúng tôi nhận được yêu cầu <strong>đặt lại mật khẩu</strong> cho tài khoản của bạn.
                    Vui lòng sử dụng mã OTP dưới đây để tiến hành đặt lại mật khẩu:
                </p>
                <div style="text-align: center; margin: 24px 0;">
                    <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                                 color: #e53935; background: #fff3f3; padding: 12px 24px;
                                 border-radius: 8px; display: inline-block;">
                        %s
                    </span>
                </div>
                <p style="font-size: 14px; color: #666; text-align: center;">
                    Mã có hiệu lực trong <strong>5 phút</strong>.<br/>
                    Không chia sẻ mã này cho bất kỳ ai.
                </p>
                <div style="background: #fff8e1; border-left: 4px solid #ffa000;
                            padding: 12px 16px; border-radius: 4px; margin: 16px 0;">
                    <p style="font-size: 13px; color: #5d4037; margin: 0;">
                        ⚠️ Nếu bạn <strong>không</strong> yêu cầu đặt lại mật khẩu,
                        hãy bỏ qua email này. Tài khoản của bạn vẫn an toàn.
                    </p>
                </div>
                <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;"/>
                <p style="font-size: 12px; color: #999; text-align: center;">
                    © KT-JLPT — Luyện thi JLPT hiệu quả
                </p>
            </div>
            """.formatted(otp);
    }
}
