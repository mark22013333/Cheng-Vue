package com.cheng.crawler.mapper;

import com.cheng.crawler.domain.CrawlImportLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 爬蟲匯入記錄 Mapper
 *
 * @author cheng
 */
@Mapper
public interface CrawlImportLogMapper {

    int insertLog(CrawlImportLog log);

    List<CrawlImportLog> selectLogsByBatchId(@Param("batchId") String batchId);
}
