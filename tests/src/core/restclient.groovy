package com.slist.apitests.core

import org.apache.http.client.utils.URLEncodedUtils

import java.nio.charset.Charset
import java.net.URI
import java.net.URLEncoder
import java.net.URISyntaxException

import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory

import org.apache.http.conn.ssl.SSLContextBuilder
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.conn.ssl.SSLSocketFactory

import groovy.json.JsonSlurper
import groovy.json.JsonBuilder 
import groovy.json.JsonOutput

import org.testng.Reporter

class MyHttpClientErrorException extends HttpClientErrorException {
	public MyHttpClientErrorException(HttpStatus statusCode, String statusText,
                              HttpHeaders responseHeaders, byte[] responseBody, 
                              Charset responseCharset) {
		super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
        
        def resp_body = getResponseBodyAsString()
        def resp_headers = getResponseHeaders()
        // Reporter.log("Response:\nheaders ($resp_headers),\nbody ($resp_body)\n")
        MyRestTemplate.log_response(resp_body, statusCode.value(), statusText)
	}
}

class HttpBadRequestException extends MyHttpClientErrorException {
	public HttpBadRequestException(HttpStatus statusCode, String statusText,
                              HttpHeaders responseHeaders, byte[] responseBody, 
                              Charset responseCharset) {
		super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
	}
}

class HttpUnauthenticatedException extends MyHttpClientErrorException {
	public HttpUnauthenticatedException(HttpStatus statusCode, String statusText,
                              HttpHeaders responseHeaders, byte[] responseBody, 
                              Charset responseCharset) {
		super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
	}
}

class HttpAuthorizationException extends MyHttpClientErrorException {
	public HttpAuthorizationException(HttpStatus statusCode, String statusText,
                              HttpHeaders responseHeaders, byte[] responseBody, 
                              Charset responseCharset) {
		super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
	}
}

class HttpNotFoundException extends MyHttpClientErrorException {
	public HttpNotFoundException(HttpStatus statusCode, String statusText,
                                 HttpHeaders responseHeaders, byte[] responseBody, 
                                 Charset responseCharset) {
		super(statusCode, statusText, responseHeaders, responseBody, responseCharset);
	}
}

class MyResponseErrorHandler extends DefaultResponseErrorHandler {
    public void handleError(ClientHttpResponse response) throws IOException {
        def statusCode = response.getStatusCode()
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new HttpUnauthenticatedException(statusCode, response.getStatusText(),
                                         response.getHeaders(), super.getResponseBody(response), 
                                         super.getCharset(response));
        } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
            throw new HttpBadRequestException(statusCode, response.getStatusText(),
                                         response.getHeaders(), super.getResponseBody(response), 
                                         super.getCharset(response));
        } else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            throw new HttpAuthorizationException(statusCode, response.getStatusText(),
                                         response.getHeaders(), super.getResponseBody(response), 
                                         super.getCharset(response));
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new HttpNotFoundException(statusCode, response.getStatusText(),
                                            response.getHeaders(), super.getResponseBody(response), 
                                            super.getCharset(response));
        } else {
            super.handleError(response)
        }
    }
}

class MyRestTemplate extends RestTemplate {
    public MyRestTemplate() {
        super()
    }

    public MyRestTemplate(HttpComponentsClientHttpRequestFactory httpRequestFactory) {
        super(httpRequestFactory)
    }

    static void log_request(String url, String method, HttpEntity<?> requestEntity) {
        def msg = "\nRequest: \n----------\n${method} $url\n"

        if (requestEntity != null) {
            def req_body = requestEntity.getBody()
            if (req_body != null) {
                try {
                    req_body = JsonOutput.prettyPrint(req_body)
                } catch (Exception e) {
                    // Ignore any exceptions. 
                }
                msg = msg + "\n${req_body}\n"
            }
        }

        Reporter.log(msg)
    }

    static void log_response(resp_body, status_code, status_text) {
        def msg = "\nResponse: \n" + status_code + " " + status_text + "\n"

        if (resp_body != null) {
            try {
                resp_body = JsonOutput.prettyPrint(resp_body)
            } catch (Exception e) {
                // Ignore any exceptions. 
            }
            msg = msg + "\n${resp_body}\n"
        }

        Reporter.log(msg)
    }

    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity,
                        Class<T> responseType) throws RestClientException {

        def req_body = requestEntity.getBody()
        def req_headers = requestEntity.getHeaders()
        // Reporter.log("Request:\nURL ($url),\nheaders ($req_headers),\nbody ($req_body)\n")
        log_request(url, method.toString(), requestEntity)

        def response = super.exchange(url, method, requestEntity, responseType);

        def resp_body = response.getBody();
        def resp_headers = response.getHeaders()
        def status = response.getStatusCode()
        // Reporter.log("Response:\nheaders ($resp_headers),\nbody ($resp_body)\n")
        log_response(resp_body, status.value(), status.getReasonPhrase())

        return response
    }

    public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
        log_request(url.toString(), "GET", null)
        // Reporter.log("Request:\nURL ($url)\n")
        def response = super.getForObject(url, responseType);

        // Reporter.log("Response:\nbody ($response)\n")
        log_response(response, 200, "OK")

        return response
    }
}

