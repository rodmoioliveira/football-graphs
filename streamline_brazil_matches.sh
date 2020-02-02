#!/bin/bash
for id in 2057978 2057980 2057982 2058011
do
  echo "==========================================================="
  echo "Fetching match $id from dataset. Please wait..."
  echo "==========================================================="
  clj src/main/io/spit_match.clj --id=$id --type=edn
  clj src/main/io/spit_match.clj --id=$id --type=json
  echo "==========================================================="
  echo "Creating graph..."
  echo "==========================================================="
  clj src/main/io/spit_graph.clj --id=$id --type=edn
  clj src/main/io/spit_graph.clj --id=$id --type=json
  echo "==========================================================="
  echo "Calculating metrics..."
  echo "==========================================================="
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=edn
  clj src/main/io/spit_graph_analysis.clj --id=$id --type=json
  echo "Done!"
done
