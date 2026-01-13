package com.example.cafeProject.informationBoard;

import com.example.cafeProject.board_view.Board_viewDTO;
import com.example.cafeProject.board_view.Board_viewService;
import com.example.cafeProject.informationBoardComment.InformationBoardComment;
import com.example.cafeProject.informationBoardComment.InformationBoardCommentService;
import com.example.cafeProject.like.LikeService;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.validation.ManagementOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

@RequestMapping("/informationBoard")
@RequiredArgsConstructor
@Controller
public class InformationBoardController {

    private final InformationBoardService informationBoardService;
    private final InformationBoardCommentService informationBoardCommentService;
    private final MemberService memberService;
    private final LikeService likeService;
    private final Board_viewService board_viewService;

    String dirName = "informationBoard";
    /*=============================== 각 게시판 공지글 ===================================*/

    // 공지글과 일반글로 전환
    @ManagementOnly
    @PostMapping("/toggleNotice/{id}")
    public String toggleNotice(
            @PathVariable Integer id, // 게시글 아이디
            RedirectAttributes redirectAttributes
    ) {
        informationBoardService.toggleNotice(id);
        redirectAttributes.addFlashAttribute("msg", "공지 상태가 변경되었습니다.");
        return "redirect:/" + dirName + "/list";
    }


    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(required = false) String keyword, // 검색값
            @RequestParam (required = false) String sort,
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        /*====================================================== 공지글!! =======================================================*/
        List<InformationBoard> subNoticeList = informationBoardService.getSubNoticeList(); // 공지글 불러오기

        Pageable sortedPageable = pageable;

