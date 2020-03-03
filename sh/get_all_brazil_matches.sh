#!/bin/bash
for id in 2057978 2057980 2057982 2058011 2058006
do
  printf "===========================================================\n"
  printf "Fetching match $id from dataset. Please wait...\n"
  printf "===========================================================\n"
  clj src/main/io/spit_match.clj --id=$id --type=edn
  clj src/main/io/spit_match.clj --id=$id --type=json
  printf "Done!"
done
