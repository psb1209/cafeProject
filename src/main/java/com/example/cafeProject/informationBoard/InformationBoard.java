package com.example.cafeProject.informationBoard;

//    정보 게시판 기능 정리
//    1. 회원삭제하면 관련 게시글 모두 삭제
//    2. 게시글 삭제하면 관련 댓글 모두 삭제
//    3. 사용자가 페이징 기준 선택 - size(한페이지에 몇개), sort(페이지 정렬 순서)
//    4. 게시글의 삭제 페이지 없이 경고문만 뛰어주고 삭제(댓글처럼) + 본인이 작성한 게시글만 삭제 & 관리자만 삭제 버튼 따로 생성(for 보안)
//    5. 회원등급 반영 - 일반, 성실, 우수, 최우수

import com.example.cafeProject.member.Member;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
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

    @CreationTimestamp
    private Timestamp createDate;

    @OnDelete(action = OnDeleteAction.CASCADE) //회원삭제하면 관련 게시글 모두 삭제되도록
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
