# Glass Launcher Legacy
A much more lightweight approach to what PyMCL was trying to do.

## Features

- Made in Java
- Instancing that works.

## Compiling

Use `gradlew shadowJar` to build.  
Output is in `/build/libs`.

Glass Launcher will automatically download any dependencies it needs into `%appdata%/.glass-launcher/lib` if on windows, `/Library/Application Support/.glass-launcher` on osx or `~/.glass-launcher` on other OSes.

## Usage

You can just double click the jar file to launch the launcher normally.  
Launch with `-proxy` to launch just the proxy. Optional `-doskin`, `-dosound`, `-docape` args can be used.  
Use `-help` for detailed help about all launch args.
