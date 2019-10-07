# Glass Launcher Legacy
A much more lightweight approach to what PyMCL was trying to do.

## Features

- Made in Java
- Instancing that works.

## Compiling

Use `gradlew jar` to build.  
Output is in `/out`.

Glass Launcher will automatically download any dependencies it needs into `%appdata%/.PyMCL/lib` if on windows, `/Library/Application Support/.PyMCL` on osx or `~/.PyMCL` on other OSes.

## Usage

You can just double click the jar file to launch the launcher normally.  
Launch with `-proxy` to launch just the proxy. Optional `-doskin`, `-dosound`, `-docape` args can be used.  
Use `-help` for detailed help about all launch args.

## Issues

- No instance creation of any kind.
