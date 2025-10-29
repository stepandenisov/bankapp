package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Пополнение счёта"

    request {
        method POST()
        url("/accounts/2/top-up")
        headers {
            contentType(applicationJson())
        }
        body([
                amount: 9000.0
        ])
    }

    response {
        status 200
    }
}
