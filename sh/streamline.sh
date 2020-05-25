#!/bin/bash

. ./sh/match_ids.sh $1

if [ $2 ]
then
  for id in $2
  do
    printf "===========================================================\n"
    printf "Fetching match $id from dataset. Please wait...\n"
    printf "===========================================================\n"
    clj src/main/io/spit_match.clj --id=$id --championship=$championship
    printf "===========================================================\n"
    printf "Creating graph...\n"
    printf "===========================================================\n"
    clj src/main/io/spit_graph.clj --id=$id --championship=$championship
    printf "===========================================================\n"
    printf "Calculating metrics...\n"
    printf "This operations may take a while, please be patient...\n"
    printf "===========================================================\n"
    clj src/main/io/spit_graph_analysis.clj --id=$id --championship=$championship
    printf "Done!\n"
  done
  printf "===========================================================\n"
  printf "Generating filenames... \n"
  clj src/main/io/spit_filenames.clj
  printf "===========================================================\n"
  printf "Generating missing files... \n"
  clj src/main/io/spit_missing.clj
  printf "===========================================================\n"
else
  printf "===========================================================\n"
  printf "Fetching matches of $championship from dataset. Please wait...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_match.clj --id="$ids" --championship=$championship
  printf "Done!\n"

  printf "===========================================================\n"
  printf "Creating graphs for $championship. Please wait...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph.clj --id="$ids" --championship=$championship
  printf "Done!\n"

  printf "===========================================================\n"
  printf "Calculating metrics for $championship. \n"
  printf "This operations may take a while, please be patient...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph_analysis.clj --id="$ids" --championship=$championship
  printf "Done!\n"

  printf "===========================================================\n"
  printf "Generating filenames... \n"
  clj src/main/io/spit_filenames.clj
  printf "===========================================================\n"
  printf "Generating missing files... \n"
  clj src/main/io/spit_missing.clj
  printf "===========================================================\n"
fi

