package jpabook.jpashop.controller;


import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    /**
     * items/new 요청을 받아서, 상품 등록 화면(items/createItemForm.html) 로 보내주는 메서드
     * @param model
     * @return
     */
    @GetMapping("items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createItemForm";
    }

    /**
     * POST 방식(데이터: 사용자 입력 데이터) 를 받아서, DB 에 INSERT 하도록 하는 메서드
     * @param form
     * @return
     */
    @PostMapping ("/items/new")
    public String create(BookForm form) {

        // Book 객체 생성 및 초기화
        Book book = new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());


        itemService.saveItem(book);
        return "redirect:/";
    }

    /**
     * itemService.findItems() -> itermRepository.findaAll() 의 결과물을 받아서 items 에 저장하는 메서드
     * @param model
     * @return
     */
    @GetMapping("/items")
    public String list (Model model) {
        List<Item> items = itemService.findItems();

        // model 에 addAttribute 하고 return
        model.addAttribute("items", items);
        return "items/itemList";
    }

    /**
     * itemId 와 모델을 인자로 받아서, Item 객체를 생성 및 초기화하고, model 에 addAttribute 하여 화면에 넘겨주는 메소드
     * @param itemId
     * @param model
     * @return
     */
    @GetMapping("items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {
        // DB 로부터 해당 id 를 갖는 item 을 가져옴
        Book item = (Book) itemService.findOne(itemId);

        // book 엔티티를 보내는 게 아니라, form 을 보냄
        BookForm form = new BookForm();
        form.setId(item.getId()); // jpa 에 한번 들어갔다 나온 애로, 식별자가 정해진 아이 -> 준영속 상태의 객체, 영속성 컨텍스트가 관리 X
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getIsbn());
        form.setIsbn(item.getIsbn());

        // 만든 form 을 model 에 addAttribute 하고, item 수정 화면(itmes/updateItemForm) 으로 이동
        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    /**
     * itemId 와 사용자가 수정 입력한 form 객체를 바탕으로, 해당 상품을 수정하는 메서드
     * @param itemId
     * @param form
     * @return
     */
    @PostMapping("items/{itemId}/edit")
    public String updateItem(@PathVariable Long itemId, @ModelAttribute("form") BookForm form) {
//        Book book = new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());
//        itemService.saveItem(book);

        // 상품 이름, 가격, 수량을 가지고 update
        itemService.updateItem(itemId, form.getName(), form.getPrice(), form.getStockQuantity());
        return "redirect:/items";
    }
}
