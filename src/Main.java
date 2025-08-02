import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final char[] FORBIDDEN_FILE_CHARS = {'<', '>', ':', '"', '/', '\\', '|', '?', '*'};
    private static final String[] FORBIDDEN_FILE_NAMES = {"CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM0",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "LPT0"};
    private static final int MEGABIT_FACTOR = 1024;

    public static void main(String[] args) {

        Scanner console = new Scanner(System.in);

        String serverFolderName = getServerFolderName(console);
        String client = getClient(console);
        String version = getVersion(console, client);
        int ramAlloc = getRamAlloc(console);

        String jarName = client + "-" + version + ".jar";

        String jarPath = getServerJar(jarName, client, version, serverFolderName);
        createBat(jarName, client, serverFolderName, ramAlloc);
        runBat(serverFolderName);

        if (!waitForEULA(serverFolderName)) {

            System.out.println("EULA file was not created in time. Please try again later.");
            System.exit(1); // exit the program if the EULA file was not created

        }

        acceptEula(console, serverFolderName);
        setMOTD("goated server", serverFolderName);

    }

    private static String getServerFolderName(Scanner console) {

        System.out.println("-- Step 1: Server name -- ");
        System.out.println("What would you like to name your server? (Name of the folder to be created, does not affect actual server)");

        String output = "";

        while (output.isEmpty()) {

            System.out.print("\nFolder name: ");
            String input = console.nextLine();

            if (isFileNameValid(input))
                output = input;
            else
                System.out.println("Your server name (folder name) contains one or more invalid characters, or the name is not allowed by Windows. Please try again");

        }

        return output;

    }

    private static boolean isFileNameValid(String name) {

        // check forbidden chars for inclusion
        for (int i = 0; i < name.length(); i++)
            for (char c : FORBIDDEN_FILE_CHARS)
                if (name.toLowerCase().charAt(i) == c)
                    return false;

        // check forbidden names for insensitive equality
        for (String str : FORBIDDEN_FILE_NAMES)
            if (name.equalsIgnoreCase(str))
                return false;

        return true;

    }

    //private static String getServerJar(String client, String version)
    private static String getClient(Scanner console) {

        System.out.println("-- Step 2: Client version --");
        System.out.println("Please type the server client you would like to use");
        System.out.println("\t[1] Paper: Recommended default, has support for plugins");
        System.out.println("\t[2] Forge: Allows Forge modes (no native plugin support)");
        System.out.println("\t[3] Fabric: Allows Fabric modes (no native plugin support)");
        System.out.println("\t[4] Vanilla: Plain Minecraft (no native plugin support). It is advised to use Paper instead: it is the same thing but with significant performance improvements.");

        String output = "";

        while (output.isEmpty()) {

            System.out.print("\nClient: ");
            String input = console.nextLine();

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

        System.out.println("-- Step 3: Client version --");
        System.out.println("Please type the name of the minecraft version you want to use");
        System.out.println("(Visit https://mcutils.com/server-jars to see a list of versions supported on each client)");
        System.out.println("\tExample: 1.8.9, 1.16.3, 1.20.1...");

        String output = "";

        while (output.isEmpty()) {

            System.out.print("\nVersion: ");
            String input = console.nextLine();

            if (testVersion(client, input))
                output = input;
            else
                System.out.println("Invalid input, please try again");

        }

        return output;

    }

    private static int getRamAlloc(Scanner console) {

        System.out.println("-- Step 4: Ram allocation --");
        System.out.println("How many gigabytes of RAM would you like to reserve for your server?");
        System.out.println("\tIdeally provide at least 1GB.");
        System.out.println("\t4GB is enough for most servers. Ensure the server does not use up all system resources.");
        System.out.println("\tAllocate more memory for servers with many mods or players. Increase/decrease later as needed.");

        int output = 0;

        while (output == 0) {

            System.out.print("\nMemory allocation (in gigabytes): ");
            String input = console.nextLine();
            float memory = 0;

            try {

                memory = Float.parseFloat(input); // parse the input to a float

            } catch (NumberFormatException e) {

                System.out.println("Invalid input, please try again");
                continue; // if the input is not a valid float, ask for input again

            }

            // cast float to int and convert gigabytes to megabits
            output = (int) memory * MEGABIT_FACTOR;

            if (output == 0)
                System.out.println("Invalid input, please try again");

        }

        return output;

    }


    private static boolean testVersion(String client, String version) {

        // url: https://mcutils.com/api/server-jars/{client}/{version}/exists
        String url = "https://mcutils.com/api/server-jars/" + client + "/" + version + "/download";
        System.out.print(url);

        try {

            URL testURL = URI.create(url).toURL(); // create a URL object from the string
            HttpURLConnection huc = (HttpURLConnection) testURL.openConnection(); // open a connection to the URL
            huc.setRequestMethod("GET"); // set the request method to GET
            huc.connect(); // connect to the URL

            int responseCode = huc.getResponseCode(); // get the response code

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
    private static String getServerJar(String jarName, String client, String version, String serverFolderName) {

        // url: https://mcutils.com/api/server-jars/{client}/{version}/download
        String url = "https://mcutils.com/api/server-jars/" + client + "/" + version + "/download";
        String jarPath = serverFolderName + "/" + jarName;

        try {

            Files.createDirectories(Paths.get(serverFolderName)); // make sure the directory exists
            InputStream in = URI.create(url).toURL().openStream(); // open the input stream from the URL
            Files.copy(in, Paths.get(jarPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING); // copy the input stream to the file; replace if it exists already
            return jarPath; // return the path to the jar file

        } catch (Exception e) {

            System.err.println("Error downloading server jar: " + e.getMessage()); // output error message
            return null;

        }
    }

    private static void createBat(String jarName, String client, String serverFolderName, int ramAlloc) {

        // alloc greater than 12gb has different flags
        // credit: https://flags.sh/
        boolean isLargeAlloc = ramAlloc >= 12 * MEGABIT_FACTOR;
        System.out.println(ramAlloc + "MB allocated to the server");

        // create a run.bat file to start the server
        String runBatContent = "java -Xms" + ramAlloc + "M -Xmx" + ramAlloc + "M --add-modules=jdk.incubator.vector -XX:+UseG1GC " +
                "-XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions " +
                "-XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 " +
                "-XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 " +
                "-XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem " +
                "-XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true " +
                "-XX:G1NewSizePercent=" + (isLargeAlloc ? 40 : 30) + " -XX:G1MaxNewSizePercent=" + (isLargeAlloc ? 50 : 40) + " -XX:G1HeapRegionSize=" + (isLargeAlloc ? 16 : 8)
                + "M -XX:G1ReservePercent=" + (isLargeAlloc ? 15 : 20) + " -jar " + jarName + (client.equalsIgnoreCase("forge") ? " --installServer" : "");

        try {

            Files.write(Paths.get(serverFolderName, "run.bat"), runBatContent.getBytes()); // write the content to run.bat

        } catch (Exception e) {

            System.err.println("Error creating run.bat: " + e.getMessage()); // output error message

        }
    }

    private static void runBat(String serverFolderName) {

        // run the run.bat file
        try {

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "run.bat");
            pb.directory(Paths.get(serverFolderName).toFile()); // set the working directory to the server folder so the JAR file can be found
            Process process = pb.start(); // start the process

            int exitCode = process.waitFor(); // wait for the process to finish
            System.out.println("Exit code: " + exitCode); // output the exit code

        } catch (IOException e) {

            System.err.println("Error running run.bat: " + e.getMessage()); // output error message

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt(); // restore the interrupted status
            System.err.println("Process was interrupted: " + e.getMessage()); // output error message

        }
    }

    private static boolean waitForEULA(String serverFolderName) {

        System.out.println("-- Step 5: EULA --");
        System.out.println("Waiting for the EULA to be created by the server...");

        int timeoutSeconds = 60;
        int waited = 0;

        while (waited < timeoutSeconds) {

            try {

                if (Files.exists(Paths.get(serverFolderName, "eula.txt"))) {

                    System.out.println("EULA file found. You can now accept the EULA.");
                    return true; // EULA file exists, exit the loop and return true

                }

                Thread.sleep(1000); // wait for 1 second before checking again
                waited++;

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt(); // restore the interrupted status
                System.err.println("Waiting for EULA was interrupted: " + e.getMessage());
                return false; // return false if interrupted

            }
        }

        return false;

    }

    private static void acceptEula(Scanner console, String serverFolderName) {

        System.out.println("Please type \"yes\" to agree to Minecraft's EULA (this is a required step to run the server");
        System.out.println("View the EULA here: https://aka.ms/MinecraftEULA");

        System.out.print("Accept EULA: ");

        while (!console.nextLine().equalsIgnoreCase("yes"))
            System.out.println("You did not accept the EULA. Please type \"yes\" to accept it");

        // at this point, the user has accepted the EULA
        // access the eula.txt file and set eula=true

        try {

            Path path = Paths.get(serverFolderName, "eula.txt");
            List<String> lines = Files.readAllLines(path);

            // find the line that starts with "eula=" and set it to "eula=true"
            for (int i = 0; i < lines.size(); i++)
                if (lines.get(i).trim().startsWith("eula="))
                    lines.set(i, "eula=true");

            Files.write(path, lines); // write the modified lines back to the file
            System.out.println("EULA accepted.");

            printCompletedMessage();

        } catch (IOException e) {

            System.err.println("Error writing to eula.txt: " + e.getMessage()); // output error message

        }
    }

    private static void setMOTD(String motd, String serverFolderName) {

        // go to server.properties file and find the line that starts with "motd=", set it to the provided motd if it exists or create it if it does not exist
        try {

            Path path = Paths.get(serverFolderName, "server.properties");
            List<String> lines = Files.readAllLines(path);

            boolean motdSet = false;

            for (int i = 0; i < lines.size(); i++) {

                if (lines.get(i).trim().startsWith("motd=")) {

                    lines.set(i, "motd=" + motd);
                    motdSet = true;
                    break;

                }
            }

            if (!motdSet)
                lines.add("motd=" + motd); // add the motd line if it does not exist

            Files.write(path, lines); // write the modified lines back to the file
            System.out.println("MOTD set to: " + motd);

        } catch (IOException e) {

            System.err.println("Error writing to server.properties: " + e.getMessage());

        }
    }

    private static void printCompletedMessage() {

        System.out.println("\nYour server has been created successfully!\n");
        System.out.println("A GUI will open where you can monitor your server and type commands.");
        System.out.println("Type \"/op [username]\" to grant a user admin privileges (ability to type commands in game instead of console)");
        System.out.println("To allow users on other networks to join your server, you will likely need to port forward your router. Be careful when doing this, it is advanced and requires changing router settings.");
        System.out.println("\tCheck out this guide to port forwarding: https://www.wikihow.com/Portforward-Minecraft ");
        System.out.println("In your server folder, check out server.properties to edit things like server render distance, seed, and description\n");

        System.out.println("Be careful when handing out your IP.");

        try {

            URL url = new URL("https://api.ipify.org"); // you can also use "https://checkip.amazonaws.com"
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String publicIP = reader.readLine();
            reader.close();

            System.out.println("Here is your public IP (use this to connect to your server): " + publicIP + ":25565");
            System.out.println("IP without port: " + publicIP);

        } catch (Exception e) {

            System.err.println("Error fetching public IP: " + e.getMessage());
            System.out.println("You can find your public IP by searching \"what is my ip\" in your browser.");

        }
    }
}
