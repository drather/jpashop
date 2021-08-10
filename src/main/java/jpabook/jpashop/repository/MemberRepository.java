package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class MemberRepository {
    @PersistenceContext // entityManager 를 injecction 시켜주는 표준 annotation
    private EntityManager em;

    public void save(Member member){
        // 이 순간, 영속성 컨텍스트에 이 객체를 올린다.
        // 영속성 컨텍스트는 key: value 구조인데, id 값이 키가 된다.
        // db pk 맵핑?
        // sequence 등에서 pk 를 얻는데, id 값이 항상 생성되는 것이 보장된다.
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        // JPQL
        // 테이블에 대해 쿼리하는 것이 아니라, 엔티티에 대해 쿼리하는 것?
        // from 절의 대상이 테이블이 아니라, Entity 이다.
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
