.PHONY: help push_image

help:

TAG = git rev-parse --short HEAD
DOCKERFILE = backend/Dockerfile

push_image:
	# 1. Check Dockerfile using hadolint
	docker run --rm -i hadolint/hadolint < $(DOCKERFILE)

	# 2. Build docker image with two tags, numeric and "latest"
	docker build \
		-f $(DOCKERFILE)
		-t ghcr.io/marshallbjorn/trippy-backend:latest \
		-t ghcr.io/marshallbjorn/trippy-backend:$(TAG)

	# 3. Check created image with both dockle and trivy.
	docker run --rm -i aquasec/trivy 
	