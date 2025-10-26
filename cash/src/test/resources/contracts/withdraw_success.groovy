package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Успешное снятие денег со счета"

    request {
        method POST()
        urlPath('/withdraw/123') {
        }
        headers {
            contentType(applicationJson())
        }
        body(
                amount: 500,
                currency: "RUB"
        )
    }

    response {
        status OK()
    }
}
