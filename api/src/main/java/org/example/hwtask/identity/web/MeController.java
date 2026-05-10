package org.example.hwtask.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.hwtask.identity.web.dto.UserPublicResponse;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Profile")
public class MeController {

    @GetMapping
    @Operation(summary = "Текущий пользователь")
    public UserPublicResponse me(@AuthenticationPrincipal UserPrincipal user) {
        return new UserPublicResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
