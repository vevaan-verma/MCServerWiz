import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Main {

    private static final char[] FORBIDDEN_FILE_CHARS = {'<', '>', ':', '"', '/', '\\', '|', '?', '*'};
    private static final String[] FORBIDDEN_FILE_NAMES = {"CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM0",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "LPT0"};
    private static final int BINARY_FACTOR = 1024;

    public static void main(String[] args) {

        // MCServerWiz
        System.out.println("\n********************************************");
        System.out.println("Ⅰ         Welcome to MCServerWiz!         Ⅰ");
        System.out.println("********************************************");

        System.out.println("\nAll files are from https://mcutils.com/");
        System.out.println("Your PUBLIC IP is accessed at https://api.ipify.org");
        System.out.println("\tWe are not affiliated with these sites.\n");

        System.out.println("Please do not type unless prompted.\n");

        Scanner console = new Scanner(System.in);

        String osStr = System.getProperty("os.name").toLowerCase();
        OperatingSystem os;

        if (osStr.contains("win"))
            os = OperatingSystem.Windows;
        else if (osStr.contains("mac"))
            os = OperatingSystem.MacOS;
        else if (osStr.contains("nix") || osStr.contains("nux") || osStr.contains("aix"))
            os = OperatingSystem.Linux;
        else
            os = OperatingSystem.Other; // if the operating system is not recognized, set it to Other

        if (os == OperatingSystem.Other) {

            System.out.println("Your operating system is not recognized: " + osStr + ". Some features may not work as expected.");
            System.out.println("You will have to run the run.sh or run.bat file manually.");
            System.out.println("Press enter to continue...");
            console.nextLine();

        }

        String serverFolderName = getServerFolderName(console);
        Client client = getClient(console);
        String version = getVersion(console, client);
        int ramAlloc = getRamAlloc(console);

        String jarName = getServerJar(client, version, serverFolderName);
        createRunFiles(jarName, client, serverFolderName, ramAlloc, os);

        // run the server based on the operating system
        if (os == OperatingSystem.Windows) {

            runBat(serverFolderName);

        } else if (os == OperatingSystem.Linux || os == OperatingSystem.MacOS) {

            runSh(serverFolderName);

        } else {

            System.out.println("You will have to run the run.sh or run.bat file manually.");
            System.out.println("Press enter when you have done so. If you continue without running the server, the program will not work as expected.");
            console.nextLine();

        }

        if (client == Client.forge) {

            printCompletedMessageForge();
            return;

        }

        if (!waitForEULA(serverFolderName)) {

            System.out.println("EULA file was not created in time. Please try again later.");
            System.exit(1); // exit the program if the EULA file was not created

        }

        acceptEula(console, serverFolderName);
        setMOTD(getMOTD(console), serverFolderName);

        printCompletedMessage();

        turnOnServerPrompt(console, serverFolderName, os);

    }

    // region Step 1: Server Name
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
                System.out.println("Your folder name contains one or more invalid chars, or the name is not allowed by Windows. Please try again");

        }

        System.out.println("\n** Your server's folder will be named \"" + output + "\" **\n");
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
    // endregion

    // region Step 2: Client
    private static Client getClient(Scanner console) {

        System.out.println("-- Step 2: Client version --");
        System.out.println("Please type the server client you would like to use");
        System.out.println("\t[1] Paper: Recommended default, has support for plugins");
        System.out.println("\t[2] Fabric: Allows Fabric mods (no native plugin support)");
        System.out.println("\t[3] Forge: Allows Forge mods. REQUIRES MANUAL SETUP! (no native plugin support)");
        System.out.println("\t[4] Vanilla: Plain Minecraft (no native plugin support). \n\t\t(It is advised to use Paper instead of Vanilla for performance.)");

        Client client = null; // initialize client to null

        while (client == null) {

            System.out.print("\nClient: ");
            String input = console.nextLine();

            client = switch (input) {

                case "1" -> Client.paper;
                case "2" -> Client.fabric;
                case "3" -> Client.forge;
                case "4" -> Client.vanilla;
                default -> null;

            };

            if (client == null)
                System.out.println("Invalid input, please try again");

        }

        System.out.println("\n** Your server will run on " + client.name() + " **\n");
        return client;

    }
    // endregion

    // region Step 3: Version
    private static String getVersion(Scanner console, Client client) {

        System.out.println("-- Step 3: Client version --");
        System.out.println("Please type the name of the minecraft version you want to use");
        System.out.println("(Visit https://mcutils.com/server-jars to see a list of versions supported on each client)");
        System.out.println("\tExample: 1.8.9, 1.16.3, 1.20.1...");

        String output = "";

        while (output.isEmpty()) {

            System.out.print("\nVersion: ");
            String input = console.nextLine();
            String version = input;

            // ignore everything after the first space
            if (input.contains(" "))
                version = input.substring(0, input.indexOf(" "));

            if (testVersion(client, version))
                output = version;
            else
                System.out.println("Invalid input, please try again");

        }

        System.out.println("\n** Your server will run on Minecraft " + output + " **\n");
        return output;

    }
    // endregion

    // region Step 4: Ram Allocation
    private static int getRamAlloc(Scanner console) {

        System.out.println("-- Step 4: Ram allocation --");
        System.out.println("How many gigabytes of RAM would you like to reserve for your server?");
        System.out.println("\tIdeally provide at least 1GB.");
        System.out.println("\t4GB is enough for most servers. Ensure the server does not use up all system resources.");
        System.out.println("\tAllocate more memory for servers with many mods or players. Increase/decrease later as needed.");

        int output = 0;
        float memory = 0;

        while (output == 0) {

            System.out.print("\nMemory allocation (in gigabytes): ");
            String input = console.nextLine();

            try {

                memory = Float.parseFloat(input); // parse the input to a float

            } catch (NumberFormatException e) {

                System.out.println("Invalid input, please try again");
                continue; // if the input is not a valid float, ask for input again

            }

            // cast float to int and convert gigabytes to megabytes (binary)
            output = (int) (memory * BINARY_FACTOR);

            if (output == 0)
                System.out.println("Invalid input, please try again");
            else if (output < 0.5 * BINARY_FACTOR) { // if less than half a GB, do not accept value to avoid fatal error

                System.out.println("Please provide at least 0.5 gigabytes of memory.");
                output = 0;

            }
        }

        System.out.println("\n** Your server will use " + memory + " gigabytes of ram when active **");
        System.out.println("\t\t(" + output + " binary megabytes)\n");
        return output;

    }
    // endregion

    //region Step 5: EULA
    private static boolean waitForEULA(String serverFolderName) {

        System.out.println("\n-- Step 5: EULA --");
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

        System.out.print("\nAccept EULA: ");

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
            System.out.println("\nEULA accepted.\n");

        } catch (IOException e) {

            System.err.println("Error writing to eula.txt: " + e.getMessage()); // output error message

        }
    }
    // endregion

    // region Step 6: MOTD
    private static String getMOTD(Scanner console) {

        System.out.println("-- Step 6: Server description (MOTD) --");
        System.out.println("You may set a MOTD description that players will see in their server browser");
        System.out.println("Visit this link to learn how to custom format your MOTD: https://minecraft.fandom.com/wiki/Formatting_codes#Use_in_server.properties_and_pack.mcmeta");
        System.out.println("Press enter to skip a line. ");
        System.out.println("Type \"DONE\" on a new line when you're done typing.");

        System.out.println("\nType your MOTD below:");

        String input = "";
        StringBuilder output = new StringBuilder();

        while (!input.equalsIgnoreCase("DONE")) {

            input = console.nextLine();

            if (!input.equalsIgnoreCase("DONE"))
                output.append(input).append("\\n");
            else if (!output.isEmpty())
                output = new StringBuilder(output.substring(0, output.length() - 2)); //remove the final extra \n

        }

        return output.toString();

    }
    // endregion

    //region Turn on server prompt

    private static void turnOnServerPrompt(Scanner console, String serverFolderName, OperatingSystem os) {

        System.out.println("^^ Please read all the above text before proceeding ^^");
        System.out.print("Would you like to turn your server on now? (y/n): ");

        String input = console.nextLine();

        if (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {

            // run the server based on the operating system
            if (os == OperatingSystem.Windows)
                runBat(serverFolderName);
            else if (os == OperatingSystem.Linux || os == OperatingSystem.MacOS)
                runSh(serverFolderName);
            else
                System.out.println("Operating system not recognized: " + os + ". You will have to run the run.sh or run.bat file manually.");

        }
    }

    //endregion

    private static boolean testVersion(Client client, String version) {

        // url: https://mcutils.com/api/server-jars/{client}/{version}/exists
        String url = "https://mcutils.com/api/server-jars/" + client.toString() + "/" + version + "/download";

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

    // returns the name of the jar file
    private static String getServerJar(Client client, String version, String serverFolderName) {

        // url: https://mcutils.com/api/server-jars/{client}/{version}/download
        String url = "https://mcutils.com/api/server-jars/" + client.toString() + "/" + version + "/download";
        String jarName = client + "-" + version + ".jar"; // the name of the jar file to be downloaded

        try {

            Files.createDirectories(Paths.get(serverFolderName)); // make sure the directory exists
            InputStream in = URI.create(url).toURL().openStream(); // open the input stream from the URL
            Files.copy(in, Paths.get(serverFolderName, jarName), StandardCopyOption.REPLACE_EXISTING); // copy the input stream to the file; replace if it exists already
            return jarName; // return the path to the jar file

        } catch (Exception e) {

            System.err.println("Error downloading server jar: " + e.getMessage()); // output error message
            return null;

        }
    }

    private static void createRunFiles(String jarName, Client client, String serverFolderName, int ramAlloc, OperatingSystem os) {

        // alloc greater than 12gb has different flags
        // credit: https://flags.sh/
        boolean isLargeAlloc = ramAlloc >= 12 * BINARY_FACTOR;
        System.out.println(ramAlloc + "MB allocated to the server");

        String baseCommand = "java -Xms" + ramAlloc + "M -Xmx" + ramAlloc + "M --add-modules=jdk.incubator.vector -XX:+UseG1GC " +
                "-XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions " +
                "-XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 " +
                "-XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 " +
                "-XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem " +
                "-XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true " +
                "-XX:G1NewSizePercent=" + (isLargeAlloc ? 40 : 30) + " -XX:G1MaxNewSizePercent=" + (isLargeAlloc ? 50 : 40) + " -XX:G1HeapRegionSize=" + (isLargeAlloc ? 16 : 8)
                + "M -XX:G1ReservePercent=" + (isLargeAlloc ? 15 : 20) + " -jar " + jarName + (client.toString().equalsIgnoreCase("forge") ? " --installServer" : "");

        // Windows run.bat; will close when done
        String runBatContent = "@echo off\n" +
                "echo Starting server...\n" +
                baseCommand + "\n" +
                "if exist eula.txt (\n" +
                "   exit\n" +
                ") else (\n" +
                "   timeout /t 5 >nul\n" +
                "   exit\n" +
                ")";

        // create a run.bat file to start the server
        try {

            Files.write(Paths.get(serverFolderName, "run.bat"), runBatContent.getBytes()); // write the content to run.bat

        } catch (Exception e) {

            System.err.println("Error creating run.bat: " + e.getMessage()); // output error message

        }

        // Linux/Mac run.sh; will close when done
        String runShContent = "#!/bin/sh\n" +
                "echo \"Starting server...\"\n" +
                baseCommand + "\n" +
                "if [ -f \"eula.txt\" ]; then\n" +
                "   exit 0\n" +
                "else\n" +
                "   sleep 5\n" +
                "   exit 0\n" +
                "fi";

        try {

            Path path = Paths.get(serverFolderName, "run.sh");
            Files.write(path, runShContent.getBytes()); // write the content to run.sh

            if (os == OperatingSystem.Windows) {

                // if the operating system is Windows, we don't need to set the file as executable
                System.out.println("run.sh created, but not set as executable since this is Windows");

            } else {

                // set the file as executable for Linux and macOS
                Set<PosixFilePermission> perms = Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ);
                Files.setPosixFilePermissions(path, perms);
                System.out.println("run.sh created and set as executable");

            }
        } catch (Exception e) {

            System.err.println("Error creating run.sh: " + e.getMessage()); // output error message

        }
    }

    private static void runBat(String serverFolderName) {

        // run the run.bat file
        try {

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "run.bat");
            pb.directory(Paths.get(serverFolderName).toFile());
            pb.start();

        } catch (IOException e) {

            System.err.println("Error running run.bat: " + e.getMessage()); // output error message

        }
    }

    private static void runSh(String serverFolderName) {

        // run the run.sh file
        try {

            ProcessBuilder pb = new ProcessBuilder("sh", "run.sh");
            pb.directory(Paths.get(serverFolderName).toFile()); // set the working directory to the server folder so the JAR file can be found
            pb.inheritIO(); // inherit the IO streams so the output is displayed in the console
            pb.start(); // start the process

        } catch (IOException e) {

            System.err.println("Error running run.sh: " + e.getMessage()); // output error message

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
            System.out.println("^^ MOTD set! ^^");

        } catch (IOException e) {

            System.err.println("Error writing to server.properties: " + e.getMessage());

        }
    }

    private static void printCompletedMessage() {

        System.out.println("\n** Your server has been created successfully! **\n");
        System.out.println("A GUI will open where you can monitor your server and type commands.");
        System.out.println("Type \"/op [username]\" to grant a user admin privileges (ability to type commands in game)");
        System.out.println("To allow users on other networks to join your server, you will need to port forward your router.");
        System.out.println("\tCheck out this guide to port forwarding: https://www.wikihow.com/Portforward-Minecraft ");
        System.out.println("\tBe careful when doing this, it is advanced and requires changing router settings.");
        System.out.println("\nCheck out this guide to learn how to configure your server settings (render distance, seed, MOTD, etc.):");
        System.out.println("\thttps://minecraft.wiki/w/Server.properties\n");

        System.out.println("Typing \"stop\" stops your server safely.");
        System.out.println("Open the \"run.bat\" file to turn your server on later (use \"run.sh\" if on Linux or Mac).");

        printPublicIP();

    }

    private static void printCompletedMessageForge() {

        System.out.println("\n** Your server installer has been downloaded successfully **\n");
        System.out.println("You must run the installer \".jar\" file to complete setup. (Sorry, Forge is annoying)");
        System.out.println("Make sure to select \"Server\" in the installer.\n");

        System.out.println("Once setup is complete, open \"run.bat\" to turn your server on (use \"run.sh\" if on Linux or Mac).");
        System.out.println("When your server starts, a GUI will open where you can monitor your server and type commands.");
        System.out.println("\tType \"/op [username]\" to grant a user admin privileges (ability to type commands in game)");
        System.out.println("\tTyping \"/stop\" stops the server safely");
        System.out.println("To allow users on other networks to join your server, you will need to port forward your router.");
        System.out.println("\tCheck out this guide to port forwarding: https://www.wikihow.com/Portforward-Minecraft ");
        System.out.println("\tBe careful when doing this, it is advanced and requires changing router settings.");
        System.out.println("\nCheck out this guide to learn how to configure your server settings (render distance, seed, MOTD, etc.):");
        System.out.println("\thttps://minecraft.wiki/w/Server.properties");

        printPublicIP();

    }

    private static void printPublicIP() {

        System.out.println();
        try {

            URL url = URI.create("https://api.ipify.org").toURL(); // you can also use "https://checkip.amazonaws.com"
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String publicIP = reader.readLine();
            reader.close();

            System.out.println("Here is your public IP (users outside your network use this to join): " + publicIP);

        } catch (Exception e) {

            System.err.println("Error fetching public IP: " + e.getMessage());
            System.out.println("You can find your public IP by searching \"what is my ip\" in your browser.");

        }

        // get local IP address
        try {

            InetAddress localHost = InetAddress.getLocalHost();
            String localIP = localHost.getHostAddress();
            System.out.println("Here is your local IP (you + users within your network use this to join): " + localIP);

        } catch (UnknownHostException e) {

            System.err.println("Error fetching local IP: " + e.getMessage());
            System.out.println("Learn how to find your local IP here: https://www.computerhope.com/issues/ch000483.htm");

        }

        System.out.println("Be careful when handing out your IP.");
        System.out.println("\nYou MUST port forward your router to allow users from other networks to join your server.\n");

    }
}
