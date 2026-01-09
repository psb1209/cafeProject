package com.example.cafeProject._postTest;

import com.example.base.BaseImageService;
import com.example.base.BaseUtility;
import com.example.cafeProject._boardTest.Board;
import com.example.cafeProject._boardTest.BoardService;
import com.example.cafeProject.member.Grade;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.member.RoleType;
import com.example.exception.EntityNotFoundException;
import com.example.exception.PermissionDeniedException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Arrays;
import java.sql.Timestamp;
import java.util.List;

@Service
public class PostService extends BaseImageService<Post, PostDTO> {

    private final PostRepository postRepository;
    private final BoardService boardService;
    private final MemberService memberService;

    public PostService(PostRepository repository, ModelMapper modelMapper, BoardService boardService, MemberService memberService) {
        super(repository, modelMapper, Post.class, PostDTO.class);
        this.postRepository = repository;
        this.boardService = boardService;
        this.memberService = memberService;
    }

    @Override
    public Integer getIdFromDTO(PostDTO dto) {
        return dto.getId();
    }

    /**
     * 게시판 코드(code)에 속한 게시글 목록 조회 + 검색 지원
     * 1) keyword 없음/공백 -> 전체 목록(게시판 기준)
     * 2) 초성 검색(예: "ㅅㄱ") -> titleKey 기반 초성 검색
     * 3) 일반 검색 -> title like 검색
     */
    public Page<Post> listByBoardId(Integer boardId, String keyword, Pageable pageable) {
        if (boardId == null) throw new IllegalArgumentException("boardId is null");

        if (keyword == null || keyword.isBlank())
            return postRepository.findByBoard_Id(boardId, pageable);

        if (BaseUtility.isChosungQuery(keyword.trim()))
            return postRepository.searchByChosungTitle(
                    boardId,
                    BaseUtility.jaeumBreaker(keyword),
                    keyword.trim(),
                    pageable
            );

        return postRepository.searchByTitle(boardId, keyword.trim(), pageable);
    }

    public Page<PostDTO> listByBoardIdDTO(Integer boardId, String keyword, Pageable pageable) {
        return listByBoardId(boardId, keyword, pageable).map(this::toDTO);
    }

    /**
     * 게시글 상세 조회
     * - 없으면 EntityNotFoundException 발생
     */
    public Post viewDetail(int id) {
        return postRepository.findDetailById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 id=" + id));
    }
    public PostDTO viewDetailDTO(int id) {
        return toDTO(viewDetail(id));
    }

    /** 삭제된 게시글 조회 */
    public Page<Post> trashListByBoardId(Integer boardId, Pageable pageable) {
        return postRepository.findTrashByBoard_Id(boardId, pageable);
    }
    public Page<PostDTO> trashListByBoardIdDTO(Integer boardId, Pageable pageable) {
        return trashListByBoardId(boardId, pageable).map(this::toDTO);
    }

    /** 삭제된 게시글 상세 */
    public Post viewTrash(int id) {
        return postRepository.findTrashById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 id=" + id));
    }
    public PostDTO viewTrashDTO(int id) {
        return toDTO(viewTrash(id));
    }

    @Override
    protected Post toEntity(PostDTO dto) {
        Post post = super.toEntity(dto);
        post.setBoard(boardService.getReference(dto.getBoardId()));
        post.setMember(memberService.getReference(dto.getMemberId()));
        return post;
    }

    @Override
    protected PostDTO toDTO(Post post) {
        // 기본 필드 매핑(제목/내용/공지 등)은 super.toDTO(entity)가 담당
        PostDTO dto = super.toDTO(post);

        if (post.getBoard() != null) {
            dto.setBoardId(post.getBoard().getId());
            dto.setBoardCode(post.getBoard().getCode());
            dto.setBoardName(post.getBoard().getName());
        }
        if (post.getMember() != null) {
            dto.setMemberId(post.getMember().getId());
            dto.setUsername(post.getMember().getUsername());
            dto.setGrade(post.getMember().getGrade());
        }
        return dto;
    }

    @Override
    protected void updateEntity(Post post, PostDTO dto) {
        post.setTitle(dto.getTitle());
        post.setTitleKey(dto.getTitleKey());
        post.setContent(dto.getContent());
        post.setNotice(dto.isNotice());
    }

