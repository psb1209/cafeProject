package com.example.cafeProject.informationBoard;

import com.example.cafeProject.informationBoardComment.InformationBoardComment;
import com.example.cafeProject.member.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    /*=============================== 각 게시판 공지글 ===================================*/
    @Column(nullable = false)
    private boolean subNotice = false;
    /*=====================================================================================*/

    @CreationTimestamp
    private Timestamp createDate;

    @OnDelete(action = OnDeleteAction.CASCADE) //회원삭제하면 관련 게시글 모두 삭제되도록
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId")
    private Member member;

    /*============================================== 대댓글 ===============================================*/
    @OneToMany(mappedBy = "informationBoard", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    //mappedBy = "informationBoard" --> 반대편 테이블의 외래키(Foreign Key)를 참조해서 조회용으로만 사용
    //cascade = CascadeType.REMOVE --> JPA가 댓글을 일일이 신경쓰면서 직접 삭제(db에서 지워지는 게 아님)
    //fetch = FetchType.LAZY --> 댓글이 당장 필요 없으면 가져오지 말고, 나중에 진짜로 부를 때 가져오게 함(성능향상)
    @OrderBy("id desc")
    private List<InformationBoardComment> commentList;
    /*============================================== 대댓글 ===============================================*/

    public void IncreaseViewCnt() {
        cnt++;
    }

    public static InformationBoard dtoToEntity(InformationBoardDTO informationBoardDTO, Member member) {
        InformationBoard informationBoard = new InformationBoard();
        informationBoard.setSubject(informationBoardDTO.getSubject());
        informationBoard.setContent(informationBoardDTO.getContent());
        informationBoard.setCnt(0);
        informationBoard.setMember(member);

        /*=============================== 각 게시판 공지글 ===================================*/
        informationBoard.setSubNotice(informationBoardDTO.isSubNotice());
        /*=====================================================================================*/

        return informationBoard;
    }




}
