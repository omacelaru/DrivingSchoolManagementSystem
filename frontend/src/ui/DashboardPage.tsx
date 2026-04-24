import { Link } from "react-router-dom";
import {
  canAccessCourses,
  canAccessInstructors,
  canAccessLessons,
  canAccessMaintenances,
  canAccessPayments,
  canAccessStudents,
  canAccessVehicles,
  canManageAuthAdmin,
  getRoleLabels
} from "../authz";

export function DashboardPage(): JSX.Element {
  const cards = [
    ...(canAccessVehicles()
      ? [{ to: "/vehicles", title: "Vehicles", description: "View school vehicles and their availability." }]
      : []),
    ...(canAccessCourses()
      ? [{ to: "/courses", title: "Courses", description: "Browse driving course plans and details." }]
      : []),
    ...(canAccessLessons() ? [{ to: "/lessons", title: "Lessons", description: "Check lesson schedules and status." }] : []),
    ...(canAccessPayments()
      ? [{ to: "/payments", title: "Payments", description: "Review payment history and current status." }]
      : []),
    ...(canAccessMaintenances()
      ? [{ to: "/maintenances", title: "Maintenances", description: "Track service work for each vehicle." }]
      : []),
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
