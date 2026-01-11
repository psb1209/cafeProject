package com.example.cafeProject.communityBoard;


import com.example.cafeProject.member.*;

import com.example.cafeProject.operationBoard.OperationBoard;
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

@RequiredArgsConstructor
@Service
public class CommunityBoardService {

    private final CommunityBoardRepository communityBoardRepository;
    private final MemberRepository memberRepository;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final MemberService memberService;

    /*=============================== 각 게시판 공지글 ===================================*/
    @Transactional
    public void toggleNotice(int id) {
        CommunityBoard communityBoard = communityBoardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        communityBoard.setSubNotice(!communityBoard.isSubNotice());
    }

    public List<CommunityBoard> getSubNoticeList() {
        return communityBoardRepository.findBySubNoticeTrueOrderByCreateDateDesc();
    }

    public Page<CommunityBoard> list(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) // 검색을 안 했을 경우
            return communityBoardRepository.findBySubNoticeFalse(pageable);
        return communityBoardRepository.searchBySubject(keyword.trim(), pageable);
    }
    /*====================================================================================*/


    @Transactional(readOnly = true)
    public CommunityBoard getSelectOneById(CommunityBoardDTO paramDTO) {
        return communityBoardRepository.findById(paramDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional
    public void setInsert(CommunityBoardDTO paramDTO) {
        CommunityBoard communityBoard = new CommunityBoard();
        communityBoard.setSubject(paramDTO.getSubject());
        communityBoard.setContent(paramDTO.getContent());
        communityBoard.setMember(memberService.viewCurrentMember());
        communityBoard.setCnt(0);

        communityBoardRepository.save(communityBoard);
    }

    @Transactional
    public CommunityBoard setUpdate(CommunityBoardDTO paramDTO) {
        CommunityBoard communityBoard = getSelectOneById(paramDTO);

        if (
                paramDTO.getSubject() != null && paramDTO.getSubject().isBlank()
             && paramDTO.getContent() != null && paramDTO.getContent().isBlank()
        ) {
            communityBoard.setSubject(paramDTO.getSubject());
            communityBoard.setContent(paramDTO.getContent());
        } else {
            communityBoard.setCnt(paramDTO.getCnt());
        }

        return communityBoard;
    }

    @Transactional
    public void setDelete(CommunityBoardDTO paramDTO) {
        CommunityBoard communityBoard = getSelectOneById(paramDTO);

        // 1. 메모 내용에서 이미지 src 추출
        List<String> imageUrls = extractImageUrls(communityBoard.getContent());

        // 2. 이미지 파일 삭제
        deleteImageFiles(imageUrls);

        communityBoardRepository.delete(communityBoard);
    }

    @Transactional
    public void cntPlus(CommunityBoard communityBoard) {
        communityBoard.setCnt(communityBoard.getCnt() + 1);

        communityBoardRepository.save(communityBoard);
    }

    //회원등업
    @Transactional
    public void updateGrade(Member member) {

        //관리자는 회원등급에 영향을 받지 않도록.
        if (member.getRole() == RoleType.ADMIN || member.getRole() == RoleType.MANAGER) {
            return;
        }

        member.increasePostCount(); //게시글 작성 +1
        int posts = member.getPostCount();
        int replies = member.getReplyCount();

        if (posts >= 7 && replies >= 12) member.setGrade(Grade.SPECIAL);
        else if (posts >= 5 && replies >= 10) member.setGrade(Grade.BEST);
        else if (posts >= 3 && replies >= 5) member.setGrade(Grade.REGULAR);
        else member.setGrade(Grade.USER);
    }

    //카페 회원만 게시글 작성
    @Transactional
    public boolean setInsert(CommunityBoardDTO communityBoardDTO, Authentication authentication) {
        Member member = memberService.viewCurrentMember();
        Grade oldGrade = member.getGrade(); //예전 등급

        CommunityBoard communityBoard = CommunityBoard.dtoToEntity(communityBoardDTO, member);
        communityBoardRepository.save(communityBoard);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = communityBoard.getMember().getGrade(); //새로운 등급
        return oldGrade != newGrade; //비교해서 등급이 바뀌었으면 true 반환
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
