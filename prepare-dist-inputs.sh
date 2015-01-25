#!/bin/bash

cp -R ../../Features\ Topic\ Modelling/*/* input

#double check names:
rename -v -f 's/input\/(.+)\/.+\.input\.(.+)\.txt.preprocessed(.+)$/input\/$1\/differencePhraseSTS.$2.preprocessed.tsv/' input/*/*difference*.tsv
rename -v -f 's/input\/(.+)\/.+\.input\.(.+)\.txt.preprocessed(.+)$/input\/$1\/euclideanDistanceSTS.$2.preprocessed.tsv/' input/*/*euclidean*.tsv

#correct spaces to tabs:
sed -i 's/ /\t/g' input/*/*

#add column to euclideanDistanceSTS*:
sed -i '1i euclidean_distance' input/*/euclideanDistanceSTS.*

#add columns to JavaLDA*:
sed -i '1i jlda0\tjlda1\tjlda2\tjlda3\tjlda4\tjlda5\tjlda6\tjlda7\tjlda8\tjlda9\tjlda10\tjlda11\tjlda12\tjlda13\tjlda14\tjlda15\tjlda16\tjlda17\tjlda18\tjlda19\tjlda20\tjlda21\tjlda22\tjlda23\tjlda24' input/JavaLDA/differencePhraseSTS.*

#add columns to GensimTfidf*:
sed -i '1i gensim-tfidf0\tgensim-tfidf1\tgensim-tfidf2\tgensim-tfidf3\tgensim-tfidf4\tgensim-tfidf5\tgensim-tfidf6\tgensim-tfidf7\tgensim-tfidf8\tgensim-tfidf9\tgensim-tfidf10\tgensim-tfidf11\tgensim-tfidf12\tgensim-tfidf13\tgensim-tfidf14\tgensim-tfidf15\tgensim-tfidf16\tgensim-tfidf17\tgensim-tfidf18\tgensim-tfidf19\tgensim-tfidf20\tgensim-tfidf21\tgensim-tfidf22\tgensim-tfidf23\tgensim-tfidf24' input/GensimTfidf/differencePhraseSTS.*

#add columns to GensimNoTfidf*:
sed -i '1i gensim-notfidf0\tgensim-notfidf1\tgensim-notfidf2\tgensim-notfidf3\tgensim-notfidf4\tgensim-notfidf5\tgensim-notfidf6\tgensim-notfidf7\tgensim-notfidf8\tgensim-notfidf9\tgensim-notfidf10\tgensim-notfidf11\tgensim-notfidf12\tgensim-notfidf13\tgensim-notfidf14\tgensim-notfidf15\tgensim-notfidf16\tgensim-notfidf17\tgensim-notfidf18\tgensim-notfidf19\tgensim-notfidf20\tgensim-notfidf21\tgensim-notfidf22\tgensim-notfidf23\tgensim-notfidf24' input/GensimNoTfidf/differencePhraseSTS.*

#concat files for training with more than one dataset file:
for d in input/*/ ; do

	[ "$d" = "input/alpha1beta1/" ] && continue
	[ "$d" = "input/alpha2beta05/" ] && continue
	[ "$d" = "input/alphabetadefault/" ] && continue

	#images:
	cat "${d}differencePhraseSTS.images.2014.test.preprocessed.tsv" > "${d}differencePhraseSTS.images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.images.2015.train.preprocessed.tsv" >> "${d}differencePhraseSTS.images.train.preprocessed.tsv"
	
	cat "${d}euclideanDistanceSTS.images.2014.test.preprocessed.tsv" > "${d}euclideanDistanceSTS.images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.images.2015.train.preprocessed.tsv" >> "${d}euclideanDistanceSTS.images.train.preprocessed.tsv"
	
	#MSRvid + images:
	cat "${d}differencePhraseSTS.MSRvid.2012.train.preprocessed.tsv" > "${d}differencePhraseSTS.MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.MSRvid.2012.test.preprocessed.tsv" >> "${d}differencePhraseSTS.MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.images.2014.test.preprocessed.tsv" >> "${d}differencePhraseSTS.MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.images.2015.train.preprocessed.tsv" >> "${d}differencePhraseSTS.MSRvid-images.train.preprocessed.tsv"
	
	cat "${d}euclideanDistanceSTS.MSRvid.2012.train.preprocessed.tsv" > "${d}euclideanDistanceSTS.MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.MSRvid.2012.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.images.2014.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.images.2015.train.preprocessed.tsv" >> "${d}euclideanDistanceSTS.MSRvid-images.train.preprocessed.tsv"
	
	#headlines:
	cat "${d}differencePhraseSTS.headlines.2013.test.preprocessed.tsv" > "${d}differencePhraseSTS.headlines.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.headlines.2014.test.preprocessed.tsv" >> "${d}differencePhraseSTS.headlines.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.headlines.2015.train.preprocessed.tsv" >> "${d}differencePhraseSTS.headlines.train.preprocessed.tsv"
	
	cat "${d}euclideanDistanceSTS.headlines.2013.test.preprocessed.tsv" > "${d}euclideanDistanceSTS.headlines.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.headlines.2014.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.headlines.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.headlines.2015.train.preprocessed.tsv" >> "${d}euclideanDistanceSTS.headlines.train.preprocessed.tsv"
	
	#all but MSRvid + images:
	cat "${d}differencePhraseSTS.MSRpar.2012.train.preprocessed.tsv" > "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.MSRpar.2012.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.SMTeuroparl.2012.train.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.SMTeuroparl.2012.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.surprise.OnWN.2012.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.surprise.SMTnews.2012.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.FNWN.2013.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.OnWN.2013.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.deft-forum.2014.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.deft-news.2014.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.OnWN.2014.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.tweet-news.2014.test.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.answers-forums.2015.train.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.answers-students.2015.train.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}differencePhraseSTS.belief.2015.train.preprocessed.tsv" >> "${d}differencePhraseSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	
	cat "${d}euclideanDistanceSTS.MSRpar.2012.train.preprocessed.tsv" > "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.MSRpar.2012.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.SMTeuroparl.2012.train.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.SMTeuroparl.2012.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.surprise.OnWN.2012.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.surprise.SMTnews.2012.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.FNWN.2013.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.OnWN.2013.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.deft-forum.2014.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.deft-news.2014.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.OnWN.2014.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.tweet-news.2014.test.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.answers-forums.2015.train.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.answers-students.2015.train.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	tail -n +2 "${d}euclideanDistanceSTS.belief.2015.train.preprocessed.tsv" >> "${d}euclideanDistanceSTS.all-but-headlines-MSRvid-images.train.preprocessed.tsv"
	
