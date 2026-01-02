package com.example.cafeProject.operationBoard;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
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
@Table(name = "operationBoard")
@Entity
public class OperationBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String subject;

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    private int cnt;
/*=============================== 각 게시판 공지글 ===================================*/
    @Column(nullable = false)
    private boolean subNotice = false;
/*=====================================================================================*/
    @CreationTimestamp
    private Timestamp createDate;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private Member member;

    @OneToMany(mappedBy = "operationBoard", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("id desc")
    private List<OperationBoardComment> commentList;
    
    public static OperationBoard dtoToEntity(OperationBoardDTO operationBoardDTO, Member member) {
        OperationBoard operationBoard = new OperationBoard();
        operationBoard.setSubject(operationBoardDTO.getSubject());
        operationBoard.setContent(operationBoardDTO.getContent());
        operationBoard.setCnt(0);
        operationBoard.setMember(member);

        /*=============================== 각 게시판 공지글 ===================================*/
        operationBoard.setSubNotice(operationBoardDTO.isSubNotice());
        /*=====================================================================================*/

        return operationBoard;
    }
}
