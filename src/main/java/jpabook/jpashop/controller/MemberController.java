package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    /**
     * /members/new 요청을 받아서, 회원 정보 입력 화면으로 이동시키는 메서드
     * @param model
     * @return members/createMemberForm
     */
    @GetMapping("/members/new")
    public String createForm(Model model) {
        log.info("member controller: 신규 가입 화면 이동");

        // addAttribute: 화면에 보낼 값을 key:value 의 형태로 묶어서 보냄. 여기선 memberForm이라는 key에 MemberForm 객체를 맵핑
        // 한 후 return
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * /members/new 요청을 받아서, 사용자가 입력한 회원 덩보를 저장하는 메소드.
     * @param form: 사용자가 입력한 데이터
     * @param result: validation 결과
     * @return redirect 할 url(홈 화면)
     */
    @PostMapping("/members/new")
    public String create(@Valid MemberForm form, BindingResult result) {

        // form 데이터에 에러가 있다면, redirect 시킨다.
        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        // 주소 객체를 생성하고 초기화한다.
        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        // Member 객체 생성 및 초기화
        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        // memberService 의 join 메소드를 호출한다.
        // memberService.join(member) -> memberRepository.save(member)
        memberService.join(member);
        return "redirect:/";
    }

    /**
     * /members 요청을 받아, DB에 저장된 회원 정보를 모두 불러와 화면을 넘겨주는 메소드
     * @param model: 회원 정보를 담을 model. model.addAttribute("key", value) 를 이용해 화면 렌더링하고 리턴
     * @return /members/memberList
     */
    @GetMapping("/members")
    public String list(Model model) {
        // memberService.findMembers() -> memberRepository.findAll() 을 통해 members에 화원 정보 저장
        List<Member> members = memberService.findMembers();

        // model 에 데이터 추가 후 리턴
        model.addAttribute("members", members);
        return "/members/memberList";
    }

}
