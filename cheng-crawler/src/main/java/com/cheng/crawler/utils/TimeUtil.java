package com.cheng.crawler.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cheng
 * @since 2024/3/7 23:59
 **/
@Slf4j
public class TimeUtil {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String HH_MM_SS = "HH:mm:ss";

    /**
     * 轉換台灣民國年格式的日期為西元日期格式（yyyy-MM-dd）。
     * <p>
     * 可用以下格式：
     * <ul>
     *     <li>112年05月03日</li>
     *     <li>112 年 5 月 3 日</li>
     *     <li>民國99年12月25日</li>
     *     <li>191 年 1 月 9 日</li>
     *     <li>114.2.14</li>
     *     <li>民國 114 年 02 月 14 日</li>
     *     <li>112/10/03</li>
     *     <li>109-08-27</li>
     * </ul>
     * <p>
     * 無論是否有額外空格、是否有「民國」字串，或是分隔符號為「年」、「.」或「/」都能正確解析。
     *
     * @param input 轉換的民國年格式日期字串，例如 "112年05月03日"、"民國 112 年 5 月 3 日" 或 "112/10/03"
     * @return 轉換後的西元日期字串，格式為 "yyyy-MM-dd"；若格式不符則回傳原始輸入字串
     */
    public static String convertTaiwanDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        // 使用正規表達式處理多種格式
        // 修改正則表達式，移除 $ 結尾符號，允許「日」後面有其他文字
        String regex = "^\\s*(?:(?:民國|中華民國)\\s*)?(\\d{1,3})\\s*(?:年|[./-])\\s*(\\d{1,2})\\s*(?:月|[./-])\\s*(\\d{1,2})\\s*日?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input.trim());

        if (matcher.find()) {
            try {
                int taiwanYear = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));

