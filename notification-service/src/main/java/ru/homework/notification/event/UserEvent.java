package ru.homework.notification.event;

public record UserEvent(UserOperation operation, String email) {
}