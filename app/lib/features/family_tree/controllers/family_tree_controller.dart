import 'package:flutter/foundation.dart';
import 'package:flutter/painting.dart';

import '../data/family_repository.dart';
import '../models/family_tree.dart';
import '../models/person.dart';
import '../models/tree_layout.dart';

class FamilyTreeController extends ChangeNotifier {
  FamilyTreeController({
    required FamilyRepository repository,
  }) : _repository = repository,
       _familyTree = repository.load() {
    _rebuildLayout();
  }

  final FamilyRepository _repository;
  FamilyTree _familyTree;
  int _layoutVersion = 0;
  TreeLayout _layout = const TreeLayout(
    nodes: <TreeNodeLayout>[],
    connectors: <TreeConnector>[],
    canvasSize: Size(1, 1),
    version: 0,
  );

  FamilyTree get familyTree => _familyTree;
  TreeLayout get layout => _layout;
  int get expandedCount => _familyTree.expandedIds.length;

  void toggleExpanded(Person person) {
    _familyTree = _familyTree.toggleExpanded(person.id);
    _rebuildLayout();
    notifyListeners();
  }

  void reload() {
    _familyTree = _repository.load();
    _rebuildLayout();
    notifyListeners();
  }

  void _rebuildLayout() {
    _layoutVersion += 1;
    _layout = TreeLayout.fromTree(_familyTree, version: _layoutVersion);
  }
}
