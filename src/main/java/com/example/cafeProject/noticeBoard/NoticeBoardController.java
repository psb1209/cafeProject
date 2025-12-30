package com.example.cafeProject.noticeBoard;

import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoardComment.NoticeBoardComment;
import com.example.cafeProject.noticeBoardComment.NoticeBoardCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@RequestMapping("/noticeBoard")
@RequiredArgsConstructor
@Controller
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final NoticeBoardCommentService noticeBoardCommentService;
    private final MemberService memberService;

    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size=10, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NoticeBoard> noticeBoardList = noticeBoardService.list(pageable);
        model.addAttribute("noticeBoardList", noticeBoardList);
        return "noticeBoard/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") int id,
            @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        NoticeBoardDTO noticeBoardDTO = new NoticeBoardDTO();
            noticeBoardDTO.setId(id);
        NoticeBoard noticeBoard = noticeBoardService.view(noticeBoardDTO);
        if (noticeBoard == null) {
            return "redirect:/";
        }
        model.addAttribute("noticeBoard", noticeBoard);
        Page<NoticeBoardComment> noticeBoardCommentList = noticeBoardCommentService.listByNoticeBoard(id, pageable);
        model.addAttribute("noticeBoardCommentList", noticeBoardCommentList);


        noticeBoardService.cntUpdateProc(noticeBoard);

        model.addAttribute("noticeBoard", noticeBoard);
        return "noticeBoard/view";
    }

    @GetMapping("/create")
    public String create() {

        return "noticeBoard/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            Model model,
            @PathVariable("id") int id
    ) {
        NoticeBoardDTO noticeBoardDTO = new NoticeBoardDTO();
        noticeBoardDTO.setId(id);
        NoticeBoard noticeBoard = noticeBoardService.view(noticeBoardDTO);
        if (noticeBoard == null) {
            model.addAttribute("errCode", "err0001");
            model.addAttribute("errMsg", "존재하지 않는 게시글입니다.");
            return "error/error";
        }
        model.addAttribute("noticeBoard", noticeBoard);
        return "noticeBoard/update";
    }

    @GetMapping("/delete/{id}")
    public String delete(
            Model model,
            @PathVariable("id") int id
    ) {
        NoticeBoardDTO noticeBoardDTO = new NoticeBoardDTO();
        noticeBoardDTO.setId(id);
        NoticeBoard noticeBoard = noticeBoardService.view(noticeBoardDTO);
        if (noticeBoard == null) {
            model.addAttribute("errCode", "err0001");
            model.addAttribute("errMsg", "존재하지 않는 게시글입니다.");
            return "error/error";
        }
        model.addAttribute("noticeBoard", noticeBoard);
        return "noticeBoard/delete";
    }

    @PostMapping("/createProc")
    public String createProc(
            Model model,
            NoticeBoardDTO noticeBoardDTO,
            Authentication authentication
    ) {
        int authenticationId = memberService.viewCurrentMember(authentication).getId();
        noticeBoardDTO.setMemberId(authenticationId);
        int result = noticeBoardService.createProc(noticeBoardDTO);
        if (result > 0) { //실패
            model.addAttribute("errCode", "err0002");
            model.addAttribute("errMsg", "게시글 등록 중 오류가 발생했습니다.");
            return "error/error";
        }
        return "redirect:/noticeBoard/list";
    }

    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            NoticeBoardDTO noticeBoardDTO
    ) {
        NoticeBoard noticeBoard = noticeBoardService.view(noticeBoardDTO);
        if (noticeBoard == null) {
            model.addAttribute("errCode", "err0003");
            model.addAttribute("errMsg", "등록된 게시글이 아닙니다.");
            return "error/error";
        }

        noticeBoardDTO.setCreateDate(noticeBoard.getCreateDate());
        noticeBoardDTO.setCnt(noticeBoard.getCnt());
        int result = noticeBoardService.updateProc(noticeBoardDTO);
        if (result > 0) { //실패
            model.addAttribute("errCode", "err0002");
            model.addAttribute("errMsg", "게시글 수정중 예외가 발생했습니다.");
            return "error/error";
        }
        return "redirect:/noticeBoard/view/" + noticeBoardDTO.getId();
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            Model model,
            NoticeBoardDTO noticeBoardDTO
    ) {
        try {
//            noticeBoardCommentService.setDeleteAll(noticeBoardDTO);
//            boardLikeService.deleteByNoticeBoardId();
//            noticeBoardService.setDelete(noticeBoardDTO);
            noticeBoardService.deleteNoticeBoard(noticeBoardDTO.getId());
            return "redirect:/noticeBoard/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errCode", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err2422");
            model.addAttribute("errMsg", "삭제 중 문제가 발생했습니다.");
            return "error/error";
        }
    }

    @Value("${app.image.upload-dir}")
    protected String uploadDir; //저장할 폴더

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

    @ResponseBody
    @PostMapping(value = "/uploadImage", produces = "application/json")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        File folder = new File(uploadDir);
        if (!folder.exists() && !folder.mkdirs() && !folder.exists()) throw new IOException("업로드 폴더 생성 실패: " + folder.getAbsolutePath());

        //파일명에서 한글 제거:
        String original = Objects.requireNonNull(file.getOriginalFilename(), "파일 이름이 null입니다.")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID() + "_" + original;

        file.transferTo(Paths.get(uploadDir, fileName).toFile()); // 파일 저장

        Map<String, Object> response = new HashMap<>();

        response.put("url", urlPrefix.endsWith("/")
                ? urlPrefix + fileName
                : urlPrefix + "/" + fileName);
        return response;
    }


}
