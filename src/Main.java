import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    // global fields
    private static String client;
    private static String version;
    private static String profileName;
    private static String serverJarPath;
    private static String serverJarName;
    private static String runBatPath;

    public static void main(String[] args) {

        Scanner console = new Scanner(System.in);

        client = getClient(console);
        version = getVersion(console, client);

        System.out.println(client + " " + version);

    }

    //private static String getServerJar(String client, String version)
    private static String getClient(Scanner console) {

        System.out.println("-- Step 1: Client version --");
        System.out.println("Please type the server client you would like to use");
        System.out.println("\t[1] Paper: Recommended default, has support for plugins");
        System.out.println("\t[2] Forge: Allows Forge modes (no native plugin support)");
        System.out.println("\t[3] Fabric: Allows Fabric modes (no native plugin support)");
        System.out.println("\t[4] Vanilla: Plain Minecraft. It is advised to use Paper instead (no native plugin support)");

        String output = "";

        while (output.isEmpty()) {

            System.out.print("\nClient: ");
            String input = console.next();

            output = switch (input) {

                case "1" -> "paper";
                case "2" -> "forge";
                case "3" -> "fabric";
                case "4" -> "vanilla";
                default -> "";

            };

            if (output.isEmpty())
                System.out.println("Invalid input, please try again");

        }

        return output;

    }

    private static String getVersion(Scanner console, String client) {

        System.out.println("-- Step 2: Client version --");
        System.out.println("Please type the name of the minecraft version you want to use");
        System.out.println("(Visit https://mcutils.com/server-jars to see a list of versions supported on each client)");
        System.out.println("\tExample: 1.8.9, 1.16.3, 1.20.1...");

        String output = "";

        while (output.isEmpty()) {

            System.out.print("\nVersion: ");
            String input = console.next();

            if (testVersion(client, input))
                output = input;
            else
                System.out.println("Invalid input, please try again");

        }

        return output;

    }


    private static boolean testVersion(String client, String version) {

        // url: https://mcutils.com/api/server-jars/{client}/{version}/exists
        String url = "https://mcutils.com/api/server-jars/" + client + "/" + version + "/download";
        System.out.print(url);

        try {

            URL url1 = URI.create(url).toURL(); // create a URL object from the string
            HttpURLConnection huc = (HttpURLConnection) url1.openConnection(); // open a connection to the URL
            huc.setRequestMethod("GET"); // set the request method to GET
            huc.connect(); // connect to the URL

            int responseCode = huc.getResponseCode(); // get the response code
            System.out.println("Response Code: " + responseCode); // output the response code

            if (responseCode == 200) { // if the response code is 200, the version exists

                System.out.println("Version " + version + " for client " + client + " exists.");
                return true; // return true if the version exists

            } else {

                System.out.println("Version " + version + " for client " + client + " does not exist.");
                return false; // return false if the version does not exist

            }
        } catch (Exception e) {

            System.err.println("Error checking version: " + e.getMessage()); // output error message
            return false;

        }
    }

    // returns the path to the server jar file
    private static String getServerJar(String client, String version, String profileName) {

        // url: https://mcutils.com/api/server-jars/{client}/{version}/download
        String url = "https://mcutils.com/api/server-jars/" + client + "/" + version + "/download";
        serverJarName = client + "-" + version + ".jar";
        serverJarPath = "server-jars/" + serverJarPath;

        try {

            Files.createDirectories(Paths.get(profileName)); // make sure the directory exists
            InputStream in = URI.create(url).toURL().openStream(); // open the input stream from the URL
            Files.copy(in, Paths.get(serverJarPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING); // copy the input stream to the file; replace if it exists already
            return serverJarPath; // return the path to the jar file

        } catch (Exception e) {

            System.err.println("Error downloading server jar: " + e.getMessage()); // output error message
            return null;

        }
    }

    private static void createRunBat(String jarName) {

        // create a run.bat file to start the server
        String runBatContent = "java -Xms4096M -Xmx4096M --add-modules=jdk.incubator.vector -XX:+UseG1GC " +
                "-XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions " +
                "-XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 " +
                "-XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 " +
                "-XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem " +
                "-XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true " +
                "-XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 " +
                "-jar " + jarName;

        try {

            Files.write(Paths.get("run.bat"), runBatContent.getBytes()); // write the content to run.bat

        } catch (Exception e) {

            System.err.println("Error creating run.bat: " + e.getMessage()); // output error message

        }
    }
}
