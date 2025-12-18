package com.example.cafeProject._boardTest;

import com.example.base.BaseImageService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.member.RoleType;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BoardService extends BaseImageService<Board, BoardDTO> {

    private final BoardRepository boardRepository;
    private final MemberService memberService;

    public BoardService(BoardRepository repository, ModelMapper modelMapper, MemberService memberService) {
        super(repository, modelMapper, Board.class, BoardDTO.class);
        this.boardRepository = repository;
        this.memberService = memberService;
    }

    public Page<Board> listVisible(Pageable pageable, RoleType[] roles) {
        if (roles == null || roles.length == 0) return Page.empty(pageable);
        List<RoleType> roleList = new ArrayList<>(roles.length);
        for (RoleType r : roles) {
            if (r == null) continue; // null 방어
            if (roleList.contains(r)) continue; // 중복값 방어
            roleList.add(r);
        }
        if (roleList.isEmpty()) return Page.empty(pageable);
        return boardRepository.findVisible(roleList, pageable);
    }
    public Page<BoardDTO> listVisibleDTO(Pageable pageable, RoleType[] roles) {
        return listVisible(pageable, roles).map(this::toDTO);
    }


    @Override
    public Integer getIdFromDTO(BoardDTO dto) {
        return dto.getId(); // BaseDTO의 id 사용
    }

    @Override
    protected Board toEntity(BoardDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (memberService.isNotLogin(authentication)) throw new AccessDeniedException("현재 로그인 정보를 확인할 수 없습니다.");
        Board board = super.toEntity(dto);
        board.setMember(memberService.viewCurrentMember(authentication));
        board.setEnabled(true);
        return board;
    }

    @Override
    protected BoardDTO toDTO(Board board) {
        BoardDTO dto = super.toDTO(board);
        if (board.getMember() != null) {
            dto.setMemberId(board.getMember().getId());
            dto.setUsername(board.getMember().getUsername());
        }
        return dto;
    }

    @Override
    protected void updateEntity(Board board, BoardDTO dto) {
        board.setName(dto.getName());
        board.setDescription(dto.getDescription());
        board.setWriteRole(dto.getWriteRole());
    }

    @Override
    protected void beforeInsert(BoardDTO dto) {
        dto.setId(null);
        dto.setCreateDate(null);
        dto.setMemberId(null);
        dto.setUsername(null);
        dto.setEnabled(true);

        // 기본값
        if (dto.getReadRole() == null)  dto.setReadRole(RoleType.GUEST);
        if (dto.getWriteRole() == null) dto.setWriteRole(RoleType.USER);

        if (dto.getReadRole() == RoleType.BANNED || dto.getWriteRole() == RoleType.BANNED) {
            throw new IllegalArgumentException("읽기/쓰기 권한에 BANNED는 사용할 수 없습니다.");
        }

        // 읽기 권한은 쓰기 권한보다 높을 수 없음
        if (roleRank(dto.getReadRole()) > roleRank(dto.getWriteRole())) {
            throw new IllegalArgumentException("보기 권한은 쓰기 권한보다 높을 수 없습니다.");
        }
    }

    private int roleRank(RoleType role) {
        if (role == null) return Integer.MIN_VALUE;

        switch (role) {
            case GUEST:
                return 0;
            case USER:
                return 10;
            case MANAGER:
                return 20;
            case ADMIN:
                return 30;
            default:
                return Integer.MIN_VALUE;
        }
    }
}
