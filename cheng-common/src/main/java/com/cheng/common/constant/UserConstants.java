package com.cheng.common.constant;

/**
 * 使用者常量訊息
 *
 * @author cheng
 */
public class UserConstants
{
    /**
     * 平台内系統使用者的唯一標誌
     */
    public static final String SYS_USER = "SYS_USER";

    /**
     * 正常狀態
     */
    public static final String NORMAL = "0";

    /** 異常狀態 */
    public static final String EXCEPTION = "1";

    /** 使用者封禁狀態 */
    public static final String USER_DISABLE = "1";

    /** 角色正常狀態 */
    public static final String ROLE_NORMAL = "0";

    /** 角色封禁狀態 */
    public static final String ROLE_DISABLE = "1";

    /** 部門正常狀態 */
    public static final String DEPT_NORMAL = "0";

    /** 部門停用狀態 */
    public static final String DEPT_DISABLE = "1";

    /** 字典正常狀態 */
    public static final String DICT_NORMAL = "0";

    /** 是否為系統預設（是） */
    public static final String YES = "Y";

    /** 是否選單外鏈（是） */
    public static final String YES_FRAME = "0";

    /** 是否選單外鏈（否） */
    public static final String NO_FRAME = "1";

    /** 選單類型（目錄） */
    public static final String TYPE_DIR = "M";

    /** 選單類型（選單） */
    public static final String TYPE_MENU = "C";

    /** 選單類型（按鈕） */
    public static final String TYPE_BUTTON = "F";

    /** Layout元件標識 */
    public final static String LAYOUT = "Layout";

    /** ParentView元件標識 */
    public final static String PARENT_VIEW = "ParentView";

    /** InnerLink元件標識 */
    public final static String INNER_LINK = "InnerLink";

    /** 校驗是否唯一的返回標識 */
    public final static boolean UNIQUE = true;
    public final static boolean NOT_UNIQUE = false;

    /**
     * 使用者名長度限制
     */
    public static final int USERNAME_MIN_LENGTH = 2;
    public static final int USERNAME_MAX_LENGTH = 20;

    /**
     * 密碼長度限制
     */
    public static final int PASSWORD_MIN_LENGTH = 5;
    public static final int PASSWORD_MAX_LENGTH = 20;
}
