package com.eventpulse.booking.service;

import com.eventpulse.booking.dto.CostCalculationRequest;
import com.eventpulse.booking.dto.CostCalculationResponse;

public interface CostingService {
    CostCalculationResponse calculateCost(CostCalculationRequest request);
}
