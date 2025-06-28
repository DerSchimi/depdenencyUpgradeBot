package de.schimi.dependencyupdater;

import de.schimi.dependencyupdater.gradle.GradleUpdateService;
import de.schimi.dependencyupdater.maven.MavenUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Main entry point for the dependency update bot
 */
public class DependencyUpdateBot {
    
    private static final Logger LOG = LoggerFactory.getLogger(DependencyUpdateBot.class);
    
    private final Map<ProjectType, DependencyUpdateService> services;
    
    public DependencyUpdateBot() {
        this.services = new HashMap<>();
        this.services.put(ProjectType.GRADLE, new GradleUpdateService());
        this.services.put(ProjectType.MAVEN, new MavenUpdateService());
    }
    
    public static void main(String[] args) {
        DependencyUpdateBot bot = new DependencyUpdateBot();
        
        if (args.length > 0) {
            // If project type is specified as argument
            String projectTypeArg = args[0].toLowerCase();
            switch (projectTypeArg) {
                case "gradle":
                    bot.updateProject(ProjectType.GRADLE);
                    break;
                case "maven":
                    bot.updateProject(ProjectType.MAVEN);
                    break;
                default:
                    LOG.error("Unsupported project type: {}. Supported types: gradle, maven", args[0]);
                    System.exit(1);
            }
        } else {
            // Auto-detect project types
            bot.updateAllDetectedProjects();
        }
    }
    
    /**
     * Updates dependencies for a specific project type
     */
    public void updateProject(ProjectType projectType) {
        DependencyUpdateService service = services.get(projectType);
        if (service != null) {
            LOG.info("Updating {} projects...", projectType.name().toLowerCase());
            service.updateAllBuildFiles();
        } else {
            LOG.error("No service available for project type: {}", projectType);
        }
    }
    
    /**
     * Auto-detects project types in current directory and updates all found projects
     */
    public void updateAllDetectedProjects() {
        LOG.info("Auto-detecting project types in current directory...");
        
        for (ProjectType projectType : ProjectType.values()) {
            Path buildFile = Paths.get(projectType.getBuildFileName());
            if (Files.exists(buildFile)) {
                LOG.info("Detected {} project (found {})", projectType.name().toLowerCase(), projectType.getBuildFileName());
                updateProject(projectType);
            }
        }
        
        // Also check subdirectories for multi-module projects
        for (ProjectType projectType : ProjectType.values()) {
            try {
                long count = Files.walk(Paths.get("."), 3) // limit depth to avoid going too deep
                    .filter(p -> p.getFileName().toString().equals(projectType.getBuildFileName()))
                    .filter(p -> !p.equals(Paths.get(projectType.getBuildFileName()))) // exclude already processed root file
                    .count();
                
                if (count > 0) {
                    LOG.info("Found {} additional {} files in subdirectories", count, projectType.getBuildFileName());
                    updateProject(projectType);
                }
            } catch (Exception e) {
                LOG.warn("Error scanning for {} files: {}", projectType.getBuildFileName(), e.getMessage());
            }
        }
    }
}