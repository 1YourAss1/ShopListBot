package ru.yourass.shoplist;

import java.util.ArrayList;
import java.util.List;

public class ItemRepository {
    private final List<ItemDto> items = new ArrayList<>();

    public List<ItemDto> getAll() {
        return new ArrayList<>(items);
    }
    public void save(ItemDto it) {
        items.add(it);
    }
}