#!/bin/bash

. ./sh/match_ids.sh $1

for id in $ids
do
  printf "===========================================================\n"
  printf "Fetching match $id from dataset. Please wait...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_match.clj --id=$id --type=edn --championship=$championship
  clj src/main/io/spit_match.clj --id=$id --type=json --championship=$championship
  printf "===========================================================\n"
  printf "Creating graph...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph.clj --id=$id --type=edn --championship=$championship
  clj src/main/io/spit_graph.clj --id=$id --type=json --championship=$championship
  printf "===========================================================\n"
  printf "Calculating metrics...\n"
  printf "This operations may take a while, please be patient...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn --championship=$championship
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=json --championship=$championship
  printf "Done!\n"
done
printf "===========================================================\n"
printf "Generating filenames... \n"
clj src/main/io/spit_filenames.clj
printf "===========================================================\n"
printf "Generating missing files... \n"
clj src/main/io/spit_missing.clj
printf "===========================================================\n"
