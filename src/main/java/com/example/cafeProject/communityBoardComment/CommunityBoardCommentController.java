package com.example.cafeProject.communityBoardComment;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/communityBoardComment")
@RequiredArgsConstructor
@Controller
public class CommunityBoardCommentController {
    private final CommunityBoardCommentService communityBoardCommentService;

    @PostMapping("/createProc")
    public String CreateProc(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model
    ){
            try{
                communityBoardCommentService.setInsert(communityBoardCommentDTO);
                return "redirect:/communityBoard/view/"+communityBoardCommentDTO.getCommunityBoardId();
            } catch (Exception e) {
                model.addAttribute("errorCode", "에러0005");
                model.addAttribute("errorMsg", e.getMessage());
                return "error/error";
            }
    }

    @GetMapping("/deleteProc/{commentId}")
    public String deleteProc(
            @PathVariable("commentId") int commentId,
            Model model
    ){
        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(commentId);
        if(communityBoardComment==null){
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 존재하지 않습니다.");
            return "error/error";
        }

        int communityBoardId=communityBoardComment.getCommunityBoard().getId();

        communityBoardCommentService.setDelete(commentId);
        return "redirect:/communityBoard/view/"+communityBoardId;
    }

    @GetMapping("/commentUpdate/{commentId}")
    public String commentUpdate(
            @PathVariable("commentId") int commentId,
            Model model
    ){
        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(commentId);
        if(communityBoardComment==null){
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 존재하지 않습니다.");
            return "error/error";
        }
        model.addAttribute("communityBoardComment",communityBoardComment);
        return "communityBoard/commentUpdate";
    }


    @PostMapping("/updateProc")
    public String updateProc(
            CommunityBoardCommentDTO communityBoardCommentDTO,
            Model model
    ){
        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(communityBoardCommentDTO.getId());
        if(communityBoardComment==null){
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 존재하지 않습니다.");
            return "error/error";
        }



        int communityBoardId=communityBoardComment.getCommunityBoard().getId();

        communityBoardCommentService.setUpdate(communityBoardCommentDTO);
        return "redirect:/communityBoard/view/"+communityBoardId;
    }

}
