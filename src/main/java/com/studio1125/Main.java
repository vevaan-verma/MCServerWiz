package com.studio1125;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    // region Class Constants
    private static final char[] FORBIDDEN_FILE_CHARS = {'<', '>', ':', '"', '/', '\\', '|', '?', '*'};
    private static final String[] FORBIDDEN_FILE_NAMES = {"CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "COM0",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9", "LPT0"};
    private static final int BINARY_FACTOR = 1024;
    // endregion

    // region UI Components
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    // endregion

    // region Server Configuration
    private OperatingSystem os;
    private String serverFolderName;
    private Client client;
    private String version;
    private int ramAlloc;
    private String jarName;
    private String motd;
    // endregion

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new Main().createAndShowGUI());

    }

    // region Main GUI Setup
    private void createAndShowGUI() {

        // region Initialization, OS detection
        String osStr = System.getProperty("os.name").toLowerCase();

        if (osStr.contains("win"))
            os = OperatingSystem.Windows;
        else if (osStr.contains("mac"))
            os = OperatingSystem.MacOS;
        else if (osStr.contains("nix") || osStr.contains("nux") || osStr.contains("aix"))
            os = OperatingSystem.Linux;
        else
            os = OperatingSystem.Other;
        // endregion

        // region Frame Setup
        frame = new JFrame("MCServerWiz - Minecraft Server Setup Wizard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setMinimumSize(new Dimension(600, 500));
        //frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
        // endregion

        // region Main Panel Setup
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        frame.add(mainPanel);
        // endregion

        // region Create All Panels
        createWelcomePanel();
        createServerNamePanel();
        createClientPanel();
        createVersionPanel();
        createRamPanel();
        createEulaPanel();
        createMotdPanel();
        createCompletionPanel();
        // endregion

        // show welcome panel first
        cardLayout.show(mainPanel, "welcome");

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
    // endregion

    // region Panel Creation Methods
    private void createWelcomePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Welcome to MCServerWiz!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Info Text
        JTextArea infoArea = new JTextArea();
        infoArea.setText("All files are from https://mcutils.com/\n" +
                "Your PUBLIC IP is accessed at https://api.ipify.org\n\n" +
                "We are not affiliated with these sites.\n\n" +
                "This wizard will guide you through setting up a Minecraft server.");
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(panel.getBackground());
        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        // endregion

        // region Next Button
        JButton nextButton = new JButton("Start Setup");
        nextButton.addActionListener(e -> cardLayout.show(mainPanel, "serverName"));
        panel.add(nextButton, BorderLayout.SOUTH);
        // endregion

        mainPanel.add(panel, "welcome");

    }

    private void createServerNamePanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Step 1: Server Name", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Center Panel Setup
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        // endregion

        // region Instructions
        JLabel instructionLabel = new JLabel("What would you like to name your server?");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(instructionLabel, gbc);

        JLabel subInstructionLabel = new JLabel("(Name of the folder to be created, does not affect actual server)");
        subInstructionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        centerPanel.add(subInstructionLabel, gbc);
        // endregion

        // region Input Field
        JTextField folderNameField = new JTextField(20);
        centerPanel.add(folderNameField, gbc);
        // endregion

        // region Error Label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        centerPanel.add(errorLabel, gbc);
        // endregion

        // region Next Button
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {

            String input = folderNameField.getText().trim();

            if (isFileNameValid(input)) {

                serverFolderName = input;
                cardLayout.show(mainPanel, "client");

            } else {

                errorLabel.setText("Invalid folder name. Please try again.");

            }
        });

        centerPanel.add(nextButton, gbc);
        // endregion

        panel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(panel, "serverName");

    }

    private void createClientPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Step 2: Server Client", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Center Panel Setup
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        // endregion

        // region Instructions
        JLabel instructionLabel = new JLabel("Select the server client you would like to use:");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(instructionLabel, gbc);
        // endregion

        // region Radio Buttons
        ButtonGroup clientGroup = new ButtonGroup();
        JRadioButton paperButton = new JRadioButton("Paper: Recommended default, has support for plugins");
        JRadioButton fabricButton = new JRadioButton("Fabric: Allows Fabric mods (no native plugin support)");
        JRadioButton forgeButton = new JRadioButton("Forge: Allows Forge mods. REQUIRES MANUAL SETUP! (no native plugin support)");
        JRadioButton vanillaButton = new JRadioButton("Vanilla: Plain Minecraft (no native plugin support)");

        clientGroup.add(paperButton);
        clientGroup.add(fabricButton);
        clientGroup.add(forgeButton);
        clientGroup.add(vanillaButton);

        centerPanel.add(paperButton, gbc);
        centerPanel.add(fabricButton, gbc);
        centerPanel.add(forgeButton, gbc);
        centerPanel.add(vanillaButton, gbc);
        // endregion

        // region Error Label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        centerPanel.add(errorLabel, gbc);
        // endregion

        // region Navigation Buttons
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {

            if (paperButton.isSelected()) {

                client = Client.paper;

            } else if (fabricButton.isSelected()) {

                client = Client.fabric;

            } else if (forgeButton.isSelected()) {

                client = Client.forge;

            } else if (vanillaButton.isSelected()) {

                client = Client.vanilla;

            } else {

                errorLabel.setText("Please select a client");
                return;

            }

            cardLayout.show(mainPanel, "version");

        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "serverName"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        // endregion

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(panel, "client");

    }

    private void createVersionPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Step 3: Minecraft Version", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Center Panel Setup
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        // endregion

        // region Instructions
        JLabel instructionLabel = new JLabel("Enter the Minecraft version you want to use:");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(instructionLabel, gbc);

        JLabel exampleLabel = new JLabel("Example: 1.8.9, 1.16.3, 1.20.1...");
        exampleLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        centerPanel.add(exampleLabel, gbc);

        JLabel urlLabel = new JLabel("<html>Visit <a href=\"https://mcutils.com/server-jars\">mcutils.com/server-jars</a> for supported versions</html>");
        urlLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        urlLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                try {

                    Desktop.getDesktop().browse(new URI("https://mcutils.com/server-jars"));

                } catch (Exception ex) {

                    System.out.println("Error opening URL: " + ex.getMessage());

                }
            }
        });
        centerPanel.add(urlLabel, gbc);
        // endregion

        // region Input Field
        JTextField versionField = new JTextField(20);
        centerPanel.add(versionField, gbc);
        // endregion

        // region Error Label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        centerPanel.add(errorLabel, gbc);
        // endregion

        // region Navigation Buttons
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {

            String input = versionField.getText().trim();

            if (input.contains(" "))
                input = input.substring(0, input.indexOf(" "));

            if (testVersion(client, input)) {

                version = input;
                cardLayout.show(mainPanel, "ram");

            } else {

                errorLabel.setText("Invalid version or version not supported for selected client");

            }
        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "client"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        // endregion

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(panel, "version");

    }

    private void createRamPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Step 4: RAM Allocation", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Center Panel Setup
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        // endregion

        // region Instructions
        JLabel instructionLabel = new JLabel("How many gigabytes of RAM would you like to reserve for your server?");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(instructionLabel, gbc);

        JLabel recommendationLabel = new JLabel("<html>4GB is enough for most servers. Allocate more for servers with many mods or players.<br>" +
                "Ideally provide at least 1GB. Ensure the server does not use up all system resources.</html>");
        recommendationLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        centerPanel.add(recommendationLabel, gbc);
        // endregion

        // region RAM Spinner
        JSpinner ramSpinner = new JSpinner(new SpinnerNumberModel(4.0, 0.5, 64.0, 0.5));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(ramSpinner, "#.#");
        ramSpinner.setEditor(editor);
        centerPanel.add(ramSpinner, gbc);
        // endregion

        // region Error Label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        centerPanel.add(errorLabel, gbc);
        // endregion

        // region Waiting Label
        JLabel waitingLabel = new JLabel("Waiting for EULA...", SwingConstants.CENTER);
        waitingLabel.setForeground(new Color(0, 100, 0));  // dark green color
        waitingLabel.setVisible(false);
        centerPanel.add(waitingLabel, gbc);
        // endregion

        // region Next Button Action
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {

            double memory = (Double) ramSpinner.getValue();

            if (memory < 0.5) {

                errorLabel.setText("Please provide at least 0.5 gigabytes of memory.");
                return;

            }

            ramAlloc = (int) (memory * BINARY_FACTOR);

            // disable button and show waiting text
            nextButton.setEnabled(false);
            waitingLabel.setVisible(true);
            panel.revalidate();
            panel.repaint();

            // run in background thread to keep UI responsive
            new Thread(() -> {

                // download server jar and create run files
                jarName = getServerJar(client, version, serverFolderName);
                createRunFiles(jarName, client, serverFolderName, ramAlloc, os);

                SwingUtilities.invokeLater(() -> {

                    if (versionEnforcesEULA(version)) {

                        if (client == Client.forge) {

                            cardLayout.show(mainPanel, "completion");

                        } else {

                            if (os == OperatingSystem.Windows)
                                runBat(serverFolderName);
                            else if (os == OperatingSystem.Linux || os == OperatingSystem.MacOS)
                                runSh(serverFolderName);

                            if (!waitForEULA(serverFolderName)) {

                                JOptionPane.showMessageDialog(frame, "EULA file was not created in time. Please try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                                nextButton.setEnabled(true);
                                waitingLabel.setVisible(false);
                                return;

                            }

                            cardLayout.show(mainPanel, "eula");

                        }
                    } else {

                        cardLayout.show(mainPanel, "motd");

                    }
                });
            }).start();
        });
        // endregion

        // region Navigation Buttons
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "version"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        // endregion

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(panel, "ram");

    }

    private void createEulaPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Step 5: Minecraft EULA", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Center Panel Setup
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        // endregion

        // region Instructions
        JLabel instructionLabel = new JLabel("You must accept the Minecraft EULA to run the server.");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(instructionLabel, gbc);

        JLabel eulaLabel = new JLabel("<html>View the EULA here: <a href=\"https://aka.ms/MinecraftEULA\">https://aka.ms/MinecraftEULA</a></html>");
        eulaLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        eulaLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                try {

                    Desktop.getDesktop().browse(new URI("https://aka.ms/MinecraftEULA"));

                } catch (Exception ex) {

                    ex.printStackTrace();

                }
            }
        });
        centerPanel.add(eulaLabel, gbc);
        // endregion

        // region Checkbox
        JCheckBox acceptCheckbox = new JCheckBox("I accept the Minecraft EULA");
        centerPanel.add(acceptCheckbox, gbc);
        // endregion

        // region Error Label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        centerPanel.add(errorLabel, gbc);
        // endregion

        // region Navigation Buttons
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> {

            if (acceptCheckbox.isSelected()) {

                acceptEula(serverFolderName);
                cardLayout.show(mainPanel, "motd");

            } else {

                errorLabel.setText("You must accept the EULA to continue");

            }
        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "ram"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        // endregion

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(panel, "eula");

    }

    private void createMotdPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Step 6: Server Description (MOTD)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Center Panel Setup
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        // endregion

        // region Instructions
        JLabel instructionLabel = new JLabel("Set a description that players will see in their server browser:");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(instructionLabel, gbc);

        JLabel formatLabel = new JLabel("<html>Learn how to format your MOTD: <a href=\"https://minecraft.fandom.com/wiki/Formatting_codes\">Formatting Codes</a></html>");
        formatLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        formatLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                try {

                    Desktop.getDesktop().browse(new URI("https://minecraft.fandom.com/wiki/Formatting_codes"));

                } catch (Exception ex) {

                    ex.printStackTrace();

                }
            }
        });

        centerPanel.add(formatLabel, gbc);
        // endregion

        // region MOTD Text Area
        JTextArea motdArea = new JTextArea(5, 40);
        motdArea.setLineWrap(true);
        motdArea.setWrapStyleWord(true);
        centerPanel.add(new JScrollPane(motdArea), gbc);
        // endregion

        // region Error Label
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        centerPanel.add(errorLabel, gbc);
        // endregion

        // region Navigation Buttons
        JButton nextButton = new JButton("Finish");
        nextButton.addActionListener(e -> {

            String input = motdArea.getText().trim();

            if (!input.isEmpty()) {

                motd = input.replace("\n", "\\n");
                setMOTD(motd, serverFolderName);

            }

            cardLayout.show(mainPanel, "completion");

        });

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, versionEnforcesEULA(version) ? "eula" : "ram"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        // endregion

        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(panel, "motd");

    }

    private void createCompletionPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // region Title
        JLabel titleLabel = new JLabel("Setup Complete!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);
        // endregion

        // region Info Text Area
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(panel.getBackground());
        panel.add(new JScrollPane(infoArea), BorderLayout.CENTER);
        // endregion

        // region Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton startButton = new JButton("Start Server Now");

        startButton.addActionListener(e -> {

            if (os == OperatingSystem.Windows)
                runBat(serverFolderName);
            else if (os == OperatingSystem.Linux || os == OperatingSystem.MacOS)
                runSh(serverFolderName);
            else
                JOptionPane.showMessageDialog(frame,
                        "Operating system not recognized. You will have to run the run.sh or run.bat file manually.",
                        "Warning", JOptionPane.WARNING_MESSAGE);

            frame.dispose();

        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> frame.dispose());

        buttonPanel.add(startButton);
        buttonPanel.add(closeButton);
        // endregion

        // region Set Info Text Based on Client
        if (client == Client.forge)
            infoArea.setText("** Your server installer has been downloaded successfully **\n\n" +
                    "Wait for Forge to finish installing. Afterwards, open \"run.bat\" (use \"run.sh\" if on Linux or Mac).\n" +
                    "You will be prompted to agree to the EULA. Open \"eula.txt\" and set \"eula=true\".\n\n" +
                    "Once setup is complete, open \"run.bat\" to turn your server on (use \"run.sh\" if on Linux or Mac).\n" +
                    "When your server starts, a GUI will open where you can monitor your server and type commands.\n" +
                    "\tType \"/op [username]\" to grant a user admin privileges (ability to type commands in game)\n" +
                    "\tTyping \"/stop\" stops the server safely\n" +
                    "To allow users on other networks to join your server, you will need to port forward your router.\n" +
                    "\tCheck out this guide to port forwarding: https://www.wikihow.com/Portforward-Minecraft\n" +
                    "\tBe careful when doing this, it is advanced and requires changing router settings.\n\n" +
                    "Check out this guide to learn how to configure your server settings (render distance, seed, MOTD, etc.):\n" +
                    "\thttps://minecraft.wiki/w/Server.properties\n\n" +
                    getIpInfoText() +
                    "\n^^ Please read all the above text before proceeding ^^");
        else
            infoArea.setText(
                    "** Your server has been created successfully! **\n\n" +
                            "A GUI will open where you can monitor your server and type commands.\n" +
                            "Type \"/op [username]\" to grant a user admin privileges (ability to type commands in game)\n" +
                            "To allow users on other networks to join your server, you will need to port forward your router.\n" +
                            "\tCheck out this guide to port forwarding: https://www.wikihow.com/Portforward-Minecraft\n" +
                            "\tBe careful when doing this, it is advanced and requires changing router settings.\n\n" +
                            "Check out this guide to learn how to configure your server settings (render distance, seed, MOTD, etc.):\n" +
                            "\thttps://minecraft.wiki/w/Server.properties\n\n" +
                            "Typing \"stop\" stops your server safely.\n" +
                            "Open the \"run.bat\" file to turn your server on later (use \"run.sh\" if on Linux or Mac).\n\n" +
                            getIpInfoText() +
                            "\n^^ Please read all the above text before proceeding ^^");
        // endregion

        panel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.add(panel, "completion");

    }
    // endregion

    // region Utility Methods
    private boolean isFileNameValid(String name) {

        name = name.trim(); // trim the name to remove leading and trailing whitespace

        // check if the name is empty
        if (name.isEmpty())
            return false;

        // check if the name is too long
        if (name.length() > 255)
            return false;

        // check forbidden chars for inclusion
        for (int i = 0; i < name.length(); i++)
            for (char c : FORBIDDEN_FILE_CHARS)
                if (name.toLowerCase().charAt(i) == c)
                    return false;

        // check forbidden names for insensitive equality
        for (String str : FORBIDDEN_FILE_NAMES)
            if (name.equalsIgnoreCase(str))
                return false;

        // check if the folder already exists
        Path path = Paths.get(name);
        return !Files.exists(path);

    }

    private boolean testVersion(Client client, String version) {

        String url = "https://mcutils.com/api/server-jars/" + client.toString() + "/" + version + "/download";

        try {

            URL testURL = URI.create(url).toURL();
            HttpURLConnection huc = (HttpURLConnection) testURL.openConnection();
            huc.setRequestMethod("GET");
            huc.connect();
            return huc.getResponseCode() == 200;

        } catch (Exception e) {

            return false;

        }
    }

    private String getServerJar(Client client, String version, String serverFolderName) {

        String url = "https://mcutils.com/api/server-jars/" + client.toString() + "/" + version + "/download";
        String jarName = client + "-" + version + ".jar";

        try {

            Files.createDirectories(Paths.get(serverFolderName));
            InputStream in = URI.create(url).toURL().openStream();
            Files.copy(in, Paths.get(serverFolderName, jarName), StandardCopyOption.REPLACE_EXISTING);
            return jarName;

        } catch (Exception e) {

            JOptionPane.showMessageDialog(frame, "Error downloading server jar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;

        }
    }

    private void createRunFiles(String jarName, Client client, String serverFolderName, int ramAlloc, OperatingSystem os) {

        // alloc greater than 12gb has different flags
        // credit: https://flags.sh/
        boolean isLargeAlloc = ramAlloc >= 12 * BINARY_FACTOR;

        String baseCommand = "java -Xms" + ramAlloc + "M -Xmx" + ramAlloc + "M --add-modules=jdk.incubator.vector -XX:+UseG1GC " +
                "-XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions " +
                "-XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 " +
                "-XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 " +
                "-XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem " +
                "-XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true " +
                "-XX:G1NewSizePercent=" + (isLargeAlloc ? 40 : 30) + " -XX:G1MaxNewSizePercent=" + (isLargeAlloc ? 50 : 40) +
                " -XX:G1HeapRegionSize=" + (isLargeAlloc ? 16 : 8) + "M -XX:G1ReservePercent=" + (isLargeAlloc ? 15 : 20) +
                " -jar " + jarName + (client.toString().equalsIgnoreCase("forge") ? " --installServer" : "");

        // region Windows run.bat
        String runBatContent = "@echo off\n" +
                "echo Starting server...\n" +
                baseCommand + "\n" +
                "if exist eula.txt (\n" +
                "   exit\n" +
                ") else (\n" +
                "   timeout /t 5 >nul\n" +
                "   exit\n" +
                ")";

        try {

            Files.write(Paths.get(serverFolderName, "run.bat"), runBatContent.getBytes());

        } catch (Exception e) {

            JOptionPane.showMessageDialog(frame, "Error creating run.bat: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
        // endregion

        // region Linux/Mac run.sh
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
            Files.write(path, runShContent.getBytes());

            if (os != OperatingSystem.Windows) {

                // set the file as executable for Linux and macOS
                Set<PosixFilePermission> perms = Set.of(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(path, perms);

            }
        } catch (Exception e) {

            JOptionPane.showMessageDialog(frame, "Error creating run.sh: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
        // endregion
    }

    private void runBat(String serverFolderName) {

        try {

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "run.bat");
            pb.directory(Paths.get(serverFolderName).toFile());
            pb.start();

        } catch (IOException e) {

            JOptionPane.showMessageDialog(frame, "Error running run.bat: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }

    private void runSh(String serverFolderName) {

        try {

            ProcessBuilder pb = new ProcessBuilder("sh", "run.sh");
            pb.directory(Paths.get(serverFolderName).toFile());
            pb.inheritIO();
            pb.start();

        } catch (IOException e) {

            JOptionPane.showMessageDialog(frame, "Error running run.sh: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }

    private void acceptEula(String serverFolderName) {

        try {

            Path path = Paths.get(serverFolderName, "eula.txt");
            List<String> lines = Files.readAllLines(path);

            for (int i = 0; i < lines.size(); i++)
                if (lines.get(i).trim().startsWith("eula="))
                    lines.set(i, "eula=true");

            Files.write(path, lines);

        } catch (IOException e) {

            JOptionPane.showMessageDialog(frame, "Error writing to eula.txt: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }

    private void setMOTD(String motd, String serverFolderName) {

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
                lines.add("motd=" + motd);

            Files.write(path, lines);

        } catch (IOException e) {

            JOptionPane.showMessageDialog(frame, "Error writing to server.properties: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }

    private boolean waitForEULA(String serverFolderName) {

        int timeoutSeconds = 60;
        int waited = 0;

        while (waited < timeoutSeconds) {

            try {

                if (Files.exists(Paths.get(serverFolderName, "eula.txt")))
                    return true;

                Thread.sleep(1000);
                waited++;

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                return false;

            } catch (Exception e) {

                return false;

            }
        }

        return false;

    }

    private boolean versionEnforcesEULA(String version) {

        List<Integer> parsedVersion = new ArrayList<>();
        int parsedVersionIdx = 0;
        int nextDotIdx = 0;

        String versionDecomp = version;

        while (!versionDecomp.isEmpty()) {

            nextDotIdx = versionDecomp.indexOf(".");

            if (nextDotIdx != -1) {

                int versionDigit = Integer.parseInt(versionDecomp.substring(0, nextDotIdx));
                parsedVersion.add(parsedVersionIdx, versionDigit);
                versionDecomp = versionDecomp.substring(nextDotIdx + 1);
                parsedVersionIdx++;

            } else {

                parsedVersion.add(parsedVersionIdx, Integer.parseInt((versionDecomp)));
                versionDecomp = "";

            }
        }

        // versions 1.7.10 and newer return "true"
        return (parsedVersion.size() == 3 && parsedVersion.get(1) == 7 && parsedVersion.get(2) == 10) || parsedVersion.get(1) > 7;

    }

    private String getIpInfoText() {

        StringBuilder sb = new StringBuilder();

        try {

            URL url = URI.create("https://api.ipify.org").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String publicIP = reader.readLine();
            reader.close();

            sb.append("Public IP (users outside your network use this to join): ").append(publicIP).append("\n");

        } catch (Exception e) {

            sb.append("Could not fetch public IP. You can find it by searching \"what is my ip\" in your browser.\n");

        }

        try {

            InetAddress localHost = InetAddress.getLocalHost();
            String localIP = localHost.getHostAddress();
            sb.append("Local IP (you + users within your network use this to join): ").append(localIP).append("\n");

        } catch (UnknownHostException e) {

            sb.append("Could not fetch local IP. Learn how to find it here: https://www.computerhope.com/issues/ch000483.htm\n");

        }

        return sb.toString();

    }
    // endregion

    // region Enums
    private enum OperatingSystem {

        Windows, MacOS, Linux, Other

    }

    private enum Client {

        paper, fabric, forge, vanilla

    }
    // endregion
}
