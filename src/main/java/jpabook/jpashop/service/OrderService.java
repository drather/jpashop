package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepositoryOld;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepositoryOld memberRepositoryOld;
    private final ItemRepository itemRepository;

    /**
     * Id 를 바탕으로 각 member, item 을 찾아온다.
     * 배송 정보를 생성한다.
     * 주문상품 정보를 생성한다.
     * 주문을 생성한다.
     * 주문을 저장한다.
     * @param memberId
     * @param itemId
     * @param count
     * @return
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepositoryOld.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        // 배송 정보 생성

        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        // 원래는 Delivery repository 가 있어서, save 및 할당해줘여 함.
        // 그러나, cascade 옵션이 있어서, order에 퍼시스트 하면 orderitem 에도 퍼시스트를 날려주고, delivery 엔티티도 persist 됨.
        // Order -> OrderItem, Order -> Delivery 같이, 확실하게 연관관계가 정해지는 경우에만 CASCADE 옵션을 사용할 것
        // 즉, delivery 는 order 에서만 참조한다. orderItem 또한 order 에서만 참조한다.

        // 주문 상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 주문 저장
        orderRepository.save(order);
        return order.getId();
    }

    /**
     * 해당 주문을 DB 에서 찾아오는 메서드
     * 실제 cancel 에 해당하는 메서드는 Order 클래스에 정의되어 있음.
     * @param orderId
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findOne(orderId);

        // 주문 취소
        order.cancel();
    }

    // 검색

    /**
     * 인자로 넘겨받은 orderSearch 에 있는 필드 중, 조건을 만족하는 order 를 모두 불러오는 코드
     * @param orderSearch
     * @return
     */
     public List<Order> findOrders(OrderSearch orderSearch){
         return orderRepository.findAllByString(orderSearch);
     }
}

