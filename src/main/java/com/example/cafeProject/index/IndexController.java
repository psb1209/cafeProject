package com.example.cafeProject.index;

import com.example.cafeProject._cafeTest.CafeService;
import com.example.cafeProject.communityBoard.CommunityBoard;
import com.example.cafeProject.communityBoard.CommunityBoardService;
import com.example.cafeProject.informationBoard.InformationBoard;
import com.example.cafeProject.informationBoard.InformationBoardService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.noticeBoard.NoticeBoard;
import com.example.cafeProject.noticeBoard.NoticeBoardService;
import com.example.validation.ManagementOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class IndexController {
    private final CafeService cafeService;
    private final MemberService memberService;
    private final NoticeBoardService noticeBoardService;
    private final CommunityBoardService communityBoardService;
    private final InformationBoardService informationBoardService;

    @ManagementOnly
    @GetMapping("/admin")
    public String admin(
            @RequestParam(name = "c", required = false) String code
    ) {
        if (code == null || code.isBlank()) return "redirect:/member/list";

        return "redirect:/cafe/meta/"+cafeService.viewDTOByCode(code).getId();
    }

    @GetMapping({"healthCafe/", "healthCafe"})
    public String list(
            Model model,
            @PageableDefault(size=10, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Sort sort = Sort.by(
                Sort.Order.desc("subNotice"),
                Sort.Order.desc("createDate")
        );

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort
        );

        List<NoticeBoard> subNoticeBoardNoticeList = noticeBoardService.getSubNoticeList();

        Page<NoticeBoard> noticeBoardList = noticeBoardService.list(sortedPageable, null);
        model.addAttribute("subNoticeBoardNoticeList", subNoticeBoardNoticeList);
        model.addAttribute("noticeBoardList", noticeBoardList);
        /* ====================================================================================================*/

        List<CommunityBoard> subCommunityBoardNoticeList = communityBoardService.getSubNoticeList();

        model.addAttribute("subCommunityBoardNoticeList", subCommunityBoardNoticeList);

        Page<CommunityBoard> communityBoardList = communityBoardService.list(pageable,null);
        model.addAttribute("communityBoardList", communityBoardList);
        /* ====================================================================================================*/
        List<InformationBoard> subInformationBoardNoticeList = informationBoardService.getSubNoticeList(); // 공지글 불러오기

        // 공지글 정렬값 담기
        Page<InformationBoard> informationBoardList = informationBoardService.list(sortedPageable, null);

        model.addAttribute("subInformationBoardNoticeList", subInformationBoardNoticeList);
        model.addAttribute("informationBoardList", informationBoardList);

        model.addAttribute("activeMenu", "main");

        return "index/index";
    }

    @GetMapping("/healthCafe/healthCafeIntroduction")
    public String healthCafeIntroduction(
            Model model
    ) {
        model.addAttribute("activeMenu", "about");
        return "healthCafe/healthCafeIntroduction";
    }

    @GetMapping("/error/runtimeErrorPage")
    private String runtimeErrorPage() {
        return "error/runtimeErrorPage";
    }
}
