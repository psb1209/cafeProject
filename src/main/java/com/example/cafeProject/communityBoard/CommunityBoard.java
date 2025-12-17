package com.example.cafeProject.communityBoard;


import com.example.cafeProject.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "CommunityBoard")
public class CommunityBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 50)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String content;


    private int cnt;

    @CreationTimestamp
    private Timestamp createDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid")
    private Member member;
}
