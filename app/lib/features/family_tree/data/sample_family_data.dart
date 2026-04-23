import '../models/family_tree.dart';
import '../models/person.dart';

class SampleFamilyData {
  const SampleFamilyData._();

  static FamilyTree tree() {
    final people = <Person>[
      const Person(
        id: 'p1',
        fullName: 'Eleanor Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=47',
        birthYear: 1938,
        deathYear: 2010,
        bio: 'Matriarch of the Stone family and a retired teacher.',
        location: 'Portland, OR',
      ),
      const Person(
        id: 'p2',
        fullName: 'Martha Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=5',
        birthYear: 1960,
        bio: 'Archivist preserving family history and photo collections.',
        location: 'Seattle, WA',
      ),
      const Person(
        id: 'p3',
        fullName: 'Daniel Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=14',
        birthYear: 1958,
        bio: 'Civil engineer and avid mountain biker.',
        location: 'Denver, CO',
      ),
      const Person(
        id: 'p4',
        fullName: 'Lucas Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=33',
        birthYear: 1988,
        bio: 'Photographer documenting extended family events.',
        location: 'Austin, TX',
      ),
      const Person(
        id: 'p5',
        fullName: 'Nina Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=45',
        birthYear: 1991,
        bio: 'UX designer focused on accessibility.',
        location: 'San Diego, CA',
      ),
      const Person(
        id: 'p6',
        fullName: 'Sophie Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=22',
        birthYear: 1993,
        bio: 'Pediatric nurse and weekend gardener.',
        location: 'Boise, ID',
      ),
      const Person(
        id: 'p7',
        fullName: 'Ethan Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=29',
        birthYear: 1990,
        bio: 'Musician and owner of a downtown studio.',
        location: 'Nashville, TN',
      ),
      const Person(
        id: 'p8',
        fullName: 'Amelia Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=38',
        birthYear: 2015,
        bio: 'Loves astronomy and drawing planets.',
      ),
      const Person(
        id: 'p9',
        fullName: 'Oliver Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=68',
        birthYear: 2018,
        bio: 'Building a legendary toy dinosaur collection.',
      ),
      const Person(
        id: 'p10',
        fullName: 'Maya Stone',
        photoUrl: 'https://i.pravatar.cc/200?img=32',
        birthYear: 2021,
        bio: 'Future tree climber and book collector.',
      ),
    ];

    return FamilyTree(
      rootId: 'p1',
      peopleById: {for (final person in people) person.id: person},
      childIdsByParentId: const {
        'p1': ['p2', 'p3'],
        'p2': ['p4', 'p5'],
        'p3': ['p6', 'p7'],
        'p4': ['p8', 'p9'],
        'p7': ['p10'],
      },
      expandedIds: const {'p1', 'p2', 'p3', 'p4', 'p7'},
    );
  }
}
