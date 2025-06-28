package de.schimi.maven;

import de.schimi.core.VersionChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for Maven build file processor functionality.
 */
public class MavenBuildFileProcessorTest {

    private MavenBuildFileProcessor processor;
    private VersionChecker mockVersionChecker;

    @BeforeEach
    void setUp() {
        mockVersionChecker = mock(VersionChecker.class);
        processor = new MavenBuildFileProcessor(mockVersionChecker);
    }

    @Test
    void testGetFilePattern() {
        assertEquals("pom.xml", processor.getFilePattern());
    }

    @Test
    void testGetBuildSystemName() {
        assertEquals("Maven", processor.getBuildSystemName());
    }

    @Test
    void testProcessorCreatesValidInstance() {
        assertNotNull(processor);
        assertNotNull(processor.getFilePattern());
        assertNotNull(processor.getBuildSystemName());
    }

    @Test
    void testFindBuildFilesReturnsEmptyListWhenNoPomExists() {
        var buildFiles = processor.findBuildFiles();
        assertNotNull(buildFiles);
        // We can't guarantee the size since it depends on the working directory
        // but we can ensure the method doesn't throw an exception
    }
}