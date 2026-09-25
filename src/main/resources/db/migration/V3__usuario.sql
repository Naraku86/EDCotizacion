-- Usuarios para entrar a la app. Si la tabla está vacía, al arrancar se crea admin/admin
-- (por_defecto = 1 muestra un aviso hasta que se cambie la contraseña).

CREATE TABLE usuario (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre       TEXT NOT NULL UNIQUE COLLATE NOCASE,
    password     TEXT NOT NULL,
    por_defecto  INTEGER NOT NULL DEFAULT 0
);
