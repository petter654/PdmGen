package br.com.aegea.geradorpdm.controller; // Ou o nome exato do seu pacote de controller

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

}