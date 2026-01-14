package com.example.cafeProject.noticeBoard;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.noticeBoardComment.NoticeBoardComment;
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
@Table(name = "noticeBoard")
@Entity
public class NoticeBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String subject;

    @Lob
    @Column(nullable = false, columnDefinition = "longtext")
    private String content;

    private int cnt;

    @Builder.Default
    @Column(nullable = false)
    private boolean subNotice = false;
    
    @CreationTimestamp
    private Timestamp createDate;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private Member member;

    @OneToMany(mappedBy = "noticeBoard", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("id desc")
    private List<NoticeBoardComment> commentList;
    
    public static NoticeBoard dtoToEntity(NoticeBoardDTO noticeBoardDTO, Member member) {
        NoticeBoard noticeBoard = new NoticeBoard();
        noticeBoard.setSubject(noticeBoardDTO.getSubject());
        noticeBoard.setContent(noticeBoardDTO.getContent());
        noticeBoard.setCnt(0);
        noticeBoard.setMember(member);
        noticeBoard.setSubNotice(noticeBoardDTO.isSubNotice());
        return noticeBoard;
    }
}
