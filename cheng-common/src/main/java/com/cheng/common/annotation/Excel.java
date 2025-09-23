package com.cheng.common.annotation;

import com.cheng.common.utils.poi.ExcelHandlerAdapter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;

/**
 * 自定義匯出Excel數據註解
 *
 * @author cheng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel {
    /**
     * 匯出時在excel中排序
     */
    int sort() default Integer.MAX_VALUE;

    /**
     * 匯出到Excel中的名字.
     */
    String name() default "";

    /**
     * 日期格式, 如: yyyy-MM-dd
     */
    String dateFormat() default "";

    /**
     * 如果是字典類型，請設定字典的type值 (如: sys_user_sex)
     */
    String dictType() default "";

    /**
     * 讀取内容轉表達式 (如: 0=男,1=女,2=未知)
     */
    String readConverterExp() default "";

    /**
     * 分隔符號，讀取字串組内容
     */
    String separator() default ",";

    /**
     * BigDecimal 精度 預設:-1(預設不開啟BigDecimal格式化)
     */
    int scale() default -1;

    /**
     * BigDecimal 舍入規則 預設: BigDecimal.ROUND_HALF_EVEN
     */
    @SuppressWarnings("deprecation")
    int roundingMode() default BigDecimal.ROUND_HALF_EVEN;

    /**
     * 匯出時在excel中每個列的高度
     */
    double height() default 14;

    /**
     * 匯出時在excel中每個列的寬度
     */
    double width() default 16;

    /**
     * 文字後綴,如% 90 變成90%
     */
    String suffix() default "";

    /**
     * 當值為空時,欄位的預設值
     */
    String defaultValue() default "";

    /**
     * 提示訊息
     */
    String prompt() default "";

    /**
     * 是否允許内容換行
     */
    boolean wrapText() default false;

    /**
     * 設定只能選擇不能輸入的列内容.
     */
    String[] combo() default {};

    /**
     * 是否從字典讀數據到combo,預設不讀取,如讀取需要設定dictType註解.
     */
    boolean comboReadDict() default false;

    /**
     * 是否需要縱向合併單元格,應對需求:含有list集合單元格)
     */
    boolean needMerge() default false;

    /**
     * 是否匯出數據,應對需求:有時我們需要匯出一份模板,這是標題需要但内容需要使用者手工填寫.
     */
    boolean isExport() default true;

    /**
     * 另一個類中的屬性名稱,支援多級取得,以小數點隔開
     */
    String targetAttr() default "";

    /**
     * 是否自動統計數據,在最後追加一行統計數據總和
     */
    boolean isStatistics() default false;

    /**
     * 匯出類型（0數字 1字串 2圖片）
     */
    ColumnType cellType() default ColumnType.STRING;

    /**
     * 匯出列頭背景顏色
     */
    IndexedColors headerBackgroundColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * 匯出列頭字體顏色
     */
    IndexedColors headerColor() default IndexedColors.WHITE;

    /**
     * 匯出單元格背景顏色
     */
    IndexedColors backgroundColor() default IndexedColors.WHITE;

    /**
     * 匯出單元格字體顏色
     */
    IndexedColors color() default IndexedColors.BLACK;

    /**
     * 匯出欄位對齊方式
     */
    HorizontalAlignment align() default HorizontalAlignment.CENTER;

    /**
     * 自定義數據處理器
     */
    Class<?> handler() default ExcelHandlerAdapter.class;

    /**
     * 自定義數據處理器參數
     */
    String[] args() default {};

    /**
     * 欄位類型（0：匯出匯入；1：僅匯出；2：僅匯入）
     */
    Type type() default Type.ALL;

    enum Type {
        ALL(0), EXPORT(1), IMPORT(2);
        private final int value;

        Type(int value) {
            this.value = value;
        }

        int value() {
            return this.value;
        }
    }

    enum ColumnType {
        NUMERIC(0), STRING(1), IMAGE(2), TEXT(3);
        private final int value;

        ColumnType(int value) {
            this.value = value;
        }

        int value() {
            return this.value;
        }
    }
}