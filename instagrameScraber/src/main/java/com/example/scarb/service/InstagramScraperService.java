package com.example.scarb.service;

/**
 * Author: Naveen Saini
 * Date: 03-Jan-2025	
 */
import com.example.scarb.model.InstagramProfile;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Service
public class InstagramScraperService {
	
    private static final String INSTAGRAM_BASE_URL = "https://www.instagram.com/";
    private static final String USERNAME = "email"; // Replace with your Instagram username
    private static final String PASSWORD = "password"; // Replace with your Instagram password

    // a function for making the new chrome 
    public WebDriver webDriver() {
        // Set up chrome options
        ChromeOptions options = new ChromeOptions();

        // Add default arguments
        options.addArguments(Arrays.asList(
                "--disable-blink-features=AutomationControlled",
                "--disable-notifications",
                "--disable-gpu",
                "--no-sandbox",
                "--disable-dev-shm-usage"));

        // Set user agent
        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        // Disable automation flags
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        // Add additional preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        // Create and return WebDriver
        return new ChromeDriver(options);
        // fucking old code

        // ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Run in headless mode
        // options.addArguments("--disable-notifications");
        // options.addArguments("--disable-gpu");
        // options.addArguments("--no-sandbox");
        // options.addArguments("--disable-dev-shm-usage");
        // ChromeDriver driver = new ChromeDriver(options);
        // return driver;

    }

