import { getToken } from "./auth";
import type {
  ApiResult,
  AuthRegisterResponse,
  Course,
  Instructor,
  Lesson,
  Maintenance,
  PageResponse,
  Payment,
  Student,
  Vehicle
} from "./types";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "";

export class ApiError extends Error {
  public readonly status: number;
  public readonly errorCode: string | null;

  constructor(message: string, status: number, errorCode: string | null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.errorCode = errorCode;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = getToken();
  const headers = new Headers(init?.headers ?? {});
  if (!headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...init,
    headers
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const payload = (await response.json()) as ApiResult<T>;
  if (!response.ok || !payload.success) {
    throw new ApiError(payload.message ?? "Request failed", response.status, payload.errorCode);
  }

  if (payload.data === null && response.status !== 204) {
    throw new ApiError(payload.message ?? "Empty response payload", response.status, payload.errorCode);
  }

  return payload.data as T;
}

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  username: string;
  roles: string[];
  profileType: string | null;
  profileId: number | null;
};

export async function login(email: string, password: string): Promise<LoginResponse> {
  return request<LoginResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password })
  });
}

export async function logout(): Promise<void> {
  await request<void>("/auth/logout", {
    method: "POST"
  });
}

export type RegisterStudentPayload = {
  email: string;
  password: string;
  studentProfile: {
    firstName: string;
    lastName: string;
    cnp: string;
    phone: string;
    address: string;
    targetDrivingCategoryCodes: string[];
    profile?: {
      emergencyContactName?: string;
      emergencyContactPhone?: string;
      notes?: string;
    };
  };
};

