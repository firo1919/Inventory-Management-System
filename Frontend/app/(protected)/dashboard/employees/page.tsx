"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { apiClient } from "@/lib/api-client";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import {
  Plus,
  Search,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Trash2,
  AlertTriangle,
  Loader2,
  Shield,
  CheckCircle,
  XCircle,
  Eye,
} from "lucide-react";

const employeeSchema = z.object({
  firstName: z.string().min(1, "First name is required"),
  lastName: z.string().min(1, "Last name is required"),
  username: z.string().min(3, "Username must be at least 3 characters"),
  email: z.string().min(1, "Email is required").email("Invalid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
  phone: z.string().min(5, "Phone number must be at least 5 characters"),
  role: z.enum(["EMPLOYEE", "ADMIN"]),
});

type EmployeeInput = z.infer<typeof employeeSchema>;

export default function EmployeesPage() {
  const { isAdmin, loading: authLoading } = useAuth();
  const router = useRouter();

  // Route Guard
  useEffect(() => {
    if (!authLoading && !isAdmin) {
      router.replace("/dashboard");
    }
  }, [isAdmin, authLoading, router]);

  // Data States
  const [employees, setEmployees] = useState<any[]>([]);

  // Pagination & Search
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");

  // Loading States
  const [loading, setLoading] = useState(true);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Selected Employee
  const [selectedEmployee, setSelectedEmployee] = useState<any>(null);

  const [formError, setFormError] = useState("");

  // React Hook Form
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<EmployeeInput>({
    resolver: zodResolver(employeeSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      username: "",
      email: "",
      password: "",
      phone: "",
      role: "EMPLOYEE",
    },
  });

  const fetchEmployees = async () => {
    if (!isAdmin) return;
    try {
      setLoading(true);
      const res = await apiClient.get("/api/v1/admin/employees", {
        params: {
          page,
          size: pageSize,
        },
      });

      let content = res.data?.content || [];

      // Client-side search filtering
      if (search) {
        const query = search.toLowerCase();
        content = content.filter(
          (emp: any) =>
            emp.firstName.toLowerCase().includes(query) ||
            emp.lastName.toLowerCase().includes(query) ||
            emp.email.toLowerCase().includes(query) ||
            emp.username.toLowerCase().includes(query)
        );
      }

      setEmployees(content);
      setTotalPages(res.data?.totalPages || 1);
      setTotalElements(res.data?.totalElements || content.length);
    } catch {
      toast.error("Failed to load employees.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, [page, pageSize, isAdmin]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchEmployees();
  };

  // Add Employee
  const handleAddEmployee = async (data: EmployeeInput) => {
    setFormError("");
    setLoading(true);
    try {
      await apiClient.post("/api/v1/admin/employees", data);
      setIsAddModalOpen(false);
      reset();
      toast.success("Employee registered successfully!");
      fetchEmployees();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to create employee");
      setFormError(err.response?.data?.message || err.message || "Failed to create employee");
    } finally {
      setLoading(false);
    }
  };

  // Edit Employee
  const handleEditEmployee = async (data: EmployeeInput) => {
    setFormError("");
    setLoading(true);
    try {
      await apiClient.put(`/api/v1/admin/employees/${selectedEmployee.id}`, data);
      setIsEditModalOpen(false);
      reset();
      toast.success("Employee updated!");
      fetchEmployees();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to update employee");
      setFormError(err.response?.data?.message || err.message || "Failed to update employee");
    } finally {
      setLoading(false);
    }
  };

  // Delete Employee
  const handleDeleteEmployee = async () => {
    setLoading(true);
    try {
      await apiClient.delete(`/api/v1/admin/employees/${selectedEmployee.id}`);
      setIsDeleteModalOpen(false);
      toast.success("Employee deleted.");
      fetchEmployees();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to delete employee");
    } finally {
      setLoading(false);
    }
  };

  // Toggle Active/Deactive Employee Status
  const handleToggleActive = async (employee: any) => {
    try {
      const endpoint = `/api/v1/admin/employees/${employee.id}/${
        employee.active ? "deactivate" : "activate"
      }`;
      await apiClient.post(endpoint);
      toast.success(employee.active ? "Employee deactivated." : "Employee activated.");
      fetchEmployees();
    } catch (err: any) {
      toast.error(err.response?.data?.message || err.message || "Failed to change active status");
    }
  };

  const openAddModal = () => {
    reset({
      firstName: "",
      lastName: "",
      username: "",
      email: "",
      password: "",
      phone: "",
      role: "EMPLOYEE",
    });
    setFormError("");
    setIsAddModalOpen(true);
  };

  const openEditModal = (emp: any) => {
    setSelectedEmployee(emp);
    reset({
      firstName: emp.firstName,
      lastName: emp.lastName,
      username: emp.username,
      email: emp.email,
      password: "", // User must re-enter or type new password for safety check
      phone: emp.phone || "",
      role: emp.role?.name || "EMPLOYEE",
    });
    setFormError("");
    setIsEditModalOpen(true);
  };

  const openDeleteModal = (emp: any) => {
    setSelectedEmployee(emp);
    setIsDeleteModalOpen(true);
  };

  const openViewModal = (emp: any) => {
    setSelectedEmployee(emp);
    setIsViewModalOpen(true);
  };

  if (authLoading || !isAdmin) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] text-slate-400">
        <Loader2 className="w-8 h-8 animate-spin text-indigo-500 mb-2" />
        <p className="text-xs">Verifying Admin Permissions...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* HEADER */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold dark:text-white">Employees Directory</h1>
          <p className="text-xs text-slate-400">Register new staff accounts, manage roles, and activate/deactivate access</p>
        </div>
        <button
          onClick={openAddModal}
          className="inline-flex items-center gap-1.5 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-505 text-white text-xs font-semibold rounded-xl transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
        >
          <Plus className="w-4 h-4" /> Add Employee
        </button>
      </div>

      {/* FILTER & SEARCH */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-4 flex flex-col md:flex-row gap-4 items-center justify-between shadow-sm">
        <form onSubmit={handleSearchSubmit} className="relative w-full md:max-w-xs">
          <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400 pointer-events-none">
            <Search className="w-4 h-4" />
          </span>
          <input
            type="text"
            placeholder="Search by name, email, user..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
          />
        </form>
      </div>

      {/* EMPLOYEES TABLE */}
      <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-slate-200 dark:border-white/5 bg-slate-50/50 dark:bg-[#0a0a0f]/50 text-slate-400 text-xs font-semibold">
                <th className="p-4">Name</th>
                <th className="p-4">Username</th>
                <th className="p-4">Email</th>
                <th className="p-4">Phone</th>
                <th className="p-4">Role</th>
                <th className="p-4">Status</th>
                <th className="p-4 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-white/5">
              {loading ? (
                Array.from({ length: 3 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={7} className="p-4 h-12 bg-slate-50/10 dark:bg-[#0e0e13]/10" />
                  </tr>
                ))
              ) : employees.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-slate-400">
                    No employees registered in directory.
                  </td>
                </tr>
              ) : (
                employees.map((emp) => (
                  <tr
                    key={emp.id}
                    onClick={() => openViewModal(emp)}
                    className="hover:bg-slate-50/50 dark:hover:bg-[#1c1c24]/10 text-slate-600 dark:text-slate-300 cursor-pointer"
                  >
                    <td className="p-4 font-semibold text-slate-800 dark:text-white">
                      {emp.firstName} {emp.lastName}
                    </td>
                    <td className="p-4 font-mono text-xs">{emp.username}</td>
                    <td className="p-4">{emp.email}</td>
                    <td className="p-4 text-xs">{emp.phone || "-"}</td>
                    <td className="p-4">
                      <span
                        className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded text-[10px] font-bold uppercase ${
                          emp.role?.name === "ADMIN"
                            ? "bg-purple-500/10 text-purple-500 border border-purple-500/20"
                            : "bg-blue-500/10 text-blue-500 border border-blue-500/20"
                        }`}
                      >
                        <Shield className="w-3 h-3" />
                        {emp.role?.name}
                      </span>
                    </td>
                    <td className="p-4" onClick={(e) => e.stopPropagation()}>
                      <button
                        onClick={() => handleToggleActive(emp)}
                        className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold uppercase transition-all border ${
                          emp.active
                            ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20 hover:bg-emerald-500/20"
                            : "bg-red-500/10 text-red-500 border-red-500/20 hover:bg-red-500/20"
                        } cursor-pointer`}
                      >
                        {emp.active ? <CheckCircle className="w-3.5 h-3.5" /> : <XCircle className="w-3.5 h-3.5" />}
                        {emp.active ? "Active" : "Deactivated"}
                      </button>
                    </td>
                    <td className="p-4" onClick={(e) => e.stopPropagation()}>
                      <div className="flex items-center justify-center gap-1.5">
                        <button
                          onClick={() => openViewModal(emp)}
                          title="View Details"
                          className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-indigo-500 hover:bg-slate-100 transition-colors cursor-pointer"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => openEditModal(emp)}
                          title="Edit details"
                          className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-emerald-500 hover:bg-slate-100 transition-colors cursor-pointer"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => openDeleteModal(emp)}
                          title="Delete Employee"
                          className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 bg-slate-50 dark:bg-[#0a0a0f] text-slate-400 hover:text-red-500 hover:bg-slate-100 transition-colors cursor-pointer"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* PAGINATION */}
        <div className="p-4 border-t border-slate-200 dark:border-white/5 flex items-center justify-between text-xs text-slate-400 bg-slate-50/50 dark:bg-[#0a0a0f]/50">
          <p>
            Showing {employees.length} of {totalElements} employees
          </p>
          <div className="flex gap-2">
            <button
              disabled={page === 0 || loading}
              onClick={() => setPage(page - 1)}
              className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 disabled:opacity-50"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="py-1 px-3 bg-slate-100 dark:bg-white/5 rounded-lg font-semibold text-slate-700 dark:text-white">
              Page {page + 1} of {totalPages}
            </span>
            <button
              disabled={page >= totalPages - 1 || loading}
              onClick={() => setPage(page + 1)}
              className="p-1.5 rounded-lg border border-slate-200 dark:border-white/5 disabled:opacity-50"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* CREATE MODAL */}
      {isAddModalOpen && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-md p-6 overflow-y-auto max-h-[90vh]">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Register New Employee</h3>
            <form onSubmit={handleSubmit(handleAddEmployee)} className="space-y-4">
              {formError && (
                <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4" />
                  <span>{formError}</span>
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">First Name *</label>
                  <input
                    type="text"
                    {...register("firstName")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.firstName && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.firstName.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Last Name *</label>
                  <input
                    type="text"
                    {...register("lastName")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.lastName && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.lastName.message}</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Username *</label>
                <input
                  type="text"
                  {...register("username")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {errors.username && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.username.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Email Address *</label>
                <input
                  type="email"
                  {...register("email")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {errors.email && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.email.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Temporary Password (min 8 chars) *</label>
                <input
                  type="password"
                  {...register("password")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {errors.password && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.password.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Phone Number *</label>
                <input
                  type="text"
                  {...register("phone")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  placeholder="+1234567890"
                />
                {errors.phone && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.phone.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Select Role *</label>
                <select
                  {...register("role")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs font-semibold text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  <option value="EMPLOYEE">EMPLOYEE</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
                {errors.role && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.role.message}</p>
                )}
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
                <button
                  type="button"
                  onClick={() => setIsAddModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
                >
                  {loading ? "Registering..." : "Add Employee"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT MODAL */}
      {isEditModalOpen && selectedEmployee && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-md p-6 overflow-y-auto max-h-[90vh]">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-4">Edit Employee Details</h3>
            <form onSubmit={handleSubmit(handleEditEmployee)} className="space-y-4">
              {formError && (
                <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-xs p-3 rounded-lg flex items-center gap-1.5">
                  <AlertTriangle className="w-4 h-4" />
                  <span>{formError}</span>
                </div>
              )}

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">First Name *</label>
                  <input
                    type="text"
                    {...register("firstName")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.firstName && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.firstName.message}</p>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1">Last Name *</label>
                  <input
                    type="text"
                    {...register("lastName")}
                    className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  />
                  {errors.lastName && (
                    <p className="text-[10px] text-red-400 mt-1">{errors.lastName.message}</p>
                  )}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Username *</label>
                <input
                  type="text"
                  {...register("username")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {errors.username && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.username.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Email Address *</label>
                <input
                  type="email"
                  {...register("email")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {errors.email && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.email.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Password (Required for update validation) *</label>
                <input
                  type="password"
                  {...register("password")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                  placeholder="Re-enter or type new password"
                />
                {errors.password && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.password.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Phone Number *</label>
                <input
                  type="text"
                  {...register("phone")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {errors.phone && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.phone.message}</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-400 mb-1">Select Role *</label>
                <select
                  {...register("role")}
                  className="w-full px-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs font-semibold text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  <option value="EMPLOYEE">EMPLOYEE</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
                {errors.role && (
                  <p className="text-[10px] text-red-400 mt-1">{errors.role.message}</p>
                )}
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-slate-200 dark:border-white/5">
                <button
                  type="button"
                  onClick={() => setIsEditModalOpen(false)}
                  className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-505 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-indigo-500/10 cursor-pointer"
                >
                  {loading ? "Saving..." : "Save Changes"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION */}
      {isDeleteModalOpen && selectedEmployee && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-sm p-6 text-center">
            <AlertTriangle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-2">Delete Employee Account</h3>
            <p className="text-xs text-slate-400 mb-6 font-medium">
              Are you sure you want to delete <span className="font-semibold text-slate-700 dark:text-white">{selectedEmployee.firstName} {selectedEmployee.lastName}</span>? This will permanently delete the employee record and suspend all system access.
            </p>
            <div className="flex justify-center gap-3">
              <button
                onClick={() => setIsDeleteModalOpen(false)}
                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-500 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteEmployee}
                disabled={loading}
                className="px-4 py-2 bg-red-600 hover:bg-red-505 text-white rounded-xl text-xs font-semibold transition-all shadow-md shadow-red-500/10 cursor-pointer"
              >
                {loading ? "Deleting..." : "Delete Account"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* EMPLOYEE VIEW DETAILS MODAL */}
      {isViewModalOpen && selectedEmployee && (
        <div className="fixed inset-0 bg-slate-950/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-5">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">Employee details</h3>
              <span
                className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase border ${
                  selectedEmployee.role?.name === "ADMIN"
                    ? "bg-purple-500/10 text-purple-500 border-purple-500/20"
                    : "bg-blue-500/10 text-blue-500 border-blue-500/20"
                }`}
              >
                <Shield className="w-3 h-3" />
                {selectedEmployee.role?.name}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4 text-xs mb-6">
              <div>
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">First Name</span>
                <span className="font-semibold text-slate-800 dark:text-white">{selectedEmployee.firstName}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Last Name</span>
                <span className="font-semibold text-slate-800 dark:text-white">{selectedEmployee.lastName}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Username</span>
                <span className="font-mono">{selectedEmployee.username}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">User ID</span>
                <span className="font-mono text-slate-500">{selectedEmployee.id}</span>
              </div>
              <div className="col-span-2">
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Email Address</span>
                <span className="font-semibold">{selectedEmployee.email}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Phone Number</span>
                <span>{selectedEmployee.phone || "—"}</span>
              </div>
              <div>
                <span className="text-[10px] text-slate-400 uppercase tracking-wider block mb-0.5">Account Status</span>
                <span
                  className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded text-[10px] font-bold uppercase border ${
                    selectedEmployee.active
                      ? "bg-emerald-500/10 text-emerald-500 border-emerald-500/20"
                      : "bg-red-500/10 text-red-500 border-red-500/20"
                  }`}
                >
                  {selectedEmployee.active ? "Active" : "Deactivated"}
                </span>
              </div>
            </div>

            <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
              <button
                onClick={() => setIsViewModalOpen(false)}
                className="px-4 py-2 border border-slate-200 dark:border-white/10 text-slate-505 hover:text-slate-700 dark:text-slate-400 rounded-xl text-xs font-semibold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
