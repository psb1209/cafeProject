package com.example.cafeProject.boardTest;

import com.example.base.BaseImageService;
import com.example.cafeProject.member.MemberService;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class BoardService extends BaseImageService<Board, BoardDTO> {

    private final MemberService memberService;

    public BoardService(BoardRepository repository, ModelMapper modelMapper, MemberService memberService) {
        super(repository, modelMapper, Board.class, BoardDTO.class);
        this.memberService = memberService;
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
    }
}
