package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    /**
     * Repository 에 해당 item 저장하라고 하는 메소드
     * @param item
     * @return itemId
     */
    @Transactional
    public Long saveItem(Item item) {
        itemRepository.save(item);
        return item.getId();
    }

    /**
     * 인자로 넘겨받은 id를 가지고 item 을 찾고, 사용자의 수정사항을 수정하게끔 Repository 를 호출하는 메서드
     * jpa 가 알아서 변경을 감지함.
     * @param itemId
     * @param name
     * @param price
     * @param stockQuantity
     */
    @Transactional
    public void updateItem(Long itemId, String name, int price, int stockQuantity) {
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(price);
        findItem.setName(name);
        findItem.setStockQuantity(stockQuantity);
        // itemRepository.save(findItem);
        // 위 코드를 실행할 필요가 없음
        // 영속성 컨텍스트가 flush 를 날릴 때, 변경된 사항을 감지해서 이를 처리함.

    }

    /**
     * 모든 item 을 가져오도록 repository 를 호출하는 메서드
     * @return items
     */
    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    /**
     * 해당 itemId 를 갖는 item 을 찾아오도록 repository 를 호출하는 메서드
     * @param itemId
     * @return item
     */
    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
