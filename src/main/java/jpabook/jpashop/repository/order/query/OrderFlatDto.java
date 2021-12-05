package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Order 와 OrderItem join 해서 가져온 결과를 받을 DTO
 * Order 와 OrderItem 을 한 row 로 가져올 것이다.
 *
 * 처음으로 실행하면, order 데이터가 중복된다.
 * 그러나, 단 한방의 쿼리로 가져올 수 있다.
 * 그러나, 일대다에서 일 에 해당하는 엔티티를 기준으로 paging 할 수 는 없다.
 */
@Data
public class OrderFlatDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    private String itemName;
    private int orderPrice;
    private int count;

    public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }


}
