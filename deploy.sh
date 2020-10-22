#!/usr/bin/env bash

PROJECT_ID="first-cluster-293016"
CLUSTER_ID="cluster-tfm-devsecop-travisci"
CLUSTER_ZONE="europe-west1-b"


# Make sure we have gcloud installed in travis env
#if [ ! -d "$HOME/google-cloud-sdk/bin" ]; then
#  rm -rf "$HOME/google-cloud-sdk"
#  curl https://sdk.cloud.google.com | bash > /dev/null
#fi

# Promote gcloud to PATH top priority (prevent using old version from travis)
#source $HOME/google-cloud-sdk/path.bash.inc

# Make sure kubectl is updated to latest version
#gcloud components update kubectl
echo $KEY_FILE >> file.json
gcloud auth activate-service-account --key-file file.json
rm file.json
gcloud container clusters get-credentials $CLUSTER_ID --zone $CLUSTER_ZONE --project $PROJECT_ID
kubectl apply -f kube.yml

