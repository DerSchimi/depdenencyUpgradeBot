package de.schimi.dependencyupdater;

import java.nio.file.Path;

/**
 * Interface for services that can update dependencies in different project types
 */
public interface DependencyUpdateService {
    
    /**
     * Updates all build files of this service's project type
     */
    void updateAllBuildFiles();
    
    /**
     * Updates a specific build file
     * @param buildFile the path to the build file to update
     */
    void updateBuildFile(Path buildFile);
    
    /**
     * Gets the project type this service handles
     * @return the project type
     */
    ProjectType getProjectType();
}