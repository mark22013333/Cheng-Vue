package com.cheng.web.controller.system;

import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.domain.model.RegisterBody;
import com.cheng.common.utils.StringUtils;
import com.cheng.framework.web.service.SysRegisterService;
import com.cheng.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 註冊驗證
 *
 * @author cheng
 */
@RestController
public class SysRegisterController extends BaseController {
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user) {
        if (!(Boolean.parseBoolean(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("目前系統沒有開啟註冊功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}
