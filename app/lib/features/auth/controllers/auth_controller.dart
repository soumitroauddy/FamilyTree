import 'package:flutter/foundation.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import '../../../core/services/auth_service.dart';
import '../../../core/services/api_client.dart';

export '../../../core/services/auth_service.dart' show AuthSession;

enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthController extends ChangeNotifier {
  final AuthService _authService;

  AuthStatus _status = AuthStatus.unknown;
  AuthSession? _session;
  String? _error;
  bool _isLoading = false;

  AuthController(this._authService) {
    _checkSession();
  }

  AuthStatus get status => _status;
  AuthSession? get session => _session;
  String? get error => _error;
  bool get isLoading => _isLoading;

  Future<void> _checkSession() async {
    _session = await _authService.currentSession();
    _status = _session != null ? AuthStatus.authenticated : AuthStatus.unauthenticated;
    notifyListeners();
  }

  Future<bool> register({
    required String email,
    required String password,
    required String displayName,
  }) async {
    _setLoading(true);
    try {
      _session = await _authService.register(
        email: email,
        password: password,
        displayName: displayName,
      );
      _status = AuthStatus.authenticated;
      _error = null;
      return true;
    } on AuthException catch (e) {
      _error = e.message;
      return false;
    } on ApiException catch (e) {
      _error = e.message;
      return false;
    } catch (e) {
      _error = 'Registration failed. Please try again.';
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<bool> login({
    required String email,
    required String password,
  }) async {
    _setLoading(true);
    try {
      _session = await _authService.login(email: email, password: password);
      _status = AuthStatus.authenticated;
      _error = null;
      return true;
    } on AuthException catch (e) {
      _error = e.message;
      return false;
    } on ApiException catch (e) {
      _error = e.message;
      return false;
    } catch (e) {
      _error = 'Login failed. Please try again.';
      return false;
    } finally {
      _setLoading(false);
    }
  }

  Future<void> logout() async {
    await _authService.logout();
    _session = null;
    _status = AuthStatus.unauthenticated;
    _error = null;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }
}
