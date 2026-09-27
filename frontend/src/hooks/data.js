import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { message } from "../services/api";
export const useData = (key, fn, enabled = true) =>
  useQuery({
    queryKey: Array.isArray(key) ? key : [key],
    queryFn: fn,
    enabled,
  });
export function useAction(fn, keys, success = "Alterações salvas.") {
  const client = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => {
      keys.forEach((key) => client.invalidateQueries({ queryKey: [key] }));
      toast.success(success);
    },
    onError: (e) => toast.error(message(e)),
  });
}
