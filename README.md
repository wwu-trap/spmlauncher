# README #

##Requirements
* Maven
* Java 8

##Build
compile with:
mvn package (.jar lands in target/ directory)

##Setup
* create /tmp/SPMLauncher with chmod 777
* create /opt/applications/SPMLauncher/ManagedSoftware/spm with spm installations
* create /opt/applications/SPMLauncher/ManagedSoftware/toolbox with toolboxes

Example:
~~~~
ManagedSoftware
├── spm
│   ├── spm12
│   │   ├── launch.sh (chmod 777)
│   │   └── toolbox
│   │       └── TFCE
│   │           └── v110
│   ├── spm5
│   │   ├── launch.sh (chmod 777)
│   │   └── toolbox
│   └── spm8
│   │   ├── launch.sh (chmod 777)
│   │   └── toolbox
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
~~~~            
Every toolbox needs a subdirectory in the toolbox-directory of the spm installation with the excact same name!

Every spm installation needs a launch.sh with command to start the spm installation. The launch.sh will be called with the tmpSpmDir as argument. The launch file should be executable. That means chmod 777 and #!/bin/bash in first line. 
 