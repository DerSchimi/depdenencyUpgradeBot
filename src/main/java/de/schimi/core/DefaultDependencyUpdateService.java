package de.schimi.core;

import de.schimi.gradle.GradleBuildFileProcessor;
import de.schimi.maven.MavenBuildFileProcessor;
import de.schimi.version.MavenCentralVersionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of the dependency update service that supports multiple build systems.
 */
public class DefaultDependencyUpdateService implements DependencyUpdateService {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultDependencyUpdateService.class);
    
    private final List<BuildFileProcessor> processors;
    
    public DefaultDependencyUpdateService() {
        VersionChecker versionChecker = new MavenCentralVersionChecker();
        this.processors = Arrays.asList(
            new GradleBuildFileProcessor(versionChecker),
            new MavenBuildFileProcessor(versionChecker)
        );
    }
    
    @Override
    public void updateAllBuildFiles() {
        LOG.info("Starting dependency update process for all supported build systems...");
        
        for (BuildFileProcessor processor : processors) {
            LOG.info("Processing {} build files...", processor.getBuildSystemName());
            
            List<Path> buildFiles = processor.findBuildFiles();
            if (buildFiles.isEmpty()) {
                LOG.info("No {} build files found.", processor.getBuildSystemName());
                continue;
            }
            
            for (Path buildFile : buildFiles) {
                LOG.info("Updating {} build file: {}", processor.getBuildSystemName(), buildFile);
                processor.updateBuildFile(buildFile);
            }
        }
        
        LOG.info("Dependency update process completed.");
    }
    
    @Override
    public List<BuildFileProcessor> getSupportedProcessors() {
        return processors;
    }
}