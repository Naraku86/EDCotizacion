/* Comportamientos comunes sin JavaScript en línea (lo prohíbe la política de seguridad CSP):
   - <form data-confirmar="¿Seguro?">  pide confirmación antes de enviar
   - <select data-autoenviar>          envía su formulario al cambiar */
document.addEventListener('submit', (e) => {
    const msg = e.target.dataset?.confirmar;
    if (msg && !confirm(msg)) e.preventDefault();
});
document.addEventListener('change', (e) => {
    if (e.target.matches?.('select[data-autoenviar]')) e.target.form.submit();
});
