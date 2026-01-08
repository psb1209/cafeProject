package com.example.cafeProject._postTest;

import com.example.base.BaseEntity;
import com.example.cafeProject._boardTest.Board;
import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.sql.Timestamp;

@Entity
@Table(name = "posts")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 300)
    private String titleKey; // 검색용 초성 깬 문자열

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private int cnt = 0;     // 조회수

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean notice = false;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean deleted = false;

    private Timestamp deletedAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardid")
    private Board board;
}
