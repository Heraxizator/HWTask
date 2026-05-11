package org.example.hwtask.timetracking.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.security.UserPrincipal;
import org.example.hwtask.timetracking.dto.TimeEntryResponse;
import org.example.hwtask.timetracking.service.TimeTrackingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/time-entries")
@Tag(name = "Time tracking (me)")
public class MeTimeTrackingController {

    private final TimeTrackingService timeTrackingService;

    public MeTimeTrackingController(TimeTrackingService timeTrackingService) {
        this.timeTrackingService = timeTrackingService;
    }

    @PostMapping("/stop")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Остановить активный таймер")
    public TimeEntryResponse stop(@AuthenticationPrincipal UserPrincipal user) {
        return timeTrackingService.stopTimer(user.getId());
    }
}
