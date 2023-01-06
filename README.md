# README #
Do you ever wonder how to handle all the toolboxes written for spm and how to keep track of its different versions? This issue is especially complicated in a multi-user environment. SPMLauncer can help you with this: it provides an overview over your SPM and MATLAB installations, allows you to quickly enable or disable toolboxes and switch versions.

![SPMLauncher GUI preview](https://github.com/wwu-trap/spmlauncher/blob/main/assets/SPMLauncher-v3.0.0.png?raw=true)

## Requirements
* Java 11 JRE (or higher)
* bwrap command (bubblewrap)
  * Ubuntu: `sudo apt install bubblewrap`
  * source: https://github.com/containers/bubblewrap

## Build
### Additional build requirements
* Java 11 **JDK** (or higher)
* Maven

Compile with: `mvn package` - the SPMLauncher jar lands in `target/` directory

## Setup
* create /opt/applications/SPMLauncher/ManagedSoftware/spm with spm installations
* create /opt/applications/SPMLauncher/ManagedSoftware/toolbox with toolboxes


Example:
~~~~
ManagedSoftware
├── spm
│   ├── spm12
│   │   ├── ...other spm12 files...
│   │   └── toolbox
│   │       ├── cat *
│   │       └── TFCE *
│   ├── spm5
│   │   ├── ...other spm5 files...
│   │   └── toolbox
│   │       └── TFCE *
│   └── spm8
│       ├── ...other spm8 files...
│       └── toolbox
│           ├── cat *
│           └── TFCE *
└── toolbox
    ├── spm12
    │   ├── cat
    |   │   ├── standard -> this file will load cat12 on default
    |   │   ├── addToPath -> this file will add the cat12 toolbox to matlab path
    │   │   ├── v1
    │   │   ├── v2
    │   │   ├── v3
    │   │   └── v4
    │   └── TFCE
    |       ├── addToPathRecursively this file will add the TFCE toolbox and all its subdirs to path
    │       ├── v2.3
    │       ├── v2.5
    │       └── v2.7
    ├── spm5
    │   └── TFCE
    │       └── v2.3
    └── spm8
        ├── cat
        │   └── v1
        └── TFCE
            ├── v2.3
            └── v2.5
~~~~            
\*: These are empty directories as placeholder where the toolbox will be loaded to. I.e. for every toolbox you needs a subdirectory in the spm/spm12/toolbox directory with the exact same name! E.g. if you want to install the conn toolbox with version v21.a for spm12, do the following:
  1. create toolbox dir: toolbox/spm12/conn/v21.a
  2. download and unzip conn v21.a this directory
  3. create placeholder (empty) dir in spm12 installation spm/spm12/toolbox/conn
