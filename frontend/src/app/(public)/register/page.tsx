import { RegisterForm } from "@/components/auth/register-form";

export default function RegisterPage() {
  return (
    <div className="min-h-screen bg-bg-primary flex items-center justify-center relative overflow-hidden">
      <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_50%_40%,rgba(212,165,116,0.06)_0%,transparent_60%)]" />

      <div className="absolute top-8 left-1/2 -translate-x-1/2 font-[family-name:var(--font-playfair)] text-[22px] text-accent-bright tracking-[2px]">
        CINÉMA
      </div>

      <div className="relative w-[380px] p-10 bg-glass-bg border border-glass-border rounded-2xl backdrop-blur-xl z-10">
        <h1 className="font-[family-name:var(--font-playfair)] text-2xl text-text-primary mb-2">
          Join CINÉMA
        </h1>
        <p className="text-sm text-text-muted mb-7">Create your account</p>
        <RegisterForm />
      </div>
    </div>
  );
}
