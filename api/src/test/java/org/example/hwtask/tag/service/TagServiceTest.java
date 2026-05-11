package org.example.hwtask.tag.service;

import org.example.hwtask.identity.service.AccessControlService;
import org.example.hwtask.tag.persistence.Tag;
import org.example.hwtask.tag.persistence.TagRepository;
import org.example.hwtask.tag.persistence.TaskTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    TagRepository tagRepository;

    @Mock
    TaskTagRepository taskTagRepository;

    @Mock
    AccessControlService accessControlService;

    TagService tagService;

    UUID projectId;
    UUID userId;

    @BeforeEach
    void init() {
        tagService = new TagService(tagRepository, taskTagRepository, accessControlService);
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void tagsGroupedByTaskId_emptyTaskIds_returnsEmptyMap() {
        assertThat(tagService.tagsGroupedByTaskId(List.of())).isEqualTo(Map.of());
        verify(tagRepository, never()).findAllById(any());
    }

    @Test
    void createTag_blankName_throws() {
        assertThatThrownBy(() -> tagService.createTag(projectId, "   ", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пустое");
        verify(tagRepository, never()).save(any());
    }

    @Test
    void createTag_duplicate_throws() {
        when(tagRepository.existsByProjectIdAndNameIgnoreCase(projectId, "dup")).thenReturn(true);
        assertThatThrownBy(() -> tagService.createTag(projectId, "dup", userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("существует");
        verify(tagRepository, never()).save(any());
    }

    @Test
    void setTaskTags_emptyList_clearsAssignments() {
        UUID taskId = UUID.randomUUID();
        tagService.setTaskTags(taskId, projectId, List.of(), userId);
        verify(taskTagRepository).deleteByIdTaskId(taskId);
        verify(tagRepository, never()).findAllById(any());
    }

    @Test
    void setTaskTags_nullTagIds_clearsAssignments() {
        UUID taskId = UUID.randomUUID();
        tagService.setTaskTags(taskId, projectId, null, userId);
        verify(taskTagRepository).deleteByIdTaskId(taskId);
        verify(tagRepository, never()).findAllById(any());
    }

    @Test
    void setTaskTags_tagFromOtherProject_throws() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        Tag tag = mock(Tag.class);
        when(tag.getProjectId()).thenReturn(UUID.randomUUID());
        when(tag.getId()).thenReturn(tagId);
        when(tagRepository.findAllById(List.of(tagId))).thenReturn(List.of(tag));

        assertThatThrownBy(() -> tagService.setTaskTags(taskId, projectId, List.of(tagId), userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("проект");
        verify(taskTagRepository, never()).save(any());
    }

    @Test
    void setTaskTags_unknownTag_throwsWhenCountMismatch() {
        UUID taskId = UUID.randomUUID();
        UUID tagId = UUID.randomUUID();
        when(tagRepository.findAllById(List.of(tagId))).thenReturn(List.of());

        assertThatThrownBy(() -> tagService.setTaskTags(taskId, projectId, List.of(tagId), userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неизвестный");
        verify(taskTagRepository, never()).save(any());
    }
}
