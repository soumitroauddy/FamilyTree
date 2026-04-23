import 'person.dart';

class FamilyTree {
  const FamilyTree({
    required this.rootId,
    required this.peopleById,
    required this.childIdsByParentId,
    this.expandedIds = const <String>{},
  });

  final String rootId;
  final Map<String, Person> peopleById;
  final Map<String, List<String>> childIdsByParentId;
  final Set<String> expandedIds;

  List<Person> childrenOf(String personId) {
    final childIds = childIdsByParentId[personId] ?? const <String>[];
    return childIds
        .map((id) => peopleById[id])
        .whereType<Person>()
        .toList(growable: false);
  }

  bool isExpanded(String personId) => expandedIds.contains(personId);

  bool isVisible(String personId) {
    if (personId == rootId) {
      return true;
    }
    final parentId = _findParentId(personId);
    if (parentId == null) {
      return false;
    }
    if (!isExpanded(parentId)) {
      return false;
    }
    return isVisible(parentId);
  }

  List<Person> get visibleNodes {
    final result = <Person>[];
    final queue = <_VisibleEntry>[_VisibleEntry(id: rootId, depth: 0)];
    final visited = <String>{};

    while (queue.isNotEmpty) {
      final current = queue.removeAt(0);
      if (!visited.add(current.id)) {
        continue;
      }
      final person = peopleById[current.id];
      if (person == null) {
        continue;
      }
      result.add(
        person.copyWith(
          depth: current.depth,
          isExpanded: isExpanded(current.id),
          childrenIds: childIdsByParentId[current.id] ?? const <String>[],
        ),
      );

      if (!isExpanded(current.id)) {
        continue;
      }
      for (final child in childrenOf(current.id)) {
        queue.add(_VisibleEntry(id: child.id, depth: current.depth + 1));
      }
    }

    return result;
  }

  FamilyTree toggleExpanded(String personId) {
    final next = Set<String>.from(expandedIds);
    if (!next.add(personId)) {
      next.remove(personId);
    }
    return copyWith(expandedIds: next);
  }

  FamilyTree copyWith({
    String? rootId,
    Map<String, Person>? peopleById,
    Map<String, List<String>>? childIdsByParentId,
    Set<String>? expandedIds,
  }) {
    return FamilyTree(
      rootId: rootId ?? this.rootId,
      peopleById: peopleById ?? this.peopleById,
      childIdsByParentId: childIdsByParentId ?? this.childIdsByParentId,
      expandedIds: expandedIds ?? this.expandedIds,
    );
  }

  String? _findParentId(String childId) {
    for (final entry in childIdsByParentId.entries) {
      if (entry.value.contains(childId)) {
        return entry.key;
      }
    }
    return null;
  }
}

class _VisibleEntry {
  const _VisibleEntry({
    required this.id,
    required this.depth,
  });

  final String id;
  final int depth;
}
