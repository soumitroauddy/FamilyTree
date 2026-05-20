import 'package:flutter/foundation.dart';
import 'package:flutter/painting.dart';

import '../data/family_repository.dart';
import '../models/family_tree.dart';
import '../models/person.dart';
import '../models/tree_layout.dart';

class FamilyTreeController extends ChangeNotifier {
  FamilyTreeController({
    required FamilyRepository repository,
    String? familyId,
  })  : _repository = repository,
        _familyId = familyId,
        _familyTree = repository.load() {
    _rebuildLayout();
    if (familyId != null) {
      loadFromBackend();
    }
  }

  final FamilyRepository _repository;
  final String? _familyId;
  FamilyTree _familyTree;
  int _layoutVersion = 0;
  bool _isLoading = false;
  String? _error;

  TreeLayout _layout = const TreeLayout(
    nodes: <TreeNodeLayout>[],
    connectors: <TreeConnector>[],
    canvasSize: Size(1, 1),
    version: 0,
  );

  FamilyTree get familyTree => _familyTree;
  TreeLayout get layout => _layout;
  int get expandedCount => _familyTree.expandedIds.length;
  bool get isLoading => _isLoading;
  String? get error => _error;

  void toggleExpanded(Person person) {
    _familyTree = _familyTree.toggleExpanded(person.id);
    _rebuildLayout();
    notifyListeners();
  }

  void reload() {
    if (_familyId != null) {
      loadFromBackend();
    } else {
      _familyTree = _repository.load();
      _rebuildLayout();
      notifyListeners();
    }
  }

  Future<void> loadFromBackend() async {
    if (_familyId == null) return;
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _familyTree = await _repository.loadFromBackend(_familyId);
      _rebuildLayout();
    } catch (e) {
      _error = e.toString();
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void _rebuildLayout() {
    _layoutVersion += 1;
    _layout = TreeLayout.fromTree(_familyTree, version: _layoutVersion);
  }
}