    @Transactional
    public void softDelete(PostDTO dto) {
        Post post = viewDetail(dto.getId());

        Authentication authentication = memberService.getCurrentMember();
        RoleType[] roles = memberService.getEffectiveRoles(authentication);
        if (roles.length == 0) throw new PermissionDeniedException("Banned 사용자입니다.");

        // 권한 검사
        if (!canEdit(post, authentication, roles)) throw new AccessDeniedException("삭제 권한이 없습니다.");

        // 삭제 체크
        if (post.isDeleted()) throw new PermissionDeniedException("삭제된 글은 삭제할 수 없습니다.");

        // 삭제 플래그를 true로
        post.setDeleted(true);
        post.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        postRepository.save(post);
    }

    @Override
    protected void beforeInsert(PostDTO dto) {
        dto.setId(null);
        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setCnt(0);
        dto.setDeleted(false);

        Authentication authentication = memberService.getCurrentMember();
        RoleType[] roles = memberService.getEffectiveRoles(authentication);
        if (roles.length == 0) throw new PermissionDeniedException("Banned 사용자입니다.");
        if (dto.getBoardId() == null) throw new IllegalArgumentException("현재 게시판 정보가 존재하지 않습니다.");

        Board board = boardService.view(dto.getBoardId());

        if (!board.isEnabled()) throw new PermissionDeniedException("비활성화된 게시판입니다.");

        // 권한 검사
        if (!Arrays.asList(roles).contains(board.getWriteRole())) throw new AccessDeniedException("쓰기 권한이 없습니다.");
        if (dto.isNotice() && !canSetNotice(board, authentication, roles)) throw new AccessDeniedException("공지 권한이 없습니다.");

        // 초성 검색을 위한 titleKey 세팅
        dto.setTitleKey(BaseUtility.toChosungKey(dto.getTitle()));

        dto.setMemberId(memberService.viewCurrentMember().getId());
    }

    @Override
    protected void beforeUpdate(PostDTO dto, Post entity) {
        Authentication authentication = memberService.getCurrentMember();
        RoleType[] roles = memberService.getEffectiveRoles(authentication);
        if (roles.length == 0) throw new PermissionDeniedException("Banned 사용자입니다.");

        // 권한 검사
        if (!canEdit(entity, authentication, roles)) throw new AccessDeniedException("수정 권한이 없습니다.");
        if (dto.isNotice() != entity.isNotice() && !canSetNotice(entity.getBoard(), authentication, roles)) throw new AccessDeniedException("공지 변경 권한이 없습니다.");

        // 삭제 체크
        if (entity.isDeleted()) throw new PermissionDeniedException("삭제된 글은 수정할 수 없습니다.");

        // 초성 검색을 위한 titleKey 갱신
        dto.setTitleKey(BaseUtility.toChosungKey(dto.getTitle()));
    }

    @Override
    protected void beforeDelete(Post entity) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "게시판 삭제는 허용되지 않습니다.");
    }

    public boolean isManagerOrAbove(RoleType[] effectiveRoles) {
        return Arrays.asList(effectiveRoles).contains(RoleType.MANAGER);
    }
    public boolean canSetNotice(Board board, Authentication authentication, RoleType[] roles) {
        if (isManagerOrAbove(roles)) return true;
        return board.getMember().getUsername().equals(authentication.getName());
    }
    public boolean canEdit(Post post, Authentication authentication, RoleType[] roles) {
        if (isManagerOrAbove(roles)) return true;
        return post.getMember().getUsername().equals(authentication.getName());
    }
    public boolean canEdit(int postId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;

        RoleType[] roles = memberService.getEffectiveRoles(authentication);
        if (roles == null || roles.length == 0) return false;

        Post post = viewDetail(postId);
        return canEdit(post, authentication, roles);
    }

    public List<PostDTO> noticeListByBoardIdDTO(Integer boardId) {
        return postRepository.findByNotice(boardId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void toggleNotice(int id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        post.setNotice(!post.isNotice());
    }

    public boolean gradeUpdateCheck(PostDTO dto) {
        Member member = memberService.viewCurrentMember();
        Grade oldGrade = member.getGrade();
        updateGrade(member);
        Grade newGrade = memberService.view(member.getId()).getGrade();
        return oldGrade != newGrade;
    }

    @Transactional
    public void updateGrade(Member member) {
        member.increasePostCount(); //게시글 작성 +1
        int posts = member.getPostCount();
        int replies = member.getReplyCount();

        if (posts >= 7 && replies >= 12) member.setGrade(Grade.SPECIAL);
        else if (posts >= 5 && replies >= 10) member.setGrade(Grade.BEST);
        else if (posts >= 3 && replies >= 5) member.setGrade(Grade.REGULAR);
        else member.setGrade(Grade.USER);
    }

}
