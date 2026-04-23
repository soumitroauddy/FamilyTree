import 'package:family_tree_app/features/family_tree/controllers/family_tree_controller.dart';
import 'package:family_tree_app/features/family_tree/data/family_repository.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('controller toggles expansion and updates layout version', () {
    final controller = FamilyTreeController(repository: const FamilyRepository());
    controller.load();

    final before = controller.layout;
    expect(before.nodes.length, greaterThan(0));

    final root = controller.familyTree.peopleById[controller.familyTree.rootId]!;
    controller.toggleExpanded(root);
    final after = controller.layout;

    expect(after.version, greaterThan(before.version));
  });
}
