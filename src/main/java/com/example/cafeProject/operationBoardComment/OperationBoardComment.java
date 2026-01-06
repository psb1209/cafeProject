package com.example.cafeProject.operationBoardComment;

import com.example.cafeProject.operationBoard.OperationBoard;
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
@Table(name = "operationBoardComment")
@Entity
public class OperationBoardComment {

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
    @JoinColumn(name = "operationBoardId")
    private OperationBoard operationBoard;

    /*============================================== 대댓글 ===============================================*/
    private int ref;
    private int step;
    private int level;
    /*============================================== 대댓글 ===============================================*/

    public static OperationBoardComment dtoToEntity(
            OperationBoardCommentDTO operationBoardCommentDTO, Member member, OperationBoard operationBoard
    ) {
        OperationBoardComment operationBoardComment = new OperationBoardComment();
        operationBoardComment.setContent(operationBoardCommentDTO.getContent());
        operationBoardComment.setMember(member);
        operationBoardComment.setOperationBoard(operationBoard);
        /*============================================== 대댓글 추가사항 ===============================================*/
        // service에서 넘긴 값 받아오기
        operationBoardComment.setRef(operationBoardCommentDTO.getRef());
        operationBoardComment.setStep(operationBoardCommentDTO.getStep());
        operationBoardComment.setLevel(operationBoardCommentDTO.getLevel());
        /*============================================== 대댓글 추가사항 ===============================================*/
        return operationBoardComment;
    }
}
