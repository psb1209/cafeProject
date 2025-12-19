package com.example.cafeProject._postTest;

import com.example.base.BaseImageService;
import com.example.base.BaseUtility;
import com.example.cafeProject._boardTest.BoardService;
import com.example.cafeProject.member.MemberService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public Page<Post> listByBoardCode(String code, String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank())
            return postRepository.findByBoard_Code(code, pageable);

        if (BaseUtility.isChosungQuery(keyword.trim()))
            return postRepository.searchByChosungTitle(code, BaseUtility.jaeumBreaker(keyword), pageable);

        return postRepository.searchByTitle(code, keyword.trim(), pageable);
    }
    public Page<PostDTO> listByBoardCodeDTO(String code, String keyword, Pageable pageable) {
        return listByBoardCode(code, keyword, pageable).map(this::toDTO);
    }

    @Override
    protected Post toEntity(PostDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (memberService.isNotLogin(authentication)) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        if (dto.getBoardId() == null) throw new IllegalArgumentException("현재 게시판 정보가 존재하지 않습니다.");

        Post post = super.toEntity(dto);
        post.setMember(memberService.viewCurrentMember(authentication));
        post.setBoard(boardService.view(dto.getBoardId()));
        return post;
    }

    @Override
    protected PostDTO toDTO(Post post) {
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
    }

    @Override
    protected void beforeInsert(PostDTO dto) {
        dto.setId(null);
        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setCnt(0);

        // 초성 검색을 위한 titleKey 세팅
        dto.setTitleKey(BaseUtility.toChosungKey(dto.getTitle()));
    }

    @Override
    protected void beforeUpdate(PostDTO dto, Post entity) {
        // 초성 검색을 위한 titleKey 갱신
        dto.setTitleKey(BaseUtility.toChosungKey(dto.getTitle()));
    }
}
