#!/bin/bash -eu

declare -i count=0

while read -r dep
do
    printf "${FORMAT?}" $dep ${SUBJECT?} |
        tee --append ${TEST_UNDECLARED_OUTPUTS_DIR}/unused-deps.txt
    count+=1
done < <(grep --invert-match --line-regexp --fixed-strings --file=${IGNORE?} ${UNUSED_DEPS?})

test $count -eq 0