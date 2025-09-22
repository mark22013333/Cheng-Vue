package com.cheng.generator.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cheng.common.constant.Constants;
import com.cheng.common.constant.GenConstants;
import com.cheng.common.core.text.CharsetKit;
import com.cheng.common.exception.ServiceException;
import com.cheng.common.utils.StringUtils;
import com.cheng.generator.domain.GenTable;
import com.cheng.generator.domain.GenTableColumn;
import com.cheng.generator.mapper.GenTableColumnMapper;
import com.cheng.generator.mapper.GenTableMapper;
import com.cheng.generator.util.GenUtils;
import com.cheng.generator.util.VelocityInitializer;
import com.cheng.generator.util.VelocityUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 業務 服務層實現
 *
 * @author cheng
 */
@Service
public class GenTableServiceImpl implements IGenTableService {
    private static final Logger log = LoggerFactory.getLogger(GenTableServiceImpl.class);

    @Autowired
    private GenTableMapper genTableMapper;

    @Autowired
    private GenTableColumnMapper genTableColumnMapper;

    /**
     * 取得程式碼產生地址
     *
     * @param table    業務表訊息
     * @param template 模板文件路徑
     * @return 產生地址
     */
    public static String getGenPath(GenTable table, String template) {
        String genPath = table.getGenPath();
        if (StringUtils.equals(genPath, "/")) {
            return System.getProperty("user.dir") + File.separator + "src" + File.separator + VelocityUtils.getFileName(template, table);
        }
        return genPath + File.separator + VelocityUtils.getFileName(template, table);
    }

    /**
     * 查詢業務訊息
     *
     * @param id 業務ID
     * @return 業務訊息
     */
    @Override
    public GenTable selectGenTableById(Long id) {
        GenTable genTable = genTableMapper.selectGenTableById(id);
        setTableFromOptions(genTable);
        return genTable;
    }

    /**
     * 查詢業務列表
     *
     * @param genTable 業務訊息
     * @return 業務集合
     */
    @Override
    public List<GenTable> selectGenTableList(GenTable genTable) {
        return genTableMapper.selectGenTableList(genTable);
    }

    /**
     * 查詢據庫列表
     *
     * @param genTable 業務訊息
     * @return 資料庫表集合
     */
    @Override
    public List<GenTable> selectDbTableList(GenTable genTable) {
        return genTableMapper.selectDbTableList(genTable);
    }

    /**
     * 查詢據庫列表
     *
     * @param tableNames 表名稱組
     * @return 資料庫表集合
     */
    @Override
    public List<GenTable> selectDbTableListByNames(String[] tableNames) {
        return genTableMapper.selectDbTableListByNames(tableNames);
    }

    /**
     * 查詢所有表訊息
     *
     * @return 表訊息集合
     */
    @Override
    public List<GenTable> selectGenTableAll() {
        return genTableMapper.selectGenTableAll();
    }

    /**
     * 修改業務
     *
     * @param genTable 業務訊息
     * @return 結果
     */
    @Override
    @Transactional
    public void updateGenTable(GenTable genTable) {
        String options = JSON.toJSONString(genTable.getParams());
        genTable.setOptions(options);
        int row = genTableMapper.updateGenTable(genTable);
        if (row > 0) {
            for (GenTableColumn genTableColumn : genTable.getColumns()) {
                genTableColumnMapper.updateGenTableColumn(genTableColumn);
            }
        }
    }

    /**
     * 刪除業務物件
     *
     * @param tableIds 需要刪除的數據ID
     * @return 結果
     */
    @Override
    @Transactional
    public void deleteGenTableByIds(Long[] tableIds) {
        genTableMapper.deleteGenTableByIds(tableIds);
        genTableColumnMapper.deleteGenTableColumnByIds(tableIds);
    }

    /**
     * 建立表
     *
     * @param sql 建立表語句
     * @return 結果
     */
    @Override
    public boolean createTable(String sql) {
        return genTableMapper.createTable(sql) == 0;
    }

