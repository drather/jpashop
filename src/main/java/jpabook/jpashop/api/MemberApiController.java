package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;

@RestController // @Controller + @ResponseBody
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    /**
     *  회원 등록 API
     *  v1
     *  JSON 으로 넘어온 request 의 body 를 Member 에 쭉 넣어줌
     *  엔티티가 직접 오고가기 때문에, API 와 엔티티가 강하게 결합된다.
     *  따라서 엔티티가 변할때마다 API 스펙이 변하고, 유지보수를 힘들게 한다.
     */
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /**
    *  v2
    *  v1 에서, 엔티티와
    *  member 엔티티가 변함과 상관없이, API 스펙은 변하지 않는다.
    *  또한, request 만 까보게 되면 request 에서 넘어오는 데이터를 한번에 알 수 있다.
     * @param request
     * @return
     */
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 회원 수정 API

    /**
     * 회원 수정 API
     * 엔티티가 노출되어 있던 것을 DTO 로 전환
     * 그로써, 필요한 정보만 처리하게 되고, API 와 엔티티 간의 결합을 약하게 함.
     * @param id
     * @param request
     * @return
     */
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(
            @PathVariable("id") Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);
        return new UpdateMemberResponse(findMember.getId(), findMember.getName());
    }



    /**
     *  회원 조회 API v1
     *  문제점 1: 화면에 뿌리기 위한 로직이 엔티티에 추가(@JsonIgnore)
     *  문제점 2: 다양한 API 에 대응 불가
     *  문제점 3: 원하는 정보만 뿌려줄 수 없음
     *  문제점 4: array 를 넘겨버림으로 인해, JSON 스펙 확장 불가
     * @return
     */
    @GetMapping("/api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    /**
     * 회원 조회 API V2
     * 화면을 위한 로직 제거됨
     * 다양한 API 스펙에 유연하게 대처 가능
     * 필요하지 않은 정보(예를 들어 주문내역) 등을 노출시키지 않음
     * JSON 스펙 확장 가능
     * @return
     */
    @GetMapping("/api/v2/members")
    public Result memberV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect.size(), collect);
    }

    /**
     * 회원 조회 API response DTO
     * @param <T>
     */
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private int count;
        private T data;
    }

    /**
     * 회원 조회 API response DTO 에 들어갈 DTO
     */
    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }

    /**
     * 회원 수정 API 의 request 를 변환할 DTO
     */
    @Data
    static class UpdateMemberRequest {
        private String name;

    }

    /**
     * 회원 수정 API 의 response 를 변환할 DTO
     */
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    /**
     * 회원 등록 API 의 request 를 변환할 DTO
     */
    @Data
    static class CreateMemberRequest {
        @NotEmpty
        private String name;
    }

    /**
     * 회원 등록 API 의 response 를 변환할 DTO
     */
    @Data
    static class CreateMemberResponse {
        private Long id;
        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }
}
