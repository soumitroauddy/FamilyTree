import 'package:supabase_flutter/supabase_flutter.dart';
import 'api_client.dart';

class AuthSession {
  final String userId;
  final String accessToken;
  final String displayName;
  final String email;
  final String provider;
  final String? familyId;
  final String? familyName;

  const AuthSession({
    required this.userId,
    required this.accessToken,
    required this.displayName,
    required this.email,
    required this.provider,
    this.familyId,
    this.familyName,
  });
}

class AuthService {
  final ApiClient _client;

  AuthService(this._client);

  SupabaseClient get _supabase => Supabase.instance.client;

  /// Signs up a new user with Supabase Auth, then syncs to the backend.
  Future<AuthSession> register({
    required String email,
    required String password,
    required String displayName,
  }) async {
    final response = await _supabase.auth.signUp(
      email: email,
      password: password,
      data: {'display_name': displayName},
    );

    final session = response.session;
    if (session == null) {
      throw const ApiException(400, 'Registration failed. Check your email for a confirmation link.');
    }

    return _syncAndBuildSession(session);
  }

  /// Signs in with email and password via Supabase Auth, then syncs to backend.
  Future<AuthSession> login({
    required String email,
    required String password,
  }) async {
    final response = await _supabase.auth.signInWithPassword(
      email: email,
      password: password,
    );

    final session = response.session;
    if (session == null) {
      throw const ApiException(401, 'Invalid email or password.');
    }

    return _syncAndBuildSession(session);
  }

  /// Returns the current session if the user is still signed in, or null.
  Future<AuthSession?> currentSession() async {
    final session = _supabase.auth.currentSession;
    if (session == null) return null;
    try {
      return _syncAndBuildSession(session);
    } catch (_) {
      return null;
    }
  }

  Future<void> logout() async {
    await _supabase.auth.signOut();
  }

  /// Calls the backend /auth/sync endpoint to ensure a local user row exists
  /// and to retrieve current family state. Returns an AuthSession built from
  /// the Supabase session + backend bootstrap data.
  Future<AuthSession> _syncAndBuildSession(Session session) async {
    final user = session.user;
    final json = await _client.post('/api/control-plane/v1/auth/sync')
        as Map<String, dynamic>;

    final displayName = json['displayName'] as String? ??
        (user.userMetadata?['display_name'] as String?) ??
        user.email?.split('@').first ??
        'User';

    return AuthSession(
      userId: user.id,
      accessToken: session.accessToken,
      displayName: displayName,
      email: user.email ?? '',
      provider: 'SUPABASE',
      familyId: json['familyId'] as String?,
      familyName: json['familyName'] as String?,
    );
  }
}
