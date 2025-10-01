package com.cheng.generator.domain;

import com.cheng.common.constant.GenConstants;
import com.cheng.common.core.domain.BaseEntity;
import com.cheng.common.utils.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serial;
import java.util.List;

/**
 * 業務表 gen_table
 *
 * @author cheng
 */
@Setter
@Getter
public class GenTable extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 編號
     */
    private Long tableId;

    /**
     * 表名稱
     */
    @NotBlank(message = "表名稱不能為空")
    private String tableName;

    /**
     * 表描述
     */
    @NotBlank(message = "表描述不能為空")
    private String tableComment;

    /**
     * 關聯父表的表名
     */
    private String subTableName;

    /**
     * 本表關聯父表的外鍵名
     */
    private String subTableFkName;

    /**
     * 實體類名稱(首字母大寫)
     */
    @NotBlank(message = "實體類名稱不能為空")
    private String className;

    /**
     * 使用的模板（crud單表操作 tree樹表操作 sub主子表操作）
     */
    private String tplCategory;

    /**
     * 前端類型（element-ui模版 element-plus模版）
     */
    private String tplWebType;

    /**
     * 產生包路徑
     */
    @NotBlank(message = "產生包路徑不能為空")
    private String packageName;

    /**
     * 產生模組名
     */
    @NotBlank(message = "產生模組名不能為空")
    private String moduleName;

    /**
     * 產生業務名
     */
    @NotBlank(message = "產生業務名不能為空")
    private String businessName;

    /**
     * 產生功能名
     */
    @NotBlank(message = "產生功能名不能為空")
    private String functionName;

    /**
     * 產生作者
     */
    @NotBlank(message = "作者不能為空")
    private String functionAuthor;

    /**
     * 產生程式碼方式（0zip壓縮包 1自定義路徑）
     */
    private String genType;

    /**
     * 產生路徑（不填預設專案路徑）
     */
    private String genPath;

    /**
     * 主鍵訊息
     */
    private GenTableColumn pkColumn;

    /**
     * 子表訊息
     */
    private GenTable subTable;

    /**
     * 表列訊息
     */
    @Valid
    private List<GenTableColumn> columns;

    /**
     * 其它產生選項
     */
    private String options;

    /**
     * 樹編碼欄位
     */
    private String treeCode;

    /**
     * 樹父編碼欄位
     */
    private String treeParentCode;

    /**
     * 樹名稱欄位
     */
    private String treeName;

    /**
     * 上級選單ID欄位
     */
    private Long parentMenuId;

    /**
     * 上級選單名稱欄位
     */
    private String parentMenuName;

    public boolean isSub() {
        return isSub(this.tplCategory);
    }

    public static boolean isSub(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
    }

    public boolean isTree() {
        return isTree(this.tplCategory);
    }

    public static boolean isTree(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
    }

    public boolean isCrud() {
        return isCrud(this.tplCategory);
    }

    public static boolean isCrud(String tplCategory) {
        return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
    }

    public boolean isSuperColumn(String javaField) {
        return isSuperColumn(this.tplCategory, javaField);
    }

    public static boolean isSuperColumn(String tplCategory, String javaField) {
        if (isTree(tplCategory)) {
            return StringUtils.equalsAnyIgnoreCase(javaField,
                    ArrayUtils.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
        }
        return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
    }
}