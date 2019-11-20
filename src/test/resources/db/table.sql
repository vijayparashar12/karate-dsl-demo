CREATE TABLE event
(
    e_id bigserial NOT NULL,
    e_event_type character varying NOT NULL,
    e_event JSONB NOT NULL,
    e_eid character varying NOT NULL,
    e_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    UNIQUE(e_eid, e_event_type),
    CONSTRAINT events_pk PRIMARY KEY (e_id)
);