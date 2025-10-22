package com.cheng.web.controller.tool;

import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.R;
import com.cheng.common.utils.StringUtils;
import com.cheng.crawler.CrawlerHandler;
import com.cheng.crawler.enums.CrawlerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RestController
@RequestMapping("/test/user")
public class TestController extends BaseController {
    private final static Map<Integer, UserEntity> users = new LinkedHashMap<Integer, UserEntity>();

    {
        users.put(1, new UserEntity(1, "admin", "admin123", "15888888888"));
        users.put(2, new UserEntity(2, "ry", "admin123", "15666666666"));
    }

    @GetMapping("crawler/")
    public R<String> crawler() {
        CrawlerHandler.getHandler(CrawlerType.CA102).execute();
        return R.ok(" crawler");
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
    public R<String> update(@RequestBody
                            UserEntity user) {
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

    public UserEntity() {

    }

    public UserEntity(Integer userId, String username, String password, String mobile) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.mobile = mobile;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
