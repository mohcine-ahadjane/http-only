package com.app.service;

import com.app.dto.response.DepotResponse;
import com.app.dto.response.SiegeResponse;

import java.util.List;

public interface StockageService {
    List<SiegeResponse> getSiegesForCurrentUser(String username);
    List<DepotResponse> getDepotsBySiege(Long siegeId, String username);
    List<DepotResponse> getDepotsForCurrentUser(String username);
}
