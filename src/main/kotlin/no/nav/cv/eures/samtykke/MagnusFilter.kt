//package no.nav.cv.eures.samtykke
//
//import io.micronaut.http.HttpRequest
//import io.micronaut.http.HttpStatus
//import io.micronaut.http.MutableHttpResponse
//import io.micronaut.http.filter.ServerFilterChain
//import io.micronaut.http.hateoas.JsonError
//import io.micronaut.http.HttpResponse.status
//import io.micronaut.http.HttpStatus.UNAUTHORIZED
//import io.micronaut.http.annotation.Filter
//import io.micronaut.http.filter.HttpServerFilter
//
//
//import io.reactivex.Flowable
//import io.reactivex.Flowable.fromCallable
//import io.reactivex.Flowable.just
//import io.reactivex.schedulers.Schedulers.io
//import org.reactivestreams.Publisher
//import org.slf4j.LoggerFactory
//import javax.inject.Singleton
//
//@Filter("/internal/**")
//class MagnusFilter(
//        private val authService: AuthService
//) : HttpServerFilter {
//
//    override fun doFilter(request: HttpRequest<*>?, chain: ServerFilterChain?): Publisher<MutableHttpResponse<*>>
//            = authService.validRequest(request)
//            .subscribeOn(io())
//            .switchMap { valid ->
//                if(valid)
//                    chain?.proceed(request) as Publisher<MutableHttpResponse<*>>
//                else
//                    notValid()
//            }
//
//    private fun notValid() : Flowable<MutableHttpResponse<JsonError>> {
//        return fromCallable {
//            val body = JsonError("Auth failed")
//            status<JsonError>(UNAUTHORIZED).body(body)
//        }.subscribeOn(io())
//    }
//}
//
//
//interface AuthService {
//    fun validRequest(request: HttpRequest<*>?) : Flowable<Boolean>
//}
//
//@Singleton
//class AuthServiceImplementation : AuthService {
//    companion object {
//        val log = LoggerFactory.getLogger(AuthService::class.java)
//    }
//
//    override fun validRequest(request: HttpRequest<*>?): Flowable<Boolean>
//            = just(request?.parameters?.contains("allow") == true)
//}