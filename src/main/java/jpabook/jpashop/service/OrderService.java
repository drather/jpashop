package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        // 엔티티 조회
        Member member = memberRepository.findOne(memberId);
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

    @Transactional
    public void cancelOrder(Long orderId) {
        // 주문 조회
        Order order = orderRepository.findOne(orderId);

        // 주문 취소
        order.cancel();
    }

    // 검색
    // public List<Order> findOrders(OrderSearch orderSearch){
    //     return orderRepository.findAll(orderSearch);
    // }
}
