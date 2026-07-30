.PHONY: help push_image

help:
	@echo "Available commands:"
	@echo ""
	@echo "  make push_image SERVICE=<backend|frontend> [TAG=<tag>]"
	@echo ""
	@echo "  Scans Dockerfile, builds image, scans image and push to GHCR."
	@echo ""
	@echo "  Variables:"
	@echo "    SERVICE    - backend or frontend (default: backend)"
	@echo "    TAG	      - image tag (default: git short SHA)"
	@echo "    GHCR_TOKEN - GHCR token, mandatory for push"

SERVICE ?= backend
TAG ?= $(shell git rev-parse --short HEAD)

GHCR_USER ?= marshallbjorn
GHCR_TOKEN ?=

IMAGE_NAME ?= ghcr.io/marshallbjorn/trippy-$(SERVICE)
DOCKERFILE = $(SERVICE)/Dockerfile

DOCKLE_LATEST := $(shell curl --silent "https://api.github.com/repos/goodwithtech/dockle/releases/latest" | grep '"tag_name":' | sed -E 's/.*"v([^"]+)".*/\1/')
ifeq ($(DOCKLE_LATEST),)
$(warning Could not fetch latest dockle version, using fallback 0.4.15)
endif
DOCKLE_VERSION := $(if $(DOCKLE_LATEST),$(DOCKLE_LATEST),0.4.15)


push_image:
ifeq ($(filter $(SERVICE),backend frontend),)
	@echo "Error: SERVICE must be 'backend' or 'frontend', got '$(SERVICE)'"
	@exit 1
endif

	# 1. Check Dockerfile using hadolint
	docker run --rm -i hadolint/hadolint < $(DOCKERFILE)

	# 2. Build docker image with two tags, numeric and "latest"
	docker build \
		-t $(IMAGE_NAME):latest \
		-t $(IMAGE_NAME):$(TAG) \
		./$(SERVICE)

	# 3. Check created image with both dockle and trivy.
	docker run \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v trivy-cache:/root/.cache \
		--rm aquasec/trivy \
		image $(IMAGE_NAME):$(TAG) \
		--severity HIGH,CRITICAL \
		--exit-code 1

	docker run --rm \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v $(shell pwd)/.dockleignore:/.dockleignore \
		goodwithtech/dockle:v$(DOCKLE_VERSION) \
		$(IMAGE_NAME):$(TAG) --exit-code 1 --exit-level fatal 

	# 4. Push image to registry
	@test -n "$(GHCR_TOKEN)" || (echo "Error: GHCR_TOKEN not set" && exit 1)
	@echo "$(GHCR_TOKEN)" | docker login ghcr.io -u $(GHCR_USER) --password-stdin
	
	docker push $(IMAGE_NAME):$(TAG)
	docker push $(IMAGE_NAME):latest