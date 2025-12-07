package com.cheng.common.utils.poi;

import com.cheng.common.annotation.Excel;
import com.cheng.common.annotation.Excel.ColumnType;
import com.cheng.common.annotation.Excel.Type;
import com.cheng.common.annotation.Excels;
import com.cheng.common.config.CoolAppsConfig;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.text.Convert;
import com.cheng.common.exception.UtilException;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.DictUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.file.FileTypeUtils;
import com.cheng.common.utils.file.FileUtils;
import com.cheng.common.utils.file.ImageUtils;
import com.cheng.common.utils.reflect.ReflectUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Excel相關處理
 *
 * @author cheng
 */
public class ExcelUtil<T> {
    private static final Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    public static final String SEPARATOR = ",";

    public static final String FORMULA_REGEX_STR = "=|-|\\+|@";

    public static final String[] FORMULA_STR = {"=", "-", "+", "@"};
    /**
     * Excel sheet最大行數，預設65536
     */
    public static final int sheetSize = 65536;
    /**
     * 統計列表
     */
    private final Map<Integer, Double> statistics = new HashMap<>();
    /**
     * 用於dictType屬性數據儲存，避免重複查暫存
     */
    public Map<String, String> sysDictMap = new HashMap<String, String>();
    /**
     * 實體物件
     */
    public Class<T> clazz;
    /**
     * 需要顯示列屬性
     */
    public String[] includeFields;
    /**
     * 需要排除列屬性
     */
    public String[] excludeFields;
    /**
     * 工作表名稱
     */
    private String sheetName;
    /**
     * 匯出類型（EXPORT:匯出數據；IMPORT：匯入模板）
     */
    private Type type;
    /**
     * 工作薄物件
     */
    private Workbook wb;
    /**
     * 工作表物件
     */
    private Sheet sheet;
    /**
     * 樣式列表
     */
    private Map<String, CellStyle> styles;

    /**
     * 最大高度
     */
    private short maxHeight;
    /**
     * 匯入匯出數據列表
     */
    private List<T> list;
    /**
     * 註解列表
     */
    private List<Object[]> fields;
    /**
     * 目前行號
     */
    private int rownum;
    /**
     * 標題
     */
    private String title;
    /**
     * 合併後最後行數
     */
    private int subMergedLastRowNum = 0;
    /**
     * 合併後開始行數
     */
    private int subMergedFirstRowNum = 1;
    /**
     * 物件的子列表方法
     */
    private Method subMethod;
    /**
     * 物件的子列表屬性
     */
    private List<Field> subFields;

