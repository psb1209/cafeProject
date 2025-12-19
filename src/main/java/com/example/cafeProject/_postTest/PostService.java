package com.example.cafeProject._postTest;

import com.example.base.BaseImageService;
import com.example.cafeProject._boardTest.Board;
import com.example.cafeProject._boardTest.BoardRepository;
import com.example.cafeProject._boardTest.BoardService;
import com.example.cafeProject.member.MemberService;
import com.example.exception.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PostService extends BaseImageService<Post, PostDTO> {

    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final MemberService memberService;

    public PostService(PostRepository repository, ModelMapper modelMapper, BoardRepository boardRepository, MemberService memberService) {
        super(repository, modelMapper, Post.class, PostDTO.class);
        this.postRepository = repository;
        this.boardRepository = boardRepository;
        this.memberService = memberService;
    }

    @Override
    public Integer getIdFromDTO(PostDTO dto) {
        return dto.getId();
    }

    public Page<PostDTO> listByBoardCode(String code, String keyword, Pageable pageable) {
        Page<Post> page = StringUtils.hasText(keyword)
                ? postRepository.searchByTitle(code, keyword, pageable)
                : postRepository.findByBoard_Code(code, pageable);

        return page.map(this::toDTO);
    }

    @Override
    protected Post toEntity(PostDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (memberService.isNotLogin(authentication)) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        Board board = boardRepository.findByCode(dto.getBoardCode())
                .orElseThrow(() -> new EntityNotFoundException("현재 게시판 정보를 확인할 수 없습니다."));

        Post post = super.toEntity(dto);
        post.setMember(memberService.viewCurrentMember(authentication));
        post.setBoard(board);
        post.setCnt(0);
        return post;
    }

    @Override
    protected PostDTO toDTO(Post post) {
        PostDTO dto = super.toDTO(post);
        if (post.getMember() != null) {
            dto.setMemberId(post.getMember().getId());
            dto.setUsername(post.getMember().getUsername());
        }
        return dto;
    }

    @Override
    protected void updateEntity(Post post, PostDTO dto) {
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
    }
}
