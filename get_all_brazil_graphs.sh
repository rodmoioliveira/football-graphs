#!/bin/bash
for id in 2057978 2057980 2057982 2058011
do
  echo "==========================================================="
  echo "Creating graph for match $id"
  echo "==========================================================="
  clj src/main/io/spit_graph.clj --id=$id --type=edn
  clj src/main/io/spit_graph.clj --id=$id --type=json
  echo "Done!"
done
