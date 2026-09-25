/* Editor de la plantilla del PDF: se edita directo sobre la hoja.
   El servidor arma el HTML (el mismo del PDF, con editor=true) y aquí se muestra dentro de
   un Shadow DOM. Los elementos con data-editar="ruta" se vuelven editables; los que tienen
   data-accion son botones (subir logo, agregar/quitar secciones). */
(() => {
    'use strict';

    const $ = (sel, ctx = document) => ctx.querySelector(sel);
    const base = () => $('.barra .logo').getAttribute('href').replace(/\/?$/, '/');
    const URL_PLANTILLA = base() + 'configuracion/plantilla';

    const csrf = () => {
        const t = $('meta[name=_csrf]');
        return t ? { [$('meta[name=_csrf_header]').content]: t.content } : {};
    };
    const postJson = (url, cuerpo) => fetch(url, {
        method: 'POST', headers: { 'Content-Type': 'application/json', ...csrf() }, body: cuerpo,
    });

    const OPCIONES = [
        ['mostrarNombre', 'Mostrar el nombre de la empresa en el encabezado'],
        ['mayusculas', 'Nombre en MAYÚSCULAS'],
        ['numeroPartida', 'Columna # en la tabla'],
        ['paginas', 'Número de página en el pie'],
    ];

    // contentEditable "plaintext-only" evita que se peguen formatos; si el navegador no lo soporta se usa "true"
    const EDITABLE = (() => {
        try {
            const d = document.createElement('div');
            d.contentEditable = 'plaintext-only';
            return d.contentEditable === 'plaintext-only' ? 'plaintext-only' : 'true';
        } catch {
            return 'true';
        }
    })();

    // ---------- estado e historial ----------

    let datos = PLANTILLA;
    datos.empresa = datos.empresa || {};
    let historial = [JSON.stringify(datos)];
    let pos = 0;
    let guardado = historial[0];
    let ultimaFusion = { clave: null, t: 0 };
    let vistaAtrasada = false; // hubo tecleo que aún no se refleja en toda la hoja (p. ej. nombre en dos colores)

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
    }

    function irA(n) {
        if (n < 0 || n >= historial.length) return;
        pos = n;
        datos = JSON.parse(historial[pos]);
        ultimaFusion = { clave: null, t: 0 };
        estado();
        pintarBarra();
        pintarHoja();
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
        setTimeout(() => t.remove(), error ? 5000 : 2400);
    }

    /** "empresa.rfc" -> [datos.empresa, 'rfc']; "textos.titulo" -> [datos.diseno.textos, 'titulo']; "banco.texto" -> [datos.diseno.banco, 'texto'] */
    function ref(ruta) {
        const [grupo, clave] = ruta.split('.');
        if (grupo === 'empresa') return [datos.empresa, clave];
        const d = datos.diseno;
        if (!d[grupo] || typeof d[grupo] !== 'object') d[grupo] = {};
        return [d[grupo], clave];
    }

    // ---------- hoja ----------

    const host = $('#hoja-host');
    const raiz = host.attachShadow({ mode: 'open' });
    let pedido = 0;

    const CSS_HOJA = `
        :host { display: block; }
        .hoja { position: relative; width: 612pt; min-height: 792pt; box-sizing: border-box;
                padding: 50pt 42pt 48pt; background: #fff; box-shadow: 0 3px 18px rgba(0,0,0,.16);
                display: flex; flex-direction: column; }
        .hoja.a4 { width: 595pt; min-height: 842pt; }
        .hoja > .pie { order: 99; margin-top: auto; padding-top: 24pt; }
        .np::after { content: "1 de 1"; }
    `;
    const CSS_EDITOR = `
        .solo-editor { display: block; }
        [data-editar] { outline: 1.5px dashed transparent; outline-offset: 2px; border-radius: 2px; cursor: text; }
        [data-editar]:hover { outline-color: rgba(26,156,141,.65); background-color: rgba(26,156,141,.07); }
        [data-editar]:focus { outline: 2px solid #1A9C8D; }
        span[data-editar] { display: inline-block; min-width: 50px; }
        [data-editar]:empty::before { content: attr(data-ph); color: #9AA7AD; font-style: italic; font-weight: normal;
                                      text-transform: none; letter-spacing: 0; }
        .subir-logo { border: 1.5px dashed currentColor; border-radius: 6px; padding: 10px 16px; margin-bottom: 8px;
                      width: 150px; text-align: center; color: #8A9AA0; font: 13px/1.3 system-ui, sans-serif; cursor: pointer; }
        .subir-logo small { font-size: 11px; }
        .p-clasica .subir-logo { color: rgba(255,255,255,.75); }
        .subir-logo:hover, .subir-logo.encima { background: rgba(26,156,141,.15); }
        .logo-caja { position: relative; display: inline-block; cursor: pointer; }
        .logo-caja.encima { outline: 2px dashed #1A9C8D; }
        .extra, .firma-caja { position: relative; }
        .quitar { display: none; position: absolute; top: -9px; right: -9px; z-index: 3; width: 20px; height: 20px;
                  border-radius: 50%; background: #C0392B; color: #fff; font: 12px/20px system-ui, sans-serif;
                  text-align: center; cursor: pointer; }
        .logo-caja:hover .quitar, .extra:hover .quitar, .firma-caja:hover .quitar { display: block; }
        .agregar { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 18pt; }
        .agregar span { font: 13px system-ui, sans-serif; color: #2563EB; background: #EFF4FF; border: 1px dashed #A8BFF5;
                        padding: 6px 12px; border-radius: 6px; cursor: pointer; }
        .agregar span:hover { background: #E0EAFF; }
    `;

    async function vista(diseno, editor) {
        const r = await postJson(URL_PLANTILLA + '/vista' + (editor ? '?editor=true' : ''),
            JSON.stringify({ diseno, empresa: datos.empresa }));
        if (!r.ok) throw new Error('El servidor respondió ' + r.status);
        const doc = new DOMParser().parseFromString(await r.text(), 'text/html');
        const estilo = document.createElement('style');
        estilo.textContent = [...doc.querySelectorAll('style')].map((s) => s.textContent).join('\n')
            + CSS_HOJA + (editor ? CSS_EDITOR : '');
        const hoja = document.adoptNode(doc.querySelector('.hoja'));
        hoja.classList.toggle('a4', diseno.papel === 'a4');
        return [estilo, hoja];
    }

    async function pintarHoja() {
        const n = ++pedido;
        vistaAtrasada = false;
        let nodos;
        try {
            nodos = await vista(datos.diseno, true);
        } catch (e) {
            if (n === pedido) aviso('No se pudo actualizar la hoja: ' + e.message, true);
            return;
        }
        if (n !== pedido) return; // llegó una respuesta más nueva

        // si se estaba escribiendo en un campo, se vuelve a poner el cursor ahí
        const activo = raiz.activeElement?.dataset?.editar;
        raiz.replaceChildren(...nodos);
        raiz.querySelectorAll('[data-editar]').forEach((el) => {
            el.contentEditable = EDITABLE;
            el.spellcheck = false;
        });
        if (activo) {
            const el = raiz.querySelector(`[data-editar="${activo}"]`);
            if (el) { el.focus(); cursorAlFinal(el); }
        }
        ajustarZoom();
    }

    function cursorAlFinal(el) {
        const r = document.createRange();
        r.selectNodeContents(el);
        r.collapse(false);
        const s = window.getSelection();
        s.removeAllRanges();
        s.addRange(r);
    }

    function ajustarZoom() {
        const ancho = datos.diseno.papel === 'a4' ? 793 : 816;
        host.style.zoom = Math.min(1, ($('#lienzo').clientWidth - 48) / ancho).toFixed(3);
    }

    const leer = (el) => {
        let v = el.dataset.multi ? el.innerText : el.textContent;
        if (el.dataset.multi && v.endsWith('\n')) v = v.slice(0, -1);
        return v;
    };

    raiz.addEventListener('input', (e) => {
        const el = e.target.closest?.('[data-editar]');
        if (!el) return;
        const ruta = el.dataset.editar;
        let v = leer(el);
        if (!v.trim()) { v = ''; el.replaceChildren(); } // vacío: que se vea la ayuda
        const [obj, k] = ref(ruta);
        obj[k] = v;
        // el mismo dato puede estar en dos lugares (nombre en el encabezado y en proveedor)
        raiz.querySelectorAll(`[data-editar="${ruta}"]`).forEach((otro) => { if (otro !== el) otro.textContent = v; });
        vistaAtrasada = true;
        cambio('editar:' + ruta);
    });

    raiz.addEventListener('keydown', (e) => {
        const el = e.target.closest?.('[data-editar]');
        if (!el) return;
        if ((e.key === 'Enter' && !el.dataset.multi) || e.key === 'Escape') {
            e.preventDefault();
            el.blur();
        }
    });

    raiz.addEventListener('paste', (e) => {
        if (EDITABLE === 'plaintext-only' || !e.target.closest?.('[data-editar]')) return;
        e.preventDefault();
        document.execCommand('insertText', false, e.clipboardData.getData('text/plain'));
    });

    // al salir de la edición (sin pasar a otro campo) se vuelve a dibujar la hoja completa
    raiz.addEventListener('focusout', (e) => {
        const siguiente = e.relatedTarget;
        if (vistaAtrasada && !(siguiente && siguiente.closest?.('[data-editar]'))) pintarHoja();
    });

    raiz.addEventListener('click', (e) => {
        const el = e.target.closest?.('[data-accion]');
        if (!el) return;
        e.stopPropagation();
        const [accion, seccion] = el.dataset.accion.split(':');
        if (accion === 'logo') {
            $('#archivoLogo').click();
            return;
        }
        if (accion === 'quitar-logo') datos.empresa.logo = null;
        if (accion === 'agregar') datos.diseno[seccion].activo = true;
        if (accion === 'quitar') datos.diseno[seccion].activo = false;
        cambio();
        pintarHoja();
    });

    // ---------- logo ----------

    async function ponerLogo(file) {
        if (!file || !file.type.startsWith('image/')) {
            aviso('Elige un archivo de imagen (PNG o JPG).', true);
            return;
        }
        try {
            datos.empresa.logo = await leerImagen(file);
            cambio();
            pintarHoja();
        } catch (e) {
            aviso(e.message, true);
        }
    }

    $('#archivoLogo').addEventListener('change', (e) => {
        ponerLogo(e.target.files[0]);
        e.target.value = '';
    });

    const zonaLogo = () => raiz.querySelector('.subir-logo, .logo-caja');
    const hayArchivos = (e) => [...(e.dataTransfer?.types || [])].includes('Files');
    $('#lienzo').addEventListener('dragover', (e) => {
        if (!hayArchivos(e)) return;
        e.preventDefault();
        zonaLogo()?.classList.add('encima');
    });
    $('#lienzo').addEventListener('dragleave', (e) => {
        if (!$('#lienzo').contains(e.relatedTarget)) zonaLogo()?.classList.remove('encima');
    });
    $('#lienzo').addEventListener('drop', (e) => {
        if (!hayArchivos(e)) return;
        e.preventDefault();
        zonaLogo()?.classList.remove('encima');
        ponerLogo(e.dataTransfer.files[0]);
    });

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

    // ---------- barra: color, papel y opciones ----------

    function el(tag, props = {}, ...hijos) {
        const e = document.createElement(tag);
        Object.assign(e, props);
        hijos.forEach((h) => h != null && e.append(h));
        return e;
    }

    function cerrarMenus(excepto) {
        document.querySelectorAll('.ed-menu').forEach((m) => { if (m !== excepto) m.hidden = true; });
    }

    function alternarMenu(menu) {
        const abrir = menu.hidden;
        cerrarMenus();
        menu.hidden = !abrir;
    }

    $('#btnColor').addEventListener('click', (e) => { e.stopPropagation(); alternarMenu($('#menuColor')); });
    $('#btnOpciones').addEventListener('click', (e) => { e.stopPropagation(); alternarMenu($('#menuOpciones')); });
    document.querySelectorAll('.ed-menu').forEach((m) => m.addEventListener('click', (e) => e.stopPropagation()));
    document.addEventListener('click', () => cerrarMenus());

    function pintarBarra() {
        const d = datos.diseno;
        $('#puntoColor').style.background = `linear-gradient(135deg, ${d.colores.primario} 50%, ${d.colores.acento} 50%)`;
        document.querySelectorAll('#papel button').forEach((b) => b.classList.toggle('activo', b.dataset.papel === d.papel));

        // colores
        const menu = $('#menuColor');
        menu.innerHTML = '';
        menu.append(el('div', { className: 'ed-menu-titulo', textContent: 'Combinaciones' }));
        const vistos = new Set();
        const pares = el('div', { className: 'pares' });
        EJEMPLOS.forEach((ej) => {
            const clave = ej.colores.primario + ej.colores.acento;
            if (vistos.has(clave)) return;
            vistos.add(clave);
            const b = el('button', { type: 'button', className: 'par', title: ej.nombre });
            b.style.background = `linear-gradient(135deg, ${ej.colores.primario} 50%, ${ej.colores.acento} 50%)`;
            b.classList.toggle('activo', d.colores.primario === ej.colores.primario && d.colores.acento === ej.colores.acento);
            b.addEventListener('click', () => { d.colores = { ...ej.colores }; cambio(); pintarBarra(); pintarHoja(); });
            pares.append(b);
        });
        menu.append(pares);
        menu.append(el('div', { className: 'ed-menu-titulo', textContent: 'Personalizado' }));
        [['primario', 'Principal'], ['acento', 'Acento']].forEach(([k, etq]) => {
            const picker = el('input', { type: 'color', value: d.colores[k] });
            let timer;
            picker.addEventListener('input', () => {
                d.colores[k] = picker.value.toUpperCase();
                cambio('color.' + k);
                $('#puntoColor').style.background = `linear-gradient(135deg, ${d.colores.primario} 50%, ${d.colores.acento} 50%)`;
                clearTimeout(timer);
                timer = setTimeout(pintarHoja, 150);
            });
            menu.append(el('label', { className: 'color-fila' }, picker, etq));
        });

        // opciones
        const op = $('#menuOpciones');
        op.innerHTML = '';
        OPCIONES.forEach(([k, etq]) => {
            const cb = el('input', { type: 'checkbox', checked: d[k] === true });
            cb.addEventListener('change', () => { d[k] = cb.checked; cambio(); pintarHoja(); });
            op.append(el('label', { className: 'opcion' }, cb, etq));
        });
    }

    document.querySelectorAll('#papel button').forEach((b) => b.addEventListener('click', () => {
        datos.diseno.papel = b.dataset.papel;
        cambio();
        pintarBarra();
        pintarHoja();
    }));

    // ---------- galería de plantillas ----------

    async function abrirGaleria() {
        const lista = $('#galeriaLista');
        lista.innerHTML = '';
        $('#galeria').hidden = false;
        const d = datos.diseno;
        EJEMPLOS.forEach((ej) => {
            const actual = d.plantilla === ej.plantilla && d.colores.primario === ej.colores.primario && d.colores.acento === ej.colores.acento;
            const mini = el('div', { className: 'mini' });
            const tarjeta = el('button', { type: 'button', className: 'tarjeta-plantilla' + (actual ? ' actual' : '') },
                el('div', { className: 'mini-marco' }, mini),
                el('b', { textContent: ej.nombre + (actual ? ' · actual' : '') }),
                el('span', { className: 'tenue', textContent: ej.descripcion }));
            tarjeta.addEventListener('click', () => {
                d.plantilla = ej.plantilla;
                d.colores = { ...ej.colores };
                cambio();
                cerrarGaleria();
                pintarBarra();
                pintarHoja();
            });
            lista.append(tarjeta);
            const sombra = mini.attachShadow({ mode: 'open' });
            vista({ ...d, plantilla: ej.plantilla, colores: ej.colores }, false)
                .then((nodos) => sombra.replaceChildren(...nodos))
                .catch(() => sombra.replaceChildren(el('p', { textContent: 'Sin vista previa' })));
        });
    }

    function cerrarGaleria() {
        $('#galeria').hidden = true;
    }

    $('#btnPlantillas').addEventListener('click', abrirGaleria);
    $('#cerrarGaleria').addEventListener('click', cerrarGaleria);
    $('#galeria').addEventListener('click', (e) => { if (e.target === e.currentTarget) cerrarGaleria(); });
    $('#restablecer').addEventListener('click', (e) => {
        e.preventDefault();
        if (!confirm('¿Regresar todos los textos, colores y secciones a la plantilla genérica?\nTus datos de empresa y logo se conservan. Puedes deshacerlo con Ctrl+Z.')) return;
        datos.diseno = structuredClone(GENERICO);
        cambio();
        cerrarGaleria();
        pintarBarra();
        pintarHoja();
    });

    // ---------- guardar, PDF y teclado ----------

    async function guardar() {
        const enviado = historial[pos];
        try {
            const r = await postJson(URL_PLANTILLA, enviado);
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

    const editando = () => {
        const a = document.activeElement;
        return /^(INPUT|TEXTAREA|SELECT)$/.test(a?.tagName) || (a === host && raiz.activeElement);
    };
    document.addEventListener('keydown', (e) => {
        const ctrl = e.ctrlKey || e.metaKey;
        const tecla = e.key.toLowerCase();
        if (ctrl && tecla === 's') {
            e.preventDefault();
            raiz.activeElement?.blur();
            if (!$('#guardar').disabled) guardar();
        } else if (e.key === 'Escape') {
            cerrarGaleria();
            cerrarMenus();
        } else if (editando()) {
            // dentro de un texto, Ctrl+Z es el del navegador
        } else if (ctrl && tecla === 'z') {
            e.preventDefault();
            irA(e.shiftKey ? pos + 1 : pos - 1);
        } else if (ctrl && tecla === 'y') {
            e.preventDefault();
            irA(pos + 1);
        }
    });

    window.addEventListener('beforeunload', (e) => {
        if (historial[pos] !== guardado) e.preventDefault();
    });
    window.addEventListener('resize', ajustarZoom);

    estado();
    $('#estado').textContent = 'Sin cambios';
    pintarBarra();
    pintarHoja();
})();
