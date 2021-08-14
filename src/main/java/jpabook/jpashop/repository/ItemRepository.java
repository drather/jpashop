package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    public final EntityManager em;

    /**
     * 상품 저장
     * @param item
     */
    public void save(Item item) {
        // item 은 jpa 에 저장하기 전까지 id가 없음
        if (item.getId() == null) {
            em.persist(item);
        }
        // 이미 DB 에 있는 객체를 '업데이트'한다는 느낌. 추후 설
        else {
            em.merge(item);
        }
    }

    /**
     * 상품 단건 조회
     * @param id
     * @return
     */
    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    /**
     * 상품 모두 조회
     * @return
     */
    public List<Item> findAll() {
        return em.createQuery("select i from Item i", Item.class)
                .getResultList();
    }
}
