package jpabook.jpashop.api;

import javassist.Loader;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jpabook.jpashop.domain.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * v1 버젼에서, 엔티티를 직접 노출시키지 않고, DTO 로 변환해서 통신하게끔 수정.
     * 이를 통해, 엔티티를 직접 노출시킴으로써 발생했던 문제점(엔티티-API 간 결합 증가) 해결.
     * 그러나, 지연 로딩으로 인해 DB 에 쿼리가 너무 많이 나간다는 문제점은 아직 존재함.
     * Order, Member, Delivery 테이블을 조회해야 하는 상황
     *
     * N+1 문제 발생.
     * 첫번째 쿼리(1, 주문조회) 이후, 첫번째 쿼리의 수인 N(회원, 딜리버리 조회) 번만큼 쿼리가 추가실행 되는 문제를 말함.
     * @return
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {

        // ORDER 2개 조회, 쿼리 1, 누적 쿼리수 1
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        // 루프를 돌면서, Member 를 찾는 쿼리와 Delivery 를 찾는 쿼리 2개가 나감.
        // 아래 루프를 돌고나면, Member 조회 쿼리와 Delivery 조회 쿼리 2개 * Order 수 2 해서 총 4, 누적 쿼리수 5
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address; //

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // 이 시점에서 Lazy 초기화. 영속성 컨텍스트가 id를 가지고 멤버를 찾아봄.
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // 이 시점에서 Lazy 초기화. 영속성 컨텍스트가 id를 가지고 멤버를 찾아봄.
        }
    }

    /**
     * fetch join 을 통해 주문을 조회하는 API
     * 한 번의 쿼리로, Order, Member, Delivery 세 엔티티를 모두 조회해서 가져온다.
     * 다음 쿼리가 나간다.
     * select
     *  order0_.order_id as order_id1_6_0_,
     *  member1_.member_id as member_i1_4_1_,
     *  delivery2_.id as id1_2_2_,
     *  order0_.delivery_id as delivery4_6_0_,
     *  order0_.member_id as member_i5_6_0_,
     *  order0_.order_date as order_da2_6_0_,
     *  order0_.status as status3_6_0_,
     *  member1_.city as city2_4_1_,
     *  member1_.street as street3_4_1_,
     *  member1_.zipcode as zipcode4_4_1_,
     *  member1_.name as name5_4_1_,
     *  delivery2_.city as city2_2_2_,
     *  delivery2_.street as street3_2_2_,
     *  delivery2_.zipcode as zipcode4_2_2_,
     *  delivery2_.status as status5_2_2_
     * from
     *  orders order0_ inner join member member1_
     *  on order0_.member_id=member1_.member_id inner join delivery delivery2_
     *  on order0_.delivery_id=delivery2_.id
     *
     *  그러나 단점이 있다.
     *  엔티티의 값들을 모두 긁어왔다는 점이 문제.
     *  다음 시간에, 이것까지 최적화 해보겠다.
     * @return
     */
    @GetMapping("api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        List<SimpleOrderDto> results = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return results;
    }

    /**
     * 엔티티를 조회한 후, DTO 로 변환을 했었다.
     * 변환하는 과정 없이, 바로 JPA 에서 DTO 로 꺼낼 수 있다면 성능 최적화를 할 수 있다.
     *
     * 일반적인 SQL 을 사용할 때처럼 원하는 값을 선택해서 조회
     * new 명령어를 사용해서 jPQL 의 결과를 즉시 DTO 로 전환
     *
     * 정리
     * V3 과 V4 는 우열을 가리기 힘들다.
     * 왜냐하면, V4 가 성능 최적화는 되었지만, 재사용성이 떨어진다. 해당 DTO 를 사용하지 않으면 사용할 수 없음.
     * DTO 로 조회한 것은 재사용성이 떨어짐.
     * 또한 코드가 지저분해진다.
     * 그러나 V3 는 여러 API 에서 사용할 수 있으며, 깔끔하다.
     *
     * repository 에 API 스펙에 관련된 로직이 들어오는 것은 좋지 않다. aPI 스펙이 바뀌면 그 repository 의 로직을 다 뜯어고쳐야 하기 때문
     * reposirory 는 가급적 entity 를 조회하는 데에만 사용하는 것이 좋다.
     * 그말인 즉슨, 계층간의 분리가 이뤄지지 않는다는 뜻.
     * 또한, 성능 최적화의 효과가 그렇게 크지 않음.
     *
     * 따라서, repository 에 이러한 쿼리를 넣는 것 보다는,
     * 따로 디렉토리를 만들고 (simpleQuery), 그것에 dto 와 해당 유형 repository 를 모아놓는 것이 좋다.
     * @return
     */
    @GetMapping("api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderRepository.findOrderDtos();
    }
}
