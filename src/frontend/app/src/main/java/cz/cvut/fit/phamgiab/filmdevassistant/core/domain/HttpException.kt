package cz.cvut.fit.phamgiab.filmdevassistant.core.domain

class HttpException(val statusCode: Int, val bodyAsText: String) : Exception () {
    override val message = "statusCode=$statusCode, bodyAsText=$bodyAsText"
}