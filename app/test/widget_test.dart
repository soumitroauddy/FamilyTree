import 'package:flutter_test/flutter_test.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'package:family_tree_app/app/app.dart';

void main() {
  setUpAll(() async {
    TestWidgetsFlutterBinding.ensureInitialized();
    await Supabase.initialize(
      url: 'https://obtctulilwkjxixizryr.supabase.co',
      anonKey: 'sb_publishable_OSGMwAzQIHlvVQv6BNw8jg_3Fpm4h-F',
    );
  });

  testWidgets('App launches and shows Family Tree title', (WidgetTester tester) async {
    await tester.pumpWidget(const FamilyTreeApp());
    await tester.pump();

    expect(find.text('Family Tree'), findsWidgets);
  });
}
