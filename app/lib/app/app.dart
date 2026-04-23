import 'package:flutter/material.dart';

import '../core/theme/app_theme.dart';
import '../features/family_tree/screens/family_tree_screen.dart';

class FamilyTreeApp extends StatelessWidget {
  const FamilyTreeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Family Tree',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme(),
      home: const FamilyTreeScreen(),
    );
  }
}
