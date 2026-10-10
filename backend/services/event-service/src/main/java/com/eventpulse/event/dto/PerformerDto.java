package com.eventpulse.event.dto;

import com.eventpulse.event.entity.Performer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformerDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String role;
    private String imageUrl;
    private String bio;
    private Integer displayOrder;

    public static PerformerDto fromEntity(Performer performer) {
        if (performer == null) {
            return null;
        }
        return PerformerDto.builder()
                .id(performer.getId())
                .name(performer.getName())
                .role(performer.getRole())
                .imageUrl(performer.getImageUrl())
                .bio(performer.getBio())
                .displayOrder(performer.getDisplayOrder())
                .build();
    }
}
