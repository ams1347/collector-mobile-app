package com.example.schwartzfinalproject;

import java.io.Serializable;

public class Game implements Serializable {

    private String title;
    private String imageFile;
    private String console;
    private String description;
    private String releaseDate;

    // No-argument constructor is required to support conversion of Firestore document to POJO
    public Game() {  }

    public Game(String title, String imageFile, String console, String description, String releaseDate) {
        this.title = title;
        this.imageFile = imageFile;
        this.console = console;
        this.description = description;
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getConsole() {
        return console;
    }

    public void setConsole(String console) {
        this.console = console;
    }

    @Override
    public String toString() {
        return "Game{" +
                "title='" + title + '\'' +
                ", imageFile='" + imageFile + '\'' +
                ", console='" + console + '\'' +
                '}';
    }
}
