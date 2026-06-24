export interface ApiError {
  errorCode: string
  message: string
}

export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiError | null
}

export const extractErrorMessage = (error: unknown): string => {
  const axiosError = error as { response?: { data?: ApiResponse<unknown> & { message?: string } }; message?: string }
  return axiosError.response?.data?.error?.message ?? axiosError.response?.data?.message ?? axiosError.message ?? 'Something went wrong'
}
