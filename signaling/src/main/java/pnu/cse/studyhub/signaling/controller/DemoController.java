package pnu.cse.studyhub.signaling.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class DemoController {
    @GetMapping
    public String home() {
        return "home";
    }
}
