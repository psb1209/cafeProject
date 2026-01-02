package com.example.cafeProject.noticeBoard;


import com.example.cafeProject.member.Grade;
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

@RequiredArgsConstructor
@Service
public class NoticeBoardService {

    private final NoticeBoardRepository noticeBoardRepository;
    private final MemberRepository memberRepository;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final MemberService memberService;

    public Page<NoticeBoard> list(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) // 검색을 안 했을 경우
            return noticeBoardRepository.findAll(pageable);

        return noticeBoardRepository.searchBySubject(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public NoticeBoard getSelectOneById(NoticeBoardDTO paramDTO) {
        return noticeBoardRepository.findById(paramDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public Member getSelectOneByUsername(Authentication authentication) {
        return memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional
    public void setInsert(NoticeBoardDTO paramDTO) {
        NoticeBoard noticeBoard = new NoticeBoard();
        noticeBoard.setSubject(paramDTO.getSubject());
        noticeBoard.setContent(paramDTO.getContent());
        noticeBoard.setMember(memberService.view(paramDTO.getMemberId()));
        noticeBoard.setCnt(0);

        noticeBoardRepository.save(noticeBoard);
    }

    @Transactional
    public NoticeBoard setUpdate(NoticeBoardDTO paramDTO) {
        NoticeBoard noticeBoard = getSelectOneById(paramDTO);

        noticeBoard.setSubject(paramDTO.getSubject());
        noticeBoard.setContent(paramDTO.getContent());
        /*noticeBoard.setCnt(paramDTO.getCnt());*/

        return noticeBoard;
    }

    @Transactional
    public void setDelete(NoticeBoardDTO paramDTO) {
        NoticeBoard noticeBoard = getSelectOneById(paramDTO);

        // 1. 메모 내용에서 이미지 src 추출
        List<String> imageUrls = extractImageUrls(noticeBoard.getContent());

        // 2. 이미지 파일 삭제
        deleteImageFiles(imageUrls);

        noticeBoardRepository.delete(noticeBoard);
    }

    @Transactional
    public void cntPlus(NoticeBoard noticeBoard) {
        noticeBoard.setCnt(noticeBoard.getCnt() + 1);

        noticeBoardRepository.save(noticeBoard);
    }

    //회원등업
    @Transactional
    public void updateGrade(Member member) {

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
    public boolean setInsert(NoticeBoardDTO noticeBoardDTO, Authentication authentication) {
        Member member = getSelectOneByUsername(authentication);
        Grade oldGrade = member.getGrade(); //예전 등급

        NoticeBoard noticeBoard = NoticeBoard.dtoToEntity(noticeBoardDTO, member);
        noticeBoardRepository.save(noticeBoard);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = noticeBoard.getMember().getGrade(); //새로운 등급
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
