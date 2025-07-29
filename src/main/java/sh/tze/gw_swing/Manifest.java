package sh.tze.gw_swing;

import opennlp.tools.lemmatizer.LemmatizerModel;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class Manifest {
    public static final String DISPLAY_NAME = "Demo Application";
    public static final String UI_STYLE = "Nimbus";
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final int ON_WINDOW_CLOSE = JFrame.EXIT_ON_CLOSE;

    public static class Resources {
        public enum ModelVariants {
            en, de
        }

        public LemmatizerModel getLemmatizer(ModelVariants variant) {
            String resourceName = switch (variant) {
                // FYI: https://opennlp.apache.org/maven-dependency.html, https://repo1.maven.org/maven2/org/apache/opennlp/
                case en -> "/opennlp-en-ud-ewt-lemmas-1.3-2.5.0.bin";
                case de -> "/opennlp-de-ud-hdt-lemmas-1.2-2.5.0.bin";
                default -> throw new IllegalArgumentException("Unsupported language variant: " + variant);
            };
            try (InputStream is = getClass().getResourceAsStream(resourceName)) {
                if (is == null) {
                    throw new IllegalArgumentException("Resource not found: " + resourceName);
                }
                return new LemmatizerModel(is);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load lemmatizer model: " + resourceName, e);
            }
        }
    }

}
