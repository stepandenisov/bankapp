package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Неуспешное снятие денег — недостаточно средств"

    request {
        method POST()
        urlPath('/withdraw/123') {
        }
        headers {
            contentType(applicationJson())
        }
        body(
                amount: 999999,
                currency: "RUB"
        )
    }

    response {
        status BAD_REQUEST()
    }
}
