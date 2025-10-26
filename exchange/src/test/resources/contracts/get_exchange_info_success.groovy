package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Конвертирует USD в RUB"
    request {
        method GET()
        urlPath("/rate/convert") {
            queryParameters {
                parameter("from", "USD")
                parameter("to", "RUB")
                parameter("amount", "100")
            }
        }
    }
    response {
        status 200
        body(
                currency: "RUB",
                value: 9000.0
        )
        headers {
            contentType(applicationJson())
        }
    }
}
