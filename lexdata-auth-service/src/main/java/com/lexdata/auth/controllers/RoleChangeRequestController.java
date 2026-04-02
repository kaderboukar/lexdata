package com.lexdata.auth.controllers;

import com.lexdata.auth.models.ERole;
import com.lexdata.auth.payload.request.RoleChangeRequestCreateRequest;
import com.lexdata.auth.payload.request.RoleChangeRequestRejectRequest;
import com.lexdata.auth.payload.response.RoleChangeRequestResponse;
import com.lexdata.auth.security.services.RoleChangeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/role-requests")
@RequiredArgsConstructor
public class RoleChangeRequestController {

    private final RoleChangeRequestService roleChangeRequestService;

    @PostMapping
    public ResponseEntity<RoleChangeRequestResponse> create(@Valid @RequestBody RoleChangeRequestCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String roleValue = request.getRequestedRole().toUpperCase();
        String normalized = roleValue.startsWith("ROLE_") ? roleValue : "ROLE_" + roleValue;
        ERole requestedRole;
        try {
            requestedRole = ERole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Role demande invalide.");
        }
        return ResponseEntity.ok(roleChangeRequestService.createRequest(username, requestedRole));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<RoleChangeRequestResponse>> listAll() {
        return ResponseEntity.ok(roleChangeRequestService.listAll());
    }

    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RoleChangeRequestResponse> approve(@PathVariable Long id) {
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(roleChangeRequestService.approve(id, performedBy));
    }

    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasAnyRole('AGENT_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<RoleChangeRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RoleChangeRequestRejectRequest request) {
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(roleChangeRequestService.reject(id, performedBy, request.getReason()));
    }
}
