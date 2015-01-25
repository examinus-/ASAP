#!/bin/bash

rm asap.tar.gz
tar zcvf asap.tar.gz dist input stanford-models opennlp-models *word-list.txt *.sh outputs_folder.list correlation*

