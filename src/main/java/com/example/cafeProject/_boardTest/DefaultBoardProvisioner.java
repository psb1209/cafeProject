package com.example.cafeProject._boardTest;

import com.example.base.BaseUtility;
import com.example.cafeProject._cafeTest.Cafe;
import com.example.cafeProject.member.MemberService;
import com.example.exception.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultBoardProvisioner {

    private final BoardRepository boardRepository;// (예시) 보드 메타 저장소
    private final MemberService memberService;

    @Transactional
    public void ensureDefaults(Cafe cafe) {
        if (!memberService.isManagement(memberService.getCurrentMember()))
            throw new PermissionDeniedException("권한 없음");

        for (DefaultBoard t : DefaultBoard.values()) {

            // 이미 있으면 스킵(중복 생성 방지)
            if (boardRepository.existsByCafe_CodeAndCode(cafe.getCode(), t.getCode())) {
                continue;
            }

            Board b = Board.builder()
                    .cafe(cafe)
                    .code(t.getCode())
                    .name(t.getName())
                    .nameKey(BaseUtility.toChosungKey(t.getName()))
                    .description(t.getDescription())
                    .enabled(true)
                    .readRole(t.getReadRole())
                    .writeRole(t.getWriteRole())
                    .build();

            boardRepository.save(b);
        }
    }
}
