package translator

import client.translator.TranslationRequest
import client.translator.TranslationResponse
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.io.ClassPathResource
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload
import org.springframework.ws.transport.http.MessageDispatcherServlet
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition
import org.springframework.xml.xsd.SimpleXsdSchema

/** The namespace of the payload root element. */
private const val TRANSLATOR_NAMESPACE_URI = "http://translator/web/ws/schema"

/**
 * The translator endpoint, which is reached by the [MessageDispatcherServlet] when incoming XML are
 * received
 */
@Endpoint
class TranslatorEndpoint {
    /** [localpart]: specifies what kind of XML messages are handled by the endpoint */
    @PayloadRoot(namespace = TRANSLATOR_NAMESPACE_URI, localPart = "TranslationRequest")
    /**
     * Receives [request] and translate [request.text] in order to send the translation back to the
     * client as a [resp]
     */
    @ResponsePayload
    fun translation(@RequestPayload request: TranslationRequest): TranslationResponse {
        var resp = TranslationResponse()
        resp.translation =
                LanguageTranslator.getTranslation(request.langFrom, request.langTo, request.text)
        return resp
    }
}

@EnableWs
@SpringBootApplication
class Server {

    /**
     * This bean detects automatically WSDL definitions and endpoints.
     *
     * The [Wsdl11Definition] bean named [translator] will be exposed as `translator.wsdl` in the
     * context `/ws/translator.wsdl`
     */
    @Bean
    fun messageDispatcherServlet(applicationContext: ApplicationContext) =
            MessageDispatcherServlet()
                    .apply {
                        setApplicationContext(applicationContext)
                        isTransformWsdlLocations = true
                    }
                    .let { ServletRegistrationBean(it, "/ws/*") }

    /** Creates the wsdl definition for the namespace [TRANSLATOR_NAMESPACE_URI]. */
    @Bean
    fun translator(): Wsdl11Definition =
            DefaultWsdl11Definition().apply {
                setPortTypeName("TranslationPort")
                setLocationUri("/ws")
                setTargetNamespace(TRANSLATOR_NAMESPACE_URI)
                setSchema(translatorSchema())
            }

    /** Loads the schema. */
    @Bean fun translatorSchema() = SimpleXsdSchema(ClassPathResource("/ws/translator.xsd"))
}

/** The main entry point. */
fun main(args: Array<String>) {
    runApplication<Server>(*args)
}
