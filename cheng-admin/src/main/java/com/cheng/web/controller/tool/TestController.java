package com.cheng.web.controller.tool;

import com.cheng.common.annotation.Anonymous;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.R;
import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.common.utils.StringUtils;
import com.cheng.crawler.handler.CrawlerHandler;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.framework.security.context.AuthenticationContextHolder;
import com.cheng.framework.web.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * swagger 使用者測試方法
 *
 * @author cheng
 */
@Tag(name = "使用者訊息管理")
@Anonymous
@RestController
@RequiredArgsConstructor
@RequestMapping("/test/user")
public class TestController extends BaseController {
    private final static Map<Integer, UserEntity> users = new LinkedHashMap<>();

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    {
        users.put(1, new UserEntity(1, "admin", "admin123", "15888888888"));
        users.put(2, new UserEntity(2, "yu", "admin123", "15666666666"));
    }

    @Operation(
            summary = "測試登入取得 Token",
            description = "用於測試的快速登入介面，無需驗證碼。預設帳號: admin / 密碼: admin123"
    )
    @PostMapping("/test-login")
    public R<TokenResponse> testLogin(
            @Parameter(description = "登入請求參數", required = true)
            @RequestBody TestLoginRequest request) {
        try {
            // 建立身份驗證令牌
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());

            // 設定到 Context（重要：讓 Spring Security 能正確處理）
            AuthenticationContextHolder.setContext(authenticationToken);

            // 進行身份驗證
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 取得登入使用者資訊
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();

            // 產生 Token
            String token = tokenService.createToken(loginUser);

            TokenResponse response = new TokenResponse();
            response.setToken(token);
            response.setMessage("登入成功！請複製 Token 到 Swagger 的 Authorize 按鈕中使用");
            response.setUsername(loginUser.getUsername());
            response.setUserId(loginUser.getUserId());

            return R.ok(response, "登入成功");
        } catch (Exception e) {
            return R.fail("登入失敗: " + e.getMessage());
        } finally {
            // 清除 Context（確保不會影響其他請求）
            AuthenticationContextHolder.clearContext();
        }
    }

    @Operation(summary = "執行爬蟲（簡易版）", description = "快速執行指定類型的爬蟲任務")
    @GetMapping("crawler/{crawlerType}")
    public R<String> crawler(@PathVariable(name = "crawlerType") CrawlerType crawlerType) {
        CrawlerHandler.getHandler(crawlerType).execute();
        return R.ok("爬蟲執行完成: " + crawlerType.name());
    }

    @Operation(summary = "取得使用者列表")
    @GetMapping("/list")
    public R<List<UserEntity>> userList() {
        List<UserEntity> userList = new ArrayList<UserEntity>(users.values());
        return R.ok(userList);
    }

    @Operation(summary = "取得使用者詳細")
    @GetMapping("/{userId}")
    public R<UserEntity> getUser(@PathVariable(name = "userId")
                                 Integer userId) {
        if (!users.isEmpty() && users.containsKey(userId)) {
            return R.ok(users.get(userId));
        } else {
            return R.fail("使用者不存在");
        }
    }

    @Operation(summary = "新增使用者")
    @PostMapping("/save")
    public R<String> save(UserEntity user) {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserId())) {
            return R.fail("使用者ID不能為空");
        }
        users.put(user.getUserId(), user);
        return R.ok();
    }

    @Operation(summary = "更新使用者")
    @PutMapping("/update")
    public R<String> update(@RequestBody UserEntity user) {
        if (StringUtils.isNull(user) || StringUtils.isNull(user.getUserId())) {
            return R.fail("使用者ID不能為空");
        }
        if (users.isEmpty() || !users.containsKey(user.getUserId())) {
            return R.fail("使用者不存在");
        }
        users.remove(user.getUserId());
        users.put(user.getUserId(), user);
        return R.ok();
    }

    @Operation(summary = "刪除使用者訊息")
    @DeleteMapping("/{userId}")
    public R<String> delete(@PathVariable(name = "userId")
                            Integer userId) {
        if (!users.isEmpty() && users.containsKey(userId)) {
            users.remove(userId);
            return R.ok();
        } else {
            return R.fail("使用者不存在");
        }
    }
}

@Setter
@Getter
@AllArgsConstructor
@Schema(description = "使用者實體")
class UserEntity {
    @Schema(title = "使用者ID")
    private Integer userId;

    @Schema(title = "使用者名稱")
    private String username;

    @Schema(title = "使用者密碼")
    private String password;

    @Schema(title = "使用者手機")
    private String mobile;

}

/**
 * 測試登入請求
 */
@Setter
@Getter
@Schema(description = "測試登入請求參數")
class TestLoginRequest {
    @Schema(description = "使用者名稱", example = "admin", required = true)
    private String username;

    @Schema(description = "密碼", example = "admin123", required = true)
    private String password;
}

/**
 * Token 回應
 */
@Setter
@Getter
@Schema(description = "Token 回應")
class TokenResponse {
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "提示訊息")
    private String message;

    @Schema(description = "使用者名稱")
    private String username;

    @Schema(description = "使用者ID")
    private Long userId;
}
