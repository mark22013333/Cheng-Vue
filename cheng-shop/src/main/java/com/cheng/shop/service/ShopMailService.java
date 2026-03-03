package com.cheng.shop.service;

import com.cheng.common.utils.StringUtils;
import com.cheng.shop.config.ShopConfigKey;
import com.cheng.shop.config.ShopConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * 商城郵件發送服務
 * <p>
 * 郵件設定來源優先順序：
 * <ol>
 *   <li>資料庫 sys_config（shop.mail.*）— 可在管理後台即時修改</li>
 *   <li>application.yml（spring.mail.*）— 備援</li>
 *   <li>開發模式 — 僅以 log 輸出</li>
 * </ol>
 *
 * @author cheng
 */
@Slf4j
@Service
public class ShopMailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String ymlFromEmail;

    @Value("${cheng.name:CoolApps}")
    private String appName;

    @Autowired
    private ShopConfigService shopConfigService;

    @PostConstruct
    public void init() {
        log.info("========== 郵件服務初始化診斷 ==========");
        log.info("JavaMailSender bean: {}",
                mailSender != null ? mailSender.getClass().getSimpleName() : "null（未注入）");
        log.info("YML 寄件人 (spring.mail.username): {}",
                StringUtils.isNotEmpty(ymlFromEmail) ? ymlFromEmail : "（未設定）");

        MailSenderHolder holder = resolveMailSender();
        if (holder != null) {
            log.info("郵件服務就緒，來源：{}，寄件人：{}", holder.source(), holder.fromEmail());
        } else {
            log.warn("郵件服務未就緒 → 進入開發模式（驗證碼僅輸出到 log）");
            log.warn("排查建議：");
            log.warn("  1. 確認 application-{{profile}}.yml 中有 spring.mail.host 設定");
            log.warn("  2. 確認 spring-boot-starter-mail 依賴已正確引入");
            log.warn("  3. 若使用 Gmail，確認是否為 App Password（非帳號密碼）");
        }
        log.info("==========================================");
    }

    /**
     * 發送密碼重設連結
     *
     * @param to            收件人 Email
     * @param resetUrl      重設密碼 URL（含 selector + token）
     * @param expireMinutes 連結有效期（分鐘）
     */
    public void sendResetLink(String to, String resetUrl, int expireMinutes) {
        MailSenderHolder holder = resolveMailSender();
        String maskedTo = maskEmail(to);
        String selectorPrefix = extractSelectorPrefix(resetUrl);

        if (holder == null) {
            log.warn("【開發模式】密碼重設連結未寄送（郵件服務未配置）email={}, selectorPrefix={}",
                    maskedTo, selectorPrefix);
            return;
        }

        try {
            MimeMessage message = holder.sender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(holder.fromEmail(), appName);
            helper.setTo(to);
            helper.setSubject(appName + " — 密碼重設");
            helper.setText(buildResetLinkEmailHtml(resetUrl, expireMinutes), true);

            holder.sender().send(message);
            log.info("密碼重設連結已寄送（來源：{}，email={}，selectorPrefix={}）",
                    holder.source(), maskedTo, selectorPrefix);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("寄送密碼重設連結失敗：{}", e.getMessage(), e);
            log.warn("【降級模式】密碼重設連結未寄出（email={}，selectorPrefix={}）",
                    maskedTo, selectorPrefix);
        }
    }

    /**
     * 發送密碼變更成功通知
     *
     * @param to        收件人 Email
     * @param ipAddress 操作 IP 地址
     * @param userAgent 操作瀏覽器 User-Agent
     */
    public void sendPasswordChangedNotification(String to, String ipAddress, String userAgent) {
        MailSenderHolder holder = resolveMailSender();

        if (holder == null) {
            log.warn("【開發模式】密碼變更通知 → {}（郵件服務未配置，僅 log 輸出）", to);
            return;
        }

        try {
            MimeMessage message = holder.sender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(holder.fromEmail(), appName);
            helper.setTo(to);
            helper.setSubject(appName + " — 密碼已變更");
            helper.setText(buildPasswordChangedEmailHtml(ipAddress, userAgent), true);

            holder.sender().send(message);
            log.info("密碼變更通知已寄送至 {}（來源：{}）", to, holder.source());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("寄送密碼變更通知失敗：{}", e.getMessage(), e);
        }
    }

    /**
     * 發送 Email 驗證連結
     *
     * @param to          收件人 Email
     * @param verifyUrl   驗證 URL（含 selector + token）
     * @param expireHours 連結有效期（小時）
     */
    public void sendEmailVerification(String to, String verifyUrl, int expireHours) {
        MailSenderHolder holder = resolveMailSender();

        if (holder == null) {
            log.warn("【開發模式】Email 驗證連結 → {} : {}（郵件服務未配置，僅 log 輸出）", to, verifyUrl);
            return;
        }

        try {
            MimeMessage message = holder.sender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(holder.fromEmail(), appName);
            helper.setTo(to);
            helper.setSubject(appName + " — 請驗證您的 Email");
            helper.setText(buildEmailVerificationHtml(verifyUrl, expireHours), true);

            holder.sender().send(message);
            log.info("Email 驗證連結已寄送至 {}（來源：{}）", to, holder.source());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("寄送 Email 驗證連結失敗：{}", e.getMessage(), e);
            log.warn("【降級模式】Email 驗證連結 → {} : {}", to, verifyUrl);
        }
    }

    /**
     * 發送歡迎信
     *
     * @param to       收件人 Email
     * @param nickname 會員暱稱
     */
    public void sendWelcomeEmail(String to, String nickname) {
        MailSenderHolder holder = resolveMailSender();

        if (holder == null) {
            log.warn("【開發模式】歡迎信 → {}（郵件服務未配置，僅 log 輸出）", to);
            return;
        }

        try {
            MimeMessage message = holder.sender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(holder.fromEmail(), appName);
            helper.setTo(to);
            helper.setSubject("歡迎加入 " + appName + "！🎉");
            helper.setText(buildWelcomeEmailHtml(nickname), true);

            holder.sender().send(message);
            log.info("歡迎信已寄送至 {}（來源：{}）", to, holder.source());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("寄送歡迎信失敗：{}", e.getMessage(), e);
        }
    }

    /**
     * 發送密碼重設驗證碼
     *
     * @param to   收件人 Email
     * @param code 6 位驗證碼
     * @deprecated 已改用 {@link #sendResetLink}，此方法將於後續版本移除
     */
    @Deprecated(since = "2026-03", forRemoval = true)
    public void sendResetCode(String to, String code) {
        MailSenderHolder holder = resolveMailSender();

        if (holder == null) {
            log.warn("【開發模式】密碼重設驗證碼 → {} : {}（郵件服務未配置，僅 log 輸出）", to, code);
            return;
        }

        try {
            MimeMessage message = holder.sender().createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(holder.fromEmail(), appName);
            helper.setTo(to);
            helper.setSubject(appName + " — 密碼重設驗證碼");
            helper.setText(buildResetEmailHtml(code), true);

            holder.sender().send(message);
            log.info("密碼重設驗證碼已寄送至 {}（來源：{}）", to, holder.source());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("寄送密碼重設驗證碼失敗：{}", e.getMessage(), e);
            log.warn("【降級模式】密碼重設驗證碼 → {} : {}", to, code);
        }
    }

    /**
     * 依優先順序解析可用的 MailSender
     * <ol>
     *   <li>DB sys_config 設定（動態建立 JavaMailSenderImpl）— 可在管理後台即時修改</li>
     *   <li>YML 設定（Spring 自動配置的 JavaMailSender）— 備援</li>
     *   <li>都沒有 → 返回 null（開發模式）</li>
     * </ol>
     */
    private MailSenderHolder resolveMailSender() {
        // 1. DB 優先（管理後台可即時修改）
        String dbHost = shopConfigService.getString(ShopConfigKey.MAIL_HOST);
        String dbUsername = shopConfigService.getString(ShopConfigKey.MAIL_USERNAME);
        String dbPassword = shopConfigService.getString(ShopConfigKey.MAIL_PASSWORD);

        if (StringUtils.isNotEmpty(dbHost) && StringUtils.isNotEmpty(dbUsername)
                && StringUtils.isNotEmpty(dbPassword)) {
            JavaMailSenderImpl dbSender = new JavaMailSenderImpl();
            dbSender.setHost(dbHost);
            dbSender.setPort(shopConfigService.getInt(ShopConfigKey.MAIL_PORT));
            dbSender.setUsername(dbUsername);
            dbSender.setPassword(dbPassword);

            Properties props = dbSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            return new MailSenderHolder(dbSender, dbUsername, "DB");
        }
        log.debug("DB 郵件來源不可用 — host='{}', username='{}'", dbHost, dbUsername);

        // 2. YML 備援
        if (mailSender != null && StringUtils.isNotEmpty(ymlFromEmail)) {
            return new MailSenderHolder(mailSender, ymlFromEmail, "YML");
        }
        log.debug("YML 郵件來源不可用 — mailSender={}, ymlFromEmail='{}'",
                mailSender != null ? "存在" : "null", ymlFromEmail);

        // 3. 都沒有 → 開發模式
        return null;
    }

    /**
     * 郵件寄件者包裝
     */
    private record MailSenderHolder(JavaMailSender sender, String fromEmail, String source) {
    }

    /**
     * 脫敏 email，避免在 log 暴露完整帳號
     */
    private String maskEmail(String email) {
        if (StringUtils.isEmpty(email) || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.isEmpty()) {
            return "***@" + domain;
        }
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.substring(0, 1) + "***" + local.substring(local.length() - 1) + "@" + domain;
    }

    /**
     * 從重設連結中萃取 selector 前綴（僅用於非敏感追蹤）
     */
    private String extractSelectorPrefix(String resetUrl) {
        if (StringUtils.isEmpty(resetUrl)) {
            return "n/a";
        }
        String selectorKey = "selector=";
        int idx = resetUrl.indexOf(selectorKey);
        if (idx < 0) {
            return "n/a";
        }
        int start = idx + selectorKey.length();
        int end = resetUrl.indexOf('&', start);
        String selector = end > start ? resetUrl.substring(start, end) : resetUrl.substring(start);
        if (selector.isEmpty()) {
            return "n/a";
        }
        return selector.length() <= 8 ? selector : selector.substring(0, 8);
    }

    private String buildResetLinkEmailHtml(String resetUrl, int expireMinutes) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:'Noto Sans TC',Arial,sans-serif;color:#3D2B1F;">
                  <div style="padding:32px;background:#FFFFFF;border:1px solid #E8E4DF;border-radius:16px;">
                    <h2 style="margin:0 0 8px;font-size:22px;color:#3D2B1F;">密碼重設</h2>
                    <p style="margin:0 0 24px;color:#7A6B5D;font-size:14px;">
                      您正在重設 %s 帳號密碼，請點擊以下按鈕完成操作：
                    </p>
                    <div style="text-align:center;padding:20px 0;">
                      <a href="%s"
                         style="display:inline-block;padding:14px 40px;background:#4A6B7C;color:#FFFFFF;
                           font-size:16px;font-weight:600;text-decoration:none;border-radius:12px;">
                        重設密碼
                      </a>
                    </div>
                    <p style="margin:16px 0 0;color:#9A8B7D;font-size:12px;">
                      若按鈕無法點擊，請複製以下連結到瀏覽器：
                    </p>
                    <p style="margin:4px 0 0;color:#4A6B7C;font-size:11px;word-break:break-all;">
                      %s
                    </p>
                    <hr style="margin:24px 0;border:none;border-top:1px solid #E8E4DF;"/>
                    <p style="margin:0;color:#9A8B7D;font-size:12px;text-align:center;">
                      此連結有效期限為 %d 分鐘，請儘速完成操作。<br/>
                      若非本人操作，請忽略此信件，您的密碼不會被變更。
                    </p>
                  </div>
                </div>
                """.formatted(appName, resetUrl, resetUrl, expireMinutes);
    }

    private String buildPasswordChangedEmailHtml(String ipAddress, String userAgent) {
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        // 精簡 User-Agent，取前 80 字元
        String shortUa = (userAgent != null && userAgent.length() > 80)
                ? userAgent.substring(0, 80) + "..." : (userAgent != null ? userAgent : "未知");

        return """
                <div style="max-width:480px;margin:0 auto;font-family:'Noto Sans TC',Arial,sans-serif;color:#3D2B1F;">
                  <div style="padding:32px;background:#FFFFFF;border:1px solid #E8E4DF;border-radius:16px;">
                    <h2 style="margin:0 0 8px;font-size:22px;color:#3D2B1F;">密碼已成功變更</h2>
                    <p style="margin:0 0 16px;color:#7A6B5D;font-size:14px;">
                      您的 %s 帳號密碼已於以下時間變更：
                    </p>
                    <div style="padding:16px;background:#FAF8F5;border-radius:8px;margin:0 0 16px;">
                      <table style="width:100%%;font-size:13px;color:#3D2B1F;">
                        <tr><td style="padding:4px 0;color:#9A8B7D;">時間</td><td style="padding:4px 0;">%s</td></tr>
                        <tr><td style="padding:4px 0;color:#9A8B7D;">IP 位址</td><td style="padding:4px 0;">%s</td></tr>
                        <tr><td style="padding:4px 0;color:#9A8B7D;">裝置</td><td style="padding:4px 0;font-size:11px;">%s</td></tr>
                      </table>
                    </div>
                    <div style="padding:12px 16px;background:#FFF5F5;border:1px solid #FED7D7;border-radius:8px;">
                      <p style="margin:0;color:#C53030;font-size:13px;font-weight:600;">
                        ⚠ 若非您本人操作，請立即聯繫客服處理。
                      </p>
                    </div>
                  </div>
                </div>
                """.formatted(appName, time, ipAddress, shortUa);
    }

    @Deprecated(since = "2026-03", forRemoval = true)
    private String buildResetEmailHtml(String code) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:'Noto Sans TC',Arial,sans-serif;color:#3D2B1F;">
                  <div style="padding:32px;background:#FFFFFF;border:1px solid #E8E4DF;border-radius:16px;">
                    <h2 style="margin:0 0 8px;font-size:22px;color:#3D2B1F;">密碼重設驗證碼</h2>
                    <p style="margin:0 0 24px;color:#7A6B5D;font-size:14px;">
                      您正在重設 %s 帳號密碼，請在頁面上輸入以下驗證碼：
                    </p>
                    <div style="text-align:center;padding:20px 0;">
                      <span style="display:inline-block;padding:14px 32px;background:#FAF8F5;border:2px solid #4A6B7C;
                        border-radius:12px;font-size:32px;font-weight:700;letter-spacing:8px;color:#4A6B7C;">
                        %s
                      </span>
                    </div>
                    <p style="margin:24px 0 0;color:#9A8B7D;font-size:12px;text-align:center;">
                      驗證碼有效期限為 5 分鐘，請儘速完成操作。<br/>
                      若非本人操作，請忽略此信件。
                    </p>
                  </div>
                </div>
                """.formatted(appName, code);
    }

    private String buildEmailVerificationHtml(String verifyUrl, int expireHours) {
        return """
                <div style="max-width:520px;margin:0 auto;font-family:'Noto Sans TC','Helvetica Neue',Arial,sans-serif;color:#3D2B1F;">
                  <div style="padding:32px;background:#FFFFFF;border:1px solid #E8E4DF;border-radius:16px;">
                    <!-- 標題區 -->
                    <div style="text-align:center;margin-bottom:24px;">
                      <div style="display:inline-block;padding:6px 16px;background:rgba(74,107,124,0.1);border:1px solid rgba(74,107,124,0.2);border-radius:999px;font-size:12px;font-weight:700;color:#4A6B7C;letter-spacing:0.08em;">
                        EMAIL 驗證
                      </div>
                      <h2 style="margin:16px 0 8px;font-size:24px;color:#3D2B1F;font-weight:700;">
                        歡迎註冊 %s
                      </h2>
                      <p style="margin:0;color:#7A6B5D;font-size:14px;line-height:1.6;">
                        感謝您的註冊！請點擊下方按鈕驗證您的 Email 地址，<br/>完成後即可開始使用所有功能。
                      </p>
                    </div>
                    <!-- 按鈕區 -->
                    <div style="text-align:center;padding:24px 0;">
                      <a href="%s"
                         style="display:inline-block;padding:14px 48px;background:linear-gradient(135deg,#4A6B7C,#5A8A9A);
                           color:#FFFFFF;font-size:16px;font-weight:600;text-decoration:none;border-radius:12px;
                           box-shadow:0 4px 12px rgba(74,107,124,0.25);">
                        ✉ 驗證我的 Email
                      </a>
                    </div>
                    <!-- 備用連結 -->
                    <div style="padding:16px;background:#FAF8F5;border-radius:8px;margin-bottom:20px;">
                      <p style="margin:0 0 6px;color:#9A8B7D;font-size:12px;">
                        若按鈕無法點擊，請複製以下連結到瀏覽器：
                      </p>
                      <p style="margin:0;color:#4A6B7C;font-size:11px;word-break:break-all;line-height:1.5;">
                        %s
                      </p>
                    </div>
                    <!-- 分隔線 -->
                    <hr style="margin:0 0 16px;border:none;border-top:1px solid #E8E4DF;"/>
                    <!-- 提示 -->
                    <p style="margin:0;color:#9A8B7D;font-size:12px;text-align:center;line-height:1.6;">
                      此連結有效期限為 <strong style="color:#4A6B7C;">%d 小時</strong>，過期後需重新申請。<br/>
                      若非本人操作，請忽略此信件，帳號不會被啟用。
                    </p>
                  </div>
                  <!-- 頁尾 -->
                  <p style="margin:16px 0 0;text-align:center;color:#C4B5A5;font-size:11px;">
                    此信件由系統自動發送，請勿直接回覆。
                  </p>
                </div>
                """.formatted(appName, verifyUrl, verifyUrl, expireHours);
    }

    private String buildWelcomeEmailHtml(String nickname) {
        String frontendUrl = shopConfigService.getString(ShopConfigKey.PAYMENT_FRONTEND_URL);
        String shopUrl = (StringUtils.isNotEmpty(frontendUrl) ? frontendUrl : "#") + "/products";
        String displayName = StringUtils.isNotEmpty(nickname) ? nickname : "新朋友";

        return """
                <div style="max-width:520px;margin:0 auto;font-family:'Noto Sans TC','Helvetica Neue',Arial,sans-serif;color:#3D2B1F;">
                  <div style="padding:32px;background:#FFFFFF;border:1px solid #E8E4DF;border-radius:16px;">
                    <!-- 歡迎標題 -->
                    <div style="text-align:center;margin-bottom:24px;">
                      <div style="font-size:48px;margin-bottom:8px;">🎉</div>
                      <h2 style="margin:0 0 8px;font-size:24px;color:#3D2B1F;font-weight:700;">
                        歡迎加入 %s！
                      </h2>
                      <p style="margin:0;color:#7A6B5D;font-size:14px;line-height:1.6;">
                        嗨 <strong style="color:#4A6B7C;">%s</strong>，您的帳號已成功建立！
                      </p>
                    </div>
                    <!-- 功能亮點 -->
                    <div style="padding:20px;background:#FAF8F5;border-radius:12px;margin-bottom:24px;">
                      <p style="margin:0 0 12px;color:#5A4A3C;font-size:14px;font-weight:600;">
                        您現在可以享受以下服務：
                      </p>
                      <table style="width:100%%;font-size:13px;color:#7A6B5D;line-height:2;">
                        <tr><td>🛒</td><td style="padding-left:8px;">瀏覽並購買精選商品</td></tr>
                        <tr><td>📦</td><td style="padding-left:8px;">追蹤訂單與物流狀態</td></tr>
                        <tr><td>🎁</td><td style="padding-left:8px;">累積會員積分享優惠</td></tr>
                        <tr><td>💝</td><td style="padding-left:8px;">接收專屬優惠與最新消息</td></tr>
                      </table>
                    </div>
                    <!-- CTA 按鈕 -->
                    <div style="text-align:center;padding:8px 0 24px;">
                      <a href="%s"
                         style="display:inline-block;padding:14px 48px;background:linear-gradient(135deg,#4A6B7C,#5A8A9A);
                           color:#FFFFFF;font-size:16px;font-weight:600;text-decoration:none;border-radius:12px;
                           box-shadow:0 4px 12px rgba(74,107,124,0.25);">
                        🛍 開始購物
                      </a>
                    </div>
                    <!-- 分隔線 -->
                    <hr style="margin:0 0 16px;border:none;border-top:1px solid #E8E4DF;"/>
                    <!-- 提示 -->
                    <p style="margin:0;color:#9A8B7D;font-size:12px;text-align:center;line-height:1.6;">
                      如有任何問題，歡迎隨時聯繫我們的客服團隊。<br/>
                      祝您購物愉快！
                    </p>
                  </div>
                  <!-- 頁尾 -->
                  <p style="margin:16px 0 0;text-align:center;color:#C4B5A5;font-size:11px;">
                    此信件由系統自動發送，請勿直接回覆。
                  </p>
                </div>
                """.formatted(appName, displayName, shopUrl);
    }
}
