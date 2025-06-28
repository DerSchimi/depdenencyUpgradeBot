package de.schimi.dependencyupdater.gradle;

import de.schimi.dependencyupdater.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for updating Gradle build.gradle files
 */
public class GradleUpdateService implements DependencyUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(GradleUpdateService.class);
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("'([\\w\\-.]+):([\\w\\-.]+):([\\d.]+)'");
    
    private final VersionService versionService;
    
    public GradleUpdateService() {
        this.versionService = new VersionService();
    }
    
    public GradleUpdateService(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    public void updateAllBuildFiles() {
        try {
            var gradleFiles = Files.walk(Paths.get("."))
                .filter(p -> p.getFileName().toString().equals(getProjectType().getBuildFileName()))
                .collect(Collectors.toList());
            LOG.info("Found {} build.gradle files.", gradleFiles.size());

            for (Path gradleFile : gradleFiles) {
                updateBuildFile(gradleFile);
            }
        } catch (IOException e) {
            LOG.error("Error while searching for build.gradle files: {}", e.getMessage());
        }
    }

    @Override
    public void updateBuildFile(Path gradleFile) {
        try {
            String originalContent = Files.readString(gradleFile);
            Matcher matcher = DEPENDENCY_PATTERN.matcher(originalContent);
            StringBuffer newContent = new StringBuffer();

            while (matcher.find()) {
                String group = matcher.group(1);
                String artifact = matcher.group(2);
                String currentVersion = matcher.group(3);

                String updatedVersion = versionService.findNewerMinorVersion(group, artifact, currentVersion);
                if (updatedVersion != null && !updatedVersion.equals(currentVersion)) {
                    LOG.info("Updating {}:{} from {} to {}", group, artifact, currentVersion, updatedVersion);
                    String replacement = "'" + group + ":" + artifact + ":" + updatedVersion + "'";
                    matcher.appendReplacement(newContent, Matcher.quoteReplacement(replacement));
                } else {
                    matcher.appendReplacement(newContent, Matcher.quoteReplacement(matcher.group(0)));
                    LOG.info("Skipping {}:{} as no newer minor version found or already up-to-date.", group, artifact);
                }
            }
            matcher.appendTail(newContent);
            String newFileName = gradleFile.getFileName().toString() + ".updated";
            Files.writeString(Path.of(newFileName), newContent.toString());
            LOG.info("Updated file {} and saved as {}", gradleFile, newFileName);
        } catch (Exception e) {
            LOG.error("Error updating file {}: {}", gradleFile, e.getMessage());
        }
    }

    @Override
    public ProjectType getProjectType() {
        return ProjectType.GRADLE;
    }
}