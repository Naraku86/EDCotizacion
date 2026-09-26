-- Los intentos fallidos de entrar se cuentan en memoria por usuario + IP (IntentosLogin);
-- el bloqueo por cuenta en la base permitía que cualquiera dejara fuera al dueño.
ALTER TABLE usuario DROP COLUMN intentos_fallidos;
ALTER TABLE usuario DROP COLUMN bloqueado_hasta;
