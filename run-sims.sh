#!/bin/bash
./simulate2014-task10-4th.sh | tee sim4.log
./simulate2014-task10-5th.sh | tee sim5.log
./simulate2014-task10-6th.sh | tee sim6.log
./simulation-topicmodellingtest.sh | tee simtmt.log

