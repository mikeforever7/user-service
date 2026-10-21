package ru.homework.event;

public record UserEvent(UserOperation operation, String email) {
}