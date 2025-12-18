package com.example.cafeProject._boardTest;

import com.example.base.BaseEntity;
import com.example.cafeProject.member.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "Boards")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Board extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String description;

    @Column(length = 255)
    private String imgName;

    @Column(nullable = false, length = 20, unique = true, updatable = false)
    private String code; // 링크에 표시될 코드

    @Column(nullable = false)
    private boolean enabled; // 활성화 여부

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType writeRole; // 작성 권한

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType readRole; // 보기 권한
}
