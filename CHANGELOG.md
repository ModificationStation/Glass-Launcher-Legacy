#### v0.5
\+ Added some ~~basic~~ pretty advanced glass-repo integration.  
\+ Added the `-help` and `-h` parameters.  
\+ Added a mod info panel for viewing info about mods, if they have info attached.  
\+ Added Microsoft account support. The button can be hidden in the instances window for those who dislike it.  
\+ Added a proper logger for minecraft so you can get an isolated minecraft log inside of `.glass-launcher/logs/minecraft` now.    
\+ Made the launcher a bit more verbose when doing background tasks.  
\+ Added some basic compatibility checks.  
\+ Added the ability to set the install folder through the `-installdir` parameter.  
\+ Added a new default theme! Can be disabled in the instances window for your system default.  
\+ Mods can be dragged into a mod list to be added to the current instance.  
\+ Made it so instance versions can be changed. Jars will have to be provided manually though.  
\* Changed how glass-launcher gets its version. No more mislabelled launcher versions.  
\* Made the console not display if `-noguiconsole` is passed, or if Java detects an attached console.  
\* Fixed new subwindows not being centered on the parent window. Should make it less painful to use on multi-monitor setups.  
\* JavaFX is no longer mandatory, everything now works without JavaFX aside from Microsoft auth.    
\* Knot is now used to launch Minecraft instead of EasyMineLauncher. This should fix Minecraft hanging when being closed, as well as adding [Cursed Fabric](https://github.com/minecraft-cursed-legacy/Cursed-fabric-loader) support.  
\* Changed how authentication is handled. It is now much less janky and will now remember your auth token.  
\* Updated dependencies used in glass-launcher.  
\- Cleared up a few unused classes and images.

#### v0.4.5
\* Fixed the resource proxy not working on old alpha versions.

#### v0.4.4
\+ The last instance launched is now remembered between restarts.  
\+ The launcher now uses glass-commons for logging and most file related functions.  
\* Fixed a crash relating to the cache folder not being made when the proxy is started in standalone mode.  

#### v0.4.3
\+ Added an "Open Instances Folder" button to the instance manager.  
\+ Added the ability to press enter to log in.  
\+ Added an exe version of glass-launcher to releases. This will only be available in "stable" releases.  
\+ Added a progress window when applying mods from the options window.  
\* Made it so file select dialogs are native. This should make file selecting a little easier.  
\* Potentially fixed text in buttons being cut off.  
\* Fixed 404 errors in the resource proxy.  
\* Progress windows now log their text to console.  
\* Fixed mods not installing when installing glass-launcher modpacks.

#### v0.4.2
\+ Made the FileUtils class far more useful.  
\+ New jar constructing system. Should be WAY faster than the old system.  
\* Fixed maxRam not being saved in instance config.  
\* Fixed path being serialised to JSON in instance config.  
\* Fixed instance importing. Also fixed instance names having .zip at the end.  
\* Fixed applying mods.  
\* Fixed me not knowing how to use my own JsonConfig class.  
\- Removed all * imports.

#### v0.4.1
\* Disabled automatic dependency downloads in favour of FatJar because BouncyCastle libs are broken.  
\* Fixed Minecraft being unable to launch.

#### v0.4
\+ Added some basic mod management.  
\+ Added a basic progress window.  
\+ Added MultiMC modpack support.  
\+ Added automatic sound downloading on creating an instance. This will preserve any custom sounds from modpacks, if there are any.  
\+ Added authentication support to the proxy. You now no longer need to install a login fix!  
\+ Added caching of versions, sounds and LWJGL.  
\+ Added Lombok to make things easier.  
\+ Added the ability to use external links in `!net/glasslauncher/legacy/assets/mcversions.json`.  
\+ Added the ability to delete instances.  
\+ Added automatic instance list refreshes to make things feel more fluent.
\+ Added account verification when creating/installing instances.
\- Removed certain debug logs.  
\- Removed some unneeded classes.  
\- Removed PyMCL instance support due to how the mod manager and new config works.  
\* Swapped Json-IO with GSON. Config files are now more robust and should produce less errors (if any).  
\* Config overhaul for the library swap.  
\* Fixed some visual bugs.  
\* Changed install folder from `.PyMCL` to `.glass-launcher`.

#### v0.3
\+ Added input hints.  
\+ Automatic dependency downloading to `.PyMCL/lib` instead of making a fatjar.  
\+ MitM proxy that redirects to a localhosted webserver ran in glass-launcher, which has a semi-configurable cache time on skins and capes. Saves cache in `.PyMCL/cache/webproxy`.  
\+ Last used username is now saved upon a successful LaunchArgs validation.  
\* Better JSON reading and writing.  

#### v0.2
\+ Config saving and loading re-added.  
\- Removed all legacy and out of date test code.  
\*  Cleaned up some unused imports.

#### v0.1
\+ Initial release.
