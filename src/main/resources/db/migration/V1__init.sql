-- Los montos se guardan como TEXT para conservar la precisión exacta de BigDecimal.

CREATE TABLE config (
    clave TEXT PRIMARY KEY,
    valor TEXT
);

CREATE TABLE cliente (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre    TEXT NOT NULL UNIQUE COLLATE NOCASE,
    contacto  TEXT,
    telefono  TEXT,
    email     TEXT,
    rfc       TEXT,
    direccion TEXT
);

CREATE TABLE producto (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    descripcion   TEXT NOT NULL UNIQUE COLLATE NOCASE,
    ultimo_costo  TEXT,
    ultimo_precio TEXT
);

CREATE TABLE cotizacion (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    folio             TEXT NOT NULL UNIQUE,
    fecha             TEXT NOT NULL,
    vigencia_dias     INTEGER NOT NULL,
    estado            TEXT NOT NULL DEFAULT 'BORRADOR',
    cliente_id        INTEGER REFERENCES cliente(id),
    -- copia de los datos del cliente al momento de cotizar
    cliente_nombre    TEXT NOT NULL,
    cliente_contacto  TEXT,
    cliente_telefono  TEXT,
    cliente_email     TEXT,
    cliente_rfc       TEXT,
    cliente_direccion TEXT,
    aplica_iva        INTEGER NOT NULL DEFAULT 1,
    tasa_iva          TEXT NOT NULL,
    envio             TEXT NOT NULL DEFAULT '0',
    subtotal          TEXT NOT NULL,
    iva               TEXT NOT NULL,
    total             TEXT NOT NULL,
    forma_pago        TEXT,
    tiempo_entrega    TEXT,
    garantia          TEXT,
    observaciones     TEXT,
    creada            TEXT NOT NULL,
    modificada        TEXT NOT NULL
);

CREATE TABLE partida (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    cotizacion_id   INTEGER NOT NULL REFERENCES cotizacion(id) ON DELETE CASCADE,
    orden           INTEGER NOT NULL,
    producto_id     INTEGER REFERENCES producto(id),
    descripcion     TEXT NOT NULL,
    cantidad        TEXT NOT NULL,
    costo           TEXT,
    pct_ganancia    TEXT,
    precio_unitario TEXT NOT NULL,
    importe         TEXT NOT NULL
);

CREATE INDEX idx_partida_cotizacion ON partida(cotizacion_id);

INSERT INTO config (clave, valor) VALUES
    ('folio.siguiente', '1'),
    ('folio.prefijo', 'COT-'),
    ('iva.tasa', '16'),
    ('ganancia.default', '30'),
    ('vigencia.default', '15'),
    ('condicion.forma_pago', '50% anticipo y 50% contra entrega'),
    ('condicion.tiempo_entrega', '3 a 7 días hábiles sujeto a existencias y confirmación de pago.'),
    ('condicion.garantia', 'Garantía de fabricante según producto. Soporte de gestión incluido.'),
    ('condicion.observaciones', 'Precios expresados en MXN.');
