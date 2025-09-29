import { useState, useEffect, createContext, useContext } from 'react';
import { User, UserRole } from '../types';
import { apiService } from '../lib/api';
import { storage } from '../lib/storage';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  signIn: (email: string, password: string) => Promise<void>;
  signUp: (email: string, password: string, role: UserRole) => Promise<void>;
  signOut: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};


export const useAuthProvider = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check for existing user session
    const currentUser = storage.getCurrentUser();
    setUser(currentUser);
    setLoading(false);
  }, []);

  const signIn = async (email: string, password: string) => {
    const result = await apiService.signIn(email, password);
    
    if (result.error) {
      throw new Error(result.error);
    }

    if (result.data) {
      const userSession: User = {
        id: result.data.id.toString(),
        email: result.data.email,
        role: result.data.role.toLowerCase(),
        created_at: new Date().toISOString(),
        farmer_id: result.data.farmerId,
        distributor_id: result.data.distributorId,
        name: result.data.name,
        location: result.data.location
      }

      setUser(userSession);
      localStorage.setItem('current_user', JSON.stringify(userSession));
    }
  };

  const signUp = async (email: string, password: string, role: UserRole, name: string, location: string) => {
    const result = await apiService.signUp({
      email,
      password,
      name,
      location,
      role
    });
    
    if (result.error) {
      throw new Error(result.error);
    }

    // After successful signup, sign in the user
    await signIn(email, password);
  };

  const signOut = async () => {
    apiService.signOut();
    setUser(null);
    localStorage.removeItem('current_user');
  };

  useEffect(() => {
    // Check for existing user session
    const storedUser = localStorage.getItem('current_user');
    if (storedUser) {
      try {
        const userSession = JSON.parse(storedUser);
        setUser(userSession);
      } catch (error) {
        console.error('Error parsing stored user:', error);
        localStorage.removeItem('current_user');
      }
    }
    setLoading(false);
  }, []);

  return {
    user,
    loading,
    signIn,
    signUp,
    signOut
  };
};