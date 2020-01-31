#!/bin/bash
for id in 2057978 2057980 2057982 2058011 2058006
do
  echo "Getting match $id"
  clj src/main/io/spit_match.clj --id=$id --type=edn
  clj src/main/io/spit_match.clj --id=$id --type=json
  echo "Done with match $id"
done
