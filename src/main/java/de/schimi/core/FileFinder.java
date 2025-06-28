package de.schimi.core;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for finding build files in the file system.
 */
public interface FileFinder {
    
    /**
     * Find all files matching the given pattern.
     * @param filePattern the file pattern to search for
     * @return list of paths to matching files
     */
    List<Path> findFiles(String filePattern);
}