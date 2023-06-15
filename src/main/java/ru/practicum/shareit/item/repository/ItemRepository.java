package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderById(long ownerId, PageRequest page);

    @Query("SELECT i from Item as i " +
            "WHERE i.available = TRUE " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT( '%',?1,'%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%',?1,'%'))) ")
    List<Item> search(String text, PageRequest pageRequest);

    List<Item> findAllByRequestInOrderByRequestId(List<ItemRequest> itemRequests);
}