done



#MSRvid + images:
cat "input/STS.gs.MSRvid.2012.train.txt" > "input/STS.gs.MSRvid-images.train.txt"
cat "input/STS.gs.MSRvid.2012.test.txt" >> "input/STS.gs.MSRvid-images.train.txt"
cat "input/STS.gs.images.2014.test.txt" >> "input/STS.gs.MSRvid-images.train.txt"
cat "input/STS.gs.images.2015.train.txt" >> "input/STS.gs.MSRvid-images.train.txt"

cat "input/STS.input.MSRvid.2012.train.txt" > "input/STS.input.MSRvid-images.train.txt"
cat "input/STS.input.MSRvid.2012.test.txt" >> "input/STS.input.MSRvid-images.train.txt"
cat "input/STS.input.images.2014.test.txt" >> "input/STS.input.MSRvid-images.train.txt"
cat "input/STS.input.images.2015.train.txt" >> "input/STS.input.MSRvid-images.train.txt"

#headlines:
cat "input/STS.gs.headlines.2013.test.txt" > "input/STS.gs.headlines.train.txt"
cat "input/STS.gs.headlines.2014.test.txt" >> "input/STS.gs.headlines.train.txt"
cat "input/STS.gs.headlines.2015.train.txt" >> "input/STS.gs.headlines.train.txt"

cat "input/STS.input.headlines.2013.test.txt" > "input/STS.input.headlines.train.txt"
cat "input/STS.input.headlines.2014.test.txt" >> "input/STS.input.headlines.train.txt"
cat "input/STS.input.headlines.2015.train.txt" >> "input/STS.input.headlines.train.txt"

#all but MSRvid + images:
cat "input/STS.gs.MSRpar.2012.train.txt" > "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.MSRpar.2012.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.SMTeuroparl.2012.train.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.SMTeuroparl.2012.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.surprise.OnWN.2012.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.surprise.SMTnews.2012.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.FNWN.2013.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.OnWN.2013.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.deft-forum.2014.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.deft-news.2014.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.OnWN.2014.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.tweet-news.2014.test.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.answers-forums.2015.train.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.answers-students.2015.train.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.gs.belief.2015.train.txt" >> "input/STS.gs.all-but-headlines-MSRvid-images.train.txt"

cat "input/STS.input.MSRpar.2012.train.txt" > "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.MSRpar.2012.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.SMTeuroparl.2012.train.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.SMTeuroparl.2012.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.surprise.OnWN.2012.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.surprise.SMTnews.2012.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.FNWN.2013.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.OnWN.2013.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.deft-forum.2014.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.deft-news.2014.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.OnWN.2014.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.tweet-news.2014.test.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.answers-forums.2015.train.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.answers-students.2015.train.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"
cat "input/STS.input.belief.2015.train.txt" >> "input/STS.input.all-but-headlines-MSRvid-images.train.txt"


