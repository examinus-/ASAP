#!/bin/bash
pack-dist.sh

scp asap.tar.gz david@kenobi.dei.uc.pt:asap.tar.gz
echo "cd asap && tar zxvf ../asap.tar.gz && java7 -jar ASAP-Project.jar benchmark input/STS.input.all.txt input/STS.gs.all.txt outputs/benchmark > outputs/benchmark.out" > kenobi-run.sh
ssh -l david kenobi.dei.uc.pt < kenobi-run.sh
rm -f kenobi-run.sh

