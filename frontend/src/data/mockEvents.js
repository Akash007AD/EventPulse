// Mock events dataset matching EventPulse backend schema (EventResponse + PerformerDto)

export const MOCK_EVENTS = [
  {
    id: "evt-001",
    title: "The Royal Symphony: Beethoven & Shostakovich",
    slug: "royal-symphony-beethoven-shostakovich",
    description: "An extraordinary symphonic performance featuring Beethoven's Symphony No. 9 in D minor and Shostakovich's Cello Concerto No. 1. Conducted by world-renowned maestro Elena Rostova with soloists from the National Philharmonic.",
    category: "CONCERT",
    venueName: "Avery Fisher Concert Hall",
    address: "10 Lincoln Center Plaza",
    city: "New York",
    startTime: "2026-11-15T19:30:00Z",
    endTime: "2026-11-15T22:00:00Z",
    status: "PUBLISHED",
    posterUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1200&q=80",
    bannerUrl: "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=1400&q=80",
    basePrice: 1500,
    currency: "INR",
    totalCapacity: 120,
    availableCapacity: 84,
    organizerName: "Metropolitan Classical Guild",
    performers: [
      {
        id: "perf-101",
        name: "Elena Rostova",
        role: "Chief Conductor & Music Director",
        bio: "Laureate of the Karajan Conducting Award and Principal Guest Conductor of the Vienna Symphony.",
        imageUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-102",
        name: "Marcus Vance",
        role: "Solo Cello Virtuoso",
        bio: "Internationally acclaimed soloist performing on a 1712 Stradivarius cello on loan from the Zurich Arts Trust.",
        imageUrl: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-103",
        name: "Claire Chen",
        role: "Concertmaster Violin",
        bio: "First chair concertmaster with 14 seasons leading international orchestral tours across Europe and Asia.",
        imageUrl: "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      type: "AUDITORIUM",
      stageLabel: "GRAND ORCHESTRA STAGE",
      rows: [
        {
          rowLetter: "A",
          tier: "VIP",
          price: 3500,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "RESERVED" },
            { number: 4, status: "RESERVED" },
            { number: 5, status: "AVAILABLE" },
            { number: 6, status: "AVAILABLE" },
            { number: 7, status: "AVAILABLE" },
            { number: 8, status: "RESERVED" }
          ]
        },
        {
          rowLetter: "B",
          tier: "VIP",
          price: 3500,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" },
            { number: 5, status: "RESERVED" },
            { number: 6, status: "AVAILABLE" },
            { number: 7, status: "AVAILABLE" },
            { number: 8, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "C",
          tier: "PREMIUM",
          price: 2400,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "RESERVED" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" },
            { number: 5, status: "AVAILABLE" },
            { number: 6, status: "AVAILABLE" },
            { number: 7, status: "RESERVED" },
            { number: 8, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "D",
          tier: "PREMIUM",
          price: 2400,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "RESERVED" },
            { number: 5, status: "RESERVED" },
            { number: 6, status: "AVAILABLE" },
            { number: 7, status: "AVAILABLE" },
            { number: 8, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "E",
          tier: "STANDARD",
          price: 1500,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" },
            { number: 5, status: "AVAILABLE" },
            { number: 6, status: "AVAILABLE" },
            { number: 7, status: "AVAILABLE" },
            { number: 8, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "F",
          tier: "STANDARD",
          price: 1500,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "RESERVED" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" },
            { number: 5, status: "AVAILABLE" },
            { number: 6, status: "RESERVED" },
            { number: 7, status: "AVAILABLE" },
            { number: 8, status: "AVAILABLE" }
          ]
        }
      ]
    }
  },
  {
    id: "evt-002",
    title: "Hamlet: A Modernist Noir Staging",
    slug: "hamlet-modernist-noir-staging",
    description: "Shakespeare's timeless tragedy reimagined within a dystopian, rain-slicked metropolis. Directed by Olivier Award nominee Katherine Gray, featuring high-contrast shadow work and original analog synth orchestration.",
    category: "THEATRE",
    venueName: "The Garrick Dramatic Theatre",
    address: "Charing Cross Road",
    city: "London",
    startTime: "2026-11-20T19:00:00Z",
    endTime: "2026-11-20T21:45:00Z",
    status: "PUBLISHED",
    posterUrl: "https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=1200&q=80",
    bannerUrl: "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=1400&q=80",
    basePrice: 1200,
    currency: "INR",
    totalCapacity: 96,
    availableCapacity: 62,
    organizerName: "Vanguard Stage Productions",
    performers: [
      {
        id: "perf-201",
        name: "Alexander Thorne",
        role: "Prince Hamlet",
        bio: "Royal Shakespeare Company alumnus and Broadway World Best Actor recipient.",
        imageUrl: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-202",
        name: "Sophia Sterling",
        role: "Ophelia",
        bio: "Critically praised stage actress nominated for the Evening Standard Theatre Emerging Talent award.",
        imageUrl: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-203",
        name: "Julian Ross",
        role: "King Claudius",
        bio: "Veteran West End performer with over 30 leading theatre and screen credits.",
        imageUrl: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      type: "PROSCENIUM",
      stageLabel: "THEATRE STAGE",
      rows: [
        {
          rowLetter: "A",
          tier: "VIP",
          price: 2800,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "RESERVED" },
            { number: 5, status: "RESERVED" },
            { number: 6, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "B",
          tier: "PREMIUM",
          price: 1900,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" },
            { number: 5, status: "AVAILABLE" },
            { number: 6, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "C",
          tier: "STANDARD",
          price: 1200,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "RESERVED" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" },
            { number: 5, status: "RESERVED" },
            { number: 6, status: "AVAILABLE" }
          ]
        }
      ]
    }
  },
  {
    id: "evt-003",
    title: "Global Tech Summit 2026: Next-Gen Systems & AI",
    slug: "global-tech-summit-2026-systems-ai",
    description: "The definitive engineering conference for distributed architecture, reactive event streaming, and generative frontier models. 2 days of keynotes, architecture deep dives, and founder roundtables.",
    category: "TECH",
    venueName: "Moscone Center West",
    address: "747 Howard Street",
    city: "San Francisco",
    startTime: "2026-11-28T09:00:00Z",
    endTime: "2026-11-29T18:00:00Z",
    status: "PUBLISHED",
    posterUrl: "https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80",
    bannerUrl: "https://images.unsplash.com/photo-1505373877841-8d25f7d46678?auto=format&fit=crop&w=1400&q=80",
    basePrice: 4500,
    currency: "INR",
    totalCapacity: 250,
    availableCapacity: 175,
    organizerName: "Frontier Engineering Collective",
    performers: [
      {
        id: "perf-301",
        name: "Dr. Aris Thorne",
        role: "Keynote: Systems AI & LLM Kernels",
        bio: "Former Head of Infrastructure at DeepMind, currently leading high-throughput inference architectures.",
        imageUrl: "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-302",
        name: "Priya Nair",
        role: "Keynote: Event-Driven Kafka at Scale",
        bio: "Distinguished Engineer authoring mission-critical distributed consensus protocols for fintech.",
        imageUrl: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      type: "CONFERENCE_HALL",
      stageLabel: "KEYNOTE MAIN STAGE",
      rows: [
        {
          rowLetter: "A",
          tier: "VIP",
          price: 6500,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "B",
          tier: "STANDARD",
          price: 4500,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" }
          ]
        }
      ]
    }
  },
  {
    id: "evt-004",
    title: "Raw & Unfiltered: Standup Comedy Showcase",
    slug: "raw-unfiltered-standup-comedy-showcase",
    description: "An intimate, no-holds-barred comedy showcase featuring top-touring national headliners. 90 minutes of rapid-fire observational wit and candid satire.",
    category: "COMEDY",
    venueName: "The Comedy Cellar Club",
    address: "117 MacDougal Street",
    city: "New York",
    startTime: "2026-12-05T20:30:00Z",
    endTime: "2026-12-05T22:15:00Z",
    status: "PUBLISHED",
    posterUrl: "https://images.unsplash.com/photo-1585699324551-f6c309eedeca?auto=format&fit=crop&w=1200&q=80",
    bannerUrl: "https://images.unsplash.com/photo-1514306191717-452ec28c7814?auto=format&fit=crop&w=1400&q=80",
    basePrice: 999,
    currency: "INR",
    totalCapacity: 60,
    availableCapacity: 28,
    organizerName: "Greenwich Comedy Syndicate",
    performers: [
      {
        id: "perf-401",
        name: "Daniel Craig",
        role: "Headline Comic & Host",
        bio: "Special on Netflix and regular performer at Edinburgh Fringe Festival.",
        imageUrl: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80"
      },
      {
        id: "perf-402",
        name: "Zoe Patel",
        role: "Feature Standup Comic",
        bio: "Winner of the National Standup Open 2025, known for sharp cultural commentary.",
        imageUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"
      }
    ],
    seatingSchema: {
      type: "CLUB_SEATING",
      stageLabel: "MIC STAGE",
      rows: [
        {
          rowLetter: "A",
          tier: "FRONT_ROW",
          price: 1499,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "RESERVED" },
            { number: 4, status: "AVAILABLE" }
          ]
        },
        {
          rowLetter: "B",
          tier: "GENERAL",
          price: 999,
          seats: [
            { number: 1, status: "AVAILABLE" },
            { number: 2, status: "AVAILABLE" },
            { number: 3, status: "AVAILABLE" },
            { number: 4, status: "AVAILABLE" }
          ]
        }
      ]
    }
  }
];
