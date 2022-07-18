# README #

## Requirements
* Maven
* Java 11

## Build
compile with:
mvn package (.jar lands in target/ directory)

## Setup
* create /opt/applications/SPMLauncher/ManagedSoftware/spm with spm installations
* create /opt/applications/SPMLauncher/ManagedSoftware/toolbox with toolboxes
* add tmp-mount to /usr/local/bin with 755 and root as owner

~~~~
#!/bin/bash

# place this file in /usr/local/bin/tmp-mount
# to let anyone use this command use, add the following to the !!END!! of the sudoers file:
# ALL ALL=NOPASSWD: /usr/local/bin/tmp-mount
# and don't forget to execute `chown root` and `chmod +x` on this file!

MANAGED_SOFTWARE_DIR=/opt/applications/SPMLauncher/ManagedSoftware
MOUNT_DIR=/tmp/SPMLauncher

if [ "$1" = "-m" ] ; then
        /bin/mount --bind $MANAGED_SOFTWARE_DIR/$2 $MOUNT_DIR/$3
        /bin/mount --make-slave $MOUNT_DIR/$3
elif [ "$1" = "-u" ] ; then
        /bin/umount -l $MOUNT_DIR/$2
fi
~~~~


Example:
~~~~
ManagedSoftware
├── spm
│   ├── spm12
│   │   ├── launch.sh (chmod 777)
│   │   └── toolbox
│   │       ├── cat
│   │       └── TFCE
│   ├── spm5
│   │   ├── launch.sh (chmod 777)
│   │   └── toolbox
│   │       └── TFCE
│   └── spm8
│       ├── launch.sh (chmod 777)
│       └── toolbox
│           ├── cat
│           └── TFCE
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
