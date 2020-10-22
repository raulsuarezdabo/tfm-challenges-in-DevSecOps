#!/usr/bin/env bash

PROJECT_ID=$1
CLUSTER_ID=$2
CLUSTER_ZONE=$3


# Make sure we have gcloud installed in travis env
#if [ ! -d "$HOME/google-cloud-sdk/bin" ]; then
#  rm -rf "$HOME/google-cloud-sdk"
#  curl https://sdk.cloud.google.com | bash > /dev/null
#fi

# Promote gcloud to PATH top priority (prevent using old version from travis)
#source $HOME/google-cloud-sdk/path.bash.inc

# Make sure kubectl is updated to latest version
#gcloud components update kubectl
echo "Project: $PROJECT_ID with cluster ID: $CLUSTER_ID at zone: $CLUSTER_ZONE"
gcloud auth activate-service-account --key-file $KEY_FILE
gcloud container clusters get-credentials $CLUSTER_ID --zone $CLUSTER_ZONE --project $PROJECT_ID
kubectl apply -f kube.yml

