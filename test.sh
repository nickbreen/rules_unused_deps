#!/bin/bash -eu

declare -i count=0 inc
while read -r inc _
do
    count+=$inc
done < <(wc -l ${UNUSED_DEPS?})
if [ $count -gt 0 ]
then
    cat ${UNUSED_DEPS?} | while read -r dep
    do
        printf "${FORMAT?}" $dep ${SUBJECT?}
    done
    exit 65
fi
