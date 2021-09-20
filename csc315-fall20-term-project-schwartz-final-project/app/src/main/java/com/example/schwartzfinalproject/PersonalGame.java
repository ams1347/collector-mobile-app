package com.example.schwartzfinalproject;

import java.io.Serializable;
import java.util.Objects;

public class PersonalGame implements Serializable {

    private String title;
    private String imageFile;
    private String console;
    private String description;
    private String releaseDate;

    private String condition = "Brand New";
    private String completion = "CIB";
    private String notes = "";

    // No-argument constructor is required to support conversion of Firestore document to POJO
    public PersonalGame() {  }

    public PersonalGame(String title, String imageFile, String console, String description, String releaseDate) {
        this.title = title;
        this.imageFile = imageFile;
        this.console = console;
        this.description = description;
        this.releaseDate = releaseDate;
    }

    public PersonalGame(String title, String imageFile, String console, String description, String releaseDate, String condition, String completion, String notes) {
        this.title = title;
        this.imageFile = imageFile;
        this.console = console;
        this.description = description;
        this.releaseDate = releaseDate;
        this.condition = condition;
        this.completion = completion;
        this.notes = notes;
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

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCompletion() {
        return completion;
    }

    public void setCompletion(String completion) {
        this.completion = completion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "PersonalGame{" +
                "title='" + title + '\'' +
                ", imageFile='" + imageFile + '\'' +
                ", console='" + console + '\'' +
                ", description='" + description + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", condition='" + condition + '\'' +
                ", completion='" + completion + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalGame that = (PersonalGame) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(imageFile, that.imageFile) &&
                Objects.equals(console, that.console) &&
                Objects.equals(description, that.description) &&
                Objects.equals(releaseDate, that.releaseDate) &&
                Objects.equals(condition, that.condition) &&
                Objects.equals(completion, that.completion) &&
                Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, imageFile, console, description, releaseDate, condition, completion, notes);
    }
}
