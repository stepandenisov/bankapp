package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "External transfer success"

    request {
        method POST()
        url("/transfer/external")
        headers {
            contentType(applicationJson())
        }
        body(
                userId: 1L,
                fromAccount: 1L,
                toCurrency: "CNY",
                amount: 100.0
        )
    }

    response {
        status 200
    }
}
