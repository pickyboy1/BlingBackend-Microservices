package com.pickyboy.blingBackend.dto.resource;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceInteractionStatusVO {

    private Boolean isLiked;

    private Boolean isFavorited;
}