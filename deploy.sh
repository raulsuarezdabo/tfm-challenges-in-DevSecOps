#!/usr/bin/env bash

echo "$KEY_FILE" >> "$FILE_NAME"
gcloud auth activate-service-account --key-file "$FILE_NAME"
rm file.json
# shellcheck disable=SC2086
gcloud container clusters get-credentials "$CLUSTER_ID" --zone "$CLUSTER_ZONE" --project $PROJECT_ID
kubectl apply -f kube.yml

