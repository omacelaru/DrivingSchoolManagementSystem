import { Link } from "react-router-dom";
import { canAccessInstructors, canAccessStudents, canManageAuthAdmin, getRoleLabels } from "../authz";

export function DashboardPage(): JSX.Element {
  const cards = [
    { to: "/vehicles", title: "Vehicles", description: "Manage school vehicles and their availability." },
    { to: "/courses", title: "Courses", description: "Browse and update driving course plans." },
    { to: "/lessons", title: "Lessons", description: "View and organize upcoming lessons." },
    { to: "/payments", title: "Payments", description: "Check payment history and current status." },
    { to: "/maintenances", title: "Maintenances", description: "Track service work for each vehicle." },
    ...(canAccessStudents()
      ? [{ to: "/students", title: "Students", description: "Open student profiles and progress details." }]
      : []),
    ...(canAccessInstructors()
      ? [{ to: "/instructors", title: "Instructors", description: "See instructor profiles and schedules." }]
      : []),
    ...(canManageAuthAdmin()
      ? [{ to: "/auth-management", title: "Access Management", description: "Create accounts for team members." }]
      : [])
  ];

  return (
    <section className="page">
      <h1>Dashboard</h1>
      <p className="dashboard-lead">Signed in as: {getRoleLabels().join(", ") || "User"}</p>
      <div className="dashboard-grid">
        {cards.map((card) => (
          <Link key={card.title} to={card.to} className="dashboard-card dashboard-card-link">
            <h3>{card.title}</h3>
            <p>{card.description}</p>
          </Link>
        ))}
      </div>
    </section>
  );
}
