package com.example.cafeProject.boardTest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardPublicController {

    private final BoardService boardService;

    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size = 30, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        model.addAttribute("list", boardService.listDTO(pageable)); // 메서드는 네가 구현
        return "board/publicList";
    }

    @GetMapping("/{code}")
    public String enter(@PathVariable String code) {
        return "redirect:/post/list?b=" + code;
    }
}
