package jpabook.jpashop.api;

import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jpabook.jpashop.domain.Order;
import java.util.List;

/**
 * xToOne 관계에서, 성능 최적화를 어떻게 할것인가?
 * Order 를 조회
 * Order -> Member (Many To One)
 * Order -> Delivery (One To One)
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;

    /**
     * 첫 번째 문제
     * Order -> Member -> Order -> Member -> ....
     * 무한 루프에 빠지게 됨, 양방향 무한루프의 문제
     * Member -> Order, Order -> Member 둘 중 하나는 @JsonIgnore 해줘야 함
     * 해결: 즉, 양방향 연관관계인 엔티티 둘 중 하나에는 무조건 @JsonIgnore 해줘야 함
     *
     * 두 번째 문제
     * 지연 로딩(DB에서 실제 가져오지 않음) 이기 떄문에, Order 와 연관된 Member 를 가져오는 것이 아니라 ProxyMember 를 생성해서 넣어둠.
     * Proxy 객체를 가짜로 넣어놓고, Member 객체에 손을 대게 되면 그 객체에 proxy 의 값을 채워준다.
     * 그런데 이 과정에서, Jackson 라이브러리가 기능하지 못함.
     * 해결: Hibernate 5 모듈을 설치해야 함. -> 별로 중요한 내용 아님
     *
     * 그러나, Lazy loading 이기 때문에, 별 쓸모없는 것까지 전부 다 불러오게 됨.
     * 이러다보면, 엔티티가 웹에 노출되기 때문에, 엔티티의 내용이 변경되었을 떄 API 스펙이 전부 변하게 됨
     *
     * @return
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getAddress();
        }
        return all;
    }
}
