<!-- Champ fichier (PDF) -->
<div class="mb-3" id="docBlock" style="display:none;">
  <label class="form-label">Justificatif (PDF)</label>
  <input type="file" class="form-control" name="justificatif" id="justificatif" accept="application/pdf">
  <div class="form-text">PDF uniquement, 5 Mo max.</div>

  <!-- Aperçu immédiat du PDF sélectionné -->
  <div id="pdfPreviewWrapper" class="mt-3" style="display:none;">
    <div class="border rounded">
      <embed id="pdfPreview" type="application/pdf" width="100%" height="500px"/>
    </div>
  </div>
</div>

<script>
  // Logique "requires_doc" : si le type choisi exige un doc -> afficher le bloc
  const typeSelect = document.getElementById('typeId');
  const docBlock = document.getElementById('docBlock');

  // Option 1 : si tu as data-requires-doc sur les <option>
  function evalRequiresDoc() {
    const opt = typeSelect.selectedOptions[0];
    if (!opt) return;
    const needs = opt.dataset.requiresDoc === 'true';
    docBlock.style.display = needs ? '' : 'none';
  }
  typeSelect?.addEventListener('change', evalRequiresDoc);
  evalRequiresDoc();

  // Prévisualisation locale du PDF avec FileReader (avant upload)
  const fileInput = document.getElementById('justificatif');
  const previewWrap = document.getElementById('pdfPreviewWrapper');
  const preview = document.getElementById('pdfPreview');

  fileInput?.addEventListener('change', () => {
    const f = fileInput.files?.[0];
    if (!f) { previewWrap.style.display = 'none'; return; }
    if (f.type !== 'application/pdf') {
      alert('Veuillez sélectionner un fichier PDF.');
      fileInput.value = '';
      previewWrap.style.display = 'none';
      return;
    }
    if (f.size > 5 * 1024 * 1024) {
      alert('Fichier trop volumineux (max 5 Mo).');
      fileInput.value = '';
      previewWrap.style.display = 'none';
      return;
    }
    const url = URL.createObjectURL(f);
    preview.src = url;
    previewWrap.style.display = '';
  });
</script>
