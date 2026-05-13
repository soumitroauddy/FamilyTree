import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';

import '../core/theme/app_theme.dart';
import '../features/family_tree/screens/family_tree_screen.dart';

class FamilyTreeApp extends StatelessWidget {
  const FamilyTreeApp({super.key});

  @override
  Widget build(BuildContext context) {
    Animate.restartOnHotReload = true;
    return MaterialApp(
      title: 'Family Tree',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.theme(),
      home: const FamilyTreeScreen(),
    );
  }
}
