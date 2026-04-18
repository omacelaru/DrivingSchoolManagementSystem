import { useState } from "react";
import { ApiError, createMaintenance, deleteMaintenance, getMaintenanceById, updateMaintenance } from "../api";
import { canDeleteAny } from "../authz";
import type { Maintenance } from "../types";

type FormState = {
  vehicleId: string;
  maintenanceDate: string;
  description: string;
  cost: string;
  type: Maintenance["type"];
};

const emptyForm: FormState = {
  vehicleId: "",
  maintenanceDate: "",
  description: "",
  cost: "",
  type: "ROUTINE"
};

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) return "Conflict: maintenance operation violates business constraints.";
  if (error.status === 400) return `Validation failed: ${error.message}`;
  return error.message;
}

function validateForm(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const vehicleId = Number(form.vehicleId);
  const cost = Number(form.cost);
  if (!Number.isInteger(vehicleId) || vehicleId <= 0) errors.vehicleId = "Vehicle ID must be positive.";
  if (!form.maintenanceDate) errors.maintenanceDate = "Maintenance date is required.";
  if (!Number.isFinite(cost) || cost < 0) errors.cost = "Cost must be zero or positive.";
  return errors;
}

export function MaintenancesPage(): JSX.Element {
  const [lookupId, setLookupId] = useState("");
  const [current, setCurrent] = useState<Maintenance | null>(null);
  const [formMode, setFormMode] = useState<"create" | "edit">("create");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const deleteAllowed = canDeleteAny();

  function resetForm(): void {
    setForm(emptyForm);
    setErrors({});
    setFormMode("create");
    setEditingId(null);
  }

  async function handleLookup(): Promise<void> {
    setMessage("");
    setError("");
    const id = Number(lookupId);
    if (!Number.isInteger(id) || id <= 0) {
      setError("Provide a valid maintenance ID.");
      return;
    }
    try {
      const item = await getMaintenanceById(id);
      setCurrent(item);
      setFormMode("edit");
      setEditingId(item.id);
      setForm({
        vehicleId: String(item.vehicleId),
        maintenanceDate: item.maintenanceDate,
        description: item.description ?? "",
        cost: String(item.cost),
        type: item.type
      });
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleDelete(): Promise<void> {
    if (!current) return;
    if (!window.confirm("Delete this maintenance record?")) return;
    try {
      await deleteMaintenance(current.id);
      setCurrent(null);
      setLookupId("");
      resetForm();
      setMessage("Maintenance deleted successfully.");
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setMessage("");
    setError("");
    const formErrors = validateForm(form);
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

    const payload = {
      vehicleId: Number(form.vehicleId),
      maintenanceDate: form.maintenanceDate,
      description: form.description.trim() || undefined,
      cost: Number(form.cost),
      type: form.type
    };

    try {
      if (formMode === "create") {
        const item = await createMaintenance(payload);
        setCurrent(item);
        setLookupId(String(item.id));
        setMessage("Maintenance created successfully.");
      } else if (editingId !== null) {
        const item = await updateMaintenance(editingId, {
          maintenanceDate: payload.maintenanceDate,
          description: payload.description,
          cost: payload.cost,
          type: payload.type
        });
        setCurrent(item);
        setMessage("Maintenance updated successfully.");
      }
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  return (
    <section className="page">
      <h1>Maintenances</h1>

      <div className="entity-form">
        <h2>Find maintenance by ID</h2>
        <div className="form-grid">
          <label>
            Maintenance ID
            <input value={lookupId} onChange={(e) => setLookupId(e.target.value)} />
          </label>
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-secondary" onClick={() => void handleLookup()}>
            Load maintenance
          </button>
          {current && deleteAllowed && (
            <button type="button" className="btn btn-danger" onClick={() => void handleDelete()}>
              Delete loaded maintenance
            </button>
          )}
        </div>
      </div>

      <form className="entity-form" onSubmit={handleSubmit}>
        <h2>{formMode === "create" ? "Create maintenance" : "Edit maintenance"}</h2>
        <div className="form-grid">
          <label>
            Vehicle ID
            <input value={form.vehicleId} onChange={(e) => setForm((c) => ({ ...c, vehicleId: e.target.value }))} />
            {errors.vehicleId && <span className="error">{errors.vehicleId}</span>}
          </label>
          <label>
            Date
            <input
              type="date"
              value={form.maintenanceDate}
              onChange={(e) => setForm((c) => ({ ...c, maintenanceDate: e.target.value }))}
            />
            {errors.maintenanceDate && <span className="error">{errors.maintenanceDate}</span>}
          </label>
          <label>
            Cost
            <input value={form.cost} onChange={(e) => setForm((c) => ({ ...c, cost: e.target.value }))} />
            {errors.cost && <span className="error">{errors.cost}</span>}
          </label>
          <label>
            Type
            <select value={form.type} onChange={(e) => setForm((c) => ({ ...c, type: e.target.value as Maintenance["type"] }))}>
              <option value="ROUTINE">ROUTINE</option>
              <option value="REPAIR">REPAIR</option>
              <option value="INSPECTION">INSPECTION</option>
              <option value="OTHER">OTHER</option>
            </select>
          </label>
          <label className="full-width">
            Description
            <input
              value={form.description}
              onChange={(e) => setForm((c) => ({ ...c, description: e.target.value }))}
            />
          </label>
        </div>
        <div className="form-actions">
          <button className="btn btn-primary" type="submit">
            {formMode === "create" ? "Create" : "Update"}
          </button>
          {formMode === "edit" && (
            <button type="button" className="btn btn-secondary" onClick={resetForm}>
              Cancel edit
            </button>
          )}
        </div>
      </form>

      {current && (
        <div className="entity-form">
          <h2>Loaded maintenance</h2>
          <p>ID: {current.id}</p>
          <p>Vehicle ID: {current.vehicleId}</p>
          <p>Date: {current.maintenanceDate}</p>
          <p>Type: {current.type}</p>
          <p>Cost: {current.cost}</p>
        </div>
      )}

      {message && <p>{message}</p>}
      {error && <p className="error">{error}</p>}
    </section>
  );
}
