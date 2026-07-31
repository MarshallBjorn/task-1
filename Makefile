.PHONY: help check_env dockerfile_lint build scan push release

help:
	@echo "Available commands:"
	@echo "  make dockerfile_lint SERVICE=<backend|frontend>     - Check Dockerfile with hadolint"
	@echo "  make build SERVICE=<backend|frontend> [TAG=<tag>]   - Build docker image"
	@echo "  make scan SERVICE=<backend|frontend> [TAG=<tag>]    - Scane image with Trivy and Dockle"
	@echo "  make push SERVICE=<backend|frontend> [TAG=<tag>]    - Push image to GHCR"
	@echo "  make release SERVICE=<backend|frontend> [TAG=<tag>] - Run all steps (lint -> build -> scan -> push)"
	@echo ""
	@echo "  Variables:"
	@echo "    SERVICE    - backend or frontend (default: backend)"
	@echo "    TAG        - image tag (default: git short SHA)"
	@echo "    GHCR_TOKEN - GHCR token, mandatory for push, should be placed in env."

SERVICE ?= backend
TAG ?= $(shell git rev-parse --short HEAD)
GHCR_USER ?= marshallbjorn
GHCR_TOKEN ?=
NVD_API_KEY ?=

IMAGE_NAME ?= ghcr.io/marshallbjorn/trippy-$(SERVICE)
DOCKERFILE = $(SERVICE)/Dockerfile

DOCKLE_LATEST := $(shell curl --silent "https://api.github.com/repos/goodwithtech/dockle/releases/latest" | grep '"tag_name":' | sed -E 's/.*"v([^"]+)".*/\1/')
ifeq ($(DOCKLE_LATEST),)
$(warning Could not fetch latest dockle version, using fallback 0.4.15)
endif
DOCKLE_VERSION := $(if $(DOCKLE_LATEST),$(DOCKLE_LATEST),0.4.15)

# Helper, check for correctes of SERVICE arg.
check_env:
ifeq ($(filter $(SERVICE),backend frontend),)
	@echo "Error: SERVICE must be 'backend' or 'frontend', got '$(SERVICE)'"
	@exit 1
endif

# 1.1 Check Dockerfile using hadolint
dockerfile_lint: check_env
	@echo "==> Linting $(DOCKERFILE)"
	docker run --rm -i hadolint/hadolint < $(DOCKERFILE)

# 1.2 Scan filesystem for secrets
secret_scan: check_env
	@echo "==> Trivy Secrets Scan"
	docker run --rm \
		-v $(CURDIR):/src \
		-v trivy-cache:/root/.cache \
		-v ${HOME}/.m2:/root/.m2 \
		aquasec/trivy \
		fs --scanners secret /src
			--exit-code 1

# 2. Build docker image with two tags, numeric and "latest"
build: check_env
	@echo "==> Building image $(IMAGE_NAME):$(TAG)..."
	docker build \
		-t $(IMAGE_NAME):latest \
		-t $(IMAGE_NAME):$(TAG) \
		./$(SERVICE)

# 3.1 Check created image with both dockle and trivy.
scan: check_env
	@echo "==> Scanning image with Trivy..."
	docker run \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v trivy-cache:/root/.cache \
		-v $(CURDIR)/.trivyignore:/work/.trivyignore -w /work \
		--rm aquasec/trivy \
		image $(IMAGE_NAME):$(TAG) \
		--severity HIGH,CRITICAL \
		--exit-code 1

	@echo "==> Scanning image with Dockle..."
	docker run --rm \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v $(CURDIR)/.dockleignore:/.dockleignore \
		goodwithtech/dockle:v$(DOCKLE_VERSION) \
		$(IMAGE_NAME):$(TAG) --exit-code 1 --exit-level fatal 

# 3.2 Create SBOM of image
create_sbom: check_env
	@echo "==> Creating SBOM file with Trivy..."
	docker run \
		-v /var/run/docker.sock:/var/run/docker.sock \
		-v $(CURDIR)/.trivy-cache:/root/.cache \
		-v $(CURDIR)/sbom:/sbom \
		--rm aquasec/trivy \
		image $(IMAGE_NAME):$(TAG) \
		--format cyclonedx --output /sbom/$(SERVICE)-$(TAG)-sbom.json

# 3.3 OWASP Dependency Check
owasp_dep_check: check_env
	@test -n "$(NVD_API_KEY)" || (echo "Error: NVD_API_KEY not set" && exit 1)
	@mkdir -p $(CURDIR)/odc-report
	@mkdir -p $(CURDIR)/.nvd-cache

	@echo "==> Cleaning up potential stale H2 locks..."
	@rm -f $(CURDIR)/.nvd-cache/*.lock.db
	@rm -f $(CURDIR)/.nvd-cache/odc.update.lock

	@echo "==> Checking image with OWASP Dependency Check..."
	@docker run --rm \
		-v $(CURDIR)/$(SERVICE):/src \
		-v $(HOME)/.m2:/root/.m2 \
		-v $(CURDIR)/.nvd-cache:/usr/share/dependency-check/data \
		-v $(CURDIR)/odc-report:/report \
		owasp/dependency-check \
			--scan /src \
			--format HTML \
			--out /report \
			--nvdApiKey $(NVD_API_KEY) \
			--project "trippy-$(SERVICE)"

# 4. Push image to registry
push: check_env
	@test -n "$(GHCR_TOKEN)" || (echo "Error: GHCR_TOKEN not set" && exit 1)
	@echo "==> Logging into GHCR..."
	@echo "$(GHCR_TOKEN)" | docker login ghcr.io -u $(GHCR_USER) --password-stdin
	@echo "==> Pushing images..."
	
	docker push $(IMAGE_NAME):$(TAG)
	docker push $(IMAGE_NAME):latest

# For developement tests, not CI.
release: dockerfile_lint build scan push
	@echo "==> Release process for $(SERVICE) completed successfully."
	