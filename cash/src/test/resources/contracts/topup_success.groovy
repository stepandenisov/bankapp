package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Успешное пополнение счета"

    request {
        method POST()
        urlPath('/top-up/123') {
        }
        headers {
            contentType(applicationJson())
        }
        body(
                amount: 1000,
                currency: "RUB"
        )
    }

    response {
        status OK()
    }
}