export async function registerStudent(payload: RegisterStudentPayload): Promise<AuthRegisterResponse> {
  return request<AuthRegisterResponse>("/auth/register/student", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export type RegisterInstructorPayload = {
  email: string;
  password: string;
  instructorProfile: {
    firstName: string;
    lastName: string;
    licenseNumber: string;
    phone: string;
    specialization: "THEORETICAL" | "PRACTICAL" | "BOTH";
  };
};

export async function registerInstructor(payload: RegisterInstructorPayload): Promise<AuthRegisterResponse> {
  return request<AuthRegisterResponse>("/auth/register/instructor", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export type RegisterAdminPayload = {
  email: string;
  password: string;
};

export async function registerAdmin(payload: RegisterAdminPayload): Promise<AuthRegisterResponse> {
  return request<AuthRegisterResponse>("/auth/register/admin", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function getStudentsPage(params: URLSearchParams): Promise<PageResponse<Student>> {
  return request<PageResponse<Student>>(`/api/students?${params.toString()}`);
}

export async function getStudentById(id: number): Promise<Student> {
  return request<Student>(`/api/students/${id}`);
}

export async function getMyStudentProfile(): Promise<Student> {
  return request<Student>("/api/students/me");
}

export type StudentRequestPayload = {
  firstName: string;
  lastName: string;
  cnp: string;
  email: string;
  phone: string;
  address: string;
  targetDrivingCategoryCodes: string[];
  profile?: {
    emergencyContactName?: string;
    emergencyContactPhone?: string;
    notes?: string;
  } | null;
};

export async function createStudent(payload: StudentRequestPayload): Promise<Student> {
  return request<Student>("/api/students", {
    method: "POST",
    body: JSON.stringify({ ...payload, profile: payload.profile ?? null })
  });
}

export async function updateStudent(id: number, payload: StudentRequestPayload): Promise<Student> {
  return request<Student>(`/api/students/${id}`, {
    method: "PUT",
    body: JSON.stringify({ ...payload, profile: payload.profile ?? null })
  });
}

export async function updateMyStudentProfile(payload: StudentRequestPayload): Promise<Student> {
  return request<Student>("/api/students/me", {
    method: "PUT",
    body: JSON.stringify({ ...payload, profile: payload.profile ?? null })
  });
}

export async function deleteStudent(id: number): Promise<void> {
  await request<void>(`/api/students/${id}`, { method: "DELETE" });
}

export type InstructorRequestPayload = {
  firstName: string;
  lastName: string;
  licenseNumber: string;
  email: string;
  phone: string;
  specialization: "THEORETICAL" | "PRACTICAL" | "BOTH";
};

export async function getInstructors(): Promise<Instructor[]> {
  return request<Instructor[]>("/api/instructors");
}

export async function getInstructorById(id: number): Promise<Instructor> {
  return request<Instructor>(`/api/instructors/${id}`);
}

export async function getMyInstructorProfile(): Promise<Instructor> {
  return request<Instructor>("/api/instructors/me");
}

export async function createInstructor(payload: InstructorRequestPayload): Promise<Instructor> {
  return request<Instructor>("/api/instructors", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function updateInstructor(id: number, payload: InstructorRequestPayload): Promise<Instructor> {
  return request<Instructor>(`/api/instructors/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function updateMyInstructorProfile(payload: InstructorRequestPayload): Promise<Instructor> {
  return request<Instructor>("/api/instructors/me", {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function deleteInstructor(id: number): Promise<void> {
  await request<void>(`/api/instructors/${id}`, { method: "DELETE" });
}

export type VehicleRequestPayload = {
  licensePlate: string;
  make: string;
  model: string;
  year: number;
  insuranceExpiry: string;
};

export async function getVehiclesPage(params: URLSearchParams): Promise<PageResponse<Vehicle>> {
  return request<PageResponse<Vehicle>>(`/api/vehicles?${params.toString()}`);
}

export async function createVehicle(payload: VehicleRequestPayload): Promise<Vehicle> {
  return request<Vehicle>("/api/vehicles", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function updateVehicle(id: number, payload: VehicleRequestPayload): Promise<Vehicle> {
  return request<Vehicle>(`/api/vehicles/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function deleteVehicle(id: number): Promise<void> {
  await request<void>(`/api/vehicles/${id}`, { method: "DELETE" });
}

export type CourseRequestPayload = {
  name: string;
  description: string;
  price: number;
  instructorId: number;
  vehicleId: number;
  numberOfLessons: number;
  courseType: "THEORETICAL" | "PRACTICAL";
  courseTagCodes: string[];
};

export async function getCoursesPage(params: URLSearchParams): Promise<PageResponse<Course>> {
  return request<PageResponse<Course>>(`/api/courses?${params.toString()}`);
}

export async function createCourse(payload: CourseRequestPayload): Promise<Course> {
  return request<Course>("/api/courses", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function updateCourse(id: number, payload: CourseRequestPayload): Promise<Course> {
  return request<Course>(`/api/courses/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function deleteCourse(id: number): Promise<void> {
  await request<void>(`/api/courses/${id}`, { method: "DELETE" });
}

export type LessonRequestPayload = {
  courseId: number;
  startTime: string;
  endTime?: string;
};

export async function getLessonsByDateRange(startTime: string, endTime: string): Promise<Lesson[]> {
  const params = new URLSearchParams();
  params.set("startTime", startTime);
  params.set("endTime", endTime);
  return request<Lesson[]>(`/api/lessons/date-range?${params.toString()}`);
}

export async function getLessonsForStudent(): Promise<Lesson[]> {
  return request<Lesson[]>("/api/lessons/students/me");
}

export async function getLessonsForInstructor(): Promise<Lesson[]> {
  return request<Lesson[]>("/api/lessons/instructors/me");
}

export async function createLesson(payload: LessonRequestPayload): Promise<Lesson> {
  return request<Lesson>("/api/lessons", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function updateLesson(id: number, payload: LessonRequestPayload): Promise<Lesson> {
  return request<Lesson>(`/api/lessons/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function deleteLesson(id: number): Promise<void> {
  await request<void>(`/api/lessons/${id}`, { method: "DELETE" });
}

export type PaymentPendingRequestPayload = {
  amount: number;
  lessonId?: number;
  notes?: string;
};

export type PaymentProcessRequestPayload = {
  paymentMethod: "CARD" | "CASH" | "BANK_TRANSFER" | "ONLINE";
  transactionId?: string;
  lessonId: number;
};

export async function getStudentPayments(status?: Payment["status"]): Promise<Payment[]> {
  const params = new URLSearchParams();
  if (status) params.set("status", status);
  const query = params.toString();
  return request<Payment[]>(`/api/payments/me${query ? `?${query}` : ""}`);
}

export async function createPendingPayment(payload: PaymentPendingRequestPayload): Promise<Payment> {
  return request<Payment>("/api/payments/pending", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function processPayment(payload: PaymentProcessRequestPayload): Promise<Payment> {
  return request<Payment>("/api/payments", {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function updatePaymentStatus(id: number, status: Payment["status"]): Promise<Payment> {
  return request<Payment>(`/api/payments/${id}/status`, {
    method: "PUT",
    body: JSON.stringify({ status })
  });
}

export async function deletePayment(id: number): Promise<void> {
  await request<void>(`/api/payments/${id}`, { method: "DELETE" });
}

export type MaintenanceRequestPayload = {
  vehicleId: number;
  maintenanceDate: string;
  description?: string;
  cost: number;
  type: Maintenance["type"];
};

export async function getMaintenanceById(id: number): Promise<Maintenance> {
  return request<Maintenance>(`/api/maintenances/${id}`);
}

export async function createMaintenance(payload: MaintenanceRequestPayload): Promise<Maintenance> {
  return request<Maintenance>("/api/maintenances", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function updateMaintenance(id: number, payload: Omit<MaintenanceRequestPayload, "vehicleId">): Promise<Maintenance> {
  return request<Maintenance>(`/api/maintenances/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export async function deleteMaintenance(id: number): Promise<void> {
  await request<void>(`/api/maintenances/${id}`, { method: "DELETE" });
}
