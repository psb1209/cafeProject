package com.example.cafeProject.mainCafe;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainCafeController {

    @GetMapping({"/", ""})
    public String mainIndex(
            Model model
    ) {
        model.addAttribute("activeMenu", "main");
        return "index/mainIndex";
    }

    @GetMapping("/testIndex")
    public String testindex(
            Model model
    ) {
        model.addAttribute("activeMenu", "main");
        return "index/testIndex";
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

    @GetMapping("/test2")
    public String test2(
            Model model
    ) {
        model.addAttribute("activeMenu", "main");
        return "index/test2";
    }

    @GetMapping("/explore_cafes/my")
    public String myCafe(
            Model model
    ) {
        model.addAttribute("activeMenu", "my");
        return "explore_cafes/my";
    }

    @GetMapping("/explore_cafes/favorite")
    public String favoriteCafe(
            Model model
    ) {
        model.addAttribute("activeMenu", "favorite");
        return "explore_cafes/favorite";
    }



}
