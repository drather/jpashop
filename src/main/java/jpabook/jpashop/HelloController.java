package jpabook.jpashop;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController  {
    // 'hello' 라는 요청을 받음
    @GetMapping("hello")
    public String hello(Model model) {
        // model 에 attribute(내용) 을 채워서, hello 라는 화면으로 보냄
        model.addAttribute("data", "hello!!");

        // 화면 이름을 return
        return "hello";
    }

}
