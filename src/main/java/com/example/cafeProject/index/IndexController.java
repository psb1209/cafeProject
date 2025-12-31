package com.example.cafeProject.index;

import com.example.cafeProject.communityBoard.CommunityBoard;
import com.example.cafeProject.communityBoard.CommunityBoardService;
import com.example.cafeProject.informationBoard.InformationBoard;
import com.example.cafeProject.informationBoard.InformationBoardService;
import com.example.cafeProject.noticeBoard2.NoticeBoard2;
import com.example.cafeProject.noticeBoard2.NoticeBoard2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class IndexController {
    private final NoticeBoard2Service noticeBoard2Service;
    private final CommunityBoardService communityBoardService;
    private final InformationBoardService informationBoardService;

    @GetMapping({"healthCafe/", "healthCafe"})
    public String list(
            Model model,
            @PageableDefault(size=10, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NoticeBoard2> noticeBoard2List = noticeBoard2Service.list(pageable, null);
        model.addAttribute("noticeBoard2List", noticeBoard2List);

        Page<CommunityBoard> communityBoardList = communityBoardService.list(pageable,null);
        model.addAttribute("communityBoardList", communityBoardList);

        Page<InformationBoard> informationBoardList = informationBoardService.getSelectAllPage(pageable, null);
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
}
