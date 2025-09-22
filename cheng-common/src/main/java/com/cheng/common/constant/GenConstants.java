package com.cheng.common.constant;

/**
 * 程式碼產生共用常量
 *
 * @author cheng
 */
public class GenConstants
{
    /**
     * 單表（增刪改查）
     */
    public static final String TPL_CRUD = "crud";

    /** 樹表（增刪改查） */
    public static final String TPL_TREE = "tree";

    /** 主子表（增刪改查） */
    public static final String TPL_SUB = "sub";

    /** 樹編碼欄位 */
    public static final String TREE_CODE = "treeCode";

    /** 樹父編碼欄位 */
    public static final String TREE_PARENT_CODE = "treeParentCode";

    /** 樹名稱欄位 */
    public static final String TREE_NAME = "treeName";

    /** 上級選單ID欄位 */
    public static final String PARENT_MENU_ID = "parentMenuId";

    /** 上級選單名稱欄位 */
    public static final String PARENT_MENU_NAME = "parentMenuName";

    /** 資料庫字串類型 */
    public static final String[] COLUMNTYPE_STR = { "char", "varchar", "nvarchar", "varchar2"};

    /** 資料庫文字類型 */
    public static final String[] COLUMNTYPE_TEXT = { "tinytext", "text", "mediumtext", "longtext"};

    /** 資料庫時間類型 */
    public static final String[] COLUMNTYPE_TIME = { "datetime", "time", "date", "timestamp"};

    /** 資料庫數字類型 */
    public static final String[] COLUMNTYPE_NUMBER = { "tinyint", "smallint", "mediumint", "int", "number", "integer",
            "bit", "bigint", "float", "double", "decimal"};

    /** 頁面不需要編輯欄位 */
    public static final String[] COLUMNNAME_NOT_EDIT = { "id", "create_by", "create_time", "del_flag"};

    /** 頁面不需要顯示的列表欄位 */
    public static final String[] COLUMNNAME_NOT_LIST = { "id", "create_by", "create_time", "del_flag", "update_by",
            "update_time"};

    /** 頁面不需要查詢欄位 */
    public static final String[] COLUMNNAME_NOT_QUERY = { "id", "create_by", "create_time", "del_flag", "update_by",
            "update_time", "remark"};

    /** Entity基礎類欄位 */
    public static final String[] BASE_ENTITY = { "createBy", "createTime", "updateBy", "updateTime", "remark"};

    /** Tree基礎類欄位 */
    public static final String[] TREE_ENTITY = { "parentName", "parentId", "orderNum", "ancestors", "children"};

    /** 文字框 */
    public static final String HTML_INPUT = "input";

    /** 文字域 */
    public static final String HTML_TEXTAREA = "textarea";

    /** 下拉框 */
    public static final String HTML_SELECT = "select";

    /** 單選框 */
    public static final String HTML_RADIO = "radio";

    /** 複選框 */
    public static final String HTML_CHECKBOX = "checkbox";

    /** 日期元件 */
    public static final String HTML_DATETIME = "datetime";

    /** 圖片上傳元件 */
    public static final String HTML_IMAGE_UPLOAD = "imageUpload";

    /** 文件上傳元件 */
    public static final String HTML_FILE_UPLOAD = "fileUpload";

    /** 富文字元件 */
    public static final String HTML_EDITOR = "editor";

    /** 字串類型 */
    public static final String TYPE_STRING = "String";

    /** 整型 */
    public static final String TYPE_INTEGER = "Integer";

    /** 長整型 */
    public static final String TYPE_LONG = "Long";

    /** 浮點型 */
    public static final String TYPE_DOUBLE = "Double";

    /** 高精度計算類型 */
    public static final String TYPE_BIGDECIMAL = "BigDecimal";

    /** 時間類型 */
    public static final String TYPE_DATE = "Date";

    /** 模糊查詢 */
    public static final String QUERY_LIKE = "LIKE";

    /** 相等查詢 */
    public static final String QUERY_EQ = "EQ";

    /** 需要 */
    public static final String REQUIRE = "1";
}
