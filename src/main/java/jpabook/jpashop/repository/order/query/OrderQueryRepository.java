package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 패키지를 나눈 이유
 *  OrderRepository: 핵심 엔티티에 대한 로직을 담음
 *  OrderQueryRepository: 화면 및 API 와 의존적인 로직을 담음
 * findOrderQueryDtos 메서드를 별도로 만든 이유
 *  orderQueryDto 를 참조하게 되면, repository 가 controller 를 순환참조 하게 됨
 *  OrderQueryDto 를 따라서 같은 패키지에 넣게 되었음1
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    /**
     * OrderQueryDto 의 배열을 리턴하는 메서드
     * findOrders 메서드를 통해 Order 객체와, ToOne 관계인 엔티티를 먼저 조회
     * 이후, findOrderItems() 메서드를 통해 연관된 OrderItems 객체들을 조회
     * @return
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();

        // result 는 연관 관계가 있는 orderItems 를 아직 불러오지 않은 order 들의 컬렉션
        // result 의 원소인 order o 를 순회하면서, 각 o 에 해당하는 orderItems 를 findOrderItems 를 통해 조회
        // 그 결과인 orderItems 를 o 의 orderItems 변수에 할당
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }

    /**
     * Order 엔티티와 연관된 OrderITems 엔티티를 조회
     * OrderItems 를 조회해오는 시점에, OrderItemQueryDto 형태로 SELECT 해서 가져옴
     * 1+N 문제 발생
     * @param orderId
     * @return
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /**
     * Order 엔티티와, ToOne 관계로 연관된 엔티티들을 불러오는 메서드
     * DB 조회 시점에 OrderQueryDto 형태로 값을 가져옴
     * @return
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderQueryDto.class
                ).getResultList();
    }

    /**
     * findOrders 메서드를 통해, order 엔티티와 ToOne 관계인 엔티티들을 먼저 조회함
     * 이후, result 의 orderId 를 가져옴 (쿼리 X)
     * 해당 orderId 를 JPQL 의 IN 키워드에 바인딩 해줌으로써, 컬렉션 조회를 단 한번의 쿼리만 나가게 함
     *
     * @return
     */
    public List<OrderQueryDto> findAllByDto_optimization() {
        List<OrderQueryDto> result = findOrders(); // root 쿼리 1번

        // 주문 ID 를 조회해 옴
        List<Long> orderIds = toOrderIds(result);

        // 다음 문장에서, 쿼리 1. IN 키워드 활용
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        // Map 을 통해,
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    /**
     * in 키워드와 Map 자료구조를 이용해 최적화
     * order Id 를 통해 orderItem 엔티티를 order 에 맵핑
     * @param orderIds
     * @return
     */
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count) " +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    /**
     * order 엔티티의 배열을 순회하면서 orderId 를 모아서 return
     * @param result
     * @return
     */
    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    /**
     * @return
     */
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i ", OrderFlatDto.class)
                .getResultList();
    }
}
