package com.simats.fuelonwheels.repository

import com.simats.fuelonwheels.api.ApiService
import com.simats.fuelonwheels.models.*
import okhttp3.MultipartBody
import retrofit2.Response

class ApiRepository(private val apiService: ApiService) {

    // ============ AUTHENTICATION ============

    suspend fun register(request: RegisterRequest): Response<MessageResponse> {
        return apiService.register(request)
    }

    suspend fun verifyOtp(request: VerifyOtpRequest): Response<VerifyOtpResponse> {
        return apiService.verifyOtp(request)
    }

    suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return apiService.login(request)
    }

    suspend fun forgotPassword(request: ForgotPasswordRequest): Response<MessageResponse> {
        return apiService.forgotPassword(request)
    }

    suspend fun resetPassword(request: ResetPasswordRequest): Response<MessageResponse> {
        return apiService.resetPassword(request)
    }

    // ============ PROFILE ============

    suspend fun getProfile(token: String): Response<ProfileResponse> {
        return apiService.getProfile("Bearer $token")
    }

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): Response<MessageResponse> {
        return apiService.updateProfile("Bearer $token", request)
    }

    suspend fun uploadImage(token: String, imageType: String, image: MultipartBody.Part): Response<UploadImageResponse> {
        return apiService.uploadImage("Bearer $token", imageType, image)
    }

    // ============ USER ============

    suspend fun updateLocation(token: String, latitude: Double, longitude: Double): Response<MessageResponse> {
        return apiService.updateLocation("Bearer $token", LocationRequest(latitude, longitude))
    }

    suspend fun getNearbyShops(token: String, serviceType: String, radius: Int = 10): Response<NearbyShopsResponse> {
        return apiService.getNearbyShops("Bearer $token", serviceType, radius)
    }

    suspend fun createServiceRequest(token: String, request: CreateServiceRequest): Response<CreateServiceResponse> {
        return apiService.createServiceRequest("Bearer $token", request)
    }

    suspend fun getOrderHistory(token: String, page: Int = 1, limit: Int = 10): Response<OrderHistoryResponse> {
        return apiService.getOrderHistory("Bearer $token", page, limit)
    }

    suspend fun getOrderStatus(token: String, orderId: Int): Response<OrderStatusResponse> {
        return apiService.getOrderStatus("Bearer $token", orderId)
    }

    suspend fun makePayment(token: String, request: PaymentRequest): Response<PaymentResponse> {
        return apiService.makePayment("Bearer $token", request)
    }

    suspend fun giveRating(token: String, request: RatingRequest): Response<RatingResponse> {
        return apiService.giveRating("Bearer $token", request)
    }

    // ============ MECHANIC ============

    suspend fun registerShop(token: String, request: RegisterShopRequest): Response<RegisterShopResponse> {
        return apiService.registerShop("Bearer $token", request)
    }

    suspend fun addService(token: String, request: AddServiceRequest): Response<AddServiceResponse> {
        return apiService.addService("Bearer $token", request)
    }

    suspend fun acceptRejectRequest(token: String, request: AcceptRejectRequest): Response<MessageResponse> {
        return apiService.acceptRejectRequest("Bearer $token", request)
    }

    suspend fun completeRequest(token: String, request: CompleteRequestBody): Response<MessageResponse> {
        return apiService.completeRequest("Bearer $token", request)
    }

    suspend fun getMechanicDashboard(token: String): Response<MechanicDashboardResponse> {
        return apiService.getMechanicDashboard("Bearer $token")
    }

    // ============ FUEL OWNER ============

    suspend fun registerBunk(token: String, request: RegisterBunkRequest): Response<RegisterBunkResponse> {
        return apiService.registerBunk("Bearer $token", request)
    }

    suspend fun addFuelPrice(token: String, request: AddFuelPriceRequest): Response<AddFuelPriceResponse> {
        return apiService.addFuelPrice("Bearer $token", request)
    }

    suspend fun completeFuelRequest(token: String, request: CompleteFuelRequest): Response<MessageResponse> {
        return apiService.completeFuelRequest("Bearer $token", request)
    }
    suspend fun getServices(token: String): Response<ServiceListResponse> {
        return apiService.getServices("Bearer $token")
    }

    suspend fun getFuelPrices(token: String): Response<FuelPriceListResponse> {
        return apiService.getFuelPrices("Bearer $token")
    }

    suspend fun getOwnerDashboard(token: String): Response<OwnerDashboardResponse> {
        return apiService.getOwnerDashboard("Bearer $token")
    }

    // ============ NEW REQUEST MANAGEMENT & LOCATION TRACKING APIs ============

    // Mechanic APIs
    suspend fun viewMechanicRequests(
        token: String,
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Response<ViewRequestsResponse> {
        return apiService.viewMechanicRequests("Bearer $token", status, page, limit)
    }

    suspend fun trackMechanicUserLocation(
        token: String,
        requestId: Int
    ): Response<TrackLocationResponse> {
        return apiService.trackMechanicUserLocation("Bearer $token", requestId)
    }

    suspend fun updateMechanicLocation(
        token: String,
        request: UpdateLocationRequest
    ): Response<MessageResponse> {
        return apiService.updateMechanicLocation("Bearer $token", request)
    }

    // Owner APIs
    suspend fun viewOwnerRequests(
        token: String,
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Response<ViewRequestsResponse> {
        return apiService.viewOwnerRequests("Bearer $token", status, page, limit)
    }

    suspend fun trackOwnerUserLocation(
        token: String,
        requestId: Int
    ): Response<TrackLocationResponse> {
        return apiService.trackOwnerUserLocation("Bearer $token", requestId)
    }

    suspend fun updateOwnerLocation(
        token: String,
        request: UpdateLocationRequest
    ): Response<MessageResponse> {
        return apiService.updateOwnerLocation("Bearer $token", request)
    }

    suspend fun acceptRejectFuelRequest(
        token: String,
        request: AcceptRejectRequest
    ): Response<MessageResponse> {
        return apiService.acceptRejectFuelRequest("Bearer $token", request)
    }

    // User APIs for tracking service providers
    suspend fun trackMechanicLocation(
        token: String,
        requestId: Int
    ): Response<TrackLocationResponse> {
        return apiService.trackMechanicLocation("Bearer $token", requestId)
    }

    suspend fun trackOwnerLocation(
        token: String,
        requestId: Int
    ): Response<TrackLocationResponse> {
        return apiService.trackOwnerLocation("Bearer $token", requestId)
    }
}
