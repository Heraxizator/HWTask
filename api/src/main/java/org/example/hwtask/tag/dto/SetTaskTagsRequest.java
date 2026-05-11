package org.example.hwtask.tag.dto;

import java.util.List;
import java.util.UUID;

public record SetTaskTagsRequest(List<UUID> tagIds) {
    public SetTaskTagsRequest {
        if (tagIds == null) {
            tagIds = List.of();
        }
    }
}