                // 確保年份、月份和日期在合理範圍內
                if (taiwanYear >= 1 && taiwanYear <= 999 &&
                        month >= 1 && month <= 12 &&
                        day >= 1 && day <= 31) {

                    // 處理前導零的情況（如 099 年）
                    int gregorianYear = taiwanYear + 1911;

                    // 格式化輸出 YYYY-MM-DD
                    return String.format("%04d-%02d-%02d", gregorianYear, month, day);
                }
            } catch (NumberFormatException e) {
                log.warn("ERR:{}", e.getMessage());
                return input;
            }
        }

        return input;
    }

    /**
     * 取得今天日期
     *
     * @return {@link Date}
     */
    public static Date getCurrDate() {
        return localDate2Date(LocalDate.now());
    }

    /**
     * 取得現在時間
     *
     * @return {@link Date}
     */
    public static Date getCurrDateTime() {
        return localDateTime2Date(LocalDateTime.now());
    }

    /**
     * 取得今天日期
     *
     * @return {@link String}
     */
    public static String getCurrDateStr() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD));
    }

    /**
     * 取得現在時間
     *
     * @return {@link String}
     */
    public static String getCurrDateTimeStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS));
    }

    /**
     * 取時間最大值
     *
     * @return {@link String}
     */
    public static String getTimeMaxStr() {
        return LocalTime.MAX.format(DateTimeFormatter.ofPattern(HH_MM_SS));
    }

    /**
     * 取時間最小值
     *
     * @return {@link String}
     */
    public static String getTimeMinStr() {
        return LocalTime.MIN.format(DateTimeFormatter.ofPattern(HH_MM_SS));
    }

    /**
     * Date轉LocalDate
     *
     * @param date {@link Date}
     */
    public static LocalDate date2LocalDate(Date date) {
        if (null == date) {
            log.warn("date2LocalDate ===> date is null");
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Date轉LocalDateTime
     *
     * @param date {@link Date}
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        if (null == date) {
            throw new IllegalArgumentException("date2LocalDateTime ===> date is null");
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * LocalDate轉Date
     *
     * @param localDate {@link LocalDate}
     * @return {@link Date}
     */
    public static Date localDate2Date(LocalDate localDate) {
        if (null == localDate) {
            log.warn("===> localDate is null");
            return null;
        }
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public static String toLDTString(LocalDateTime date) {
        return Optional.ofNullable(date)
                .map(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)::format)
                .orElse("");
    }

    public static LocalDateTime toLDT(String date) {
        return Optional.ofNullable(date)
                .map(DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS)::parse)
                .map(LocalDateTime::from)
                .orElse(null);
    }

    /**
     * LocalTime轉Date (日期會是當天日期)
     *
     * @param localTime {@link LocalTime}
     * @return {@link Date}
     */
    public static Date localTime2Date(LocalTime localTime) {
        if (localTime == null) {
            log.warn("===> localTime is null");
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), localTime);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate轉Date
     *
     * @param localDateTime {@link LocalDateTime}
     * @return {@link Date}
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            log.warn("===> localDateTime is null");
            return null;
        }
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 返回指定星期幾的下一次出現日期及給定的重複時間
     *
     * @param dayOfWeek    目標星期幾的字串 (例如："TUESDAY")
     * @param repeatedTime 設定的時間
     * @param isNextOrSame 是否指定下一週的星期幾
     * @return 表示下一次出現的指定星期幾及給定時間的 LocalDateTime
     * @throws IllegalArgumentException 如果指定的星期幾無效，則拋出此異常
     */
    public static LocalDateTime getNextDayOfWeek(String dayOfWeek, LocalTime repeatedTime, boolean isNextOrSame) {
        DayOfWeek targetDayOfWeek = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        LocalDate now = LocalDate.now();
        // 找下一個或目前的目標星期幾的日期，如果當天已經超過目標時間，則將目標日期延後一週
        LocalDate nextDayOfWeek = now.with(isNextOrSame ?
                TemporalAdjusters.nextOrSame(targetDayOfWeek) :
                TemporalAdjusters.next(targetDayOfWeek));

        return LocalDateTime.of(nextDayOfWeek, repeatedTime);
    }

    public static LocalDateTime getNextDayOfWeek(String dayOfWeek, Date repeatedTime, boolean isNextOrSame) {
        LocalDateTime localDateTime = date2LocalDateTime(repeatedTime);
        return getNextDayOfWeek(dayOfWeek, localDateTime.toLocalTime(), isNextOrSame);
    }

    /**
     * 返回指定星期幾的下一次出現日期及給定的重複時間，找下一個或目前的目標星期幾的日期，如果當天已經超過目標時間，則將目標日期延後一週
     *
     * @param dayOfWeek    目標星期幾的字串 (例如："TUESDAY")
     * @param repeatedTime 設定的時間
     * @return 表示下一次出現的指定星期幾及給定時間的 LocalDateTime
     * @throws IllegalArgumentException 如果指定的星期幾無效，則拋出此異常
     */
    public static LocalDateTime getNextDayOfWeek(String dayOfWeek, LocalTime repeatedTime) {
        return getNextDayOfWeek(dayOfWeek, repeatedTime, true);
    }

    /**
     * 返回當月或下個月指定日期及給定時間的 LocalDateTime
     *
     * @param dayOfMonth   每月的幾號
     * @param repeatedTime 設定的時間
     * @return 表示當月或下一月指定日期及給定時間的 LocalDateTime
     * @throws IllegalArgumentException 如果指定的日期無效，則拋出此異常
     */
    public static LocalDateTime getNextDayOfMonth(int dayOfMonth, LocalTime repeatedTime) {
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        LocalDate targetDate;

        if (dayOfMonth < 1 || dayOfMonth > 31) {
            throw new IllegalArgumentException("每月的日期必須在 1 到 31 之間");
        }

        // 如果當月有指定日期並且未過指定時間，則使用當月日期
        if (dayOfMonth <= currentMonth.lengthOfMonth() && (now.getDayOfMonth() < dayOfMonth || (now.getDayOfMonth() == dayOfMonth && LocalTime.now().isBefore(repeatedTime)))) {
            targetDate = LocalDate.of(now.getYear(), now.getMonth(), dayOfMonth);
        } else {
            // 否則使用下個月日期
            YearMonth nextMonth = currentMonth.plusMonths(1);
            if (dayOfMonth > nextMonth.lengthOfMonth()) {
                // 如果下個月沒有該日期，則設定為下個月的最後一天
                dayOfMonth = nextMonth.lengthOfMonth();
            }
            targetDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), dayOfMonth);
        }

        return LocalDateTime.of(targetDate, repeatedTime);
    }

    /**
     * 減少指定分鐘數的日期時間
     *
     * @param date    {@link Date}
     * @param minutes 要減少的分鐘數
     * @return 減少指定分鐘數後的新日期時間
     */
    public static Date subtractMinutes(Date date, int minutes) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime newLocalDateTime = localDateTime.minusMinutes(minutes);
        ZonedDateTime zonedDateTime = newLocalDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 字串轉日期，預設格式為 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate 日期字串
     * @return {@link Date}
     */
    public static Date stringToDate(String strDate) throws ParseException {
        return stringToDate(strDate, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 字串轉日期
     *
     * @param strDate 日期字串
     * @param pattern 日期格式
     * @return {@link Date}
     */
    public static Date stringToDate(String strDate, String pattern) throws ParseException {
        if (StringUtils.isEmpty(strDate)) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.parse(strDate);
    }

    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = YYYY_MM_DD_HH_MM_SS;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.parseLong(seconds) * 1000));
    }

    /**
     * 將 Timestamp 轉換為 Date。
     *
     * @param timestamp 要轉換的 Timestamp
     * @return 轉換後的 Date，如果傳入的 Timestamp 為 null，則返回 null
     */
    public static Date timestampToDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp.getTime());
    }

    /**
     * 將 Date 轉換為 Timestamp。
     *
     * @param date 要轉換的 Date
     * @return 轉換後的 Timestamp，如果傳入的 Date 為 null，則返回 null
     */
    public static Timestamp dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        return new Timestamp(date.getTime());
    }

    /**
     * 增加日期天數，時間不變
     * <p>
     * 此方法接受一個 Date 物件，取時間部分（小時和分鐘），然後將這個時間應用於當下日期的下一天。
     * 這常用於需要將某一具體時間點的活動或任務推遲到下一天的相同時間。
     *
     * @param days                增加的天數
     * @param originalTriggerTime 原始的觸發時間
     * @return Date 新的觸發時間，設定為明天的對應時刻
     */
    public static Date shiftTimeByDays(Date originalTriggerTime, int days) {
        if (originalTriggerTime == null) {
            log.warn("===> originalTriggerTime is null");
            return null;
        }

        // 將 Date 轉換為 LocalDateTime
        LocalDateTime oldTime = date2LocalDateTime(originalTriggerTime);

        // 取得當前日期的下一天
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(days);

        // 建立新的 LocalDateTime，保持原有時分，日期為明天
        assert oldTime != null;
        LocalDateTime newTime = tomorrow
                .withHour(oldTime.getHour())
                .withMinute(oldTime.getMinute());

        // 將 LocalDateTime 轉換回 Date
        return Date.from(newTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 增加一天日期天數，時間不變
     * <p>
     * 此方法接受一個 Date 物件，取時間部分（小時和分鐘），然後將這個時間應用於當下日期的下一天。
     * 這常用於需要將某一具體時間點的活動或任務推遲到下一天的相同時間。
     *
     * @param originalTriggerTime 原始的觸發時間
     * @return Date 新的觸發時間，設定為明天的對應時刻
     */
    public static Date shiftTimeByDays(Date originalTriggerTime) {
        return shiftTimeByDays(originalTriggerTime, 1);
    }

    public static void main(String[] args) {
        System.out.println(getCurrDate());
        System.out.println(getCurrDateTime());
        System.out.println(getCurrDateStr());
        System.out.println(getCurrDateTimeStr());
        System.out.println(getTimeMaxStr());
        System.out.println(getTimeMinStr());

        LocalTime repeatedTime = LocalTime.of(15, 30);
        DateTimeFormatter formatterYMDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String dayOfWeek = "WEDNESDAY";
        LocalDateTime date = getNextDayOfWeek(dayOfWeek, repeatedTime);
        System.out.println("下一次 " + dayOfWeek + " 的日期和時間為: " + date.format(formatterYMDS));

        int dayOfMonth = 10;
        LocalDateTime dateTime = getNextDayOfMonth(dayOfMonth, repeatedTime);
        System.out.println("下一次 " + dayOfMonth + " 號的日期和時間為: " + dateTime.format(formatterYMDS));
    }

}
