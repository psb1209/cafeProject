package com.example.cafeProject.informationBoard;

import com.example.cafeProject.member.*;
import com.example.cafeProject.noticeBoard.NoticeBoard;
import com.example.cafeProject.noticeBoard.NoticeBoardDTO;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class InformationBoardService {

    private final InformationBoardRepository informationBoardRepository;
    private final MemberRepository memberRepository;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final MemberService memberService;

    /*=============================== 각 게시판 공지글 ===================================*/
    @Transactional
    public void toggleNotice(int id) {
        InformationBoard informationBoard = informationBoardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        informationBoard.setSubNotice(!informationBoard.isSubNotice());
    }

    public List<InformationBoard> getSubNoticeList() {
        return informationBoardRepository.findBySubNoticeTrueOrderByCreateDateDesc();
    }

    public Page<InformationBoard> list(Pageable pageable, String keyword) {
        if (keyword == null || keyword.isBlank()) // 검색을 안 했을 경우
            return informationBoardRepository.findBySubNoticeFalse(pageable);
        return informationBoardRepository.searchBySubject(keyword.trim(), pageable);
    }
    /*====================================================================================*/


    //기본키로 정보게시글 레코드 한줄 찾기
    @Transactional(readOnly = true)
    public InformationBoard getSelectOneById(int id) {
        return informationBoardRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("해당 게시글 없음"));
    }

    @Transactional(readOnly = true)
    public Member getSelectOneByUsername(Authentication authentication) {
        return memberRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }


    //카페 회원만 게시글 작성
    @Transactional
    public boolean setInsert(InformationBoardDTO informationBoardDTO, Authentication authentication) {
       Member member = memberService.viewCurrentMember(authentication);
       Grade oldGrade = member.getGrade(); //예전 등급

       InformationBoard informationBoard = InformationBoard.dtoToEntity(informationBoardDTO, member);
       informationBoardRepository.save(informationBoard);

        updateGrade(member); //회원등업 메서드 호출

        Grade newGrade = informationBoard.getMember().getGrade(); //새로운 등급
        return oldGrade != newGrade; //비교해서 등급이 바뀌었으면 true 반환
    }

//    @Transactional
//    public void setInsert(InformationBoardDTO informationBoardDTO) {
//        InformationBoard informationBoard = new InformationBoard();
//        informationBoard.setSubject(informationBoardDTO.getSubject());
//        informationBoard.setContent(informationBoardDTO.getContent());
//        informationBoard.setMember(memberService.view(informationBoardDTO.getMemberId()));
//        informationBoard.setCnt(0);
//
//        informationBoardRepository.save(informationBoard);
//    }

    //작성자만 게시글 수정
    @Transactional
    public void setUpdate(InformationBoardDTO informationBoardDTO, User user) {

        InformationBoard informationBoard = getSelectOneById(informationBoardDTO.getId());
        if(!user.getUsername().equals(informationBoard.getMember().getUsername())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        informationBoard.setSubject(informationBoardDTO.getSubject());
        informationBoard.setContent(informationBoardDTO.getContent());

    }


    //작성자 & 관리자 & 매니저만 게시글 삭제
    @Transactional
    public void setDelete(InformationBoardDTO informationBoardDTO, User user) {

        InformationBoard informationBoard = getSelectOneById(informationBoardDTO.getId());
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        boolean isAdminOrManager = false;
        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_MANAGER")) {
                isAdminOrManager = true;
                break;
            }
        }
        boolean isAuthor = user.getUsername().equals(informationBoard.getMember().getUsername());

        if (isAuthor || isAdminOrManager) {

            // 1. 메모 내용에서 이미지 src 추출
            List<String> imageUrls = extractImageUrls(informationBoard.getContent());
            // 2. 이미지 파일 삭제
            deleteImageFiles(imageUrls);

            informationBoardRepository.delete(informationBoard);
        } else {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
    }

    //조회수 증가
    @Transactional
    public void cntPlus(InformationBoard informationBoard) {
        informationBoard.setCnt(informationBoard.getCnt() + 1);

        informationBoardRepository.save(informationBoard);
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
