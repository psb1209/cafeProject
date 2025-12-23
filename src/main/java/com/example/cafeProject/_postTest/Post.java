package com.example.cafeProject._postTest;

import com.example.base.BaseEntity;
import com.example.cafeProject._boardTest.Board;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

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

    private Integer cnt;

    @Builder.Default
    private boolean notice = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardid")
    private Board board;
}
