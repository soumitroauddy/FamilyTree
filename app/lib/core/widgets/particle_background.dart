import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

class ParticleBackground extends StatefulWidget {
  const ParticleBackground({super.key});

  @override
  State<ParticleBackground> createState() => _ParticleBackgroundState();
}

class _ParticleBackgroundState extends State<ParticleBackground>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller;
  late final List<_Particle> _particles;
  late final List<_LightOrb> _orbs;

  static const _particleCount = 50;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 20),
    )..repeat();

    final rng = math.Random(42);
    _particles = List.generate(
      _particleCount,
      (_) => _Particle.random(rng),
    );
    _orbs = [
      _LightOrb(
        color: AppColors.primary.withValues(alpha: 0.08),
        x: 0.2,
        y: 0.3,
        radius: 0.35,
        driftX: 0.06,
        driftY: 0.04,
        speed: 0.3,
      ),
      _LightOrb(
        color: AppColors.secondary.withValues(alpha: 0.06),
        x: 0.8,
        y: 0.7,
        radius: 0.30,
        driftX: -0.05,
        driftY: -0.06,
        speed: 0.2,
      ),
    ];
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, _) {
        return CustomPaint(
          painter: _ParticlePainter(
            particles: _particles,
            orbs: _orbs,
            progress: _controller.value,
          ),
          size: Size.infinite,
        );
      },
    );
  }
}

class _Particle {
  _Particle({
    required this.x,
    required this.y,
    required this.radius,
    required this.opacity,
    required this.speed,
    required this.angle,
    required this.depth,
  });

  factory _Particle.random(math.Random rng) {
    return _Particle(
      x: rng.nextDouble(),
      y: rng.nextDouble(),
      radius: 0.5 + rng.nextDouble() * 2.0,
      opacity: 0.1 + rng.nextDouble() * 0.4,
      speed: 0.003 + rng.nextDouble() * 0.008,
      angle: rng.nextDouble() * 2 * math.pi,
      depth: rng.nextDouble(),
    );
  }

  final double x;
  final double y;
  final double radius;
  final double opacity;
  final double speed;
  final double angle;
  final double depth;
}

class _LightOrb {
  const _LightOrb({
    required this.color,
    required this.x,
    required this.y,
    required this.radius,
    required this.driftX,
    required this.driftY,
    required this.speed,
  });

  final Color color;
  final double x;
  final double y;
  final double radius;
  final double driftX;
  final double driftY;
  final double speed;
}

class _ParticlePainter extends CustomPainter {
  const _ParticlePainter({
    required this.particles,
    required this.orbs,
    required this.progress,
  });

  final List<_Particle> particles;
  final List<_LightOrb> orbs;
  final double progress;

  @override
  void paint(Canvas canvas, Size size) {
    _drawOrbs(canvas, size);
    _drawParticles(canvas, size);
  }

  void _drawOrbs(Canvas canvas, Size size) {
    for (final orb in orbs) {
      final t = progress * orb.speed * 2 * math.pi;
      final cx = (orb.x + math.sin(t) * orb.driftX) * size.width;
      final cy = (orb.y + math.cos(t * 0.7) * orb.driftY) * size.height;
      final r = orb.radius * size.shortestSide;

      final paint = Paint()
        ..shader = RadialGradient(
          colors: [orb.color, Colors.transparent],
        ).createShader(Rect.fromCircle(center: Offset(cx, cy), radius: r));

      canvas.drawCircle(Offset(cx, cy), r, paint);
    }
  }

  void _drawParticles(Canvas canvas, Size size) {
    for (int i = 0; i < particles.length; i++) {
      final p = particles[i];
      final t = progress + i * 0.02;
      final parallax = 1.0 - p.depth * 0.4;

      final x =
          ((p.x + math.cos(p.angle + t * p.speed * 6) * 0.05) % 1.0) *
          size.width;
      final y =
          ((p.y + math.sin(p.angle + t * p.speed * 6) * 0.05) % 1.0) *
          size.height;

      final twinkle = 0.6 + 0.4 * math.sin(t * 4 + i);
      final paint = Paint()
        ..color = AppColors.primary.withValues(alpha: p.opacity * twinkle * parallax)
        ..maskFilter = MaskFilter.blur(BlurStyle.normal, p.radius * 0.8);

      canvas.drawCircle(Offset(x, y), p.radius * parallax, paint);
    }
  }

  @override
  bool shouldRepaint(covariant _ParticlePainter oldDelegate) {
    return oldDelegate.progress != progress;
  }
}
