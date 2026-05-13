import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';

import '../../../core/theme/app_theme.dart';
import '../../../core/widgets/glass_container.dart';
import '../../../core/widgets/particle_background.dart';
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

class _FamilyTreeScreenState extends State<FamilyTreeScreen>
    with TickerProviderStateMixin {
  late final FamilyTreeController _controller;
  final TransformationController _transformationController =
      TransformationController();
  late final AnimationController _pulseController;

  @override
  void initState() {
    super.initState();
    _controller = FamilyTreeController(repository: const FamilyRepository());
    _controller.addListener(_handleUpdate);

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 4),
    )..repeat();
  }

  @override
  void dispose() {
    _controller.removeListener(_handleUpdate);
    _controller.dispose();
    _transformationController.dispose();
    _pulseController.dispose();
    super.dispose();
  }

  void _handleUpdate() {
    if (mounted) setState(() {});
  }

  void _showPersonDetails(Person person) {
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      barrierColor: Colors.black.withValues(alpha: 0.65),
      isScrollControlled: true,
      builder: (context) => PersonDetailsSheet(person: person),
    );
  }

  @override
  Widget build(BuildContext context) {
    final layout = _controller.layout;

    return Scaffold(
      backgroundColor: AppColors.scaffold,
      extendBodyBehindAppBar: true,
      appBar: _GlassAppBar(
        onResetView: () {
          _transformationController.value = Matrix4.identity();
        },
        onResetTree: _controller.reload,
      ),
      body: Stack(
        children: [
          // Layer 1: Animated particle background
          const Positioned.fill(child: ParticleBackground()),

          // Layer 2: Tree canvas with InteractiveViewer
          Column(
            children: [
              Expanded(
                child: InteractiveViewer(
                  transformationController: _transformationController,
                  constrained: false,
                  boundaryMargin: const EdgeInsets.all(400),
                  minScale: 0.3,
                  maxScale: 3.0,
                  child: AnimatedBuilder(
                    animation: _pulseController,
                    builder: (context, _) {
                      return _TreeCanvas(
                        layout: layout,
                        onNodeTap: _showPersonDetails,
                        onToggleExpansion: _controller.toggleExpanded,
                        pulseProgress: _pulseController.value,
                      );
                    },
                  ),
                ),
              ),
              // Bottom safe area placeholder so the floating hint bar doesn't
              // overlap system navigation
              SizedBox(
                height: MediaQuery.of(context).padding.bottom + 80,
              ),
            ],
          ),

          // Layer 3: Floating glass hint bar at the bottom
          Positioned(
            left: 16,
            right: 16,
            bottom: MediaQuery.of(context).padding.bottom + 12,
            child: _FloatingHintBar(
              expandedCount: _controller.expandedCount,
              totalCount: _controller.familyTree.peopleById.length,
            ),
          ),
        ],
      ),
    );
  }
}

class _GlassAppBar extends StatelessWidget implements PreferredSizeWidget {
  const _GlassAppBar({
    required this.onResetView,
    required this.onResetTree,
  });

  final VoidCallback onResetView;
  final VoidCallback onResetTree;

  @override
  Size get preferredSize => const Size.fromHeight(kToolbarHeight);

  @override
  Widget build(BuildContext context) {
    final topPadding = MediaQuery.of(context).padding.top;

    return ClipRect(
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 20, sigmaY: 20),
        child: Container(
          height: kToolbarHeight + topPadding,
          padding: EdgeInsets.only(top: topPadding, left: 8, right: 8),
          decoration: BoxDecoration(
            color: AppColors.scaffold.withValues(alpha: 0.65),
            border: Border(
              bottom: BorderSide(
                color: Colors.white.withValues(alpha: 0.08),
                width: 1,
              ),
            ),
          ),
          child: Row(
            children: [
              const SizedBox(width: 8),
              Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Family Tree',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          color: AppColors.onSurface,
                          letterSpacing: 0.5,
                        ),
                  ),
                  Text(
                    'Stone Family',
                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                          color: AppColors.primary.withValues(alpha: 0.8),
                          letterSpacing: 1.5,
                        ),
                  ),
                ],
              ),
              const Spacer(),
              _AppBarAction(
                icon: Icons.center_focus_strong_rounded,
                tooltip: 'Reset view',
                onTap: onResetView,
              ),
              const SizedBox(width: 4),
              _AppBarAction(
                icon: Icons.restart_alt_rounded,
                tooltip: 'Reset tree',
                onTap: onResetTree,
              ),
              const SizedBox(width: 4),
            ],
          ),
        ),
      ),
    );
  }
}

