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






    @Deprecated
    public static class Resources {
        public enum ModelVariants {
            en, de
        }

        record ModelPaths(String tk, String lm, String pos, String sd){

        }

        @Deprecated
        public static LemmatizerModel getLemmatizer(ModelVariants variant) {
            String resourceName = switch (variant) {
                // FYI: https://opennlp.apache.org/maven-dependency.html, https://repo1.maven.org/maven2/org/apache/opennlp/
                // also, why this path? well, it expands to "CLASS_PATH/PATH"
                case en -> "/opennlp-en-ud-ewt-lemmas-1.3-2.5.4.bin";
                case de -> "/opennlp-de-ud-hdt-lemmas-1.2-2.5.4.bin";
                default -> throw new IllegalArgumentException("Unsupported language variant: " + variant);
            };
            try (InputStream is = Resources.class.getResourceAsStream(resourceName)) {
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
