package com.simats.fuelonwheels.api

import com.simats.fuelonwheels.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ============ AUTHENTICATION APIs ============

    @POST("auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<MessageResponse>

    @POST("auth/verify_otp.php")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/forgot_password.php")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/reset_password.php")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    // ============ PROFILE APIs ============

    @GET("profile/get_profile.php")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @POST("profile/update_profile.php")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<MessageResponse>

    @Multipart
    @POST("profile/upload_image.php")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Query("image_type") imageType: String,
        @Part image: MultipartBody.Part
    ): Response<UploadImageResponse>

    // ============ USER APIs ============

    @POST("user/update_location.php")
    suspend fun updateLocation(
        @Header("Authorization") token: String,
        @Body request: LocationRequest
    ): Response<MessageResponse>

    @GET("user/get_nearby_shops.php")
    suspend fun getNearbyShops(
        @Header("Authorization") token: String,
        @Query("service_type") serviceType: String,
        @Query("radius") radius: Int = 10
    ): Response<NearbyShopsResponse>

    @POST("user/create_service_request.php")
    suspend fun createServiceRequest(
        @Header("Authorization") token: String,
        @Body request: CreateServiceRequest
    ): Response<CreateServiceResponse>

    @GET("user/get_order_history.php")
    suspend fun getOrderHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<OrderHistoryResponse>

    @GET("user/get_order_status.php")
    suspend fun getOrderStatus(
        @Header("Authorization") token: String,
        @Query("order_id") orderId: Int
    ): Response<OrderStatusResponse>

    @POST("user/make_payment.php")
    suspend fun makePayment(
        @Header("Authorization") token: String,
        @Body request: PaymentRequest
    ): Response<PaymentResponse>

    @POST("user/give_rating.php")
    suspend fun giveRating(
        @Header("Authorization") token: String,
        @Body request: RatingRequest
    ): Response<RatingResponse>

    // ============ MECHANIC APIs ============

    @POST("mechanic/register_shop.php")
    suspend fun registerShop(
        @Header("Authorization") token: String,
        @Body request: RegisterShopRequest
    ): Response<RegisterShopResponse>

    @POST("mechanic/add_service.php")
    suspend fun addService(
        @Header("Authorization") token: String,
        @Body request: AddServiceRequest
    ): Response<AddServiceResponse>

    @POST("mechanic/accept_reject_request.php")
    suspend fun acceptRejectRequest(
        @Header("Authorization") token: String,
        @Body request: AcceptRejectRequest
    ): Response<MessageResponse>

    @POST("mechanic/complete_request.php")
    suspend fun completeRequest(
        @Header("Authorization") token: String,
        @Body request: CompleteRequestBody
    ): Response<MessageResponse>

    @GET("mechanic/dashboard.php")
    suspend fun getMechanicDashboard(
        @Header("Authorization") token: String
    ): Response<MechanicDashboardResponse>

    // ============ FUEL OWNER APIs ============

    @POST("owner/register_bunk.php")
    suspend fun registerBunk(
        @Header("Authorization") token: String,
        @Body request: RegisterBunkRequest
    ): Response<RegisterBunkResponse>

    @POST("owner/add_fuel_price.php")
    suspend fun addFuelPrice(
        @Header("Authorization") token: String,
        @Body request: AddFuelPriceRequest
    ): Response<AddFuelPriceResponse>

    @POST("owner/complete_fuel_request.php")
    suspend fun completeFuelRequest(
        @Header("Authorization") token: String,
        @Body request: CompleteFuelRequest
    ): Response<MessageResponse>

    @GET("owner/dashboard.php")
    suspend fun getOwnerDashboard(
        @Header("Authorization") token: String
    ): Response<OwnerDashboardResponse>

    @GET("owner/view_service.php")
    suspend fun getServices(
        @Header("Authorization") token: String
    ): Response<ServiceListResponse>

    @GET("owner/view_service.php")
    suspend fun getFuelPrices(
        @Header("Authorization") token: String
    ): Response<FuelPriceListResponse>


    // ============ NEW APIs FOR REQUEST MANAGEMENT & LOCATION TRACKING ============

    // Mechanic APIs
    @GET("mechanic/view_requests.php")
    suspend fun viewMechanicRequests(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ViewRequestsResponse>

    @GET("mechanic/track_user_location.php")
    suspend fun trackMechanicUserLocation(
        @Header("Authorization") token: String,
        @Query("request_id") requestId: Int
    ): Response<TrackLocationResponse>

    @POST("mechanic/update_location.php")
    suspend fun updateMechanicLocation(
        @Header("Authorization") token: String,
        @Body request: UpdateLocationRequest
    ): Response<MessageResponse>

    // Owner APIs
    @GET("owner/view_requests.php")
    suspend fun viewOwnerRequests(
        @Header("Authorization") token: String,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ViewRequestsResponse>

    @GET("owner/track_user_location.php")
    suspend fun trackOwnerUserLocation(
        @Header("Authorization") token: String,
        @Query("request_id") requestId: Int
    ): Response<TrackLocationResponse>

    @POST("owner/update_location.php")
    suspend fun updateOwnerLocation(
        @Header("Authorization") token: String,
        @Body request: UpdateLocationRequest
    ): Response<MessageResponse>

    @POST("owner/accept_reject_request.php")
    suspend fun acceptRejectFuelRequest(
        @Header("Authorization") token: String,
        @Body request: AcceptRejectRequest
    ): Response<MessageResponse>

    // User APIs for tracking service providers


    // User APIs for tracking service providers
    @GET("user/track_mechanic_location.php")
    suspend fun trackMechanicLocation(
        @Header("Authorization") token: String,
        @Query("request_id") requestId: Int
    ): Response<TrackLocationResponse>

    @GET("user/track_owner_location.php")
    suspend fun trackOwnerLocation(
        @Header("Authorization") token: String,
        @Query("request_id") requestId: Int
    ): Response<TrackLocationResponse>
}
