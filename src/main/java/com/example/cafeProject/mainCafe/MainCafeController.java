package com.example.cafeProject.mainCafe;

import com.example.cafeProject.noticeBoard.NoticeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainCafeController {

    @GetMapping({"/", ""})
    public String list(
            Model model
    ) {
        return "index/mainIndex";
    }
}