    public ExcelUtil(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * 取得畫布
     */
    public static Drawing<?> getDrawingPatriarch(Sheet sheet) {
        if (sheet.getDrawingPatriarch() == null) {
            sheet.createDrawingPatriarch();
        }
        return sheet.getDrawingPatriarch();
    }

    /**
     * 解析匯出值 0=男,1=女,2=未知
     *
     * @param propertyValue 參數值
     * @param converterExp  翻譯註解
     * @param separator     分隔符號
     * @return 解析後值
     */
    public static String convertByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(SEPARATOR);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[0].equals(value)) {
                        propertyString.append(itemArray[1]).append(separator);
                        break;
                    }
                }
            } else {
                if (itemArray[0].equals(propertyValue)) {
                    return itemArray[1];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 反向解析值 男=0,女=1,未知=2
     *
     * @param propertyValue 參數值
     * @param converterExp  翻譯註解
     * @param separator     分隔符號
     * @return 解析後值
     */
    public static String reverseByExp(String propertyValue, String converterExp, String separator) {
        StringBuilder propertyString = new StringBuilder();
        String[] convertSource = converterExp.split(SEPARATOR);
        for (String item : convertSource) {
            String[] itemArray = item.split("=");
            if (StringUtils.containsAny(propertyValue, separator)) {
                for (String value : propertyValue.split(separator)) {
                    if (itemArray[1].equals(value)) {
                        propertyString.append(itemArray[0]).append(separator);
                        break;
                    }
                }
            } else {
                if (itemArray[1].equals(propertyValue)) {
                    return itemArray[0];
                }
            }
        }
        return StringUtils.stripEnd(propertyString.toString(), separator);
    }

    /**
     * 解析字典值
     *
     * @param dictValue 字典值
     * @param dictType  字典類型
     * @param separator 分隔符號
     * @return 字典標籤
     */
    public static String convertDictByExp(String dictValue, String dictType, String separator) {
        return DictUtils.getDictLabel(dictType, dictValue, separator);
    }

    /**
     * 反向解析值字典值
     *
     * @param dictLabel 字典標籤
     * @param dictType  字典類型
     * @param separator 分隔符號
     * @return 字典值
     */
    public static String reverseDictByExp(String dictLabel, String dictType, String separator) {
        return DictUtils.getDictValue(dictType, dictLabel, separator);
    }

    /**
     * 取得Excel2003圖片
     *
     * @param sheet    目前sheet物件
     * @param workbook 工作簿物件
     * @return Map key:圖片單元格索引（1_1）String，value:圖片流PictureData
     */
    public static Map<String, List<PictureData>> getSheetPictures03(HSSFSheet sheet, HSSFWorkbook workbook) {
        Map<String, List<PictureData>> sheetIndexPicMap = new HashMap<>();
        List<HSSFPictureData> pictures = workbook.getAllPictures();
        if (!pictures.isEmpty() && sheet.getDrawingPatriarch() != null) {
            for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                if (shape instanceof HSSFPicture pic) {
                    HSSFClientAnchor anchor = (HSSFClientAnchor) pic.getAnchor();
                    String picIndex = anchor.getRow1() + "_" + anchor.getCol1();
                    sheetIndexPicMap.computeIfAbsent(picIndex, k -> new ArrayList<>()).add(pic.getPictureData());
                }
            }
        }
        return sheetIndexPicMap;
    }

    /**
     * 取得Excel2007圖片
     *
     * @param sheet    目前sheet物件
     * @param workbook 工作簿物件
     * @return Map key:圖片單元格索引（1_1）String，value:圖片流PictureData
     */
    public static Map<String, List<PictureData>> getSheetPictures07(XSSFSheet sheet, XSSFWorkbook workbook) {
        Map<String, List<PictureData>> sheetIndexPicMap = new HashMap<>();
        for (POIXMLDocumentPart dr : sheet.getRelations()) {
            if (dr instanceof XSSFDrawing drawing) {
                for (XSSFShape shape : drawing.getShapes()) {
                    if (shape instanceof XSSFPicture pic) {
                        XSSFClientAnchor anchor = pic.getPreferredSize();
                        CTMarker ctMarker = anchor.getFrom();
                        String picIndex = ctMarker.getRow() + "_" + ctMarker.getCol();
                        sheetIndexPicMap.computeIfAbsent(picIndex, k -> new ArrayList<>()).add(pic.getPictureData());
                    }
                }
            }
        }
        return sheetIndexPicMap;
    }

    /**
     * 僅在Excel中顯示列屬性
     *
     * @param fields 列屬性名 示例[單個"name"/多個"id","name"]
     */
    public void showColumn(String... fields) {
        this.includeFields = fields;
    }

    /**
     * 隱藏Excel中列屬性
     *
     * @param fields 列屬性名 示例[單個"name"/多個"id","name"]
     */
    public void hideColumn(String... fields) {
        this.excludeFields = fields;
    }

    public void init(List<T> list, String sheetName, String title, Type type) {
        if (list == null) {
            list = new ArrayList<T>();
        }
        this.list = list;
        this.sheetName = sheetName;
        this.type = type;
        this.title = title;
        createExcelField();
        createWorkbook();
        createTitle();
        createSubHead();
    }

    /**
     * 建立excel第一行標題
     */
    public void createTitle() {
        if (StringUtils.isNotEmpty(title)) {
            int titleLastCol = this.fields.size() - 1;
            if (isSubList()) {
                titleLastCol = titleLastCol + subFields.size() - 1;
            }
            Row titleRow = sheet.createRow(rownum == 0 ? rownum++ : 0);
            titleRow.setHeightInPoints(30);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellStyle(styles.get("title"));
            titleCell.setCellValue(title);
            sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, titleLastCol));
        }
    }

    /**
     * 建立物件的子列表名稱
     */
    public void createSubHead() {
        if (isSubList()) {
            Row subRow = sheet.createRow(rownum);
            int column = 0;
            int subFieldSize = subFields != null ? subFields.size() : 0;
            for (Object[] objects : fields) {
                Field field = (Field) objects[0];
                Excel attr = (Excel) objects[1];
                if (Collection.class.isAssignableFrom(field.getType())) {
                    Cell cell = subRow.createCell(column);
                    cell.setCellValue(attr.name());
                    cell.setCellStyle(styles.get(StringUtils.format("header_{}_{}", attr.headerColor(), attr.headerBackgroundColor())));
                    if (subFieldSize > 1) {
                        CellRangeAddress cellAddress = new CellRangeAddress(rownum, rownum, column, column + subFieldSize - 1);
                        sheet.addMergedRegion(cellAddress);
                    }
                    column += subFieldSize;
                } else {
                    Cell cell = subRow.createCell(column++);
                    cell.setCellValue(attr.name());
                    cell.setCellStyle(styles.get(StringUtils.format("header_{}_{}", attr.headerColor(), attr.headerBackgroundColor())));
                }
            }
            rownum++;
        }
    }

    /**
     * 對excel表單預設第一個索引名轉換成list
     *
     * @param is 輸入流
     * @return 轉換後集合
     */
    public List<T> importExcel(InputStream is) {
        return importExcel(is, 0);
    }

    /**
     * 對excel表單預設第一個索引名轉換成list
     *
     * @param is       輸入流
     * @param titleNum 標題佔用行數
     * @return 轉換後集合
     */
    public List<T> importExcel(InputStream is, int titleNum) {
        List<T> list;
        try {
            list = importExcel(StringUtils.EMPTY, is, titleNum);
        } catch (Exception e) {
            log.error("匯入Excel異常{}", e.getMessage());
            throw new UtilException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(is);
        }
        return list;
    }

    /**
     * 對excel表單指定表格索引名轉換成list
     *
     * @param sheetName 表格索引名
     * @param titleNum  標題佔用行數
     * @param is        輸入流
     * @return 轉換後集合
     */
    public List<T> importExcel(String sheetName, InputStream is, int titleNum) throws Exception {
        this.type = Type.IMPORT;
        this.wb = WorkbookFactory.create(is);
        List<T> list = new ArrayList<T>();
        // 如果指定sheet名,則取指定sheet中的内容 否則預設指向第1個sheet
        Sheet sheet = StringUtils.isNotEmpty(sheetName) ? wb.getSheet(sheetName) : wb.getSheetAt(0);
        if (sheet == null) {
            throw new IOException("檔案sheet不存在");
        }
        boolean isXSSFWorkbook = !(wb instanceof HSSFWorkbook);
        Map<String, List<PictureData>> pictures = null;
        if (isXSSFWorkbook) {
            pictures = getSheetPictures07((XSSFSheet) sheet, (XSSFWorkbook) wb);
        } else {
            pictures = getSheetPictures03((HSSFSheet) sheet, (HSSFWorkbook) wb);
        }
        // 取得最後一個非空行的行下標，比如總行數為n，則返回的為n-1
        int rows = sheet.getLastRowNum();
        if (rows > 0) {
            // 定義一個map用於存放excel列的序號和field.
            Map<String, Integer> cellMap = new HashMap<String, Integer>();
            // 取得表頭
            Row heard = sheet.getRow(titleNum);
            for (int i = 0; i < heard.getPhysicalNumberOfCells(); i++) {
                Cell cell = heard.getCell(i);
                if (StringUtils.isNotNull(cell)) {
                    String value = this.getCellValue(heard, i).toString();
                    cellMap.put(value, i);
                    // 同時支援帶 * 和不帶 * 的標題格式
                    // 例如：「物品名稱*」和「物品名稱」都能匹配到 @Excel(name = "物品名稱")
                    if (value.endsWith("*")) {
                        String valueWithoutAsterisk = value.substring(0, value.length() - 1);
                        cellMap.put(valueWithoutAsterisk, i);
                    }
                } else {
                    cellMap.put(null, i);
                }
            }
            // 有數據時才處理 得到類的所有field.
            List<Object[]> fields = this.getFields();
            Map<Integer, Object[]> fieldsMap = new HashMap<Integer, Object[]>();
            for (Object[] objects : fields) {
                Excel attr = (Excel) objects[1];
                Integer column = cellMap.get(attr.name());
                if (column != null) {
                    fieldsMap.put(column, objects);
                }
            }
            for (int i = titleNum + 1; i <= rows; i++) {
                // 從第2行開始取數據,預設第一行是表頭.
                Row row = sheet.getRow(i);
                // 判斷目前行是否是空行
                if (isRowEmpty(row)) {
                    continue;
                }
                T entity = null;
                for (Map.Entry<Integer, Object[]> entry : fieldsMap.entrySet()) {
                    Object val = this.getCellValue(row, entry.getKey());

                    // 如果不存在實體則新建.
                    entity = (entity == null ? clazz.getDeclaredConstructor().newInstance() : entity);
                    // 從map中得到對應列的field.
                    Field field = (Field) entry.getValue()[0];
                    Excel attr = (Excel) entry.getValue()[1];
                    // 取得類型,並根據物件類型設定值.
                    Class<?> fieldType = field.getType();
                    if (String.class == fieldType) {
                        String s = Convert.toStr(val);
                        if (s.matches("^\\d+\\.0$")) {
                            val = StringUtils.substringBefore(s, ".0");
                        } else {
                            String dateFormat = field.getAnnotation(Excel.class).dateFormat();
                            if (StringUtils.isNotEmpty(dateFormat)) {
                                val = parseDateToStr(dateFormat, val);
                            } else {
                                val = Convert.toStr(val);
                            }
                        }
                    } else if ((Integer.TYPE == fieldType || Integer.class == fieldType) && StringUtils.isNumeric(Convert.toStr(val))) {
                        val = Convert.toInt(val);
                    } else if ((Long.TYPE == fieldType || Long.class == fieldType) && StringUtils.isNumeric(Convert.toStr(val))) {
                        val = Convert.toLong(val);
                    } else if (Double.TYPE == fieldType || Double.class == fieldType) {
                        val = Convert.toDouble(val);
                    } else if (Float.TYPE == fieldType || Float.class == fieldType) {
                        val = Convert.toFloat(val);
                    } else if (BigDecimal.class == fieldType) {
                        val = Convert.toBigDecimal(val);
                    } else if (Date.class == fieldType) {
                        if (val instanceof String) {
                            val = DateUtils.parseDate(val);
                        } else if (val instanceof Double) {
                            val = DateUtil.getJavaDate((Double) val);
                        }
                    } else if (Boolean.TYPE == fieldType || Boolean.class == fieldType) {
                        val = Convert.toBool(val, false);
                    }
                    if (StringUtils.isNotNull(fieldType)) {
                        String propertyName = field.getName();
                        if (StringUtils.isNotEmpty(attr.targetAttr())) {
                            propertyName = field.getName() + "." + attr.targetAttr();
                        }
                        if (StringUtils.isNotEmpty(attr.readConverterExp())) {
                            val = reverseByExp(Convert.toStr(val), attr.readConverterExp(), attr.separator());
                        } else if (StringUtils.isNotEmpty(attr.dictType())) {
                            if (!sysDictMap.containsKey(attr.dictType() + val)) {
                                String dictValue = reverseDictByExp(Convert.toStr(val), attr.dictType(), attr.separator());
                                sysDictMap.put(attr.dictType() + val, dictValue);
                            }
                            val = sysDictMap.get(attr.dictType() + val);
                        } else if (!attr.handler().equals(ExcelHandlerAdapter.class)) {
                            val = dataFormatHandlerAdapter(val, attr, null);
                        } else if (ColumnType.IMAGE == attr.cellType() && StringUtils.isNotEmpty(pictures)) {
                            StringBuilder propertyString = new StringBuilder();
                            List<PictureData> images = pictures.get(row.getRowNum() + "_" + entry.getKey());
                            for (PictureData picture : images) {
                                byte[] data = picture.getData();
                                String fileName = FileUtils.writeImportBytes(data);
                                propertyString.append(fileName).append(SEPARATOR);
                            }
                            val = StringUtils.stripEnd(propertyString.toString(), SEPARATOR);
                        }
                        ReflectUtils.invokeSetter(entity, propertyName, val);
                    }
                }
                list.add(entity);
            }
        }
        return list;
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param list      匯出數據集合
     * @param sheetName 工作表的名稱
     * @return 結果
     */
    public AjaxResult exportExcel(List<T> list, String sheetName) {
        return exportExcel(list, sheetName, StringUtils.EMPTY);
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param list      匯出數據集合
     * @param sheetName 工作表的名稱
     * @param title     標題
     * @return 結果
     */
    public AjaxResult exportExcel(List<T> list, String sheetName, String title) {
        this.init(list, sheetName, title, Type.EXPORT);
        return exportExcel();
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param response  返回數據
     * @param list      匯出數據集合
     * @param sheetName 工作表的名稱
     */
    public void exportExcel(HttpServletResponse response, List<T> list, String sheetName) {
        exportExcel(response, list, sheetName, StringUtils.EMPTY);
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param response  返回數據
     * @param list      匯出數據集合
     * @param sheetName 工作表的名稱
     * @param title     標題
     */
    public void exportExcel(HttpServletResponse response, List<T> list, String sheetName, String title) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        this.init(list, sheetName, title, Type.EXPORT);
        exportExcel(response);
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param sheetName 工作表的名稱
     * @return 結果
     */
    public AjaxResult importTemplateExcel(String sheetName) {
        return importTemplateExcel(sheetName, StringUtils.EMPTY);
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param sheetName 工作表的名稱
     * @param title     標題
     */
    public AjaxResult importTemplateExcel(String sheetName, String title) {
        this.init(null, sheetName, title, Type.IMPORT);
        return exportExcel();
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param sheetName 工作表的名稱
     */
    public void importTemplateExcel(HttpServletResponse response, String sheetName) {
        importTemplateExcel(response, sheetName, StringUtils.EMPTY);
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @param sheetName 工作表的名稱
     * @param title     標題
     */
    public void importTemplateExcel(HttpServletResponse response, String sheetName, String title) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        this.init(null, sheetName, title, Type.IMPORT);
        exportExcel(response);
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     */
    public void exportExcel(HttpServletResponse response) {
        try {
            writeSheet();
            wb.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("匯出Excel異常{}", e.getMessage());
        } finally {
            IOUtils.closeQuietly(wb);
        }
    }

    /**
     * 對list資料來源將其裡面的數據匯入到excel表單
     *
     * @return 結果
     */
    public AjaxResult exportExcel() {
        OutputStream out = null;
        try {
            writeSheet();
            String filename = encodingFilename(sheetName);
            out = new FileOutputStream(getAbsoluteFile(filename));
            wb.write(out);
            return AjaxResult.success(filename);
        } catch (Exception e) {
            log.error("匯出Excel異常{}", e.getMessage());
            throw new UtilException("匯出Excel失敗，請聯絡網站管理員！");
        } finally {
            IOUtils.closeQuietly(wb);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * 建立寫入數據到Sheet
     */
    public void writeSheet() {
        // 取出一共有多少個sheet.
        int sheetNo = Math.max(1, (int) Math.ceil(list.size() * 1.0 / sheetSize));
        for (int index = 0; index < sheetNo; index++) {
            createSheet(sheetNo, index);

            // 產生一行
            Row row = sheet.createRow(rownum);
            int column = 0;
            // 寫入各個欄位的列頭名稱
            for (Object[] os : fields) {
                Field field = (Field) os[0];
                Excel excel = (Excel) os[1];
                if (Collection.class.isAssignableFrom(field.getType())) {
                    for (Field subField : subFields) {
                        Excel subExcel = subField.getAnnotation(Excel.class);
                        this.createHeadCell(subExcel, row, column++);
                    }
                } else {
                    this.createHeadCell(excel, row, column++);
                }
            }
            if (Type.EXPORT.equals(type)) {
                fillExcelData(index, row);
                addStatisticsRow();
            }
        }
    }

    /**
     * 填充excel數據
     *
     * @param index 序號
     * @param row   單元格行
     */
    @SuppressWarnings("unchecked")
    public void fillExcelData(int index, Row row) {
        int startNo = index * sheetSize;
        int endNo = Math.min(startNo + sheetSize, list.size());
        int currentRowNum = rownum + 1; // 從標題行後開始

        for (int i = startNo; i < endNo; i++) {
            row = sheet.createRow(currentRowNum);
            T vo = (T) list.get(i);
            int column = 0;
            int maxSubListSize = getCurrentMaxSubListSize(vo);
            for (Object[] os : fields) {
                Field field = (Field) os[0];
                Excel excel = (Excel) os[1];
                if (Collection.class.isAssignableFrom(field.getType())) {
                    try {
                        Collection<?> subList = (Collection<?>) getTargetValue(vo, field, excel);
                        if (subList != null && !subList.isEmpty()) {
                            int subIndex = 0;
                            for (Object subVo : subList) {
                                Row subRow = sheet.getRow(currentRowNum + subIndex);
                                if (subRow == null) {
                                    subRow = sheet.createRow(currentRowNum + subIndex);
                                }

                                int subColumn = column;
                                for (Field subField : subFields) {
                                    Excel subExcel = subField.getAnnotation(Excel.class);
                                    addCell(subExcel, subRow, (T) subVo, subField, subColumn++);
                                }
                                subIndex++;
                            }
                            column += subFields.size();
                        }
                    } catch (Exception e) {
                        log.error("填充集合數據失敗", e);
                    }
                } else {
                    // 建立單元格並設定值
                    addCell(excel, row, vo, field, column);
                    if (maxSubListSize > 1 && excel.needMerge()) {
                        sheet.addMergedRegion(new CellRangeAddress(currentRowNum, currentRowNum + maxSubListSize - 1, column, column));
                    }
                    column++;
                }
            }
            currentRowNum += maxSubListSize;
        }
    }

    /**
     * 取得子列表最大數
     */
    private int getCurrentMaxSubListSize(T vo) {
        int maxSubListSize = 1;
        for (Object[] os : fields) {
            Field field = (Field) os[0];
            if (Collection.class.isAssignableFrom(field.getType())) {
                try {
                    Collection<?> subList = (Collection<?>) getTargetValue(vo, field, (Excel) os[1]);
                    if (subList != null && !subList.isEmpty()) {
                        maxSubListSize = Math.max(maxSubListSize, subList.size());
                    }
                } catch (Exception e) {
                    log.error("取得集合大小失敗", e);
                }
            }
        }
        return maxSubListSize;
    }

    /**
     * 建立表格樣式
     *
     * @param wb 工作薄物件
     * @return 樣式列表
     */
    private Map<String, CellStyle> createStyles(Workbook wb) {
        // 寫入各條記錄,每條記錄對應excel表中的一行
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        Font titleFont = wb.createFont();
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        style.setFont(titleFont);
        DataFormat dataFormat = wb.createDataFormat();
        style.setDataFormat(dataFormat.getFormat("@"));
        styles.put("title", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        Font dataFont = wb.createFont();
        dataFont.setFontName("Arial");
        dataFont.setFontHeightInPoints((short) 10);
        style.setFont(dataFont);
        styles.put("data", style);

        style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(dataFormat.getFormat("######0.00"));
        Font totalFont = wb.createFont();
        totalFont.setFontName("Arial");
        totalFont.setFontHeightInPoints((short) 10);
        style.setFont(totalFont);
        styles.put("total", style);

        styles.putAll(annotationHeaderStyles(wb, styles));

        styles.putAll(annotationDataStyles(wb));

        return styles;
    }

    /**
     * 根據Excel註解建立表格頭樣式
     *
     * @param wb 工作薄物件
     * @return 自定義樣式列表
     */
    private Map<String, CellStyle> annotationHeaderStyles(Workbook wb, Map<String, CellStyle> styles) {
        Map<String, CellStyle> headerStyles = new HashMap<String, CellStyle>();
        for (Object[] os : fields) {
            Excel excel = (Excel) os[1];
            String key = StringUtils.format("header_{}_{}", excel.headerColor(), excel.headerBackgroundColor());
            if (!headerStyles.containsKey(key)) {
                CellStyle style = wb.createCellStyle();
                style.cloneStyleFrom(styles.get("data"));
                style.setAlignment(HorizontalAlignment.CENTER);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                style.setFillForegroundColor(excel.headerBackgroundColor().index);
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font headerFont = wb.createFont();
                headerFont.setFontName("Arial");
                headerFont.setFontHeightInPoints((short) 10);
                headerFont.setBold(true);
                headerFont.setColor(excel.headerColor().index);
                style.setFont(headerFont);
                // 設定表格頭單元格文字形式
                DataFormat dataFormat = wb.createDataFormat();
                style.setDataFormat(dataFormat.getFormat("@"));
                headerStyles.put(key, style);
            }
        }
        return headerStyles;
    }

    /**
     * 根據Excel註解建立表格列樣式
     *
     * @param wb 工作薄物件
     * @return 自定義樣式列表
     */
    private Map<String, CellStyle> annotationDataStyles(Workbook wb) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        for (Object[] os : fields) {
            Field field = (Field) os[0];
            Excel excel = (Excel) os[1];
            if (Collection.class.isAssignableFrom(field.getType())) {
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                Class<?> subClass = (Class<?>) pt.getActualTypeArguments()[0];
                List<Field> subFields = FieldUtils.getFieldsListWithAnnotation(subClass, Excel.class);
                for (Field subField : subFields) {
                    Excel subExcel = subField.getAnnotation(Excel.class);
                    annotationDataStyles(styles, subField, subExcel);
                }
            } else {
                annotationDataStyles(styles, field, excel);
            }
        }
        return styles;
    }

    /**
     * 根據Excel註解建立表格列樣式
     *
     * @param styles 自定義樣式列表
     * @param field  屬性列訊息
     * @param excel  註解訊息
     */
    public void annotationDataStyles(Map<String, CellStyle> styles, Field field, Excel excel) {
        String key = StringUtils.format("data_{}_{}_{}_{}_{}", excel.align(), excel.color(), excel.backgroundColor(), excel.cellType(), excel.wrapText());
        if (!styles.containsKey(key)) {
            CellStyle style = wb.createCellStyle();
            style.setAlignment(excel.align());
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setBorderRight(BorderStyle.THIN);
            style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setBorderLeft(BorderStyle.THIN);
            style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setBorderTop(BorderStyle.THIN);
            style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setBorderBottom(BorderStyle.THIN);
            style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(excel.backgroundColor().getIndex());
            style.setWrapText(excel.wrapText());
            Font dataFont = wb.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 10);
            dataFont.setColor(excel.color().index);
            style.setFont(dataFont);
            if (ColumnType.TEXT == excel.cellType()) {
                DataFormat dataFormat = wb.createDataFormat();
                style.setDataFormat(dataFormat.getFormat("@"));
            }
            styles.put(key, style);
        }
    }

    /**
     * 建立單元格
     */
    public Cell createHeadCell(Excel attr, Row row, int column) {
        // 建立列
        Cell cell = row.createCell(column);
        // 寫入列訊息
        cell.setCellValue(attr.name());
        setDataValidation(attr, row, column);
        cell.setCellStyle(styles.get(StringUtils.format("header_{}_{}", attr.headerColor(), attr.headerBackgroundColor())));
        if (isSubList()) {
            // 填充預設樣式，防止合併單元格樣式失效
            sheet.setDefaultColumnStyle(column, styles.get(StringUtils.format("data_{}_{}_{}_{}_{}", attr.align(), attr.color(), attr.backgroundColor(), attr.cellType(), attr.wrapText())));
            if (attr.needMerge()) {
                sheet.addMergedRegion(new CellRangeAddress(rownum - 1, rownum, column, column));
            }
        }
        return cell;
    }

    /**
     * 設定單元格訊息
     *
     * @param value 單元格值
     * @param attr  註解相關
     * @param cell  單元格訊息
     */
    public void setCellVo(Object value, Excel attr, Cell cell) {
        if (ColumnType.STRING == attr.cellType() || ColumnType.TEXT == attr.cellType()) {
            String cellValue = Convert.toStr(value);
            // 對於任何以表達式觸發字串 =-+@開頭的單元格，直接使用tab字串作為前綴，防止CSV注入。
            if (StringUtils.startsWithAny(cellValue, FORMULA_STR)) {
                cellValue = RegExUtils.replaceFirst(cellValue, FORMULA_REGEX_STR, "\t$0");
            }
            if (value instanceof Collection && StringUtils.equals("[]", cellValue)) {
                cellValue = StringUtils.EMPTY;
            }
            cell.setCellValue(StringUtils.isNull(cellValue) ? attr.defaultValue() : cellValue + attr.suffix());
        } else if (ColumnType.NUMERIC == attr.cellType()) {
            if (StringUtils.isNotNull(value)) {
                cell.setCellValue(StringUtils.contains(Convert.toStr(value), ".") ? Convert.toDouble(value) : Convert.toInt(value));
            }
        } else if (ColumnType.IMAGE == attr.cellType()) {
            ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) cell.getColumnIndex(), cell.getRow().getRowNum(), (short) (cell.getColumnIndex() + 1), cell.getRow().getRowNum() + 1);
            String propertyValue = Convert.toStr(value);
            if (StringUtils.isNotEmpty(propertyValue)) {
                List<String> imagePaths = StringUtils.str2List(propertyValue, SEPARATOR);
                for (String imagePath : imagePaths) {
                    byte[] data = ImageUtils.getImage(imagePath);
                    getDrawingPatriarch(cell.getSheet()).createPicture(anchor, cell.getSheet().getWorkbook().addPicture(data, getImageType(data)));
                }
            }
        }
    }

    /**
     * 取得圖片類型,設定圖片新增類型
     */
    public int getImageType(byte[] value) {
        String type = FileTypeUtils.getFileExtendName(value);
        if ("JPG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_JPEG;
        } else if ("PNG".equalsIgnoreCase(type)) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        return Workbook.PICTURE_TYPE_JPEG;
    }

    /**
     * 建立表格樣式
     */
    public void setDataValidation(Excel attr, Row row, int column) {
        if (attr.name().contains("註：")) {
            sheet.setColumnWidth(column, 6000);
        } else {
            // 設定列寬
            sheet.setColumnWidth(column, (int) ((attr.width() + 0.72) * 256));
        }
        if (StringUtils.isNotEmpty(attr.prompt()) || attr.combo().length > 0 || attr.comboReadDict()) {
            String[] comboArray = attr.combo();
            if (attr.comboReadDict()) {
                if (!sysDictMap.containsKey("combo_" + attr.dictType())) {
                    String labels = DictUtils.getDictLabels(attr.dictType());
                    sysDictMap.put("combo_" + attr.dictType(), labels);
                }
                String val = sysDictMap.get("combo_" + attr.dictType());
                comboArray = StringUtils.split(val, DictUtils.SEPARATOR);
            }
            if (comboArray.length > 15 || StringUtils.join(comboArray).length() > 255) {
                // 如果下拉數大於15或字串長度大於255，則使用一個新sheet儲存，避免產生的模板下拉值取得不到
                setXSSFValidationWithHidden(sheet, comboArray, attr.prompt(), 1, 100, column, column);
            } else {
                // 提示訊息或只能選擇不能輸入的列内容.
                setPromptOrValidation(sheet, comboArray, attr.prompt(), 1, 100, column, column);
            }
        }
    }

    /**
     * 新增單元格
     */
    @SuppressWarnings("deprecation")
    public Cell addCell(Excel attr, Row row, T vo, Field field, int column) {
        Cell cell = null;
        try {
            // 設定行高
            row.setHeight(maxHeight);
            // 根據Excel中設定情況決定是否匯出,有些情況需要保持為空,希望使用者填寫這一列.
            if (attr.isExport()) {
                // 建立cell
                cell = row.createCell(column);
                if (isSubListValue(vo) && getListCellValue(vo).size() > 1 && attr.needMerge()) {
                    if (subMergedLastRowNum >= subMergedFirstRowNum) {
                        sheet.addMergedRegion(new CellRangeAddress(subMergedFirstRowNum, subMergedLastRowNum, column, column));
                    }
                }
                cell.setCellStyle(styles.get(StringUtils.format("data_{}_{}_{}_{}_{}", attr.align(), attr.color(), attr.backgroundColor(), attr.cellType(), attr.wrapText())));

                // 用於讀取物件中的屬性
                Object value = getTargetValue(vo, field, attr);
                String dateFormat = attr.dateFormat();
                String readConverterExp = attr.readConverterExp();
                String separator = attr.separator();
                String dictType = attr.dictType();
                if (StringUtils.isNotEmpty(dateFormat) && StringUtils.isNotNull(value)) {
                    cell.getCellStyle().setDataFormat(this.wb.getCreationHelper().createDataFormat().getFormat(dateFormat));
                    cell.setCellValue(parseDateToStr(dateFormat, value));
                } else if (StringUtils.isNotEmpty(readConverterExp) && StringUtils.isNotNull(value)) {
                    cell.setCellValue(convertByExp(Convert.toStr(value), readConverterExp, separator));
                } else if (StringUtils.isNotEmpty(dictType) && StringUtils.isNotNull(value)) {
                    if (!sysDictMap.containsKey(dictType + value)) {
                        String lable = convertDictByExp(Convert.toStr(value), dictType, separator);
                        sysDictMap.put(dictType + value, lable);
                    }
                    cell.setCellValue(sysDictMap.get(dictType + value));
                } else if (value instanceof BigDecimal && -1 != attr.scale()) {
                    cell.setCellValue((((BigDecimal) value).setScale(attr.scale(), attr.roundingMode())).doubleValue());
                } else if (!attr.handler().equals(ExcelHandlerAdapter.class)) {
                    cell.setCellValue(dataFormatHandlerAdapter(value, attr, cell));
                } else {
                    // 設定列類型
                    setCellVo(value, attr, cell);
                }
                addStatisticsData(column, Convert.toStr(value), attr);
            }
        } catch (Exception e) {
            log.error("匯出Excel失敗:", e);
        }
        return cell;
    }

    /**
     * 設定 POI XSSFSheet 單元格提示或選擇框
     *
     * @param sheet         表單
     * @param textlist      下拉框顯示的内容
     * @param promptContent 提示内容
     * @param firstRow      開始行
     * @param endRow        結束行
     * @param firstCol      開始列
     * @param endCol        結束列
     */
    public void setPromptOrValidation(Sheet sheet, String[] textlist, String promptContent, int firstRow, int endRow,
                                      int firstCol, int endCol) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = textlist.length > 0 ? helper.createExplicitListConstraint(textlist) : helper.createCustomConstraint("DD1");
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        if (StringUtils.isNotEmpty(promptContent)) {
            // 如果設定了提示訊息則游標放上去提示
            dataValidation.createPromptBox("", promptContent);
            dataValidation.setShowPromptBox(true);
        }
        // 處理Excel兼容性問題
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }
        sheet.addValidationData(dataValidation);
    }

    /**
     * 設定某些列的值只能輸入預製的數據，顯示下拉框（兼容超出一定數量的下拉框）
     *
     * @param sheet         要設定的sheet
     * @param textlist      下拉框顯示的内容
     * @param promptContent 提示内容
     * @param firstRow      開始行
     * @param endRow        結束行
     * @param firstCol      開始列
     * @param endCol        結束列
     */
    public void setXSSFValidationWithHidden(Sheet sheet, String[] textlist, String promptContent, int firstRow, int endRow, int firstCol, int endCol) {
        String hideSheetName = "combo_" + firstCol + "_" + endCol;
        Sheet hideSheet = null;
        String hideSheetDataName = hideSheetName + "_data";
        Name name = wb.getName(hideSheetDataName);
        if (name != null) {
            // 名稱已存在，嘗試從名稱的引用中找到sheet名稱
            String refersToFormula = name.getRefersToFormula();
            if (StringUtils.isNotEmpty(refersToFormula) && refersToFormula.contains("!")) {
                String sheetNameFromFormula = refersToFormula.substring(0, refersToFormula.indexOf("!"));
                hideSheet = wb.getSheet(sheetNameFromFormula);
            }
        }

        if (hideSheet == null) {
            hideSheet = wb.createSheet(hideSheetName); // 儲存用(下拉選單)
            for (int i = 0; i < textlist.length; i++) {
                hideSheet.createRow(i).createCell(0).setCellValue(textlist[i]);
            }
            // 建立名稱，可以被其他單元格引用
            name = wb.createName();
            name.setNameName(hideSheetDataName);
            name.setRefersToFormula(hideSheetName + "!$A$1:$A$" + textlist.length);
        }
        DataValidationHelper helper = sheet.getDataValidationHelper();
        // 載入下拉列表内容
        DataValidationConstraint constraint = helper.createFormulaListConstraint(hideSheetName);
        // 設定數據有效性載入在哪個單元格上,四個參數分別是：起始行、終止行、起始列、終止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 數據有效性物件
        DataValidation dataValidation = helper.createValidation(constraint, regions);
        if (StringUtils.isNotEmpty(promptContent)) {
            // 如果設定了提示訊息則游標放上去提示
            dataValidation.createPromptBox("", promptContent);
            dataValidation.setShowPromptBox(true);
        }
        // 處理Excel兼容性問題
        if (dataValidation instanceof XSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        } else {
            dataValidation.setSuppressDropDownArrow(false);
        }

        sheet.addValidationData(dataValidation);
        // 設定hiddenSheet隱藏
        wb.setSheetHidden(wb.getSheetIndex(hideSheet), true);
    }

    /**
     * 數據處理器
     *
     * @param value 數據值
     * @param excel 數據註解
     */
    public String dataFormatHandlerAdapter(Object value, Excel excel, Cell cell) {
        try {
            Object instance = excel.handler().getDeclaredConstructor().newInstance();
            Method formatMethod = excel.handler().getMethod("format", Object.class, String[].class, Cell.class, Workbook.class);
            value = formatMethod.invoke(instance, value, excel.args(), cell, this.wb);
        } catch (Exception e) {
            log.error("不能格式化數據 {}, ERR:{}", excel.handler(), e.getMessage());
        }
        return Convert.toStr(value);
    }

    /**
     * 合計統計訊息
     */
    private void addStatisticsData(Integer index, String text, Excel entity) {
        if (entity != null && entity.isStatistics()) {
            Double temp = 0D;
            if (!statistics.containsKey(index)) {
                statistics.put(index, temp);
            }
            try {
                temp = Double.valueOf(text);
            } catch (NumberFormatException ignored) {
            }
            statistics.put(index, statistics.get(index) + temp);
        }
    }

    /**
     * 建立統計行
     */
    public void addStatisticsRow() {
        if (!statistics.isEmpty()) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Set<Integer> keys = statistics.keySet();
            Cell cell = row.createCell(0);
            cell.setCellStyle(styles.get("total"));
            cell.setCellValue("合計");

            for (Integer key : keys) {
                cell = row.createCell(key);
                cell.setCellStyle(styles.get("total"));
                cell.setCellValue(statistics.get(key));
            }
            statistics.clear();
        }
    }

    /**
     * 編碼檔案名
     */
    public String encodingFilename(String filename) {
        return UUID.randomUUID() + "_" + filename + ".xlsx";
    }

    /**
     * 取得下載路徑
     *
     * @param filename 檔案名稱
     */
    public String getAbsoluteFile(String filename) {
        Path path = Paths.get(CoolAppsConfig.getDownloadPath(), filename);
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new RuntimeException("建立目錄失敗：" + path.getParent(), e);
        }

        return path.toString();
    }

    /**
     * 取得bean中的屬性值
     *
     * @param vo    實體物件
     * @param field 欄位
     * @param excel 註解
     * @return 最終的屬性值
     */
    private Object getTargetValue(T vo, Field field, Excel excel) throws Exception {
        field.setAccessible(true);
        Object o = field.get(vo);
        if (StringUtils.isNotEmpty(excel.targetAttr())) {
            String target = excel.targetAttr();
            if (target.contains(".")) {
                String[] targets = target.split("[.]");
                for (String name : targets) {
                    o = getValue(o, name);
                }
            } else {
                o = getValue(o, target);
            }
        }
        return o;
    }

    /**
     * 以類別的屬性get方法形式取得值
     *
     * @param o
     * @param name
     * @return value
     */
    private Object getValue(Object o, String name) throws Exception {
        if (StringUtils.isNotNull(o) && StringUtils.isNotEmpty(name)) {
            Class<?> clazz = o.getClass();
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            o = field.get(o);
        }
        return o;
    }

    /**
     * 得到所有定義欄位
     */
    private void createExcelField() {
        this.fields = getFields();
        this.fields = this.fields.stream().sorted(Comparator.comparing(objects -> ((Excel) objects[1]).sort())).collect(Collectors.toList());
        this.maxHeight = getRowHeight();
    }

    /**
     * 取得欄位註解訊息
     */
    public List<Object[]> getFields() {
        List<Object[]> fields = new ArrayList<>();
        List<Field> tempFields = new ArrayList<>();
        tempFields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
        tempFields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (StringUtils.isNotEmpty(includeFields)) {
            for (Field field : tempFields) {
                if (ArrayUtils.contains(this.includeFields, field.getName()) || field.isAnnotationPresent(Excels.class)) {
                    addField(fields, field);
                }
            }
        } else if (StringUtils.isNotEmpty(excludeFields)) {
            for (Field field : tempFields) {
                if (!ArrayUtils.contains(this.excludeFields, field.getName())) {
                    addField(fields, field);
                }
            }
        } else {
            for (Field field : tempFields) {
                addField(fields, field);
            }
        }
        return fields;
    }

    /**
     * 新增欄位訊息
     */
    public void addField(List<Object[]> fields, Field field) {
        // 單註解
        if (field.isAnnotationPresent(Excel.class)) {
            Excel attr = field.getAnnotation(Excel.class);
            if (attr != null && (attr.type() == Type.ALL || attr.type() == type)) {
                fields.add(new Object[]{field, attr});
            }
            if (Collection.class.isAssignableFrom(field.getType())) {
                subMethod = getSubMethod(field.getName(), clazz);
                ParameterizedType pt = (ParameterizedType) field.getGenericType();
                Class<?> subClass = (Class<?>) pt.getActualTypeArguments()[0];
                this.subFields = FieldUtils.getFieldsListWithAnnotation(subClass, Excel.class);
            }
        }

        // 多註解
        if (field.isAnnotationPresent(Excels.class)) {
            Excels attrs = field.getAnnotation(Excels.class);
            Excel[] excels = attrs.value();
            for (Excel attr : excels) {
                if (StringUtils.isNotEmpty(includeFields)) {
                    if (ArrayUtils.contains(this.includeFields, field.getName() + "." + attr.targetAttr()) && (attr.type() == Type.ALL || attr.type() == type)) {
                        fields.add(new Object[]{field, attr});
                    }
                } else {
                    if (!ArrayUtils.contains(this.excludeFields, field.getName() + "." + attr.targetAttr()) && (attr.type() == Type.ALL || attr.type() == type)) {
                        fields.add(new Object[]{field, attr});
                    }
                }
            }
        }
    }

    /**
     * 根據註解取得最大行高
     */
    public short getRowHeight() {
        double maxHeight = 0;
        for (Object[] os : this.fields) {
            Excel excel = (Excel) os[1];
            maxHeight = Math.max(maxHeight, excel.height());
        }
        return (short) (maxHeight * 20);
    }

    /**
     * 建立一個工作簿
     */
    public void createWorkbook() {
        this.wb = new SXSSFWorkbook(500);
        this.sheet = wb.createSheet();
        wb.setSheetName(0, sheetName);
        this.styles = createStyles(wb);
    }

    /**
     * 建立工作表
     *
     * @param sheetNo sheet數量
     * @param index   序號
     */
    public void createSheet(int sheetNo, int index) {
        // 設定工作表的名稱.
        if (sheetNo > 1 && index > 0) {
            this.sheet = wb.createSheet();
            this.createTitle();
            wb.setSheetName(index, sheetName + index);
        }
    }

    /**
     * 取得單元格值
     *
     * @param row    取得的行
     * @param column 取得單元格列號
     * @return 單元格值
     */
    public Object getCellValue(Row row, int column) {
        if (row == null) {
            return null;
        }
        Object val = "";
        try {
            Cell cell = row.getCell(column);
            if (StringUtils.isNotNull(cell)) {
                if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
                    val = cell.getNumericCellValue();
                    if (DateUtil.isCellDateFormatted(cell)) {
                        val = DateUtil.getJavaDate((Double) val); // POI Excel 日期格式轉換
                    } else {
                        if ((Double) val % 1 != 0) {
                            val = new BigDecimal(val.toString());
                        } else {
                            val = new DecimalFormat("0").format(val);
                        }
                    }
                } else if (cell.getCellType() == CellType.STRING) {
                    val = cell.getStringCellValue();
                } else if (cell.getCellType() == CellType.BOOLEAN) {
                    val = cell.getBooleanCellValue();
                } else if (cell.getCellType() == CellType.ERROR) {
                    val = cell.getErrorCellValue();
                }

            }
        } catch (Exception e) {
            return val;
        }
        return val;
    }

    /**
     * 判斷是否是空行
     *
     * @param row 判斷的行
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * 格式化不同類型的日期物件
     *
     * @param dateFormat 日期格式
     * @param val        被格式化的日期物件
     * @return 格式化後的日期字串
     */
    public String parseDateToStr(String dateFormat, Object val) {
        if (val == null) {
            return "";
        }
        String str;
        if (val instanceof Date) {
            str = DateUtils.parseDateToStr(dateFormat, (Date) val);
        } else if (val instanceof LocalDateTime) {
            str = DateUtils.parseDateToStr(dateFormat, DateUtils.toDate((LocalDateTime) val));
        } else if (val instanceof LocalDate) {
            str = DateUtils.parseDateToStr(dateFormat, DateUtils.toDate((LocalDate) val));
        } else {
            str = val.toString();
        }
        return str;
    }

    /**
     * 是否有物件的子列表
     */
    public boolean isSubList() {
        return StringUtils.isNotNull(subFields) && !subFields.isEmpty();
    }

    /**
     * 是否有物件的子列表，集合不為空
     */
    public boolean isSubListValue(T vo) {
        return StringUtils.isNotNull(subFields) && !subFields.isEmpty() && StringUtils.isNotNull(getListCellValue(vo)) && !getListCellValue(vo).isEmpty();
    }

    /**
     * 取得集合的值
     */
    public Collection<?> getListCellValue(Object obj) {
        Object value;
        try {
            value = subMethod.invoke(obj);
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return (Collection<?>) value;
    }

    /**
     * 取得物件的子列表方法
     *
     * @param name      名稱
     * @param pojoClass 類物件
     * @return 子列表方法
     */
    public Method getSubMethod(String name, Class<?> pojoClass) {
        StringBuilder getMethodName = new StringBuilder("get");
        getMethodName.append(name.substring(0, 1).toUpperCase());
        getMethodName.append(name.substring(1));
        Method method = null;
        try {
            method = pojoClass.getMethod(getMethodName.toString());
        } catch (Exception e) {
            log.error("取得物件異常{}", e.getMessage());
        }
        return method;
    }
}
