#!/bin/bash -eu

declare -i count=0

while read -r dep
do
    printf "${FORMAT?}" $dep ${SUBJECT?}
    count+=1
done < <(grep --invert-match --line-regexp --fixed-strings --file=${IGNORE?} ${UNUSED_DEPS?})

test $count = 0