package com.mentorx.api.feature.analytics.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        UUID userId,
        LocalDate generatedAt,
        List<Tile> earnings,
        List<Tile> jobs,
        List<Tile> courses,
        List<Tile> views,
        List<Tile> conversions
) {
    public record Tile(
            String key,
            String label,
            Object value,
            String unit,
            String trend
    ) {}
}
