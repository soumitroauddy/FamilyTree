import '../models/family_tree.dart';
import 'sample_family_data.dart';

class FamilyRepository {
  const FamilyRepository();

  FamilyTree load() {
    return SampleFamilyData.tree();
  }
}
