package com.example.cafeProject._postTest;

import com.example.base.BaseImageService;
import com.example.base.BaseUtility;
import com.example.cafeProject._boardTest.Board;
import com.example.cafeProject._boardTest.BoardService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.member.RoleType;
import com.example.exception.EntityNotFoundException;
import com.example.exception.PermissionDeniedException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.Arrays;

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
    public Page<Post> listByBoardCode(String code, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) // 검색을 안 했을 경우
            return postRepository.findByBoard_Code(code, pageable);

        if (BaseUtility.isChosungQuery(keyword.trim())) // 초성 검색
            return postRepository.searchByChosungTitle(code, BaseUtility.jaeumBreaker(keyword), keyword.trim(), pageable);

        return postRepository.searchByTitle(code, keyword.trim(), pageable); // 일반 검색
    }
    public Page<PostDTO> listByBoardCodeDTO(String code, String keyword, Pageable pageable) {
        return listByBoardCode(code, keyword, pageable).map(this::toDTO);
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

    @Override
    protected void beforeInsert(PostDTO dto) {
        dto.setId(null);
        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setCnt(0);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RoleType[] roles = memberService.getEffectiveRoles(authentication);
        if (roles.length == 0) throw new PermissionDeniedException("Banned 사용자입니다.");
        if (dto.getBoardId() == null) throw new IllegalArgumentException("현재 게시판 정보가 존재하지 않습니다.");

        Board board = boardService.view(dto.getBoardId());

        // 권한 검사
        if (!Arrays.asList(roles).contains(board.getWriteRole())) throw new AccessDeniedException("쓰기 권한이 없습니다.");
        if (dto.isNotice() && !canSetNotice(board, authentication, roles)) throw new AccessDeniedException("공지 권한이 없습니다.");

        // 초성 검색을 위한 titleKey 세팅
        dto.setTitleKey(BaseUtility.toChosungKey(dto.getTitle()));

        dto.setMemberId(memberService.viewCurrentMember(authentication).getId());
    }

    @Override
    protected void beforeUpdate(PostDTO dto, Post entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RoleType[] roles = memberService.getEffectiveRoles(authentication);
        if (roles.length == 0) throw new PermissionDeniedException("Banned 사용자입니다.");

        // 권한 검사
        if (!canEdit(entity, authentication, roles)) throw new AccessDeniedException("수정 권한이 없습니다.");
        if (dto.isNotice() != entity.isNotice() && !canSetNotice(entity.getBoard(), authentication, roles)) throw new AccessDeniedException("공지 변경 권한이 없습니다.");

        // 초성 검색을 위한 titleKey 갱신
        dto.setTitleKey(BaseUtility.toChosungKey(dto.getTitle()));
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
}
