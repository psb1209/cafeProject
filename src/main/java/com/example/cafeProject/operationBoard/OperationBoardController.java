package com.example.cafeProject.operationBoard;

import com.example.cafeProject.board_view.Board_viewDTO;
import com.example.cafeProject.board_view.Board_viewService;
import com.example.cafeProject.like.LikeDTO;
import com.example.cafeProject.like.LikeService;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import com.example.cafeProject.operationBoardComment.OperationBoardCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/operationBoard")
@Controller
public class OperationBoardController {

    private final OperationBoardService operationBoardService;
    private final OperationBoardCommentService operationBoardCommentService;
    private final MemberService memberService;
    private final LikeService likeService;
    private final Board_viewService board_viewService;

    String dirName = "operationBoard";


    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size=5, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OperationBoard> operationBoardList = operationBoardService.list(pageable, keyword);
        model.addAttribute("operationBoardList", operationBoardList);
        model.addAttribute("activeMenu", "operationBoard");
        model.addAttribute("keyword", keyword);

        // ======================
        // ✅ 조회수 Map 생성
        // ======================
        Map<Integer, Integer> viewCntMap = new HashMap<>(); // 한 게시글에 여러명의 id값이 있기에 Map으로 값을 여러개 받는다

        for (OperationBoard board : operationBoardList.getContent()) {
            int viewCnt = board_viewService.board_viewCnt(
                    "operation",
                    board.getId()
            );
            viewCntMap.put(board.getId(), viewCnt);
        }

        model.addAttribute("viewCntMap", viewCntMap);

        return dirName + "/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            OperationBoardDTO operationBoardDTO,
            Authentication authentication,
            @PageableDefault(size=3, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.getSelectOneById(operationBoardDTO);
            model.addAttribute("operationBoard", operationBoard);
            Page<OperationBoardComment> commentList = operationBoardCommentService.getCommentListPage(operationBoardDTO.getId(), pageable);
            model.addAttribute("commentList", commentList);
            model.addAttribute("activeMenu", "operationBoard");
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            boolean isLike = false;

            if(!memberService.isNotLogin(authentication)) {
                Member member = memberService.viewCurrentMember(authentication);
                isLike = likeService.isLike(operationBoard.getId(), member.getId());
            }

            model.addAttribute("isLike", isLike);
            model.addAttribute("likeCnt", likeService.likeCnt(dirName, operationBoard.getId()));

            if (!memberService.isNotLogin(authentication)) {
                Member member = memberService.viewCurrentMember(authentication);

                Board_viewDTO board_viewDTO = new Board_viewDTO();
                board_viewDTO.setUserId(member.getId());
                board_viewDTO.setOperationBoardNumber(operationBoard.getId());

                board_viewService.createProc(board_viewDTO);
            }

            int viewCnt = board_viewService.board_viewCnt(
                    "operation",
                    operationBoard.getId()
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
        model.addAttribute("activeMenu", "operationBoard");
        return dirName + "/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            Model model,
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.getSelectOneById(operationBoardDTO);
            model.addAttribute("operationBoard", operationBoard);
            model.addAttribute("activeMenu", "operationBoard");
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
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.getSelectOneById(operationBoardDTO);
            model.addAttribute("operationBoard", operationBoard);
            model.addAttribute("activeMenu", "operationBoard");
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
            OperationBoardDTO operationBoardDTO,
            RedirectAttributes redirectAttributes,
            Authentication authentication,
            @AuthenticationPrincipal User user
    ) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String loginId = userDetails.getUsername(); // 로그인했을 때 아이디

        try {
            Member member = operationBoardService.getSelectOneByUsername(authentication);
            operationBoardDTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = operationBoardService.setInsert(operationBoardDTO, authentication);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/operationBoard/list";
            }
            return "redirect:/operationBoard/list"; //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }

    }

    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.setUpdate(operationBoardDTO);
            return "redirect:/" + dirName + "/view/" + operationBoardDTO.getId();
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
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            operationBoardCommentService.setDeleteAll(operationBoardDTO);
            operationBoardService.setDelete(operationBoardDTO);
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
