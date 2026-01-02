package com.example.cafeProject._noticeBoard;

import com.example.cafeProject.member.Member;
import com.example.cafeProject._noticeBoardComment.NoticeBoardComment1;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "noticeBoard")
public class NoticeBoard1 {
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid")
    private Member member;

    //게시글 제목 옆에 댓글 수 표시
    @OneToMany(mappedBy = "noticeBoard", cascade = CascadeType.REMOVE)
    private List<NoticeBoardComment1> noticeBoardCommentList;

//    @Transient //db에 데이터를 저장할 목적이 아니라 화면에 숫자 띄우는 용도로 사용함
//    private int likeCnt;

    @Transient
    private int commentCnt;
}
