package com.lexdata.auth.security.services;

import com.lexdata.auth.models.*;
import com.lexdata.auth.payload.response.RoleChangeRequestResponse;
import com.lexdata.auth.repository.RoleChangeRequestRepository;
import com.lexdata.auth.repository.RoleRepository;
import com.lexdata.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleChangeRequestService {

    private final RoleChangeRequestRepository roleChangeRequestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecurityAuditService securityAuditService;

    @Transactional
    public RoleChangeRequestResponse createRequest(String username, ERole requestedRole) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (requestedRole == ERole.ROLE_AGENT_ADMIN || requestedRole == ERole.ROLE_SUPER_ADMIN
                || requestedRole == ERole.ROLE_USER) {
            throw new IllegalArgumentException("Ce role ne peut pas etre demande.");
        }

        roleChangeRequestRepository.findByUserAndStatus(user, RoleChangeRequestStatus.PENDING)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Une demande en attente existe deja.");
                });

        RoleChangeRequest request = roleChangeRequestRepository.save(RoleChangeRequest.builder()
                .user(user)
                .requestedRole(requestedRole)
                .status(RoleChangeRequestStatus.PENDING)
                .build());

        securityAuditService.log("ROLE_CHANGE_REQUEST_CREATED", user.getId(), username,
                "requestedRole=" + requestedRole.name());
        return toResponse(request);
    }

    public List<RoleChangeRequestResponse> listAll() {
        return roleChangeRequestRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RoleChangeRequestResponse approve(Long requestId, String performedBy) {
        RoleChangeRequest request = roleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        if (request.getStatus() != RoleChangeRequestStatus.PENDING) {
            throw new IllegalArgumentException("Seules les demandes PENDING peuvent etre traitees.");
        }

        Role role = roleRepository.findByName(request.getRequestedRole())
                .orElseThrow(() -> new IllegalArgumentException("Role introuvable en base"));

        User user = request.getUser();
        user.setRoles(new HashSet<>(java.util.Set.of(role)));
        userRepository.save(user);

        request.setStatus(RoleChangeRequestStatus.APPROVED);
        request.setRejectionReason(null);
        request.setProcessedBy(performedBy);
        request.setProcessedAt(LocalDateTime.now());

        RoleChangeRequest saved = roleChangeRequestRepository.save(request);
        securityAuditService.log("ROLE_CHANGE_REQUEST_APPROVED", user.getId(), performedBy,
                "requestId=" + requestId + ", newRole=" + request.getRequestedRole().name());
        return toResponse(saved);
    }

    @Transactional
    public RoleChangeRequestResponse reject(Long requestId, String performedBy, String reason) {
        RoleChangeRequest request = roleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Demande introuvable"));
        if (request.getStatus() != RoleChangeRequestStatus.PENDING) {
            throw new IllegalArgumentException("Seules les demandes PENDING peuvent etre traitees.");
        }

        request.setStatus(RoleChangeRequestStatus.REJECTED);
        request.setRejectionReason(reason);
        request.setProcessedBy(performedBy);
        request.setProcessedAt(LocalDateTime.now());

        RoleChangeRequest saved = roleChangeRequestRepository.save(request);
        securityAuditService.log("ROLE_CHANGE_REQUEST_REJECTED", request.getUser().getId(), performedBy,
                "requestId=" + requestId + ", reason=" + reason);
        return toResponse(saved);
    }

    private RoleChangeRequestResponse toResponse(RoleChangeRequest request) {
        return RoleChangeRequestResponse.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .username(request.getUser().getUsername())
                .requestedRole(request.getRequestedRole().name())
                .status(request.getStatus().name())
                .rejectionReason(request.getRejectionReason())
                .processedBy(request.getProcessedBy())
                .processedAt(request.getProcessedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
