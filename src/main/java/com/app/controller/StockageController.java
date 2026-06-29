package com.app.controller;

import com.app.dto.response.DepotResponse;
import com.app.dto.response.SiegeResponse;
import com.app.service.StockageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stockage")
@RequiredArgsConstructor
@Tag(name = "Stockage", description = "Endpoints for sieges and depots")
public class StockageController {

    private final StockageService stockageService;

    @GetMapping("/sieges")
    @Operation(summary = "Get the siege(s) of the connected user")
    public ResponseEntity<List<SiegeResponse>> getSiegesForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(stockageService.getSiegesForCurrentUser(userDetails.getUsername()));
    }

    @GetMapping("/sieges/{siegeId}/depots")
    @Operation(summary = "Get all depots of a specific siege (must belong to user)")
    public ResponseEntity<List<DepotResponse>> getDepotsBySiege(
            @PathVariable Long siegeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(stockageService.getDepotsBySiege(siegeId, userDetails.getUsername()));
    }

    @GetMapping("/depots")
    @Operation(summary = "Get all depots accessible by the connected user")
    public ResponseEntity<List<DepotResponse>> getDepotsForCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(stockageService.getDepotsForCurrentUser(userDetails.getUsername()));
    }
}
