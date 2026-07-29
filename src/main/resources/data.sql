MERGE INTO judge (name) KEY(name) VALUES
('Judy Naga Lastimosa'),
('Atty. Jonah Margarette'),
('Argie Ryan Asaria'),
('Steven Patrick C. Fernandez'),
('Marites Maguindra');


MERGE INTO contingent (display_name) KEY(display_name) VALUES
('Lanao Agricultural College'),
('Masiricampo NHS'),
('Pagayawan NHS'),
('Bansayan NHS'),
('Pantar NHS'),
('Kapai NHS'),
('Marantao NHS'),
('Buadiposo Buntong NHS');

MERGE INTO criteria (display_name, category, weight) KEY(display_name, category) VALUES
('Stationary Performance', 'STREET_DANCE', 0.40),
('Forward Motion', 'STREET_DANCE', 0.30),
('Costume and Props', 'STREET_DANCE', 0.20),
('Musicality', 'STREET_DANCE', 0.10),
('Performance', 'CULTURAL_SHOWDOWN', 0.40),
('Choreography', 'CULTURAL_SHOWDOWN', 0.30),
('Costume and Props', 'CULTURAL_SHOWDOWN', 0.20),
('Musicality', 'CULTURAL_SHOWDOWN', 0.10);
