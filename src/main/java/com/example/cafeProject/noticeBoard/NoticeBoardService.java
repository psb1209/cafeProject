package com.example.cafeProject.noticeBoard;

import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoardComment.NoticeBoardCommentRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class NoticeBoardService {

    private final NoticeBoardRepository noticeBoardRepository;
    private final NoticeBoardCommentRepository noticeBoardCommentRepository;
    private final MemberService memberService;
    protected final Logger log = LoggerFactory.getLogger(getClass());


    public Page<NoticeBoard> list(Pageable pageable) {
        return noticeBoardRepository.findAll(pageable);
    }

    public NoticeBoard view(NoticeBoardDTO noticeBoardDTO) {
        Optional<NoticeBoard> optionalNoticeBoard = noticeBoardRepository.findById(noticeBoardDTO.getId());
        NoticeBoard noticeBoard = null;
        if (optionalNoticeBoard.isPresent()) {
            noticeBoard = optionalNoticeBoard.get();
        }
        //댓글 수 표시
        int commentCnt = noticeBoardCommentRepository.countCommentsByNoticeBoardId(noticeBoard.getId());
        noticeBoard.setCommentCnt(commentCnt);

        return noticeBoard;
    }

    public int createProc(NoticeBoardDTO noticeBoardDTO) {
        NoticeBoard noticeBoard = new NoticeBoard();
        noticeBoard.setSubject(noticeBoardDTO.getSubject());
        noticeBoard.setContent(noticeBoardDTO.getContent());
        noticeBoard.setCnt(0);
        noticeBoard.setMember(memberService.view(noticeBoardDTO.getMemberId()));

        int result = 0;
        try {
            noticeBoardRepository.save(noticeBoard);
        } catch (Exception e) {
            //e.printStackTrace();
            result++;
        }
        return result;
    }

    public int updateProc(NoticeBoardDTO noticeBoardDTO) {
        NoticeBoard noticeBoard = view(noticeBoardDTO);
        noticeBoard.setSubject(noticeBoardDTO.getSubject());
        noticeBoard.setContent(noticeBoardDTO.getContent());
        int result = 0;
        try {
            noticeBoardRepository.save(noticeBoard);
        } catch (Exception e) {
            //e.printStackTrace();
            result++;
        }
        return result;
    }

//    public int deleteProc(NoticeBoardDTO noticeBoardDTO) {
//        NoticeBoard noticeBoard = new NoticeBoard();
//        noticeBoard.setId(noticeBoardDTO.getId());
//        int result = 0;
//        try {
//            //noticeBoardCommentRepository.deleteByNoticeBoardId(noticeBoardDTO.getId());
//            noticeBoardRepository.delete(noticeBoard);
//
//        } catch (Exception e) {
//            //e.printStackTrace();
//            result++;
//        }
//        return result;
//    }

    //게시글 삭제 전용 메소드
    @Transactional
    public void deleteNoticeBoard(int noticeBoardId) {

        // 1. 댓글 삭제
        noticeBoardCommentRepository.deleteByNoticeBoardId(noticeBoardId);


        // 3. 게시글 삭제
        noticeBoardRepository.deleteById(noticeBoardId);
    }


    public void setDelete(NoticeBoardDTO paramDTO) {
        NoticeBoard noticeBoard = getSelectOneById(paramDTO);

        // 1. 메모 내용에서 이미지 src 추출
        List<String> imageUrls = extractImageUrls(noticeBoard.getContent());

        // 2. 이미지 파일 삭제
        deleteImageFiles(imageUrls);

        noticeBoardRepository.delete(noticeBoard);
    }

    public NoticeBoard getSelectOneById(NoticeBoardDTO paramDTO) {
        return noticeBoardRepository.findById(paramDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));
    }


    void cntUpdateProc(NoticeBoard noticeBoard) {
        noticeBoard.setCnt(noticeBoard.getCnt() + 1);
        noticeBoardRepository.save(noticeBoard);
    }

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
}
