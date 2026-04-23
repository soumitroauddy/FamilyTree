import 'package:flutter/material.dart';

class PersonAvatar extends StatelessWidget {
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
  Widget build(BuildContext context) {
    final hasPhoto = photoUrl.trim().isNotEmpty;
    final diameter = radius * 2;
    final fallback = _InitialsFallback(initials: initials, diameter: diameter);

    if (!hasPhoto) {
      return fallback;
    }

    return ClipOval(
      child: SizedBox.square(
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
        color: Color(0xFFE1ECFA),
        shape: BoxShape.circle,
      ),
      alignment: Alignment.center,
      child: Text(
        initials.isEmpty ? '?' : initials,
        style: Theme.of(context).textTheme.labelLarge?.copyWith(
              fontWeight: FontWeight.w700,
            ),
      ),
    );
  }
}
