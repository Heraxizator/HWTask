package org.example.hwtask.notification.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.notification.dto.NotificationResponse;
import org.example.hwtask.notification.service.NotificationService;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/notifications")
@Tag(name = "Notifications")
public class MeNotificationsController {

    private final NotificationService notificationService;

    public MeNotificationsController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Лента уведомлений")
    public Page<NotificationResponse> list(
            @AuthenticationPrincipal UserPrincipal user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return notificationService.list(user.getId(), pageable);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Число непрочитанных")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal UserPrincipal user) {
        return new UnreadCountResponse(notificationService.unreadCount(user.getId()));
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Отметить все как прочитанные")
    public void markAllRead(@AuthenticationPrincipal UserPrincipal user) {
        notificationService.markAllRead(user.getId());
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Отметить уведомление прочитанным")
    public void markRead(@AuthenticationPrincipal UserPrincipal user, @PathVariable UUID id) {
        notificationService.markRead(user.getId(), id);
    }

    public record UnreadCountResponse(long count) {
    }
}
