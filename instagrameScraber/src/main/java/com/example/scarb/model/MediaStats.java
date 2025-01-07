package com.example.scarb.model;
/**
 * Author: Naveen Saini
 * Date: 03-Jan-2025	
 */
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class MediaStats {
    private int totalLikes;
    private int totalComments;
    private int reelsCount;
    private int photosCount;
    private int reelsLikes;
    private int photosLikes;
    private int reelsComments;
    private int photosComments;
}
