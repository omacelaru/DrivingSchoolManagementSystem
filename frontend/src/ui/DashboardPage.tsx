import { canAccessInstructors, canAccessStudents, canManageAuthAdmin, getRoles } from "../authz";

export function DashboardPage(): JSX.Element {
  const cards = [
    { title: "Vehicles", description: "Fleet status, insurance and maintenance operations." },
    { title: "Courses", description: "Driving course catalog and allocations." },
    { title: "Lessons", description: "Scheduling and lesson lifecycle." },
    { title: "Payments", description: "Pending/completed payments and status updates." },
    { title: "Maintenances", description: "Maintenance records and cost tracking." },
    ...(canAccessStudents() ? [{ title: "Students", description: "Student CRUD and document flows." }] : []),
    ...(canAccessInstructors() ? [{ title: "Instructors", description: "Instructor CRUD and availability flows." }] : []),
    ...(canManageAuthAdmin() ? [{ title: "Auth Management", description: "Admin-only registration operations." }] : [])
  ];

  return (
    <section className="page">
      <h1>Dashboard</h1>
      <p className="dashboard-lead">Signed in roles: {getRoles().join(", ") || "none"}</p>
      <div className="dashboard-grid">
        {cards.map((card) => (
          <article key={card.title} className="dashboard-card">
            <h3>{card.title}</h3>
            <p>{card.description}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
