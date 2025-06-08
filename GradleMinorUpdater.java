import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import org.json.*;

public class GradleMinorUpdater {

    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile(
        "'([\\w\\-.]+):([\\w\\-.]+):([\\d.]+)'");

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Gradle Minor Version Updater...");
        Path projectDir = Paths.get(".");
        List<Path> gradleFiles = Files.walk(projectDir)
            .filter(p -> p.getFileName().toString().equals("build.gradle"))
            .collect(Collectors.toList());

        for (Path gradleFile : gradleFiles) {
            String originalContent = Files.readString(gradleFile);
            Matcher matcher = DEPENDENCY_PATTERN.matcher(originalContent);
            StringBuffer newContent = new StringBuffer();

            while (matcher.find()) {
                String group = matcher.group(1);
                String artifact = matcher.group(2);
                String currentVersion = matcher.group(3);

                String updatedVersion = findNewerMinorVersion(group, artifact, currentVersion);
                if (updatedVersion != null && !updatedVersion.equals(currentVersion)) {
                    System.out.printf("Updating %s:%s from %s to %s%n", group, artifact, currentVersion, updatedVersion);
                    String replacement = "'" + group + ":" + artifact + ":" + updatedVersion + "'";
                    matcher.appendReplacement(newContent, Matcher.quoteReplacement(replacement));
                } else {
                    matcher.appendReplacement(newContent, Matcher.quoteReplacement(matcher.group(0)));
                }
            }

            matcher.appendTail(newContent);
            Files.writeString(gradleFile, newContent.toString());
        }
    }

    private static String findNewerMinorVersion(String group, String artifact, String currentVersion) {
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
                System.out.println("Found newer minor versions: " + newerMinorVersions);
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
                })                .orElse(null);

        } catch (Exception e) {
            System.err.printf("Error fetching version for %s:%s → %s%n", group, artifact, e.getMessage());
            return null;
        }
    }

    private static List<Integer> versionToTuple(String version) {
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
