import { useEffect, useMemo, useState } from "react";
import {
  ApiError,
  createVehicle,
  deleteVehicle,
  getVehiclesPage,
  updateVehicle,
  type VehicleRequestPayload
} from "../api";
import { canCreateInstructorsOrVehicles, canDeleteAny } from "../authz";
import type { Vehicle } from "../types";

type FormState = {
  licensePlate: string;
  make: string;
  model: string;
  year: string;
  insuranceExpiry: string;
};

const emptyForm: FormState = {
  licensePlate: "",
  make: "",
  model: "",
  year: "",
  insuranceExpiry: ""
};

function mapApiError(error: unknown): string {
  if (!(error instanceof ApiError)) {
    return error instanceof Error ? error.message : "Unexpected error";
  }
  if (error.status === 409) {
    return "Conflict: vehicle already exists or cannot be deleted due to active assignments.";
  }
  if (error.status === 400) {
    return `Validation failed: ${error.message}`;
  }
  return error.message;
}

function validateForm(form: FormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const plateRegex = /^[A-Z]{1,3}-[0-9]{2,3}-[A-Z]{2,3}$/;
  const year = Number(form.year);
  const insuranceDate = form.insuranceExpiry ? new Date(form.insuranceExpiry) : null;

  if (!plateRegex.test(form.licensePlate.trim().toUpperCase())) {
    errors.licensePlate = "License plate must be Romanian format, e.g. B-123-ABC.";
  }
  if (!form.make.trim()) errors.make = "Make is required.";
  if (!form.model.trim()) errors.model = "Model is required.";
  if (!Number.isInteger(year) || year < 1900 || year > 2100) {
    errors.year = "Year must be between 1900 and 2100.";
  }
  if (!insuranceDate || Number.isNaN(insuranceDate.getTime())) {
    errors.insuranceExpiry = "Insurance expiry is required.";
  } else {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    if (insuranceDate <= today) {
      errors.insuranceExpiry = "Insurance expiry must be in the future.";
    }
  }
  return errors;
}

function toPayload(form: FormState): VehicleRequestPayload {
  return {
    licensePlate: form.licensePlate.trim().toUpperCase(),
    make: form.make.trim(),
    model: form.model.trim(),
    year: Number(form.year),
    insuranceExpiry: form.insuranceExpiry
  };
}

