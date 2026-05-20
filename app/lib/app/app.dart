import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:provider/provider.dart';

import '../core/services/api_client.dart';
import '../core/services/auth_service.dart';
import '../core/theme/app_theme.dart';
import '../features/auth/controllers/auth_controller.dart';
import '../features/auth/screens/login_screen.dart';
import '../features/family_tree/screens/family_tree_screen.dart';

class FamilyTreeApp extends StatelessWidget {
  const FamilyTreeApp({super.key});

  @override
  Widget build(BuildContext context) {
    Animate.restartOnHotReload = true;
    final apiClient = ApiClient();
    final authService = AuthService(apiClient);

    return ChangeNotifierProvider(
      create: (_) => AuthController(authService),
      child: MaterialApp(
        title: 'Family Tree',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.theme(),
        home: const _AppRouter(),
      ),
    );
  }
}

class _AppRouter extends StatelessWidget {
  const _AppRouter();

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthController>();

    switch (auth.status) {
      case AuthStatus.unknown:
        // Show splash / loading while checking persisted session
        return const Scaffold(
          backgroundColor: AppColors.scaffold,
          body: Center(child: CircularProgressIndicator()),
        );
      case AuthStatus.unauthenticated:
        return const LoginScreen();
      case AuthStatus.authenticated:
        return FamilyTreeScreen(familyId: auth.session?.familyId);
    }
  }
}
