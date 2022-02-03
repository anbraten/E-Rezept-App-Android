# Release 1.2.1
# Release 1.2.1

### Added

- Support for multiple profiles/health cards (Not enabled yet)
- NFC troubleshooting
- Display differences in data terms since accepting it originally
- Konnektathon build-type

### Changed

- Refactoring of audit events processing
- Update health insurances list
- Updated Readme
- Moved audit events to profile details
- Moved loading of audit events to background 

### Fixed

- Token-Display in Demo Mode
- SafetyNet handling on devices without Google services

### Internal

- Restructured database-schemes

# Release 1.1.0
- Insurance list is now up to date.
- Onboarding enforces a password strength of at least two (yellow indicator)
- Multiprofile handling is currently hidden behind a feature flag.
  - The settings menu is now located at the bottom right.
  - Enables to add/edit/delete profiles.
- This repo is now a kotlin multiplatform project including the android and the desktop app. This is currently work in progress and the code between both variants is not yet aligned to each other.
- Fasttrack (authentication without a health card) is currently behind a feature flag.
- The NFC reading is now blocked after an error occurs (e.g. PIN & CAN wrong).
- Add precise & coarse location support for Android 12.
- Fixes db migration.
- Several other bug fixes.

# Release 1.0.13
1.0.13

# Release 1.0.12
1.0.12

# Release 1.0.9
Release 1.0.9

# Release 1.0.7
Initial public release
