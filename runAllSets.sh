#!/bin/bash

#make sure all output directoties are present
cat outputs_folder.list | xargs mkdir > /dev/null 2>&1

#SICK 2014 (task1)
java -jar dist/ASAP-Project.jar -i "input/SICK_train.txt" -t "input/SICK_test_annotated.txt" -i "input/SICK_trial.txt" -ll -lg -lt -mt 2 -o "outputs/2014-task1/predictions/SICK_test.txt" -mo "outputs/2014-task1/weka-models" -fo "outputs/2014-task1/features.txt" -tof "outputs/2014-task1/preprocessed.txt" -no "outputs/2014-task1/predictions/SICK_test_new-format.txt" -llf "outputs/2014-task1/LemmasNotFound.log" -lgf "outputs/2014-task1/GrammarCounters.log" -ltf "outputs/2014-task1/Timings.log" -lner -lnerf "outputs/2014-task1/NamedEntitiesFound.log" | tee outputs/2014-task1/0-first-full-run.log
#manually added distributional features (can't add up the trial set because of this):
java -jar dist/ASAP-Project.jar -i "input/SICK_train.txt" -t "input/SICK_test_annotated.txt" -ll -lg -lt -o "outputs/2014-task1/with distributional features/predictions/firstFullTest.out" -mo "outputs/2014-task1/with distributional features/models" -fo "outputs/2014-task1/with distributional features/features-firstFull.arff" -tof "outputs/2014-task1/with distributional features/preprocessed.txt" -fi "input/SICK_train_only-topics-both-tfidf.tsv" -ft "input/SICK_test_only-topics-both-tfidf.tsv" -no "outputs/2014-task1/with distributional features/predictions/SICK_test_new-format.txt" -llf "outputs/2014-task1/with distributional features/LemmasNotFound.log" -lgf "outputs/2014-task1/with distributional features/GrammarCounters.log" -ltf "outputs/2014-task1/with distributional features/Timings.log" -lner -lnerf "outputs/2014-task1/with distributional features/NamedEntitiesFound.log" | tee outputs/2014-task1/with\ distributional\ features/0-first-full-run.log
#and without the added features using the same train&eval datasets, for comparison:
java -jar dist/ASAP-Project.jar -i "input/SICK_train.txt" -t "input/SICK_test_annotated.txt" -ll -lg -lt -o "outputs/2014-task1/without distributional features/predictions/firstFullTest.out" -mo "outputs/2014-task1/without distributional features/models" -fo "outputs/2014-task1/without distributional features/features-firstFull.arff" -tof "outputs/2014-task1/without distributional features/preprocessed.txt" -no "outputs/2014-task1/without distributional features/predictions/SICK_test_new-format.txt" -llf "outputs/2014-task1/without distributional features/LemmasNotFound.log" -lgf "outputs/2014-task1/without distributional features/GrammarCounters.log" -ltf "outputs/2014-task1/without distributional features/Timings.log" -lner -lnerf "outputs/2014-task1/without distributional features/NamedEntitiesFound.log" | tee outputs/2014-task1/without\ distributional\ features/0-first-full-run.log


#STS 2014 (task10)
java -jar dist/ASAP-Project.jar -i "input/STS.input.all.txt;input/STS.gs.all.txt" -tp 30 -ll -lg -lt -mt 2 -o "outputs/2014-task10/predictions/STS.all.txt" -mo "outputs/2014-task10/weka-models" -fo "outputs/2014-task10/features-sts2014.txt" -tof "outputs/2014-task10/preprocessed.txt" -no "outputs/2014-task10/predictions/STS2014.all_new-format.txt" -llf "outputs/2014-task10/LemmasNotFound.log" -lgf "outputs/2014-task10/GrammarCounters.log" -ltf "outputs/2014-task10/Timings.log" -lner -lnerf "outputs/2014-task10/NamedEntitiesFound.log" | tee outputs/2014-task10/0-first-full-run.log

#missing 2015 task1...

#STS 2015 (task2)
java -jar dist/ASAP-Project.jar -i "input/STS.input.all.2015.txt;input/STS.gs.all.2015.txt" -i "input/STS.input.all.2013.txt;input/STS.gs.all.2013.txt" -i "input/STS.input.all.2012.txt;input/STS.gs.all.2012.txt" -i "input/STS.input.all.txt;input/STS.gs.all.txt" -tp 30 -ll -lg -lt -mt 2 -o "outputs/2015-task2/predictions/STS.all.txt" -mo "outputs/2015-task2/weka-models" -fo "outputs/2015-task2/features-sts2015.txt" -tof "outputs/2015-task2/preprocessed.txt" -no "outputs/2015-task2/predictions/STS2015.all_new-format.txt" -llf "outputs/2015-task2/LemmasNotFound.log" -lgf "outputs/2015-task2/GrammarCounters.log" -ltf "outputs/2015-task2/Timings.log" -lner -lnerf "outputs/2015-task2/NamedEntitiesFound.log" | tee outputs/2015-task2/0-first-full-run.log

