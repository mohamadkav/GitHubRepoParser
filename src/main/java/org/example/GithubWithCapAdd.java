package org.example;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class GithubWithCapAdd {
    private static final String TOKEN = ""; // Replace with your token
    private static final String SEARCH_URL = "https://api.github.com/search/repositories";
    private static final String CONTENTS_URL = "https://api.github.com/repos/";

    public static void main(String[] args) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String query = "language:C++&sort=stars&order=desc&per_page=100"; // 100 results per page
            int page = 1;
            boolean moreResults = true;

            while (moreResults) {
                //Thread.sleep(20000);
                HttpGet searchRequest = new HttpGet(SEARCH_URL + "?q=" + query + "&page=" + page);
                searchRequest.addHeader("Authorization", "token " + TOKEN);
                searchRequest.addHeader("Accept", "application/vnd.github.v3+json");

                String searchResponse = EntityUtils.toString(client.execute(searchRequest).getEntity());
                JSONArray items = new JSONObject(searchResponse).getJSONArray("items");

                if (items.length() == 0) {
                    moreResults = false;
                    break;
                }

                for (int i = 0; i < items.length(); i++) {
                    JSONObject repo = items.getJSONObject(i);
                    String repoName = repo.getString("full_name");

                    // Check for Dockerfile in the repository
                    HttpGet contentsRequest = new HttpGet(CONTENTS_URL + repoName + "/contents/");
                    contentsRequest.addHeader("Authorization", "token " + TOKEN);
                    contentsRequest.addHeader("Accept", "application/vnd.github.v3+json");

                    String contentsResponse = EntityUtils.toString(client.execute(contentsRequest).getEntity());
                    JSONArray contents = new JSONArray(contentsResponse);

                    boolean hasDockerfile = contents.toString().contains("\"name\":\"Dockerfile\"");
                    if (hasDockerfile) {
                        System.out.println("https://github.com/" + repoName);

                        HttpGet readmeRequest = new HttpGet(CONTENTS_URL + repoName + "/readme");
                        readmeRequest.addHeader("Authorization", "token " + TOKEN);
                        readmeRequest.addHeader("Accept", "application/vnd.github.VERSION.raw");

                        try {
                            String readmeResponse = EntityUtils.toString(client.execute(readmeRequest).getEntity());
                            if (readmeResponse.contains("cap-add")) {
                                System.out.println("Repository with Dockerfile and 'cap-add' in README: " + repoName);
                            }
                        } catch (Exception e) {
                            // Handle the case where README is not present or other errors
                            System.out.println("Error fetching README for " + repoName);
                        }
                    }
                }

                page++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
