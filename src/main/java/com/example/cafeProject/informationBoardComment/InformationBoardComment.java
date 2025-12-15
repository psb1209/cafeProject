package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.informationBoard.InformationBoard;
import com.example.cafeProject.member.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private InformationBoard informationBoard;


    public InformationBoardComment dtoToEntity(InformationBoardCommentDTO informationBoardCommentDTO,
                                               Member member, InformationBoard informationBoard) {

        InformationBoardComment informationBoardComment = new InformationBoardComment();

        informationBoardComment.setId(informationBoardCommentDTO.getId());
        informationBoardComment.setContent(informationBoardCommentDTO.getContent());
        informationBoardComment.setCreateDate(informationBoardCommentDTO.getCreateDate());
        informationBoardComment.setMember(member);
        informationBoardComment.setInformationBoard(informationBoard);

        return informationBoardComment;

    }

}
