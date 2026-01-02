package com.example.cafeProject.communityBoardComment;

import com.example.cafeProject.communityBoard.CommunityBoard;
import com.example.cafeProject.member.Member;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "communityBoardComment")
@Entity
public class CommunityBoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Lob
    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    private Timestamp createDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private Member member;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "communityBoardId")
    private CommunityBoard communityBoard;

    private int ref;
    private int step;
    private int level;

    public static CommunityBoardComment dtoToEntity(CommunityBoardCommentDTO communityBoardCommentDTO,
                                                    Member member, CommunityBoard communityBoard) {

        CommunityBoardComment communityBoardComment = new CommunityBoardComment();
        communityBoardComment.setContent(communityBoardCommentDTO.getContent());
        communityBoardComment.setMember(member);
        communityBoardComment.setCommunityBoard(communityBoard);
        return communityBoardComment;
    }
}
