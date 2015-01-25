#/bin/bash

# prepare images:
cat STS.input.images.2014.test.txt STS.input.images.2015.train.txt > STS.input.images.all.txt
cat STS.gs.images.2014.test.txt STS.gs.images.2015.train.txt > STS.gs.images.all.txt
randomfiles STS.input.images.all.txt STS.gs.images.all.txt 
head -n 451 STS.gs.images.all.txt.rnd > STS.gs.images.all.test.txt
head -n 451 STS.input.images.all.txt.rnd > STS.input.images.all.test.txt
tail -n +452 STS.input.images.all.txt.rnd > STS.input.images.all.train.txt
tail -n +452 STS.gs.images.all.txt.rnd > STS.gs.images.all.train.txt

# prepare images2:
cat STS.input.MSRvid.2012.train.txt STS.input.MSRvid.2012.test.txt STS.input.images.2014.test.txt STS.input.images.2015.train.txt > STS.input.images-MSRvid.all.txt
cat STS.gs.MSRvid.2012.train.txt STS.gs.MSRvid.2012.test.txt STS.gs.images.2014.test.txt STS.gs.images.2015.train.txt > STS.gs.images-MSRvid.all.txt
randomfiles STS.input.images-MSRvid.all.txt STS.gs.images-MSRvid.all.txt 
head -n 451 STS.gs.images-MSRvid.all.txt.rnd > STS.gs.images-MSRvid.all.test.txt
head -n 451 STS.input.images-MSRvid.all.txt.rnd > STS.input.images-MSRvid.all.test.txt
tail -n +452 STS.input.images-MSRvid.all.txt.rnd > STS.input.images-MSRvid.all.train.txt
tail -n +452 STS.gs.images-MSRvid.all.txt.rnd > STS.gs.images-MSRvid.all.train.txt



# prepare headlines:
cat STS.input.headlines.2013.test.txt STS.input.headlines.2014.test.txt STS.input.headlines.2015.train.txt > STS.input.headlines.all.txt
cat STS.gs.headlines.2013.test.txt STS.gs.headlines.2014.test.txt STS.gs.headlines.2015.train.txt > STS.gs.headlines.all.txt
randomfiles STS.input.headlines.all.txt STS.gs.headlines.all.txt 
head -n 451 STS.gs.headlines.all.txt.rnd > STS.gs.headlines.all.test.txt
head -n 451 STS.input.headlines.all.txt.rnd > STS.input.headlines.all.test.txt
tail -n +452 STS.input.headlines.all.txt.rnd > STS.input.headlines.all.train.txt
tail -n +452 STS.gs.headlines.all.txt.rnd > STS.gs.headlines.all.train.txt

