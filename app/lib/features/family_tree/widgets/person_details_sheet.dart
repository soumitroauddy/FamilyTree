import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';

import '../../../../core/theme/app_theme.dart';
import '../../../../core/widgets/glass_container.dart';
import '../models/person.dart';
import 'person_avatar.dart';

class PersonDetailsSheet extends StatelessWidget {
  const PersonDetailsSheet({
    super.key,
    required this.person,
  });

  final Person person;

  @override
  Widget build(BuildContext context) {
    final years = person.lifeSpanLabel;
    final location = person.location?.trim();
    final screenHeight = MediaQuery.of(context).size.height;

    return GlassContainer(
      borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
      blur: 32,
      backgroundOpacity: 0.08,
      borderOpacity: 0.15,
      child: ConstrainedBox(
        constraints: BoxConstraints(maxHeight: screenHeight * 0.75),
        child: SingleChildScrollView(
          physics: const BouncingScrollPhysics(),
          child: Padding(
            padding: EdgeInsets.fromLTRB(
              24,
              20,
              24,
              24 + MediaQuery.of(context).viewInsets.bottom,
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _DragHandle(),
                const SizedBox(height: 20),
                _HeaderRow(person: person, years: years, location: location)
                    .animate()
                    .fadeIn(duration: 350.ms, delay: 80.ms)
                    .slideY(
                      begin: 0.1,
                      end: 0,
                      duration: 400.ms,
                      delay: 80.ms,
                      curve: Curves.easeOut,
                    ),
                const SizedBox(height: 24),
                _Divider(),
                const SizedBox(height: 20),
                _BiographySection(person: person)
                    .animate()
                    .fadeIn(duration: 350.ms, delay: 160.ms)
                    .slideY(
                      begin: 0.1,
                      end: 0,
                      duration: 400.ms,
                      delay: 160.ms,
                      curve: Curves.easeOut,
                    ),
                const SizedBox(height: 28),
                _CloseButton()
                    .animate()
                    .fadeIn(duration: 350.ms, delay: 240.ms),
              ],
            ),
          ),
        ),
      ),
    )
        .animate()
        .slideY(begin: 0.3, end: 0, duration: 450.ms, curve: Curves.easeOut)
        .fadeIn(duration: 300.ms);
  }
}

class _DragHandle extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        width: 36,
        height: 4,
        decoration: BoxDecoration(
          color: Colors.white.withValues(alpha: 0.2),
          borderRadius: BorderRadius.circular(2),
        ),
      ),
    );
  }
}

class _HeaderRow extends StatelessWidget {
  const _HeaderRow({
    required this.person,
    required this.years,
    required this.location,
  });

  final Person person;
  final String years;
  final String? location;

  @override
  Widget build(BuildContext context) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        PersonAvatar(
          photoUrl: person.photoUrl,
          initials: person.initials,
          radius: 34,
        ),
        const SizedBox(width: 16),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                person.fullName,
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      color: AppColors.onSurface,
                      fontWeight: FontWeight.w700,
                    ),
              ),
              const SizedBox(height: 4),
              if (years.isNotEmpty)
                Row(
                  children: [
                    Icon(
                      Icons.calendar_today_rounded,
                      size: 12,
                      color: AppColors.primary.withValues(alpha: 0.8),
                    ),
                    const SizedBox(width: 6),
                    Text(
                      years,
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                            color: AppColors.primary.withValues(alpha: 0.8),
                          ),
                    ),
                  ],
                ),
              if (location != null && location!.isNotEmpty) ...[
                const SizedBox(height: 2),
                Row(
                  children: [
                    Icon(
                      Icons.location_on_rounded,
                      size: 12,
                      color: AppColors.onSurfaceSecondary,
                    ),
                    const SizedBox(width: 6),
                    Text(
                      location!,
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                  ],
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }
}

class _Divider extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(
      height: 1,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Colors.transparent,
            Colors.white.withValues(alpha: 0.12),
            Colors.transparent,
          ],
        ),
      ),
    );
  }
}

class _BiographySection extends StatelessWidget {
  const _BiographySection({required this.person});

  final Person person;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Container(
              width: 3,
              height: 16,
              decoration: BoxDecoration(
                color: AppColors.primary,
                borderRadius: BorderRadius.circular(2),
                boxShadow: [
                  BoxShadow(
                    color: AppColors.primary.withValues(alpha: 0.5),
                    blurRadius: 6,
                  ),
                ],
              ),
            ),
            const SizedBox(width: 10),
            Text(
              'About',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: AppColors.onSurface,
                    letterSpacing: 0.5,
                  ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Text(
          person.bio ??
              'No biography available yet. Connect to the backend to load full profile details.',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: AppColors.onSurfaceSecondary,
                height: 1.6,
              ),
        ),
      ],
    );
  }
}

class _CloseButton extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      child: OutlinedButton(
        onPressed: () => Navigator.of(context).pop(),
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary,
          side: BorderSide(color: AppColors.primary.withValues(alpha: 0.4), width: 1),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
          ),
          padding: const EdgeInsets.symmetric(vertical: 14),
          backgroundColor: AppColors.primary.withValues(alpha: 0.06),
        ),
        child: const Text(
          'Close',
          style: TextStyle(
            fontWeight: FontWeight.w600,
            letterSpacing: 0.5,
          ),
        ),
      ),
    );
  }
}
