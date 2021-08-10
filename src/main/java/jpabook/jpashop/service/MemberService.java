package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(readOnly = true)
//@AllArgsConstructor // 필드를 가지고, 생성자를 만들어주는 lombok annotation

@RequiredArgsConstructor // final 에 있는 필드를 가지고, 생성자를 만들어주는 lombok annotation
public class MemberService {

    // 1. field injection
    // @Autowired // 의존성 주입, 필드 injection
    // private MemberRepository memberRepository;

    // 2. second injection
    // private MemberRepository memberRepository;
    // @Autowired //
    // public void setMemberRepository(MemberRepository memberRepository) {
    //    this.memberRepository = memberRepository;
    //}

    // 3. constructor injection
    // private final MemberRepository memberRepository; // 변경될 일 없기에, final 로 지정
    // @Autowired
    // public MemberService(MemberRepository memberRepository) {
    //     this.memberRepository = memberRepository;
    // }

    private final MemberRepository memberRepository; // 변경될 일 없기에, final 로 지정
    // == 비즈니스 로직 == //
    /**
     * 회원 가입
     * @param: Member
     * @return x
     */
    public Long join(Member member) {
        validateDuplicateMember(member);
        memberRepository.save(member);
        return member.getId();
    }

    public void validateDuplicateMember(Member member) {
        /**
         * 중복 검사
         * @params: Member
         */
        // EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    @Transactional(readOnly = true) // jpa의 모든 데이터 변경 및 로직은 transaction 안에서 실행 & 최적화 이점
    // readOnly 는 읽기 전용.
    public List<Member> findMembers() {
        /**
         * 전체 조회
         */
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true) // jpa의 모든 데이터 변경 및 로직은 transaction 안에서 실행 & 최적화 이점
    // readOnly 는 읽기 전용.
    public Member findOne(Long memberId) {
        /**
         * 단건 조회
        */
        return memberRepository.findOne(memberId);
    }
}