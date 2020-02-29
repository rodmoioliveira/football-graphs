#!/bin/bash
for id in 2057978 2057980 2057982 2058011
do
  printf "===========================================================\n"
  printf "Creating graph for match $id \n"
  printf "===========================================================\n"
  clj src/main/io/spit_graph.clj --id=$id --type=edn
  clj src/main/io/spit_graph.clj --id=$id --type=json
  printf "Done!"
done
