package com.example.cafeProject._postTest;

import com.example.cafeProject._boardTest.BoardDTO;
import com.example.cafeProject._boardTest.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final BoardService boardService;

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

    /** 모든 링크에 자동으로 imageUrlPrefix를 모델에 담아서 보냄 */
    @ModelAttribute("imageUrlPrefix")
    public String imageUrlPrefix() {
        // /attach/summernote/ 형태로 끝에 / 보정
        return urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
    }

    /** 모든 링크에 자동으로 board에 대한 정보를 모델에 담아서 보냄 */
    @ModelAttribute("board")
    public BoardDTO board(
            @RequestParam(name = "b", required = false) String b
    ) {
        if (b == null || b.isBlank()) return null; // b 없이 들어오는 페이지는 일단 null
        return boardService.viewDTOByCode(b);
    }

    @GetMapping("/list")
    public String list(
            @RequestParam(name = "b") String code,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 30, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<PostDTO> dto = postService.listByBoardCodeDTO(code, keyword, pageable);
        model.addAttribute("list", dto);
        model.addAttribute("code", code);
        model.addAttribute("keyword", keyword);
        return "post/list";
    }

    @GetMapping("/create")
    public String create(@RequestParam(name="b") String code, Model model) {
        PostDTO dto = postService.newDTO();
        model.addAttribute("data", dto);
        return "post/create";
    }

    @PostMapping("/createProc")
    public String createProc(@ModelAttribute("data") PostDTO dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "post/create";
        postService.setInsert(dto);
        String code = boardService.view(dto.getBoardId()).getCode();
        return "redirect:/post/list?b=" + code;
    }
}
