package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    /**
     * 인자로 넘겨받은 order 를 추가한다.
     * @param order
     */
    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * 문자열로 쿼리 조립 및 실행, 권장 XX
     * 너무 어려우므로, 나중에 다시 공부. 동적 쿼리 등을 공부해야 한다.
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language = JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class) .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    /**
     * JPA Criteria 로 해결하는 방법, 권장 X
     * @param orderSearch
     * @return List<Order>
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        // 주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        // 회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    /**
     * order 를 조회하는데, Member 와 Delivery 를 조인하면서, SELECT 절에서 다 가져옴
     * 한번의 쿼리로 Order 와 Member, Delivery 를 조인한 뒤, SELECT 에 다넣고 다 가져옴
     * 이 경우, fetch = LAZY 를 다 무시하고, 모든 값을 다 가져온다.
     * 이러한 경우를 fetch join 이라고 한다.
     *
     * 기술적으로는 SQL 에 join 을 사용한다.
     * join fetch 는 깊이가 있는 기술이며, 실무에서 많이 사용하기 때문에 100% 이해할 것.
     *
     * paging 가능
     * @return
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        " from Order o " +
                        " join o.member m" +
                        " join o.delivery d", OrderSimpleQueryDto.class // <- 이 부분에서, OrderSimpleQueryDto 와 매핑 x
                ).getResultList();
    }

    /**
     * fetch join 을 통한 주문 내역 조회 API
     * Order(2개) 와 OrderItem(4개) 을 join -> 조회되는 order 의 수는 4개
     * 중복돼서 조회된 Order 두 쌍은 id값까지 똑같음
     * 우리는 중복없이 2개의 order 가 필요함.
     * 즉, 하나의 order 가 orderItems 의 수만큼 들어난 데이터가 조회됨
     *
     * 따라서, `distinct` 키워드를 통해서 중복을 제거하는 기능을 수행해야 함
     * 실제 SQL 에서도 distinct 키워드가 SELECT 문에서 동작함
     *
     * SQL 에서의 distinct 는, 모든 row 가 똑같아야 중복을 제거함.
     * JPA 에서의 distinct 는, application 단으로 가져와서, order 의 ID 값이 같으면,
     * 중복된 order 를 제거한 채로 리턴함.
     * 즉, order 객체의 id 에 대해서 중복을 제거한다고 볼 수 있음.
     *
     * v2 에서는 지연 로딩으로 인해 10 번의 쿼리가 나가던 것이, 1번의 쿼리로 해결됨
     *
     * 그러나, 일대다를 fetch join 하는 순간, 페이징이 불가하게 됨.
     * firstResult/maxResult specified with collection fetch; applying in memory
     * 위 WARNING 이 뜨게 됨.
     * 즉, 모든 데이터를 메모리에서 페이징 처리를 하겠다는 의미.
     * 만약 데이터가 10000개가 있다면, 10000개를 memory 에 올려놓고 페이징 처리를 하겠다는 뜻.
     *
     * 일대다 조인을 통해 DB 를 조회하면서, order 를 기준으로 row 를 paging 할 수 없음 (order 에 대한 기준이 틀어져서)
     * 따라서, orderItem 으로 offset 을 잡고, paging 처리를 하게 됨
     * 그래서, 어쩔 수 없이, hibernate 는 경고를 내고 memory 에 올려놓고 paging 처리를 함
     *
     * member, delivery 같이, xToOne 관계의 엔티티는 fetch join 만으로도 충분
     * order - orderItem 은 fetch join 불가능!
     *
     * 컬렉션 페치 조인은 1개만 사용할 수 있다. 즉, 컬렉션 둘 이상에 fetch join 을 사용하면 안된다
     * 1 * N * M 만큼의 데이터가 부정합하게 조회될 수 있으며, 이는 에러를 야기한다.
     * @return
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item", Order.class)
                .getResultList();
    }

    /**
     * offset 과 limit 을 가지고, order 와 xToOne 관계인 엔티티 member, delivery 를,
     * fetch join + paging 으로 가져오는 메서드
     * @param offset
     * @param limit
     * @return
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

}