.PHONY: help push_image

help:

TAG := $(shell git rev-parse --short HEAD)
IMAGE_NAME = ghcr.io/marshallbjorn/trippy-backend
DOCKERFILE = backend/Dockerfile

DOCKLE_VERSION := $(shell curl --silent "https://api.github.com/repos/goodwithtech/dockle/releases/latest" | grep '"tag_name":' | sed -E 's/.*"v([^"]+)".*/\1/')


push_image:
	# 1. Check Dockerfile using hadolint
	docker run --rm -i hadolint/hadolint < $(DOCKERFILE)

	# 2. Build docker image with two tags, numeric and "latest"
	docker build \
		-t $(IMAGE_NAME):latest \
		-t $(IMAGE_NAME):$(TAG) \
		./backend

	# 3. Check created image with both dockle and trivy.
	docker run \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v trivy-cache:/root/.cache \
		--rm aquasec/trivy \
		--exit-code 0 \
		--severity HIGH, CRITICAL
		image $(IMAGE_NAME):$(TAG) \
		
	# CHANGE EXIT CODE TO 1 LATER

	docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
		goodwithtech/dockle:v$(DOCKLE_VERSION) $(IMAGE_NAME):$(TAG)