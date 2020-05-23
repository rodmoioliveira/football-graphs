#!/bin/bash

. ./sh/match_ids.sh $1

for id in $ids
do
  printf "===========================================================\n"
  printf "Creating graph for match $id \n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph.clj --id=$id --type=edn --championship=$championship
  clj src/main/io/spit_graph.clj --id=$id --type=json --championship=$championship
  printf "Done!"
done

