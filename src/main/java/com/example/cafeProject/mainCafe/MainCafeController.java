package com.example.cafeProject.mainCafe;

import com.example.cafeProject._cafeTest.CafeService;
import com.example.cafeProject.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class MainCafeController {

    private final CafeService cafeService;
    private final MemberService memberService;

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

    /** 모든 링크에 자동으로 imageUrlPrefix를 모델에 담아서 보냄 */
    @ModelAttribute("imageUrlPrefix")
    public String imageUrlPrefix() {
        // /attach/summernote/ 형태로 끝에 / 보정
        return urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
    }

    @GetMapping({"/", ""})
    public String mainIndex(
            @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute("activeMenu", "main");       // 네비(사이드바/상단메뉴)용
        model.addAttribute("activeTab", "my");      // 카페탭 기본 선택
        model.addAttribute("list", cafeService.listVisibleDTO(pageable, memberService.getEffectiveRoles(authentication)));
        return "index/mainIndex";
    }

    @GetMapping("/explore_cafes/{code}")
    public String explore_cafes(
            @PathVariable("code") String code,
            Model model
    ) {
        if ("subscribe".equals(code)) model.addAttribute("activeMenu", "subscribe");
        else if ("notice".equals(code)) model.addAttribute("activeMenu", "notice");
        else if ("cafes_by_location".equals(code)) model.addAttribute("activeMenu", "LOCATION");
        else if ("featured_cafes".equals(code)) model.addAttribute("activeMenu", "FEATURED");
        else if ("themed_cafes".equals(code)) model.addAttribute("activeMenu", "THEMED");
        else if ("top_cafes".equals(code)) model.addAttribute("activeMenu", "TOP");
        return "explore_cafes/" + code;
    }

    @GetMapping("/explore_cafes/my")
    public String myCafe(
            @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute("activeMenu", "main");
        model.addAttribute("activeTab", "my");
        model.addAttribute("list", cafeService.listVisibleDTO(pageable, memberService.getEffectiveRoles(authentication)));
        return "explore_cafes/my :: cafeList"; // explore_cafes/my의 cafeList 조각만 로딩
    }

    @GetMapping("/explore_cafes/favorite")
    public String favoriteCafe(
            Model model
    ) {
        model.addAttribute("activeMenu", "main");
        model.addAttribute("activeTab", "favorite");
        return "explore_cafes/favorite :: cafeList"; // explore_cafes/favorite의 cafeList 조각만 로딩
    }

}
