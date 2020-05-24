#!/bin/bash

. ./sh/match_ids.sh $1

for id in $ids
do
  printf "===========================================================\n"
  printf "Getting match $id \n"
  printf "===========================================================\n"
  clj src/main/io/spit_match.clj --id=$id --championship=$championship
  printf "Done!\n"
done

