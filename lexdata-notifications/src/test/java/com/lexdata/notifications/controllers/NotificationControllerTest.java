// package com.lexdata.notifications.controllers;

// import com.lexdata.notifications.models.Notification;
// import com.lexdata.notifications.models.NotificationPreference;
// import com.lexdata.notifications.repository.NotificationPreferenceRepository;
// import com.lexdata.notifications.repository.NotificationRepository;
// import com.lexdata.notifications.services.NotificationService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Pageable;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContext;
// import org.springframework.security.core.context.SecurityContextHolder;

// import java.util.Collections;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class NotificationControllerTest {

// @Mock
// private NotificationService notificationService;

// @Mock
// private NotificationRepository notificationRepository;

// @Mock
// private NotificationPreferenceRepository preferenceRepository;

// @Mock
// private SecurityContext securityContext;

// @Mock
// private Authentication authentication;

// @InjectMocks
// private NotificationController notificationController;

// @BeforeEach
// void setUp() {
// SecurityContextHolder.setContext(securityContext);
// }

// @Test
// void getMyNotifications_ShouldReturnList() {
// // Arrange
// when(securityContext.getAuthentication()).thenReturn(authentication);
// when(authentication.getName()).thenReturn("test_user");
// when(notificationRepository.findByUsernameOrderByDateCreationDesc(eq("test_user"),
// any(Pageable.class)))
// .thenReturn(Collections.emptyList());

// // Act
// List<Notification> result = notificationController.getMyNotifications(0, 20);

// // Assert
// assertNotNull(result);
// verify(notificationRepository).findByUsernameOrderByDateCreationDesc(eq("test_user"),
// any(Pageable.class));
// }

// @Test
// void markAsRead_ShouldUpdateStatus() {
// // Arrange
// Long notifId = 1L;
// Notification notif = new Notification();
// notif.setId(notifId);
// notif.setRead(false);

// when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
// when(notificationRepository.save(any(Notification.class))).thenAnswer(i ->
// i.getArgument(0));

// // Act
// ResponseEntity<Notification> response =
// notificationController.markAsRead(notifId);

// // Assert
// assertEquals(200, response.getStatusCode().value());
// assertTrue(response.getBody().isRead());
// assertNotNull(response.getBody().getDateLecture());
// verify(notificationRepository).save(notif);
// }

// @Test
// void getPreferences_ShouldReturnPreferences() {
// // Arrange
// when(securityContext.getAuthentication()).thenReturn(authentication);
// when(authentication.getName()).thenReturn("test_user");
// NotificationPreference prefs = new NotificationPreference();
// prefs.setUsername("test_user");
// when(preferenceRepository.findByUsername("test_user")).thenReturn(Optional.of(prefs));

// // Act
// ResponseEntity<NotificationPreference> response =
// notificationController.getPreferences();

// // Assert
// assertEquals(200, response.getStatusCode().value());
// assertNotNull(response.getBody());
// verify(preferenceRepository).findByUsername("test_user");
// }

// @Test
// void sendTestNotification_ShouldCallService() {
// // Arrange
// when(securityContext.getAuthentication()).thenReturn(authentication);
// when(authentication.getName()).thenReturn("test_user");

// // Act
// ResponseEntity<String> response =
// notificationController.sendTestNotification("Titre", "Message");

// // Assert
// assertEquals(200, response.getStatusCode().value());
// verify(notificationService).sendNotification(eq("test_user"), eq("Titre"),
// eq("Message"), any());
// }
// }
