/* Editor visual de la plantilla del PDF.
   El diseño es una lista de bloques; el servidor lo convierte a HTML (el mismo que usa
   el PDF) y aquí se muestra en una "hoja" dentro de un Shadow DOM para que los estilos
   de la app no se mezclen. Arrastrar y soltar reordena la lista y se vuelve a pintar. */
(() => {
    'use strict';

    const $ = (sel, ctx = document) => ctx.querySelector(sel);
    const base = () => $('.barra .logo').getAttribute('href').replace(/\/?$/, '/');
    const URL_PLANTILLA = base() + 'configuracion/plantilla';

    // ---------- tipos de bloque ----------

    const COLORES = [['texto', 'Texto'], ['primario', 'Principal'], ['acento', 'Acento'], ['gris', 'Gris']];
    const PALETAS = [
        ['Azul', '#243B53', '#3E7CB1'],
        ['Verde azulado', '#184E62', '#1A9C8D'],
        ['Vino', '#5B1A2E', '#C0392B'],
        ['Grafito', '#2D3436', '#E67E22'],
        ['Bosque', '#1E4D2B', '#43A047'],
        ['Morado', '#3D2C5E', '#8E6CCF'],
    ];

    // campos: [clave, etiqueta, tipo de control, opciones]
    const TIPOS = {
        encabezado: {
            nombre: 'Encabezado', icono: '▀', unico: true,
            nuevo: { titulo: 'COTIZACIÓN', logo: true, nombre: true, dosTonos: true, mayusculas: true, lema: true },
            campos: [['titulo', 'Título', 'texto'], ['logo', 'Mostrar logo', 'check'],
                ['nombre', 'Mostrar nombre de la empresa', 'check'], ['dosTonos', 'Nombre en dos colores', 'check'],
                ['mayusculas', 'Nombre en MAYÚSCULAS', 'check'], ['lema', 'Mostrar lema', 'check']],
            nota: 'El logo, el nombre y el lema se cambian en la pestaña Empresa. Folio, fecha y vigencia los pone cada cotización.',
        },
        partes: {
            nombre: 'Proveedor y cliente', icono: '▥', unico: true,
            nuevo: { tituloProveedor: 'DATOS DEL PROVEEDOR', tituloCliente: 'COTIZADO A' },
            campos: [['tituloProveedor', 'Título del proveedor', 'texto'], ['tituloCliente', 'Título del cliente', 'texto']],
            nota: 'Tus datos se editan en la pestaña Empresa. Los que dejes vacíos no se imprimen.',
        },
        detalle: {
            nombre: 'Tabla de productos', icono: '☰', unico: true,
            nuevo: { titulo: 'Detalle de productos y servicios', numero: true },
            campos: [['titulo', 'Título (vacío = sin título)', 'texto'], ['numero', 'Columna # (número de partida)', 'check']],
        },
        totales: {
            nombre: 'Totales', icono: 'Σ', unico: true, nuevo: {}, campos: [],
            nota: 'Subtotal, IVA, envío y total se calculan solos en cada cotización.',
        },
        condiciones: {
            nombre: 'Condiciones comerciales', icono: '✓', unico: true,
            nuevo: { titulo: 'Condiciones comerciales' },
            campos: [['titulo', 'Título (vacío = sin título)', 'texto']],
            nota: 'Los textos se capturan en cada cotización; los de por defecto están en Configuración.',
        },
        titulo: {
            nombre: 'Título', icono: 'T',
            nuevo: { texto: 'Nuevo título', color: 'primario', alineacion: 'izquierda' },
            campos: [['texto', 'Texto', 'texto'], ['color', 'Color', 'color'], ['alineacion', 'Alineación', 'alinear']],
        },
        texto: {
            nombre: 'Párrafo', icono: '¶',
            nuevo: { texto: 'Escribe aquí tu texto…', tamano: 9, alineacion: 'izquierda', color: 'texto', negrita: false },
            campos: [['texto', 'Texto', 'area'], ['tamano', 'Tamaño de letra', 'numero', { min: 6, max: 30, step: 0.5 }],
                ['alineacion', 'Alineación', 'alinear'], ['color', 'Color', 'color'], ['negrita', 'Negrita', 'check']],
        },
        imagen: {
            nombre: 'Imagen', icono: '▣',
            nuevo: { alto: 60, alineacion: 'centro' },
            campos: [['imagen', 'Imagen', 'imagen'], ['alto', 'Alto (puntos)', 'numero', { min: 10, max: 600, step: 5 }],
                ['alineacion', 'Alineación', 'alinear']],
        },
        firma: {
            nombre: 'Firma', icono: '✍',
            nuevo: { nombre: '', puesto: 'Ejecutivo de ventas', alineacion: 'centro' },
            campos: [['nombre', 'Nombre (vacío = el ejecutivo)', 'texto'], ['puesto', 'Puesto', 'texto'],
                ['alineacion', 'Alineación', 'alinear']],
        },
        linea: {
            nombre: 'Línea', icono: '―', nuevo: { color: 'acento' },
            campos: [['color', 'Color', 'color']],
        },
        espacio: {
            nombre: 'Espacio', icono: '↕', nuevo: { alto: 12 },
            campos: [['alto', 'Alto (puntos)', 'numero', { min: 1, max: 400, step: 1 }]],
        },
        salto: {
            nombre: 'Salto de página', icono: '⤓', nuevo: {}, campos: [],
            nota: 'Todo lo que esté debajo empieza en una hoja nueva.',
        },
        pie: {
            nombre: 'Pie de página', fijo: true,
            campos: [['texto', 'Texto (se repite en cada hoja)', 'texto', { vineta: true }], ['paginas', 'Número de página', 'check']],
        },
    };

    const CAMPOS_EMPRESA = [
        ['nombre', 'Nombre de la empresa'], ['lema', 'Lema / eslogan'], ['rfc', 'RFC'], ['telefono', 'Teléfono'],
        ['correo', 'Correo'], ['web', 'Página web'], ['direccion', 'Dirección'], ['ejecutivo', 'Ejecutivo / vendedor'],
    ];

    // ---------- estado e historial ----------

    let datos = PLANTILLA;
    datos.empresa = datos.empresa || {};
    datos.diseno.pie = datos.diseno.pie || { texto: '', paginas: false };
    datos.diseno.estilo = datos.diseno.estilo || {};

    let sel = null;           // id del bloque seleccionado ('pie' para el pie)
    let arrastre = null;      // { nuevo: tipo } o { mover: id }
    let historial = [JSON.stringify(datos)];
    let pos = 0;
    let guardado = historial[0];
    let ultimaFusion = { clave: null, t: 0 };

    const bloques = () => datos.diseno.bloques;
    const bloque = (id) => (id === 'pie' ? datos.diseno.pie : bloques().find((b) => b.id === id));
    const tipoDe = (id) => (id === 'pie' ? 'pie' : bloque(id)?.tipo);
    const nuevoId = () => 'b' + Date.now().toString(36) + Math.random().toString(36).slice(2, 5);

    /** Registra un cambio ya hecho en `datos`. `fusionar` junta tecleos seguidos en un solo paso de deshacer. */
    function cambio(fusionar) {
        const j = JSON.stringify(datos);
        if (j === historial[pos]) return;
        const ahora = Date.now();
        if (fusionar && fusionar === ultimaFusion.clave && ahora - ultimaFusion.t < 1500 && pos === historial.length - 1 && pos > 0) {
            historial[pos] = j;
        } else {
            historial = historial.slice(0, pos + 1);
            historial.push(j);
            pos = historial.length - 1;
        }
        ultimaFusion = { clave: fusionar, t: ahora };
        estado();
        pintarLuego();
    }

    function irA(n) {
        if (n < 0 || n >= historial.length) return;
        pos = n;
        datos = JSON.parse(historial[pos]);
        ultimaFusion = { clave: null, t: 0 };
        if (sel && !bloque(sel)) sel = null;
        estado();
        pintarTodo();
    }

    function estado() {
        const sucio = historial[pos] !== guardado;
        const e = $('#estado');
        e.textContent = sucio ? 'Cambios sin guardar' : 'Guardado';
        e.classList.toggle('sucio', sucio);
        $('#guardar').disabled = !sucio;
        $('#deshacer').disabled = pos === 0;
        $('#rehacer').disabled = pos === historial.length - 1;
    }

    function aviso(texto, error) {
        const t = document.createElement('div');
        t.className = 'toast' + (error ? ' error' : '');
        t.textContent = texto;
        document.body.appendChild(t);
        setTimeout(() => t.remove(), error ? 5000 : 2200);
    }

    // ---------- hoja (vista previa) ----------

    const host = $('#hoja-host');
    const raiz = host.attachShadow({ mode: 'open' });
    let pedido = 0;
    let timer;

    const CSS_EDITOR = `
        :host { display: block; }
        .hoja { position: relative; width: 612pt; min-height: 792pt; box-sizing: border-box;
                padding: 60pt 46pt 48pt; background: #fff; box-shadow: 0 3px 18px rgba(0,0,0,.16);
                display: flex; flex-direction: column; }
        .hoja > .pie { order: 99; margin-top: auto; padding-top: 30pt; }
        .blq, .pie { position: relative; outline: 1.5px dashed transparent; outline-offset: 3px; cursor: pointer; }
        .blq { cursor: grab; }
        .blq:hover, .pie:hover { outline-color: #9DB8C4; }
        .sel, .sel:hover { outline: 2px solid #1A9C8D; }
        .blq::before, .pie::before {
            content: attr(data-etiqueta); position: absolute; left: -4px; top: -21px; z-index: 5;
            font: 600 11px system-ui, sans-serif; color: #fff; background: #7B96A2; padding: 2px 8px;
            border-radius: 4px 4px 0 0; white-space: nowrap; pointer-events: none; display: none; }
        .blq:hover::before, .pie:hover::before, .sel::before { display: block; }
        .sel::before { background: #1A9C8D; }
        .arrastrando { opacity: .35; }
        .solo-editor { display: block; }
        .hueco { border: 2px dashed #C9D6DA; color: #8A9AA0; text-align: center; padding: 18pt;
                 font: 12px system-ui, sans-serif; border-radius: 4px; }
        .salto-marca { border-top: 2px dashed #B0BEC5; color: #8A9AA0; font: 11px system-ui, sans-serif;
                       text-align: center; padding-top: 3px; margin: 6pt 0; }
        .blq-espacio { background: repeating-linear-gradient(45deg, transparent 0 6px, rgba(0,0,0,.035) 6px 12px); }
        .blq-linea { padding: 2px 0; }
        .np::after { content: "1 de 1"; }
        .vacia { border: 2px dashed #C9D6DA; border-radius: 6px; color: #8A9AA0; text-align: center;
                 padding: 60pt 20pt; font: 14px system-ui, sans-serif; }
        .indicador { position: absolute; left: 30pt; right: 30pt; height: 4px; margin-top: -2px; background: #1A9C8D;
                     border-radius: 2px; pointer-events: none; z-index: 10; display: none; }
        .indicador::before, .indicador::after { content: ''; position: absolute; top: -4px; width: 12px; height: 12px;
                     border-radius: 50%; background: #1A9C8D; }
        .indicador::before { left: -6px; }
        .indicador::after { right: -6px; }
    `;

    function pintarLuego(ms = 120) {
        clearTimeout(timer);
        timer = setTimeout(pintarHoja, ms);
    }

    async function pintarHoja() {
        const n = ++pedido;
        let html;
        try {
            const r = await fetch(URL_PLANTILLA + '/vista', {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(datos),
            });
            if (!r.ok) throw new Error('El servidor respondió ' + r.status);
            html = await r.text();
        } catch (e) {
            if (n === pedido) aviso('No se pudo actualizar la vista previa: ' + e.message, true);
            return;
        }
        if (n !== pedido) return; // llegó una respuesta más nueva

        const doc = new DOMParser().parseFromString(html, 'text/html');
        const estilo = document.createElement('style');
        estilo.textContent = [...doc.querySelectorAll('style')].map((s) => s.textContent).join('\n') + CSS_EDITOR;
        const hoja = document.adoptNode(doc.querySelector('.hoja'));

        hoja.querySelectorAll('[data-bloque]').forEach((el) => {
            const t = TIPOS[el.dataset.tipo];
            el.dataset.etiqueta = t ? t.nombre : el.dataset.tipo;
            if (el.classList.contains('blq')) el.draggable = true;
            el.classList.toggle('sel', el.dataset.bloque === sel);
        });
        if (!bloques().length) {
            const v = document.createElement('div');
            v.className = 'vacia';
            v.textContent = 'La hoja está vacía. Arrastra aquí bloques de la izquierda.';
            hoja.prepend(v);
        }
        const ind = document.createElement('div');
        ind.className = 'indicador';
        hoja.appendChild(ind);

        raiz.replaceChildren(estilo, hoja);
    }

    function marcarSeleccion() {
        raiz.querySelectorAll('[data-bloque]').forEach((el) => el.classList.toggle('sel', el.dataset.bloque === sel));
    }

    function ajustarZoom() {
        const disponible = $('#lienzo').clientWidth - 48;
        host.style.zoom = Math.min(1, disponible / 816).toFixed(3);
    }

    // ---------- arrastrar y soltar ----------

    const piezas = () => [...raiz.querySelectorAll('.blq')];

    /** Índice donde caería lo arrastrado según la altura del puntero. */
    function destino(clientY) {
        const lista = piezas();
        for (let i = 0; i < lista.length; i++) {
            const r = lista[i].getBoundingClientRect();
            if (clientY < r.top + r.height / 2) return i;
        }
        return lista.length;
    }

    function indicador(i) {
        const ind = $('.indicador', raiz);
        if (!ind) return;
        if (i == null) { ind.style.display = 'none'; return; }
        const lista = piezas();
        let y;
        if (!lista.length) y = 60 * 4 / 3;
        else if (i < lista.length) y = lista[i].offsetTop - 5;
        else y = lista[lista.length - 1].offsetTop + lista[lista.length - 1].offsetHeight + 5;
        ind.style.top = y + 'px';
        ind.style.display = 'block';
    }

    function soltar(i) {
        const lista = bloques();
        if (arrastre.nuevo) {
            const b = crear(arrastre.nuevo);
            lista.splice(i, 0, b);
            sel = b.id;
        } else {
            const de = lista.findIndex((b) => b.id === arrastre.mover);
            if (de < 0 || i === de || i === de + 1) return;
            const [b] = lista.splice(de, 1);
            lista.splice(i > de ? i - 1 : i, 0, b);
        }
        cambio();
        pintarPaleta();
        pintarPanel();
    }

    function terminarArrastre() {
        arrastre = null;
        indicador(null);
        raiz.querySelectorAll('.arrastrando').forEach((el) => el.classList.remove('arrastrando'));
    }

    raiz.addEventListener('dragstart', (e) => {
        const el = e.target.closest?.('.blq');
        if (!el) return;
        arrastre = { mover: el.dataset.bloque };
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/plain', el.dataset.bloque);
        el.classList.add('arrastrando');
    });
    raiz.addEventListener('dragover', (e) => {
        if (!arrastre) return;
        e.preventDefault();
        e.dataTransfer.dropEffect = arrastre.nuevo ? 'copy' : 'move';
        indicador(destino(e.clientY));
    });
    raiz.addEventListener('drop', (e) => {
        if (!arrastre) return;
        e.preventDefault();
        const i = destino(e.clientY);
        soltar(i);
        terminarArrastre();
    });
    raiz.addEventListener('dragend', terminarArrastre);
    host.addEventListener('dragleave', (e) => {
        if (!host.contains(e.relatedTarget) && !raiz.contains(e.relatedTarget)) indicador(null);
    });
    // soltar en el fondo gris alrededor de la hoja también cuenta
    $('#lienzo').addEventListener('dragover', (e) => {
        if (!arrastre) return;
        e.preventDefault();
        indicador(destino(e.clientY));
    });
    $('#lienzo').addEventListener('drop', (e) => {
        if (!arrastre) return;
        e.preventDefault();
        soltar(destino(e.clientY));
        terminarArrastre();
    });

    raiz.addEventListener('click', (e) => {
        const el = e.target.closest?.('[data-bloque]');
        seleccionar(el ? el.dataset.bloque : null);
    });
    $('#lienzo').addEventListener('click', (e) => {
        if (e.target === e.currentTarget) seleccionar(null);
    });

    function crear(tipo) {
        return { id: nuevoId(), tipo, ...structuredClone(TIPOS[tipo].nuevo) };
    }

    function seleccionar(id) {
        sel = id;
        marcarSeleccion();
        pestana('bloque');
        pintarPanel();
    }

    // ---------- paleta de bloques ----------

    function pintarPaleta() {
        const cont = $('#paleta');
        cont.innerHTML = '';
        Object.entries(TIPOS).filter(([, t]) => !t.fijo).forEach(([tipo, t]) => {
            const usado = t.unico && bloques().some((b) => b.tipo === tipo);
            const p = document.createElement('div');
            p.className = 'pieza' + (usado ? ' usado' : '');
            p.draggable = !usado;
            p.title = usado ? 'Ya está en la hoja' : 'Arrastra a la hoja o haz clic';
            p.innerHTML = '<span class="ico"></span><span></span>';
            p.firstChild.textContent = t.icono;
            p.lastChild.textContent = t.nombre;
            if (!usado) {
                p.addEventListener('dragstart', (e) => {
                    arrastre = { nuevo: tipo };
                    e.dataTransfer.effectAllowed = 'copy';
                    e.dataTransfer.setData('text/plain', tipo);
                });
                p.addEventListener('dragend', terminarArrastre);
                p.addEventListener('click', () => {
                    // se agrega debajo del seleccionado, o al final
                    const i = bloques().findIndex((b) => b.id === sel);
                    arrastre = { nuevo: tipo };
                    soltar(i >= 0 ? i + 1 : bloques().length);
                    arrastre = null;
                });
            }
            cont.appendChild(p);
        });
    }

    // ---------- panel de propiedades ----------

    function pestana(nombre) {
        document.querySelectorAll('.ed-tabs button').forEach((b) => b.classList.toggle('activo', b.dataset.tab === nombre));
        ['bloque', 'empresa', 'estilo'].forEach((t) => { $('#tab-' + t).hidden = t !== nombre; });
    }
    document.querySelectorAll('.ed-tabs button').forEach((b) => b.addEventListener('click', () => pestana(b.dataset.tab)));

    function el(tag, props = {}, ...hijos) {
        const e = document.createElement(tag);
        Object.assign(e, props);
        hijos.forEach((h) => h != null && e.append(h));
        return e;
    }

    /** Crea el control para obj[clave] y lo engancha al historial. */
    function control(obj, clave, etiqueta, tipo, op = {}, idFusion = '') {
        const fus = idFusion + '.' + clave;
        const poner = (v, fusionar) => { obj[clave] = v; cambio(fusionar ? fus : null); };
        const campo = el('div', { className: 'campo' });

        switch (tipo) {
            case 'check': {
                campo.classList.add('check');
                const cb = el('input', { type: 'checkbox', checked: obj[clave] === true });
                cb.addEventListener('change', () => poner(cb.checked));
                campo.append(el('label', {}, cb, etiqueta));
                return campo;
            }
            case 'area':
            case 'texto': {
                const inp = tipo === 'area' ? el('textarea', { rows: 5 }) : el('input');
                inp.value = obj[clave] ?? '';
                inp.addEventListener('input', () => poner(inp.value, true));
                campo.append(el('label', { textContent: etiqueta }), inp);
                if (op.vineta) {
                    const b = el('button', { type: 'button', className: 'btn chico', textContent: 'Insertar •', style: 'margin-top:6px' });
                    b.addEventListener('click', () => {
                        const i = inp.selectionStart ?? inp.value.length;
                        inp.value = inp.value.slice(0, i) + '  •  ' + inp.value.slice(inp.selectionEnd ?? i);
                        poner(inp.value);
                        inp.focus();
                    });
                    campo.append(b);
                }
                return campo;
            }
            case 'numero': {
                const inp = el('input', { type: 'number', min: op.min, max: op.max, step: op.step, className: 'num' });
                inp.value = obj[clave] ?? '';
                inp.addEventListener('input', () => {
                    const n = Number(inp.value);
                    if (inp.value !== '' && Number.isFinite(n)) poner(n, true);
                });
                campo.append(el('label', { textContent: etiqueta }), inp);
                return campo;
            }
            case 'alinear': {
                const seg = el('div', { className: 'segmento' });
                [['izquierda', '⯇ Izq.'], ['centro', 'Centro'], ['derecha', 'Der. ⯈']].forEach(([v, t]) => {
                    const b = el('button', { type: 'button', textContent: t });
                    b.classList.toggle('activo', (obj[clave] || 'izquierda') === v);
                    b.addEventListener('click', () => { poner(v); pintarPanel(); });
                    seg.append(b);
                });
                campo.append(el('label', { textContent: etiqueta }), seg);
                return campo;
            }
            case 'color': {
                const est = new Estilo(datos.diseno.estilo);
                const fila = el('div', { className: 'muestras' });
                COLORES.forEach(([v, t]) => {
                    const b = el('button', { type: 'button', className: 'muestra-color', title: t });
                    b.style.background = est.color(v);
                    b.classList.toggle('activo', (obj[clave] || 'texto') === v);
                    b.addEventListener('click', () => { poner(v); pintarPanel(); });
                    fila.append(b);
                });
                campo.append(el('label', { textContent: etiqueta }), fila);
                return campo;
            }
            case 'imagen': {
                campo.append(el('label', { textContent: etiqueta }));
                if (obj[clave]) campo.append(el('img', { src: obj[clave], className: 'img-prev', alt: '' }));
                const archivo = el('input', { type: 'file', accept: 'image/*', hidden: true });
                const elegir = el('button', { type: 'button', className: 'btn chico', textContent: obj[clave] ? 'Cambiar imagen…' : 'Elegir imagen…' });
                elegir.addEventListener('click', () => archivo.click());
                archivo.addEventListener('change', async () => {
                    if (!archivo.files[0]) return;
                    try {
                        poner(await leerImagen(archivo.files[0]));
                        pintarPanel();
                    } catch (e) {
                        aviso(e.message, true);
                    }
                });
                const botones = el('div', { className: 'botones-img' }, elegir, archivo);
                if (obj[clave]) {
                    const quitar = el('button', { type: 'button', className: 'btn chico peligro', textContent: 'Quitar' });
                    quitar.addEventListener('click', () => { obj[clave] = null; cambio(); pintarPanel(); });
                    botones.append(quitar);
                }
                campo.append(botones);
                return campo;
            }
        }
        return campo;
    }

    /** Lee una imagen, la reduce a máximo 800 px y la devuelve como data URI (PNG o JPG). */
    function leerImagen(file) {
        return new Promise((ok, mal) => {
            const img = new Image();
            const url = URL.createObjectURL(file);
            img.onload = () => {
                URL.revokeObjectURL(url);
                const k = Math.min(1, 800 / Math.max(img.naturalWidth, img.naturalHeight));
                const cv = document.createElement('canvas');
                cv.width = Math.max(1, Math.round(img.naturalWidth * k));
                cv.height = Math.max(1, Math.round(img.naturalHeight * k));
                cv.getContext('2d').drawImage(img, 0, 0, cv.width, cv.height);
                ok(file.type === 'image/jpeg' ? cv.toDataURL('image/jpeg', 0.9) : cv.toDataURL('image/png'));
            };
            img.onerror = () => { URL.revokeObjectURL(url); mal(new Error('No se pudo leer la imagen')); };
            img.src = url;
        });
    }

    function pintarPanel() {
        const cont = $('#tab-bloque');
        cont.innerHTML = '';
        const b = sel && bloque(sel);
        if (!b) {
            cont.append(el('p', { className: 'tenue', textContent: 'Haz clic en un bloque de la hoja para editarlo.' }));
            cont.append(el('p', { className: 'tenue', textContent: 'Tip: arrastra los bloques para cambiar el orden; Supr borra el seleccionado.' }));
            return;
        }
        const tipo = tipoDe(sel);
        const t = TIPOS[tipo] || { nombre: tipo, campos: [] };
        cont.append(el('h3', { className: 'ed-titulo-bloque', textContent: t.nombre }));

        if (!t.fijo) {
            const i = bloques().findIndex((x) => x.id === sel);
            const acc = el('div', { className: 'ed-acc-bloque' });
            const boton = (texto, titulo, fn, deshabilitado, clase = '') => {
                const bt = el('button', { type: 'button', className: 'btn chico ' + clase, textContent: texto, title: titulo, disabled: deshabilitado });
                bt.addEventListener('click', fn);
                acc.append(bt);
            };
            boton('↑ Subir', 'Mover arriba', () => mover(i, -1), i === 0);
            boton('↓ Bajar', 'Mover abajo', () => mover(i, 1), i === bloques().length - 1);
            boton('Duplicar', 'Duplicar bloque', duplicar, !!t.unico);
            boton('Eliminar', 'Eliminar bloque (Supr)', eliminar, false, 'peligro');
            cont.append(acc);
        }
        if (t.nota) cont.append(el('div', { className: 'nota', textContent: t.nota }));
        t.campos.forEach(([k, etq, tc, op]) => cont.append(control(b, k, etq, tc, op, sel)));
    }

    function mover(i, d) {
        const lista = bloques();
        const j = i + d;
        if (j < 0 || j >= lista.length) return;
        [lista[i], lista[j]] = [lista[j], lista[i]];
        cambio();
        pintarPanel();
    }

    function duplicar() {
        const lista = bloques();
        const i = lista.findIndex((x) => x.id === sel);
        const copia = { ...structuredClone(lista[i]), id: nuevoId() };
        lista.splice(i + 1, 0, copia);
        sel = copia.id;
        cambio();
        pintarPanel();
    }

    function eliminar() {
        const lista = bloques();
        const i = lista.findIndex((x) => x.id === sel);
        if (i < 0) return;
        lista.splice(i, 1);
        sel = null;
        cambio();
        pintarPaleta();
        pintarPanel();
        aviso('Bloque eliminado (Ctrl+Z para deshacer)');
    }

    function pintarEmpresa() {
        const cont = $('#tab-empresa');
        cont.innerHTML = '';
        cont.append(el('div', { className: 'nota', textContent: 'Aparecen en el encabezado y en "Datos del proveedor". Lo que dejes vacío no se imprime.' }));
        CAMPOS_EMPRESA.forEach(([k, etq]) => cont.append(control(datos.empresa, k, etq, 'texto', {}, 'empresa')));
        cont.append(control(datos.empresa, 'logo', 'Logo', 'imagen'));
        const img = $('#tab-empresa .campo:last-child');
        img.append(el('p', { className: 'tenue', textContent: 'PNG con fondo transparente se ve mejor sobre el color del encabezado.' }));
    }

    function pintarEstilo() {
        const cont = $('#tab-estilo');
        const est = datos.diseno.estilo;
        cont.innerHTML = '';

        cont.append(el('label', { textContent: 'Combinaciones rápidas' }));
        const pal = el('div', { className: 'paletas' });
        PALETAS.forEach(([nombre, p, a]) => {
            const b = el('button', { type: 'button', className: 'paleta-btn' },
                el('span', { className: 'c', style: 'background:' + p }), el('span', { className: 'c', style: 'background:' + a }), nombre);
            b.addEventListener('click', () => { est.primario = p; est.acento = a; cambio(); pintarEstilo(); });
            pal.append(b);
        });
        cont.append(pal);

        const color = (k, etq, def) => {
            const campo = el('div', { className: 'campo' });
            const picker = el('input', { type: 'color', value: est[k] || def });
            const hex = el('input', { value: (est[k] || def).toUpperCase(), maxLength: 7 });
            picker.addEventListener('input', () => { est[k] = picker.value.toUpperCase(); hex.value = est[k]; cambio('estilo.' + k); });
            hex.addEventListener('input', () => {
                if (/^#[0-9a-f]{6}$/i.test(hex.value)) { est[k] = hex.value.toUpperCase(); picker.value = hex.value; cambio('estilo.' + k); }
            });
            campo.append(el('label', { textContent: etq }), el('div', { className: 'color-fila' }, picker, hex));
            return campo;
        };
        cont.append(el('div', { style: 'height:14px' }));
        cont.append(color('primario', 'Color principal (encabezado, tabla)', '#243B53'));
        cont.append(color('acento', 'Color de acento (título, total)', '#3E7CB1'));
        cont.append(color('texto', 'Color del texto', '#2B3A40'));

        const fuente = el('div', { className: 'campo' });
        const sf = el('select');
        [['Exo2', 'Exo 2 (moderna)'], ['Liberation', 'Liberation Sans (clásica)']].forEach(([v, t]) => sf.append(el('option', { value: v, textContent: t })));
        sf.value = est.fuenteMarca || 'Exo2';
        sf.addEventListener('change', () => { est.fuenteMarca = sf.value; cambio(); });
        fuente.append(el('label', { textContent: 'Letra del nombre de la empresa' }), sf);
        cont.append(fuente);

        cont.append(control(est, 'tamano', 'Tamaño de letra general (pt)', 'numero', { min: 7, max: 12, step: 0.5 }, 'estilo'));
    }

    /** Mismo cálculo de colores que Estilo.java, para las muestras del panel. */
    function Estilo(e) {
        const p = e.primario || '#243B53', a = e.acento || '#3E7CB1', t = e.texto || '#2B3A40';
        this.color = (n) => ({ primario: p, acento: a, gris: '#7A8A90' }[n] || t);
    }

    function pintarTodo() {
        pintarPaleta();
        pintarPanel();
        pintarEmpresa();
        pintarEstilo();
        pintarHoja();
    }

    // ---------- barra de acciones ----------

    async function guardar() {
        const enviado = historial[pos];
        try {
            const r = await fetch(URL_PLANTILLA, {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: enviado,
            });
            if (!r.ok) {
                const j = await r.json().catch(() => ({}));
                throw new Error(j.error || 'El servidor respondió ' + r.status);
            }
            guardado = enviado;
            estado();
            aviso('Plantilla guardada. Los siguientes PDF ya salen así.');
        } catch (e) {
            aviso('No se pudo guardar: ' + e.message, true);
        }
    }

    $('#guardar').addEventListener('click', guardar);
    $('#deshacer').addEventListener('click', () => irA(pos - 1));
    $('#rehacer').addEventListener('click', () => irA(pos + 1));
    $('#verPdf').addEventListener('click', () => {
        const f = $('#formPdf');
        f.datos.value = JSON.stringify(datos);
        f.submit();
    });
    $('#generico').addEventListener('click', () => {
        if (!confirm('¿Reemplazar el diseño por la plantilla genérica?\nTus datos de empresa y logo se conservan. Puedes deshacerlo con Ctrl+Z.')) return;
        datos.diseno = structuredClone(GENERICO);
        sel = null;
        cambio();
        pintarTodo();
    });

    const escribiendo = () => /^(INPUT|TEXTAREA|SELECT)$/.test(document.activeElement?.tagName);
    document.addEventListener('keydown', (e) => {
        const ctrl = e.ctrlKey || e.metaKey;
        if (ctrl && e.key.toLowerCase() === 's') {
            e.preventDefault();
            if (!$('#guardar').disabled) guardar();
        } else if (escribiendo()) {
            // en los campos de texto, Ctrl+Z y Supr son los del navegador
        } else if (ctrl && e.key.toLowerCase() === 'z') {
            e.preventDefault();
            irA(e.shiftKey ? pos + 1 : pos - 1);
        } else if (ctrl && e.key.toLowerCase() === 'y') {
            e.preventDefault();
            irA(pos + 1);
        } else if ((e.key === 'Delete' || e.key === 'Backspace') && sel && sel !== 'pie') {
            e.preventDefault();
            eliminar();
        } else if (e.key === 'Escape') {
            seleccionar(null);
        }
    });

    window.addEventListener('beforeunload', (e) => {
        if (historial[pos] !== guardado) e.preventDefault();
    });
    window.addEventListener('resize', ajustarZoom);

    ajustarZoom();
    estado();
    $('#estado').textContent = 'Sin cambios';
    pintarTodo();
})();
