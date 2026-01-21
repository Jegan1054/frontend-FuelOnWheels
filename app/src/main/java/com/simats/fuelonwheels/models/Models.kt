package com.simats.fuelonwheels.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName

import kotlinx.parcelize.Parcelize

// ============ REQUEST MODELS ============

data class RegisterRequest(
    val email: String,
    val password: String,
    val role: String,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val phone: String? = null
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val otp: String,
    @SerializedName("new_password") val newPassword: String
)

data class LocationRequest(
    val latitude: Double,
    val longitude: Double
)

data class CreateServiceRequest(
    @SerializedName("shop_id") val shopId: Int,
    @SerializedName("service_id") val serviceId: Int,
    val description: String? = null
)

data class PaymentRequest(
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("payment_method") val paymentMethod: String
)

data class RatingRequest(
    @SerializedName("order_id") val orderId: Int,
    val rating: Int,
    val review: String? = null
)

data class RegisterShopRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int
)

data class AddServiceRequest(
    val name: String,
    val price: Double
)

data class AcceptRejectRequest(
    @SerializedName("request_id") val requestId: Int,
    val action: String
)

data class CompleteRequestBody(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("final_amount") val finalAmount: Double
)

data class RegisterBunkRequest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int
)

data class AddFuelPriceRequest(
    val name: String,
    val price: Double
)

data class CompleteFuelRequest(
    @SerializedName("request_id") val requestId: Int,
    @SerializedName("final_amount") val finalAmount: Double,
    val liters: Double
)

data class UpdateProfileRequest(
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val phone: String? = null,
    val shop: ShopUpdateData? = null
)

data class ShopUpdateData(
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Int? = null
)

// ============ RESPONSE MODELS ============

data class MessageResponse(
    val message: String? = null,
    val error: String? = null
)

data class VerifyOtpResponse(
    val message: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val user_role: String? = null,
    val token: String? = null,
    val error: String? = null
)

data class LoginResponse(
    val message: String? = null,
    val user: User? = null,
    val token: String? = null,
    val error: String? = null
)

data class ProfileResponse(
    val user: User? = null,
    val shop: Shop? = null,
    val error: String? = null
)

data class UploadImageResponse(
    val message: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    val error: String? = null
)

data class NearbyShopsResponse(
    val shops: List<NearbyShop>? = null,
    val error: String? = null
)

data class CreateServiceResponse(
    val message: String? = null,
    @SerializedName("request_id") val requestId: Int? = null,
    val error: String? = null
)

data class OrderHistoryResponse(   // ← Must be Boolean
    val message: String? = null,
    val orders: List<Order>? = null,
    val pagination: Pagination? = null,
    val error: String? = null
)

data class OrderStatusResponse(
    val order: OrderDetail? = null,
    val error: String? = null
)

data class PaymentResponse(
    val message: String? = null,
    @SerializedName("payment_id") val paymentId: Int? = null,
    val status: String? = null,
    val method: String? = null,
    val amount: Double? = null,
    val error: String? = null
)

data class RatingResponse(
    val message: String? = null,
    val rating: Int? = null,
    val review: String? = null,
    val error: String? = null
)

data class RegisterShopResponse(
    val message: String? = null,
    @SerializedName("shop_id") val shopId: Int? = null,
    val error: String? = null
)

data class AddServiceResponse(
    val message: String? = null,
    @SerializedName("service_id") val serviceId: Int? = null,
    val error: String? = null
)

data class MechanicDashboardResponse(
    val dashboard: MechanicDashboard? = null,
    val error: String? = null
)

data class RegisterBunkResponse(
    val message: String? = null,
    @SerializedName("bunk_id") val bunkId: Int? = null,
    val error: String? = null
)

data class AddFuelPriceResponse(
    val message: String? = null,
    @SerializedName("service_id") val serviceId: Int? = null,
    val error: String? = null
)

data class OwnerDashboardResponse(
    val dashboard: OwnerDashboard? = null,
    val error: String? = null
)

// ============ DATA CLASSES ============

