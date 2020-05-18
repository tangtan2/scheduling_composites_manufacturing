#!/usr/bin/env bash

# Set parameters
fileName="chap3_4000_3"
mainFolder="/Users/tanyatang/Documents/scheduling_composites_manufacturing/data/temp"
instanceFolder="/instances_042220"
numJobs=("/jobs_4000")
numInstances=30
modelTypes=("3")
#repetitions=("100")

# Copy files over to new directories if does not exist
for model in "${modelTypes[@]}"
do
  src="${mainFolder}${instanceFolder}/"
  dest="${mainFolder}${instanceFolder}_${model}/"
  if [ ! -d "${dest}" ]
  then
    mkdir -p ${dest}
    cp -rf ${src} ${dest}
  fi
done

# Create experiment parameter text file
touch "${fileName}.txt"
if [ "${repetitions}" == "" ]
then
  for num in "${numJobs[@]}"
  do
    for model in "${modelTypes[@]}"
    do
      for ((i=0;i<numInstances;i++))
      do
        filePath="${mainFolder}${instanceFolder}_${model}${num}/instance_${i}.xlsx"
        sumFilePath="${mainFolder}${instanceFolder}_${model}${num}/summary.xlsx"
        interFilePath="${mainFolder}${instanceFolder}_${model}${num}/intermediates.txt"
        newLine="${model},${filePath},${sumFilePath},${interFilePath},${i}"
        echo -e ${newLine} >> "${fileName}.txt"
      done <"${fileName}.txt"
    done
  done
else
  for num in "${numJobs[@]}"
  do
    for ((j=0;j<${#modelTypes[@]};j++))
    do
      for ((i=0;i<numInstances;i++))
      do
        filePath="${mainFolder}${instanceFolder}_${model}${num}/instance_${i}.xlsx"
        sumFilePath="${mainFolder}${instanceFolder}_${model}${num}/summary.xlsx"
        interFilePath="${mainFolder}${instanceFolder}_${model}${num}/intermediates.txt"
        newLine="${modelTypes[${j}]},${filePath},${sumFilePath},${interFilePath},${i},${repetitions[${j}]}"
        echo -e ${newLine} >> "${fileName}.txt"
      done <"${fileName}.txt"
    done
  done
fi
