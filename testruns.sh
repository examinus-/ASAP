#!/bin/bash

#first run::
#java7 -Xmx3g -jar dist/ASAP-Project.jar -t "input/STS.input.headlines.2015.test.txt" -ft "input/GensimTfidf/differencePhraseSTS.headlines.2015.test.preprocessed.tsv" -ft "input/GensimTfidf/euclideanDistanceSTS.headlines.2015.test.preprocessed.tsv" -lt -lfl -mi "outputs/2015-task2/sim-limit-pre-sum/weka-models-headlines-GensimTfidf" -o "outputs/2015-task2/chosen/firstrun/headlines/predictions/STS.test.txt" -p -mt 12 -fo "outputs/2015-task2/chosen/firstrun/headlines/features-sts2014-test.txt" -tof "outputs/2015-task2/chosen/firstrun/headlines/preprocessed-test.txt" -ltf "outputs/2015-task2/chosen/firstrun/headlines/Timings-test.log" -ssaropiem | tee "outputs/2015-task2/chosen/firstrun/run-headlines.log"

java7 -Xmx3g -jar dist/ASAP-Project.jar -t "input/STS.input.images.2015.test.txt" -ft "input/GensimTfidf/differencePhraseSTS.images.2015.test.preprocessed.tsv" -ft "input/GensimTfidf/euclideanDistanceSTS.images.2015.test.preprocessed.tsv" -lt -lfl -mi "outputs/2015-task2/sim-limit-pre-sum/weka-models-images-GensimTfidf" -o "outputs/2015-task2/chosen/firstrun/images/predictions/STS.test.txt" -p -mt 12 -fo "outputs/2015-task2/chosen/firstrun/images/features-sts2014-test.txt" -tof "outputs/2015-task2/chosen/firstrun/images/preprocessed-test.txt" -ltf "outputs/2015-task2/chosen/firstrun/images/Timings-test.log" -ssaropiem | tee "outputs/2015-task2/chosen/firstrun/run-images.log"

java7 -Xmx3g -jar dist/ASAP-Project.jar -t "input/STS.input.answers-forums.2015.test.txt" -ft "input/GensimTfidf/differencePhraseSTS.answers-forums.2015.test.preprocessed.tsv" -ft "input/GensimTfidf/euclideanDistanceSTS.answers-forums.2015.test.preprocessed.tsv" -lt -lfl -mi "outputs/2015-task2/sim-limit-pre-sum/weka-models-all-but-headlines-MSRvid-images-GensimTfidf" -o "outputs/2015-task2/chosen/firstrun/answers-forums/predictions/STS.test.txt" -p -mt 12 -fo "outputs/2015-task2/chosen/firstrun/answers-forums/features-sts2014-test.txt" -tof "outputs/2015-task2/chosen/firstrun/answers-forums/preprocessed-test.txt" -ltf "outputs/2015-task2/chosen/firstrun/answers-forums/Timings-test.log" -ssaropiem | tee "outputs/2015-task2/chosen/firstrun/run-answers-forums.log"

java7 -Xmx3g -jar dist/ASAP-Project.jar -t "input/STS.input.answers-students.2015.test.txt" -ft "input/GensimTfidf/differencePhraseSTS.answers-students.2015.test.preprocessed.tsv" -ft "input/GensimTfidf/euclideanDistanceSTS.answers-students.2015.test.preprocessed.tsv" -lt -lfl -mi "outputs/2015-task2/sim-limit-pre-sum/weka-models-all-but-headlines-MSRvid-images-GensimTfidf" -o "outputs/2015-task2/chosen/firstrun/answers-students/predictions/STS.test.txt" -p -mt 12 -fo "outputs/2015-task2/chosen/firstrun/answers-students/features-sts2014-test.txt" -tof "outputs/2015-task2/chosen/firstrun/answers-students/preprocessed-test.txt" -ltf "outputs/2015-task2/chosen/firstrun/answers-students/Timings-test.log" -ssaropiem | tee "outputs/2015-task2/chosen/firstrun/run-answers-students.log"

java7 -Xmx3g -jar dist/ASAP-Project.jar -t "input/STS.input.belief.2015.test.txt" -ft "input/GensimTfidf/differencePhraseSTS.belief.2015.test.preprocessed.tsv" -ft "input/GensimTfidf/euclideanDistanceSTS.belief.2015.test.preprocessed.tsv" -lt -lfl -mi "outputs/2015-task2/sim-limit-pre-sum/weka-models-all-but-headlines-MSRvid-images-GensimTfidf" -o "outputs/2015-task2/chosen/firstrun/belief/predictions/STS.test.txt" -p -mt 12 -fo "outputs/2015-task2/chosen/firstrun/belief/features-sts2014-test.txt" -tof "outputs/2015-task2/chosen/firstrun/belief/preprocessed-test.txt" -ltf "outputs/2015-task2/chosen/firstrun/belief/Timings-test.log" -ssaropiem | tee "outputs/2015-task2/chosen/firstrun/run-belief.log"



#second run::
java7 -Xmx3g -jar dist/ASAP-Project.jar -t "input/STS.input.headlines.2015.test.txt" -ft "input/GensimNoTfidf/differencePhraseSTS.headlines.2015.test.preprocessed.tsv" -ft "input/GensimNoTfidf/euclideanDistanceSTS.headlines.2015.test.preprocessed.tsv" -lt -lfl -mi "outputs/2015-task2/sim-ignore-exact-matches/weka-models-headlines-GensimNoTfidf" -o "outputs/2015-task2/chosen/secondrun/headlines/predictions/STS.test.txt" -p -mt 12 -fo "outputs/2015-task2/chosen/secondrun/headlines/features-sts2014-test.txt" -tof "outputs/2015-task2/chosen/secondrun/headlines/preprocessed-test.txt" -ltf "outputs/2015-task2/chosen/secondrun/headlines/Timings-test.log" -ssaropiem | tee "outputs/2015-task2/chosen/secondrun/run-headlines.log"

java7 -Xmx3g -jar dist/ASAP-Project.jar -t "input/STS.input.images.2015.test.txt" -ft "input/GensimNoTfidf/differencePhraseSTS.images.2015.test.preprocessed.tsv" -ft "input/GensimNoTfidf/euclideanDistanceSTS.images.2015.test.preprocessed.tsv" -lt -lfl -mi "outputs/2015-task2/sim-ignore-exact-matches/weka-models-images-GensimNoTfidf" -o "outputs/2015-task2/chosen/secondrun/images/predictions/STS.test.txt" -p -mt 12 -fo "outputs/2015-task2/chosen/secondrun/images/features-sts2014-test.txt" -tof "outputs/2015-task2/chosen/secondrun/images/preprocessed-test.txt" -ltf "outputs/2015-task2/chosen/secondrun/images/Timings-test.log" -ssaropiem | tee "outputs/2015-task2/chosen/secondrun/run-images.log"


