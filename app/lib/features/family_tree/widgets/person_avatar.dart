import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../../../core/theme/app_theme.dart';

class PersonAvatar extends StatefulWidget {
  const PersonAvatar({
    super.key,
    required this.photoUrl,
    required this.initials,
    this.radius = 24,
  });

  final String photoUrl;
  final String initials;
  final double radius;

  @override
  State<PersonAvatar> createState() => _PersonAvatarState();
}

class _PersonAvatarState extends State<PersonAvatar>
    with SingleTickerProviderStateMixin {
  late final AnimationController _glowController;

  @override
  void initState() {
    super.initState();
    _glowController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2500),
    )..repeat(reverse: true);
  }

  @override
  void dispose() {
    _glowController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final diameter = widget.radius * 2;
    final hasPhoto = widget.photoUrl.trim().isNotEmpty;
    final imageChild = hasPhoto
        ? _PhotoImage(
            photoUrl: widget.photoUrl,
            diameter: diameter,
            initials: widget.initials,
          )
        : _InitialsFallback(initials: widget.initials, diameter: diameter);

    return AnimatedBuilder(
      animation: _glowController,
      builder: (context, child) {
        final glow = 0.25 + 0.35 * _glowController.value;
        final ringWidth = 1.5 + 0.5 * _glowController.value;
        return CustomPaint(
          painter: _GlowRingPainter(
            glowOpacity: glow,
            ringWidth: ringWidth,
            radius: widget.radius,
          ),
          child: child,
        );
      },
      child: Padding(
        padding: const EdgeInsets.all(3),
        child: ClipOval(child: imageChild),
      ),
    );
  }
}

class _GlowRingPainter extends CustomPainter {
  const _GlowRingPainter({
    required this.glowOpacity,
    required this.ringWidth,
    required this.radius,
  });

  final double glowOpacity;
  final double ringWidth;
  final double radius;

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final outerRadius = size.width / 2;

    // Soft outer glow
    final glowPaint = Paint()
      ..color = AppColors.primary.withValues(alpha: glowOpacity * 0.4)
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 6);
    canvas.drawCircle(center, outerRadius, glowPaint);

    // Gradient ring border
    final ringPaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = ringWidth
      ..shader = SweepGradient(
        colors: [
          AppColors.primary.withValues(alpha: glowOpacity),
          AppColors.secondary.withValues(alpha: glowOpacity * 0.6),
          AppColors.primary.withValues(alpha: glowOpacity),
        ],
        transform: GradientRotation(
          glowOpacity * math.pi,
        ),
      ).createShader(Rect.fromCircle(center: center, radius: outerRadius));

    canvas.drawCircle(center, outerRadius - ringWidth / 2, ringPaint);
  }

  @override
  bool shouldRepaint(covariant _GlowRingPainter oldDelegate) {
    return oldDelegate.glowOpacity != glowOpacity ||
        oldDelegate.ringWidth != ringWidth;
  }
}

class _PhotoImage extends StatelessWidget {
  const _PhotoImage({
    required this.photoUrl,
    required this.diameter,
    required this.initials,
  });

  final String photoUrl;
  final double diameter;
  final String initials;

  @override
  Widget build(BuildContext context) {
    final fallback = _InitialsFallback(initials: initials, diameter: diameter);
    return SizedBox.square(
      dimension: diameter,
      child: Image.network(
        photoUrl,
        fit: BoxFit.cover,
        errorBuilder: (_, __, ___) => fallback,
        loadingBuilder: (_, child, loadingProgress) {
          if (loadingProgress == null) return child;
          return fallback;
        },
      ),
    );
  }
}

class _InitialsFallback extends StatelessWidget {
  const _InitialsFallback({
    required this.initials,
    required this.diameter,
  });

  final String initials;
  final double diameter;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: diameter,
      height: diameter,
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF1A2235), Color(0xFF0D1520)],
        ),
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Text(
        initials.isEmpty ? '?' : initials,
        style: TextStyle(
          fontSize: diameter * 0.35,
          fontWeight: FontWeight.w700,
          color: AppColors.primary,
          letterSpacing: 0.5,
        ),
      ),
    );
  }
}
