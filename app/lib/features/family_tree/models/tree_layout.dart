import 'dart:math' as math;

import 'package:flutter/material.dart';

import 'family_tree.dart';
import 'person.dart';

class TreeConnector {
  const TreeConnector({
    required this.parentId,
    required this.childId,
    required this.start,
    required this.end,
  });

  final String parentId;
  final String childId;
  final Offset start;
  final Offset end;
}

class TreeNodeLayout {
  const TreeNodeLayout({
    required this.person,
    required this.position,
    required this.rect,
    required this.hasChildren,
  });

  final Person person;
  final Offset position;
  final Rect rect;
  final bool hasChildren;
}

class TreeLayout {
  const TreeLayout({
    required this.nodes,
    required this.connectors,
    required this.canvasSize,
    required this.version,
  });

  static const Size nodeSize = Size(220, 110);
  static const double horizontalSpacing = 56;
  static const double verticalSpacing = 72;
  static const double canvasPadding = 44;

  final List<TreeNodeLayout> nodes;
  final List<TreeConnector> connectors;
  final Size canvasSize;
  final int version;

  static TreeLayout fromTree(
    FamilyTree tree, {
    required int version,
  }) {
    final visibleNodes = tree.visibleNodes;
    final levelToNodes = <int, List<Person>>{};
    for (final person in visibleNodes) {
      levelToNodes.putIfAbsent(person.depth, () => <Person>[]).add(person);
    }
    for (final people in levelToNodes.values) {
      people.sort((a, b) => a.id.compareTo(b.id));
    }

    final maxLevel =
        levelToNodes.keys.isEmpty ? 0 : levelToNodes.keys.reduce(math.max);
    final widestRow = levelToNodes.values.fold<int>(
      1,
      (maxWidth, row) => row.length > maxWidth ? row.length : maxWidth,
    );
    final width =
        (widestRow * nodeSize.width) +
        ((widestRow - 1) * horizontalSpacing) +
        (canvasPadding * 2);
    final height =
        ((maxLevel + 1) * nodeSize.height) +
        (maxLevel * verticalSpacing) +
        (canvasPadding * 2);

    final nodes = <TreeNodeLayout>[];
    final positionsById = <String, Rect>{};

    for (int level = 0; level <= maxLevel; level++) {
      final row = levelToNodes[level] ?? const <Person>[];
      if (row.isEmpty) {
        continue;
      }

      final rowWidth =
          (row.length * nodeSize.width) +
          ((row.length - 1) * horizontalSpacing);
      final rowStartX = (width - rowWidth) / 2;
      final y = canvasPadding + (level * (nodeSize.height + verticalSpacing));

      for (int i = 0; i < row.length; i++) {
        final person = row[i];
        final x = rowStartX + (i * (nodeSize.width + horizontalSpacing));
        final rect = Rect.fromLTWH(x, y, nodeSize.width, nodeSize.height);
        positionsById[person.id] = rect;
        nodes.add(
          TreeNodeLayout(
            person: person,
            position: Offset(x, y),
            rect: rect,
            hasChildren: tree.childrenOf(person.id).isNotEmpty,
          ),
        );
      }
    }

    final connectors = <TreeConnector>[];
    for (final parent in visibleNodes) {
      if (!parent.isExpanded) {
        continue;
      }
      final parentRect = positionsById[parent.id];
      if (parentRect == null) {
        continue;
      }
      final children = tree
          .childrenOf(parent.id)
          .where((child) => tree.isVisible(child.id))
          .toList(growable: false);
      for (final child in children) {
        final childRect = positionsById[child.id];
        if (childRect == null) {
          continue;
        }
        connectors.add(
          TreeConnector(
            parentId: parent.id,
            childId: child.id,
            start: Offset(parentRect.center.dx, parentRect.bottom),
            end: Offset(childRect.center.dx, childRect.top),
          ),
        );
      }
    }

    return TreeLayout(
      nodes: nodes,
      connectors: connectors,
      canvasSize: Size(width, height),
      version: version,
    );
  }
}
