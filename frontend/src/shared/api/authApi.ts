import client from './client';

export const login = async (credentials: any) => {
  const { data } = await client.post('/auth/login', credentials);
  return data;
};

export const adminLogin = async (credentials: any) => {
  const { data } = await client.post('/admin/auth/login', credentials);
  return data;
};

export const adminRegister = async (payload: any) => {
  const { data } = await client.post('/admin/auth/register', payload);
  return data;
};

export const register = async (payload: any) => {
  const { data } = await client.post('/auth/register', payload);
  return data;
};
