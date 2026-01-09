package com.example.cafeProject._commentTest;

import com.example.cafeProject._postTest.Post;
import com.example.cafeProject._postTest.PostRepository;
import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final MemberService memberService;

    /** 댓글 등록 (등급업 여부 반환) */
    @Transactional
    public boolean setInsert(PostCommentDTO postCommentDTO, User user) {
        Post post = getSelectOneById_post(postCommentDTO.getPostId());
        Member member = memberService.viewCurrentMember();

        Grade oldGrade = member.getGrade();

        postCommentDTO.setRef(postCommentRepository.getMaxRef() + 1);
        postCommentDTO.setStep(0);
        postCommentDTO.setLevel(0);

        PostComment postComment = PostComment.dtoToEntity(postCommentDTO, member, post);
        postCommentRepository.save(postComment);

        updateGrade(member);

        Grade newGrade = postComment.getMember().getGrade();
        return oldGrade != newGrade;
    }

    public Page<PostComment> getCommentListPage(int postId, Pageable pageable) {
        return postCommentRepository.findByPostIdOrderByRefDescLevelAsc(postId, pageable);
    }

    @Transactional
    public void setDelete(PostCommentDTO paramDTO) {
        PostComment postComment = postCommentRepository.findById(paramDTO.getPostCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
        postCommentRepository.delete(postComment);
    }

    @Transactional
    public void setDeleteAllByPostId(int postId) {
        List<PostComment> list = postCommentRepository.findByPostId(postId);
        postCommentRepository.deleteAll(list);
    }

    @Transactional
    public PostComment setUpdate(PostCommentDTO paramDTO) {
        PostComment postComment = postCommentRepository.findById(paramDTO.getPostCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
        postComment.setContent(paramDTO.getContent());
        return postComment;
    }

    public PostComment getPostCommentId(PostCommentDTO paramDTO) {
        return postCommentRepository.findById(paramDTO.getPostCommentId())
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));
    }

    // 대댓글 추가
    @Transactional
    public void replySetInsert(PostCommentDTO paramDTO, UserDetails userDetails) {
        // 부모 댓글
        PostComment parent = postCommentRepository.findById(paramDTO.getPostCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글 없음"));

        // 로그인 회원
        Member member = memberService.viewCurrentMember();

        // 정렬을 위한 level 밀기
        postCommentRepository.updateRelevel(parent.getRef(), parent.getLevel());

        int ref = parent.getRef();
        int step = parent.getStep() + 1;
        int level = parent.getLevel() + 1;

        Post post = null;
        Optional<Post> postOptional = postRepository.findById(paramDTO.getPostId());
        if (postOptional.isPresent()) {
            post = postOptional.get();
        }

        PostComment postComment = new PostComment();
        postComment.setContent(paramDTO.getContent());
        postComment.setPost(post);
        postComment.setMember(member);
        postComment.setRef(ref);
        postComment.setStep(step);
        postComment.setLevel(level);

        postCommentRepository.save(postComment);
    }

    // 회원 등업(팀원 댓글 서비스 로직 동일)
    public Member updateGrade(Member member) {
        member.increaseReplyCount(); // 댓글작성 +1
        int posts = member.getPostCount();
        int replies = member.getReplyCount();

        if (posts >= 7 && replies >= 12) member.setGrade(Grade.SPECIAL);
        else if (posts >= 5 && replies >= 10) member.setGrade(Grade.BEST);
        else if (posts >= 3 && replies >= 5) member.setGrade(Grade.REGULAR);
        else member.setGrade(Grade.USER);
        return member;
    }

    public Post getSelectOneById_post(int postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글 없음"));
    }
}
