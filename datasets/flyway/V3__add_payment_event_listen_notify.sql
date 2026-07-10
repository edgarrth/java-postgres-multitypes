create table if not exists payment_event_notifications (
    id uuid primary key,
    event_id uuid not null references payment_outbox_events (id),
    channel varchar(80) not null,
    action varchar(80) not null,
    aggregate_id varchar(80) not null,
    event_type varchar(120) not null,
    status varchar(20) not null check (status in ('PENDING', 'PUBLISHED', 'FAILED')),
    payload jsonb not null,
    received_at timestamptz not null default now()
);

create index if not exists idx_payment_event_notifications_received_at
    on payment_event_notifications (received_at desc);

create index if not exists idx_payment_event_notifications_event_id
    on payment_event_notifications (event_id);

create or replace function notify_payment_outbox_event()
returns trigger
language plpgsql
as $$
declare
    notification_payload jsonb;
begin
    notification_payload := jsonb_build_object(
        'action', case when TG_OP = 'INSERT' then 'EVENT_APPENDED' else 'EVENT_STATUS_CHANGED' end,
        'eventId', NEW.id
    );

    perform pg_notify('payment_events', notification_payload::text);
    return NEW;
end;
$$;

drop trigger if exists trg_notify_payment_outbox_event on payment_outbox_events;

create trigger trg_notify_payment_outbox_event
after insert or update of status on payment_outbox_events
for each row
execute function notify_payment_outbox_event();
