package de.schimi.dependencyupdater;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for checking and finding newer versions of dependencies
 */
public class VersionService {
    
    private static final Logger LOG = LoggerFactory.getLogger(VersionService.class);
    
    /**
     * Finds a newer minor version for the given dependency
     * @param groupId the group ID of the dependency
     * @param artifactId the artifact ID of the dependency  
     * @param currentVersion the current version
     * @return the newer minor version, or null if none found
     */
    public String findNewerMinorVersion(String groupId, String artifactId, String currentVersion) {
        try {
            String[] parts = currentVersion.split("\\.");
            if (parts.length < 2) return null;
            int currentMajor = Integer.parseInt(parts[0]);
            int currentMinor = Integer.parseInt(parts[1]);

            String query = String.format("g:\"%s\" AND a:\"%s\"", groupId, artifactId);
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String queryUrl = String.format(
                "https://search.maven.org/solrsearch/select?q=%s&rows=100&wt=json",
                encodedQuery);
            HttpURLConnection conn = (HttpURLConnection) new URL(queryUrl).openConnection();
            conn.setRequestProperty("Accept", "application/json");

            String response = new String(conn.getInputStream().readAllBytes());
            JSONObject json = new JSONObject(response);
            JSONArray docs = json.getJSONObject("response").getJSONArray("docs");

            List<String> newerMinorVersions = new ArrayList<>();
            for (int i = 0; i < docs.length(); i++) {
                String v = docs.getJSONObject(i).optString("latestVersion", "");
                if (v.matches("\\d+\\.\\d+\\.\\d+")) {
                    String[] vParts = v.split("\\.");
                    int major = Integer.parseInt(vParts[0]);
                    int minor = Integer.parseInt(vParts[1]);
                    if (major == currentMajor && minor > currentMinor) {
                        newerMinorVersions.add(v);
                    }
                }
            }

            if (!newerMinorVersions.isEmpty()) {
                LOG.info("Found newer minor versions: {} for {}:{}", newerMinorVersions, groupId, artifactId);
            }

            return newerMinorVersions.stream()
                .max((v1, v2) -> {
                    List<Integer> t1 = versionToTuple(v1);
                    List<Integer> t2 = versionToTuple(v2);
                    for (int i = 0; i < Math.min(t1.size(), t2.size()); i++) {
                        int cmp = Integer.compare(t1.get(i), t2.get(i));
                        if (cmp != 0) return cmp;
                    }
                    return Integer.compare(t1.size(), t2.size());
                })
                .orElse(null);

        } catch (Exception e) {
            LOG.error("Error fetching version for {}:{} → {}", groupId, artifactId, e.getMessage());
            return null;
        }
    }
    
    private List<Integer> versionToTuple(String version) {
        return Arrays.stream(version.split("\\."))
            .map(s -> {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return -1;
                }
            })
            .collect(Collectors.toList());
    }
}