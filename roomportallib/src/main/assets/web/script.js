document.addEventListener("DOMContentLoaded", function () {
    const tableSelect = document.getElementById("tableSelect");
    const columnCheckboxes = document.getElementById("columnCheckboxes");
    const resultContainer = document.getElementById("result");
    const exportBtn = document.getElementById("exportBtn");
    const addFormContainer = document.getElementById("addFormContainer");
    const addBtn = document.getElementById("addBtn");
    const editFormContainer = document.getElementById("editFormContainer");
    const editForm = document.getElementById("editForm");

    let currentTable = null;
    let availableColumns = [];
    let currentData = [];
    let editingRow = null;

    init();

    function init() {
        loadTables();
        setupEventListeners();
        setupCSVUploadListeners();
    }

  function loadTables() {
      fetch("/tables")
          .then(response => response.json())
          .then(tables => {
              tableSelect.innerHTML = '<option value="">-- Choose --</option>';
              tables.forEach(name => {
                  const option = document.createElement("option");
                  option.value = name;
                  option.textContent = name;
                  tableSelect.appendChild(option);
              });
          });
  }

    function setupEventListeners() {
        tableSelect.addEventListener("change", onTableChange);
        document.getElementById("loadBtn").addEventListener("click", onLoadButtonClick);
        exportBtn.addEventListener("click", onExportButtonClick);
        addBtn.addEventListener("click", onAddButtonClick);
        editForm.addEventListener("submit", onEditFormSubmit);

         const resetViewBtn = document.getElementById("resetViewBtn");
         resetViewBtn.addEventListener("click", () => {
             currentTable = null;
             availableColumns = [];
             currentData = [];
             editingRow = null;

             tableSelect.value = "";
             columnCheckboxes.innerHTML = "";
             addFormContainer.innerHTML = "";
             resultContainer.innerHTML = "";
             exportBtn.style.display = "none";
             editFormContainer.style.display = "none";

             loadTables();
         });
    }

    function setupCSVUploadListeners() {
        const loadFromCsvBtn = document.getElementById("loadFromCsvBtn");
        const csvFileInput = document.getElementById("csvFileInput");

        if (!loadFromCsvBtn || !csvFileInput) return;

        loadFromCsvBtn.addEventListener("click", () => {
            csvFileInput.click();
        });

        csvFileInput.addEventListener("change", async function () {
            const file = this.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = async function (e) {
                const text = e.target.result;
                const parsedData = parseCSV(text);

                if (parsedData.length === 0) {
                    alert("CSV is empty or invalid");
                    return;
                }

                currentTable = "csvTempTable_" + file.name.replace(/\.[^/.]+$/, "");
                currentData = parsedData;

                availableColumns = Object.keys(parsedData[0]);
                renderCheckboxes(availableColumns);
                renderAddForm(availableColumns);

                resultContainer.innerHTML = createTableHTML(currentData);
                exportBtn.style.display = "inline-block";

                alert("CSV loaded and ready for local operations");
            };

            reader.readAsText(file);
        });
    }

    function onTableChange() {
        currentTable = tableSelect.value;
        if (!currentTable) return;

        fetch(`/schema/${currentTable}`)
            .then(response => response.json())
            .then(columns => {
                availableColumns = columns.map(col => col.name);
                renderCheckboxes(availableColumns);
                renderAddForm(availableColumns);
                onLoadButtonClick();
            });
    }

    function onLoadButtonClick() {
        if (!currentTable) return;

        if (currentTable.startsWith("csvTempTable_")) {
            resultContainer.innerHTML = createTableHTML(currentData);
            exportBtn.style.display = currentData.length ? "inline-block" : "none";
            renderCheckboxes(availableColumns);
            renderAddForm(availableColumns);
            return;
        }

        const selectedCols = getSelectedColumns();
        const query = selectedCols.length ? `?cols=${selectedCols.join(",")}` : "";

        fetch(`/table/${currentTable}${query}`)
            .then(response => response.json())
            .then(data => {
                currentData = data;
                resultContainer.innerHTML = createTableHTML(data);
                exportBtn.style.display = data.length ? "inline-block" : "none";
            });
    }

    function onExportButtonClick() {
        if (!currentData.length) return;
        const headers = Object.keys(currentData[0]);
        let csv = headers.join(",") + "\n";
        currentData.forEach(row => {
            csv += headers.map(h => JSON.stringify(row[h] ?? "")).join(",") + "\n";
        });

        const blob = new Blob([csv], { type: "text/csv" });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `${currentTable}.csv`;
        a.click();
        URL.revokeObjectURL(url);
    }

    function onAddButtonClick() {
        if (!currentTable) return;

        const inputs = addFormContainer.querySelectorAll("input");
        const row = {};
        inputs.forEach(input => {
            row[input.name] = input.value;
        });

        if (currentTable.startsWith("csvTempTable_")) {
            currentData.push(row);
            resultContainer.innerHTML = createTableHTML(currentData);
            renderCheckboxes(availableColumns);
            renderAddForm(availableColumns);
            alert("Row added locally (CSV data)");
            return;
        }

        fetch(`/table/${currentTable}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(row)
        })
        .then(res => res.text())
        .then(txt => {
            alert(txt);
            document.getElementById("loadBtn").click();
        })
        .catch(() => alert("Failed to add row"));
    }

    function onEditFormSubmit(e) {
        e.preventDefault();
        if (!editingRow) return;

        const formData = new FormData(editForm);
        const updatedRow = {};
        for (const [key, value] of formData.entries()) {
            updatedRow[key] = value;
        }

        if (currentTable.startsWith("csvTempTable_")) {
            const index = currentData.findIndex(row =>
                JSON.stringify(row) === JSON.stringify(editingRow)
            );
            if (index !== -1) {
                currentData[index] = updatedRow;
                resultContainer.innerHTML = createTableHTML(currentData);
                renderCheckboxes(availableColumns);
                renderAddForm(availableColumns);
                alert("Row edited locally (CSV data)");
            }
            closeEditForm();
            return;
        }

        fetch(`/table/${currentTable}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ ...updatedRow, _method: "PUT" })
        })
        .then(res => res.text())
        .then(msg => {
            alert(msg);
            closeEditForm();
            document.getElementById("loadBtn").click();
        })
        .catch(() => alert("Failed to update row"));
    }

    function deleteRow(row) {
        if (!confirm("Are you sure you want to delete this row?")) return;

        if (currentTable.startsWith("csvTempTable_")) {
            currentData = currentData.filter(r => JSON.stringify(r) !== JSON.stringify(row));
            resultContainer.innerHTML = createTableHTML(currentData);
            renderCheckboxes(availableColumns);
            renderAddForm(availableColumns);
            alert("Row deleted locally (CSV data)");
            return;
        }

        fetch(`/table/${currentTable}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ ...row, _method: "DELETE" })
        })
        .then(res => res.text())
        .then(msg => {
            alert(msg);
            document.getElementById("loadBtn").click();
        });
    }

    function renderCheckboxes(cols) {
        columnCheckboxes.innerHTML = "<strong>Select columns:</strong><br/>";
        cols.forEach(col => {
            const label = document.createElement("label");
            label.innerHTML = `<input type='checkbox' value='${col}' checked> ${col}`;
            columnCheckboxes.appendChild(label);
            columnCheckboxes.appendChild(document.createElement("br"));
        });
    }

    function renderAddForm(cols) {
        addFormContainer.innerHTML = "<strong>Add new row:</strong><br/>";
        cols.forEach(col => {
            const input = document.createElement("input");
            input.placeholder = col;
            input.name = col;
            input.style.margin = "4px";
            addFormContainer.appendChild(input);
        });
    }

    function getSelectedColumns() {
        return [...columnCheckboxes.querySelectorAll("input[type=checkbox]")]
            .filter(cb => cb.checked)
            .map(cb => cb.value);
    }

    function editRow(row) {
        editingRow = row;
        editFormContainer.style.display = "block";
        editForm.innerHTML = "";

        Object.keys(row).forEach(key => {
            const input = document.createElement("input");
            input.name = key;
            input.placeholder = key;
            input.value = row[key] ?? "";
            editForm.appendChild(input);
            editForm.appendChild(document.createElement("br"));
        });

        const saveBtn = document.createElement("button");
        saveBtn.type = "submit";
        saveBtn.textContent = "Save Changes";

        const cancelBtn = document.createElement("button");
        cancelBtn.type = "button";
        cancelBtn.textContent = "Cancel";
        cancelBtn.onclick = closeEditForm;

        editForm.appendChild(saveBtn);
        editForm.appendChild(cancelBtn);
    }

    function closeEditForm() {
        editFormContainer.style.display = "none";
        editingRow = null;
    }

    window.deleteRow = deleteRow;
    window.editRow = editRow;
});

function createTableHTML(data) {
    if (!data || data.length === 0) return "<p>No data available</p>";

    const headers = Object.keys(data[0]);
    let html = "<table border='1'><thead><tr>";
    headers.forEach(h => html += `<th>${h}</th>`);
    html += "<th>Actions</th></tr></thead><tbody>";

    data.forEach(row => {
        html += "<tr>";
        headers.forEach(h => html += `<td>${row[h]}</td>`);

        const rowStr = encodeURIComponent(JSON.stringify(row));
        html += `<td>
                    <button onclick="deleteRow(JSON.parse(decodeURIComponent('${rowStr}')))">🗑</button>
                    <button onclick="editRow(JSON.parse(decodeURIComponent('${rowStr}')))">✏️</button>
                 </td>`;
        html += "</tr>";
    });

    html += "</tbody></table>";
    return html;
}

function parseCSV(text) {
    const lines = text.trim().split("\n");
    if (lines.length < 2) return [];

    const headers = lines[0].split(",");
    const data = lines.slice(1).map(line => {
        const values = line.split(",");
        const obj = {};
        headers.forEach((header, i) => {
            obj[header.trim()] = values[i]?.trim() || "";
        });
        return obj;
    });
    return data;
}