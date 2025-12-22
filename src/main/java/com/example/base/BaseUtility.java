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

    /** "사과" -> "ㅅㅏㄱㅘ", "삭제" -> "ㅅㅏㄱㅈㅔ" */
    public static String toKey(String s) {
        if (s == null || s.isBlank()) return "";

        // StringBuilder를 통해 문자열 합치기
        // "내용" + "내용" 문자열 합치기랑 똑같음
        StringBuilder out = new StringBuilder(s.length() * 3);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            // 공백 제거
            if (Character.isWhitespace(ch)) continue;

            // 한글 음절을 잘라서 저장
            if (ch >= BASE && ch <= LAST) {
                int code = ch - BASE;
                int l = code / (21 * 28);        // 초성
                int v = (code % (21 * 28)) / 28; // 중성
                int t = code % 28;               // 종성

                out.append(CHO[l]).append(JUNG[v]);
                if (t != 0) out.append(JONG[t]);
                continue;
            }

            // 영문 소문자, 숫자는 유지, 그 외는 버림
            if (Character.isAlphabetic(ch)) out.append(Character.toLowerCase(ch));
            else if (Character.isDigit(ch)) out.append(ch);
        }
        return out.toString();
    }

    /** "사과" -> "ㅅㄱ", "삭제" -> "ㅅㅈ" */
    public static String toChosungKey(String s) {
        if (s == null || s.isBlank()) return "";

        // StringBuilder를 통해 문자열 합치기
        // "내용" + "내용" 문자열 합치기랑 똑같음
        StringBuilder out = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            if (Character.isWhitespace(ch)) continue;

            // 초성 음절을 잘라서 저장
            if (ch >= BASE && ch <= LAST) {
                int code = ch - BASE;
                int l = code / (21 * 28);
                out.append(CHO[l]);
            }
        }
        return out.toString();
    }

    /** 들어온 겹자음을 분해 */
    public static String jaeumCutter(char ch) {
        switch (ch) {
            case 'ㄳ': return "ㄱㅅ";
            case 'ㄵ': return "ㄴㅈ";
            case 'ㄶ': return "ㄴㅎ";
            case 'ㄺ': return "ㄹㄱ";
            case 'ㄻ': return "ㄹㅁ";
            case 'ㄼ': return "ㄹㅂ";
            case 'ㄽ': return "ㄹㅅ";
            case 'ㄾ': return "ㄹㅌ";
            case 'ㄿ': return "ㄹㅍ";
            case 'ㅀ': return "ㄹㅎ";
            case 'ㅄ': return "ㅂㅅ";
            default: return String.valueOf(ch);
        }
    }

    /** 문자열에서 겹자음을 전부 분해 후 초성만 남김, 나머지는 전부 버림 */
    public static String jaeumBreaker(String s) {
        if (s == null || s.isBlank()) return "";

        StringBuilder out = new StringBuilder(s.length() * 2);
        for (char ch : s.toCharArray()) {
            if (Character.isWhitespace(ch)) continue;
            for (char c : jaeumCutter(ch).toCharArray()) {
                if (isChosungJamo(c)) out.append(c);  // 초성 19개만 통과
            }
        }
        return out.toString();
    }

    /** 들어온 char값이 초성 자모 배열의 값과 일치하는지 확인 */
    private static boolean isChosungJamo(char c) {
        for (String s : CHO) { // 초성에 들어가있는지 검사
            if (s.charAt(0) == c) return true;
        }
        return false;
    }

    /** 사용자가 'ㅅㄱ' 같이 초성만 입력했는지 판별 */
    public static boolean isChosungQuery(String s) {
        if (s == null || s.isBlank()) return false;

        for (char ch : s.toCharArray()) {
            if (Character.isWhitespace(ch)) continue;
            for (char c : jaeumCutter(ch).toCharArray())
                if (!isChosungJamo(c)) return false;
        }
        return true;
    }
}
