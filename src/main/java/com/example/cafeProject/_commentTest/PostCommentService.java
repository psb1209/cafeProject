package com.example.cafeProject._commentTest;

import com.example.cafeProject._postTest.Post;
import com.example.cafeProject._postTest.PostRepository;
import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.member.RoleType;
import com.example.exception.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    /**
     * 댓글/대댓글 목록 조회
     * - 삭제 댓글도 목록에는 포함(대댓글 구조 유지 목적)
     * - 실제 표시/버튼 노출은 view에서 deleted 플래그로 제어
     */
    public Page<PostComment> getCommentListPage(int postId, Pageable pageable) {
        return postCommentRepository.findByPostIdOrderByRefDescLevelAsc(postId, pageable);
    }

    /** 화면 상단 "댓글 n" 표기용(삭제되지 않은 댓글만 카운트) */
    public long countActiveComments(int postId) {
        return postCommentRepository.countByPostIdAndDeletedFalse(postId);
    }

    /**
     * 댓글 소프트 삭제
     * - row 삭제 X
     * - deleted=true, deletedAt=now
     */
    @Transactional
    public void setDelete(PostCommentDTO paramDTO) {
        PostComment postComment = getPostCommentId(paramDTO);

        if (postComment.isDeleted()) {
            throw new IllegalArgumentException("이미 삭제된 댓글입니다.");
        }

        ensureCanModify(postComment);

        postComment.softDelete();
        // dirty-checking으로 update
    }

    /**
     * 게시글 단위 일괄 소프트 삭제(사용처가 생기면 활용)
     */
    @Transactional
    public void setDeleteAllByPostId(int postId) {
        List<PostComment> list = postCommentRepository.findByPostId(postId);
        for (PostComment c : list) {
            if (!c.isDeleted()) {
                c.softDelete();
            }
        }
    }

    /** 댓글 수정 */
    @Transactional
    public PostComment setUpdate(PostCommentDTO paramDTO) {
        PostComment postComment = getPostCommentId(paramDTO);

        if (postComment.isDeleted()) {
            throw new IllegalArgumentException("삭제된 댓글은 수정할 수 없습니다.");
        }

        ensureCanModify(postComment);

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

        int ref = parent.getRef();
        int step = parent.getStep() + 1;
        int level = postCommentRepository.getMaxLevelInRef(ref) + 1;

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
        postComment.setDeleted(false);
        postComment.setDeletedAt(null);

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

    /** 댓글 수정/삭제 권한 체크(서버 측 방어) */
    private void ensureCanModify(PostComment comment) {
        Authentication authentication = memberService.getCurrentMember();
        RoleType[] roles = memberService.getEffectiveRoles(authentication);
        if (roles == null || roles.length == 0) {
            throw new PermissionDeniedException("Banned 사용자입니다.");
        }

        boolean managerOrAbove = Arrays.asList(roles).contains(RoleType.MANAGER)
                || Arrays.asList(roles).contains(RoleType.ADMIN);

        if (managerOrAbove) return;

        if (memberService.isNotLogin(authentication)) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        String writer = (comment.getMember() == null) ? null : comment.getMember().getUsername();
        if (writer == null || writer.isBlank()) {
            throw new AccessDeniedException("작성자 정보가 없습니다.");
        }

        if (!writer.equals(authentication.getName())) {
            throw new AccessDeniedException("댓글 수정/삭제 권한이 없습니다.");
        }
    }
}
