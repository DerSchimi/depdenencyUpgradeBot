package de.schimi.version;

import de.schimi.core.VersionChecker;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Version checker implementation using Maven Central repository.
 */
public class MavenCentralVersionChecker implements VersionChecker {
    
    private static final Logger LOG = LoggerFactory.getLogger(MavenCentralVersionChecker.class);
    
    @Override
    public String findNewerMinorVersion(String group, String artifact, String currentVersion) {
        try {
            String[] parts = currentVersion.split("\\.");
            if (parts.length < 2) return null;
            int currentMajor = Integer.parseInt(parts[0]);
            int currentMinor = Integer.parseInt(parts[1]);

            String query = String.format("g:\"%s\" AND a:\"%s\"", group, artifact);
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
                LOG.info("Found newer minor versions: " + newerMinorVersions + " for " + group + ":" + artifact);
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
            LOG.error("Error fetching version for {}:{} → {}", group, artifact, e.getMessage());
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