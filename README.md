# README #

##Requirements
* Maven
* Java 8

##Build
compile with:
mvn package 

##Setup
create /tmp/SPMLauncher with chmod 777
create /opt/applications/SPMLauncher/ManagedSoftware/spm with spm installations
create /opt/applications/SPMLauncher/ManagedSoftware/toolbox with toolboxes
Example:
.
├── spm
│   ├── spm12
│   ├── spm5
│   └── spm8
└── toolbox
    ├── spm12
    │   ├── cat
    │   │   ├── v1
    │   │   ├── v2
    │   │   ├── v3
    │   │   └── v4
    │   └── TFCE
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
Every toolbox needs a subdirectory in the toolbox-directory of the spm installation with the excact same name!
 