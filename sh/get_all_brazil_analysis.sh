#!/bin/bash
for id in 2057978 2057980 2057982 2058011
do
  printf "===========================================================\n"
  printf "Calculating metrics for graph $id"
  printf "This operations may take a while, please be patient..."
  printf "===========================================================\n"
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=json
  printf "Done!"
done
