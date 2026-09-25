-- Conserva la identidad y la plantilla actuales como primera empresa.
CREATE TABLE empresa (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    datos TEXT NOT NULL,
    diseno TEXT NOT NULL
);

INSERT INTO empresa (id, nombre, datos, diseno)
SELECT 1,
       COALESCE(NULLIF(TRIM((SELECT valor FROM config WHERE clave = 'empresa.nombre')), ''), 'Mi Empresa'),
       (SELECT json_group_object(substr(clave, 9), valor) FROM config WHERE clave LIKE 'empresa.%'),
       COALESCE(NULLIF((SELECT valor FROM config WHERE clave = 'plantilla.diseno'), ''), '{}');

-- SQLite no permite añadir una FK con DEFAULT no nulo a una tabla con datos.
ALTER TABLE cotizacion ADD COLUMN empresa_id INTEGER REFERENCES empresa(id);
UPDATE cotizacion SET empresa_id = 1;
CREATE INDEX idx_cotizacion_empresa ON cotizacion(empresa_id);

-- La fuente de datos pasa a ser empresa; evitamos mantener dos copias editables.
DELETE FROM config WHERE clave LIKE 'empresa.%' OR clave = 'plantilla.diseno';
