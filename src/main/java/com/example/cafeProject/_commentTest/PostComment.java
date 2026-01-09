package com.example.cafeProject._commentTest;

import com.example.cafeProject._postTest.Post;
import com.example.cafeProject.member.Member;
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
@Table(name = "postComment")
@Entity
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    private Timestamp createDate;

    @ManyToOne
    @JoinColumn(name = "memberId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "postId")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    // 대댓글(정렬)
    private int ref;
    private int step;
    private int level;

    /**
     * 소프트 삭제 플래그
     * - true면 "삭제 처리된 댓글"로 간주
     */
    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean deleted = false;

    /** 소프트 삭제 시각 */
    private Timestamp deletedAt;

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = new Timestamp(System.currentTimeMillis());
    }

    public static PostComment dtoToEntity(PostCommentDTO postCommentDTO, Member member, Post post) {
        PostComment postComment = new PostComment();
        postComment.setContent(postCommentDTO.getContent());
        postComment.setMember(member);
        postComment.setPost(post);
        postComment.setRef(postCommentDTO.getRef());
        postComment.setStep(postCommentDTO.getStep());
        postComment.setLevel(postCommentDTO.getLevel());
        postComment.setDeleted(false);
        postComment.setDeletedAt(null);
        return postComment;
    }
}
