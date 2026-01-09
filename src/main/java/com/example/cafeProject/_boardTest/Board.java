package com.example.cafeProject._boardTest;

import com.example.base.BaseEntity;
import com.example.cafeProject._cafeTest.Cafe;
import com.example.cafeProject.member.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "boards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_boards_cafeid_code",
                        columnNames = {"cafeid", "code"}
                )
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Board extends BaseEntity {

    @Column(nullable = false, length = 100, updatable = false)
    private String name;

    @Column(nullable = false, length = 300)
    private String nameKey; // 검색용 초성 깬 문자열

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String description;

    @Column(length = 255)
    private String imgName;

    @Column(nullable = false, length = 20, updatable = false)
    private String code; // 링크에 표시될 코드

    @Column(nullable = false)
    private boolean enabled; // 활성화 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType writeRole; // 작성 권한

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType readRole; // 보기 권한

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cafeid", nullable = false)
    private Cafe cafe;
}
