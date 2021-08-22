package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.MemberService;
import jpabook.jpashop.service.OrderService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final MemberService memberService;
    private final ItemService itemService;

    /**
     * 주문 버튼을 클릭했을 때, /order 요청을 받아 처리하는 메서드.
     * 회원, 상품과는 다르게, 다른 기능(회원, 상품)의 serivce 를 호출하여 해당 member, items 를 들고있어야 한다.
     * 이후, model 에 회원, 상품 정보를 addAttribute 하고 화면 이동
     * order/orderForm.html 화면으로 이동
     * @param model
     * @return order/orderForm.html
     */
    @GetMapping("/order")
    public String createForm(Model model) {
        // members, items 는 각각 모든 데이터를 갖고 있는다.
        List<Member> members = memberService.findMembers();
        List<Item> items = itemService.findItems();

        // models, items 를 바탕으로 화면에 드롭박스에 값을 채운다.
        model.addAttribute("members", members);
        model.addAttribute("items", items);

        // 값을 채운 화면을 리턴한다.
        return "order/orderForm";
    }

    /**
     * 사용자가 작성한 주문 정보를 가지고, order 를 추가하는 메서드
     * @param memberId
     * @param itemId
     * @param count
     * @return
     */
    @PostMapping("/order")
    // 핵심 비즈니스 로직에 대한 식별자만 넘겨주고, 로직 처리.
    public String order(@RequestParam("memberId") Long memberId,
                        @RequestParam("itemId") Long itemId,
                        @RequestParam("count") int count){
        orderService.order(memberId, itemId, count);
        return "redirect:/orders";

    }

    /**
     * /orders 요청을 받아, 모든 주문 내역을 가져와 보여주는 메서드
     * @param orderSearch
     * @param model
     * @return
     */
    @GetMapping("/orders")
    public String orderList(@ModelAttribute("orderSearch") OrderSearch orderSearch, Model model) {
        List<Order> orders = orderService.findOrders(orderSearch);

        // 찾아온 내역을 model 에 addAttribute
        model.addAttribute("orders", orders);

        // 화면을 return
        return "order/orderList";
    }

    /**
     * orderList.html 화면에서, 주문 취소 요청을 받아 처리하는 메서드
     * @param orderId
     * @return
     */
    @PostMapping("orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.cancelOrder(orderId);
        return "redirect:/orders";
    }
}
