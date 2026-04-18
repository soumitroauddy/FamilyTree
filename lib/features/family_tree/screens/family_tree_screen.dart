import 'package:flutter/material.dart';

import '../../../core/constants/app_constants.dart';
import '../controllers/family_tree_controller.dart';
import '../data/family_repository.dart';
import '../models/person.dart';
import '../models/tree_layout.dart';
import '../widgets/family_tree_node_card.dart';
import '../widgets/family_tree_painter.dart';
import '../widgets/person_details_sheet.dart';

class FamilyTreeScreen extends StatefulWidget {
  const FamilyTreeScreen({super.key});

  @override
  State<FamilyTreeScreen> createState() => _FamilyTreeScreenState();
}

class _FamilyTreeScreenState extends State<FamilyTreeScreen> {
  late final FamilyTreeController _controller;
  final TransformationController _transformationController =
      TransformationController();

  @override
  void initState() {
    super.initState();
    _controller = FamilyTreeController(repository: const FamilyRepository());
    _controller.addListener(_handleUpdate);
  }

  @override
  void dispose() {
    _controller.removeListener(_handleUpdate);
    _controller.dispose();
    _transformationController.dispose();
    super.dispose();
  }

  void _handleUpdate() {
    if (mounted) {
      setState(() {});
    }
  }

  void _showPersonDetails(Person person) {
    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (context) => PersonDetailsSheet(person: person),
    );
  }

  @override
  Widget build(BuildContext context) {
    final layout = _controller.layout;

    return Scaffold(
      appBar: AppBar(
        title: const Text(AppConstants.appTitle),
        actions: [
          IconButton(
            tooltip: 'Reset view',
            onPressed: () {
              _transformationController.value = Matrix4.identity();
            },
            icon: const Icon(Icons.center_focus_strong),
          ),
          IconButton(
            tooltip: 'Reset tree',
            onPressed: _controller.reload,
            icon: const Icon(Icons.restart_alt),
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: InteractiveViewer(
              transformationController: _transformationController,
              constrained: false,
              boundaryMargin: const EdgeInsets.all(400),
              minScale: 0.4,
              maxScale: 2.5,
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 350),
                switchInCurve: Curves.easeOutCubic,
                switchOutCurve: Curves.easeInCubic,
                child: _TreeCanvas(
                  key: ValueKey(layout.version),
                  layout: layout,
                  onNodeTap: _showPersonDetails,
                  onToggleExpansion: _controller.toggleExpanded,
                ),
              ),
            ),
          ),
          _HintBar(
            expandedCount: _controller.expandedCount,
            totalCount: _controller.familyTree.peopleById.length,
          ),
        ],
      ),
    );
  }
}

class _TreeCanvas extends StatelessWidget {
  const _TreeCanvas({
    super.key,
    required this.layout,
    required this.onNodeTap,
    required this.onToggleExpansion,
  });

  final TreeLayout layout;
  final ValueChanged<Person> onNodeTap;
  final ValueChanged<Person> onToggleExpansion;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: layout.canvasSize.width,
      height: layout.canvasSize.height,
      child: Stack(
        children: [
          Positioned.fill(
            child: CustomPaint(
              painter: FamilyTreePainter(
                connectors: layout.connectors,
              ),
            ),
          ),
          for (final node in layout.nodes)
            AnimatedPositioned(
              key: ValueKey(node.person.id),
              duration: const Duration(milliseconds: 300),
              curve: Curves.easeInOut,
              left: node.position.dx,
              top: node.position.dy,
              child: FamilyTreeNodeCard(
                person: node.person,
                node: node,
                onTap: () => onNodeTap(node.person),
                onToggleExpanded: node.hasChildren
                    ? () => onToggleExpansion(node.person)
                    : null,
              ),
            ),
        ],
      ),
    );
  }
}

class _HintBar extends StatelessWidget {
  const _HintBar({
    required this.expandedCount,
    required this.totalCount,
  });

  final int expandedCount;
  final int totalCount;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      color: theme.colorScheme.surface,
      child: Wrap(
        spacing: 16,
        runSpacing: 8,
        crossAxisAlignment: WrapCrossAlignment.center,
        children: [
          Text(
            'Tap a card for details',
            style: theme.textTheme.bodyMedium,
          ),
          Text(
            'Use chevron buttons to expand or collapse branches',
            style: theme.textTheme.bodyMedium,
          ),
          Text(
            'Expanded: $expandedCount / $totalCount',
            style: theme.textTheme.labelLarge,
          ),
        ],
      ),
    );
  }
}
