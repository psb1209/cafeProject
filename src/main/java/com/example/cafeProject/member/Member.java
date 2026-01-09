package com.example.cafeProject.member;

import com.example.base.BaseUtility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.PROTECTED)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Grade grade;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createDate;

    /**
     * 소프트 삭제 플래그
     * - true면 "탈퇴(삭제) 처리된 계정"으로 간주
     */
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean deleted;

    /** 삭제 처리 시각(soft delete 시각) */
    private Timestamp deletedDate;

    /** 삭제(탈퇴) 사유(선택) */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReasonType deleteReason;

    private int postCount;

    private int replyCount;

    public void increasePostCount() {
        postCount++;
    }

    public void increaseReplyCount() { replyCount++; }

    /**
     * 소프트 삭제 처리
     * - deleted=true
     * - deletedDate=now
     * - deleteReason=null이면 NONE으로 보정
     */
    public void softDelete(ReasonType reason) {
        this.deleted = true;
        this.deletedDate = new Timestamp(System.currentTimeMillis());
        this.deleteReason = (reason == null) ? ReasonType.NONE : reason;
    }

    @Override
    public String toString() {
        String deletedDateStr = (deletedDate == null)
                ? "null"
                : BaseUtility.formatTimestamp(deletedDate, "yyyy-MM-dd");
        return "[Member]: {[id: " + id +
                "][username: " + username +
                "][email: " + email +
                "][role: " + role +
                "][grade: " + grade +
                "][createDate: " + BaseUtility.formatTimestamp(createDate, "yyyy-MM-dd") +
                "][deleted: " + deleted +
                "][deletedDate: " + deletedDateStr +
                "][deleteReason: " + deleteReason +
                "][postCnt: " + postCount +
                "][replyCnt: " + replyCount + "]}";
    }
}
