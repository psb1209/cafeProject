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

    @GetMapping("/deleteProc/{commentid}")
    public String deleteProc(
            @PathVariable("commentid") int commentid,
            Model model
    ){
        CommunityBoardComment communityBoardComment=communityBoardCommentService.getSelectOneById(commentid);
        if(communityBoardComment==null){
            model.addAttribute("errorCode", "에러0005");
            model.addAttribute("errorMsg", "해당 댓글이 해당하지 않습니다.");
            return "error/error";
        }

        int communityBoardId=communityBoardComment.getCommunityBoard().getId();

        communityBoardCommentService.setDelete(commentid);
        return "redirect:/communityBoard/view/"+communityBoardId;
    }

}
