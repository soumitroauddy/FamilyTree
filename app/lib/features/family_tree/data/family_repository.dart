import '../models/family_tree.dart';
import '../models/person.dart';
import '../../../core/services/family_service.dart';
import 'sample_family_data.dart';

class FamilyRepository {
  const FamilyRepository({this.familyService});

  final FamilyService? familyService;

  /// Synchronous load — returns sample data (used as placeholder / offline fallback).
  FamilyTree load() => SampleFamilyData.tree();

  /// Async load from backend. Falls back to sample data if unavailable.
  Future<FamilyTree> loadFromBackend(String familyId) async {
    if (familyService == null) return SampleFamilyData.tree();
    try {
      final members = await familyService!.getMembers(familyId);
      return _buildTree(members);
    } catch (_) {
      return SampleFamilyData.tree();
    }
  }

  FamilyTree _buildTree(List<FamilyMember> members) {
    if (members.isEmpty) return SampleFamilyData.tree();

    final peopleById = <String, Person>{};
    final childIdsByParentId = <String, List<String>>{};
    String? rootId;

    for (final m in members) {
      peopleById[m.id] = Person(
        id: m.id,
        fullName: m.fullName,
        photoUrl: m.photoUrl ?? '',
        birthYear: m.birthYear,
        deathYear: m.deathYear,
        bio: m.bio,
        location: m.location,
      );

      if (m.parentMemberId == null) {
        rootId ??= m.id;
      } else {
        childIdsByParentId.putIfAbsent(m.parentMemberId!, () => []).add(m.id);
      }
    }

    // If no root found, use the first member
    rootId ??= members.first.id;

    // Expand root and its direct children by default
    final expandedIds = <String>{rootId};
    for (final childId in (childIdsByParentId[rootId] ?? [])) {
      expandedIds.add(childId);
    }

    return FamilyTree(
      rootId: rootId,
      peopleById: peopleById,
      childIdsByParentId: childIdsByParentId,
      expandedIds: expandedIds,
    );
  }
}
