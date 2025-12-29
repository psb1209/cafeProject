package com.example.cafeProject.boardLike;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.noticeBoard.NoticeBoard;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "boardLike", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"board_type", "board_id", "member_id"})
})

public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false)
    private BoardType boardType;

    @Column(name = "board_id", nullable = false)
    private int boardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
}


