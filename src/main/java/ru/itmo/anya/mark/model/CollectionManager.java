package ru.itmo.anya.mark.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionManager {
    private Map<Integer, Person> storage = new HashMap<>();
    private int nextId = 1;
    public void add(Person person){
        if (person == null) {
            throw new IllegalArgumentException("Ошибка: Нельзя добавить пустой объект");
        }

        int id = generateNextId();
        person.setId(id);
        storage.put(id, person);
        System.out.println("Объект успешно добавлен с ID: " + id);
    }
    public Person getById(int id) {
        return storage.get(id);
    }
    public Collection<Person> getAll() {
        return storage.values();
    }
    public void update(int id, Person newData) {
        if (!storage.containsKey(id)) {
            System.out.println("Ошибка: Объект с ID " + id + " не найден");
            return;
        }
        Person personToUpdate = storage.get(id);
        try {

            personToUpdate.setName(newData.getName());
            personToUpdate.setCoordinates(newData.getCoordinates());
            personToUpdate.setHeight(newData.getHeight());

            System.out.println("Объект с ID " + id + " успешно обновлен");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации при обновлении: " + e.getMessage());
        }
    }


    public void remove(int id) {
        if (storage.remove(id) != null) {
            System.out.println("Объект с ID " + id + " удален");
        } else {
            System.out.println("Ошибка: ID " + id + " не существует");
        }
    }


    private int generateNextId() {
        return nextId++;
    }

}
