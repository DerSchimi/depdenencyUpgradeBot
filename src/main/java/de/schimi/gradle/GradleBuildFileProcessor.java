package de.schimi.gradle;

import de.schimi.core.BuildFileProcessor;
import de.schimi.core.DefaultFileFinder;
import de.schimi.core.FileFinder;
import de.schimi.core.VersionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Build file processor for Gradle build.gradle files.
 */
public class GradleBuildFileProcessor implements BuildFileProcessor {
    
    private static final Logger LOG = LoggerFactory.getLogger(GradleBuildFileProcessor.class);
    
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("'([\\w\\-.]+):([\\w\\-.]+):([\\d.]+)'");
    
    private final VersionChecker versionChecker;
    private final FileFinder fileFinder;
    
    public GradleBuildFileProcessor(VersionChecker versionChecker) {
        this.versionChecker = versionChecker;
        this.fileFinder = new DefaultFileFinder();
    }
    
    @Override
    public String getFilePattern() {
        return "build.gradle";
    }
    
    @Override
    public List<Path> findBuildFiles() {
        List<Path> gradleFiles = fileFinder.findFiles(getFilePattern());
        LOG.info("Found {} build.gradle files.", gradleFiles.size());
        return gradleFiles;
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

                String updatedVersion = versionChecker.findNewerMinorVersion(group, artifact, currentVersion);
                if (updatedVersion != null && !updatedVersion.equals(currentVersion)) {
                    LOG.info("Updating {}:{} from {} to {}", group, artifact, currentVersion, updatedVersion);
                    String replacement = "'" + group + ":" + artifact + ":" + updatedVersion + "'";
                    matcher.appendReplacement(newContent, Matcher.quoteReplacement(replacement));
                } else {
                    matcher.appendReplacement(newContent, Matcher.quoteReplacement(matcher.group(0)));
                    LOG.info("Skipping " + group + ":" + artifact + " as no newer minor version found or already up-to-date.");
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
    public String getBuildSystemName() {
        return "Gradle";
    }
}