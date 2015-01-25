#!/bin/bash
./pack-dist.sh

#scp asap.tar.gz david@kenobi.dei.uc.pt:asap.tar.gz
rsync -vahz --progress -rsh=ssh asap.tar.gz david@kenobi.dei.uc.pt:asap.tar.gz
ssh -l david kenobi.dei.uc.pt unpack-asap-and-run.sh

