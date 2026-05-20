import 'dart:convert';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import 'api_client.dart';

class FamilyMember {
  final String id;
  final String familyId;
  final String fullName;
  final String? photoUrl;
  final int? birthYear;
  final int? deathYear;
  final String? bio;
  final String? location;
  final String? parentMemberId;

  const FamilyMember({
    required this.id,
    required this.familyId,
    required this.fullName,
    this.photoUrl,
    this.birthYear,
    this.deathYear,
    this.bio,
    this.location,
    this.parentMemberId,
  });

  factory FamilyMember.fromJson(Map<String, dynamic> json) => FamilyMember(
        id: json['id'] as String,
        familyId: json['familyId'] as String,
        fullName: json['fullName'] as String,
        photoUrl: json['photoUrl'] as String?,
        birthYear: json['birthYear'] as int?,
        deathYear: json['deathYear'] as int?,
        bio: json['bio'] as String?,
        location: json['location'] as String?,
        parentMemberId: json['parentMemberId'] as String?,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'familyId': familyId,
        'fullName': fullName,
        if (photoUrl != null) 'photoUrl': photoUrl,
        if (birthYear != null) 'birthYear': birthYear,
        if (deathYear != null) 'deathYear': deathYear,
        if (bio != null) 'bio': bio,
        if (location != null) 'location': location,
        if (parentMemberId != null) 'parentMemberId': parentMemberId,
      };
}

class FamilyInfo {
  final String id;
  final String name;
  final String joinCode;

  const FamilyInfo({required this.id, required this.name, required this.joinCode});

  factory FamilyInfo.fromJson(Map<String, dynamic> json) => FamilyInfo(
        id: json['id'] as String,
        name: json['name'] as String,
        joinCode: json['joinCode'] as String,
      );
}

class InvitationInfo {
  final String inviteCode;
  final String familyName;
  final String inviterDisplayName;

  const InvitationInfo({
    required this.inviteCode,
    required this.familyName,
    required this.inviterDisplayName,
  });

  factory InvitationInfo.fromJson(Map<String, dynamic> json) => InvitationInfo(
        inviteCode: json['inviteCode'] as String,
        familyName: json['familyName'] as String,
        inviterDisplayName: json['inviterDisplayName'] as String,
      );
}

class FamilyService {
  final ApiClient _client;

  FamilyService(this._client);

  Future<FamilyInfo> createFamily(String name) async {
    final json = await _client.post(
      '/api/control-plane/v1/families',
      body: {'familyName': name},
    ) as Map<String, dynamic>;
    return FamilyInfo.fromJson(json);
  }

  Future<FamilyInfo> joinFamily(String joinCode) async {
    final json = await _client.post(
      '/api/control-plane/v1/families/join',
      body: {'joinCode': joinCode},
    ) as Map<String, dynamic>;
    return FamilyInfo.fromJson(json);
  }

  Future<void> leaveFamily(String familyId) async {
    await _client.delete('/api/control-plane/v1/families/$familyId/members/me');
  }

  Future<InvitationInfo> createInvitation({String? email}) async {
    final json = await _client.post(
      '/api/control-plane/v1/invitations',
      body: {if (email != null) 'email': email},
    ) as Map<String, dynamic>;
    return InvitationInfo.fromJson(json);
  }

  Future<InvitationInfo> getInvitationDetails(String code) async {
    final json = await _client.get('/api/control-plane/v1/invitations/$code/details')
        as Map<String, dynamic>;
    return InvitationInfo.fromJson(json);
  }

  Future<InvitationInfo> acceptInvitation(String code) async {
    final json = await _client.post('/api/control-plane/v1/invitations/$code/accept')
        as Map<String, dynamic>;
    return InvitationInfo.fromJson(json);
  }

  Future<List<FamilyMember>> getMembers(String familyId) async {
    final json = await _client.get('/api/data-plane/v1/families/$familyId/members') as List;
    return json.map((e) => FamilyMember.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<FamilyMember> addMember({
    required String familyId,
    required String fullName,
    String? parentMemberId,
    int? birthYear,
    int? deathYear,
    String? bio,
    String? location,
  }) async {
    final json = await _client.post(
      '/api/data-plane/v1/families/$familyId/members',
      body: {
        'fullName': fullName,
        if (parentMemberId != null) 'parentMemberId': parentMemberId,
        if (birthYear != null) 'birthYear': birthYear,
        if (deathYear != null) 'deathYear': deathYear,
        if (bio != null) 'bio': bio,
        if (location != null) 'location': location,
      },
    ) as Map<String, dynamic>;
    return FamilyMember.fromJson(json);
  }

  Future<FamilyMember> updateMember({
    required String familyId,
    required String memberId,
    String? fullName,
    int? birthYear,
    int? deathYear,
    String? bio,
    String? location,
    String? parentMemberId,
  }) async {
    final json = await _client.put(
      '/api/data-plane/v1/families/$familyId/members/$memberId',
      body: {
        if (fullName != null) 'fullName': fullName,
        if (birthYear != null) 'birthYear': birthYear,
        if (deathYear != null) 'deathYear': deathYear,
        if (bio != null) 'bio': bio,
        if (location != null) 'location': location,
        if (parentMemberId != null) 'parentMemberId': parentMemberId,
      },
    ) as Map<String, dynamic>;
    return FamilyMember.fromJson(json);
  }

  Future<FamilyMember> uploadPhoto({
    required String familyId,
    required String memberId,
    required File photo,
  }) async {
    final json = await _client.uploadFile(
      '/api/data-plane/v1/families/$familyId/members/$memberId/photo',
      photo,
      'photo',
    ) as Map<String, dynamic>;
    return FamilyMember.fromJson(json);
  }

  Future<Map<String, dynamic>> exportTree(String familyId) async {
    return await _client.get('/api/data-plane/v1/families/$familyId/export')
        as Map<String, dynamic>;
  }

  /// Saves a JSON export to the app documents directory and returns the file path.
  Future<String> saveExportLocally(String familyId, Map<String, dynamic> exportData) async {
    final dir = await getApplicationDocumentsDirectory();
    final file = File('${dir.path}/family_tree_export_$familyId.json');
    await file.writeAsString(jsonEncode(exportData));
    return file.path;
  }

  /// Reads a previously saved local export.
  Future<Map<String, dynamic>?> loadLocalExport(String familyId) async {
    try {
      final dir = await getApplicationDocumentsDirectory();
      final file = File('${dir.path}/family_tree_export_$familyId.json');
      if (!await file.exists()) return null;
      return jsonDecode(await file.readAsString()) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }
}
