package com.aranza.directorio

data class Geo(val lat: String, val lng: String)
data class Address(val street: String, val suite: String, val city: String,
                   val zipcode: String, val geo: Geo)
data class Company(val name: String, val catchPhrase: String, val bs: String)
data class User(val id: Int, val name: String, val username: String,
                val email: String, val phone: String, val website: String,
                val address: Address, val company: Company)
data class Post(val userId: Int, val id: Int, val title: String, val body: String)

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Success<T>(val data: T) : LoadState<T>
    data class Error(val message: String) : LoadState<Nothing>
}
