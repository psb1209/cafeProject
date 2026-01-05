package com.example.cafeProject.noticeBoard;

import com.example.cafeProject.board_view.Board_viewDTO;
import com.example.cafeProject.board_view.Board_viewService;
import com.example.cafeProject.like.LikeService;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoardComment.NoticeBoardComment;
import com.example.cafeProject.noticeBoardComment.NoticeBoardCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@RequiredArgsConstructor
@RequestMapping("/noticeBoard")
@Controller
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final NoticeBoardCommentService noticeBoardCommentService;
    private final MemberService memberService;
    private final LikeService likeService;
    private final Board_viewService board_viewService;

    String dirName = "noticeBoard";

    @PostMapping("/toggleNotice/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    public String toggleNotice(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        noticeBoardService.toggleNotice(id);
        redirectAttributes.addFlashAttribute("msg", "공지 상태가 변경되었습니다.");
        return "redirect:/" + dirName + "/list";
    }

    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 2, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        /*====================================================== 공지글!! =======================================================*/
        List<NoticeBoard> subNoticeList = noticeBoardService.getSubNoticeList();

        // 2️⃣ 공지 → 일반 게시글 순 + 최신순 정렬
        Sort sort = Sort.by(
                Sort.Order.desc("subNotice"),
                Sort.Order.desc("createDate")
        );

        // 정렬값 다시 받기
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        Page<NoticeBoard> noticeBoardList =
                noticeBoardService.list(sortedPageable, keyword);

        model.addAttribute("subNoticeList", subNoticeList);
        /*====================================================== 공지글!! =======================================================*/
        model.addAttribute("noticeBoardList", noticeBoardList);
        model.addAttribute("activeMenu", "noticeBoard");
        model.addAttribute("keyword", keyword);

        // ======================
        // ✅ 조회수 Map 생성
        // ======================
        Map<Integer, Integer> viewCntMap = new HashMap<>(); // 한 게시글에 여러명의 id값이 있기에 Map으로 값을 여러개 받는다

        // 일반 게시글
        for (NoticeBoard board : noticeBoardList.getContent()) {
            int viewCnt = board_viewService.board_viewCnt("notice", board.getId());
            viewCntMap.put(board.getId(), viewCnt);
        }
        // 공지 게시글
        for (NoticeBoard board : subNoticeList) {
            int viewCnt = board_viewService.board_viewCnt("notice", board.getId());
            viewCntMap.put(board.getId(), viewCnt);
        }

        model.addAttribute("viewCntMap", viewCntMap);

        return dirName + "/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            NoticeBoardDTO noticeBoardDTO,
            Authentication authentication,
            @PageableDefault(size=3, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            NoticeBoard noticeBoard = noticeBoardService.getSelectOneById(noticeBoardDTO);
            model.addAttribute("noticeBoard", noticeBoard);
            Page<NoticeBoardComment> commentList = noticeBoardCommentService.getCommentListPage(noticeBoardDTO.getId(), pageable);
            model.addAttribute("commentList", commentList);
            model.addAttribute("activeMenu", "noticeBoard");

            boolean isLike = false;

            if(!memberService.isNotLogin(authentication)) {
                Member member = memberService.viewCurrentMember(authentication);
                isLike = likeService.isLike(noticeBoard.getId(), member.getId());
            }

            model.addAttribute("isLike", isLike);
            model.addAttribute("likeCnt", likeService.likeCnt(dirName, noticeBoard.getId()));

            if (!memberService.isNotLogin(authentication)) {
                Member member = memberService.viewCurrentMember(authentication);

                Board_viewDTO board_viewDTO = new Board_viewDTO();
                board_viewDTO.setUserId(member.getId());
                board_viewDTO.setNoticeBoardNumber(noticeBoard.getId());

                board_viewService.createProc(board_viewDTO);
            }

            int viewCnt = board_viewService.board_viewCnt(
                    "notice",
                    noticeBoard.getId()
            );
            model.addAttribute("viewCnt", viewCnt);

            return dirName + "/view";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/create")
    public String create(
            Model model
    ) {
        model.addAttribute("activeMenu", "noticeBoard");
        return dirName + "/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            Model model,
            NoticeBoardDTO noticeBoardDTO
    ) {
        try {
            NoticeBoard noticeBoard = noticeBoardService.getSelectOneById(noticeBoardDTO);
            model.addAttribute("noticeBoard", noticeBoard);
            model.addAttribute("activeMenu", "noticeBoard");
            return dirName + "/update";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(
            Model model,
            NoticeBoardDTO noticeBoardDTO
    ) {
        try {
            NoticeBoard noticeBoard = noticeBoardService.getSelectOneById(noticeBoardDTO);
            model.addAttribute("noticeBoard", noticeBoard);
            model.addAttribute("activeMenu", "noticeBoard");
            return dirName + "/delete";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @PostMapping("/createProc")
    public String createProc(
            Model model,
            NoticeBoardDTO noticeBoardDTO,
            RedirectAttributes redirectAttributes,
            Authentication authentication
    ) {
        try {
            Member member = memberService.viewCurrentMember(authentication);
            noticeBoardDTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = noticeBoardService.setInsert(noticeBoardDTO, authentication);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/noticeBoard/list";
            }
            return "redirect:/noticeBoard/list"; //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }

    }

    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            NoticeBoardDTO noticeBoardDTO
    ) {
        try {
            NoticeBoard noticeBoard = noticeBoardService.setUpdate(noticeBoardDTO);
            return "redirect:/" + dirName + "/view/" + noticeBoardDTO.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err2322");
            model.addAttribute("errMsg", "수정 중 문제가 발생했습니다.");
            return "error/error";
        }
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            Model model,
            NoticeBoardDTO noticeBoardDTO
    ) {
        try {
            noticeBoardCommentService.setDeleteAll(noticeBoardDTO);
            noticeBoardService.setDelete(noticeBoardDTO);
            return "redirect:/" + dirName + "/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
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
    //********************************************************************************************


}
