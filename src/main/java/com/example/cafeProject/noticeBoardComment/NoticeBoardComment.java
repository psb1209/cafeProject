package com.example.cafeProject.noticeBoardComment;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.noticeBoard.NoticeBoard;
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
@Table(name = "noticeBoardComment")
@Entity
public class NoticeBoardComment {

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
    @JoinColumn(name = "noticeBoardId")
    private NoticeBoard noticeBoard;

    private int ref;
    private int step;
    private int level;

    public static NoticeBoardComment dtoToEntity(
            NoticeBoardCommentDTO noticeBoardCommentDTO,
            Member member, NoticeBoard noticeBoard) {
        NoticeBoardComment noticeBoardComment = new NoticeBoardComment();
        noticeBoardComment.setContent(noticeBoardCommentDTO.getContent());
        noticeBoardComment.setMember(member);
        noticeBoardComment.setNoticeBoard(noticeBoard);
        noticeBoardComment.setRef(noticeBoardCommentDTO.getRef());
        noticeBoardComment.setStep(noticeBoardCommentDTO.getStep());
        noticeBoardComment.setLevel(noticeBoardCommentDTO.getLevel());
        return noticeBoardComment;
    }
}
