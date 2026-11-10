package android.content.res;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Virtual Robot's shim for Android's AssetManager.
 *
 * On the robot, files in TeamCode/src/main/assets are packaged into the APK under
 * "assets/". The simulator build copies that same folder onto its classpath under
 * "assets/" (see syncSharedAssets in Controller/build.gradle), so {@link #open(String)}
 * reads the same files team code reads on the Control Hub.
 */
public final class AssetManager implements AutoCloseable {

    public static final int ACCESS_UNKNOWN = 0;
    public static final int ACCESS_RANDOM = 1;
    public static final int ACCESS_STREAMING = 2;
    public static final int ACCESS_BUFFER = 3;

    private static final String ROOT = "assets/";

    private static final AssetManager INSTANCE = new AssetManager();

    /** The simulator has one asset store, shared by every Context. */
    public static AssetManager getSystem() {
        return INSTANCE;
    }

    private AssetManager() {
    }

    private static ClassLoader loader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl != null ? cl : AssetManager.class.getClassLoader();
    }

    private static String normalize(String fileName) {
        String name = fileName.replace('\\', '/');
        while (name.startsWith("/")) name = name.substring(1);
        return name;
    }

    public InputStream open(String fileName) throws IOException {
        return open(fileName, ACCESS_STREAMING);
    }

    public InputStream open(String fileName, int accessMode) throws IOException {
        if (fileName == null) throw new NullPointerException("fileName");
        InputStream in = loader().getResourceAsStream(ROOT + normalize(fileName));
        if (in == null) {
            throw new FileNotFoundException(fileName
                    + " (no such asset; files go in TeamCode/src/main/assets)");
        }
        return in;
    }

    /** Lists the entries directly inside an asset directory ("" for the root). */
    public String[] list(String path) throws IOException {
        String dir = normalize(path == null ? "" : path);
        if (!dir.isEmpty() && !dir.endsWith("/")) dir += "/";
        String prefix = ROOT + dir;

        List<String> names = new ArrayList<>();
        Enumeration<URL> roots = loader().getResources(prefix.isEmpty() ? ROOT : prefix);
        while (roots.hasMoreElements()) {
            URL url = roots.nextElement();
            if ("file".equals(url.getProtocol())) {
                try {
                    String[] children = new File(url.toURI()).list();
                    if (children != null) {
                        for (String c : children) if (!names.contains(c)) names.add(c);
                    }
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            } else if ("jar".equals(url.getProtocol())) {
                String spec = url.getPath();
                String jarPath = spec.substring("file:".length(), spec.indexOf('!'));
                try (JarFile jar = new JarFile(java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String entry = entries.nextElement().getName();
                        if (!entry.startsWith(prefix) || entry.equals(prefix)) continue;
                        String rest = entry.substring(prefix.length());
                        int slash = rest.indexOf('/');
                        String child = slash == -1 ? rest : rest.substring(0, slash);
                        if (!child.isEmpty() && !names.contains(child)) names.add(child);
                    }
                }
            }
        }
        String[] result = names.toArray(new String[0]);
        Arrays.sort(result);
        return result;
    }

    @Override
    public void close() {
    }
}
