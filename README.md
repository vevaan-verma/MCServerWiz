# Minecraft Server Wizard (MCServerWiz)
*Online Minecraft made easy*

## About

MCServerWiz is a console utility that guides a user through setting up a ready-to-run, dedicated Minecraft Java localhost server quickly and easily.

No prior experience in Minecraft servers is required. MCServerWiz holds the user's hand through the process as it briefly explains each step and points 
the user to helpful resources. 

MCServerWiz automatically installs server.jar files from the internet as per the user's requested client and Minecraft version, and creates a customized 
run.bat (+run.sh for MacOS and Linux) file using Aikar's Flags and the user's requested memory allocation. The user is also granted the ability
to name the destination folder and set a server MOTD. 

Please note that **the application does not port forward your router** or change any firewall settings: this must be done manually to allow users from 
outside networks to join the server. **_You must port forward your router, or else your server will be LAN-only._**
You can learn how to port forward [here](https://www.wikihow.com/Portforward-Minecraft#:~:text=This%20wikiHow%20teaches).

## Compatibility 

Currently supports creating servers for Vanilla, Fabric, and Paper clients. Forge is also present, but requires manual setup. 

Only tested on Windows 11 ~~and Ubuntu 22.04 LTS~~. Both a run.bat and run.sh are provided (run.bat is for Windows, and run.sh is for Linux distros + MacOS).

## Known Issues

- User does not have the required Java version to run the server. If this is the case, download the necessary version of Java. 
- Unable to join the server. This is likely because the player is attempting to join from an outside network, but the server has not been port forwarded. 

## Notes

Server files are accessed from  https://mcutils.com/ 
We are not affiliated with this site. 
