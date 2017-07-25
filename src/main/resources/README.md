#Installation:
- configure tmp-mount script
- create /tmp/SPMLauncher with 777
- create ManagerSoftware structure
- spm installation needs to have a file launch_command.txt with the start command in it. It support the variable $SPM_DIR which resolves to the absolute path of the temp spm installation (the mount)





Minimal example with one spm version and variable toolbox for that version

ManagedSoftware
├── spm
│   └── spm12
│       ├── launch_command.txt
│       └── toolbox
│           └── TFCE
│               └── v110
└── toolbox
    └── spm12
        └── TFCE
