/* Formulario de cotización: autocompletado, cálculo de precios y guardado.
   El servidor vuelve a calcular todo al guardar; aquí es solo para verlo en vivo. */
(() => {
    'use strict';

    const $ = (sel, ctx = document) => ctx.querySelector(sel);
    const tbody = $('#partidas tbody');
    let cambios = false;

    // ---------- números ----------

    /** Lee un número de un input ("1,234.50" -> 1234.5); null si está vacío. */
    const num = (input) => {
        const v = String(input.value).replace(/[,$\s]/g, '');
        if (v === '') return null;
        const n = Number(v);
        return Number.isFinite(n) ? n : null;
    };
    const r2 = (n) => Math.round((n + Number.EPSILON) * 100) / 100;
    const fijo = (n) => (n == null ? '' : r2(n).toFixed(2));
    const pctTxt = (n) => (n == null ? '' : String(r2(n)));
    const moneda = (n) => '$' + r2(n || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    const tasaIva = () => num($('#tasaIva')) ?? 0;
    const aplicaIva = () => $('#aplicaIva').checked;

    // ---------- autocompletado ----------

    function autocompletar(input, { url, texto, detalle, elegir, alEscribir }) {
        const lista = document.createElement('div');
        lista.className = 'ac-lista';
        lista.hidden = true;
        input.parentElement.appendChild(lista);
        let items = [], sel = -1, timer, pedido = 0;

        const cerrar = () => { lista.hidden = true; sel = -1; };
        const pintar = () => {
            lista.innerHTML = '';
            items.forEach((it, i) => {
                const d = document.createElement('div');
                d.className = 'ac-item' + (i === sel ? ' sel' : '');
                d.innerHTML = '<span></span><span class="det"></span>';
                d.firstChild.textContent = texto(it);
                d.lastChild.textContent = detalle ? detalle(it) : '';
                d.addEventListener('mousedown', (e) => { e.preventDefault(); tomar(i); });
                lista.appendChild(d);
            });
            const q = input.value.trim();
            const exacto = items.some((it) => texto(it).toLowerCase() === q.toLowerCase());
            if (q && !exacto) {
                const n = document.createElement('div');
                n.className = 'ac-nuevo';
                n.textContent = '+ "' + q + '" es nuevo: se agrega al guardar';
                lista.appendChild(n);
            }
            lista.hidden = lista.childElementCount === 0;
        };
        const tomar = (i) => {
            input.value = texto(items[i]);
            cerrar();
            elegir(items[i]);
            marcarCambio();
        };
        const buscar = async () => {
            const q = input.value.trim();
            if (!q) { items = []; cerrar(); return; }
            const mio = ++pedido;
            let res = [];
            try {
                const r = await fetch(url + '?q=' + encodeURIComponent(q));
                if (r.ok) res = await r.json();
            } catch (e) {
                // sin sugerencias; se puede seguir capturando a mano
            }
            if (mio !== pedido) return; // llegó una respuesta vieja
            items = Array.isArray(res) ? res : [];
            sel = -1;
            pintar();
        };

        input.addEventListener('input', () => {
            if (alEscribir) alEscribir();
            clearTimeout(timer);
            timer = setTimeout(buscar, 150);
        });
        input.addEventListener('keydown', (e) => {
            if (lista.hidden) return;
            if (e.key === 'ArrowDown') { sel = Math.min(sel + 1, items.length - 1); pintar(); e.preventDefault(); }
            else if (e.key === 'ArrowUp') { sel = Math.max(sel - 1, 0); pintar(); e.preventDefault(); }
            else if (e.key === 'Enter' && sel >= 0) { tomar(sel); e.preventDefault(); e.stopPropagation(); }
            else if (e.key === 'Escape') { cerrar(); }
        });
        input.addEventListener('blur', () => setTimeout(cerrar, 100));
    }

    // ---------- cliente ----------

    const camposCliente = ['contacto', 'telefono', 'email', 'rfc', 'direccion'];

    function llenarCliente(cl) {
        $('#cliente').value = cl.nombre || '';
        camposCliente.forEach((k) => { $('#cl-' + k).value = cl[k] || ''; });
        if (camposCliente.some((k) => cl[k])) $('#cliente-mas').open = true;
    }

    autocompletar($('#cliente'), {
        url: base() + 'api/clientes',
        texto: (c) => c.nombre,
        detalle: (c) => c.contacto || c.telefono || '',
        elegir: (c) => { llenarCliente(c); $('#cliente-nuevo').hidden = true; },
        alEscribir: () => { $('#cliente-nuevo').hidden = !$('#cliente').value.trim(); },
    });

    // ---------- partidas ----------

    function agregarPartida(p = {}) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="col-n"></td>
            <td><div class="ac"><input data-k="descripcion" autocomplete="off" placeholder="Busca o escribe un producto nuevo…"></div></td>
            <td class="col-cant"><input data-k="cantidad" class="num" inputmode="decimal"></td>
            <td class="col-costo interno"><input data-k="costo" class="num" inputmode="decimal" placeholder="0.00"></td>
            <td class="col-pct interno"><input data-k="pct" class="num" inputmode="decimal"></td>
            <td class="col-precio"><input data-k="precio" class="num" inputmode="decimal" placeholder="0.00"></td>
            <td class="col-iva"><input data-k="precioIva" class="num" inputmode="decimal" placeholder="0.00"></td>
            <td class="col-imp calc" data-k="importe"></td>
            <td class="col-gan calc interno" data-k="ganancia"></td>
            <td class="col-x"><button type="button" class="quitar" title="Quitar">✕</button></td>`;
        const f = (k) => tr.querySelector(`[data-k="${k}"]`);
        tbody.appendChild(tr);

        f('descripcion').value = p.descripcion || '';
        f('cantidad').value = p.cantidad ?? 1;
        f('costo').value = p.costo != null ? fijo(p.costo) : '';
        f('precio').value = p.precioUnitario != null ? fijo(p.precioUnitario) : '';
        f('pct').value = p.pctGanancia != null ? pctTxt(p.pctGanancia) : (p.precioUnitario != null ? '' : GANANCIA_DEFAULT);
        actualizarPrecioIva(tr);

        autocompletar(f('descripcion'), {
            url: base() + 'api/productos',
            texto: (x) => x.descripcion,
            detalle: (x) => (x.ultimoPrecio != null ? moneda(x.ultimoPrecio) : '') +
                (x.ultimoCosto != null ? '  ·  costo ' + moneda(x.ultimoCosto) : ''),
            elegir: (x) => {
                f('costo').value = x.ultimoCosto != null ? fijo(x.ultimoCosto) : '';
                if (x.ultimoPrecio != null) {
                    f('precio').value = fijo(x.ultimoPrecio);
                    desdePrecio(tr);
                } else {
                    f('pct').value = GANANCIA_DEFAULT;
                    desdeCosto(tr);
                }
                f('cantidad').focus();
                f('cantidad').select();
            },
        });

        // cada campo que se edita recalcula los demás
        f('costo').addEventListener('input', () => desdeCosto(tr, true));
        f('pct').addEventListener('input', () => desdeCosto(tr));
        f('precio').addEventListener('input', () => desdePrecio(tr));
        f('precioIva').addEventListener('input', () => desdePrecioIva(tr));
        f('cantidad').addEventListener('input', () => recalcular());
        tr.querySelectorAll('input').forEach((i) => {
            i.addEventListener('input', marcarCambio);
            // Enter en la última partida agrega otra
            i.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && !e.defaultPrevented && tr === tbody.lastElementChild) {
                    e.preventDefault();
                    agregarPartida().querySelector('[data-k="descripcion"]').focus();
                }
            });
        });
        tr.querySelector('.quitar').addEventListener('click', () => {
            tr.remove();
            if (!tbody.children.length) agregarPartida();
            marcarCambio();
            recalcular();
        });
        recalcular();
        return tr;
    }

    const campo = (tr, k) => tr.querySelector(`[data-k="${k}"]`);

    /** Costo o % cambió: precio = costo + % */
    function desdeCosto(tr, cambioCosto = false) {
        const costo = num(campo(tr, 'costo'));
        const pct = num(campo(tr, 'pct'));
        if (costo != null && pct != null) {
            campo(tr, 'precio').value = fijo(costo * (1 + pct / 100));
        } else if (cambioCosto) {
            actualizarPct(tr); // precio capturado antes que el costo
        }
        actualizarPrecioIva(tr);
        recalcular();
    }

    /** Precio cambió a mano: se recalcula el % real de ganancia */
    function desdePrecio(tr) {
        actualizarPct(tr);
        actualizarPrecioIva(tr);
        recalcular();
    }

    /** Precio final con IVA: precio = monto / (1 + IVA) */
    function desdePrecioIva(tr) {
        const conIva = num(campo(tr, 'precioIva'));
        campo(tr, 'precio').value = conIva == null ? '' : fijo(conIva / (1 + tasaIva() / 100));
        actualizarPct(tr);
        recalcular();
    }

    function actualizarPct(tr) {
        const costo = num(campo(tr, 'costo'));
        const precio = num(campo(tr, 'precio'));
        campo(tr, 'pct').value = costo && precio != null ? pctTxt((precio / costo - 1) * 100) : '';
    }

    function actualizarPrecioIva(tr) {
        const precio = num(campo(tr, 'precio'));
        campo(tr, 'precioIva').value = precio == null ? '' : fijo(precio * (1 + tasaIva() / 100));
    }

    // ---------- totales ----------

    function recalcular() {
        let subtotal = 0, costoTotal = 0, ganancia = 0, ventaConCosto = 0, hayCosto = false;
        [...tbody.children].forEach((tr, i) => {
            tr.querySelector('.col-n').textContent = i + 1;
            const cant = num(campo(tr, 'cantidad')) ?? 0;
            const precio = num(campo(tr, 'precio'));
            const costo = num(campo(tr, 'costo'));
            const importe = precio == null ? 0 : r2(cant * r2(precio));
            subtotal += importe;
            campo(tr, 'importe').textContent = precio == null ? '' : moneda(importe);
            const g = campo(tr, 'ganancia');
            if (costo != null && precio != null) {
                const gan = r2((r2(precio) - costo) * cant);
                costoTotal += costo * cant;
                ganancia += gan;
                ventaConCosto += importe;
                hayCosto = true;
                g.textContent = moneda(gan);
                g.classList.toggle('gan-neg', gan < 0);
            } else {
                g.textContent = '';
            }
        });
        subtotal = r2(subtotal);
        const iva = aplicaIva() ? r2(subtotal * tasaIva() / 100) : 0;
        const envio = r2(num($('#envio')) ?? 0);
        $('#t-subtotal').textContent = moneda(subtotal);
        $('#t-iva').textContent = moneda(iva);
        $('#t-total').textContent = moneda(subtotal + iva + envio);
        $('#t-costo').textContent = hayCosto ? moneda(costoTotal) : '–';
        $('#t-ganancia').textContent = hayCosto ? moneda(ganancia) : '–';
        $('#t-margen').textContent = hayCosto && ventaConCosto ? r2(ganancia / ventaConCosto * 100) + ' %' : '–';
    }

    function alCambiarIva() {
        $('#partidas').classList.toggle('sin-iva', !aplicaIva());
        $('#tasaIva').disabled = !aplicaIva();
        [...tbody.children].forEach(actualizarPrecioIva);
        recalcular();
        marcarCambio();
    }

    // ---------- guardar ----------

    function datos() {
        const cliente = { nombre: $('#cliente').value.trim() };
        camposCliente.forEach((k) => { cliente[k] = $('#cl-' + k).value.trim(); });
        return {
            fecha: $('#fecha').value,
            vigenciaDias: num($('#vigencia')) ?? 0,
            cliente,
            aplicaIva: aplicaIva(),
            tasaIva: tasaIva(),
            envio: num($('#envio')) ?? 0,
            formaPago: $('#formaPago').value,
            tiempoEntrega: $('#tiempoEntrega').value,
            garantia: $('#garantia').value,
            observaciones: $('#observaciones').value,
            partidas: [...tbody.children].map((tr) => ({
                descripcion: campo(tr, 'descripcion').value.trim(),
                cantidad: num(campo(tr, 'cantidad')),
                costo: num(campo(tr, 'costo')),
                precioUnitario: num(campo(tr, 'precio')),
            })),
        };
    }

    async function guardar(verPdf) {
        const err = $('#error');
        err.hidden = true;
        // la ventana del PDF se abre en el clic para que el navegador no la bloquee
        const ventana = verPdf ? window.open('', '_blank') : null;
        const botones = [$('#guardar'), $('#guardar-pdf')];
        botones.forEach((b) => { b.disabled = true; });
        try {
            const r = await fetch(base() + 'api/cotizaciones' + (COT.id ? '/' + COT.id : ''), {
                method: COT.id ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json', ...csrf() },
                body: JSON.stringify(datos()),
            });
            const res = await r.json().catch(() => ({}));
            if (!r.ok) throw new Error(res.error || res.detail || 'No se pudo guardar (' + r.status + ')');
            cambios = false;
            if (ventana) ventana.location = base() + 'cotizaciones/' + res.id + '/pdf';
            location = base();
        } catch (e) {
            if (ventana) ventana.close();
            err.textContent = e.message;
            err.hidden = false;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            botones.forEach((b) => { b.disabled = false; });
        }
    }

    function marcarCambio() { cambios = true; }

    /** Encabezado con el token CSRF (lo exige el login para POST/PUT). */
    function csrf() {
        const token = $('meta[name=_csrf]');
        return token ? { [$('meta[name=_csrf_header]').content]: token.content } : {};
    }

    function base() {
        return document.querySelector('.barra .logo').getAttribute('href').replace(/\/?$/, '/');
    }

    // ---------- inicio ----------

    llenarCliente(COT.cliente || {});
    $('#fecha').value = COT.fecha;
    $('#vigencia').value = COT.vigenciaDias;
    $('#aplicaIva').checked = COT.aplicaIva;
    $('#tasaIva').value = COT.tasaIva;
    $('#envio').value = COT.envio ? fijo(COT.envio) : '';
    ['formaPago', 'tiempoEntrega', 'garantia', 'observaciones'].forEach((k) => { $('#' + k).value = COT[k] || ''; });
    (COT.partidas && COT.partidas.length ? COT.partidas : [{}]).forEach((p) => agregarPartida(p));

    $('#agregar').addEventListener('click', () => agregarPartida().querySelector('[data-k="descripcion"]').focus());
    $('#aplicaIva').addEventListener('change', alCambiarIva);
    $('#tasaIva').addEventListener('input', () => { [...tbody.children].forEach(actualizarPrecioIva); recalcular(); });
    $('#envio').addEventListener('input', recalcular);
    $('#guardar').addEventListener('click', () => guardar(false));
    $('#guardar-pdf').addEventListener('click', () => guardar(true));
    document.querySelectorAll('main input, main textarea').forEach((i) => i.addEventListener('input', marcarCambio));
    window.addEventListener('beforeunload', (e) => { if (cambios) e.preventDefault(); });
    document.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key === 's') { e.preventDefault(); guardar(false); }
    });

    alCambiarIva();
    cambios = false;
    if (!COT.id) $('#cliente').focus();
})();
