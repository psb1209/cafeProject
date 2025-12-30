package com.example.cafeProject.operationBoard;

import com.example.cafeProject.operationBoard.OperationBoard;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.noticeBoardComment.NoticeBoardComment;
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
        return operationBoard;
    }
}
