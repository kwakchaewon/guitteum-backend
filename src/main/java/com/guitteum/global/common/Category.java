package com.guitteum.global.common;

import java.util.List;
import java.util.Map;

public enum Category {

    ECONOMY("경제", List.of("경제", "성장", "투자", "수출", "일자리", "반도체", "산업", "기업", "무역", "금융", "예산", "세금")),
    FOREIGN("외교", List.of("외교", "동맹", "협력", "정상회담", "국제", "평화", "한미", "아세안", "유엔", "정상")),
    WELFARE("복지", List.of("복지", "교육", "의료", "출산", "민생", "돌봄", "연금", "주거", "보육", "청년")),
    DEFENSE("안보", List.of("안보", "국방", "군사", "미사일", "북한", "핵", "군", "전쟁", "방위", "사이버")),
    ENVIRONMENT("환경", List.of("기후", "탄소", "에너지", "환경", "녹색", "재생", "탄소중립", "친환경")),
    ETC("기타", List.of());

    private final String displayName;
    private final List<String> keywords;

    Category(String displayName, List<String> keywords) {
        this.displayName = displayName;
        this.keywords = keywords;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public static Category classify(String content) {
        if (content == null || content.isBlank()) {
            return ETC;
        }

        Category best = ETC;
        int maxCount = 0;

        for (Category cat : values()) {
            if (cat == ETC) continue;
            int count = 0;
            for (String keyword : cat.keywords) {
                int idx = 0;
                while ((idx = content.indexOf(keyword, idx)) != -1) {
                    count++;
                    idx += keyword.length();
                }
            }
            if (count > maxCount) {
                maxCount = count;
                best = cat;
            }
        }

        return best;
    }
}
