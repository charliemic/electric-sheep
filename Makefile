# Makefile for Electric Sheep Development
# Provides convenient shortcuts for common tasks

.PHONY: help test-emulator test-emulator-setup test-emulator-verbose test-emulator-file

help: ## Show this help message
	@echo "Electric Sheep - Development Commands"
	@echo ""
	@echo "Emulator Management Testing:"
	@echo "  make test-emulator-setup     Run setup for emulator tests"
	@echo "  make test-emulator          Run all emulator management tests"
	@echo "  make test-emulator-verbose  Run tests with verbose output"
	@echo ""
	@echo "Examples:"
	@echo "  make test-emulator-setup"
	@echo "  make test-emulator"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

test-emulator-setup: ## Run setup for emulator management tests
	@./scripts/tests/setup.sh

test-emulator: ## Run all emulator management tests
	@./scripts/tests/run_tests.sh

test-emulator-verbose: ## Run emulator tests with verbose output
	@./scripts/tests/run_tests.sh --verbose

test-emulator-file: ## Run specific test file (usage: make test-emulator-file FILE=test_emulator_lock_manager.bats)
	@if [ -z "$(FILE)" ]; then \
		echo "Error: FILE not specified"; \
		echo "Usage: make test-emulator-file FILE=test_emulator_lock_manager.bats"; \
		exit 1; \
	fi
	@./scripts/tests/run_tests.sh $(FILE)

