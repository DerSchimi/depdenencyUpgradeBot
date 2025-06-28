package de.schimi.maven;

import de.schimi.core.BuildFileProcessor;
import de.schimi.core.DefaultFileFinder;
import de.schimi.core.FileFinder;
import de.schimi.core.VersionChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Build file processor for Maven pom.xml files.
 */
public class MavenBuildFileProcessor implements BuildFileProcessor {
    
    private static final Logger LOG = LoggerFactory.getLogger(MavenBuildFileProcessor.class);
    
    private final VersionChecker versionChecker;
    private final FileFinder fileFinder;
    
    public MavenBuildFileProcessor(VersionChecker versionChecker) {
        this.versionChecker = versionChecker;
        this.fileFinder = new DefaultFileFinder();
    }
    
    @Override
    public String getFilePattern() {
        return "pom.xml";
    }
    
    @Override
    public List<Path> findBuildFiles() {
        List<Path> pomFiles = fileFinder.findFiles(getFilePattern());
        LOG.info("Found {} pom.xml files.", pomFiles.size());
        return pomFiles;
    }
    
    @Override
    public void updateBuildFile(Path pomFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomFile.toFile());
            doc.getDocumentElement().normalize();
            
            boolean hasUpdates = false;
            
            // Process dependencies in <dependencies> section
            hasUpdates |= processDependencies(doc.getElementsByTagName("dependency"));
            
            // Process dependencies in <dependencyManagement> section
            NodeList dependencyManagement = doc.getElementsByTagName("dependencyManagement");
            if (dependencyManagement.getLength() > 0) {
                Element dmElement = (Element) dependencyManagement.item(0);
                NodeList dependencies = dmElement.getElementsByTagName("dependency");
                hasUpdates |= processDependencies(dependencies);
            }
            
            if (hasUpdates) {
                // Write updated pom.xml
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                
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
    
    private boolean processDependencies(NodeList dependencies) {
        boolean hasUpdates = false;
        
        for (int i = 0; i < dependencies.getLength(); i++) {
            Node dependency = dependencies.item(i);
            if (dependency.getNodeType() == Node.ELEMENT_NODE) {
                Element depElement = (Element) dependency;
                
                String groupId = getElementValue(depElement, "groupId");
                String artifactId = getElementValue(depElement, "artifactId");
                String currentVersion = getElementValue(depElement, "version");
                
                if (groupId != null && artifactId != null && currentVersion != null && !currentVersion.contains("${")) {
                    String updatedVersion = versionChecker.findNewerMinorVersion(groupId, artifactId, currentVersion);
                    if (updatedVersion != null && !updatedVersion.equals(currentVersion)) {
                        LOG.info("Updating {}:{} from {} to {}", groupId, artifactId, currentVersion, updatedVersion);
                        setElementValue(depElement, "version", updatedVersion);
                        hasUpdates = true;
                    } else {
                        LOG.info("Skipping {}:{} as no newer minor version found or already up-to-date.", groupId, artifactId);
                    }
                }
            }
        }
        
        return hasUpdates;
    }
    
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.getFirstChild() != null) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return null;
    }
    
    private void setElementValue(Element parent, String tagName, String value) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.getFirstChild() != null) {
                node.getFirstChild().setNodeValue(value);
            }
        }
    }
    
    @Override
    public String getBuildSystemName() {
        return "Maven";
    }
}