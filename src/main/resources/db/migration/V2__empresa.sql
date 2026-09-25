-- Datos de la empresa que aparecen en el PDF. Vienen genéricos; se editan en
-- Configuración > Editar plantilla. El diseño (bloques y colores) se guarda en
-- 'plantilla.diseno' como JSON; si no existe se usa el diseño genérico del jar.

INSERT OR IGNORE INTO config (clave, valor) VALUES
    ('empresa.nombre', 'Mi Empresa'),
    ('empresa.lema', 'Productos y servicios para tu negocio'),
    ('empresa.rfc', 'XAXX010101000'),
    ('empresa.telefono', '55 0000 0000'),
    ('empresa.correo', 'ventas@miempresa.com'),
    ('empresa.direccion', 'Ciudad, Estado'),
    ('empresa.web', ''),
    ('empresa.ejecutivo', 'Nombre del ejecutivo'),
    ('empresa.logo', '');