        if(sort != null && !sort.isBlank()) {
            String[] sortList = sort.split(",");

            // 2️⃣ 공지 → 일반글 순 + 최신순 정렬 (기본형)
            Sort sort2 = Sort.by(
                    Sort.Order.desc("subNotice"),
                    Sort.Order.desc("createDate")
            );

            // 정렬 방식에 따른 바꿈
            if ("createDate".equals(sortList[0]) && "asc".equals(sortList[1])) {
                sort2 = Sort.by(
                        Sort.Order.desc("subNotice"),
                        Sort.Order.asc("createDate")
                );
            } else if ("cnt".equals(sortList[0]) && "desc".equals(sortList[1])) {
                sort2 = Sort.by(
                        Sort.Order.desc("subNotice"),
                        Sort.Order.desc("cnt"),
                        Sort.Order.desc("createDate")
                );
            } else if ("cnt".equals(sortList[0]) && "asc".equals(sortList[1])) {
                sort2 = Sort.by(
                        Sort.Order.desc("subNotice"),
                        Sort.Order.asc("cnt"),
                        Sort.Order.desc("createDate")
                );
            }

            // 정렬값 다시 받기
            sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sort2
            );
        }

        // 공지글 정렬값 담기
        Page<InformationBoard> informationBoardList = informationBoardService.list(sortedPageable, keyword);

        model.addAttribute("subNoticeList", subNoticeList);
        /*====================================================== 공지글!! =======================================================*/
        model.addAttribute("informationBoardList", informationBoardList);
        model.addAttribute("activeMenu", "informationBoard");
        model.addAttribute("keyword", keyword);

        // ======================
        // 조회수 Map 생성
        // ======================
        Map<Integer, Integer> viewCntMap = new HashMap<>(); // 한 게시글에 여러명의 id값이 있기에 Map으로 값을 여러개 받는다

        // 일반 글
        for (InformationBoard board : informationBoardList.getContent()) {
            int viewCnt = board_viewService.board_viewCnt("information", board.getId());
            viewCntMap.put(board.getId(), viewCnt);
        }
        /*====================================================== 공지글!! =======================================================*/
        // 공지글
        for (InformationBoard board : subNoticeList) {
            int viewCnt = board_viewService.board_viewCnt("information", board.getId());
            viewCntMap.put(board.getId(), viewCnt);
        }
        /*====================================================== 공지글!! =======================================================*/
        model.addAttribute("viewCntMap", viewCntMap);
        return dirName + "/list";
    }
    /*=====================================================================================*/

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            InformationBoardDTO informationBoardDTO,
            Authentication authentication,
            @PageableDefault(size=10, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            // 해당 게시글 존재여부 확인
            InformationBoard informationBoard = informationBoardService.getSelectOneById(informationBoardDTO.getId());
            model.addAttribute("informationBoard", informationBoard);

            /*============================================== 대댓글 ===============================================*/
            // 해당 게시글의 댓글, 대댓글 값 불러오기
            Page<InformationBoardComment> commentList = informationBoardCommentService.getCommentListPage(informationBoardDTO.getId(), pageable);

            model.addAttribute("commentList", commentList);
            /*============================================== 대댓글 ===============================================*/
            model.addAttribute("activeMenu", "informationBoard");

            boolean isLike = false;

            if(!memberService.isNotLogin(authentication)) {
                Member member = memberService.viewCurrentMember(authentication);
                isLike = likeService.isLike("information", informationBoard.getId(), member.getId());
            }

            model.addAttribute("isLike", isLike);
            model.addAttribute("likeCnt", likeService.likeCnt(dirName, informationBoard.getId()));

            if (!memberService.isNotLogin(authentication)) {
                Member member = memberService.viewCurrentMember(authentication);

                Board_viewDTO board_viewDTO = new Board_viewDTO();
                board_viewDTO.setUserId(member.getId());
                board_viewDTO.setInformationBoardNumber(informationBoard.getId());

                board_viewService.createProc(board_viewDTO);
            }

            int viewCnt = board_viewService.board_viewCnt("information", informationBoard.getId());
            informationBoardDTO.setCnt(viewCnt);
            informationBoardService.updateViewCnt(informationBoardDTO);
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
        model.addAttribute("activeMenu", "informationBoard");
        return dirName + "/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            Model model,
            InformationBoardDTO informationBoardDTO,
            Authentication authentication
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String loginId = userDetails.getUsername(); // 로그인한 아이디
        if (!loginId.equals(informationBoardService.getSelectOneById(informationBoardDTO.getId()).getMember().getUsername())) {
            model.addAttribute("errCode", "err0000");
            model.addAttribute("errMsg", "로그인 후 이용 가능합니다.");
            return "error/error";
        }
        try {
            InformationBoard informationBoard = informationBoardService.getSelectOneById(informationBoardDTO.getId());
            model.addAttribute("informationBoard", informationBoard);
            model.addAttribute("activeMenu", "informationBoard");
            return dirName + "/update";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }


    // Proc --------------------------------------------------------------------------------------------

    @PostMapping("/createProc")
    public String createProc(Model model,
                             InformationBoardDTO informationBoardDTO,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes)
    {

        try {
            Member member = memberService.viewCurrentMember();
            informationBoardDTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = informationBoardService.setInsert(informationBoardDTO, authentication);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/informationBoard/list";
            }
            return "redirect:/informationBoard/list"; //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(
            Model model, InformationBoardDTO informationBoardDTO,
            @AuthenticationPrincipal User user
    ) {
        try {
            informationBoardService.setUpdate(informationBoardDTO, user);
            return "redirect:/" + dirName + "/view/" + informationBoardDTO.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err2322");
            model.addAttribute("errMsg", "수정 중 문제가 발생했습니다.");
            return "error/error";
        }
    }

    @ResponseBody
    @PostMapping("/deleteProc")
    public String deleteProc(InformationBoardDTO informationBoardDTO,
                             @AuthenticationPrincipal User user)
    {
        try {
            informationBoardCommentService.setDeleteAll(informationBoardDTO);
            informationBoardService.setDelete(informationBoardDTO, user);
            return "<script>" +
                    "alert('성공적으로 삭제되었습니다.');" +
                    "location.href='/informationBoard/list';" +
                    "</script>";
        } catch (Exception e) {
            return "<script>" +
                    "alert('삭제에 실패했습니다: " + e.getMessage() + "');" +
                    "history.back();" + // 이전 상세 페이지로 되돌리기
                    "</script>";
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
