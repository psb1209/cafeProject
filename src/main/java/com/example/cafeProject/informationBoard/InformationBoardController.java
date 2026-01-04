package com.example.cafeProject.informationBoard;

import com.example.cafeProject.informationBoardComment.InformationBoardComment;
import com.example.cafeProject.informationBoardComment.InformationBoardCommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

@RequestMapping("/informationBoard")
@RequiredArgsConstructor
@Controller
public class InformationBoardController {

    private final InformationBoardService informationBoardService;
    private final InformationBoardCommentService informationBoardCommentService;

    private static final Logger log = LoggerFactory.getLogger(InformationBoardController.class);


    @GetMapping("/list")
    public String list(Model model,
                       @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(required = false) String keyword) {
        try {
            Page<InformationBoard> informationBoardList = informationBoardService.getSelectAllPage(pageable, keyword);
            model.addAttribute("informationBoardList", informationBoardList);
            model.addAttribute("activeMenu", "informationBorad");
            model.addAttribute("keyword", keyword);

            return "informationBoard/list";
        } catch (DataAccessException e) {
            model.addAttribute("errMsg", "접근 중 오류가 발생했습니다. 관리자에게 문의해주세요.");
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errMsg", "예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "error/error";
        }
    }

    @GetMapping("/view/{id}")
    public String view(Model model, InformationBoardDTO informationBoardDTO, @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            InformationBoard informationBoard = informationBoardService.getSelectOneById(informationBoardDTO.getId());
            model.addAttribute("informationBoard", informationBoard);
            Page<InformationBoardComment> CommentList = informationBoardCommentService.getSelectAllPage(informationBoardDTO.getId(), pageable);
            model.addAttribute("commentList", CommentList);
            informationBoardService.increaseViewCount(informationBoardDTO.getId());
            return "informationBoard/view";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @GetMapping("/create")
    public String create() {
        return "informationBoard/create";
    }

    @GetMapping("/update/{id}")
    public String update(Model model, InformationBoardDTO informationBoardDTO) {
        try {
            InformationBoard informationBoard = informationBoardService.getSelectOneById(informationBoardDTO.getId());
            model.addAttribute("informationBoard", informationBoard);
            return "informationBoard/update";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    //---> 삭제페이지 없이 삭제버튼으로만 경고창 띄어서 삭제여부확인 뒤 바로 삭제처리.

    // Proc --------------------------------------------------------------------------------------------

    @PostMapping("/createProc")
    public String createProc(Model model, InformationBoardDTO informationBoardDTO,
                             @AuthenticationPrincipal User user, RedirectAttributes redirectAttributes) { //리다이텍트할때 데이터를 같이 보내기 위해서 객체주입
        try {
            boolean isUpgraded = informationBoardService.setInsert(informationBoardDTO, user);
            if(isUpgraded) { //등급 상승시 "msg" 데이터를 같이 리다이렉트 시킴
                redirectAttributes.addFlashAttribute("msg","축하합니다! 등급이 올랐습니다!🎉"); //윈도우 로고 키(⊞) + 마침표(.) --> 임티창
                return "redirect:/informationBoard/list";
            }
            return "redirect:/informationBoard/list"; //등급 안올랐으면 걍 조용히 이동

        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(Model model, InformationBoardDTO informationBoardDTO, @AuthenticationPrincipal User user) {
        try {
            informationBoardService.setUpdate(informationBoardDTO, user);
            return "redirect:/informationBoard/view/" + informationBoardDTO.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "error404");
            model.addAttribute("errMsg", "요청하신 게시글을 찾을 수 없습니다.");
            return "error/error";
        }
    }

    @ResponseBody
    @PostMapping("/deleteProc")
    public String deleteProc(InformationBoardDTO informationBoardDTO, @AuthenticationPrincipal User user) {
        try {
            informationBoardService.setDelete(informationBoardDTO, user);
            return "<script>" +
                    "alert('성공적으로 삭제되었습니다.');" +
                    "location.href='/informationBoard/list';" +
                    "</script>";
        } catch (Exception e) {
            return "<script>" +
                    "alert('삭제에 실패했습니다: " + e.getMessage() + "');" +
                    "history.back();" + // 이전 상세 페이지로 되돌리기
                    "</script>";
        }
    }

    @Value("${app.image.upload-dir}")
    protected String uploadDir; //저장할 폴더

    @Value("${app.image.url-prefix}")
    protected String urlPrefix; // 클라이언트에 반환할 URL

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
        return response;
    }

}
