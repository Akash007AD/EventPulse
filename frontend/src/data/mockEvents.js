// EventPulse Live Entertainment Catalog (Stitch Light Matte Spec)

export const MOCK_EVENTS = [
  {
    id: "evt-001",
    title: "Acoustic Nights with Arijit Singh Live",
    slug: "arijit-singh-acoustic-nights-live",
    category: "CONCERT",
    categoryLabel: "Concerts",
    genre: "Bollywood | Sufi | Acoustic",
    language: "Hindi",
    duration: "3h 15m",
    ageRestriction: "All Ages",
    city: "Mumbai",
    venueName: "DY Patil Stadium",
    venueAddress: "Sector 7, Nerul, Navi Mumbai",
    startTime: "2026-12-14T19:00:00Z",
    endTime: "2026-12-14T22:15:00Z",
    posterUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuC8QOOlV1iJbyC-Lqx0CaRY6ZbMpRsA0y6gn5Ko5QiWYHzZK1LTP_URByQavUbn9NODDsQT1qgssQ91QgY6UpeURaks44xgO7t9dqwYjcU9Lf9us-tVg0YSYmXr26X2-Vdbk6j8_lao3QfNCphg12_Tek8Uo2goiKQOmS6-vp_13datXB_ngX6DZIUuIcBKVGeWrJBO1Tr_yFocQ22ubLWTUP1dJYPUfvP__M-3qsxwmHIb1Nyd0uXS",
    bannerUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuC8QOOlV1iJbyC-Lqx0CaRY6ZbMpRsA0y6gn5Ko5QiWYHzZK1LTP_URByQavUbn9NODDsQT1qgssQ91QgY6UpeURaks44xgO7t9dqwYjcU9Lf9us-tVg0YSYmXr26X2-Vdbk6j8_lao3QfNCphg12_Tek8Uo2goiKQOmS6-vp_13datXB_ngX6DZIUuIcBKVGeWrJBO1Tr_yFocQ22ubLWTUP1dJYPUfvP__M-3qsxwmHIb1Nyd0uXS",
    basePrice: 1999,
    rating: "98% (12.4k ratings)",
    tag: "SELLING FAST",
    formatBadge: "STADIUM TOUR",
    description: "An unforgettable evening of soulful melodies and acoustic orchestrations. Arijit Singh performs live with a 45-piece symphony orchestra playing all chart-topping romantic ballads and iconic acoustic unplugged anthems.",
    performers: [
      {
        id: "perf-1",
        name: "Arijit Singh",
        role: "Lead Vocalist",
        imageUrl: "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-2",
        name: "Subir Malik",
        role: "Keyboards & Synth",
        imageUrl: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      stageLabel: "GRAND STAGE THIS WAY",
      tiers: [
        { name: "LOUNGE / VIP", price: 6500, rows: ["A", "B"] },
        { name: "EXECUTIVE", price: 3800, rows: ["C", "D", "E"] },
        { name: "CLUB / STANDARD", price: 1999, rows: ["F", "G", "H", "J"] }
      ]
    }
  },
  {
    id: "evt-002",
    title: "Sunburn Arena ft. Alan Walker",
    slug: "sunburn-arena-alan-walker",
    category: "CONCERT",
    categoryLabel: "Concerts",
    genre: "Electronic | EDM | Dance",
    language: "English",
    duration: "4h 00m",
    ageRestriction: "16+ Only",
    city: "Mumbai",
    venueName: "Jio World Garden, BKC",
    venueAddress: "Plot No. RG1A, G Block, Bandra Kurla Complex",
    startTime: "2026-11-28T17:30:00Z",
    endTime: "2026-11-28T22:00:00Z",
    posterUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuAhTvp9jN9UkP2SQ_PpKyJsEhsls3H5nQS55NyfM2lDYdDwS3iuKMaklj7MsEJpvh1AGyqyYhjDzZP6utLCkjViaxIwvmhMqpanjcSRrfMKVfrww3pmbLeWqV5slF31GirLHdGMhsJNkq4QqcX-EQJxRRRjyC8Ze6Z2tIIQ3zNpHfWRafEJU99l-19aAXEdvzT4SH2b0YtKgj3Q8wVPqJURPlstILOaYia9L54UnZaXnrHHMsr2itdf",
    basePrice: 1499,
    rating: "94% (8.1k ratings)",
    tag: "ALBUM TOUR",
    formatBadge: "OUTDOOR ARENA",
    description: "The global EDM pioneer brings his WalkerWorld Tour to Mumbai. Expect laser pyrotechnics, colossal LED walls, and basslines featuring global anthems 'Faded', 'Spectre', and 'Alone'.",
    performers: [
      {
        id: "perf-3",
        name: "Alan Walker",
        role: "DJ & Music Producer",
        imageUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      stageLabel: "ARENA PYRO STAGE",
      tiers: [
        { name: "FAN PIT VIP", price: 4999, rows: ["A", "B"] },
        { name: "PHASE 1 GA", price: 1499, rows: ["C", "D", "E", "F"] }
      ]
    }
  },
  {
    id: "evt-003",
    title: "Symphony of Lights: The Grand Philharmonic",
    slug: "symphony-of-lights-grand-philharmonic",
    category: "CONCERT",
    categoryLabel: "Concerts",
    genre: "Classical & Contemporary Fusion",
    language: "English & Instrumental",
    duration: "2h 30m",
    ageRestriction: "All Ages",
    city: "Mumbai",
    venueName: "NCPA Jamshed Bhabha Theatre",
    venueAddress: "NCPA Marg, Nariman Point, South Mumbai",
    startTime: "2026-11-20T19:30:00Z",
    endTime: "2026-11-20T22:00:00Z",
    posterUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuBjeJ8FRSXPm-0Hh6i7LEOIc23e3OCI-dVi10qjeGAzXhnL6MQlbtO5cetEcRCGG4PlOWzzimvcEQwEuzt8enDxaiisHKw6X2VK8VCGfyKZIlYyZLTar2s3rSf5ENrPCymsks0-D_YM9s-qklov_exSkdU0nWD9fTX_qR_po-kSa6NxvniV6clyrW9eq51t3RFrwK_Wlo4L7YxO1oxiXbeJjhX04VoIfUYdRS7QX7MqaxexRjQIai04",
    basePrice: 1200,
    rating: "98% (3.4k ratings)",
    tag: "FILLING FAST",
    formatBadge: "SURROUND 7.1",
    description: "An 80-piece international philharmonic orchestra playing live in sync with volumetric golden light beams and acoustic surround architecture. Featuring Beethoven's Symphony No. 9 and film score orchestrations.",
    performers: [
      {
        id: "perf-4",
        name: "Maestro Elena Rostova",
        role: "Chief Conductor",
        imageUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-5",
        name: "Julian Sterling",
        role: "Concertmaster Violin",
        imageUrl: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      stageLabel: "THEATRE ORCHESTRA STAGE",
      tiers: [
        { name: "ROYAL BOX", price: 3500, rows: ["A", "B"] },
        { name: "ORCHESTRA STALLS", price: 2200, rows: ["C", "D", "E"] },
        { name: "BALCONY", price: 1200, rows: ["F", "G", "H"] }
      ]
    }
  },
  {
    id: "evt-004",
    title: "Zakir Khan: Live & Unfiltered Stand-up Comedy",
    slug: "zakir-khan-live-unfiltered-comedy",
    category: "COMEDY",
    categoryLabel: "Comedy",
    genre: "Stand-up Comedy | Storytelling",
    language: "Hindi",
    duration: "1h 45m",
    ageRestriction: "16+ Only",
    city: "Mumbai",
    venueName: "The Habitat, Khar West",
    venueAddress: "1st Floor, Hotel Unicontinental, Khar",
    startTime: "2026-11-22T20:00:00Z",
    endTime: "2026-11-22T21:45:00Z",
    posterUrl: "https://lh3.googleusercontent.com/aida-public/AB6AXuBsEuunif-OldVcovgIj2OI-pWOUNA2Eyann2lu5_3MfkjPgnF6J__n_1IldNDw8nYRzHkWvPVul5lOLHj1_as4v6wLTg4HLwghpDU-VXo0HNEtEm9JFSUQA9UidrNOjF-6KmcZvio-oJxWs5SR8MxCnloYheKQIgluJRAg02VEqsRK6HJkYjpc45CN78pECFVI3dXIdkKSnMrNjkzTAbp6K7T66wh8D-62UvR7x0LWcxFtYK9DCd00",
    basePrice: 999,
    rating: "99% (18.2k ratings)",
    tag: "EXCLUSIVE",
    formatBadge: "INTIMATE VENUE",
    description: "The 'Sakht Launda' returns with an all-new confidential hour of hilarious storytelling, family observations, and unfiltered relatable humor in an intimate comedy room setting.",
    performers: [
      {
        id: "perf-6",
        name: "Zakir Khan",
        role: "Stand-up Comedian",
        imageUrl: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      stageLabel: "COMEDY STAGE",
      tiers: [
        { name: "FRONT ROW VIP", price: 1999, rows: ["A"] },
        { name: "GENERAL SEATING", price: 999, rows: ["B", "C", "D", "E"] }
      ]
    }
  },
  {
    id: "evt-005",
    title: "Hamlet: A Modernist Noir Staging",
    slug: "hamlet-modernist-noir-staging",
    category: "THEATRE",
    categoryLabel: "Plays",
    genre: "Drama | Shakespearean Tragedy",
    language: "English",
    duration: "2h 45m",
    ageRestriction: "14+ Only",
    city: "Mumbai",
    venueName: "Prithvi Theatre, Juhu",
    venueAddress: "20 Janki Kutir, Juhu Church Road",
    startTime: "2026-11-25T19:00:00Z",
    endTime: "2026-11-25T21:45:00Z",
    posterUrl: "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80",
    basePrice: 800,
    rating: "96% (2.1k ratings)",
    tag: "CRITICS PICK",
    formatBadge: "THEATRE",
    description: "Shakespeare's immortal masterpiece reimagined in a rain-slicked, neon noir metropolis with stark shadow lighting, live cello accompaniment, and breathtaking performances.",
    performers: [
      {
        id: "perf-7",
        name: "Alexander Thorne",
        role: "Prince Hamlet",
        imageUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      stageLabel: "INTIMATE PRITHVI STAGE",
      tiers: [
        { name: "PREMIUM STALLS", price: 1200, rows: ["A", "B"] },
        { name: "STANDARD SEATING", price: 800, rows: ["C", "D", "E"] }
      ]
    }
  }
];
