package com.example.scarb.model;
/**
 * Author: Naveen Saini
 * Date: 03-Jan-2025	
 */
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class InstagramProfile {
    private String username;
    private String image;
    private String totalPosts;
    private String totalReels;
    private String followers;
    private String following;
    private String bio;
}