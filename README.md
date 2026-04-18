# FamilyTree

Interactive Flutter family tree app for iOS and Android with:

- Zoom + pan canvas (`InteractiveViewer`)
- Expand/collapse branches with animated transitions
- Node cards with photo, name, and life span label
- Connector rendering with horizontal + vertical edges
- Tap-to-open person details sheet
- Modular data/domain/UI architecture ready for backend integration

## Project structure

```text
lib/
  app/
    app.dart
  core/
    constants/
      app_constants.dart
    theme/
      app_theme.dart
  features/
    family_tree/
      controllers/
        family_tree_controller.dart
      data/
        family_repository.dart
        sample_family_data.dart
      models/
        family_tree.dart
        person.dart
        tree_layout.dart
      screens/
        family_tree_screen.dart
      widgets/
        family_tree_node_card.dart
        family_tree_painter.dart
        person_avatar.dart
        person_details_sheet.dart
  main.dart
test/
  family_tree_controller_test.dart
pubspec.yaml
```

## Run locally

1. Install Flutter SDK (stable channel) and verify:

   ```bash
   flutter doctor
   ```

2. Get dependencies:

   ```bash
   flutter pub get
   ```

3. Run the app:

   ```bash
   flutter run
   ```

4. Run tests:

   ```bash
   flutter test
   ```

## Notes

- Data currently comes from `SampleFamilyData.tree()`.
- To connect a backend later, replace `FamilyRepository.load()` with API or local DB integration while keeping controller and widgets unchanged.
