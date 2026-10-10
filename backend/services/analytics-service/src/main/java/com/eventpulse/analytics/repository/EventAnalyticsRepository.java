package com.eventpulse.analytics.repository;

import com.eventpulse.analytics.entity.EventAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventAnalyticsRepository extends JpaRepository<EventAnalytics, UUID> {

    Optional<EventAnalytics> findByEventId(UUID eventId);

    @Query("SELECT COALESCE(SUM(ea.totalRevenue), 0) FROM EventAnalytics ea")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(ea.ticketsSold), 0) FROM EventAnalytics ea")
    Long sumTotalTicketsSold();

    @Query("SELECT COALESCE(SUM(ea.confirmedBookings), 0) FROM EventAnalytics ea")
    Long sumTotalConfirmedBookings();
}