    /**
     * 匯入表結構
     *
     * @param tableList 匯入表列表
     */
    @Override
    @Transactional
    public void importGenTable(List<GenTable> tableList, String operName) {
        try {
            for (GenTable table : tableList) {
                String tableName = table.getTableName();
                GenUtils.initTable(table, operName);
                int row = genTableMapper.insertGenTable(table);
                if (row > 0) {
                    // 儲存列訊息
                    List<GenTableColumn> genTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
                    for (GenTableColumn column : genTableColumns) {
                        GenUtils.initColumnField(column, table);
                        genTableColumnMapper.insertGenTableColumn(column);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("匯入失敗：" + e.getMessage());
        }
    }

    /**
     * 預覽程式碼
     *
     * @param tableId 表編號
     * @return 預覽數據列表
     */
    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        // 查詢表訊息
        GenTable table = genTableMapper.selectGenTableById(tableId);
        // 設定主子表訊息
        setSubTable(table);
        // 設定主鍵列訊息
        setPkColumn(table);
        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 取得模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getTplWebType());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            dataMap.put(template, sw.toString());
        }
        return dataMap;
    }

    /**
     * 產生程式碼（下載方式）
     *
     * @param tableName 表名稱
     * @return 數據
     */
    @Override
    public byte[] downloadCode(String tableName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generatorCode(tableName, zip);
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * 產生程式碼（自定義路徑）
     *
     * @param tableName 表名稱
     */
    @Override
    public void generatorCode(String tableName) {
        // 查詢表訊息
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        // 設定主子表訊息
        setSubTable(table);
        // 設定主鍵列訊息
        setPkColumn(table);

        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 取得模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getTplWebType());
        for (String template : templates) {
            if (!StringUtils.containsAny(template, "sql.vm", "api.js.vm", "index.vue.vm", "index-tree.vue.vm")) {
                // 渲染模板
                StringWriter sw = new StringWriter();
                Template tpl = Velocity.getTemplate(template, Constants.UTF8);
                tpl.merge(context, sw);
                try {
                    String path = getGenPath(table, template);
                    FileUtils.writeStringToFile(new File(path), sw.toString(), CharsetKit.UTF_8);
                } catch (IOException e) {
                    throw new ServiceException("渲染模板失敗，表名：" + table.getTableName());
                }
            }
        }
    }

    /**
     * 同步資料庫
     *
     * @param tableName 表名稱
     */
    @Override
    @Transactional
    public void synchDb(String tableName) {
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        List<GenTableColumn> tableColumns = table.getColumns();
        Map<String, GenTableColumn> tableColumnMap = tableColumns.stream().collect(Collectors.toMap(GenTableColumn::getColumnName, Function.identity()));

        List<GenTableColumn> dbTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
        if (StringUtils.isEmpty(dbTableColumns)) {
            throw new ServiceException("同步數據失敗，原表結構不存在");
        }
        List<String> dbTableColumnNames = dbTableColumns.stream().map(GenTableColumn::getColumnName).collect(Collectors.toList());

        dbTableColumns.forEach(column -> {
            GenUtils.initColumnField(column, table);
            if (tableColumnMap.containsKey(column.getColumnName())) {
                GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
                column.setColumnId(prevColumn.getColumnId());
                if (column.isList()) {
                    // 如果是列表，繼續保留查詢方式/字典類型選項
                    column.setDictType(prevColumn.getDictType());
                    column.setQueryType(prevColumn.getQueryType());
                }
                if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
                        && (column.isInsert() || column.isEdit())
                        && ((column.isUsableColumn()) || (!column.isSuperColumn()))) {
                    // 如果是(新增/修改&非主鍵/非忽略及父屬性)，繼續保留必填/顯示類型選項
                    column.setIsRequired(prevColumn.getIsRequired());
                    column.setHtmlType(prevColumn.getHtmlType());
                }
                genTableColumnMapper.updateGenTableColumn(column);
            } else {
                genTableColumnMapper.insertGenTableColumn(column);
            }
        });

