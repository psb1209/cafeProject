package com.example.base;

public class BaseUtility {
    private static final int BASE = 0xAC00; // 가
    private static final int LAST = 0xD7A3; // 힣

    private static final String[] CHO = {
            "ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"
    };
    private static final String[] JUNG = {
            "ㅏ","ㅐ","ㅑ","ㅒ","ㅓ","ㅔ","ㅕ","ㅖ","ㅗ","ㅘ","ㅙ","ㅚ","ㅛ","ㅜ","ㅝ","ㅞ","ㅟ","ㅠ","ㅡ","ㅢ","ㅣ"
    };
    private static final String[] JONG = {
            "", "ㄱ","ㄲ","ㄳ","ㄴ","ㄵ","ㄶ","ㄷ","ㄹ","ㄺ","ㄻ","ㄼ","ㄽ","ㄾ","ㄿ","ㅀ",
            "ㅁ","ㅂ","ㅄ","ㅅ","ㅆ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"
    };

    /** "사과" -> "ㅅㅏㄱㅘ", "삭" -> "ㅅㅏㄱ" */
    public static String toKey(String s) {
        if (s == null || s.isBlank()) return "";

        StringBuilder out = new StringBuilder(s.length() * 3);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            // 공백 제거
            if (Character.isWhitespace(ch)) continue;

            // 한글 음절
            if (ch >= BASE && ch <= LAST) {
                int code = ch - BASE;
                int l = code / (21 * 28);
                int v = (code % (21 * 28)) / 28;
                int t = code % 28;

                out.append(CHO[l]).append(JUNG[v]);
                if (t != 0) out.append(JONG[t]);
                continue;
            }

            // 영문은 소문자, 숫자는 유지
            if (Character.isAlphabetic(ch)) out.append(Character.toLowerCase(ch));
            // 그 외 특수문자는 버림
            else if (Character.isDigit(ch)) out.append(ch);
        }
        return out.toString();
    }
}
