SET search_path TO rbt, public;

INSERT INTO venue (name, address, capacity) VALUES
                                                ('Stark Arena', 'Belgrade, RS', 18000),
                                                ('Sava Centar', 'Belgrade, RS', 4000),
                                                ('O2 Arena', 'London, UK', 20000),
                                                ('Madison Square Garden', 'New York, US', 19500),
                                                ('Arena Zagreb', 'Zagreb, HR', 15000),
                                                ('Kombank Hall', 'Belgrade, RS', 2500),
                                                ('Wiener Stadthalle', 'Vienna, AT', 16000),
                                                ('Pula Arena', 'Pula, HR', 7000),
                                                ('Palau Sant Jordi', 'Barcelona, ES', 17000),
                                                ('Allianz Stadium', 'Turin, IT', 41000);

INSERT INTO performer (name, genre) VALUES
                                        ('Imagine Dragons', 'Rock'),
                                        ('Metallica', 'Metal'),
                                        ('John Mulaney', 'Comedy'),
                                        ('LA Lakers vs Boston Celtics', 'Basketball'),
                                        ('Golden State Warriors vs Phoenix Suns', 'Basketball'),
                                        ('Red Hot Chili Peppers', 'Rock'),
                                        ('Dua Lipa', 'Pop'),
                                        ('Andrea Bocelli', 'Classical'),
                                        ('FC Barcelona vs Real Madrid', 'Football'),
                                        ('Swan Lake (Ballet Company)', 'Ballet');


WITH v AS (
    SELECT name, id FROM venue
),
     p AS (
         SELECT name, id FROM performer
     )
INSERT INTO event (venue_id, performer_id, name, description, start_time, total_tickets, max_per_request, status)
VALUES
    ((SELECT id FROM v WHERE name='Stark Arena'),
     (SELECT id FROM p WHERE name='Imagine Dragons'),
     'Imagine Dragons Live', 'World tour stop', TIMESTAMPTZ '2025-10-15 20:00+02', 8000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Arena Zagreb'),
     (SELECT id FROM p WHERE name='Metallica'),
     'Metallica World Tour', 'M72 tour', TIMESTAMPTZ '2025-11-05 20:00+01', 12000, 4, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Sava Centar'),
     (SELECT id FROM p WHERE name='John Mulaney'),
     'John Mulaney Stand-up', 'One-night show', TIMESTAMPTZ '2025-09-30 19:00+02', 3000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Madison Square Garden'),
     (SELECT id FROM p WHERE name='LA Lakers vs Boston Celtics'),
     'NBA: Lakers vs Celtics', 'Regular season game', TIMESTAMPTZ '2025-10-28 19:30+00', 18000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='O2 Arena'),
     (SELECT id FROM p WHERE name='Golden State Warriors vs Phoenix Suns'),
     'NBA: Warriors vs Suns (London Game)', 'International game', TIMESTAMPTZ '2025-11-10 19:00+00', 19500, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Wiener Stadthalle'),
     (SELECT id FROM p WHERE name='Red Hot Chili Peppers'),
     'RHCP Live', 'Unlimited Love tour', TIMESTAMPTZ '2025-10-18 20:00+02', 12000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Palau Sant Jordi'),
     (SELECT id FROM p WHERE name='Dua Lipa'),
     'Dua Lipa Future Nostalgia', 'European leg', TIMESTAMPTZ '2025-12-02 20:00+01', 15000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Pula Arena'),
     (SELECT id FROM p WHERE name='Andrea Bocelli'),
     'Andrea Bocelli Gala', 'Open-air concert', TIMESTAMPTZ '2025-08-20 20:30+02', 6000, 4, 'PUBLISHED'), -- past event for testing

    ((SELECT id FROM v WHERE name='Kombank Hall'),
     (SELECT id FROM p WHERE name='Swan Lake (Ballet Company)'),
     'Swan Lake', 'Classical ballet', TIMESTAMPTZ '2025-09-25 19:30+02', 2000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Allianz Stadium'),
     (SELECT id FROM p WHERE name='FC Barcelona vs Real Madrid'),
     'El Clásico (Friendly)', 'Exhibition match', TIMESTAMPTZ '2025-10-20 20:45+02', 35000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Sava Centar'),
     (SELECT id FROM p WHERE name='John Mulaney'),
     'John Mulaney (Draft Extra Date)', 'Potential extra date', TIMESTAMPTZ '2025-10-02 19:00+02', 3000, 6, 'PUBLISHED'),

    ((SELECT id FROM v WHERE name='Stark Arena'),
     (SELECT id FROM p WHERE name='Red Hot Chili Peppers'),
     'RHCP (Closed Rehearsal)', 'Invite-only', TIMESTAMPTZ '2025-09-22 18:00+02', 1000, 2, 'CLOSED');

--Pre-generate tickets for each event
WITH prices AS (
    SELECT e.id AS event_id,
           CASE
               WHEN e.name LIKE 'NBA:%' THEN 120.00
               WHEN e.name ILIKE '%Bocelli%' THEN 95.00
               WHEN e.name ILIKE '%Swan Lake%' THEN 45.00
               WHEN e.name ILIKE '%El Clásico%' THEN 130.00
               WHEN e.name ILIKE '%Metallica%' THEN 110.00
               WHEN e.name ILIKE '%RHCP%' THEN 85.00
               WHEN e.name ILIKE '%Dua Lipa%' THEN 75.00
               WHEN e.name ILIKE '%Imagine Dragons%' THEN 70.00
               WHEN e.name ILIKE '%Mulaney%' THEN 35.00
               ELSE 60.00
               END::NUMERIC(12,2) AS price
    FROM event e
)
INSERT INTO ticket (event_id, price, status)
SELECT e.id, p.price, 'AVAILABLE'
FROM event e
         JOIN prices p ON p.event_id = e.id
         CROSS JOIN LATERAL generate_series(1, e.total_tickets) g(n);

--Mark a few as BOOKED
UPDATE ticket
SET status='BOOKED', booked_at=now(), booked_by_ref='sample-user-1@example.com'
WHERE id IN (
    SELECT t.id FROM ticket t
    WHERE t.event_id = (SELECT id FROM event WHERE name='Imagine Dragons Live')
    ORDER BY t.id
    LIMIT 10
);

UPDATE ticket
SET status='BOOKED', booked_at=now(), booked_by_ref='sample-user-2@example.com'
WHERE id IN (
    SELECT t.id FROM ticket t
    WHERE t.event_id = (SELECT id FROM event WHERE name='NBA: Lakers vs Celtics')
    ORDER BY t.id
    LIMIT 15
);
