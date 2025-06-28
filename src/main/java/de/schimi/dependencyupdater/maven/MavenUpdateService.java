package de.schimi.dependencyupdater.maven;

import de.schimi.dependencyupdater.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

/**
 * Service for updating Maven pom.xml files
 */
public class MavenUpdateService implements DependencyUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(MavenUpdateService.class);
    
    private final VersionService versionService;
    
    public MavenUpdateService() {
        this.versionService = new VersionService();
    }
    
    public MavenUpdateService(VersionService versionService) {
        this.versionService = versionService;
    }

    @Override
    public void updateAllBuildFiles() {
        try {
            var pomFiles = Files.walk(Paths.get("."))
                .filter(p -> p.getFileName().toString().equals(getProjectType().getBuildFileName()))
                .collect(Collectors.toList());
            LOG.info("Found {} pom.xml files.", pomFiles.size());

            for (Path pomFile : pomFiles) {
                updateBuildFile(pomFile);
            }
        } catch (Exception e) {
            LOG.error("Error while searching for pom.xml files: {}", e.getMessage());
        }
    }

    @Override
    public void updateBuildFile(Path pomFile) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomFile.toFile());
            
            boolean hasUpdates = false;
            
            // Update dependencies in <dependencies> section
            NodeList dependencies = document.getElementsByTagName("dependency");
            for (int i = 0; i < dependencies.getLength(); i++) {
                Element dependency = (Element) dependencies.item(i);
                if (updateDependencyElement(dependency)) {
                    hasUpdates = true;
                }
            }
            
            if (hasUpdates) {
                // Save updated pom.xml
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                
                DOMSource source = new DOMSource(document);
                String newFileName = pomFile.getFileName().toString() + ".updated";
                StreamResult result = new StreamResult(new File(newFileName));
                transformer.transform(source, result);
                
                LOG.info("Updated file {} and saved as {}", pomFile, newFileName);
            } else {
                LOG.info("No updates needed for {}", pomFile);
            }
            
        } catch (Exception e) {
            LOG.error("Error updating file {}: {}", pomFile, e.getMessage());
        }
    }
    
    private boolean updateDependencyElement(Element dependency) {
        try {
            NodeList groupIdNodes = dependency.getElementsByTagName("groupId");
            NodeList artifactIdNodes = dependency.getElementsByTagName("artifactId");
            NodeList versionNodes = dependency.getElementsByTagName("version");
            
            if (groupIdNodes.getLength() == 0 || artifactIdNodes.getLength() == 0 || versionNodes.getLength() == 0) {
                return false; // Skip if missing required elements
            }
            
            String groupId = groupIdNodes.item(0).getTextContent().trim();
            String artifactId = artifactIdNodes.item(0).getTextContent().trim();
            String currentVersion = versionNodes.item(0).getTextContent().trim();
            
            // Skip if version contains variables like ${project.version}
            if (currentVersion.contains("${")) {
                LOG.debug("Skipping {}:{} as version contains variables: {}", groupId, artifactId, currentVersion);
                return false;
            }
            
            String updatedVersion = versionService.findNewerMinorVersion(groupId, artifactId, currentVersion);
            if (updatedVersion != null && !updatedVersion.equals(currentVersion)) {
                LOG.info("Updating {}:{} from {} to {}", groupId, artifactId, currentVersion, updatedVersion);
                versionNodes.item(0).setTextContent(updatedVersion);
                return true;
            } else {
                LOG.info("Skipping {}:{} as no newer minor version found or already up-to-date.", groupId, artifactId);
                return false;
            }
        } catch (Exception e) {
            LOG.error("Error processing dependency element: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ProjectType getProjectType() {
        return ProjectType.MAVEN;
    }
}