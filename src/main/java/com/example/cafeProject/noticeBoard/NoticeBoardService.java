package com.example.cafeProject.noticeBoard;

import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoardComment.NoticeBoardCommentRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Page<NoticeBoard> list(Pageable pageable) {

        return noticeBoardRepository.findAll(pageable);
    }


    public NoticeBoard view(NoticeBoardDTO noticeBoardDTO) {
        Optional<NoticeBoard> optionalNoticeBoard = noticeBoardRepository.findById(noticeBoardDTO.getId());
        NoticeBoard noticeBoard = null;
        if (optionalNoticeBoard.isPresent()) {
            noticeBoard = optionalNoticeBoard.get();
        }
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

    public int deleteProc(NoticeBoardDTO noticeBoardDTO) {
        NoticeBoard noticeBoard = new NoticeBoard();
        noticeBoard.setId(noticeBoardDTO.getId());
        int result = 0;
        try {
            //noticeBoardCommentRepository.deleteByNoticeBoardId(noticeBoardDTO.getId());
            noticeBoardRepository.delete(noticeBoard);

        } catch (Exception e) {
            //e.printStackTrace();
            result++;
        }
        return result;
    }

    void cntUpdateProc(NoticeBoard noticeBoard) {
        noticeBoard.setCnt(noticeBoard.getCnt() + 1);
        noticeBoardRepository.save(noticeBoard);
    }


    private List<String> extractImageUrls(String html) {
        List<String> urls = new ArrayList<>();

        Document doc = Jsoup.parse(html);
        Elements imgs = doc.select("img");

        for (Element img : imgs) {
            String src = img.attr("src");
            urls.add(src);
        }

        return urls;
    }

    private void deleteImageFiles(List<String> imageUrls) {
        String basePath = "C:/dw202/attach/summernote/";

        for (String url : imageUrls) {
            try {
                // URL에서 파일명만 가져오기
                String fileName = Paths.get(url).getFileName().toString();

                File file = new File(basePath + fileName);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
