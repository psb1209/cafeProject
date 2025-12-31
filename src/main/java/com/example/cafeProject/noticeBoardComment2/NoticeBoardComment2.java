package com.example.cafeProject.noticeBoardComment2;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.noticeBoard2.NoticeBoard2;
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
@Table(name = "noticeBoardComment2")
@Entity
public class NoticeBoardComment2 {

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
    private NoticeBoard2 noticeBoard2;

    private int ref;
    private int step;
    private int level;

    public static NoticeBoardComment2 dtoToEntity(NoticeBoardComment2DTO noticeBoardComment2DTO,
                                                    Member member, NoticeBoard2 noticeBoard2) {

        NoticeBoardComment2 noticeBoardComment2 = new NoticeBoardComment2();
        noticeBoardComment2.setContent(noticeBoardComment2DTO.getContent());
        noticeBoardComment2.setMember(member);
        noticeBoardComment2.setNoticeBoard2(noticeBoard2);
        return noticeBoardComment2;
    }
}
