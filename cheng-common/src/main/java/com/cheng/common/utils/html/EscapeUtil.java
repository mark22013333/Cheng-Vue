package com.cheng.common.utils.html;

import com.cheng.common.utils.StringUtils;

/**
 * 轉譯和反轉譯工具類
 *
 * @author cheng
 */
public class EscapeUtil {
    public static final String RE_HTML_MARK = "(<[^<]*?>)|(<[\\s]*?/[^<]*?>)|(<[^<]*?/[\\s]*?>)";

    private static final char[][] TEXT = new char[64][];

    static {
        for (int i = 0; i < 64; i++) {
            TEXT[i] = new char[]{(char) i};
        }

        // special HTML characters
        TEXT['\''] = "&#039;".toCharArray(); // 單引號
        TEXT['"'] = "&#34;".toCharArray(); // 雙引號
        TEXT['&'] = "&#38;".toCharArray(); // &符
        TEXT['<'] = "&#60;".toCharArray(); // 小於號
        TEXT['>'] = "&#62;".toCharArray(); // 大於號
    }

    /**
     * 轉譯文字中的HTML字串為安全的字串
     *
     * @param text 被轉譯的文字
     * @return 轉譯後的文字
     */
    public static String escape(String text) {
        return encode(text);
    }

    /**
     * 還原被轉譯的HTML特殊字串
     *
     * @param content 包含轉譯符號的HTML内容
     * @return 轉換後的字串
     */
    public static String unescape(String content) {
        return decode(content);
    }

    /**
     * 清除所有HTML標籤，但是不刪除標籤内的内容
     *
     * @param content 文字
     * @return 清除標籤後的文字
     */
    public static String clean(String content) {
        return new HTMLFilter().filter(content);
    }

    /**
     * Escape編碼
     *
     * @param text 被編碼的文字
     * @return 編碼後的字串
     */
    private static String encode(String text) {
        if (StringUtils.isEmpty(text)) {
            return StringUtils.EMPTY;
        }

        final StringBuilder tmp = new StringBuilder(text.length() * 6);
        char c;
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            if (c < 256) {
                tmp.append("%");
                if (c < 16) {
                    tmp.append("0");
                }
                tmp.append(Integer.toString(c, 16));
            } else {
                tmp.append("%u");
                if (c <= 0xfff) {
                    // issue#I49JU8@Gitee
                    tmp.append("0");
                }
                tmp.append(Integer.toString(c, 16));
            }
        }
        return tmp.toString();
    }

    /**
     * Escape解碼
     *
     * @param content 被轉譯的内容
     * @return 解碼後的字串
     */
    public static String decode(String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }

        StringBuilder tmp = new StringBuilder(content.length());
        int lastPos = 0, pos = 0;
        char ch;
        while (lastPos < content.length()) {
            pos = content.indexOf("%", lastPos);
            if (pos == lastPos) {
                if (content.charAt(pos + 1) == 'u') {
                    ch = (char) Integer.parseInt(content.substring(pos + 2, pos + 6), 16);
                    tmp.append(ch);
                    lastPos = pos + 6;
                } else {
                    ch = (char) Integer.parseInt(content.substring(pos + 1, pos + 3), 16);
                    tmp.append(ch);
                    lastPos = pos + 3;
                }
            } else {
                if (pos == -1) {
                    tmp.append(content.substring(lastPos));
                    lastPos = content.length();
                } else {
                    tmp.append(content, lastPos, pos);
                    lastPos = pos;
                }
            }
        }
        return tmp.toString();
    }

    public static void main(String[] args) {
        String html = "<script>alert(1);</script>";
        String escape = EscapeUtil.escape(html);
        // String html = "<scr<script>ipt>alert(\"XSS\")</scr<script>ipt>";
        // String html = "<123";
        // String html = "123>";
        System.out.println("clean: " + EscapeUtil.clean(html));
        System.out.println("escape: " + escape);
        System.out.println("unescape: " + EscapeUtil.unescape(escape));
    }
}
