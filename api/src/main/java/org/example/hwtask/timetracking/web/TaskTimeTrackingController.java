package org.example.hwtask.timetracking.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.timetracking.dto.StartTimerRequest;
import org.example.hwtask.timetracking.dto.TimeEntryResponse;
import org.example.hwtask.timetracking.service.TimeTrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks/{taskId}/time-entries")
@Tag(name = "Time tracking")
public class TaskTimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    public TaskTimeTrackingController(TimeTrackingService timeTrackingService) {
        this.timeTrackingService = timeTrackingService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Начать учёт времени по задаче (останавливает предыдущий таймер пользователя)")
    public TimeEntryResponse start(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId,
            @RequestBody(required = false) StartTimerRequest request
    ) {
        String note = request != null ? request.commentNote() : null;
        return timeTrackingService.startTimer(taskId, user.getId(), note);
    }

    @GetMapping
    @Operation(summary = "Записи учёта времени по задаче")
    public List<TimeEntryResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable UUID taskId
    ) {
        return timeTrackingService.listTaskEntries(taskId, user.getId());
    }
}
