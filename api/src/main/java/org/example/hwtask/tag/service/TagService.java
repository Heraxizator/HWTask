package org.example.hwtask.tag.service;

import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.tag.persistence.Tag;
import org.example.hwtask.tag.persistence.TagRepository;
import org.example.hwtask.tag.persistence.TaskTag;
import org.example.hwtask.tag.persistence.TaskTagRepository;
import org.example.hwtask.tag.dto.TagResponse;
import org.example.hwtask.task.dto.response.TaskTagResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TaskTagRepository taskTagRepository;
    private final AccessControlService accessControlService;

    public TagService(
            TagRepository tagRepository,
            TaskTagRepository taskTagRepository,
            AccessControlService accessControlService
    ) {
        this.tagRepository = tagRepository;
        this.taskTagRepository = taskTagRepository;
        this.accessControlService = accessControlService;
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listProjectTags(UUID projectId, UUID actorUserId) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        return tagRepository.findByProjectIdOrderByNameAsc(projectId).stream()
                .map(t -> new TagResponse(t.getId(), t.getName()))
                .toList();
    }

    @Transactional
    public TagResponse createTag(UUID projectId, String name, UUID actorUserId) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Пустое имя тега");
        }
        if (tagRepository.existsByProjectIdAndNameIgnoreCase(projectId, trimmed)) {
            throw new IllegalArgumentException("Тег уже существует");
        }
        Tag saved = tagRepository.save(new Tag(projectId, trimmed));
        return new TagResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public void setTaskTags(UUID taskId, UUID projectId, List<UUID> tagIds, UUID actorUserId) {
        accessControlService.assertProjectMember(projectId, actorUserId);
        if (tagIds == null || tagIds.isEmpty()) {
            taskTagRepository.deleteByIdTaskId(taskId);
            return;
        }
        List<Tag> tags = tagRepository.findAllById(tagIds);
        for (Tag t : tags) {
            if (!t.getProjectId().equals(projectId)) {
                throw new IllegalArgumentException("Тег не из этого проекта");
            }
        }
        if (tags.size() != tagIds.size()) {
            throw new IllegalArgumentException("Неизвестный тег");
        }
        taskTagRepository.deleteByIdTaskId(taskId);
        for (UUID tid : tagIds) {
            taskTagRepository.save(new TaskTag(taskId, tid));
        }
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<TaskTagResponse>> tagsGroupedByTaskId(List<UUID> taskIds) {
        if (taskIds.isEmpty()) {
            return Map.of();
        }
        List<TaskTag> links = taskTagRepository.findByIdTaskIdIn(taskIds);
        if (links.isEmpty()) {
            return Map.of();
        }
        Set<UUID> tagIds = links.stream().map(l -> l.getId().getTagId()).collect(Collectors.toSet());
        Map<UUID, Tag> tagById = tagRepository.findAllById(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, t -> t));
        Map<UUID, List<TaskTagResponse>> out = new HashMap<>();
        for (TaskTag link : links) {
            Tag tg = tagById.get(link.getId().getTagId());
            if (tg != null) {
                out.computeIfAbsent(link.getId().getTaskId(), k -> new ArrayList<>())
                        .add(new TaskTagResponse(tg.getId(), tg.getName()));
            }
        }
        return out;
    }
}
