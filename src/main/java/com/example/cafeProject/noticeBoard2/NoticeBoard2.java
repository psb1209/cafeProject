package com.example.cafeProject.noticeBoard2;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.noticeBoardComment2.NoticeBoardComment2;
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
@Table(name = "noticeBoard2")
@Entity
public class NoticeBoard2 {

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

    @OneToMany(mappedBy = "noticeBoard2", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @OrderBy("id desc")
    private List<NoticeBoardComment2> noticeBoardComment2List;
    
    public static NoticeBoard2 dtoToEntity(NoticeBoard2DTO noticeBoard2DTO, Member member) {
        NoticeBoard2 noticeBoard2 = new NoticeBoard2();
        noticeBoard2.setSubject(noticeBoard2DTO.getSubject());
        noticeBoard2.setContent(noticeBoard2DTO.getContent());
        noticeBoard2.setCnt(0);
        noticeBoard2.setMember(member);
        return noticeBoard2;
    }
}
