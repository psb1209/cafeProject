package com.example.cafeProject._boardTest;

import com.example.base.BaseUtility;
import com.example.cafeProject._cafeTest.Cafe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultBoardProvisioner {

    private final BoardRepository boardRepository;// (예시) 보드 메타 저장소

    @Transactional
    public void ensureDefaults(Cafe cafe) {
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

            try {
                boardRepository.save(b);
                log.info("[ensureDefaults] {} 생성됨.", b.getName());
            } catch (DataIntegrityViolationException e) {
                log.info("[ensureDefaults] {} 이미 존재하거나 제약 위반으로 생성 스킵: {}",
                        b.getName(), e.getMostSpecificCause().getMessage());
            }
        }
    }
}
