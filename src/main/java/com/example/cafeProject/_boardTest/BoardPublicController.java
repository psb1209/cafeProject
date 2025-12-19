package com.example.cafeProject._boardTest;

import com.example.cafeProject.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardPublicController {

    private final BoardService boardService;
    private final MemberService memberService;

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

    /** 모든 링크에 자동으로 imageUrlPrefix를 모델에 담아서 보냄 */
    @ModelAttribute("imageUrlPrefix")
    public String imageUrlPrefix() {
        // /attach/summernote/ 형태로 끝에 / 보정
        return urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
    }

    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 30, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            @RequestParam(required = false) String keyword,
            Authentication authentication
    ) {
        model.addAttribute("list", boardService.listVisibleDTO(pageable, memberService.getEffectiveRoles(authentication), keyword));
        model.addAttribute("keyword", keyword);
        return "board/publicList";
    }

    @GetMapping("/{code}")
    public String enter(@PathVariable String code) {
        return "redirect:/post/list?b=" + code;
    }
}
