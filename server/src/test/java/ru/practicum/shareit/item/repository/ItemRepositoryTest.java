package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class ItemRepositoryTest {
    @Autowired
    ItemRepository itemRepository;
    User user = User.builder()
            .id(null)
            .name("Smith")
            .email("smith@mail.com")
            .build();
    Item item1;
    Item item2;
    Item item3;
    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void beforeEach() {
        entityManager.persist(user);
        item1 = Item.builder()
                .id(null)
                .name("pen1")
                .description("black pen")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        item2 = Item.builder()
                .id(null)
                .name("pen2")
                .description("sharp pen")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        item3 = Item.builder()
                .id(null)
                .name("pen3")
                .description("black pen")
                .available(true)
                .owner(user)
                .request(null)
                .build();
        entityManager.persist(item1);
        entityManager.persist(item2);
        entityManager.persist(item3);
    }

    @Test
    void shouldReturnAllItemsWithPagination() {
        List<Item> items = itemRepository.search("pen", PageRequest.of(0, 3));
        assertThat(items, containsInAnyOrder(item1, item2, item3));
        assertThat(items, hasSize(3));
    }

    @Test
    void shouldReturnTwoItemsWithPagination() {
        List<Item> firstPage = itemRepository.search("black pen", PageRequest.of(0, 1));
        assertThat(firstPage, containsInAnyOrder(item1));
        List<Item> secondPage = itemRepository.search("black pen", PageRequest.of(1, 1));
        assertThat(secondPage, containsInAnyOrder(item3));
    }

    @Test
    void shouldReturnNoItems() {
        List<Item> items = itemRepository.search("brick", PageRequest.of(0, 10));
        assertThat(items, empty());
    }
}