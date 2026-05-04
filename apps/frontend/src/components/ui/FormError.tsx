type FormErrorProps = {
  message?: string;
};

export function FormError({ message }: FormErrorProps) {
  if (!message) {
    return null;
  }

  return (
    <p role="alert" className="neo-panel rounded-[1rem] border-[3px] border-[#111111] bg-[#ffcfcb] px-4 py-3 text-sm font-semibold text-[#111111]">
      {message}
    </p>
  );
}
