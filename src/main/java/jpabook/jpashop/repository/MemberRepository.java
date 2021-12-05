package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 다음 쿼리가 나가게 됨
    // SELECT m FROM Member m where m.name = ?
    List<Member> findByName(String name);
}
