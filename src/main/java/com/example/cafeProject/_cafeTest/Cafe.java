package com.example.cafeProject._cafeTest;

import com.example.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cafes")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Cafe extends BaseEntity {

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false, length = 300)
    private String nameKey; // 검색용 초성 깬 문자열

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String description;

    @Column(length = 20)
    private String topic;

    @Column(length = 255)
    private String imgName;

    @Column(nullable = false, length = 20, unique = true, updatable = false)
    private String code; // 링크에 표시될 코드

    @Column(nullable = false)
    private boolean enabled; // 활성화 여부
}
