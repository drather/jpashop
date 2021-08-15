package jpabook.jpashop.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomeController {

    // slf4j logger 선택, @Slf4j 어노테이션으로 아래 코드 대체
    // Logger log = LoggerFactory.getLogger()
    @RequestMapping("/")
    public String home() {
        log.info("home controller");
        return "home";
    }
}
