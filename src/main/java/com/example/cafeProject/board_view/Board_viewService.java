package com.example.cafeProject.board_view;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class Board_viewService {

    private final Board_viewRepository board_viewRepository;
    private final MemberRepository memberRepository;

    // ======================
    // 조회 기록 저장 (1계정 1조회)
    // ======================
    @Transactional
    public void createProc(Board_viewDTO dto) {

        Optional<Board_view> optional = Optional.empty();

        if (dto.getOperationBoardNumber() != null) {
            optional = board_viewRepository
                    .findByOperationBoardNumberAndUserId(
                            dto.getOperationBoardNumber(),
                            dto.getUserId()
                    );
        } else if (dto.getCommunityBoardNumber() != null) {
            optional = board_viewRepository
                    .findByCommunityBoardNumberAndUserId(
                            dto.getCommunityBoardNumber(),
                            dto.getUserId()
                    );
        } else if (dto.getNoticeBoardNumber() != null) {
            optional = board_viewRepository
                    .findByNoticeBoardNumberAndUserId(
                            dto.getNoticeBoardNumber(),
                            dto.getUserId()
                    );
        } else if (dto.getInformationBoardNumber() != null) {
            optional = board_viewRepository
                    .findByInformationBoardNumberAndUserId(
                            dto.getInformationBoardNumber(),
                            dto.getUserId()
                    );
        }

        // ✅ 이미 조회한 글이면 아무 것도 안 함
        if (optional.isPresent()) return;

        // ✅ 최초 조회만 insert
        Board_view view = Board_view.builder()
                .userId(dto.getUserId())
                .operationBoardNumber(dto.getOperationBoardNumber())
                .communityBoardNumber(dto.getCommunityBoardNumber())
                .noticeBoardNumber(dto.getNoticeBoardNumber())
                .informationBoardNumber(dto.getInformationBoardNumber())
                .build();

        board_viewRepository.save(view);
    }

    // ======================
    // 조회수 반환
    // ======================
    public int board_viewCnt(String boardCode, int postId) {

        if (boardCode == null || boardCode.isBlank()) return 0;

        boardCode = boardCode.toLowerCase();

        if (boardCode.contains("operation")) {
            return board_viewRepository
                    .countBoard_viewWithOperationBoardNumber(postId);
        }

        if (boardCode.contains("community")) {
            return board_viewRepository
                    .countBoard_viewWithCommunityBoardNumber(postId);
        }

        if (boardCode.contains("notice")) {
            return board_viewRepository
                    .countBoard_viewWithNoticeBoardNumber(postId);
        }

        if (boardCode.contains("information")) {
            return board_viewRepository
                    .countBoard_viewWithInformationBoardNumber(postId);
        }

        return 0;
    }

    // ======================
    // 유저 조회
    // ======================
    public Member selectByUsername(String username) {
        return memberRepository.findByUsername(username).orElseThrow();
    }
}
