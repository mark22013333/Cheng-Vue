package com.cheng.generator.controller;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.core.text.Convert;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.sql.SqlUtil;
import com.cheng.generator.config.GenConfig;
import com.cheng.generator.domain.GenTable;
import com.cheng.generator.domain.GenTableColumn;
import com.cheng.generator.service.IGenTableColumnService;
import com.cheng.generator.service.IGenTableService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 程式碼產生 操作處理
 *
 * @author cheng
 */
@RestController
@RequestMapping("/tool/gen")
public class GenController extends BaseController {
    @Autowired
    private IGenTableService genTableService;

    @Autowired
    private IGenTableColumnService genTableColumnService;

    /**
     * 查詢程式碼產生列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo genList(GenTable genTable) {
        startPage();
        List<GenTable> list = genTableService.selectGenTableList(genTable);
        return getDataTable(list);
    }

    /**
     * 取得程式碼產生訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.QUERY + "')")
    @GetMapping(value = "/{tableId}")
    public AjaxResult getInfo(@PathVariable Long tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        List<GenTable> tables = genTableService.selectGenTableAll();
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("info", table);
        map.put("rows", list);
        map.put("tables", tables);
        return success(map);
    }

    /**
     * 查詢資料庫列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.LIST + "')")
    @GetMapping("/db/list")
    public TableDataInfo dataList(GenTable genTable) {
        startPage();
        List<GenTable> list = genTableService.selectDbTableList(genTable);
        return getDataTable(list);
    }

    /**
     * 查詢數據表欄位列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.LIST + "')")
    @GetMapping(value = "/column/{tableId}")
    public TableDataInfo columnList(Long tableId) {
        TableDataInfo dataInfo = new TableDataInfo();
        List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
        dataInfo.setRows(list);
        dataInfo.setTotal(list.size());
        return dataInfo;
    }

    /**
     * 匯入表結構（儲存）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.IMPORT + "')")
    @Log(title = "程式碼產生", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    public AjaxResult importTableSave(String tables) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查詢表訊息
        List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
        genTableService.importGenTable(tableList, SecurityUtils.getUsername());
        return success();
    }

    /**
     * 建立表結構（儲存）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "建立表", businessType = BusinessType.OTHER)
    @PostMapping("/createTable")
    public AjaxResult createTableSave(String sql) {
        try {
            SqlUtil.filterKeyword(sql);
            List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, DbType.mysql);
            List<String> tableNames = new ArrayList<>();
            for (SQLStatement sqlStatement : sqlStatements) {
                if (sqlStatement instanceof MySqlCreateTableStatement createTableStatement) {
                    if (genTableService.createTable(createTableStatement.toString())) {
                        String tableName = createTableStatement.getTableName().replaceAll("`", "");
                        tableNames.add(tableName);
                    }
                }
            }
            List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames.toArray(new String[0]));
            String operName = SecurityUtils.getUsername();
            genTableService.importGenTable(tableList, operName);
            return AjaxResult.success();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return AjaxResult.error("建立表結構異常");
        }
    }

    /**
     * 修改儲存程式碼產生業務
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.EDIT + "')")
    @Log(title = "程式碼產生", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult editSave(@Validated @RequestBody GenTable genTable) {
        genTableService.validateEdit(genTable);
        genTableService.updateGenTable(genTable);
        return success();
    }

    /**
     * 刪除程式碼產生
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.REMOVE + "')")
    @Log(title = "程式碼產生", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    public AjaxResult remove(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return success();
    }

    /**
     * 預覽程式碼
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.PREVIEW + "')")
    @GetMapping("/preview/{tableId}")
    public AjaxResult preview(@PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return success(dataMap);
    }

    /**
     * 產生程式碼（下載方式）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.CODE + "')")
    @Log(title = "程式碼產生", businessType = BusinessType.GENCODE)
    @GetMapping("/download/{tableName}")
    public void download(HttpServletResponse response, @PathVariable("tableName") String tableName) throws IOException {
        byte[] data = genTableService.downloadCode(tableName);
        genCode(response, data);
    }

    /**
     * 產生程式碼（自定義路徑）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.CODE + "')")
    @Log(title = "程式碼產生", businessType = BusinessType.GENCODE)
    @GetMapping("/genCode/{tableName}")
    public AjaxResult genCode(@PathVariable("tableName") String tableName) {
        if (!GenConfig.isAllowOverwrite()) {
            return AjaxResult.error("【系統預設】不允許產生檔案覆蓋到本機");
        }
        genTableService.generatorCode(tableName);
        return success();
    }

    /**
     * 同步資料庫
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.EDIT + "')")
    @Log(title = "程式碼產生", businessType = BusinessType.UPDATE)
    @GetMapping("/synchDb/{tableName}")
    public AjaxResult synchDb(@PathVariable("tableName") String tableName) {
        genTableService.synchDb(tableName);
        return success();
    }

    /**
     * 批次產生程式碼
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.Tool.Gen.CODE + "')")
    @Log(title = "程式碼產生", businessType = BusinessType.GENCODE)
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tables) throws IOException {
        String[] tableNames = Convert.toStrArray(tables);
        byte[] data = genTableService.downloadCode(tableNames);
        genCode(response, data);
    }

    /**
     * 產生zip檔案
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"cheng.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IOUtils.write(data, response.getOutputStream());
    }
}