class _AppBarAction extends StatefulWidget {
  const _AppBarAction({
    required this.icon,
    required this.tooltip,
    required this.onTap,
  });

  final IconData icon;
  final String tooltip;
  final VoidCallback onTap;

  @override
  State<_AppBarAction> createState() => _AppBarActionState();
}

class _AppBarActionState extends State<_AppBarAction> {
  bool _pressed = false;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: widget.onTap,
      onTapDown: (_) => setState(() => _pressed = true),
      onTapUp: (_) => setState(() => _pressed = false),
      onTapCancel: () => setState(() => _pressed = false),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 120),
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: _pressed
              ? AppColors.primary.withValues(alpha: 0.2)
              : Colors.white.withValues(alpha: 0.06),
          border: Border.all(
            color: Colors.white.withValues(alpha: _pressed ? 0.2 : 0.08),
            width: 1,
          ),
        ),
        child: Icon(
          widget.icon,
          size: 20,
          color: _pressed ? AppColors.primary : AppColors.onSurfaceSecondary,
        ),
      ),
    );
  }
}

class _TreeCanvas extends StatelessWidget {
  const _TreeCanvas({
    required this.layout,
    required this.onNodeTap,
    required this.onToggleExpansion,
    required this.pulseProgress,
  });

  final TreeLayout layout;
  final ValueChanged<Person> onNodeTap;
  final ValueChanged<Person> onToggleExpansion;
  final double pulseProgress;

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
                version: layout.version,
                pulseProgress: pulseProgress,
              ),
            ),
          ),
          for (int i = 0; i < layout.nodes.length; i++)
            AnimatedPositioned(
              key: ValueKey(layout.nodes[i].person.id),
              duration: const Duration(milliseconds: 350),
              curve: Curves.easeInOut,
              left: layout.nodes[i].position.dx,
              top: layout.nodes[i].position.dy,
              child: FamilyTreeNodeCard(
                person: layout.nodes[i].person,
                node: layout.nodes[i],
                onTap: () => onNodeTap(layout.nodes[i].person),
                onToggleExpanded: layout.nodes[i].hasChildren
                    ? () => onToggleExpansion(layout.nodes[i].person)
                    : null,
                animationIndex: i,
              ),
            ),
        ],
      ),
    );
  }
}

class _FloatingHintBar extends StatelessWidget {
  const _FloatingHintBar({
    required this.expandedCount,
    required this.totalCount,
  });

  final int expandedCount;
  final int totalCount;

  @override
  Widget build(BuildContext context) {
    return GlassContainer(
      borderRadius: const BorderRadius.all(Radius.circular(16)),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      blur: 20,
      backgroundOpacity: 0.08,
      borderOpacity: 0.12,
      glowColor: AppColors.primary,
      glowBlurRadius: 12,
      child: Row(
        children: [
          Icon(
            Icons.touch_app_rounded,
            size: 14,
            color: AppColors.primary.withValues(alpha: 0.7),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              'Tap for details · Use chevron to expand',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppColors.onSurfaceSecondary,
                  ),
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 3),
            decoration: BoxDecoration(
              color: AppColors.primary.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(
                color: AppColors.primary.withValues(alpha: 0.25),
                width: 1,
              ),
            ),
            child: Text(
              '$expandedCount / $totalCount',
              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                    color: AppColors.primary,
                    fontWeight: FontWeight.w600,
                  ),
            ),
          ),
        ],
      ),
    )
        .animate()
        .fadeIn(duration: 600.ms, delay: 400.ms)
        .slideY(begin: 0.3, end: 0, duration: 500.ms, delay: 400.ms);
  }
}
