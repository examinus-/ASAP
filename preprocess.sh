#!/bin/bash

#make sure all output directories are present
cat outputs_folder.list | xargs mkdir > /dev/null 2>&1


java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.answers-forum.2015.train.txt;input/STS.gs.answers-forum.2015.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.answers-forum.2015.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.answers-forum.2015.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.answers-forums.2015.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.answers-forums.2015.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.answers-forums.2015.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.answers-students.2015.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.answers-students.2015.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.answers-students.2015.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.answers-students.2015.train.txt;input/STS.gs.answers-students.2015.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.answers-students.2015.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.answers-students.2015.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.belief.2015.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.belief.2015.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.belief.2015.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.belief.2015.train.txt;input/STS.gs.belief.2015.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.belief.2015.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.belief.2015.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.deft-forum.2014.test.txt;input/STS.gs.deft-forum.2014.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.deft-forum.2014.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.deft-forum.2014.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.deft-news.2014.test.txt;input/STS.gs.deft-news.2014.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.deft-news.2014.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.deft-news.2014.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.FNWN.2013.test.txt;input/STS.gs.FNWN.2013.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.FNWN.2013.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.FNWN.2013.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.headlines.2013.test.txt;input/STS.gs.headlines.2013.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.headlines.2013.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.headlines.2013.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.headlines.2014.test.txt;input/STS.gs.headlines.2014.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.headlines.2014.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.headlines.2014.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.headlines.2015.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.headlines.2015.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.headlines.2015.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.headlines.2015.train.txt;input/STS.gs.headlines.2015.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.headlines.2015.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.headlines.2015.train.txt.preprocessed.txt" -lfl


java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.images.2014.test.txt;input/STS.gs.images.2014.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.images.2014.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.images.2014.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.images.2015.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.images.2015.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.images.2015.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.images.2015.train.txt;input/STS.gs.images.2015.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.images.2015.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.images.2015.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.MSRpar.2012.test.txt;input/STS.gs.MSRpar.2012.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.MSRpar.2012.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.MSRpar.2012.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.MSRpar.2012.train.txt;input/STS.gs.MSRpar.2012.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.MSRpar.2012.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.MSRpar.2012.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.MSRvid.2012.test.txt;input/STS.gs.MSRvid.2012.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.MSRvid.2012.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.MSRvid.2012.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.MSRvid.2012.train.txt;input/STS.gs.MSRvid.2012.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.MSRvid.2012.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.MSRvid.2012.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.OnWN.2013.test.txt;input/STS.gs.OnWN.2013.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.OnWN.2013.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.OnWN.2013.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.OnWN.2014.DEBUG.txt;input/STS.gs.OnWN.2014.DEBUG.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.OnWN.2014.DEBUG.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.OnWN.2014.DEBUG.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.OnWN.2014.test.txt;input/STS.gs.OnWN.2014.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.OnWN.2014.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.OnWN.2014.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.OnWN.train.txt;input/STS.gs.OnWN.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.OnWN.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.OnWN.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.SMTeuroparl.2012.test.txt;input/STS.gs.SMTeuroparl.2012.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.SMTeuroparl.2012.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.SMTeuroparl.2012.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.SMTeuroparl.2012.train.txt;input/STS.gs.SMTeuroparl.2012.train.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.SMTeuroparl.2012.train.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.SMTeuroparl.2012.train.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.surprise.OnWN.2012.test.txt;input/STS.gs.surprise.OnWN.2012.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.surprise.OnWN.2012.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.surprise.OnWN.2012.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.surprise.SMTnews.2012.test.txt;input/STS.gs.surprise.SMTnews.2012.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.surprise.SMTnews.2012.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.surprise.SMTnews.2012.test.txt.preprocessed.txt" -lfl

java7 -jar dist/ASAP-Project.jar -pre-only -p -mt 2 -i "input/STS.input.tweet-news.2014.test.txt;input/STS.gs.tweet-news.2014.test.txt" -fo "outputs/2015-task2/preprocess/input/STS.input.tweet-news.2014.test.txt.features.arff" -tof "outputs/2015-task2/preprocess/input/STS.input.tweet-news.2014.test.txt.preprocessed.txt" -lfl


