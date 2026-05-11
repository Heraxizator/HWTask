package org.example.hwtask.collaboration.service;

import org.example.hwtask.collaboration.persistence.TaskAttachment;
import org.example.hwtask.collaboration.persistence.TaskAttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class AttachmentPurgeService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentPurgeService.class);

    private final TaskAttachmentRepository taskAttachmentRepository;

    public AttachmentPurgeService(TaskAttachmentRepository taskAttachmentRepository) {
        this.taskAttachmentRepository = taskAttachmentRepository;
    }

    public void deleteStoredFilesForTask(UUID taskId) {
        for (TaskAttachment a : taskAttachmentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)) {
            Path path = Path.of(a.getStoragePath());
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                log.warn("Could not delete attachment file {}: {}", path, e.toString());
            }
        }
    }
}
