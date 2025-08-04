#

<img width="1536" height="512" alt="MCServerWiz Banner" src="https://github.com/user-attachments/assets/4c65b184-d568-432c-b7b4-5a886d4a48e0" />

### <p align="center">Localhosting made easy</p>

<p align="center"><i>Network settings not included</i></p>

## About

MCServerWiz is a utility that guides a user through setting up a ready-to-run, dedicated Minecraft Java localhost server quickly and easily. No prior experience in Minecraft servers is required: MCServerWiz holds the user's hand through the process as it briefly explains each step and points the user to helpful resources. 

MCServerWiz automatically installs server.jar files from the internet as per the user's requested client and Minecraft version, and creates a customized run.bat (+run.sh for macOS and Linux) file using [Aikar's Flags](https://flags.sh/) and the user's requested memory allocation. The user is also granted the ability to name the destination folder and set a server MOTD. 

Please note that **the application does not port forward your router** or change any firewall settings; this must be done manually to allow users from outside networks to join the server. **_If you do not port forward your router/device, your server will be LAN-only._** You can learn how to port forward [here](https://www.wikihow.com/Portforward-Minecraft#:~:text=This%20wikiHow%20teaches). *Pro tip: setting a static IP is totally optional. If the server is just for you and your friends, simply give them your new IP when the old one stops working.*

## Compatibility 

Currently supports creating servers for Vanilla, Fabric, and Paper clients. Forge is also present, but requires manual setup. 

Only tested on Windows 11 ~~and Ubuntu 22.04 LTS~~. Both a run.bat and run.sh are provided (run.bat is for Windows, and run.sh is for Linux distros + macOS).

## Known Issues

**"Unsupported Java detected."**

- This happens on occasion when using Paper. If you get this error message, download the necessary version of Java and try again. 

**Unable to join the server.** 

- This is likely because the player is attempting to join from an outside network, but the server has not been port forwarded properly. Verify router settings and consider opening the ports on your device's firewall. 

- Alternatively, double-check your IP, as it may have changed. If this was the case, and the issue becomes frequent, consider setting a static IP.

**Took to long to create the EULA.**

- Often times, simply trying again will resolve the issue. Otherwise, check the console for errors. If the issue persists, consider attempting a manual setup.

## Notes

When hosting a server, it is recommended for your computer to have at least a decent processor, especially if you plan to run other software simultaneously (i.e., Minecraft). Close unecessary applications, and consider increasing the priority of the server software. Here's how you do that on Windows:
1. Open Task Manager
2. Open the "Details" menu
3. Look for the server application in the menu. Most likely, this will be called "java.exe". If there are multiple "java.exe"s, it is most likely the one with higher memory usage.
4. Right-click the application, go to "Set priority," and set priority to "Above normal" or "High." **Do not set it to realtime unless you know what you are doing.** 
5. If you're having performance issues, experiment with "Below normal" and "Low."
   
*(The trick above is a good way to squeeze a bit of performance out of apps. It works great on Minecraft!)* 

Server files are accessed from  https://mcutils.com/. *We are not affiliated with this site.*

Credit to [lflowers01's MC Server Wizard](https://github.com/lflowers01/mc-server-wizard). We were unaware that this app, identical in both purpose and name to ours, existed at all until we finished ours. Feel free to check it out if this less advanced app doesn't meet your needs... 
