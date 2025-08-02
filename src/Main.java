import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

    }

    // returns the path to the server jar file
    private static String getServerJar(String client, String version) {

        // url: https://mcutils.com/api/server-jars/{client}/{version}/download
        String url = "https://mcutils.com/api/server-jars/" + client + "/" + version + "/download";
        String jarName = client + "-" + version + ".jar";
        String jarPath = "server-jars/" + jarName;

        try {

            Files.createDirectories(Paths.get("server-jars")); // make sure the directory exists
            InputStream in = URI.create(url).toURL().openStream(); // open the input stream from the URL
            Files.copy(in, Paths.get(jarPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING); // copy the input stream to the file; replace if it exists already
            return jarPath; // return the path to the jar file

        } catch (Exception e) {

            System.err.println("Error downloading server jar: " + e.getMessage()); // output error message
            return null;

        }
    }
}
