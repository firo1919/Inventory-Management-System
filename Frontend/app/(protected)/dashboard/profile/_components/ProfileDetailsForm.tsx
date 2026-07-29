import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { User, Mail, Phone } from "lucide-react";
import { Loader } from "@/components/ui/Loader";
import { profileDetailsSchema, ProfileDetailsInput } from "../schema";

interface ProfileDetailsFormProps {
    initialData: ProfileDetailsInput | null;
    onSubmit: (data: ProfileDetailsInput) => Promise<void>;
    updating: boolean;
}

export function ProfileDetailsForm({
    initialData,
    onSubmit,
    updating,
}: ProfileDetailsFormProps) {
    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<ProfileDetailsInput>({
        resolver: zodResolver(profileDetailsSchema),
        defaultValues: initialData || {
            firstName: "",
            lastName: "",
            username: "",
            email: "",
            phone: "",
        },
    });

    useEffect(() => {
        if (initialData) {
            reset(initialData);
        }
    }, [initialData, reset]);

    return (
        <div className="bg-white dark:bg-[#13131a] border border-slate-200 dark:border-white/5 rounded-2xl p-6 shadow-sm">
            <h3 className="font-bold text-slate-900 dark:text-white text-base mb-6">
                Profile Details
            </h3>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                    <div>
                        <label className="block text-xs font-semibold text-slate-400 mb-1">
                            First Name *
                        </label>
                        <div className="relative">
                            <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                                <User className="w-3.5 h-3.5" />
                            </span>
                            <input
                                type="text"
                                {...register("firstName")}
                                className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                            />
                        </div>
                        {errors.firstName && (
                            <p className="text-[10px] text-red-400 mt-1">
                                {errors.firstName.message}
                            </p>
                        )}
                    </div>

                    <div>
                        <label className="block text-xs font-semibold text-slate-400 mb-1">
                            Last Name *
                        </label>
                        <div className="relative">
                            <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                                <User className="w-3.5 h-3.5" />
                            </span>
                            <input
                                type="text"
                                {...register("lastName")}
                                className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                            />
                        </div>
                        {errors.lastName && (
                            <p className="text-[10px] text-red-400 mt-1">
                                {errors.lastName.message}
                            </p>
                        )}
                    </div>
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Username *
                    </label>
                    <div className="relative">
                        <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                            <User className="w-3.5 h-3.5" />
                        </span>
                        <input
                            type="text"
                            {...register("username")}
                            className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                    </div>
                    {errors.username && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.username.message}
                        </p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Email Address *
                    </label>
                    <div className="relative">
                        <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                            <Mail className="w-3.5 h-3.5" />
                        </span>
                        <input
                            type="email"
                            {...register("email")}
                            className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                    </div>
                    {errors.email && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.email.message}
                        </p>
                    )}
                </div>

                <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">
                        Phone Number
                    </label>
                    <div className="relative">
                        <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-slate-400">
                            <Phone className="w-3.5 h-3.5" />
                        </span>
                        <input
                            type="text"
                            {...register("phone")}
                            className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-[#0a0a0f] border border-slate-200 dark:border-white/5 rounded-xl text-xs text-slate-800 dark:text-white focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                    </div>
                    {errors.phone && (
                        <p className="text-[10px] text-red-400 mt-1">
                            {errors.phone.message}
                        </p>
                    )}
                </div>

                <div className="flex justify-end pt-4 border-t border-slate-200 dark:border-white/5">
                    <button
                        type="submit"
                        disabled={updating}
                        className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-60 text-white rounded-xl text-xs font-semibold shadow-md shadow-indigo-500/10 flex items-center gap-1.5 transition-colors cursor-pointer"
                    >
                        {updating ? (
                            <Loader
                                size={3.5}
                                text="Saving..."
                                className="flex-row gap-1"
                            />
                        ) : (
                            "Save Changes"
                        )}
                    </button>
                </div>
            </form>
        </div>
    );
}
