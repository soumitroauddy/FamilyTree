import 'package:flutter/material.dart';

import '../models/tree_layout.dart';

class FamilyTreePainter extends CustomPainter {
  const FamilyTreePainter({
    required this.connectors,
  });

  final List<TreeConnector> connectors;

  @override
  void paint(Canvas canvas, Size size) {
    if (connectors.isEmpty) {
      return;
    }

    final paint = Paint()
      ..color = const Color(0xFF7D8A9B)
      ..strokeWidth = 2
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    for (final connector in connectors) {
      final midY = connector.start.dy + ((connector.end.dy - connector.start.dy) / 2);
      final path = Path()
        ..moveTo(connector.start.dx, connector.start.dy)
        ..lineTo(connector.start.dx, midY)
        ..lineTo(connector.end.dx, midY)
        ..lineTo(connector.end.dx, connector.end.dy);
      canvas.drawPath(path, paint);
    }
  }

  @override
  bool shouldRepaint(covariant FamilyTreePainter oldDelegate) {
    return oldDelegate.connectors != connectors;
  }
}
