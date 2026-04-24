import 'package:flutter/material.dart';

import '../../../../core/theme/app_theme.dart';
import '../models/tree_layout.dart';

class FamilyTreePainter extends CustomPainter {
  const FamilyTreePainter({
    required this.connectors,
    required this.version,
    this.pulseProgress = 0.0,
  });

  final List<TreeConnector> connectors;
  final int version;
  final double pulseProgress;

  @override
  void paint(Canvas canvas, Size size) {
    if (connectors.isEmpty) return;

    for (final connector in connectors) {
      final path = _buildBezierPath(connector);
      _drawGlowLayer(canvas, path);
      _drawCoreLine(canvas, path);
      _drawPulseDot(canvas, connector, path);
    }
  }

  Path _buildBezierPath(TreeConnector connector) {
    final start = connector.start;
    final end = connector.end;
    final midY = (start.dy + end.dy) / 2;

    return Path()
      ..moveTo(start.dx, start.dy)
      ..cubicTo(
        start.dx,
        midY,
        end.dx,
        midY,
        end.dx,
        end.dy,
      );
  }

  void _drawGlowLayer(Canvas canvas, Path path) {
    final glowPaint = Paint()
      ..color = AppColors.primary.withValues(alpha: 0.18)
      ..strokeWidth = 8
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 6);

    canvas.drawPath(path, glowPaint);
  }

  void _drawCoreLine(Canvas canvas, Path path) {
    final linePaint = Paint()
      ..color = AppColors.primary.withValues(alpha: 0.55)
      ..strokeWidth = 1.5
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    canvas.drawPath(path, linePaint);
  }

  void _drawPulseDot(Canvas canvas, TreeConnector connector, Path path) {
    // Animate a glowing dot traveling along the bezier path
    final metrics = path.computeMetrics().toList();
    if (metrics.isEmpty) return;

    final metric = metrics.first;
    final length = metric.length;
    if (length == 0) return;

    // Stagger per connector using a hash of the IDs
    final offset = (connector.parentId.hashCode ^ connector.childId.hashCode)
            .abs() %
        100 /
        100.0;
    final t = ((pulseProgress + offset) % 1.0) * length;

    final tangent = metric.getTangentForOffset(t);
    if (tangent == null) return;

    final dotPos = tangent.position;

    // Outer glow
    final glowPaint = Paint()
      ..color = AppColors.primary.withValues(alpha: 0.30)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 4);
    canvas.drawCircle(dotPos, 5, glowPaint);

    // Bright core dot
    final dotPaint = Paint()..color = AppColors.primary.withValues(alpha: 0.9);
    canvas.drawCircle(dotPos, 2, dotPaint);
  }

  @override
  bool shouldRepaint(covariant FamilyTreePainter oldDelegate) {
    return oldDelegate.version != version ||
        oldDelegate.pulseProgress != pulseProgress;
  }
}
