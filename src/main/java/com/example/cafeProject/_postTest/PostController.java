package com.example.cafeProject._postTest;

import com.example.cafeProject._boardTest.BoardDTO;
import com.example.cafeProject._boardTest.BoardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final BoardService boardService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${app.image.upload-dir}")
    protected String uploadDir; //저장할 폴더

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

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable int id
    ) {
        PostDTO dto = postService.viewDetailDTO(id);
        log.debug("=============================================");
        log.debug("view id={} title={} content={}", id, dto.getTitle(), dto.getContent());
        model.addAttribute("data", dto);
        return "post/view";
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

    @ResponseBody
    @PostMapping(value = "/uploadImage", produces = "application/json")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        File folder = new File(uploadDir);
        if (!folder.exists() && !folder.mkdirs() && !folder.exists()) throw new IOException("업로드 폴더 생성 실패: " + folder.getAbsolutePath());

        //파일명에서 한글 제거:
        String original = Objects.requireNonNull(file.getOriginalFilename(), "파일 이름이 null입니다.")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID() + "_" + original;

        file.transferTo(Paths.get(uploadDir, fileName).toFile()); // 파일 저장

        Map<String, Object> response = new HashMap<>();

        response.put("url", urlPrefix.endsWith("/")
                ? urlPrefix + fileName
                : urlPrefix + "/" + fileName);
        response.put("fileName", fileName);
        return response;
    }
}
