package com.app.dto.response;

public record DepotResponse(
        Long id,
        String nom,
        String adresse,
        Long siegeId,
        String siegeNom
) {}
