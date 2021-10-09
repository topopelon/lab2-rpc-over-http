## Harcoded server response

Client makes a translation request to the server.

```
TranslationRequest().apply {
                    langFrom = "en"
                    langTo = "es"
                    text = input
                }
```

Server receives the xml request and the message dispatcher `MessageDispatcherServlet` 
search the endpoint which is handleling the request. In our case, `TranslatorEndpoint` handles
the request and return a hardcoded response through `TranslationResponse`.

```
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

```
