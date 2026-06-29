package com.app.service.impl;

import com.app.dto.response.DepotResponse;
import com.app.dto.response.SiegeResponse;
import com.app.entity.AppUser;
import com.app.entity.Depot;
import com.app.entity.Siege;
import com.app.exception.BadRequestException;
import com.app.exception.ResourceNotFoundException;
import com.app.repository.DepotRepository;
import com.app.repository.SiegeRepository;
import com.app.repository.UserRepository;
import com.app.service.StockageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockageServiceImpl implements StockageService {

    private final UserRepository userRepository;
    private final SiegeRepository siegeRepository;
    private final DepotRepository depotRepository;

    @Override
    public List<SiegeResponse> getSiegesForCurrentUser(String username) {
        AppUser user = getUser(username);
        // Un user appartient à un seul siège
        Siege siege = user.getSiege();
        return List.of(toSiegeResponse(siege));
    }

    @Override
    public List<DepotResponse> getDepotsBySiege(Long siegeId, String username) {
        AppUser user = getUser(username);

        // Vérifier que le siège demandé est bien celui de l'utilisateur
        if (!user.getSiege().getId().equals(siegeId)) {
            throw new BadRequestException("Access denied to siege: " + siegeId);
        }

        Siege siege = siegeRepository.findById(siegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Siege not found: " + siegeId));

        return depotRepository.findAllBySiegeId(siegeId).stream()
                .map(d -> toDepotResponse(d, siege))
                .toList();
    }

    @Override
    public List<DepotResponse> getDepotsForCurrentUser(String username) {
        AppUser user = getUser(username);
        Siege siege = user.getSiege();
        return depotRepository.findAllBySiegeId(siege.getId()).stream()
                .map(d -> toDepotResponse(d, siege))
                .toList();
    }

    // ─── Mappers ────────────────────────────────────────────────────────────

    private SiegeResponse toSiegeResponse(Siege siege) {
        return new SiegeResponse(siege.getId(), siege.getNom(), siege.getAdresse());
    }

    private DepotResponse toDepotResponse(Depot depot, Siege siege) {
        return new DepotResponse(
                depot.getId(),
                depot.getNom(),
                depot.getAdresse(),
                siege.getId(),
                siege.getNom()
        );
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }
}
