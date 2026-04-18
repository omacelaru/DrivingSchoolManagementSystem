export type ApiResult<T> = {
  success: boolean;
  message: string | null;
  data: T | null;
  timestamp: string;
  errorCode: string | null;
};

export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  sort: string;
};

export type Student = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  cnp: string;
  address: string;
  targetDrivingCategoryCodes: string[];
  status: "PENDING" | "ACTIVE" | "SUSPENDED" | "GRADUATED";
  profile?: StudentProfile | null;
};

export type StudentProfile = {
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
  notes: string | null;
};

export type Instructor = {
  id: number;
  firstName: string;
  lastName: string;
  licenseNumber: string;
  email: string;
  phone: string;
  specialization: "THEORETICAL" | "PRACTICAL" | "BOTH";
  rating: number;
};

export type Vehicle = {
  id: number;
  licensePlate: string;
  make: string;
  model: string;
  year: number;
  insuranceExpiry: string;
  status: "AVAILABLE" | "IN_USE" | "MAINTENANCE" | "OUT_OF_SERVICE";
};

export type Course = {
  id: number;
  name: string;
  description: string;
  price: number;
  numberOfLessons: number;
  courseType: "THEORETICAL" | "PRACTICAL";
  instructorId: number;
  vehicleId: number;
  courseTagCodes: string[];
};

export type Lesson = {
  id: number;
  studentId: number;
  instructorId: number;
  instructorName: string;
  vehicleId: number;
  courseId: number | null;
  startTime: string;
  endTime: string;
  status: "SCHEDULED" | "COMPLETED" | "CANCELLED" | "NO_SHOW";
};

export type Payment = {
  id: number;
  studentId: number;
  amount: number;
  paymentMethod: "CARD" | "CASH" | "BANK_TRANSFER" | "ONLINE" | null;
  status: "PENDING" | "COMPLETED" | "FAILED" | "REFUNDED" | "CANCELLED";
  transactionDate: string;
  transactionId: string | null;
  lessonId: number | null;
  notes: string | null;
};

export type Maintenance = {
  id: number;
  vehicleId: number;
  maintenanceDate: string;
  description: string | null;
  cost: number;
  type: "ROUTINE" | "REPAIR" | "INSPECTION" | "OTHER";
  createdAt: string;
};

export type AuthRegisterResponse = {
  userId: number;
  username: string;
  role: string;
  profileType: string;
  profileId: number | null;
};
