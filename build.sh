#!/bin/bash

# Define RELEASE=1 before running to build a release.

# Compiler

echo "Downloading and updating compiler..."
if git clone https://github.com/Feodor0090/j2me_compiler.git 2>/dev/null
then
  echo "Done."
else
  echo "Already downloaded."
fi
cd j2me_compiler
git pull
cd ..

# Manifest

cp Application\ Descriptor manifest.mf
echo -en "Commit: " >> manifest.mf
git rev-parse --short HEAD >> manifest.mf

WORK_DIR=`dirname $0`
cd ${WORK_DIR}


mkdir -p jar

APP=mm_v1

# STATIC VARS
JAVA_HOME=./j2me_compiler/jdk1.6.0_45
WTK_HOME=./j2me_compiler/WTK2.5.2
PROGUARD=./j2me_compiler/proguard/bin/proguard.sh
RES=res
MANIFEST=manifest.mf
PATHSEP=":"
JAVAC=javac
JAR=jar

# DYNAMIC VARS
LIB_DIR=${WTK_HOME}/lib
CLDCAPI=${LIB_DIR}/cldcapi11.jar
MIDPAPI=${LIB_DIR}/midpapi20.jar
PREVERIFY=${WTK_HOME}/bin/preverify
TCP=${LIB_DIR}/*
CLASSPATH=`echo $TCP | sed "s/ /:/g"`

if [ -n "${JAVA_HOME}" ] ; then
  JAVAC=${JAVA_HOME}/bin/javac
  JAR=${JAVA_HOME}/bin/jar
fi

# ACTION
echo "Working on" ${APP}
pwd
echo "Creating or cleaning directories..."
mkdir -p ./tmpclasses
mkdir -p ./classes
rm -rf ./tmpclasses/*
rm -rf ./classes/*

echo "Compiling source files..."
${JAVAC} \
    -bootclasspath ${CLDCAPI}${PATHSEP}${MIDPAPI} \
    -source 1.3 \
    -target 1.3 \
    -d ./tmpclasses \
    -classpath ./tmpclasses${PATHSEP}${CLASSPATH} \
    `find ./src -name '*'.java`
if [ $? -eq 0 ]
then
  echo "Compilation ok!"
else
  exit 1
fi
echo "Preverifying class files..."
${PREVERIFY} \
    -classpath ${CLDCAPI}${PATHSEP}${MIDPAPI}${PATHSEP}${CLASSPATH}${PATHSEP}./tmpclasses \
    -d ./classes \
    ./tmpclasses
if [ $? -eq 0 ]
then
  echo "Preverify ok!"
else
  exit 1
fi

echo "Jaring preverified class files..."
${JAR} cmf ${MANIFEST} ${APP}.jar -C ./classes .

if [ -d ${RES} ] ; then
  ${JAR} uf ${APP}.jar -C ${RES} .
fi

echo "Build done!" ./${APP}.jar

echo "Removing location classes..."
rm ./classes/mahomaps/map/LocationAPI*

echo "Jaring preverified class files..."
${JAR} cmf ${MANIFEST} ${APP}_no_geo.jar -C ./classes .

if [ -d ${RES} ] ; then
  ${JAR} uf ${APP}_no_geo.jar -C ${RES} .
fi

echo Optimizing ${APP}
chmod +x ${PROGUARD}
touch cf.cfg

cat proguard.basecfg > cf.cfg
echo "-injars ./${APP}.jar" >> cf.cfg
echo "-outjar ./${APP}_obf.jar" >> cf.cfg
echo "-printseeds ./${APP}_obf_seeds.txt" >> cf.cfg
echo "-printmapping ./${APP}_obf_map.txt" >> cf.cfg
echo "-libraryjars ${CLASSPATH}" >> cf.cfg

${PROGUARD} @cf.cfg
