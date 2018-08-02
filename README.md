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
│       ├── launch.sh (chmod 777)
│       └── toolbox
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
A simple example:
~~~~  
#!/bin/bash

nice -n +1 /opt/applications/matlab/R2012a/bin/matlab -r "path('$1',path); path('$1/toolbox/mania',path); cd('/spm-data'); spm fmri; "  -nodesktop -nosplash
~~~~  
It is advised to use the following launch script so toolboxes can be dynamically (and if wished also recursively) added to the MATLAB path:
~~~
#!/bin/bash


PATH_COMMANDS="path('$1',path);  "
MATLAB_BIN="/opt/applications/matlab/R2012a/bin/matlab"

#$2: add toolbox to path, non recursive
#$3: add toolbox to path, recursive

cd $1

for toolbox in $2; do
    PATH_COMMANDS="$PATH_COMMANDS path('$1/toolbox/$toolbox',path); ";
done


for toolbox in $3; do
    for subdir in $(find toolbox/$toolbox -type d | grep -v "@"); do
        PATH_COMMANDS="$PATH_COMMANDS path('$1/$subdir',path); ";
    done
done

COMMAND="nice -n +1 $MATLAB_BIN -r \"$PATH_COMMANDS cd('/spm-data'); spm fmri; \"  -nodesktop -nosplash"

echo -e "Starting SPM with the following command:\n    $(echo $COMMAND | sed -e 's/path); /path); \n\t/g')"
eval $COMMAND

~~~