data class User(
    val id: Int,
    val email: String,
    val role: String,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    val phone: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class Shop(
    val id: Int,
    val name: String,
    val type: String,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
    @SerializedName("shop_image") val shopImage: String? = null,
    @SerializedName("total_orders") val totalOrders: Int? = null,
    @SerializedName("avg_rating") val avgRating: Double? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

@Parcelize
data class NearbyShop(
    val id: Int,
    val name: String,
    val distance: Double,
    val avgRating: Double?,
    val services: List<String>,
    val serviceType: String
) : Parcelable


data class Order(
    val id: Int,
    @SerializedName("shop_name") val shopName: String,
    @SerializedName("shop_type") val shopType: String,
    @SerializedName("service_name") val serviceName: String,
    @SerializedName("service_price") val servicePrice: Double,
    val description: String? = null,
    val status: String,
    @SerializedName("final_amount") val finalAmount: Double? = null,
    val liters: Double? = null,
    @SerializedName("requested_at") val requestedAt: String,
    @SerializedName("completed_at") val completedAt: String? = null,
    val payment: PaymentInfo? = null,
    val rating: RatingInfo? = null
)

data class OrderDetail(
    val id: Int,
    val shop: ShopInfo,
    val service: ServiceInfo,
    val description: String? = null,
    val status: String,
    @SerializedName("final_amount") val finalAmount: Double? = null,
    val liters: Double? = null,
    @SerializedName("requested_at") val requestedAt: String,
    @SerializedName("completed_at") val completedAt: String? = null,
    val payment: PaymentInfo? = null,
    val rating: RatingInfo? = null
)

data class ShopInfo(
    val id: Int,
    val name: String,
    val type: String,
    val location: Location
)

data class Location(
    val latitude: Double,
    val longitude: Double
)

data class ServiceInfo(
    val name: String,
    val price: Double
)
// Existing items
data class ServiceItem(
    val id: String,
    val name: String,
    val price: String // Note: your API sends price as STRING "500.00"
)

data class FuelItem(
    val id: String,
    val name: String,
    val price: String
)

// NEW: Wrapper responses
data class ServiceListResponse(
    val message: String,
    val services: List<ServiceItem>
)

data class FuelPriceListResponse(
    val message: String,
    val services: List<FuelItem> // or "fuel_prices", match your API key
)

data class PaymentInfo(
    val amount: Double,
    val method: String,
    val status: String
)

data class RatingInfo(
    val stars: Int,
    val review: String? = null
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

data class MechanicDashboard(
    @SerializedName("pending_orders") val pendingOrders: Int,
    @SerializedName("total_delivered") val totalDelivered: Int,
    @SerializedName("total_earnings") val totalEarnings: Double,
    @SerializedName("total_orders") val totalOrders: Int,
    val rating: Double
)

data class OwnerDashboard(
    @SerializedName("pending_orders") val pendingOrders: Int,
    @SerializedName("total_delivered") val totalDelivered: Int,
    @SerializedName("total_earnings") val totalEarnings: Double,
    @SerializedName("total_orders") val totalOrders: Int,
    val rating: Double,
    @SerializedName("total_liters_delivered") val totalLitersDelivered: Double
)

// ============ NEW MODELS FOR REQUEST MANAGEMENT & LOCATION TRACKING ============

data class ViewRequestsResponse(
    val success: Boolean? = null,
    val data: ViewRequestsData? = null,
    val error: String? = null
)

data class ViewRequestsData(
    val requests: List<ServiceRequestItem>? = null,
    val pagination: Pagination? = null
)

data class ServiceRequestItem(
    val id: Int,
    val user: ServiceRequestUser? = null,
    @SerializedName("fuel_type") val fuelType: String? = null,
    @SerializedName("service_name") val serviceName: String? = null,
    val description: String? = null,
    @SerializedName("estimated_price") val estimatedPrice: Double? = null,
    @SerializedName("final_price") val finalPrice: Double? = null,
    val status: String,
    @SerializedName("user_location") val userLocation: LocationData? = null,
    @SerializedName("quantity") val quantity: Double? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

data class ServiceRequestUser(
    val id: Int,
    val name: String,
    val phone: String
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

data class TrackLocationResponse(
    val success: Boolean? = null,
    @SerializedName("request_id") val requestId: Int? = null,
    val user: TrackLocationUser? = null,
    val mechanic: TrackLocationUser? = null,
    @SerializedName("delivery_personnel") val deliveryPersonnel: TrackLocationUser? = null,
    val service: TrackLocationService? = null,
    @SerializedName("fuel_request") val fuelRequest: TrackLocationFuelRequest? = null,
    @SerializedName("current_location") val currentLocation: CurrentLocation? = null,
    @SerializedName("mechanic_location") val mechanicLocation: CurrentLocation? = null,
    @SerializedName("delivery_location") val deliveryLocation: CurrentLocation? = null,
    @SerializedName("distance_from_shop") val distanceFromShop: Double? = null,
    @SerializedName("distance_from_bunk") val distanceFromBunk: Double? = null,
    @SerializedName("distance_to_user") val distanceToUser: Double? = null,
    @SerializedName("estimated_delivery_time") val estimatedDeliveryTime: Int? = null,
    @SerializedName("estimated_arrival_time") val estimatedArrivalTime: Int? = null,
    val unit: String? = null,
    @SerializedName("time_unit") val timeUnit: String? = null,
    val error: String? = null
)

data class TrackLocationUser(
    val name: String,
    val phone: String,
    @SerializedName("shop_name") val shopName: String? = null,
    @SerializedName("fuel_bunk_name") val fuelBunkName: String? = null
)

data class TrackLocationService(
    val type: String,
    val name: String,
    val price: Double,
    val status: String
)

data class TrackLocationFuelRequest(
    @SerializedName("fuel_type") val fuelType: String,
    val quantity: Double,
    val status: String
)

data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("last_updated") val lastUpdated: String
)

data class UpdateLocationRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("request_id") val requestId: Int? = null
)