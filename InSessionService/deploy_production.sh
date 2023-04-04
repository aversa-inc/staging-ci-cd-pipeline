#!/usr/bin/env bash

# Build the project
./gradlew clean && ./gradlew build

if [[ $? -eq 0 ]]; then
  tag=latest #$(openssl rand -hex 6)

  # Build the image
  docker build -t "871653551639.dkr.ecr.us-east-1.amazonaws.com/insession:$tag" .

  # Get ECR login creds, and deploy
  aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 871653551639.dkr.ecr.us-east-1.amazonaws.com
  docker push "871653551639.dkr.ecr.us-east-1.amazonaws.com/insession:$tag"

  # Deploy
#  cd ../InSessionCDK
#  export INSESSION_SERVICE_IMAGE_TAG=$tag
#  cdk deploy insession-service-ecsservicestack-beta
else
  echo "Gradle build failed"
fi