package com.example.cafeProject.communityBoard;

import com.example.cafeProject.communityBoardComment.CommunityBoardComment;
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
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "communityBoard")
@Entity
public class CommunityBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String subject;

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    private int cnt;

    @CreationTimestamp
    private Timestamp createDate;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private Member member;

    /*=============================== 각 게시판 공지글 ===================================*/
    @Column(nullable = false)
    private boolean subNotice = false;
    /*=====================================================================================*/

    @OneToMany(mappedBy = "communityBoard", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("id desc")
    private List<CommunityBoardComment> commentList;

    public static CommunityBoard dtoToEntity(CommunityBoardDTO communityBoardDTO, Member member) {
        CommunityBoard communityBoard = new CommunityBoard();
        communityBoard.setSubject(communityBoardDTO.getSubject());
        communityBoard.setContent(communityBoardDTO.getContent());
        communityBoard.setCnt(0);
        communityBoard.setMember(member);

        /*=============================== 각 게시판 공지글 ===================================*/
        communityBoard.setSubNotice(communityBoardDTO.isSubNotice());
        /*=====================================================================================*/


        return communityBoard;
    }



}
