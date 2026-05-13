import 'package:flutter/physics.dart';
import 'package:flutter/widgets.dart';

/// A [Curve] backed by a [SpringSimulation] for organic, physics-based motion.
///
/// Use [SpringCurve.bouncy] for touch-release spring-backs on interactive cards,
/// or [SpringCurve.snappy] for fast, crisp transitions without overshoot.
class SpringCurve extends Curve {
  SpringCurve({
    double mass = 1.0,
    double stiffness = 500.0,
    double damping = 28.0,
    double velocity = 0.0,
  }) : _sim = SpringSimulation(
          SpringDescription(
            mass: mass,
            stiffness: stiffness,
            damping: damping,
          ),
          0.0,
          1.0,
          velocity,
        );

  /// A natural spring with slight overshoot -- great for tap-release on cards.
  static SpringCurve bouncy = SpringCurve(
    mass: 1.0,
    stiffness: 400.0,
    damping: 22.0,
  );

  /// A crisp spring with no overshoot -- ideal for quick state transitions.
  static SpringCurve snappy = SpringCurve(
    mass: 1.0,
    stiffness: 600.0,
    damping: 35.0,
  );

  /// A slow, weighty spring for dramatic entrance animations.
  static SpringCurve cinematic = SpringCurve(
    mass: 1.2,
    stiffness: 180.0,
    damping: 18.0,
  );

  final SpringSimulation _sim;

  @override
  double transformInternal(double t) {
    return _sim.x(t).clamp(0.0, 1.0);
  }
}
