import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';

import '../../../../core/animations/spring_curve.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../core/widgets/glass_container.dart';
import '../models/person.dart';
import '../models/tree_layout.dart';
import 'person_avatar.dart';

class FamilyTreeNodeCard extends StatefulWidget {
  const FamilyTreeNodeCard({
    super.key,
    required this.person,
    required this.node,
    required this.onTap,
    required this.onToggleExpanded,
    this.animationIndex = 0,
  });

  final Person person;
  final TreeNodeLayout node;
  final VoidCallback onTap;
  final VoidCallback? onToggleExpanded;
  final int animationIndex;

  @override
  State<FamilyTreeNodeCard> createState() => _FamilyTreeNodeCardState();
}

class _FamilyTreeNodeCardState extends State<FamilyTreeNodeCard>
    with SingleTickerProviderStateMixin {
  late final AnimationController _pressController;
  late final Animation<double> _scaleAnim;

  bool _isPressed = false;
  Offset _tiltOffset = Offset.zero;

  @override
  void initState() {
    super.initState();
    _pressController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 120),
      reverseDuration: const Duration(milliseconds: 400),
    );
    _scaleAnim = Tween<double>(begin: 1.0, end: 0.95).animate(
      CurvedAnimation(parent: _pressController, curve: Curves.easeOut),
    );
  }

  @override
  void dispose() {
    _pressController.dispose();
    super.dispose();
  }

  void _onPointerDown(PointerDownEvent event) {
    setState(() => _isPressed = true);
    _pressController.forward();
    _updateTilt(event.localPosition);
  }

  void _onPointerUp(PointerUpEvent event) {
    setState(() {
      _isPressed = false;
      _tiltOffset = Offset.zero;
    });
    _pressController.reverse();
  }

  void _onPointerCancel(PointerCancelEvent event) {
    setState(() {
      _isPressed = false;
      _tiltOffset = Offset.zero;
    });
    _pressController.reverse();
  }

  void _onPointerMove(PointerMoveEvent event) {
    _updateTilt(event.localPosition);
  }

  void _updateTilt(Offset localPos) {
    final w = widget.node.rect.width;
    final h = widget.node.rect.height;
    final dx = (localPos.dx / w - 0.5) * 2;
    final dy = (localPos.dy / h - 0.5) * 2;
    setState(() {
      _tiltOffset = Offset(
        dx.clamp(-1.0, 1.0),
        dy.clamp(-1.0, 1.0),
      );
    });
  }

  Matrix4 _buildTiltMatrix() {
    const maxAngle = 0.04; // ~2.3 degrees
    final rx = -_tiltOffset.dy * maxAngle;
    final ry = _tiltOffset.dx * maxAngle;

    return Matrix4.identity()
      ..setEntry(3, 2, 0.001) // perspective
      ..rotateX(rx)
      ..rotateY(ry);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;
    final delay = Duration(milliseconds: 60 * widget.animationIndex);

    return AnimatedBuilder(
      animation: _scaleAnim,
      builder: (context, child) {
        return Transform(
          alignment: Alignment.center,
          transform: _buildTiltMatrix(),
          child: Transform.scale(
            scale: _scaleAnim.value,
            child: child,
          ),
        );
      },
      child: Listener(
        onPointerDown: _onPointerDown,
        onPointerUp: _onPointerUp,
        onPointerCancel: _onPointerCancel,
        onPointerMove: _onPointerMove,
        child: GestureDetector(
          onTap: widget.onTap,
          child: GlassCard(
            width: widget.node.rect.width,
            height: widget.node.rect.height,
            padding: const EdgeInsets.all(10),
            borderRadius: const BorderRadius.all(Radius.circular(18)),
            glowColor: AppColors.primary,
            isPressed: _isPressed,
            child: Row(
              children: [
                PersonAvatar(
                  photoUrl: widget.person.photoUrl,
                  initials: widget.person.initials,
                  radius: 26,
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        widget.person.fullName,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.w700,
                          color: AppColors.onSurface,
                        ),
                      ),
                      const SizedBox(height: 3),
                      Text(
                        widget.person.lifeSpanLabel,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: textTheme.bodySmall?.copyWith(
                          color: AppColors.primary.withValues(alpha: 0.8),
                        ),
                      ),
                    ],
                  ),
                ),
                if (widget.person.childrenIds.isNotEmpty)
                  _ExpandButton(
                    isExpanded: widget.person.isExpanded,
                    onTap: widget.onToggleExpanded,
                  ),
              ],
            ),
          ),
        ),
      ),
    )
        .animate(delay: delay)
        .fadeIn(duration: 400.ms, curve: Curves.easeOut)
        .slideY(
          begin: 0.15,
          end: 0,
          duration: 500.ms,
          curve: SpringCurve.snappy,
        )
        .blurXY(begin: 4, end: 0, duration: 400.ms);
  }
}

class _ExpandButton extends StatelessWidget {
  const _ExpandButton({
    required this.isExpanded,
    required this.onTap,
  });

  final bool isExpanded;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 30,
        height: 30,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: AppColors.primary.withValues(alpha: 0.12),
          border: Border.all(
            color: AppColors.primary.withValues(alpha: 0.3),
            width: 1,
          ),
        ),
        child: Center(
          child: AnimatedRotation(
            turns: isExpanded ? 0.0 : -0.25,
            duration: const Duration(milliseconds: 250),
            curve: SpringCurve.snappy,
            child: Icon(
              Icons.expand_more_rounded,
              size: 18,
              color: AppColors.primary,
            ),
          ),
        ),
      ),
    );
  }
}

// Re-export for use in painter for glow math
extension RadiansHelper on double {
  double get radians => this * math.pi / 180;
}
