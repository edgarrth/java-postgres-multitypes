insert into payment_search_documents (id, payment_reference, channel, description, created_at) values
('11111111-1111-1111-1111-111111111111', 'PAY-QR-0001', 'QR', 'Pago QR aprobado para comercio cafetería Axiz con liquidación inmediata', now()),
('11111111-1111-1111-1111-111111111112', 'PAY-YAPE-0002', 'WALLET', 'Transferencia wallet interoperable pendiente de conciliación con adquirente', now()),
('11111111-1111-1111-1111-111111111113', 'PAY-CARD-0003', 'CARD', 'Autorización tarjeta rechazada por score antifraude y comercio de alto riesgo', now())
on conflict (payment_reference) do nothing;

insert into payment_semantic_rules (id, rule_code, title, description, embedding, created_at) values
('22222222-2222-2222-2222-222222222221', 'ROUTE-QR-FAST', 'Enrutamiento QR de baja fricción', 'Usar procesador QR principal para pagos presenciales con comercios verificados y bajo monto', '[0.142100,0.661400,-0.321500,0.774100,-0.012200,0.410000]'::vector, now()),
('22222222-2222-2222-2222-222222222222', 'ROUTE-WALLET-INTEROP', 'Interoperabilidad wallet', 'Enviar pagos de billeteras interoperables a switch de pagos inmediatos y generar evento de conciliación', '[-0.220000,0.380000,0.725000,-0.451000,0.202000,-0.118000]'::vector, now()),
('22222222-2222-2222-2222-222222222223', 'ROUTE-RISK-HIGH', 'Ruta de revisión antifraude', 'Derivar transacciones con alto riesgo o comercio observado al flujo de validación antifraude', '[0.850000,-0.640000,0.110000,-0.050000,0.490000,0.310000]'::vector, now())
on conflict (rule_code) do nothing;

insert into payment_cache_entries (cache_key, cache_value, expires_at, updated_at) values
('merchant-risk:MRC-1001', '{"merchantId":"MRC-1001","riskLevel":"LOW","decision":"ALLOW"}'::jsonb, now() + interval '12 hours', now()),
('exchange-rate:USD-PEN', '{"pair":"USD-PEN","rate":3.72,"source":"demo"}'::jsonb, now() + interval '1 hour', now())
on conflict (cache_key) do nothing;

insert into payment_profiles (id, profile_code, document_number, attributes, created_at) values
('33333333-3333-3333-3333-333333333331', 'PROFILE-MERCHANT-LOW', '20123456789', '{"customerSegment":"SME","merchantCategory":"restaurant","riskLevel":"LOW","settlementMode":"T0"}'::jsonb, now()),
('33333333-3333-3333-3333-333333333332', 'PROFILE-MERCHANT-HIGH', '20987654321', '{"customerSegment":"ENTERPRISE","merchantCategory":"gaming","riskLevel":"HIGH","settlementMode":"T1"}'::jsonb, now())
on conflict (profile_code) do nothing;

insert into payment_outbox_events (id, aggregate_id, event_type, payload, status, created_at) values
('44444444-4444-4444-4444-444444444441', 'PAY-QR-0001', 'PaymentAuthorized', '{"paymentReference":"PAY-QR-0001","amount":120.50,"currency":"PEN","channel":"QR"}'::jsonb, 'PENDING', now()),
('44444444-4444-4444-4444-444444444442', 'PAY-YAPE-0002', 'PaymentPendingReconciliation', '{"paymentReference":"PAY-YAPE-0002","amount":89.90,"currency":"PEN","channel":"WALLET"}'::jsonb, 'PENDING', now())
on conflict (id) do nothing;

insert into payment_digital_certificates (id, alias, owner, algorithm, fingerprint_sha256, pem_content, created_at) values
('55555555-5555-5555-5555-555555555551', 'processor-demo-public-key', 'Demo Payment Processor', 'RSA',
 '6f4b0ab2b5f2a472b2c7b062fa2b79d7e183fbf8140ef5a86ad733da2439b9a6',
 convert_to('-----BEGIN PUBLIC KEY-----\nMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALv2nDemoOnlyForAxizPocNotRealKey\n9QIDAQAB\n-----END PUBLIC KEY-----\n', 'UTF8'), now())
on conflict (alias) do nothing;
