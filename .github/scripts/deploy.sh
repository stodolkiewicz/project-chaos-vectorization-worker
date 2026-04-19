#!/bin/bash
set -euo pipefail

IMAGE=${{ vars.ARTIFACT_REGISTRY_REGION }}-docker.pkg.dev/${{ secrets.GCP_PROJECT_ID }}/${{ vars.ARTIFACT_REPO }}/project-chaos-vectorization-worker:${{ github.ref_name }}

gcloud auth configure-docker ${{ vars.ARTIFACT_REGISTRY_REGION }}-docker.pkg.dev --quiet
sudo docker pull $IMAGE

sudo docker stop project-chaos-vectorization-worker --time=30 2>/dev/null || true
sudo docker rm project-chaos-vectorization-worker 2>/dev/null || true

sudo docker run -d \
  --name project-chaos-vectorization-worker \
  --restart unless-stopped \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_AI_OPENAI_API_KEY=$(gcloud secrets versions access latest --secret=SPRING_AI_OPENAI_API_KEY) \
  -e PROD_DB_NAME=$(gcloud secrets versions access latest --secret=PROD_DB_NAME) \
  -e PROD_DB_USERNAME=$(gcloud secrets versions access latest --secret=PROD_DB_USERNAME) \
  -e PROD_DB_PASSWORD=$(gcloud secrets versions access latest --secret=PROD_DB_PASSWORD) \
  -e GCP_SQL_INSTANCE_CONNECTION_NAME=$(gcloud secrets versions access latest --secret=GCP_SQL_INSTANCE_CONNECTION_NAME) \
  -e BOOTSTRAP_SERVERS=$(gcloud secrets versions access latest --secret=KAFKA_BOOTSTRAP_SERVERS) \
  -e SCHEMA_REGISTRY_URL=$(gcloud secrets versions access latest --secret=SCHEMA_REGISTRY_URL) \
  $IMAGE

sudo docker image prune -f
