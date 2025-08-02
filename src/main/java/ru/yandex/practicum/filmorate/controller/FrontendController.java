package ru.yandex.practicum.filmorate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping(value = {
            "/",
            "/films",
            "/users",
            "/reviews",
            "/genres",
            "/mpa",
            "/films/{id}",
            "/users/{id}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
