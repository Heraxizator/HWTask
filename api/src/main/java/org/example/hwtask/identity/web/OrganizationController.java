package org.example.hwtask.identity.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hwtask.identity.service.OrganizationService;
import org.example.hwtask.identity.web.dto.CreateOrganizationRequest;
import org.example.hwtask.identity.web.dto.OrganizationResponse;
import org.example.hwtask.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать организацию")
    public OrganizationResponse create(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        return organizationService.create(request, user.getId());
    }

    @GetMapping
    @Operation(summary = "Мои организации")
    public List<OrganizationResponse> list(@AuthenticationPrincipal UserPrincipal user) {
        return organizationService.listForUser(user.getId());
    }
}
