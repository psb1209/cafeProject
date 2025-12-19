package com.example.cafeProject.informationBoard;


import com.example.cafeProject.member.Member;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.List;

@NoArgsConstructor
@Data
@Entity
public class InformationBoard {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;


    public static InformationBoard dtoToEntity(InformationBoardDTO informationBoardDTO, Member member) {
        InformationBoard informationBoard = new InformationBoard();
        informationBoard.setSubject(informationBoardDTO.getSubject());
        informationBoard.setContent(informationBoardDTO.getContent());
        informationBoard.setCnt(0);
        informationBoard.setMember(member);
        return informationBoard;
    }

    public void IncreaseViewCnt() {
        cnt++;
    }
}