class RestClient {
    static def TOPURL =  System.getProperty("apitests.serverURL", "http://localhost:8080")

    static def TOPURL_API = TOPURL + "/api"
    static def HEADER_AUTHORIZATION = "Authorization"
    static def HEADER_CONTENT_TYPE = "Content-Type"
    static def HEADER_ACCEPT = "Accept"
    static def HEADER_LOCATION = "Location"
    static def CONTENT_TYPE_JSON = "application/json"

    def restTemplate = createRestTemplate()

    static def nativeadmin_sessionid

    def createRestTemplate() {
        SSLContextBuilder builder = new SSLContextBuilder()
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy())
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build()
        
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient)
        
        restTemplate = new MyRestTemplate(httpRequestFactory)
        restTemplate.setErrorHandler(new MyResponseErrorHandler())
        
        return restTemplate
    }

    def get_headers(session_id) {
        def headers = new LinkedMultiValueMap<String, String>()
        headers.add(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
        headers.add(HEADER_ACCEPT, CONTENT_TYPE_JSON)

        def request = new HttpEntity<Object>(headers)

        return [headers, request]
    }

    def get_uri(url, queryStr) {
        def uri = new URI(url)

        if ((uri.getQuery() != null) && queryStr) {
            queryStr = uri.getQuery() + "&" + queryStr
        }
        
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                       uri.getPort(), uri.getPath(), queryStr, uri.getFragment())
    }

    def post(url, data) {
        def (headers, request) = get_headers()
        request = new HttpEntity<String>(JsonOutput.toJson(data), headers)
            
        def response = restTemplate.exchange(url, HttpMethod.POST, request, String.class)
        return new JsonSlurper().parseText(response.getBody())
    }

    def get(url) {
        return new JsonSlurper().parseText(get_body(url))
    }
    
    def get_body(url) {
        def (headers, request) = get_headers()
        def response = getRestTemplate().exchange(url, HttpMethod.GET, request, String.class)

        return response.getBody()
    }

    def delete(url) {
        def (headers, request) = get_headers()
        getRestTemplate().exchange(url, HttpMethod.DELETE, request, String.class)
    }

    def put(url, data) {
        def (headers, request) = get_headers()
        request = new HttpEntity<Object>(JsonOutput.toJson(data), headers)
        def response = getRestTemplate().exchange(url, HttpMethod.PUT, request, String.class)
        return new JsonSlurper().parseText(response.getBody())
    }
}

class StoresAPI extends RestClient {
    static def TOPURL_STORES = TOPURL_API + "/stores"

    def create(data) {
        def resp = post(TOPURL_STORES, data)
        list()
        return resp
    }

    def update(storeId, data) {
        def resp = put("${TOPURL_STORES}/$storeId", data)
        list()
        return resp
    }

    def list() {
        return super.get(TOPURL_STORES).stores
    }

    def delete(storeId) {
        def resp = super.delete("${TOPURL_STORES}/$storeId")
        list()
        return resp
    }

    def get(storeId) {
        return super.get("${TOPURL_STORES}/$storeId")
    }
}

class CategoriesAPI extends RestClient {
    static def TOPURL_CATEGORIES = TOPURL_API + "/categories"

    def create(data) {
        def resp = post(TOPURL_CATEGORIES, data)
        list()
        return resp
    }

    def update(categoryId, data) {
        def resp = put("${TOPURL_CATEGORIES}/$categoryId", data)
        list()
        return resp
    }

    def list() {
        return super.get(TOPURL_CATEGORIES).categories
    }

    def delete(categoryId) {
        def resp = super.delete("${TOPURL_CATEGORIES}/$categoryId")
        list()
        return resp
    }

    def get(categoryId) {
        return super.get("${TOPURL_CATEGORIES}/$categoryId")
    }
}

class ItemsAPI extends RestClient {
    static def TOPURL_ITEMS = TOPURL_API + "/items"

    def create(data) {
        def resp = post(TOPURL_ITEMS, data)
        list()
        return resp
    }

    def update(itemId, data) {
        def resp = put("${TOPURL_ITEMS}/$itemId", data)
        list()
        return resp
    }

    def update_multiple(data) {
        def resp = put(TOPURL_ITEMS, data)
        list()
        return resp.items
    }

    def list(queryStr=null) {
        def url = TOPURL_ITEMS
        if (queryStr) {
            url = url + "?" + queryStr
        }

        return super.get(url).items
    }

    def delete(itemId) {
        def resp = super.delete("${TOPURL_ITEMS}/$itemId")
        list()
        return resp
    }

    def get(itemId=null, queryStr=null) {
        def url = TOPURL_ITEMS

        if (itemId) {
            url = "${url}/$itemId"
        }

        if (queryStr) {
            url = url + "?" + queryStr
        }

        return super.get(url)
    }
}


