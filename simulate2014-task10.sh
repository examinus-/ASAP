#!/bin/bash

#SICK 2014 (task1) simulation
#build&train models
java -jar dist/ASAP-Project.jar -i "input/SICK_train_2014.txt" -fi "input/SICK_train_only-topics-both-tfidf.tsv" -ll -lg -lt -mt 2 -o "outputs/2014-task1/simulation/predictions/SICK_train.txt" -mo "outputs/2014-task1/simulation/weka-models" -fo "outputs/2014-task1/simulation/features-SICK2014.txt" -tof "outputs/2014-task1/simulation/preprocessed.txt" -llf "outputs/2014-task1/simulation/LemmasNotFound.log" -lgf "outputs/2014-task1/simulation/GrammarCounters.log" -ltf "outputs/2014-task1/simulation/Timings.log" -lner -lnerf "outputs/2014-task1/simulation/NamedEntitiesFound.log" | tee outputs/2014-task1/simulation/train-run.log


#evaluate
java -jar dist/ASAP-Project.jar -t "input/SICK_test_2014.txt" -ft "input/SICK_test_only-topics-both-tfidf.tsv" -mt 2 -o "outputs/2014-task1/simulation/predictions/SICK_test.txt" -mi "outputs/2014-task1/simulation/weka-models" -llf "outputs/2014-task1/simulation/LemmasNotFound.test.log" -lgf "outputs/2014-task1/simulation/GrammarCounters.test.log" -ltf "outputs/2014-task1/simulation/Timings.test.log" -lner -lnerf "outputs/2014-task1/simulation/NamedEntitiesFound.test.log" | tee outputs/2014-task1/simulation/test-run.log

