import { useEffect, useState } from "react";
import { ApiError, createMaintenance, deleteMaintenance, getMaintenances, updateMaintenance } from "../api";
import { canDeleteAny } from "../authz";
import type { Maintenance } from "../types";

type CreateFormState = {
  vehicleId: string;
  maintenanceDate: string;
  description: string;
  cost: string;
  type: Maintenance["type"];
};

type EditFormState = {
  maintenanceDate: string;
  description: string;
  cost: string;
  type: Maintenance["type"];
};

const emptyCreateForm: CreateFormState = {
  vehicleId: "",
  maintenanceDate: "",
  description: "",
  cost: "",
  type: "ROUTINE"
};

const emptyEditForm: EditFormState = {
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

function validateCreateForm(form: CreateFormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const vehicleId = Number(form.vehicleId);
  const cost = Number(form.cost);
  if (!Number.isInteger(vehicleId) || vehicleId <= 0) errors.vehicleId = "Vehicle ID must be positive.";
  if (!form.maintenanceDate) errors.maintenanceDate = "Maintenance date is required.";
  if (!Number.isFinite(cost) || cost < 0) errors.cost = "Cost must be zero or positive.";
  return errors;
}

function validateEditForm(form: EditFormState): Record<string, string> {
  const errors: Record<string, string> = {};
  const cost = Number(form.cost);
  if (!form.maintenanceDate) errors.maintenanceDate = "Maintenance date is required.";
  if (!Number.isFinite(cost) || cost < 0) errors.cost = "Cost must be zero or positive.";
  return errors;
}

export function MaintenancesPage(): JSX.Element {
  const [activeTab, setActiveTab] = useState<"browse" | "create" | "edit">("browse");
  const [vehicleSearch, setVehicleSearch] = useState("");
  const [current, setCurrent] = useState<Maintenance | null>(null);
  const [maintenances, setMaintenances] = useState<Maintenance[]>([]);
  const [loadingList, setLoadingList] = useState(false);
  const [createForm, setCreateForm] = useState<CreateFormState>(emptyCreateForm);
  const [editForm, setEditForm] = useState<EditFormState>(emptyEditForm);
  const [createErrors, setCreateErrors] = useState<Record<string, string>>({});
  const [editErrors, setEditErrors] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const deleteAllowed = canDeleteAny();

  function resetCreateForm(): void {
    setCreateForm(emptyCreateForm);
    setCreateErrors({});
  }

  function resetEditForm(): void {
    if (!current) {
      setEditForm(emptyEditForm);
      setEditErrors({});
      return;
    }
    setEditForm({
      maintenanceDate: current.maintenanceDate,
      description: current.description ?? "",
      cost: String(current.cost),
      type: current.type
    });
    setEditErrors({});
  }

  async function handleDelete(): Promise<void> {
    if (!current) return;
    if (!window.confirm("Delete this maintenance record?")) return;
    try {
      await deleteMaintenance(current.id);
      setCurrent(null);
      setEditForm(emptyEditForm);
      setEditErrors({});
      setMessage("Maintenance deleted successfully.");
      setActiveTab("browse");
      void loadMaintenances();
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleCreate(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    setMessage("");
    setError("");
    const formErrors = validateCreateForm(createForm);
    setCreateErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

    const payload = {
      vehicleId: Number(createForm.vehicleId),
      maintenanceDate: createForm.maintenanceDate,
      description: createForm.description.trim() || undefined,
      cost: Number(createForm.cost),
      type: createForm.type
    };

    try {
      const item = await createMaintenance(payload);
      setCurrent(item);
      setEditForm({
        maintenanceDate: item.maintenanceDate,
        description: item.description ?? "",
        cost: String(item.cost),
        type: item.type
      });
      setMessage("Maintenance created successfully.");
      resetCreateForm();
      setActiveTab("edit");
      void loadMaintenances();
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function handleUpdate(event: React.FormEvent<HTMLFormElement>): Promise<void> {
    event.preventDefault();
    if (!current) return;
    setMessage("");
    setError("");
    const formErrors = validateEditForm(editForm);
    setEditErrors(formErrors);
    if (Object.keys(formErrors).length > 0) return;

    try {
      const item = await updateMaintenance(current.id, {
        maintenanceDate: editForm.maintenanceDate,
        description: editForm.description.trim() || undefined,
        cost: Number(editForm.cost),
        type: editForm.type
      });
      setCurrent(item);
      setEditForm({
        maintenanceDate: item.maintenanceDate,
        description: item.description ?? "",
        cost: String(item.cost),
        type: item.type
      });
      setMessage("Maintenance updated successfully.");
      void loadMaintenances();
    } catch (err) {
      setError(mapApiError(err));
    }
  }

  async function loadMaintenances(): Promise<void> {
    setLoadingList(true);
    try {
      const items = await getMaintenances();
      setMaintenances(items);
    } catch (err) {
      setError(mapApiError(err));
    } finally {
      setLoadingList(false);
    }
  }

  function openForEdit(item: Maintenance): void {
    setCurrent(item);
    setEditForm({
      maintenanceDate: item.maintenanceDate,
      description: item.description ?? "",
      cost: String(item.cost),
      type: item.type
    });
    setEditErrors({});
    setActiveTab("edit");
  }

  useEffect(() => {
    void loadMaintenances();
  }, []);

  const normalizedVehicleSearch = vehicleSearch.trim().toUpperCase();
  const visibleMaintenances = normalizedVehicleSearch.length === 0
    ? maintenances
    : maintenances.filter((item) =>
      (item.vehicleLicensePlate ?? "").toUpperCase().includes(normalizedVehicleSearch)
    );

  return (
    <section className="page">
      <h1>Maintenances</h1>

      <div className="entity-form">
        <h2>Workspace</h2>
        <div className="form-actions">
          <button type="button" className={activeTab === "browse" ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setActiveTab("browse")}>
            Browse all
          </button>
          <button type="button" className={activeTab === "create" ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setActiveTab("create")}>
            Create new
          </button>
          <button type="button" className={activeTab === "edit" ? "btn btn-primary" : "btn btn-secondary"} onClick={() => setActiveTab("edit")} disabled={!current}>
            Edit selected
          </button>
        </div>
      </div>

      {activeTab === "browse" && (
        <div className="entity-form">
          <h2>All maintenances</h2>
          <div className="form-grid">
            <label>
              Search by Vehicle number plate
              <input
                value={vehicleSearch}
                onChange={(e) => setVehicleSearch(e.target.value)}
                placeholder="e.g. AB-12-CDE"
              />
            </label>
          </div>
          <div className="form-actions">
            <button type="button" className="btn btn-secondary" onClick={() => void loadMaintenances()} disabled={loadingList}>
              {loadingList ? "Refreshing..." : "Refresh list"}
            </button>
          </div>
          {loadingList && <p>Loading maintenances...</p>}
          {!loadingList && visibleMaintenances.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Vehicle plate</th>
                  <th>Vehicle ID</th>
                  <th>Date</th>
                  <th>Type</th>
                  <th>Cost</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {visibleMaintenances.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>
                    <td>{item.vehicleLicensePlate ?? "-"}</td>
                    <td>{item.vehicleId}</td>
                    <td>{item.maintenanceDate}</td>
                    <td>{item.type}</td>
                    <td>{item.cost}</td>
                    <td className="actions-cell">
                      <button type="button" className="btn btn-secondary btn-sm" onClick={() => openForEdit(item)}>
                        Open
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
          {!loadingList && visibleMaintenances.length === 0 && <p>No maintenance records found for this search.</p>}
        </div>
      )}

      {activeTab === "create" && (
        <form className="entity-form" onSubmit={handleCreate}>
          <h2>Create maintenance</h2>
          <div className="form-grid">
            <label>
              Vehicle ID
              <input value={createForm.vehicleId} onChange={(e) => setCreateForm((c) => ({ ...c, vehicleId: e.target.value }))} />
              {createErrors.vehicleId && <span className="error">{createErrors.vehicleId}</span>}
            </label>
            <label>
              Date
              <input
                type="date"
                value={createForm.maintenanceDate}
                onChange={(e) => setCreateForm((c) => ({ ...c, maintenanceDate: e.target.value }))}
              />
              {createErrors.maintenanceDate && <span className="error">{createErrors.maintenanceDate}</span>}
            </label>
            <label>
              Cost
              <input value={createForm.cost} onChange={(e) => setCreateForm((c) => ({ ...c, cost: e.target.value }))} />
              {createErrors.cost && <span className="error">{createErrors.cost}</span>}
            </label>
            <label>
              Type
              <select value={createForm.type} onChange={(e) => setCreateForm((c) => ({ ...c, type: e.target.value as Maintenance["type"] }))}>
                <option value="ROUTINE">ROUTINE</option>
                <option value="REPAIR">REPAIR</option>
                <option value="INSPECTION">INSPECTION</option>
                <option value="OTHER">OTHER</option>
              </select>
            </label>
            <label className="full-width">
              Description
              <input
                value={createForm.description}
                onChange={(e) => setCreateForm((c) => ({ ...c, description: e.target.value }))}
              />
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">
              Create maintenance
            </button>
            <button type="button" className="btn btn-secondary" onClick={resetCreateForm}>
              Reset create form
            </button>
          </div>
        </form>
      )}

      {activeTab === "edit" && current && (
        <>
          <form className="entity-form" onSubmit={handleUpdate}>
            <h2>Edit maintenance #{current.id}</h2>
            <div className="form-grid">
              <label>
                Vehicle ID
                <input value={String(current.vehicleId)} disabled />
              </label>
              <label>
                Date
                <input
                  type="date"
                  value={editForm.maintenanceDate}
                  onChange={(e) => setEditForm((c) => ({ ...c, maintenanceDate: e.target.value }))}
                />
                {editErrors.maintenanceDate && <span className="error">{editErrors.maintenanceDate}</span>}
              </label>
              <label>
                Cost
                <input value={editForm.cost} onChange={(e) => setEditForm((c) => ({ ...c, cost: e.target.value }))} />
                {editErrors.cost && <span className="error">{editErrors.cost}</span>}
              </label>
              <label>
                Type
                <select value={editForm.type} onChange={(e) => setEditForm((c) => ({ ...c, type: e.target.value as Maintenance["type"] }))}>
                  <option value="ROUTINE">ROUTINE</option>
                  <option value="REPAIR">REPAIR</option>
                  <option value="INSPECTION">INSPECTION</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </label>
              <label className="full-width">
                Description
                <input
                  value={editForm.description}
                  onChange={(e) => setEditForm((c) => ({ ...c, description: e.target.value }))}
                />
              </label>
            </div>
            <div className="form-actions">
              <button className="btn btn-primary" type="submit">
                Save changes
              </button>
              <button type="button" className="btn btn-secondary" onClick={resetEditForm}>
                Reset edit form
              </button>
              {deleteAllowed && (
                <button type="button" className="btn btn-danger" onClick={() => void handleDelete()}>
                  Delete this maintenance
                </button>
              )}
            </div>
          </form>

          <div className="entity-form">
            <h2>Selected maintenance details</h2>
            <p>ID: {current.id}</p>
            <p>Vehicle ID: {current.vehicleId}</p>
            <p>Date: {current.maintenanceDate}</p>
            <p>Type: {current.type}</p>
            <p>Cost: {current.cost}</p>
          </div>
        </>
      )}

      {activeTab === "edit" && !current && (
        <div className="entity-form">
          <h2>Edit maintenance</h2>
          <p>Select a record from <strong>Browse all</strong> first.</p>
        </div>
      )}

      {message && <p className="message-success">{message}</p>}
      {error && <p className="error">{error}</p>}
    </section>
  );
}
