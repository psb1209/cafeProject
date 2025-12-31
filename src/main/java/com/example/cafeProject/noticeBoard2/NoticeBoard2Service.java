package com.example.cafeProject.noticeBoard2;


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
public class NoticeBoard2Service {

    private final NoticeBoard2Repository noticeBoard2Repository;
    private final MemberRepository memberRepository;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final MemberService memberService;

    public Page<NoticeBoard2> list(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) // 검색을 안 했을 경우
            return noticeBoard2Repository.findAll(pageable);

        return noticeBoard2Repository.searchBySubject(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public NoticeBoard2 getSelectOneById(NoticeBoard2DTO paramDTO) {
        return noticeBoard2Repository.findById(paramDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public Member getSelectOneByUsername(Authentication authentication) {
        return memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }

    @Transactional
    public void setInsert(NoticeBoard2DTO paramDTO) {
        NoticeBoard2 noticeBoard2 = new NoticeBoard2();
        noticeBoard2.setSubject(paramDTO.getSubject());
        noticeBoard2.setContent(paramDTO.getContent());
        noticeBoard2.setMember(memberService.view(paramDTO.getMemberId()));
        noticeBoard2.setCnt(0);

        noticeBoard2Repository.save(noticeBoard2);
    }

    @Transactional
    public NoticeBoard2 setUpdate(NoticeBoard2DTO paramDTO) {
        NoticeBoard2 noticeBoard2 = getSelectOneById(paramDTO);

        noticeBoard2.setSubject(paramDTO.getSubject());
        noticeBoard2.setContent(paramDTO.getContent());
        /*noticeBoard2.setCnt(paramDTO.getCnt());*/

        return noticeBoard2;
    }

    @Transactional
    public void setDelete(NoticeBoard2DTO paramDTO) {
        NoticeBoard2 noticeBoard2 = getSelectOneById(paramDTO);

        // 1. 메모 내용에서 이미지 src 추출
        List<String> imageUrls = extractImageUrls(noticeBoard2.getContent());

        // 2. 이미지 파일 삭제
        deleteImageFiles(imageUrls);

        noticeBoard2Repository.delete(noticeBoard2);
    }

    @Transactional
    public void cntPlus(NoticeBoard2 noticeBoard2) {
        noticeBoard2.setCnt(noticeBoard2.getCnt() + 1);

        noticeBoard2Repository.save(noticeBoard2);
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
    public boolean setInsert(NoticeBoard2DTO noticeBoard2DTO, Authentication authentication) {
        Member member = getSelectOneByUsername(authentication);
        Grade oldGrade = member.getGrade(); //예전 등급

        NoticeBoard2 noticeBoard2 = NoticeBoard2.dtoToEntity(noticeBoard2DTO, member);
        noticeBoard2Repository.save(noticeBoard2);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = noticeBoard2.getMember().getGrade(); //새로운 등급
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