    // a function for login because have to login first so we won't get the inappropriate screen stuff 
    public void login(WebDriver driver) {
        driver.get(INSTAGRAM_BASE_URL + "accounts/login/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Handle cookie consent if present
        try {
            WebElement cookieConsent = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Allow essential and optional cookies')]")));
            cookieConsent.click();
        } catch (TimeoutException e) {
        	System.out.println("got exception in login method"+e.getMessage());
            // Cookie consent might not appear, continue
        }

        // Login
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordInput = driver.findElement(By.name("password"));

        usernameInput.sendKeys(USERNAME);
        passwordInput.sendKeys(PASSWORD);
        passwordInput.sendKeys(Keys.ENTER);

        // Wait for login to complete
        try {
            wait.until(ExpectedConditions.urlContains("instagram.com/"));
            System.out.println("logedin");

        } catch (TimeoutException e) {
            throw new RuntimeException("Login failed");
        }
    }

    public InstagramProfile getProfile(String username) {
    	
        WebDriver driver = webDriver();
        try {
        	
        driver.get(INSTAGRAM_BASE_URL + username);
        System.out.println("geting in get Profile fucntions " + username);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Wait for profile data to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("header section")));

        // Extract profile information
        List<WebElement> statsElements = driver.findElements(By.cssSelector("header section li"));

        String posts = statsElements.get(0).getText().split(" ")[0].replace(",", "");
        String followers = statsElements.get(1).getText().split(" ")[0].replace(",", "");
        String following = statsElements.get(2).getText().split(" ")[0].replace(",", "");
        WebElement imageElement = driver.findElement(By.cssSelector("header > :first-child div img"));
        String imageUrl = imageElement.getAttribute("src");

        String bio = driver.findElement(By.cssSelector("header section div:nth-child(3)")).getText();

        return InstagramProfile.builder()
                .username(username)
                .totalPosts(posts)
                .followers(followers)
                .following(following)
                .bio(bio)
                .image(imageUrl)
                .build();
        }
        finally {
        	driver.quit();
        }
    }

    public Map<String, Object> getMediaStats(String username) {
        WebDriver driver = webDriver();
        try {

            {
                try {
                    login(driver);

                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            int maxScrolls = 15;
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> postsData = new ArrayList<>();
            List<Map<String, Object>> reelsData = new ArrayList<>();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;

            try {
                // Fetch posts
                driver.get("https://www.instagram.com/" + username);
                System.out.println("Fetching posts...");
                Thread.sleep(5000); // Initial wait for page load
                Actions actions = new Actions(driver);

                List<WebElement> posts = new ArrayList<>();
                Set<String> loadedPostsUrl = new HashSet<>();

                int previousSize = 0;

                for (int i = 0; i < maxScrolls; i++) {
                    // Scroll to load more posts
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    Thread.sleep(2000); // Wait for new content to load

                    // Fetch visible posts
                    List<WebElement> currentPosts = driver.findElements(By.cssSelector("a[href*='/p/']"));
                    for (WebElement post : currentPosts) {
                        String reelUrl = post.getAttribute("href");
                        if (!loadedPostsUrl.contains(reelUrl)) {
                            loadedPostsUrl.add(reelUrl); // Track this reel as loaded
                            posts.add(post); // Add the new reel to the list

                            System.out.println("Loaded posts: " + currentPosts.size());

                            try {
                                Map<String, Object> postData = new HashMap<>();
                                String href = post.getAttribute("href");
                                actions.moveToElement(post).perform();
                                WebElement ulElement = post.findElement(By.cssSelector("ul"));
                                List<WebElement> listItems = ulElement.findElements(By.cssSelector("li"));

                                String likesCount = "N/A";
                                String commentsCount = "N/A";
                                System.out.println(listItems.size() + " post size");
                                for (WebElement item : listItems) {
                                    // Check for specific indicators of likes or comments
                                    WebElement spanElement = item.findElement(By.cssSelector("span.html-span"));
                                    String count = spanElement.getText();

                                    // Example: Assume the first <li> contains likes, second contains comments
                                    if (listItems.indexOf(item) == 0) {
                                        likesCount = count; // First item is likes
                                    } else if (listItems.indexOf(item) == 1) {
                                        commentsCount = count; // Second item is comments
                                    }
                                }

                                postData.put("likes", likesCount);
                                postData.put("comments", commentsCount);
                                postData.put("type", "post");
                                postData.put("url", href);

                                // Extract post-specific metadata
                                WebElement image = post.findElement(By.tagName("img"));
                                postData.put("imageUrl", image.getAttribute("src"));
                                postData.put("bio", image.getAttribute("alt"));

                                postsData.add(postData);
                            } catch (Exception e) {
                                System.err.println("Error processing post: " + e.getMessage());
                            }
                        }

                    }

                }
                // Fetch reels
                // Initial wait for page load
                driver.get("https://www.instagram.com/" + username + "/reels/");
                System.out.println("Fetching reels...");
                Actions reelAction = new Actions(driver);
                Thread.sleep(5000); // Initial wait for page load

                List<WebElement> reels = new ArrayList<>();
                Set<String> loadedReelUrls = new HashSet<>();

                previousSize = 0;

                for (int i = 0; i < 10; i++) {
                    // Scroll to load more reels
                    jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                    Thread.sleep(5000); // Wait for new content to load

                    List<WebElement> currentReels = driver.findElements(By.cssSelector("a[href*='/reel/']"));

                    for (WebElement reel : currentReels) {
                        String reelUrl = reel.getAttribute("href");
                        if (!loadedReelUrls.contains(reelUrl)) {
                            loadedReelUrls.add(reelUrl); // Track this reel as loaded
                            reels.add(reel); // Add the new reel to the list

                            System.out.println(" reel size " + reels.size() + " reelsData " + reelsData.size());

                            try {
                                Map<String, Object> reelData = new HashMap<>();

                                String href = reel.getAttribute("href");

                                reelData.put("type", "reel");
                                reelData.put("url", href);

                                String imageUrl = "N/A";

                                WebElement imageElement = reel.findElement(By.cssSelector("div:first-child"));
                                String style = imageElement.getAttribute("style");
                                if (style.contains("background-image")) {
                                    imageUrl = style.substring(style.indexOf("url(") + 4, style.indexOf(")"))
                                            .replace("\"", "");
                                }
                                reelData.put("image", imageUrl);

                                // adding the likes and comment
                                boolean wating = addLikesAndComments(reel, reelData, driver, reelAction);

                                System.out.println("wait done " + wating);

                                // Extract reel-specific metadata (e.g., views)
                                try {

                                    WebElement viewsSpan = reel.findElement(By.cssSelector("span.html-span"));
                                    reelData.put("views", viewsSpan.getText());

                                } catch (NoSuchElementException e) {
                                    System.err.println("Views element not found for reel: " + href);
                                    reelData.put("views", "N/A");

                                }

                                reelsData.add(reelData);
                            } catch (Exception e) {
                                System.err.println("Error processing reel: " + e.getMessage());

                            }

                        }
                    }

                    System.out.println("Loaded reels: " + reels.size());

                    // Break the loop if no new reels are loaded
                    if (reels.size() == previousSize) {
                        System.out.println("No more new reels loaded.");
                        break;
                    }

                    previousSize = reels.size();
                }

                System.out.println("Loop ends here of loded reels" + reels.size() + " ff " + loadedReelUrls.size());

                System.out.println(reels.size() + " beofre moving the add likes and comments " + reelsData.size());

            } catch (Exception e) {

                System.err.println("Error fetching data: " + e.getMessage());
            }
            // Combine results
            result.put("posts", postsData);
            result.put("reels", reelsData);

            return result;
        }

        finally {

            driver.quit();
        }

    }

    public boolean addLikesAndComments(WebElement reel, Map<String, Object> reelsData, WebDriver driver,
            Actions actions) {

        try {

            // Hover over the reel element to reveal likes and comments
            actions.moveToElement(reel).perform();

            // Find the <ul> element that contains likes and comments
            WebElement ulElement = reel.findElement(By.cssSelector("ul"));
            List<WebElement> listItems = ulElement.findElements(By.cssSelector("li"));

            // Extract likes
            String likesCount = "N/A";
            if (!listItems.isEmpty()) {
                try {
                    WebElement likesElement = listItems.get(0).findElement(By.cssSelector("span.html-span"));
                    likesCount = likesElement.getText();
                } catch (NoSuchElementException e) {
                    System.err.println("Likes element not found for reel: ");
                }
            }

            // Extract comments
            String commentsCount = "N/A";
            if (listItems.size() > 1) {
                try {
                    WebElement commentsElement = listItems.get(1).findElement(By.cssSelector("span.html-span"));
                    commentsCount = commentsElement.getText();
                } catch (NoSuchElementException e) {
                    System.err.println("Comments element not found for reel: ");
                }
            }

            // Update the existing reelData map with likes and comments
            reelsData.put("likes", likesCount);
            reelsData.put("comments", commentsCount);
        } catch (Exception e) {
            System.err.println("Error processing reel for likes/comments: " + e.getMessage());
        }

        return true;
    }
}