export function VehiclesPage(): JSX.Element {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [formMode, setFormMode] = useState<"create" | "edit">("create");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});
  const [formMessage, setFormMessage] = useState("");
  const [formMessageType, setFormMessageType] = useState<"success" | "error">("success");
  const writeAllowed = canCreateInstructorsOrVehicles();
  const deleteAllowed = canDeleteAny();
  const showActions = writeAllowed || deleteAllowed;

  const query = useMemo(() => {
    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", "10");
    params.set("sortBy", "createdAt");
    params.set("sortDir", "desc");
    return params;
  }, [page]);

  function loadVehicles(): (() => void) {
    let active = true;
    setLoading(true);
    setError("");
    getVehiclesPage(query)
      .then((res) => {
        if (!active) return;
        setVehicles(res.items);
        setTotalPages(Math.max(1, res.totalPages));
      })
      .catch((err) => {
        if (!active) return;
        setError(mapApiError(err));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }

  useEffect(() => {
    return loadVehicles();
  }, [query]);

  function resetForm(): void {
    setForm(emptyForm);
    setFormErrors({});
    setFormMode("create");
    setEditingId(null);
  }

  function startEdit(vehicle: Vehicle): void {
    setFormMode("edit");
    setEditingId(vehicle.id);
    setFormErrors({});
    setForm({
      licensePlate: vehicle.licensePlate,
      make: vehicle.make,
      model: vehicle.model,
      year: String(vehicle.year),
      insuranceExpiry: vehicle.insuranceExpiry
    });
  }

  async function handleDelete(id: number): Promise<void> {
    if (!window.confirm("Delete this vehicle?")) {
      return;
    }
    try {
      await deleteVehicle(id);
      setFormMessage("Vehicle deleted successfully.");
      setFormMessageType("success");
      loadVehicles();
    } catch (err) {
      setFormMessage(mapApiError(err));
      setFormMessageType("error");
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    const errors = validateForm(form);
    setFormErrors(errors);
    setFormMessage("");
    setFormMessageType("success");
    if (Object.keys(errors).length > 0) {
      return;
    }

    setSubmitting(true);
    try {
      const payload = toPayload(form);
      if (formMode === "create") {
        await createVehicle(payload);
        setFormMessage("Vehicle created successfully.");
        setFormMessageType("success");
      } else if (editingId !== null) {
        await updateVehicle(editingId, payload);
        setFormMessage("Vehicle updated successfully.");
        setFormMessageType("success");
      }
      resetForm();
      loadVehicles();
    } catch (err) {
      setFormMessage(mapApiError(err));
      setFormMessageType("error");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="page">
      <h1>Vehicles</h1>

      {writeAllowed && (
      <form className="entity-form" onSubmit={handleSubmit}>
        <h2>{formMode === "create" ? "Create vehicle" : "Edit vehicle"}</h2>
        <div className="form-grid">
          <label>
            License plate
            <input
              value={form.licensePlate}
              onChange={(e) => setForm((c) => ({ ...c, licensePlate: e.target.value }))}
              placeholder="B-123-ABC"
            />
            {formErrors.licensePlate && <span className="error">{formErrors.licensePlate}</span>}
          </label>
          <label>
            Make
            <input value={form.make} onChange={(e) => setForm((c) => ({ ...c, make: e.target.value }))} />
            {formErrors.make && <span className="error">{formErrors.make}</span>}
          </label>
          <label>
            Model
            <input value={form.model} onChange={(e) => setForm((c) => ({ ...c, model: e.target.value }))} />
            {formErrors.model && <span className="error">{formErrors.model}</span>}
          </label>
          <label>
            Year
            <input value={form.year} onChange={(e) => setForm((c) => ({ ...c, year: e.target.value }))} />
            {formErrors.year && <span className="error">{formErrors.year}</span>}
          </label>
          <label>
            Insurance expiry
            <input
              type="date"
              value={form.insuranceExpiry}
              onChange={(e) => setForm((c) => ({ ...c, insuranceExpiry: e.target.value }))}
            />
            {formErrors.insuranceExpiry && <span className="error">{formErrors.insuranceExpiry}</span>}
          </label>
        </div>
        {formMessage && <p className={formMessageType === "success" ? "message-success" : "error"}>{formMessage}</p>}
        <div className="form-actions">
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? "Saving..." : formMode === "create" ? "Create" : "Update"}
          </button>
          {formMode === "edit" && (
            <button type="button" className="btn btn-secondary" onClick={resetForm}>
              Cancel edit
            </button>
          )}
        </div>
      </form>
      )}

      <div className="header-line">
        <h2>Vehicle list</h2>
        <div className="pager">
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page <= 0 || loading}
          >
            Prev
          </button>
          <span>
            Page {page + 1} / {totalPages}
          </span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1 || loading}
          >
            Next
          </button>
        </div>
      </div>

      {loading && <p>Loading vehicles...</p>}
      {error && <p className="error">{error}</p>}

      {!loading && !error && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Plate</th>
              <th>Vehicle</th>
              <th>Year</th>
              <th>Status</th>
              {showActions && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {vehicles.map((vehicle) => (
              <tr key={vehicle.id}>
                <td>{vehicle.id}</td>
                <td>{vehicle.licensePlate}</td>
                <td>
                  {vehicle.make} {vehicle.model}
                </td>
                <td>{vehicle.year}</td>
                <td>{vehicle.status}</td>
                {showActions && (
                  <td className="actions-cell">
                    {writeAllowed && (
                      <button type="button" className="btn btn-secondary btn-sm" onClick={() => startEdit(vehicle)}>
                        Edit
                      </button>
                    )}
                    {deleteAllowed && (
                      <button type="button" className="btn btn-danger btn-sm" onClick={() => void handleDelete(vehicle.id)}>
                        Delete
                      </button>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
