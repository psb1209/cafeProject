package com.example.cafeProject.operationBoard;

import com.example.cafeProject.operationBoardComment.OperationBoardComment;
import com.example.cafeProject.operationBoardComment.OperationBoardCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/operationBoard")
@Controller
public class OperationBoardController {

    private final OperationBoardService operationBoardService;
    private final OperationBoardCommentService operationBoardCommentService;

    String dirName = "operationBoard";

    @GetMapping("/list")
    public String list(
            Model model,
            @PageableDefault(size=8, sort="id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OperationBoard> operationBoardList = operationBoardService.list(pageable);
        model.addAttribute("operationBoardList", operationBoardList);
        return dirName + "/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            OperationBoardDTO operationBoardDTO,
            @PageableDefault(size=7, sort="Id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.getSelectOneById(operationBoardDTO);
            model.addAttribute("operationBoard", operationBoard);
            Page<OperationBoardComment> commentList = operationBoardCommentService.getCommentListPage(operationBoardDTO.getId(), pageable);
            model.addAttribute("commentList", commentList);
            return dirName + "/view";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0101");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/create")
    public String create() {
        return dirName + "/create";
    }

    @GetMapping("/update/{id}")
    public String update(
            Model model,
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.getSelectOneById(operationBoardDTO);
            model.addAttribute("operationBoard", operationBoard);
            return dirName + "/update";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0101");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(
            Model model,
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.getSelectOneById(operationBoardDTO);
            model.addAttribute("operationBoard", operationBoard);
            return dirName + "/delete";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0101");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        }
    }

    @PostMapping("/createProc")
    public String createProc(
            Model model,
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            operationBoardService.setInsert(operationBoardDTO);
            return "redirect:/" + dirName + "/list";
        } catch (Exception e) {
            model.addAttribute("errCode", "err0202");
            model.addAttribute("errMsg", "등록 중 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @PostMapping("/updateProc")
    public String updateProc(
            Model model,
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            OperationBoard operationBoard = operationBoardService.setUpdate(operationBoardDTO);
            return "redirect:/" + dirName + "/view/" + operationBoardDTO.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0101");
            model.addAttribute("errMsg", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err0303");
            model.addAttribute("errMsg", "처리하는 과정에서 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @PostMapping("/deleteProc")
    public String deleteProc(
            Model model,
            OperationBoardDTO operationBoardDTO
    ) {
        try {
            operationBoardService.setDelete(operationBoardDTO);
            return "redirect:/" + dirName + "/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errCode", "err0101");
            model.addAttribute("errCode", e.getMessage());
            return "error/error";
        } catch (Exception e) {
            model.addAttribute("errCode", "err0303");
            model.addAttribute("errMsg", "처리하는 과정에서 오류가 발생했습니다.");
            return "error/error";
        }
    }

    @ResponseBody
    @PostMapping(value = "/uploadImage", produces = "application/json")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        String uploadDir = "C:/dw202/attach/summernote/";  // 저장할 폴더  // 폴더 위치 변경
        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();


        //파일명에서 한글 제거:
        String original = file.getOriginalFilename();
        original = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID() + "_" + original;

        //String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        file.transferTo(new File(filePath)); // 파일 저장

        Map<String, Object> response = new HashMap<>();
        response.put("url", "/dw202/attach/summernote/" + fileName); // 클라이언트에 반환할 URL // 파일 위치 벼경
        return response;
    }
    //********************************************************************************************


}
