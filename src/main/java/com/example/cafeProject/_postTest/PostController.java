package com.example.cafeProject._postTest;

import com.example.cafeProject._boardTest.BoardDTO;
import com.example.cafeProject._boardTest.BoardService;
import com.example.cafeProject.member.MemberService;
import com.example.cafeProject.validation.ValidationGroups;
import com.example.exception.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final MemberService memberService;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${app.image.upload-dir}")
    protected String uploadDir; //저장할 폴더

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

    /**
     * 모든 핸들러 메서드 실행 전에 모델에 "imageUrlPrefix"를 자동 주입
     * - 뷰(Thymeleaf)에서 이미지 경로를 만들 때 공통으로 사용
     * - 설정값이 / 로 끝나지 않으면 /를 붙여서 보정
     */
    @ModelAttribute("imageUrlPrefix")
    public String imageUrlPrefix() {
        // /attach/summernote/ 형태로 끝에 / 보정
        return urlPrefix.endsWith("/") ? urlPrefix : urlPrefix + "/";
    }

    /**
     * 모든 핸들러 메서드 실행 전에 모델에 "board"를 자동 주입
     * - 요청 파라미터 b(=board code)가 있으면 해당 게시판 DTO를 조회해서 모델에 담음
     * - b 없이 들어오는 요청(예: /post/view/1 같이 b를 안 붙인 경우)은 null 반환
     * 뷰에서 `${board}`를 바로 참조할 수 있게 만들기 위한 전역 모델 세팅용 훅
     */
    @ModelAttribute("board")
    public BoardDTO board(
            @RequestParam(name = "b", required = false) String b
    ) {
        if (b == null || b.isBlank()) return null; // b 없이 들어오는 페이지는 일단 null
        return boardService.viewDTOByCode(b);
    }

    /** dto에 값을 담을 때 해당 필드는 무시됨 */
    protected String[] disallowedFields() {
        return new String[]{"memberId", "username", "createDate", "deleted", "deletedAt", "titleKey"};
    }

    @InitBinder("data")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(disallowedFields());
    }

    @GetMapping("/list")
    public String list(
            @RequestParam(name = "b") String code,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        Page<PostDTO> dto = postService.listByBoardCodeDTO(code, keyword, pageable);
        model.addAttribute("list", dto);
        model.addAttribute("keyword", keyword);
        return "post/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            @RequestParam(name="b", required = false) String code,
            @PathVariable int id,
            Authentication authentication,
            Model model
    ) {
        PostDTO dto = postService.viewDetailDTO(id);
        if (code == null || code.isBlank()) return "redirect:/post/view/" + id + "?b=" + dto.getBoardCode();
        model.addAttribute("data", dto);
        model.addAttribute("canEdit", postService.canEdit(id, authentication));
        return "post/view";
    }

    @GetMapping("/create")
    public String create(
            @RequestParam(name="b") String code,
            @ModelAttribute("board") BoardDTO board,
            Authentication authentication,
            Model model)
    {
        PostDTO dto = postService.newDTO();
        model.addAttribute("data", dto);
        return "post/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            @RequestParam(name="b", required = false) String code,
            @ModelAttribute("board") BoardDTO board,
            @PathVariable int id,
            Authentication authentication,
            Model model
    ) {
        PostDTO dto = postService.viewDetailDTO(id);
        if (code == null || code.isBlank()) return "redirect:/post/update/" + id + "?b=" + dto.getBoardCode();
        model.addAttribute("data", dto);
        return "post/update";
    }

    @PostMapping("/createProc")
    public String createProc(
            @Validated(ValidationGroups.OnCreate.class) @ModelAttribute("data") PostDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "post/create";
        try {
            postService.setInsert(dto);
            return "redirect:/post/list?b=" + dto.getBoardCode();
        } catch (AccessDeniedException | PermissionDeniedException e) {
            bindingResult.reject("permissionDenied", e.getMessage());
            return "post/create";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("invalid", e.getMessage());
            return "post/create";
        } catch (Exception e) {
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(
            @Validated(ValidationGroups.OnUpdate.class) @ModelAttribute("data") PostDTO dto,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "post/update";
        try {
            postService.setUpdate(dto);
            return "redirect:/post/view/" + dto.getId() + "?b=" + dto.getBoardCode();
        } catch (AccessDeniedException | PermissionDeniedException e) {
            bindingResult.reject("permissionDenied", e.getMessage());
            return "post/update";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("invalid", e.getMessage());
            return "post/update";
        } catch (Exception e) {
            return "redirect:/error/runtimeErrorPage";
        }
    }

    @PostMapping("/deleteProc/{id}")
    public String deleteProc(
            @PathVariable int id,
            @RequestParam(name="b") String code,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PostDTO dto = postService.newDTO();
            dto.setId(id);

            postService.softDelete(dto); // 소프트 삭제로 동작 (@SQLDelete)
            return "redirect:/post/list?b=" + code;
        } catch (AccessDeniedException | PermissionDeniedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/post/view/" + id + "?b=" + code;
        }
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
