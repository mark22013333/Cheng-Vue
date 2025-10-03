package com.cheng.system.domain;

import com.cheng.common.annotation.Excel;
import com.cheng.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.util.Date;

/**
 * 書籍詳細資訊表 inv_book_info
 *
 * @author cheng
 */
@Setter
@Getter
public class InvBookInfo extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 書籍資訊ID
     */
    private Long bookInfoId;

    /**
     * 關聯物品ID
     */
    @Excel(name = "關聯物品ID")
    private Long itemId;

    /**
     * ISBN
     */
    @Excel(name = "ISBN")
    @NotBlank(message = "ISBN不能為空")
    @Size(min = 0, max = 20, message = "ISBN長度不能超過20個字元")
    private String isbn;

    /**
     * 書名
     */
    @Excel(name = "書名")
    @NotBlank(message = "書名不能為空")
    @Size(min = 0, max = 200, message = "書名長度不能超過200個字元")
    private String title;

    /**
     * 作者
     */
    @Excel(name = "作者")
    @Size(min = 0, max = 200, message = "作者長度不能超過200個字元")
    private String author;

    /**
     * 出版社
     */
    @Excel(name = "出版社")
    @Size(min = 0, max = 100, message = "出版社長度不能超過100個字元")
    private String publisher;

    /**
     * 出版日期
     */
    @Excel(name = "出版日期")
    @Size(min = 0, max = 50, message = "出版日期長度不能超過50個字元")
    private String publishDate;

    /**
     * 出版地
     */
    @Excel(name = "出版地")
    @Size(min = 0, max = 50, message = "出版地長度不能超過50個字元")
    private String publishLocation;

    /**
     * 語言
     */
    @Excel(name = "語言")
    @Size(min = 0, max = 50, message = "語言長度不能超過50個字元")
    private String language;

    /**
     * 版本
     */
    @Excel(name = "版本")
    @Size(min = 0, max = 50, message = "版本長度不能超過50個字元")
    private String edition;

    /**
     * 裝訂
     */
    @Excel(name = "裝訂")
    @Size(min = 0, max = 50, message = "裝訂長度不能超過50個字元")
    private String binding;

    /**
     * 分級
     */
    @Excel(name = "分級")
    @Size(min = 0, max = 50, message = "分級長度不能超過50個字元")
    private String classification;

    /**
     * 封面圖片路徑
     */
    @Excel(name = "封面圖片路徑")
    @Size(min = 0, max = 300, message = "封面圖片路徑長度不能超過300個字元")
    private String coverImagePath;

    /**
     * 簡介
     */
    @Excel(name = "簡介")
    private String introduction;

    /**
     * 來源網址
     */
    @Excel(name = "來源網址")
    @Size(min = 0, max = 300, message = "來源網址長度不能超過300個字元")
    private String sourceUrl;

    /**
     * 爬取時間
     */
    @Excel(name = "爬取時間", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date crawlTime;

    /**
     * 狀態（0正常 1停用）
     */
    @Excel(name = "狀態", readConverterExp = "0=正常,1=停用")
    private String status;

    public InvBookInfo() {
    }

    public InvBookInfo(String isbn) {
        this.isbn = isbn;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("bookInfoId", getBookInfoId())
                .append("itemId", getItemId())
                .append("isbn", getIsbn())
                .append("title", getTitle())
                .append("author", getAuthor())
                .append("publisher", getPublisher())
                .append("publishDate", getPublishDate())
                .append("publishLocation", getPublishLocation())
                .append("language", getLanguage())
                .append("edition", getEdition())
                .append("binding", getBinding())
                .append("classification", getClassification())
                .append("coverImagePath", getCoverImagePath())
                .append("introduction", getIntroduction())
                .append("sourceUrl", getSourceUrl())
                .append("crawlTime", getCrawlTime())
                .append("status", getStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
