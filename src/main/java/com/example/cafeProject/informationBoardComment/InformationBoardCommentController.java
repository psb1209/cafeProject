package com.example.cafeProject.informationBoardComment;

import com.example.cafeProject.informationBoard.InformationBoardDTO;
import com.example.cafeProject.informationBoard.InformationBoardService;
import com.example.cafeProject.member.Member;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.operationBoard.OperationBoardDTO;
import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import com.example.cafeProject.operationBoardComment.OperationBoardCommentDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@RequestMapping("/informationBoardComment")
@RequiredArgsConstructor
@Controller
public class InformationBoardCommentController {

    private final InformationBoardCommentService informationBoardCommentService;
    private final InformationBoardService informationBoardService;
    private final MemberService memberService;

    String dirName = "informationBoard";

    @PostMapping("/createProc")
    public String createProc(Model model,
                             InformationBoardCommentDTO informationBoardCommentDTO,
                             @AuthenticationPrincipal User user,
                             RedirectAttributes redirectAttributes)
    {
        //회원값 찾기
        try {
            Member member = memberService.viewCurrentMember();
            informationBoardCommentDTO.setMemberId(member.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1110");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        //해당 부모글 찾기
        try {
            InformationBoardDTO informationBoardDTO = new InformationBoardDTO();
            informationBoardDTO.setId(informationBoardCommentDTO.getInformationBoardId());
            informationBoardService.getSelectOneById(informationBoardDTO.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            boolean isUpgraded = informationBoardCommentService.setInsert(informationBoardCommentDTO, user);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
            }
            return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId(); //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }


    @PostMapping("/deleteProc")
    public String deleteProc(Model model,
                             InformationBoardCommentDTO informationBoardCommentDTO,
                             @AuthenticationPrincipal User user)
    {
        try {
            InformationBoardDTO informationBoardDTO = new InformationBoardDTO();
            informationBoardDTO.setId(informationBoardCommentDTO.getInformationBoardId());
            informationBoardService.getSelectOneById(informationBoardDTO.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            informationBoardCommentService.setDelete(informationBoardCommentDTO, user);
            return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
        } catch (Exception e) {
            log.error(e.getMessage());
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 댓글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @PostMapping("/update")
    public String update(
            InformationBoardCommentDTO informationBoardCommentDTO,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            InformationBoardComment informationBoardCommentUpdate = informationBoardCommentService.getSelectOneById(informationBoardCommentDTO.getId());
            redirectAttributes.addFlashAttribute("informationBoardCommentUpdate", informationBoardCommentUpdate);
            return "redirect:/informationBoard/view/" + informationBoardCommentDTO.getInformationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            InformationBoardCommentDTO informationBoardCommentDTO,
            @AuthenticationPrincipal User user
    ) {
        try {
            InformationBoardDTO informationBoardDTO = new InformationBoardDTO();
            informationBoardDTO.setId(informationBoardCommentDTO.getInformationBoardId());
            informationBoardService.getSelectOneById(informationBoardDTO.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }

        try {
            informationBoardCommentService.setUpdate(informationBoardCommentDTO, user);
            return "redirect:/" + dirName + "/view/" + informationBoardCommentDTO.getInformationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err1111");
            model.addAttribute("errMsg", e.getMessage()); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err2242");
            model.addAttribute("errMsg", "수정 중 문제가 발생했습니다."); // e.getMessage() 기본적으로 나오는 메세지
            return "error/error";
        }
    }

    /*============================================== 대댓글 ===============================================*/
    //대댓글 추가처리
    @PostMapping("/replyProc")
    public String replyProc(
            InformationBoardCommentDTO informationBoardCommentDTO,
            Model model,
            Authentication authentication
    ){
        try {
            UserDetails userDetails = (UserDetails)authentication.getPrincipal();
            // 대댓글 등록
            informationBoardCommentService.replySetInsert(informationBoardCommentDTO, userDetails);
            return "redirect:/information/view/" + informationBoardCommentDTO.getInformationBoardId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "221213321");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "에러0005");
            model.addAttribute("errMsg", "해당 댓글이 해당하지 않습니다.");
            return "error/error";
        }
    }
    /*============================================== 대댓글 ===============================================*/

}
