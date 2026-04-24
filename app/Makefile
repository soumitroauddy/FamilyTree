.PHONY: run-macos clean-macos

# The project lives inside ~/Documents which is synced by iCloud Drive. The iCloud file
# provider daemon stamps every .app/.framework bundle directory with com.apple.FinderInfo
# and com.apple.fileprovider.fpfs#P extended attributes that cannot be removed (the daemon
# re-adds them immediately). codesign rejects these as "resource fork, Finder information,
# or similar detritus not allowed".
#
# Fix: symlink the build directory to a path outside iCloud's reach so that Xcode creates
# bundle directories in an un-synced filesystem location.
BUILD_LINK := build
BUILD_REAL := /tmp/flutter-family-tree-build

run-macos:
	@if [ ! -L "$(BUILD_LINK)" ]; then \
	  rm -rf "$(BUILD_LINK)" 2>/dev/null || true; \
	  mkdir -p "$(BUILD_REAL)"; \
	  ln -s "$(BUILD_REAL)" "$(BUILD_LINK)"; \
	  echo "Symlinked $(BUILD_LINK) -> $(BUILD_REAL) (outside iCloud)"; \
	fi
	flutter run -d macos

clean-macos:
	flutter clean
	rm -rf "$(BUILD_REAL)" 2>/dev/null || true
