package com.eventpulse.ai.client;

import com.eventpulse.ai.dto.EventSummaryDto;
import com.eventpulse.ai.dto.SearchCriteriaDto;

import java.util.List;

public interface EventCatalogClient {

    /**
     * Queries Event Service catalog for published events matching structured criteria.
     */
    List<EventSummaryDto> searchPublishedEvents(SearchCriteriaDto criteria);
}
