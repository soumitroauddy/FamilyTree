import 'package:flutter/material.dart';

import '../models/person.dart';
import '../models/tree_layout.dart';
import 'person_avatar.dart';

class FamilyTreeNodeCard extends StatelessWidget {
  const FamilyTreeNodeCard({
    super.key,
    required this.person,
    required this.node,
    required this.onTap,
    required this.onToggleExpanded,
  });

  final Person person;
  final TreeNodeLayout node;
  final VoidCallback onTap;
  final VoidCallback? onToggleExpanded;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final textTheme = theme.textTheme;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: onTap,
        child: Container(
          width: node.rect.width,
          height: node.rect.height,
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: theme.colorScheme.outlineVariant),
            boxShadow: const [
              BoxShadow(
                color: Color(0x1A000000),
                blurRadius: 10,
                offset: Offset(0, 4),
              ),
            ],
          ),
          child: Row(
            children: [
              PersonAvatar(
                photoUrl: person.photoUrl,
                initials: person.initials,
                radius: 26,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      person.fullName,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      person.lifeSpanLabel,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              if (person.childrenIds.isNotEmpty)
                IconButton(
                  tooltip: person.isExpanded
                      ? 'Collapse descendants'
                      : 'Expand descendants',
                  onPressed: onToggleExpanded,
                  icon: AnimatedRotation(
                    turns: person.isExpanded ? 0.0 : -0.25,
                    duration: const Duration(milliseconds: 220),
                    child: const Icon(Icons.expand_more_rounded),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
