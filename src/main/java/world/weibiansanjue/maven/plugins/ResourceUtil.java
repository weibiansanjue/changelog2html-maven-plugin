package world.weibiansanjue.maven.plugins;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

public class ResourceUtil {

    private ResourceUtil() {

    }

    public static String getString(String filename) throws IOException {
        String jarPath = ResourceUtil.class.getClassLoader()
                                     .getResource("world")
                                     .getPath()
                                     .replace("file:", "")
                                     .replace("!/world", "");


        JarFile jarFile = new JarFile(jarPath);
        InputStream is = jarFile.getInputStream(jarFile.getEntry(filename));
        return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
}
