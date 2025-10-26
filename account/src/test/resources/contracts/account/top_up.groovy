package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Пополнение счёта"

    request {
        method POST()
        url("/accounts/1/top-up")
        headers {
            contentType(applicationJson())
        }
        body([
                amount: 10.0
        ])
    }

    response {
        status 200
    }
}
