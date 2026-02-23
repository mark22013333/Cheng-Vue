package com.cheng.line.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.enums.BusinessType;
import com.cheng.line.dto.SendMessageDTO;
import com.cheng.line.service.ILineMessageSendService;
import com.cheng.line.util.FlexMessageParser;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * LINE 訊息發送 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/line/message/send")
public class LineMessageSendController extends BaseController {

    private @Resource ILineMessageSendService lineMessageSendService;
    private @Resource FlexMessageParser flexMessageParser;

    /**
     * 發送訊息（通用介面）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE訊息發送", businessType = BusinessType.OTHER)
    @PostMapping
    public AjaxResult send(@Validated @RequestBody SendMessageDTO dto) {
        Long messageId = lineMessageSendService.sendMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送純文字訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE文字訊息", businessType = BusinessType.OTHER)
    @PostMapping("/text")
    public AjaxResult sendText(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("TEXT");
        Long messageId = lineMessageSendService.sendTextMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送圖片訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE圖片訊息", businessType = BusinessType.OTHER)
    @PostMapping("/image")
    public AjaxResult sendImage(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("IMAGE");
        Long messageId = lineMessageSendService.sendImageMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送影片訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE影片訊息", businessType = BusinessType.OTHER)
    @PostMapping("/video")
    public AjaxResult sendVideo(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("VIDEO");
        Long messageId = lineMessageSendService.sendVideoMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送音訊訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE音訊訊息", businessType = BusinessType.OTHER)
    @PostMapping("/audio")
    public AjaxResult sendAudio(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("AUDIO");
        Long messageId = lineMessageSendService.sendAudioMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送位置訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE位置訊息", businessType = BusinessType.OTHER)
    @PostMapping("/location")
    public AjaxResult sendLocation(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("LOCATION");
        Long messageId = lineMessageSendService.sendLocationMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送貼圖訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE貼圖訊息", businessType = BusinessType.OTHER)
    @PostMapping("/sticker")
    public AjaxResult sendSticker(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("STICKER");
        Long messageId = lineMessageSendService.sendStickerMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送 Imagemap 訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE Imagemap訊息", businessType = BusinessType.OTHER)
    @PostMapping("/imagemap")
    public AjaxResult sendImagemap(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("IMAGEMAP");
        Long messageId = lineMessageSendService.sendImagemapMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 發送 Flex 訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE Flex訊息", businessType = BusinessType.OTHER)
    @PostMapping("/flex")
    public AjaxResult sendFlex(@Validated @RequestBody SendMessageDTO dto) {
        dto.setContentType("FLEX");
        Long messageId = lineMessageSendService.sendFlexMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 使用範本發送訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @Log(title = "LINE範本訊息", businessType = BusinessType.OTHER)
    @PostMapping("/template")
    public AjaxResult sendTemplate(@Validated @RequestBody SendMessageDTO dto) {
        Long messageId = lineMessageSendService.sendTemplateMessage(dto);
        return success("發送成功:" + messageId);
    }

    /**
     * 驗證 Flex Message JSON
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @PostMapping("/validate/flex")
    public AjaxResult validateFlex(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        FlexMessageParser.ValidationResult result = flexMessageParser.validate(content);

        if (result.valid()) {
            return success("Flex Message JSON 格式正確");
        } else {
            return error(result.errorMessage());
        }
    }

    /**
     * 格式化 JSON
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Line.Message.SEND + "')")
    @PostMapping("/format/json")
    public AjaxResult formatJson(@RequestBody Map<String, String> params) {
        String content = params.get("content");
        String formatted = flexMessageParser.formatJson(content);
        return success(formatted);
    }
}
