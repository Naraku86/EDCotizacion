-- Bloqueo temporal tras varios intentos fallidos de login.
ALTER TABLE usuario ADD COLUMN intentos_fallidos INTEGER NOT NULL DEFAULT 0;
ALTER TABLE usuario ADD COLUMN bloqueado_hasta TEXT;
