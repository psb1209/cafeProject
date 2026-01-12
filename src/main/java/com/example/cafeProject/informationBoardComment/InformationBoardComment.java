package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.informationBoard.InformationBoard;
import com.example.cafeProject.member.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@NoArgsConstructor
@Data
@Entity
public class InformationBoardComment {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE) // 해당 게시글 삭제시 댓글 모두 삭제(DB에서 직접 삭제) + 테이블 drop하고 재시작
    @JoinColumn(name = "informationBoardId")
    private InformationBoard informationBoard;

    /*============================================== 대댓글 ===============================================*/
    private int ref;
    private int step;
    private int level;
    /*============================================== 대댓글 ===============================================*/


    public static InformationBoardComment dtoToEntity(InformationBoardCommentDTO informationBoardCommentDTO,
                                               Member member, InformationBoard informationBoard) {

        InformationBoardComment informationBoardComment = new InformationBoardComment();
        informationBoardComment.setContent(informationBoardCommentDTO.getContent());
        informationBoardComment.setMember(member);
        informationBoardComment.setInformationBoard(informationBoard);
        /*============================================== 대댓글 추가사항 ===============================================*/
        // service에서 넘긴 값 받아오기
        informationBoardComment.setRef(informationBoardCommentDTO.getRef());
        informationBoardComment.setStep(informationBoardCommentDTO.getStep());
        informationBoardComment.setLevel(informationBoardCommentDTO.getLevel());
        /*============================================== 대댓글 추가사항 ===============================================*/
        return informationBoardComment;
    }

}
