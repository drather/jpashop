package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryOld {

    private final EntityManager em;

    /**
     * DB 에 Member member 저장하는 메서드
     * em.persist 사용에 주목?
     * @param member
     */
    public void save(Member member) {
        em.persist(member);
    }

    /**
     * id 를 통해 member 객체 찾는 메서드
     * @param id
     * @return
     */
    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    /**
     * DB 에 JPQL 을 날려서, Member 객체를 모두 읽어오는 메서드
     * @return members
     */
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    /**
     * DB 에 JPQL 을 날려서, 해당 이름을 가진 Member 객체를 모두 읽어오는 메서드
     * @return members
     */
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}