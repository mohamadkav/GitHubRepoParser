package org.example;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubRepoParser {

    private static final String GITHUB_API_URL = "https://api.github.com/search/repositories";
    private static final String TOKEN = ""; // Replace with your GitHub token
    private static boolean containsKeywordInReadme(String repoFullName) {
        try {
            String readmeUrl = "https://api.github.com/repos/" + repoFullName + "/contents/README.md";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(readmeUrl))
                    .header("Authorization", "token " + TOKEN)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject readmeContent = new JSONObject(response.body());
            String contentEncoded = readmeContent.getString("content");
            String contentDecoded = new String(Base64.getDecoder().decode(contentEncoded));

            return contentDecoded.contains("cap-add");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String query = "language:C++ Dockerfile in:path";
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String fullUrl = GITHUB_API_URL + "?q=" + encodedQuery + "&sort=stars&order=desc&per_page=100";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .header("Authorization", "token " + TOKEN)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray repos = jsonResponse.getJSONArray("items");

            for (int i = 0; i < repos.length(); i++) {
                JSONObject repo = repos.getJSONObject(i);
                String repoName = repo.getString("full_name");
                System.out.println("https://github.com/" + repoName);
                System.out.println(containsKeywordInReadme(repoName));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}