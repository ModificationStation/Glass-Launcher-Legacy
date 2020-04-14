### v0.4.1
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
