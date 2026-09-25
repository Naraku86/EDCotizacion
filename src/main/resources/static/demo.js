/* Demo público: empresa, diseño y cotización en una sola página; se descarga el PDF.
   Nada se guarda en el servidor. Lo capturado se queda en localStorage de este navegador
   (si el navegador no lo permite, la página funciona igual, solo no lo recuerda).
   La vista previa es el mismo HTML del PDF, pintado dentro de un Shadow DOM. */
(() => {
    'use strict';

    const $ = (sel, ctx = document) => ctx.querySelector(sel);
    const $$ = (sel, ctx = document) => [...ctx.querySelectorAll(sel)];
    const DATOS = document.getElementById('datos').dataset;
    const URL_DEMO = DATOS.url;
    const EJEMPLOS = JSON.parse(DATOS.ejemplos);
    const MAX_PARTIDAS = Number(DATOS.max || 50);
    const CLAVE = 'edcotizacion.demo.v1';
    const tbody = $('#partidas');
    let logo = null;

    // ---------- números (mismas reglas que el formulario del sistema) ----------

    /** Lee un número de un input ("1,234.50" -> 1234.5); null si está vacío o no es número. */
    const num = (input) => {
        const v = String(input.value).replace(/[,$\s]/g, '');
        if (v === '') return null;
        const n = Number(v);
        return Number.isFinite(n) ? n : null;
    };
    const r2 = (n) => Math.round((n + Number.EPSILON) * 100) / 100;
    const moneda = (n) => '$' + r2(n || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    const hoy = () => {
        const d = new Date();
        return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString().slice(0, 10);
    };

    const INICIAL = () => ({
        empresa: {},
        diseno: { plantilla: EJEMPLOS[0].plantilla, ...EJEMPLOS[0].colores, papel: 'carta' },
        folio: 'COT-0001',
        fecha: hoy(),
        vigenciaDias: 15,
        cliente: {},
        aplicaIva: true,
        tasaIva: 16,
        envio: null,
        formaPago: 'Transferencia o depósito bancario.',
        tiempoEntrega: '3 a 5 días hábiles.',
        garantia: '',
        observaciones: 'Precios en pesos mexicanos (MXN).',
        partidas: [{}],
    });

    // ---------- leer y llenar la página ----------

    function leer() {
        const empresa = { logo };
        $$('[data-empresa]').forEach((i) => { empresa[i.dataset.empresa] = i.value.trim(); });
        const cliente = {};
        $$('[data-cliente]').forEach((i) => { cliente[i.dataset.cliente] = i.value.trim(); });
        return {
            empresa,
            diseno: { plantilla: $('#ejemplos').dataset.plantilla, primario: $('#primario').value,
                      acento: $('#acento').value, papel: $('#papel').value },
            folio: $('#folio').value.trim(),
            fecha: $('#fecha').value,
            vigenciaDias: num($('#vigencia')) ?? 0,
            cliente,
            aplicaIva: $('#aplicaIva').checked,
            tasaIva: num($('#tasaIva')),
            envio: num($('#envio')),
            formaPago: $('#formaPago').value,
            tiempoEntrega: $('#tiempoEntrega').value,
            garantia: $('#garantia').value,
            observaciones: $('#observaciones').value,
            partidas: $$('tr', tbody).map((tr) => ({
                descripcion: campo(tr, 'descripcion').value.trim(),
                cantidad: num(campo(tr, 'cantidad')),
                precioUnitario: num(campo(tr, 'precio')),
            })),
        };
    }

    function llenar(d) {
        logo = d.empresa?.logo || null;
        pintarLogo();
        $$('[data-empresa]').forEach((i) => { i.value = d.empresa?.[i.dataset.empresa] || ''; });
        $$('[data-cliente]').forEach((i) => { i.value = d.cliente?.[i.dataset.cliente] || ''; });
        elegirDiseno(d.diseno.plantilla, d.diseno.primario, d.diseno.acento);
        $('#papel').value = d.diseno.papel === 'a4' ? 'a4' : 'carta';
        $('#folio').value = d.folio || '';
        $('#fecha').value = d.fecha || hoy();
        $('#vigencia').value = d.vigenciaDias ?? 15;
        $('#aplicaIva').checked = d.aplicaIva !== false;
        $('#tasaIva').value = d.tasaIva ?? 16;
        $('#envio').value = d.envio == null ? '' : r2(d.envio).toFixed(2);
        ['formaPago', 'tiempoEntrega', 'garantia', 'observaciones'].forEach((k) => { $('#' + k).value = d[k] || ''; });
        tbody.replaceChildren();
        (d.partidas?.length ? d.partidas : [{}]).slice(0, MAX_PARTIDAS).forEach(agregarPartida);
        recalcular();
    }

    // ---------- partidas ----------

    const campo = (tr, k) => tr.querySelector(`[data-k="${k}"]`);

    function agregarPartida(p = {}) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="col-n"></td>
            <td><input aria-label="Descripción" data-k="descripcion" maxlength="1000" placeholder="Producto o servicio"></td>
            <td class="col-cant"><input aria-label="Cantidad" data-k="cantidad" class="num" inputmode="decimal"></td>
            <td class="col-precio"><input aria-label="Precio unitario" data-k="precio" class="num" inputmode="decimal" placeholder="0.00"></td>
            <td class="col-imp num" data-k="importe"></td>
            <td class="col-x"><button type="button" class="quitar" title="Quitar producto" aria-label="Quitar producto">✕</button></td>`;
        campo(tr, 'descripcion').value = p.descripcion || '';
        campo(tr, 'cantidad').value = p.cantidad ?? 1;
        campo(tr, 'precio').value = p.precioUnitario == null ? '' : r2(p.precioUnitario).toFixed(2);
        campo(tr, 'precio').addEventListener('blur', (e) => {
            const n = num(e.target);
            if (n != null) e.target.value = r2(n).toFixed(2);
        });
        $('.quitar', tr).addEventListener('click', () => {
            tr.remove();
            if (!tbody.children.length) agregarPartida();
            alCambiar();
        });
        tbody.append(tr);
        $('#agregar').disabled = tbody.children.length >= MAX_PARTIDAS;
        return tr;
    }

    function recalcular() {
        let subtotal = 0;
        $$('tr', tbody).forEach((tr, i) => {
            $('.col-n', tr).textContent = i + 1;
            const cant = num(campo(tr, 'cantidad')) ?? 0;
            const precio = num(campo(tr, 'precio'));
            const importe = precio == null ? 0 : r2(cant * r2(precio));
            subtotal += importe;
            campo(tr, 'importe').textContent = precio == null ? '' : moneda(importe);
        });
        subtotal = r2(subtotal);
        const iva = $('#aplicaIva').checked ? r2(subtotal * (num($('#tasaIva')) ?? 0) / 100) : 0;
        const envio = r2(num($('#envio')) ?? 0); // el envío no lleva IVA
        $('#t-subtotal').textContent = moneda(subtotal);
        $('#t-iva').textContent = moneda(iva);
        $('#t-total').textContent = moneda(subtotal + iva + envio);
        $('#tasaIva').disabled = !$('#aplicaIva').checked;
        $('#agregar').disabled = tbody.children.length >= MAX_PARTIDAS;
    }

    // ---------- diseño ----------

    function pintarEjemplos() {
        EJEMPLOS.forEach((ej) => {
            const b = document.createElement('button');
            b.type = 'button';
            b.className = 'ejemplo';
            b.dataset.plantilla = ej.plantilla;
            b.dataset.primario = ej.colores.primario;
            b.dataset.acento = ej.colores.acento;
            b.title = ej.descripcion;
            b.innerHTML = '<span class="puntos"><span class="punto"></span><span class="punto"></span></span><b></b>';
            const [p, a] = $$('.punto', b);
            p.style.background = ej.colores.primario;
            a.style.background = ej.colores.acento;
            $('b', b).textContent = ej.nombre;
            b.addEventListener('click', () => {
                elegirDiseno(ej.plantilla, ej.colores.primario, ej.colores.acento);
                alCambiar();
            });
            $('#ejemplos').append(b);
        });
    }

    function elegirDiseno(plantilla, primario, acento) {
        $('#ejemplos').dataset.plantilla = plantilla;
        $('#primario').value = primario;
        $('#acento').value = acento;
        marcarEjemplo();
    }

    /** Marca el ejemplo que coincide con la plantilla y los colores actuales (si cambias un color, ninguno). */
    function marcarEjemplo() {
        const plantilla = $('#ejemplos').dataset.plantilla;
        const pri = $('#primario').value.toLowerCase();
        const ace = $('#acento').value.toLowerCase();
        $$('.ejemplo').forEach((b) => {
            const igual = b.dataset.plantilla === plantilla && b.dataset.primario.toLowerCase() === pri
                && b.dataset.acento.toLowerCase() === ace;
            b.setAttribute('aria-pressed', String(igual));
        });
    }

    // ---------- logo ----------

    function pintarLogo() {
        $('#logoVista').hidden = !logo;
        if (logo) $('#logoVista').src = logo;
        $('#quitarLogo').hidden = !logo;
        $('#btnLogo').textContent = logo ? 'Cambiar logo' : 'Subir logo';
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
            img.onerror = () => { URL.revokeObjectURL(url); mal(new Error('No se pudo leer la imagen.')); };
            img.src = url;
        });
    }

    // ---------- vista previa ----------

    const host = $('#hoja-host');
    const raiz = host.attachShadow({ mode: 'open' });
    const CSS_HOJA = `
        :host { display: block; }
        .hoja { position: relative; width: 612pt; min-height: 792pt; box-sizing: border-box;
                padding: 50pt 42pt 48pt; background: #fff; box-shadow: 0 3px 18px rgba(0,0,0,.16);
                display: flex; flex-direction: column; }
        .hoja.a4 { width: 595pt; min-height: 842pt; }
        .hoja > .pie { order: 99; margin-top: auto; padding-top: 24pt; }
        .np::after { content: "1 de 1"; }
    `;
    let pedido = 0;

    const csrf = () => {
        const t = $('meta[name=_csrf]');
        return t ? { [$('meta[name=_csrf_header]').content]: t.content } : {};
    };
    const postJson = (ruta, cuerpo) => fetch(URL_DEMO + ruta, {
        method: 'POST', headers: { 'Content-Type': 'application/json', ...csrf() }, body: JSON.stringify(cuerpo),
    });
    const mensajeDe = async (r) => {
        try { return (await r.json()).error || 'Error ' + r.status; } catch { return 'Error ' + r.status; }
    };

    /** Para la vista previa se completan los huecos con textos de ejemplo; el PDF usa solo lo capturado. */
    function paraVista(d) {
        const partidas = d.partidas.filter((p) => p.descripcion && p.cantidad > 0 && p.precioUnitario != null);
        return {
            ...d,
            empresa: { ...d.empresa, nombre: d.empresa.nombre || 'Tu Empresa' },
            folio: d.folio || 'COT-0001',
            fecha: d.fecha || hoy(),
            cliente: { ...d.cliente, nombre: d.cliente.nombre || 'Nombre de tu cliente' },
            tasaIva: d.tasaIva ?? 0,
            partidas: partidas.length ? partidas : [{ descripcion: 'Tu producto o servicio', cantidad: 1, precioUnitario: 0 }],
        };
    }

    async function pintarVista() {
        const n = ++pedido;
        const d = leer();
        estado('Actualizando…');
        let r;
        try {
            r = await postJson('/vista', paraVista(d));
        } catch {
            if (n === pedido) estado('Sin conexión con el servidor', true);
            return;
        }
        if (n !== pedido) return; // ya se pidió una más nueva
        if (!r.ok) {
            estado('Revisa: ' + (await mensajeDe(r)).split('\n')[0], true);
            return;
        }
        const doc = new DOMParser().parseFromString(await r.text(), 'text/html');
        if (n !== pedido) return;
        const estilo = document.createElement('style');
        estilo.textContent = $$('style', doc).map((s) => s.textContent).join('\n') + CSS_HOJA;
        const hoja = document.adoptNode(doc.querySelector('.hoja'));
        hoja.classList.toggle('a4', d.diseno.papel === 'a4');
        raiz.replaceChildren(estilo, hoja);
        ajustarZoom();
        estado(guardadoLocal ? 'Guardado en este navegador' : 'Al día');
    }

    function ajustarZoom() {
        const hoja = $('.hoja', raiz);
        if (!hoja) return;
        const lienzo = $('#lienzo');
        const cs = getComputedStyle(lienzo);
        const ancho = lienzo.clientWidth - parseFloat(cs.paddingLeft) - parseFloat(cs.paddingRight);
        host.style.zoom = Math.max(0.2, Math.min(1, ancho / hoja.offsetWidth)).toFixed(3);
    }

    function estado(texto, error) {
        $('#estado').textContent = texto;
        $('#estado').classList.toggle('error', !!error);
    }

    // ---------- guardar en este navegador ----------

    let guardadoLocal = false;

    function guardarLocal() {
        const d = leer();
        try {
            localStorage.setItem(CLAVE, JSON.stringify(d));
            guardadoLocal = true;
        } catch {
            // sin espacio (el logo es lo más pesado) o almacenamiento bloqueado: se intenta sin logo
            try {
                localStorage.setItem(CLAVE, JSON.stringify({ ...d, empresa: { ...d.empresa, logo: null } }));
                guardadoLocal = true;
            } catch {
                guardadoLocal = false;
            }
        }
    }

    function cargarLocal() {
        try {
            const d = JSON.parse(localStorage.getItem(CLAVE));
            if (d && typeof d === 'object') {
                const base = INICIAL();
                return { ...base, ...d, diseno: { ...base.diseno, ...d.diseno } };
            }
        } catch {
            // dato dañado o almacenamiento bloqueado: se empieza de cero
        }
        return INICIAL();
    }

    // ---------- descargar ----------

    async function descargar() {
        const boton = $('#descargar');
        const error = $('#error');
        error.hidden = true;
        boton.disabled = true;
        boton.textContent = 'Generando…';
        try {
            const r = await postJson('/pdf', leer());
            if (!r.ok) {
                error.textContent = await mensajeDe(r);
                error.hidden = false;
                error.focus();
                return;
            }
            const nombre = /filename="?([^";]+)"?/.exec(r.headers.get('Content-Disposition') || '')?.[1] || 'cotizacion.pdf';
            const url = URL.createObjectURL(await r.blob());
            const a = document.createElement('a');
            a.href = url;
            a.download = nombre;
            document.body.append(a);
            a.click();
            a.remove();
            setTimeout(() => URL.revokeObjectURL(url), 60000);
        } catch {
            error.textContent = 'No se pudo conectar con el servidor.';
            error.hidden = false;
            error.focus();
        } finally {
            boton.disabled = false;
            boton.textContent = 'Descargar PDF';
        }
    }

    // ---------- eventos ----------

    let timerVista, timerLocal;

    function alCambiar() {
        recalcular();
        marcarEjemplo();
        clearTimeout(timerVista);
        clearTimeout(timerLocal);
        timerVista = setTimeout(pintarVista, 600);
        timerLocal = setTimeout(() => { timerLocal = null; guardarLocal(); }, 400);
    }

    pintarEjemplos();
    llenar(cargarLocal());

    $('.demo-form').addEventListener('input', alCambiar);
    $('.demo-form').addEventListener('change', alCambiar);
    $('#agregar').addEventListener('click', () => {
        if (tbody.children.length >= MAX_PARTIDAS) return;
        campo(agregarPartida(), 'descripcion').focus();
        alCambiar();
    });
    $('#btnLogo').addEventListener('click', () => $('#archivoLogo').click());
    $('#archivoLogo').addEventListener('change', async (e) => {
        const file = e.target.files[0];
        e.target.value = '';
        if (!file) return;
        if (!/^image\/(png|jpeg)$/.test(file.type)) {
            estado('El logo debe ser PNG o JPG', true);
            return;
        }
        try {
            logo = await leerImagen(file);
            pintarLogo();
            alCambiar();
        } catch (err) {
            estado(err.message, true);
        }
    });
    $('#quitarLogo').addEventListener('click', () => {
        logo = null;
        pintarLogo();
        alCambiar();
    });
    $('#reiniciar').addEventListener('click', () => {
        if (!confirm('¿Borrar todo lo capturado y empezar de cero?')) return;
        try { localStorage.removeItem(CLAVE); } catch { /* almacenamiento bloqueado */ }
        guardadoLocal = false;
        llenar(INICIAL());
        $('#error').hidden = true;
        pintarVista();
    });
    $('#descargar').addEventListener('click', descargar);
    // si se recarga o cierra antes de que venza el temporizador, no se pierde lo último que se escribió
    window.addEventListener('pagehide', () => {
        if (timerLocal) { clearTimeout(timerLocal); guardarLocal(); }
    });
    new ResizeObserver(ajustarZoom).observe($('#lienzo'));

    pintarVista();
})();
