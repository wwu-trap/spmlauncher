# README #

##Requirements
* Maven
* Java 8

##Build
compile with:
mvn package (.jar lands in target/ directory)

##Setup
* create /opt/applications/SPMLauncher/ManagedSoftware/spm with spm installations
* create /opt/applications/SPMLauncher/ManagedSoftware/toolbox with toolboxes
* add tmp-mount to /usr/local/bin with 777

~~~~
#!/bin/bash

# place this file in /usr/local/bin/tmp-mount
# to let anyone use this command use, add the following to the !!END!! of the sudoers file:
# ALL ALL=NOPASSWD: /usr/local/bin/tmp-mount
# and don't forget to chmod +x this file!

MANAGED_SOFTWARE_DIR=/opt/applications/SPMLauncher/ManagedSoftware
MOUNT_DIR=/tmp/SPMLauncher

if [ "$1" = "-m" ] ; then
        /bin/mount --bind $MANAGED_SOFTWARE_DIR/$2 $MOUNT_DIR/$3
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
Example:
~~~~  
#!/bin/bash

nice -n +1 /opt/applications/matlab/R2012a/bin/matlab -r "path('$1',path); path('$1/toolbox/mania',path); cd('/spm-data'); spm fmri; "  -nodesktop -nosplash
~~~~  
 