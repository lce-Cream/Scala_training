#!/bin/bash

# All of this could've been avoided if I weren't using Windows and could pass
# absolute path in Dockerfile.
# This script builds and loads Spark image in minikube.

KUBEDIR="$SPARK_HOME\kubernetes\dockerfiles\spark"
BUILD_ARGS=""

function usage {
  cat <<EOF
Usage: $0 [options]
Builds and loads the built-in Spark Docker image in minikube.

Options:
  -m          Use minikube's Docker daemon.
  -t          Image tag.
  -h          Print this help message.

Using minikube when building images will do so directly into minikube's Docker daemon.
There is no need to push the images into minikube in that case, they'll be automatically
available when running applications inside the minikube cluster.

Examples:
  - Build image in minikube with tag "testing"
    $0 -m -t testing

  - Build docker image
    $0 -t testing

EOF
}

while getopts "t:mh" option
do
 case "${option}"
 in
 t)
   BUILD_ARGS+="-${option} ${OPTARG} "
   ;;
 m)
   if ! which minikube 1>/dev/null; then
     error "Cannot find minikube."
   fi
   if ! minikube status 1>/dev/null; then
     error "Cannot contact minikube. Make sure it's running."
   fi

   BUILD_ARGS+="-${option} ${OPTARG} "
   ;;
  h | *)
    usage
    exit 0
    ;;
 esac
done

# move jars folder to Spark kubernetes folder
cp -r "../jars" "$KUBEDIR"

# append new layers to the standard Spark Dockerfile
{
  echo "COPY /kubernetes/dockerfiles/spark/jars /app"
  echo "ENV PATH=/opt/spark/bin:\${PATH}"
  echo "WORKDIR /app"
} >> "$KUBEDIR\Dockerfile"

# launch actual build
command="docker-image-tool.sh $BUILD_ARGS build"
$command

# remove all layers from the standard Spark Dockerfile leaving it untouched
cat "$KUBEDIR\Dockerfile" | head -n -3 > tmp && mv tmp "$KUBEDIR\Dockerfile"
