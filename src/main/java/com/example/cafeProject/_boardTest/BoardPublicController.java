package com.example.cafeProject._boardTest;

import com.example.cafeProject._cafeTest.CafeDTO;
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
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cafe/{cafeCode}/board")
@RequiredArgsConstructor
public class BoardPublicController {

    private final BoardService boardService;
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

    @ModelAttribute("cafe")
    public CafeDTO cafeAttr(
            @PathVariable String cafeCode
    ) {
        if (cafeCode == null || cafeCode.isBlank()) return null;
        return cafeService.viewDTOByCode(cafeCode);
    }

    @GetMapping({"", "/", "/list"})
    public String list(
            @PathVariable String cafeCode,
            @RequestParam(name = "keyword", required = false) String keyword,
            @PageableDefault(size = 30, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication,
            Model model
    ) {
        model.addAttribute("list", boardService.listVisibleDTO(cafeCode, pageable, memberService.getEffectiveRoles(authentication), keyword));
        model.addAttribute("keyword", keyword);
        return "board/publicList";
    }

    // "/cafe/{cafeCode}/board/{boardCode}" 형식으로 오는 링크를 post 컨트롤러에게 떠넘김
    @GetMapping("/{boardCode}")
    public String enter(
            @PathVariable String cafeCode,
            @PathVariable String boardCode
    ) {
        return "redirect:/cafe/" + cafeCode + "/post/list?b=" + boardCode;
    }
}
