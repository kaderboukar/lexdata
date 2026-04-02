package com.lexdata.juridique.controllers;

import com.lexdata.juridique.dto.FavoriteDto;
import com.lexdata.juridique.models.Favorite;
import com.lexdata.juridique.models.TexteJuridique;
import com.lexdata.juridique.repository.FavoriteRepository;
import com.lexdata.juridique.repository.TexteJuridiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/juridique/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteRepository favoriteRepository;
    private final TexteJuridiqueRepository texteRepository;

    @PostMapping("/{textId}")
    public ResponseEntity<FavoriteDto> addFavorite(@PathVariable Long textId) {
        String userId = getCurrentUserId();
        TexteJuridique texte = texteRepository.findByIdAndEstPublieTrue(textId)
                .orElseThrow(() -> new IllegalArgumentException("Texte juridique introuvable ou non publie"));

        if (favoriteRepository.findByUserIdAndTexteId(userId, textId).isPresent()) {
            throw new IllegalArgumentException("Ce texte est deja dans vos favoris");
        }

        Favorite saved = favoriteRepository.save(Favorite.builder()
                .userId(userId)
                .texte(texte)
                .build());

        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{textId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long textId) {
        String userId = getCurrentUserId();
        Favorite favorite = favoriteRepository.findByUserIdAndTexteId(userId, textId)
                .orElseThrow(() -> new IllegalArgumentException("Favori introuvable"));
        favoriteRepository.delete(favorite);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<FavoriteDto>> myFavorites() {
        String userId = getCurrentUserId();
        List<FavoriteDto> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(favorites);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new IllegalArgumentException("Utilisateur non authentifie");
        }
        return auth.getName();
    }

    private FavoriteDto toDto(Favorite favorite) {
        return FavoriteDto.builder()
                .id(favorite.getId())
                .legalTextId(favorite.getTexte().getId())
                .titre(favorite.getTexte().getTitre())
                .referenceOfficielle(favorite.getTexte().getReferenceOfficielle())
                .domaine(favorite.getTexte().getDomaine().name())
                .type(favorite.getTexte().getType().name())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
