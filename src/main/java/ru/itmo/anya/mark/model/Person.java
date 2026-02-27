package ru.itmo.anya.mark.model;

import java.util.Date;

public class Person {
    private static int nextId = 1;
    private int id;
    private String name;
    private Coordinates coordinates;
    private java.util.Date creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private float height; //Значение поля должно быть больше 0
    private java.time.LocalDateTime birthday;
    private Color hairColor;
    private Country nationality;
    private Location location;


    public Person(String name, Coordinates coordinates, float height) {
        this.id = nextId++;
        this.creationDate = new java.util.Date();

        setName(name);
        setCoordinates(coordinates);
        setHeight(height);
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть больше 0");
        }
        this.id = id;
    }

    public void setName(String name) {
        // Добавляем проверку на пустую строку
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть null или пустым");
        }
        this.name = name;
    }

    public void setHeight(float height) {

        if (height <= 0) {
            throw new IllegalArgumentException("Рост должен быть больше 0");
        }
        this.height = height;
    }


    public int getId() {
        return id;
    }


    public String getName() {
        return name;
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


    public java.time.LocalDateTime getBirthday() {
        return birthday;
    }

    public void setBirthday(java.time.LocalDateTime birthday) {
        this.birthday = birthday; // Может быть null
    }

    public Color getHairColor() {
        return hairColor;
    }

    public void setHairColor(Color hairColor) {
        this.hairColor = hairColor; // Может быть null
    }

    public Country getNationality() {
        return nationality;
    }

    public void setNationality(Country nationality) {
        this.nationality = nationality; // Может быть null
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location; // Может быть null
    }
}