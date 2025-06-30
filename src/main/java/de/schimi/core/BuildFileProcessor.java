package de.schimi.core;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for processing build files of specific types (Gradle, Maven, etc.).
 */
public interface BuildFileProcessor {
    
    /**
     * Get the file pattern this processor handles (e.g., "build.gradle", "pom.xml").
     * @return the filename pattern
     */
    String getFilePattern();
    
    /**
     * Find all build files of this type in the current directory and subdirectories.
     * @return list of paths to build files
     */
    List<Path> findBuildFiles();
    
    /**
     * Update dependencies in the specified build file.
     * @param buildFile path to the build file
     */
    void updateBuildFile(Path buildFile);
    
    /**
     * Get the build system name this processor handles.
     * @return build system name (e.g., "Gradle", "Maven")
     */
    String getBuildSystemName();
}