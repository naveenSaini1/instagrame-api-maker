package com.example.scarb.InstagramScraperController;
/**
 * Author: Naveen Saini
 * Date: 03-Jan-2025	
 */
import com.example.scarb.model.InstagramProfile;
import com.example.scarb.model.MediaStats;
import com.example.scarb.service.InstagramScraperService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instagram")
@RequiredArgsConstructor
public class InstagramScraperController {
    private final InstagramScraperService scraperService;

    @GetMapping("/login")
    public void login() {
       // scraperService.login();
    }

    @GetMapping("/profile/{username}")
    public InstagramProfile getProfile(@PathVariable("username") String username) {
        return scraperService.getProfile(username);
    }

    @GetMapping("/stats/{username}")
    public Map<String, Object>getMediaStats(@PathVariable("username") String username) {
        return scraperService.getMediaStats(username);
    }
}
