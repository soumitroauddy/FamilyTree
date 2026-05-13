import 'dart:ui';

import 'package:flutter/material.dart';

import '../theme/app_theme.dart';

class GlassContainer extends StatelessWidget {
  const GlassContainer({
    super.key,
    required this.child,
    this.borderRadius = const BorderRadius.all(Radius.circular(20)),
    this.padding = EdgeInsets.zero,
    this.blur = 24.0,
    this.backgroundOpacity = 0.06,
    this.borderOpacity = 0.10,
    this.glowColor,
    this.glowBlurRadius = 20.0,
    this.glowSpreadRadius = 0.0,
    this.width,
    this.height,
  });

  final Widget child;
  final BorderRadius borderRadius;
  final EdgeInsetsGeometry padding;
  final double blur;
  final double backgroundOpacity;
  final double borderOpacity;
  final Color? glowColor;
  final double glowBlurRadius;
  final double glowSpreadRadius;
  final double? width;
  final double? height;

  @override
  Widget build(BuildContext context) {
    final shadows = glowColor != null
        ? [
            BoxShadow(
              color: glowColor!.withValues(alpha: 0.25),
              blurRadius: glowBlurRadius,
              spreadRadius: glowSpreadRadius,
            ),
          ]
        : const <BoxShadow>[];

    return Container(
      width: width,
      height: height,
      decoration: BoxDecoration(
        borderRadius: borderRadius,
        boxShadow: shadows,
      ),
      child: ClipRRect(
        borderRadius: borderRadius,
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: blur, sigmaY: blur),
          child: Container(
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: backgroundOpacity),
              borderRadius: borderRadius,
              border: Border.all(
                color: Colors.white.withValues(alpha: borderOpacity),
                width: 1.0,
              ),
            ),
            padding: padding,
            child: child,
          ),
        ),
      ),
    );
  }
}

/// A variant with a stronger inner glow on one edge, giving a "light source"
/// effect typical of premium glassmorphic UI.
class GlassCard extends StatelessWidget {
  const GlassCard({
    super.key,
    required this.child,
    this.borderRadius = const BorderRadius.all(Radius.circular(20)),
    this.padding = const EdgeInsets.all(16),
    this.glowColor,
    this.width,
    this.height,
    this.isPressed = false,
  });

  final Widget child;
  final BorderRadius borderRadius;
  final EdgeInsetsGeometry padding;
  final Color? glowColor;
  final double? width;
  final double? height;
  final bool isPressed;

  @override
  Widget build(BuildContext context) {
    final effectiveGlow = glowColor ?? AppColors.primary;
    final backgroundOpacity = isPressed ? 0.10 : 0.06;
    final borderOpacity = isPressed ? 0.20 : 0.10;
    final glowOpacity = isPressed ? 0.35 : 0.15;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 150),
      width: width,
      height: height,
      decoration: BoxDecoration(
        borderRadius: borderRadius,
        boxShadow: [
          BoxShadow(
            color: effectiveGlow.withValues(alpha: glowOpacity),
            blurRadius: 20,
            spreadRadius: 0,
          ),
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.4),
            blurRadius: 12,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: borderRadius,
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 20, sigmaY: 20),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  Colors.white.withValues(alpha: backgroundOpacity + 0.04),
                  Colors.white.withValues(alpha: backgroundOpacity),
                ],
              ),
              borderRadius: borderRadius,
              border: Border.all(
                color: Colors.white.withValues(alpha: borderOpacity),
                width: 1.0,
              ),
            ),
            padding: padding,
            child: child,
          ),
        ),
      ),
    );
  }
}
