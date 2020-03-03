#!/bin/bash
for id in 2057978 2057980 2057982 2058011
do
  printf "===========================================================\n"
  printf "Fetching match $id from dataset. Please wait...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_match.clj --id=$id --type=edn
  clj src/main/io/spit_match.clj --id=$id --type=json
  printf "===========================================================\n"
  printf "Creating graph...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph.clj --id=$id --type=edn
  clj src/main/io/spit_graph.clj --id=$id --type=json
  printf "===========================================================\n"
  printf "Calculating metrics..."
  printf "This operations may take a while, please be patient...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=json
  printf "Done!\n"
done
