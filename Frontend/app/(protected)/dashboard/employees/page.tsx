"use client";

import React, { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { employeesService } from "@/services/employees";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Plus, Search, Edit2, Trash2, Shield, CheckCircle, XCircle, Eye } from "lucide-react";
import { EmployeeFormModal } from "./_components/EmployeeFormModal";
import { DeleteEmployeeModal } from "./_components/DeleteEmployeeModal";
import { EmployeeViewModal } from "./_components/EmployeeViewModal";
import { Pagination } from "@/components/ui/Pagination";
import { Loader } from "@/components/ui/Loader";
import { EmployeeInput } from "./schema";
import { Employee } from "@/types";

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
  const [employees, setEmployees] = useState<Employee[]>([]);

  // Pagination & Search
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState("");

  // Loading States
  const [loading, setLoading] = useState(true);
  const [modalLoading, setModalLoading] = useState(false);

  // Modal States
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Selected Employee
  const [selectedEmployee, setSelectedEmployee] = useState<Employee | null>(null);
  const [formError, setFormError] = useState("");

  const fetchEmployees = async () => {
    if (!isAdmin) return;
    try {
      setLoading(true);
      const data = await employeesService.getEmployees({
        page,
        size: pageSize,
      });

      let content = data?.content || [];

      // Client-side search filtering
      if (search) {
        const query = search.toLowerCase();
        content = content.filter(
          (emp: Employee) =>
            emp.firstName.toLowerCase().includes(query) ||
            emp.lastName.toLowerCase().includes(query) ||
            emp.email.toLowerCase().includes(query) ||
            emp.username?.toLowerCase().includes(query)
        );
      }

      setEmployees(content);
      setTotalPages(data?.totalPages || 1);
      setTotalElements(data?.totalElements || content.length);
    } catch {
      toast.error("Failed to load employees.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const load = async () => {
      await fetchEmployees();
    };
    load();
  }, [page, pageSize, isAdmin]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchEmployees();
  };

  // Add Employee
  const handleAddEmployee = async (data: EmployeeInput) => {
    setFormError("");
    setModalLoading(true);
    try {
      await employeesService.createEmployee(data);
      setIsAddModalOpen(false);
      toast.success("Employee registered successfully!");
      fetchEmployees();
    } catch (err: unknown) {
      const errorMsg = (err as { response?: { data?: { message?: string } } }).response?.data?.message || (err as Error).message || "Failed to create employee";
      toast.error(errorMsg);
      setFormError(errorMsg);
    } finally {
      setModalLoading(false);
    }
  };

  // Edit Employee
  const handleEditEmployee = async (data: EmployeeInput) => {
    setFormError("");
    setModalLoading(true);
    try {
      if (!selectedEmployee) return;
      await employeesService.updateEmployee(selectedEmployee.id, data);
      setIsEditModalOpen(false);
      toast.success("Employee updated!");
      fetchEmployees();
    } catch (err: unknown) {
      const errorMsg = (err as { response?: { data?: { message?: string } } }).response?.data?.message || (err as Error).message || "Failed to update employee";
      toast.error(errorMsg);
      setFormError(errorMsg);
    } finally {
      setModalLoading(false);
    }
  };

  // Delete Employee
  const handleDeleteEmployee = async () => {
    setModalLoading(true);
    try {
      if (!selectedEmployee) return;
      await employeesService.deleteEmployee(selectedEmployee.id);
      setIsDeleteModalOpen(false);
      toast.success("Employee deleted.");
      fetchEmployees();
    } catch (err: unknown) {
      toast.error((err as { response?: { data?: { message?: string } } }).response?.data?.message || (err as Error).message || "Failed to delete employee");
    } finally {
      setModalLoading(false);
    }
  };

  // Toggle Active/Deactive Employee Status
  const handleToggleActive = async (employee: Employee) => {
    try {
      await employeesService.toggleEmployeeStatus(employee.id, employee.active);
      toast.success(employee.active ? "Employee deactivated." : "Employee activated.");
      fetchEmployees();
    } catch (err: unknown) {
      toast.error((err as { response?: { data?: { message?: string } } }).response?.data?.message || (err as Error).message || "Failed to change active status");
    }
  };

  const openAddModal = () => {
    setFormError("");
    setIsAddModalOpen(true);
  };

  const openEditModal = (emp: Employee) => {
    setSelectedEmployee(emp);
    setFormError("");
    setIsEditModalOpen(true);
  };

  const openDeleteModal = (emp: Employee) => {
    setSelectedEmployee(emp);
    setIsDeleteModalOpen(true);
  };

  const openViewModal = (emp: Employee) => {
    setSelectedEmployee(emp);
    setIsViewModalOpen(true);
  };

  if (authLoading || !isAdmin) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] text-slate-400">
        <Loader text="Verifying Admin Permissions..." />
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
                          emp.role === "ADMIN"
                            ? "bg-purple-500/10 text-purple-500 border border-purple-500/20"
                            : "bg-blue-500/10 text-blue-500 border border-blue-500/20"
                        }`}
                      >
                        <Shield className="w-3 h-3" />
                        {emp.role}
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
        <Pagination
          page={page}
          pageSize={pageSize}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={setPage}
          loading={loading}
          itemName="employees"
        />
      </div>

      {/* CREATE MODAL */}
      <EmployeeFormModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSubmit={handleAddEmployee}
        loading={modalLoading}
        title="Register New Employee"
        submitLabel="Add Employee"
        formError={formError}
      />

      {/* EDIT MODAL */}
      <EmployeeFormModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        onSubmit={handleEditEmployee}
        initialData={selectedEmployee ? {
          firstName: selectedEmployee.firstName,
          lastName: selectedEmployee.lastName,
          username: selectedEmployee.username || "",
          email: selectedEmployee.email,
          password: "",
          phone: selectedEmployee.phone || "",
          role: selectedEmployee.role === "ADMIN" ? "ADMIN" : "EMPLOYEE",
        } : null}
        loading={modalLoading}
        title="Edit Employee Details"
        submitLabel="Save Changes"
        formError={formError}
      />

      {/* DELETE CONFIRMATION */}
      <DeleteEmployeeModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteEmployee}
        employeeName={selectedEmployee ? `${selectedEmployee.firstName} ${selectedEmployee.lastName}` : ""}
        loading={modalLoading}
      />

      {/* EMPLOYEE VIEW DETAILS MODAL */}
      <EmployeeViewModal
        isOpen={isViewModalOpen}
        onClose={() => setIsViewModalOpen(false)}
        employee={selectedEmployee}
      />
    </div>
  );
}
