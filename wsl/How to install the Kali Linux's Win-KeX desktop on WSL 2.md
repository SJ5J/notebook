# [How to install the Kali Linux's Win-KeX desktop on WSL 2](https://www.neowin.net/news/how-to-install-the-kali-linuxs-win-kex-desktop-on-wsl-2)

https://www.neowin.net/news/how-to-install-the-kali-linuxs-win-kex-desktop-on-wsl-2/

In the last week or so, Kali Linux 2020.3 [was released](https://www.kali.org/news/kali-2020-3-release/) for download. The new update introduced lots of new features but we want to focus on the Windows + Kali Desktop EXperience (Win-KeX), a GUI that can be used with [Windows Subsystem for Linux (WSL) 2](https://www.neowin.net/news/microsoft-back-ports-wsl2-to-windows-10-versions-1903-and-1909-updates-rolling-out-now).

To get started, you need to have updated to [Windows 10 version 2004](https://www.neowin.net/news/more-blocks-being-lifted-from-installing-the-windows-10-may-2020-update), if you are running this version you’ll want to install Kali Linux in WSL 2 next.

According to the [Kali Linux documentation](https://www.kali.org/docs/wsl/win-kex/), you’ll first want to open PowerShell as administrator and run **Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Windows-Subsystem-Linux** then restart. Once you’ve done that, open up PowerShell again as the admin and run **dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart** and **dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart** then restart again.

Next, you’ll want to download the [WSL 2 Linux kernel](https://aka.ms/wsl2kernel). After you’ve installed it open PowerShell as administrator once again and run **wsl --set-default-version 2**. Once all of that’s done, head into the Microsoft Store and install Kali Linux.

If you have Kali Linux installed on WSL 1, it will be necessary to upgrade it before continuing. Run **wsl --set-version kali-linux 2** then open up Kali Linux in WSL to finish the setup.

At this point, you should have Kali Linux running on WSL 2, to get Win-KeX up and running, you’ll need to run **sudo apt update && sudo apt install kali-win-kex** within your Kali Linux installation. Once installed, type **kex** to run the Win-KeX interface.

The [Kali Linux documentation](http://www.kali.org/docs/wsl/win-kex/) also contains some extra commands that you can run to further customise your installation, however, these are beyond the scope of this guide.



# Installing GitKraken in WSL 2

https://medium.com/@chuckdries/installing-gitkraken-in-wsl-2-15bf6459f823

![Image for post](https://miro.medium.com/max/60/1*SNiauos5CURxUx1OGX8z1w.png?q=20)

![Image for post]()

GitKraken for Linux running in WSL 2

# Motivation

The Windows Subsystem for Linux provides a Linux environment integrated tightly into Windows 10. WSL 2 accomplishes this by running a Linux kernel in a virtual machine. Microsoft recommends that WSL 2 users store their documents and files within the Linux root filesystem to take advantage of the file performance benefits that WSL 2 aims to provide.

Windows apps can access this filesystem from `\\wsl$\Ubuntu`(for example), where it appears to act like a network share. Mounting this directory in Windows applications generally works fine, but at the time of this writing, [accessing it has relatively poor performance](https://docs.microsoft.com/en-us/windows/wsl/wsl2-ux-changes#cross-os-file-speed-will-be-slower-in-initial-preview-builds), which can slow down filesystem heavy operations like checkout enough to make using the Windows version of GitKraken to interact with repos on that filesystem undesirable.

**This document is a guide for tinkerers and advanced users who wish to install the Linux version of GitKraken within the WSL 2 virtual machine** to get better performance interacting with files in the Linux root filesystem. **This technique is unsupported**. Follow this guide at your own risk — I’m publishing it as an adventurous tinkerer, not a representative of Axosoft. That being said, feel free to ping me in the [official GitKraken slack](https://slack.gitkraken.com/) if you have any feedback.

## tl;dr

In Windows: Install an X server, allow it to accept incoming connections from public networks, and, optionally, tighten the firewall rules to only allow incoming connections from the WSL VM.

In Linux: set the`DISPLAY` environment variable, download and install the Linux version of GitKraken, and run it.

# Before we begin

1. [Make sure you have WSL 2 installed and configured](https://docs.microsoft.com/en-us/windows/wsl/wsl2-install). I’m using Ubuntu 18.04 for this tutorial
2. You’ll need an internet connection and permission to edit your computer’s firewall rules

We’re first going to install and configure an X server for windows, then we’re going to install GitKraken in the Linux userspace.

# Install an X server

Unix compatible systems (historically*) use a protocol called the X windowing system to display GUIs. Computers with screens run X servers, then applications implement X clients and connect to X servers to render graphics. It works locally and over a network (and even over SSH), so that you can still use graphical applications on a server or time-shared system that doesn’t have a display. This isn’t a common use case on your average Linux PC, but we can take advantage of this feature to run GitKraken in our headless Linux environment and control it from Windows.

We need to install an X server on Windows for GitKraken to connect to. I personally have been using X410, but for the purpose of this guide we’re going to install VcXsrv because it’s free and works just as well in my testing.

*Avid Linux users may be aware that alternatives to X such as Wayland are becoming more popular, but X is showing no signs of going away.

## Download VcXsrv

[Download VcXsrv from SourceForge](https://sourceforge.net/projects/vcxsrv/). Go ahead and install it, all the defaults should be fine.

## Generate a config file

On the last page of the installer, there’s a button to launch the configuration tool. Open it, or, if you already closed the installer, find ‘XLaunch’ under ‘VcXsrv’ in your start menu and open it.

1. Display settings: select “**Multiple windows**” and set the display number to **-1**. (default)
2. Client startup**:** select **“Start no client”** (default)
3. Extra settings**: leave everything as is but check “Disable access control”**. By default, VcXsrv only allows incoming connections from localhost and the like, but WSL 2 VMs have their own IP addresses. We need VcXsrv to accept connections from them. Don’t worry, we’ll go over firewall configuration to make this safer later.
4. Save the configuration file somewhere convenient. You’ll double click it whenever you want to start the X server in the future. I put it on my desktop.

**Before you click finish to start the server, a note about networking**. As previously mentioned, [WSL 2 VMs have their own IP addresses](https://docs.microsoft.com/en-us/windows/wsl/wsl2-ux-changes#accessing-network-applications) (though apparently this may change — if it does, the firewall step will go away completely and this guide will be updated). Windows provides a dedicated virtual network interface for interacting with these VMs. This network is flagged as “public”.

When you start the VcXsrv for the first time, windows firewall will prompt you about allowing incoming connections to it. Check **public networks**.

Doing so will give all incoming connections from public networks access to your X server. Our next step will be configuring a tighter firewall rule to only allow incoming connections from the WSL VMs.

You may now click finish in the configurator.

## Configuring Windows Firewall

As previously mentioned, your X server is now open and accessible to incoming connections from any public networks you connect to your computer. Let’s write a firewall rule that’s more restrictive instead.

If you’re following along on a desktop at home, or are otherwise certain that you will only ever connect to networks you trust, (or just don’t care), **feel free to skip this— the X server is in a working state and ready to go. You can always come back and configure the firewall later.**

We’re going to do two things: create our new, restrictive firewall rule, and disable the automatic ones that Windows generated automatically when it prompted you to allow incoming connections.

First, use `ifconfig` in your Linux shell to make note of the WSL VM’s IP address. Look for the `inet` field on the first line under `eth0`

![111](https://miro.medium.com/max/60/1*EOjaQxwztGOfkH6pvJ7gtw.png?q=20)

![Image for post](https://miro.medium.com/max/1722/1*EOjaQxwztGOfkH6pvJ7gtw.png)

Next, get to the “Windows Defender Firewall with Advanced Security” page. You should be able to type that entire long name into the Start menu to find it. It looks like this

![Image for post](https://miro.medium.com/max/60/1*NIFFbAuEqUmS6L5XCvfQmQ.png?q=20)

![222](https://miro.medium.com/max/1600/1*NIFFbAuEqUmS6L5XCvfQmQ.png)

We need to create a new inbound rule. Select “inbound rules” in the left panel, then click “new rule” in the actions panel.

The rule creation wizard that appears has a lot of screens, so for the sake of scroll wheels everywhere I’m publishing screenshots of the rule creation as [a Google Slides slideshow.](https://docs.google.com/presentation/d/1o4HKNzcgnN2-OCuWsHWFXHB31RLyuIeuHeY45ORU4EE/edit?usp=sharing) If you already know what you’re doing, we need an inbound rule, that applies only to our X server, that allows connections from the range of remote IPs the WSL VM may take. I’m not a windows firewall expert, so if you spot a mistake please reach out.

Finally, we need to disable the automatic rules that Windows generated after we clicked through the firewall prompt when we first launched VcXsrv. There should be two inbound rules named “VcXsrv windows xserver”. Right click to disable them both.

# Install GitKraken

## Download and install the `.deb`

Run the following commands from your shell

```
wget https://release.gitkraken.com/linux/gitkraken-amd64.deb
sudo dpkg -i ./gitkraken-amd64.deb
sudo apt-get install -f
```

If you plan on using our hosting provider integrations (recommended), you’ll need a web browser so you can log in to those services. In my testing, Firefox threw errors but Chromium started just fine

```
sudo apt install chromium-browser
```

UPDATE: I just tried this again on a new laptop, this time around I was missing a few dependencies. It looks like everything that didn’t come through at the original install step gets installed by chromium — I’ve just always installed chromium so I didn’t notice. If you’re having issues with libssl or others when you launch gk, try installing chromium. You can, of course, go through and install whatever is missing manually.

By default, the Ubuntu install from the Microsoft store doesn’t come with an emoji font. We like to use emoji in our release notes, so if you want those, go ahead and install an emoji font 😉

```
sudo apt install fonts-noto-color-emoji
```

And if you find the “open file” dialog has a bunch of squares instead of letters, go ahead and install the default Ubuntu font

```
sudo apt install fonts-noto
```

## Set the DISPLAY environment variable in your bash_profile

You may need to do this every time you start a new WSL session. You can add it to your `.bash_profile` to do so.

```
export DISPLAY=$(cat /etc/resolv.conf | grep nameserver | awk '{print $2; exit;}'):0.0
```

Also, if you run into issues working with SSH remotes (AUTH SOCK not available), you might need to add some code to start your SSH agent on login. See this github issue: https://github.com/Microsoft/WSL/issues/3183#issuecomment-604437585

## Display Scaling

If you have a high resolution display like I do, you may need to also set the `GDK_SCALE` environment variable.

```
export GDK_SCALE=2
```

## Start GitKraken

Gitkraken is installed in your path and can be invoked from the shell. Use `&` after any given command to put it in the background.

```
gitkraken &
```

You should see our splash screen appear! The icon in your taskbar will be that of the X server, not GitKraken’s. It may take a moment to connect, but it should only take a moment. Unfortunately, if the X server is unreachable, things seem to hang instead of failing with an error message. If Gitkraken doesn’t open or log any output, try running something like `xeyes` (`sudo apt install x11-apps`).

My tip: GitKraken, as well as several other Linux GUIs I tried, sends occasional messages to stdout, which still appear in your prompt even when the process is backgrounded. I like to start it in a tmux session so I don’t have to look at the output. Start tmux by typing `tmux`, start gitkraken with `gitkraken`, and then press `ctrl+b` then `d` to detach from the session. You can re-attach with `tmux a`.

![Image for post](https://miro.medium.com/freeze/max/60/1*nsEhl2BCljhx4foGfs4fhQ.gif?q=20)

![Image for post]()

Note the VcXsrv icon in my system tray. In this gif I double clicked my config file to launch it, but it’s probably already running for you.