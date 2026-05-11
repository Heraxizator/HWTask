package org.example.hwtask.checklist.dto;

import java.util.UUID;

public record ChecklistItemResponse(UUID id, String title, boolean done, int sortOrder) {
}
