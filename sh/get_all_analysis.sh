#!/bin/bash

. ./sh/match_ids.sh $1

for id in $ids
do
  printf "===========================================================\n"
  printf "Calculating metrics for graph $id \n"
  printf "This operations may take a while, please be patient...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn --championship=$championship
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=json --championship=$championship
  printf "Done!\n"
done

