class Person {
  const Person({
    required this.id,
    required this.fullName,
    required this.photoUrl,
    this.birthYear,
    this.deathYear,
    this.bio,
    this.location,
    this.depth = 0,
    this.childrenIds = const <String>[],
    this.isExpanded = true,
  });

  final String id;
  final String fullName;
  final String photoUrl;
  final int? birthYear;
  final int? deathYear;
  final String? bio;
  final String? location;
  final int depth;
  final List<String> childrenIds;
  final bool isExpanded;

  String get lifeSpanLabel {
    if (birthYear == null && deathYear == null) {
      return 'Year unknown';
    }
    if (birthYear != null && deathYear == null) {
      return 'b. $birthYear';
    }
    if (birthYear == null && deathYear != null) {
      return 'd. $deathYear';
    }
    return '$birthYear – $deathYear';
  }

  String get initials {
    final parts = fullName.split(' ').where((part) => part.trim().isNotEmpty);
    final value = parts.take(2).map((part) => part[0].toUpperCase()).join();
    return value.isEmpty ? '?' : value;
  }

  Person copyWith({
    int? depth,
    List<String>? childrenIds,
    bool? isExpanded,
  }) {
    return Person(
      id: id,
      fullName: fullName,
      photoUrl: photoUrl,
      birthYear: birthYear,
      deathYear: deathYear,
      bio: bio,
      location: location,
      depth: depth ?? this.depth,
      childrenIds: childrenIds ?? this.childrenIds,
      isExpanded: isExpanded ?? this.isExpanded,
    );
  }
}
