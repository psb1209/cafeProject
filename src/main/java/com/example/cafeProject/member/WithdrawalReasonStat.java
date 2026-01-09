package com.example.cafeProject.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 탈퇴(삭제) 사유 통계용 DTO
 */
@Getter
@AllArgsConstructor
public class WithdrawalReasonStat {
    private final ReasonType reason;
    private final String label;
    private final long count;
    /** 0~100 (%) */
    private final double percent;
}