        List<GenTableColumn> delColumns = tableColumns.stream().filter(column -> !dbTableColumnNames.contains(column.getColumnName())).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(delColumns)) {
            genTableColumnMapper.deleteGenTableColumns(delColumns);
        }
    }

    /**
     * 批次產生程式碼（下載方式）
     *
     * @param tableNames 表陣列
     * @return 數據
     */
    @Override
    public byte[] downloadCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : tableNames) {
            generatorCode(tableName, zip);
        }
        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

    /**
     * 查詢表訊息並產生程式碼
     */
    private void generatorCode(String tableName, ZipOutputStream zip) {
        // 查詢表訊息
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        // 設定主子表訊息
        setSubTable(table);
        // 設定主鍵列訊息
        setPkColumn(table);

        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 取得模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getTplWebType());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            try {
                // 新增到zip
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                IOUtils.write(sw.toString(), zip, Constants.UTF8);
                IOUtils.closeQuietly(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("渲染模板失敗，表名：" + table.getTableName(), e);
            }
        }
    }

    /**
     * 修改儲存參數校驗
     *
     * @param genTable 業務訊息
     */
    @Override
    public void validateEdit(GenTable genTable) {
        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
            String options = JSON.toJSONString(genTable.getParams());
            JSONObject paramsObj = JSON.parseObject(options);
            if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_CODE))) {
                throw new ServiceException("樹編碼欄位不能為空");
            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_PARENT_CODE))) {
                throw new ServiceException("樹父編碼欄位不能為空");
            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_NAME))) {
                throw new ServiceException("樹名稱欄位不能為空");
            }
        } else if (GenConstants.TPL_SUB.equals(genTable.getTplCategory())) {
            if (StringUtils.isEmpty(genTable.getSubTableName())) {
                throw new ServiceException("關聯子表的表名不能為空");
            } else if (StringUtils.isEmpty(genTable.getSubTableFkName())) {
                throw new ServiceException("子表關聯的外鍵名不能為空");
            }
        }
    }

    /**
     * 設定主鍵列訊息
     *
     * @param table 業務表訊息
     */
    public void setPkColumn(GenTable table) {
        for (GenTableColumn column : table.getColumns()) {
            if (column.isPk()) {
                table.setPkColumn(column);
                break;
            }
        }
        if (StringUtils.isNull(table.getPkColumn())) {
            table.setPkColumn(table.getColumns().get(0));
        }
        if (GenConstants.TPL_SUB.equals(table.getTplCategory())) {
            for (GenTableColumn column : table.getSubTable().getColumns()) {
                if (column.isPk()) {
                    table.getSubTable().setPkColumn(column);
                    break;
                }
            }
            if (StringUtils.isNull(table.getSubTable().getPkColumn())) {
                table.getSubTable().setPkColumn(table.getSubTable().getColumns().get(0));
            }
        }
    }

    /**
     * 設定主子表訊息
     *
     * @param table 業務表訊息
     */
    public void setSubTable(GenTable table) {
        String subTableName = table.getSubTableName();
        if (StringUtils.isNotEmpty(subTableName)) {
            table.setSubTable(genTableMapper.selectGenTableByName(subTableName));
        }
    }

    /**
     * 設定程式碼產生其他選項值
     *
     * @param genTable 設定後的產生物件
     */
    public void setTableFromOptions(GenTable genTable) {
        JSONObject paramsObj = JSON.parseObject(genTable.getOptions());
        if (StringUtils.isNotNull(paramsObj)) {
            String treeCode = paramsObj.getString(GenConstants.TREE_CODE);
            String treeParentCode = paramsObj.getString(GenConstants.TREE_PARENT_CODE);
            String treeName = paramsObj.getString(GenConstants.TREE_NAME);
            Long parentMenuId = paramsObj.getLongValue(GenConstants.PARENT_MENU_ID);
            String parentMenuName = paramsObj.getString(GenConstants.PARENT_MENU_NAME);

            genTable.setTreeCode(treeCode);
            genTable.setTreeParentCode(treeParentCode);
            genTable.setTreeName(treeName);
            genTable.setParentMenuId(parentMenuId);
            genTable.setParentMenuName(parentMenuName);
        }
    }
}