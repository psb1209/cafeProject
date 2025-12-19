package com.example.cafeProject.informationBoard;

import com.example.cafeProject.informationBoardComment.InformationBoardComment;
import com.example.cafeProject.informationBoardComment.InformationBoardCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/informationBoard")
@RequiredArgsConstructor
@Controller
public class InformationBoardController {

    private final InformationBoardService informationBoardService;
    private final InformationBoardCommentService informationBoardCommentService;


    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<InformationBoard> informationBoardList = informationBoardService.getSelectAllPage(pageable);
            model.addAttribute("informationBoardList", informationBoardList);
            return "informationBoard/list";
        } catch (DataAccessException e) {
            model.addAttribute("errMsg", "접근 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errMsg", "예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "error/error";
        }
    }

    @GetMapping("/view/{id}")
    public String view(Model model, InformationBoardDTO informationBoardDTO, @PageableDefault(size = 8, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            InformationBoard informationBoard = informationBoardService.getSelectOneById(informationBoardDTO.getId());
            model.addAttribute("informationBoard", informationBoard);
            Page<InformationBoardComment> CommentList = informationBoardCommentService.getSelectAllPage(informationBoardDTO.getId(), pageable);
            model.addAttribute("commentList", CommentList);
            informationBoardService.increaseViewCount(informationBoardDTO.getId());
            return "informationBoard/view";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @GetMapping("/create")
    public String create() {
        return "informationBoard/create";
    }

    @GetMapping("/update/{id}")
    public String update(Model model, InformationBoardDTO informationBoardDTO) {
        try {
            InformationBoard informationBoard = informationBoardService.getSelectOneById(informationBoardDTO.getId());
            model.addAttribute("informationBoard", informationBoard);
            return "informationBoard/update";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    //---> 삭제페이지 없이 삭제버튼으로만 경고창 띄어서 삭제여부확인 뒤 바로 삭제처리.

    // Proc --------------------------------------------------------------------------------------------

    @PostMapping("/createProc")
    public String createProc(Model model, InformationBoardDTO informationBoardDTO, @AuthenticationPrincipal User user) {
        try {
            informationBoardService.setInsert(informationBoardDTO, user);
            return "redirect:/informationBoard/list";
        } catch (Exception e) {
            model.addAttribute("errCode", "error000");
            model.addAttribute("errMsg", "에러 발생"); // @ControllerAdvice를 통한 전역적인 예외처리 ??
            return "error/error";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(Model model, InformationBoardDTO informationBoardDTO, @AuthenticationPrincipal User user) {
        try {
            informationBoardService.setUpdate(informationBoardDTO, user);
            return "redirect:/informationBoard/view/" + informationBoardDTO.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @ResponseBody
    @PostMapping("/deleteProc")
    public String deleteProc(InformationBoardDTO informationBoardDTO, @AuthenticationPrincipal User user) {
        try {
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

}
