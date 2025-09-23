#!/bin/bash

source .env

## Docker login to ECR - public with correct profile
aws ecr-public get-login-password --region us-east-1 --profile $PROFILE \
  | docker login --username AWS --password-stdin public.ecr.aws

#build
docker build -t $IMAGE_TAG .

#tag
docker tag $IMAGE_TAG:latest $REPOSITORY_URI:$IMAGE_TAG

#push
docker push $REPOSITORY_URI:$IMAGE_TAG

echo "Image $IMAGE_TAG sent to ECR."
