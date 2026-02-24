package ru.itmo.anya.mark.model;

import java.util.Date;

public class Person {
    private static int nextId = 1;
    private int id;
    private String name;
    private Coordinates coordinates;
    private java.util.Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private float height; //Значение поля должно быть больше 0

    public Person(int id, String name, Coordinates coordinates, Date creationDate, float height) {
        this.id = nextId++;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.height = height;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id > 0) {
            throw new IllegalArgumentException("Неправильный формат id");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name не может быть null");
        }
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {

        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates не может быть null");
        }
        this.coordinates = coordinates;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        if (creationDate == null) {
            throw new IllegalArgumentException("Date of creation не может быть null");
        }
        this.creationDate = creationDate;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        if (height >0) {
            throw new IllegalArgumentException("Значение должно быть больше 0");
        }
        this.height = height;
    }
}