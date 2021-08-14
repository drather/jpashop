package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Album;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.item.Movie;
import jpabook.jpashop.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ItemServiceTest {

    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;
    @Autowired EntityManager em;

    @Test
    public void saveItem() {
        //given
        Item item = new Movie();
        item.setName("맛섹사");

        //when
        Long id = itemService.saveItem(item);

        //then
        assertEquals(item, itemRepository.findOne(id));

    }

    @Test
    public void findItems() {
        //given
        List<Item> items = itemService.findItems();

        Item item1 = new Movie();
        item1.setName("맛섹사");
        Long id1 = itemService.saveItem(item1);
        items.add(item1);

        Item item2 = new Album();
        item2.setName("소세지타령");
        Long id2 = itemService.saveItem(item2);
        items.add(item2);

        //when
        List<Item> new_items = itemService.findItems();

        //then
        assertEquals(items, new_items);
    }

    @Test
    public void findOne() {
        //given
        Item item = new Movie();
        item.setName("맛섹사");
        Long id1 = itemService.saveItem(item);

        //when
        Long id2 = itemService.findOne(id1).getId();

        //then
        assertEquals(id1, id2);
    }
}