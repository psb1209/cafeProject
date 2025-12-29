package com.example.cafeProject.operationBoard;

import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberRepository;
import com.example.cafeProject.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class OperationBoardService {

    private final OperationBoardRepository operationBoardRepository;
    private final MemberRepository memberRepository;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final MemberService memberService;

    public Page<OperationBoard> list(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) // 검색을 안 했을 경우
            return operationBoardRepository.findAll(pageable);

        return operationBoardRepository.searchBySubject(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public OperationBoard getSelectOneById(OperationBoardDTO paramDTO) {
        return operationBoardRepository.findById(paramDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public Member getSelectOneByUsername(Authentication authentication) {
        return memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional
    public void setInsert(OperationBoardDTO paramDTO) {
        OperationBoard operationBoard = new OperationBoard();
        operationBoard.setSubject(paramDTO.getSubject());
        operationBoard.setContent(paramDTO.getContent());
        operationBoard.setMember(memberService.view(paramDTO.getMemberId()));
        operationBoard.setCnt(0);

        operationBoardRepository.save(operationBoard);
    }

    @Transactional
    public OperationBoard setUpdate(OperationBoardDTO paramDTO) {
        OperationBoard operationBoard = getSelectOneById(paramDTO);

        operationBoard.setSubject(paramDTO.getSubject());
        operationBoard.setContent(paramDTO.getContent());
        /*operationBoard.setCnt(paramDTO.getCnt());*/

        return operationBoard;
    }

    @Transactional
    public void setDelete(OperationBoardDTO paramDTO) {
        OperationBoard operationBoard = getSelectOneById(paramDTO);

        // 1. 메모 내용에서 이미지 src 추출
        List<String> imageUrls = extractImageUrls(operationBoard.getContent());

        // 2. 이미지 파일 삭제
        deleteImageFiles(imageUrls);

        operationBoardRepository.delete(operationBoard);
    }

    @Transactional
    public void cntPlus(OperationBoard operationBoard) {
        operationBoard.setCnt(operationBoard.getCnt() + 1);

        operationBoardRepository.save(operationBoard);
    }

    //************************************************************************************************************************

    @Value("${app.image.upload-dir}")
    protected String imgPath;

    //서머노트관련
    protected List<String> extractImageUrls(String html) {
        List<String> urls = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements imgs = doc.select("img");

        for (Element img : imgs) {
            String src = img.attr("src");
            urls.add(src);
        }

        return urls;
    }

    protected void deleteImageFiles(List<String> imageUrls) {

        for (String url : imageUrls) {
            try {
                // URL에서 파일명만 가져오기
                String fileName = Paths.get(url).getFileName().toString();

                File file = Paths.get(imgPath, fileName).toFile();
                if (file.exists()) {
                    if (file.delete()) {
                        log.info("[deleteImageFiles] 이미지 파일 삭제 성공 - fileName={}, path={}",
                                fileName, file.getAbsolutePath());
                    } else {
                        log.warn("[deleteImageFiles] 이미지 파일 삭제 실패 - fileName={}, path={}",
                                fileName, file.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                log.error("[deleteImageFiles] 이미지 삭제중 예상 못한 오류 : {}", e.getMessage());
            }
        }
    }
    //************************************************************************************************************************


}
