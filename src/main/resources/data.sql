MERGE INTO judge (name) KEY(name) VALUES
('Judy Naga Lastimosa'),
('Pepito Sumayan'),
('Argie Ryan Asaria'),
('Steven Patrick C. Fernandez'),
('Comm. Sittie Aisha Cayongcat-Nuska');


MERGE INTO contingent (display_name) KEY(display_name) VALUES
('Lanao Agricultural College'),
('Masiricampo National High School'),
('Pooni Lumabao Memorial & Ragayan  & Poona Bayabao NHS'),
('Malabang National High School'),
('Ganassi National High School'),
('Marawi City National High School'),
('Mindanao Islamic College'),
('Angoyao National High School');

MERGE INTO criteria (display_name, category, weight) KEY(display_name, category) VALUES
('Progressive Dancing', 'STREET_DANCE', 0.20),
('Stationary Performance', 'STREET_DANCE', 0.40),
('Musicality', 'STREET_DANCE', 0.20),
('Attire', 'STREET_DANCE', 0.20),
('Attire/ Props', 'CULTURAL_SHOWDOWN', 0.20),
('Performance/ Concept', 'CULTURAL_SHOWDOWN', 0.40),
('Musicality', 'CULTURAL_SHOWDOWN', 0.20),
('Choreography & Style', 'CULTURAL_SHOWDOWN', 0.20);
