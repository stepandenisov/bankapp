package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Неуспешное пополнение — сервис вернул false"

    request {
        method POST()
        urlPath('/top-up/123') {
        }
        headers {
            contentType(applicationJson())
        }
        body(
                amount: -100,
                currency: "RUB"
        )
    }

    response {
        status BAD_REQUEST()
    }
}
