package com.cheng.crawler.handler;

import com.cheng.crawler.CrawlerHandler;
import com.cheng.crawler.dto.CompanyNewsDTO;
import com.cheng.crawler.dto.CrawlerParams;
import com.cheng.crawler.enums.CrawlerType;
import com.cheng.crawler.repository.CrawlerDataRepository;
import com.cheng.crawler.repository.GenericCrawlerRepository;
import com.cheng.crawler.utils.JacksonUtil;
import com.cheng.crawler.utils.TimeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 公開資訊觀測站爬蟲 - 擷取公司重大訊息資料
 * 抓取時間區間：105年8月25日至今日
 *
 * @author Cheng
 * @since 2025-03-28 10:50
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CA102WHandler extends CrawlerHandler<String[], CompanyNewsDTO> {

    private static final String BASE_URL = "https://mops.twse.com.tw/mops/api/";
    private static final String LIST_API = BASE_URL + "t05st02";
    private static final String DETAIL_API = BASE_URL + "t05st02_detail";

    // 是否只爬取今天的資料
    @Value("${crawler.ca102.today-only:false}")
    private boolean todayOnly;

    private final GenericCrawlerRepository genericCrawlerRepository;

    @Override
    protected CrawlerType getCrawlerType() {
        return CrawlerType.CA102;
    }

    @Override
    protected void init() {
        // 初始化時建立動態 SQL 語句
        String timestampFunc = jdbcSqlTemplate.getCurrentTimestampFunction();
        // SQL 語句與 DDL 結構對應
        String SQL = String.format(
                "INSERT INTO CAT102 (COMPANY_NAME, COMPANY_NO, TITLE, CONTENT, PUBLISH_DATE, EXTRACT_DATE) " +
                        "VALUES (?, ?, ?, ?, ?, %s)",
                timestampFunc
        );

        // 註冊 SQL 和資料轉換器到 Repository
        genericCrawlerRepository.registerSql(
                CrawlerType.CA102,
                SQL,
                dto -> {
                    // 將 CompanyNewsDTO 轉換為 String[]
                    CompanyNewsDTO news = (CompanyNewsDTO) dto;
                    return new String[]{
                            news.getCompanyName(),
                            news.getCompanyId(),
                            news.getSubject(),
                            news.getContent(),
                            news.getPublishDate()
                    };
                }
        );

        log.info("初始化 CA102WHandler，已註冊 SQL 到 Repository");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected CrawlerDataRepository<CompanyNewsDTO> getRepository() {
        // GenericCrawlerRepository 使用 Object 泛型，需要強制轉換
        return (CrawlerDataRepository<CompanyNewsDTO>) (CrawlerDataRepository<?>) genericCrawlerRepository;
    }

    @Override
    protected List<String[]> crawlWebsiteFetchData(WebDriver driver, CrawlerParams params) throws Exception {
        List<String[]> allData = new ArrayList<>();

        try {
            // 設定起始日期 (根據參數決定是抓取今日還是從歷史開始)
            LocalDate startDate = todayOnly ? LocalDate.now() : LocalDate.of(2016, 8, 25);
            // 設定結束日期 (今日)
            LocalDate endDate = LocalDate.now();

            log.info("開始抓取公司重大訊息資料，模式：{}", todayOnly ? "僅爬取今日資料" : "從民國105年8月25日開始爬取");
            log.info("時間範圍: {} 至 {}", startDate, endDate);

            // 逐日抓取資料
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                List<String[]> dailyData = processDailyData(currentDate);
                if (!dailyData.isEmpty()) {
                    // 將每日資料合併到總資料集合
                    allData.addAll(dailyData);
                    log.info("日期 {} 取得 {} 筆資料", currentDate, dailyData.size());
                }

                // 進到下一天
                currentDate = currentDate.plusDays(1);
            }

            log.info("完成抓取公司重大訊息資料，共取得 {} 筆資料", allData.size());
        } catch (Exception e) {
            log.error(">>>抓取公司重大訊息資料時發生錯誤", e);
            throw e;
        }

        return allData;
    }

    /**
     * 處理特定日期的資料
     *
     * @param date 要處理的日期
     * @return 當日處理的資料列表
     */
    private List<String[]> processDailyData(LocalDate date) {
        List<String[]> dailyData = new ArrayList<>();

        try {
            // 將西元年轉換為民國年
            int rocYear = date.getYear() - 1911;
            int month = date.getMonthValue();
            int day = date.getDayOfMonth();

            log.info("處理日期: {}/{}/{}", rocYear, month, day);

            // 請求參數
            Map<String, String> requestData = new HashMap<>();
            requestData.put("year", String.valueOf(rocYear));
            requestData.put("month", String.valueOf(month));
            requestData.put("day", String.valueOf(day));

            try {
                // 呼叫第一個 API
                String responseJson = callListApi(requestData);

                // 解析第一個 API 回應
                List<CompanyNewsDTO> newsList = parseListApiResponse(responseJson);
                log.info("日期 {}/{}/{} 取得 {} 筆重大訊息", rocYear, month, day, newsList.size());

                // 處理每筆資料
                for (CompanyNewsDTO item : newsList) {
                    try {
                        // 呼叫第二個 API 取得詳細資料
                        String detailJson = callDetailApi(item.getParameters());

                        // 解析第二個 API 回應
                        processDetailApiResponse(detailJson, item);

                        String[] data = {
                                item.getCompanyName(),     // 公司名稱
                                item.getCompanyId(),       // 公司代號
                                item.getSubject(),         // 主旨
                                item.getContent(),         // HTML 內容
                                item.getPublishDate(),     // 發布日期
                        };

                        dailyData.add(data);
                        log.info("成功取得重大訊息: {}, 公司: {}", item.getSubject(), item.getCompanyName());

                        // 等待一下，避免過於頻繁請求
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        log.error("處理公司重大訊息詳細資料時出錯: {}", item.getSubject(), e);
                    }
                }
            } catch (Exception e) {
                log.error("處理日期 {}/{}/{} 資料時出錯", rocYear, month, day, e);
            }
        } catch (Exception e) {
            log.error("處理日期資料時出錯: {}", date, e);
        }

        return dailyData;
    }

    @Override
    protected List<CompanyNewsDTO> processData(List<String[]> rawData, CrawlerParams params) throws Exception {
        List<CompanyNewsDTO> processedData = new ArrayList<>();

        for (String[] data : rawData) {
            if (data != null && data.length >= 5) {
                // 將原始資料轉換為業務物件
                CompanyNewsDTO dto = new CompanyNewsDTO();
                dto.setCompanyName(data[0]);
                dto.setCompanyId(data[1]);
                dto.setSubject(data[2]);
                dto.setContent(data[3]);
                dto.setPublishDate(data[4]);

                processedData.add(dto);
            }
        }

        log.info("資料處理完成，共 {} 筆", processedData.size());
        return processedData;
    }

    @Override
    protected void beforeCrawl(CrawlerParams params) throws Exception {
        log.info("初始化爬蟲環境，參數: {}", params);
    }

    @Override
    protected void afterCrawl(CrawlerParams params, boolean success) {
        if (success) {
            log.info("爬蟲任務成功完成");
        } else {
            log.error("爬蟲任務失敗，需要檢查");
        }
    }


    /**
     * 呼叫列表 API
     *
     * @param requestData 請求參數
     * @return API 回應的 JSON 字串
     * @throws IOException 如果呼叫 API 失敗
     */
    private String callListApi(Map<String, String> requestData) throws IOException {
        String requestBody = JacksonUtil.encodeToJson(requestData);

        Connection.Response response = Jsoup.connect(LIST_API)
                .header("Accept", "*/*")
                .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Connection", "keep-alive")
                .header("Origin", "https://mops.twse.com.tw")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36")
                .header("content-type", "application/json")
                .requestBody(requestBody)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();

        return response.body();
    }

    /**
     * 解析列表 API 的回應
     *
     * @param json API 回應的 JSON 字串
     * @return 新聞項目列表
     */
    private List<CompanyNewsDTO> parseListApiResponse(String json) {
        List<CompanyNewsDTO> newsList = new ArrayList<>();

        try {
            ObjectNode jsonObject = JacksonUtil.toObjectNode(json);

            // 檢查 API 回應狀態
            int code = jsonObject.get("code").asInt();
            if (code != 200) {
                log.warn("API 回應非成功狀態: {}", code);
                return newsList;
            }

            // 檢查是否有資料
            if (!jsonObject.has("result") || !jsonObject.path("result").has("data")) {
                log.info("API 回應中無資料");
                return newsList;
            }

            // 解析資料
            ArrayNode dataArray = (ArrayNode) jsonObject.path("result").path("data");
            for (JsonNode element : dataArray) {
                if (!element.isArray() || element.size() <= 5) {
                    continue;
                }

                // 取得基本資訊
                String date = element.path(0).asText("");
                String time = element.path(1).asText("");
                String companyId = element.path(2).asText("");
                String companyName = element.path(3).asText("");
                String subject = element.path(4).asText("");

                // 取得 parameters
                if (!element.has(5) || !element.path(5).has("parameters")) {
                    continue;
                }

                JsonNode parametersObj = element.path(5).path("parameters");

                // 將 parameters 轉為 Map
                Map<String, String> parameters = new HashMap<>();
                parameters.put("companyId", parametersObj.path("companyId").asText());
                parameters.put("marketKind", parametersObj.path("marketKind").asText());
                parameters.put("enterDate", parametersObj.path("enterDate").asText());
                parameters.put("serialNumber", parametersObj.path("serialNumber").asText());

                // 轉換日期格式
                String publishDate = TimeUtil.convertTaiwanDate(date);

                // 建立 DTO 並設定資料
                CompanyNewsDTO newsDto = new CompanyNewsDTO()
                        .setCompanyId(companyId)
                        .setCompanyName(companyName)
                        .setSubject(subject)
                        .setDate(date)
                        .setTime(time)
                        .setParameters(parameters)
                        .setPublishDate(publishDate);

                newsList.add(newsDto);
            }
        } catch (Exception e) {
            log.error("解析列表 API 回應時出錯", e);
        }

        return newsList;
    }

    /**
     * 呼叫詳細資料 API
     *
     * @param parameters 請求參數
     * @return API 回應的 JSON 字串
     * @throws IOException 如果呼叫 API 失敗
     */
    private String callDetailApi(Map<String, String> parameters) throws IOException {
        String requestBody = JacksonUtil.encodeToJson(parameters);

        Connection.Response response = Jsoup.connect(DETAIL_API)
                .header("Accept", "*/*")
                .header("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Connection", "keep-alive")
                .header("Origin", "https://mops.twse.com.tw")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36")
                .header("content-type", "application/json")
                .requestBody(requestBody)
                .method(Connection.Method.POST)
                .ignoreContentType(true)
                .execute();

        return response.body();
    }

    /**
     * 解析詳細資料 API 的回應，填充 DTO
     *
     * @param json API 回應的 JSON 字串
     * @param dto  公司新聞 DTO
     */
    private void processDetailApiResponse(String json, CompanyNewsDTO dto) {
        try {
            ObjectNode jsonObject = JacksonUtil.toObjectNode(json);

            // 檢查 API 回應狀態
            int code = jsonObject.get("code").asInt();
            if (code != 200) {
                log.warn("詳細資料 API 回應非成功狀態: {}, 公司代號: {}", code, dto.getCompanyId());

                // 從非 200 回應中提取 message 並寫入 content
                String errorMessage = jsonObject.has("message") ? jsonObject.get("message").asText("未知錯誤") : "未知錯誤";
                log.info("取得錯誤訊息: {}", errorMessage);

                // 將錯誤訊息寫入 content 欄位
                dto.setContent(errorMessage);
                return;
            }

            // 取得結果資料
            JsonNode resultObj = jsonObject.path("result");
            if (resultObj.isMissingNode()) {
                log.warn("詳細資料 API 回應中無 result 欄位");
                return;
            }

            // 從 result 區段取得各欄位資訊
            dto.setMarketName(getStringValueFromNode(resultObj, "marketName"));
            dto.setCompanyId(getStringValueFromNode(resultObj, "companyId"));

            // 從 data 陣列中取得詳細資料
            if (resultObj.has("data") && resultObj.path("data").isArray() && !resultObj.path("data").isEmpty()) {
                JsonNode dataArray = resultObj.path("data").get(0);
                if (dataArray.isArray() && dataArray.size() >= 10) {
                    // 根據實際回傳格式設定欄位
                    dto.setSerialNumber(dataArray.get(0).asText(""));
                    dto.setDate(dataArray.get(1).asText(""));
                    dto.setTime(dataArray.get(2).asText(""));
                    dto.setSpeaker(dataArray.get(3).asText(""));
                    dto.setSpeakerTitle(dataArray.get(4).asText(""));
                    dto.setSpeakerPhone(dataArray.get(5).asText(""));
                    dto.setSubject(dataArray.get(6).asText(""));
                    dto.setMatchItem(dataArray.get(7).asText(""));
                    dto.setEventDate(dataArray.get(8).asText(""));

                    // 說明欄位可能有多行文字，保留原始格式
                    String description = dataArray.get(9).asText("");
                    dto.setDescription(description);
                }
            }

            // 設定 HTML 內容 (使用回應的結構動態產生，避免 hardcode)
            dto.setContent(generateHtmlContent(resultObj, dto));

        } catch (Exception e) {
            log.error("解析詳細資料 API 回應時出錯", e);
        }
    }

    /**
     * 從 JsonNode 中取得字串值，如果不存在則返回空字串
     *
     * @param node JsonNode
     * @param key  鍵值
     * @return 字串值或空字串
     */
    private String getStringValueFromNode(JsonNode node, String key) {
        JsonNode value = node.path(key);
        if (!value.isMissingNode() && !value.isNull()) {
            return value.asText("");
        }
        return "";
    }

    /**
     * 產生 HTML 內容，根據 API 回應的結構動態產生
     *
     * @param resultObj 詳細資料 JsonNode
     * @param dto       公司新聞 DTO
     * @return 產生的 HTML 內容
     */
    private String generateHtmlContent(JsonNode resultObj, CompanyNewsDTO dto) {
        StringBuilder html = new StringBuilder();

        // 取得標題和說明
        String mainTitle = "";
        if (resultObj.has("header") && resultObj.path("header").has("mainTitle") &&
                resultObj.path("header").path("mainTitle").isArray() &&
                !resultObj.path("header").path("mainTitle").isEmpty()) {
            mainTitle = resultObj.path("header").path("mainTitle").get(0).asText("公司當日重大訊息之詳細內容");
        } else {
            mainTitle = "公司當日重大訊息之詳細內容";
        }

        // 取得頁尾
        String footer;
        if (resultObj.has("footer") && resultObj.path("footer").isArray() && !resultObj.path("footer").isEmpty()) {
            footer = resultObj.path("footer").get(0).asText("以上資料均由各公司依發言當時所屬市場別之規定申報後，由本系統對外公佈，資料如有虛偽不實，均由該公司負責。");
        } else {
            footer = "以上資料均由各公司依發言當時所屬市場別之規定申報後，由本系統對外公佈，資料如有虛偽不實，均由該公司負責。";
        }

        // 取得欄位標題
        List<String> titleNames = new ArrayList<>();
        if (resultObj.has("titles") && resultObj.path("titles").isArray()) {
            JsonNode titles = resultObj.path("titles");
            for (int i = 0; i < titles.size(); i++) {
                String titleName = titles.path(i).path("main").asText("");
                titleNames.add(titleName);
            }
        } else {
            // 預設欄位標題
            titleNames.add("序號");
            titleNames.add("發言日期");
            titleNames.add("發言時間");
            titleNames.add("發言人");
            titleNames.add("發言人職稱");
            titleNames.add("發言人電話");
            titleNames.add("主旨");
            titleNames.add("符合條款");
            titleNames.add("事實發生日");
            titleNames.add("說明");
        }

        // 開始產生 HTML
        html.append("<div class=\"content\">\n");
        html.append("    <div>\n");

        // 標題區塊
        html.append("        <div class=\"caption\"><h1>").append(mainTitle).append("</h1>\n");
        html.append("            <div>本資料由　(").append(dto.getMarketName()).append("公司) ");
        html.append(dto.getCompanyId()).append(" ").append(dto.getCompanyName()).append("　公司提供</div>\n");
        html.append("        </div>\n");

        // 詳細資料表格
        html.append("        <div class=\"detailList\">\n");
        html.append("            <table>\n");

        // 根據標題和資料動態產生表格
        if (titleNames.size() >= 10) {
            // 序號
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(0)).append("</th>\n");
            html.append("                    <td>").append(dto.getSerialNumber()).append("</td>\n");
            html.append("                </tr>\n");

            // 發言日期
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(1)).append("</th>\n");
            html.append("                    <td>").append(dto.getDate()).append("</td>\n");
            html.append("                </tr>\n");

            // 發言時間
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(2)).append("</th>\n");
            html.append("                    <td>").append(dto.getTime()).append("</td>\n");
            html.append("                </tr>\n");

            // 發言人
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(3)).append("</th>\n");
            html.append("                    <td>").append(dto.getSpeaker()).append("</td>\n");
            html.append("                </tr>\n");

            // 發言人職稱
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(4)).append("</th>\n");
            html.append("                    <td>").append(dto.getSpeakerTitle()).append("</td>\n");
            html.append("                </tr>\n");

            // 發言人電話
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(5)).append("</th>\n");
            html.append("                    <td>").append(dto.getSpeakerPhone()).append("</td>\n");
            html.append("                </tr>\n");

            // 主旨
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(6)).append("</th>\n");
            html.append("                    <td>").append(dto.getSubject()).append("</td>\n");
            html.append("                </tr>\n");

            // 符合條款
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(7)).append("</th>\n");
            html.append("                    <td>").append(dto.getMatchItem()).append("</td>\n");
            html.append("                </tr>\n");

            // 事實發生日
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(8)).append("</th>\n");
            html.append("                    <td>").append(dto.getEventDate()).append("</td>\n");
            html.append("                </tr>\n");

            // 說明 (可能有多行文字，需要保留換行)
            html.append("                <tr>\n");
            html.append("                    <th>").append(titleNames.get(9)).append("</th>\n");
            html.append("                    <td>").append(dto.getDescription().replace("\n", "<br>")).append("</td>\n");
            html.append("                </tr>\n");
        }

        html.append("            </table>\n");
        html.append("        </div>\n");

        // 頁尾
        html.append("        <div class=\"footer\">\n");
        html.append("            <div>").append(footer).append("\n");
        html.append("            </div>\n");
        html.append("        </div>\n");

        html.append("    </div>\n");
        html.append("</div>");

        return html.toString();
    }
}
