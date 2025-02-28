 DOCKER ?= docker

.PHONY: lint
lint:
	@echo "Running linter..."
	@cargo clippy -- -D warnings

.PHONY: build
build:
	@echo "Building..."
	@cargo build

