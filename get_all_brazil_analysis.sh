#!/bin/bash

source my-project-env/bin/activate

for id in 2057978 2057980 2057982 2058011
do
  echo "==========================================================="
  echo "Calculating metrics for graph $id"
  echo "==========================================================="
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=json
  echo "Done!"
done
