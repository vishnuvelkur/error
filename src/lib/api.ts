const API_BASE_URL = 'http://localhost:8080/api';

interface ApiResponse<T> {
  data?: T;
  error?: string;
}

class ApiService {
  private getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('auth_token');
    return {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` })
    };
  }

  private async handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
    if (!response.ok) {
      const errorText = await response.text();
      console.error('API Error:', response.status, errorText);
      return { error: errorText || `HTTP error! status: ${response.status}` };
    }

    try {
      const data = await response.json();
      return { data };
    } catch (error) {
      return { data: null as T };
    }
  }

  // Authentication APIs
  async signIn(email: string, password: string): Promise<ApiResponse<any>> {
    try {
      console.log('Attempting backend signin for:', email);
      const response = await fetch(`${API_BASE_URL}/auth/signin`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });

      const result = await this.handleResponse(response);
      if (result.data?.token) {
        localStorage.setItem('auth_token', result.data.token);
        console.log('Backend signin successful, token stored');
      }
      return result;
    } catch (error) {
      console.error('Backend signin failed:', error);
      return { error: 'Network error occurred' };
    }
  }

  async signUp(userData: {
    email: string;
    password: string;
    name: string;
    location: string;
    role: string;
  }): Promise<ApiResponse<any>> {
    try {
      console.log('Attempting backend signup for:', userData.email);
      const response = await fetch(`${API_BASE_URL}/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...userData,
          role: userData.role.toUpperCase()
        })
      });

      return await this.handleResponse(response);
    } catch (error) {
      console.error('Backend signup failed:', error);
      return { error: 'Network error occurred' };
    }
  }

  // Crop APIs
  async getCrops(): Promise<ApiResponse<any[]>> {
    try {
      const response = await fetch(`${API_BASE_URL}/crops`, {
        headers: this.getAuthHeaders()
      });

      const result = await this.handleResponse(response);
      if (result.data) {
        result.data = result.data.map(this.transformCropFromBackend);
      }
      return result;
    } catch (error) {
      console.error('Get crops failed:', error);
      return { error: 'Network error occurred' };
    }
  }

  // Transform backend crop to frontend format
  private transformCropFromBackend(backendCrop: any): any {
    return {
      id: backendCrop.id?.toString() || '',
      name: backendCrop.name || '',
      crop_type: backendCrop.cropType || '',
      harvest_date: backendCrop.harvestDate || '',
      expiry_date: backendCrop.expiryDate || '',
      soil_type: backendCrop.soilType || '',
      pesticides_used: backendCrop.pesticidesUsed || '',
      image_url: backendCrop.imageUrl || '',
      user_id: backendCrop.user?.id?.toString() || '',
      created_at: backendCrop.createdAt || new Date().toISOString(),
      farmer_info: backendCrop.farmerId ? {
        farmer_id: backendCrop.farmerId,
        name: backendCrop.farmerName || '',
        location: backendCrop.farmerLocation || ''
      } : undefined,
      distributor_info: backendCrop.distributorId ? {
        name: backendCrop.distributorName || '',
        location: backendCrop.distributorLocation || '',
        received_date: backendCrop.distributorReceivedDate || '',
        sent_to_retailer: backendCrop.sentToRetailer || '',
        retailer_location: backendCrop.retailerLocation || ''
      } : undefined,
      retailer_info: backendCrop.retailerName ? {
        name: backendCrop.retailerName,
        location: backendCrop.distributorLocationRetailer || '',
        received_date: backendCrop.retailerReceivedDate || '',
        received_from_distributor: backendCrop.receivedFromDistributor || '',
        distributor_location: backendCrop.distributorLocationRetailer || ''
      } : undefined
    };
  }

  async createCrop(cropData: any): Promise<ApiResponse<any>> {
    try {
      console.log('Creating crop with backend data:', cropData);
      const response = await fetch(`${API_BASE_URL}/crops`, {
        method: 'POST',
        headers: this.getAuthHeaders(),
        body: JSON.stringify(cropData)
      });

      const result = await this.handleResponse(response);
      if (result.error) {
        console.error('Backend crop creation failed:', result.error);
      } else {
        console.log('Backend crop creation successful:', result.data);
        if (result.data) {
          result.data = this.transformCropFromBackend(result.data);
        }
      }
      return result;
    } catch (error) {
      console.error('Create crop network error:', error);
      return { error: 'Network error occurred' };
    }
  }

  async updateCrop(cropId: string, cropData: any): Promise<ApiResponse<any>> {
    try {
      console.log('Updating crop', cropId, 'with backend data:', cropData);
      const response = await fetch(`${API_BASE_URL}/crops/${cropId}`, {
        method: 'PUT',
        headers: this.getAuthHeaders(),
        body: JSON.stringify(cropData)
      });

      const result = await this.handleResponse(response);
      if (result.error) {
        console.error('Backend crop update failed:', result.error);
      } else {
        console.log('Backend crop update successful:', result.data);
        if (result.data) {
          result.data = this.transformCropFromBackend(result.data);
        }
      }
      return result;
    } catch (error) {
      console.error('Update crop network error:', error);
      return { error: 'Network error occurred' };
    }
  }

  async deleteCrop(cropId: string): Promise<ApiResponse<any>> {
    try {
      const response = await fetch(`${API_BASE_URL}/crops/${cropId}`, {
        method: 'DELETE',
        headers: this.getAuthHeaders()
      });

      return await this.handleResponse(response);
    } catch (error) {
      return { error: 'Network error occurred' };
    }
  }

  async getCropsByFarmerId(farmerId: string): Promise<ApiResponse<any[]>> {
    try {
      const response = await fetch(`${API_BASE_URL}/crops/farmer/${farmerId}`, {
        headers: this.getAuthHeaders()
      });

      const result = await this.handleResponse(response);
      if (result.data) {
        result.data = result.data.map(this.transformCropFromBackend);
      }
      return result;
    } catch (error) {
      return { error: 'Network error occurred' };
    }
  }

  async getCropsByDistributorId(distributorId: string): Promise<ApiResponse<any[]>> {
    try {
      const response = await fetch(`${API_BASE_URL}/crops/distributor/${distributorId}`, {
        headers: this.getAuthHeaders()
      });

      const result = await this.handleResponse(response);
      if (result.data) {
        result.data = result.data.map(this.transformCropFromBackend);
      }
      return result;
    } catch (error) {
      return { error: 'Network error occurred' };
    }
  }

  async getCropForScanning(cropId: string): Promise<ApiResponse<any>> {
    try {
      const response = await fetch(`${API_BASE_URL}/crops/scan/${cropId}`);
      const result = await this.handleResponse(response);
      if (result.data) {
        result.data = this.transformCropFromBackend(result.data);
      }
      return result;
    } catch (error) {
      return { error: 'Network error occurred' };
    }
  }

  signOut(): void {
    localStorage.removeItem('auth_token');
  }
}

export const apiService = new ApiService();